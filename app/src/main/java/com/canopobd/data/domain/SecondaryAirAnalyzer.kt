package com.canopobd.data.domain

class SecondaryAirAnalyzer {

    data class SAIInput(
        val saActive: Boolean = false,
        val engineRpm: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val intakeTemp: Double = 0.0,
        val o2VoltageB1S1: Double = 0.0,
        val o2VoltageB1S2: Double = 0.0,
        val engineRuntimeSeconds: Double = 0.0,
        val activeDTCs: List<String> = emptyList(),
        val totalKm: Double = 0.0
    )

    data class SAIAnalysis(
        val status: com.canopobd.data.model.SAIStatus,
        val healthScore: Int,
        val operationPlausibility: Boolean,
        val leanSwingDetected: Boolean,
        val diagnosis: String,
        val recommendation: String
    )

    enum class SAIIssue(val label: String, val description: String) {
        VALVE_STUCK_CLOSED("Ventil klemmt", "SAI-Ventil öffnet nicht"),
        VALVE_STUCK_OPEN("Ventil offen", "SAI-Ventil schliesst nicht"),
        PUMP_FAULT("Pumpenfehler", "Luftpumpe defekt"),
        RELAY_FAULT("Relaisfehler", "SAI-Relais defekt"),
        PLUMBING_LEAK("Leckage", "SAI-Druckleitung undicht"),
        INCORRECT_TIMING("Falsches Timing", "SAI-Aktivierung zur falschen Zeit")
    }

    companion object {
        private const val SAI_MAX_OPERATION_SECONDS = 120L
        private const val SAI_MIN_OPERATION_SECONDS = 15L
        private const val SAI_MAX_COOLANT_TEMP = 65.0
        private const val SAI_O2_LEAN_THRESHOLD = 0.3
        private const val SAI_O2_RICH_RESPONSE = 0.8
        private const val SAI_RUNTIME_MAX_FOR_ACTIVE = 180.0

        private const val WEIGHT_DTC = 40
        private const val WEIGHT_TIMING = 30
        private const val WEIGHT_O2_RESPONSE = 30
    }

    fun analyze(input: SAIInput): SAIAnalysis {
        val issues = mutableListOf<SAIIssue>()

        val dtcScore = evaluateDTCs(input.activeDTCs, issues)
        val timingScore = evaluateTiming(input, issues)
        val o2Score = evaluateO2Response(input, issues)

        val rawScore = (dtcScore * WEIGHT_DTC +
            timingScore * WEIGHT_TIMING +
            o2Score * WEIGHT_O2_RESPONSE) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)
        val operationTime = estimateOperationTime(input)
        val plausibility = checkOperationPlausibility(input)
        val leanSwing = detectLeanSwing(input)

        val status = com.canopobd.data.model.SAIStatus(
            isActive = input.saActive,
            operationTimeSeconds = operationTime,
            healthScore = adjustedScore
        )

        val diagnosis = generateDiagnosis(status, issues, input)
        val recommendation = generateRecommendation(status, issues, input)

        return SAIAnalysis(
            status = status,
            healthScore = adjustedScore,
            operationPlausibility = plausibility,
            leanSwingDetected = leanSwing,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    private fun evaluateDTCs(dtcCodes: List<String>, issues: MutableList<SAIIssue>): Int {
        var penalty = 0
        for (code in dtcCodes) {
            val upper = code.uppercase()
            when {
                upper.contains("P0410") -> {
                    penalty = penalty.coerceAtLeast(35)
                    issues.add(SAIIssue.VALVE_STUCK_OPEN)
                }
                upper.contains("P0411") -> {
                    penalty = penalty.coerceAtLeast(30)
                    issues.add(SAIIssue.PUMP_FAULT)
                }
                upper.contains("P0412") -> {
                    penalty = penalty.coerceAtLeast(25)
                    issues.add(SAIIssue.VALVE_STUCK_CLOSED)
                }
                upper.contains("P0413") || upper.contains("P0414") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(SAIIssue.RELAY_FAULT)
                }
                upper.contains("P0415") || upper.contains("P0416") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(SAIIssue.PLUMBING_LEAK)
                }
            }
        }
        return (100 - penalty).coerceAtLeast(0)
    }

    private fun evaluateTiming(input: SAIInput, issues: MutableList<SAIIssue>): Int {
        val isColdStartPhase = input.engineRuntimeSeconds < SAI_RUNTIME_MAX_FOR_ACTIVE
        val isColdEngine = input.coolantTemp < SAI_MAX_COOLANT_TEMP

        if (input.saActive && !isColdStartPhase) {
            issues.add(SAIIssue.INCORRECT_TIMING)
            return 25
        }

        if (input.saActive && !isColdEngine) {
            issues.add(SAIIssue.INCORRECT_TIMING)
            return 30
        }

        if (!input.saActive && isColdStartPhase && isColdEngine && input.engineRpm > 1000) {
            return 50
        }

        return if (isColdStartPhase && isColdEngine) 90 else 100
    }

    @Suppress("UNUSED_PARAMETER")
    private fun evaluateO2Response(input: SAIInput, issues: MutableList<SAIIssue>): Int {
        if (!input.saActive) return 85

        if (input.o2VoltageB1S1 <= 0) return 70

        if (input.o2VoltageB1S1 > SAI_O2_RICH_RESPONSE && input.coolantTemp < SAI_MAX_COOLANT_TEMP) {
            return 100
        }

        if (input.o2VoltageB1S1 < SAI_O2_LEAN_THRESHOLD) {
            return 90
        }

        return 75
    }

    private fun estimateOperationTime(input: SAIInput): Long {
        if (!input.saActive) return 0L
        if (input.engineRuntimeSeconds > SAI_RUNTIME_MAX_FOR_ACTIVE) return 0L
        return input.engineRuntimeSeconds.toLong().coerceAtMost(SAI_MAX_OPERATION_SECONDS)
    }

    private fun checkOperationPlausibility(input: SAIInput): Boolean {
        if (input.saActive && input.coolantTemp > SAI_MAX_COOLANT_TEMP) return false
        if (input.saActive && input.engineRuntimeSeconds > SAI_RUNTIME_MAX_FOR_ACTIVE) return false
        if (!input.saActive && input.coolantTemp < 30 && input.engineRuntimeSeconds < 60 && input.engineRpm > 1500) {
            return false
        }
        return true
    }

    private fun detectLeanSwing(input: SAIInput): Boolean {
        if (!input.saActive) return false
        return input.o2VoltageB1S1 < SAI_O2_LEAN_THRESHOLD
    }

    private fun generateDiagnosis(status: com.canopobd.data.model.SAIStatus, issues: List<SAIIssue>, input: SAIInput): String {
        return when {
            issues.isEmpty() && status.healthScore >= 80 -> {
                if (input.saActive) {
                    "Sekundaerluftsystem aktiv. Kaltstart-Einblasung laeuft " +
                        "(${status.operationTimeSeconds}s)."
                } else {
                    "Sekundaerluftsystem bereit. Keine Fehler erkannt."
                }
            }
            issues.isNotEmpty() -> {
                val issueNames = issues.map { it.label }
                "SAI-Problem: ${issueNames.joinToString(", ")}. " +
                    "Score: ${status.healthScore}/100."
            }
            else -> {
                "SAI-Status nicht eindeutig. Score: ${status.healthScore}/100."
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateRecommendation(status: com.canopobd.data.model.SAIStatus, issues: List<SAIIssue>, input: SAIInput): String {
        return when {
            issues.isEmpty() -> "Keine Massnahmen erforderlich."
            issues.any { it == SAIIssue.VALVE_STUCK_OPEN || it == SAIIssue.VALVE_STUCK_CLOSED } -> {
                "SAI-Einlassventil pruefen und reinigen. " +
                    "Bei ${input.totalKm.toInt()} km kann Verkohlung vorliegen."
            }
            issues.any { it == SAIIssue.PUMP_FAULT } -> {
                "Luftpumpe und Sicherung pruefen. " +
                    "Pumpenstrom bei Werkstatt messen."
            }
            issues.any { it == SAIIssue.RELAY_FAULT } -> {
                "SAI-Relais und Verkabelung pruefen."
            }
            issues.any { it == SAIIssue.PLUMBING_LEAK } -> {
                "SAI-Druckleitungen auf Risse und Dichtheit pruefen."
            }
            issues.any { it == SAIIssue.INCORRECT_TIMING } -> {
                "SAI-Aktivierung zur falschen Zeit. ECU-Steuerung " +
                    "und Kuehlmittelsensor pruefen."
            }
            else -> {
                "SAI-System bei Werkstatt pruefen lassen."
            }
        }
    }
}
