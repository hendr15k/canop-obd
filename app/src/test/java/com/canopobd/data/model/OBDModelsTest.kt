package com.canopobd.data.model

import org.junit.Assert.*
import org.junit.Test

class OBDModelsTest {

    @Test
    fun `OBDPID RPM formula calculates correctly`() {
        val bytes = byteArrayOf(0x1F.toByte(), 0x3C.toByte())
        val result = OBDPID.RPM.formula(bytes)
        assertEquals(1999.0, result, 0.001)
    }

    @Test
    fun `OBDPID SPEED formula returns raw byte value`() {
        val bytes = byteArrayOf(0x7B.toByte())
        val result = OBDPID.SPEED.formula(bytes)
        assertEquals(123.0, result, 0.001)
    }

    @Test
    fun `OBDPID COOLANT_TEMP formula subtracts 40`() {
        val bytes = byteArrayOf(0x80.toByte())
        val result = OBDPID.COOLANT_TEMP.formula(bytes)
        assertEquals(88.0, result, 0.001)
    }

    @Test
    fun `OBDPID THROTTLE formula returns percentage`() {
        val bytes = byteArrayOf(0xFF.toByte())
        val result = OBDPID.THROTTLE.formula(bytes)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `OBDPID ENGINE_LOAD formula returns percentage`() {
        val bytes = byteArrayOf(0x80.toByte())
        val result = OBDPID.ENGINE_LOAD.formula(bytes)
        assertEquals(50.196, result, 0.01)
    }

    @Test
    fun `OBDPID FUEL_LEVEL formula returns percentage`() {
        val bytes = byteArrayOf(0x7F.toByte())
        val result = OBDPID.FUEL_LEVEL.formula(bytes)
        assertEquals(49.8, result, 2.0)
    }

    @Test
    fun `OBDPID TIMING_ADVANCE formula divides by 2 and subtracts 64`() {
        val bytes = byteArrayOf(0x7F.toByte())
        val result = OBDPID.TIMING_ADVANCE.formula(bytes)
        assertEquals(-0.5, result, 0.001)
    }

    @Test
    fun `OBDPID MAF_RATE formula calculates air flow rate`() {
        val bytes = byteArrayOf(0x1F.toByte(), 0x3C.toByte())
        val result = OBDPID.MAF_RATE.formula(bytes)
        assertEquals(79.96, result, 0.01)
    }

    @Test
    fun `OBDPID fromCode returns correct PID`() {
        assertEquals(OBDPID.RPM, OBDPID.fromCode("010C"))
        assertEquals(OBDPID.SPEED, OBDPID.fromCode("010D"))
        assertEquals(OBDPID.COOLANT_TEMP, OBDPID.fromCode("0105"))
        assertEquals(OBDPID.THROTTLE, OBDPID.fromCode("0111"))
    }

    @Test
    fun `OBDPID fromCode returns null for unknown code`() {
        assertNull(OBDPID.fromCode("9999"))
        assertNull(OBDPID.fromCode(""))
        assertNull(OBDPID.fromCode("ZZZZ"))
    }

    @Test
    fun `OBDPID fromCode is case sensitive`() {
        assertEquals(OBDPID.RPM, OBDPID.fromCode("010C"))
        assertEquals(OBDPID.SPEED, OBDPID.fromCode("010D"))
        assertNull(OBDPID.fromCode("010c"))
        assertNull(OBDPID.fromCode("010c"))
    }

    @Test
    fun `OBDPID codes are unique`() {
        val codes = OBDPID.entries.map { it.code }
        val uniqueCodes = codes.toSet()
        assertEquals(codes.size, uniqueCodes.size)
    }

    @Test
    fun `MeasurementUnit METRIC converts speed correctly`() {
        assertEquals(100.0, MeasurementUnit.METRIC.convertSpeed(100.0), 0.001)
    }

    @Test
    fun `MeasurementUnit IMPERIAL converts speed to mph`() {
        val result = MeasurementUnit.IMPERIAL.convertSpeed(100.0)
        assertEquals(62.1371, result, 0.01)
    }

    @Test
    fun `MeasurementUnit METRIC converts temperature correctly`() {
        assertEquals(0.0, MeasurementUnit.METRIC.convertTemp(0.0), 0.001)
        assertEquals(100.0, MeasurementUnit.METRIC.convertTemp(100.0), 0.001)
    }

    @Test
    fun `MeasurementUnit IMPERIAL converts temperature to Fahrenheit`() {
        assertEquals(32.0, MeasurementUnit.IMPERIAL.convertTemp(0.0), 0.001)
        assertEquals(212.0, MeasurementUnit.IMPERIAL.convertTemp(100.0), 0.001)
    }

    @Test
    fun `MeasurementUnit labels are defined`() {
        assertEquals("Metric", MeasurementUnit.METRIC.label)
        assertEquals("Imperial", MeasurementUnit.IMPERIAL.label)
    }

    @Test
    fun `MeasurementUnit speed and temp units are defined`() {
        assertEquals("km/h", MeasurementUnit.METRIC.speedUnit)
        assertEquals("mph", MeasurementUnit.IMPERIAL.speedUnit)
        assertEquals("°C", MeasurementUnit.METRIC.tempUnit)
        assertEquals("°F", MeasurementUnit.IMPERIAL.tempUnit)
    }

    @Test
    fun `MeasurementUnit distance unit returns correct label`() {
        assertEquals("km", MeasurementUnit.METRIC.distanceUnit())
        assertEquals("mi", MeasurementUnit.IMPERIAL.distanceUnit())
    }

    @Test
    fun `OBDData has correct defaults`() {
        val data = OBDData()
        assertEquals(0.0, data.rpm, 0.001)
        assertEquals(0.0, data.speed, 0.001)
        assertEquals(0.0, data.coolantTemp, 0.001)
        assertEquals("", data.vin)
        assertTrue(data.timestamp > 0)
    }

    @Test
    fun `OBDConnectionState sealed class has all states`() {
        val disconnected = OBDConnectionState.Disconnected
        val connecting = OBDConnectionState.Connecting
        val connected = OBDConnectionState.Connected
        val error = OBDConnectionState.Error("test error")

        assertTrue(disconnected is OBDConnectionState.Disconnected)
        assertTrue(connecting is OBDConnectionState.Connecting)
        assertTrue(connected is OBDConnectionState.Connected)
        assertTrue(error is OBDConnectionState.Error)
        assertEquals("test error", (error as OBDConnectionState.Error).message)
    }

    @Test
    fun `BluetoothDeviceInfo stores name and address`() {
        val device = BluetoothDeviceInfo("ELM327", "00:11:22:33:44:55")
        assertEquals("ELM327", device.name)
        assertEquals("00:11:22:33:44:55", device.address)
    }

    @Test
    fun `DataRecord stores all fields`() {
        val record = DataRecord(
            timestamp = 1000L,
            rpm = 2000.0,
            speed = 60.0,
            coolantTemp = 90.0,
            throttle = 25.0,
            fuelLevel = 50.0,
            batteryVoltage = 13.5
        )
        assertEquals(1000L, record.timestamp)
        assertEquals(2000.0, record.rpm, 0.001)
        assertEquals(60.0, record.speed, 0.001)
        assertEquals(90.0, record.coolantTemp, 0.001)
        assertEquals(25.0, record.throttle, 0.001)
        assertEquals(50.0, record.fuelLevel, 0.001)
        assertEquals(13.5, record.batteryVoltage, 0.001)
    }

    @Test
    fun `DiagnosticTroubleCode stores code and description`() {
        val dtc = DiagnosticTroubleCode("P0301", "Cylinder 1 Misfire Detected")
        assertEquals("P0301", dtc.code)
        assertEquals("Cylinder 1 Misfire Detected", dtc.description)
        assertFalse(dtc.pending)

        val pendingDtc = DiagnosticTroubleCode("P0171", "System Too Lean", pending = true)
        assertTrue(pendingDtc.pending)
    }

    @Test
    fun `DTCResponse combines codes and pending codes`() {
        val dtc1 = DiagnosticTroubleCode("P0301", "desc1")
        val dtc2 = DiagnosticTroubleCode("P0302", "desc2", pending = true)
        val response = DTCResponse(listOf(dtc1), listOf(dtc2))
        assertEquals(1, response.codes.size)
        assertEquals(1, response.pendingCodes.size)
    }

    @Test
    fun `ControlModuleVoltage formula calculates correctly`() {
        val bytes = byteArrayOf(0x35.toByte(), 0x84.toByte())
        val result = OBDPID.CONTROL_MODULE_VOLTAGE.formula(bytes)
        assertEquals(13.7, result, 0.01)
    }

    @Test
    fun `AbsoluteLoadValue formula calculates correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x80.toByte())
        val result = OBDPID.ABSOLUTE_LOAD_VALUE.formula(bytes)
        assertEquals(50.196, result, 0.1)
    }

    @Test
    fun `EngineFuelRate formula calculates correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x64.toByte())
        val result = OBDPID.ENGINE_FUEL_RATE.formula(bytes)
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun `OBDData has new fields with defaults`() {
        val data = OBDData()
        assertEquals(0.0, data.controlModuleVoltage, 0.001)
        assertEquals(0.0, data.absoluteLoadValue, 0.001)
        assertEquals(0.0, data.engineFuelRate, 0.001)
    }

    @Test
    fun `TripData has correct defaults`() {
        val trip = TripData()
        assertEquals(0L, trip.durationSeconds)
        assertEquals(0.0, trip.distanceKm, 0.001)
        assertEquals(0.0, trip.maxSpeedKmh, 0.001)
        assertEquals(0.0, trip.avgSpeedKmh, 0.001)
        assertEquals(0L, trip.sampleCount)
        assertTrue(trip.startTime > 0)
    }

    @Test
    fun `ConnectionQuality fromSuccessRate returns correct quality`() {
        assertEquals(ConnectionQuality.EXCELLENT, ConnectionQuality.fromSuccessRate(0.95))
        assertEquals(ConnectionQuality.GOOD, ConnectionQuality.fromSuccessRate(0.75))
        assertEquals(ConnectionQuality.FAIR, ConnectionQuality.fromSuccessRate(0.55))
        assertEquals(ConnectionQuality.POOR, ConnectionQuality.fromSuccessRate(0.3))
    }

    @Test
    fun `ConnectionStats calculates success rate correctly`() {
        val stats = ConnectionStats(successCount = 8, failureCount = 2, quality = ConnectionQuality.GOOD)
        assertEquals(10, stats.totalCount)
        assertEquals(0.8, stats.successRate, 0.01)
    }

    @Test
    fun `ConnectionStats successRate is 1 when no requests`() {
        val stats = ConnectionStats()
        assertEquals(1.0, stats.successRate, 0.001)
        assertEquals(ConnectionQuality.EXCELLENT, stats.quality)
    }
}
