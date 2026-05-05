package com.canopobd.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.canopobd.bluetooth.RemoteBridge
import com.canopobd.data.model.*
import com.canopobd.data.repository.OBDRepository
import kotlinx.coroutines.flow.*

@SuppressLint("MissingPermission")
class DashboardViewModel private constructor(
    private val context: Context
) : ViewModel() {

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

    private val _powerCalculation = MutableStateFlow(com.canopobd.data.model.PowerCalculation())
    val powerCalculation: StateFlow<com.canopobd.data.model.PowerCalculation> = _powerCalculation.asStateFlow()

    private val _driveScore = MutableStateFlow(com.canopobd.data.model.DriveScore())
    val driveScore: StateFlow<com.canopobd.data.model.DriveScore> = _driveScore.asStateFlow()

    private val _driveSession = MutableStateFlow(com.canopobd.data.model.DriveSession())
    val driveSession: StateFlow<com.canopobd.data.model.DriveSession> = _driveSession.asStateFlow()

    private val _shiftLightConfig = MutableStateFlow(com.canopobd.data.model.ShiftLightConfig())
    val shiftLightConfig: StateFlow<com.canopobd.data.model.ShiftLightConfig> = _shiftLightConfig.asStateFlow()

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

    init {
        if (_permissionsGranted.value) refreshDevices()
        _maintenanceItems.value = repository.loadMaintenanceItems()
        _shiftLightConfig.value = repository.loadShiftLightConfig()
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
            _powerCalculation.value = com.canopobd.data.model.PowerCalculation.calculate(d.mafRate, d.rpm)
        }
    }

    fun toggleDriveScore() {
        _showDriveScore.value = !_showDriveScore.value
    }

    fun toggleShiftLight() {
        _showShiftLight.value = !_showShiftLight.value
    }

    fun updateDriveScore() {
        val session = _driveSession.value
        val score = com.canopobd.data.model.DriveScore(
            accelerationScore = (100 - session.harshAccels * 10).coerceIn(0, 100),
            brakingScore = (100 - session.harshBrakes * 10).coerceIn(0, 100),
            cruisingScore = calculateCruisingScore(session),
            idleScore = calculateIdleScore(session),
            rpmScore = calculateRpmScore(session),
            throttleScore = calculateThrottleScore(session),
            score = 0
        )
        val avgScore = (score.accelerationScore + score.brakingScore + score.cruisingScore +
                score.idleScore + score.rpmScore + score.throttleScore) / 6
        _driveScore.value = score.copy(score = avgScore)
    }

    private fun calculateCruisingScore(session: com.canopobd.data.model.DriveSession): Int {
        return if (session.avgSpeed > 0) {
            ((session.speedSamples / (session.speedSamples + session.harshAccels + session.harshBrakes)) * 100).toInt().coerceIn(0, 100)
        } else 50
    }

    private fun calculateIdleScore(session: com.canopobd.data.model.DriveSession): Int {
        val totalSeconds = if (session.endTime > 0) (session.endTime - session.startTime) / 1000 else 0L
        return if (totalSeconds > 0) {
            ((1.0 - (session.idleTimeSeconds.toDouble() / totalSeconds)) * 100).toInt().coerceIn(0, 100)
        } else 50
    }

    private fun calculateRpmScore(session: com.canopobd.data.model.DriveSession): Int {
        return when {
            session.avgRpm < 1500 -> 80
            session.avgRpm < 2500 -> 100
            session.avgRpm < 3500 -> 80
            session.avgRpm < 4500 -> 60
            else -> 40
        }
    }

    private fun calculateThrottleScore(session: com.canopobd.data.model.DriveSession): Int {
        return when {
            session.avgThrottle < 30 -> 100
            session.avgThrottle < 50 -> 80
            session.avgThrottle < 70 -> 60
            else -> 40
        }
    }

    fun resetDriveScore() {
        _driveSession.value = com.canopobd.data.model.DriveSession()
        _driveScore.value = com.canopobd.data.model.DriveScore()
    }

    fun recordDriveSample(rpm: Double, throttle: Double, speed: Double, prevRpm: Double) {
        val session = _driveSession.value
        val rpmDelta = rpm - prevRpm
        val newSession = session.copy(
            rpmSamples = session.rpmSamples + rpm,
            throttleSamples = session.throttleSamples + throttle,
            speedSamples = session.speedSamples + speed,
            avgRpm = if (session.rpmSamples + rpm > 0) (session.rpmSamples + rpm) / 2.0 else rpm,
            avgThrottle = if (session.throttleSamples + throttle > 0) (session.throttleSamples + throttle) / 2.0 else throttle,
            avgSpeed = if (session.speedSamples + speed > 0) (session.speedSamples + speed) / 2.0 else speed,
            maxRpm = maxOf(session.maxRpm, rpm),
            maxThrottle = maxOf(session.maxThrottle, throttle),
            harshAccels = if (rpmDelta > 3000) session.harshAccels + 1 else session.harshAccels,
            harshBrakes = if (throttle < 10.0 && speed > 50.0 && prevRpm > rpm) session.harshBrakes + 1 else session.harshBrakes
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

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(context.applicationContext) as T
        }
    }
}
