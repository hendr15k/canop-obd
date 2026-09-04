package com.canopobd.data.domain

import com.canopobd.data.model.FuelSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FuelConsumptionAnalyzerTest {

    @Test
    fun `MAF conversion accounts for stoichiometric air fuel ratio`() {
        val analyzer = FuelConsumptionAnalyzer()

        val consumption = analyzer.calculateFromMAF(maf = 15.0, speed = 100.0)

        assertEquals(4.8979, consumption, 0.001)
    }

    @Test
    fun `efficiency rating uses ordered consumption bands`() {
        val analyzer = FuelConsumptionAnalyzer()

        assertEquals(
            FuelConsumptionAnalyzer.EfficiencyRating.EXCELLENT,
            analyzer.getEfficiencyRating(6.5)
        )
        assertEquals(
            FuelConsumptionAnalyzer.EfficiencyRating.GOOD,
            analyzer.getEfficiencyRating(7.5)
        )
        assertEquals(
            FuelConsumptionAnalyzer.EfficiencyRating.AVERAGE,
            analyzer.getEfficiencyRating(9.0)
        )
    }

    @Test
    fun `out of order sample does not subtract distance or fuel`() {
        val analyzer = FuelConsumptionAnalyzer()
        analyzer.addSample(FuelSample(timestamp = 2000L, fuelRateLph = 5.0, speedKmh = 50.0, rpm = 2000, load = 30.0))
        val before = analyzer.getConsumptionData()
        analyzer.addSample(FuelSample(timestamp = 1000L, fuelRateLph = 5.0, speedKmh = 50.0, rpm = 2000, load = 30.0))
        val after = analyzer.getConsumptionData()

        assertTrue(after.tripDistance >= before.tripDistance)
        assertTrue(after.totalFuelUsed >= before.totalFuelUsed)
    }
}
