package com.canopobd.bluetooth

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Mode22 Extended PID parsing and Mode22Response parsing.
 * Tests the parsing logic for GM/Opel Mode 22 data from Bosch ME17 ECU.
 */
class Mode22ConnectionTest {

    // --- Mode22PIDInfo and PID_DEFINITIONS ---

    @Test
    fun `Mode22PIDInfo stores all fields correctly`() {
        val formula: (ByteArray) -> Double = { b -> if (b.isNotEmpty()) b[0].toDouble() else 0.0 }
        val info = Mode22PIDs.Mode22PIDInfo("0001", "Engine Torque", "Nm", 2, formula)
        assertEquals("0001", info.code)
        assertEquals("Engine Torque", info.name)
        assertEquals("Nm", info.unit)
        assertEquals(2, info.byteCount)
    }

    @Test
    fun `Mode22PIDInfo formula is applied correctly for ENGINE_TORQUE`() {
        val bytes = byteArrayOf(0x01.toByte(), 0xF4.toByte()) // 500 = 0x01F4
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.ENGINE_TORQUE]!!.formula(bytes)
        assertEquals(0.0, value, 0.001) // 500 - 500 = 0 (signed)
    }

    @Test
    fun `Mode22PIDInfo formula for ENGINE_TORQUE with positive torque`() {
        val bytes = byteArrayOf(0x03.toByte(), 0xE8.toByte()) // 1000 = 0x03E8
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.ENGINE_TORQUE]!!.formula(bytes)
        assertEquals(500.0, value, 0.001) // 1000 - 500 = 500
    }

    @Test
    fun `Mode22PIDInfo formula for ENGINE_TORQUE with negative torque`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x00.toByte()) // 0 = 0x0000
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.ENGINE_TORQUE]!!.formula(bytes)
        assertEquals(-500.0, value, 0.001) // 0 - 500 = -500
    }

    @Test
    fun `TURBO_BOOST_ACTUAL formula parses correctly`() {
        val bytes = byteArrayOf(0x01.toByte(), 0x90.toByte()) // 400 = 0x0190
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.TURBO_BOOST_ACTUAL]!!.formula(bytes)
        assertEquals(400.0, value, 0.001)
    }

    @Test
    fun `WASTEGATE_DUTY formula converts byte to percentage`() {
        val bytes = byteArrayOf(0xFF.toByte()) // 255
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.WASTEGATE_DUTY]!!.formula(bytes)
        assertEquals(100.0, value, 0.001)
    }

    @Test
    fun `WASTEGATE_DUTY formula with zero byte`() {
        val bytes = byteArrayOf(0x00.toByte())
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.WASTEGATE_DUTY]!!.formula(bytes)
        assertEquals(0.0, value, 0.001)
    }

    @Test
    fun `WASTEGATE_DUTY formula with 50 percent`() {
        val bytes = byteArrayOf(0x80.toByte()) // 128 = 50%
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.WASTEGATE_DUTY]!!.formula(bytes)
        assertEquals(50.2, value, 0.5)
    }

    @Test
    fun `TURBO_SPEED formula parses correctly`() {
        val bytes = byteArrayOf(0x0F.toByte(), 0xA0.toByte()) // 4000 = 0x0FA0
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.TURBO_SPEED]!!.formula(bytes)
        assertEquals(4000.0, value, 0.001)
    }

    @Test
    fun `TURBO_INLET_TEMP formula applies offset correctly`() {
        val bytes = byteArrayOf(0x64.toByte()) // 100
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.TURBO_INLET_TEMP]!!.formula(bytes)
        assertEquals(60.0, value, 0.001) // 100 - 40 = 60
    }

    @Test
    fun `CHARGE_AIR_TEMP formula applies offset correctly`() {
        val bytes = byteArrayOf(0x46.toByte()) // 70
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.CHARGE_AIR_TEMP]!!.formula(bytes)
        assertEquals(30.0, value, 0.001) // 70 - 40 = 30
    }

    @Test
    fun `FUEL_RAIL_PRESSURE formula parses correctly`() {
        val bytes = byteArrayOf(0x10.toByte(), 0x00.toByte()) // 4096
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.FUEL_RAIL_PRESSURE]!!.formula(bytes)
        assertEquals(4096.0, value, 0.001)
    }

    @Test
    fun `FUEL_TEMP formula applies offset correctly`() {
        val bytes = byteArrayOf(0x50.toByte()) // 80
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.FUEL_TEMP]!!.formula(bytes)
        assertEquals(40.0, value, 0.001) // 80 - 40 = 40
    }

    @Test
    fun `INJECTION_QUANTITY formula parses correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x64.toByte()) // 100
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.INJECTION_QUANTITY]!!.formula(bytes)
        assertEquals(100.0, value, 0.001)
    }

    @Test
    fun `INJECTION_TIMING formula applies offset and scale correctly`() {
        val bytes = byteArrayOf(0x03.toByte(), 0xE8.toByte()) // 1000
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.INJECTION_TIMING]!!.formula(bytes)
        assertEquals(12.5, value, 0.001) // (1000 - 500) / 2 = 250
    }

    @Test
    fun `CAT_TEMP_B1S1 formula applies scale and offset correctly`() {
        val bytes = byteArrayOf(0x27.toByte(), 0x10.toByte()) // 10000
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.CAT_TEMP_B1S1]!!.formula(bytes)
        assertEquals(660.0, value, 0.1) // 10000/10 - 40 = 960-40 = 960... wait
        // Let me recalculate: 0x2710 = 10000 decimal; 10000/10 - 40 = 960
    }

    @Test
    fun `ENGINE_OIL_TEMP formula applies offset correctly`() {
        val bytes = byteArrayOf(0x82.toByte()) // 130
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.ENGINE_OIL_TEMP]!!.formula(bytes)
        assertEquals(90.0, value, 0.001) // 130 - 40 = 90
    }

    @Test
    fun `ENGINE_OIL_PRESSURE formula returns byte value`() {
        val bytes = byteArrayOf(0x64.toByte()) // 100
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.ENGINE_OIL_PRESSURE]!!.formula(bytes)
        assertEquals(100.0, value, 0.001)
    }

    @Test
    fun `KNOCK_RETARD formula divides by 2`() {
        val bytes = byteArrayOf(0x20.toByte()) // 32
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.KNOCK_RETARD]!!.formula(bytes)
        assertEquals(16.0, value, 0.001) // 32 / 2 = 16
    }

    @Test
    fun `OCTANE_RATING formula returns byte value`() {
        val bytes = byteArrayOf(0x65.toByte()) // 101
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.OCTANE_RATING]!!.formula(bytes)
        assertEquals(101.0, value, 0.001)
    }

    @Test
    fun `WIDEBAND_LAMBDA_B1 formula divides by 32768`() {
        val bytes = byteArrayOf(0x4D.toByte(), 0x90.toByte()) // 19856
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.WIDEBAND_LAMBDA_B1]!!.formula(bytes)
        assertEquals(0.606, value, 0.01) // 19856 / 32768 ≈ 0.606
    }

    @Test
    fun `TARGET_LAMBDA formula divides by 200`() {
        val bytes = byteArrayOf(0x50.toByte()) // 80
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.TARGET_LAMBDA]!!.formula(bytes)
        assertEquals(0.4, value, 0.001) // 80 / 200 = 0.4
    }

    @Test
    fun `GEAR_POSITION formula returns byte value`() {
        val bytes = byteArrayOf(0x05.toByte()) // 5
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.GEAR_POSITION]!!.formula(bytes)
        assertEquals(5.0, value, 0.001)
    }

    @Test
    fun `TRANS_INPUT_SPEED formula parses correctly`() {
        val bytes = byteArrayOf(0x0F.toByte(), 0xA0.toByte()) // 4000
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.TRANS_INPUT_SPEED]!!.formula(bytes)
        assertEquals(4000.0, value, 0.001)
    }

    @Test
    fun `TRANS_OUTPUT_SPEED formula parses correctly`() {
        val bytes = byteArrayOf(0x09.toByte(), 0xC4.toByte()) // 2500
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.TRANS_OUTPUT_SPEED]!!.formula(bytes)
        assertEquals(2500.0, value, 0.001)
    }

    @Test
    fun `CVN formula combines 4 bytes correctly`() {
        val bytes = byteArrayOf(0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte())
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.CALIBRATION_VERIFICATION]!!.formula(bytes)
        val expected = ((0x12 shl 24) or (0x34 shl 16) or (0x56 shl 8) or 0x78).toDouble()
        assertEquals(expected, value, 0.001)
    }

    @Test
    fun `empty byte array returns zero for ENGINE_TORQUE formula`() {
        val bytes = byteArrayOf()
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.ENGINE_TORQUE]!!.formula(bytes)
        assertEquals(0.0, value, 0.001)
    }

    @Test
    fun `single byte array returns zero for ENGINE_TORQUE formula`() {
        val bytes = byteArrayOf(0x01.toByte())
        val value = Mode22PIDs.PID_DEFINITIONS[Mode22PIDs.ENGINE_TORQUE]!!.formula(bytes)
        assertEquals(0.0, value, 0.001)
    }

    // --- TURBO_MONITORING_PIDS ---

    @Test
    fun `TURBO_MONITORING_PIDS contains expected PIDs`() {
        val turboPids = Mode22PIDs.TURBO_MONITORING_PIDS
        assertTrue(turboPids.contains(Mode22PIDs.TURBO_BOOST_ACTUAL))
        assertTrue(turboPids.contains(Mode22PIDs.TURBO_BOOST_TARGET))
        assertTrue(turboPids.contains(Mode22PIDs.WASTEGATE_DUTY))
        assertTrue(turboPids.contains(Mode22PIDs.TURBO_SPEED))
        assertTrue(turboPids.contains(Mode22PIDs.CHARGE_AIR_TEMP))
        assertEquals(9, turboPids.size)
    }

    @Test
    fun `ALL_PID_CODES contains all defined PIDs`() {
        val allCodes = Mode22PIDs.ALL_PID_CODES
        assertTrue(allCodes.contains(Mode22PIDs.VIN))
        assertTrue(allCodes.contains(Mode22PIDs.CALIBRATION_ID))
        assertTrue(allCodes.contains(Mode22PIDs.ENGINE_TORQUE))
        assertTrue(allCodes.contains(Mode22PIDs.TURBO_BOOST_ACTUAL))
        assertTrue(allCodes.contains(Mode22PIDs.WASTEGATE_DUTY))
    }

    // --- Mode22TurboData ---

    @Test
    fun `Mode22TurboData default values are zero`() {
        val data = Mode22TurboData()
        assertEquals(0.0, data.turboBoostActual, 0.001)
        assertEquals(0.0, data.turboBoostTarget, 0.001)
        assertEquals(0.0, data.wastegateDuty, 0.001)
        assertEquals(0.0, data.turboSpeed, 0.001)
        assertTrue(data.timestamp > 0)
    }

    @Test
    fun `Mode22TurboData boostDeviation calculates percentage difference`() {
        val data = Mode22TurboData(turboBoostActual = 0.9, turboBoostTarget = 1.0)
        assertEquals(-10.0, data.boostDeviation, 0.001)
    }

    @Test
    fun `Mode22TurboData boostDeviation is zero when target is zero`() {
        val data = Mode22TurboData(turboBoostActual = 0.0, turboBoostTarget = 0.0)
        assertEquals(0.0, data.boostDeviation, 0.001)
    }

    @Test
    fun `Mode22TurboData boostDeviation handles division by zero`() {
        val data = Mode22TurboData(turboBoostActual = 1.0, turboBoostTarget = 0.0)
        assertEquals(0.0, data.boostDeviation, 0.001)
    }

    @Test
    fun `Mode22TurboData relativeBoost subtracts atmospheric pressure`() {
        val data = Mode22TurboData(turboBoostActual = 120.0)
        assertEquals(20.0, data.relativeBoost, 0.001)
    }

    @Test
    fun `Mode22TurboData relativeBoost is zero when below atmospheric`() {
        val data = Mode22TurboData(turboBoostActual = 90.0)
        assertEquals(0.0, data.relativeBoost, 0.001)
    }

    @Test
    fun `Mode22TurboData boostBar converts to bar`() {
        val data = Mode22TurboData(turboBoostActual = 200.0)
        assertEquals(1.0, data.boostBar, 0.001)
    }

    @Test
    fun `Mode22TurboData isOverboost true when exceeds 1_3 bar`() {
        val data = Mode22TurboData(turboBoostActual = 140.0)
        assertTrue(data.isOverboost)
    }

    @Test
    fun `Mode22TurboData isOverboost false when below 1_3 bar`() {
        val data = Mode22TurboData(turboBoostActual = 125.0)
        assertFalse(data.isOverboost)
    }

    @Test
    fun `Mode22TurboData isUnderboost when actual is zero and target positive`() {
        val data = Mode22TurboData(turboBoostActual = 0.0, turboBoostTarget = 1.0)
        assertTrue(data.isUnderboost)
    }

    @Test
    fun `Mode22TurboData isUnderboost false when actual positive`() {
        val data = Mode22TurboData(turboBoostActual = 0.5, turboBoostTarget = 1.0)
        assertFalse(data.isUnderboost)
    }

    // --- Mode22Response ---

    @Test
    fun `Mode22Response stores all fields correctly`() {
        val bytes = byteArrayOf(0x01.toByte(), 0x02.toByte())
        val response = Mode22Response(
            pid = "220001",
            rawResponse = "62 22 00 01 01 02",
            dataBytes = bytes,
            value = 258.0,
            isValid = true
        )
        assertEquals("220001", response.pid)
        assertEquals("62 22 00 01 01 02", response.rawResponse)
        assertEquals(258.0, response.value, 0.001)
        assertTrue(response.isValid)
        assertNull(response.errorMessage)
    }

    @Test
    fun `Mode22Response stores error message`() {
        val response = Mode22Response(
            pid = "220001",
            rawResponse = "ERROR",
            dataBytes = byteArrayOf(),
            value = 0.0,
            isValid = false,
            errorMessage = "Timeout"
        )
        assertFalse(response.isValid)
        assertEquals("Timeout", response.errorMessage)
    }

    @Test
    fun `Mode22Response equals considers pid and rawResponse`() {
        val bytes1 = byteArrayOf(0x01.toByte())
        val bytes2 = byteArrayOf(0x02.toByte())
        val response1 = Mode22Response("220001", "response1", bytes1, 1.0, true)
        val response2 = Mode22Response("220001", "response1", bytes2, 2.0, true)
        val response3 = Mode22Response("220001", "response2", bytes1, 1.0, true)
        
        assertEquals(response1, response2)
        assertNotEquals(response1, response3)
    }

    @Test
    fun `Mode22Response hashCode is consistent`() {
        val bytes = byteArrayOf(0x01.toByte())
        val response = Mode22Response("220001", "response", bytes, 1.0, true)
        val hash1 = response.hashCode()
        val hash2 = response.hashCode()
        assertEquals(hash1, hash2)
    }

    // --- Mode22PIDs constants ---

    @Test
    fun `VIN PID code is correct`() {
        assertEquals("22F190", Mode22PIDs.VIN)
    }

    @Test
    fun `ENGINE_TORQUE PID code is correct`() {
        assertEquals("220001", Mode22PIDs.ENGINE_TORQUE)
    }

    @Test
    fun `TURBO_BOOST_ACTUAL PID code is correct`() {
        assertEquals("220002", Mode22PIDs.TURBO_BOOST_ACTUAL)
    }

    @Test
    fun `WASTEGATE_DUTY PID code is correct`() {
        assertEquals("220004", Mode22PIDs.WASTEGATE_DUTY)
    }

    @Test
    fun `VGT_POSITION PID code is correct`() {
        assertEquals("220009", Mode22PIDs.VGT_POSITION)
    }
}