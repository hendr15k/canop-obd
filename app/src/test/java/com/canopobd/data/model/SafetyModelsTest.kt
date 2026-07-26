package com.canopobd.data.model

import org.junit.Assert.*
import org.junit.Test

class SafetyModelsTest {

    // --- SystemStatus tests ---

    @Test
    fun `SystemStatus has correct labels`() {
        assertEquals("OK", SystemStatus.OK.label)
        assertEquals("Warnung", SystemStatus.WARNING.label)
        assertEquals("Fehler", SystemStatus.FAULT.label)
        assertEquals("Deaktiviert", SystemStatus.DISABLED.label)
        assertEquals("Unbekannt", SystemStatus.UNKNOWN.label)
    }

    @Test
    fun `LevelStatus has correct labels`() {
        assertEquals("Normal", LevelStatus.NORMAL.label)
        assertEquals("Niedrig", LevelStatus.LOW.label)
        assertEquals("Kritisch", LevelStatus.CRITICAL.label)
        assertEquals("Unbekannt", LevelStatus.UNKNOWN.label)
    }

    @Test
    fun `DtcSeverity has correct labels`() {
        assertEquals("Information", DtcSeverity.INFO.label)
        assertEquals("Warnung", DtcSeverity.WARNING.label)
        assertEquals("Kritisch", DtcSeverity.CRITICAL.label)
    }

    @Test
    fun `ESPSelectedMode has correct labels`() {
        assertEquals("Standard", ESPSelectedMode.STANDARD.label)
        assertEquals("Sport", ESPSelectedMode.SPORT.label)
        assertEquals("Aus", ESPSelectedMode.OFF.label)
    }

    // --- WheelSpeeds tests ---

    @Test
    fun `WheelSpeeds maxSpeed returns correct value`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 60.0, rearLeft = 55.0, rearRight = 45.0)
        assertEquals(60.0, ws.maxSpeed, 0.001)
    }

    @Test
    fun `WheelSpeeds minSpeed returns correct value`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 60.0, rearLeft = 55.0, rearRight = 45.0)
        assertEquals(45.0, ws.minSpeed, 0.001)
    }

    @Test
    fun `WheelSpeeds speedDifference returns correct value`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 60.0, rearLeft = 55.0, rearRight = 45.0)
        assertEquals(15.0, ws.speedDifference, 0.001)
    }

    @Test
    fun `WheelSpeeds frontAxleDiff returns correct value`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 55.0, rearLeft = 50.0, rearRight = 50.0)
        assertEquals(5.0, ws.frontAxleDiff, 0.001)
    }

    @Test
    fun `WheelSpeeds rearAxleDiff returns correct value`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 50.0, rearLeft = 50.0, rearRight = 55.0)
        assertEquals(5.0, ws.rearAxleDiff, 0.001)
    }

    @Test
    fun `WheelSpeeds isWheelSpeedWarning returns true when diff exceeds warning threshold`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 60.0, rearLeft = 50.0, rearRight = 50.0)
        // WHEEL_SPEED_DIFF_WARNING = 5.0
        assertTrue(ws.isWheelSpeedWarning())
    }

    @Test
    fun `WheelSpeeds isWheelSpeedWarning returns false when diff is below threshold`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 52.0, rearLeft = 50.0, rearRight = 50.0)
        assertFalse(ws.isWheelSpeedWarning())
    }

    @Test
    fun `WheelSpeeds isWheelSpeedCritical returns true when diff exceeds critical threshold`() {
        val ws = WheelSpeeds(frontLeft = 50.0, frontRight = 65.0, rearLeft = 50.0, rearRight = 50.0)
        // WHEEL_SPEED_DIFF_CRITICAL = 10.0
        assertTrue(ws.isWheelSpeedCritical())
    }

    // --- SafetySystemStatus tests ---

    @Test
    fun `SafetySystemStatus activeWarnings includes ABS when warning`() {
        val status = SafetySystemStatus(absStatus = SystemStatus.WARNING)
        assertTrue(status.activeWarnings.contains("ABS"))
    }

    @Test
    fun `SafetySystemStatus activeWarnings includes ESP when fault`() {
        val status = SafetySystemStatus(espStatus = SystemStatus.FAULT)
        assertTrue(status.activeWarnings.contains("ESP"))
    }

    @Test
    fun `SafetySystemStatus activeWarnings includes TPMS when warning`() {
        val status = SafetySystemStatus(tpmsStatus = SystemStatus.WARNING)
        assertTrue(status.activeWarnings.contains("TPMS"))
    }

    @Test
    fun `SafetySystemStatus activeWarnings includes Airbag when fault`() {
        val status = SafetySystemStatus(airbagStatus = SystemStatus.FAULT)
        assertTrue(status.activeWarnings.contains("Airbag"))
    }

    @Test
    fun `SafetySystemStatus activeWarnings includes Bremsfluessigkeit when low`() {
        val status = SafetySystemStatus(brakeFluidLevel = LevelStatus.LOW)
        assertTrue(status.activeWarnings.contains("Bremsfluessigkeit"))
    }

    @Test
    fun `SafetySystemStatus activeWarnings is empty when all OK`() {
        val status = SafetySystemStatus(
            absStatus = SystemStatus.OK,
            espStatus = SystemStatus.OK,
            tractionControlStatus = SystemStatus.OK,
            tpmsStatus = SystemStatus.OK,
            airbagStatus = SystemStatus.OK,
            brakeFluidLevel = LevelStatus.NORMAL
        )
        assertTrue(status.activeWarnings.isEmpty())
    }

    @Test
    fun `SafetySystemStatus hasCriticalFault returns true for critical ABS`() {
        val status = SafetySystemStatus(absStatus = SystemStatus.FAULT)
        assertTrue(status.hasCriticalFault)
    }

    @Test
    fun `SafetySystemStatus hasCriticalFault returns true for critical airbag`() {
        val status = SafetySystemStatus(airbagStatus = SystemStatus.FAULT)
        assertTrue(status.hasCriticalFault)
    }

    @Test
    fun `SafetySystemStatus hasCriticalFault returns true for critical brake fluid`() {
        val status = SafetySystemStatus(brakeFluidLevel = LevelStatus.CRITICAL)
        assertTrue(status.hasCriticalFault)
    }

    @Test
    fun `SafetySystemStatus hasCriticalFault returns false when no critical issues`() {
        val status = SafetySystemStatus(
            absStatus = SystemStatus.OK,
            espStatus = SystemStatus.OK,
            airbagStatus = SystemStatus.OK,
            brakeFluidLevel = LevelStatus.NORMAL
        )
        assertFalse(status.hasCriticalFault)
    }

    // --- ChassisSensors tests ---

    @Test
    fun `ChassisSensors isYawRateWarning returns true when exceeding threshold`() {
        val sensors = ChassisSensors(yawRate = 35.0) // YAW_RATE_WARNING = 30.0
        assertTrue(sensors.isYawRateWarning)
    }

    @Test
    fun `ChassisSensors isYawRateWarning returns false when below threshold`() {
        val sensors = ChassisSensors(yawRate = 20.0)
        assertFalse(sensors.isYawRateWarning)
    }

    @Test
    fun `ChassisSensors isLateralAccelWarning returns true when exceeding threshold`() {
        val sensors = ChassisSensors(lateralAcceleration = 0.9) // LATERAL_ACCEL_WARNING = 0.8
        assertTrue(sensors.isLateralAccelWarning)
    }

    @Test
    fun `ChassisSensors isLateralAccelWarning returns false when below threshold`() {
        val sensors = ChassisSensors(lateralAcceleration = 0.5)
        assertFalse(sensors.isLateralAccelWarning)
    }

    @Test
    fun `ChassisSensors isSteeringAngleValid returns true when within range`() {
        val sensors = ChassisSensors(steeringAngle = 180.0)
        assertTrue(sensors.isSteeringAngleValid)
    }

    @Test
    fun `ChassisSensors isSteeringAngleValid returns false when exceeding max`() {
        val sensors = ChassisSensors(steeringAngle = 800.0) // STEERING_ANGLE_MAX = 720
        assertFalse(sensors.isSteeringAngleValid)
    }

    @Test
    fun `ChassisSensors totalAcceleration calculates correctly`() {
        val sensors = ChassisSensors(lateralAcceleration = 3.0, longitudinalAcceleration = 4.0)
        assertEquals(5.0, sensors.totalAcceleration, 0.001) // 3-4-5 triangle
    }

    // --- BrakeWear tests ---

    @Test
    fun `BrakeWear minFront returns correct value`() {
        val wear = BrakeWear(frontLeft = 40, frontRight = 60, rearLeft = 80, rearRight = 90)
        assertEquals(40, wear.minFront)
    }

    @Test
    fun `BrakeWear minRear returns correct value`() {
        val wear = BrakeWear(frontLeft = 40, frontRight = 60, rearLeft = 80, rearRight = 20)
        assertEquals(20, wear.minRear)
    }

    @Test
    fun `BrakeWear overallMin returns correct value`() {
        val wear = BrakeWear(frontLeft = 40, frontRight = 60, rearLeft = 80, rearRight = 20)
        assertEquals(20, wear.overallMin)
    }

    @Test
    fun `BrakeWear isFrontWarning returns true when below warning threshold`() {
        val wear = BrakeWear(frontLeft = 25, frontRight = 30) // BRAKE_WEAR_WARNING = 30
        assertTrue(wear.isFrontWarning)
    }

    @Test
    fun `BrakeWear isFrontWarning returns false when above threshold`() {
        val wear = BrakeWear(frontLeft = 50, frontRight = 60)
        assertFalse(wear.isFrontWarning)
    }

    @Test
    fun `BrakeWear isFrontCritical returns true when below critical threshold`() {
        val wear = BrakeWear(frontLeft = 10, frontRight = 20) // BRAKE_WEAR_CRITICAL = 15
        assertTrue(wear.isFrontCritical)
    }

    @Test
    fun `BrakeWear needsService returns true when front warning`() {
        val wear = BrakeWear(frontLeft = 25, frontRight = 30)
        assertTrue(wear.needsService)
    }

    @Test
    fun `BrakeWear needsService returns true when rear warning`() {
        val wear = BrakeWear(rearLeft = 25, rearRight = 30)
        assertTrue(wear.needsService)
    }

    // --- TPMSData tests ---

    @Test
    fun `TPMSData avgPressure calculates correctly`() {
        val tpms = TPMSData(
            frontLeftPressure = 30.0,
            frontRightPressure = 32.0,
            rearLeftPressure = 31.0,
            rearRightPressure = 33.0,
        )
        assertEquals(31.5, tpms.avgPressure, 0.001)
    }

    @Test
    fun `TPMSData maxPressure returns correct value`() {
        val tpms = TPMSData(
            frontLeftPressure = 30.0,
            frontRightPressure = 35.0,
            rearLeftPressure = 32.0,
            rearRightPressure = 33.0,
        )
        assertEquals(35.0, tpms.maxPressure, 0.001)
    }

    @Test
    fun `TPMSData minPressure returns correct value`() {
        val tpms = TPMSData(
            frontLeftPressure = 28.0,
            frontRightPressure = 35.0,
            rearLeftPressure = 32.0,
            rearRightPressure = 33.0,
        )
        assertEquals(28.0, tpms.minPressure, 0.001)
    }

    @Test
    fun `TPMSData pressureDifference returns correct value`() {
        val tpms = TPMSData(
            frontLeftPressure = 28.0,
            frontRightPressure = 35.0,
            rearLeftPressure = 32.0,
            rearRightPressure = 33.0,
        )
        assertEquals(7.0, tpms.pressureDifference, 0.001)
    }

    @Test
    fun `TPMSData isLowPressure returns true when below threshold in PSI`() {
        val tpms = TPMSData(
            frontLeftPressure = 25.0,
            frontRightPressure = 32.0,
            rearLeftPressure = 32.0,
            rearRightPressure = 32.0,
            unit = "PSI",
        )
        assertTrue(tpms.isLowPressure)
    }

    @Test
    fun `TPMSData isLowPressure returns false when above threshold in PSI`() {
        val tpms = TPMSData(
            frontLeftPressure = 30.0,
            frontRightPressure = 32.0,
            rearLeftPressure = 32.0,
            rearRightPressure = 32.0,
            unit = "PSI",
        )
        assertFalse(tpms.isLowPressure)
    }

    @Test
    fun `TPMSData isCriticalPressure returns true when below critical threshold`() {
        val tpms = TPMSData(
            frontLeftPressure = 20.0,
            frontRightPressure = 32.0,
            rearLeftPressure = 32.0,
            rearRightPressure = 32.0,
            unit = "PSI",
        )
        assertTrue(tpms.isCriticalPressure)
    }

    @Test
    fun `TPMSData isHighPressure returns true when above high threshold`() {
        val tpms = TPMSData(
            frontLeftPressure = 45.0,
            frontRightPressure = 32.0,
            rearLeftPressure = 32.0,
            rearRightPressure = 32.0,
            unit = "PSI",
        )
        assertTrue(tpms.isHighPressure)
    }

    // --- SafetyDtc tests ---

    @Test
    fun `SafetyDtc isCritical returns true for CRITICAL severity`() {
        val dtc = SafetyDtc(code = "P0400", description = "Test", system = "EGR", severity = DtcSeverity.CRITICAL)
        assertTrue(dtc.isCritical)
    }

    @Test
    fun `SafetyDtc isCritical returns false for non-CRITICAL severity`() {
        val dtc = SafetyDtc(code = "P0400", description = "Test", system = "EGR", severity = DtcSeverity.WARNING)
        assertFalse(dtc.isCritical)
    }

    @Test
    fun `SafetyDtc displayCode returns uppercase code`() {
        val dtc = SafetyDtc(code = "p0400", description = "Test", system = "EGR", severity = DtcSeverity.INFO)
        assertEquals("P0400", dtc.displayCode)
    }

    // --- ESPState tests ---

    @Test
    fun `ESPState isFullyActive returns true when active and STANDARD mode`() {
        val state = ESPState(isActive = true, selectedMode = ESPSelectedMode.STANDARD)
        assertTrue(state.isFullyActive)
    }

    @Test
    fun `ESPState isFullyActive returns false when in sport mode`() {
        val state = ESPState(isActive = true, selectedMode = ESPSelectedMode.SPORT)
        assertFalse(state.isFullyActive)
    }

    @Test
    fun `ESPState isDisabled returns true when OFF mode`() {
        val state = ESPState(selectedMode = ESPSelectedMode.OFF)
        assertTrue(state.isDisabled)
    }

    @Test
    fun `ESPState isIntervening returns true when active with intervention level`() {
        val state = ESPState(isActive = true, interventionLevel = 5)
        assertTrue(state.isIntervening)
    }

    @Test
    fun `ESPState isIntervening returns false when not active`() {
        val state = ESPState(isActive = false, interventionLevel = 5)
        assertFalse(state.isIntervening)
    }

    // --- AirbagStatus tests ---

    @Test
    fun `AirbagStatus allAirbagsReady returns true when all ready`() {
        val status = AirbagStatus(
            driverFront = true, passengerFront = true, driverSide = true, passengerSide = true,
            curtainLeft = true, curtainRight = true, driverKnee = true,
            pretensionerDriver = true, pretensionerPassenger = true, systemReady = true
        )
        assertTrue(status.allAirbagsReady)
    }

    @Test
    fun `AirbagStatus allAirbagsReady returns false when any not ready`() {
        val status = AirbagStatus(driverFront = false)
        assertFalse(status.allAirbagsReady)
    }

    @Test
    fun `AirbagStatus hasFault returns true when not all ready`() {
        val status = AirbagStatus(driverFront = false)
        assertTrue(status.hasFault)
    }

    @Test
    fun `AirbagStatus faultCount returns correct count`() {
        val status = AirbagStatus(driverFront = false, passengerFront = false, driverSide = false)
        assertEquals(3, status.faultCount)
    }

    // --- SafetySummary tests ---

    @Test
    fun `SafetySummary hasWarnings returns true when any warning exists`() {
        val summary = SafetySummary(
            safetyStatus = SafetySystemStatus(espStatus = SystemStatus.WARNING)
        )
        assertTrue(summary.hasWarnings)
    }

    @Test
    fun `SafetySummary hasCritical returns true when any critical exists`() {
        val summary = SafetySummary(
            safetyStatus = SafetySystemStatus(absStatus = SystemStatus.FAULT)
        )
        assertTrue(summary.hasCritical)
    }

    @Test
    fun `SafetySummary dtcCount returns correct count`() {
        val summary = SafetySummary(
            safetyDTCs = listOf(
                SafetyDtc("C0035", "Test1", "ABS", DtcSeverity.INFO),
                SafetyDtc("C0040", "Test2", "ABS", DtcSeverity.WARNING)
            )
        )
        assertEquals(2, summary.dtcCount)
    }

    @Test
    fun `SafetySummary criticalDtcCount returns correct count`() {
        val summary = SafetySummary(
            safetyDTCs = listOf(
                SafetyDtc("C0035", "Test1", "ABS", DtcSeverity.INFO),
                SafetyDtc("B0016", "Test2", "Airbag", DtcSeverity.CRITICAL)
            )
        )
        assertEquals(1, summary.criticalDtcCount)
    }

    @Test
    fun `SafetySummary activeWarningCount returns correct count`() {
        val summary = SafetySummary(
            safetyStatus = SafetySystemStatus(espStatus = SystemStatus.WARNING, tpmsStatus = SystemStatus.WARNING)
        )
        assertEquals(2, summary.activeWarningCount)
    }

    // --- SafetyDTCMappings tests ---

    @Test
    fun `SafetyDTCMappings getDTCSeverity returns CRITICAL for circuit DTCs`() {
        val severity = SafetyDTCMappings.getDTCSeverity("C0035 CIRCUIT")
        assertEquals(DtcSeverity.CRITICAL, severity)
    }

    @Test
    fun `SafetyDTCMappings getDTCSeverity returns WARNING for range DTCs`() {
        val severity = SafetyDTCMappings.getDTCSeverity("C0036 RANGE")
        assertEquals(DtcSeverity.WARNING, severity)
    }

    @Test
    fun `SafetyDTCMappings getDTCSystem returns ABS for wheel speed DTCs`() {
        val system = SafetyDTCMappings.getDTCSystem("C0035")
        assertEquals("ABS", system)
    }

    @Test
    fun `SafetyDTCMappings getDTCSystem returns ESP for yaw rate DTCs`() {
        val system = SafetyDTCMappings.getDTCSystem("C0235")
        assertEquals("ESP/Stabilitaet", system)
    }

    @Test
    fun `SafetyDTCMappings getDTCSystem returns Airbag for B-codes`() {
        val system = SafetyDTCMappings.getDTCSystem("B0016")
        assertEquals("Airbag/SRS", system)
    }

    @Test
    fun `SafetyDTCMappings getDTCDescription returns description for known DTC`() {
        val description = SafetyDTCMappings.getDTCDescription("C0035")
        assertEquals("Linker Vorderrad-Geschwindigkeitssensor Stromkreis", description)
    }

    @Test
    fun `SafetyDTCMappings getDTCDescription returns null for unknown DTC`() {
        val description = SafetyDTCMappings.getDTCDescription("C9999")
        assertNull(description)
    }

    @Test
    fun `SafetyDTCMappings isKnownDTC returns true for known DTC`() {
        assertTrue(SafetyDTCMappings.isKnownDTC("C0035"))
        assertTrue(SafetyDTCMappings.isKnownDTC("B0016"))
    }

    @Test
    fun `SafetyDTCMappings isKnownDTC returns false for unknown DTC`() {
        assertFalse(SafetyDTCMappings.isKnownDTC("C9999"))
    }
}
