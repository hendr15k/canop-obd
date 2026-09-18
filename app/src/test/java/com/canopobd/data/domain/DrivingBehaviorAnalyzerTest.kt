package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the previously uncovered driving-behavior analyzers:
 * drive mode detection, drive style, driving efficiency, fuel system and
 * lambda balance.
 */
class DrivingBehaviorAnalyzerTest {

    private lateinit var driveStyle: DriveStyleAnalyzer
    private lateinit var efficiency: DrivingEfficiencyScorer
    private lateinit var fuelSystem: FuelSystemAnalyzer
    private lateinit var lambda: LambdaBalanceAnalyzer

    @Before
    fun setup() {
        driveStyle = DriveStyleAnalyzer()
        efficiency = DrivingEfficiencyScorer()
        fuelSystem = FuelSystemAnalyzer()
        lambda = LambdaBalanceAnalyzer()
    }

    @Test
    fun `eco cruise is detected as eco`() {
        assertEquals(
            DriveMode.ECO,
            DriveModeDetector.detectMode(
                throttle = 20.0,
                rpm = 2200.0,
                speed = 80.0,
                engineLoad = 25.0,
                acceleratorPedalD = 30.0,
                throttleActuator = 15.0
            )
        )
    }

    @Test
    fun `high rpm is detected as sport`() {
        assertEquals(
            DriveMode.SPORT,
            DriveModeDetector.detectMode(
                throttle = 70.0,
                rpm = 4500.0,
                speed = 120.0,
                engineLoad = 80.0,
                acceleratorPedalD = 60.0,
                throttleActuator = 60.0
            )
        )
    }

    @Test
    fun `moderate driving is normal`() {
        assertEquals(
            DriveMode.NORMAL,
            DriveModeDetector.detectMode(
                throttle = 40.0,
                rpm = 2500.0,
                speed = 60.0,
                engineLoad = 45.0,
                acceleratorPedalD = 40.0,
                throttleActuator = 35.0
            )
        )
    }

    @Test
    fun `coasting detection matches closed throttle rolling`() {
        assertTrue(DriveModeDetector.isCoasting(speed = 60.0, throttle = 2.0, rpm = 2000.0))
        assertFalse(DriveModeDetector.isCoasting(speed = 60.0, throttle = 30.0, rpm = 2000.0))
        assertFalse(DriveModeDetector.isCoasting(speed = 10.0, throttle = 0.0, rpm = 2000.0))
    }

    @Test
    fun `drive style empty history returns balanced placeholder`() {
        val result = driveStyle.analyze(
            DriveStyleAnalyzer.DriveStyleInput(
                rpmHistory = emptyList(),
                throttleHistory = emptyList(),
                speedHistory = emptyList()
            )
        )

        assertEquals(DriveStyleAnalyzer.DriveStyle.BALANCED, result.driveStyle)
        assertEquals(50, result.ecoScore)
        assertTrue(result.feedback.isNotBlank())
    }

    @Test
    fun `drive style steady eco cruise scores higher than aggressive`() {
        val eco = driveStyle.analyze(
            DriveStyleAnalyzer.DriveStyleInput(
                rpmHistory = List(50) { 2200.0 },
                throttleHistory = List(50) { 20.0 },
                speedHistory = List(50) { 80.0 }
            )
        )
        val aggressive = driveStyle.analyze(
            DriveStyleAnalyzer.DriveStyleInput(
                rpmHistory = List(50) { 5500.0 },
                throttleHistory = List(50) { 90.0 },
                speedHistory = List(50) { 140.0 }
            )
        )

        assertTrue(eco.ecoScore > aggressive.ecoScore)
        assertTrue(aggressive.sportScore > eco.sportScore)
    }

    @Test
    fun `efficiency calm session outscores harsh session`() {
        val calm = efficiency.calculateScore(
            DrivingEfficiencyScorer.DriveSessionData(
                avgRpm = 2200.0,
                maxRpm = 3000.0,
                avgSpeed = 80.0,
                maxSpeed = 100.0,
                avgThrottle = 20.0,
                maxThrottle = 40.0,
                avgLoad = 30.0,
                totalSamples = 100
            )
        )
        val harsh = efficiency.calculateScore(
            DrivingEfficiencyScorer.DriveSessionData(
                avgRpm = 5000.0,
                maxRpm = 6600.0,
                avgSpeed = 120.0,
                maxSpeed = 190.0,
                avgThrottle = 85.0,
                maxThrottle = 100.0,
                avgLoad = 90.0,
                harshAccelerations = 8,
                harshBrakes = 8,
                totalSamples = 100
            )
        )

        assertTrue(calm.overall in 0..100)
        assertTrue(harsh.overall in 0..100)
        assertTrue(calm.overall > harsh.overall)
    }

    @Test
    fun `fuel system normal rail pressure stays healthy`() {
        val result = fuelSystem.analyze(
            FuelSystemAnalyzer.FuelSystemInput(
                activeDTCs = emptyList(),
                fuelRailPressureBar = 55.0,
                engineLoad = 20.0,
                rpm = 850.0
            )
        )

        assertEquals(FuelSystemAnalyzer.FuelSystemHealth.HEALTHY, result.health)
        assertTrue(result.trimHealthScore in 0..100)
        assertTrue(result.injectorHealthScore in 0..100)
    }

    @Test
    fun `fuel system low pressure dtc flags hpfp wear`() {
        val result = fuelSystem.analyze(
            FuelSystemAnalyzer.FuelSystemInput(
                activeDTCs = listOf("P0087"),
                fuelRailPressureBar = 25.0,
                engineLoad = 60.0,
                rpm = 2500.0
            )
        )

        assertTrue(result.detectedIssues.contains(FuelSystemAnalyzer.FuelSystemIssue.HPFP_WEAR))
        assertTrue(result.health != FuelSystemAnalyzer.FuelSystemHealth.HEALTHY)
    }

    @Test
    fun `lambda sequence below minimum returns perfect default`() {
        val result = lambda.analyzeLambdaSequence(listOf(1.0, 1.01))

        assertEquals(LambdaBalanceAnalyzer.LambdaStatus.PERFECT, result.status)
        assertEquals(100, result.healthScore)
    }

    @Test
    fun `lambda stoichiometric samples stay perfect`() {
        val result = lambda.analyzeLambdaSequence(List(30) { 1.0 })

        assertEquals(LambdaBalanceAnalyzer.LambdaStatus.PERFECT, result.status)
        assertEquals(100, result.healthScore)
    }

    @Test
    fun `lambda lean average is detected`() {
        val result = lambda.analyzeLambdaSequence(List(30) { 1.06 })

        assertEquals(LambdaBalanceAnalyzer.LambdaStatus.SLIGHTLY_LEAN, result.status)
    }

    @Test
    fun `lambda invalid samples are ignored`() {
        lambda.addLambdaSample(-1.0)
        lambda.addLambdaSample(50.0)

        assertEquals(LambdaBalanceAnalyzer.LambdaStatus.PERFECT, lambda.analyzeCurrentSequence().status)
    }
}
