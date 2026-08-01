package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

/**
 * EGT-Monitoring (Abgastemperatur) fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Fortschrittliche Abgastemperatur-Analyse:
 * - Trend-Analyse (steigend/stabil/fallend)
 * - Thermischer Stress-Berechnung
 * - Zylinder-Ausgleichspruefung (bei Doppel-EGT-Sensoren)
 *
 * Technische Hintergrund:
 * Der BorgWarner KP39 hat keinen direkten EGT-Sensor, aber der
 * ME17.9.24 ECU kann externe EGT-Daten via Mode 22 empfangen.
 *
 * EGT-Schwellenwerte A14NET:
 * - Normalbetrieb: 600-750°C
 * - Warnung: 800°C
 * - Kritisch: 850°C
 * - Schädlich: >900°C
 *
 * Thermischer Stress basiert auf der Beschleunigungsgleichung:
 * - Hohe EGT = hohe Verbrennungsrate = mehr Thermalfuss
 * - Hohe EGT over time = beschleunigte Komponentverschleiss
 */
class EGTMonitor(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * EGT-Status
     */
    enum class EGTStatus(val label: String, val colorHex: Long, val severity: Int) {
        NORMAL("Normal", 0xFF00FF88, 0),
        OPTIMAL("Optimal", 0xFF88FF44, 0),
        ELEVATED("Erhöht", 0xFFFFE066, 1),
        HIGH("Hoch", 0xFFFF8C00, 2),
        CRITICAL("Kritisch", 0xFFFF4444, 3),
        OVERHEAT("Ueberhitzung!", 0xFFFF0000, 4),
        NO_DATA("Keine Daten", 0xFFAAAAAA, -1)
    }

    /**
     * EGT-Trend
     */
    enum class EGTTrend(val label: String, val colorHex: Long) {
        RISING("Rising", 0xFFFF4444),
        STABLE("Stable", 0xFF00FF88),
        FALLING("Falling", 0xFF00AA00),
        VOLATILE("Volatile", 0xFFFF8800)
    }

    /**
     * Eingabedaten fuer die EGT-Analyse
     */
    data class EGTInput(
        val egtBank1: Double,
        val egtBank2: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val boostPressureKpa: Double = 0.0,
        val throttle: Double = 0.0,
        val recentEgtHistory: List<Double> = emptyList(),
        val recentEgt2History: List<Double> = emptyList(),
        val coolantTemp: Double = 0.0,
        val intakeTemp: Double = 0.0,
        val fuelRailPressure: Double = 0.0
    )

    /**
     * Ergebnis der EGT-Analyse
     */
    data class EGTAnalysis(
        val status: EGTStatus,
        val healthScore: Int,
        val trend: EGTTrend,
        val thermalStressIndex: Double,
        val thermalStressHours: Double,
        val cylinderBalance: Double,
        val estimatedEgtMax: Double,
        val egtDeviation: Double,
        val diagnosis: String,
        val recommendation: String,
        val warningFlags: List<String>
    )

    companion object {
        // EGT-Schwellenwerte (°C)
        private const val EGT_NORMAL_MAX = 750.0
        private const val EGT_WARNING = 800.0
        private const val EGT_CRITICAL = 850.0
        private const val EGT_OVERHEAT = 900.0

        // Trend-Berechnung
        private const val TREND_WINDOW_SIZE = 10
        private const val TREND_RISING_THRESHOLD = 15.0
        private const val TREND_FALLING_THRESHOLD = 10.0
        private const val TREND_VOLATILE_THRESHOLD = 25.0

        // Thermischer Stress
        private const val STRESS_TEMP_BASE = 700.0
        private const val STRESS_RATE_PER_10C = 0.1

        // Zylinder-Ausgleich (EGT1 vs EGT2)
        private const val CYLINDER_BALANCE_WARNING = 30.0
        private const val CYLINDER_BALANCE_CRITICAL = 50.0

        // Gewichtung
        private const val WEIGHT_CURRENT_TEMP = 30
        private const val WEIGHT_TREND = 25
        private const val WEIGHT_THERMAL_STRESS = 25
        private const val WEIGHT_CYLINDER_BALANCE = 20
    }

    /**
     * Fuehrt eine vollstaendige EGT-Analyse durch
     */
    fun analyze(input: EGTInput): EGTAnalysis {
        val warningFlags = mutableListOf<String>()

        // 1. Aktuelle Temperatur bewerten
        val (status, tempScore) = evaluateCurrentTemperature(input, warningFlags)

        // 2. Trend-Analyse
        val (trend, trendScore) = evaluateTrend(input)

        // 3. Thermischer Stress
        val (thermalStress, stressHours) = calculateThermalStress(input)
        val stressScore = evaluateThermalStressScore(thermalStress)

        // 4. Zylinder-Ausgleich (wenn zwei Sensoren)
        val (cylinderBalance, balanceScore) = evaluateCylinderBalance(input)

        // 5. Gesamtbewertung
        val rawScore = (tempScore * WEIGHT_CURRENT_TEMP +
            trendScore * WEIGHT_TREND +
            stressScore * WEIGHT_THERMAL_STRESS +
            balanceScore * WEIGHT_CYLINDER_BALANCE) / 100

        val healthScore = rawScore.coerceIn(0, 100)

        val diagnosis = generateDiagnosis(status, trend, thermalStress, cylinderBalance, input)
        val recommendation = generateRecommendation(status, trend, thermalStress, input)

        return EGTAnalysis(
            status = status,
            healthScore = healthScore,
            trend = trend,
            thermalStressIndex = thermalStress,
            thermalStressHours = stressHours,
            cylinderBalance = cylinderBalance,
            estimatedEgtMax = input.egtBank1,
            egtDeviation = if (input.egtBank2 > 0) abs(input.egtBank1 - input.egtBank2) else 0.0,
            diagnosis = diagnosis,
            recommendation = recommendation,
            warningFlags = warningFlags
        )
    }

    /**
     * Bewertet die aktuelle EGT
     */
    private fun evaluateCurrentTemperature(
        input: EGTInput,
        warnings: MutableList<String>
    ): Pair<EGTStatus, Int> {
        if (input.egtBank1 <= 0) return EGTStatus.NO_DATA to 70

        val score = when {
            input.egtBank1 <= EGT_NORMAL_MAX -> 100
            input.egtBank1 <= EGT_WARNING -> {
                warnings.add("EGT leicht erhöht: ${"%.0f".format(input.egtBank1)}°C")
                80
            }
            input.egtBank1 <= EGT_CRITICAL -> {
                warnings.add("EGT hoch: ${"%.0f".format(input.egtBank1)}°C")
                50
            }
            input.egtBank1 <= EGT_OVERHEAT -> {
                warnings.add("EGT kritisch: ${"%.0f".format(input.egtBank1)}°C")
                20
            }
            else -> {
                warnings.add("EGT UEBERHITZUNG: ${"%.0f".format(input.egtBank1)}°C")
                5
            }
        }

        val status = when {
            input.egtBank1 <= EGT_NORMAL_MAX -> EGTStatus.NORMAL
            input.egtBank1 <= EGT_WARNING -> EGTStatus.ELEVATED
            input.egtBank1 <= EGT_CRITICAL -> EGTStatus.HIGH
            input.egtBank1 <= EGT_OVERHEAT -> EGTStatus.CRITICAL
            else -> EGTStatus.OVERHEAT
        }

        return status to score
    }

    /**
     * Bewertet den EGT-Trend
     */
    private fun evaluateTrend(input: EGTInput): Pair<EGTTrend, Int> {
        if (input.recentEgtHistory.size < TREND_WINDOW_SIZE) {
            return EGTTrend.STABLE to 70
        }

        val recent = input.recentEgtHistory.takeLast(TREND_WINDOW_SIZE)
        val firstHalf = recent.take(TREND_WINDOW_SIZE / 2)
        val secondHalf = recent.drop(TREND_WINDOW_SIZE / 2)

        val avgFirst = firstHalf.average()
        val avgSecond = secondHalf.average()
        val change = avgSecond - avgFirst

        val (trend, score) = when {
            abs(change) > TREND_VOLATILE_THRESHOLD -> {
                EGTTrend.VOLATILE to 40
            }
            change > TREND_RISING_THRESHOLD -> {
                EGTTrend.RISING to 30
            }
            change < -TREND_FALLING_THRESHOLD -> {
                EGTTrend.FALLING to 80
            }
            else -> {
                EGTTrend.STABLE to 100
            }
        }

        return trend to score
    }

    /**
     * Berechnet den thermischen Stress-Index
     *
     * Basiert auf der Anzahl von "Stress-Einheiten" die das Oel
     * und andere Komponenten überhitzt wurden.
     * Formel: Integral der EGT-Überschreitung über die Zeit
     */
    private fun calculateThermalStress(input: EGTInput): Pair<Double, Double> {
        if (input.recentEgtHistory.isEmpty()) return 0.0 to 0.0

        var stressUnits = 0.0
        var totalTimeHours = 0.0

        for (temp in input.recentEgtHistory) {
            if (temp > STRESS_TEMP_BASE) {
                val excess = temp - STRESS_TEMP_BASE
                stressUnits += excess * STRESS_RATE_PER_10C
                totalTimeHours += (30.0 / 3600.0) // Angenommen: 30 Sekunden pro Messung
            }
        }

        return stressUnits to totalTimeHours
    }

    /**
     * Bewertet den thermischen Stress
     */
    private fun evaluateThermalStressScore(stressIndex: Double): Int {
        return when {
            stressIndex < 10 -> 100
            stressIndex < 50 -> 75
            stressIndex < 100 -> 50
            stressIndex < 200 -> 25
            else -> 10
        }
    }

    /**
     * Bewertet Zylinder-Ausgleich (EGT1 vs EGT2)
     *
     * Gleiche Zylinder-Temperatures = gesunder Zylinder-Ausgleich.
     * Große Unterschiede deuten auf:
     * - Verstopfte Kraftstoffeinspritzung
     * - Defekte Zündkerzen
     * - Kompressionsverlust
     */
    private fun evaluateCylinderBalance(
        input: EGTInput
    ): Pair<Double, Int> {
        if (input.egtBank2 <= 0 || input.recentEgt2History.isEmpty()) {
            return 0.0 to 100
        }

        val diff = abs(input.egtBank1 - input.egtBank2)

        val score = when {
            diff < CYLINDER_BALANCE_WARNING -> 100
            diff < CYLINDER_BALANCE_CRITICAL -> {
                100 - ((diff - CYLINDER_BALANCE_WARNING) / (CYLINDER_BALANCE_CRITICAL - CYLINDER_BALANCE_WARNING) * 40).toInt()
            }
            else -> 20
        }

        return diff to score
    }

    /**
     * Generiert Diagnose
     */
    private fun generateDiagnosis(
        status: EGTStatus,
        trend: EGTTrend,
        thermalStress: Double,
        cylinderBalance: Double,
        input: EGTInput
    ): String {
        val baseInfo = "EGT: ${"%.0f".format(input.egtBank1)}°C. Trend: ${trend.label}. Stress: ${"%.0f".format(thermalStress)} Einheiten."
        val balanceInfo = if (cylinderBalance > CYLINDER_BALANCE_WARNING) " Zylinder-Differenz: ${"%.0f".format(cylinderBalance)}°C." else ""

        return when (status) {
            EGTStatus.NORMAL, EGTStatus.OPTIMAL -> {
                "Abgastemperatur normal. $baseInfo$balanceInfo"
            }
            EGTStatus.ELEVATED -> {
                "EGT leicht erhöht. $baseInfo$balanceInfo. Vielleicht höhere Fahrstufe."
            }
            EGTStatus.HIGH -> {
                "EGT hoch. $baseInfo$balanceInfo. Kühlung prüfen, evtl. Turbo-Leck."
            }
            EGTStatus.CRITICAL -> {
                "EGT kritisch hoch! $baseInfo$balanceInfo. Sofort reduzieren!"
            }
            EGTStatus.OVERHEAT -> {
                "EGT UEBERHITZUNG! $baseInfo$balanceInfo. Motor abschalten!"
            }
            EGTStatus.NO_DATA -> {
                "Keine EGT-Daten verfügbar."
            }
        }
    }

    /**
     * Generiert Empfehlung
     */
    private fun generateRecommendation(
        status: EGTStatus,
        trend: EGTTrend,
        thermalStress: Double,
        input: EGTInput
    ): String {
        val stressHint = if (thermalStress > 100) " Hoher thermischer Stress — Ölwechsel prüfen." else ""
        val trendHint = if (trend == EGTTrend.RISING) " EGT steigt — Last reduzieren!" else ""
        return when (status) {
            EGTStatus.NORMAL, EGTStatus.OPTIMAL -> {
                "EGT im Normalbereich. Regelmäßige Beobachtung.$stressHint"
            }
            EGTStatus.ELEVATED -> {
                "EGT beobachten. Bei ${input.boostPressureKpa.toInt()} kPa Boost: " +
                    "eventuell höhere Kühlmitteltemperatur erlaubt.$trendHint"
            }
            EGTStatus.HIGH -> {
                "EGT reduzieren. Nicht mit Volllast fahren. " +
                    "Kühlmittelstand und -temperatur prüfen.$stressHint$trendHint"
            }
            EGTStatus.CRITICAL -> {
                "EGT kritisch! Nicht mit Last fahren. " +
                    "Turbo und Kühlung sofort prüfen. " +
                    "Nur notwendige Fahrten mit reduzierter Last.$stressHint"
            }
            EGTStatus.OVERHEAT -> {
                "SOFORT anhalten! Motor abkühlen lassen. " +
                    "Nicht erneut starten bis < 700°C.$stressHint"
            }
            EGTStatus.NO_DATA -> {
                "EGT-Sensor oder -Daten nicht verfügbar."
            }
        }
    }

    /**
     * Hilfsfunktion: Berechnet thermischen Stress in "EGT-Stunden"
     *
     * Ein hilfreiches Maß für die gesamte Wartungsplanung.
     */
    fun calculateCumulativeStress(stressIndex: Double): Double {
        return stressIndex / 10.0 // Einfache Umrechnung
    }

    /**
     * Hilfsfunktion: Schätzt die nächste EGT-Schwelle
     */
    fun predictNextThreshold(currentEGT: Double, trend: EGTTrend): Double {
        return when (trend) {
            EGTTrend.RISING -> currentEGT + 25.0
            EGTTrend.STABLE -> currentEGT + 50.0
            EGTTrend.FALLING -> currentEGT - 25.0
            EGTTrend.VOLATILE -> currentEGT + 10.0
        }
    }

    /**
     * Hilfsfunktion: Bewertet ob EGT für Turbo-Gesundheit relevant ist
     */
    fun isTurboStressful(egt: Double, load: Double): Boolean {
        return egt > calibration.maxEgtC * 0.8 && load > 50
    }

    /**
     * Hilfsfunktion: Berechnet die empfohlene EGT-Reduktion
     */
    fun recommendEGTReduction(currentEGT: Double, targetEGT: Double = 750.0): String {
        val reductionNeeded = currentEGT - targetEGT
        return when {
            reductionNeeded < 20 -> "Leichte Reduktion empfohlen"
            reductionNeeded < 50 -> "Mittleres Lastreduktionsprogramm starten"
            reductionNeeded < 100 -> "Starke Lastreduktion - Turbo schützen!"
            else -> "SOFORT Last reduzieren - Turbo vor Überhitzung schützen!"
        }
    }
}
