package com.canopobd.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.canopobd.data.domain.WastegateHealthAnalyzer
import com.canopobd.data.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class TurboViewModel(application: Application) : AndroidViewModel(application) {

    val turboSpeedRpm = MutableStateFlow(0.0)
    val wastegateDuty = MutableStateFlow(0.0)
    val wastegatePosition = MutableStateFlow(0.0)
    val chargeAirTemp = MutableStateFlow(0.0)
    val turboEfficiency = MutableStateFlow(0.0)
    val turboHealthScore = MutableStateFlow(100.0)

    private val _turboData = MutableStateFlow(TurboData())
    val turboData: StateFlow<TurboData> = _turboData.asStateFlow()

    private val _oilData = MutableStateFlow(OilData())
    val oilData: StateFlow<OilData> = _oilData.asStateFlow()

    private val _showTurboMonitor = MutableStateFlow(false)
    val showTurboMonitor: StateFlow<Boolean> = _showTurboMonitor.asStateFlow()

    private val _showTurboCooldown = MutableStateFlow(false)
    val showTurboCooldown: StateFlow<Boolean> = _showTurboCooldown.asStateFlow()

    private val _turboCooldownState = MutableStateFlow(TurboCoolDownState())
    val turboCooldownState: StateFlow<TurboCoolDownState> = _turboCooldownState.asStateFlow()

    fun updateFromOBDData(data: OBDData, carProfile: CarProfile) {
        val calibration = AstraJ14TurboCalibration.INSTANCE
        val baroKpa = data.barometricPressure.takeIf { it > 0.0 } ?: 100.0
        val absoluteBoostKpa = if (data.boostPressure > 0) { data.boostPressure } else { data.intakePressure }
        val relativeBoostKpa = (absoluteBoostKpa - baroKpa).coerceAtLeast(0.0)
        val targetBoostKpa = calibration.normalBoostTargetBar * 100.0

        turboSpeedRpm.value = data.turboRpm
        chargeAirTemp.value = data.chargeAirCoolerTemp
        wastegateDuty.value = data.wastegateControl

        val boostAnalysis = analyzeBoost(
            relativeBoostKpa, targetBoostKpa, calibration
        )
        val wgAnalysis = analyzeWastegateWithSession(
            data.wastegateControl, data.rpm.toInt(), data.engineLoad, calibration, relativeBoostKpa, null
        )
        wastegatePosition.value = wgAnalysis.position

        val turboHealth = calculateTurboHealth(
            boostAnalysis, wgAnalysis, data.turboRpm, data.egtBank1, calibration
        )
        turboHealthScore.value = turboHealth.overallScore.toDouble()

        val speedFactor = (calibration.maxTurboRpm.toDouble() / 200000.0)
        val efficiencyFactor = if (data.turboRpm > 0) {
            (boostAnalysis.actual / (data.turboRpm * speedFactor * 0.001)).coerceIn(0.0, 100.0)
        } else { 0.0 }
        turboEfficiency.value = efficiencyFactor

        updateTurboDataInternal(data, carProfile, boostAnalysis)
        updateOilDataInternal(data)
    }

    fun updateFromOBDDataWithDriveSession(
        data: OBDData,
        carProfile: CarProfile,
        driveSession: DriveSession
    ) {
        val calibration = AstraJ14TurboCalibration.INSTANCE
        val baroKpa = data.barometricPressure.takeIf { it > 0.0 } ?: 100.0
        val absoluteBoostKpa = if (data.boostPressure > 0) { data.boostPressure } else { data.intakePressure }
        val relativeBoostKpa = (absoluteBoostKpa - baroKpa).coerceAtLeast(0.0)
        val targetBoostKpa = calibration.normalBoostTargetBar * 100.0

        turboSpeedRpm.value = data.turboRpm
        chargeAirTemp.value = data.chargeAirCoolerTemp
        wastegateDuty.value = data.wastegateControl

        val boostAnalysis = analyzeBoost(
            relativeBoostKpa, targetBoostKpa, calibration
        )
        val wgAnalysis = analyzeWastegateWithSession(
            data.wastegateControl, data.rpm.toInt(), data.engineLoad, calibration, relativeBoostKpa, driveSession
        )
        wastegatePosition.value = wgAnalysis.position

        val turboHealth = calculateTurboHealth(
            boostAnalysis, wgAnalysis, data.turboRpm, data.egtBank1, calibration
        )
        turboHealthScore.value = turboHealth.overallScore.toDouble()

        val speedFactor = (calibration.maxTurboRpm.toDouble() / 200000.0)
        val efficiencyFactor = if (data.turboRpm > 0) {
            (boostAnalysis.actual / (data.turboRpm * speedFactor * 0.001)).coerceIn(0.0, 100.0)
        } else { 0.0 }
        turboEfficiency.value = efficiencyFactor

        updateTurboDataInternal(data, carProfile, boostAnalysis)
        updateOilDataInternal(data)
    }

    private fun updateTurboDataInternal(
        data: OBDData,
        carProfile: CarProfile,
        boostAnalysis: BoostAnalysis
    ) {
        val baroKpa = if (data.barometricPressure > 0) { data.barometricPressure } else { 100.0 }
        val absoluteBoostKpa = if (data.boostPressure > 0) { data.boostPressure } else { data.intakePressure }
        val relativeBoostKpa = (absoluteBoostKpa - baroKpa).coerceAtLeast(0.0)
        val relativeBoostBar = relativeBoostKpa / 100.0
        val targetRelativeBar = carProfile.normalBoostBar.toDouble()
        val overboostActive = relativeBoostBar > 1.0
        val underboostDetected = relativeBoostBar < targetRelativeBar * 0.5 && data.rpm > 2000
        val healthScore = when {
            overboostActive -> 40
            underboostDetected -> 50
            boostAnalysis.status == BoostStatus.HIGH -> 75
            relativeBoostBar > targetRelativeBar * 0.85 -> 90
            else -> 100
        }
        _turboData.value = _turboData.value.copy(
            boostPressure = relativeBoostBar,
            boostTarget = targetRelativeBar,
            wastegateDutyCycle = if (relativeBoostBar > 0.01) { (targetRelativeBar / relativeBoostBar * 50).coerceIn(25.0, 95.0) } else { 95.0 },
            turboHealthScore = healthScore,
            overboostActive = overboostActive,
            underboostDetected = underboostDetected,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun updateOilDataInternal(data: OBDData) {
        _oilData.value = _oilData.value.copy(
            temperature = data.oilTemp,
            pressure = (data.turboOilPressure ?: 0.0) / 100.0,
            timestamp = System.currentTimeMillis()
        )
    }

    fun toggleTurboMonitor() {
        _showTurboMonitor.value = !_showTurboMonitor.value
    }

    fun toggleTurboCooldown() {
        _showTurboCooldown.value = !_showTurboCooldown.value
    }

    fun analyzeBoost(
        actualKpa: Double,
        targetKpa: Double,
        calibration: AstraJ14TurboCalibration
    ): BoostAnalysis {
        val actualBar = calibration.getBoostBar(actualKpa)
        val targetBar = calibration.getBoostBar(targetKpa)
        val deviation = if (targetBar > 0) { ((actualBar - targetBar) / targetBar * 100.0) } else { 0.0 }

        val status = when {
            actualBar >= calibration.overboostBar -> BoostStatus.OVERBOOST
            actualBar >= calibration.maxBoostBar * 0.85 -> BoostStatus.HIGH
            actualBar < targetBar * 0.5 && targetBar > 0 -> BoostStatus.LOW
            else -> BoostStatus.NORMAL
        }

        val healthScore = when {
            actualBar > 1.35 -> 10
            actualBar > calibration.maxBoostBar -> 20
            actualBar >= calibration.overboostBar -> 40
            abs(deviation) > 30 -> 50
            abs(deviation) > 20 -> 70
            abs(deviation) > 10 -> 85
            else -> 100
        }

        return BoostAnalysis(
            actual = actualBar,
            target = targetBar,
            deviation = deviation,
            status = status,
            healthScore = healthScore
        )
    }

    fun analyzeWastegate(
        dutyCycle: Double,
        rpm: Int,
        load: Double,
        calibration: AstraJ14TurboCalibration
    ): WastegateAnalysisResult {
        return analyzeWastegateWithSession(dutyCycle, rpm, load, calibration, 0.0, null)
    }

    private fun analyzeWastegateWithSession(
        dutyCycle: Double,
        rpm: Int,
        load: Double,
        calibration: AstraJ14TurboCalibration,
        actualBoostKpa: Double,
        driveSession: DriveSession?
    ): WastegateAnalysisResult {
        val avgWastegate = if (driveSession != null && driveSession.wastegateSampleCount > 0) {
            driveSession.wastegateDutySum / driveSession.wastegateSampleCount
        } else { dutyCycle }

        val targetBoostKpa = calibration.normalBoostTargetBar * 100.0

        val analyzer = WastegateHealthAnalyzer()
        val analysis = analyzer.analyze(
            wastegateDuty = dutyCycle,
            avgWastegateDuty = avgWastegate,
            targetBoost = targetBoostKpa,
            actualBoost = actualBoostKpa,
            rpm = rpm.toDouble(),
            engineLoad = load
        )

        val position = when {
            dutyCycle > 90.0 -> 95.0
            dutyCycle < 5.0 -> 2.0
            else -> dutyCycle
        }

        val recommendations = mutableListOf<String>()
        recommendations.add(analysis.recommendation)
        if (rpm > 2000 && dutyCycle < 20 && load > 60) {
            recommendations.add("Wastegate geschlossen bei Last - Aktuator prüfen")
        }

        return WastegateAnalysisResult(
            dutyCycle = dutyCycle,
            position = position,
            status = analysis.condition.name,
            healthScore = analysis.healthScore,
            recommendations = recommendations
        )
    }

    fun calculateTurboHealth(
        boostAnalysis: BoostAnalysis,
        wastegateAnalysis: WastegateAnalysisResult,
        turboSpeed: Double,
        egt: Double,
        calibration: AstraJ14TurboCalibration
    ): TurboHealthResult {
        val boostScore = boostAnalysis.healthScore
        val wastegateScore = wastegateAnalysis.healthScore

        val egtScore = when {
            egt > 950 -> 10
            egt > calibration.maxEgtC -> 20
            egt > 800 -> 50
            egt > calibration.maxEgtC * 0.9 -> 70
            else -> 100
        }

        val speedScore = when {
            turboSpeed <= 0 -> 100
            turboSpeed > calibration.maxTurboRpm -> 15
            turboSpeed > calibration.maxTurboRpm * 0.9 -> 50
            turboSpeed > calibration.maxTurboRpm * 0.75 -> 75
            else -> 100
        }

        val overallScore = (boostScore + wastegateScore + egtScore + speedScore) / 4

        val status = when {
            overallScore >= 90 -> TurboHealthStatus.HEALTHY
            boostAnalysis.status == BoostStatus.OVERBOOST -> TurboHealthStatus.OVERBOOST
            boostAnalysis.status == BoostStatus.LOW -> TurboHealthStatus.UNDERBOOST
            wastegateScore < 60 -> TurboHealthStatus.WASTEGATE_ISSUE
            egtScore < 60 -> TurboHealthStatus.INTERCOOLER_EFFICIENCY
            else -> TurboHealthStatus.HEALTHY
        }

        return TurboHealthResult(
            overallScore = overallScore,
            boostScore = boostScore,
            wastegateScore = wastegateScore,
            egtScore = egtScore,
            speedScore = speedScore,
            status = status
        )
    }

    private var turboAnalysisJob: Job? = null

    private val _currentDriveSession = MutableStateFlow(DriveSession())

    fun startTurboAnalysisCollection(obdDataFlow: kotlinx.coroutines.flow.Flow<OBDData>, carProfileFlow: kotlinx.coroutines.flow.Flow<CarProfile>, driveSession: DriveSession = DriveSession()) {
        turboAnalysisJob?.cancel()
        _currentDriveSession.value = driveSession
        turboAnalysisJob = viewModelScope.launch {
            kotlinx.coroutines.flow.combine(obdDataFlow, carProfileFlow) { data, carProfile ->
                data to carProfile
            }.collect { (data, carProfile) ->
                if (data.rpm > 0) {
                    updateFromOBDDataWithDriveSession(data, carProfile, _currentDriveSession.value)
                }
            }
        }
    }

    fun updateDriveSession(driveSession: DriveSession) {
        _currentDriveSession.value = driveSession
    }

    override fun onCleared() {
        turboAnalysisJob?.cancel()
        super.onCleared()
    }
}
