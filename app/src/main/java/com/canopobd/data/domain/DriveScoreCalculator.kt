package com.canopobd.data.domain

import com.canopobd.data.model.DriveScore
import com.canopobd.data.model.DriveSession
import kotlin.math.abs
import kotlin.math.sqrt

object DriveScoreCalculator {

    private const val NORMAL_ACCEL_MIN_THROTTLE = 50.0
    private const val AGGRESSIVE_THROTTLE = 70.0
    private const val HARSH_THROTTLE = 85.0
    private const val HARSH_RPM_THRESHOLD = 4000.0

    private const val OPTIMAL_BOOST_LOW = 0.4
    private const val OPTIMAL_BOOST_HIGH = 0.7
    private const val HIGH_BOOST_NO_LOAD = 0.9
    private const val LOW_BOOST = 0.15

    private const val A14NET_ECO_RPM_LOW = 1500.0
    private const val A14NET_ECO_RPM_HIGH = 3000.0
    private const val A14NET_RPM_PENALTY = 4500.0

    fun calculateCruisingScore(session: DriveSession): Int {
        return if (session.avgSpeed > 0) {
            ((session.speedSamples / (session.speedSamples + session.harshAccels + session.harshBrakes)) * 100).toInt().coerceIn(0, 100)
        } else 50
    }

    fun calculateIdleScore(session: DriveSession): Int {
        val totalSeconds = if (session.endTime > 0) (session.endTime - session.startTime) / 1000 else 0L
        return if (totalSeconds > 0) {
            ((1.0 - (session.idleTimeSeconds.toDouble() / totalSeconds)) * 100).toInt().coerceIn(0, 100)
        } else 50
    }

    fun calculateRpmScore(session: DriveSession): Int {
        return when {
            session.avgRpm < A14NET_ECO_RPM_LOW -> 80
            session.avgRpm < A14NET_ECO_RPM_HIGH -> 100
            session.avgRpm < 3500.0 -> 80
            session.avgRpm < A14NET_RPM_PENALTY -> 55
            else -> 30
        }
    }

    fun calculateThrottleScore(session: DriveSession): Int {
        return when {
            session.avgThrottle < 30 -> 100
            session.avgThrottle < 50 -> 80
            session.avgThrottle < 70 -> 60
            else -> 40
        }
    }

    fun calculateAccelerationScore(session: DriveSession): Int {
        if (session.harshAccels == 0 && session.harshBrakes == 0) return 100

        var score = 100

        score -= session.harshAccels * 12

        score -= session.harshBrakes * 10

        val aggressiveAccelCount = ((session.throttleSamples * session.rpmSamples) /
            (session.avgRpm.coerceAtLeast(1.0) * session.avgThrottle.coerceAtLeast(1.0))).toInt()
        val harshAccelCount = if (session.avgThrottle > HARSH_THROTTLE && session.avgRpm > HARSH_RPM_THRESHOLD)
            aggressiveAccelCount.coerceAtMost(session.harshAccels) else 0
        score -= harshAccelCount * 15

        return score.coerceIn(0, 100)
    }

    fun calculateBoostScore(session: DriveSession): Int {
        if (session.boostSampleCount == 0) return 50

        var score = 70

        val boostRatio = session.optimalBoostTime.toDouble() / session.boostSampleCount
        score += (boostRatio * 30).toInt().coerceIn(0, 30)

        val highBoostRatio = session.highBoostTime.toDouble() / session.boostSampleCount
        score -= (highBoostRatio * 40).toInt().coerceIn(0, 30)

        if (session.boostSampleCount > 2) {
            val variance = session.boostSumOfSquares / session.boostSampleCount -
                (session.avgBoostBar * session.avgBoostBar)
            val stddev = sqrt(variance.coerceAtLeast(0.0))
            if (stddev > 0.15) score -= 10
            if (stddev > 0.3) score -= 10
            if (stddev < 0.05) score += 5
        }

        return score.coerceIn(0, 100)
    }

    fun calculateEcoScore(session: DriveSession): Int {
        if (session.rpmSamples == 0.0) return 50

        var score = 80

        val rpmInOptimalBand = session.avgRpm in A14NET_ECO_RPM_LOW..A14NET_ECO_RPM_HIGH
        if (rpmInOptimalBand) {
            score += 15
        } else if (session.avgRpm < A14NET_ECO_RPM_LOW) {
            score += 5
        }

        val rpmOverPenaltyRatio = session.rpmAbove4500Samples.toDouble() /
            (session.boostSampleCount.coerceAtLeast(1))
        score -= (rpmOverPenaltyRatio * 30).toInt().coerceIn(0, 25)

        if (session.deceleratingSamples > 0) {
            val coastRatio = session.coastingInGearSamples.toDouble() / session.deceleratingSamples
            score += (coastRatio * 10).toInt().coerceIn(0, 10)
        }

        return score.coerceIn(0, 100)
    }

    fun calculateTurboHealthScore(session: DriveSession): Int {
        var score = 100

        if (session.boostSampleCount > 2) {
            val variance = session.boostSumOfSquares / session.boostSampleCount -
                (session.avgBoostBar * session.avgBoostBar)
            val stddev = sqrt(variance.coerceAtLeast(0.0))
            when {
                stddev > 0.4 -> score -= 30
                stddev > 0.25 -> score -= 20
                stddev > 0.15 -> score -= 10
                stddev < 0.05 -> score += 5
            }
        }

        if (session.wastegateSampleCount > 0) {
            val avgWastegate = session.wastegateDutySum / session.wastegateSampleCount
            when {
                avgWastegate > 90.0 -> score -= 15
                avgWastegate < 10.0 && session.avgBoostBar > LOW_BOOST -> score -= 15
                avgWastegate < 5.0 -> score -= 20
                avgWastegate in 25.0..65.0 -> score += 5
            }
        }

        if (session.rpmRateSampleCount > 2) {
            val avgSpoolRate = session.rpmRateSamples / session.rpmRateSampleCount
            when {
                avgSpoolRate > 5000.0 -> score -= 15
                avgSpoolRate > 3000.0 -> score -= 5
                avgSpoolRate in 800.0..2000.0 -> score += 5
            }
        }

        return score.coerceIn(0, 100)
    }

    fun computeScore(session: DriveSession): DriveScore {
        val boostScore = calculateBoostScore(session)
        val ecoScore = calculateEcoScore(session)
        val turboHealthScore = calculateTurboHealthScore(session)

        val score = DriveScore(
            accelerationScore = calculateAccelerationScore(session),
            brakingScore = (100 - session.harshBrakes * 10).coerceIn(0, 100),
            cruisingScore = calculateCruisingScore(session),
            idleScore = calculateIdleScore(session),
            rpmScore = calculateRpmScore(session),
            throttleScore = calculateThrottleScore(session),
            boostScore = boostScore,
            ecoScore = ecoScore,
            turboHealthScore = turboHealthScore,
            score = 0
        )
        val avgScore = (score.accelerationScore + score.brakingScore + score.cruisingScore +
                score.idleScore + score.rpmScore + score.throttleScore +
                boostScore + ecoScore + turboHealthScore) / 9
        return score.copy(score = avgScore)
    }
}
