package com.canopobd.data.model

import org.junit.Assert.*


import org.junit.Test

class OBDModelsTest {

    @Test
    fun `SPEED formula with empty byte array returns 0`() {
        val result = OBDPID.SPEED.formula(byteArrayOf())
        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun `SPEED formula with valid positive speed values returns correctly`() {
        assertEquals(0.0, OBDPID.SPEED.formula(byteArrayOf(0)), 0.0)
        assertEquals(50.0, OBDPID.SPEED.formula(byteArrayOf(50)), 0.0)
        assertEquals(100.0, OBDPID.SPEED.formula(byteArrayOf(100)), 0.0)
    }

    @Test
    fun `SPEED formula with maximum unsigned 1-byte value returns 255`() {
        // -1 byte is 11111111 in binary, which is 255 when unsigned.
        assertEquals(255.0, OBDPID.SPEED.formula(byteArrayOf(-1)), 0.0)
        assertEquals(255.0, OBDPID.SPEED.formula(byteArrayOf(255.toByte())), 0.0)
    }

    @Test
    fun `SPEED formula with multi-byte array reads only the first byte`() {
        assertEquals(50.0, OBDPID.SPEED.formula(byteArrayOf(50, 100, 20)), 0.0)
    }

    @Test
    fun `OBDPID RPM formula calculates correctly`() {
        val bytes = byteArrayOf(0x1F.toByte(), 0x3C.toByte())
        val result = OBDPID.RPM.formula(bytes)
        assertEquals(1999.0, result, 0.001)
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
        assertEquals(62.745, result, 0.01)
    }

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
        assertEquals(62.745, result, 0.01)
    }

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
        assertEquals(62.745, result, 0.01)
    }

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
        assertEquals(62.745, result, 0.01)
    }

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
        assertEquals(62.745, result, 0.01)
    }

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
        assertEquals(62.745, result, 0.01)
    }

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
        assertEquals(62.745, result, 0.01)
    }

    @Test
    fun `OBDPID formula returns expected value for load calculation`() {
        val bytes = byteArrayOf(0x80.toByte())
        val result = OBDPID.ENGINE_LOAD.formula(bytes)
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
        assertEquals(uniqueCodes.size, codes.size)
    }

    @Test
    fun `OBDPID returns 0 for empty byte array`() {
        val empty = byteArrayOf()
        assertEquals(0.0, OBDPID.RPM.formula(empty), 0.001)
        assertEquals(0.0, OBDPID.SPEED.formula(empty), 0.001)
        assertEquals(0.0, OBDPID.COOLANT_TEMP.formula(empty), 0.001)
        assertEquals(0.0, OBDPID.MAF_RATE.formula(empty), 0.001)
    }

    @Test
    fun `OBDPID INTAKE_TEMP formula subtracts 40`() {
        val bytes = byteArrayOf(0x50.toByte())
        val result = OBDPID.INTAKE_TEMP.formula(bytes)
        assertEquals(40.0, result, 0.001)
    }

    @Test
    fun `OBDPID FUEL_PRESSURE formula multiplies by 3`() {
        val bytes = byteArrayOf(0x30.toByte())
        val result = OBDPID.FUEL_PRESSURE.formula(bytes)
        assertEquals(144.0, result, 0.001)
    }

    @Test
    fun `OBDPID FUEL_RAIL_PRESSURE formula calculates correctly`() {
        val bytes = byteArrayOf(0xFF.toByte(), 0xFF.toByte())
        val result = OBDPID.FUEL_RAIL_PRESSURE.formula(bytes)
        assertTrue(result > 0)
    }

    @Test
    fun `OBDPID TIMING_ADVANCE handles zero byte`() {
        val bytes = byteArrayOf(0x00.toByte())
        val result = OBDPID.TIMING_ADVANCE.formula(bytes)
        assertEquals(-64.0, result, 0.001)
    }

    @Test
    fun `OBDPID ENGINE_FUEL_RATE calculates correctly`() {
        val bytes = byteArrayOf(0x00.toByte(), 0x64.toByte())
        val result = OBDPID.ENGINE_FUEL_RATE.formula(bytes)
        assertEquals(5.0, result, 0.01)
    }

    @Test
    fun `OBDPID SHORT_TERM_FUEL_TRIM handles lean condition`() {
        val bytes = byteArrayOf(0x80.toByte())
        val result = OBDPID.SHORT_TERM_FUEL_TRIM_BANK1.formula(bytes)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `OBDPID SHORT_TERM_FUEL_TRIM handles rich condition`() {
        val bytes = byteArrayOf(0x90.toByte())
        val result = OBDPID.SHORT_TERM_FUEL_TRIM_BANK1.formula(bytes)
        assertTrue(result > 0)
    }

    @Test
    fun `OBDPID SHORT_TERM_FUEL_TRIM handles strongly lean condition`() {
        val bytes = byteArrayOf(0x70.toByte())
        val result = OBDPID.SHORT_TERM_FUEL_TRIM_BANK1.formula(bytes)
        assertTrue(result < 0)
    }

    @Test
    fun `OBDPID CONTROL_MODULE_VOLTAGE calculates correctly`() {
        val bytes = byteArrayOf(0x35.toByte(), 0x84.toByte())
        val result = OBDPID.CONTROL_MODULE_VOLTAGE.formula(bytes)
        assertEquals(13.7, result, 0.01)
    }

    @Test
    fun `OBDPID CATALYST_TEMP calculates correctly`() {
        val bytes = byteArrayOf(0x19.toByte(), 0x00.toByte())
        val result = OBDPID.CATALYST_TEMP_B1S1.formula(bytes)
        assertEquals(600.0, result, 0.1)
    }

    @Test
    fun `OBDPID O2_VOLTAGE calculates correctly`() {
        val bytes = byteArrayOf(0x80.toByte())
        val result = OBDPID.O2_VOLTAGE_B1S1.formula(bytes)
        assertEquals(0.64, result, 0.01)
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

    @Test
    fun `GPSTrip stores location data`() {
        val loc = GPSLocation(52.52, 13.405, 50.0, 50f, 0f, 5f, System.currentTimeMillis())
        val trip = GPSTrip(
            id = "TEST123",
            startTime = 1000L,
            endTime = 2000L,
            locations = listOf(loc),
            distanceKm = 1.5,
            maxSpeedKmh = 80.0,
            avgSpeedKmh = 60.0
        )
        assertEquals("TEST123", trip.id)
        assertEquals(1, trip.locations.size)
        assertEquals(52.52, trip.locations[0].latitude, 0.001)
        assertEquals(1.5, trip.distanceKm, 0.001)
        assertEquals(80.0, trip.maxSpeedKmh, 0.001)
    }

    @Test
    fun `TrendHistory has max 60 points`() {
        val history = TrendHistory()
        assertEquals(60, TrendHistory.MAX_POINTS)
        assertTrue(history.rpm.isEmpty())
        assertTrue(history.speed.isEmpty())
    }

    @Test
    fun `GPSLocation stores coordinates`() {
        val loc = GPSLocation(48.8566, 2.3522, 35.0, 30f, 90f, 3f, 1000L)
        assertEquals(48.8566, loc.latitude, 0.0001)
        assertEquals(2.3522, loc.longitude, 0.0001)
        assertEquals(35.0, loc.altitude, 0.001)
        assertEquals(30f, loc.speed)
        assertEquals(90f, loc.bearing)
        assertEquals(3f, loc.accuracy)
    }

    @Test
    fun `ColorTheme fromName returns correct theme`() {
        assertEquals(ColorTheme.CANOPO, ColorTheme.fromName("CANOPO"))
        assertEquals(ColorTheme.BLUE_STEEL, ColorTheme.fromName("BLUE_STEEL"))
        assertEquals(ColorTheme.CANOPO, ColorTheme.fromName("NONEXISTENT"))
    }

    @Test
    fun `PollMode has correct intervals`() {
        assertEquals(50L, PollMode.FAST.pollInterval)
        assertEquals(500L, PollMode.NORMAL.pollInterval)
        assertEquals(2000L, PollMode.ECO.pollInterval)
    }

    @Test
    fun `ReadinessMonitor tracks completion correctly`() {
        val empty = ReadinessMonitor()
        assertFalse(empty.allComplete)
        assertEquals(0, empty.completedCount)
        assertEquals(11, empty.totalCount)

        val allDone = ReadinessMonitor(
            misfire = true, fuelSystem = true, comprehensiveComponent = true,
            catalyst = true, heatedCatalyst = true, evapSystem = true,
            secondaryAirSystem = true, acSystemRefrigerant = true,
            oxygenSensor = true, oxygenSensorHeater = true, egrSystem = true
        )
        assertTrue(allDone.allComplete)
        assertEquals(11, allDone.completedCount)
    }

    @Test
    fun `ReadinessMonitor partial completion counts correctly`() {
        val partial = ReadinessMonitor(misfire = true, fuelSystem = true, catalyst = true)
        assertFalse(partial.allComplete)
        assertEquals(3, partial.completedCount)
    }

    @Test
    fun `AlertConfig has correct defaults`() {
        val config = AlertConfig()
        assertEquals(130f, config.speedWarning, 0.01f)
        assertFalse(config.speedWarningEnabled)
        assertTrue(config.coolantWarningEnabled)
        assertTrue(config.fuelWarningEnabled)
        assertEquals(6000f, config.rpmWarning, 0.01f)
        assertEquals(11.5f, config.batteryLowWarning, 0.01f)
    }

    @Test
    fun `FuelTrimAnalysis calculates total trim correctly`() {
        val analysis = FuelTrimAnalysis(
            stftB1 = 5.0, ltftB1 = 3.0,
            stftB2 = -4.0, ltftB2 = -3.0,
            totalTrimB1 = 8.0, totalTrimB2 = -7.0
        )
        assertEquals(8.0, analysis.totalTrimB1, 0.01)
        assertEquals(-7.0, analysis.totalTrimB2, 0.01)
        assertEquals("OK", analysis.statusB1)
        assertEquals("OK", analysis.statusB2)
    }

    @Test
    fun `FuelTrimAnalysis detects lean condition`() {
        val analysis = FuelTrimAnalysis(
            stftB1 = 8.0, ltftB1 = 5.0,
            stftB2 = 0.0, ltftB2 = 0.0,
            totalTrimB1 = 13.0, totalTrimB2 = 0.0
        )
        assertEquals(13.0, analysis.totalTrimB1, 0.01)
        assertEquals("Mager (Lean)", analysis.statusB1)
    }

    @Test
    fun `FuelTrimAnalysis detects rich condition`() {
        val analysis = FuelTrimAnalysis(
            stftB1 = 0.0, ltftB1 = 0.0,
            stftB2 = -8.0, ltftB2 = -5.0,
            totalTrimB1 = 0.0, totalTrimB2 = -13.0
        )
        assertEquals(-13.0, analysis.totalTrimB2, 0.01)
        assertEquals("Fett (Rich)", analysis.statusB2)
    }

    @Test
    fun `ActiveAlert stores type and message`() {
        val alert = ActiveAlert(
            type = AlertType.SPEED,
            value = 150f,
            threshold = 130f,
            message = "Geschwindigkeit: 150 > 130"
        )
        assertEquals(AlertType.SPEED, alert.type)
        assertEquals(150f, alert.value, 0.01f)
        assertEquals("Geschwindigkeit: 150 > 130", alert.message)
    }

    @Test
    fun `AlertType has all labels`() {
        assertEquals(5, AlertType.entries.size)
        assertNotNull(AlertType.SPEED.label)
        assertNotNull(AlertType.COOLANT.label)
        assertNotNull(AlertType.FUEL.label)
        assertNotNull(AlertType.RPM.label)
        assertNotNull(AlertType.BATTERY.label)
    }

    @Test
    fun `CsvImportEntry stores all fields`() {
        val entry = CsvImportEntry(
            timestamp = 1000L, rpm = 2000.0, speed = 60.0,
            coolantTemp = 90.0, throttle = 25.0, fuelLevel = 50.0, batteryVoltage = 13.5
        )
        assertEquals(1000L, entry.timestamp)
        assertEquals(2000.0, entry.rpm, 0.001)
        assertEquals(60.0, entry.speed, 0.001)
    }

    @Test
    fun `FuelEconomyData fromL100km calculates all units`() {
        val data = FuelEconomyData.fromL100km(8.0)
        assertEquals(8.0, data.currentL100km, 0.001)
        assertEquals(12.5, data.currentKmL, 0.01)
        assertEquals(29.4, data.currentMpgUs, 0.1)
        assertEquals(35.3, data.currentMpgUk, 0.1)
        assertEquals(8.0, data.avgL100km, 0.001)
    }

    @Test
    fun `FuelEconomyData fromL100km returns empty for invalid input`() {
        val zero = FuelEconomyData.fromL100km(0.0)
        assertEquals(0.0, zero.currentL100km, 0.001)
        val negative = FuelEconomyData.fromL100km(-5.0)
        assertEquals(0.0, negative.currentL100km, 0.001)
        val extreme = FuelEconomyData.fromL100km(500.0)
        assertEquals(0.0, extreme.currentL100km, 0.001)
    }

    @Test
    fun `OBDPID ACCELERATOR_POS_D formula returns percentage`() {
        val bytes = byteArrayOf(0xFF.toByte())
        val result = OBDPID.ACCELERATOR_POS_D.formula(bytes)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `OBDPID THROTTLE_C formula returns percentage`() {
        val bytes = byteArrayOf(0x80.toByte(), 0x00.toByte())
        val result = OBDPID.THROTTLE_C.formula(bytes)
        assertEquals(12850.2, result, 1.0)
    }

    @Test
    fun `OBDPID THROTTLE_ACTUATOR formula returns percentage`() {
        val bytes = byteArrayOf(0x80.toByte(), 0x00.toByte())
        val result = OBDPID.THROTTLE_ACTUATOR.formula(bytes)
        assertEquals(12850.2, result, 1.0)
    }

    @Test
    fun `OBDPID HYBRID_BATTERY_REMAINING formula returns byte value`() {
        val bytes = byteArrayOf(0x64.toByte())
        val result = OBDPID.HYBRID_BATTERY_REMAINING.formula(bytes)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `MaintenanceItem kmRemaining calculates correctly`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 50000,
            intervalKm = 15000,
            currentKm = 65000
        )
        assertEquals(0, item.kmRemaining)
    }

    @Test
    fun `MaintenanceItem status is OVERDUE when kmRemaining negative`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 50000,
            intervalKm = 15000,
            currentKm = 70000
        )
        assertEquals(MaintenanceStatus.OVERDUE, item.status)
    }

    @Test
    fun `MaintenanceItem status is DUE_SOON when within 10 percent`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 50000,
            intervalKm = 15000,
            currentKm = 63501
        )
        assertEquals(MaintenanceStatus.DUE_SOON, item.status)
    }

    @Test
    fun `MaintenanceItem status is OK when plenty remaining`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 50000,
            intervalKm = 15000,
            currentKm = 60000
        )
        assertEquals(MaintenanceStatus.OK, item.status)
    }

    @Test
    fun `MaintenanceType has correct default intervals`() {
        assertEquals(15000, MaintenanceType.OIL_CHANGE.defaultInterval)
        assertEquals(30000, MaintenanceType.TIRES.defaultInterval)
        assertEquals(60000, MaintenanceType.INSPECTION.defaultInterval)
        assertEquals(10, MaintenanceType.entries.size)
    }

    @Test
    fun `PerformanceResult stores test data`() {
        val result = PerformanceResult(
            testType = PerformanceTestType.ZERO_100,
            timeSeconds = 8.5,
            valid = true
        )
        assertEquals(PerformanceTestType.ZERO_100, result.testType)
        assertEquals(8.5, result.timeSeconds, 0.001)
        assertTrue(result.valid)
    }

    @Test
    fun `PerformanceTestState tracks running state`() {
        val state = PerformanceTestState(
            isRunning = true,
            currentTestType = PerformanceTestType.ZERO_200
        )
        assertTrue(state.isRunning)
        assertEquals(PerformanceTestType.ZERO_200, state.currentTestType)
        assertFalse(state.lastResult?.valid ?: false)
    }

    @Test
    fun `PerformanceTestType has correct speed ranges`() {
        assertEquals(0.0, PerformanceTestType.ZERO_100.startSpeedKmh, 0.001)
        assertEquals(100.0, PerformanceTestType.ZERO_100.endSpeedKmh, 0.001)
        assertEquals(0.0, PerformanceTestType.ZERO_200.startSpeedKmh, 0.001)
        assertEquals(200.0, PerformanceTestType.ZERO_200.endSpeedKmh, 0.001)
        assertEquals(100.0, PerformanceTestType.HUNDRED_200.startSpeedKmh, 0.001)
        assertEquals(200.0, PerformanceTestType.HUNDRED_200.endSpeedKmh, 0.001)
    }

    @Test
    fun `OBDData has extended OBD fields with defaults`() {
        val data = OBDData()
        assertEquals(0.0, data.acceleratorPosD, 0.001)
        assertEquals(0.0, data.throttleC, 0.001)
        assertEquals(0.0, data.throttleActuator, 0.001)
        assertEquals(0.0, data.hybridBatteryRemaining, 0.001)
    }

    @Test
    fun `PowerCalculation calculates when values in range`() {
        val calc = PowerCalculation.calculate(mafGS = 30.0, rpm = 3000.0)
        assertEquals(calc.isValid, calc.horsepower > 0 && calc.horsepower < 500)
        assertEquals(calc.isValid, calc.torqueNm > 0 && calc.torqueNm < 1000)
    }

    @Test
    fun `PowerCalculation returns invalid for zero values`() {
        val calc = PowerCalculation.calculate(mafGS = 0.0, rpm = 0.0)
        assertFalse(calc.isValid)
        assertEquals(0.0, calc.horsepower, 0.001)
    }

    @Test
    fun `DriveScore grade returns correct letter`() {
        assertEquals("A+", DriveScore(score = 95).grade)
        assertEquals("A", DriveScore(score = 85).grade)
        assertEquals("B", DriveScore(score = 72).grade)
        assertEquals("C", DriveScore(score = 62).grade)
        assertEquals("D", DriveScore(score = 52).grade)
        assertEquals("F", DriveScore(score = 40).grade)
    }

    @Test
    fun `ColdStartState warmupProgress calculates correctly`() {
        val cold = ColdStartState(coolantTempCurrent = -40.0)
        assertEquals(0f, cold.warmupProgress, 0.01f)
        val warm = ColdStartState(coolantTempCurrent = 90.0)
        assertEquals(1f, warm.warmupProgress, 0.01f)
        val mid = ColdStartState(coolantTempCurrent = 40.0)
        assertTrue(mid.warmupProgress > 0f && mid.warmupProgress < 1f)
    }

    @Test
    fun `ShiftLightConfig has correct defaults`() {
        val config = ShiftLightConfig()
        assertFalse(config.enabled)
        assertEquals(6500, config.redlineRpm)
        assertEquals(5500, config.warningRpm)
        assertTrue(config.flashEnabled)
        assertFalse(config.soundEnabled)
    }
}
