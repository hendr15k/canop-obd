package com.canopobd.data.domain

import com.canopobd.data.model.PerformanceTestType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccelerationTimerTest {

    private lateinit var timer: AccelerationTimer

    @Before
    fun setup() {
        timer = AccelerationTimer()
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(AccelerationTimer.TimerState.IDLE, timer.state)
    }

    @Test
    fun `start transitions to WAITING_START`() {
        val state = timer.start(PerformanceTestType.ZERO_100)
        assertEquals(AccelerationTimer.TimerState.WAITING_START, state)
        assertEquals(PerformanceTestType.ZERO_100, timer.currentTestType)
    }

    @Test
    fun `update below start speed stays in WAITING_START`() {
        timer.start(PerformanceTestType.ZERO_100)
        val state = timer.update(0.1, System.currentTimeMillis())
        assertEquals(AccelerationTimer.TimerState.WAITING_START, state)
    }

    @Test
    fun `update above start speed transitions to RUNNING`() {
        timer.start(PerformanceTestType.ZERO_100)
        val state = timer.update(1.0, System.currentTimeMillis())
        assertEquals(AccelerationTimer.TimerState.RUNNING, state)
    }

    @Test
    fun `reaching target speed transitions to FINISHED`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, 1000L)
        val targetMs = AccelerationTimer.TARGET_SPEED_100_MS
        val state = timer.update(targetMs + 0.1, 10000L)
        assertEquals(AccelerationTimer.TimerState.FINISHED, state)
    }

    @Test
    fun `cancel transitions to CANCELLED`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, System.currentTimeMillis())
        val state = timer.cancel()
        assertEquals(AccelerationTimer.TimerState.CANCELLED, state)
    }

    @Test
    fun `buildResult returns null when not finished`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, System.currentTimeMillis())
        assertNull(timer.buildResult())
    }

    @Test
    fun `buildResult returns valid result when finished`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, 1000L)
        timer.update(AccelerationTimer.TARGET_SPEED_100_MS + 0.1, 10000L)

        val result = timer.buildResult()
        assertNotNull(result)
        assertTrue(result!!.valid)
        assertEquals(PerformanceTestType.ZERO_100, result.testType)
        assertTrue(result.timeSeconds > 0)
    }

    @Test
    fun `buildResult returns cancelled result`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, 1000L)
        timer.cancel()

        val result = timer.buildResult()
        assertNotNull(result)
        assertTrue(result!!.cancelled)
        assertFalse(result.valid)
    }

    @Test
    fun `getCurrentSpeedKmh returns last speed in kmh`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(10.0, System.currentTimeMillis())
        assertEquals(36.0, timer.getCurrentSpeedKmh(), 0.01)
    }

    @Test
    fun `getMaxSpeedKmh tracks maximum`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(5.0, 1000L)
        timer.update(15.0, 2000L)
        timer.update(10.0, 3000L)
        assertEquals(54.0, timer.getMaxSpeedKmh(), 0.01)
    }

    @Test
    fun `getProgress returns 0 when idle`() {
        assertEquals(0.0f, timer.getProgress(), 0.001f)
    }

    @Test
    fun `getProgress returns 1 when finished`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, 1000L)
        timer.update(AccelerationTimer.TARGET_SPEED_100_MS + 0.1, 10000L)
        assertEquals(1.0f, timer.getProgress(), 0.001f)
    }

    @Test
    fun `getProgress returns partial when running`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, 1000L)
        val halfTarget = AccelerationTimer.TARGET_SPEED_100_MS / 2.0
        timer.update(halfTarget, 5000L)
        val progress = timer.getProgress()
        assertTrue(progress > 0.4f && progress < 0.6f)
    }

    @Test
    fun `targetSpeedForType returns correct values`() {
        assertEquals(
            AccelerationTimer.TARGET_SPEED_100_MS,
            AccelerationTimer.targetSpeedForType(PerformanceTestType.ZERO_100),
            0.001
        )
        assertEquals(
            AccelerationTimer.TARGET_SPEED_200_MS,
            AccelerationTimer.targetSpeedForType(PerformanceTestType.ZERO_200),
            0.001
        )
        assertEquals(
            AccelerationTimer.TARGET_SPEED_200_MS,
            AccelerationTimer.targetSpeedForType(PerformanceTestType.HUNDRED_200),
            0.001
        )
    }

    @Test
    fun `startSpeedForType returns 0 for zero tests`() {
        assertEquals(0.0, AccelerationTimer.startSpeedForType(PerformanceTestType.ZERO_100), 0.001)
        assertEquals(0.0, AccelerationTimer.startSpeedForType(PerformanceTestType.ZERO_200), 0.001)
    }

    @Test
    fun `startSpeedForType returns 100 kmh for 100-200 test`() {
        assertEquals(
            AccelerationTimer.TARGET_SPEED_100_MS,
            AccelerationTimer.startSpeedForType(PerformanceTestType.HUNDRED_200),
            0.001
        )
    }

    @Test
    fun `100-200 test does not start before 100 kmh`() {
        timer.start(PerformanceTestType.HUNDRED_200)

        assertEquals(
            AccelerationTimer.TimerState.WAITING_START,
            timer.update(20.0, 1000L)
        )
        assertEquals(
            AccelerationTimer.TimerState.RUNNING,
            timer.update(AccelerationTimer.TARGET_SPEED_100_MS, 2000L)
        )

        timer.update(AccelerationTimer.TARGET_SPEED_200_MS, 12000L)
        val result = timer.buildResult()
        assertNotNull(result)
        assertEquals(10.0, result!!.timeSeconds, 0.001)
    }

    @Test
    fun `max acceleration tracked correctly`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, 1000L)
        timer.update(6.0, 2000L)
        assertTrue(timer.getMaxAccelerationMs2() > 0)
    }

    @Test
    fun `getElapsedSeconds returns 0 before start`() {
        timer.start(PerformanceTestType.ZERO_100)
        assertEquals(0.0, timer.getElapsedSeconds(), 0.001)
    }

    @Test
    fun `update in IDLE state does nothing`() {
        val state = timer.update(50.0, System.currentTimeMillis())
        assertEquals(AccelerationTimer.TimerState.IDLE, state)
    }

    @Test
    fun `update in FINISHED state does nothing`() {
        timer.start(PerformanceTestType.ZERO_100)
        timer.update(1.0, 1000L)
        timer.update(AccelerationTimer.TARGET_SPEED_100_MS + 0.1, 10000L)
        val state = timer.update(0.0, 20000L)
        assertEquals(AccelerationTimer.TimerState.FINISHED, state)
    }
}
