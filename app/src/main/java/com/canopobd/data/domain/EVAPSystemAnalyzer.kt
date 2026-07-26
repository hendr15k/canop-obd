package com.canopobd.data.domain

import kotlin.math.abs

class EVAPSystemAnalyzer {

    data class EVAPInput(
        val commandedEvapPurge: Double,
        val vaporPressure: Double = 0.0,
        val fuelLevel: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val intakeTemp: Double = 0.0,
        val engineRpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val ambientTemp: Double = 20.0,
        val activeDTCs: List<String> = emptyList(),
        val totalKm: Double = 0.0
    )

    data class EVAPAnalysis(
        val status: com.canopobd.data.model.EVAPStatus,
        val healthScore: Int,
        val purgeEfficiency: Double,
        val pressureDeviation: Double,
        val detectedIssues: List<EVAPIssue>,
        val diagnosis: String,
        val recommendation: String
    )

    enum class EVAPIssue(val label: String, val description: String) {
        LARGE_LEAK("Grosses Leck", "EVAP-System hat ein grosses Leck"),
        SMALL_LEAK("Kleines Leck", "EVAP-System hat ein kleines Leck"),
        PURGE_VALVE_FAULT("Purge-Ventil", "Purge-Ventil funktioniert nicht korrekt"),
        VENT_VALVE_FAULT("Ventilationsventil", "Ventilationsventil defekt"),
        PRESSURE_SENSOR_FAULT("Drucksensor", "Tankdrucksensor unplausibel"),
        FUEL_CAP_LOSE("Tankdeckel", "Tankdeckel nicht richtig verschlossen"),
        CANISTER_SATURATED("Adsorber", "Aktivkohlebehaelter gesaettigt")
    }

    companion object {
        private const val PURGE_MAX_DUTY = 80.0
        private const val PURGE_NORMAL_MAX = 50.0
        private const val VAPOR_PRESSURE_NORMAL_MIN = -500.0
        private const val VAPOR_PRESSURE_NORMAL_MAX = 500.0
        private const val VAPOR_PRESSURE_LEAK_THRESHOLD = -300.0
        private const val VAPOR_PRESSURE_BLOCKED_THRESHOLD = 1500.0
        private const val EVAP_TEMP_MIN = 5.0
        private const val FUEL_LEVEL_MIN_PURGE = 15.0
        private const val FUEL_LEVEL_MAX_PURGE = 85.0
        private const val LARGE_LEAK_THRESHOLD = 1000
        private const val SMALL_LEAK_THRESHOLD = 300

        private const val WEIGHT_DTC = 30
        private const val WEIGHT_PURGE = 25
        private const val WEIGHT_PRESSURE = 25
        private const val WEIGHT_CONDITIONS = 20
    }

    fun analyze(input: EVAPInput): EVAPAnalysis {
        val issues = mutableListOf<EVAPIssue>()

        val dtcScore = evaluateDTCs(input.activeDTCs, issues)
        val purgeScore = evaluatePurgeDuty(input.commandedEvapPurge, input.engineRpm, input.engineLoad, issues)
        val pressureScore = evaluateVaporPressure(input.vaporPressure, issues)
        val conditionScore = evaluateConditions(input, issues)

        val rawScore = (dtcScore * WEIGHT_DTC +
            purgeScore * WEIGHT_PURGE +
            pressureScore * WEIGHT_PRESSURE +
            conditionScore * WEIGHT_CONDITIONS) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)
        val (hasLeak, leakSize) = detectLeak(input.vaporPressure, input.activeDTCs, issues)
        val purgeEfficiency = calculatePurgeEfficiency(input)
        val pressureDeviation = calculatePressureDeviation(input.vaporPressure)

        val status = com.canopobd.data.model.EVAPStatus(
            purgeDuty = input.commandedEvapPurge,
            tankPressure = input.vaporPressure,
            hasLeak = hasLeak,
            leakSize = leakSize
        )

        val diagnosis = generateDiagnosis(status, issues, input)
        val recommendation = generateRecommendation(status, issues, input)

        return EVAPAnalysis(
            status = status,
            healthScore = adjustedScore,
            purgeEfficiency = purgeEfficiency,
            pressureDeviation = pressureDeviation,
            detectedIssues = issues,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    private fun evaluateDTCs(dtcCodes: List<String>, issues: MutableList<EVAPIssue>): Int {
        var penalty = 0
        for (code in dtcCodes) {
            val upper = code.uppercase()
            when {
                upper.contains("P0441") -> {
                    penalty = penalty.coerceAtLeast(25)
                    issues.add(EVAPIssue.PURGE_VALVE_FAULT)
                }
                upper.contains("P0442") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(EVAPIssue.SMALL_LEAK)
                }
                upper.contains("P0440") || upper.contains("P0443") -> {
                    penalty = penalty.coerceAtLeast(30)
                    issues.add(EVAPIssue.PURGE_VALVE_FAULT)
                }
                upper.contains("P0446") -> {
                    penalty = penalty.coerceAtLeast(25)
                    issues.add(EVAPIssue.VENT_VALVE_FAULT)
                }
                upper.contains("P0455") -> {
                    penalty = penalty.coerceAtLeast(35)
                    issues.add(EVAPIssue.LARGE_LEAK)
                }
                upper.contains("P0456") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(EVAPIssue.SMALL_LEAK)
                }
                upper.contains("P0452") || upper.contains("P0453") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(EVAPIssue.PRESSURE_SENSOR_FAULT)
                }
            }
        }
        return (100 - penalty).coerceAtLeast(0)
    }

    private fun evaluatePurgeDuty(
        purge: Double,
        rpm: Double,
        load: Double,
        issues: MutableList<EVAPIssue>
    ): Int {
        if (rpm < 500) return 90

        val isOperating = rpm > 800 && load > 5
        if (!isOperating && purge > 5) {
            issues.add(EVAPIssue.PURGE_VALVE_FAULT)
            return 40
        }

        return when {
            purge > PURGE_MAX_DUTY -> {
                issues.add(EVAPIssue.PURGE_VALVE_FAULT)
                30
            }
            purge > PURGE_NORMAL_MAX -> 60
            purge in 0.0..PURGE_NORMAL_MAX -> 100
            purge < 0 -> {
                issues.add(EVAPIssue.PRESSURE_SENSOR_FAULT)
                20
            }
            else -> 80
        }
    }

    private fun evaluateVaporPressure(pressure: Double, issues: MutableList<EVAPIssue>): Int {
        return when {
            pressure < VAPOR_PRESSURE_LEAK_THRESHOLD -> {
                issues.add(EVAPIssue.LARGE_LEAK)
                20
            }
            pressure < VAPOR_PRESSURE_NORMAL_MIN -> {
                issues.add(EVAPIssue.SMALL_LEAK)
                50
            }
            pressure > VAPOR_PRESSURE_BLOCKED_THRESHOLD -> {
                issues.add(EVAPIssue.VENT_VALVE_FAULT)
                30
            }
            pressure in VAPOR_PRESSURE_NORMAL_MIN..VAPOR_PRESSURE_NORMAL_MAX -> 100
            pressure > VAPOR_PRESSURE_NORMAL_MAX -> 70
            else -> 80
        }
    }

    private fun evaluateConditions(input: EVAPInput, issues: MutableList<EVAPIssue>): Int {
        var score = 100

        if (input.fuelLevel > 0 && (input.fuelLevel < FUEL_LEVEL_MIN_PURGE || input.fuelLevel > FUEL_LEVEL_MAX_PURGE)) {
            score -= 15
        }

        if (input.coolantTemp > 0 && input.coolantTemp < EVAP_TEMP_MIN) {
            score -= 20
        }

        if (input.fuelLevel > 0 && input.fuelLevel > 90) {
            issues.add(EVAPIssue.CANISTER_SATURATED)
            score -= 10
        }

        return score.coerceIn(0, 100)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun detectLeak(
        pressure: Double,
        dtcCodes: List<String>,
        issues: MutableList<EVAPIssue>
    ): Pair<Boolean, com.canopobd.data.model.LeakSize?> {
        val hasLargeLeakDTC = dtcCodes.any {
            it.uppercase().let { c -> c.contains("P0455") || c.contains("P0440") }
        }
        val hasSmallLeakDTC = dtcCodes.any {
            it.uppercase().let { c -> c.contains("P0442") || c.contains("P0456") }
        }

        return when {
            hasLargeLeakDTC || pressure < -LARGE_LEAK_THRESHOLD -> true to com.canopobd.data.model.LeakSize.LARGE
            hasSmallLeakDTC || pressure < -SMALL_LEAK_THRESHOLD -> true to com.canopobd.data.model.LeakSize.SMALL
            pressure < -100 -> true to com.canopobd.data.model.LeakSize.SMALL
            else -> false to null
        }
    }

    private fun calculatePurgeEfficiency(input: EVAPInput): Double {
        if (input.commandedEvapPurge <= 0) return 0.0
        val tempFactor = when {
            input.coolantTemp < 60 -> 0.5
            input.coolantTemp < 80 -> 0.8
            else -> 1.0
        }
        val fuelFactor = when {
            input.fuelLevel < 20 -> 0.6
            input.fuelLevel > 90 -> 0.4
            else -> 1.0
        }
        return (input.commandedEvapPurge * tempFactor * fuelFactor).coerceIn(0.0, 100.0)
    }

    private fun calculatePressureDeviation(pressure: Double): Double {
        val target = 0.0
        return abs(pressure - target)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateDiagnosis(status: com.canopobd.data.model.EVAPStatus, issues: List<EVAPIssue>, input: EVAPInput): String {
        return when {
            issues.isEmpty() -> {
                "EVAP-System funktioniert normal. " +
                    "Purge: ${"%.1f".format(status.purgeDuty)}%, " +
                    "Tankdruck: ${"%.0f".format(status.tankPressure)} Pa."
            }
            status.hasLeak -> {
                val leakDesc = when (status.leakSize) {
                    com.canopobd.data.model.LeakSize.LARGE -> "Grosses Leck"
                    com.canopobd.data.model.LeakSize.MEDIUM -> "Mittleres Leck"
                    com.canopobd.data.model.LeakSize.SMALL -> "Kleines Leck"
                    null -> "Leck erkannt"
                }
                "$leakDesc im EVAP-System. " +
                    "Tankdruck: ${"%.0f".format(status.tankPressure)} Pa."
            }
            else -> {
                val issueNames = issues.map { it.label }
                "EVAP-Problem: ${issueNames.joinToString(", ")}. " +
                    "Purge: ${"%.1f".format(status.purgeDuty)}%."
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateRecommendation(status: com.canopobd.data.model.EVAPStatus, issues: List<EVAPIssue>, input: EVAPInput): String {
        return when {
            issues.isEmpty() -> "Keine Massnahmen erforderlich."
            issues.any { it == EVAPIssue.FUEL_CAP_LOSE } -> {
                "Tankdeckel pruefen und festziehen. " +
                    "Nach dem Tanken pruefen ob der Deckel richtig sitzt."
            }
            issues.any { it == EVAPIssue.LARGE_LEAK } -> {
                "SOFORT: Tankdeckel, Tankflasche und EVAP-Leitungen auf " +
                    "Undichtigkeit pruefen. Grosses Leck erkannt."
            }
            issues.any { it == EVAPIssue.SMALL_LEAK } -> {
                "Kleines Leck im EVAP-System. Tankdeckel, Schlaeuche " +
                    "und Dichtungen pruefen."
            }
            issues.any { it == EVAPIssue.PURGE_VALVE_FAULT } -> {
                "Purge-Ventil pruefen. Ventil kann klemmen oder " +
                    "Verkabelung defekt sein."
            }
            issues.any { it == EVAPIssue.CANISTER_SATURATED } -> {
                "Aktivkohlebehaelter moeglich gesaettigt. " +
                    "Bei Ueberladung des Tanks kann Kraftstoff in " +
                    "den Adsorber gelangen."
            }
            else -> {
                "EVAP-System bei Werkstatt pruefen lassen. " +
                    "Drucktest und Undichtigkeitspruefung durchfuehren."
            }
        }
    }
}
