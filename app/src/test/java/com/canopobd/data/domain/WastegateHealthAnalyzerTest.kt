package com.canopobd.data.domain

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WastegateHealthAnalyzerTest {

    private lateinit var analyzer: WastegateHealthAnalyzer

    @Before
    fun setup() {
        analyzer = WastegateHealthAnalyzer()
    }

    @Test
    fun `healthy wastegate at idle`() {
        val result = analyzer.analyze(
            wastegateDuty = 90.0,
            avgWastegateDuty = 88.0,
            targetBoost = 0.0,
            actualBoost = 0.0,
            rpm = 750.0,
            engineLoad = 15.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.HEALTHY, result.condition)
        assertEquals(100, result.healthScore)
        assertEquals(90.0, result.currentDutyPercent, 0.001)
        assertEquals(88.0, result.avgDutyPercent, 0.001)
    }

    @Test
    fun `healthy wastegate at WOT`() {
        val result = analyzer.analyze(
            wastegateDuty = 40.0,
            avgWastegateDuty = 42.0,
            targetBoost = 1.0,
            actualBoost = 1.0,
            rpm = 5500.0,
            engineLoad = 95.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.HEALTHY, result.condition)
        assertEquals(100, result.healthScore)
    }

    @Test
    fun `stuck open detection`() {
        val result = analyzer.analyze(
            wastegateDuty = 97.0,
            avgWastegateDuty = 96.0,
            targetBoost = 1.0,
            actualBoost = 0.5,
            rpm = 5000.0,
            engineLoad = 85.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.STUCK_OPEN, result.condition)
        assertEquals(20, result.healthScore)
        assertTrue(result.diagnosis.contains("offen"))
    }

    @Test
    fun `stuck closed detection - very low duty`() {
        val result = analyzer.analyze(
            wastegateDuty = 3.0,
            avgWastegateDuty = 4.0,
            targetBoost = 1.0,
            actualBoost = 1.3,
            rpm = 5500.0,
            engineLoad = 90.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.STUCK_CLOSED, result.condition)
        assertEquals(30, result.healthScore)
        assertTrue(result.diagnosis.contains("geschlossen"))
    }

    @Test
    fun `wastegate leak detection`() {
        val result = analyzer.analyze(
            wastegateDuty = 30.0,
            avgWastegateDuty = 35.0,
            targetBoost = 1.0,
            actualBoost = 0.6,
            rpm = 4000.0,
            engineLoad = 70.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.WASTEGATE_LEAK, result.condition)
        assertEquals(40, result.healthScore)
    }

    @Test
    fun `overboost with high duty detects stuck closed variant`() {
        val result = analyzer.analyze(
            wastegateDuty = 80.0,
            avgWastegateDuty = 78.0,
            targetBoost = 1.0,
            actualBoost = 1.2,
            rpm = 5000.0,
            engineLoad = 80.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.STUCK_CLOSED, result.condition)
        assertEquals(45, result.healthScore)
        assertTrue(result.diagnosis.contains("schließt"))
    }

    @Test
    fun `abnormal pattern at high load with high duty and overboost`() {
        val result = analyzer.analyze(
            wastegateDuty = 75.0,
            avgWastegateDuty = 73.0,
            targetBoost = 1.0,
            actualBoost = 1.15,
            rpm = 5500.0,
            engineLoad = 80.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.SOLENOID_ISSUE, result.condition)
        assertEquals(50, result.healthScore)
        assertTrue(result.diagnosis.contains("Regelung abnormal"))
    }

    @Test
    fun `boost deviation calculated correctly`() {
        val result = analyzer.analyze(
            wastegateDuty = 50.0,
            avgWastegateDuty = 50.0,
            targetBoost = 1.0,
            actualBoost = 0.85,
            rpm = 3000.0,
            engineLoad = 60.0
        )
        val expectedDeviation = ((0.85 - 1.0) / 1.0) * 100.0
        assertEquals(expectedDeviation, result.boostDeviation, 0.01)
    }

    @Test
    fun `boost deviation is zero when target boost is zero`() {
        val result = analyzer.analyze(
            wastegateDuty = 50.0,
            avgWastegateDuty = 50.0,
            targetBoost = 0.0,
            actualBoost = 0.0,
            rpm = 750.0,
            engineLoad = 15.0
        )
        assertEquals(0.0, result.boostDeviation, 0.001)
    }

    @Test
    fun `normal wastegate behavior across RPM range`() {
        val duties = listOf(90.0, 70.0, 50.0, 35.0, 40.0)
        val rpms = listOf(800.0, 2000.0, 3500.0, 5000.0, 6000.0)

        for (i in duties.indices) {
            val result = analyzer.analyze(
                wastegateDuty = duties[i],
                avgWastegateDuty = duties[i],
                targetBoost = 0.8,
                actualBoost = 0.8,
                rpm = rpms[i],
                engineLoad = 50.0
            )
            assertEquals(
                WastegateHealthAnalyzer.WastegateCondition.HEALTHY,
                result.condition
            )
        }
    }

    @Test
    fun `analysis stores current and average duty`() {
        val result = analyzer.analyze(
            wastegateDuty = 55.0,
            avgWastegateDuty = 52.5,
            targetBoost = 0.9,
            actualBoost = 0.88,
            rpm = 3000.0,
            engineLoad = 60.0
        )
        assertEquals(55.0, result.currentDutyPercent, 0.001)
        assertEquals(52.5, result.avgDutyPercent, 0.001)
    }

    @Test
    fun `WastegateCondition enum has expected values`() {
        val values = WastegateHealthAnalyzer.WastegateCondition.entries
        assertEquals(7, values.size)
        assertTrue(values.contains(WastegateHealthAnalyzer.WastegateCondition.HEALTHY))
        assertTrue(values.contains(WastegateHealthAnalyzer.WastegateCondition.STUCK_OPEN))
        assertTrue(values.contains(WastegateHealthAnalyzer.WastegateCondition.STUCK_CLOSED))
        assertTrue(values.contains(WastegateHealthAnalyzer.WastegateCondition.WASTEGATE_LEAK))
        assertTrue(values.contains(WastegateHealthAnalyzer.WastegateCondition.SOLENOID_ISSUE))
        assertTrue(values.contains(WastegateHealthAnalyzer.WastegateCondition.SENSOR_FAULT))
        assertTrue(values.contains(WastegateHealthAnalyzer.WastegateCondition.UNKNOWN))
    }

    @Test
    fun `analysis at boundary duty values`() {
        val atMinBoundary = analyzer.analyze(
            wastegateDuty = 5.0,
            avgWastegateDuty = 5.0,
            targetBoost = 1.0,
            actualBoost = 1.0,
            rpm = 4000.0,
            engineLoad = 70.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.HEALTHY, atMinBoundary.condition)

        val atMaxBoundary = analyzer.analyze(
            wastegateDuty = 95.0,
            avgWastegateDuty = 95.0,
            targetBoost = 1.0,
            actualBoost = 1.0,
            rpm = 4000.0,
            engineLoad = 70.0
        )
        assertEquals(WastegateHealthAnalyzer.WastegateCondition.HEALTHY, atMaxBoundary.condition)
    }

    @Test
    fun `recommendation is non-empty for all conditions`() {
        val conditions = mapOf(
            Triple(97.0, 5000.0, 85.0) to WastegateHealthAnalyzer.WastegateCondition.STUCK_OPEN,
            Triple(3.0, 5500.0, 90.0) to WastegateHealthAnalyzer.WastegateCondition.STUCK_CLOSED,
            Triple(50.0, 3000.0, 60.0) to WastegateHealthAnalyzer.WastegateCondition.HEALTHY
        )
        for ((params, expectedCondition) in conditions) {
            val result = analyzer.analyze(
                wastegateDuty = params.first,
                avgWastegateDuty = params.first,
                targetBoost = 1.0,
                actualBoost = if (expectedCondition == WastegateHealthAnalyzer.WastegateCondition.STUCK_OPEN) {
                    0.5
                } else {
                    1.0
                },
                rpm = params.second,
                engineLoad = params.third
            )
            assertEquals(expectedCondition, result.condition)
            assertTrue(result.recommendation.isNotEmpty())
        }
    }
}
