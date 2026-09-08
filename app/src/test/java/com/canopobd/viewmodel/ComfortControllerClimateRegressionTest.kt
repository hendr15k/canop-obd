package com.canopobd.viewmodel

import com.canopobd.protocol.BCMProtocol
import com.canopobd.ui.climate.ClimateCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Regression: SET_TEMP_DRIVER/PASSENGER must encode the requested value on CAN.
 * Bug: parameterless objects + ComfortController reading never-updated
 * climateState -> frame always carried default 22C.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ComfortControllerClimateRegressionTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val sentFrames = CopyOnWriteArrayList<String>()
    private lateinit var latch: CountDownLatch
    private lateinit var controller: ComfortController

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sentFrames.clear()
        latch = CountDownLatch(1)
        controller = ComfortController(CoroutineScope(testDispatcher)) { frame ->
            sentFrames.add(frame)
            latch.countDown()
            null
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun expectedTempFrame(temp: Int): String {
        return BCMProtocol.Climate.temperatureFrame(temp).joinToString("") { "%02X".format(it) }
    }

    @Test
    fun `driver temp 25 encodes 25 on CAN frame`() {
        controller.sendClimateCommand(ClimateCommand.SET_TEMP_DRIVER(25))
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(expectedTempFrame(25), sentFrames.last())
    }

    @Test
    fun `passenger temp 19 encodes 19 on CAN frame`() {
        controller.sendClimateCommand(ClimateCommand.SET_TEMP_PASSENGER(19))
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(expectedTempFrame(19), sentFrames.last())
    }

    @Test
    fun `dialog callback state update is reflected in next frame`() {
        controller.updateClimateState(controller.climateState.value.copy(driverTemp = 27))
        assertEquals(27, controller.climateState.value.driverTemp)
        controller.sendClimateCommand(ClimateCommand.SET_TEMP_DRIVER(27))
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(expectedTempFrame(27), sentFrames.last())
    }
}
