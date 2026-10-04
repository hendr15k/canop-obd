package com.canopobd.data.domain

/**
 * Zündungs-/Verbrennungsanalyse für Opel Astra J 1.4 Turbo (A14NET).
 *
 * Der A14NET nutzt eine Bosch ME17.9.22 mit Klopfregelung je Zylinder.
 * Typische Auffälligkeiten:
 * - Klopfrücknahme (Knock Retard) unter Last bei 95 RON statt 98
 * - Übermäßig späte Zündung bei warmem Motor (Wirkungsgradverlust)
 * - Zündaussetzer (P030x) durch alternde Zündspulenleiste oder Kerzen
 * - Klopfsensorfehler (P0325/P0327/P0328)
 *
 * Eingangsgrößen: Zündzeitpunkt (PID 010E), Drehzahl, Last, Kühlmittel,
 * Klopfrücknahme sowie aktive DTCs.
 */
class IgnitionAnalyzer {

    enum class CombustionHealth(val label: String, val severity: Int) {
        HEALTHY("Gesund", 0),
        DEGRADED("Vermindert", 1),
        CRITICAL("Kritisch", 2),
        UNKNOWN("Unbekannt", -1)
    }

    enum class IgnitionIssue(val label: String, val description: String) {
        KNOCK_RETARD("Klopfrücknahme", "ECU nimmt Zündung wegen Klopfen zurück"),
        MISFIRE("Zündaussetzer", "Verbrennungsaussetzer erkannt (DTC P030x)"),
        KNOCK_SENSOR_FAULT("Klopfsensor", "Klopfsensor meldet Fehler (DTC P032x)"),
        RETARDED_TIMING("Späte Zündung", "Zündzeitpunkt unplausibel spät bei warmem Motor"),
        ADVANCED_TIMING("Frühe Zündung", "Zündzeitpunkt unplausibel früh unter Last")
    }

    data class IgnitionInput(
        val timingAdvance: Double,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val knockRetard: Double = 0.0,
        val activeDTCs: List<String> = emptyList()
    )

    data class IgnitionAnalysis(
        val health: CombustionHealth,
        val healthScore: Int,
        val detectedIssues: List<IgnitionIssue>,
        val knockRetardDeg: Double,
        val timingAdvanceDeg: Double,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        private const val KNOCK_WARN_DEG = 3.0
        private const val KNOCK_CRITICAL_DEG = 6.0
        private const val RETARDED_LIMIT_DEG = -5.0
        private const val ADVANCED_LIMIT_DEG = 45.0
        private const val WARM_COOLANT_C = 80.0
        private const val HIGH_LOAD_PCT = 70.0
        private const val SCORE_START = 100
        private const val PENALTY_KNOCK_WARN = 15
        private const val PENALTY_KNOCK_CRITICAL = 55
        private const val PENALTY_TIMING = 25
        private const val PENALTY_DTC = 40
        private const val SCORE_DEGRADED_MAX = 79
        private const val SCORE_CRITICAL_MAX = 49
    }

    fun analyze(input: IgnitionInput): IgnitionAnalysis {
        if (input.rpm <= 0.0) {
            return IgnitionAnalysis(
                health = CombustionHealth.UNKNOWN,
                healthScore = 0,
                detectedIssues = emptyList(),
                knockRetardDeg = input.knockRetard,
                timingAdvanceDeg = 0.0,
                diagnosis = "Keine Zündungsdaten verfügbar (Motor steht).",
                recommendation = "Motor starten und OBD-Verbindung prüfen."
            )
        }

        val issues = mutableListOf<IgnitionIssue>()
        var score = SCORE_START
        score = applyKnockPenalty(input, issues, score)
        score = applyTimingPenalty(input, issues, score)
        score = applyDtcPenalty(input, issues, score)

        score = score.coerceIn(0, SCORE_START)
        val health = classifyHealth(issues, input, score)

        return IgnitionAnalysis(
            health = health,
            healthScore = score,
            detectedIssues = issues,
            knockRetardDeg = input.knockRetard,
            timingAdvanceDeg = input.timingAdvance,
            diagnosis = generateDiagnosis(health, issues, input),
            recommendation = generateRecommendation(health, issues)
        )
    }

    private fun applyKnockPenalty(
        input: IgnitionInput,
        issues: MutableList<IgnitionIssue>,
        score: Int
    ): Int {
        var result = score
        if (input.knockRetard >= KNOCK_CRITICAL_DEG) {
            issues.add(IgnitionIssue.KNOCK_RETARD)
            result -= PENALTY_KNOCK_CRITICAL
        } else if (input.knockRetard >= KNOCK_WARN_DEG) {
            issues.add(IgnitionIssue.KNOCK_RETARD)
            result -= PENALTY_KNOCK_WARN
        }
        return result
    }

    private fun applyTimingPenalty(
        input: IgnitionInput,
        issues: MutableList<IgnitionIssue>,
        score: Int
    ): Int {
        var result = score
        val warm = input.coolantTemp >= WARM_COOLANT_C
        val loaded = input.engineLoad >= HIGH_LOAD_PCT
        if (warm && input.timingAdvance < RETARDED_LIMIT_DEG) {
            issues.add(IgnitionIssue.RETARDED_TIMING)
            result -= PENALTY_TIMING
        }
        if (loaded && input.timingAdvance > ADVANCED_LIMIT_DEG) {
            issues.add(IgnitionIssue.ADVANCED_TIMING)
            result -= PENALTY_TIMING
        }
        return result
    }

    private fun applyDtcPenalty(
        input: IgnitionInput,
        issues: MutableList<IgnitionIssue>,
        score: Int
    ): Int {
        var result = score
        for (code in input.activeDTCs) {
            val upper = code.uppercase()
            when {
                upper.startsWith("P030") -> {
                    if (!issues.contains(IgnitionIssue.MISFIRE)) {
                        issues.add(IgnitionIssue.MISFIRE)
                        result -= PENALTY_DTC
                    }
                }
                upper.startsWith("P032") -> {
                    if (!issues.contains(IgnitionIssue.KNOCK_SENSOR_FAULT)) {
                        issues.add(IgnitionIssue.KNOCK_SENSOR_FAULT)
                        result -= PENALTY_DTC
                    }
                }
            }
        }
        return result
    }

    private fun classifyHealth(
        issues: List<IgnitionIssue>,
        input: IgnitionInput,
        score: Int
    ): CombustionHealth = when {
        issues.contains(IgnitionIssue.MISFIRE) -> CombustionHealth.CRITICAL
        input.knockRetard >= KNOCK_CRITICAL_DEG -> CombustionHealth.CRITICAL
        score <= SCORE_CRITICAL_MAX -> CombustionHealth.CRITICAL
        score <= SCORE_DEGRADED_MAX -> CombustionHealth.DEGRADED
        issues.isNotEmpty() -> CombustionHealth.DEGRADED
        else -> CombustionHealth.HEALTHY
    }

    private fun generateDiagnosis(
        health: CombustionHealth,
        issues: List<IgnitionIssue>,
        input: IgnitionInput
    ): String {
        if (health == CombustionHealth.HEALTHY) {
            return "Zündung plausibel (${input.timingAdvance.toInt()}° bei " +
                "${input.rpm.toInt()} U/min), keine Klopfrücknahme."
        }
        return "Zündung ${health.label.lowercase()}: " +
            issues.joinToString(", ") { it.label } +
            " (Zündzeitpunkt ${input.timingAdvance.toInt()}°, " +
            "Klopfrücknahme ${input.knockRetard.toInt()}°)."
    }

    private fun generateRecommendation(
        health: CombustionHealth,
        issues: List<IgnitionIssue>
    ): String {
        if (health == CombustionHealth.HEALTHY) return "Keine Maßnahme erforderlich."
        val tips = mutableListOf<String>()
        if (issues.contains(IgnitionIssue.MISFIRE)) {
            tips.add("Zündkerzen und Zündspulenleiste prüfen")
        }
        if (issues.contains(IgnitionIssue.KNOCK_RETARD)) {
            tips.add("98 RON tanken und Ladelufttemperatur prüfen")
        }
        if (issues.contains(IgnitionIssue.KNOCK_SENSOR_FAULT)) {
            tips.add("Klopfsensor und Verkabelung prüfen")
        }
        if (issues.contains(IgnitionIssue.RETARDED_TIMING) ||
            issues.contains(IgnitionIssue.ADVANCED_TIMING)
        ) {
            tips.add("Zündkennfeld per Diagnose prüfen lassen")
        }
        if (tips.isEmpty()) tips.add("Verbrennung per Diagnose prüfen lassen")
        return tips.joinToString("; ") + "."
    }
}
