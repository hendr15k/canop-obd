package com.canopobd.data.domain

import com.canopobd.data.model.AccelerationRun
import com.canopobd.data.model.AccelerationPhase
import com.canopobd.data.model.PerformanceTestType

/**
 * GPS-based acceleration timer for measuring 0-100 km/h and similar tests.
 *
 * Uses GPS speed data to automatically detect when the vehicle starts moving
 * and when it reaches the target speed, measuring elapsed time accurately.
 *
 * For the Opel Astra J 1.4 Turbo (A14NET):
 * - Stock 0-100: ~9.0 seconds
 * - Stage 1: ~7.5-8.0 seconds
 * - Stage 2: ~6.5-7.0 seconds
 */
class AccelerationTimer {

    enum class TimerState {
        IDLE, // Ready to start
        WAITING_START, // Waiting for vehicle to start moving (speed > threshold)
        RUNNING, // Accelerating, measuring time
        FINISHED, // Target speed reached
        CANCELLED // Test was cancelled
    }

    companion object {
        // Speed thresholds in m/s (GPS speed is in m/s)
        const val START_SPEED_MS = 0.5 // ~1.8 km/h — vehicle has started moving
        const val MAX_IDLE_TIME_MS = 30_000L // 30 seconds max waiting for start
        const val MAX_TEST_TIME_MS = 60_000L // 60 seconds max test duration
        const val TARGET_SPEED_100_MS = 100.0 / 3.6 // 100 km/h in m/s
        const val TARGET_SPEED_200_MS = 200.0 / 3.6 // 200 km/h in m/s

        // A14NET baseline values (stock)
        const val BASELINE_0_100_S = 9.0
        const val BASELINE_100_200_S = 12.0

        fun targetSpeedForType(type: PerformanceTestType): Double = when (type) {
            PerformanceTestType.ZERO_100 -> TARGET_SPEED_100_MS
            PerformanceTestType.ZERO_200 -> TARGET_SPEED_200_MS
            PerformanceTestType.HUNDRED_200 -> TARGET_SPEED_200_MS
        }

        fun startSpeedForType(type: PerformanceTestType): Double = when (type) {
            PerformanceTestType.ZERO_100 -> 0.0
            PerformanceTestType.ZERO_200 -> 0.0
            PerformanceTestType.HUNDRED_200 -> TARGET_SPEED_100_MS
        }
    }

    var state: TimerState = TimerState.IDLE
        private set

    var currentTestType: PerformanceTestType = PerformanceTestType.ZERO_100
        private set

    private var startTimeMs: Long = 0L
    private var targetReachedTimeMs: Long = 0L
    private var startTimestampMs: Long = 0L
    private var lastSpeedMs: Double = 0.0
    private var maxSpeedMs: Double = 0.0
    private var maxAcceleration: Double = 0.0
    private var lastSampleTimeMs: Long = 0L
    private var lastSampleSpeedMs: Double = 0.0

    // Speed samples for the run (time_ms, speed_ms)
    private val speedSamples = mutableListOf<Pair<Long, Double>>()

    // Phase markers
    private val phaseMarkers = mutableListOf<AccelerationPhase>()

    fun start(testType: PerformanceTestType): TimerState {
        currentTestType = testType
        state = TimerState.WAITING_START
        startTimeMs = 0L
        targetReachedTimeMs = 0L
        startTimestampMs = System.currentTimeMillis()
        lastSpeedMs = 0.0
        maxSpeedMs = 0.0
        maxAcceleration = 0.0
        lastSampleTimeMs = 0L
        lastSampleSpeedMs = 0.0
        speedSamples.clear()
        phaseMarkers.clear()

        phaseMarkers.add(AccelerationPhase(
            name = "ready",
            timestamp = startTimestampMs,
            speedKmh = 0.0,
            rpm = null
        ))
        return state
    }

    /**
     * Feed a new GPS speed sample. Returns the current timer state.
     * @param speedMs GPS speed in meters/second
     * @param timestampMs Sample timestamp in milliseconds
     * @param rpm Optional OBD RPM data for correlation
     */
    fun update(speedMs: Double, timestampMs: Long, rpm: Int? = null): TimerState {
        val now = System.currentTimeMillis()

        // Record all speed samples
        speedSamples.add(timestampMs to speedMs)

        when (state) {
            TimerState.WAITING_START -> {
                val requiredStartSpeed = startSpeedForType(currentTestType)
                val hasReachedStart = if (requiredStartSpeed > START_SPEED_MS) {
                    speedMs >= requiredStartSpeed &&
                        (lastSpeedMs < requiredStartSpeed || lastSpeedMs == 0.0)
                } else {
                    speedMs >= START_SPEED_MS && speedMs > lastSpeedMs
                }

                // For a rolling test, timing starts at 100 km/h rather than
                // at the first non-zero GPS sample.
                if (hasReachedStart) {
                    state = TimerState.RUNNING
                    startTimeMs = timestampMs
                    maxSpeedMs = speedMs
                    lastSampleTimeMs = timestampMs
                    lastSampleSpeedMs = speedMs
                    phaseMarkers.add(AccelerationPhase(
                        name = "launch",
                        timestamp = timestampMs,
                        speedKmh = speedMs * 3.6,
                        rpm = rpm
                    ))
                } else if (now - startTimestampMs > MAX_IDLE_TIME_MS) {
                    // Timed out waiting for start
                    state = TimerState.CANCELLED
                }
            }

            TimerState.RUNNING -> {
                val elapsed = timestampMs - startTimeMs
                if (speedMs > maxSpeedMs) maxSpeedMs = speedMs

                // Calculate instantaneous acceleration
                if (lastSampleTimeMs > 0) {
                    val dt = (timestampMs - lastSampleTimeMs) / 1000.0
                    if (dt > 0) {
                        val accel = (speedMs - lastSampleSpeedMs) / dt
                        if (accel > maxAcceleration) maxAcceleration = accel
                    }
                }

                lastSampleTimeMs = timestampMs
                lastSampleSpeedMs = speedMs

                // Phase markers at 10% intervals
                val targetSpeed = targetSpeedForType(currentTestType)
                val progress = (speedMs / targetSpeed * 100).toInt()
                val lastPhasePercent = phaseMarkers.lastOrNull()?.let {
                    (it.speedKmh / (targetSpeed * 3.6) * 100).toInt()
                } ?: 0

                val phasePercent = (progress / 10) * 10
                if (progress / 10 > lastPhasePercent / 10 && phasePercent in 10..90) {
                    phaseMarkers.add(AccelerationPhase(
                        name = "$phasePercent%",
                        timestamp = timestampMs,
                        speedKmh = speedMs * 3.6,
                        rpm = rpm
                    ))
                }

                // Check if target speed reached
                if (speedMs >= targetSpeed) {
                    targetReachedTimeMs = timestampMs
                    state = TimerState.FINISHED
                    phaseMarkers.add(AccelerationPhase(
                        name = "finish",
                        timestamp = timestampMs,
                        speedKmh = speedMs * 3.6,
                        rpm = rpm
                    ))
                } else if (elapsed > MAX_TEST_TIME_MS) {
                    // A sample that reaches the target after the timeout is
                    // still a completed run, not a cancelled one.
                    state = TimerState.CANCELLED
                }
            }

            else -> { /* Do nothing if IDLE, FINISHED, or CANCELLED */ }
        }

        lastSpeedMs = speedMs
        return state
    }

    fun cancel(): TimerState {
        state = TimerState.CANCELLED
        return state
    }

    /**
     * Get the elapsed time since start, in seconds.
     */
    fun getElapsedSeconds(): Double {
        if (startTimeMs == 0L) return 0.0
        val endTime = if (state == TimerState.FINISHED) targetReachedTimeMs else System.currentTimeMillis()
        return (endTime - startTimeMs) / 1000.0
    }

    /**
     * Get the current speed in km/h.
     */
    fun getCurrentSpeedKmh(): Double = lastSpeedMs * 3.6

    /**
     * Get the max speed reached during the run in km/h.
     */
    fun getMaxSpeedKmh(): Double = maxSpeedMs * 3.6

    /**
     * Get the max acceleration in m/s².
     */
    fun getMaxAccelerationMs2(): Double = maxAcceleration

    /**
     * Build a completed AccelerationRun result.
     */
    fun buildResult(): AccelerationRun? {
        if (state != TimerState.FINISHED && state != TimerState.CANCELLED) return null
        if (startTimeMs == 0L) return null

        val targetSpeed = targetSpeedForType(currentTestType)
        val startSpeed = startSpeedForType(currentTestType)
        val elapsed = (targetReachedTimeMs - startTimeMs) / 1000.0
        val valid = state == TimerState.FINISHED && elapsed > 0.5

        // Calculate intermediate times
        val target50 = startSpeed + (targetSpeed - startSpeed) * 0.5
        val target90 = startSpeed + (targetSpeed - startSpeed) * 0.9

        var timeTo50: Double? = null
        var timeTo90: Double? = null

        for ((ts, spd) in speedSamples) {
            if (ts < startTimeMs) continue
            if (timeTo50 == null && spd >= target50) {
                timeTo50 = (ts - startTimeMs) / 1000.0
            }
            if (timeTo90 == null && spd >= target90) {
                timeTo90 = (ts - startTimeMs) / 1000.0
            }
        }

        // Detect gear shifts (sudden RPM drops correlated with speed plateaus)
        val shiftPoints = detectGearShifts()

        return AccelerationRun(
            timestamp = startTimestampMs,
            testType = currentTestType,
            timeSeconds = if (valid) elapsed else 0.0,
            valid = valid,
            maxSpeedKmh = getMaxSpeedKmh(),
            maxAcceleration = maxAcceleration,
            timeTo50Percent = timeTo50,
            timeTo90Percent = timeTo90,
            sampleCount = speedSamples.size,
            phases = phaseMarkers.toList(),
            gearShifts = shiftPoints,
            cancelled = state == TimerState.CANCELLED
        )
    }

    /**
     * Detect gear shifts by analyzing speed acceleration patterns.
     * A gear shift shows as a brief plateau/dip in acceleration while speed stays constant.
     */
    private fun detectGearShifts(): List<Int> {
        if (speedSamples.size < 5) return emptyList()

        val shifts = mutableListOf<Int>()
        var inShift = false
        var shiftStartIdx = 0

        // Calculate moving average of acceleration
        val windowSize = 5
        for (i in windowSize until speedSamples.size) {
            val dt = (speedSamples[i].first - speedSamples[i - windowSize].first) / 1000.0
            if (dt <= 0) continue
            val dv = speedSamples[i].second - speedSamples[i - windowSize].second
            val avgAccel = dv / dt

            // Detect shift: acceleration drops close to zero or negative briefly
            if (avgAccel < 0.5 && !inShift && i > shiftStartIdx + 10) {
                inShift = true
                shiftStartIdx = i
            } else if (avgAccel > 2.0 && inShift) {
                inShift = false
                val shiftSpeedKmh = speedSamples[shiftStartIdx].second * 3.6
                if (shiftSpeedKmh > 5.0) { // Ignore initial launch
                    shifts.add(shiftSpeedKmh.toInt())
                }
            }
        }

        return shifts
    }

    /**
     * Get progress as 0.0 - 1.0 for the current test.
     */
    fun getProgress(): Float {
        if (state != TimerState.RUNNING) return if (state == TimerState.FINISHED) 1.0f else 0.0f
        if (startTimeMs == 0L) return 0f
        val targetSpeed = targetSpeedForType(currentTestType)
        val startSpeed = startSpeedForType(currentTestType)
        val range = targetSpeed - startSpeed
        if (range <= 0) return 0f
        return ((lastSpeedMs - startSpeed) / range).coerceIn(0.0, 1.0).toFloat()
    }
}
