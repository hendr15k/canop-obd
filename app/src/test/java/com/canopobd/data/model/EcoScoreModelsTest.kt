package com.canopobd.data.model

import org.junit.Assert.*
import org.junit.Test

class EcoScoreModelsTest {

    // --- EcoScoreData tests ---

    @Test
    fun `EcoScoreData calculateGrade returns A+ for score 95`() {
        assertEquals("A+", EcoScoreData.calculateGrade(95))
    }

    @Test
    fun `EcoScoreData calculateGrade returns A for score 90`() {
        assertEquals("A", EcoScoreData.calculateGrade(90))
    }

    @Test
    fun `EcoScoreData calculateGrade returns A- for score 85`() {
        assertEquals("A-", EcoScoreData.calculateGrade(85))
    }

    @Test
    fun `EcoScoreData calculateGrade returns B+ for score 82`() {
        assertEquals("B+", EcoScoreData.calculateGrade(82))
    }

    @Test
    fun `EcoScoreData calculateGrade returns B for score 75`() {
        assertEquals("B", EcoScoreData.calculateGrade(75))
    }

    @Test
    fun `EcoScoreData calculateGrade returns B- for score 72`() {
        assertEquals("B-", EcoScoreData.calculateGrade(72))
    }

    @Test
    fun `EcoScoreData calculateGrade returns C+ for score 67`() {
        assertEquals("C+", EcoScoreData.calculateGrade(67))
    }

    @Test
    fun `EcoScoreData calculateGrade returns C for score 62`() {
        assertEquals("C", EcoScoreData.calculateGrade(62))
    }

    @Test
    fun `EcoScoreData calculateGrade returns C- for score 57`() {
        assertEquals("C-", EcoScoreData.calculateGrade(57))
    }

    @Test
    fun `EcoScoreData calculateGrade returns D+ for score 52`() {
        assertEquals("D+", EcoScoreData.calculateGrade(52))
    }

    @Test
    fun `EcoScoreData calculateGrade returns D for score 47`() {
        assertEquals("D", EcoScoreData.calculateGrade(47))
    }

    @Test
    fun `EcoScoreData calculateGrade returns D- for score 42`() {
        assertEquals("D-", EcoScoreData.calculateGrade(42))
    }

    @Test
    fun `EcoScoreData calculateGrade returns F for score 35`() {
        assertEquals("F", EcoScoreData.calculateGrade(35))
    }

    @Test
    fun `EcoScoreData calculateGrade handles edge cases`() {
        assertEquals("A+", EcoScoreData.calculateGrade(100))
        assertEquals("A+", EcoScoreData.calculateGrade(99))
        assertEquals("F", EcoScoreData.calculateGrade(0))
    }

    // --- DrivingStyleAnalysis tests ---

    @Test
    fun `DrivingStyleAnalysis has correct default style`() {
        val analysis = DrivingStyleAnalysis()
        assertEquals(DrivingStyle.NORMAL, analysis.style)
    }

    // --- CO2Data tests ---

    @Test
    fun `CO2Data constants are correctly defined`() {
        assertEquals(2.31, CO2Data.CO2_PER_LITER_GASOLINE_KG, 0.001)
        assertEquals(19.6, CO2Data.CO2_PER_GALLON_GASOLINE_LB, 0.001)
        assertEquals(2.20462, CO2Data.KG_TO_LB, 0.001)
        assertEquals(0.621371, CO2Data.KM_TO_MILE, 0.001)
        assertEquals(22.0, CO2Data.AVG_TREE_CO2_ABSORPTION_KG_YEAR, 0.001)
    }

    // --- FuelCostData tests ---

    @Test
    fun `FuelCostData constants are correctly defined`() {
        assertEquals(1.70, FuelCostData.DEFAULT_PRICE_EUR, 0.001)
        assertEquals(3.50, FuelCostData.DEFAULT_PRICE_USD, 0.001)
        assertEquals(0.119, FuelCostData.COST_PER_KM_APPROX, 0.001)
    }

    // --- RangeEstimation tests ---

    @Test
    fun `RangeEstimation constants are correctly defined`() {
        assertEquals(52.0, RangeEstimation.ASTRA_J_TANK_LITERS, 0.001)
        assertEquals(5.0, RangeEstimation.RESERVE_LITERS, 0.001)
    }

    // --- FuelEfficiencyMetrics tests ---

    @Test
    fun `FuelEfficiencyMetrics constants are correctly defined`() {
        assertEquals(0.621371, FuelEfficiencyMetrics.KM_TO_MILE, 0.001)
        assertEquals(0.264172, FuelEfficiencyMetrics.LITER_TO_GALLON, 0.001)
    }

    // --- EcoTip tests ---

    @Test
    fun `EcoTip can be created with all fields`() {
        val tip = EcoTip(
            id = "1",
            title = "Test Tip",
            description = "Test Description",
            potentialSavingsPercent = 10.0,
            priority = TipPriority.HIGH,
            impact = TipImpact.FUEL_CONSUMPTION
        )
        assertEquals("1", tip.id)
        assertEquals("Test Tip", tip.title)
        assertEquals(10.0, tip.potentialSavingsPercent, 0.001)
        assertEquals(TipPriority.HIGH, tip.priority)
        assertEquals(TipImpact.FUEL_CONSUMPTION, tip.impact)
        assertFalse(tip.isImplemented)
    }

    // --- TipPriority tests ---

    @Test
    fun `TipPriority enum values exist`() {
        assertNotNull(TipPriority.HIGH)
        assertNotNull(TipPriority.MEDIUM)
        assertNotNull(TipPriority.LOW)
    }

    // --- TipImpact tests ---

    @Test
    fun `TipImpact enum values exist`() {
        assertNotNull(TipImpact.FUEL_CONSUMPTION)
        assertNotNull(TipImpact.TIRE_WEAR)
        assertNotNull(TipImpact.BRAKE_WEAR)
        assertNotNull(TipImpact.ENGINE_LIFE)
        assertNotNull(TipImpact.SAFETY)
    }

    // --- EcoTripSummary tests ---

    @Test
    fun `EcoTripSummary can be created with all fields`() {
        val summary = EcoTripSummary(
            tripId = "trip1",
            startTime = 1000L,
            endTime = 2000L,
            durationMinutes = 60,
            distanceKm = 50.0,
            fuelUsedLiters = 5.0,
            averageSpeedKmh = 50.0,
            ecoScore = 85,
            grade = "A",
            co2EmittedKg = 11.55,
            fuelCost = 8.5,
            drivingStyle = DrivingStyle.ECONOMICAL,
            efficiencyTrend = TrendDirection.IMPROVING
        )
        assertEquals("trip1", summary.tripId)
        assertEquals(50.0, summary.distanceKm, 0.001)
        assertEquals(85, summary.ecoScore)
        assertEquals(DrivingStyle.ECONOMICAL, summary.drivingStyle)
        assertEquals(TrendDirection.IMPROVING, summary.efficiencyTrend)
    }

    // --- TrendDirection tests ---

    @Test
    fun `TrendDirection enum values exist`() {
        assertNotNull(TrendDirection.IMPROVING)
        assertNotNull(TrendDirection.STABLE)
        assertNotNull(TrendDirection.DECLINING)
    }

    // --- AstraJEcoThresholds tests ---

    @Test
    fun `AstraJEcoThresholds consumption targets are defined`() {
        assertEquals(8.0, AstraJEcoThresholds.TARGET_CITY, 0.001)
        assertEquals(5.5, AstraJEcoThresholds.TARGET_HIGHWAY, 0.001)
        assertEquals(6.5, AstraJEcoThresholds.TARGET_COMBINED, 0.001)
        assertEquals(9.0, AstraJEcoThresholds.TARGET_SPORTS, 0.001)
    }

    @Test
    fun `AstraJEcoThresholds ECO score thresholds are defined`() {
        assertEquals(85, AstraJEcoThresholds.ECO_EXCELLENT)
        assertEquals(70, AstraJEcoThresholds.ECO_GOOD)
        assertEquals(55, AstraJEcoThresholds.ECO_AVERAGE)
        assertEquals(40, AstraJEcoThresholds.ECO_POOR)
    }

    @Test
    fun `AstraJEcoThresholds speed thresholds are defined`() {
        assertEquals(90.0, AstraJEcoThresholds.OPTIMAL_CRUISE_SPEED_KMH, 0.001)
        assertEquals(120.0, AstraJEcoThresholds.MAX_ECO_SPEED_KMH, 0.001)
        assertEquals(50.0, AstraJEcoThresholds.MIN_ECO_SPEED_KMH, 0.001)
    }

    @Test
    fun `AstraJEcoThresholds RPM thresholds are defined`() {
        assertEquals(2000, AstraJEcoThresholds.OPTIMAL_RPM)
        assertEquals(3000, AstraJEcoThresholds.MAX_ECO_RPM)
        assertEquals(2500, AstraJEcoThresholds.ECO_UPSHIFT_RPM)
    }

    @Test
    fun `AstraJEcoThresholds boost threshold is defined`() {
        assertEquals(0.3, AstraJEcoThresholds.ECO_MAX_BOOST_BAR, 0.001)
    }

    @Test
    fun `AstraJEcoThresholds temperature thresholds are defined`() {
        assertEquals(90.0, AstraJEcoThresholds.OPTIMAL_ENGINE_TEMP_C, 0.001)
        assertEquals(70.0, AstraJEcoThresholds.ENGINE_WARM_THRESHOLD_C, 0.001)
    }

    @Test
    fun `AstraJEcoThresholds tire PSI thresholds are defined`() {
        assertEquals(32.0, AstraJEcoThresholds.RECOMMENDED_TIRE_PSI, 0.001)
        assertEquals(35.0, AstraJEcoThresholds.TIRE_PSI_EFFICIENCY_BONUS, 0.001)
    }

    // --- EcoComparison tests ---

    @Test
    fun `EcoComparison can be created with all fields`() {
        val comparison = EcoComparison(
            previousScore = 75,
            currentScore = 82,
            improvement = 7,
            previousConsumption = 7.5,
            currentConsumption = 6.8,
            consumptionImprovementPercent = 9.3,
            previousCO2 = 17.325,
            currentCO2 = 15.708,
            co2SavingsKg = 1.617,
            moneySaved = 2.73,
            comparisonDate = System.currentTimeMillis()
        )
        assertEquals(75, comparison.previousScore)
        assertEquals(82, comparison.currentScore)
        assertEquals(7, comparison.improvement)
        assertTrue(comparison.consumptionImprovementPercent > 0)
    }

    // --- EcoDashboardSummary tests ---

    @Test
    fun `EcoDashboardSummary hasWarnings returns true when score is low`() {
        val summary = EcoDashboardSummary(
            ecoScore = EcoScoreData(overallScore = 50)
        )
        // ECO_AVERAGE = 55
        assertTrue(summary.hasWarnings)
    }

    @Test
    fun `EcoDashboardSummary hasWarnings returns false when score is good`() {
        val summary = EcoDashboardSummary(
            ecoScore = EcoScoreData(overallScore = 70)
        )
        assertFalse(summary.hasWarnings)
    }

    @Test
    fun `EcoDashboardSummary isExcellent returns true for high score`() {
        val summary = EcoDashboardSummary(
            ecoScore = EcoScoreData(overallScore = 90)
        )
        // ECO_EXCELLENT = 85
        assertTrue(summary.isExcellent)
    }

    @Test
    fun `EcoDashboardSummary isExcellent returns false for average score`() {
        val summary = EcoDashboardSummary(
            ecoScore = EcoScoreData(overallScore = 80)
        )
        assertFalse(summary.isExcellent)
    }

    @Test
    fun `EcoDashboardSummary can be created with default values`() {
        val summary = EcoDashboardSummary()
        assertNotNull(summary.ecoScore)
        assertNotNull(summary.drivingStyle)
        assertNotNull(summary.co2Data)
        assertNotNull(summary.fuelCost)
        assertNotNull(summary.rangeEstimation)
        assertNotNull(summary.efficiency)
        assertTrue(summary.topTips.isEmpty())
        assertTrue(summary.recentTrips.isEmpty())
        assertNull(summary.comparison)
    }
}
