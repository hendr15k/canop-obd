package com.canopobd.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.canopobd.bluetooth.RemoteBridge
import com.canopobd.data.repository.CANRepository
import com.canopobd.data.repository.TurboMonitoringData
import com.canopobd.data.domain.BatteryHealthAnalyzer
import com.canopobd.data.domain.DriveMode
import com.canopobd.data.domain.DriveModeDetector
import com.canopobd.data.domain.DriveScoreCalculator
import com.canopobd.data.domain.EGRHealthAnalyzer
import com.canopobd.data.domain.EmissionsReadinessAnalyzer
import com.canopobd.data.domain.EVAPSystemAnalyzer
import com.canopobd.data.domain.FuelTrimAnalyzer
import com.canopobd.data.domain.LambdaO2SensorAnalyzer
import com.canopobd.data.domain.SecondaryAirAnalyzer

import com.canopobd.data.model.*
import com.canopobd.data.repository.OBDRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

@SuppressLint("MissingPermission")
class DashboardViewModel private constructor(
    application: Application
) : ViewModel() {

    private val context: Application = application
    private val turboViewModel = TurboViewModel(application)
    private val safetyViewModel = SafetyViewModel(application)
    private val ecoScoreViewModel = EcoScoreViewModel(application)

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val repository = OBDRepository(context, bluetoothManager?.adapter)

    val connectionState: StateFlow<OBDConnectionState> = repository.connectionState
    val obdData: StateFlow<OBDData> = repository.obdData
    val dtcResponse: StateFlow<DTCResponse?> = repository.dtcResponse
    val recordingActive: StateFlow<Boolean> = repository.recordingActive
    val recordedData: StateFlow<List<DataRecord>> = repository.recordedData
    val pollRate: StateFlow<Long> = repository.pollRate
    val measurementUnit: StateFlow<MeasurementUnit> = repository.measurementUnit

    val remoteServerRunning: StateFlow<Boolean> = repository.remoteServerRunning
    val remoteServerPort: StateFlow<Int> = repository.remoteServerPort
    val remoteConnectedClients: StateFlow<Int> = repository.remoteConnectedClients
    val remoteServerIp: StateFlow<String> = repository.remoteServerIp

    val tripData: StateFlow<TripData> = repository.tripData
    val connectionStats: StateFlow<ConnectionStats> = repository.connectionStats
    val autoReconnect: StateFlow<Boolean> = repository.autoReconnect
    val lastError: StateFlow<String?> = repository.lastError
    val colorTheme: StateFlow<ColorTheme> = repository.colorTheme
    val appThemeMode: StateFlow<AppThemeMode> = repository.appThemeMode
    val primaryGaugeIds: StateFlow<Set<String>> = repository.primaryGaugeIds
    val pollMode: StateFlow<PollMode> = repository.pollMode

    val currentLocation = repository.currentLocation
    val isGPSTracking = repository.isGPSTracking
    val currentTrip = repository.currentTrip
    val tripHistory = repository.tripHistory
    val trendHistory = repository.trendHistory

    val readinessMonitor = repository.readinessMonitor
    val detectedProtocol = repository.detectedProtocol
    val supportedPIDs = repository.supportedPIDs
    val alertConfig = repository.alertConfig
    val activeAlerts = repository.activeAlerts
    val freezeFrames = repository.freezeFrames
    val importedData = repository.importedData

    private val _showCustomization = MutableStateFlow(false)
    val showCustomization: StateFlow<Boolean> = _showCustomization.asStateFlow()

    private val _showHUDMode = MutableStateFlow(false)
    val showHUDMode: StateFlow<Boolean> = _showHUDMode.asStateFlow()

    private val _showTrendGraph = MutableStateFlow(false)
    val showTrendGraph: StateFlow<Boolean> = _showTrendGraph.asStateFlow()

    private val _showReadiness = MutableStateFlow(false)
    val showReadiness: StateFlow<Boolean> = _showReadiness.asStateFlow()

    private val _showDiagnostics = MutableStateFlow(false)
    val showDiagnostics: StateFlow<Boolean> = _showDiagnostics.asStateFlow()

    private val _showAlertSettings = MutableStateFlow(false)
    val showAlertSettings: StateFlow<Boolean> = _showAlertSettings.asStateFlow()

    private val _showDataAnalysis = MutableStateFlow(false)
    val showDataAnalysis: StateFlow<Boolean> = _showDataAnalysis.asStateFlow()

    private val _showFuelEconomy = MutableStateFlow(false)
    val showFuelEconomy: StateFlow<Boolean> = _showFuelEconomy.asStateFlow()

    private val _showMaintenance = MutableStateFlow(false)
    val showMaintenance: StateFlow<Boolean> = _showMaintenance.asStateFlow()

    private val _showPerformanceTest = MutableStateFlow(false)
    val showPerformanceTest: StateFlow<Boolean> = _showPerformanceTest.asStateFlow()

    private val _showTripHistory = MutableStateFlow(false)
    val showTripHistory: StateFlow<Boolean> = _showTripHistory.asStateFlow()

    private val _maintenanceItems = MutableStateFlow<List<com.canopobd.data.model.MaintenanceItem>>(emptyList())
    val maintenanceItems: StateFlow<List<com.canopobd.data.model.MaintenanceItem>> = _maintenanceItems.asStateFlow()

    private val _currentKm = MutableStateFlow(0)
    val currentKm: StateFlow<Int> = _currentKm.asStateFlow()

    private val _fuelEconomyData = MutableStateFlow(com.canopobd.data.model.FuelEconomyData())
    val fuelEconomyData: StateFlow<com.canopobd.data.model.FuelEconomyData> = _fuelEconomyData.asStateFlow()

    private val _performanceTestState = MutableStateFlow(com.canopobd.data.model.PerformanceTestState())
    val performanceTestState: StateFlow<com.canopobd.data.model.PerformanceTestState> = _performanceTestState.asStateFlow()

    private val _showPowerCalculator = MutableStateFlow(false)
    val showPowerCalculator: StateFlow<Boolean> = _showPowerCalculator.asStateFlow()

    private val _showDriveScore = MutableStateFlow(false)
    val showDriveScore: StateFlow<Boolean> = _showDriveScore.asStateFlow()

    private val _showShiftLight = MutableStateFlow(false)
    val showShiftLight: StateFlow<Boolean> = _showShiftLight.asStateFlow()

    private val _showVehicleInfo = MutableStateFlow(false)
    val showVehicleInfo: StateFlow<Boolean> = _showVehicleInfo.asStateFlow()

    private val _showKnownIssues = MutableStateFlow(false)
    val showKnownIssues: StateFlow<Boolean> = _showKnownIssues.asStateFlow()

    private val _powerCalculation = MutableStateFlow(com.canopobd.data.model.PowerCalculation())
    val powerCalculation: StateFlow<com.canopobd.data.model.PowerCalculation> = _powerCalculation.asStateFlow()

    private val _driveScore = MutableStateFlow(com.canopobd.data.model.DriveScore())
    val driveScore: StateFlow<com.canopobd.data.model.DriveScore> = _driveScore.asStateFlow()

    private val _driveSession = MutableStateFlow(com.canopobd.data.model.DriveSession())
    val driveSession: StateFlow<com.canopobd.data.model.DriveSession> = _driveSession.asStateFlow()

    private val _shiftLightConfig = MutableStateFlow(com.canopobd.data.model.ShiftLightConfig())
    val shiftLightConfig: StateFlow<com.canopobd.data.model.ShiftLightConfig> = _shiftLightConfig.asStateFlow()

    private val _carProfileState = MutableStateFlow(com.canopobd.data.model.CarProfile.default())
    val carProfile: StateFlow<com.canopobd.data.model.CarProfile> = _carProfileState.asStateFlow()
    private val _timingChainState = MutableStateFlow(com.canopobd.data.model.TimingChainState())
    val timingChainState: StateFlow<com.canopobd.data.model.TimingChainState> = _timingChainState.asStateFlow()

    val turboData: StateFlow<com.canopobd.data.model.TurboData> get() = turboViewModel.turboData
    val oilData: StateFlow<com.canopobd.data.model.OilData> get() = turboViewModel.oilData

    private val _showTimingChainMonitor = MutableStateFlow(false)
    val showTimingChainMonitor: StateFlow<Boolean> = _showTimingChainMonitor.asStateFlow()
    private val _showCarProfile = MutableStateFlow(false)
    val showCarProfile: StateFlow<Boolean> = _showCarProfile.asStateFlow()

    val showTurboMonitor: StateFlow<Boolean> get() = turboViewModel.showTurboMonitor
    val showTurboCooldown: StateFlow<Boolean> get() = turboViewModel.showTurboCooldown
    val turboCooldownState: StateFlow<com.canopobd.data.model.TurboCoolDownState> get() = turboViewModel.turboCooldownState

    // Safety System State
    val showSafetySystems: StateFlow<Boolean> get() = safetyViewModel.showSafetySystems
    val safetySummary: StateFlow<com.canopobd.data.model.SafetySummary> get() = safetyViewModel.safetySummary
    val safetyDTCs: StateFlow<List<com.canopobd.data.model.SafetyDtc>> get() = safetyViewModel.safetyDTCs

    // ECO Score State
    val showEcoScoreDialog: StateFlow<Boolean> get() = ecoScoreViewModel.showEcoScore
    val ecoScoreData: StateFlow<com.canopobd.data.model.EcoScoreData> get() = ecoScoreViewModel.ecoScore
    val co2Data: StateFlow<com.canopobd.data.model.CO2Data> get() = ecoScoreViewModel.co2Data
    val fuelCostData: StateFlow<com.canopobd.data.model.FuelCostData> get() = ecoScoreViewModel.fuelCost
    val rangeEstimation: StateFlow<com.canopobd.data.model.RangeEstimation> get() = ecoScoreViewModel.rangeEstimation
    val drivingStyleAnalysis: StateFlow<com.canopobd.data.model.DrivingStyleAnalysis> get() = ecoScoreViewModel.drivingStyle
    val ecoTips: StateFlow<List<com.canopobd.data.model.EcoTip>> get() = ecoScoreViewModel.tips

    // Extended Feature State
    private val _showExtendedGearbox = MutableStateFlow(false)
    val showExtendedGearbox: StateFlow<Boolean> = _showExtendedGearbox.asStateFlow()

    private val _showExtendedTurbo = MutableStateFlow(false)
    val showExtendedTurbo: StateFlow<Boolean> = _showExtendedTurbo.asStateFlow()

    private val _showExtendedFuel = MutableStateFlow(false)
    val showExtendedFuel: StateFlow<Boolean> = _showExtendedFuel.asStateFlow()

    private val _showExtendedMaintenance = MutableStateFlow(false)
    val showExtendedMaintenance: StateFlow<Boolean> = _showExtendedMaintenance.asStateFlow()

    private val _showComfortControl = MutableStateFlow(false)
    val showComfortControl: StateFlow<Boolean> = _showComfortControl.asStateFlow()

    private val _devices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val devices: StateFlow<List<BluetoothDeviceInfo>> = _devices.asStateFlow()

    private val _showDevicePicker = MutableStateFlow(false)
    val showDevicePicker: StateFlow<Boolean> = _showDevicePicker.asStateFlow()

    private val _showDTCDialog = MutableStateFlow(false)
    val showDTCDialog: StateFlow<Boolean> = _showDTCDialog.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showDataLog = MutableStateFlow(false)
    val showDataLog: StateFlow<Boolean> = _showDataLog.asStateFlow()

    private val _showPIDScreen = MutableStateFlow(false)
    val showPIDScreen: StateFlow<Boolean> = _showPIDScreen.asStateFlow()

    private val _showRemoteDialog = MutableStateFlow(false)
    val showRemoteDialog: StateFlow<Boolean> = _showRemoteDialog.asStateFlow()

    private val _showTripComputer = MutableStateFlow(false)
    val showTripComputer: StateFlow<Boolean> = _showTripComputer.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private val _availableUpdate = MutableStateFlow<AppUpdate?>(null)
    val availableUpdate: StateFlow<AppUpdate?> = _availableUpdate.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    val turboSpeedRpm: StateFlow<Double> get() = turboViewModel.turboSpeedRpm
    val wastegateDuty: StateFlow<Double> get() = turboViewModel.wastegateDuty
    val wastegatePosition: StateFlow<Double> get() = turboViewModel.wastegatePosition
    val chargeAirTemp: StateFlow<Double> get() = turboViewModel.chargeAirTemp
    val turboEfficiency: StateFlow<Double> get() = turboViewModel.turboEfficiency
    val turboHealthScore: StateFlow<Double> get() = turboViewModel.turboHealthScore

    // Chain Tensioner State
    val chainHealthScore = MutableStateFlow(100.0)
    val chainTensionerHealth = MutableStateFlow(ChainHealth.UNKNOWN)
    val timingCorrelation = MutableStateFlow(0.0)

    // PCV State
    val pcvHealthScore = MutableStateFlow(100.0)
    val pcvHealth = MutableStateFlow(PCVHealth.UNKNOWN)
    val oilConsumptionRate = MutableStateFlow(0.0)

    // Fuel System State
    val fuelRailPressure = MutableStateFlow(0.0)
    val injectionQuantity = MutableStateFlow(0.0)
    val fuelSystemHealth = MutableStateFlow(FuelSystemHealth.UNKNOWN)

    // Drive Style State
    val ecoScore = MutableStateFlow(0.0)
    val sportScore = MutableStateFlow(0.0)
    val drivingStyle = MutableStateFlow(DriveStyle.ECONOMICAL)

    // Emissions Analyzer State
    private val batteryAnalyzer = BatteryHealthAnalyzer()
    private val egrAnalyzer = EGRHealthAnalyzer()
    private val evapAnalyzer = EVAPSystemAnalyzer()
    private val saiAnalyzer = SecondaryAirAnalyzer()
    private val lambdaAnalyzer = LambdaO2SensorAnalyzer()
    private val readinessAnalyzer = EmissionsReadinessAnalyzer()

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

    private val _voltageHistory = MutableStateFlow<List<Double>>(emptyList())
    private val _o2VoltageHistory = MutableStateFlow<List<Double>>(emptyList())

    // Warning System
    val criticalWarnings = MutableStateFlow<List<VehicleWarning>>(emptyList())

    // DTC Processing
    private val _processedDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val processedDTCs: StateFlow<List<ProcessedDTC>> = _processedDTCs.asStateFlow()
    private val _criticalDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val criticalDTCs: StateFlow<List<ProcessedDTC>> = _criticalDTCs.asStateFlow()
    private val _warningDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val warningDTCs: StateFlow<List<ProcessedDTC>> = _warningDTCs.asStateFlow()
    private val _infoDTCs = MutableStateFlow<List<ProcessedDTC>>(emptyList())
    val infoDTCs: StateFlow<List<ProcessedDTC>> = _infoDTCs.asStateFlow()

    // Mode 22 State
    private val _supportedMode22Pids = MutableStateFlow<List<String>>(emptyList())
    val supportedMode22Pids: StateFlow<List<String>> = _supportedMode22Pids.asStateFlow()
    private val _mode22DataCache = MutableStateFlow<Map<String, Mode22Data>>(emptyMap())
    val mode22DataCache: StateFlow<Map<String, Mode22Data>> = _mode22DataCache.asStateFlow()

    private val _dtcProcessingJob = MutableStateFlow<Job?>(null)

    init {
        if (_permissionsGranted.value) refreshDevices()
        _maintenanceItems.value = repository.loadMaintenanceItems()
        _shiftLightConfig.value = repository.loadShiftLightConfig()
        _carProfileState.value = repository.loadCarProfile()
        checkForUpdate()
        startTurboAnalysisCollection()
        startWarningMonitoring()
    }

    fun onPermissionsGranted() {
        _permissionsGranted.value = true
        refreshDevices()
        repository.getLastDevice()?.let { addr ->
            if (repository.autoReconnect.value) connect(addr)
        }
    }

    fun refreshDevices() {
        _devices.value = repository.getPairedDevices()
    }

    fun connect(deviceAddress: String) {
        _showDevicePicker.value = false
        repository.connect(deviceAddress)
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun toggleDevicePicker() {
        _showDevicePicker.value = !_showDevicePicker.value
        if (_showDevicePicker.value) refreshDevices()
    }

    fun toggleDTCDialog() {
        _showDTCDialog.value = !_showDTCDialog.value
        if (_showDTCDialog.value) repository.readDTCs()
    }

    fun clearDTCs() {
        repository.clearDTCs()
    }

    fun toggleSettings() {
        _showSettings.value = !_showSettings.value
    }

    fun toggleDataLog() {
        _showDataLog.value = !_showDataLog.value
    }

    fun togglePIDScreen() {
        _showPIDScreen.value = !_showPIDScreen.value
    }

    fun toggleRemoteDialog() {
        _showRemoteDialog.value = !_showRemoteDialog.value
    }

    fun toggleTripComputer() {
        _showTripComputer.value = !_showTripComputer.value
    }

    fun toggleCustomization() {
        _showCustomization.value = !_showCustomization.value
    }

    fun toggleHUDMode() {
        _showHUDMode.value = !_showHUDMode.value
    }

    fun toggleTrendGraph() {
        _showTrendGraph.value = !_showTrendGraph.value
    }

    fun toggleReadiness() {
        _showReadiness.value = !_showReadiness.value
        if (_showReadiness.value) repository.readReadinessMonitor()
    }

    fun toggleDiagnostics() {
        _showDiagnostics.value = !_showDiagnostics.value
        if (_showDiagnostics.value) {
            repository.readProtocol()
            repository.scanSupportedPIDs()
            repository.readFreezeFrames()
        }
    }

    fun toggleAlertSettings() {
        _showAlertSettings.value = !_showAlertSettings.value
    }

    fun toggleDataAnalysis() {
        _showDataAnalysis.value = !_showDataAnalysis.value
    }

    fun toggleFuelEconomy() {
        _showFuelEconomy.value = !_showFuelEconomy.value
        if (_showFuelEconomy.value) {
            _fuelEconomyData.value = repository.getFuelEconomyData()
        }
    }

    fun toggleMaintenance() {
        _showMaintenance.value = !_showMaintenance.value
    }

    fun togglePerformanceTest() {
        _showPerformanceTest.value = !_showPerformanceTest.value
    }

    fun toggleTripHistory() {
        _showTripHistory.value = !_showTripHistory.value
    }

    fun setMaintenanceItem(type: com.canopobd.data.model.MaintenanceType, lastKm: Int, interval: Int) {
        val item = com.canopobd.data.model.MaintenanceItem(
            type = type,
            lastServiceKm = lastKm,
            intervalKm = interval,
            currentKm = _currentKm.value
        )
        val current = _maintenanceItems.value.toMutableList()
        val index = current.indexOfFirst { it.type == type }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(item)
        }
        _maintenanceItems.value = current
        repository.saveMaintenanceItem(item)
    }

    fun resetMaintenanceItem(type: com.canopobd.data.model.MaintenanceType) {
        val km = _currentKm.value
        val item = com.canopobd.data.model.MaintenanceItem(
            type = type,
            lastServiceKm = km,
            intervalKm = type.defaultInterval,
            currentKm = km
        )
        val current = _maintenanceItems.value.toMutableList()
        val index = current.indexOfFirst { it.type == type }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(item)
        }
        _maintenanceItems.value = current
        repository.saveMaintenanceItem(item)
    }

    fun startPerformanceTest(testType: com.canopobd.data.model.PerformanceTestType) {
        _performanceTestState.value = _performanceTestState.value.copy(
            isRunning = true,
            currentTestType = testType,
            startTimeNanos = System.nanoTime(),
            statusMessage = "Warte auf Start…"
        )
    }

    fun stopPerformanceTest() {
        val state = _performanceTestState.value
        if (state.isRunning) {
            val elapsedNanos = System.nanoTime() - state.startTimeNanos
            val elapsedSeconds = elapsedNanos / 1_000_000_000.0
            val result = com.canopobd.data.model.PerformanceResult(
                testType = state.currentTestType,
                timeSeconds = elapsedSeconds,
                valid = elapsedSeconds > 0.5 && elapsedSeconds < 300.0
            )
            val history = listOf(result) + state.history.take(9)
            _performanceTestState.value = state.copy(
                isRunning = false,
                lastResult = result,
                history = history,
                statusMessage = ""
            )
        } else {
            _performanceTestState.value = state.copy(isRunning = false, statusMessage = "")
        }
    }

    fun updatePerformanceTestStatus(message: String) {
        _performanceTestState.value = _performanceTestState.value.copy(statusMessage = message)
    }

    fun togglePowerCalculator() {
        _showPowerCalculator.value = !_showPowerCalculator.value
        if (_showPowerCalculator.value) {
            val d = repository.obdData.value
            _powerCalculation.value = com.canopobd.data.model.PowerCalculation.calculate(d.mafRate, d.rpm, d.intakeTemp)
        }
    }

    fun toggleDriveScore() {
        _showDriveScore.value = !_showDriveScore.value
    }

    fun toggleShiftLight() {
        _showShiftLight.value = !_showShiftLight.value
    }

    fun toggleVehicleInfo() {
        _showVehicleInfo.value = !_showVehicleInfo.value
    }

    fun toggleKnownIssues() {
        _showKnownIssues.value = !_showKnownIssues.value
    }

    fun toggleTurboMonitor() {
        val turningOn = !turboViewModel.showTurboMonitor.value
        turboViewModel.toggleTurboMonitor()
        if (turningOn) {
            val data = repository.obdData.value
            val profile = _carProfileState.value
            turboViewModel.updateFromOBDData(data, profile)
        }
    }

    fun toggleTimingChainMonitor() {
        _showTimingChainMonitor.value = !_showTimingChainMonitor.value
        if (_showTimingChainMonitor.value) {
            updateTimingChainState()
        }
    }

    fun toggleCarProfile() {
        _showCarProfile.value = !_showCarProfile.value
    }

    fun toggleTurboCooldown() {
        turboViewModel.toggleTurboCooldown()
    }

    fun toggleSafetySystems() { safetyViewModel.toggleSafetySystems() }
    fun dismissSafetySystems() { safetyViewModel.dismissSafetySystems() }

    fun toggleEcoScore() { ecoScoreViewModel.toggleEcoScore() }
    fun dismissEcoScore() { ecoScoreViewModel.dismissEcoScore() }
    fun setFuelPrice(price: Double) { ecoScoreViewModel.setFuelPrice(price) }

    fun toggleExtendedGearbox() { _showExtendedGearbox.value = !_showExtendedGearbox.value }
    fun toggleExtendedTurbo() { _showExtendedTurbo.value = !_showExtendedTurbo.value }
    fun toggleExtendedFuel() { _showExtendedFuel.value = !_showExtendedFuel.value }
    fun toggleExtendedMaintenance() { _showExtendedMaintenance.value = !_showExtendedMaintenance.value }
    fun toggleComfortControl() { _showComfortControl.value = !_showComfortControl.value }

    fun selectCarProfile(profile: com.canopobd.data.model.CarProfile) {
        _carProfileState.value = profile
        repository.saveCarProfile(profile)
        _showCarProfile.value = false
    }

    private fun updateTurboData() {
        turboViewModel.updateFromOBDData(repository.obdData.value, _carProfileState.value)
    }

    private fun updateTimingChainState() {
        val data = repository.obdData.value
        val rpm = data.rpm
        val coolant = data.coolantTemp
        val isWarmedUp = coolant > 80
        val rpmVariation = if (rpm > 0) (rpm * 0.02) else 0.0
        val healthScore = when {
            rpmVariation < 2.0 && isWarmedUp -> 95
            rpmVariation < 5.0 && isWarmedUp -> 80
            rpmVariation < 10.0 -> 60
            else -> 40
        }
        val phase = when {
            rpm < 500 -> com.canopobd.data.model.TimingChainPhase.UNKNOWN
            rpmVariation > 5.0 && !isWarmedUp -> com.canopobd.data.model.TimingChainPhase.COLD_RATTLE
            !isWarmedUp -> com.canopobd.data.model.TimingChainPhase.WARMING_UP
            rpmVariation < 2.0 -> com.canopobd.data.model.TimingChainPhase.HEALTHY
            rpmVariation < 5.0 -> com.canopobd.data.model.TimingChainPhase.STABLE
            else -> com.canopobd.data.model.TimingChainPhase.WARNING
        }
        _timingChainState.value = _timingChainState.value.copy(
            healthScore = healthScore,
            coldStartRattleDetected = rpmVariation > 5.0 && !isWarmedUp,
            idleRpmVariation = rpmVariation,
            isWarmedUp = isWarmedUp,
            phase = phase,
            recordedSamples = _timingChainState.value.recordedSamples + 1,
            coldSampleCount = if (!isWarmedUp) _timingChainState.value.coldSampleCount + 1 else _timingChainState.value.coldSampleCount,
            lastRpmReading = rpm,
            avgRpmCold = if (!isWarmedUp) (_timingChainState.value.avgRpmCold + rpm) / (_timingChainState.value.coldSampleCount + 1.0) else _timingChainState.value.avgRpmCold,
            avgRpmWarm = if (isWarmedUp) (_timingChainState.value.avgRpmWarm + rpm) / 2 else _timingChainState.value.avgRpmWarm,
            rpmDeviationCold = rpmVariation
        )
    }

    fun updateDriveScore() {
        val session = _driveSession.value
        _driveScore.value = DriveScoreCalculator.computeScore(session)
    }

    fun resetDriveScore() {
        _driveSession.value = com.canopobd.data.model.DriveSession()
        _driveScore.value = com.canopobd.data.model.DriveScore()
    }

    fun recordDriveSample(rpm: Double, throttle: Double, speed: Double, prevRpm: Double, boostBar: Double = 0.0, wastegateDuty: Double = 0.0) {
        val session = _driveSession.value
        val rpmDelta = rpm - prevRpm
        val isDecelerating = throttle < 10.0 && prevRpm > rpm
        val isCoastingInGear = isDecelerating && speed > 10.0

        val newBoostSum = session.boostSamples + boostBar
        val newBoostCount = session.boostSampleCount + 1
        val newAvgBoost = newBoostSum / newBoostCount
        val newRpmSampleCount = session.rpmSampleCount + 1

        val newSession = session.copy(
            rpmSamples = session.rpmSamples + rpm,
            rpmSampleCount = newRpmSampleCount,
            throttleSamples = session.throttleSamples + throttle,
            speedSamples = session.speedSamples + speed,
            avgRpm = if (newRpmSampleCount > 0) (session.rpmSamples + rpm) / newRpmSampleCount.toDouble() else rpm,
            avgThrottle = if (session.throttleSamples + throttle > 0) (session.throttleSamples + throttle) / 2.0 else throttle,
            avgSpeed = if (session.speedSamples + speed > 0) (session.speedSamples + speed) / 2.0 else speed,
            maxRpm = maxOf(session.maxRpm, rpm),
            maxThrottle = maxOf(session.maxThrottle, throttle),
            harshAccels = if (rpmDelta > 3000) session.harshAccels + 1 else session.harshAccels,
            harshBrakes = if (throttle < 10.0 && speed > 50.0 && prevRpm > rpm) session.harshBrakes + 1 else session.harshBrakes,
            boostSamples = newBoostSum,
            boostSampleCount = newBoostCount,
            avgBoostBar = newAvgBoost,
            maxBoostBar = maxOf(session.maxBoostBar, boostBar),
            optimalBoostTime = session.optimalBoostTime + if (boostBar in 0.4..0.7) 1 else 0,
            highBoostTime = session.highBoostTime + if (boostBar > 0.9 && throttle < 30.0) 1 else 0,
            coastingInGearSamples = session.coastingInGearSamples + if (isCoastingInGear) 1 else 0,
            deceleratingSamples = session.deceleratingSamples + if (isDecelerating) 1 else 0,
            rpmAbove4500Samples = session.rpmAbove4500Samples + if (rpm > 4500.0) 1 else 0,
            boostSumOfSquares = session.boostSumOfSquares + (boostBar * boostBar),
            wastegateDutySum = session.wastegateDutySum + wastegateDuty,
            wastegateSampleCount = session.wastegateSampleCount + 1,
            rpmRateSamples = session.rpmRateSamples + kotlin.math.abs(rpmDelta),
            rpmRateSampleCount = session.rpmRateSampleCount + 1
        )
        _driveSession.value = newSession
        updateDriveScore()
    }

    fun updateShiftLightConfig(config: com.canopobd.data.model.ShiftLightConfig) {
        _shiftLightConfig.value = config
        repository.saveShiftLightConfig(config)
    }

    fun setAlertConfig(config: com.canopobd.data.model.AlertConfig) {
        repository.setAlertConfig(config)
    }

    fun importCsvData(csvContent: String) {
        repository.importCsvData(csvContent)
    }

    fun clearImportedData() {
        repository.clearImportedData()
    }

    fun getFuelTrimAnalysis(): com.canopobd.data.model.FuelTrimAnalysis {
        return repository.getFuelTrimAnalysis()
    }

    fun startGPSTracking() {
        repository.startGPSTracking()
    }

    fun stopGPSTracking() {
        repository.stopGPSTracking()
    }

    fun getGPSTripHistory(): List<com.canopobd.data.model.GPSTrip> = repository.getGPSTripHistory()

    fun exportTripToGPX(): String = repository.exportCurrentTripToGPX()

    fun exportTripToKML(): String = repository.exportCurrentTripToKML()

    fun clearGPSTripHistory() {
        repository.clearGPSTripHistory()
    }

    fun startRemoteServer(port: Int = RemoteBridge.DEFAULT_PORT) {
        repository.startRemoteServer(port)
    }

    fun stopRemoteServer() {
        repository.stopRemoteServer()
    }

    fun startRecording() {
        repository.startRecording()
    }

    fun stopRecording() {
        repository.stopRecording()
    }

    fun setPollRate(rate: Long) {
        repository.setPollRate(rate)
    }

    fun setMeasurementUnit(unit: MeasurementUnit) {
        repository.setMeasurementUnit(unit)
    }

    fun setAutoReconnect(enabled: Boolean) {
        repository.setAutoReconnect(enabled)
    }

    fun setColorTheme(theme: ColorTheme) {
        repository.setColorTheme(theme)
    }

    fun setAppThemeMode(mode: AppThemeMode) = repository.setAppThemeMode(mode)

    fun setPrimaryGauges(ids: Set<String>) {
        repository.setPrimaryGauges(ids)
    }

    fun setPollMode(mode: PollMode) {
        repository.setPollMode(mode)
    }

    fun resetTrip() {
        repository.resetTrip()
    }

    fun getStoredVin(): String = repository.getStoredVin()

    fun getExportData(): String = repository.exportToCsv()

    fun clearRecordedData() {
        repository.clearRecordedData()
    }

    fun checkForUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            val update = UpdateChecker.checkForUpdate(context)
            if (update != null) {
                _availableUpdate.value = update
                withContext(Dispatchers.Main) {
                    _showUpdateDialog.value = true
                }
            }
            UpdateChecker.markChecked(context)
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun skipUpdateVersion() {
        _availableUpdate.value?.let {
            UpdateChecker.skipVersion(context, it.versionName)
        }
        _showUpdateDialog.value = false
    }

    // ========== Turbo Analysis ==========

    private fun startTurboAnalysisCollection() {
        viewModelScope.launch {
            obdData.collect { data ->
                if (data.rpm > 0) {
                    updateAllTurboMetrics(data)
                    updateEmissionsAnalyzers(data)
                }
            }
        }
    }

    // ========== Emissions Analyzers ==========

    private fun updateEmissionsAnalyzers(data: OBDData) {
        val dtcCodes = dtcResponse.value?.codes?.map { it.code } ?: emptyList()
        val history = _voltageHistory.value.toMutableList()
        if (data.batteryVoltage > 0) {
            history.add(data.batteryVoltage)
            if (history.size > 60) history.removeAt(0)
            _voltageHistory.value = history
        }

        val o2History = _o2VoltageHistory.value.toMutableList()
        if (data.o2VoltageB1S1 > 0) {
            o2History.add(data.o2VoltageB1S1)
            if (o2History.size > 60) o2History.removeAt(0)
            _o2VoltageHistory.value = o2History
        }

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
        val input = SecondaryAirAnalyzer.SAIInput(
            saActive = data.commandedEGR > 5 && data.rpm < 2000,
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

    fun analyzeReadiness(readinessBits: Int): EmissionsReadinessAnalyzer.ReadinessAnalysis {
        val dtcCodes = dtcResponse.value?.codes?.map { it.code } ?: emptyList()
        val input = EmissionsReadinessAnalyzer.ReadinessInput(
            readinessBits = readinessBits,
            activeDTCs = dtcCodes,
            engineRuntimeSeconds = obdData.value.runTime,
            coolantTemp = obdData.value.coolantTemp
        )
        val result = readinessAnalyzer.analyze(input)
        emissionsReadiness.value = result
        return result
    }

    private fun updateAllTurboMetrics(data: OBDData) {
        turboViewModel.updateFromOBDData(data, _carProfileState.value)

        fuelRailPressure.value = data.fuelRailPressure
        injectionQuantity.value = if (data.mafRate > 0 && data.rpm >= 100.0) {
            data.mafRate * 14.7 * 0.0007 / (data.rpm / 2.0) * 1000.0
        } else 0.0
    }

    fun analyzeBoost(
        actualKpa: Double,
        targetKpa: Double,
        calibration: AstraJ14TurboCalibration
    ): BoostAnalysis = turboViewModel.analyzeBoost(actualKpa, targetKpa, calibration)

    fun analyzeWastegate(
        dutyCycle: Double,
        rpm: Int,
        load: Double,
        calibration: AstraJ14TurboCalibration
    ): WastegateAnalysisResult = turboViewModel.analyzeWastegate(dutyCycle, rpm, load, calibration)

    fun calculateTurboHealth(
        boostAnalysis: BoostAnalysis,
        wastegateAnalysis: WastegateAnalysisResult,
        turboSpeed: Double,
        egt: Double,
        calibration: AstraJ14TurboCalibration
    ): TurboHealthResult = turboViewModel.calculateTurboHealth(boostAnalysis, wastegateAnalysis, turboSpeed, egt, calibration)

    fun calculateChainHealth(
        hasDtcP0016: Boolean,
        hasDtcP0017: Boolean,
        hasDtcP0340: Boolean,
        hasDtcP1345: Boolean,
        timingVariance: Double,
        rpmStability: Double
    ): ChainHealthResult {
        val hasDtcFault = hasDtcP0016 || hasDtcP0017 || hasDtcP0340 || hasDtcP1345

        var score = 100
        if (hasDtcP0016) score -= 30
        if (hasDtcP0017) score -= 25
        if (hasDtcP0340) score -= 25
        if (hasDtcP1345) score -= 20

        if (timingVariance > 5.0) score -= 15
        else if (timingVariance > 3.0) score -= 8

        if (rpmStability < 90) score -= 10

        score = score.coerceIn(0, 100)

        val correlation = if (timingVariance > 0) {
            (100.0 - timingVariance * 5.0).coerceIn(0.0, 100.0)
        } else {
            if (hasDtcFault) 30.0 else 95.0
        }

        val chainHealth = when {
            score >= 85 -> ChainHealth.GOOD
            score >= 50 -> ChainHealth.WARNING
            else -> ChainHealth.CRITICAL
        }

        val recommendation = when {
            hasDtcP0016 -> "P0016: Nockenwellen-Kurbelwellen-Korrelation prüfen - Steuerkette und Spanner ersetzen"
            hasDtcP0017 -> "P0017: Nockenwellen-Kurbelwellen-Korrelation Bank 1 Sensor B - Steuerkette prüfen"
            hasDtcP0340 -> "P0340: Nockenwellenpositionssensor prüfen - Steuerkettenspanner möglicherweise verschlissen"
            hasDtcP1345 -> "P1345: Nockenwellen-Kurbelwellen-Phasenabweichung - Sofortige Prüfung empfohlen"
            timingVariance > 5.0 -> "Hohe Timing-Variabilität - Kettenspanner-Vorwärtsverschleiß prüfen"
            else -> "Keine Maßnahmen erforderlich"
        }

        chainHealthScore.value = score.toDouble()
        chainTensionerHealth.value = chainHealth
        timingCorrelation.value = correlation

        return ChainHealthResult(
            healthScore = score,
            chainHealth = chainHealth,
            timingCorrelation = correlation,
            hasDtcFault = hasDtcFault,
            recommendation = recommendation
        )
    }

    // ========== PCV Analysis ==========

    fun analyzePCV(): PCVHealth {
        val data = repository.obdData.value
        val calibration = AstraJ14TurboCalibration.INSTANCE

        var score = 100
        var health = PCVHealth.GOOD

        if (data.intakePressure > 0 && data.barometricPressure > 0) {
            val vacuumKpa = data.barometricPressure - data.intakePressure
            if (data.rpm > 800 && data.throttle < 20 && vacuumKpa < 20) {
                score -= 30
                health = PCVHealth.WEAK
            }
        }

        if (data.shortTermFuelTrimB1 > 10 || data.longTermFuelTrimB1 > 10) {
            score -= 15
            if (score < 60) health = PCVHealth.WEAK
        }

        if (data.oilTemp > calibration.maxOilTempC * 0.9) {
            score -= 10
        }

        score = score.coerceIn(0, 100)
        if (score < 40) health = PCVHealth.FAILED

        pcvHealthScore.value = score.toDouble()
        pcvHealth.value = health
        return health
    }

    // ========== Fuel System ==========

    fun analyzeFuelSystem(): FuelSystemHealth {
        val data = repository.obdData.value
        val analyzer = FuelTrimAnalyzer()
        val status = analyzer.analyze(data.shortTermFuelTrimB1, data.longTermFuelTrimB1)

        val health = when {
            status.isLean -> FuelSystemHealth.LEAN
            status.isRich -> FuelSystemHealth.RICH
            else -> FuelSystemHealth.NORMAL
        }

        fuelSystemHealth.value = health
        fuelRailPressure.value = data.fuelRailPressure
        return health
    }

    // ========== Drive Style Detection ==========

    fun detectDriveStyle(
        throttle: Double, rpm: Double, speed: Double,
        load: Double, acceleratorPedalD: Double, throttleActuator: Double
    ): DriveStyle {
        val driveMode = DriveModeDetector.detectMode(throttle, rpm, speed, load, acceleratorPedalD, throttleActuator)
        val style = when (driveMode) {
            DriveMode.ECO -> DriveStyle.ECONOMICAL
            DriveMode.SPORT -> DriveStyle.AGGRESSIVE
            DriveMode.NORMAL -> DriveStyle.BALANCED
        }

        val session = _driveSession.value
        val totalSamples = session.rpmSamples.coerceAtLeast(1.0)
        val ecoRatio = (session.coastingInGearSamples + session.deceleratingSamples).toDouble() / totalSamples
        val sportRatio = session.rpmAbove4500Samples.toDouble() / totalSamples

        ecoScore.value = (ecoRatio * 100.0).coerceIn(0.0, 100.0)
        sportScore.value = (sportRatio * 100.0).coerceIn(0.0, 100.0)
        drivingStyle.value = style
        return style
    }

    // ========== Warning System ==========

    private fun startWarningMonitoring() {
        viewModelScope.launch {
            obdData.collect { data ->
                if (data.rpm > 0) {
                    val warnings = checkCriticalWarnings(data)
                    criticalWarnings.value = warnings
                }
            }
        }
    }

    fun checkCriticalWarnings(data: OBDData): List<VehicleWarning> {
        val calibration = AstraJ14TurboCalibration.INSTANCE
        val warnings = mutableListOf<VehicleWarning>()

        val baroKpa = if (data.barometricPressure > 0) data.barometricPressure else 100.0
        val absoluteBoostKpa = if (data.boostPressure > 0) data.boostPressure else data.intakePressure
        val boostBar = calibration.getBoostBar((absoluteBoostKpa - baroKpa).coerceAtLeast(0.0))

        if (boostBar > 1.35) {
            warnings.add(VehicleWarning(
                "TURBO_OVERBOOST", WarningPriority.CRITICAL,
                "Überladung!", "Ladedruck ${"%.2f".format(boostBar)} bar - sofort Pedal loslassen!"
            ))
        }

        val egt = data.egtBank1
        if (egt > 950) {
            warnings.add(VehicleWarning(
                "EGT_CRITICAL", WarningPriority.CRITICAL,
                "Abgastemperatur kritisch!", "EGT ${egt.toInt()}°C - Motorlast sofort reduzieren!"
            ))
        }

        if (egt > calibration.maxEgtC * 0.9) {
            warnings.add(VehicleWarning(
                "EGT_HIGH", WarningPriority.WARNING,
                "Abgastemperatur erhöht", "EGT ${egt.toInt()}°C - Last reduzieren empfohlen"
            ))
        }

        val oilPressureBar = data.turboOilPressure ?: 0.0
        if (data.rpm > 1000 && oilPressureBar > 0 && oilPressureBar < calibration.minOilPressureIdle) {
            warnings.add(VehicleWarning(
                "OIL_PRESSURE", WarningPriority.CRITICAL,
                "Öldruck niedrig!", "Öldruck ${"%.1f".format(oilPressureBar)} bar unter Mindestwert!"
            ))
        }

        val chainState = _timingChainState.value
        if (chainState.coldStartRattleDetected) {
            warnings.add(VehicleWarning(
                "CHAIN_RATTLE", WarningPriority.WARNING,
                "Steuerkette Rattern!", "Kaltstart-Rattern erkannt - Kettenspanner prüfen"
            ))
        }

        if (data.coolantTemp > calibration.maxCoolantTempC * 0.95) {
            warnings.add(VehicleWarning(
                "COOLANT_HIGH", WarningPriority.WARNING,
                "Kühlmitteltemperatur hoch", "${data.coolantTemp.toInt()}°C"
            ))
        }

        if (data.rpm > calibration.redlineRpm) {
            warnings.add(VehicleWarning(
                "RPM_REDLINE", WarningPriority.CRITICAL,
                "Redline überschritten!", "Drehzahl ${data.rpm.toInt()} rpm"
            ))
        }

        if (boostBar < calibration.normalBoostTargetBar * 0.5 && data.rpm > 2000 && data.throttle > 60) {
            warnings.add(VehicleWarning(
                "LOW_BOOST", WarningPriority.WARNING,
                "Unterladung", "Ladedruck ${"%.2f".format(boostBar)} bar bei ${data.rpm.toInt()} rpm"
            ))
        }

        return warnings
    }

    // ========== DTC Processing ==========

    fun processDTC(dtc: String): ProcessedDTC {
        val knownDTCs = mapOf(
            "P0016" to ProcessedDTC("P0016", "Nockenwellen-Kurbelwellen-Korrelation Bank 1 Sensor A", DTCSeverity.CRITICAL, "Steuerkette", "Steuerkette, Kettenspanner und Sensoren prüfen"),
            "P0017" to ProcessedDTC("P0017", "Nockenwellen-Kurbelwellen-Korrelation Bank 1 Sensor B", DTCSeverity.CRITICAL, "Steuerkette", "Steuerkette und Nockenwellenposition prüfen"),
            "P0100" to ProcessedDTC("P0100", "Luftmassenmesser (MAF) - Stromkreisfehler", DTCSeverity.WARNING, "Sensor", "MAF-Sensor prüfen und reinigen"),
            "P0101" to ProcessedDTC("P0101", "Luftmassenmesser (MAF) - Leistungsbereich", DTCSeverity.WARNING, "Sensor", "MAF-Sensor prüfen, Luftfilter wechseln"),
            "P0102" to ProcessedDTC("P0102", "Luftmassenmesser (MAF) - Signaleingang niedrig", DTCSeverity.WARNING, "Sensor", "MAF-Sensor reinigen oder ersetzen"),
            "P0103" to ProcessedDTC("P0103", "Luftmassenmesser (MAF) - Signaleingang hoch", DTCSeverity.WARNING, "Sensor", "MAF-Sensor prüfen"),
            "P0116" to ProcessedDTC("P0116", "Kühlmitteltemperatur-Sensor - Plausibilitätsfehler", DTCSeverity.WARNING, "Sensor", "Temperatursensor prüfen"),
            "P0117" to ProcessedDTC("P0117", "Kühlmitteltemperatur-Sensor - Signaleingang niedrig", DTCSeverity.WARNING, "Sensor", "Kühlmitteltemperatursensor ersetzen"),
            "P0234" to ProcessedDTC("P0234", "Turbolader-Überladung (Overboost)", DTCSeverity.CRITICAL, "Turbo", "Wastegate und Ladedruckregelung prüfen"),
            "P0235" to ProcessedDTC("P0235", "Turbolader-Überladungs-Sensor A", DTCSeverity.WARNING, "Turbo", "Ladedrucksensor prüfen"),
            "P0340" to ProcessedDTC("P0340", "Nockenwellenpositionssensor - Stromkreisfehler", DTCSeverity.CRITICAL, "Sensor", "Sensor und Verkabelung prüfen"),
            "P0341" to ProcessedDTC("P0341", "Nockenwellenpositionssensor - Leistungsbereich", DTCSeverity.CRITICAL, "Sensor", "Sensor prüfen, Steuerkette inspizieren"),
            "P1100" to ProcessedDTC("P1100", "PCV-System (Crankcase Ventilation) Störung", DTCSeverity.WARNING, "PCV", "PCV-Ventil und Zylinderkopfhaube prüfen"),
            "P1101" to ProcessedDTC("P1101", "Ansaugluftsystem - Luftleck erkannt", DTCSeverity.WARNING, "Ansaugung", "Saugrohr und Dichtungen auf Luftleck prüfen"),
            "P1345" to ProcessedDTC("P1345", "Nockenwellen-Kurbelwellen-Phasenabweichung", DTCSeverity.CRITICAL, "Steuerkette", "Steuerkette und Kettenspanner ersetzen"),
            "P0171" to ProcessedDTC("P0171", "System zu mager (Bank 1)", DTCSeverity.WARNING, "Kraftstoff", "MAF, O2-Sensor, Kraftstoffdruck und Luftleck prüfen"),
            "P0172" to ProcessedDTC("P0172", "System zu fett (Bank 1)", DTCSeverity.WARNING, "Kraftstoff", "Einspritzventile, Kraftstoffdruck und O2-Sensor prüfen"),
            "P0420" to ProcessedDTC("P0420", "Katalysator-Wirkung unter Schwellenwert (Bank 1)", DTCSeverity.WARNING, "Abgas", "Katalysator prüfen, O2-Sensoren messen"),
            "P0562" to ProcessedDTC("P0562", "Systemspannung niedrig", DTCSeverity.INFO, "Elektrik", "Batterie und Lichtmaschine prüfen"),
            "P0130" to ProcessedDTC("P0130", "O2-Sensor Stromkreis (Bank 1 Sensor 1)", DTCSeverity.WARNING, "Sensor", "O2-Sensor prüfen und ggf. ersetzen")
        )

        val upperCode = dtc.trim().uppercase()
        return knownDTCs[upperCode] ?: ProcessedDTC(
            code = upperCode,
            description = "Astra J 1.4T DTC: $upperCode",
            severity = DTCSeverity.INFO,
            category = "Sonstige",
            recommendation = "Herstellerspezifischen Diagnose-Code nachschlagen"
        )
    }

    fun processAllDTCs() {
        val response = dtcResponse.value ?: return
        val allCodes = response.codes + response.pendingCodes
        val processed = allCodes.map { processDTC(it.code) }
        _processedDTCs.value = processed
        _criticalDTCs.value = processed.filter { it.severity == DTCSeverity.CRITICAL }
        _warningDTCs.value = processed.filter { it.severity == DTCSeverity.WARNING }
        _infoDTCs.value = processed.filter { it.severity == DTCSeverity.INFO || it.severity == DTCSeverity.PERFORMANCE }
    }

    // ========== Mode 22 Data Handling ==========

    fun parseMode22Response(pid: String, data: ByteArray): Mode22Data? {
        if (data.size < 2) return null
        val rawValue = ((data[0].toInt() and 0xFF) * 256 + (data[1].toInt() and 0xFF)).toDouble()

        return when (pid) {
            "0174" -> Mode22Data(pid, rawValue, "rpm", data)
            "010C" -> Mode22Data(pid, rawValue / 4.0, "rpm", data)
            "010D" -> Mode22Data(pid, rawValue, "km/h", data)
            "0104" -> Mode22Data(pid, rawValue * 100.0 / 255.0, "%", data)
            "0111" -> Mode22Data(pid, rawValue * 100.0 / 255.0, "%", data)
            "0105" -> Mode22Data(pid, rawValue - 40.0, "°C", data)
            "010F" -> Mode22Data(pid, rawValue - 40.0, "°C", data)
            "0170" -> Mode22Data(pid, rawValue * 0.03125, "kPa", data)
            "0171" -> Mode22Data(pid, rawValue * 100.0 / 255.0, "%", data)
            "0172" -> Mode22Data(pid, rawValue * 100.0 / 255.0, "%", data)
            "0178" -> Mode22Data(pid, rawValue / 10.0 - 40.0, "°C", data)
            "0179" -> Mode22Data(pid, rawValue / 10.0 - 40.0, "°C", data)
            "0177" -> Mode22Data(pid, rawValue - 40.0, "°C", data)
            "010B" -> Mode22Data(pid, rawValue, "kPa", data)
            "010A" -> Mode22Data(pid, rawValue * 3.0, "kPa", data)
            "0110" -> Mode22Data(pid, rawValue / 100.0, "g/s", data)
            "015C" -> Mode22Data(pid, rawValue - 40.0, "°C", data)
            "012A" -> Mode22Data(pid, rawValue * 0.079, "kPa", data)
            "0142" -> Mode22Data(pid, rawValue / 1000.0, "V", data)
            "0133" -> Mode22Data(pid, rawValue, "kPa", data)
            else -> Mode22Data(pid, rawValue, "", data)
        }
    }

    fun requestMode22Data(pid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rawPid = if (pid.startsWith("22")) pid.drop(2) else pid
                _mode22DataCache.value = _mode22DataCache.value + (rawPid to Mode22Data(
                    pid = rawPid,
                    value = 0.0,
                    unit = "",
                    timestamp = System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                _mode22DataCache.value = _mode22DataCache.value - pid
            }
        }
    }

    fun discoverMode22Support(): List<String> {
        val knownMode22Pids = AstraJ14TurboCalibration.SUPPORTED_TURBO_PIDS.map { it.code }
        val discovered = mutableListOf<String>()
        for (pid in knownMode22Pids) {
            val rawPid = if (pid.startsWith("01")) pid.drop(2) else pid
            discovered.add(rawPid)
        }
        _supportedMode22Pids.value = discovered
        return discovered
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(application) as T
        }
    }
}
