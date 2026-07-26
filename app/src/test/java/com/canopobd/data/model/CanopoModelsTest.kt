package com.canopobd.data.model

import org.junit.Assert.*
import org.junit.Test

class CanopoModelsTest {

    @Test
    fun `DriveScore grade A+ for score 95`() {
        val score = DriveScore(score = 95)
        assertEquals("A+", score.grade)
    }

    @Test
    fun `DriveScore grade F for score below 50`() {
        val score = DriveScore(score = 40)
        assertEquals("F", score.grade)
    }

    @Test
    fun `FuelTrimAnalysis status is OK when trim near zero`() {
        val analysis = FuelTrimAnalysis(
            stftB1 = 1.0, ltftB1 = 1.0,
            stftB2 = -1.0, ltftB2 = -1.0,
            totalTrimB1 = 2.0, totalTrimB2 = -2.0
        )
        assertEquals("OK", analysis.statusB1)
        assertEquals("OK", analysis.statusB2)
    }

    @Test
    fun `FuelTrimAnalysis totalTrim is sum of short and long term`() {
        val analysis = FuelTrimAnalysis(
            stftB1 = 3.0, ltftB1 = 5.0,
            stftB2 = -2.0, ltftB2 = -4.0,
            totalTrimB1 = 8.0, totalTrimB2 = -6.0
        )
        assertEquals(8.0, analysis.totalTrimB1, 0.001)
        assertEquals(-6.0, analysis.totalTrimB2, 0.001)
    }

    @Test
    fun `MaintenanceItem calculates kmRemaining correctly`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 10000,
            intervalKm = 15000,
            currentKm = 22000
        )
        assertEquals(3000, item.kmRemaining)
    }

    @Test
    fun `MaintenanceItem status is OVERDUE when kmRemaining negative`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 10000,
            intervalKm = 15000,
            currentKm = 26000
        )
        assertEquals(MaintenanceStatus.OVERDUE, item.status)
    }

    @Test
    fun `MaintenanceItem status is DUE_SOON when within 10 percent`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 10000,
            intervalKm = 15000,
            currentKm = 24000
        )
        assertEquals(MaintenanceStatus.DUE_SOON, item.status)
    }

    @Test
    fun `MaintenanceItem status is OK when plenty remaining`() {
        val item = MaintenanceItem(
            type = MaintenanceType.OIL_CHANGE,
            lastServiceKm = 10000,
            intervalKm = 15000,
            currentKm = 15000
        )
        assertEquals(MaintenanceStatus.OK, item.status)
    }

    @Test
    fun `PerformanceTestResult stores data correctly`() {
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
    fun `PerformanceTestType speed ranges are correct`() {
        assertEquals(0.0, PerformanceTestType.ZERO_100.startSpeedKmh, 0.001)
        assertEquals(100.0, PerformanceTestType.ZERO_100.endSpeedKmh, 0.001)
        assertEquals(100.0, PerformanceTestType.HUNDRED_200.startSpeedKmh, 0.001)
        assertEquals(200.0, PerformanceTestType.HUNDRED_200.endSpeedKmh, 0.001)
    }

    @Test
    fun `PowerCalculation returns invalid for zero inputs`() {
        val calc = PowerCalculation.calculate(mafGS = 0.0, rpm = 0.0)
        assertFalse(calc.isValid)
    }

    @Test
    fun `PowerCalculation returns valid for normal inputs`() {
        val calc = PowerCalculation.calculate(mafGS = 50.0, rpm = 4000.0)
        if (calc.isValid) {
            assertTrue(calc.horsepower > 0)
            assertTrue(calc.torqueNm > 0)
        }
    }

    @Test
    fun `ColdStartState warmupProgress is 0 at cold start`() {
        val cold = ColdStartState(coolantTempCurrent = -40.0)
        assertEquals(0f, cold.warmupProgress, 0.01f)
    }

    @Test
    fun `ColdStartState warmupProgress is 1 when warm`() {
        val warm = ColdStartState(coolantTempCurrent = 90.0)
        assertEquals(1f, warm.warmupProgress, 0.01f)
    }

    @Test
    fun `ShiftLightConfig defaults are correct`() {
        val config = ShiftLightConfig()
        assertFalse(config.enabled)
        assertEquals(6500, config.redlineRpm)
        assertEquals(5500, config.warningRpm)
        assertTrue(config.flashEnabled)
        assertFalse(config.soundEnabled)
    }

    @Test
    fun `AlertConfig defaults are correct`() {
        val config = AlertConfig()
        assertFalse(config.speedWarningEnabled)
        assertTrue(config.coolantWarningEnabled)
        assertTrue(config.fuelWarningEnabled)
        assertFalse(config.rpmWarningEnabled)
        assertTrue(config.batteryLowWarningEnabled)
    }

    @Test
    fun `TrendHistory has max 60 points`() {
        assertEquals(60, TrendHistory.MAX_POINTS)
    }

    @Test
    fun `ConnectionQuality fromSuccessRate boundaries`() {
        assertEquals(ConnectionQuality.EXCELLENT, ConnectionQuality.fromSuccessRate(1.0))
        assertEquals(ConnectionQuality.EXCELLENT, ConnectionQuality.fromSuccessRate(0.9))
        assertEquals(ConnectionQuality.GOOD, ConnectionQuality.fromSuccessRate(0.8))
        assertEquals(ConnectionQuality.FAIR, ConnectionQuality.fromSuccessRate(0.6))
        assertEquals(ConnectionQuality.POOR, ConnectionQuality.fromSuccessRate(0.4))
    }

    @Test
    fun `PollMode intervals are correct`() {
        assertEquals(50L, PollMode.FAST.pollInterval)
        assertEquals(500L, PollMode.NORMAL.pollInterval)
        assertEquals(2000L, PollMode.ECO.pollInterval)
    }

    @Test
    fun `ReadinessMonitor tracks completion`() {
        val monitor = ReadinessMonitor()
        assertFalse(monitor.allComplete)
        assertEquals(0, monitor.completedCount)
    }

    @Test
    fun `ReadinessMonitor all complete when all flags true`() {
        val monitor = ReadinessMonitor(
            misfire = true, fuelSystem = true, comprehensiveComponent = true,
            catalyst = true, heatedCatalyst = true, evapSystem = true,
            secondaryAirSystem = true, acSystemRefrigerant = true,
            oxygenSensor = true, oxygenSensorHeater = true, egrSystem = true
        )
        assertTrue(monitor.allComplete)
        assertEquals(11, monitor.completedCount)
    }

    @Test
    fun `ActiveAlert stores correct values`() {
        val alert = ActiveAlert(
            type = AlertType.SPEED,
            severity = AlertSeverity.WARNING,
            value = 150f,
            threshold = 130f,
            message = "Speed too high"
        )
        assertEquals(AlertType.SPEED, alert.type)
        assertEquals(150f, alert.value, 0.01f)
        assertEquals(130f, alert.threshold, 0.01f)
    }

    @Test
    fun `AlertType has all types`() {
        assertEquals(11, AlertType.entries.size)
        assertNotNull(AlertType.SPEED.label)
        assertNotNull(AlertType.COOLANT.label)
        assertNotNull(AlertType.FUEL.label)
        assertNotNull(AlertType.RPM.label)
        assertNotNull(AlertType.BATTERY.label)
    }

    @Test
    fun `FuelEconomyData fromL100km calculates all units`() {
        val data = FuelEconomyData.fromL100km(8.0)
        assertEquals(8.0, data.currentL100km, 0.001)
        assertEquals(12.5, data.currentKmL, 0.01)
        assertTrue(data.currentMpgUs > 0)
        assertTrue(data.currentMpgUk > 0)
    }

    @Test
    fun `FuelEconomyData fromL100km returns empty for invalid input`() {
        val zero = FuelEconomyData.fromL100km(0.0)
        assertEquals(0.0, zero.currentL100km, 0.001)
    }

    @Test
    fun `OBDData defaults are correct`() {
        val data = OBDData()
        assertEquals(0.0, data.rpm, 0.001)
        assertEquals(0.0, data.speed, 0.001)
        assertEquals(0.0, data.coolantTemp, 0.001)
        assertTrue(data.timestamp > 0)
    }

    @Test
    fun `OBDConnectionState sealed class works correctly`() {
        val disconnected = OBDConnectionState.Disconnected
        val connecting = OBDConnectionState.Connecting
        val connected = OBDConnectionState.Connected
        val error = OBDConnectionState.Error("test")

        assertTrue(disconnected is OBDConnectionState.Disconnected)
        assertTrue(connecting is OBDConnectionState.Connecting)
        assertTrue(connected is OBDConnectionState.Connected)
        assertTrue(error is OBDConnectionState.Error)
    }

    @Test
    fun `BluetoothDeviceInfo stores data correctly`() {
        val device = BluetoothDeviceInfo("ELM327", "00:11:22:33:44:55")
        assertEquals("ELM327", device.name)
        assertEquals("00:11:22:33:44:55", device.address)
    }

    @Test
    fun `DataRecord stores all fields`() {
        val record = DataRecord(
            timestamp = 1000L, rpm = 2000.0, speed = 60.0,
            coolantTemp = 90.0, throttle = 25.0, fuelLevel = 50.0, batteryVoltage = 13.5
        )
        assertEquals(1000L, record.timestamp)
        assertEquals(2000.0, record.rpm, 0.001)
        assertEquals(13.5, record.batteryVoltage, 0.001)
    }

    @Test
    fun `TripData defaults are correct`() {
        val trip = TripData()
        assertEquals(0L, trip.durationSeconds)
        assertEquals(0.0, trip.distanceKm, 0.001)
        assertEquals(0.0, trip.maxSpeedKmh, 0.001)
        assertTrue(trip.startTime > 0)
    }

    @Test
    fun `GPSTrip stores data correctly`() {
        val loc = GPSLocation(48.8566, 2.3522, 35.0, 30f, 90f, 3f, 1000L)
        val trip = GPSTrip(
            id = "TEST", startTime = 1000L, endTime = 2000L,
            locations = listOf(loc), distanceKm = 1.5,
            maxSpeedKmh = 80.0, avgSpeedKmh = 60.0
        )
        assertEquals("TEST", trip.id)
        assertEquals(1, trip.locations.size)
        assertEquals(1.5, trip.distanceKm, 0.001)
    }

    @Test
    fun `GPSLocation stores coordinates correctly`() {
        val loc = GPSLocation(48.8566, 2.3522, 35.0, 30f, 90f, 3f, 1000L)
        assertEquals(48.8566, loc.latitude, 0.0001)
        assertEquals(2.3522, loc.longitude, 0.0001)
        assertEquals(35.0, loc.altitude, 0.001)
    }

    @Test
    fun `ColorTheme fromName handles unknown theme`() {
        assertEquals(ColorTheme.CANOPO, ColorTheme.fromName("UNKNOWN_THEME"))
    }

    @Test
    fun `CarProfile fromId returns correct profile`() {
        val profile = CarProfile.fromId("astra_j_2012_14t")
        assertEquals(CarProfile.ASTRA_J_2012_14T, profile)
    }

    @Test
    fun `CarProfile fromId returns null for unknown id`() {
        assertNull(CarProfile.fromId("unknown"))
    }

    @Test
    fun `CarProfile default is ASTRA_J_2012_14T`() {
        assertEquals(CarProfile.ASTRA_J_2012_14T, CarProfile.default())
    }

    @Test
    fun `TimingChainPhase labels are non-empty`() {
        TimingChainPhase.entries.forEach { phase ->
            assertTrue(phase.label.isNotEmpty())
            assertTrue(phase.description.isNotEmpty())
        }
    }

    @Test
    fun `TurboCoolDownState defaults are correct`() {
        val state = TurboCoolDownState()
        assertFalse(state.isActive)
        assertEquals(0, state.secondsRemaining)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `OilData defaults are correct`() {
        val data = OilData()
        assertEquals(0.0, data.temperature, 0.001)
        assertEquals(0.0, data.pressure, 0.001)
        assertEquals(100, data.oilLifeRemaining)
        assertFalse(data.consumptionWarning)
    }

    @Test
    fun `CsvImportEntry stores data correctly`() {
        val entry = CsvImportEntry(
            timestamp = 1000L, rpm = 2000.0, speed = 60.0,
            coolantTemp = 90.0, throttle = 25.0, fuelLevel = 50.0, batteryVoltage = 13.5
        )
        assertEquals(1000L, entry.timestamp)
        assertEquals(2000.0, entry.rpm, 0.001)
    }

    @Test
    fun `MaintenanceType entries cover common services`() {
        assertEquals(10, MaintenanceType.entries.size)
        assertNotNull(MaintenanceType.OIL_CHANGE.defaultInterval)
        assertNotNull(MaintenanceType.TIRES.defaultInterval)
        assertNotNull(MaintenanceType.BRAKE_PADS.defaultInterval)
    }

    @Test
    fun `DTCResponse combines codes correctly`() {
        val dtc1 = DiagnosticTroubleCode("P0301", "Cylinder 1 Misfire")
        val dtc2 = DiagnosticTroubleCode("P0171", "System Too Lean", pending = true)
        val response = DTCResponse(listOf(dtc1), listOf(dtc2))
        assertEquals(1, response.codes.size)
        assertEquals(1, response.pendingCodes.size)
    }

    @Test
    fun `DiagnosticTroubleCode pending flag works`() {
        val normal = DiagnosticTroubleCode("P0301", "desc")
        val pending = DiagnosticTroubleCode("P0301", "desc", pending = true)
        assertFalse(normal.pending)
        assertTrue(pending.pending)
    }

    @Test
    fun `TurboData defaults are correct`() {
        val data = TurboData()
        assertEquals(0.0, data.boostPressure, 0.001)
        assertEquals(0.0, data.boostTarget, 0.001)
        assertEquals(0.0, data.wastegateDutyCycle, 0.001)
        assertEquals(100, data.turboHealthScore)
        assertFalse(data.overboostActive)
        assertFalse(data.underboostDetected)
    }

    @Test
    fun `TimingChainState defaults are correct`() {
        val state = TimingChainState()
        assertEquals(100, state.healthScore)
        assertFalse(state.coldStartRattleDetected)
        assertEquals(0.0, state.idleRpmVariation, 0.001)
        assertFalse(state.isWarmedUp)
        assertEquals(TimingChainPhase.UNKNOWN, state.phase)
        assertEquals(0, state.recordedSamples)
    }

    @Test
    fun `ConnectionStats successRate is 1 when only successes`() {
        val stats = ConnectionStats(successCount = 10, failureCount = 0)
        assertEquals(1.0, stats.successRate, 0.001)
    }

    @Test
    fun `ConnectionStats successRate is 0 when only failures`() {
        val stats = ConnectionStats(successCount = 0, failureCount = 5)
        assertEquals(0.0, stats.successRate, 0.001)
    }

    @Test
    fun `ConnectionStats totalCount equals success plus failure`() {
        val stats = ConnectionStats(successCount = 8, failureCount = 2)
        assertEquals(10, stats.totalCount)
    }

    @Test
    fun `ConnectionStats copy preserves values`() {
        val original = ConnectionStats(successCount = 5, failureCount = 1)
        val copied = original.copy(successCount = 6, quality = ConnectionQuality.GOOD)
        assertEquals(6, copied.successCount)
        assertEquals(1, copied.failureCount)
        assertEquals(ConnectionQuality.GOOD, copied.quality)
    }
}
