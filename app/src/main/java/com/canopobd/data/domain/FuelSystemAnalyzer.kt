package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

/**
 * Kraftstoffsystem-Analyse fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Der A14NET uses Direkteinspritzung (SIDI - Spark Ignition Direct Injection)
 * mit einem Hochdruckkraftstoffpumpen-System (HPFP).
 *
 * Komponenten:
 * - Niedrigdruck-Kraftstoffpumpe (Tankpumpe, ca. 4-6 bar)
 * - Hochdruck-Kraftstoffpumpe (HPFP, ca. 50-150 bar)
 * - Kraftstoffrail mit Drucksensor
 * - Direkteinspritzduesen (6 Loch, piezo-betrieben)
 * - Kraftstoffdruckregler
 *
 * Bekannte Probleme:
 * - P0087: Kraftstoffsystemdruck zu niedrig (HPFP-Verschleiss)
 * - P0088: Kraftstoffsystemdruck zu hoch (Regler defekt)
 * - P0190: Kraftstoffdrucksensorfehler
 * - P0171/P0172: System zu mager/fett (Injektor-Verschleiss)
 * - Kohlenstoffablagerungen auf Einspritzventilen (typisch bei Direkteinspritzung)
 *
 * Kraftstoffdruck-Werte A14NET:
 * - Leerlauf: ~50-70 bar (Direkteinspritzung)
 * - Vollast: ~100-150 bar
 * - Startup: ~5-6 bar (Vorlauf)
 */
class FuelSystemAnalyzer(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Gesundheitsstatus des Kraftstoffsystems
     */
    enum class FuelSystemHealth(val label: String, val colorHex: Long, val severity: Int) {
        HEALTHY("Gesund", 0xFF00FF88, 0),
        DEGRADED("Vermindert", 0xFFFFE066, 1),
        CRITICAL("Kritisch", 0xFFFF4444, 2),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * Erkannte Kraftstoffsystem-Probleme
     */
    enum class FuelSystemIssue(val label: String, val description: String) {
        HPFP_WEAR("HPFP-Verschleiss", "Hochdruck-Kraftstoffpumpe zeigt Verschleisserscheinungen"),
        INJECTOR_LEAK("Injektor-Leckage", "Einspritzventil undicht oder verstopft"),
        INJECTOR_CLOG("Injektor-Verkohlung", "Kohlenstoffablagerungen auf Einspritzventilen"),
        PRESSURE_REGULATOR("Druckregler", "Kraftstoffdruckregler funktioniert nicht korrekt"),
        LOW_PRESSURE("Niedrigdruck", "Tankpumpe liefert unzureichenden Vordruck"),
        SENSOR_FAULT("Sensorfehler", "Kraftstoffdrucksensor zeigt unplausible Werte")
    }

    /**
     * Eingabedaten fuer die Kraftstoffsystem-Analyse
     */
    data class FuelSystemInput(
        val activeDTCs: List<String>,
        val fuelRailPressureBar: Double,
        val fuelPressureLowBar: Double = 0.0,
        val stftB1: Double = 0.0,
        val ltftB1: Double = 0.0,
        val stftB2: Double = 0.0,
        val ltftB2: Double = 0.0,
        val mafRate: Double = 0.0,
        val mapPressure: Double = 0.0,
        val engineLoad: Double = 0.0,
        val rpm: Double = 0.0,
        val throttle: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val fuelLevel: Double = 0.0,
        val engineFuelRate: Double = 0.0,
        val o2Voltage: Double = 0.0,
        val totalKm: Double = 0.0
    )

    /**
     * Ergebnis der Kraftstoffsystem-Analyse
     */
    data class FuelSystemAnalysis(
        val health: FuelSystemHealth,
        val healthScore: Int,
        val detectedIssues: List<FuelSystemIssue>,
        val fuelRailPressureDeviation: Double,
        val trimHealthScore: Int,
        val injectorHealthScore: Int,
        val carbonBuildupRisk: Int,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        // Kraftstoffdruck-Schwellenwerte (Bar)
        private const val RAIL_PRESSURE_MIN_IDLE = 40.0
        private const val RAIL_PRESSURE_MAX_IDLE = 80.0
        private const val RAIL_PRESSURE_MIN_LOAD = 80.0
        private const val RAIL_PRESSURE_MAX_LOAD = 160.0
        private const val RAIL_PRESSURE_LOW_CRITICAL = 30.0
        private const val RAIL_PRESSURE_HIGH_CRITICAL = 170.0

        // Niedrigdruck-Pumpe
        private const val LOW_PRESSURE_MIN = 3.5
        private const val LOW_PRESSURE_MAX = 6.5

        // Trimm-Schwellenwerte
        private const val TRIM_WARNING = 6.0
        private const val TRIM_CRITICAL = 12.0

        // Kraftstoffverbrauch vs MAF (fuer Injektor-Erkennung)
        private const val FUEL_MAF_RATIO_IDLE = 0.015  // L/h pro g/s MAF

        // Kohlenstoffablagerungs-Risiko (basierend auf Laufleistung)
        private const val CARBON_BUILDUP_START_KM = 30000.0
        private const val CARBON_BUILDUP_HIGH_KM = 60000.0

        // Gewichtung (Summe = 100)
        private const val WEIGHT_DTC = 25
        private const val WEIGHT_RAIL_PRESSURE = 25
        private const val WEIGHT_TRIM = 30
        private const val WEIGHT_INJECTOR = 20
    }

    /**
     * Fuehrt eine vollstaendige Kraftstoffsystem-Analyse durch
     */
    fun analyze(input: FuelSystemInput): FuelSystemAnalysis {
        val detectedIssues = mutableListOf<FuelSystemIssue>()

        // 1. DTC-Bewertung
        val dtcResult = evaluateDTCs(input.activeDTCs)
        detectedIssues.addAll(dtcResult.second)

        // 2. Rail-Druck-Analyse
        val (railPressureScore, railDeviation) = evaluateRailPressure(
            input.fuelRailPressureBar, input.engineLoad, input.rpm
        )

        // 3. Kraftstofftrimm-Analyse
        val trimScore = evaluateTrims(input.stftB1, input.ltftB1, input.stftB2, input.ltftB2)

        // 4. Injektor-Gesundheit (basierend auf Trimm-Mustern und Kraftstoffverbrauch)
        val (injectorScore, injectorIssues) = evaluateInjectors(
            input.stftB1, input.ltftB1, input.stftB2, input.ltftB2,
            input.engineFuelRate, input.mafRate, input.rpm
        )
        detectedIssues.addAll(injectorIssues)

        // 5. Kohlenstoffablagerungs-Risiko
        val carbonRisk = evaluateCarbonBuildupRisk(input.totalKm, input.mafRate, input.mapPressure)

        // Gesamtbewertung
        val rawScore = (dtcResult.first * WEIGHT_DTC +
                railPressureScore * WEIGHT_RAIL_PRESSURE +
                trimScore * WEIGHT_TRIM +
                injectorScore * WEIGHT_INJECTOR) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)

        val health = determineHealth(adjustedScore, input.activeDTCs, detectedIssues)
        val diagnosis = generateDiagnosis(health, detectedIssues, input)
        val recommendation = generateRecommendation(health, detectedIssues, input)

        return FuelSystemAnalysis(
            health = health,
            healthScore = adjustedScore,
            detectedIssues = detectedIssues,
            fuelRailPressureDeviation = railDeviation,
            trimHealthScore = trimScore,
            injectorHealthScore = injectorScore,
            carbonBuildupRisk = carbonRisk,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    /**
     * Bewertet kraftstoffrelevante DTCs
     */
    private fun evaluateDTCs(dtcCodes: List<String>): Pair<Int, List<FuelSystemIssue>> {
        val issues = mutableListOf<FuelSystemIssue>()
        var penalty = 0

        for (code in dtcCodes) {
            val upper = code.uppercase()
            when {
                upper.contains("P0087") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(FuelSystemIssue.HPFP_WEAR)
                }
                upper.contains("P0088") -> {
                    penalty = penalty.coerceAtLeast(25)
                    issues.add(FuelSystemIssue.PRESSURE_REGULATOR)
                }
                upper.contains("P0190") || upper.contains("P0191") -> {
                    penalty = penalty.coerceAtLeast(15)
                    issues.add(FuelSystemIssue.SENSOR_FAULT)
                }
                upper.contains("P0171") || upper.contains("P0172") -> {
                    penalty = penalty.coerceAtLeast(10)
                    issues.add(FuelSystemIssue.INJECTOR_LEAK)
                }
                upper.contains("P0201") || upper.contains("P0202") ||
                        upper.contains("P0203") || upper.contains("P0204") -> {
                    penalty = penalty.coerceAtLeast(30)
                    issues.add(FuelSystemIssue.INJECTOR_LEAK)
                }
            }
        }

        val score = (100 - penalty).coerceAtLeast(0)
        return score to issues
    }

    /**
     * Bewertet Kraftstoffrail-Druck
     */
    @Suppress("UNUSED_PARAMETER")
    private fun evaluateRailPressure(
        pressureBar: Double,
        engineLoad: Double,
        rpm: Double
    ): Pair<Int, Double> {
        // Soll-Druck basierend auf Last bestimmen
        val targetPressure = when {
            engineLoad > 70 -> RAIL_PRESSURE_MIN_LOAD + (RAIL_PRESSURE_MAX_LOAD - RAIL_PRESSURE_MIN_LOAD) *
                    (engineLoad / 100.0)
            engineLoad > 30 -> RAIL_PRESSURE_MIN_IDLE + (RAIL_PRESSURE_MIN_LOAD - RAIL_PRESSURE_MIN_IDLE) *
                    (engineLoad / 70.0)
            else -> RAIL_PRESSURE_MIN_IDLE + 10.0 // Leerlauf: ca. 50-60 bar
        }

        val deviation = if (targetPressure > 0) {
            ((pressureBar - targetPressure) / targetPressure) * 100.0
        } else 0.0

        val absDeviation = abs(deviation)

        val score = when {
            // Kritischer Unterdruck
            pressureBar < RAIL_PRESSURE_LOW_CRITICAL -> 10
            // Kritischer Ueberdruck
            pressureBar > RAIL_PRESSURE_HIGH_CRITICAL -> 15
            // Deutliche Abweichung
            absDeviation > 30 -> 30
            // Moderate Abweichung
            absDeviation > 15 -> 60
            // Leichte Abweichung
            absDeviation > 8 -> 80
            // Normal
            else -> 100
        }

        return score to deviation
    }

    /**
     * Bewertet Kraftstofftrimm-Werte
     */
    private fun evaluateTrims(stftB1: Double, ltftB1: Double, stftB2: Double, ltftB2: Double): Int {
        val totalB1 = abs(stftB1 + ltftB1)
        val totalB2 = abs(stftB2 + ltftB2)
        val worstTotal = maxOf(totalB1, totalB2)

        // Asymmetrie zwischen Banken
        val bankAsymmetry = abs(totalB1 - totalB2)

        return when {
            worstTotal > TRIM_CRITICAL -> 25
            worstTotal > TRIM_WARNING -> {
                val base = 60
                val asymmetryPenalty = if (bankAsymmetry > 5) 10 else 0
                (base - asymmetryPenalty).coerceAtLeast(0)
            }
            worstTotal > TRIM_WARNING * 0.5 -> 85
            else -> 100
        }
    }

    /**
     * Bewertet Injektor-Gesundheit basierend auf Trimm-Mustern und Kraftstoffverbrauch
     */
    @Suppress("UNUSED_PARAMETER")
    private fun evaluateInjectors(
        stftB1: Double, ltftB1: Double,
        stftB2: Double, ltftB2: Double,
        fuelRate: Double, mafRate: Double, rpm: Double
    ): Pair<Int, List<FuelSystemIssue>> {
        val issues = mutableListOf<FuelSystemIssue>()
        var score = 100

        // Pruefe Kraftstoffverbrauch vs MAF-Verhaeltnis
        if (mafRate > 0 && fuelRate > 0) {
            val fuelMafRatio = fuelRate / mafRate
            if (fuelMafRatio > FUEL_MAF_RATIO_IDLE * 1.5 && rpm < 1500) {
                score -= 15
                issues.add(FuelSystemIssue.INJECTOR_LEAK)
            }
        }

        // Pruefe ob STFT ungewoehnliche Spruenge macht (Injektor-Tropfen)
        val totalB1 = abs(stftB1 + ltftB1)
        if (totalB1 > TRIM_WARNING) {
            // Fett-Tendenz (negative Trimmsumme) -> moegliches Injektor-Leck
            if (stftB1 + ltftB1 < -TRIM_WARNING) {
                score -= 10
                issues.add(FuelSystemIssue.INJECTOR_LEAK)
            }
            // Mager-Tendenz (positive Trimmsumme) -> moegliche Verstopfung
            if (stftB1 + ltftB1 > TRIM_WARNING) {
                score -= 10
                issues.add(FuelSystemIssue.INJECTOR_CLOG)
            }
        }

        return score.coerceAtLeast(0) to issues
    }

    /**
     * Bewertet Kohlenstoffablagerungs-Risiko (typisch fuer Direkteinspritzung)
     */
    private fun evaluateCarbonBuildupRisk(totalKm: Double, mafRate: Double, mapPressure: Double): Int {
        return when {
            totalKm < CARBON_BUILDUP_START_KM -> 10
            totalKm < CARBON_BUILDUP_HIGH_KM -> {
                ((totalKm - CARBON_BUILDUP_START_KM) /
                        (CARBON_BUILDUP_HIGH_KM - CARBON_BUILDUP_START_KM) * 50).toInt().coerceIn(10, 60)
            }
            // Bei hohem Kilometerstand: MAF/MAP-Korrelation pruefen
            totalKm >= CARBON_BUILDUP_HIGH_KM && mafRate > 0 && mapPressure > 0 -> {
                val correlation = mafRate / mapPressure
                val risk = if (correlation < 0.08) 70 else 40 + ((totalKm - CARBON_BUILDUP_HIGH_KM) / 50000.0 * 30).toInt().coerceAtMost(30)
                risk
            }
            else -> 40
        }
    }

    /**
     * Bestimmt Gesundheitsstatus
     */
    private fun determineHealth(
        score: Int,
        dtcCodes: List<String>,
        issues: List<FuelSystemIssue>
    ): FuelSystemHealth {
        val hasCriticalDTC = dtcCodes.any { it.uppercase().contains("P0087") || it.uppercase().contains("P0088") }
        val hasCriticalIssue = issues.any {
            it == FuelSystemIssue.HPFP_WEAR || it == FuelSystemIssue.PRESSURE_REGULATOR
        }

        return when {
            hasCriticalDTC || hasCriticalIssue -> FuelSystemHealth.CRITICAL
            issues.isNotEmpty() && score < 50 -> FuelSystemHealth.CRITICAL
            score >= 70 -> FuelSystemHealth.HEALTHY
            score >= 45 -> FuelSystemHealth.DEGRADED
            else -> FuelSystemHealth.CRITICAL
        }
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        health: FuelSystemHealth,
        issues: List<FuelSystemIssue>,
        input: FuelSystemInput
    ): String {
        return when (health) {
            FuelSystemHealth.HEALTHY -> {
                "Kraftstoffsystem funktioniert normal. " +
                        "Rail-Druck: ${"%.1f".format(input.fuelRailPressureBar)} bar."
            }
            FuelSystemHealth.DEGRADED -> {
                val issueNames = issues.map { it.label }
                "Kraftstoffsystem eingeschraenkt: ${issueNames.joinToString(", ")}. " +
                        "Rail-Druck: ${"%.1f".format(input.fuelRailPressureBar)} bar."
            }
            FuelSystemHealth.CRITICAL -> {
                val issueNames = issues.map { it.description }
                "KRITISCH: Kraftstoffsystemfehler - ${issueNames.joinToString("; ")}. " +
                        "Sofortige Pruefung erforderlich."
            }
            FuelSystemHealth.UNKNOWN -> {
                "Kraftstoffsystem-Status nicht bestimmbar. Weitere Daten erforderlich."
            }
        }
    }

    /**
     * Generiert Wartungsempfehlung
     */
    private fun generateRecommendation(
        health: FuelSystemHealth,
        issues: List<FuelSystemIssue>,
        input: FuelSystemInput
    ): String {
        return when (health) {
            FuelSystemHealth.HEALTHY -> {
                if (input.totalKm > CARBON_BUILDUP_START_KM) {
                    "Bei ${input.totalKm.toInt()} km: Reinigung der Einspritzduegen " +
                            "und Ansaugwege bei naechster Wartung empfohlen."
                } else {
                    "Keine Massnahmen erforderlich."
                }
            }
            FuelSystemHealth.DEGRADED -> {
                val actions = mutableListOf<String>()
                if (issues.contains(FuelSystemIssue.HPFP_WEAR)) {
                    actions.add("HPFP pruefen lassen")
                }
                if (issues.contains(FuelSystemIssue.INJECTOR_LEAK) ||
                    issues.contains(FuelSystemIssue.INJECTOR_CLOG)) {
                    actions.add("Einspritzduegen reinigen oder ersetzen")
                }
                if (issues.contains(FuelSystemIssue.PRESSURE_REGULATOR)) {
                    actions.add("Druckregler ersetzen")
                }
                "Empfohlen: ${actions.joinToString("; ")}. " +
                        "Kraftstofffilter pruefen."
            }
            FuelSystemHealth.CRITICAL -> {
                "SOFORT Werkstatt aufsuchen! " +
                        "Kraftstoffsystem erfordert sofortige Reparatur. " +
                        "Fahrten bis zur Werkstatt auf Minimum beschraenken."
            }
            FuelSystemHealth.UNKNOWN -> {
                "Kraftstoffdruck-Manometer-Messung bei Werkstatt durchfuehren. " +
                        "Datenlogger fuer laengere Analyse aktivieren."
            }
        }
    }
}
