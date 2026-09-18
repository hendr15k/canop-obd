package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the new EngineWarmupMonitor (cold-start protection).
 */
class EngineWarmupMonitorTest {

    private lateinit var monitor: EngineWarmupMonitor

    @Before
    fun setup() {
        monitor = EngineWarmupMonitor()
    }

    @Test
    fun `cold coolant reports cold phase`() {
        val result = monitor.analyze(
            EngineWarmupMonitor.WarmupInput(coolantTemp = 25.0, rpm = 1200.0)
        )

        assertEquals(EngineWarmupMonitor.WarmupPhase.COLD, result.phase)
        assertEquals(3000, result.recommendedMaxRpm)
        assertEquals(0.0, result.recommendedMaxBoostBar, 0.001)
        assertTrue(result.warmupProgress in 0.0..1.0)
    }

    @Test
    fun `operating temperature reports warm phase with full release`() {
        val result = monitor.analyze(
            EngineWarmupMonitor.WarmupInput(coolantTemp = 92.0, rpm = 2500.0)
        )

        assertEquals(EngineWarmupMonitor.WarmupPhase.WARM, result.phase)
        assertEquals(0, result.coldViolations)
        assertEquals(100, result.healthScore)
    }

    @Test
    fun `warming phase allows moderate limits`() {
        val result = monitor.analyze(
            EngineWarmupMonitor.WarmupInput(coolantTemp = 70.0, rpm = 3000.0)
        )

        assertEquals(EngineWarmupMonitor.WarmupPhase.WARMING, result.phase)
        assertEquals(4500, result.recommendedMaxRpm)
        assertEquals(0.5, result.recommendedMaxBoostBar, 0.001)
    }

    @Test
    fun `overrev on cold engine counts violations`() {
        val gentle = monitor.analyze(
            EngineWarmupMonitor.WarmupInput(coolantTemp = 25.0, rpm = 2000.0)
        )
        val harsh = monitor.analyze(
            EngineWarmupMonitor.WarmupInput(
                coolantTemp = 25.0,
                rpm = 5000.0,
                boostBar = 0.6,
                engineLoad = 80.0
            )
        )

        assertEquals(0, gentle.coldViolations)
        assertEquals(100, gentle.healthScore)
        assertTrue(harsh.coldViolations > 0)
        assertTrue(harsh.healthScore < gentle.healthScore)
    }

    @Test
    fun `missing coolant signal reports no data`() {
        val result = monitor.analyze(EngineWarmupMonitor.WarmupInput(coolantTemp = 0.0))

        assertEquals(EngineWarmupMonitor.WarmupPhase.NO_DATA, result.phase)
        assertEquals(0, result.healthScore)
        assertEquals(0.0, result.warmupProgress, 0.001)
    }

    @Test
    fun `progress reaches one at operating temperature`() {
        val result = monitor.analyze(EngineWarmupMonitor.WarmupInput(coolantTemp = 90.0))

        assertEquals(1.0, result.warmupProgress, 0.001)
    }
}
