package com.canopobd.data.domain

import org.junit.Assert.assertEquals
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
}
