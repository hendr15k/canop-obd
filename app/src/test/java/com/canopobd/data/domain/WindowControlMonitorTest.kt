package com.canopobd.data.domain

import org.junit.Assert.*
import org.junit.Test

class WindowControlMonitorTest {

    @Test
    fun `commandForAction DRIVER_UP returns closeDriver frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.DRIVER_UP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020100", hex)
    }

    @Test
    fun `commandForAction DRIVER_DOWN returns openDriver frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.DRIVER_DOWN)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020164", hex)
    }

    @Test
    fun `commandForAction DRIVER_STOP returns stopDriver frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.DRIVER_STOP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF0201FF", hex)
    }

    @Test
    fun `commandForAction PASSENGER_UP returns closePassenger frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.PASSENGER_UP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020200", hex)
    }

    @Test
    fun `commandForAction PASSENGER_DOWN returns openPassenger frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.PASSENGER_DOWN)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020264", hex)
    }

    @Test
    fun `commandForAction PASSENGER_STOP returns stopPassenger frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.PASSENGER_STOP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF0202FF", hex)
    }

    @Test
    fun `commandForAction REAR_LEFT_UP returns closeRearLeft frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.REAR_LEFT_UP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020300", hex)
    }

    @Test
    fun `commandForAction REAR_LEFT_DOWN returns openRearLeft frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.REAR_LEFT_DOWN)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020364", hex)
    }

    @Test
    fun `commandForAction REAR_RIGHT_UP returns closeRearRight frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.REAR_RIGHT_UP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020400", hex)
    }

    @Test
    fun `commandForAction REAR_RIGHT_DOWN returns openRearRight frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.REAR_RIGHT_DOWN)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020464", hex)
    }

    @Test
    fun `commandForAction ALL_UP returns closeAll frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.ALL_UP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020000000000", hex)
    }

    @Test
    fun `commandForAction ALL_DOWN returns openAll frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.ALL_DOWN)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020064646464", hex)
    }

    @Test
    fun `commandForAction ALL_STOP returns stopAll frame`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.ALL_STOP)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF0200FF", hex)
    }

    @Test
    fun `buildPositionCommand clamps position to valid range`() {
        val frame = WindowControlMonitor.buildPositionCommand(0x01, 150)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020164", hex)
    }

    @Test
    fun `buildPositionCommand with 50 percent returns correct frame`() {
        val frame = WindowControlMonitor.buildPositionCommand(0x02, 50)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020232", hex)
    }

    @Test
    fun `updateStateFromAction DRIVER_UP sets driverPos to 0`() {
        val state = WindowState(driverPos = 100)
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.DRIVER_UP)
        assertEquals(0, newState.driverPos)
    }

    @Test
    fun `updateStateFromAction DRIVER_DOWN sets driverPos to 100`() {
        val state = WindowState(driverPos = 0)
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.DRIVER_DOWN)
        assertEquals(100, newState.driverPos)
    }

    @Test
    fun `updateStateFromAction DRIVER_STOP preserves state`() {
        val state = WindowState(driverPos = 50, passengerPos = 30)
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.DRIVER_STOP)
        assertEquals(50, newState.driverPos)
        assertEquals(30, newState.passengerPos)
    }

    @Test
    fun `updateStateFromAction ALL_UP resets all to 0`() {
        val state = WindowState(100, 100, 100, 100)
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.ALL_UP)
        assertEquals(0, newState.driverPos)
        assertEquals(0, newState.passengerPos)
        assertEquals(0, newState.rearLeftPos)
        assertEquals(0, newState.rearRightPos)
    }

    @Test
    fun `updateStateFromAction ALL_DOWN sets all to 100`() {
        val state = WindowState()
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.ALL_DOWN)
        assertEquals(100, newState.driverPos)
        assertEquals(100, newState.passengerPos)
        assertEquals(100, newState.rearLeftPos)
        assertEquals(100, newState.rearRightPos)
    }

    @Test
    fun `updateStateFromAction PASSENGER_UP sets passengerPos to 0`() {
        val state = WindowState(passengerPos = 80)
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.PASSENGER_UP)
        assertEquals(0, newState.passengerPos)
    }

    @Test
    fun `updateStateFromAction REAR_LEFT_DOWN sets rearLeftPos to 100`() {
        val state = WindowState()
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.REAR_LEFT_DOWN)
        assertEquals(100, newState.rearLeftPos)
    }

    @Test
    fun `updateStateFromAction REAR_RIGHT_UP sets rearRightPos to 0`() {
        val state = WindowState(rearRightPos = 70)
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.REAR_RIGHT_UP)
        assertEquals(0, newState.rearRightPos)
    }

    @Test
    fun `evaluate all closed returns perfect safetyScore and no warning`() {
        val state = WindowState(0, 0, 0, 0)
        val result = WindowControlMonitor.evaluate(state)
        assertEquals(0, result.openWindowCount)
        assertFalse(result.allOpen)
        assertFalse(result.anyOpen)
        assertEquals(100, result.safetyScore)
        assertNull(result.warning)
    }

    @Test
    fun `evaluate all open returns low safetyScore and warning`() {
        val state = WindowState(100, 100, 100, 100)
        val result = WindowControlMonitor.evaluate(state)
        assertEquals(4, result.openWindowCount)
        assertTrue(result.allOpen)
        assertTrue(result.anyOpen)
        assertEquals(40, result.safetyScore)
        assertNotNull(result.warning)
        assertTrue(result.warning!!.contains("Alle"))
    }

    @Test
    fun `evaluate one window open returns high safetyScore`() {
        val state = WindowState(100, 0, 0, 0)
        val result = WindowControlMonitor.evaluate(state)
        assertEquals(1, result.openWindowCount)
        assertFalse(result.allOpen)
        assertTrue(result.anyOpen)
        assertEquals(85, result.safetyScore)
        assertNotNull(result.warning)
        assertTrue(result.warning!!.contains("1"))
    }

    @Test
    fun `evaluate two windows open returns medium safetyScore`() {
        val state = WindowState(100, 100, 0, 0)
        val result = WindowControlMonitor.evaluate(state)
        assertEquals(2, result.openWindowCount)
        assertEquals(70, result.safetyScore)
    }

    @Test
    fun `evaluate three windows open returns lower safetyScore`() {
        val state = WindowState(100, 100, 100, 0)
        val result = WindowControlMonitor.evaluate(state)
        assertEquals(3, result.openWindowCount)
        assertEquals(55, result.safetyScore)
    }

    @Test
    fun `WindowState isFullyClosed returns true when all zero`() {
        val state = WindowState(0, 0, 0, 0)
        assertTrue(state.isFullyClosed())
        assertFalse(state.isFullyOpen())
    }

    @Test
    fun `WindowState isFullyOpen returns true when all 100`() {
        val state = WindowState(100, 100, 100, 100)
        assertTrue(state.isFullyOpen())
        assertFalse(state.isFullyClosed())
    }

    @Test
    fun `WindowState anyOpen returns true when any window above 0`() {
        val state = WindowState(0, 50, 0, 0)
        assertTrue(state.anyOpen())
        assertFalse(state.isFullyClosed())
        assertFalse(state.isFullyOpen())
    }

    @Test
    fun `WindowState anyOpen returns false when all zero`() {
        val state = WindowState(0, 0, 0, 0)
        assertFalse(state.anyOpen())
    }

    @Test
    fun `WindowState default is all closed`() {
        val state = WindowState()
        assertEquals(0, state.driverPos)
        assertEquals(0, state.passengerPos)
        assertEquals(0, state.rearLeftPos)
        assertEquals(0, state.rearRightPos)
        assertTrue(state.isFullyClosed())
    }

    @Test
    fun `WindowTarget fromByte 0x01 returns DRIVER`() {
        assertEquals(WindowTarget.DRIVER, WindowTarget.fromByte(0x01))
    }

    @Test
    fun `WindowTarget fromByte 0x02 returns PASSENGER`() {
        assertEquals(WindowTarget.PASSENGER, WindowTarget.fromByte(0x02))
    }

    @Test
    fun `WindowTarget fromByte 0x03 returns REAR_LEFT`() {
        assertEquals(WindowTarget.REAR_LEFT, WindowTarget.fromByte(0x03))
    }

    @Test
    fun `WindowTarget fromByte 0x04 returns REAR_RIGHT`() {
        assertEquals(WindowTarget.REAR_RIGHT, WindowTarget.fromByte(0x04))
    }

    @Test
    fun `WindowTarget fromByte 0x00 returns ALL`() {
        assertEquals(WindowTarget.ALL, WindowTarget.fromByte(0x00))
    }

    @Test
    fun `WindowTarget fromByte unknown returns null`() {
        assertNull(WindowTarget.fromByte(0x99))
    }

    @Test
    fun `WindowTarget toByte maps to BCM window constant`() {
        assertEquals(0x01, WindowTarget.DRIVER.toByte())
        assertEquals(0x02, WindowTarget.PASSENGER.toByte())
        assertEquals(0x03, WindowTarget.REAR_LEFT.toByte())
        assertEquals(0x04, WindowTarget.REAR_RIGHT.toByte())
        assertEquals(0x00, WindowTarget.ALL.toByte())
    }

    @Test
    fun `buildSetPosition uses target window byte and percent`() {
        val frame = WindowControlMonitor.buildSetPosition(WindowTarget.REAR_LEFT, 50)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020332", hex)
    }

    @Test
    fun `buildSetPosition clamps percent to 100`() {
        val frame = WindowControlMonitor.buildSetPosition(WindowTarget.DRIVER, 150)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020164", hex)
    }

    @Test
    fun `buildSetPosition clamps negative to zero`() {
        val frame = WindowControlMonitor.buildSetPosition(WindowTarget.DRIVER, -50)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020100", hex)
    }

    @Test
    fun `commandForAction ALL_VENTILATE returns 20 percent frame for index 0x00`() {
        val frame = WindowControlMonitor.commandForAction(WindowAction.ALL_VENTILATE)
        val hex = frame.joinToString("") { "%02X".format(it) }
        assertEquals("2EFF020014", hex)
    }

    @Test
    fun `updateStateFromAction ALL_VENTILATE sets all windows to 20 percent`() {
        val state = WindowState()
        val newState = WindowControlMonitor.updateStateFromAction(state, WindowAction.ALL_VENTILATE)
        assertEquals(20, newState.driverPos)
        assertEquals(20, newState.passengerPos)
        assertEquals(20, newState.rearLeftPos)
        assertEquals(20, newState.rearRightPos)
    }

    @Test
    fun `applyPositionPreset sets single window position`() {
        val state = WindowState()
        val newState = WindowControlMonitor.applyPositionPreset(state, WindowTarget.DRIVER, 50)
        assertEquals(50, newState.driverPos)
        assertEquals(0, newState.passengerPos)
    }

    @Test
    fun `applyPositionPreset with ALL sets all windows`() {
        val state = WindowState()
        val newState = WindowControlMonitor.applyPositionPreset(state, WindowTarget.ALL, 75)
        assertEquals(75, newState.driverPos)
        assertEquals(75, newState.passengerPos)
        assertEquals(75, newState.rearLeftPos)
        assertEquals(75, newState.rearRightPos)
    }

    @Test
    fun `WindowState withPosition clamps and stores per target`() {
        val state = WindowState()
        val s1 = state.withPosition(WindowTarget.DRIVER, 150)
        assertEquals(100, s1.driverPos)
        val s2 = state.withPosition(WindowTarget.REAR_LEFT, -10)
        assertEquals(0, s2.rearLeftPos)
    }

    @Test
    fun `WindowState positionFor target returns matching position`() {
        val state = WindowState(driverPos = 30, passengerPos = 50, rearLeftPos = 70, rearRightPos = 90)
        assertEquals(30, state.positionFor(WindowTarget.DRIVER))
        assertEquals(50, state.positionFor(WindowTarget.PASSENGER))
        assertEquals(70, state.positionFor(WindowTarget.REAR_LEFT))
        assertEquals(90, state.positionFor(WindowTarget.REAR_RIGHT))
    }

    @Test
    fun `WindowState positionFor ALL returns 100 if any open`() {
        val state = WindowState(driverPos = 0, passengerPos = 0, rearLeftPos = 30, rearRightPos = 0)
        assertEquals(100, state.positionFor(WindowTarget.ALL))
    }

    @Test
    fun `WindowState positionFor ALL returns 0 if all closed`() {
        val state = WindowState()
        assertEquals(0, state.positionFor(WindowTarget.ALL))
    }

    @Test
    fun `parseStatusFromDidResponse returns null for empty bytes`() {
        assertNull(WindowControlMonitor.parseStatusFromDidResponse(ByteArray(0)))
    }

    @Test
    fun `parseStatusFromDidResponse returns null for non FF02 DID`() {
        val bytes = byteArrayOf(
            0x62.toByte(), 0xFF.toByte(), 0x99.toByte(), 0x50, 0x50, 0x50, 0x50
        )
        assertNull(WindowControlMonitor.parseStatusFromDidResponse(bytes))
    }

    @Test
    fun `parseStatusFromDidResponse parses four window positions`() {
        val bytes = byteArrayOf(
            0x62.toByte(),
            0xFF.toByte(),
            0x02.toByte(),
            0x05,
            0x32,
            0x4B,
            0x64
        )
        val state = WindowControlMonitor.parseStatusFromDidResponse(bytes)
        assertNotNull(state)
        assertEquals(5, state!!.driverPos)
        assertEquals(50, state.passengerPos)
        assertEquals(75, state.rearLeftPos)
        assertEquals(100, state.rearRightPos)
    }

    @Test
    fun `parseStatusFromDidResponse treats 0xFF as closed`() {
        val bytes = byteArrayOf(
            0x62.toByte(), 0xFF.toByte(), 0x02.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )
        val state = WindowControlMonitor.parseStatusFromDidResponse(bytes)
        assertNotNull(state)
        assertEquals(0, state!!.driverPos)
        assertEquals(0, state.passengerPos)
    }

    @Test
    fun `parseStatusFromDidResponse treats 0x00 as closed`() {
        val bytes = byteArrayOf(
            0x62.toByte(), 0xFF.toByte(), 0x02.toByte(), 0x00, 0x00, 0x00, 0x00
        )
        val state = WindowControlMonitor.parseStatusFromDidResponse(bytes)
        assertNotNull(state)
        assertEquals(0, state!!.driverPos)
    }

    @Test
    fun `parseStatusFromDidResponse caps values above 100`() {
        val bytes = byteArrayOf(
            0x62.toByte(), 0xFF.toByte(), 0x02.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )
        val state = WindowControlMonitor.parseStatusFromDidResponse(bytes)
        assertNotNull(state)
        assertTrue(state!!.driverPos <= 100)
    }

    @Test
    fun `WindowPresets POSITIONS contains 25 50 and 75 percent`() {
        val percents = WindowPresets.POSITIONS.map { it.percent }
        assertTrue(percents.contains(25))
        assertTrue(percents.contains(50))
        assertTrue(percents.contains(75))
    }

    @Test
    fun `WindowPresets VENTILATE_PERCENT is 20`() {
        assertEquals(20, WindowPresets.VENTILATE_PERCENT)
    }
}
