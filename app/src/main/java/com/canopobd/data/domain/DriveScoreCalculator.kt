package com.canopobd.data.domain

import com.canopobd.data.model.DriveScore
import com.canopobd.data.model.DriveSession

object DriveScoreCalculator {
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
            session.avgRpm < 1500 -> 80
            session.avgRpm < 2500 -> 100
            session.avgRpm < 3500 -> 80
            session.avgRpm < 4500 -> 60
            else -> 40
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

    fun computeScore(session: DriveSession): DriveScore {
        val score = DriveScore(
            accelerationScore = (100 - session.harshAccels * 10).coerceIn(0, 100),
            brakingScore = (100 - session.harshBrakes * 10).coerceIn(0, 100),
            cruisingScore = calculateCruisingScore(session),
            idleScore = calculateIdleScore(session),
            rpmScore = calculateRpmScore(session),
            throttleScore = calculateThrottleScore(session),
            score = 0
        )
        val avgScore = (score.accelerationScore + score.brakingScore + score.cruisingScore +
                score.idleScore + score.rpmScore + score.throttleScore) / 6
        return score.copy(score = avgScore)
    }
}
