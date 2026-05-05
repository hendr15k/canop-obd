package com.canopobd.data.domain

import kotlin.math.abs
import kotlin.math.sqrt

class LambdaO2SensorAnalyzer {

    data class LambdaInput(
        val o2VoltageB1S1: Double = 0.0,
        val o2VoltageB1S2: Double = 0.0,
        val o2VoltageB1S3: Double = 0.0,
        val o2VoltageB2S1: Double = 0.0,
        val o2VoltageB2S2: Double = 0.0,
        val fuelAirRatio: Double = 0.0,
        val stftB1: Double = 0.0,
        val ltftB1: Double = 0.0,
        val stftB2: Double = 0.0,
        val ltftB2: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val engineLoad: Double = 0.0,
        val rpm: Double = 0.0,
        val catalystTemp: Double = 0.0,
        val catalystTempB1S2: Double = 0.0,
        val engineRuntimeSeconds: Double = 0.0,
        val activeDTCs: List<String> = emptyList(),
        val totalKm: Double = 0.0,
        val voltageHistoryB1S1: List<Double> = emptyList(),
        val voltageHistoryB1S2: List<Double> = emptyList()
    )

    data class LambdaAnalysis(
        val preCatSensor: com.canopobd.data.model.LambdaSensorStatus,
        val postCatSensor: com.canopobd.data.model.LambdaSensorStatus?,
        val catalystEfficiency: Double,
        val catalystHealthScore: Int,
        val fuelTrimStatus: String,
        val detectedIssues: List<LambdaIssue>,
        val diagnosis: String,
        val recommendation: String
    )

    enum class LambdaIssue(val label: String, val description: String) {
        PRE_CAT_SLOW("Pre-Kat traege", "Lambdasonde reagiert zu langsam"),
        PRE_CAT_STUCK("Pre-Kat festgefahren", "Lambdasonde gibt kein Schwanksignal"),
        POST_CAT_FAILED("Post-Kat defekt", "Post-Cat-Sonde defekt"),
        HEATER_FAULT("Heizelement", "Lambdasonden-Heizelement defekt"),
        CATALYST_WORN("Katalysator abgenutzt", "Katalysator-Wirkung unter Schwellenwert"),
        CIRCUIT_FAULT("Stromkreisfehler", "Lambdasonden-Stromkreis defekt"),
        LEAN_CONDITION("Mager", "System zu mager"),
        RICH_CONDITION("Fett", "System zu fett")
    }

    companion object {
        private const val NARROWBAND_MIN = 0.0
        private const val NARROWBAND_MAX = 1.275
        private const val NARROWBAND_STOICHIOMETRIC = 0.45
        private const val NARROWBAND_LEAN_THRESHOLD = 0.3
        private const val NARROWBAND_RICH_THRESHOLD = 0.8
        private const val POST_CAT_HEALTHY_MIN = 0.4
        private const val POST_CAT_HEALTHY_MAX = 0.7
        private const val POST_CAT_SWING_MAX = 0.2
        private const val CATALYST_EFFICIENCY_THRESHOLD = 0.7
        private const val CATALYST_EFFICIENCY_CRITICAL = 0.5
        private const val CROSS_COUNT_MIN_HEALTHY = 5
        private const val CROSS_COUNT_MAX_HEALTHY = 40
        private const val WIDEBAND_STOICHIOMETRIC = 1.0
        private const val WIDEBAND_LEAN = 1.2
        private const val WIDEBAND_RICH = 0.8
        private const val HEATER_WARMUP_SECONDS = 30.0
        private const val MIN_RPM_FOR_ANALYSIS = 1000.0
        private const val MIN_COOLANT_FOR_ANALYSIS = 70.0

        private const val WEIGHT_DTC = 25
        private const val WEIGHT_VOLTAGE = 25
        private const val WEIGHT_CROSS_COUNT = 25
        private const val WEIGHT_CATALYST = 25
    }

    fun analyze(input: LambdaInput): LambdaAnalysis {
        val issues = mutableListOf<LambdaIssue>()

        val dtcScore = evaluateDTCs(input.activeDTCs, issues)
        val (preCatScore, preCatVoltage) = evaluatePreCatSensor(input, issues)
        val (postCatScore, postCatVoltage) = evaluatePostCatSensor(input, issues)
        val crossCountRate = calculateCrossCountRate(input.voltageHistoryB1S1)
        val crossCountScore = evaluateCrossCount(crossCountRate)
        val (catalystEff, catalystScore) = evaluateCatalystEfficiency(input, issues)

        val rawScore = (dtcScore * WEIGHT_DTC +
                preCatScore * WEIGHT_VOLTAGE +
                crossCountScore * WEIGHT_CROSS_COUNT +
                catalystScore * WEIGHT_CATALYST) / 100
        val adjustedScore = rawScore.coerceIn(0, 100)

        val preCatLambda = calculateLambdaValue(input.o2VoltageB1S1)
        val preCatHeaterOK = checkHeaterStatus(input, false)
        val preCatSensor = com.canopobd.data.model.LambdaSensorStatus(
            sensor = com.canopobd.data.model.O2SensorType.PRECAT_WIDEBAND,
            voltage = input.o2VoltageB1S1,
            lambda = preCatLambda,
            heaterStatus = preCatHeaterOK,
            healthScore = preCatScore,
            crossCountRate = crossCountRate
        )

        val postCatLambda = calculateLambdaValue(input.o2VoltageB1S2)
        val postCatHeaterOK = checkHeaterStatus(input, true)
        val postCatSensor = if (input.o2VoltageB1S2 > 0 || postCatScore < 100) {
            com.canopobd.data.model.LambdaSensorStatus(
                sensor = com.canopobd.data.model.O2SensorType.POSTCAT_NARROWBAND,
                voltage = input.o2VoltageB1S2,
                lambda = postCatLambda,
                heaterStatus = postCatHeaterOK,
                healthScore = postCatScore,
                crossCountRate = calculateCrossCountRate(input.voltageHistoryB1S2)
            )
        } else null

        val fuelTrimStatus = evaluateFuelTrim(input.stftB1, input.ltftB1, input.stftB2, input.ltftB2, issues)
        val diagnosis = generateDiagnosis(preCatSensor, postCatSensor, catalystEff, issues, input)
        val recommendation = generateRecommendation(preCatSensor, postCatSensor, catalystEff, issues, input)

        return LambdaAnalysis(
            preCatSensor = preCatSensor,
            postCatSensor = postCatSensor,
            catalystEfficiency = catalystEff,
            catalystHealthScore = catalystScore,
            fuelTrimStatus = fuelTrimStatus,
            detectedIssues = issues,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    fun analyzeFromOBDData(
        o2B1S1: Double, o2B1S2: Double,
        stftB1: Double, ltftB1: Double,
        coolantTemp: Double, rpm: Double,
        catalystTemp: Double,
        dtcCodes: List<String> = emptyList()
    ): LambdaAnalysis {
        return analyze(LambdaInput(
            o2VoltageB1S1 = o2B1S1,
            o2VoltageB1S2 = o2B1S2,
            stftB1 = stftB1,
            ltftB1 = ltftB1,
            coolantTemp = coolantTemp,
            rpm = rpm,
            catalystTemp = catalystTemp,
            activeDTCs = dtcCodes
        ))
    }

    private fun evaluateDTCs(dtcCodes: List<String>, issues: MutableList<LambdaIssue>): Int {
        var penalty = 0
        for (code in dtcCodes) {
            val upper = code.uppercase()
            when {
                upper.contains("P0130") || upper.contains("P0131") ||
                        upper.contains("P0132") || upper.contains("P0133") -> {
                    penalty = penalty.coerceAtLeast(25)
                    issues.add(LambdaIssue.CIRCUIT_FAULT)
                }
                upper.contains("P0134") -> {
                    penalty = penalty.coerceAtLeast(30)
                    issues.add(LambdaIssue.PRE_CAT_STUCK)
                }
                upper.contains("P0135") || upper.contains("P0141") ||
                        upper.contains("P0155") || upper.contains("P0161") -> {
                    penalty = penalty.coerceAtLeast(20)
                    issues.add(LambdaIssue.HEATER_FAULT)
                }
                upper.contains("P0420") || upper.contains("P0430") -> {
                    penalty = penalty.coerceAtLeast(30)
                    issues.add(LambdaIssue.CATALYST_WORN)
                }
                upper.contains("P0171") -> {
                    penalty = penalty.coerceAtLeast(15)
                    issues.add(LambdaIssue.LEAN_CONDITION)
                }
                upper.contains("P0172") -> {
                    penalty = penalty.coerceAtLeast(15)
                    issues.add(LambdaIssue.RICH_CONDITION)
                }
            }
        }
        return (100 - penalty).coerceAtLeast(0)
    }

    private fun evaluatePreCatSensor(input: LambdaInput, issues: MutableList<LambdaIssue>): Pair<Int, Double> {
        val voltage = input.o2VoltageB1S1
        if (voltage <= 0 && input.rpm > MIN_RPM_FOR_ANALYSIS) {
            issues.add(LambdaIssue.PRE_CAT_STUCK)
            return 20 to voltage
        }

        val isWarmedUp = input.coolantTemp > MIN_COOLANT_FOR_ANALYSIS
        val isOperating = input.rpm > MIN_RPM_FOR_ANALYSIS && isWarmedUp

        if (!isOperating) return 80 to voltage

        if (voltage < NARROWBAND_MIN || voltage > NARROWBAND_MAX) {
            issues.add(LambdaIssue.CIRCUIT_FAULT)
            return 15 to voltage
        }

        if (input.voltageHistoryB1S1.size >= 10) {
            val swing = calculateSwingAmplitude(input.voltageHistoryB1S1)
            if (swing < 0.2) {
                issues.add(LambdaIssue.PRE_CAT_STUCK)
                return 25 to voltage
            }
        }

        val score = when {
            voltage < 0.05 || voltage > 1.2 -> {
                issues.add(LambdaIssue.PRE_CAT_STUCK)
                30
            }
            else -> 95
        }
        return score to voltage
    }

    private fun evaluatePostCatSensor(input: LambdaInput, issues: MutableList<LambdaIssue>): Pair<Int, Double> {
        val voltage = input.o2VoltageB1S2
        if (voltage <= 0 && input.rpm > MIN_RPM_FOR_ANALYSIS && input.coolantTemp > MIN_COOLANT_FOR_ANALYSIS) {
            return 70 to voltage
        }

        if (input.rpm < MIN_RPM_FOR_ANALYSIS || input.coolantTemp < MIN_COOLANT_FOR_ANALYSIS) {
            return 80 to voltage
        }

        val swing = if (input.voltageHistoryB1S2.size >= 5) {
            calculateSwingAmplitude(input.voltageHistoryB1S2)
        } else 0.0

        return when {
            swing > 0.4 -> {
                issues.add(LambdaIssue.CATALYST_WORN)
                40 to voltage
            }
            voltage in POST_CAT_HEALTHY_MIN..POST_CAT_HEALTHY_MAX && swing < POST_CAT_SWING_MAX -> 95 to voltage
            voltage in 0.3..0.8 -> 80 to voltage
            else -> 70 to voltage
        }
    }

    private fun calculateCrossCountRate(history: List<Double>): Double {
        if (history.size < 3) return 0.0
        val threshold = NARROWBAND_STOICHIOMETRIC
        var crossings = 0
        for (i in 1 until history.size) {
            val prev = history[i - 1]
            val curr = history[i]
            if ((prev < threshold && curr >= threshold) || (prev >= threshold && curr < threshold)) {
                crossings++
            }
        }
        val duration = history.size.toDouble() / 10.0
        return if (duration > 0) crossings / duration else 0.0
    }

    private fun calculateSwingAmplitude(history: List<Double>): Double {
        if (history.isEmpty()) return 0.0
        return history.max() - history.min()
    }

    private fun evaluateCrossCount(rate: Double): Int {
        return when {
            rate <= 0 -> 60
            rate in CROSS_COUNT_MIN_HEALTHY.toDouble()..CROSS_COUNT_MAX_HEALTHY.toDouble() -> 100
            rate < CROSS_COUNT_MIN_HEALTHY -> {
                60 + ((rate / CROSS_COUNT_MIN_HEALTHY) * 40).toInt()
            }
            rate > CROSS_COUNT_MAX_HEALTHY -> {
                (100.0 - (rate - CROSS_COUNT_MAX_HEALTHY.toDouble()) * 2.0).coerceAtLeast(30.0).toInt()
            }
            else -> 70
        }
    }

    private fun evaluateCatalystEfficiency(input: LambdaInput, issues: MutableList<LambdaIssue>): Pair<Double, Int> {
        if (input.o2VoltageB1S1 <= 0 || input.o2VoltageB1S2 <= 0) {
            return 0.8 to 70
        }

        val preCatSwing = if (input.voltageHistoryB1S1.size >= 5) {
            calculateSwingAmplitude(input.voltageHistoryB1S1)
        } else abs(input.o2VoltageB1S1 - NARROWBAND_STOICHIOMETRIC) * 2

        val postCatSwing = if (input.voltageHistoryB1S2.size >= 5) {
            calculateSwingAmplitude(input.voltageHistoryB1S2)
        } else abs(input.o2VoltageB1S2 - NARROWBAND_STOICHIOMETRIC) * 2

        val efficiency = if (preCatSwing > 0.1) {
            (1.0 - (postCatSwing / preCatSwing)).coerceIn(0.0, 1.0)
        } else 0.8

        val score = when {
            efficiency >= CATALYST_EFFICIENCY_THRESHOLD -> 95
            efficiency >= CATALYST_EFFICIENCY_CRITICAL -> 60
            efficiency >= 0.3 -> 35
            else -> 15
        }

        if (efficiency < CATALYST_EFFICIENCY_THRESHOLD) {
            issues.add(LambdaIssue.CATALYST_WORN)
        }

        return efficiency to score
    }

    private fun evaluateFuelTrim(
        stftB1: Double, ltftB1: Double,
        stftB2: Double, ltftB2: Double,
        issues: MutableList<LambdaIssue>
    ): String {
        val totalB1 = stftB1 + ltftB1
        val totalB2 = stftB2 + ltftB2
        val worst = maxOf(abs(totalB1), abs(totalB2))

        return when {
            worst > 15 -> {
                if (totalB1 > 15 || totalB2 > 15) {
                    issues.add(LambdaIssue.LEAN_CONDITION)
                    "Mager"
                } else {
                    issues.add(LambdaIssue.RICH_CONDITION)
                    "Fett"
                }
            }
            worst > 10 -> "Grenzwertig"
            else -> "Normal"
        }
    }

    private fun checkHeaterStatus(input: LambdaInput, isPostCat: Boolean): Boolean {
        if (input.engineRuntimeSeconds < HEATER_WARMUP_SECONDS) return true
        return true
    }

    private fun calculateLambdaValue(voltage: Double): Double {
        if (voltage <= 0) return 0.0
        return when {
            voltage < NARROWBAND_LEAN_THRESHOLD -> WIDEBAND_LEAN + (NARROWBAND_LEAN_THRESHOLD - voltage) * 2.0
            voltage > NARROWBAND_RICH_THRESHOLD -> WIDEBAND_RICH - (voltage - NARROWBAND_RICH_THRESHOLD) * 2.0
            else -> WIDEBAND_STOICHIOMETRIC
        }.coerceIn(0.5, 2.0)
    }

    private fun generateDiagnosis(
        preCat: com.canopobd.data.model.LambdaSensorStatus,
        postCat: com.canopobd.data.model.LambdaSensorStatus?,
        catalystEff: Double,
        issues: List<LambdaIssue>,
        input: LambdaInput
    ): String {
        val parts = mutableListOf<String>()

        parts.add("Pre-Cat: ${"%.3f".format(preCat.voltage)}V " +
                "(Lambda: ${"%.2f".format(preCat.lambda)}), " +
                "Kreuzrate: ${"%.1f".format(preCat.crossCountRate)}/s.")

        if (postCat != null) {
            parts.add("Post-Cat: ${"%.3f".format(postCat.voltage)}V.")
        }

        if (catalystEff > 0) {
            parts.add("Katalysator-Effizienz: ${(catalystEff * 100).toInt()}%.")
        }

        if (issues.isNotEmpty()) {
            parts.add("Probleme: ${issues.map { it.label }.joinToString(", ")}.")
        }

        return parts.joinToString(" ")
    }

    private fun generateRecommendation(
        preCat: com.canopobd.data.model.LambdaSensorStatus,
        postCat: com.canopobd.data.model.LambdaSensorStatus?,
        catalystEff: Double,
        issues: List<LambdaIssue>,
        input: LambdaInput
    ): String {
        return when {
            issues.isEmpty() -> "Lambdasonden und Katalysator funktionieren normal."
            issues.any { it == LambdaIssue.HEATER_FAULT } -> {
                "Lambdasonden-Heizelement pruefen. " +
                        "Bei ${input.totalKm.toInt()} km: Verkabelung und Sicherung kontrollieren."
            }
            issues.any { it == LambdaIssue.PRE_CAT_STUCK || it == LambdaIssue.PRE_CAT_SLOW } -> {
                "Pre-Cat-Lambdasonde reagiert nicht normal. " +
                        "Sensor pruefen oder ersetzen. Verkabelung kontrollieren."
            }
            issues.any { it == LambdaIssue.CATALYST_WORN } -> {
                "Katalysator-Wirkung vermindert (${(catalystEff * 100).toInt()}%). " +
                        "Katalysator pruefen lassen. " +
                        "Bei ${input.totalKm.toInt()} km kann Verschleiss normal sein."
            }
            issues.any { it == LambdaIssue.LEAN_CONDITION } -> {
                "System zu mager. Luftleck, Kraftstoffdruck " +
                        "und MAF-Sensor pruefen."
            }
            issues.any { it == LambdaIssue.RICH_CONDITION } -> {
                "System zu fett. Einspritzventile, Kraftstoffdruck " +
                        "und Luftfilter pruefen."
            }
            issues.any { it == LambdaIssue.CIRCUIT_FAULT } -> {
                "Lambdasonden-Stromkreisfehler. Verkabelung " +
                        "und Sensor pruefen."
            }
            else -> {
                "Lambdasonden-System bei Werkstatt pruefen lassen."
            }
        }
    }
}
