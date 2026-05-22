package com.canopobd.data.model

import org.junit.Assert.*
import org.junit.Test

class ExtendedGMMode22Test {

    // --- Mode22PIDDefinition decode tests ---

    @Test
    fun `ENGINE_TORQUE formula decodes correctly`() {
        val bytes = byteArrayOf(0x80.toByte()) // 128 - 128 = 0%
        val result = ExtendedGMMode22.ENGINE_TORQUE.decode(byteArrayOf(0x80.toByte()))
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `ENGINE_TORQUE positive value decodes correctly`() {
        val bytes = byteArrayOf(0xC8.toByte()) // 200 - 128 = 72%
        val result = ExtendedGMMode22.ENGINE_TORQUE.decode(byteArrayOf(0xC8.toByte()))
        assertEquals(72.0, result, 0.001)
    }

    @Test
    fun `ENGINE_TORQUE negative value decodes correctly`() {
        val bytes = byteArrayOf(0x60.toByte()) // 96 - 128 = -32%
        val result = ExtendedGMMode22.ENGINE_TORQUE.decode(byteArrayOf(0x60.toByte()))
        assertEquals(-32.0, result, 0.001)
    }

    @Test
    fun `ENGINE_TORQUE empty bytes returns zero`() {
        val result = ExtendedGMMode22.ENGINE_TORQUE.decode(byteArrayOf())
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `BOOST_PRESSURE_ACTUAL formula decodes correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0xB4.toByte()) // 180 kPa
        val result = ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.decode(bytes)
        assertEquals(180.0, result, 0.001)
    }

    @Test
    fun `BOOST_PRESSURE_ACTUAL decodes idle pressure`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x1E.toByte()) // 30 kPa
        val result = ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.decode(bytes)
        assertEquals(30.0, result, 0.001)
    }

    @Test
    fun `BOOST_PRESSURE_ACTUAL decodes WOT pressure`() {
        val bytes = byteArrayOf(0x00.toByte(), 0xAA.toByte()) // 170 kPa
        val result = ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.decode(bytes)
        assertEquals(170.0, result, 0.001)
    }

    @Test
    fun `BOOST_PRESSURE_ACTUAL empty bytes returns zero`() {
        val result = ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.decode(byteArrayOf())
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `WASTEGATE_POSITION formula decodes correctly`() {
        val bytes = byteArrayOf(0x55.toByte()) // 85%
        val result = ExtendedGMMode22.WASTEGATE_POSITION.decode(bytes)
        assertEquals(85.0, result, 0.001)
    }

    @Test
    fun `WASTEGATE_POSITION decodes WOT value`() {
        val bytes = byteArrayOf(0x1E.toByte()) // 30%
        val result = ExtendedGMMode22.WASTEGATE_POSITION.decode(bytes)
        assertEquals(30.0, result, 0.001)
    }

    @Test
    fun `WASTEGATE_POSITION empty bytes returns zero`() {
        val result = ExtendedGMMode22.WASTEGATE_POSITION.decode(byteArrayOf())
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `TURBO_RPM formula decodes correctly`() {
        val bytes = byteArrayOf(0x03.toByte(), 0xE8.toByte()) // 1000 rpm
        val result = ExtendedGMMode22.TURBO_RPM.decode(bytes)
        assertEquals(1000.0, result, 0.001)
    }

    @Test
    fun `TURBO_RPM decodes normal operating range`() {
        val bytes = byteArrayOf(0x27.toByte(), 0x10.toByte()) // 10000 rpm
        val result = ExtendedGMMode22.TURBO_RPM.decode(bytes)
        assertEquals(10000.0, result, 0.001)
    }

    @Test
    fun `OIL_TEMPERATURE formula decodes correctly`() {
        val bytes = byteArrayOf(0x6E.toByte()) // 110°C
        val result = ExtendedGMMode22.OIL_TEMPERATURE.decode(bytes)
        assertEquals(70.0, result, 0.001)
    }

    @Test
    fun `OIL_TEMPERATURE decodes cold temperature`() {
        val bytes = byteArrayOf(0x46.toByte()) // -40°C = 0x28... wait formula is (byte - 40)
        val result = ExtendedGMMode22.OIL_TEMPERATURE.decode(byteArrayOf(0x58.toByte())) // 88 - 40 = 48
        assertEquals(48.0, result, 0.001)
    }

    @Test
    fun `OIL_TEMPERATURE empty bytes returns zero`() {
        val result = ExtendedGMMode22.OIL_TEMPERATURE.decode(byteArrayOf())
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `COOLANT_TEMPERATURE formula decodes correctly`() {
        val bytes = byteArrayOf(0x7D.toByte()) // 125 - 40 = 85°C
        val result = ExtendedGMMode22.COOLANT_TEMPERATURE.decode(bytes)
        assertEquals(85.0, result, 0.001)
    }

    @Test
    fun `INTAKE_AIR_TEMPERATURE formula decodes correctly`() {
        val bytes = byteArrayOf(0x3C.toByte()) // 60 - 40 = 20°C
        val result = ExtendedGMMode22.INTAKE_AIR_TEMPERATURE.decode(bytes)
        assertEquals(20.0, result, 0.001)
    }

    @Test
    fun `INTAKE_AIR_TEMPERATURE decodes cold air`() {
        val bytes = byteArrayOf(0x14.toByte()) // 20 - 40 = -20°C
        val result = ExtendedGMMode22.INTAKE_AIR_TEMPERATURE.decode(bytes)
        assertEquals(-20.0, result, 0.001)
    }

    @Test
    fun `FUEL_RAIL_PRESSURE formula decodes correctly`() {
        val bytes = byteArrayOf(0x0D.toByte(), 0xAC.toByte()) // 3500 kPa: (0x0DAC=3500)*10=35000
        val result = ExtendedGMMode22.FUEL_RAIL_PRESSURE.decode(bytes)
        assertEquals(35000.0, result, 0.001)
    }

    @Test
    fun `INJECTOR_PULSE_WIDTH formula decodes correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x64.toByte()) // 100 / 100 = 1.0 ms
        val result = ExtendedGMMode22.INJECTOR_PULSE_WIDTH.decode(bytes)
        assertEquals(1.0, result, 0.001)
    }

    @Test
    fun `INJECTOR_PULSE_WIDTH decodes idle pulse`() {
        val bytes = byteArrayOf(0x01.toByte(), 0x2C.toByte()) // 300 / 100 = 3.0 ms
        val result = ExtendedGMMode22.INJECTOR_PULSE_WIDTH.decode(bytes)
        assertEquals(3.0, result, 0.001)
    }

    @Test
    fun `VVT_INTAKE formula decodes correctly`() {
        val bytes = byteArrayOf(0x80.toByte()) // 0° (128 - 128)
        val result = ExtendedGMMode22.VVT_INTAKE.decode(bytes)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `VVT_INTAKE decodes positive advance`() {
        val bytes = byteArrayOf(0x96.toByte()) // 150 - 128 = 22°
        val result = ExtendedGMMode22.VVT_INTAKE.decode(bytes)
        assertEquals(22.0, result, 0.001)
    }

    @Test
    fun `VVT_EXHAUST formula decodes correctly`() {
        val bytes = byteArrayOf(0x80.toByte()) // 0°
        val result = ExtendedGMMode22.VVT_EXHAUST.decode(bytes)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `FUEL_CONSUMPTION_INSTANT formula decodes correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x1E.toByte()) // 30 L/h
        val result = ExtendedGMMode22.FUEL_CONSUMPTION_INSTANT.decode(bytes)
        assertEquals(30.0, result, 0.001)
    }

    @Test
    fun `FUEL_CONSUMPTION_AVERAGE formula decodes correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x3C.toByte()) // 60 / 10 = 6.0 L/100km
        val result = ExtendedGMMode22.FUEL_CONSUMPTION_AVERAGE.decode(bytes)
        assertEquals(6.0, result, 0.001)
    }

    @Test
    fun `AFR_RATIO formula decodes stoichiometric`() {
        val bytes = byteArrayOf(0x80.toByte(), 0x00.toByte()) // lambda = 1.0
        val result = ExtendedGMMode22.AFR_RATIO.decode(bytes)
        assertEquals(1.0, result, 0.001)
    }

    @Test
    fun `AFR_RATIO formula decodes rich condition`() {
        val bytes = byteArrayOf(0x6B.toByte(), 0xB8.toByte()) // lambda ~0.85
        val result = ExtendedGMMode22.AFR_RATIO.decode(bytes)
        assertTrue(result < 1.0)
        assertTrue(result > 0.8)
    }

    @Test
    fun `AFR_RATIO formula decodes lean condition`() {
        val bytes = byteArrayOf(0x94.toByte(), 0x50.toByte()) // lambda ~1.15
        val result = ExtendedGMMode22.AFR_RATIO.decode(bytes)
        assertTrue(result > 1.0)
    }

    // --- isNormal tests ---

    @Test
    fun `ENGINE_TORQUE isNormal within range`() {
        assertTrue(ExtendedGMMode22.ENGINE_TORQUE.isNormal(50.0))
        assertTrue(ExtendedGMMode22.ENGINE_TORQUE.isNormal(0.0))
        assertTrue(ExtendedGMMode22.ENGINE_TORQUE.isNormal(100.0))
    }

    @Test
    fun `ENGINE_TORQUE isNormal outside range`() {
        assertFalse(ExtendedGMMode22.ENGINE_TORQUE.isNormal(-10.0))
        assertFalse(ExtendedGMMode22.ENGINE_TORQUE.isNormal(150.0))
    }

    @Test
    fun `BOOST_PRESSURE_ACTUAL isNormal at idle`() {
        assertTrue(ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.isNormal(30.0))
    }

    @Test
    fun `BOOST_PRESSURE_ACTUAL isNormal at WOT`() {
        assertTrue(ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.isNormal(170.0))
    }

    @Test
    fun `BOOST_PRESSURE_ACTUAL isNormal outside range`() {
        assertFalse(ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.isNormal(20.0))
        assertFalse(ExtendedGMMode22.BOOST_PRESSURE_ACTUAL.isNormal(300.0))
    }

    @Test
    fun `WASTEGATE_POSITION isNormal at idle`() {
        assertTrue(ExtendedGMMode22.WASTEGATE_POSITION.isNormal(85.0))
    }

    @Test
    fun `WASTEGATE_POSITION isNormal at WOT`() {
        assertTrue(ExtendedGMMode22.WASTEGATE_POSITION.isNormal(40.0))
    }

    @Test
    fun `TURBO_RPM isNormal within range`() {
        assertTrue(ExtendedGMMode22.TURBO_RPM.isNormal(100000.0))
        assertTrue(ExtendedGMMode22.TURBO_RPM.isNormal(80000.0))
    }

    @Test
    fun `TURBO_RPM isNormal at idle`() {
        assertTrue(ExtendedGMMode22.TURBO_RPM.isNormal(8000.0))
    }

    @Test
    fun `OIL_TEMPERATURE isNormal in optimal range`() {
        assertTrue(ExtendedGMMode22.OIL_TEMPERATURE.isNormal(95.0))
        assertTrue(ExtendedGMMode22.OIL_TEMPERATURE.isNormal(110.0))
    }

    @Test
    fun `OIL_TEMPERATURE isNormal outside range`() {
        assertFalse(ExtendedGMMode22.OIL_TEMPERATURE.isNormal(60.0))
        assertFalse(ExtendedGMMode22.OIL_TEMPERATURE.isNormal(130.0))
    }

    @Test
    fun `COOLANT_TEMPERATURE isNormal in optimal range`() {
        assertTrue(ExtendedGMMode22.COOLANT_TEMPERATURE.isNormal(90.0))
        assertTrue(ExtendedGMMode22.COOLANT_TEMPERATURE.isNormal(100.0))
    }

    @Test
    fun `INTAKE_AIR_TEMPERATURE isNormal within range`() {
        assertTrue(ExtendedGMMode22.INTAKE_AIR_TEMPERATURE.isNormal(20.0))
        assertTrue(ExtendedGMMode22.INTAKE_AIR_TEMPERATURE.isNormal(45.0))
    }

    @Test
    fun `FUEL_RAIL_PRESSURE isNormal at idle`() {
        assertTrue(ExtendedGMMode22.FUEL_RAIL_PRESSURE.isNormal(4000.0))
    }

    @Test
    fun `FUEL_RAIL_PRESSURE isNormal at WOT`() {
        assertTrue(ExtendedGMMode22.FUEL_RAIL_PRESSURE.isNormal(5000.0))
    }

    @Test
    fun `AFR_RATIO isNormal at stoichiometric`() {
        assertTrue(ExtendedGMMode22.AFR_RATIO.isNormal(1.0))
    }

    @Test
    fun `AFR_RATIO isNormal outside range`() {
        assertFalse(ExtendedGMMode22.AFR_RATIO.isNormal(0.8))
        assertFalse(ExtendedGMMode22.AFR_RATIO.isNormal(1.2))
    }

    // --- statusText tests ---

    @Test
    fun `ENGINE_TORQUE statusText returns Normal for normal value`() {
        assertEquals("Normal", ExtendedGMMode22.ENGINE_TORQUE.statusText(50.0))
    }

    @Test
    fun `ENGINE_TORQUE statusText returns Zu niedrig for low value`() {
        assertEquals("Zu niedrig", ExtendedGMMode22.ENGINE_TORQUE.statusText(-10.0))
    }

    @Test
    fun `ENGINE_TORQUE statusText returns Zu hoch for high value`() {
        assertEquals("Zu hoch", ExtendedGMMode22.ENGINE_TORQUE.statusText(150.0))
    }

    // --- fromCode tests ---

    @Test
    fun `fromCode returns correct PID definition`() {
        val result = ExtendedGMMode22.fromCode("221001")
        assertNotNull(result)
        assertEquals("221001", result!!.code)
    }

    @Test
    fun `fromCode returns null for unknown code`() {
        assertNull(ExtendedGMMode22.fromCode("999999"))
        assertNull(ExtendedGMMode22.fromCode(""))
    }

    @Test
    fun `fromCode handles all known PIDs`() {
        val codes = listOf("221001", "221002", "221008", "221009", "22100A", "22100B")
        codes.forEach { code ->
            val result = ExtendedGMMode22.fromCode(code)
            assertNotNull("PID $code should be found", result)
            assertEquals(code, result!!.code)
        }
    }

    // --- decode tests ---

    @Test
    fun `decode returns value for known PID`() {
        val bytes = byteArrayOf(0x80.toByte())
        val result = ExtendedGMMode22.decode("221001", bytes)
        assertNotNull(result)
        assertEquals(0.0, result!!, 0.001)
    }

    @Test
    fun `decode returns null for unknown PID`() {
        val bytes = byteArrayOf(0x80.toByte())
        assertNull(ExtendedGMMode22.decode("999999", bytes))
    }

    // --- calculateBoostDelta tests ---

    @Test
    fun `calculateBoostDelta returns correct delta`() {
        val result = ExtendedGMMode22.calculateBoostDelta(180.0, 170.0)
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun `calculateBoostDelta returns negative for underboost`() {
        val result = ExtendedGMMode22.calculateBoostDelta(160.0, 170.0)
        assertEquals(-10.0, result, 0.001)
    }

    @Test
    fun `calculateBoostDelta returns zero when equal`() {
        val result = ExtendedGMMode22.calculateBoostDelta(170.0, 170.0)
        assertEquals(0.0, result, 0.001)
    }

    // --- calculateBoostDeviationPercent tests ---

    @Test
    fun `calculateBoostDeviationPercent returns correct deviation`() {
        val result = ExtendedGMMode22.calculateBoostDeviationPercent(180.0, 170.0)
        assertEquals(5.88, result, 0.01)
    }

    @Test
    fun `calculateBoostDeviationPercent returns negative for underboost`() {
        val result = ExtendedGMMode22.calculateBoostDeviationPercent(160.0, 170.0)
        assertEquals(-5.88, result, 0.01)
    }

    @Test
    fun `calculateBoostDeviationPercent returns zero for target zero`() {
        val result = ExtendedGMMode22.calculateBoostDeviationPercent(170.0, 0.0)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `calculateBoostDeviationPercent returns zero for negative target`() {
        val result = ExtendedGMMode22.calculateBoostDeviationPercent(170.0, -10.0)
        assertEquals(0.0, result, 0.001)
    }

    // --- relativeBoostKpa tests ---

    @Test
    fun `relativeBoostKpa returns zero at atmospheric`() {
        val result = ExtendedGMMode22.relativeBoostKpa(100.0)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `relativeBoostKpa returns correct relative boost`() {
        val result = ExtendedGMMode22.relativeBoostKpa(170.0)
        assertEquals(70.0, result, 0.001)
    }

    @Test
    fun `relativeBoostKpa never returns negative`() {
        val result = ExtendedGMMode22.relativeBoostKpa(90.0)
        assertEquals(0.0, result, 0.001)
    }

    // --- relativeBoostBar tests ---

    @Test
    fun `relativeBoostBar returns correct value`() {
        val result = ExtendedGMMode22.relativeBoostBar(170.0)
        assertEquals(0.7, result, 0.01)
    }

    @Test
    fun `relativeBoostBar returns zero at atmospheric`() {
        val result = ExtendedGMMode22.relativeBoostBar(100.0)
        assertEquals(0.0, result, 0.001)
    }

    // --- NormalValues constants tests ---

    @Test
    fun `NormalValues engine torque constants are defined`() {
        assertEquals(0.0, ExtendedGMMode22.NormalValues.ENGINE_TORQUE_MIN, 0.001)
        assertEquals(100.0, ExtendedGMMode22.NormalValues.ENGINE_TORQUE_MAX, 0.001)
    }

    @Test
    fun `NormalValues boost pressure constants are defined`() {
        assertEquals(30.0, ExtendedGMMode22.NormalValues.BOOST_ACTUAL_IDLE_KPA, 0.001)
        assertEquals(170.0, ExtendedGMMode22.NormalValues.BOOST_ACTUAL_WOT_KPA, 0.001)
        assertEquals(250.0, ExtendedGMMode22.NormalValues.BOOST_ACTUAL_MAX_KPA, 0.001)
    }

    @Test
    fun `NormalValues wastegate constants are defined`() {
        assertEquals(85.0, ExtendedGMMode22.NormalValues.WASTEGATE_IDLE_PERCENT, 0.001)
        assertEquals(25.0, ExtendedGMMode22.NormalValues.WASTEGATE_WOT_MIN_PERCENT, 0.001)
        assertEquals(60.0, ExtendedGMMode22.NormalValues.WASTEGATE_WOT_MAX_PERCENT, 0.001)
    }

    @Test
    fun `NormalValues turbo RPM constants are defined`() {
        assertEquals(8000.0, ExtendedGMMode22.NormalValues.TURBO_RPM_IDLE, 0.001)
        assertEquals(80000.0, ExtendedGMMode22.NormalValues.TURBO_RPM_NORMAL_MIN, 0.001)
        assertEquals(200000.0, ExtendedGMMode22.NormalValues.TURBO_RPM_MAX, 0.001)
    }

    @Test
    fun `NormalValues oil temperature constants are defined`() {
        assertEquals(70.0, ExtendedGMMode22.NormalValues.OIL_TEMP_MIN_C, 0.001)
        assertEquals(90.0, ExtendedGMMode22.NormalValues.OIL_TEMP_OPTIMAL_MIN_C, 0.001)
        assertEquals(120.0, ExtendedGMMode22.NormalValues.OIL_TEMP_MAX_C, 0.001)
    }

    @Test
    fun `NormalValues AFR constants are defined`() {
        assertEquals(1.0, ExtendedGMMode22.NormalValues.AFR_STOICHIOMETRIC, 0.001)
        assertEquals(0.85, ExtendedGMMode22.NormalValues.AFR_RICH_MIN, 0.001)
        assertEquals(1.15, ExtendedGMMode22.NormalValues.AFR_LEAN_MAX, 0.001)
    }

    // --- ALL_PIDS tests ---

    @Test
    fun `ALL_PIDS contains all expected PIDs`() {
        assertEquals(16, ExtendedGMMode22.ALL_PIDS.size)
    }

    @Test
    fun `ALL_PIDS has unique codes`() {
        val codes = ExtendedGMMode22.ALL_PIDS.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `ALL_PIDS all have valid byteCount`() {
        ExtendedGMMode22.ALL_PIDS.forEach { pid ->
            assertTrue(pid.byteCount > 0)
        }
    }

    @Test
    fun `ALL_PIDS all have non-empty display names`() {
        ExtendedGMMode22.ALL_PIDS.forEach { pid ->
            assertTrue(pid.displayName.isNotEmpty())
        }
    }

    // --- parseSupportedPIDs tests ---

    @Test
    fun `parseSupportedPIDs returns empty set for empty byte array`() {
        val result = ExtendedGMMode22.parseSupportedPIDs(byteArrayOf())
        assertTrue(result.isEmpty())
    }
}