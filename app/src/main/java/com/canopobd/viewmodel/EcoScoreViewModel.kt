package com.canopobd.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.canopobd.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

class EcoScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val _ecoScore = MutableStateFlow(EcoScoreData())
    val ecoScore: StateFlow<EcoScoreData> = _ecoScore.asStateFlow()

    private val _co2Data = MutableStateFlow(CO2Data())
    val co2Data: StateFlow<CO2Data> = _co2Data.asStateFlow()

    private val _fuelCost = MutableStateFlow(FuelCostData())
    val fuelCost: StateFlow<FuelCostData> = _fuelCost.asStateFlow()

    private val _rangeEstimation = MutableStateFlow(RangeEstimation())
    val rangeEstimation: StateFlow<RangeEstimation> = _rangeEstimation.asStateFlow()

    private val _efficiency = MutableStateFlow(FuelEfficiencyMetrics())
    val efficiency: StateFlow<FuelEfficiencyMetrics> = _efficiency.asStateFlow()

    private val _drivingStyle = MutableStateFlow(DrivingStyleAnalysis())
    val drivingStyle: StateFlow<DrivingStyleAnalysis> = _drivingStyle.asStateFlow()

    private val _tips = MutableStateFlow<List<EcoTip>>(emptyList())
    val tips: StateFlow<List<EcoTip>> = _tips.asStateFlow()

    private val _showEcoScore = MutableStateFlow(false)
    val showEcoScore: StateFlow<Boolean> = _showEcoScore.asStateFlow()

    private var tripStartFuelLiters = 0.0
    private var tripDistanceKm = 0.0
    private var tripDurationSeconds = 0L
    private var tripIdleSeconds = 0L
    private var tripMaxSpeed = 0.0
    private var totalIdleTimeMs = 0L
    private var lastSpeed = 0.0
    private var lastTimestamp = 0L
    private var lastThrottle = 0.0
    private var brakeEventCount = 0
    private var harshBrakeCount = 0
    private var coastingSamples = 0
    private var totalSamples = 0
    private var decelSamples = 0

    fun updateFromOBDData(data: OBDData, fuelLevelPercent: Double) {
        val now = System.currentTimeMillis()
        val dt = if (lastTimestamp > 0) (now - lastTimestamp) / 1000.0 else 0.0
        lastTimestamp = now

        if (data.speed > 0 && dt > 0) {
            tripDistanceKm += data.speed / 3600.0 * dt
            tripDurationSeconds += dt.toLong()
        }

        if (data.speed < 3.0 && data.rpm > 500) {
            tripIdleSeconds += dt.toLong()
        }

        if (data.speed > tripMaxSpeed) {
            tripMaxSpeed = data.speed
        }

        updateEfficiency(data, fuelLevelPercent)
        updateEcoScore(data)
        updateCO2()
        updateFuelCost()
        updateRange(fuelLevelPercent)
        updateDrivingStyle(data)
        generateTips(data)

        lastSpeed = data.speed
        lastThrottle = data.throttle
    }

    private fun updateEfficiency(data: OBDData, fuelLevelPercent: Double) {
        val fuelUsedLiters = if (tripStartFuelLiters == 0.0) 0.0 else
            max(0.0, tripStartFuelLiters - (fuelLevelPercent / 100.0 * 52.0))

        if (tripStartFuelLiters == 0.0 && fuelLevelPercent > 0) {
            tripStartFuelLiters = fuelLevelPercent / 100.0 * 52.0
        }

        val instantConsumption = if (data.mafRate > 0 && data.speed > 0) {
            (data.mafRate * 3600.0) / (data.speed * 12128.4)
        } else 0.0

        val averageConsumption = if (tripDistanceKm > 0 && fuelUsedLiters > 0) {
            (fuelUsedLiters / tripDistanceKm) * 100.0
        } else 0.0

        val idlePercent = if (tripDurationSeconds > 0) {
            (tripIdleSeconds.toDouble() / tripDurationSeconds) * 100.0
        } else 0.0

        val cruisingPercent = if (tripDurationSeconds > 0) {
            ((tripDurationSeconds - tripIdleSeconds).toDouble() / tripDurationSeconds) * 100.0
        } else 0.0

        _efficiency.value = FuelEfficiencyMetrics(
            instantLPer100km = instantConsumption,
            averageLPer100km = averageConsumption,
            instantMpg = if (instantConsumption > 0) 235.215 / instantConsumption else 0.0,
            averageMpg = if (averageConsumption > 0) 235.215 / averageConsumption else 0.0,
            fuelUsedLiters = fuelUsedLiters,
            fuelUsedGallons = fuelUsedLiters * 0.264172,
            distanceKm = tripDistanceKm,
            distanceMiles = tripDistanceKm * 0.621371,
            averageSpeedKmh = if (tripDurationSeconds > 0) tripDistanceKm / (tripDurationSeconds / 3600.0) else 0.0,
            averageSpeedMph = if (tripDurationSeconds > 0) (tripDistanceKm * 0.621371) / (tripDurationSeconds / 3600.0) else 0.0,
            cruisingTimePercent = cruisingPercent,
            idleTimePercent = idlePercent
        )
    }

    private fun updateEcoScore(data: OBDData) {
        val eff = _efficiency.value
        val thresholds = AstraJEcoThresholds

        val efficiencyScore = when {
            eff.averageLPer100km <= 0 -> 50
            eff.averageLPer100km <= thresholds.TARGET_HIGHWAY -> 100
            eff.averageLPer100km <= thresholds.TARGET_COMBINED -> 80
            eff.averageLPer100km <= thresholds.TARGET_CITY -> 60
            eff.averageLPer100km <= 10.0 -> 40
            else -> 20
        }

        val smoothnessScore = when {
            data.speed <= 0 -> 50
            data.throttle < 30 -> 90
            data.throttle < 60 -> 70
            data.throttle < 80 -> 50
            else -> 30
        }

        val cruisingScore = when {
            eff.idleTimePercent < 5 -> 95
            eff.idleTimePercent < 10 -> 80
            eff.idleTimePercent < 20 -> 60
            eff.idleTimePercent < 30 -> 40
            else -> 20
        }

        val momentumScore = when {
            eff.cruisingTimePercent > 70 -> 90
            eff.cruisingTimePercent > 50 -> 75
            eff.cruisingTimePercent > 30 -> 60
            else -> 40
        }

        val overallScore = ((efficiencyScore * 0.35 +
                smoothnessScore * 0.25 +
                cruisingScore * 0.20 +
                momentumScore * 0.20)).toInt().coerceIn(0, 100)

        _ecoScore.value = EcoScoreData(
            overallScore = overallScore,
            efficiencyScore = efficiencyScore,
            smoothnessScore = smoothnessScore,
            cruisingScore = cruisingScore,
            momentumScore = momentumScore,
            grade = EcoScoreData.calculateGrade(overallScore),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun updateCO2() {
        val fuelUsed = _efficiency.value.fuelUsedLiters
        val distance = _efficiency.value.distanceKm
        val tripCO2 = fuelUsed * CO2Data.CO2_PER_LITER_GASOLINE_KG
        val perKm = if (distance > 0) tripCO2 / distance else 0.0

        val annualProjection = if (_efficiency.value.averageLPer100km > 0) {
            (_efficiency.value.averageLPer100km / 100.0 * 15000.0) * CO2Data.CO2_PER_LITER_GASOLINE_KG
        } else 0.0

        _co2Data.value = CO2Data(
            tripCO2Kg = tripCO2,
            tripCO2Lb = tripCO2 * CO2Data.KG_TO_LB,
            perKmCO2Kg = perKm,
            perMileCO2Lb = perKm / CO2Data.KM_TO_MILE * CO2Data.KG_TO_LB,
            annualEstimateKg = annualProjection,
            treesEquivalent = annualProjection / CO2Data.AVG_TREE_CO2_ABSORPTION_KG_YEAR,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun updateFuelCost() {
        val eff = _efficiency.value
        val costPerLiter = _fuelCost.value.fuelPricePerLiter

        val tripCost = eff.fuelUsedLiters * costPerLiter
        val costPerKm = if (eff.distanceKm > 0) tripCost / eff.distanceKm else 0.0

        val annualLiters = if (eff.averageLPer100km > 0) eff.averageLPer100km / 100.0 * 15000.0 else 0.0
        val annualCost = annualLiters * costPerLiter

        _fuelCost.value = _fuelCost.value.copy(
            tripCost = tripCost,
            costPerKm = costPerKm,
            costPerMile = costPerKm / 0.621371,
            dailyCost = annualCost / 365.0,
            weeklyCost = annualCost / 52.0,
            monthlyCost = annualCost / 12.0,
            annualCost = annualCost,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun updateRange(fuelLevelPercent: Double) {
        val tankLiters = RangeEstimation.ASTRA_J_TANK_LITERS
        val reserveLiters = RangeEstimation.RESERVE_LITERS
        val fuelLevelLiters = fuelLevelPercent / 100.0 * tankLiters
        val avgConsumption = _efficiency.value.averageLPer100km

        val usableFuel = max(0.0, fuelLevelLiters - reserveLiters)
        val estimatedRange = if (avgConsumption > 0) (usableFuel / avgConsumption * 100.0).toInt() else 0

        val bestRange = (usableFuel / 5.0 * 100.0).toInt()
        val worstRange = (usableFuel / 9.0 * 100.0).toInt()

        _rangeEstimation.value = RangeEstimation(
            estimatedRangeKm = estimatedRange,
            estimatedRangeMiles = (estimatedRange * 0.621371).toInt(),
            fuelLevelPercent = fuelLevelPercent.toInt(),
            fuelLevelLiters = fuelLevelLiters,
            averageConsumption = avgConsumption,
            bestCaseRangeKm = bestRange,
            worstCaseRangeKm = worstRange,
            refuelNeededAtKm = max(0, estimatedRange - 50),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun updateDrivingStyle(data: OBDData) {
        val eff = _efficiency.value
        val accelScore = when {
            data.throttle < 30 -> 95
            data.throttle < 50 -> 80
            data.throttle < 70 -> 60
            data.throttle < 90 -> 40
            else -> 20
        }

        totalSamples++
        val speedDelta = data.speed - lastSpeed
        val isDecelerating = speedDelta < -2.0
        val isCoasting = data.throttle < 10.0 && data.speed > 10.0 && speedDelta > -3.0

        if (isDecelerating) {
            decelSamples++
            brakeEventCount++
            if (speedDelta < -8.0) harshBrakeCount++
        }
        if (isCoasting) coastingSamples++

        val brakeScore = when {
            brakeEventCount == 0 -> 80
            else -> {
                val harshRatio = harshBrakeCount.toDouble() / brakeEventCount.toDouble()
                (100.0 * (1.0 - harshRatio)).coerceIn(20.0, 95.0).toInt()
            }
        }

        val cruiseScore = if (eff.cruisingTimePercent > 60) 90 else 50
        val anticipationScore = when {
            totalSamples < 10 -> 70
            coastingSamples == 0 -> 40
            else -> {
                val coastRatio = coastingSamples.toDouble() / totalSamples.toDouble()
                (coastRatio * 100.0).coerceIn(20.0, 95.0).toInt()
            }
        }

        val totalScore = (accelScore * 0.4 + brakeScore * 0.2 + cruiseScore * 0.2 + anticipationScore * 0.2).toInt()

        val style = when {
            totalScore >= 80 -> DrivingStyle.ECONOMICAL
            totalScore >= 60 -> DrivingStyle.NORMAL
            totalScore >= 40 -> DrivingStyle.AGGRESSIVE
            else -> DrivingStyle.SPORT
        }

        _drivingStyle.value = DrivingStyleAnalysis(
            style = style,
            stylePercentage = totalScore.toFloat(),
            accelerationScore = accelScore,
            brakingScore = brakeScore,
            cruisingScore = cruiseScore,
            anticipationScore = anticipationScore
        )
    }

    private fun generateTips(data: OBDData) {
        val tips = mutableListOf<EcoTip>()
        val eff = _efficiency.value

        if (data.speed > 130) {
            tips.add(EcoTip("speed", "Geschwindigkeit reduzieren",
                "Fahren über 130 km/h erhöht den Verbrauch um ~15% pro 20 km/h.",
                15.0, TipPriority.HIGH, impact = TipImpact.FUEL_CONSUMPTION))
        }

        if (data.rpm > 3000 && data.speed > 40) {
            tips.add(EcoTip("rpm", "Früher hochschalten",
                "Versuchen, bei ${data.rpm.toInt()} RPM hochzuschalten. Optimal für ECO: 2000-2500 RPM.",
                10.0, TipPriority.MEDIUM, impact = TipImpact.FUEL_CONSUMPTION))
        }

        if (eff.idleTimePercent > 15) {
            tips.add(EcoTip("idle", "Leerlauf minimieren",
                "Leerlaufzeit von ${eff.idleTimePercent.toInt()}% erkannt. Motor abstellen bei längeren Stopps.",
                8.0, TipPriority.MEDIUM, impact = TipImpact.FUEL_CONSUMPTION))
        }

        if (data.coolantTemp < AstraJEcoThresholds.ENGINE_WARM_THRESHOLD_C && data.speed > 0) {
            tips.add(EcoTip("warmup", "Motor auf Betriebstemperatur bringen",
                "Motor noch kalt (${data.coolantTemp.toInt()}°C). Vermeiden hoher Last bis 70°C.",
                5.0, TipPriority.LOW, impact = TipImpact.ENGINE_LIFE))
        }

        if (data.throttle > 80) {
            tips.add(EcoTip("accel", "Sanfter beschleunigen",
                "Starkes Beschleunigen (>80% Pedal) erhöht den Verbrauch um bis zu 20%.",
                12.0, TipPriority.HIGH, impact = TipImpact.FUEL_CONSUMPTION))
        }

        if (data.throttle < 50 && data.speed < 60) {
            tips.add(EcoTip("ecomode", "ECO-Modus aktivieren",
                "Im Stadtverkehr kann ECO-Modus bis zu 10% Kraftstoff sparen.",
                10.0, TipPriority.MEDIUM, impact = TipImpact.FUEL_CONSUMPTION))
        }

        _tips.value = tips
    }

    fun setFuelPrice(pricePerLiter: Double) {
        _fuelCost.value = _fuelCost.value.copy(
            fuelPricePerLiter = pricePerLiter,
            fuelPricePerGallon = pricePerLiter * 3.78541
        )
        updateFuelCost()
    }

    fun toggleEcoScore() {
        _showEcoScore.value = !_showEcoScore.value
    }

    fun dismissEcoScore() {
        _showEcoScore.value = false
    }

    fun resetTripData() {
        tripStartFuelLiters = 0.0
        tripDistanceKm = 0.0
        tripDurationSeconds = 0L
        tripIdleSeconds = 0L
        tripMaxSpeed = 0.0
        totalIdleTimeMs = 0L
        lastSpeed = 0.0
        lastTimestamp = 0L
        lastThrottle = 0.0
        brakeEventCount = 0
        harshBrakeCount = 0
        coastingSamples = 0
        totalSamples = 0
        decelSamples = 0
    }
}
