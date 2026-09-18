package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the emissions-related analyzers that previously had no
 * coverage: EGR, EVAP and secondary air injection.
 */
class EmissionsSystemAnalyzerTest {

    private lateinit var egr: EGRHealthAnalyzer
    private lateinit var evap: EVAPSystemAnalyzer
    private lateinit var sai: SecondaryAirAnalyzer

    @Before
    fun setup() {
        egr = EGRHealthAnalyzer()
        evap = EVAPSystemAnalyzer()
        sai = SecondaryAirAnalyzer()
    }

    private fun healthyEgrInput() = EGRHealthAnalyzer.EGRInput(
        commandedEGR = 0.0,
        egrTemp = 0.0,
        engineLoad = 0.0,
        rpm = 0.0,
        coolantTemp = 0.0,
        intakeTemp = 0.0,
        mafRate = 0.0
    )

    @Test
    fun `egr idle without commanded flow reports closed and no fault issues`() {
        val result = egr.analyze(healthyEgrInput())

        assertEquals(com.canopobd.data.model.EGRStatus.CLOSED, result.health.status)
        assertTrue(result.healthScore in 0..100)
        assertFalse(result.detectedIssues.contains(EGRHealthAnalyzer.EGRIssue.STUCK_CLOSED))
        assertTrue(result.diagnosis.isNotBlank())
    }

    @Test
    fun `egr dtc lowers the health score`() {
        val healthy = egr.analyze(healthyEgrInput())
        val faulty = egr.analyze(healthyEgrInput().copy(activeDTCs = listOf("P0401")))

        assertTrue(faulty.healthScore < healthy.healthScore)
    }

    @Test
    fun `egr stuck closed detected under load without commanded flow`() {
        val result = egr.analyze(
            EGRHealthAnalyzer.EGRInput(
                commandedEGR = 10.0,
                egrTemp = 120.0,
                engineLoad = 60.0,
                rpm = 2000.0,
                coolantTemp = 90.0,
                intakeTemp = 30.0,
                mafRate = 10.0
            )
        )

        assertTrue(result.detectedIssues.contains(EGRHealthAnalyzer.EGRIssue.STUCK_CLOSED))
    }

    @Test
    fun `evap healthy idle has no leak`() {
        val result = evap.analyze(EVAPSystemAnalyzer.EVAPInput(commandedEvapPurge = 0.0))

        assertFalse(result.status.hasLeak)
        assertEquals(null, result.status.leakSize)
        assertTrue(result.healthScore in 0..100)
    }

    @Test
    fun `evap large leak dtc sets large leak`() {
        val result = evap.analyze(
            EVAPSystemAnalyzer.EVAPInput(
                commandedEvapPurge = 20.0,
                engineRpm = 1500.0,
                engineLoad = 40.0,
                activeDTCs = listOf("P0455")
            )
        )

        assertTrue(result.status.hasLeak)
        assertEquals(com.canopobd.data.model.LeakSize.LARGE, result.status.leakSize)
    }

    @Test
    fun `evap leak size and issue agree for medium negative pressure`() {
        // Regression: evaluateVaporPressure flagged < -300 as LARGE while
        // detectLeak only classified < -1000 as LARGE, so a -500 Pa reading
        // reported a large-leak issue but a small leak size.
        val result = evap.analyze(
            EVAPSystemAnalyzer.EVAPInput(
                commandedEvapPurge = 20.0,
                vaporPressure = -500.0,
                engineRpm = 1500.0,
                engineLoad = 40.0
            )
        )

        assertEquals(com.canopobd.data.model.LeakSize.SMALL, result.status.leakSize)
        assertTrue(result.detectedIssues.contains(EVAPSystemAnalyzer.EVAPIssue.SMALL_LEAK))
        assertFalse(result.detectedIssues.contains(EVAPSystemAnalyzer.EVAPIssue.LARGE_LEAK))
    }

    @Test
    fun `evap strong negative pressure is a large leak`() {
        val result = evap.analyze(
            EVAPSystemAnalyzer.EVAPInput(
                commandedEvapPurge = 20.0,
                vaporPressure = -1200.0,
                engineRpm = 1500.0,
                engineLoad = 40.0
            )
        )

        assertEquals(com.canopobd.data.model.LeakSize.LARGE, result.status.leakSize)
        assertTrue(result.detectedIssues.contains(EVAPSystemAnalyzer.EVAPIssue.LARGE_LEAK))
    }

    @Test
    fun `sai idle without activation is plausible`() {
        val result = sai.analyze(SecondaryAirAnalyzer.SAIInput())

        assertTrue(result.operationPlausibility)
        assertFalse(result.leanSwingDetected)
        assertTrue(result.healthScore in 0..100)
        assertFalse(result.status.isActive)
    }

    @Test
    fun `sai pump dtc lowers health score`() {
        val healthy = sai.analyze(SecondaryAirAnalyzer.SAIInput())
        val faulty = sai.analyze(SecondaryAirAnalyzer.SAIInput(activeDTCs = listOf("P0411")))

        assertTrue(faulty.healthScore < healthy.healthScore)
    }

    @Test
    fun `sai stuck active on hot engine flags incorrect timing`() {
        val result = sai.analyze(
            SecondaryAirAnalyzer.SAIInput(
                saActive = true,
                engineRpm = 1500.0,
                coolantTemp = 90.0,
                engineRuntimeSeconds = 60.0
            )
        )

        assertTrue(result.healthScore < 92)
    }
}
