package com.canopobd.ui.dashboard

import com.canopobd.data.model.MaintenanceType
import com.canopobd.data.model.maintenanceTypeForServiceId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Regression tests for the ExtendedMaintenanceDialog -> MaintenanceType mapping.
 *
 * Bug: DashboardScreen used MaintenanceType.valueOf(type) with string service ids
 * from ExtendedMaintenanceDialog ("oil_change", "brake_pads_front", ...). These
 * neither match the enum's UPPER_SNAKE_CASE names nor exist for extended ids
 * (brake_pads_front/rear, cabin_filter, pcv_valve, maf_sensor, ...) ->
 * IllegalArgumentException crash on tap.
 */
class MaintenanceServiceIdMappingTest {

    private fun map(id: String): MaintenanceType? = maintenanceTypeForServiceId(id)

    @Test
    fun `dialog ids map to enum without crashing`() {
        assertEquals(MaintenanceType.OIL_CHANGE, map("oil_change"))
        assertEquals(MaintenanceType.TIMING_CHAIN, map("timing_chain"))
        assertEquals(MaintenanceType.TRANSMISSION_FLUID, map("transmission_oil"))
        assertEquals(MaintenanceType.AIR_FILTER, map("air_filter"))
        assertEquals(MaintenanceType.SPARK_PLUGS, map("spark_plugs"))
        assertEquals(MaintenanceType.COOLANT, map("coolant"))
    }

    @Test
    fun `extended brake ids fold into BRAKE_PADS instead of crashing`() {
        assertEquals(MaintenanceType.BRAKE_PADS, map("brake_pads_front"))
        assertEquals(MaintenanceType.BRAKE_PADS, map("brake_pads_rear"))
    }

    @Test
    fun `extended turbo ids map to turbo maintenance types`() {
        assertEquals(MaintenanceType.TURBO_INSPECTION, map("turbo_visual"))
        assertEquals(MaintenanceType.TURBO_BOOST_CHECK, map("turbo_pressure"))
    }

    @Test
    fun `ids without enum entry return null instead of throwing`() {
        assertNull(map("cabin_filter"))
        assertNull(map("pcv_valve"))
        assertNull(map("maf_sensor"))
        assertNull(map("totally_unknown_service"))
    }

    @Test
    fun `mapping never throws for any dialog service id`() {
        val allDialogIds = listOf(
            "oil_change", "timing_chain", "transmission_oil", "brake_pads_front",
            "brake_pads_rear", "air_filter", "spark_plugs", "coolant", "turbo_visual",
            "turbo_pressure", "cabin_filter", "pcv_valve", "maf_sensor"
        )
        allDialogIds.forEach { id ->
            // Must not throw; unknown ids simply return null.
            map(id)
        }
    }
}
