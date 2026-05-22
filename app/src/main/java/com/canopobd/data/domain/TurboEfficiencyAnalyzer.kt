package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

/**
 * Turbo-Effizienz-Analyse fuer BorgWarner KP39 (A14NET)
 *
 * Der A14NET verwendet einen fest-geometrierten Turbo (BorgWarner KP39)
 * mit pneumatisch geregeltem Wastegate.
 *
 * Technische Daten des Turbos:
 * - Typ: Single-Scroll, fixed geometry
 * - Wastegate: Pneumatisch, Vakuum-gesteuert
 * - Maximaler Ladedruck: 1.0 bar (Normal), 1.2 bar (Overboost)
 * - Overboost-Dauer: max. 10 Sekunden
 * - Max. Turbo-Drehzahl: ~200.000 rpm
 *
 * Die Effizienz-Berechnung basiert auf:
 * - Boost Ist vs Soll (PID 220002 vs 220003)
 * - Wastegate-Duty-Cycle-Verhalten
 * - Turbo-Drehzahl (falls verfuegbar)
 * - EGT-Trends (Abgastemperatur als Last-Indikator)
 *
 * Effizienz-Bereich:
 * - OPTIMAL: Volle Leistung, schnelles Ansprechverhalten
 * - GOOD: Normale Leistung, leichtes Alterungsverhalten
 * - DEGRADED: Spuerbarer Leistungsverlust
 * - FAILING: Kritischer Turbo-Verschleiss
 */
class TurboEfficiencyAnalyzer(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Turbo-Effizienz-Status
     */
    enum class TurboEfficiency(val label: String, val colorHex: Long, val severity: Int) {
        OPTIMAL("Optimal", 0xFF00FF88, 0),
        GOOD("Gut", 0xFF88FF44, 0),
        DEGRADED("Vermindert", 0xFFFFE066, 1),
        FAILING("Versagend", 0xFFFF4444, 2)
    }

    /**
     * Eingabedaten fuer die Turbo-Analyse
     */
    data class TurboInput(
        val boostActualBar: Double,
        val boostTargetBar: Double,
        val wastegateDuty: Double,
        val turboRpm: Double = 0.0,
        val egtBank1: Double = 0.0,
        val egtBank2: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val throttle: Double = 0.0,
        val chargeAirTemp: Double = 0.0,
        val intakeTemp: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val boostPressureKpa: Double = 0.0,
        val wastegateControl: Double = 0.0,
        val totalKm: Double = 0.0
    )

    /**
     * Ergebnis der Turbo-Analyse
     */
    data class TurboEfficiencyAnalysis(
        val efficiency: TurboEfficiency,
        val healthScore: Int,
        val boostEfficiency: Double,
        val responseTimeScore: Int,
        val wastegateHealthScore: Int,
        val egtTrendScore: Int,
        val intercoolerEfficiency: Double,
        val boostDeviation: Double,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        // Boost-Zielwerte A14NET (bar)
        private const val TARGET_BOOST_IDLE = 0.0
        private const val TARGET_BOOST_CRUISE = 0.3
        private const val TARGET_BOOST_MAX = 0.7
        private const val OVERBOOST_MAX = 1.2

        // Turbo-Drehzahl-Schwellenwerte (rpm)
        private const val TURBO_RPM_OPTIMAL_MIN = 80000.0
        private const val TURBO_RPM_OPTIMAL_MAX = 160000.0
        private const val TURBO_RPM_WARNING = 180000.0

        // Wastegate-Duty-Cycle-Schwellenwerte (%)
        private const val WG_DUTY_IDLE_MIN = 70.0
        private const val WG_DUTY_WOT_MAX = 50.0
        private const val WG_DUTY_STUCK_OPEN = 90.0
        private const val WG_DUTY_STUCK_CLOSED = 10.0

        // EGT-Schwellenwerte (°C)
        private const val EGT_NORMAL_MAX = 700.0
        private const val EGT_WARNING = 800.0
        private const val EGT_CRITICAL = 850.0

        // Ladeluftkuehler
        private const val INTERCOOLER_EFFICIENCY_GOOD = 70.0
        private const val INTERCOOLER_EFFICIENCY_WARNING = 50.0

        // Gewichtung (Summe = 100)
        private const val WEIGHT_BOOST_EFFICIENCY = 35
        private const val WEIGHT_WASTEGATE = 25
        private const val WEIGHT_EGT = 20
        private const val WEIGHT_TURBO_RPM = 20
    }

    /**
     * Fuehrt eine vollstaendige Turbo-Effizienz-Analyse durch
     */
    fun analyze(input: TurboInput): TurboEfficiencyAnalysis {
        // 1. Boost-Effizienz (Ist vs Soll)
        val (boostEfficiency, boostDeviation) = calculateBoostEfficiency(
            input.boostActualBar, input.boostTargetBar
        )

        // 2. Ansprechverhalten (basierend auf Boost-Aenderungsrate)
        val responseScore = evaluateBoostResponse(input)

        // 3. Wastegate-Gesundheit
        val wgScore = evaluateWastegate(input.wastegateDuty, input.boostActualBar, input.boostTargetBar)

        // 4. EGT-Trend
        val egtScore = evaluateEGT(input.egtBank1, input.egtBank2, input.boostActualBar)

        // 5. Turbo-RPM-Bewertung
        val turboRpmScore = evaluateTurboRPM(input.turboRpm)

        // 6. Ladeluftkuehler-Effizienz
        val intercoolerEfficiency = calculateIntercoolerEfficiency(
            input.chargeAirTemp, input.intakeTemp
        )

        // Gesamtbewertung
        val rawScore = (boostEfficiency.toInt() * WEIGHT_BOOST_EFFICIENCY +
                wgScore * WEIGHT_WASTEGATE +
                egtScore * WEIGHT_EGT +
                turboRpmScore * WEIGHT_TURBO_RPM) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)

        val efficiency = determineEfficiency(adjustedScore, input)
        val diagnosis = generateDiagnosis(efficiency, input, boostDeviation, intercoolerEfficiency)
        val recommendation = generateRecommendation(efficiency, input)

        return TurboEfficiencyAnalysis(
            efficiency = efficiency,
            healthScore = adjustedScore,
            boostEfficiency = boostEfficiency,
            responseTimeScore = responseScore,
            wastegateHealthScore = wgScore,
            egtTrendScore = egtScore,
            intercoolerEfficiency = intercoolerEfficiency,
            boostDeviation = boostDeviation,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    /**
     * Berechnet Boost-Effizienz (Ist vs Soll)
     */
    private fun calculateBoostEfficiency(
        actualBar: Double,
        targetBar: Double
    ): Pair<Double, Double> {
        if (targetBar <= 0.01) {
            // Leerlauf oder kein Boost-Ziel
            return if (actualBar < 0.05) 100.0 to 0.0
            else 70.0 to 0.0
        }

        val deviation = ((actualBar - targetBar) / targetBar) * 100.0
        val efficiency = when {
            // Perfektreffer
            abs(deviation) < 5.0 -> 100.0
            // Leichte Abweichung
            abs(deviation) < 15.0 -> 90.0 - abs(deviation)
            // Moderate Abweichung
            abs(deviation) < 30.0 -> 75.0 - abs(deviation)
            // Starke Abweichung
            else -> (50.0 - abs(deviation) * 0.5).coerceAtLeast(0.0)
        }

        return efficiency to deviation
    }

    /**
     * Bewertet Ansprechverhalten des Turbos
     */
    private fun evaluateBoostResponse(input: TurboInput): Int {
        if (input.throttle < 30 || input.engineLoad < 30) {
            return 80  // Nicht genuegend Last fuer Bewertung
        }

        val loadBoostRatio = if (input.engineLoad > 0) {
            input.boostActualBar / (input.engineLoad / 100.0)
        } else 0.0

        // Bei Last sollte das Verhaeltis stabil sein
        return when {
            input.boostActualBar < 0.05 && input.throttle > 60 -> 30 // Kein Boost bei Last
            loadBoostRatio in 0.5..1.5 -> 95  // Gutes Verhaeltnis
            loadBoostRatio in 0.3..2.0 -> 75
            loadBoostRatio < 0.3 -> 40  // Zu wenig Boost
            else -> 50
        }
    }

    /**
     * Bewertet Wastegate-Zustand
     */
    private fun evaluateWastegate(duty: Double, actualBoost: Double, targetBoost: Double): Int {
        return when {
            // Wastegate fast immer offen
            duty > WG_DUTY_STUCK_OPEN -> 25
            // Wastegate fast immer geschlossen
            duty < WG_DUTY_STUCK_CLOSED -> 30
            // Leerlauf: Wastegate sollte offen sein
            targetBoost < 0.05 && duty < WG_DUTY_IDLE_MIN -> 50
            // Normaler Bereich
            duty in WG_DUTY_STUCK_CLOSED..WG_DUTY_STUCK_OPEN -> 95
            else -> 70
        }
    }

    /**
     * Bewertet EGT (Abgastemperatur)
     */
    private fun evaluateEGT(egtB1: Double, egtB2: Double, boost: Double): Int {
        val maxEGT = maxOf(egtB1, egtB2)

        return when {
            maxEGT <= 0 -> 70  // Keine Daten
            maxEGT > EGT_CRITICAL -> 15
            maxEGT > EGT_WARNING -> 40
            maxEGT > EGT_NORMAL_MAX -> 70
            // Bei Boost: EGT sollte nicht zu niedrig sein (kann auf lean burn hindeuten)
            boost > 0.5 && maxEGT < 400 -> 60
            else -> 95
        }
    }

    /**
     * Bewertet Turbo-Drehzahl
     */
    private fun evaluateTurboRPM(turboRpm: Double): Int {
        return when {
            turboRpm <= 0 -> 70  // Keine Daten verfuegbar
            turboRpm in TURBO_RPM_OPTIMAL_MIN..TURBO_RPM_OPTIMAL_MAX -> 100
            turboRpm < TURBO_RPM_OPTIMAL_MIN -> 80
            turboRpm > TURBO_RPM_WARNING -> 20
            turboRpm > TURBO_RPM_OPTIMAL_MAX -> 50
            else -> 70
        }
    }

    /**
     * Berechnet Ladeluftkuehler-Effizienz (%)
     */
    private fun calculateIntercoolerEfficiency(chargeAirTemp: Double, intakeTemp: Double): Double {
        if (intakeTemp <= 0 || chargeAirTemp <= 0) return 80.0 // Keine Daten

        val tempRise = chargeAirTemp - intakeTemp
        return when {
            tempRise <= 0 -> 100.0  // Ladung kaelter als Ansaugluft (optimal)
            tempRise > 30 -> 0.0    // Intercooler komplett ineffektiv
            else -> ((1.0 - tempRise / 30.0) * 100.0).coerceIn(0.0, 100.0)
        }
    }

    /**
     * Bestimmt den Effizienz-Status
     */
    private fun determineEfficiency(score: Int, input: TurboInput): TurboEfficiency {
        // Kritische Bedingungen ueberschreiben Score
        val isOverboost = input.boostActualBar > calibration.overboostBar
        val isUnderboost = input.boostActualBar < input.boostTargetBar * 0.5 && input.throttle > 50

        return when {
            isOverboost && input.boostActualBar > calibration.overboostMaxBar -> TurboEfficiency.FAILING
            isUnderboost -> TurboEfficiency.DEGRADED
            score >= 85 -> TurboEfficiency.OPTIMAL
            score >= 70 -> TurboEfficiency.GOOD
            score >= 45 -> TurboEfficiency.DEGRADED
            else -> TurboEfficiency.FAILING
        }
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        efficiency: TurboEfficiency,
        input: TurboInput,
        boostDeviation: Double,
        intercoolerEfficiency: Double
    ): String {
        return when (efficiency) {
            TurboEfficiency.OPTIMAL -> {
                "BorgWarner KP39 Turbo arbeitet optimal. " +
                        "Boost: ${"%.2f".format(input.boostActualBar)} bar " +
                        "(Soll: ${"%.2f".format(input.boostTargetBar)} bar). " +
                        "Ladeluftkuehler: ${intercoolerEfficiency.toInt()}%."
            }
            TurboEfficiency.GOOD -> {
                "Turbo funktioniert gut mit leichtem Alterungsverhalten. " +
                        "Boost-Abweichung: ${"%.1f".format(boostDeviation)}%."
            }
            TurboEfficiency.DEGRADED -> {
                val issues = mutableListOf<String>()
                if (input.boostActualBar < input.boostTargetBar * 0.7) {
                    issues.add("Unterladung")
                }
                if (intercoolerEfficiency < INTERCOOLER_EFFICIENCY_WARNING) {
                    issues.add("Ladeluftkuehler-Effizienz: ${intercoolerEfficiency.toInt()}%")
                }
                if (input.egtBank1 > EGT_WARNING) {
                    issues.add("EGT erhoeht: ${input.egtBank1.toInt()}°C")
                }
                val detail = if (issues.isNotEmpty()) issues.joinToString(", ") else "Leichter Leistungsverlust"
                "Turbo-Leistung vermindert: $detail."
            }
            TurboEfficiency.FAILING -> {
                "KRITISCH: Turbo-Versagen droht! " +
                        "Boost: ${"%.2f".format(input.boostActualBar)} bar " +
                        "(Soll: ${"%.2f".format(input.boostTargetBar)} bar). " +
                        "Sofortige Pruefung erforderlich."
            }
        }
    }

    /**
     * Generiert Empfehlung
     */
    private fun generateRecommendation(efficiency: TurboEfficiency, input: TurboInput): String {
        return when (efficiency) {
            TurboEfficiency.OPTIMAL -> {
                "Turbo-System in Ordnung. Regelmaessige Wartung: " +
                        "Oelversorgung des Turbos pruefen (Dexos2 5W-30). " +
                        "Luftfilter bei Intervall wechseln."
            }
            TurboEfficiency.GOOD -> {
                "Turbo zeigt altersbedingte Abnutzung. " +
                        "Bei ${input.totalKm.toInt()} km: Turbolader-Inspektion " +
                        "bei naechster Grosswartung empfohlen."
            }
            TurboEfficiency.DEGRADED -> {
                "Turbo-Inspektion dringend empfohlen. " +
                        "Ladedrucksensor, Wastegate und Unterdruckleitungen pruefen. " +
                        "Oelqualitaet und -stand pruefen - Turbo benoetigt schmierende Oelung."
            }
            TurboEfficiency.FAILING -> {
                "SOFORT Werkstatt aufsuchen! " +
                        "Turbo-Versagen kann zu Motorschaden fuehren. " +
                        "Nur bis zur naechsten Werkstatt weiterfahren."
            }
        }
    }
}
