package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TurboSpoolAnalyzerTest {

    private lateinit var analyzer: TurboSpoolAnalyzer

    @Before
    fun setup() {
        analyzer = TurboSpoolAnalyzer()
    }

    private fun createOptimalInput() = TurboSpoolAnalyzer.SpoolInput(
        throttleApplication = 80.0,
        boostAtThrottleApplication = 0.1,
        boostAt80Percent = 0.56,
        targetBoostAt80 = 0.56,
        spoolTimeSeconds = 1.5,
        wastegateDutyAtSpool = 30.0,
        wastegateDutyIdle = 90.0,
        turboRpmAtSpool = 100000.0,
        rpmAtThrottleApplication = 2000.0,
        rpmAt80PercentBoost = 52000.0,
        engineLoad = 40.0,
        intakeTemp = 25.0,
        boostPressureKpa = 70.0
    )

    @Test
    fun `analyze optimal spool returns OPTIMAL status`() {
        val result = analyzer.analyze(createOptimalInput())
        assertEquals(TurboSpoolAnalyzer.SpoolStatus.OPTIMAL, result.status)
    }

    @Test
    fun `analyze optimal spool has high health score`() {
        val result = analyzer.analyze(createOptimalInput())
        assertTrue(result.healthScore >= 80)
    }

    @Test
    fun `analyze optimal spool has correct spool time`() {
        val input = createOptimalInput()
        val result = analyzer.analyze(input)
        assertEquals(1.5, result.spoolTimeSeconds, 0.001)
    }

    @Test
    fun `analyze critical spool time returns CRITICAL status`() {
        val input = createOptimalInput().copy(spoolTimeSeconds = 4.5)
        val result = analyzer.analyze(input)
        assertEquals(TurboSpoolAnalyzer.SpoolStatus.CRITICAL, result.status)
    }

    @Test
    fun `analyze critical spool has low health score`() {
        val input = createOptimalInput().copy(spoolTimeSeconds = 5.0)
        val result = analyzer.analyze(input)
        assertTrue(result.healthScore <= 50)
    }

    @Test
    fun `analyze slow spool returns SLOW_SPOOL status`() {
        val input = createOptimalInput().copy(spoolTimeSeconds = 3.0)
        val result = analyzer.analyze(input)
        assertEquals(TurboSpoolAnalyzer.SpoolStatus.SLOW_SPOOL, result.status)
    }

    @Test
    fun `analyze poor spool returns POOR status`() {
        val input = createOptimalInput().copy(spoolTimeSeconds = 3.8, rpmAt80PercentBoost = 20000.0)
        val result = analyzer.analyze(input)
        assertEquals(TurboSpoolAnalyzer.SpoolStatus.POOR, result.status)
    }

    @Test
    fun `analyze with low wastegate response returns POOR or CRITICAL`() {
        val input = createOptimalInput().copy(
            wastegateDutyIdle = 85.0,
            wastegateDutyAtSpool = 80.0
        )
        val result = analyzer.analyze(input)
        val isPoorOrCritical = result.status == TurboSpoolAnalyzer.SpoolStatus.POOR ||
            result.status == TurboSpoolAnalyzer.SpoolStatus.CRITICAL
        assertTrue(isPoorOrCritical)
    }

    @Test
    fun `analyze with bad wastegate response under 20 returns CRITICAL`() {
        val input = createOptimalInput().copy(
            wastegateDutyIdle = 90.0,
            wastegateDutyAtSpool = 75.0
        )
        val result = analyzer.analyze(input)
        assertEquals(TurboSpoolAnalyzer.SpoolStatus.CRITICAL, result.status)
    }

    @Test
    fun `analyze calculates correct wastegate response`() {
        val input = createOptimalInput().copy(
            wastegateDutyIdle = 90.0,
            wastegateDutyAtSpool = 30.0
        )
        val result = analyzer.analyze(input)
        assertEquals(60.0, result.wastegateResponse, 0.001)
    }

    @Test
    fun `analyze calculates correct turbo acceleration`() {
        val input = createOptimalInput().copy(
            rpmAtThrottleApplication = 2000.0,
            rpmAt80PercentBoost = 102000.0,
            spoolTimeSeconds = 2.0
        )
        val result = analyzer.analyze(input)
        assertEquals(50000.0, result.turboAcceleration, 0.001)
    }

    @Test
    fun `analyze with zero spool time returns zero turbo acceleration`() {
        val input = createOptimalInput().copy(spoolTimeSeconds = 0.0)
        val result = analyzer.analyze(input)
        assertEquals(0.0, result.turboAcceleration, 0.001)
    }

    @Test
    fun `analyze with decreasing rpm returns zero turbo acceleration`() {
        val input = createOptimalInput().copy(
            rpmAtThrottleApplication = 4000.0,
            rpmAt80PercentBoost = 2000.0
        )
        val result = analyzer.analyze(input)
        assertEquals(0.0, result.turboAcceleration, 0.001)
    }

    @Test
    fun `analyze trend is STABLE when deviation is small`() {
        val input = createOptimalInput()
        val result = analyzer.analyze(input)
        assertEquals(TurboSpoolAnalyzer.SpoolTrend.STABLE, result.trendIndicator)
    }

    @Test
    fun `analyze generates non-empty diagnosis`() {
        val result = analyzer.analyze(createOptimalInput())
        assertTrue(result.diagnosis.isNotEmpty())
    }

    @Test
    fun `analyze generates non-empty recommendation`() {
        val result = analyzer.analyze(createOptimalInput())
        assertTrue(result.recommendation.isNotEmpty())
    }

    @Test
    fun `analyze optimal status recommends regular maintenance`() {
        val result = analyzer.analyze(createOptimalInput())
        assertTrue(result.recommendation.contains("Regulaerer Wartungsplan") || result.recommendation.contains("optimal"))
    }

    @Test
    fun `analyze critical status recommends immediate workshop`() {
        val input = createOptimalInput().copy(spoolTimeSeconds = 5.0)
        val result = analyzer.analyze(input)
        assertTrue(result.recommendation.contains("SOFORT") || result.recommendation.contains("Werkstatt"))
    }

    @Test
    fun `health score is between 0 and 100`() {
        val inputs = listOf(
            createOptimalInput(),
            createOptimalInput().copy(spoolTimeSeconds = 3.0),
            createOptimalInput().copy(spoolTimeSeconds = 5.0)
        )
        for (input in inputs) {
            val score = analyzer.analyze(input).healthScore
            assertTrue("Health score $score should be between 0 and 100", score in 0..100)
        }
    }

    @Test
    fun `analyze with high engine load adjusts expected spool time`() {
        val lowLoad = createOptimalInput().copy(engineLoad = 30.0, spoolTimeSeconds = 1.5)
        val highLoad = createOptimalInput().copy(engineLoad = 80.0, spoolTimeSeconds = 1.5)
        val lowResult = analyzer.analyze(lowLoad)
        val highResult = analyzer.analyze(highLoad)
        assertTrue(lowResult.healthScore >= highResult.healthScore)
    }

    @Test
    fun `analyze spool deviation is calculated correctly`() {
        val input = createOptimalInput().copy(spoolTimeSeconds = 3.0)
        val result = analyzer.analyze(input)
        assertTrue(result.spoolDeviation > 0)
    }

    @Test
    fun `compareWithBaseline returns true when degraded`() {
        val (isDegraded, deviation) = analyzer.compareWithBaseline(2.5, 1.5)
        assertTrue(isDegraded)
        assertTrue(deviation > 20.0)
    }

    @Test
    fun `compareWithBaseline returns false when not degraded`() {
        val (isDegraded, deviation) = analyzer.compareWithBaseline(1.5, 1.5)
        assertFalse(isDegraded)
        assertEquals(0.0, deviation, 0.001)
    }

    @Test
    fun `compareWithBaseline uses default baseline`() {
        val (isDegraded, _) = analyzer.compareWithBaseline(1.6)
        assertFalse(isDegraded)
    }

    @Test
    fun `compareWithBaseline deviation calculation`() {
        val (isDegraded, deviation) = analyzer.compareWithBaseline(3.0, 2.0)
        assertTrue(isDegraded)
        assertEquals(50.0, deviation, 0.001)
    }

    @Test
    fun `estimateSpoolDegradation returns OPTIMAL for low km`() {
        assertEquals(
            TurboSpoolAnalyzer.SpoolStatus.OPTIMAL,
            analyzer.estimateSpoolDegradation(10000.0)
        )
    }

    @Test
    fun `estimateSpoolDegradation returns GOOD for moderate km`() {
        assertEquals(
            TurboSpoolAnalyzer.SpoolStatus.GOOD,
            analyzer.estimateSpoolDegradation(50000.0)
        )
    }

    @Test
    fun `estimateSpoolDegradation returns SLOW_SPOOL for high km`() {
        assertEquals(
            TurboSpoolAnalyzer.SpoolStatus.SLOW_SPOOL,
            analyzer.estimateSpoolDegradation(100000.0)
        )
    }

    @Test
    fun `estimateSpoolDegradation returns POOR for very high km`() {
        assertEquals(
            TurboSpoolAnalyzer.SpoolStatus.POOR,
            analyzer.estimateSpoolDegradation(180000.0)
        )
    }

    @Test
    fun `estimateSpoolDegradation returns CRITICAL for extremely high km`() {
        assertEquals(
            TurboSpoolAnalyzer.SpoolStatus.CRITICAL,
            analyzer.estimateSpoolDegradation(250000.0)
        )
    }

    @Test
    fun `estimateSpoolDegradation boundary at 30000 km`() {
        assertEquals(
            TurboSpoolAnalyzer.SpoolStatus.GOOD,
            analyzer.estimateSpoolDegradation(30000.0)
        )
    }

    @Test
    fun `SpoolStatus enum has correct severity values`() {
        assertEquals(0, TurboSpoolAnalyzer.SpoolStatus.OPTIMAL.severity)
        assertEquals(0, TurboSpoolAnalyzer.SpoolStatus.GOOD.severity)
        assertEquals(1, TurboSpoolAnalyzer.SpoolStatus.SLOW_SPOOL.severity)
        assertEquals(2, TurboSpoolAnalyzer.SpoolStatus.POOR.severity)
        assertEquals(3, TurboSpoolAnalyzer.SpoolStatus.CRITICAL.severity)
        assertEquals(-1, TurboSpoolAnalyzer.SpoolStatus.INSUFFICIENT_DATA.severity)
    }

    @Test
    fun `SpoolStatus enum has non-empty labels`() {
        TurboSpoolAnalyzer.SpoolStatus.entries.forEach { status ->
            assertTrue(status.label.isNotEmpty())
        }
    }

    @Test
    fun `SpoolTrend enum has non-empty labels`() {
        TurboSpoolAnalyzer.SpoolTrend.entries.forEach { trend ->
            assertTrue(trend.label.isNotEmpty())
        }
    }

    @Test
    fun `analyze with default calibration`() {
        val analyzerDefault = TurboSpoolAnalyzer()
        val result = analyzerDefault.analyze(createOptimalInput())
        assertNotNull(result)
    }

    @Test
    fun `analyze with custom calibration`() {
        val customCalibration = AstraJ14TurboCalibration(normalBoostTargetBar = 0.8)
        val analyzerCustom = TurboSpoolAnalyzer(customCalibration)
        val result = analyzerCustom.analyze(createOptimalInput())
        assertNotNull(result)
    }

    @Test
    fun `analyze wastegate response is clamped to 0-100`() {
        val input = createOptimalInput().copy(
            wastegateDutyIdle = 100.0,
            wastegateDutyAtSpool = -10.0
        )
        val result = analyzer.analyze(input)
        assertTrue(result.wastegateResponse in 0.0..100.0)
    }

    @Test
    fun `analyze health score degrades with slower spool time`() {
        val fast = analyzer.analyze(createOptimalInput().copy(spoolTimeSeconds = 1.5))
        val medium = analyzer.analyze(createOptimalInput().copy(spoolTimeSeconds = 2.5))
        val slow = analyzer.analyze(createOptimalInput().copy(spoolTimeSeconds = 3.5))
        assertTrue(fast.healthScore >= medium.healthScore)
        assertTrue(medium.healthScore >= slow.healthScore)
    }
}
