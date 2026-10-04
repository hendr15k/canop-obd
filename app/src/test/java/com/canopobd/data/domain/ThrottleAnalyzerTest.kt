package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TDD RED: Drosselklappen-/Pedal-Analyse für Opel Astra J 1.4 Turbo (A14NET).
 * Bewertet Drive-by-Wire-Plausibilität zwischen Pedal und Klappe.
 */
class ThrottleAnalyzerTest {

    private lateinit var analyzer: ThrottleAnalyzer

    @Before
    fun setup() {
        analyzer = ThrottleAnalyzer()
    }

    @Test
    fun `consistent pedal and throttle is healthy`() {
        val result = analyzer.analyze(
            ThrottleAnalyzer.ThrottleInput(
                throttle = 45.0,
                acceleratorPedal = 45.0,
                rpm = 2500.0,
                engineLoad = 55.0
            )
        )

        assertEquals(ThrottleAnalyzer.ThrottleHealth.HEALTHY, result.health)
        assertTrue(result.healthScore >= 80)
        assertTrue(result.detectedIssues.isEmpty())
    }

    @Test
    fun `missing pedal signal reports no data`() {
        val result = analyzer.analyze(
            ThrottleAnalyzer.ThrottleInput(
                throttle = 0.0,
                acceleratorPedal = 0.0,
                rpm = 0.0,
                engineLoad = 0.0
            )
        )

        assertEquals(ThrottleAnalyzer.ThrottleHealth.UNKNOWN, result.health)
        assertEquals(0, result.healthScore)
    }

    @Test
    fun `engine off with open throttle still reports no data`() {
        val result = analyzer.analyze(
            ThrottleAnalyzer.ThrottleInput(
                throttle = 30.0,
                acceleratorPedal = 10.0,
                rpm = 0.0,
                engineLoad = 0.0
            )
        )

        assertEquals(ThrottleAnalyzer.ThrottleHealth.UNKNOWN, result.health)
        assertEquals(0, result.healthScore)
    }

    @Test
    fun `pedal throttle mismatch raises correlation issue`() {
        val result = analyzer.analyze(
            ThrottleAnalyzer.ThrottleInput(
                throttle = 80.0,
                acceleratorPedal = 20.0,
                rpm = 2500.0,
                engineLoad = 60.0
            )
        )

        assertTrue(result.detectedIssues.contains(ThrottleAnalyzer.ThrottleIssue.PEDAL_THROTTLE_MISMATCH))
        assertTrue(result.healthScore < 80)
    }

    @Test
    fun `stuck throttle at high load is critical`() {
        val result = analyzer.analyze(
            ThrottleAnalyzer.ThrottleInput(
                throttle = 98.0,
                acceleratorPedal = 0.0,
                rpm = 5000.0,
                engineLoad = 95.0
            )
        )

        assertEquals(ThrottleAnalyzer.ThrottleHealth.CRITICAL, result.health)
        assertTrue(result.detectedIssues.contains(ThrottleAnalyzer.ThrottleIssue.STUCK_THROTTLE))
    }

    @Test
    fun `throttle dtc flags actuator issue`() {
        val result = analyzer.analyze(
            ThrottleAnalyzer.ThrottleInput(
                throttle = 30.0,
                acceleratorPedal = 30.0,
                rpm = 2000.0,
                engineLoad = 40.0,
                activeDTCs = listOf("P2135")
            )
        )

        assertTrue(result.detectedIssues.contains(ThrottleAnalyzer.ThrottleIssue.ACTUATOR_FAULT))
    }

    @Test
    fun `idle throttle jump raises hunting issue`() {
        val result = analyzer.analyze(
            ThrottleAnalyzer.ThrottleInput(
                throttle = 22.0,
                acceleratorPedal = 0.0,
                rpm = 800.0,
                engineLoad = 25.0
            )
        )

        assertTrue(result.detectedIssues.contains(ThrottleAnalyzer.ThrottleIssue.IDLE_HUNTING))
    }
}
