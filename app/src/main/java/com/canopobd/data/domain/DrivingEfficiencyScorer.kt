package com.canopobd.data.domain

class DrivingEfficiencyScorer {

    data class EfficiencyScore(
        val overall: Int = 0,
        val accelerationScore: Int = 0,
        val cruisingScore: Int = 0,
        val brakingScore: Int = 0,
        val gearUsageScore: Int = 0,
        val rpmEfficiencyScore: Int = 0,
        val throttleScore: Int = 0,
        val tips: List<String> = emptyList()
    )

    data class DriveSessionData(
        val avgRpm: Double = 0.0,
        val maxRpm: Double = 0.0,
        val avgSpeed: Double = 0.0,
        val maxSpeed: Double = 0.0,
        val avgThrottle: Double = 0.0,
        val maxThrottle: Double = 0.0,
        val avgLoad: Double = 0.0,
        val idleTimePercent: Double = 0.0,
        val harshAccelerations: Int = 0,
        val harshBrakes: Int = 0,
        val totalSamples: Int = 0,
        val avgBoostBar: Double = 0.0,
        val coastingSamples: Int = 0,
        val rpmDistribution: Map<String, Int> = emptyMap()
    )

    companion object {
        private const val OPTIMAL_RPM_MIN = 1500.0
        private const val OPTIMAL_RPM_MAX = 3000.0
        private const val POWER_BAND_RPM_MIN = 4500.0
        private const val POWER_BAND_RPM_MAX = 5500.0
        private const val REDLINE_RPM = 6500.0
        private const val OPTIMAL_SPEED_MIN = 50.0
        private const val OPTIMAL_SPEED_MAX = 110.0
    }

    fun calculateScore(data: DriveSessionData): EfficiencyScore {
        val acceleration = calculateAccelerationScore(data)
        val cruising = calculateCruisingScore(data)
        val braking = calculateBrakingScore(data)
        val gearUsage = calculateGearUsageScore(data)
        val rpmEfficiency = calculateRpmEfficiencyScore(data)
        val throttle = calculateThrottleScore(data)

        val overall = (acceleration + cruising + braking + gearUsage + rpmEfficiency + throttle) / 6

        val tips = generateTips(EfficiencyScore(
            overall = overall,
            accelerationScore = acceleration,
            cruisingScore = cruising,
            brakingScore = braking,
            gearUsageScore = gearUsage,
            rpmEfficiencyScore = rpmEfficiency,
            throttleScore = throttle
        ), data)

        return EfficiencyScore(
            overall = overall,
            accelerationScore = acceleration,
            cruisingScore = cruising,
            brakingScore = braking,
            gearUsageScore = gearUsage,
            rpmEfficiencyScore = rpmEfficiency,
            throttleScore = throttle,
            tips = tips
        )
    }

    private fun calculateAccelerationScore(data: DriveSessionData): Int {
        if (data.totalSamples == 0) {
            return 50
        }
        var score = 100
        score -= data.harshAccelerations * 8
        val throttlePenalty = (data.avgThrottle / 100.0 * 20).toInt()
        score -= throttlePenalty.coerceAtMost(25)
        if (data.maxRpm > REDLINE_RPM) {
            score -= 15
        } else if (data.maxRpm > POWER_BAND_RPM_MAX) {
            score -= 5
        }
        return score.coerceIn(0, 100)
    }

    private fun calculateCruisingScore(data: DriveSessionData): Int {
        if (data.totalSamples == 0) {
            return 50
        }
        var score = 80
        val optimalSpeedRatio = if (data.avgSpeed in OPTIMAL_SPEED_MIN..OPTIMAL_SPEED_MAX) {
            1.0
        } else {
            val deviation = if (data.avgSpeed < OPTIMAL_SPEED_MIN) {
                (OPTIMAL_SPEED_MIN - data.avgSpeed) / OPTIMAL_SPEED_MIN
            } else {
                (data.avgSpeed - OPTIMAL_SPEED_MAX) / OPTIMAL_SPEED_MAX
            }
            (1.0 - deviation).coerceAtLeast(0.3)
        }
        score = (score * optimalSpeedRatio).toInt()
        if (data.coastingSamples > 0 && data.totalSamples > 0) {
            val coastingRatio = data.coastingSamples.toDouble() / data.totalSamples
            score += (coastingRatio * 15).toInt()
        }
        return score.coerceIn(0, 100)
    }

    private fun calculateBrakingScore(data: DriveSessionData): Int {
        if (data.totalSamples == 0) {
            return 50
        }
        var score = 100
        score -= data.harshBrakes * 6
        if (data.avgSpeed > 80 && data.harshBrakes > 0) {
            score -= 10
        }
        return score.coerceIn(0, 100)
    }

    private fun calculateGearUsageScore(data: DriveSessionData): Int {
        if (data.totalSamples == 0) {
            return 50
        }
        var score = 70
        val rpmInOptimal = data.rpmDistribution["2k-3k"] ?: 0
        val rpmInPowerBand = (data.rpmDistribution["4k-5k"] ?: 0) + (data.rpmDistribution["5k+"] ?: 0)
        val totalRpmSamples = data.rpmDistribution.values.sum().coerceAtLeast(1)
        val optimalRatio = rpmInOptimal.toDouble() / totalRpmSamples
        val powerBandRatio = rpmInPowerBand.toDouble() / totalRpmSamples
        score += (optimalRatio * 20).toInt()
        score -= (powerBandRatio * 15).toInt()
        return score.coerceIn(0, 100)
    }

    private fun calculateRpmEfficiencyScore(data: DriveSessionData): Int {
        if (data.avgRpm <= 0) {
            return 50
        }
        var score = when {
            data.avgRpm < OPTIMAL_RPM_MIN -> 60
            data.avgRpm <= OPTIMAL_RPM_MAX -> 100
            data.avgRpm < POWER_BAND_RPM_MIN -> 80
            data.avgRpm < POWER_BAND_RPM_MAX -> 60
            else -> 40
        }
        return score.coerceIn(0, 100)
    }

    private fun calculateThrottleScore(data: DriveSessionData): Int {
        if (data.totalSamples == 0) {
            return 50
        }
        var score = when {
            data.avgThrottle < 40 -> 100
            data.avgThrottle < 60 -> 85
            data.avgThrottle < 80 -> 70
            else -> 55
        }
        if (data.maxThrottle > 90) {
            score -= 10
        }
        return score.coerceIn(0, 100)
    }

    fun generateTips(score: EfficiencyScore, data: DriveSessionData): List<String> {
        val tips = mutableListOf<String>()
        if (score.accelerationScore < 70) {
            tips.add("Gaspedal gleichmäßiger betätigen")
        }
        if (score.cruisingScore < 70) {
            tips.add("Geschwindigkeit zwischen 50-110 km/h halten")
        }
        if (score.brakingScore < 70) {
            tips.add("Vorausschauend fahren - Bremsungen vermeiden")
        }
        if (score.rpmEfficiencyScore < 70) {
            tips.add("Drehzahl im Bereich 1500-3000 halten")
        }
        if (data.idleTimePercent > 20) {
            tips.add("Motor im Stand nicht im Leerlauf lassen")
        }
        if (tips.isEmpty()) {
            tips.add("Fahrstil ist effizient!")
        }
        return tips.take(4)
    }

    fun getEfficiencyGrade(score: Int): Char {
        return when {
            score >= 90 -> 'A'
            score >= 80 -> 'B'
            score >= 70 -> 'C'
            score >= 60 -> 'D'
            else -> 'F'
        }
    }
}
