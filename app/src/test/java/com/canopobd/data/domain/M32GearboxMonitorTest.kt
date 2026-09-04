package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class M32GearboxMonitorTest {

    @Test
    fun `normal rpm to speed ratio is not reported as anomalous`() {
        val monitor = M32GearboxMonitor()
        val speed = 60.0
        // M32 Gang 3: 1.357 (README), Achsuebersetzung 3.940
        val expectedRatio = 1.357 * 3.940 * 8.4
        val rpm = speed * expectedRatio
        val history = List(20) { rpm }

        val result = monitor.analyze(
            M32GearboxMonitor.GearboxInput(
                rpmHistory = history,
                speedHistory = List(20) { speed },
                gearPosition = 3
            )
        )

        assertEquals(0, result.rpmSpeedRatioAnomaly)
    }

    @Test
    fun `unequal history lengths do not crash`() {
        val monitor = M32GearboxMonitor()
        val result = monitor.analyze(
            M32GearboxMonitor.GearboxInput(
                rpmHistory = List(20) { 2500.0 },
                speedHistory = List(10) { 60.0 },
                gearPosition = 3
            )
        )
        // Kein IndexOutOfBounds, Ergebnis vorhanden (Anomalie-Prozent 0-100)
        assertTrue(result.rpmSpeedRatioAnomaly in 0..100)
    }

    @Test
    fun `unequal histories do not crash shift quality evaluation`() {
        val monitor = M32GearboxMonitor()
        // Regression: evaluateShiftQuality looped over rpmHistory indices
        // and indexed speedHistory without minOf -> IndexOutOfBounds when
        // rpm history is longer (different sensor poll rates). The RPM drop
        // at index 21 forces the branch that indexes speedHistory[21],
        // which does not exist (size 20).
        val rpm = MutableList(25) { 3000.0 }
        rpm[21] = 2000.0
        val result = monitor.analyze(
            M32GearboxMonitor.GearboxInput(
                rpmHistory = rpm,
                speedHistory = List(20) { 30.0 },
                gearPosition = 3
            )
        )
        assertTrue(result.shiftQualityScore in 0..100)
    }
}
