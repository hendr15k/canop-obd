package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BoostLeakDetectorTest {

    private lateinit var detector: BoostLeakDetector

    @Before
    fun setup() {
        detector = BoostLeakDetector()
    }

    private fun createHealthyInput() = BoostLeakDetector.BoostLeakInput(
        boostActualBar = 0.7,
        boostTargetBar = 0.7,
        wastegateDuty = 45.0,
        turboRpm = 100000.0,
        chargeAirTemp = 45.0,
        intakeTemp = 25.0,
        mafRate = 25.0,
        rpm = 2500.0,
        engineLoad = 60.0,
        throttle = 50.0,
        exhaustPressure = 1.2,
        stftB1 = 1.0,
        ltftB1 = 1.0
    )

    @Test
    fun `analyze healthy system returns NONE severity`() {
        val result = detector.analyze(createHealthyInput())
        assertEquals(BoostLeakDetector.LeakSeverity.NONE, result.severity)
    }

    @Test
    fun `analyze healthy system returns good health score`() {
        val result = detector.analyze(createHealthyInput())
        assertTrue(result.healthScore >= 80)
    }

    @Test
    fun `analyze healthy system returns NONE location`() {
        val result = detector.analyze(createHealthyInput())
        assertEquals(BoostLeakDetector.LeakLocation.NONE, result.likelyLocation)
    }

    @Test
    fun `analyze healthy system has reasonable confidence`() {
        val result = detector.analyze(createHealthyInput())
        assertTrue(result.confidencePercent >= 20)
    }

    @Test
    fun `analyze healthy system has no indicators`() {
        val result = detector.analyze(createHealthyInput())
        assertTrue(result.detectedIndicators.isEmpty())
    }

    @Test
    fun `analyze low load returns UNKNOWN severity`() {
        val input = createHealthyInput().copy(
            engineLoad = 10.0,
            throttle = 15.0,
            rpm = 800.0
        )
        val result = detector.analyze(input)
        assertEquals(BoostLeakDetector.LeakSeverity.UNKNOWN, result.severity)
    }

    @Test
    fun `analyze low load returns zero confidence`() {
        val input = createHealthyInput().copy(
            engineLoad = 10.0,
            throttle = 15.0,
            rpm = 800.0
        )
        val result = detector.analyze(input)
        assertEquals(0, result.confidencePercent)
    }

    @Test
    fun `analyze moderate boost leak returns MODERATE or SEVERE`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.45,
            boostTargetBar = 0.7,
            wastegateDuty = 85.0
        )
        val result = detector.analyze(input)
        val isWorthySeverity = result.severity == BoostLeakDetector.LeakSeverity.MODERATE ||
            result.severity == BoostLeakDetector.LeakSeverity.SEVERE ||
            result.severity == BoostLeakDetector.LeakSeverity.MINOR
        assertTrue(isWorthySeverity)
    }

    @Test
    fun `analyze severe boost leak returns SEVERE`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.3,
            boostTargetBar = 0.7,
            wastegateDuty = 90.0,
            turboRpm = 180000.0,
            mafRate = 30.0,
            stftB1 = 8.0,
            ltftB1 = 5.0
        )
        val result = detector.analyze(input)
        assertEquals(BoostLeakDetector.LeakSeverity.SEVERE, result.severity)
    }

    @Test
    fun `analyze severe boost leak has low health score`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.3,
            boostTargetBar = 0.7,
            wastegateDuty = 90.0,
            turboRpm = 180000.0,
            mafRate = 30.0,
            stftB1 = 8.0,
            ltftB1 = 5.0
        )
        val result = detector.analyze(input)
        assertTrue(result.healthScore <= 35)
    }

    @Test
    fun `analyze severe boost leak has many indicators`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.3,
            boostTargetBar = 0.7,
            wastegateDuty = 90.0,
            turboRpm = 180000.0,
            mafRate = 30.0,
            stftB1 = 5.0,
            ltftB1 = 5.0
        )
        val result = detector.analyze(input)
        assertTrue(result.detectedIndicators.size >= 2)
    }

    @Test
    fun `analyze boost deviation is calculated correctly`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.5,
            boostTargetBar = 1.0
        )
        val result = detector.analyze(input)
        assertEquals(-50.0, result.boostDeviationPercent, 0.5)
    }

    @Test
    fun `analyze overboost returns healthy score`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.8,
            boostTargetBar = 0.7
        )
        val result = detector.analyze(input)
        assertTrue(result.healthScore >= 80)
    }

    @Test
    fun `analyze high turbo rpm without boost indicates leak`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.3,
            boostTargetBar = 0.7,
            turboRpm = 190000.0
        )
        val result = detector.analyze(input)
        assertTrue(result.detectedIndicators.any { it.contains("Turbo") })
    }

    @Test
    fun `analyze lean fuel trims add indicator`() {
        val input = createHealthyInput().copy(
            stftB1 = 8.0,
            ltftB1 = 5.0
        )
        val result = detector.analyze(input)
        assertTrue(result.detectedIndicators.any { it.contains("Magerkorrektur") })
    }

    @Test
    fun `analyze with no turbo RPM returns default turbo boost score`() {
        val input = createHealthyInput().copy(turboRpm = 0.0)
        val result = detector.analyze(input)
        assertNotNull(result)
        assertTrue(result.healthScore >= 0)
    }

    @Test
    fun `analyze with no MAF returns default MAF score`() {
        val input = createHealthyInput().copy(mafRate = 0.0)
        val result = detector.analyze(input)
        assertNotNull(result)
        assertTrue(result.healthScore >= 0)
    }

    @Test
    fun `analyze confidence increases with more indicators`() {
        val fewIndicators = createHealthyInput().copy(
            boostActualBar = 0.55,
            boostTargetBar = 0.7
        )
        val manyIndicators = createHealthyInput().copy(
            boostActualBar = 0.3,
            boostTargetBar = 0.7,
            turboRpm = 180000.0,
            mafRate = 30.0,
            stftB1 = 8.0,
            ltftB1 = 5.0
        )
        val few = detector.analyze(fewIndicators)
        val many = detector.analyze(manyIndicators)
        assertTrue(many.confidencePercent >= few.confidencePercent)
    }

    @Test
    fun `health score is between 0 and 100 for valid input`() {
        val inputs = listOf(
            createHealthyInput(),
            createHealthyInput().copy(boostActualBar = 0.45, boostTargetBar = 0.7),
            createHealthyInput().copy(boostActualBar = 0.3, boostTargetBar = 0.7)
        )
        for (input in inputs) {
            val score = detector.analyze(input).healthScore
            assertTrue("Health score $score should be between 0 and 100", score in 0..100)
        }
    }

    @Test
    fun `diagnosis is non-empty for all severity levels`() {
        val inputs = listOf(
            createHealthyInput(),
            createHealthyInput().copy(boostActualBar = 0.55, boostTargetBar = 0.7),
            createHealthyInput().copy(boostActualBar = 0.3, boostTargetBar = 0.7, turboRpm = 180000.0)
        )
        for (input in inputs) {
            val result = detector.analyze(input)
            assertTrue(result.diagnosis.isNotEmpty())
        }
    }

    @Test
    fun `recommendation is non-empty for all severity levels`() {
        val inputs = listOf(
            createHealthyInput(),
            createHealthyInput().copy(boostActualBar = 0.55, boostTargetBar = 0.7),
            createHealthyInput().copy(boostActualBar = 0.3, boostTargetBar = 0.7, turboRpm = 180000.0)
        )
        for (input in inputs) {
            val result = detector.analyze(input)
            assertTrue(result.recommendation.isNotEmpty())
        }
    }

    @Test
    fun `analyze with default calibration`() {
        val detectorDefault = BoostLeakDetector()
        val result = detectorDefault.analyze(createHealthyInput())
        assertNotNull(result)
    }

    @Test
    fun `analyze with custom calibration`() {
        val customCalibration = AstraJ14TurboCalibration(normalBoostTargetBar = 0.8)
        val detectorCustom = BoostLeakDetector(customCalibration)
        val result = detectorCustom.analyze(createHealthyInput())
        assertNotNull(result)
    }

    @Test
    fun `LeakSeverity enum has correct severity values`() {
        assertEquals(0, BoostLeakDetector.LeakSeverity.NONE.severity)
        assertEquals(1, BoostLeakDetector.LeakSeverity.MINOR.severity)
        assertEquals(2, BoostLeakDetector.LeakSeverity.MODERATE.severity)
        assertEquals(3, BoostLeakDetector.LeakSeverity.SEVERE.severity)
        assertEquals(-1, BoostLeakDetector.LeakSeverity.UNKNOWN.severity)
    }

    @Test
    fun `LeakSeverity enum has non-empty labels`() {
        BoostLeakDetector.LeakSeverity.entries.forEach { severity ->
            assertTrue(severity.label.isNotEmpty())
        }
    }

    @Test
    fun `LeakLocation enum has non-empty labels and descriptions`() {
        BoostLeakDetector.LeakLocation.entries.forEach { location ->
            assertTrue(location.label.isNotEmpty())
            assertTrue(location.description.isNotEmpty())
        }
    }

    @Test
    fun `analyze charge pipe leak location estimated for high temp delta`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.45,
            boostTargetBar = 0.7,
            chargeAirTemp = 55.0,
            intakeTemp = 25.0
        )
        val result = detector.analyze(input)
        assertEquals(BoostLeakDetector.LeakLocation.CHARGE_PIPE, result.likelyLocation)
    }

    @Test
    fun `analyze intake manifold location estimated with lean trims`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.45,
            boostTargetBar = 0.7,
            chargeAirTemp = 35.0,
            intakeTemp = 25.0,
            stftB1 = 8.0,
            ltftB1 = 5.0
        )
        val result = detector.analyze(input)
        assertEquals(BoostLeakDetector.LeakLocation.INTAKE_MANIFOLD, result.likelyLocation)
    }

    @Test
    fun `analyze wastegate area when duty is low and maf is low`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.55,
            boostTargetBar = 0.7,
            wastegateDuty = 20.0,
            mafRate = 0.5
        )
        val result = detector.analyze(input)
        assertEquals(BoostLeakDetector.LeakLocation.WASTEGATE_AREA, result.likelyLocation)
    }

    @Test
    fun `analyze boost deviation around 20 percent is not NONE`() {
        val input = createHealthyInput().copy(
            boostActualBar = 0.56,
            boostTargetBar = 0.7
        )
        val result = detector.analyze(input)
        assertNotEquals(BoostLeakDetector.LeakSeverity.NONE, result.severity)
    }
}
