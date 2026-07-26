package com.canopobd.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canopobd.data.domain.AccelerationTimer
import com.canopobd.data.domain.DriveScoreCalculator
import com.canopobd.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PerformanceViewModel(application: Application) : ViewModel() {

    private val accelerationTimer = AccelerationTimer()

    private val _performanceTestState = MutableStateFlow(PerformanceTestState())
    val performanceTestState: StateFlow<PerformanceTestState> = _performanceTestState.asStateFlow()

    private val _currentAccelerationRun = MutableStateFlow<AccelerationRun?>(null)
    val currentAccelerationRun: StateFlow<AccelerationRun?> = _currentAccelerationRun.asStateFlow()

    private val _gpsSpeedForTest = MutableStateFlow(0.0)
    val gpsSpeedForTest: StateFlow<Double> = _gpsSpeedForTest.asStateFlow()

    private val _driveScore = MutableStateFlow(DriveScore())
    val driveScore: StateFlow<DriveScore> = _driveScore.asStateFlow()

    private val _driveSession = MutableStateFlow(DriveSession())
    val driveSession: StateFlow<DriveSession> = _driveSession.asStateFlow()

    private var performanceTestJob: Job? = null

    fun startPerformanceTest(
        testType: PerformanceTestType,
        currentLocationFlow: StateFlow<GPSLocation?>
    ) {
        accelerationTimer.start(testType)
        _currentAccelerationRun.value = null
        _gpsSpeedForTest.value = 0.0

        _performanceTestState.value = _performanceTestState.value.copy(
            isRunning = true,
            currentTestType = testType,
            startTimeNanos = System.nanoTime(),
            statusMessage = "Warte auf GPS-Speed…"
        )

        performanceTestJob?.cancel()
        performanceTestJob = viewModelScope.launch(Dispatchers.Default) {
            currentLocationFlow.collect { loc ->
                if (loc == null || !_performanceTestState.value.isRunning) return@collect

                val speedMs = loc.speed.toDouble()
                _gpsSpeedForTest.value = speedMs * 3.6

                val timerState = accelerationTimer.update(speedMs, System.currentTimeMillis())

                _performanceTestState.value = _performanceTestState.value.copy(
                    statusMessage = "%.0f km/h".format(speedMs * 3.6)
                )

                when (timerState) {
                    AccelerationTimer.TimerState.FINISHED -> {
                        val result = accelerationTimer.buildResult()
                        _currentAccelerationRun.value = result
                        if (result != null) {
                            val perfResult = PerformanceResult(
                                testType = testType,
                                timeSeconds = result.timeSeconds,
                                valid = result.valid
                            )
                            val history = listOf(perfResult) + _performanceTestState.value.history.take(9)
                            _performanceTestState.value = _performanceTestState.value.copy(
                                isRunning = false,
                                lastResult = perfResult,
                                history = history,
                                statusMessage = "Fertig!"
                            )
                        }
                        performanceTestJob?.cancel()
                    }
                    AccelerationTimer.TimerState.CANCELLED -> {
                        _performanceTestState.value = _performanceTestState.value.copy(
                            isRunning = false,
                            statusMessage = "Abgebrochen"
                        )
                        performanceTestJob?.cancel()
                    }
                    else -> Unit
                }
            }
        }
    }

    fun stopPerformanceTest() {
        performanceTestJob?.cancel()
        performanceTestJob = null
        if (_performanceTestState.value.isRunning) {
            accelerationTimer.cancel()
            val result = accelerationTimer.buildResult()
            _currentAccelerationRun.value = result

            val state = _performanceTestState.value
            if (result != null && result.valid) {
                val perfResult = PerformanceResult(
                    testType = state.currentTestType,
                    timeSeconds = result.timeSeconds,
                    valid = true
                )
                val history = listOf(perfResult) + state.history.take(9)
                _performanceTestState.value = state.copy(
                    isRunning = false,
                    lastResult = perfResult,
                    history = history,
                    statusMessage = ""
                )
            } else {
                _performanceTestState.value = state.copy(
                    isRunning = false,
                    statusMessage = "Abgebrochen"
                )
            }
        } else {
            _performanceTestState.value = _performanceTestState.value.copy(isRunning = false, statusMessage = "")
        }
    }

    fun resetPerformanceTest() {
        _performanceTestState.value = PerformanceTestState()
        _currentAccelerationRun.value = null
        _gpsSpeedForTest.value = 0.0
    }

    fun updateDriveScore() {
        val session = _driveSession.value
        _driveScore.value = DriveScoreCalculator.computeScore(session)
    }

    fun resetDriveScore() {
        _driveSession.value = DriveSession()
        _driveScore.value = DriveScore()
    }

    fun recordDriveSample(
        rpm: Double,
        throttle: Double,
        speed: Double,
        prevRpm: Double,
        boostBar: Double = 0.0,
        wastegateDuty: Double = 0.0
    ) {
        val session = _driveSession.value
        val rpmDelta = rpm - prevRpm
        val isDecelerating = throttle < 10.0 && prevRpm > rpm
        val isCoastingInGear = isDecelerating && speed > 10.0

        val newBoostSum = session.boostSamples + boostBar
        val newBoostCount = session.boostSampleCount + 1
        val newAvgBoost = if (newBoostCount > 0) newBoostSum / newBoostCount else 0.0
        val newRpmSampleCount = session.rpmSampleCount + 1
        val newThrottleSampleCount = session.throttleSampleCount + 1
        val newSpeedSampleCount = session.speedSampleCount + 1

        val newThrottleSamples = session.throttleSamples + throttle
        val newSpeedSamples = session.speedSamples + speed

        val newSession = session.copy(
            rpmSamples = session.rpmSamples + rpm,
            rpmSampleCount = newRpmSampleCount,
            throttleSamples = newThrottleSamples,
            throttleSampleCount = newThrottleSampleCount,
            speedSamples = newSpeedSamples,
            speedSampleCount = newSpeedSampleCount,
            avgRpm = if (newRpmSampleCount > 0) (session.rpmSamples + rpm) / newRpmSampleCount.toDouble() else rpm,
            avgThrottle = if (newThrottleSampleCount > 0) newThrottleSamples / newThrottleSampleCount else throttle,
            avgSpeed = if (newSpeedSampleCount > 0) newSpeedSamples / newSpeedSampleCount else speed,
            maxRpm = maxOf(session.maxRpm, rpm),
            maxThrottle = maxOf(session.maxThrottle, throttle),
            harshAccels = if (rpmDelta > 3000) session.harshAccels + 1 else session.harshAccels,
            harshBrakes = if (throttle < 10.0 && speed > 50.0 && prevRpm > rpm) session.harshBrakes + 1 else session.harshBrakes,
            boostSamples = newBoostSum,
            boostSampleCount = newBoostCount,
            avgBoostBar = newAvgBoost,
            maxBoostBar = maxOf(session.maxBoostBar, boostBar),
            optimalBoostTime = session.optimalBoostTime + if (boostBar in 0.4..0.7) 1 else 0,
            highBoostTime = session.highBoostTime + if (boostBar > 0.9 && throttle < 30.0) 1 else 0,
            coastingInGearSamples = session.coastingInGearSamples + if (isCoastingInGear) 1 else 0,
            deceleratingSamples = session.deceleratingSamples + if (isDecelerating) 1 else 0,
            rpmAbove4500Samples = session.rpmAbove4500Samples + if (rpm > 4500.0) 1 else 0,
            boostSumOfSquares = session.boostSumOfSquares + (boostBar * boostBar),
            wastegateDutySum = session.wastegateDutySum + wastegateDuty,
            wastegateSampleCount = session.wastegateSampleCount + 1,
            rpmRateSamples = session.rpmRateSamples + kotlin.math.abs(rpmDelta),
            rpmRateSampleCount = session.rpmRateSampleCount + 1
        )
        _driveSession.value = newSession
        updateDriveScore()
    }

    override fun onCleared() {
        performanceTestJob?.cancel()
        super.onCleared()
    }
}
