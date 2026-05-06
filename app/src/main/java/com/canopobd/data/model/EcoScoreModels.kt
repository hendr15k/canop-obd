package com.canopobd.data.model

data class EcoScoreData(
    val overallScore: Int = 0,
    val efficiencyScore: Int = 0,
    val smoothnessScore: Int = 0,
    val cruisingScore: Int = 0,
    val momentumScore: Int = 0,
    val grade: String = "F",
    val tripCount: Int = 0,
    val lastUpdated: Long = 0L
) {
    companion object {
        fun calculateGrade(score: Int): String = when {
            score >= 95 -> "A+"
            score >= 90 -> "A"
            score >= 85 -> "A-"
            score >= 80 -> "B+"
            score >= 75 -> "B"
            score >= 70 -> "B-"
            score >= 65 -> "C+"
            score >= 60 -> "C"
            score >= 55 -> "C-"
            score >= 50 -> "D+"
            score >= 45 -> "D"
            score >= 40 -> "D-"
            else -> "F"
        }
    }
}

data class DrivingStyleAnalysis(
    val style: DrivingStyle = DrivingStyle.NORMAL,
    val stylePercentage: Float = 0f,
    val accelerationScore: Int = 0,
    val brakingScore: Int = 0,
    val cruisingScore: Int = 0,
    val anticipationScore: Int = 0
)

enum class DrivingStyle {
    ECONOMICAL,
    NORMAL,
    AGGRESSIVE,
    SPORT
}

data class CO2Data(
    val tripCO2Kg: Double = 0.0,
    val tripCO2Lb: Double = 0.0,
    val perKmCO2Kg: Double = 0.0,
    val perMileCO2Lb: Double = 0.0,
    val cumulativeCO2Kg: Double = 0.0,
    val annualEstimateKg: Double = 0.0,
    val treesEquivalent: Double = 0.0,
    val lastUpdated: Long = 0L
) {
    // Durchschnittlicher Baum absorbiert ~22 kg CO2 pro Jahr
    companion object {
        const val CO2_PER_LITER_GASOLINE_KG = 2.31
        const val CO2_PER_GALLON_GASOLINE_LB = 19.6
        const val KG_TO_LB = 2.20462
        const val KM_TO_MILE = 0.621371
        const val AVG_TREE_CO2_ABSORPTION_KG_YEAR = 22.0
    }
}

data class FuelCostData(
    val fuelPricePerLiter: Double = 1.70,
    val fuelPricePerGallon: Double = 6.44,
    val currency: String = "EUR",
    val tripCost: Double = 0.0,
    val costPerKm: Double = 0.0,
    val costPerMile: Double = 0.0,
    val dailyCost: Double = 0.0,
    val weeklyCost: Double = 0.0,
    val monthlyCost: Double = 0.0,
    val annualCost: Double = 0.0,
    val lastUpdated: Long = 0L
) {
    companion object {
        const val DEFAULT_PRICE_EUR = 1.70
        const val DEFAULT_PRICE_USD = 3.50

        // Kosten pro km Naeherung (Euro)
        // Basierend auf ~7L/100km Durchschnitt und 1.70 EUR/L
        const val COST_PER_KM_APPROX = 0.119
    }
}

data class RangeEstimation(
    val estimatedRangeKm: Int = 0,
    val estimatedRangeMiles: Int = 0,
    val fuelLevelPercent: Int = 0,
    val fuelLevelLiters: Double = 0.0,
    val averageConsumption: Double = 0.0,
    val bestCaseRangeKm: Int = 0,
    val worstCaseRangeKm: Int = 0,
    val refuelNeededAtKm: Int = 0,
    val lastUpdated: Long = 0L
) {
    companion object {
        const val ASTRA_J_TANK_LITERS = 52.0
        const val RESERVE_LITERS = 5.0
    }
}

data class FuelEfficiencyMetrics(
    val instantLPer100km: Double = 0.0,
    val averageLPer100km: Double = 0.0,
    val bestLPer100km: Double = 0.0,
    val worstLPer100km: Double = 0.0,
    val instantMpg: Double = 0.0,
    val averageMpg: Double = 0.0,
    val fuelUsedLiters: Double = 0.0,
    val fuelUsedGallons: Double = 0.0,
    val distanceKm: Double = 0.0,
    val distanceMiles: Double = 0.0,
    val averageSpeedKmh: Double = 0.0,
    val averageSpeedMph: Double = 0.0,
    val cruisingTimePercent: Double = 0.0,
    val idleTimePercent: Double = 0.0,
    val lastUpdated: Long = 0L
) {
    companion object {
        const val KM_TO_MILE = 0.621371
        const val LITER_TO_GALLON = 0.264172
    }
}

data class EcoTip(
    val id: String,
    val title: String,
    val description: String,
    val potentialSavingsPercent: Double,
    val priority: TipPriority,
    val isImplemented: Boolean = false,
    val impact: TipImpact
)

enum class TipPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class TipImpact {
    FUEL_CONSUMPTION,
    TIRE_WEAR,
    BRAKE_WEAR,
    ENGINE_LIFE,
    SAFETY
}

data class EcoTripSummary(
    val tripId: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val distanceKm: Double,
    val fuelUsedLiters: Double,
    val averageSpeedKmh: Double,
    val ecoScore: Int,
    val grade: String,
    val co2EmittedKg: Double,
    val fuelCost: Double,
    val drivingStyle: DrivingStyle,
    val efficiencyTrend: TrendDirection
)

enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING
}

object AstraJEcoThresholds {
    // Verbrauchsziele (L/100km)
    const val TARGET_CITY = 8.0
    const val TARGET_HIGHWAY = 5.5
    const val TARGET_COMBINED = 6.5
    const val TARGET_SPORTS = 9.0

    // ECO-Score Schwellenwerte
    const val ECO_EXCELLENT = 85
    const val ECO_GOOD = 70
    const val ECO_AVERAGE = 55
    const val ECO_POOR = 40

    // Geschwindigkeitsschwellen fuer ECO-Fahren
    const val OPTIMAL_CRUISE_SPEED_KMH = 90.0
    const val MAX_ECO_SPEED_KMH = 120.0
    const val MIN_ECO_SPEED_KMH = 50.0

    // Drehzahl-Schwellenwerte
    const val OPTIMAL_RPM = 2000
    const val MAX_ECO_RPM = 3000
    const val ECO_UPSHIFT_RPM = 2500

    // Ladedruck-Schwellenwerte
    const val ECO_MAX_BOOST_BAR = 0.3

    // Temperaturbereiche
    const val OPTIMAL_ENGINE_TEMP_C = 90.0
    const val ENGINE_WARM_THRESHOLD_C = 70.0

    // Reifendruck (PSI)
    const val RECOMMENDED_TIRE_PSI = 32.0
    const val TIRE_PSI_EFFICIENCY_BONUS = 35.0
}

data class EcoComparison(
    val previousScore: Int,
    val currentScore: Int,
    val improvement: Int,
    val previousConsumption: Double,
    val currentConsumption: Double,
    val consumptionImprovementPercent: Double,
    val previousCO2: Double,
    val currentCO2: Double,
    val co2SavingsKg: Double,
    val moneySaved: Double,
    val comparisonDate: Long
)

data class EcoDashboardSummary(
    val ecoScore: EcoScoreData = EcoScoreData(),
    val drivingStyle: DrivingStyleAnalysis = DrivingStyleAnalysis(),
    val co2Data: CO2Data = CO2Data(),
    val fuelCost: FuelCostData = FuelCostData(),
    val rangeEstimation: RangeEstimation = RangeEstimation(),
    val efficiency: FuelEfficiencyMetrics = FuelEfficiencyMetrics(),
    val topTips: List<EcoTip> = emptyList(),
    val recentTrips: List<EcoTripSummary> = emptyList(),
    val comparison: EcoComparison? = null,
    val lastUpdated: Long = 0L
) {
    val hasWarnings: Boolean
        get() = ecoScore.overallScore < AstraJEcoThresholds.ECO_AVERAGE

    val isExcellent: Boolean
        get() = ecoScore.overallScore >= AstraJEcoThresholds.ECO_EXCELLENT
}
