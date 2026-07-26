package com.canopobd.data.domain

import com.canopobd.data.model.DriveSession
import org.junit.Assert.*
import org.junit.Test

class DriveScoreCalculatorTest {

    private fun ecoSession() = DriveSession(
        avgRpm = 2200.0,
        avgThrottle = 25.0,
        avgSpeed = 70.0,
        rpmSampleCount = 100,
        rpmSamples = 220000.0,
        throttleSampleCount = 100,
        throttleSamples = 2500.0,
        speedSampleCount = 100,
        speedSamples = 7000.0,
        harshAccels = 0,
        harshBrakes = 0,
        boostSampleCount = 50,
        boostSamples = 25.0,
        avgBoostBar = 0.5,
        optimalBoostTime = 40,
        highBoostTime = 0,
        boostSumOfSquares = 13.0,
        wastegateDutySum = 2000.0,
        wastegateSampleCount = 50,
        rpmRateSamples = 50000.0,
        rpmRateSampleCount = 100,
        coastingInGearSamples = 10,
        deceleratingSamples = 15,
        rpmAbove4500Samples = 0
    )

    @Test
    fun `calculateRpmScore returns 100 for optimal band`() {
        val session = ecoSession()
        assertEquals(100, DriveScoreCalculator.calculateRpmScore(session))
    }

    @Test
    fun `calculateRpmScore returns 80 for low rpm`() {
        val session = ecoSession().copy(avgRpm = 1200.0)
        assertEquals(80, DriveScoreCalculator.calculateRpmScore(session))
    }

    @Test
    fun `calculateRpmScore returns 30 for very high rpm`() {
        val session = ecoSession().copy(avgRpm = 5000.0)
        assertEquals(30, DriveScoreCalculator.calculateRpmScore(session))
    }

    @Test
    fun `calculateThrottleScore returns 100 for gentle throttle`() {
        val session = ecoSession().copy(avgThrottle = 20.0)
        assertEquals(100, DriveScoreCalculator.calculateThrottleScore(session))
    }

    @Test
    fun `calculateThrottleScore returns 40 for aggressive throttle`() {
        val session = ecoSession().copy(avgThrottle = 80.0)
        assertEquals(40, DriveScoreCalculator.calculateThrottleScore(session))
    }

    @Test
    fun `calculateAccelerationScore returns 100 with no harsh events`() {
        val session = ecoSession()
        assertEquals(100, DriveScoreCalculator.calculateAccelerationScore(session))
    }

    @Test
    fun `calculateAccelerationScore penalizes harsh accels`() {
        val session = ecoSession().copy(harshAccels = 3)
        val score = DriveScoreCalculator.calculateAccelerationScore(session)
        assertTrue(score < 100)
        assertEquals(64, score)
    }

    @Test
    fun `calculateAccelerationScore penalizes harsh brakes`() {
        val session = ecoSession().copy(harshBrakes = 2)
        val score = DriveScoreCalculator.calculateAccelerationScore(session)
        assertEquals(80, score)
    }

    @Test
    fun `calculateBoostScore returns 50 with no samples`() {
        val session = ecoSession().copy(boostSampleCount = 0)
        assertEquals(50, DriveScoreCalculator.calculateBoostScore(session))
    }

    @Test
    fun `calculateBoostScore rewards optimal boost ratio`() {
        val session = ecoSession().copy(
            boostSampleCount = 100,
            optimalBoostTime = 90,
            highBoostTime = 0,
            avgBoostBar = 0.55,
            boostSumOfSquares = 30.0
        )
        val score = DriveScoreCalculator.calculateBoostScore(session)
        assertTrue(score >= 90)
    }

    @Test
    fun `calculateBoostScore penalizes high boost`() {
        val session = ecoSession().copy(
            boostSampleCount = 100,
            optimalBoostTime = 10,
            highBoostTime = 80,
            avgBoostBar = 1.0,
            boostSumOfSquares = 100.0
        )
        val score = DriveScoreCalculator.calculateBoostScore(session)
        assertTrue(score < 50)
    }

    @Test
    fun `calculateEcoScore returns 50 with no rpm samples`() {
        val session = ecoSession().copy(rpmSamples = 0.0)
        assertEquals(50, DriveScoreCalculator.calculateEcoScore(session))
    }

    @Test
    fun `calculateEcoScore rewards optimal rpm band`() {
        val session = ecoSession().copy(avgRpm = 2200.0, rpmAbove4500Samples = 0)
        val score = DriveScoreCalculator.calculateEcoScore(session)
        assertTrue(score >= 90)
    }

    @Test
    fun `calculateEcoScore penalizes high rpm samples`() {
        val session = ecoSession().copy(
            avgRpm = 4800.0,
            rpmAbove4500Samples = 80,
            rpmSampleCount = 100
        )
        val score = DriveScoreCalculator.calculateEcoScore(session)
        assertTrue(score < 70)
    }

    @Test
    fun `calculateTurboHealthScore returns 100 for clean session`() {
        val session = ecoSession().copy(
            boostSampleCount = 2,
            wastegateSampleCount = 0,
            rpmRateSampleCount = 2
        )
        assertEquals(100, DriveScoreCalculator.calculateTurboHealthScore(session))
    }

    @Test
    fun `calculateTurboHealthScore penalizes high wastegate duty`() {
        val session = ecoSession().copy(
            wastegateSampleCount = 50,
            wastegateDutySum = 4750.0
        )
        val score = DriveScoreCalculator.calculateTurboHealthScore(session)
        assertTrue(score < 100)
    }

    @Test
    fun `calculateTurboHealthScore rewards normal wastegate range`() {
        val session = ecoSession().copy(
            boostSampleCount = 2,
            wastegateSampleCount = 50,
            wastegateDutySum = 2250.0,
            rpmRateSampleCount = 2
        )
        val score = DriveScoreCalculator.calculateTurboHealthScore(session)
        assertTrue(score >= 100)
    }

    @Test
    fun `calculateCruisingScore returns 50 with no speed`() {
        val session = ecoSession().copy(avgSpeed = 0.0)
        assertEquals(50, DriveScoreCalculator.calculateCruisingScore(session))
    }

    @Test
    fun `calculateCruisingScore high with few harsh events`() {
        val session = ecoSession().copy(
            speedSampleCount = 100,
            harshAccels = 1,
            harshBrakes = 1
        )
        val score = DriveScoreCalculator.calculateCruisingScore(session)
        assertTrue(score >= 90)
    }

    @Test
    fun `calculateIdleScore returns 50 with no time`() {
        val session = ecoSession().copy(endTime = 0L)
        assertEquals(50, DriveScoreCalculator.calculateIdleScore(session))
    }

    @Test
    fun `calculateIdleScore rewards low idle time`() {
        val session = ecoSession().copy(
            startTime = 0L,
            endTime = 600000L,
            idleTimeSeconds = 60L
        )
        val score = DriveScoreCalculator.calculateIdleScore(session)
        assertEquals(90, score)
    }

    @Test
    fun `computeScore returns average of all sub-scores`() {
        val session = ecoSession()
        val score = DriveScoreCalculator.computeScore(session)

        val expectedAvg = (score.accelerationScore + score.brakingScore + score.cruisingScore +
            score.idleScore + score.rpmScore + score.throttleScore +
            score.boostScore + score.ecoScore + score.turboHealthScore) / 9
        assertEquals(expectedAvg, score.score)
    }

    @Test
    fun `computeScore returns valid grade`() {
        val session = ecoSession()
        val score = DriveScoreCalculator.computeScore(session)
        assertTrue(score.grade in listOf("A+", "A", "B", "C", "D", "F"))
    }

    @Test
    fun `all scores are in range 0 to 100`() {
        val session = ecoSession().copy(
            harshAccels = 20,
            harshBrakes = 20,
            avgRpm = 6000.0,
            avgThrottle = 95.0,
            highBoostTime = 100,
            rpmAbove4500Samples = 100
        )
        val score = DriveScoreCalculator.computeScore(session)

        assertTrue(score.accelerationScore in 0..100)
        assertTrue(score.brakingScore in 0..100)
        assertTrue(score.cruisingScore in 0..100)
        assertTrue(score.idleScore in 0..100)
        assertTrue(score.rpmScore in 0..100)
        assertTrue(score.throttleScore in 0..100)
        assertTrue(score.boostScore in 0..100)
        assertTrue(score.ecoScore in 0..100)
        assertTrue(score.turboHealthScore in 0..100)
        assertTrue(score.score in 0..100)
    }
}
