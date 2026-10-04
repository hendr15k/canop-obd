package com.canopobd.data.domain

import kotlin.math.abs

/**
 * Drosselklappen-/Pedal-Analyse für Opel Astra J 1.4 Turbo (A14NET).
 *
 * Der A14NET nutzt Drive-by-Wire: Das Pedal (PID 0151) steuert die
 * Drosselklappe (PID 0111) über die ECU. Typische Auffälligkeiten:
 * - Pedal/Klappe-Korrelation bricht (DTC P2135/P2138)
 * - Klappe klemmt offen (ungewollte Beschleunigung)
 * - Leerlauf-Sägen durch verschmutzte Klappe oder Falschluft
 * - Stellglied-Fehler (DTC P0121/P0221)
 */
class ThrottleAnalyzer {

    enum class ThrottleHealth(val label: String, val severity: Int) {
        HEALTHY("Gesund", 0),
        DEGRADED("Vermindert", 1),
        CRITICAL("Kritisch", 2),
        UNKNOWN("Unbekannt", -1)
    }

    enum class ThrottleIssue(val label: String, val description: String) {
        PEDAL_THROTTLE_MISMATCH("Pedal/Klappe-Abweichung", "Klappe folgt dem Pedal nicht"),
        STUCK_THROTTLE("Klemmende Klappe", "Klappe offen ohne Pedalbetaetigung"),
        ACTUATOR_FAULT("Stellgliedfehler", "Drosselklappensteller meldet Fehler (DTC)"),
        IDLE_HUNTING("Leerlauf-Saegen", "Klappe unruhig im Leerlauf")
    }

    data class ThrottleInput(
        val throttle: Double,
        val acceleratorPedal: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val activeDTCs: List<String> = emptyList()
    )

    data class ThrottleAnalysis(
        val health: ThrottleHealth,
        val healthScore: Int,
        val detectedIssues: List<ThrottleIssue>,
        val pedalThrottleDeviation: Double,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        private const val MISMATCH_WARN_PCT = 15.0
        private const val STUCK_THROTTLE_PCT = 80.0
        private const val IDLE_RPM_MAX = 1000.0
        private const val IDLE_THROTTLE_MAX_PCT = 15.0
        private const val SCORE_START = 100
        private const val PENALTY_MISMATCH = 25
        private const val PENALTY_IDLE = 15
        private const val PENALTY_DTC = 35
        private const val PENALTY_STUCK = 60
        private const val SCORE_DEGRADED_MAX = 79
        private const val SCORE_CRITICAL_MAX = 49
    }

    fun analyze(input: ThrottleInput): ThrottleAnalysis {
        if (input.rpm <= 0.0) {
            return ThrottleAnalysis(
                health = ThrottleHealth.UNKNOWN,
                healthScore = 0,
                detectedIssues = emptyList(),
                pedalThrottleDeviation = 0.0,
                diagnosis = "Keine Drosselklappendaten verfügbar (Motor steht).",
                recommendation = "Motor starten und OBD-Verbindung prüfen."
            )
        }

        val issues = mutableListOf<ThrottleIssue>()
        var score = SCORE_START
        val deviation = abs(input.throttle - input.acceleratorPedal)
        score = applyCorrelationPenalty(input, issues, deviation, score)
        score = applyIdlePenalty(input, issues, score)
        score = applyDtcPenalty(input, issues, score)

        score = score.coerceIn(0, SCORE_START)
        val health = classifyHealth(issues, score)

        return ThrottleAnalysis(
            health = health,
            healthScore = score,
            detectedIssues = issues,
            pedalThrottleDeviation = deviation,
            diagnosis = generateDiagnosis(health, issues, input, deviation),
            recommendation = generateRecommendation(health, issues)
        )
    }

    private fun applyCorrelationPenalty(
        input: ThrottleInput,
        issues: MutableList<ThrottleIssue>,
        deviation: Double,
        score: Int
    ): Int {
        var result = score
        if (deviation >= MISMATCH_WARN_PCT) {
            issues.add(ThrottleIssue.PEDAL_THROTTLE_MISMATCH)
            result -= PENALTY_MISMATCH
        }
        if (input.throttle >= STUCK_THROTTLE_PCT && input.acceleratorPedal < MISMATCH_WARN_PCT) {
            if (!issues.contains(ThrottleIssue.STUCK_THROTTLE)) {
                issues.add(ThrottleIssue.STUCK_THROTTLE)
            }
            result -= PENALTY_STUCK
        }
        return result
    }

    private fun applyIdlePenalty(
        input: ThrottleInput,
        issues: MutableList<ThrottleIssue>,
        score: Int
    ): Int {
        var result = score
        if (input.rpm in 1.0..IDLE_RPM_MAX &&
            input.acceleratorPedal < 1.0 &&
            input.throttle > IDLE_THROTTLE_MAX_PCT
        ) {
            issues.add(ThrottleIssue.IDLE_HUNTING)
            result -= PENALTY_IDLE
        }
        return result
    }

    private fun applyDtcPenalty(
        input: ThrottleInput,
        issues: MutableList<ThrottleIssue>,
        score: Int
    ): Int {
        var result = score
        for (code in input.activeDTCs) {
            val upper = code.uppercase()
            if (upper.startsWith("P012") || upper.startsWith("P022") || upper.startsWith("P213")) {
                if (!issues.contains(ThrottleIssue.ACTUATOR_FAULT)) {
                    issues.add(ThrottleIssue.ACTUATOR_FAULT)
                    result -= PENALTY_DTC
                }
            }
        }
        return result
    }

    private fun classifyHealth(issues: List<ThrottleIssue>, score: Int): ThrottleHealth = when {
        issues.contains(ThrottleIssue.STUCK_THROTTLE) -> ThrottleHealth.CRITICAL
        score <= SCORE_CRITICAL_MAX -> ThrottleHealth.CRITICAL
        score <= SCORE_DEGRADED_MAX -> ThrottleHealth.DEGRADED
        issues.isNotEmpty() -> ThrottleHealth.DEGRADED
        else -> ThrottleHealth.HEALTHY
    }

    private fun generateDiagnosis(
        health: ThrottleHealth,
        issues: List<ThrottleIssue>,
        input: ThrottleInput,
        deviation: Double
    ): String {
        if (health == ThrottleHealth.HEALTHY) {
            return "Drosselklappe folgt dem Pedal " +
                "(Klappe ${input.throttle.toInt()} %, Pedal ${input.acceleratorPedal.toInt()} %)."
        }
        return "Drosselklappe ${health.label.lowercase()}: " +
            issues.joinToString(", ") { it.label } +
            " (Abweichung ${deviation.toInt()} Prozentpunkte)."
    }

    private fun generateRecommendation(
        health: ThrottleHealth,
        issues: List<ThrottleIssue>
    ): String {
        if (health == ThrottleHealth.HEALTHY) return "Keine Maßnahme erforderlich."
        val tips = mutableListOf<String>()
        if (issues.contains(ThrottleIssue.STUCK_THROTTLE)) {
            tips.add("SOFORT anhalten und Klappe prüfen lassen (Sicherheitsrisiko)")
        }
        if (issues.contains(ThrottleIssue.ACTUATOR_FAULT)) {
            tips.add("Stellglied und Stecker (P012x/P022x/P213x) prüfen")
        }
        if (issues.contains(ThrottleIssue.IDLE_HUNTING)) {
            tips.add("Drosselklappe reinigen und Falschluft suchen")
        }
        if (issues.contains(ThrottleIssue.PEDAL_THROTTLE_MISMATCH)) {
            tips.add("Pedalwertgeber und Klappenpoti per Diagnose abgleichen")
        }
        if (tips.isEmpty()) tips.add("Drosselklappe per Diagnose prüfen lassen")
        return tips.joinToString("; ") + "."
    }
}
