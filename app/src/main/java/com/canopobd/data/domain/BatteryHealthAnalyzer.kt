package com.canopobd.data.domain

import kotlin.math.abs
import kotlin.math.sqrt

class BatteryHealthAnalyzer {

    data class BatteryInput(
        val voltageHistory: List<Double>,
        val currentVoltage: Double,
        val engineRpm: Double = 0.0,
        val alternatorDuty: Double = 0.0,
        val controlModuleVoltage: Double = 0.0,
        val activeDTCs: List<String> = emptyList(),
        val coolantTemp: Double = 0.0,
        val totalKm: Double = 0.0
    )

    data class BatteryAnalysis(
        val status: com.canopobd.data.model.BatteryStatus,
        val healthScore: Int,
        val voltageTrend: VoltageTrend,
        val rippleAmplitude: Double,
        val chargingSystemHealth: ChargingSystemHealth,
        val estimatedCca: Int,
        val diagnosis: String,
        val recommendation: String
    )

    enum class VoltageTrend(val label: String) {
        RISING("Steigend"),
        STABLE("Stabil"),
        FALLING("Fallend"),
        OSCILLATING("Schwankend")
    }

    enum class ChargingSystemHealth(val label: String, val colorHex: Long) {
        HEALTHY("Gesund", 0xFF00FF88),
        WEAK("Schwach", 0xFFFFE066),
        FAULTY("Defekt", 0xFFFF4444),
        UNKNOWN("Unbekannt", 0xFFAAAAAA)
    }

    companion object {
        private const val VOLTAGE_ENGINE_OFF_MIN = 11.8
        private const val VOLTAGE_ENGINE_OFF_MAX = 12.8
        private const val VOLTAGE_ENGINE_OFF_LOW = 12.0
        private const val VOLTAGE_ENGINE_OFF_CRITICAL = 11.5
        private const val VOLTAGE_CHARGING_MIN = 13.5
        private const val VOLTAGE_CHARGING_MAX = 14.5
        private const val VOLTAGE_CHARGING_OPTIMAL_MIN = 13.8
        private const val VOLTAGE_CHARGING_OPTIMAL_MAX = 14.4
        private const val VOLTAGE_OVERCHARGING = 15.0
        private const val VOLTAGE_UNDERCHARGING = 13.2

        private const val SOC_100_VOLTAGE = 12.7
        private const val SOC_75_VOLTAGE = 12.4
        private const val SOC_50_VOLTAGE = 12.2
        private const val SOC_25_VOLTAGE = 12.0
        private const val SOC_0_VOLTAGE = 11.5

        private const val CCA_NEW = 700
        private const val CCA_MIN_HEALTHY = 500
        private const val CCA_MIN_WEAK = 350
        private const val CCA_MIN_CRITICAL = 250

        private const val BATTERY_AGE_HIGH_KM = 80000.0
        private const val BATTERY_REPLACE_KM = 100000.0

        private const val RIPPLE_MAX_HEALTHY = 0.3
        private const val RIPPLE_MAX_WARNING = 0.8

        private const val WEIGHT_DTC = 25
        private const val WEIGHT_VOLTAGE = 25
        private const val WEIGHT_CHARGING = 25
        private const val WEIGHT_STABILITY = 25
    }

    fun analyze(input: BatteryInput): BatteryAnalysis {
        val dtcScore = evaluateDTCs(input.activeDTCs)
        val (voltageScore, isCharging) = evaluateVoltage(input.currentVoltage, input.engineRpm)
        val chargingScore = evaluateChargingSystem(input)
        val (stabilityScore, ripple, trend) = evaluateStability(input.voltageHistory)

        val rawScore = (dtcScore * WEIGHT_DTC +
                voltageScore * WEIGHT_VOLTAGE +
                chargingScore * WEIGHT_CHARGING +
                stabilityScore * WEIGHT_STABILITY) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)

        val soc = estimateStateOfCharge(input.currentVoltage, isCharging)
        val health = determineHealth(adjustedScore, input)
        val chargingHealth = determineChargingHealth(chargingScore, input)
        val estimatedCca = estimateCCA(input.totalKm, input.currentVoltage)
        val batteryStatus = com.canopobd.data.model.BatteryStatus(
            voltage = input.currentVoltage,
            soc = soc,
            health = health,
            isCharging = isCharging
        )
        val diagnosis = generateDiagnosis(health, chargingHealth, input, soc, trend, ripple)
        val recommendation = generateRecommendation(health, chargingHealth, input, estimatedCca)

        return BatteryAnalysis(
            status = batteryStatus,
            healthScore = adjustedScore,
            voltageTrend = trend,
            rippleAmplitude = ripple,
            chargingSystemHealth = chargingHealth,
            estimatedCca = estimatedCca,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    fun estimateStateOfCharge(voltage: Double, isCharging: Boolean): Int {
        if (isCharging) return -1
        return when {
            voltage >= SOC_100_VOLTAGE -> 100
            voltage >= SOC_75_VOLTAGE -> {
                val factor = (voltage - SOC_75_VOLTAGE) / (SOC_100_VOLTAGE - SOC_75_VOLTAGE)
                (75 + factor * 25).toInt().coerceIn(75, 100)
            }
            voltage >= SOC_50_VOLTAGE -> {
                val factor = (voltage - SOC_50_VOLTAGE) / (SOC_75_VOLTAGE - SOC_50_VOLTAGE)
                (50 + factor * 25).toInt().coerceIn(50, 75)
            }
            voltage >= SOC_25_VOLTAGE -> {
                val factor = (voltage - SOC_25_VOLTAGE) / (SOC_50_VOLTAGE - SOC_25_VOLTAGE)
                (25 + factor * 25).toInt().coerceIn(25, 50)
            }
            voltage >= SOC_0_VOLTAGE -> {
                val factor = (voltage - SOC_0_VOLTAGE) / (SOC_25_VOLTAGE - SOC_0_VOLTAGE)
                (factor * 25).toInt().coerceIn(0, 25)
            }
            else -> 0
        }
    }

    fun detectChargingSystemDTCs(dtcCodes: List<String>): List<String> {
        return dtcCodes.filter { code ->
            val upper = code.uppercase()
            upper.contains("P0562") || upper.contains("P0563") ||
                    upper.contains("P0620") || upper.contains("P0621") ||
                    upper.contains("P0622")
        }
    }

    private fun evaluateDTCs(dtcCodes: List<String>): Int {
        var penalty = 0
        for (code in dtcCodes) {
            val upper = code.uppercase()
            when {
                upper.contains("P0562") -> penalty = penalty.coerceAtLeast(25)
                upper.contains("P0563") -> penalty = penalty.coerceAtLeast(30)
                upper.contains("P0620") || upper.contains("P0621") -> penalty = penalty.coerceAtLeast(20)
                upper.contains("P0622") -> penalty = penalty.coerceAtLeast(20)
            }
        }
        return (100 - penalty).coerceAtLeast(0)
    }

    private fun evaluateVoltage(voltage: Double, rpm: Double): Pair<Int, Boolean> {
        val isCharging = rpm > 500

        if (isCharging) {
            val score = when {
                voltage > VOLTAGE_OVERCHARGING -> 15
                voltage < VOLTAGE_UNDERCHARGING -> 30
                voltage in VOLTAGE_CHARGING_OPTIMAL_MIN..VOLTAGE_CHARGING_OPTIMAL_MAX -> 100
                voltage in VOLTAGE_CHARGING_MIN..VOLTAGE_CHARGING_MAX -> 80
                voltage < VOLTAGE_CHARGING_MIN -> 40
                else -> 60
            }
            return score to true
        } else {
            val score = when {
                voltage < VOLTAGE_ENGINE_OFF_CRITICAL -> 10
                voltage < VOLTAGE_ENGINE_OFF_LOW -> 40
                voltage < VOLTAGE_ENGINE_OFF_MIN -> 70
                voltage <= VOLTAGE_ENGINE_OFF_MAX -> 100
                else -> 85
            }
            return score to false
        }
    }

    private fun evaluateChargingSystem(input: BatteryInput): Int {
        var score = 100

        if (input.engineRpm > 500) {
            if (input.alternatorDuty > 0) {
                score = when {
                    input.alternatorDuty > 90 -> 50
                    input.alternatorDuty > 75 -> 75
                    input.alternatorDuty in 30.0..75.0 -> 100
                    input.alternatorDuty < 20 -> 60
                    else -> 80
                }
            }

            if (input.controlModuleVoltage > 0 && input.currentVoltage > 0) {
                val diff = abs(input.controlModuleVoltage - input.currentVoltage)
                if (diff > 0.5) {
                    score -= 10
                }
            }
        }

        return score.coerceIn(0, 100)
    }

    private fun evaluateStability(history: List<Double>): Triple<Int, Double, VoltageTrend> {
        if (history.size < 3) {
            return Triple(80, 0.0, VoltageTrend.STABLE)
        }

        val recent = history.takeLast(20)
        val mean = recent.average()
        val variance = recent.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        val trend = detectTrend(recent)
        val peakToPeak = if (recent.isNotEmpty()) recent.max() - recent.min() else 0.0

        val stabilityScore = when {
            stdDev > RIPPLE_MAX_WARNING -> 30
            stdDev > RIPPLE_MAX_HEALTHY -> 60
            stdDev > 0.15 -> 85
            else -> 100
        }

        return Triple(stabilityScore, peakToPeak, trend)
    }

    private fun detectTrend(values: List<Double>): VoltageTrend {
        if (values.size < 5) return VoltageTrend.STABLE

        val firstHalf = values.take(values.size / 2).average()
        val secondHalf = values.drop(values.size / 2).average()
        val diff = secondHalf - firstHalf

        val range = values.max() - values.min()
        if (range > 1.0 && diff < 0.3) return VoltageTrend.OSCILLATING

        return when {
            diff > 0.3 -> VoltageTrend.RISING
            diff < -0.3 -> VoltageTrend.FALLING
            else -> VoltageTrend.STABLE
        }
    }

    private fun determineHealth(score: Int, input: BatteryInput): com.canopobd.data.model.BatteryHealth {
        val hasDTC = input.activeDTCs.any {
            it.uppercase().let { c -> c.contains("P0562") || c.contains("P0563") }
        }

        return when {
            hasDTC && score < 40 -> com.canopobd.data.model.BatteryHealth.CRITICAL
            score >= 80 -> com.canopobd.data.model.BatteryHealth.GOOD
            score >= 55 -> com.canopobd.data.model.BatteryHealth.FAIR
            score >= 30 -> com.canopobd.data.model.BatteryHealth.POOR
            else -> com.canopobd.data.model.BatteryHealth.CRITICAL
        }
    }

    private fun determineChargingHealth(score: Int, input: BatteryInput): ChargingSystemHealth {
        if (input.engineRpm < 500) return ChargingSystemHealth.UNKNOWN
        return when {
            score >= 80 -> ChargingSystemHealth.HEALTHY
            score >= 50 -> ChargingSystemHealth.WEAK
            else -> ChargingSystemHealth.FAULTY
        }
    }

    private fun estimateCCA(totalKm: Double, voltage: Double): Int {
        val ageFactor = when {
            totalKm < 30000 -> 1.0
            totalKm < BATTERY_AGE_HIGH_KM -> 0.9
            totalKm < BATTERY_REPLACE_KM -> 0.75
            else -> 0.6
        }

        val voltageFactor = when {
            voltage >= SOC_100_VOLTAGE -> 1.0
            voltage >= SOC_50_VOLTAGE -> 0.85
            voltage >= SOC_25_VOLTAGE -> 0.7
            else -> 0.5
        }

        return (CCA_NEW * ageFactor * voltageFactor).toInt().coerceIn(CCA_MIN_CRITICAL, CCA_NEW)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateDiagnosis(
        health: com.canopobd.data.model.BatteryHealth,
        chargingHealth: ChargingSystemHealth,
        input: BatteryInput,
        soc: Int,
        trend: VoltageTrend,
        ripple: Double
    ): String {
        return when (health) {
            com.canopobd.data.model.BatteryHealth.GOOD -> {
                val chargingInfo = if (chargingHealth != ChargingSystemHealth.UNKNOWN) {
                    " Ladesystem: ${chargingHealth.label}."
                } else ""
                "Batterie in Ordnung. Spannung: ${"%.2f".format(input.currentVoltage)}V, " +
                        "Ladestand: $soc%.$chargingInfo"
            }
            com.canopobd.data.model.BatteryHealth.FAIR -> {
                val issues = mutableListOf<String>()
                if (soc in 25..50) issues.add("Ladestand nur $soc%")
                if (trend == VoltageTrend.FALLING) issues.add("Spannung faellt")
                if (chargingHealth == ChargingSystemHealth.WEAK) issues.add("Ladesystem schwach")
                val detail = if (issues.isNotEmpty()) issues.joinToString(", ") else "Leichte Alterung"
                "Batterie zeigt Alterungserscheinungen: $detail."
            }
            com.canopobd.data.model.BatteryHealth.POOR -> {
                "Batterie schwach. Spannung: ${"%.2f".format(input.currentVoltage)}V, " +
                        "Ladestand: $soc%. Erneuerung empfohlen."
            }
            com.canopobd.data.model.BatteryHealth.CRITICAL -> {
                "KRITISCH: Batterie oder Ladesystem defekt! " +
                        "Spannung: ${"%.2f".format(input.currentVoltage)}V. " +
                        "Sofortige Pruefung erforderlich."
            }
        }
    }

    private fun generateRecommendation(
        health: com.canopobd.data.model.BatteryHealth,
        chargingHealth: ChargingSystemHealth,
        input: BatteryInput,
        estimatedCca: Int
    ): String {
        return when (health) {
            com.canopobd.data.model.BatteryHealth.GOOD -> {
                if (input.totalKm > BATTERY_AGE_HIGH_KM) {
                    "Batterie bei ${input.totalKm.toInt()} km: Regelmassige Pruefung empfohlen. " +
                            "Geschaetzte Kapazitaet: $estimatedCca CCA."
                } else {
                    "Keine Massnahmen erforderlich. Geschaetzte Kapazitaet: $estimatedCca CCA."
                }
            }
            com.canopobd.data.model.BatteryHealth.FAIR -> {
                val actions = mutableListOf<String>()
                actions.add("Batteriespannung unter Last pruefen")
                if (chargingHealth == ChargingSystemHealth.WEAK) {
                    actions.add("Lichtmaschine pruefen")
                }
                if (estimatedCca < CCA_MIN_HEALTHY) {
                    actions.add("Batterie-Erneuerung vorbereiten")
                }
                "Empfohlen: ${actions.joinToString("; ")}."
            }
            com.canopobd.data.model.BatteryHealth.POOR -> {
                "Batterie bald austauschen lassen. " +
                        "Kapazitaet: $estimatedCca CCA (Mindestens: $CCA_MIN_HEALTHY CCA). " +
                        "Lichtmaschine und Verkabelung pruefen."
            }
            com.canopobd.data.model.BatteryHealth.CRITICAL -> {
                "SOFORT Werkstatt aufsuchen! Batterie oder Ladesystem defekt. " +
                        "Fahrzeug kann jederzeit nicht mehr starten."
            }
        }
    }
}
