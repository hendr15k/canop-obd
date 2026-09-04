package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regressionstests für EmissionsReadinessAnalyzer.
 *
 * OBD-II (Mode 01 PID 01): Bit = 1 bedeutet Monitor NICHT bereit
 * (incomplete), Bit = 0 bedeutet bereit (complete).
 */
class EmissionsReadinessAnalyzerTest {

    private val analyzer = EmissionsReadinessAnalyzer()

    @Test
    fun `all bits zero means all supported monitors complete`() {
        val analysis = analyzer.analyze(
            EmissionsReadinessAnalyzer.ReadinessInput(readinessBits = 0)
        )
        assertTrue(analysis.allComplete)
        assertEquals(analysis.totalCount, analysis.completedCount)
    }

    @Test
    fun `set catalyst bit means catalyst incomplete`() {
        // CATALYST_BIT = 3 -> readinessBits = 1 shl 3
        val monitors = analyzer.parseReadinessBits(1 shl 3)
        val catalyst = monitors.first {
            it.monitor == com.canopobd.data.model.MonitorType.CATALYST
        }
        assertFalse(catalyst.isComplete)
        val misfire = monitors.first {
            it.monitor == com.canopobd.data.model.MonitorType.MISFIRE
        }
        assertTrue(misfire.isComplete)
    }

    @Test
    fun `unsupported GPF never blocks allComplete`() {
        val analysis = analyzer.analyze(
            EmissionsReadinessAnalyzer.ReadinessInput(readinessBits = 0)
        )
        // 9 supported monitors (GPF excluded), all complete
        assertEquals(9, analysis.totalCount)
        assertTrue(analysis.allComplete)
    }
}
