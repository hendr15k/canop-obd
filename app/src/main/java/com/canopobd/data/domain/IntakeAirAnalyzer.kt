package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration

/**
 * Ansaugluft-/Ladeluft-Analyse für Opel Astra J 1.4 Turbo (A14NET).
 *
 * Bewertet die gesamte Ansaugstrecke: MAF-Sensor (PID 0110),
 * Ansauglufttemperatur (PID 010F) und Ladelufttemperatur nach
 * Ladeluftkühler (PID 0177). Typische A14NET-Auffälligkeiten:
 * - Verschmutzter MAF (zu wenig gemessene Luft bei hoher Last)
 * - Schwacher Ladeluftkühler (heisse Ladeluft unter Boost)
 * - Unplausible Temperaturwerte (Sensorfehler oder Kabelbruch)
 * - MAF-DTCs (P0101/P0102/P0103)
 */
class IntakeAirAnalyzer(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    enum class IntakeHealth(val label: String, val severity: Int) {
        HEALTHY("Gesund", 0),
        DEGRADED("Vermindert", 1),
        CRITICAL("Kritisch", 2),
        UNKNOWN("Unbekannt", -1)
    }

    enum class IntakeIssue(val label: String, val description: String) {
        LOW_AIRFLOW("Geringer Luftmassenstrom", "MAF meldet zu wenig Luft für Last/Drehzahl"),
        INTERCOOLER_WEAK("Ladeluftkühler schwach", "Ladeluft zu heiss unter Boost"),
        INTERCOOLER_WATCH("Ladeluft erhöht", "Ladeluftdelta auffällig, beobachten"),
        MAF_SENSOR_FAULT("MAF-Sensorfehler", "Luftmassensensor meldet Fehler (DTC P010x)"),
        TEMP_PLAUSIBILITY("Temperatur unplausibel", "Ansaugtemperatur ausserhalb Messbereich")
    }

    data class IntakeAirInput(
        val mafRate: Double,
        val intakeTemp: Double = 0.0,
        val chargeAirTemp: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val boostBar: Double = 0.0,
        val activeDTCs: List<String> = emptyList()
    )

    data class IntakeAirAnalysis(
        val health: IntakeHealth,
        val healthScore: Int,
        val detectedIssues: List<IntakeIssue>,
        val intercoolerDeltaC: Double,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        private const val TEMP_SENSOR_MIN_C = -39.0
        private const val TEMP_SENSOR_MAX_C = 120.0
        private const val HIGH_LOAD_PCT = 70.0
        private const val EXPECTED_MAF_PER_1000RPM = 12.0
        private const val RPM_TO_MAF_DIVISOR = 1000.0
        private const val LOW_FLOW_FRACTION = 0.4
        private const val CHARGE_DELTA_WARN_C = 25.0
        private const val BOOST_FOR_INTERCOOLER_CHECK_BAR = 0.3
        private const val SCORE_START = 100
        private const val PENALTY_FLOW = 25
        private const val PENALTY_INTERCOOLER = 25
        private const val PENALTY_INTERCOOLER_WATCH = 10
        private const val PENALTY_TEMP = 15
        private const val PENALTY_DTC = 35
        private const val SCORE_DEGRADED_MAX = 79
        private const val SCORE_CRITICAL_MAX = 49
    }

    fun analyze(input: IntakeAirInput): IntakeAirAnalysis {
        if (input.rpm <= 0.0) {
            return IntakeAirAnalysis(
                health = IntakeHealth.UNKNOWN,
                healthScore = 0,
                detectedIssues = emptyList(),
                intercoolerDeltaC = 0.0,
                diagnosis = "Keine Ansaugluftdaten verfügbar (Motor steht).",
                recommendation = "Motor starten und OBD-Verbindung prüfen."
            )
        }

        val issues = mutableListOf<IntakeIssue>()
        var score = SCORE_START
        score = applyTempPenalty(input, issues, score)
        score = applyFlowPenalty(input, issues, score)
        val delta = input.chargeAirTemp - input.intakeTemp
        score = applyIntercoolerPenalty(input, issues, delta, score)
        score = applyDtcPenalty(input, issues, score)

        score = score.coerceIn(0, SCORE_START)
        val health = classifyHealth(issues, score)

        return IntakeAirAnalysis(
            health = health,
            healthScore = score,
            detectedIssues = issues,
            intercoolerDeltaC = delta,
            diagnosis = generateDiagnosis(health, issues, input, delta),
            recommendation = generateRecommendation(health, issues)
        )
    }

    private fun applyTempPenalty(
        input: IntakeAirInput,
        issues: MutableList<IntakeIssue>,
        score: Int
    ): Int {
        var result = score
        if (input.intakeTemp < TEMP_SENSOR_MIN_C || input.intakeTemp > TEMP_SENSOR_MAX_C) {
            issues.add(IntakeIssue.TEMP_PLAUSIBILITY)
            result -= PENALTY_TEMP
        }
        return result
    }

    private fun applyFlowPenalty(
        input: IntakeAirInput,
        issues: MutableList<IntakeIssue>,
        score: Int
    ): Int {
        var result = score
        if (input.engineLoad >= HIGH_LOAD_PCT && input.rpm > 0) {
            val expectedMaf = input.rpm / RPM_TO_MAF_DIVISOR * EXPECTED_MAF_PER_1000RPM
            if (input.mafRate < expectedMaf * LOW_FLOW_FRACTION) {
                issues.add(IntakeIssue.LOW_AIRFLOW)
                result -= PENALTY_FLOW
            }
        }
        return result
    }

    private fun applyIntercoolerPenalty(
        input: IntakeAirInput,
        issues: MutableList<IntakeIssue>,
        delta: Double,
        score: Int
    ): Int {
        var result = score
        if (input.boostBar >= BOOST_FOR_INTERCOOLER_CHECK_BAR && delta > CHARGE_DELTA_WARN_C) {
            val limit = calibration.maxChargeAirTempC
            if (input.chargeAirTemp > limit || delta > CHARGE_DELTA_WARN_C * 2) {
                issues.add(IntakeIssue.INTERCOOLER_WEAK)
                result -= PENALTY_INTERCOOLER
            } else {
                issues.add(IntakeIssue.INTERCOOLER_WATCH)
                result -= PENALTY_INTERCOOLER_WATCH
            }
        }
        return result
    }

    private fun applyDtcPenalty(
        input: IntakeAirInput,
        issues: MutableList<IntakeIssue>,
        score: Int
    ): Int {
        var result = score
        for (code in input.activeDTCs) {
            if (code.uppercase().startsWith("P010")) {
                if (!issues.contains(IntakeIssue.MAF_SENSOR_FAULT)) {
                    issues.add(IntakeIssue.MAF_SENSOR_FAULT)
                    result -= PENALTY_DTC
                }
            }
        }
        return result
    }

    private fun classifyHealth(issues: List<IntakeIssue>, score: Int): IntakeHealth = when {
        score <= SCORE_CRITICAL_MAX -> IntakeHealth.CRITICAL
        score <= SCORE_DEGRADED_MAX -> IntakeHealth.DEGRADED
        issues.isNotEmpty() -> IntakeHealth.DEGRADED
        else -> IntakeHealth.HEALTHY
    }

    private fun generateDiagnosis(
        health: IntakeHealth,
        issues: List<IntakeIssue>,
        input: IntakeAirInput,
        delta: Double
    ): String {
        if (health == IntakeHealth.HEALTHY) {
            return "Ansaugstrecke plausibel (MAF ${input.mafRate.toInt()} g/s, " +
                "Ansaug ${input.intakeTemp.toInt()} °C, Ladeluft ${input.chargeAirTemp.toInt()} °C)."
        }
        return "Ansaugluft ${health.label.lowercase()}: " +
            issues.joinToString(", ") { it.label } +
            " (MAF ${input.mafRate.toInt()} g/s, Delta ${delta.toInt()} K)."
    }

    private fun generateRecommendation(
        health: IntakeHealth,
        issues: List<IntakeIssue>
    ): String {
        if (health == IntakeHealth.HEALTHY) return "Keine Maßnahme erforderlich."
        val tips = mutableListOf<String>()
        if (issues.contains(IntakeIssue.MAF_SENSOR_FAULT) ||
            issues.contains(IntakeIssue.LOW_AIRFLOW)
        ) {
            tips.add("MAF-Sensor reinigen/prüfen und Luftfilter kontrollieren")
        }
        if (issues.contains(IntakeIssue.INTERCOOLER_WEAK)) {
            tips.add("Ladeluftkühler und Verschlauchung auf Leckage prüfen")
        }
        if (issues.contains(IntakeIssue.TEMP_PLAUSIBILITY)) {
            tips.add("Temperatursensor und Verkabelung prüfen")
        }
        if (tips.isEmpty()) tips.add("Ansaugstrecke per Diagnose prüfen lassen")
        return tips.joinToString("; ") + "."
    }
}
