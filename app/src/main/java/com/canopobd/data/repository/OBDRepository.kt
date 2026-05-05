package com.canopobd.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import com.canopobd.bluetooth.ELM327BTConnection
import com.canopobd.bluetooth.RemoteBridge
import com.canopobd.data.local.AlertConfigDao
import com.canopobd.data.local.AlertConfigEntity
import com.canopobd.data.local.AppSettingsDao
import com.canopobd.data.local.AppSettingsEntity
import com.canopobd.data.local.CanopoDatabase
import com.canopobd.data.local.MaintenanceDao
import com.canopobd.data.local.MaintenanceEntity
import com.canopobd.data.local.ShiftLightConfigDao
import com.canopobd.data.local.ShiftLightConfigEntity
import com.canopobd.data.local.TripDao
import com.canopobd.data.local.TripEntity
import com.canopobd.data.model.*
import com.canopobd.gps.GPSTracker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@SuppressLint("MissingPermission")
class OBDRepository(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val connection: ELM327BTConnection? = bluetoothAdapter?.let { ELM327BTConnection(it) }
    private var remoteBridge: RemoteBridge? = null
    private val prefs: SharedPreferences = context.getSharedPreferences("canop_obd_prefs", Context.MODE_PRIVATE)

    private val database = CanopoDatabase.getInstance(context)
    private val maintenanceDao: MaintenanceDao = database.maintenanceDao()
    private val alertConfigDao: AlertConfigDao = database.alertConfigDao()
    private val shiftLightConfigDao: ShiftLightConfigDao = database.shiftLightConfigDao()
    private val tripDao: TripDao = database.tripDao()
    private val appSettingsDao: AppSettingsDao = database.appSettingsDao()

    private val _connectionState = MutableStateFlow<OBDConnectionState>(OBDConnectionState.Disconnected)
    val connectionState: StateFlow<OBDConnectionState> = _connectionState.asStateFlow()

    private val _obdData = MutableStateFlow(OBDData())
    val obdData: StateFlow<OBDData> = _obdData.asStateFlow()

    private val _dtcResponse = MutableStateFlow<DTCResponse?>(null)
    val dtcResponse: StateFlow<DTCResponse?> = _dtcResponse.asStateFlow()

    private val _recordingActive = MutableStateFlow(false)
    val recordingActive: StateFlow<Boolean> = _recordingActive.asStateFlow()

    private val _recordedData = MutableStateFlow<List<DataRecord>>(emptyList())
    val recordedData: StateFlow<List<DataRecord>> = _recordedData.asStateFlow()

    private val _pollRate = MutableStateFlow(500L)
    val pollRate: StateFlow<Long> = _pollRate.asStateFlow()

    private val _measurementUnit = MutableStateFlow(MeasurementUnit.METRIC)
    val measurementUnit: StateFlow<MeasurementUnit> = _measurementUnit.asStateFlow()

    private val _remoteServerRunning = MutableStateFlow(false)
    val remoteServerRunning: StateFlow<Boolean> = _remoteServerRunning.asStateFlow()

    private val _remoteServerPort = MutableStateFlow(RemoteBridge.DEFAULT_PORT)
    val remoteServerPort: StateFlow<Int> = _remoteServerPort.asStateFlow()

    private val _remoteConnectedClients = MutableStateFlow(0)
    val remoteConnectedClients: StateFlow<Int> = _remoteConnectedClients.asStateFlow()

    private val _remoteServerIp = MutableStateFlow("")
    val remoteServerIp: StateFlow<String> = _remoteServerIp.asStateFlow()

    private val _tripData = MutableStateFlow(TripData())
    val tripData: StateFlow<TripData> = _tripData.asStateFlow()

    private val _connectionStats = MutableStateFlow(ConnectionStats())
    val connectionStats: StateFlow<ConnectionStats> = _connectionStats.asStateFlow()

    private val _autoReconnect = MutableStateFlow(false)
    val autoReconnect: StateFlow<Boolean> = _autoReconnect.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val gpsTracker = GPSTracker(context)
    val currentLocation = gpsTracker.currentLocation
    val isGPSTracking = gpsTracker.isTracking
    val currentTrip = gpsTracker.currentTrip
    val tripHistory = gpsTracker.tripHistory

    private val _trendHistory = MutableStateFlow(TrendHistory())
    val trendHistory: StateFlow<TrendHistory> = _trendHistory.asStateFlow()

    private val _colorTheme = MutableStateFlow(ColorTheme.CANOPO)
    val colorTheme: StateFlow<ColorTheme> = _colorTheme.asStateFlow()

    private val _appThemeMode = MutableStateFlow(AppThemeMode.DARK)
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    private val _primaryGaugeIds = MutableStateFlow(setOf("rpm", "speed", "coolant"))
    val primaryGaugeIds: StateFlow<Set<String>> = _primaryGaugeIds.asStateFlow()

    private val _pollMode = MutableStateFlow(PollMode.NORMAL)
    val pollMode: StateFlow<PollMode> = _pollMode.asStateFlow()

    private var lastConnectedAddress: String? = null
    private var reconnectJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null
    private var tripStartTime: Long = 0L
    private var tripSamples: Long = 0L
    private var tripSpeedSum: Double = 0.0
    private var tripRpmSum: Double = 0.0
    private var tripFuelUsedSum: Double = 0.0
    private var tripFuelStart: Double = 0.0
    private var tripPrevSpeed: Double = 0.0
    private var tripPrevTimestamp: Long = 0L
    private var storedVin: String = ""
    private var consecutivePollingFailures = 0

    private val pollPIDs = listOf(
        OBDPID.RPM, OBDPID.SPEED, OBDPID.COOLANT_TEMP, OBDPID.INTAKE_TEMP,
        OBDPID.THROTTLE, OBDPID.ENGINE_LOAD, OBDPID.FUEL_LEVEL,
        OBDPID.TIMING_ADVANCE, OBDPID.MAF_RATE, OBDPID.FUEL_PRESSURE,
        OBDPID.INTAKE_PRESSURE, OBDPID.RUN_TIME, OBDPID.FUEL_RAIL_PRESSURE,
        OBDPID.COMMANDED_EGR, OBDPID.EGR_TEMP, OBDPID.COMMANDED_EVAPORATIVE_PURGE,
        OBDPID.BAROMETRIC_PRESSURE, OBDPID.O2_VOLTAGE_B1S1, OBDPID.O2_VOLTAGE_B1S2,
        OBDPID.CATALYST_TEMP_B1S1, OBDPID.CONTROL_MODULE_VOLTAGE,
        OBDPID.ABSOLUTE_LOAD_VALUE, OBDPID.ENGINE_FUEL_RATE,
        OBDPID.SHORT_TERM_FUEL_TRIM_BANK1, OBDPID.LONG_TERM_FUEL_TRIM_BANK1,
        OBDPID.SHORT_TERM_FUEL_TRIM_BANK2, OBDPID.LONG_TERM_FUEL_TRIM_BANK2,
        OBDPID.FUEL_AIR_EQUIV_RATIO,
        OBDPID.ACCELERATOR_POS_D, OBDPID.THROTTLE_C, OBDPID.THROTTLE_ACTUATOR,
        OBDPID.HYBRID_BATTERY_REMAINING,
        OBDPID.OIL_TEMP,
        OBDPID.CHARGE_AIR_COOLER_TEMP,
        OBDPID.DEMAND_TORQUE,
        OBDPID.ACTUAL_TORQUE,
        OBDPID.REFERENCE_TORQUE,
        OBDPID.BOOST_PRESSURE,
        OBDPID.WASTEGATE_CONTROL,
        OBDPID.EGT_BANK1,
        OBDPID.ETHANOL_FUEL_PERCENT,
        OBDPID.FUEL_TANK_LEVEL_INPUT,
        OBDPID.DISTANCE_MIL,
        OBDPID.ABSOLUTE_THROTTLE_B,
        OBDPID.TURBO_OIL_PRESSURE,
        OBDPID.TURBO_WASTEGATE_B,
        OBDPID.TURBO_BOOST_B,
        OBDPID.TURBO_VARIABLE_GEOM,
        OBDPID.TURBO_WATER_COOL,
        OBDPID.TURBO_COMP_INLET_TEMP,
        OBDPID.TURBO_COMP_OUTLET_TEMP,
        OBDPID.TURBO_TURBINE_INLET_TEMP,
        OBDPID.TURBO_TURBINE_OUTLET_TEMP
    )

    private val trendRecorder = com.canopobd.ui.components.TrendRecorder(maxPoints = 60)
    private var lastTrendRecordTime = 0L
    private val trendRecordInterval = 1000L

    private val _readinessMonitor = MutableStateFlow(ReadinessMonitor())
    val readinessMonitor: StateFlow<ReadinessMonitor> = _readinessMonitor.asStateFlow()

    private val _detectedProtocol = MutableStateFlow("")
    val detectedProtocol: StateFlow<String> = _detectedProtocol.asStateFlow()

    private val _supportedPIDs = MutableStateFlow<List<String>>(emptyList())
    val supportedPIDs: StateFlow<List<String>> = _supportedPIDs.asStateFlow()

    private val _alertConfig = MutableStateFlow(AlertConfig())
    val alertConfig: StateFlow<AlertConfig> = _alertConfig.asStateFlow()

    private val _activeAlerts = MutableStateFlow<List<ActiveAlert>>(emptyList())
    val activeAlerts: StateFlow<List<ActiveAlert>> = _activeAlerts.asStateFlow()

    private val _freezeFrames = MutableStateFlow<List<FreezeFrame>>(emptyList())
    val freezeFrames: StateFlow<List<FreezeFrame>> = _freezeFrames.asStateFlow()

    private val _importedData = MutableStateFlow<List<CsvImportEntry>>(emptyList())
    val importedData: StateFlow<List<CsvImportEntry>> = _importedData.asStateFlow()

    init {
        _pollRate.value = prefs.getLong("poll_rate", 500L)
        _autoReconnect.value = prefs.getBoolean("auto_reconnect", false)
        storedVin = prefs.getString("vin", "") ?: ""
        prefs.getString("color_theme", null)?.let {
            _colorTheme.value = ColorTheme.fromName(it)
        }
        prefs.getString("app_theme_mode", null)?.let {
            _appThemeMode.value = AppThemeMode.fromName(it)
        }
        prefs.getStringSet("primary_gauges", null)?.let { ids ->
            _primaryGaugeIds.value = ids
        }
        _pollMode.value = PollMode.valueOf(prefs.getString("poll_mode", "NORMAL") ?: "NORMAL")
        _alertConfig.value = AlertConfig(
            speedWarning = prefs.getFloat("alert_speed", 130f),
            speedWarningEnabled = prefs.getBoolean("alert_speed_on", false),
            coolantWarning = prefs.getFloat("alert_coolant", 105f),
            coolantWarningEnabled = prefs.getBoolean("alert_coolant_on", true),
            fuelWarning = prefs.getFloat("alert_fuel", 15f),
            fuelWarningEnabled = prefs.getBoolean("alert_fuel_on", true),
            rpmWarning = prefs.getFloat("alert_rpm", 6000f),
            rpmWarningEnabled = prefs.getBoolean("alert_rpm_on", false),
            batteryLowWarning = prefs.getFloat("alert_battery", 11.5f),
            batteryLowWarningEnabled = prefs.getBoolean("alert_battery_on", true)
        )
        scope.launch { migrateFromPrefsIfNeeded() }
    }

    private suspend fun migrateFromPrefsIfNeeded() {
        if (prefs.getBoolean("room_migrated", false)) return
        val existingItems = loadMaintenanceItemsFromPrefs()
        if (existingItems.isNotEmpty()) {
            maintenanceDao.insertAll(existingItems.map { item ->
                MaintenanceEntity(
                    type = item.type.name,
                    lastServiceKm = item.lastServiceKm,
                    intervalKm = item.intervalKm,
                    lastServiceDate = item.lastServiceDate
                )
            })
        }
        prefs.edit().putBoolean("room_migrated", true).apply()
    }

    private fun loadMaintenanceItemsFromPrefs(): List<MaintenanceItem> {
        return MaintenanceType.entries.mapNotNull { type ->
            val km = prefs.getInt("maint_${type.name}_km", -1)
            if (km >= 0) {
                MaintenanceItem(
                    type = type,
                    lastServiceKm = km,
                    intervalKm = prefs.getInt("maint_${type.name}_interval", type.defaultInterval),
                    lastServiceDate = prefs.getLong("maint_${type.name}_date", 0L)
                )
            } else null
        }
    }

    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return try {
            bluetoothAdapter?.bondedDevices?.map { device ->
                BluetoothDeviceInfo(name = device.name ?: device.address, address = device.address)
            } ?: emptyList()
        } catch (e: SecurityException) {
            Log.w("OBDRepository", "Security exception getting paired devices", e)
            emptyList()
        }
    }

    fun connect(address: String) {
        val conn = connection
        if (conn == null) {
            val msg = "Bluetooth not available"
            _connectionState.value = OBDConnectionState.Error(msg)
            _lastError.value = msg
            return
        }
        reconnectJob?.cancel()
        lastConnectedAddress = address
        prefs.edit().putString("last_device", address).apply()

        scope.launch {
            _connectionState.value = OBDConnectionState.Connecting
            _lastError.value = null
            resetConnectionStats()

            val device = bluetoothAdapter?.getRemoteDevice(address)
            if (device == null) {
                val msg = "Device not found"
                _connectionState.value = OBDConnectionState.Error(msg)
                _lastError.value = msg
                return@launch
            }

            val result = conn.connect(device)
            if (result.isFailure) {
                val msg = result.exceptionOrNull()?.message ?: "Connection failed"
                _connectionState.value = OBDConnectionState.Error(msg)
                _lastError.value = msg
                if (_autoReconnect.value) scheduleReconnect(address)
                return@launch
            }

            _connectionState.value = OBDConnectionState.Connected
            _lastError.value = null
            startPolling(conn)
            if (remoteBridge == null) {
                remoteBridge = RemoteBridge(context, conn)
            }

            scope.launch {
                val vin = conn.readVIN()
                if (vin.isNotBlank() && vin.length >= 10) {
                    storedVin = vin
                    prefs.edit().putString("vin", vin).apply()
                }
            }
        }
    }

    private fun scheduleReconnect(address: String) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            _connectionState.value = OBDConnectionState.Error("Reconnecting…")
            delay(3000L)
            if (_autoReconnect.value && lastConnectedAddress == address) {
                connect(address)
            }
        }
    }

    fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        stopRemoteServer()
        stopGPSTracking()
        pollingJob?.cancel()
        pollingJob = null
        connection?.disconnect()
        _connectionState.value = OBDConnectionState.Disconnected
        _obdData.value = OBDData()
        _dtcResponse.value = null
        trendRecorder.clear()
        _trendHistory.value = TrendHistory()
        saveTripData()
    }

    private fun handleConnectionLoss(error: String) {
        val lastAddr = lastConnectedAddress
        if (_autoReconnect.value && lastAddr != null) {
            scheduleReconnect(lastAddr)
        } else {
            _connectionState.value = OBDConnectionState.Error(error)
            _lastError.value = error
        }
    }

    fun cleanup() {
        scope.cancel()
    }

    private fun resetConnectionStats() {
        _connectionStats.value = ConnectionStats()
    }

    private fun recordConnectionSuccess() {
        val s = _connectionStats.value
        val newSuccess = s.successCount + 1
        val newFailure = s.failureCount
        val rate = newSuccess.toDouble() / (newSuccess + newFailure)
        _connectionStats.value = s.copy(
            successCount = newSuccess,
            failureCount = newFailure,
            quality = ConnectionQuality.fromSuccessRate(rate)
        )
    }

    private fun recordConnectionFailure() {
        val s = _connectionStats.value
        val newSuccess = s.successCount
        val newFailure = s.failureCount + 1
        val rate = newSuccess.toDouble() / (newSuccess + newFailure)
        _connectionStats.value = s.copy(
            successCount = newSuccess,
            failureCount = newFailure,
            quality = ConnectionQuality.fromSuccessRate(rate)
        )
    }

    private fun startPolling(conn: ELM327BTConnection) {
        pollingJob?.cancel()
        tripStartTime = System.currentTimeMillis()
        tripSamples = 0
        tripSpeedSum = 0.0
        tripRpmSum = 0.0
        tripFuelUsedSum = 0.0
        tripPrevSpeed = 0.0
        tripPrevTimestamp = tripStartTime
        tripFuelStart = _obdData.value.fuelLevel

        pollingJob = scope.launch {
            while (isActive) {
                try {
                    val results = conn.readMultiplePIDs(pollPIDs)
                    val batteryVoltage = conn.getBatteryVoltage() ?: _obdData.value.batteryVoltage
                    val speed = results[OBDPID.SPEED] ?: _obdData.value.speed
                    val rpm = results[OBDPID.RPM] ?: _obdData.value.rpm
                    val fuelRate = results[OBDPID.ENGINE_FUEL_RATE] ?: 0.0

                    tripSamples++
                    tripSpeedSum += speed
                    tripRpmSum += rpm
                    tripFuelUsedSum += fuelRate * (_pollRate.value / 3_600_000.0)

                    val now = System.currentTimeMillis()
                    val dtHours = (now - tripPrevTimestamp) / 3_600_000.0
                    if (dtHours > 0) {
                        val distanceKm = (tripPrevSpeed + speed) / 2.0 * dtHours
                        _tripData.value = _tripData.value.copy(
                            durationSeconds = (now - tripStartTime) / 1000L,
                            distanceKm = _tripData.value.distanceKm + distanceKm,
                            maxSpeedKmh = maxOf(_tripData.value.maxSpeedKmh, speed),
                            avgSpeedKmh = tripSpeedSum / tripSamples.coerceAtLeast(1),
                            maxRpm = maxOf(_tripData.value.maxRpm, rpm),
                            avgRpm = tripRpmSum / tripSamples.coerceAtLeast(1),
                            sampleCount = tripSamples,
                            totalFuelUsed = tripFuelUsedSum,
                            avgFuelRate = if (tripSamples > 0) tripFuelUsedSum / ((now - tripStartTime) / 3_600_000.0.coerceAtLeast(0.001)) else 0.0,
                            fuelStartLevel = tripFuelStart,
                            fuelEndLevel = results[OBDPID.FUEL_LEVEL] ?: _tripData.value.fuelEndLevel,
                            vin = storedVin
                        )
                    }
                    tripPrevSpeed = speed
                    tripPrevTimestamp = now

                    _obdData.value = OBDData(
                        rpm = results[OBDPID.RPM] ?: _obdData.value.rpm,
                        speed = speed,
                        coolantTemp = results[OBDPID.COOLANT_TEMP] ?: _obdData.value.coolantTemp,
                        intakeTemp = results[OBDPID.INTAKE_TEMP] ?: _obdData.value.intakeTemp,
                        throttle = results[OBDPID.THROTTLE] ?: _obdData.value.throttle,
                        engineLoad = results[OBDPID.ENGINE_LOAD] ?: _obdData.value.engineLoad,
                        fuelLevel = results[OBDPID.FUEL_LEVEL] ?: _obdData.value.fuelLevel,
                        batteryVoltage = batteryVoltage,
                        timingAdvance = results[OBDPID.TIMING_ADVANCE] ?: 0.0,
                        mafRate = results[OBDPID.MAF_RATE] ?: 0.0,
                        fuelPressure = results[OBDPID.FUEL_PRESSURE] ?: 0.0,
                        intakePressure = results[OBDPID.INTAKE_PRESSURE] ?: 0.0,
                        runTime = results[OBDPID.RUN_TIME] ?: 0.0,
                        fuelRailPressure = results[OBDPID.FUEL_RAIL_PRESSURE] ?: 0.0,
                        commandedEGR = results[OBDPID.COMMANDED_EGR] ?: 0.0,
                        egrTemp = results[OBDPID.EGR_TEMP] ?: 0.0,
                        commandedEvapPurge = results[OBDPID.COMMANDED_EVAPORATIVE_PURGE] ?: 0.0,
                        barometricPressure = results[OBDPID.BAROMETRIC_PRESSURE] ?: 0.0,
                        o2VoltageB1S1 = results[OBDPID.O2_VOLTAGE_B1S1] ?: 0.0,
                        o2VoltageB1S2 = results[OBDPID.O2_VOLTAGE_B1S2] ?: 0.0,
                        catalystTemp = results[OBDPID.CATALYST_TEMP_B1S1] ?: 0.0,
                        controlModuleVoltage = results[OBDPID.CONTROL_MODULE_VOLTAGE] ?: 0.0,
                        absoluteLoadValue = results[OBDPID.ABSOLUTE_LOAD_VALUE] ?: 0.0,
                        engineFuelRate = fuelRate,
                        shortTermFuelTrimB1 = results[OBDPID.SHORT_TERM_FUEL_TRIM_BANK1] ?: 0.0,
                        longTermFuelTrimB1 = results[OBDPID.LONG_TERM_FUEL_TRIM_BANK1] ?: 0.0,
                        shortTermFuelTrimB2 = results[OBDPID.SHORT_TERM_FUEL_TRIM_BANK2] ?: 0.0,
                        longTermFuelTrimB2 = results[OBDPID.LONG_TERM_FUEL_TRIM_BANK2] ?: 0.0,
                        fuelAirRatio = results[OBDPID.FUEL_AIR_EQUIV_RATIO] ?: 0.0,
                        acceleratorPosD = results[OBDPID.ACCELERATOR_POS_D] ?: 0.0,
                        throttleC = results[OBDPID.THROTTLE_C] ?: 0.0,
                        throttleActuator = results[OBDPID.THROTTLE_ACTUATOR] ?: 0.0,
                        hybridBatteryRemaining = results[OBDPID.HYBRID_BATTERY_REMAINING] ?: 0.0,
                        turboOilPressure = results[OBDPID.TURBO_OIL_PRESSURE],
                        turboWastegateB = results[OBDPID.TURBO_WASTEGATE_B],
                        turboBoostB = results[OBDPID.TURBO_BOOST_B],
                        turboVgtPosition = results[OBDPID.TURBO_VARIABLE_GEOM],
                        turboWaterCoolFlow = results[OBDPID.TURBO_WATER_COOL],
                        turboCompInletTemp = results[OBDPID.TURBO_COMP_INLET_TEMP],
                        turboCompOutletTemp = results[OBDPID.TURBO_COMP_OUTLET_TEMP],
                        turboTurbineInletTemp = results[OBDPID.TURBO_TURBINE_INLET_TEMP],
                        turboTurbineOutletTemp = results[OBDPID.TURBO_TURBINE_OUTLET_TEMP],
                        vin = storedVin,
                        timestamp = now
                    )

                if (now - lastTrendRecordTime >= trendRecordInterval) {
                    trendRecorder.record(
                        _obdData.value.rpm,
                        _obdData.value.speed,
                        _obdData.value.coolantTemp,
                        _obdData.value.boostPressure,
                        _obdData.value.wastegateControl,
                        _obdData.value.turboRpm,
                        _obdData.value.egtBank1,
                        _obdData.value.chargeAirCoolerTemp
                    )
                    _trendHistory.value = trendRecorder.getHistory()
                    lastTrendRecordTime = now
                }

                    val unit = _measurementUnit.value
                    prefs.edit()
                        .putFloat("widget_rpm", _obdData.value.rpm.toFloat())
                        .putFloat("widget_speed", unit.convertSpeed(_obdData.value.speed).toFloat())
                        .putFloat("widget_coolant", unit.convertTemp(_obdData.value.coolantTemp).toFloat())
                        .putFloat("widget_load", _obdData.value.engineLoad.toFloat())
                        .putFloat("widget_fuel", _obdData.value.fuelLevel.toFloat())
                        .putBoolean("unit_metric", unit == MeasurementUnit.METRIC)
                        .apply()

                    consecutivePollingFailures = 0
                    _lastError.value = null
                    recordConnectionSuccess()
                    checkAlerts()
                    if (_recordingActive.value) recordData()
                } catch (e: Exception) {
                    consecutivePollingFailures++
                    Log.e("OBDRepository", "Polling error ($consecutivePollingFailures): ${e.message}")
                    _lastError.value = "Polling-Fehler: ${e.message}"
                    recordConnectionFailure()
                    if (consecutivePollingFailures >= 5) {
                        _connectionState.value = OBDConnectionState.Error("Verbindung unterbrochen")
                        _lastError.value = "Verbindung unterbrochen nach $consecutivePollingFailures Fehlern"
                        break
                    }
                }
                delay(_pollRate.value)
            }
        }
    }

    private fun saveTripData() {
        prefs.edit()
            .putLong("trip_distance", (_tripData.value.distanceKm * 1000).toLong())
            .putLong("trip_duration", _tripData.value.durationSeconds)
            .apply()
    }

    fun getStoredVin(): String = storedVin

    fun startRemoteServer(port: Int = RemoteBridge.DEFAULT_PORT): Result<Int> {
        val bridge = remoteBridge ?: return Result.failure(IllegalStateException("Not connected to ELM327"))
        val result = bridge.startServer(port)
        if (result.isSuccess) {
            _remoteServerIp.value = bridge.getLocalIpAddress()
            _remoteServerPort.value = bridge.serverPort.value
            _remoteServerRunning.value = true
        }
        return result
    }

    fun stopRemoteServer() {
        remoteBridge?.stopServer()
        _remoteServerRunning.value = false
        _remoteConnectedClients.value = 0
    }

    fun readDTCs() {
        val conn = connection ?: return
        scope.launch { _dtcResponse.value = conn.readDTCs() }
    }

    fun clearDTCs() {
        val conn = connection ?: return
        scope.launch {
            if (conn.clearDTCs()) _dtcResponse.value = DTCResponse(emptyList())
        }
    }

    fun startRecording() {
        _recordingActive.value = true
        _recordedData.value = emptyList()
    }

    fun stopRecording() { _recordingActive.value = false }

    private fun recordData() {
        val d = _obdData.value
        _recordedData.value = _recordedData.value + DataRecord(
            d.timestamp, d.rpm, d.speed, d.coolantTemp, d.throttle, d.fuelLevel, d.batteryVoltage
        )
    }

    fun setPollRate(rate: Long) {
        val r = rate.coerceIn(100L, 2000L)
        _pollRate.value = r
        prefs.edit().putLong("poll_rate", r).apply()
    }

    fun setMeasurementUnit(unit: MeasurementUnit) { _measurementUnit.value = unit }

    fun setAutoReconnect(enabled: Boolean) {
        _autoReconnect.value = enabled
        prefs.edit().putBoolean("auto_reconnect", enabled).apply()
    }

    fun setColorTheme(theme: ColorTheme) {
        _colorTheme.value = theme
        prefs.edit().putString("color_theme", theme.name).apply()
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode.name).apply()
    }

    fun startGPSTracking() {
        gpsTracker.startTracking()
    }

    fun stopGPSTracking() {
        gpsTracker.stopTracking()
    }

    fun getGPSTripHistory(): List<GPSTrip> = gpsTracker.tripHistory.value

    fun exportCurrentTripToGPX(): String = gpsTracker.exportToGPX()

    fun exportCurrentTripToKML(): String = gpsTracker.exportToKML()

    fun clearGPSTripHistory() {
        gpsTracker.clearTripHistory()
    }

    fun setPrimaryGauges(ids: Set<String>) {
        _primaryGaugeIds.value = ids
        prefs.edit().putStringSet("primary_gauges", ids).apply()
    }

    fun setPollMode(mode: PollMode) {
        _pollMode.value = mode
        val effectiveRate = when (mode) {
            PollMode.FAST -> 50L
            PollMode.NORMAL -> 500L
            PollMode.ECO -> 2000L
        }
        _pollRate.value = effectiveRate
        prefs.edit()
            .putString("poll_mode", mode.name)
            .putLong("poll_rate", effectiveRate)
            .apply()
    }

    fun resetTrip() {
        tripStartTime = System.currentTimeMillis()
        tripSamples = 0
        tripSpeedSum = 0.0
        tripRpmSum = 0.0
        tripFuelUsedSum = 0.0
        tripFuelStart = _obdData.value.fuelLevel
        _tripData.value = TripData(fuelStartLevel = tripFuelStart, vin = storedVin)
    }

    fun getLastDevice(): String? = prefs.getString("last_device", null)

    fun exportToCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Timestamp,RPM,Speed,Coolant,Throttle,Fuel,Battery")
        for (r in _recordedData.value) {
            sb.appendLine("${r.timestamp},${r.rpm.toInt()},${r.speed.toInt()},${r.coolantTemp.toInt()},${r.throttle.toInt()},${r.fuelLevel.toInt()},${r.batteryVoltage}")
        }
        return sb.toString()
    }

    fun clearRecordedData() { _recordedData.value = emptyList() }

    private fun checkAlerts() {
        val cfg = _alertConfig.value
        val d = _obdData.value
        val alerts = mutableListOf<ActiveAlert>()

        if (cfg.speedWarningEnabled && d.speed > cfg.speedWarning) {
            alerts.add(ActiveAlert(AlertType.SPEED, d.speed.toFloat(), cfg.speedWarning,
                "Geschwindigkeit: %.0f > %.0f".format(d.speed, cfg.speedWarning)))
        }
        if (cfg.coolantWarningEnabled && d.coolantTemp > cfg.coolantWarning) {
            alerts.add(ActiveAlert(AlertType.COOLANT, d.coolantTemp.toFloat(), cfg.coolantWarning,
                "Kühlmittel: %.0f° > %.0f°".format(d.coolantTemp, cfg.coolantWarning)))
        }
        if (cfg.fuelWarningEnabled && d.fuelLevel < cfg.fuelWarning && d.fuelLevel > 0) {
            alerts.add(ActiveAlert(AlertType.FUEL, d.fuelLevel.toFloat(), cfg.fuelWarning,
                "Kraftstoff: %.0f%% < %.0f%%".format(d.fuelLevel, cfg.fuelWarning)))
        }
        if (cfg.rpmWarningEnabled && d.rpm > cfg.rpmWarning) {
            alerts.add(ActiveAlert(AlertType.RPM, d.rpm.toFloat(), cfg.rpmWarning,
                "Drehzahl: %.0f > %.0f".format(d.rpm, cfg.rpmWarning)))
        }
        if (cfg.batteryLowWarningEnabled && d.batteryVoltage > 0 && d.batteryVoltage < cfg.batteryLowWarning) {
            alerts.add(ActiveAlert(AlertType.BATTERY, d.batteryVoltage.toFloat(), cfg.batteryLowWarning,
                "Batterie: %.1fV < %.1fV".format(d.batteryVoltage, cfg.batteryLowWarning)))
        }
        _activeAlerts.value = alerts
    }

    fun setAlertConfig(config: AlertConfig) {
        _alertConfig.value = config
        prefs.edit()
            .putFloat("alert_speed", config.speedWarning)
            .putBoolean("alert_speed_on", config.speedWarningEnabled)
            .putFloat("alert_coolant", config.coolantWarning)
            .putBoolean("alert_coolant_on", config.coolantWarningEnabled)
            .putFloat("alert_fuel", config.fuelWarning)
            .putBoolean("alert_fuel_on", config.fuelWarningEnabled)
            .putFloat("alert_rpm", config.rpmWarning)
            .putBoolean("alert_rpm_on", config.rpmWarningEnabled)
            .putFloat("alert_battery", config.batteryLowWarning)
            .putBoolean("alert_battery_on", config.batteryLowWarningEnabled)
            .apply()
        scope.launch {
            alertConfigDao.insert(
                AlertConfigEntity(
                    id = 1,
                    speedWarning = config.speedWarning,
                    speedWarningEnabled = config.speedWarningEnabled,
                    coolantWarning = config.coolantWarning,
                    coolantWarningEnabled = config.coolantWarningEnabled,
                    fuelWarning = config.fuelWarning,
                    fuelWarningEnabled = config.fuelWarningEnabled,
                    rpmWarning = config.rpmWarning,
                    rpmWarningEnabled = config.rpmWarningEnabled,
                    batteryLowWarning = config.batteryLowWarning,
                    batteryLowWarningEnabled = config.batteryLowWarningEnabled
                )
            )
        }
    }

    fun readReadinessMonitor() {
        val conn = connection ?: return
        scope.launch {
            _readinessMonitor.value = conn.readReadinessMonitor()
        }
    }

    fun readProtocol() {
        val conn = connection ?: return
        scope.launch {
            _detectedProtocol.value = conn.readProtocol()
        }
    }

    fun scanSupportedPIDs() {
        val conn = connection ?: return
        scope.launch {
            _supportedPIDs.value = conn.scanSupportedPIDs()
        }
    }

    fun readFreezeFrames() {
        val conn = connection ?: return
        scope.launch {
            _freezeFrames.value = conn.readFreezeFrames()
        }
    }

    fun importCsvData(csvContent: String) {
        val entries = mutableListOf<CsvImportEntry>()
        val lines = csvContent.lines().drop(1)
        for (line in lines) {
            val parts = line.split(",")
            if (parts.size >= 7) {
                try {
                    entries.add(CsvImportEntry(
                        timestamp = parts[0].toLongOrNull() ?: 0L,
                        rpm = parts[1].toDoubleOrNull() ?: 0.0,
                        speed = parts[2].toDoubleOrNull() ?: 0.0,
                        coolantTemp = parts[3].toDoubleOrNull() ?: 0.0,
                        throttle = parts[4].toDoubleOrNull() ?: 0.0,
                        fuelLevel = parts[5].toDoubleOrNull() ?: 0.0,
                        batteryVoltage = parts[6].toDoubleOrNull() ?: 0.0
                    ))
                } catch (e: Exception) {
                    Log.w("OBDRepository", "Failed to parse CSV line: ${parts.joinToString(",")}", e)
                }
            }
        }
        _importedData.value = entries
    }

    fun clearImportedData() { _importedData.value = emptyList() }

    fun saveMaintenanceItem(item: MaintenanceItem) {
        prefs.edit()
            .putInt("maint_${item.type.name}_km", item.lastServiceKm)
            .putInt("maint_${item.type.name}_interval", item.intervalKm)
            .putLong("maint_${item.type.name}_date", System.currentTimeMillis())
            .apply()
        scope.launch {
            maintenanceDao.insert(
                MaintenanceEntity(
                    type = item.type.name,
                    lastServiceKm = item.lastServiceKm,
                    intervalKm = item.intervalKm,
                    lastServiceDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun loadMaintenanceItems(): List<MaintenanceItem> {
        return MaintenanceType.entries.mapNotNull { type ->
            val km = prefs.getInt("maint_${type.name}_km", -1)
            if (km >= 0) {
                MaintenanceItem(
                    type = type,
                    lastServiceKm = km,
                    intervalKm = prefs.getInt("maint_${type.name}_interval", type.defaultInterval),
                    lastServiceDate = prefs.getLong("maint_${type.name}_date", 0L)
                )
            } else null
        }
    }

    fun loadMaintenanceItemsFromRoom(): List<MaintenanceItem> {
        val entities = kotlinx.coroutines.runBlocking {
            maintenanceDao.getAllOnce()
        }
        return entities.mapNotNull { entity ->
            MaintenanceType.entries.find { it.name == entity.type }?.let { type ->
                MaintenanceItem(
                    type = type,
                    lastServiceKm = entity.lastServiceKm,
                    intervalKm = entity.intervalKm,
                    lastServiceDate = entity.lastServiceDate
                )
            }
        }
    }

    fun clearMaintenanceHistory() {
        val edit = prefs.edit()
        MaintenanceType.entries.forEach { type ->
            edit.remove("maint_${type.name}_km")
            edit.remove("maint_${type.name}_interval")
            edit.remove("maint_${type.name}_date")
        }
        edit.apply()
        scope.launch { maintenanceDao.deleteAll() }
    }

    fun getFuelTrimAnalysis(): FuelTrimAnalysis {
        val d = _obdData.value
        val stftB1 = d.shortTermFuelTrimB1
        val ltftB1 = d.longTermFuelTrimB1
        val stftB2 = d.shortTermFuelTrimB2
        val ltftB2 = d.longTermFuelTrimB2
        return FuelTrimAnalysis(
            stftB1 = stftB1,
            ltftB1 = ltftB1,
            stftB2 = stftB2,
            ltftB2 = ltftB2,
            totalTrimB1 = stftB1 + ltftB1,
            totalTrimB2 = stftB2 + ltftB2
        )
    }

    fun getFuelEconomyData(): FuelEconomyData {
        val d = _obdData.value
        val td = _tripData.value

        val fuelRateLh = d.engineFuelRate
        val speedKmh = d.speed

        if (fuelRateLh > 0.01 && speedKmh > 0.5) {
            val l100km = (fuelRateLh / speedKmh) * 100.0
            if (l100km > 0.5 && l100km < 100.0) {
                return FuelEconomyData.fromL100km(l100km)
            }
        }

        if (d.mafRate > 0.1 && speedKmh > 0.5) {
            val lps = d.mafRate * 0.0014
            val lph = lps * 3600.0
            val l100km = (lph / speedKmh) * 100.0
            if (l100km > 0.5 && l100km < 100.0) {
                val kmL = 100.0 / l100km
                val mpgUs = 235.214583 / l100km
                val mpgUk = 282.4809363 / l100km
                val avgL100km = if (td.avgFuelRate > 0.0 && td.avgSpeedKmh > 0.5) {
                    (td.avgFuelRate / td.avgSpeedKmh) * 100.0
                } else l100km
                return FuelEconomyData(
                    currentL100km = l100km,
                    avgL100km = avgL100km,
                    currentKmL = kmL,
                    avgKmL = if (avgL100km > 0.5) 100.0 / avgL100km else 0.0,
                    currentMpgUs = mpgUs,
                    avgMpgUs = 235.214583 / avgL100km,
                    currentMpgUk = mpgUk,
                    avgMpgUk = 282.4809363 / avgL100km,
                    estimatedFromMaf = true
                )
            }
        }

        return FuelEconomyData()
    }

    fun saveShiftLightConfig(config: ShiftLightConfig) {
        prefs.edit()
            .putBoolean("shift_light_enabled", config.enabled)
            .putInt("shift_light_redline", config.redlineRpm)
            .putInt("shift_light_warning", config.warningRpm)
            .putBoolean("shift_light_flash", config.flashEnabled)
            .putBoolean("shift_light_sound", config.soundEnabled)
            .apply()
        scope.launch {
            shiftLightConfigDao.insert(
                ShiftLightConfigEntity(
                    id = 1,
                    enabled = config.enabled,
                    redlineRpm = config.redlineRpm,
                    warningRpm = config.warningRpm,
                    flashEnabled = config.flashEnabled,
                    soundEnabled = config.soundEnabled
                )
            )
        }
    }

    fun loadShiftLightConfig(): ShiftLightConfig {
        return ShiftLightConfig(
            enabled = prefs.getBoolean("shift_light_enabled", false),
            redlineRpm = prefs.getInt("shift_light_redline", 6500),
            warningRpm = prefs.getInt("shift_light_warning", 5500),
            flashEnabled = prefs.getBoolean("shift_light_flash", true),
            soundEnabled = prefs.getBoolean("shift_light_sound", false)
        )
    }

    fun saveCarProfile(profile: CarProfile) {
        prefs.edit().putString("car_profile_id", profile.id).apply()
    }

    fun loadCarProfile(): CarProfile {
        val id = prefs.getString("car_profile_id", null)
        return if (id != null) CarProfile.fromId(id) ?: CarProfile.default() else CarProfile.default()
    }
}
