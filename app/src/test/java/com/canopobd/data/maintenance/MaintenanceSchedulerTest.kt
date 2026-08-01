package com.canopobd.data.maintenance

import com.canopobd.data.model.MaintenanceType
import org.junit.Assert.assertEquals
import org.junit.Test

class MaintenanceSchedulerTest {

    @Test
    fun `completed service remains completed after reinitialization`() {
        val scheduler = MaintenanceScheduler
        scheduler.initialize(currentKm = 0, currentDate = 1_000L)

        scheduler.completeMaintenance("oil_change", km = 12_000, date = 2_000L)
        scheduler.initialize(currentKm = 12_100, currentDate = 3_000L)

        val oil = scheduler.getMaintenanceByType(MaintenanceType.OIL_CHANGE)
        assertEquals(27_000, oil?.nextDueKm)
    }
}
