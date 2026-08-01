package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EGTMonitorTest {

    @Test
    fun `large EGT change is classified as volatile`() {
        val monitor = EGTMonitor()
        val history = List(5) { 700.0 } + List(5) { 730.0 }

        val result = monitor.analyze(
            EGTMonitor.EGTInput(
                egtBank1 = 730.0,
                recentEgtHistory = history
            )
        )

        assertEquals(EGTMonitor.EGTTrend.VOLATILE, result.trend)
    }
}
