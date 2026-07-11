package com.canopobd.viewmodel

import android.util.Log
import com.canopobd.data.domain.*
import com.canopobd.data.model.*
import com.canopobd.data.repository.TransmissionData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnalyzerManager {

    companion object {
        private const val TAG = "AnalyzerManager"
    }

    // --- Analyzer instances ---
    val batteryAnalyzer = BatteryHealthAnalyzer()
    val egrAnalyzer = EGRHealthAnalyzer()
    val evapAnalyzer = EVAPSystemAnalyzer()
    val saiAnalyzer = SecondaryAirAnalyzer()
    val lambdaAnalyzer = LambdaO2SensorAnalyzer()
    val readinessAnalyzer = EmissionsReadinessAnalyzer()
    val oilConditionMonitor = OilConditionMonitor()
    val pcvMonitor = PCVMonitor()
    val lambdaBalanceAnalyzer = LambdaBalanceAnalyzer()
    val fuelConsumptionAnalyzer = FuelConsumptionAnalyzer()
    val m32GearboxMonitor = M32GearboxMonitor()
    val chainTensionerAnalyzer = ChainTensionerAnalyzer()
    val egtMonitor = EGTMonitor()
    val coolantHealthMonitor = CoolantSystemHealth()
    val turboSpoolAnalyzer = TurboSpoolAnalyzer()
    val turboEfficiencyAnalyzer = TurboEfficiencyAnalyzer()
    val boostLeakDetector = BoostLeakDetector()
    val wastegateHealthAnalyzer = WastegateHealthAnalyzer()
    val sensorHealthMonitor = SensorHealthMonitor()
    val driveStyleAnalyzer = DriveStyleAnalyzer()
    val drivingEfficiencyScorer = DrivingEfficiencyScorer()
    val fuelSystemAnalyzer = FuelSystemAnalyzer()
    val oilHealthPredictor = OilHealthPredictor()
    val sensorValidator = SensorValidator(AstraJ14TurboCalibration.INSTANCE)
    val fuelTrimAnalyzer = FuelTrimAnalyzer()

    // --- Emissions Analyzer State ---
    val batteryHealth = MutableStateFlow(BatteryStatus(0.0, -1, BatteryHealth.GOOD, false))
    val batteryHealthScore = MutableStateFlow(100)
    val batteryAnalysis = MutableStateFlow<BatteryHealthAnalyzer.BatteryAnalysis?>(null)

    val egrHealth = MutableStateFlow(EGRHealth(EGRStatus.CLOSED, 0.0, 0.0, 100))
    val egrAnalysis = MutableStateFlow<EGRHealthAnalyzer.EGRAnalysis?>(null)

    val evapStatus = MutableStateFlow(EVAPStatus(0.0, 0.0, false, null))
    val evapAnalysis = MutableStateFlow<EVAPSystemAnalyzer.EVAPAnalysis?>(null)

    val saiStatus = MutableStateFlow(SAIStatus(false, 0L, 100))
    val saiAnalysis = MutableStateFlow<SecondaryAirAnalyzer.SAIAnalysis?>(null)

    val lambdaAnalysis = MutableStateFlow<LambdaO2SensorAnalyzer.LambdaAnalysis?>(null)
    val emissionsReadiness = MutableStateFlow<EmissionsReadinessAnalyzer.ReadinessAnalysis?>(null)

    // --- Extended Analyzer State ---
    val oilConditionResult = MutableStateFlow(
        OilConditionMonitor.OilAnalysis(
            condition = OilConditionMonitor.OilCondition.UNKNOWN, healthScore = 0,
            oilLifeRemaining = 0.0, remainingKm = 0, remainingDays = 0,
            temperatureHealth = 0, pressureHealth = 0, contaminationRisk = 0,
            diagnosis = "", recommendation = "", oilType = ""
        )
    )
    val pcvResult = MutableStateFlow(
        PCVMonitor.PCVAnalysis(
            health = PCVMonitor.PCVHealth.UNKNOWN, healthScore = 0, mafDeviation = 0.0,
            totalTrimDeviation = 0.0, oilConsumptionStatus = "", diagnosis = "", recommendation = ""
        )
    )
    val lambdaBalanceData = MutableStateFlow(LambdaBalanceAnalyzer.LambdaBalance())
    val fuelConsumptionData = MutableStateFlow(FuelConsumptionAnalyzer.FuelConsumptionData())
    val gearboxResult = MutableStateFlow(
        M32GearboxMonitor.GearboxAnalysis(
            health = M32GearboxMonitor.GearboxHealth.UNKNOWN, healthScore = 0,
            detectedIssues = emptyList(), shiftQualityScore = 0, rpmSpeedRatioAnomaly = 0,
            bearingWearIndicator = 0, oilConditionScore = 0, diagnosis = "", recommendation = ""
        )
    )
    val chainTensionerResult = MutableStateFlow(
        ChainTensionerAnalyzer.ChainTensionerAnalysis(
            health = ChainTensionerAnalyzer.ChainTensionerHealth.UNKNOWN, healthScore = 0,
            dtcPenalty = 0, rattlePenalty = 0, rpmStabilityPenalty = 0, timingVariancePenalty = 0,
            diagnosis = "", recommendation = "", chainElongationEstimate = ""
        )
    )
    val egtResult = MutableStateFlow(
        EGTMonitor.EGTAnalysis(
            status = EGTMonitor.EGTStatus.NO_DATA, healthScore = 0,
            trend = EGTMonitor.EGTTrend.STABLE, thermalStressIndex = 0.0,
            thermalStressHours = 0.0, cylinderBalance = 0.0, estimatedEgtMax = 0.0,
            egtDeviation = 0.0, diagnosis = "", recommendation = "", warningFlags = emptyList()
        )
    )
    val coolantResult = MutableStateFlow(
        CoolantSystemHealth.CoolantAnalysis(
            status = CoolantSystemHealth.CoolantSystemStatus.UNKNOWN, healthScore = 0,
            thermostatState = CoolantSystemHealth.ThermostatState.UNKNOWN,
            thermostatOpeningTemp = 0.0, waterPumpEfficiency = 0, leakProbability = 0,
            coolantTempStable = true, diagnosis = "", recommendation = "", detectedIssues = emptyList()
        )
    )
    val oilHealthPrediction = MutableStateFlow(
        OilHealthPredictor.OilHealthPredictionResult(
            prediction = OilHealthPredictor.OilHealthPrediction.UNKNOWN,
            healthScore = 0, thermalStressIndex = 0.0, degradationPercent = 0.0,
            effectiveOilAgeKm = 0.0, kmSinceOilChange = 0.0, recommendedChangeKm = 0,
            recommendedChangeDays = 0, thermalLoadScore = 0, drivingPatternScore = 0,
            consumptionScore = 0, diagnosis = "", recommendation = "", oilType = ""
        )
    )
    val sensorValidationResult: MutableStateFlow<ValidationResult> = MutableStateFlow(ValidationResult.Unavailable)
    val turboSpoolResult = MutableStateFlow(
        TurboSpoolAnalyzer.SpoolAnalysis(
            status = TurboSpoolAnalyzer.SpoolStatus.INSUFFICIENT_DATA, healthScore = 0,
            spoolTimeSeconds = 0.0, expectedSpoolTime = 0.0, spoolDeviation = 0.0,
            wastegateResponse = 0.0, turboAcceleration = 0.0, diagnosis = "",
            recommendation = "", trendIndicator = TurboSpoolAnalyzer.SpoolTrend.UNKNOWN
        )
    )
    val turboEfficiencyResult = MutableStateFlow(
        TurboEfficiencyAnalyzer.TurboEfficiencyAnalysis(
            efficiency = TurboEfficiencyAnalyzer.TurboEfficiency.GOOD, healthScore = 0,
            boostEfficiency = 0.0, responseTimeScore = 0, wastegateHealthScore = 0,
            egtTrendScore = 0, intercoolerEfficiency = 0.0, boostDeviation = 0.0,
            diagnosis = "", recommendation = ""
        )
    )
    val boostLeakResult = MutableStateFlow(
        BoostLeakDetector.BoostLeakAnalysis(
            severity = BoostLeakDetector.LeakSeverity.UNKNOWN,
            likelyLocation = BoostLeakDetector.LeakLocation.UNKNOWN, healthScore = -1,
            boostDeviationPercent = 0.0, turboBoostCorrelation = 0.0,
            tempDeltaAnomaly = 0.0, mafDeviation = 0.0, confidencePercent = 0,
            diagnosis = "", recommendation = "", detectedIndicators = emptyList()
        )
    )
    val wastegateResult = MutableStateFlow(
        WastegateHealthAnalyzer.WastegateAnalysis(
            condition = WastegateHealthAnalyzer.WastegateCondition.UNKNOWN,
            currentDutyPercent = 0.0, avgDutyPercent = 0.0, boostDeviation = 0.0,
            healthScore = 0, diagnosis = "", recommendation = ""
        )
    )
    val sensorHealthSummary = MutableStateFlow(
        SensorHealthMonitor.SensorHealthSummary(
            overallHealthScore = 0, overallStatus = SensorHealthMonitor.HealthStatus.UNKNOWN,
            sensorHealths = emptyMap(), criticalIssues = emptyList(), recommendations = emptyList()
        )
    )
    val driveStyleResult = MutableStateFlow(
        DriveStyleAnalyzer.DriveStyleAnalysis(
            driveStyle = DriveStyleAnalyzer.DriveStyle.BALANCED, ecoScore = 0, sportScore = 0,
            wearScore = 0, rpmDistribution = DriveStyleAnalyzer.RPMDistribution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            throttleSmoothness = 0, brakingPattern = 0, overboostFrequency = 0.0,
            shiftQuality = 0, feedback = "", detailedFeedback = emptyList()
        )
    )
    val drivingEfficiencyResult = MutableStateFlow(DrivingEfficiencyScorer.EfficiencyScore())
    val fuelSystemResult = MutableStateFlow(
        FuelSystemAnalyzer.FuelSystemAnalysis(
            health = FuelSystemAnalyzer.FuelSystemHealth.UNKNOWN, healthScore = 0,
            detectedIssues = emptyList(), fuelRailPressureDeviation = 0.0,
            trimHealthScore = 0, injectorHealthScore = 0, carbonBuildupRisk = 0,
            diagnosis = "", recommendation = ""
        )
    )

    data class ExtendedAnalyzerSummary(
        val oilHealth: String = "Unbekannt",
        val pcvHealth: String = "Unbekannt",
        val chainHealth: String = "Unbekannt",
        val gearboxHealth: String = "Unbekannt",
        val coolantHealth: String = "Unbekannt",
        val boostLeakRisk: String = "Unbekannt",
        val overallScore: Int = 0,
        val criticalWarnings: List<String> = emptyList()
    )

    val extendedAnalyzerData = MutableStateFlow(ExtendedAnalyzerSummary())

    // --- Voltage histories ---
    val voltageHistory = MutableStateFlow<List<Double>>(emptyList())
    val o2VoltageHistory = MutableStateFlow<List<Double>>(emptyList())

    // --- Oil thermal stress tracking ---
    var oilTimeAbove110C = 0.0
    var oilTimeAbove115C = 0.0
    var oilTimeAbove120C = 0.0
    var oilShortTripCount = 0
    var lastOilTempSampleTime = 0L
    var lastOilTempWasCold = true

    // --- Turbo spool time tracking ---
    var turboSpoolTracking = false
    var turboSpoolStartTime = 0L
    var turboSpoolStartBoost = 0.0

    fun updateEmissionsAnalyzers(
        data: OBDData,
        dtcCodes: List<String>,
        onVoltageHistoryUpdate: (List<Double>) -> Unit,
        onO2VoltageHistoryUpdate: (List<Double>) -> Unit
    ) {
        val history = synchronized(voltageHistory) {
            val h = voltageHistory.value.toMutableList()
            if (data.batteryVoltage > 0) {
                h.add(data.batteryVoltage)
                if (h.size > 60) h.removeAt(0)
                voltageHistory.value = h
            }
            h.toList()
        }
        val o2History = synchronized(o2VoltageHistory) {
            val o2 = o2VoltageHistory.value.toMutableList()
            if (data.o2VoltageB1S1 > 0) {
                o2.add(data.o2VoltageB1S1)
                if (o2.size > 60) o2.removeAt(0)
                o2VoltageHistory.value = o2
            }
            o2.toList()
        }

        onVoltageHistoryUpdate(history)
        onO2VoltageHistoryUpdate(o2History)

        updateBatteryAnalysis(data, dtcCodes, history)
        updateEGRAnalysis(data, dtcCodes)
        updateEVAPAnalysis(data, dtcCodes)
        updateSAIAnalysis(data, dtcCodes)
        updateLambdaAnalysis(data, dtcCodes, o2History)
    }

    private fun updateBatteryAnalysis(data: OBDData, dtcCodes: List<String>, history: List<Double>) {
        val input = BatteryHealthAnalyzer.BatteryInput(
            voltageHistory = history,
            currentVoltage = data.batteryVoltage,
            engineRpm = data.rpm,
            alternatorDuty = data.alternatorDuty,
            controlModuleVoltage = data.controlModuleVoltage,
            activeDTCs = dtcCodes,
            coolantTemp = data.coolantTemp
        )
        val result = batteryAnalyzer.analyze(input)
        batteryHealth.value = result.status
        batteryHealthScore.value = result.healthScore
        batteryAnalysis.value = result
    }

    private fun updateEGRAnalysis(data: OBDData, dtcCodes: List<String>) {
        val input = EGRHealthAnalyzer.EGRInput(
            commandedEGR = data.commandedEGR,
            egrTemp = data.egrTemp,
            engineLoad = data.engineLoad,
            rpm = data.rpm,
            coolantTemp = data.coolantTemp,
            intakeTemp = data.intakeTemp,
            mafRate = data.mafRate,
            stftB1 = data.shortTermFuelTrimB1,
            ltftB1 = data.longTermFuelTrimB1,
            activeDTCs = dtcCodes
        )
        val result = egrAnalyzer.analyze(input)
        egrHealth.value = result.health
        egrAnalysis.value = result
    }

    private fun updateEVAPAnalysis(data: OBDData, dtcCodes: List<String>) {
        val input = EVAPSystemAnalyzer.EVAPInput(
            commandedEvapPurge = data.commandedEvapPurge,
            fuelLevel = data.fuelLevel,
            coolantTemp = data.coolantTemp,
            engineRpm = data.rpm,
            engineLoad = data.engineLoad,
            activeDTCs = dtcCodes
        )
        val result = evapAnalyzer.analyze(input)
        evapStatus.value = result.status
        evapAnalysis.value = result
    }

    private fun updateSAIAnalysis(data: OBDData, dtcCodes: List<String>) {
        val saActive = data.coolantTemp < 65.0 && data.rpm > 0 && data.rpm < 2000 && data.runTime > 5 && data.runTime < 120
        val input = SecondaryAirAnalyzer.SAIInput(
            saActive = saActive,
            engineRpm = data.rpm,
            coolantTemp = data.coolantTemp,
            intakeTemp = data.intakeTemp,
            o2VoltageB1S1 = data.o2VoltageB1S1,
            engineRuntimeSeconds = data.runTime,
            activeDTCs = dtcCodes
        )
        val result = saiAnalyzer.analyze(input)
        saiStatus.value = result.status
        saiAnalysis.value = result
    }

    private fun updateLambdaAnalysis(data: OBDData, dtcCodes: List<String>, o2History: List<Double>) {
        val input = LambdaO2SensorAnalyzer.LambdaInput(
            o2VoltageB1S1 = data.o2VoltageB1S1,
            o2VoltageB1S2 = data.o2VoltageB1S2,
            fuelAirRatio = data.fuelAirRatio,
            stftB1 = data.shortTermFuelTrimB1,
            ltftB1 = data.longTermFuelTrimB1,
            coolantTemp = data.coolantTemp,
            engineLoad = data.engineLoad,
            rpm = data.rpm,
            catalystTemp = data.catalystTemp,
            engineRuntimeSeconds = data.runTime,
            activeDTCs = dtcCodes,
            voltageHistoryB1S1 = o2History
        )
        val result = lambdaAnalyzer.analyze(input)
        lambdaAnalysis.value = result
    }

    fun updateExtendedAnalyzers(
        data: OBDData,
        dtcCodes: List<String>,
        currentKm: Int,
        maintenanceItems: List<com.canopobd.data.model.MaintenanceItem>,
        timingChainState: TimingChainState,
        timingAdvanceBuffer: List<Double>,
        wastegateDuty: Double,
        canTransmissionData: Any? = null
    ) {
        val oilTemp = data.oilTempMode22.takeIf { it > 0.0 } ?: data.oilTemp
        val now = System.currentTimeMillis()
        if (lastOilTempSampleTime > 0 && oilTemp > 0) {
            val dtHours = (now - lastOilTempSampleTime) / 3_600_000.0
            if (oilTemp > 110) oilTimeAbove110C += dtHours
            if (oilTemp > 115) oilTimeAbove115C += dtHours
            if (oilTemp > 120) oilTimeAbove120C += dtHours
            if (lastOilTempWasCold && data.coolantTemp > 70 && data.runTime < 600) {
                oilShortTripCount++
            }
        }
        lastOilTempWasCold = oilTemp < 70 || data.coolantTemp < 70
        lastOilTempSampleTime = now

        try {
            val oilInput = OilConditionMonitor.OilInput(
                oilTemp = data.oilTempMode22.takeIf { it > 0.0 } ?: data.coolantTemp,
                oilPressure = 0.0,
                coolantTemp = data.coolantTemp,
                rpm = data.rpm,
                engineLoad = data.engineLoad,
                speed = data.speed
            )
            oilConditionResult.value = oilConditionMonitor.analyze(oilInput)
        } catch (e: Exception) { Log.w(TAG, "OilConditionMonitor failed", e) }

        try {
            val oilHealthInput = OilHealthPredictor.OilHealthInput(
                oilTemp = data.oilTempMode22.takeIf { it > 0.0 } ?: data.oilTemp,
                coolantTemp = data.coolantTemp,
                rpm = data.rpm,
                engineLoad = data.engineLoad,
                boostPressureKpa = data.boostPressure,
                totalKm = currentKm.toDouble(),
                lastOilChangeKm = maintenanceItems.find { it.type == com.canopobd.data.model.MaintenanceType.OIL_CHANGE }?.lastServiceKm?.toDouble() ?: 0.0,
                lastOilChangeTimestamp = maintenanceItems.find { it.type == com.canopobd.data.model.MaintenanceType.OIL_CHANGE }?.lastServiceDate ?: 0L,
                engineRuntimeSec = data.runTime,
                drivingPattern = determineDrivingPattern(data),
                timeAbove110C = oilTimeAbove110C,
                timeAbove115C = oilTimeAbove115C,
                timeAbove120C = oilTimeAbove120C,
                shortTripCount = oilShortTripCount,
                oilConsumptionLPer1000Km = estimateOilConsumption(data)
            )
            oilHealthPrediction.value = oilHealthPredictor.analyze(oilHealthInput)
        } catch (e: Exception) { Log.w(TAG, "OilHealthPredictor failed", e) }

        try {
            sensorValidator.addMaf(data.mafRate)
            sensorValidator.addRpm(data.rpm)
            sensorValidationResult.value = sensorValidator.validateMaf(data.mafRate)
            val boostBar = if (data.boostPressure > 0) (data.boostPressure - data.barometricPressure).coerceAtLeast(0.0) / 100.0 else null
            val boostValidation = sensorValidator.validateBoost(boostBar, data.barometricPressure)
            val coolantValidation = sensorValidator.validateCoolant(data.coolantTemp)
            val rpmValidation = sensorValidator.validateRpm(data.rpm)
            val aggregatedResult = when {
                sensorValidationResult.value is ValidationResult.Invalid -> sensorValidationResult.value
                boostValidation is ValidationResult.Invalid -> boostValidation
                coolantValidation is ValidationResult.Invalid -> coolantValidation
                rpmValidation is ValidationResult.Invalid -> rpmValidation
                boostValidation is ValidationResult.Suspicious -> boostValidation
                coolantValidation is ValidationResult.Suspicious -> coolantValidation
                rpmValidation is ValidationResult.Suspicious -> rpmValidation
                else -> sensorValidationResult.value
            }
            sensorValidationResult.value = aggregatedResult
        } catch (e: Exception) { Log.w(TAG, "SensorValidator failed", e) }

        try {
            val expectedMaf = data.rpm * 0.01
            val pcvInput = PCVMonitor.PCVInput(
                activeDTCs = dtcCodes,
                mafRate = data.mafRate,
                mafExpectedAtRpm = expectedMaf,
                stft = data.shortTermFuelTrimB1,
                ltft = data.longTermFuelTrimB1,
                intakeManifoldPressure = data.intakePressure,
                rpm = data.rpm,
                coolantTemp = data.coolantTemp,
                engineLoad = data.engineLoad,
                throttle = data.throttle
            )
            pcvResult.value = pcvMonitor.analyze(pcvInput)
        } catch (e: Exception) { Log.w(TAG, "PCVMonitor failed", e) }

        try {
            lambdaBalanceAnalyzer.addLambdaSample(data.fuelAirRatio.takeIf { it > 0 } ?: 1.0)
            lambdaBalanceData.value = lambdaBalanceAnalyzer.analyzeCurrentSequence()
        } catch (e: Exception) { Log.w(TAG, "LambdaBalanceAnalyzer failed", e) }

        try {
            if (data.speed > 5.0 && data.mafRate > 0) {
                val l100km = fuelConsumptionAnalyzer.calculateFromMAF(data.mafRate, data.speed)
                fuelConsumptionData.value = fuelConsumptionData.value.copy(
                    instantL100km = l100km,
                    avgL100km = if (l100km > 0) (fuelConsumptionData.value.avgL100km + l100km) / 2.0 else fuelConsumptionData.value.avgL100km
                )
            }
        } catch (e: Exception) { Log.w(TAG, "FuelConsumptionAnalyzer failed", e) }

        try {
            val tcmData = canTransmissionData as? TransmissionData
            val m32Input = M32GearboxMonitor.GearboxInput(
                rpmHistory = listOf(data.rpm),
                speedHistory = listOf(data.speed),
                gearPosition = tcmData?.gear ?: 0,
                clutchPosition = if (tcmData?.clutchStatus == "slipping") 1.0 else if (tcmData?.gear != null && tcmData.gear > 0) 0.0 else 0.5,
                transmissionTemp = tcmData?.oilTemp ?: data.coolantTemp.toDouble(),
                activeDTCs = dtcCodes
            )
            gearboxResult.value = m32GearboxMonitor.analyze(m32Input)
        } catch (e: Exception) { Log.w(TAG, "M32GearboxMonitor failed", e) }

        try {
            val chainInput = ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = dtcCodes,
                coldStartRattleDurationSec = if (timingChainState.coldStartRattleDetected) timingChainState.coldSampleCount.toDouble() * 0.5 else 0.0,
                idleRpmVariance = timingChainState.idleRpmVariation,
                timingAdvanceVariance = if (timingAdvanceBuffer.size >= 3) {
                    val mean = timingAdvanceBuffer.average()
                    timingAdvanceBuffer.map { (it - mean) * (it - mean) }.sum() / timingAdvanceBuffer.size
                } else 0.0,
                currentRpm = data.rpm,
                timingAdvance = data.timingAdvance,
                coolantTemp = data.coolantTemp,
                engineRuntimeSec = data.runTime
            )
            chainTensionerResult.value = chainTensionerAnalyzer.analyze(chainInput)
        } catch (e: Exception) { Log.w(TAG, "ChainTensionerAnalyzer failed", e) }

        try {
            val egtInput = EGTMonitor.EGTInput(
                egtBank1 = data.egtBank1,
                rpm = data.rpm,
                engineLoad = data.engineLoad,
                coolantTemp = data.coolantTemp
            )
            egtResult.value = egtMonitor.analyze(egtInput)
        } catch (e: Exception) { Log.w(TAG, "EGTMonitor failed", e) }

        try {
            val coolantInput = CoolantSystemHealth.CoolantInput(
                coolantTemp = data.coolantTemp,
                intakeTemp = data.intakeTemp,
                rpm = data.rpm,
                engineLoad = data.engineLoad,
                engineRuntimeSec = data.runTime
            )
            coolantResult.value = coolantHealthMonitor.analyze(coolantInput)
        } catch (e: Exception) { Log.w(TAG, "CoolantSystemHealth failed", e) }

        try {
            sensorHealthSummary.value = sensorHealthMonitor.analyzeSensors(data)
        } catch (e: Exception) { Log.w(TAG, "SensorHealthMonitor failed", e) }

        try {
            val calibration = AstraJ14TurboCalibration.INSTANCE
            val baroKpa = if (data.barometricPressure > 0) data.barometricPressure else 100.0
            val absoluteBoostKpa = if (data.boostPressure > 0) data.boostPressure else data.intakePressure
            val boostBar = calibration.getBoostBar((absoluteBoostKpa - baroKpa).coerceAtLeast(0.0))
            val targetBoostBar = calibration.normalBoostTargetBar
            wastegateResult.value = wastegateHealthAnalyzer.analyze(
                wastegateDuty = wastegateDuty,
                avgWastegateDuty = wastegateDuty,
                targetBoost = targetBoostBar,
                actualBoost = boostBar,
                rpm = data.rpm,
                engineLoad = data.engineLoad
            )
        } catch (e: Exception) { Log.w(TAG, "WastegateHealthAnalyzer failed", e) }

        try {
            val calibration = AstraJ14TurboCalibration.INSTANCE
            val baroKpa = if (data.barometricPressure > 0) data.barometricPressure else 100.0
            val absoluteBoostKpa = if (data.boostPressure > 0) data.boostPressure else data.intakePressure
            val actualBoostBar = calibration.getBoostBar((absoluteBoostKpa - baroKpa).coerceAtLeast(0.0))
            val targetBoostBar = calibration.normalBoostTargetBar

            val boostLeakInput = BoostLeakDetector.BoostLeakInput(
                boostActualBar = actualBoostBar,
                boostTargetBar = targetBoostBar,
                wastegateDuty = data.wastegateControl,
                turboRpm = data.turboRpm,
                chargeAirTemp = data.chargeAirCoolerTemp,
                intakeTemp = data.intakeTemp,
                mafRate = data.mafRate,
                rpm = data.rpm,
                engineLoad = data.engineLoad,
                throttle = data.throttle,
                exhaustPressure = data.exhaustPressure,
                stftB1 = data.shortTermFuelTrimB1,
                ltftB1 = data.longTermFuelTrimB1
            )
            boostLeakResult.value = boostLeakDetector.analyze(boostLeakInput)
        } catch (e: Exception) { Log.w(TAG, "BoostLeakDetector failed", e) }

        try {
            val calibration = AstraJ14TurboCalibration.INSTANCE
            val baroKpa = if (data.barometricPressure > 0) data.barometricPressure else 100.0
            val absoluteBoostKpa = if (data.boostPressure > 0) data.boostPressure else data.intakePressure
            val actualBoostBar = calibration.getBoostBar((absoluteBoostKpa - baroKpa).coerceAtLeast(0.0))
            val targetBoostBar = calibration.normalBoostTargetBar

            val turboEfficiencyInput = TurboEfficiencyAnalyzer.TurboInput(
                boostActualBar = actualBoostBar,
                boostTargetBar = targetBoostBar,
                wastegateDuty = data.wastegateControl,
                turboRpm = data.turboRpm,
                egtBank1 = data.egtBank1,
                egtBank2 = data.egtBank2,
                rpm = data.rpm,
                engineLoad = data.engineLoad,
                throttle = data.throttle,
                chargeAirTemp = data.chargeAirCoolerTemp,
                intakeTemp = data.intakeTemp,
                coolantTemp = data.coolantTemp,
                boostPressureKpa = absoluteBoostKpa,
                wastegateControl = data.wastegateControl,
                totalKm = currentKm.toDouble()
            )
            turboEfficiencyResult.value = turboEfficiencyAnalyzer.analyze(turboEfficiencyInput)
        } catch (e: Exception) { Log.w(TAG, "TurboEfficiencyAnalyzer failed", e) }

        try {
            val calibration = AstraJ14TurboCalibration.INSTANCE
            val baroKpa = if (data.barometricPressure > 0) data.barometricPressure else 100.0
            val absoluteBoostKpa = if (data.boostPressure > 0) data.boostPressure else data.intakePressure
            val actualBoostBar = calibration.getBoostBar((absoluteBoostKpa - baroKpa).coerceAtLeast(0.0))
            val targetBoostAt80 = calibration.normalBoostTargetBar

            val nowMs = System.currentTimeMillis()
            val spoolTime = run {
                if (data.throttle > 80 && !turboSpoolTracking) {
                    turboSpoolTracking = true
                    turboSpoolStartTime = nowMs
                    turboSpoolStartBoost = actualBoostBar
                }
                if (turboSpoolTracking && actualBoostBar >= targetBoostAt80 * 0.8) {
                    val elapsed = (nowMs - turboSpoolStartTime) / 1000.0
                    turboSpoolTracking = false
                    if (elapsed > 0.1 && elapsed < 10.0) elapsed else 0.0
                } else if (turboSpoolTracking && (nowMs - turboSpoolStartTime) > 10000) {
                    turboSpoolTracking = false
                    0.0
                } else {
                    0.0
                }
            }

            val turboSpoolInput = TurboSpoolAnalyzer.SpoolInput(
                throttleApplication = data.throttle,
                boostAtThrottleApplication = actualBoostBar,
                boostAt80Percent = targetBoostAt80,
                targetBoostAt80 = targetBoostAt80,
                spoolTimeSeconds = spoolTime,
                wastegateDutyAtSpool = data.wastegateControl,
                wastegateDutyIdle = wastegateDuty,
                turboRpmAtSpool = data.turboRpm,
                rpmAtThrottleApplication = data.rpm,
                rpmAt80PercentBoost = data.rpm,
                engineLoad = data.engineLoad,
                intakeTemp = data.intakeTemp,
                boostPressureKpa = absoluteBoostKpa
            )
            turboSpoolResult.value = turboSpoolAnalyzer.analyze(turboSpoolInput)
        } catch (e: Exception) { Log.w(TAG, "TurboSpoolAnalyzer failed", e) }

        try {
            extendedAnalyzerData.value = ExtendedAnalyzerSummary(
                oilHealth = oilConditionResult.value.condition.name,
                pcvHealth = pcvResult.value.health.name,
                chainHealth = chainTensionerResult.value.health.name,
                gearboxHealth = gearboxResult.value.health.name,
                coolantHealth = coolantResult.value.status.name,
                boostLeakRisk = boostLeakResult.value.severity.name,
                overallScore = calculateExtendedScore(),
                criticalWarnings = buildExtendedWarnings()
            )
        } catch (e: Exception) { Log.w(TAG, "ExtendedAnalyzerSummary failed", e) }
    }

    private fun calculateExtendedScore(): Int {
        var score = 100
        if (oilConditionResult.value.condition != OilConditionMonitor.OilCondition.EXCELLENT &&
            oilConditionResult.value.condition != OilConditionMonitor.OilCondition.UNKNOWN
        ) score -= 15
        if (pcvResult.value.health != PCVMonitor.PCVHealth.HEALTHY) score -= 15
        if (chainTensionerResult.value.health == ChainTensionerAnalyzer.ChainTensionerHealth.CRITICAL) score -= 20
        if (gearboxResult.value.health == M32GearboxMonitor.GearboxHealth.CRITICAL) score -= 15
        if (coolantResult.value.status == CoolantSystemHealth.CoolantSystemStatus.OVERHEATING) score -= 10
        return score.coerceIn(0, 100)
    }

    private fun buildExtendedWarnings(): List<String> {
        val warnings = mutableListOf<String>()
        if (oilConditionResult.value.condition == OilConditionMonitor.OilCondition.CRITICAL) warnings.add("Ölwechsel dringend erforderlich!")
        if (pcvResult.value.health == PCVMonitor.PCVHealth.PLUGGED) warnings.add("PCV-Ventil verstopft!")
        if (chainTensionerResult.value.health == ChainTensionerAnalyzer.ChainTensionerHealth.CRITICAL) warnings.add("Steuerkette kritisch!")
        if (gearboxResult.value.health == M32GearboxMonitor.GearboxHealth.CRITICAL) warnings.add("Getriebe M32 kritisch!")
        if (coolantResult.value.status == CoolantSystemHealth.CoolantSystemStatus.OVERHEATING) warnings.add("Kühlsystem Überhitzung!")
        return warnings
    }

    private fun determineDrivingPattern(data: OBDData): OilHealthPredictor.DrivingPattern {
        val avgRpm = data.rpm
        val load = data.engineLoad
        val speed = data.speed
        return when {
            avgRpm > 5000 || load > 85 || speed > 180 -> OilHealthPredictor.DrivingPattern.TRACK
            avgRpm > 3000 || load > 60 || speed > 120 -> OilHealthPredictor.DrivingPattern.SPORTY
            avgRpm > 800 && speed < 30 && data.runTime < 600 -> OilHealthPredictor.DrivingPattern.SHORT_TRIP
            load > 70 && data.runTime > 300 -> OilHealthPredictor.DrivingPattern.TOWING
            speed < 5 && avgRpm < 800 -> OilHealthPredictor.DrivingPattern.EASY
            else -> OilHealthPredictor.DrivingPattern.NORMAL
        }
    }

    private fun estimateOilConsumption(data: OBDData): Double {
        val baseConsumption = 0.08
        val loadFactor = data.engineLoad / 100.0
        val rpmFactor = if (data.rpm > 4000) 2.0 else if (data.rpm > 3000) 1.5 else 1.0
        val boostFactor = if (data.boostPressure > 150) 1.8 else if (data.boostPressure > 100) 1.3 else 1.0
        return baseConsumption * rpmFactor * boostFactor * (1 + loadFactor)
    }
}
