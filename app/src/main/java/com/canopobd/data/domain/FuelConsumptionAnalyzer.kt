package com.canopobd.data.domain

import com.canopobd.data.model.FuelSample
import kotlin.math.abs

class FuelConsumptionAnalyzer {

    enum class EfficiencyRating(val label: String, val grade: Char, val minScore: Int, val maxScore: Int) {
        EXCELLENT("Ausgezeichnet", 'A', 0, 65),
        GOOD("Gut", 'B', 65, 80),
        AVERAGE("Durchschnittlich", 'C', 80, 100),
        POOR("Schlecht", 'D', 100, 150),
        CRITICAL("Kritisch", 'F', 150, Int.MAX_VALUE)
    }

    data class FuelConsumptionData(
        val instantLph: Double = 0.0,
        val instantL100km: Double = 0.0,
        val avgL100km: Double = 0.0,
        val tripDistance: Double = 0.0,
        val totalFuelUsed: Double = 0.0,
        val efficiency: EfficiencyRating = EfficiencyRating.AVERAGE
    )

    data class ConsumptionTrend(
        val direction: TrendDirection,
        val magnitude: Double,
        val samples: Int
    )

    enum class TrendDirection { IMPROVING, STABLE, WORSENING }

    companion object {
        private const val FUEL_DENSITY = 0.75
        private const val STOICHIOMETRIC_AFR = 14.7
        private const val LPH_TO_L100_CONVERSION = 100.0
        private const val MIN_SPEED_FOR_CALC = 5.0
        private const val SAMPLE_WINDOW_SIZE = 100
        // Max. plausible sample gap: groessere Luecken (App im Hintergrund,
        // Uhrensprung) wuerden Distanz/Kraftstoff unbegrenzt aufblaehen.
        private const val MAX_SAMPLE_GAP_MS = 60_000L

        private const val CITY_CONSUMPTION_MIN = 8.0
        private const val CITY_CONSUMPTION_MAX = 10.0
        private const val HIGHWAY_CONSUMPTION_MAX = 7.0
        private const val SPORT_CONSUMPTION_MIN = 12.0
    }

    private val fuelSamples = mutableListOf<FuelSample>()
    private val consumptionHistory = mutableListOf<Double>()
    private var tripDistance = 0.0
    private var totalFuelUsed = 0.0
    private var lastSpeed = 0.0
    private var lastTimestamp = 0L

    fun calculateInstantConsumption(fuelRate: Double?, speed: Double?): Double {
        if (fuelRate == null || fuelRate <= 0) { return 0.0 }
        if (speed == null || speed < MIN_SPEED_FOR_CALC) { return 0.0 }

        val lph = fuelRate
        val l100km = (lph * LPH_TO_L100_CONVERSION) / speed

        return if (l100km.isFinite() && l100km > 0 && l100km < 100) { l100km } else { 0.0 }
    }

    fun calculateFromMAF(maf: Double, speed: Double): Double {
        if (maf <= 0 || speed < MIN_SPEED_FOR_CALC) { return 0.0 }

        val fuelRateGh = maf * 3.6
        // MAF is the air mass flow. Convert it to fuel flow using the
        // stoichiometric air-fuel ratio before applying fuel density.
        val fuelRateLph = fuelRateGh / STOICHIOMETRIC_AFR / FUEL_DENSITY
        val l100km = (fuelRateLph * LPH_TO_L100_CONVERSION) / speed

        return if (l100km.isFinite() && l100km > 0 && l100km < 100) { l100km } else { 0.0 }
    }

    fun addSample(sample: FuelSample) {
        fuelSamples.add(sample)
        if (fuelSamples.size > SAMPLE_WINDOW_SIZE) {
            fuelSamples.removeAt(0)
        }

        // Out-of-order/verzoegerte Samples duerfen Distanz und Kraftstoff nie
        // verringern oder aufblaehen: dt auf [0, MAX_SAMPLE_GAP_MS] klemmen.
        val timeDeltaMs = if (lastTimestamp > 0) {
            (sample.timestamp - lastTimestamp).coerceIn(0L, MAX_SAMPLE_GAP_MS)
        } else { 0L }
        val timeDeltaHours = timeDeltaMs / 3600000.0

        val distanceDelta = if (sample.speedKmh > 0 && timeDeltaMs > 0) {
            sample.speedKmh * timeDeltaHours
        } else { 0.0 }

        tripDistance += distanceDelta

        val fuelDelta = if (sample.fuelRateLph > 0 && timeDeltaMs > 0) {
            sample.fuelRateLph * timeDeltaHours
        } else { 0.0 }

        totalFuelUsed += fuelDelta

        val instantConsumption = calculateInstantConsumption(sample.fuelRateLph, sample.speedKmh)
        if (instantConsumption > 0) {
            consumptionHistory.add(instantConsumption)
            if (consumptionHistory.size > SAMPLE_WINDOW_SIZE) {
                consumptionHistory.removeAt(0)
            }
        }

        lastSpeed = sample.speedKmh
        // lastTimestamp nur monoton vorwaerts bewegen: Ein Out-of-order-Sample
        // darf das naechste Delta nicht aufblaehen.
        if (sample.timestamp > lastTimestamp) {
            lastTimestamp = sample.timestamp
        }
    }

    fun updateAverage(newSample: FuelSample) {
        addSample(newSample)
    }

    fun getEfficiencyTrend(): ConsumptionTrend {
        if (consumptionHistory.size < 10) {
            return ConsumptionTrend(TrendDirection.STABLE, 0.0, consumptionHistory.size)
        }

        val halfSize = consumptionHistory.size / 2
        val firstHalf = consumptionHistory.take(halfSize)
        val secondHalf = consumptionHistory.takeLast(halfSize)

        val firstAvg = firstHalf.average()
        val secondAvg = secondHalf.average()

        val changePercent = if (firstAvg > 0) {
            ((secondAvg - firstAvg) / firstAvg) * 100.0
        } else { 0.0 }

        val direction = when {
            changePercent < -5.0 -> TrendDirection.IMPROVING
            changePercent > 5.0 -> TrendDirection.WORSENING
            else -> TrendDirection.STABLE
        }

        return ConsumptionTrend(direction, abs(changePercent), consumptionHistory.size)
    }

    fun getConsumptionData(): FuelConsumptionData {
        val instantConsumption = if (consumptionHistory.isNotEmpty()) {
            consumptionHistory.last()
        } else { 0.0 }

        val avgConsumption = if (consumptionHistory.isNotEmpty()) {
            consumptionHistory.average()
        } else { 0.0 }

        val efficiency = getEfficiencyRating(avgConsumption)

        val currentFuelRate = if (fuelSamples.isNotEmpty()) {
            fuelSamples.last().fuelRateLph
        } else { 0.0 }

        return FuelConsumptionData(
            instantLph = currentFuelRate,
            instantL100km = instantConsumption,
            avgL100km = avgConsumption,
            tripDistance = tripDistance,
            totalFuelUsed = totalFuelUsed,
            efficiency = efficiency
        )
    }

    fun getEfficiencyRating(consumption: Double): EfficiencyRating {
        return when {
            consumption <= 0 -> EfficiencyRating.AVERAGE
            consumption < HIGHWAY_CONSUMPTION_MAX -> EfficiencyRating.EXCELLENT
            consumption < CITY_CONSUMPTION_MIN -> EfficiencyRating.GOOD
            consumption < CITY_CONSUMPTION_MAX -> EfficiencyRating.AVERAGE
            consumption < SPORT_CONSUMPTION_MIN -> EfficiencyRating.POOR
            else -> EfficiencyRating.CRITICAL
        }
    }

    fun estimateRange(fuelLevelPercent: Double, tankCapacityLiters: Double, avgConsumption: Double): Double {
        if (avgConsumption <= 0 || fuelLevelPercent <= 0) { return 0.0 }
        val currentFuel = tankCapacityLiters * fuelLevelPercent / 100.0
        return if (avgConsumption > 0) { currentFuel / avgConsumption * 100.0 } else { 0.0 }
    }

    fun reset() {
        fuelSamples.clear()
        consumptionHistory.clear()
        tripDistance = 0.0
        totalFuelUsed = 0.0
        lastSpeed = 0.0
        lastTimestamp = 0L
    }
}
