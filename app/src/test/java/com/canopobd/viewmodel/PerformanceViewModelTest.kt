package com.canopobd.viewmodel

import android.app.Application
import com.canopobd.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class PerformanceViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PerformanceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val application = mock(Application::class.java)
        viewModel = PerformanceViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        assertFalse(viewModel.performanceTestState.value.isRunning)
        assertNull(viewModel.currentAccelerationRun.value)
        assertEquals(0.0, viewModel.gpsSpeedForTest.value, 0.01)
        assertEquals(0, viewModel.driveScore.value.score)
    }

    @Test
    fun `resetDriveScore resets session and score`() {
        viewModel.recordDriveSample(3000.0, 50.0, 100.0, 2000.0)
        viewModel.resetDriveScore()

        assertEquals(0, viewModel.driveScore.value.score)
        assertEquals(0, viewModel.driveSession.value.rpmSampleCount)
    }

    @Test
    fun `recordDriveSample updates session averages`() {
        viewModel.recordDriveSample(
            rpm = 3000.0,
            throttle = 50.0,
            speed = 100.0,
            prevRpm = 2000.0,
            boostBar = 0.5,
            wastegateDuty = 30.0
        )

        val session = viewModel.driveSession.value
        assertEquals(1, session.rpmSampleCount)
        assertEquals(3000.0, session.avgRpm, 0.01)
        assertEquals(50.0, session.avgThrottle, 0.01)
        assertEquals(100.0, session.avgSpeed, 0.01)
        assertEquals(0.5, session.avgBoostBar, 0.01)
    }

    @Test
    fun `multiple samples accumulate correctly`() {
        viewModel.recordDriveSample(2000.0, 40.0, 80.0, 1500.0)
        viewModel.recordDriveSample(4000.0, 60.0, 120.0, 2000.0)

        val session = viewModel.driveSession.value
        assertEquals(2, session.rpmSampleCount)
        assertEquals(3000.0, session.avgRpm, 0.01)
        assertEquals(50.0, session.avgThrottle, 0.01)
        assertEquals(100.0, session.avgSpeed, 0.01)
    }

    @Test
    fun `harsh acceleration detected when rpm delta exceeds 3000`() {
        viewModel.recordDriveSample(5000.0, 80.0, 120.0, 1500.0)
        assertEquals(1, viewModel.driveSession.value.harshAccels)
    }

    @Test
    fun `no harsh acceleration when rpm delta below 3000`() {
        viewModel.recordDriveSample(4000.0, 80.0, 120.0, 1500.0)
        assertEquals(0, viewModel.driveSession.value.harshAccels)
    }

    @Test
    fun `coasting in gear detected correctly`() {
        viewModel.recordDriveSample(1500.0, 5.0, 80.0, 2000.0)
        assertEquals(1, viewModel.driveSession.value.coastingInGearSamples)
        assertEquals(1, viewModel.driveSession.value.deceleratingSamples)
    }

    @Test
    fun `no coasting when throttle above 10`() {
        viewModel.recordDriveSample(1500.0, 15.0, 80.0, 2000.0)
        assertEquals(0, viewModel.driveSession.value.coastingInGearSamples)
    }

    @Test
    fun `harsh brake detected when decelerating at high speed`() {
        viewModel.recordDriveSample(2000.0, 5.0, 80.0, 3000.0)
        assertEquals(1, viewModel.driveSession.value.harshBrakes)
    }

    @Test
    fun `optimal boost counted in range 0_4 to 0_7`() {
        viewModel.recordDriveSample(3000.0, 50.0, 100.0, 2500.0, boostBar = 0.55)
        assertEquals(1, viewModel.driveSession.value.optimalBoostTime)
        assertEquals(0, viewModel.driveSession.value.highBoostTime)
    }

    @Test
    fun `high boost counted above 0_9 with low throttle`() {
        viewModel.recordDriveSample(3000.0, 20.0, 100.0, 2500.0, boostBar = 1.0)
        assertEquals(1, viewModel.driveSession.value.highBoostTime)
    }

    @Test
    fun `rpm above 4500 counted`() {
        viewModel.recordDriveSample(5000.0, 50.0, 100.0, 4800.0)
        assertEquals(1, viewModel.driveSession.value.rpmAbove4500Samples)
    }

    @Test
    fun `resetPerformanceTest clears state`() {
        viewModel.resetPerformanceTest()

        assertFalse(viewModel.performanceTestState.value.isRunning)
        assertNull(viewModel.currentAccelerationRun.value)
        assertEquals(0.0, viewModel.gpsSpeedForTest.value, 0.01)
    }

    @Test
    fun `stopPerformanceTest when not running sets idle state`() {
        viewModel.stopPerformanceTest()
        assertFalse(viewModel.performanceTestState.value.isRunning)
    }

    @Test
    fun `updateDriveScore computes score from session`() {
        viewModel.recordDriveSample(2500.0, 30.0, 90.0, 2000.0, boostBar = 0.5)
        viewModel.updateDriveScore()

        assertTrue(viewModel.driveScore.value.score > 0)
    }
}
