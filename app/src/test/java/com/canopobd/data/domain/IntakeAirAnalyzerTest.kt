package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TDD RED: Ansaugluft-/Ladeluft-Analyse für Opel Astra J 1.4 Turbo (A14NET).
 * Bewertet MAF-Plausibilität, Ladeluftkühlung und Ansaugtemperatur.
 */
class IntakeAirAnalyzerTest {

    private lateinit var analyzer: IntakeAirAnalyzer

    @Before
    fun setup() {
        analyzer = IntakeAirAnalyzer()
    }

    @Test
    fun `plausible cruise values are healthy`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 25.0,
                intakeTemp = 30.0,
                chargeAirTemp = 38.0,
                rpm = 2500.0,
                engineLoad = 55.0,
                boostBar = 0.5
            )
        )

        assertEquals(IntakeAirAnalyzer.IntakeHealth.HEALTHY, result.health)
        assertTrue(result.healthScore >= 80)
        assertTrue(result.detectedIssues.isEmpty())
    }

    @Test
    fun `missing maf signal reports no data`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 0.0,
                intakeTemp = 0.0,
                rpm = 0.0,
                engineLoad = 0.0
            )
        )

        assertEquals(IntakeAirAnalyzer.IntakeHealth.UNKNOWN, result.health)
        assertEquals(0, result.healthScore)
    }

    @Test
    fun `hot charge air under boost raises intercooler issue`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 60.0,
                intakeTemp = 30.0,
                chargeAirTemp = 85.0,
                rpm = 4500.0,
                engineLoad = 90.0,
                boostBar = 0.9
            )
        )

        assertTrue(result.detectedIssues.contains(IntakeAirAnalyzer.IntakeIssue.INTERCOOLER_WEAK))
        assertTrue(result.healthScore < 80)
    }

    @Test
    fun `moderate charge delta raises watch level`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 40.0,
                intakeTemp = 30.0,
                chargeAirTemp = 60.0,
                rpm = 3500.0,
                engineLoad = 75.0,
                boostBar = 0.6
            )
        )

        assertTrue(result.detectedIssues.contains(IntakeAirAnalyzer.IntakeIssue.INTERCOOLER_WATCH))
        assertTrue(result.healthScore < 100)
    }

    @Test
    fun `engine off reports no data regardless of maf noise`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 2.0,
                intakeTemp = 25.0,
                rpm = 0.0,
                engineLoad = 0.0
            )
        )

        assertEquals(IntakeAirAnalyzer.IntakeHealth.UNKNOWN, result.health)
        assertEquals(0, result.healthScore)
    }

    @Test
    fun `low maf at high load raises flow issue`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 8.0,
                intakeTemp = 25.0,
                rpm = 4000.0,
                engineLoad = 90.0
            )
        )

        assertTrue(result.detectedIssues.contains(IntakeAirAnalyzer.IntakeIssue.LOW_AIRFLOW))
        assertTrue(result.healthScore < 80)
    }

    @Test
    fun `maf dtc flags sensor issue`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 20.0,
                intakeTemp = 25.0,
                rpm = 2000.0,
                engineLoad = 40.0,
                activeDTCs = listOf("P0101")
            )
        )

        assertTrue(result.detectedIssues.contains(IntakeAirAnalyzer.IntakeIssue.MAF_SENSOR_FAULT))
    }

    @Test
    fun `frozen intake temp raises plausibility issue`() {
        val result = analyzer.analyze(
            IntakeAirAnalyzer.IntakeAirInput(
                mafRate = 25.0,
                intakeTemp = -40.0,
                rpm = 2500.0,
                engineLoad = 55.0
            )
        )

        assertTrue(result.detectedIssues.contains(IntakeAirAnalyzer.IntakeIssue.TEMP_PLAUSIBILITY))
    }
}
