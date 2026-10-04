package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * TDD RED: Zündungs-/Verbrennungsanalyse für Opel Astra J 1.4 Turbo (A14NET).
 * Bewertet Zündzeitpunkt, Klopfregelung und Verbrennungsstabilität.
 */
class IgnitionAnalyzerTest {

    private lateinit var analyzer: IgnitionAnalyzer

    @Before
    fun setup() {
        analyzer = IgnitionAnalyzer()
    }

    @Test
    fun `plausible advance at cruise is healthy`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = 18.0,
                rpm = 2500.0,
                engineLoad = 55.0,
                coolantTemp = 90.0,
                knockRetard = 0.0
            )
        )

        assertEquals(IgnitionAnalyzer.CombustionHealth.HEALTHY, result.health)
        assertTrue(result.healthScore >= 80)
        assertTrue(result.detectedIssues.isEmpty())
    }

    @Test
    fun `missing timing signal reports no data`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = 0.0,
                rpm = 0.0,
                engineLoad = 0.0,
                coolantTemp = 0.0
            )
        )

        assertEquals(IgnitionAnalyzer.CombustionHealth.UNKNOWN, result.health)
        assertEquals(0, result.healthScore)
    }

    @Test
    fun `engine off with warm coolant still reports no data`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = 0.0,
                rpm = 0.0,
                engineLoad = 0.0,
                coolantTemp = 90.0
            )
        )

        assertEquals(IgnitionAnalyzer.CombustionHealth.UNKNOWN, result.health)
        assertEquals(0, result.healthScore)
    }

    @Test
    fun `zero knock retard on running engine does not flag knock`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = 18.0,
                rpm = 2500.0,
                engineLoad = 55.0,
                coolantTemp = 90.0,
                knockRetard = 0.0
            )
        )

        assertTrue(!result.detectedIssues.contains(IgnitionAnalyzer.IgnitionIssue.KNOCK_RETARD))
    }

    @Test
    fun `heavy knock retard is critical`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = 2.0,
                rpm = 4500.0,
                engineLoad = 95.0,
                coolantTemp = 95.0,
                knockRetard = 9.0
            )
        )

        assertEquals(IgnitionAnalyzer.CombustionHealth.CRITICAL, result.health)
        assertTrue(result.detectedIssues.contains(IgnitionAnalyzer.IgnitionIssue.KNOCK_RETARD))
        assertTrue(result.healthScore < 50)
    }

    @Test
    fun `overly retarded timing raises issue`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = -8.0,
                rpm = 3000.0,
                engineLoad = 70.0,
                coolantTemp = 90.0
            )
        )

        assertTrue(result.detectedIssues.contains(IgnitionAnalyzer.IgnitionIssue.RETARDED_TIMING))
        assertTrue(result.healthScore < 80)
    }

    @Test
    fun `knock dtc flags sensor issue`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = 15.0,
                rpm = 2500.0,
                engineLoad = 50.0,
                coolantTemp = 90.0,
                activeDTCs = listOf("P0325")
            )
        )

        assertTrue(result.detectedIssues.contains(IgnitionAnalyzer.IgnitionIssue.KNOCK_SENSOR_FAULT))
    }

    @Test
    fun `misfire dtc is critical`() {
        val result = analyzer.analyze(
            IgnitionAnalyzer.IgnitionInput(
                timingAdvance = 12.0,
                rpm = 2000.0,
                engineLoad = 40.0,
                coolantTemp = 88.0,
                activeDTCs = listOf("P0301")
            )
        )

        assertEquals(IgnitionAnalyzer.CombustionHealth.CRITICAL, result.health)
        assertTrue(result.detectedIssues.contains(IgnitionAnalyzer.IgnitionIssue.MISFIRE))
    }
}
