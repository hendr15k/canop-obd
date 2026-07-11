package com.canopobd.data.domain

import kotlin.math.abs

class EGRHealthAnalyzer {

    data class EGRInput(
        val commandedEGR: Double,
        val egrTemp: Double,
        val engineLoad: Double,
        val rpm: Double,
        val coolantTemp: Double,
        val intakeTemp: Double,
        val mafRate: Double,
        val stftB1: Double = 0.0,
        val ltftB1: Double = 0.0,
        val activeDTCs: List<String> = emptyList(),
        val totalKm: Double = 0.0
    )

    data class EGRAnalysis(
        val health: com.canopobd.data.model.EGRHealth,
        val healthScore: Int,
        val flowDeviation: Double,
        val temperaturePlausibility: Boolean,
        val detectedIssues: List<EGRIssue>,
        val diagnosis: String,
        val recommendation: String
    )

    enum class EGRIssue(val label: String, val description: String) {
        STUCK_CLOSED("Klebt geschlossen", "EGR-Ventil oeffnet nicht"),
        STUCK_OPEN("Klebt offen", "EGR-Ventil schliesst nicht"),
        CLOGGED("Verstopft", "EGR-Kanal durch Kohlenstoffablagerungen verstopft"),
        SENSOR_FAULT("Sensorfehler", "EGR-Positionssensor unplausibel"),
        TEMPERATURE_FAULT("Temperaturfehler", "EGR-Temperatur ausserhalb des Normbereichs"),
        FLOW_LOW("Geringer Durchfluss", "EGR-Durchfluss unter Sollwert"),
        FLOW_HIGH("Erhoehter Durchfluss", "EGR-Durchfluss ueber Sollwert")
    }

    companion object {
        private const val EGR_MIN_TEMP = 0.0
        private const val EGR_MAX_TEMP = 200.0
        private const val EGR_OPEN_THRESHOLD = 5.0
        private const val EGR_CLOSED_THRESHOLD = 2.0
        private const val EGR_ERROR_THRESHOLD_WARNING = 20.0
        private const val EGR_ERROR_THRESHOLD_CRITICAL = 40.0
        private const val EGR_DUTY_MAX_IDLE = 10.0
        private const val EGR_DUTY_MIN_LOAD = 30.0
        private const val EGR_MAF_DEVIATION_WARNING = 15.0

        private const val WEIGHT_DTC = 30
        private const val WEIGHT_FLOW = 30
        private const val WEIGHT_TEMPERATURE = 20
        private const val WEIGHT_TRIM = 20
    }

    fun analyze(input: EGRInput): EGRAnalysis {
        val issues = mutableListOf<EGRIssue>()

        val dtcScore = evaluateDTCs(input.activeDTCs, issues)
        val (flowScore, flowDeviation) = evaluateFlow(input, issues)
        val tempScore = evaluateTemperature(input.egrTemp, input.coolantTemp, issues)
        val trimScore = evaluateTrims(input.stftB1, input.ltftB1, issues)

        val rawScore = (dtcScore * WEIGHT_DTC +
                flowScore * WEIGHT_FLOW +
                tempScore * WEIGHT_TEMPERATURE +
                trimScore * WEIGHT_TRIM) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)
        val tempPlausibility = checkTemperaturePlausibility(input.egrTemp, input.coolantTemp, input.rpm)
        val status = determineStatus(input.commandedEGR, adjustedScore, input.engineLoad)
        val health = com.canopobd.data.model.EGRHealth(
            status = status,
            flowRate = input.commandedEGR,
            errorPercent = flowDeviation,
            healthScore = adjustedScore
        )
        val diagnosis = generateDiagnosis(health, issues, input)
        val recommendation = generateRecommendation(health, issues, input)

        return EGRAnalysis(
            health = health,
            healthScore = adjustedScore,
            flowDeviation = flowDeviation,
            temperaturePlausibility = tempPlausibility,
            detectedIssues = issues,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    private fun evaluateDTCs(dtcCodes: List<String>, issues: MutableList<EGRIssue>): Int {
        var penalty = 0
        for (code in dtcCodes) {
            val upper = code.uppercase()
            when {
                upper.contains("P0400") -> {
                    penalty = penalty.coerceAtLeast(25)
                    issues.add(EGRIssue.FLOW_LOW)
                }
                upper.contains("P0401") -> {
                    penalty = penalty.coerceAtLeast(30)
                    issues.add(EGRIssue.CLOGGED)
                }
                upper.contains("P0402") -> {
                    penalty = penalty.coerceAtLeast(25)
                    issues.add(EGRIssue.FLOW_HIGH)
                }
                upper.contains("P0403") -> {
                    penalty = penalty.coerceAtLeast(35)
                    issues.add(EGRIssue.SENSOR_FAULT)
                }
                upper.contains("P0404") -> {
                    penalty = penalty.coerceAtLeast(30)
                    issues.add(EGRIssue.STUCK_OPEN)
                }
                upper.contains("P0405") || upper.contains("P0406") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(EGRIssue.SENSOR_FAULT)
                }
            }
        }
        return (100 - penalty).coerceAtLeast(0)
    }

    private fun evaluateFlow(input: EGRInput, issues: MutableList<EGRIssue>): Pair<Int, Double> {
        val expectedDuty = calculateExpectedEGR(input.engineLoad, input.rpm)
        val deviation = if (expectedDuty > 0) {
            ((input.commandedEGR - expectedDuty) / expectedDuty) * 100.0
        } else 0.0

        val absDeviation = abs(deviation)

        val score = when {
            input.engineLoad < 15 && input.rpm < 1200 -> {
                if (input.commandedEGR > EGR_DUTY_MAX_IDLE) {
                    issues.add(EGRIssue.STUCK_OPEN)
                    40
                } else 95
            }
            input.engineLoad > 40 -> {
                if (input.commandedEGR < EGR_DUTY_MIN_LOAD && input.coolantTemp > 70) {
                    issues.add(EGRIssue.STUCK_CLOSED)
                    45
                } else {
                    when {
                        absDeviation > EGR_ERROR_THRESHOLD_CRITICAL -> 25
                        absDeviation > EGR_ERROR_THRESHOLD_WARNING -> 55
                        absDeviation > 10 -> 80
                        else -> 100
                    }
                }
            }
            else -> {
                when {
                    absDeviation > EGR_ERROR_THRESHOLD_CRITICAL -> 30
                    absDeviation > EGR_ERROR_THRESHOLD_WARNING -> 60
                    else -> 90
                }
            }
        }

        return score to deviation
    }

    private fun calculateExpectedEGR(engineLoad: Double, rpm: Double): Double {
        if (rpm < 800 || engineLoad < 10) return 0.0
        val loadFactor = (engineLoad / 100.0).coerceIn(0.0, 1.0)
        val rpmFactor = when {
            rpm < 1500 -> 0.5
            rpm < 2500 -> 0.8
            rpm < 4000 -> 1.0
            rpm < 5500 -> 0.7
            else -> 0.3
        }
        return (loadFactor * rpmFactor * 60.0).coerceIn(0.0, 80.0)
    }

    private fun evaluateTemperature(egrTemp: Double, coolantTemp: Double, issues: MutableList<EGRIssue>): Int {
        if (egrTemp <= EGR_MIN_TEMP && coolantTemp > 70) {
            issues.add(EGRIssue.TEMPERATURE_FAULT)
            return 30
        }
        if (egrTemp > EGR_MAX_TEMP) {
            issues.add(EGRIssue.TEMPERATURE_FAULT)
            return 25
        }
        if (coolantTemp > 70 && egrTemp > 0) {
            val tempDiff = egrTemp - coolantTemp
            if (tempDiff > 80 || tempDiff < -20) {
                issues.add(EGRIssue.TEMPERATURE_FAULT)
                return 40
            }
        }
        return 100
    }

    @Suppress("UNUSED_PARAMETER")
    private fun checkTemperaturePlausibility(egrTemp: Double, coolantTemp: Double, rpm: Double): Boolean {
        if (egrTemp <= 0 && coolantTemp > 70) return false
        if (egrTemp > EGR_MAX_TEMP) return false
        if (egrTemp > 0 && coolantTemp > 70) {
            val diff = abs(egrTemp - coolantTemp)
            if (diff > 100) return false
        }
        return true
    }

    private fun evaluateTrims(stft: Double, ltft: Double, issues: MutableList<EGRIssue>): Int {
        val total = abs(stft + ltft)
        return when {
            total > 15 -> {
                issues.add(EGRIssue.CLOGGED)
                35
            }
            total > 10 -> 65
            total > 5 -> 85
            else -> 100
        }
    }

    private fun determineStatus(commandedEGR: Double, score: Int, engineLoad: Double): com.canopobd.data.model.EGRStatus {
        if (score < 40) return com.canopobd.data.model.EGRStatus.FAULT
        if (engineLoad > 40 && commandedEGR > 5) return com.canopobd.data.model.EGRStatus.OPEN
        if (commandedEGR < 2) return com.canopobd.data.model.EGRStatus.CLOSED
        return when {
            score >= 70 -> com.canopobd.data.model.EGRStatus.CLOSED
            else -> com.canopobd.data.model.EGRStatus.FAULT
        }
    }

    private fun generateDiagnosis(health: com.canopobd.data.model.EGRHealth, issues: List<EGRIssue>, input: EGRInput): String {
        return when (health.status) {
            com.canopobd.data.model.EGRStatus.CLOSED -> {
                "EGR-Ventil geschlossen, Durchfluss: ${"%.1f".format(health.flowRate)}%. " +
                        "Fehler: ${"%.1f".format(health.errorPercent)}%."
            }
            com.canopobd.data.model.EGRStatus.OPEN -> {
                "EGR-Ventil offen, Durchfluss: ${"%.1f".format(health.flowRate)}%. " +
                        "Temperatur: ${input.egrTemp.toInt()}C."
            }
            com.canopobd.data.model.EGRStatus.FAULT -> {
                val issueNames = issues.map { it.label }
                "EGR-Systemfehler: ${issueNames.joinToString(", ")}. " +
                        "Score: ${health.healthScore}/100."
            }
        }
    }

    private fun generateRecommendation(health: com.canopobd.data.model.EGRHealth, issues: List<EGRIssue>, input: EGRInput): String {
        return when {
            issues.isEmpty() && health.healthScore >= 80 -> {
                "Keine Massnahmen erforderlich. EGR-System funktioniert normal."
            }
            issues.any { it == EGRIssue.CLOGGED } -> {
                "EGR-Ventil und -Kanal reinigen lassen. " +
                        "Kohlenstoffablagerungen bei ${input.totalKm.toInt()} km typisch."
            }
            issues.any { it == EGRIssue.STUCK_CLOSED || it == EGRIssue.STUCK_OPEN } -> {
                "EGR-Ventil pruefen. Mechanischer Defekt oder Verkohlung moeglich. " +
                        "Ventil ersetzen lassen."
            }
            issues.any { it == EGRIssue.SENSOR_FAULT } -> {
                "EGR-Positionssensor und Verkabelung pruefen."
            }
            health.healthScore < 50 -> {
                "EGR-System dringend pruefen lassen. " +
                        "Emissionswerte koennen erhoeht sein."
            }
            else -> {
                "EGR-System bei naechster Wartung pruefen."
            }
        }
    }
}
