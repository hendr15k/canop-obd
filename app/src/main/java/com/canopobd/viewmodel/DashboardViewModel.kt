package com.canopobd.viewmodel

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.canopobd.bluetooth.RemoteBridge
import com.canopobd.data.domain.BatteryHealthAnalyzer
import com.canopobd.data.domain.DriveMode
import com.canopobd.data.domain.DriveModeDetector
import com.canopobd.data.domain.EGRHealthAnalyzer
import com.canopobd.data.domain.EmissionsReadinessAnalyzer
import com.canopobd.data.domain.EVAPSystemAnalyzer
import com.canopobd.data.domain.LambdaO2SensorAnalyzer
import com.canopobd.data.domain.SecondaryAirAnalyzer
import com.canopobd.data.domain.OilConditionMonitor
import com.canopobd.data.domain.PCVMonitor
import com.canopobd.data.domain.LambdaBalanceAnalyzer
import com.canopobd.data.domain.FuelConsumptionAnalyzer
import com.canopobd.data.domain.M32GearboxMonitor
import com.canopobd.data.domain.ChainTensionerAnalyzer
import com.canopobd.data.domain.EGTMonitor
import com.canopobd.data.domain.CoolantSystemHealth
import com.canopobd.data.domain.TurboSpoolAnalyzer
import com.canopobd.data.domain.TurboEfficiencyAnalyzer
import com.canopobd.data.domain.BoostLeakDetector
import com.canopobd.data.domain.WastegateHealthAnalyzer
import com.canopobd.data.domain.SensorHealthMonitor
import com.canopobd.data.domain.OilHealthPredictor
import com.canopobd.data.domain.ValidationResult
import com.canopobd.ui.comfort.ComfortCommand
import com.canopobd.notifications.LiveAlertNotifier
import com.canopobd.notifications.MaintenanceNotificationManager
import com.canopobd.data.domain.DriveStyleAnalyzer
import com.canopobd.data.domain.DrivingEfficiencyScorer
import com.canopobd.data.domain.FuelSystemAnalyzer
import com.canopobd.data.repository.CANRepository

import android.util.Log
import com.canopobd.data.model.*
import com.canopobd.data.local.TripEntity
import com.canopobd.data.local.MaintenanceEntity
import com.canopobd.data.repository.OBDRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PSI_TO_KPA = 6.89476
private const val LOW_TIRE_PRESSURE_PSI = 28.0

class DashboardViewModel private constructor(
    application: Application
) : ViewModel() {

    private val context: Application = application
    private val notificationManager = MaintenanceNotificationManager(application)
    private val liveAlertNotifier = LiveAlertNotifier(application)
    private val viewModelPrefs = application.getSharedPreferences("dashboard_vm", Context.MODE_PRIVATE)

    init {
        notificationManager.createNotificationChannel()
        liveAlertNotifier.createChannel()
    }

    companion object {
        private const val TAG = "DashboardVM"
    }

    private val turboViewModel = TurboViewModel(application)
    private val safetyViewModel = SafetyViewModel(application)
    private val ecoScoreViewModel = EcoScoreViewModel(application)
    private val performanceViewModel = PerformanceViewModel(application)

    private val analyzerManager = AnalyzerManager()
    private val comfortController by lazy { ComfortController(viewModelScope) { repository.sendRawCommand(it) } }
    private val dtcProcessor = DTCProcessor()

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val repository = OBDRepository(context, bluetoothManager?.adapter)
    private var canRepository: CANRepository? = null
    private var canInitializationJob: Job? = null
    private var previousRpmForDriveScore = 0.0

    val connectionState: StateFlow<OBDConnectionState> = repository.connectionState
    val climateReading = repository.climateReading
    val tpmsReading = repository.tpmsReading
    val tcmReading = repository.tcmReading
    val ecmReading = repository.ecmReading
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
    val emulatorMode: StateFlow<Boolean> = repository.emulatorMode

    val currentLocation = repository.currentLocation
    val isGPSTracking = repository.isGPSTracking
    val currentTrip = repository.currentTrip
    val tripHistory = repository.tripHistory
    val tripHistoryEntities: StateFlow<List<TripEntity>> = repository.tripHistoryEntities
    val trendHistory = repository.trendHistory

    val readinessMonitor = repository.readinessMonitor
    val detectedProtocol = repository.detectedProtocol
    val supportedPIDs = repository.supportedPIDs
    val alertConfig = repository.alertConfig
    val activeAlerts = repository.activeAlerts
    val freezeFrames = repository.freezeFrames
    val importedData = repository.importedData

    init {
        viewModelScope.launch {
            // Hinweis: kein distinctUntilChanged() noetig/verboten auf
            // StateFlow (Operator Fusion) - StateFlow emitiert nur bei
            // geaenderten Listen; Spam-Schutz steckt in notifyChanges().
            repository.activeAlerts.collect { alerts ->
                liveAlertNotifier.notifyChanges(alerts)
            }
        }
    }

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

    private val _currentKm = MutableStateFlow(viewModelPrefs.getInt("odometer_km", 0))
    val currentKm: StateFlow<Int> = _currentKm.asStateFlow()

    @Volatile private var lastMaintenanceCheckTime = 0L
    private val sessionNotifiedMaintenance = java.util.concurrent.ConcurrentHashMap<String, Pair<Int, MaintenanceNotificationManager.Urgency>>()

    private val _fuelEconomyData = MutableStateFlow(com.canopobd.data.model.FuelEconomyData())
    val fuelEconomyData: StateFlow<com.canopobd.data.model.FuelEconomyData> = _fuelEconomyData.asStateFlow()

    val performanceTestState: StateFlow<com.canopobd.data.model.PerformanceTestState> get() = performanceViewModel.performanceTestState
    val currentAccelerationRun: StateFlow<com.canopobd.data.model.AccelerationRun?> get() = performanceViewModel.currentAccelerationRun
    val gpsSpeedForTest: StateFlow<Double> get() = performanceViewModel.gpsSpeedForTest

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

    val driveScore: StateFlow<com.canopobd.data.model.DriveScore> get() = performanceViewModel.driveScore
    val driveSession: StateFlow<com.canopobd.data.model.DriveSession> get() = performanceViewModel.driveSession

    private val _shiftLightConfig = MutableStateFlow(com.canopobd.data.model.ShiftLightConfig())
    val shiftLightConfig: StateFlow<com.canopobd.data.model.ShiftLightConfig> = _shiftLightConfig.asStateFlow()

    private val _carProfileState = MutableStateFlow(com.canopobd.data.model.CarProfile.default())
    val carProfile: StateFlow<com.canopobd.data.model.CarProfile> = _carProfileState.asStateFlow()
    private val _timingChainState = MutableStateFlow(com.canopobd.data.model.TimingChainState())
    val timingChainState: StateFlow<com.canopobd.data.model.TimingChainState> = _timingChainState.asStateFlow()
    private val rpmSampleBuffer = java.util.Collections.synchronizedList(mutableListOf<Double>())
    private val timingAdvanceBuffer = java.util.Collections.synchronizedList(mutableListOf<Double>())

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

    private val _showQuickActions = MutableStateFlow(false)
    val showQuickActions: StateFlow<Boolean> = _showQuickActions.asStateFlow()

    private val _showVehicleProfileManager = MutableStateFlow(false)
    val showVehicleProfileManager: StateFlow<Boolean> = _showVehicleProfileManager.asStateFlow()

    private val _currentVehicleProfile = MutableStateFlow<com.canopobd.data.model.VehicleProfile?>(null)
    val currentVehicleProfile: StateFlow<com.canopobd.data.model.VehicleProfile?> = _currentVehicleProfile.asStateFlow()

    private val _showCodingDialog = MutableStateFlow(false)
    val showCodingDialog: StateFlow<Boolean> = _showCodingDialog.asStateFlow()
    private val _codingResult = MutableStateFlow<AstraJCodingModels.CodingResult?>(null)
    val codingResult: StateFlow<AstraJCodingModels.CodingResult?> = _codingResult.asStateFlow()
    private val _codingInProgress = MutableStateFlow(false)

    private val _showTPMSDialog = MutableStateFlow(false)
    val showTPMSDialog: StateFlow<Boolean> = _showTPMSDialog.asStateFlow()

    private val _tpmsData = MutableStateFlow<List<com.canopobd.ui.tpms.TireData>>(emptyList())
    val tpmsData: StateFlow<List<com.canopobd.ui.tpms.TireData>> = _tpmsData.asStateFlow()

    private val _showClimateControl = MutableStateFlow(false)
    val showClimateControl: StateFlow<Boolean> = _showClimateControl.asStateFlow()

    val climateState: StateFlow<com.canopobd.ui.climate.ClimateState> get() = comfortController.climateState

    private val _showWindowControl = MutableStateFlow(false)
    val showWindowControl: StateFlow<Boolean> = _showWindowControl.asStateFlow()

    val windowState: StateFlow<com.canopobd.data.domain.WindowState> get() = comfortController.windowState

    val windowChildLock: StateFlow<Boolean> get() = comfortController.windowChildLock

    val windowIsMoving: StateFlow<Boolean> get() = comfortController.windowIsMoving

    val windowExpressMode: StateFlow<Boolean> get() = comfortController.windowExpressMode

    val codingInProgress: StateFlow<Boolean> = _codingInProgress.asStateFlow()

    private val _devices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val devices: StateFlow<List<BluetoothDeviceInfo>> = _devices.asStateFlow()

    /**
     * The last successfully-connected Bluetooth device (persisted in SharedPreferences).
     * Null if no device has ever been connected or if the persisted address is no longer paired.
     * Re-evaluated every time [refreshDevices] runs so the dashboard "Connect" button knows
     * whether it can auto-reconnect to the last adapter or must open the device picker.
     */
    val lastDevice: StateFlow<BluetoothDeviceInfo?> = _devices
        .map { list -> list.firstOrNull { it.address == repository.getLastDevice() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
    private val _chainHealthScore = MutableStateFlow(100.0)
    val chainHealthScore: StateFlow<Double> = _chainHealthScore.asStateFlow()
    private val _chainTensionerHealth = MutableStateFlow(ChainHealth.UNKNOWN)
    val chainTensionerHealth: StateFlow<ChainHealth> = _chainTensionerHealth.asStateFlow()
    private val _timingCorrelation = MutableStateFlow(0.0)
    val timingCorrelation: StateFlow<Double> = _timingCorrelation.asStateFlow()

    // PCV State
    private val _pcvHealthScore = MutableStateFlow(100.0)
    val pcvHealthScore: StateFlow<Double> = _pcvHealthScore.asStateFlow()
    private val _pcvHealth = MutableStateFlow(PCVHealth.UNKNOWN)
    val pcvHealth: StateFlow<PCVHealth> = _pcvHealth.asStateFlow()
    private val _oilConsumptionRate = MutableStateFlow(0.0)
    val oilConsumptionRate: StateFlow<Double> = _oilConsumptionRate.asStateFlow()

    // Fuel System State
    private val _fuelRailPressure = MutableStateFlow(0.0)
    val fuelRailPressure: StateFlow<Double> = _fuelRailPressure.asStateFlow()
    private val _injectionQuantity = MutableStateFlow(0.0)
    val injectionQuantity: StateFlow<Double> = _injectionQuantity.asStateFlow()
    private val _fuelSystemHealth = MutableStateFlow(FuelSystemHealth.UNKNOWN)
    val fuelSystemHealth: StateFlow<FuelSystemHealth> = _fuelSystemHealth.asStateFlow()

    // Drive Style State
    private val _ecoScore = MutableStateFlow(0.0)
    val ecoScore: StateFlow<Double> = _ecoScore.asStateFlow()
    private val _sportScore = MutableStateFlow(0.0)
    val sportScore: StateFlow<Double> = _sportScore.asStateFlow()
    private val _drivingStyle = MutableStateFlow(DriveStyle.ECONOMICAL)
    val drivingStyle: StateFlow<DriveStyle> = _drivingStyle.asStateFlow()

    // Emissions Analyzer State - delegated to analyzerManager
    val batteryHealth: StateFlow<BatteryStatus> get() = analyzerManager.batteryHealth
    val batteryHealthScore: StateFlow<Int> get() = analyzerManager.batteryHealthScore
    val batteryAnalysis: StateFlow<BatteryHealthAnalyzer.BatteryAnalysis?> get() = analyzerManager.batteryAnalysis

    val egrHealth: StateFlow<EGRHealth> get() = analyzerManager.egrHealth
    val egrAnalysis: StateFlow<EGRHealthAnalyzer.EGRAnalysis?> get() = analyzerManager.egrAnalysis

    val evapStatus: StateFlow<EVAPStatus> get() = analyzerManager.evapStatus
    val evapAnalysis: StateFlow<EVAPSystemAnalyzer.EVAPAnalysis?> get() = analyzerManager.evapAnalysis

    val saiStatus: StateFlow<SAIStatus> get() = analyzerManager.saiStatus
    val saiAnalysis: StateFlow<SecondaryAirAnalyzer.SAIAnalysis?> get() = analyzerManager.saiAnalysis

    val lambdaAnalysis: StateFlow<LambdaO2SensorAnalyzer.LambdaAnalysis?> get() = analyzerManager.lambdaAnalysis

    val emissionsReadiness: StateFlow<EmissionsReadinessAnalyzer.ReadinessAnalysis?> get() = analyzerManager.emissionsReadiness

    // Extended Analyzer States - delegated to analyzerManager
    val oilConditionResult: StateFlow<OilConditionMonitor.OilAnalysis> get() = analyzerManager.oilConditionResult
    val pcvResult: StateFlow<PCVMonitor.PCVAnalysis> get() = analyzerManager.pcvResult
    val lambdaBalanceData: StateFlow<LambdaBalanceAnalyzer.LambdaBalance> get() = analyzerManager.lambdaBalanceData
    val fuelConsumptionData: StateFlow<FuelConsumptionAnalyzer.FuelConsumptionData> get() = analyzerManager.fuelConsumptionData
    val gearboxResult: StateFlow<M32GearboxMonitor.GearboxAnalysis> get() = analyzerManager.gearboxResult
    val chainTensionerResult: StateFlow<ChainTensionerAnalyzer.ChainTensionerAnalysis> get() = analyzerManager.chainTensionerResult
    val egtResult: StateFlow<EGTMonitor.EGTAnalysis> get() = analyzerManager.egtResult
    val coolantResult: StateFlow<CoolantSystemHealth.CoolantAnalysis> get() = analyzerManager.coolantResult
    val oilHealthPrediction: StateFlow<OilHealthPredictor.OilHealthPredictionResult> get() = analyzerManager.oilHealthPrediction
    val sensorValidationResult: StateFlow<ValidationResult> get() = analyzerManager.sensorValidationResult
    val turboSpoolResult: StateFlow<TurboSpoolAnalyzer.SpoolAnalysis> get() = analyzerManager.turboSpoolResult
    val turboEfficiencyResult: StateFlow<TurboEfficiencyAnalyzer.TurboEfficiencyAnalysis> get() = analyzerManager.turboEfficiencyResult
    val boostLeakResult: StateFlow<BoostLeakDetector.BoostLeakAnalysis> get() = analyzerManager.boostLeakResult
    val wastegateResult: StateFlow<WastegateHealthAnalyzer.WastegateAnalysis> get() = analyzerManager.wastegateResult
    val sensorHealthSummary: StateFlow<SensorHealthMonitor.SensorHealthSummary> get() = analyzerManager.sensorHealthSummary
    val driveStyleResult: StateFlow<DriveStyleAnalyzer.DriveStyleAnalysis> get() = analyzerManager.driveStyleResult
    val drivingEfficiencyResult: StateFlow<DrivingEfficiencyScorer.EfficiencyScore> get() = analyzerManager.drivingEfficiencyResult
    val fuelSystemResult: StateFlow<FuelSystemAnalyzer.FuelSystemAnalysis> get() = analyzerManager.fuelSystemResult

    val extendedAnalyzerData: StateFlow<AnalyzerManager.ExtendedAnalyzerSummary> get() = analyzerManager.extendedAnalyzerData

    // Warning System
    private val _criticalWarnings = MutableStateFlow<List<VehicleWarning>>(emptyList())
    val criticalWarnings: StateFlow<List<VehicleWarning>> = _criticalWarnings.asStateFlow()

    // DTC Processing
    val processedDTCs: StateFlow<List<ProcessedDTC>> get() = dtcProcessor.processedDTCs
    val criticalDTCs: StateFlow<List<ProcessedDTC>> get() = dtcProcessor.criticalDTCs
    val warningDTCs: StateFlow<List<ProcessedDTC>> get() = dtcProcessor.warningDTCs
    val infoDTCs: StateFlow<List<ProcessedDTC>> get() = dtcProcessor.infoDTCs

    // Mode 22 State
    private val _supportedMode22Pids = MutableStateFlow<List<String>>(emptyList())
    val supportedMode22Pids: StateFlow<List<String>> = _supportedMode22Pids.asStateFlow()
    private val _mode22DataCache = MutableStateFlow<Map<String, Mode22Data>>(emptyMap())
    val mode22DataCache: StateFlow<Map<String, Mode22Data>> = _mode22DataCache.asStateFlow()

    private val _turboAnalysisJob = MutableStateFlow<Job?>(null)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadMaintenanceItems()
            val shiftConfig = repository.loadShiftLightConfig()
            val profile = repository.loadCarProfile()
            withContext(Dispatchers.Main) {
                _maintenanceItems.value = items
                _shiftLightConfig.value = shiftConfig
                _carProfileState.value = profile
                _isInitialized.value = true
            }
        }
        if (_permissionsGranted.value) {
            refreshDevices()
        }
        checkForUpdate()
        startTurboAnalysisCollection()
        startWarningMonitoring()
        viewModelScope.launch {
            dtcResponse.collect { response ->
                safetyViewModel.updateFromSafetyDTCs(response)
                dtcProcessor.processAllDTCs(response)
            }
        }
    }

    fun onPermissionsGranted() {
        _permissionsGranted.value = true
        refreshDevices()
        val addr = repository.getLastDevice()
        if (addr != null && repository.autoReconnect.value) {
            viewModelScope.launch { connect(addr) }
        }
    }

    fun refreshDevices() {
        _devices.value = repository.getPairedDevices()
    }

    fun connect(deviceAddress: String) {
        _showDevicePicker.value = false
        repository.connect(deviceAddress)
        initializeCANRepository()
    }

    /**
     * One-tap reconnect to the most recently-used paired device.
     *
     * Behaviour:
     *  - If [lastDevice] is available (was previously connected AND still paired) → connects directly.
     *  - Otherwise → falls back to opening the device picker.
     *
     * Returns true if a direct connection was triggered, false if the picker was opened instead.
     */
    fun connectLastDevice(): Boolean {
        val target = lastDevice.value
        return if (target != null) {
            connect(target.address)
            true
        } else {
            toggleDevicePicker()
            false
        }
    }

    private fun initializeCANRepository() {
        canInitializationJob?.cancel()
        val conn = repository.connection
        if (conn == null) return

        canInitializationJob = viewModelScope.launch(Dispatchers.IO) {
            // Die aktuelle Verbindung ist bereits Connected -> sofort
            // initialisieren. (Frueheres dropWhile{Connected}.first{Connected}
            // hat das aktuelle Connected verworfen und auf ein *naechstes*
            // gewartet -> CAN-Init hing bei bestehender Verbindung.)
            val current = repository.connectionState.value
            if (current !is OBDConnectionState.Connected) {
                repository.connectionState.first { it is OBDConnectionState.Connected }
            }

            if (!conn.isConnected.value || canRepository != null) return@launch

            val candidate = CANRepository(conn)
            val result = candidate.initialize()
            if (result.isFailure) {
                Log.w(TAG, "CAN initialization failed: ${result.exceptionOrNull()?.message}")
                candidate.shutdown()
                return@launch
            }

            canRepository = candidate
            candidate.startCANMonitoring { canMessage ->
                repository.processCANMessage(canMessage.canId, canMessage.data)
            }
        }
    }

    fun disconnect() {
        checkMaintenanceNotifications()
        sessionNotifiedMaintenance.clear()
        lastMaintenanceCheckTime = 0L
        canInitializationJob?.cancel()
        canInitializationJob = null
        canRepository?.stopCANMonitoring()
        canRepository?.shutdown()
        canRepository = null
        repository.requestDisconnect()
    }

    private fun checkMaintenanceNotifications() {
        val items = repository.loadMaintenanceItems()
        val entities = items.map { item ->
            MaintenanceEntity(
                type = item.type.name,
                lastServiceKm = item.lastServiceKm,
                intervalKm = item.intervalKm,
                lastServiceDate = item.lastServiceDate
            )
        }
        val currentKm = _currentKm.value
        val reminders = notificationManager.checkMaintenanceReminders(entities, currentKm)
        if (reminders.isNotEmpty()) {
            val toShow = reminders.filter { reminder ->
                val prev = sessionNotifiedMaintenance[reminder.type]
                val shouldShow = when {
                    prev == null -> true
                    reminder.urgency.ordinal > prev.second.ordinal -> true
                    prev.first - reminder.remainingKm > 500 -> true
                    else -> false
                }
                if (shouldShow) {
                    sessionNotifiedMaintenance[reminder.type] = Pair(reminder.remainingKm, reminder.urgency)
                }
                shouldShow
            }
            if (toShow.isNotEmpty()) {
                notificationManager.showAllReminders(toShow)
            }
        }
    }

    fun toggleDevicePicker() {
        _showDevicePicker.value = !_showDevicePicker.value
        if (_showDevicePicker.value) {
            refreshDevices()
        }
    }

    fun toggleDTCDialog() {
        _showDTCDialog.value = !_showDTCDialog.value
        if (_showDTCDialog.value) {
            repository.readDTCs()
        }
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
        if (_showReadiness.value) {
            repository.readReadinessMonitor()
        }
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
        performanceViewModel.startPerformanceTest(testType, currentLocation)
    }

    fun stopPerformanceTest() {
        performanceViewModel.stopPerformanceTest()
    }

    fun updatePerformanceTestStatus(message: String) {
    }

    fun togglePowerCalculator() {
        _showPowerCalculator.value = !_showPowerCalculator.value
        if (_showPowerCalculator.value) {
            viewModelScope.launch {
                val d = repository.obdData.value
                _powerCalculation.value = com.canopobd.data.model.PowerCalculation.calculate(d.mafRate, d.rpm, d.intakeTemp)
            }
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
    fun toggleQuickActions() { _showQuickActions.value = !_showQuickActions.value }
    fun toggleVehicleProfileManager() { _showVehicleProfileManager.value = !_showVehicleProfileManager.value }

    fun loadVehicleProfile(savedProfile: com.canopobd.ui.profile.SavedProfile) {
        val vehicleId = when {
            savedProfile.vehicle.contains("1.4 Turbo") -> "astra_j_2012_14t"
            else -> "astra_j_2012_14t"
        }
        val profile = com.canopobd.data.model.VehicleProfiles.fromId(vehicleId)
        if (profile != null) {
            _currentVehicleProfile.value = profile
            val carProfile = com.canopobd.data.model.CarProfile.fromVehicleProfile(profile)
            if (carProfile != null) {
                selectCarProfile(carProfile)
            }
        }
        _showVehicleProfileManager.value = false
    }

    fun toggleCodingDialog() { _showCodingDialog.value = !_showCodingDialog.value }

    fun toggleTPMSDialog() {
        _showTPMSDialog.value = !_showTPMSDialog.value
        if (_showTPMSDialog.value) {
            syncTPMSFromRepository()
        }
    }
    fun toggleClimateControl() { _showClimateControl.value = !_showClimateControl.value }

    private fun syncTPMSFromRepository() {
        repository.readTPMS()
        val tpms = repository.tpmsReading.value
        if (tpms.frontLeftPSI > 0 || tpms.frontRightPSI > 0 ||
            tpms.rearLeftPSI > 0 || tpms.rearRightPSI > 0) {
            _tpmsData.value = buildTireDataList(
                flPressure = tpms.frontLeftPSI, flTemp = tpms.frontLeftTemp, flBattery = 0,
                frPressure = tpms.frontRightPSI, frTemp = tpms.frontRightTemp, frBattery = 0,
                rlPressure = tpms.rearLeftPSI, rlTemp = tpms.rearLeftTemp, rlBattery = 0,
                rrPressure = tpms.rearRightPSI, rrTemp = tpms.rearRightTemp, rrBattery = 0
            )
        } else {
            syncTPMSFromSafety()
        }
    }

    private fun syncTPMSFromSafety() {
        val safetySummary = safetyViewModel.safetySummary.value
        val tpms = safetySummary.tpmsData
        _tpmsData.value = buildTireDataList(
            flPressure = tpms.frontLeftPressure, flTemp = tpms.frontLeftTemp,
            flBattery = if (tpms.frontLeftPressure > 0) 100 else 0,
            frPressure = tpms.frontRightPressure, frTemp = tpms.frontRightTemp,
            frBattery = if (tpms.frontRightPressure > 0) 100 else 0,
            rlPressure = tpms.rearLeftPressure, rlTemp = tpms.rearLeftTemp,
            rlBattery = if (tpms.rearLeftPressure > 0) 100 else 0,
            rrPressure = tpms.rearRightPressure, rrTemp = tpms.rearRightTemp,
            rrBattery = if (tpms.rearRightPressure > 0) 100 else 0
        )
    }

    private fun buildTireDataList(
        flPressure: Double, flTemp: Int, flBattery: Int,
        frPressure: Double, frTemp: Int, frBattery: Int,
        rlPressure: Double, rlTemp: Int, rlBattery: Int,
        rrPressure: Double, rrTemp: Int, rrBattery: Int
    ): List<com.canopobd.ui.tpms.TireData> {
        val psiToKpa = PSI_TO_KPA
        val lowThresholdPsi = LOW_TIRE_PRESSURE_PSI
        return listOf(
            com.canopobd.ui.tpms.TireData(
                "Vorne Links", (flPressure * psiToKpa).toFloat(), flTemp,
                isLow = flPressure > 0 && flPressure < lowThresholdPsi, sensorBattery = flBattery
            ),
            com.canopobd.ui.tpms.TireData(
                "Vorne Rechts", (frPressure * psiToKpa).toFloat(), frTemp,
                isLow = frPressure > 0 && frPressure < lowThresholdPsi, sensorBattery = frBattery
            ),
            com.canopobd.ui.tpms.TireData(
                "Hinten Links", (rlPressure * psiToKpa).toFloat(), rlTemp,
                isLow = rlPressure > 0 && rlPressure < lowThresholdPsi, sensorBattery = rlBattery
            ),
            com.canopobd.ui.tpms.TireData(
                "Hinten Rechts", (rrPressure * psiToKpa).toFloat(), rrTemp,
                isLow = rrPressure > 0 && rrPressure < lowThresholdPsi, sensorBattery = rrBattery
            )
        )
    }

    fun onTPMSReset() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendRawCommand("310302")
        }
    }

    fun onSendClimateCommand(command: com.canopobd.ui.climate.ClimateCommand) {
        comfortController.sendClimateCommand(command)
    }

    fun updateClimateState(state: com.canopobd.ui.climate.ClimateState) {
        comfortController.updateClimateState(state)
    }

    fun toggleWindowControl() { _showWindowControl.value = !_showWindowControl.value }

    fun updateWindowState(state: com.canopobd.data.domain.WindowState) {
        comfortController.updateWindowState(state)
    }

    fun toggleWindowChildLock() { comfortController.toggleWindowChildLock() }

    fun toggleWindowExpressMode() { comfortController.toggleWindowExpressMode() }

    fun onSendWindowPosition(target: com.canopobd.data.domain.WindowTarget, percent: Int) {
        comfortController.sendWindowPosition(target, percent)
    }

    fun onSendWindowVentilateAll() {
        comfortController.sendWindowVentilateAll()
    }

    fun onSendSunroofCommand(action: com.canopobd.data.domain.WindowAction) {
        comfortController.sendSunroofCommand(action)
    }

    fun onSendWindowCommand(command: com.canopobd.data.domain.WindowAction) {
        comfortController.sendWindowCommand(command)
    }

    fun pollWindowStatus() {
        comfortController.pollWindowStatus { repository.sendRawCommand(it) }
    }

    fun onSendBCMCommand(command: ComfortCommand) {
        comfortController.sendBCMCommand(command)
    }

    fun executeQuickAction(actionId: String) {
        comfortController.executeQuickAction(actionId, repository)
    }

    fun applyCodingOption(option: AstraJCodingModels.CodingOption, value: AstraJCodingModels.CodingValue) {
        _codingInProgress.value = true
        _codingResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val moduleAddr = option.module.address
                val hexValue = value.value
                val cmd = "ATSH $moduleAddr; 2E${option.channel.replace(" ", "")}$hexValue; ATSH 7E0"
                val response = repository.sendRawCommand(cmd)
                val result = if (response != null && !response.contains("ERROR", ignoreCase = true) && !response.contains("NO DATA", ignoreCase = true)) {
                    AstraJCodingModels.CodingResult(success = true, option = option, newValue = value)
                } else {
                    AstraJCodingModels.CodingResult(success = false, option = option, newValue = value, error = response ?: "Keine Antwort")
                }
                _codingResult.value = result
            } catch (e: Exception) {
                Log.w(TAG, "Coding write failed for ${option.id}", e)
                _codingResult.value = AstraJCodingModels.CodingResult(success = false, option = option, newValue = value, error = e.message)
            } finally {
                _codingInProgress.value = false
            }
        }
    }

    fun clearCodingResult() { _codingResult.value = null }

    fun selectCarProfile(profile: com.canopobd.data.model.CarProfile) {
        _carProfileState.value = profile
        repository.saveCarProfile(profile)
        _showCarProfile.value = false
    }

    private fun updateTimingChainState() {
        val data = repository.obdData.value
        val rpm = data.rpm
        val coolant = data.coolantTemp
        val isWarmedUp = coolant > 80

        if (rpm > 0) {
            rpmSampleBuffer.add(rpm)
            if (rpmSampleBuffer.size > 30) {
                rpmSampleBuffer.removeAt(0)
            }
        }

        if (data.timingAdvance > 0) {
            timingAdvanceBuffer.add(data.timingAdvance)
            if (timingAdvanceBuffer.size > 30) {
                timingAdvanceBuffer.removeAt(0)
            }
        }

        val rpmVariation = if (rpmSampleBuffer.size >= 3) {
            val mean = rpmSampleBuffer.average()
            val variance = rpmSampleBuffer.map { (it - mean) * (it - mean) }.sum() / rpmSampleBuffer.size
            kotlin.math.sqrt(variance)
        } else if (rpm > 0) {
            rpm * 0.02
        } else {
            0.0
        }
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
            coldSampleCount = if (!isWarmedUp) {
                _timingChainState.value.coldSampleCount + 1
            } else {
                _timingChainState.value.coldSampleCount
            },
            lastRpmReading = rpm,
            avgRpmCold = if (!isWarmedUp) {
                val newCount = _timingChainState.value.coldSampleCount + 1
                if (newCount > 0) {
                    (_timingChainState.value.avgRpmCold * _timingChainState.value.coldSampleCount + rpm) / newCount
                } else {
                    rpm
                }
            } else {
                _timingChainState.value.avgRpmCold
            },
            warmSampleCount = if (isWarmedUp) {
                _timingChainState.value.warmSampleCount + 1
            } else {
                _timingChainState.value.warmSampleCount
            },
            avgRpmWarm = if (isWarmedUp) {
                val warmCount = _timingChainState.value.warmSampleCount + 1
                if (warmCount > 0) {
                    (_timingChainState.value.avgRpmWarm * _timingChainState.value.warmSampleCount + rpm) / warmCount
                } else {
                    rpm
                }
            } else {
                _timingChainState.value.avgRpmWarm
            },
            rpmDeviationCold = rpmVariation
        )
    }

    fun updateDriveScore() {
        performanceViewModel.updateDriveScore()
    }

    fun resetDriveScore() {
        performanceViewModel.resetDriveScore()
    }

    fun recordDriveSample(rpm: Double, throttle: Double, speed: Double, prevRpm: Double, boostBar: Double = 0.0, wastegateDuty: Double = 0.0) {
        performanceViewModel.recordDriveSample(rpm, throttle, speed, prevRpm, boostBar, wastegateDuty)
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

    fun exportTripHistoryToCsv(callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val trips = tripHistoryEntities.value
                val sb = StringBuilder()
                val df = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy", java.util.Locale.GERMAN)
                    .withZone(java.time.ZoneId.systemDefault())
                val tf = java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.GERMAN)
                    .withZone(java.time.ZoneId.systemDefault())
                sb.appendLine("Datum,Uhrzeit Start,Uhrzeit Ende,Dauer (min),Strecke (km),Ø Geschw. (km/h),Max Geschw. (km/h),Ø RPM,Max RPM,Kraftstoff (L),Ø L/100km,VIN")
                for (trip in trips) {
                    val durationMin = ((trip.endTime - trip.startTime) / 60000).toInt()
                    val fuelPer100 = if (trip.distanceKm > 0) {
                        (trip.fuelUsedLiters / trip.distanceKm * 100)
                    } else {
                        0f
                    }
                    val startStr = java.time.Instant.ofEpochMilli(trip.startTime).let { df.format(it) }
                    val startTime = java.time.Instant.ofEpochMilli(trip.startTime).let { tf.format(it) }
                    val endTime = java.time.Instant.ofEpochMilli(trip.endTime).let { tf.format(it) }
                    sb.appendLine("$startStr,$startTime,$endTime,$durationMin,%.2f,%.1f,%.0f,%.0f,%.0f,%.2f,%.1f,${trip.vin}".format(
                        trip.distanceKm, trip.avgSpeedKmh, trip.maxSpeedKmh, trip.avgRpm, trip.maxRpm, trip.fuelUsedLiters, fuelPer100
                    ))
                }
                callback(sb.toString())
            } catch (e: Exception) {
                android.util.Log.w("DashboardVM", "exportTripHistoryToCsv failed", e)
                callback("")
            }
        }
    }

    fun exportTripToGPX(): String = repository.exportCurrentTripToGPX()

    fun exportTripToKML(): String = repository.exportCurrentTripToKML()

    fun clearGPSTripHistory() {
        repository.clearGPSTripHistory()
    }

    fun deleteTrip(id: Long) {
        viewModelScope.launch { repository.deleteTrip(id) }
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

    fun setEmulatorMode(enabled: Boolean) {
        repository.setEmulatorMode(enabled)
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
            if (!UpdateChecker.shouldCheckForUpdate(context)) return@launch
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
        _turboAnalysisJob.value?.cancel()
        _turboAnalysisJob.value = viewModelScope.launch(Dispatchers.Default) {
            obdData
                .filter { data -> data.rpm > 0 }
                .conflate()
                .collect { data ->
                    val baroKpa = data.barometricPressure.takeIf { it > 0.0 } ?: 100.0
                    val absoluteBoostKpa = if (data.boostPressure > 0.0) {
                        data.boostPressure
                    } else {
                        data.intakePressure
                    }
                    safetyViewModel.updateFromOBDData(data)
                    ecoScoreViewModel.updateFromOBDData(data, data.fuelLevel)
                    performanceViewModel.recordDriveSample(
                        rpm = data.rpm,
                        throttle = data.throttle,
                        speed = data.speed,
                        prevRpm = previousRpmForDriveScore,
                        boostBar = ((absoluteBoostKpa - baroKpa) / 100.0).coerceAtLeast(0.0),
                        wastegateDuty = data.wastegateControl
                    )
                    previousRpmForDriveScore = data.rpm
                    updateAllTurboMetrics(data)
                    updateEmissionsAnalyzers(data)
                    updateExtendedAnalyzers(data)
                }
        }
        turboViewModel.updateDriveSession(performanceViewModel.driveSession.value)
    }

    // ========== Emissions Analyzers ==========

    private fun updateEmissionsAnalyzers(data: OBDData) {
        val dtcCodes = dtcResponse.value?.codes?.map { it.code } ?: emptyList()
        analyzerManager.updateEmissionsAnalyzers(
            data = data,
            dtcCodes = dtcCodes,
            onVoltageHistoryUpdate = { },
            onO2VoltageHistoryUpdate = { }
        )
    }

    fun analyzeReadiness(readinessBits: Int): com.canopobd.data.domain.EmissionsReadinessAnalyzer.ReadinessAnalysis {
        val dtcCodes = dtcResponse.value?.codes?.map { it.code } ?: emptyList()
        val input = com.canopobd.data.domain.EmissionsReadinessAnalyzer.ReadinessInput(
            readinessBits = readinessBits,
            activeDTCs = dtcCodes,
            engineRuntimeSeconds = obdData.value.runTime,
            coolantTemp = obdData.value.coolantTemp
        )
        val result = analyzerManager.readinessAnalyzer.analyze(input)
        analyzerManager.emissionsReadiness.value = result
        return result
    }

    // ========== Extended Analyzers ==========

    private fun updateExtendedAnalyzers(data: OBDData) {
        val dtcCodes = dtcResponse.value?.codes?.map { it.code } ?: emptyList()
        val tcmData = canRepository?.transmissionData?.value
        analyzerManager.updateExtendedAnalyzers(
            data = data,
            dtcCodes = dtcCodes,
            currentKm = currentKm.value,
            maintenanceItems = _maintenanceItems.value,
            timingChainState = _timingChainState.value,
            timingAdvanceBuffer = timingAdvanceBuffer,
            wastegateDuty = wastegateDuty.value,
            canTransmissionData = tcmData
        )
    }

    private fun updateAllTurboMetrics(data: OBDData) {
        val currentSession = performanceViewModel.driveSession.value
        val newEndTime = System.currentTimeMillis()
        val updatedSession = if (currentSession.endTime == 0L || newEndTime - currentSession.endTime >= 1000L) {
            currentSession.copy(endTime = newEndTime)
        } else {
            currentSession
        }
        turboViewModel.updateFromOBDDataWithDriveSession(data, _carProfileState.value, updatedSession)

        _fuelRailPressure.value = data.fuelRailPressure
        _injectionQuantity.value = if (data.mafRate > 0 && data.rpm >= 100.0) {
            data.mafRate / 14.7 * 0.0007 / (data.rpm / 2.0) * 1000.0
        } else {
            0.0
        }
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
        if (hasDtcP0016) {
            score -= 30
        }
        if (hasDtcP0017) {
            score -= 25
        }
        if (hasDtcP0340) {
            score -= 25
        }
        if (hasDtcP1345) {
            score -= 20
        }

        if (timingVariance > 5.0) {
            score -= 15
        } else if (timingVariance > 3.0) {
            score -= 8
        }

        if (rpmStability < 90) {
            score -= 10
        }

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

        _chainHealthScore.value = score.toDouble()
        _chainTensionerHealth.value = chainHealth
        _timingCorrelation.value = correlation

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
            if (score < 60) {
                health = PCVHealth.WEAK
            }
        }

        if (data.oilTemp > calibration.maxOilTempC * 0.9) {
            score -= 10
        }

        score = score.coerceIn(0, 100)
        if (score < 40) {
            health = PCVHealth.FAILED
        }

        _pcvHealthScore.value = score.toDouble()
        _pcvHealth.value = health
        return health
    }

    // ========== Fuel System ==========

    fun analyzeFuelSystem(): FuelSystemHealth {
        val data = repository.obdData.value
        val status = analyzerManager.fuelTrimAnalyzer.analyze(data.shortTermFuelTrimB1, data.longTermFuelTrimB1)

        val health = when {
            status.isLean -> FuelSystemHealth.LEAN
            status.isRich -> FuelSystemHealth.RICH
            else -> FuelSystemHealth.NORMAL
        }

        _fuelSystemHealth.value = health
        _fuelRailPressure.value = data.fuelRailPressure
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

        val session = performanceViewModel.driveSession.value
        val totalSamples = session.rpmSampleCount.coerceAtLeast(1).toDouble()
        val ecoRatio = (session.coastingInGearSamples + session.deceleratingSamples).toDouble() / totalSamples
        val sportRatio = session.rpmAbove4500Samples.toDouble() / totalSamples

        _ecoScore.value = (ecoRatio * 100.0).coerceIn(0.0, 100.0)
        _sportScore.value = (sportRatio * 100.0).coerceIn(0.0, 100.0)
        _drivingStyle.value = style
        return style
    }

    // ========== Warning System ==========

    private fun startWarningMonitoring() {
        viewModelScope.launch {
            obdData.collect { data ->
                val tripKm = repository.tripData.value.distanceKm.toInt().coerceAtLeast(0)
                if (tripKm > _currentKm.value) {
                    _currentKm.value = tripKm
                    viewModelPrefs.edit().putInt("odometer_km", tripKm).apply()
                }
                if (data.rpm > 0) {
                    val warnings = checkCriticalWarnings(data)
                    _criticalWarnings.value = warnings
                }
                val now = System.currentTimeMillis()
                if (connectionState.value is OBDConnectionState.Connected &&
                    now - lastMaintenanceCheckTime >= 60_000) {
                    lastMaintenanceCheckTime = now
                    checkMaintenanceNotifications()
                }
            }
        }
    }

    fun checkCriticalWarnings(data: OBDData): List<VehicleWarning> {
        val calibration = AstraJ14TurboCalibration.INSTANCE
        val warnings = mutableListOf<VehicleWarning>()

        val baroKpa = if (data.barometricPressure > 0) {
            data.barometricPressure
        } else {
            100.0
        }
        val absoluteBoostKpa = if (data.boostPressure > 0) {
            data.boostPressure
        } else {
            data.intakePressure
        }
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

        val oilPressureBar = (data.turboOilPressure ?: 0.0) / 100.0
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

    fun processDTC(dtc: String): ProcessedDTC = dtcProcessor.processDTC(dtc)

    fun processAllDTCs() = dtcProcessor.processAllDTCs(dtcResponse.value)

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
            val rawPid = if (pid.startsWith("22")) pid.drop(2) else pid
            try {
                val canRepo = canRepository
                if (canRepo != null) {
                    canRepo.mode22Client.readDID(rawPid).collect { result ->
                        if (result != null) {
                            val pidInfo = canRepo.getDIDInfo(rawPid)
                            _mode22DataCache.value = _mode22DataCache.value + (rawPid to Mode22Data(
                                pid = rawPid,
                                value = pidInfo?.formula?.invoke(result) ?: 0.0,
                                unit = pidInfo?.unit ?: "",
                                timestamp = System.currentTimeMillis()
                            ))
                        } else {
                            // Unsupported/failed DID: leave no stale value behind.
                            // A cached 0.0 would be indistinguishable from a real
                            // zero reading, so remove any previous entry instead.
                            _mode22DataCache.value = _mode22DataCache.value - rawPid
                        }
                    }
                } else {
                    // No CAN repository: nothing was read, so cache nothing.
                    _mode22DataCache.value = _mode22DataCache.value - rawPid
                }
            } catch (e: Exception) {
                _mode22DataCache.value = _mode22DataCache.value - rawPid
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
        _turboAnalysisJob.value?.cancel()
        // Hinweis: Turbo-/Safety-/Eco-/PerformanceViewModel werden hier per
        // `new` gehalten, nicht via ViewModelProvider. Ihre Scopes duerfen
        // NICHT von aussen gecancelt werden (doppelte cancel() nach VM-Tod);
        // sie werden mit dieser VM garbage-collected. Langfristig: als
        // normale Klassen ohne ViewModel-Basis auslagern.
        repository.cleanup()
        super.onCleared()
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(application) as T
        }
    }
}
