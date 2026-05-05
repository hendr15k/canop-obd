package com.canopobd.data.model

data class FuelSample(
    val timestamp: Long,
    val fuelRateLph: Double,
    val speedKmh: Double,
    val rpm: Int,
    val load: Double
)

data class FuelTripSummary(
    val startOdo: Double,
    val endOdo: Double,
    val fuelUsed: Double,
    val durationMinutes: Double,
    val avgSpeed: Double,
    val maxSpeed: Double
) {
    val distanceKm: Double get() = (endOdo - startOdo).coerceAtLeast(0.0)
    val avgConsumptionL100: Double get() = if (distanceKm > 0) fuelUsed / distanceKm * 100.0 else 0.0
}

data class FuelEfficiencySnapshot(
    val instant: Double = 0.0,
    val average: Double = 0.0,
    val best: Double = Double.MAX_VALUE,
    val worst: Double = 0.0
)
