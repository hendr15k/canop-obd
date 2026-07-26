package com.canopobd.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import com.canopobd.bluetooth.ELM327BTConnection
import com.canopobd.bluetooth.Mode22PIDs
import com.canopobd.bluetooth.Mode22TurboData
import com.canopobd.bluetooth.RemoteBridge
import com.canopobd.data.emulator.OBDEmulator
import com.canopobd.data.local.AlertConfigDao
import com.canopobd.data.local.AlertConfigEntity
import com.canopobd.data.local.AppSettingsDao
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
    val connection: ELM327BTConnection? = bluetoothAdapter?.let { ELM327BTConnection(it) }
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
    private val recordedDataBuffer = mutableListOf<DataRecord>()
    private val MAX_RECORDED_SAMPLES = 10_000

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
    private var connectAttempt = java.util.concurrent.atomic.AtomicLong(0)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val persistTripScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val tripHistoryEntities: StateFlow<List<TripEntity>> = tripDao.getAll()
        .stateIn(scope, SharingStarted.Lazily, emptyList())
    private var pollingJob: Job? = null
    private val tripLock = Any()
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
    private var lastWidgetUpdateTime = 0L
    private val widgetUpdateInterval = 5000L // Update widget prefs every 5 seconds to reduce SharedPreferences writes

    private val _readinessMonitor = MutableStateFlow(ReadinessMonitor())
    val readinessMonitor: StateFlow<ReadinessMonitor> = _readinessMonitor.asStateFlow()

    private val _detectedProtocol = MutableStateFlow("")
    val detectedProtocol: StateFlow<String> = _detectedProtocol.asStateFlow()

    private val _supportedPIDs = MutableStateFlow<List<String>>(emptyList())
    val supportedPIDs: StateFlow<List<String>> = _supportedPIDs.asStateFlow()

    private val _alertConfig = MutableStateFlow(AlertConfig())
    val alertConfig: StateFlow<AlertConfig> = _alertConfig.asStateFlow()

    private val lastAlertTrigger = java.util.concurrent.ConcurrentHashMap<AlertType, Long>()

    private val _activeAlerts = MutableStateFlow<List<ActiveAlert>>(emptyList())
    val activeAlerts: StateFlow<List<ActiveAlert>> = _activeAlerts.asStateFlow()

    private val _freezeFrames = MutableStateFlow<List<FreezeFrame>>(emptyList())
    val freezeFrames: StateFlow<List<FreezeFrame>> = _freezeFrames.asStateFlow()

    private val _importedData = MutableStateFlow<List<CsvImportEntry>>(emptyList())
    val importedData: StateFlow<List<CsvImportEntry>> = _importedData.asStateFlow()

    private val _mode22Data = MutableStateFlow(Mode22TurboData())
    val mode22Data: StateFlow<Mode22TurboData> = _mode22Data.asStateFlow()

    private var mode22Counter = 0

    private val _emulatorMode = MutableStateFlow(false)
    val emulatorMode: StateFlow<Boolean> = _emulatorMode.asStateFlow()
    private var emulator: OBDEmulator? = null
    private var emulatorPollingJob: Job? = null

    data class TPMSReading(
        val frontLeftPSI: Double = 0.0,
        val frontRightPSI: Double = 0.0,
        val rearLeftPSI: Double = 0.0,
        val rearRightPSI: Double = 0.0,
        val frontLeftTemp: Int = 0,
        val frontRightTemp: Int = 0,
        val rearLeftTemp: Int = 0,
        val rearRightTemp: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    )
    private val _tpmsReading = MutableStateFlow(TPMSReading())
    val tpmsReading: StateFlow<TPMSReading> = _tpmsReading.asStateFlow()

    data class ClimateReading(
        val driverTempCelsius: Int = 22,
        val passengerTempCelsius: Int = 22,
        val fanSpeed: Int = 3,
        val isACEnabled: Boolean = false,
        val isAutoMode: Boolean = true,
        val isRecirculation: Boolean = false,
        val isFrontDefrost: Boolean = false,
        val isRearDefrost: Boolean = false,
        val isMirrorDefrost: Boolean = false,
        val outsideTemp: Int = 18,
        val cabinTemp: Int = 23,
        val acCompressorActive: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    )
    private val _climateReading = MutableStateFlow(ClimateReading())
    val climateReading: StateFlow<ClimateReading> = _climateReading.asStateFlow()

    data class TCMReading(
        val currentGear: Int = 0,
        val oilTempCelsius: Int = 0,
        val pressureKpa: Int = 0,
        val inputShaftRpm: Double = 0.0,
        val outputShaftRpm: Double = 0.0,
        val sportMode: Boolean = false,
        val manualMode: Boolean = false,
        val transmissionError: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    )
    private val _tcmReading = MutableStateFlow(TCMReading())
    val tcmReading: StateFlow<TCMReading> = _tcmReading.asStateFlow()

    data class ECMReading(
        val rpm: Double = 0.0,
        val speedKmh: Double = 0.0,
        val coolantTemp: Int = 0,
        val throttlePosition: Double = 0.0,
        val engineLoad: Double = 0.0,
        val timestamp: Long = System.currentTimeMillis()
    )
    private val _ecmReading = MutableStateFlow(ECMReading())
    val ecmReading: StateFlow<ECMReading> = _ecmReading.asStateFlow()

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
        _emulatorMode.value = prefs.getBoolean("emulator_mode", false)
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
        if (_emulatorMode.value) {
            connectEmulator()
        }
    }

    private suspend fun migrateFromPrefsIfNeeded() {
        if (prefs.getBoolean("room_migrated", false)) { return }
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
            } else { null }
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
        val attempt = connectAttempt.incrementAndGet()
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

            val device = try {
                bluetoothAdapter?.getRemoteDevice(address)
            } catch (e: SecurityException) {
                Log.e("OBDRepository", "SecurityException getting remote device", e)
                null
            }
            if (device == null) {
                val msg = "Device not found"
                _connectionState.value = OBDConnectionState.Error(msg)
                _lastError.value = msg
                return@launch
            }

            val result = try {
                conn.connect(device)
            } catch (e: SecurityException) {
                Log.e("OBDRepository", "SecurityException during connect", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("OBDRepository", "Exception during connect", e)
                Result.failure(e)
            }
            if (result.isFailure) {
                val msg = result.exceptionOrNull()?.message ?: "Connection failed"
                _connectionState.value = OBDConnectionState.Error(msg)
                _lastError.value = msg
                if (_autoReconnect.value) { scheduleReconnect(address) }
                return@launch
            }

            if (connectAttempt.get() != attempt) { return@launch }
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
        connectAttempt.incrementAndGet()
        reconnectJob?.cancel()
        reconnectJob = null
        stopRemoteServer()
        stopGPSTracking()
        pollingJob?.cancel()
        pollingJob = null
        flushRecordedData()
        connection?.disconnect()
        _connectionState.value = OBDConnectionState.Disconnected
        _obdData.value = OBDData()
        _dtcResponse.value = null
        trendRecorder.clear()
        _trendHistory.value = TrendHistory()
        // Save trip to Room Database on a dedicated scope so cleanup()'s scope.cancel()
        // doesn't interrupt the room insert before the user-disconnect writes are flushed.
        val trip = _tripData.value
        val capturedStartTime = synchronized(tripLock) { tripStartTime }
        if (trip.distanceKm > 0.1) {
            persistTripScope.launch {
                try {
                    tripDao.insert(
                        TripEntity(
                            startTime = capturedStartTime,
                            endTime = System.currentTimeMillis(),
                            distanceKm = trip.distanceKm.toFloat(),
                            avgSpeedKmh = trip.avgSpeedKmh.toFloat(),
                            maxSpeedKmh = trip.maxSpeedKmh.toFloat(),
                            avgRpm = trip.avgRpm,
                            maxRpm = trip.maxRpm,
                            fuelUsedLiters = trip.totalFuelUsed.toFloat(),
                            vin = storedVin
                        )
                    )
                } catch (e: Exception) {
                    Log.e("OBDRepository", "Failed to save trip to Room: ${e.message}")
                }
            }
        }
        saveTripData()
    }

    suspend fun sendRawCommand(cmd: String): String? {
        return try {
            connection?.sendRawCommand(cmd)
        } catch (e: Exception) {
            Log.e("OBDRepository", "sendRawCommand failed: ${e.message}")
            null
        }
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
        runCatching { connection?.disconnect() }
        runCatching { remoteBridge?.stopServer() }
        runCatching { gpsTracker.cleanup() }
        scope.cancel()
        persistTripScope.cancel()
    }

    private fun resetConnectionStats() {
        _connectionStats.value = ConnectionStats()
    }

    private fun recordConnectionSuccess() {
        _connectionStats.update { s ->
            val newSuccess = s.successCount + 1
            val newFailure = s.failureCount
            val total = (newSuccess + newFailure).coerceAtLeast(1)
            val rate = newSuccess.toDouble() / total
            s.copy(
                successCount = newSuccess,
                failureCount = newFailure,
                quality = ConnectionQuality.fromSuccessRate(rate)
            )
        }
    }

    private fun recordConnectionFailure() {
        _connectionStats.update { s ->
            val newSuccess = s.successCount
            val newFailure = s.failureCount + 1
            val total = (newSuccess + newFailure).coerceAtLeast(1)
            val rate = newSuccess.toDouble() / total
            s.copy(
                successCount = newSuccess,
                failureCount = newFailure,
                quality = ConnectionQuality.fromSuccessRate(rate)
            )
        }
    }

    private fun resetTripAccumulators() {
        synchronized(tripLock) {
            tripSamples = 0
            tripSpeedSum = 0.0
            tripRpmSum = 0.0
            tripFuelUsedSum = 0.0
            tripPrevSpeed = 0.0
            tripPrevTimestamp = System.currentTimeMillis()
            tripStartTime = tripPrevTimestamp
            tripFuelStart = _obdData.value.fuelLevel
            _tripData.value = TripData(fuelStartLevel = tripFuelStart, vin = storedVin)
        }
    }

    private fun updateTripData(speed: Double, rpm: Double, fuelRate: Double, fuelEndLevel: Double, now: Long) {
        synchronized(tripLock) {
            tripSamples++
            tripSpeedSum += speed
            tripRpmSum += rpm
            tripFuelUsedSum += fuelRate * (_pollRate.value / 3_600_000.0)

            val dtHours = (now - tripPrevTimestamp) / 3_600_000.0
            if (dtHours > 0) {
                val clampedSpeed = speed.coerceAtLeast(0.0)
                val distanceKm = ((tripPrevSpeed + clampedSpeed) / 2.0 * dtHours).coerceAtLeast(0.0)
                _tripData.value = _tripData.value.copy(
                    durationSeconds = (now - tripStartTime) / 1000L,
                    distanceKm = _tripData.value.distanceKm + distanceKm,
                    maxSpeedKmh = maxOf(_tripData.value.maxSpeedKmh, clampedSpeed),
                    avgSpeedKmh = tripSpeedSum / tripSamples.coerceAtLeast(1),
                    maxRpm = maxOf(_tripData.value.maxRpm, rpm),
                    avgRpm = tripRpmSum / tripSamples.coerceAtLeast(1),
                    sampleCount = tripSamples,
                    totalFuelUsed = tripFuelUsedSum,
                    avgFuelRate = if (tripSamples > 0 && tripFuelUsedSum > 0) {
                        val elapsedHours = ((now - tripStartTime) / 3_600_000.0).coerceAtLeast(0.001)
                        tripFuelUsedSum / elapsedHours
                    } else { 0.0 },
                    fuelStartLevel = tripFuelStart,
                    fuelEndLevel = fuelEndLevel,
                    vin = storedVin
                )
            }
            tripPrevSpeed = speed.coerceAtLeast(0.0)
            tripPrevTimestamp = now
        }
    }

    private fun updateTrendIfNeeded(data: OBDData, now: Long) {
        if (now - lastTrendRecordTime >= trendRecordInterval) {
            trendRecorder.record(
                data.rpm, data.speed, data.coolantTemp, data.boostPressure,
                data.wastegateControl, data.turboRpm, data.egtBank1, data.chargeAirCoolerTemp
            )
            _trendHistory.value = trendRecorder.getHistory()
            lastTrendRecordTime = now
        }
    }

    private fun updateWidgetPreferences(data: OBDData, now: Long) {
        if (now - lastWidgetUpdateTime >= widgetUpdateInterval) {
            lastWidgetUpdateTime = now
            val unit = _measurementUnit.value
            prefs.edit()
                .putFloat("widget_rpm", data.rpm.toFloat())
                .putFloat("widget_speed", unit.convertSpeed(data.speed).toFloat())
                .putFloat("widget_coolant", unit.convertTemp(data.coolantTemp).toFloat())
                .putFloat("widget_load", data.engineLoad.toFloat())
                .putFloat("widget_fuel", data.fuelLevel.toFloat())
                .putBoolean("unit_metric", unit == MeasurementUnit.METRIC)
                .apply()
        }
    }

    private fun startPolling(conn: ELM327BTConnection) {
        pollingJob?.cancel()
        resetTripAccumulators()

        pollingJob = scope.launch {
            while (isActive) {
                try {
                    val results = conn.readMultiplePIDs(pollPIDs)
                    val batteryVoltage = conn.getBatteryVoltage() ?: _obdData.value.batteryVoltage
                    val speed = results[OBDPID.SPEED] ?: _obdData.value.speed
                    val rpm = results[OBDPID.RPM] ?: _obdData.value.rpm
                    val fuelRate = results[OBDPID.ENGINE_FUEL_RATE] ?: 0.0
                    val now = System.currentTimeMillis()

                    updateTripData(speed, rpm, fuelRate, results[OBDPID.FUEL_LEVEL] ?: _tripData.value.fuelEndLevel, now)

                    _obdData.value = OBDData(
                        rpm = results[OBDPID.RPM] ?: _obdData.value.rpm,
                        speed = speed,
                        coolantTemp = results[OBDPID.COOLANT_TEMP] ?: _obdData.value.coolantTemp,
                        intakeTemp = results[OBDPID.INTAKE_TEMP] ?: _obdData.value.intakeTemp,
                        throttle = results[OBDPID.THROTTLE] ?: _obdData.value.throttle,
                        engineLoad = results[OBDPID.ENGINE_LOAD] ?: _obdData.value.engineLoad,
                        fuelLevel = results[OBDPID.FUEL_LEVEL] ?: _obdData.value.fuelLevel,
                        batteryVoltage = batteryVoltage,
                        timingAdvance = results[OBDPID.TIMING_ADVANCE] ?: _obdData.value.timingAdvance,
                        mafRate = results[OBDPID.MAF_RATE] ?: _obdData.value.mafRate,
                        fuelPressure = results[OBDPID.FUEL_PRESSURE] ?: _obdData.value.fuelPressure,
                        intakePressure = results[OBDPID.INTAKE_PRESSURE] ?: _obdData.value.intakePressure,
                        runTime = results[OBDPID.RUN_TIME] ?: _obdData.value.runTime,
                        fuelRailPressure = results[OBDPID.FUEL_RAIL_PRESSURE] ?: _obdData.value.fuelRailPressure,
                        commandedEGR = results[OBDPID.COMMANDED_EGR] ?: _obdData.value.commandedEGR,
                        egrTemp = results[OBDPID.EGR_TEMP] ?: _obdData.value.egrTemp,
                        commandedEvapPurge = results[OBDPID.COMMANDED_EVAPORATIVE_PURGE] ?: _obdData.value.commandedEvapPurge,
                        barometricPressure = results[OBDPID.BAROMETRIC_PRESSURE] ?: _obdData.value.barometricPressure,
                        o2VoltageB1S1 = results[OBDPID.O2_VOLTAGE_B1S1] ?: _obdData.value.o2VoltageB1S1,
                        o2VoltageB1S2 = results[OBDPID.O2_VOLTAGE_B1S2] ?: _obdData.value.o2VoltageB1S2,
                        catalystTemp = results[OBDPID.CATALYST_TEMP_B1S1] ?: _obdData.value.catalystTemp,
                        controlModuleVoltage = results[OBDPID.CONTROL_MODULE_VOLTAGE] ?: _obdData.value.controlModuleVoltage,
                        absoluteLoadValue = results[OBDPID.ABSOLUTE_LOAD_VALUE] ?: _obdData.value.absoluteLoadValue,
                        engineFuelRate = fuelRate,
                        shortTermFuelTrimB1 = results[OBDPID.SHORT_TERM_FUEL_TRIM_BANK1] ?: _obdData.value.shortTermFuelTrimB1,
                        longTermFuelTrimB1 = results[OBDPID.LONG_TERM_FUEL_TRIM_BANK1] ?: _obdData.value.longTermFuelTrimB1,
                        shortTermFuelTrimB2 = results[OBDPID.SHORT_TERM_FUEL_TRIM_BANK2] ?: _obdData.value.shortTermFuelTrimB2,
                        longTermFuelTrimB2 = results[OBDPID.LONG_TERM_FUEL_TRIM_BANK2] ?: _obdData.value.longTermFuelTrimB2,
                        fuelAirRatio = results[OBDPID.FUEL_AIR_EQUIV_RATIO] ?: _obdData.value.fuelAirRatio,
                        acceleratorPosD = results[OBDPID.ACCELERATOR_POS_D] ?: _obdData.value.acceleratorPosD,
                        throttleC = results[OBDPID.THROTTLE_C] ?: _obdData.value.throttleC,
                        throttleActuator = results[OBDPID.THROTTLE_ACTUATOR] ?: _obdData.value.throttleActuator,
                        hybridBatteryRemaining = results[OBDPID.HYBRID_BATTERY_REMAINING] ?: _obdData.value.hybridBatteryRemaining,
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
                        timestamp = now,
                        distanceWithMil = results[OBDPID.DISTANCE_MIL] ?: _obdData.value.distanceWithMil
                    )

                    gpsTracker.updateTripOBDData(
                        avgRpm = tripRpmSum / tripSamples.coerceAtLeast(1),
                        maxRpm = results[OBDPID.RPM]?.let { maxOf(_tripData.value.maxRpm, it) } ?: _tripData.value.maxRpm,
                        fuelUsedLiters = tripFuelUsedSum.toFloat(),
                        vin = storedVin
                    )

                    mode22Counter++
                    if (mode22Counter >= 5) {
                        mode22Counter = 0
                        try {
                            val mode22Results = conn.readMultipleMode22PIDs(
                                listOf(
                                    Mode22PIDs.TURBO_BOOST_ACTUAL,
                                    Mode22PIDs.TURBO_BOOST_TARGET,
                                    Mode22PIDs.WASTEGATE_DUTY,
                                    Mode22PIDs.TURBO_SPEED,
                                    Mode22PIDs.ENGINE_OIL_TEMP,
                                    Mode22PIDs.TRANS_FLUID_TEMP,
                                    Mode22PIDs.WIDEBAND_LAMBDA_B1,
                                    Mode22PIDs.KNOCK_RETARD
                                )
                            )
                            val m = _mode22Data.value
                            _mode22Data.value = m.copy(
                                turboBoostActual = mode22Results[Mode22PIDs.TURBO_BOOST_ACTUAL] ?: m.turboBoostActual,
                                turboBoostTarget = mode22Results[Mode22PIDs.TURBO_BOOST_TARGET] ?: m.turboBoostTarget,
                                wastegateDuty = mode22Results[Mode22PIDs.WASTEGATE_DUTY] ?: m.wastegateDuty,
                                turboSpeed = mode22Results[Mode22PIDs.TURBO_SPEED] ?: m.turboSpeed,
                                engineTorque = mode22Results[Mode22PIDs.ENGINE_TORQUE] ?: m.engineTorque,
                                timestamp = System.currentTimeMillis()
                            )
                            val current = _obdData.value
                            _obdData.value = current.copy(
                                turboRpmMode22 = mode22Results[Mode22PIDs.TURBO_SPEED] ?: current.turboRpmMode22,
                                boostPressureTargetMode22 = mode22Results[Mode22PIDs.TURBO_BOOST_TARGET] ?: current.boostPressureTargetMode22,
                                wastegatePositionMode22 = mode22Results[Mode22PIDs.WASTEGATE_DUTY] ?: current.wastegatePositionMode22,
                                boostPressureActualMode22 = mode22Results[Mode22PIDs.TURBO_BOOST_ACTUAL] ?: current.boostPressureActualMode22,
                                oilTempMode22 = mode22Results[Mode22PIDs.ENGINE_OIL_TEMP] ?: current.oilTempMode22
                            )
                        } catch (e: Exception) {
                            Log.w("OBDRepository", "Mode 22 polling failed: ${e.message}")
                        }
                    }

                    updateTrendIfNeeded(_obdData.value, now)
                    updateWidgetPreferences(_obdData.value, now)

                    consecutivePollingFailures = 0
                    _lastError.value = null
                    recordConnectionSuccess()
                    checkAlerts()
                    if (_recordingActive.value) { recordData() }
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

    private var remoteClientsJob: Job? = null

    fun startRemoteServer(port: Int = RemoteBridge.DEFAULT_PORT): Result<Int> {
        val bridge = remoteBridge ?: return Result.failure(IllegalStateException("Not connected to ELM327"))
        val result = bridge.startServer(port)
        if (result.isSuccess) {
            _remoteServerIp.value = bridge.getLocalIpAddress()
            _remoteServerPort.value = bridge.serverPort.value
            _remoteServerRunning.value = true
            remoteClientsJob?.cancel()
            remoteClientsJob = scope.launch {
                bridge.connectedClients.collect { count ->
                    _remoteConnectedClients.value = count
                }
            }
        }
        return result
    }

    fun stopRemoteServer() {
        remoteClientsJob?.cancel()
        remoteClientsJob = null
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
            if (conn.clearDTCs()) { _dtcResponse.value = DTCResponse(emptyList()) }
        }
    }

    fun readTPMS() {
        scope.launch {
            val current = _tpmsReading.value
            if (current.frontLeftPSI == 0.0 && current.frontRightPSI == 0.0) {
                android.util.Log.d("OBDRepository", "TPMS: no data on CAN bus (CAN ID 420/422). TPMS requires BCM polling via UDS routine 0x0302")
            }
        }
    }

    fun readClimate() {
        scope.launch {
            val current = _climateReading.value
            if (current.fanSpeed == 0 && !current.isACEnabled) {
                android.util.Log.d("OBDRepository", "Climate: no data on CAN bus (CAN ID 7E5/7ED). Climate requires HVAC CAN monitoring")
            }
        }
    }

    fun startRecording() {
        _recordingActive.value = true
        synchronized(recordedDataBuffer) { recordedDataBuffer.clear() }
        _recordedData.value = emptyList()
    }

    fun stopRecording() {
        _recordingActive.value = false
        flushRecordedData()
    }

    private fun recordData() {
        val d = _obdData.value
        val loc = currentLocation.value
        val record = DataRecord(
            timestamp = d.timestamp,
            rpm = d.rpm,
            speed = d.speed,
            coolantTemp = d.coolantTemp,
            throttle = d.throttle,
            fuelLevel = d.fuelLevel,
            batteryVoltage = d.batteryVoltage,
            intakeTemp = d.intakeTemp,
            oilTemp = d.oilTemp,
            boostPressure = d.boostPressure,
            barometricPressure = d.barometricPressure,
            wastegateDuty = d.wastegateControl,
            turboRpm = d.turboRpm,
            egtBank1 = d.egtBank1,
            egtBank2 = d.egtBank2,
            chargeAirTemp = d.chargeAirCoolerTemp,
            mafRate = d.mafRate,
            engineLoad = d.engineLoad,
            shortTermFuelTrimB1 = d.shortTermFuelTrimB1,
            longTermFuelTrimB1 = d.longTermFuelTrimB1,
            timingAdvance = d.timingAdvance,
            latitude = loc?.latitude,
            longitude = loc?.longitude,
            altitude = loc?.altitude
        )
        synchronized(recordedDataBuffer) {
            recordedDataBuffer.add(record)
            if (recordedDataBuffer.size > MAX_RECORDED_SAMPLES) {
                val removeCount = recordedDataBuffer.size - MAX_RECORDED_SAMPLES
                recordedDataBuffer.subList(0, removeCount).clear()
            }
            if (recordedDataBuffer.size % 50 == 0) {
                _recordedData.value = recordedDataBuffer.toList()
            }
        }
    }

    private fun flushRecordedData() {
        synchronized(recordedDataBuffer) {
            if (recordedDataBuffer.isNotEmpty()) {
                _recordedData.value = recordedDataBuffer.toList()
            }
        }
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

    suspend fun deleteTrip(id: Long) {
        tripDao.deleteById(id)
    }

    suspend fun clearTripHistory() {
        tripDao.deleteAll()
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
        synchronized(tripLock) {
            tripStartTime = System.currentTimeMillis()
            tripSamples = 0
            tripSpeedSum = 0.0
            tripRpmSum = 0.0
            tripFuelUsedSum = 0.0
            tripFuelStart = _obdData.value.fuelLevel
            _tripData.value = TripData(fuelStartLevel = tripFuelStart, vin = storedVin)
        }
    }

    fun getLastDevice(): String? = prefs.getString("last_device", null)

    fun exportToCsv(): String = DataExporter.exportCsv(_recordedData.value, enhanced = true)

    fun exportRecordedData(format: ExportFormat): String =
        DataExporter.export(_recordedData.value, format)

    fun clearRecordedData() {
        synchronized(recordedDataBuffer) { recordedDataBuffer.clear() }
        _recordedData.value = emptyList()
    }

    private fun checkAlerts() {
        val cfg = _alertConfig.value
        val d = _obdData.value
        val now = System.currentTimeMillis()
        val cooldownMs = cfg.cooldownSeconds * 1000L

        val candidateAlerts = mutableListOf<ActiveAlert>()

        candidateAlerts.addAll(evaluateThreshold(
            AlertType.SPEED, cfg.speedWarningEnabled, d.speed.toFloat(), cfg.speedWarning,
            AlertSeverity.WARNING,
            "Geschwindigkeit: %.0f > %.0f km/h"
        ))
        candidateAlerts.addAll(evaluateThreshold(
            AlertType.COOLANT, cfg.coolantWarningEnabled, d.coolantTemp.toFloat(), cfg.coolantWarning,
            AlertSeverity.WARNING,
            "Kühlmittel: %.0f°C > %.0f°C"
        ))
        candidateAlerts.addAll(evaluateThresholdLow(
            AlertType.FUEL, cfg.fuelWarningEnabled, d.fuelLevel.toFloat(), cfg.fuelWarning,
            AlertSeverity.WARNING,
            "Kraftstoff: %.0f%% < %.0f%%"
        ))
        candidateAlerts.addAll(evaluateThreshold(
            AlertType.RPM, cfg.rpmWarningEnabled, d.rpm.toFloat(), cfg.rpmWarning,
            AlertSeverity.WARNING,
            "Drehzahl: %.0f > %.0f rpm"
        ))
        candidateAlerts.addAll(evaluateBatteryAlert(cfg))
        candidateAlerts.addAll(evaluateBoostAlerts(cfg, d))
        candidateAlerts.addAll(evaluateEgtAlerts(cfg, d))
        candidateAlerts.addAll(evaluateOilTempAlerts(cfg, d))
        candidateAlerts.addAll(evaluateTurboSpeedAlert(cfg, d))
        candidateAlerts.addAll(evaluateChargeAirTempAlert(cfg, d))
        candidateAlerts.addAll(evaluateFuelTrimAlert(cfg, d))

        val filtered = candidateAlerts.filter { alert ->
            val lastTrigger = lastAlertTrigger[alert.type]
            if (lastTrigger == null || now - lastTrigger > cooldownMs) {
                lastAlertTrigger[alert.type] = now
                true
            } else {
                false
            }
        }

        _activeAlerts.value = filtered
    }

    private fun evaluateThreshold(
        type: AlertType, enabled: Boolean, value: Float, threshold: Float,
        severity: AlertSeverity, messageFormat: String
    ): List<ActiveAlert> {
        if (!enabled) { return emptyList() }
        if (value > threshold) {
            return listOf(ActiveAlert(
                type = type,
                severity = severity,
                value = value,
                threshold = threshold,
                message = messageFormat.format(value, threshold)
            ))
        }
        return emptyList()
    }

    private fun evaluateThresholdLow(
        type: AlertType, enabled: Boolean, value: Float, threshold: Float,
        severity: AlertSeverity, messageFormat: String
    ): List<ActiveAlert> {
        if (!enabled) { return emptyList() }
        if (value in 0.1f..threshold) {
            return listOf(ActiveAlert(
                type = type,
                severity = severity,
                value = value,
                threshold = threshold,
                message = messageFormat.format(value, threshold)
            ))
        }
        return emptyList()
    }

    private fun evaluateBatteryAlert(cfg: AlertConfig): List<ActiveAlert> {
        if (!cfg.batteryLowWarningEnabled) { return emptyList() }
        val d = _obdData.value
        if (d.batteryVoltage <= 0 || d.batteryVoltage >= cfg.batteryLowWarning) { return emptyList() }
        return listOf(ActiveAlert(
            type = AlertType.BATTERY,
            severity = AlertSeverity.WARNING,
            value = d.batteryVoltage.toFloat(),
            threshold = cfg.batteryLowWarning,
            message = "Batterie: %.1fV < %.1fV".format(d.batteryVoltage, cfg.batteryLowWarning)
        ))
    }

    private fun evaluateBoostAlerts(cfg: AlertConfig, d: OBDData): List<ActiveAlert> {
        if (!cfg.boostWarningEnabled && !cfg.boostCriticalEnabled) { return emptyList() }
        if (d.barometricPressure <= 0) { return emptyList() }
        val absoluteBoost = if (d.boostPressure > 0) { d.boostPressure } else { d.intakePressure }
        val boostBar = ((absoluteBoost - d.barometricPressure).coerceAtLeast(0.0) / 100.0).toFloat()
        return when {
            cfg.boostCriticalEnabled && boostBar > cfg.boostCritical -> listOf(ActiveAlert(
                type = AlertType.BOOST,
                severity = AlertSeverity.CRITICAL,
                value = boostBar,
                threshold = cfg.boostCritical,
                message = "ÜBERLADUNG! Ladedruck %.2f bar > %.2f bar – SOFORT PEDAL LOSLASSEN".format(boostBar, cfg.boostCritical)
            ))
            cfg.boostWarningEnabled && boostBar > cfg.boostWarning -> listOf(ActiveAlert(
                type = AlertType.BOOST,
                severity = AlertSeverity.WARNING,
                value = boostBar,
                threshold = cfg.boostWarning,
                message = "Ladedruck hoch: %.2f bar > %.2f bar".format(boostBar, cfg.boostWarning)
            ))
            else -> emptyList()
        }
    }

    private fun evaluateEgtAlerts(cfg: AlertConfig, d: OBDData): List<ActiveAlert> {
        if (!cfg.egtWarningEnabled && !cfg.egtCriticalEnabled) { return emptyList() }
        val egt = d.egtBank1.toFloat()
        if (egt <= 0) { return emptyList() }
        return when {
            cfg.egtCriticalEnabled && egt > cfg.egtCritical -> listOf(ActiveAlert(
                type = AlertType.EGT,
                severity = AlertSeverity.CRITICAL,
                value = egt,
                threshold = cfg.egtCritical,
                message = "Abgastemperatur kritisch! EGT %.0f°C > %.0f°C – Motorlast sofort reduzieren".format(egt, cfg.egtCritical)
            ))
            cfg.egtWarningEnabled && egt > cfg.egtWarning -> listOf(ActiveAlert(
                type = AlertType.EGT,
                severity = AlertSeverity.WARNING,
                value = egt,
                threshold = cfg.egtWarning,
                message = "Abgastemperatur erhöht: %.0f°C > %.0f°C".format(egt, cfg.egtWarning)
            ))
            else -> emptyList()
        }
    }

    private fun evaluateOilTempAlerts(cfg: AlertConfig, d: OBDData): List<ActiveAlert> {
        if (!cfg.oilTempWarningEnabled && !cfg.oilTempCriticalEnabled) { return emptyList() }
        val oilTemp = (if (d.oilTempMode22 > 0) { d.oilTempMode22 } else { d.oilTemp }).toFloat()
        if (oilTemp <= 0) { return emptyList() }
        return when {
            cfg.oilTempCriticalEnabled && oilTemp > cfg.oilTempCritical -> listOf(ActiveAlert(
                type = AlertType.OIL_TEMP,
                severity = AlertSeverity.CRITICAL,
                value = oilTemp,
                threshold = cfg.oilTempCritical,
                message = "Öltemperatur kritisch! %.0f°C > %.0f°C".format(oilTemp, cfg.oilTempCritical)
            ))
            cfg.oilTempWarningEnabled && oilTemp > cfg.oilTempWarning -> listOf(ActiveAlert(
                type = AlertType.OIL_TEMP,
                severity = AlertSeverity.WARNING,
                value = oilTemp,
                threshold = cfg.oilTempWarning,
                message = "Öltemperatur erhöht: %.0f°C > %.0f°C".format(oilTemp, cfg.oilTempWarning)
            ))
            else -> emptyList()
        }
    }

    private fun evaluateTurboSpeedAlert(cfg: AlertConfig, d: OBDData): List<ActiveAlert> {
        if (!cfg.turboSpeedWarningEnabled) { return emptyList() }
        val turboRpm = d.turboRpm.toFloat()
        if (turboRpm <= 0 || turboRpm <= cfg.turboSpeedWarning) { return emptyList() }
        return listOf(ActiveAlert(
            type = AlertType.TURBO_SPEED,
            severity = AlertSeverity.CRITICAL,
            value = turboRpm,
            threshold = cfg.turboSpeedWarning,
            message = "Turbo-Drehzahl überschritten: %.0f > %.0f rpm".format(turboRpm, cfg.turboSpeedWarning)
        ))
    }

    private fun evaluateChargeAirTempAlert(cfg: AlertConfig, d: OBDData): List<ActiveAlert> {
        if (!cfg.chargeAirTempWarningEnabled) { return emptyList() }
        val cat = d.chargeAirCoolerTemp.toFloat()
        if (cat <= 0 || cat <= cfg.chargeAirTempWarning) { return emptyList() }
        return listOf(ActiveAlert(
            type = AlertType.CHARGE_AIR_TEMP,
            severity = AlertSeverity.WARNING,
            value = cat,
            threshold = cfg.chargeAirTempWarning,
            message = "Ladelufttemperatur erhöht: %.0f°C > %.0f°C".format(cat, cfg.chargeAirTempWarning)
        ))
    }

    private fun evaluateFuelTrimAlert(cfg: AlertConfig, d: OBDData): List<ActiveAlert> {
        if (!cfg.fuelTrimWarningEnabled) { return emptyList() }
        val totalTrim = kotlin.math.abs(d.shortTermFuelTrimB1 + d.longTermFuelTrimB1).toFloat()
        if (totalTrim <= cfg.fuelTrimWarning) { return emptyList() }
        return listOf(ActiveAlert(
            type = AlertType.FUEL_TRIM,
            severity = AlertSeverity.WARNING,
            value = totalTrim,
            threshold = cfg.fuelTrimWarning,
            message = "Kraftstofftrim außerhalb Bereich: %.1f%% > ±%.1f%%".format(totalTrim, cfg.fuelTrimWarning)
        ))
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
            .putFloat("alert_boost", config.boostWarning)
            .putBoolean("alert_boost_on", config.boostWarningEnabled)
            .putFloat("alert_boost_crit", config.boostCritical)
            .putBoolean("alert_boost_crit_on", config.boostCriticalEnabled)
            .putFloat("alert_egt", config.egtWarning)
            .putBoolean("alert_egt_on", config.egtWarningEnabled)
            .putFloat("alert_egt_crit", config.egtCritical)
            .putBoolean("alert_egt_crit_on", config.egtCriticalEnabled)
            .putFloat("alert_oil", config.oilTempWarning)
            .putBoolean("alert_oil_on", config.oilTempWarningEnabled)
            .putFloat("alert_oil_crit", config.oilTempCritical)
            .putBoolean("alert_oil_crit_on", config.oilTempCriticalEnabled)
            .putFloat("alert_turbo_rpm", config.turboSpeedWarning)
            .putBoolean("alert_turbo_rpm_on", config.turboSpeedWarningEnabled)
            .putFloat("alert_cat", config.chargeAirTempWarning)
            .putBoolean("alert_cat_on", config.chargeAirTempWarningEnabled)
            .putFloat("alert_fuel_trim", config.fuelTrimWarning)
            .putBoolean("alert_fuel_trim_on", config.fuelTrimWarningEnabled)
            .putBoolean("alert_sound", config.soundEnabled)
            .putBoolean("alert_vibration", config.vibrationEnabled)
            .putInt("alert_hysteresis", config.hysteresisSeconds)
            .putInt("alert_cooldown", config.cooldownSeconds)
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
                    batteryLowWarningEnabled = config.batteryLowWarningEnabled,
                    boostWarning = config.boostWarning,
                    boostWarningEnabled = config.boostWarningEnabled,
                    boostCritical = config.boostCritical,
                    boostCriticalEnabled = config.boostCriticalEnabled,
                    egtWarning = config.egtWarning,
                    egtWarningEnabled = config.egtWarningEnabled,
                    egtCritical = config.egtCritical,
                    egtCriticalEnabled = config.egtCriticalEnabled,
                    oilTempWarning = config.oilTempWarning,
                    oilTempWarningEnabled = config.oilTempWarningEnabled,
                    oilTempCritical = config.oilTempCritical,
                    oilTempCriticalEnabled = config.oilTempCriticalEnabled,
                    turboSpeedWarning = config.turboSpeedWarning,
                    turboSpeedWarningEnabled = config.turboSpeedWarningEnabled,
                    chargeAirTempWarning = config.chargeAirTempWarning,
                    chargeAirTempWarningEnabled = config.chargeAirTempWarningEnabled,
                    fuelTrimWarning = config.fuelTrimWarning,
                    fuelTrimWarningEnabled = config.fuelTrimWarningEnabled,
                    soundEnabled = config.soundEnabled,
                    vibrationEnabled = config.vibrationEnabled,
                    hysteresisSeconds = config.hysteresisSeconds,
                    cooldownSeconds = config.cooldownSeconds
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
        val lines = csvContent.lines().drop(1).filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            _importedData.value = emptyList()
            return
        }

        val header = csvContent.lineSequence().firstOrNull() ?: ""
        val isTorqueFormat = header.contains("Device Time", ignoreCase = true) ||
            header.contains("Torque", ignoreCase = true)
        val isEnhanced = header.contains("Boost", ignoreCase = true) ||
            header.contains("EGT", ignoreCase = true)

        for (line in lines) {
            val parts = line.split(",")
            try {
                when {
                    isTorqueFormat && parts.size >= 5 -> {
                        entries.add(CsvImportEntry(
                            timestamp = parts[0].toLongOrNull() ?: 0L,
                            rpm = parts[5].toDoubleOrNull() ?: 0.0,
                            speed = (parts[6].toDoubleOrNull() ?: 0.0) * 1.60934,
                            coolantTemp = ((parts[7].toDoubleOrNull() ?: 0.0) - 32.0) * 5.0 / 9.0,
                            throttle = parts[8].toDoubleOrNull() ?: 0.0,
                            fuelLevel = parts[9].toDoubleOrNull() ?: 0.0,
                            batteryVoltage = parts[10].toDoubleOrNull() ?: 0.0
                        ))
                    }
                    isEnhanced && parts.size >= 22 -> {
                        entries.add(CsvImportEntry(
                            timestamp = parts[0].toLongOrNull() ?: 0L,
                            rpm = parts[2].toDoubleOrNull() ?: 0.0,
                            speed = parts[3].toDoubleOrNull() ?: 0.0,
                            coolantTemp = parts[4].toDoubleOrNull() ?: 0.0,
                            throttle = parts[5].toDoubleOrNull() ?: 0.0,
                            fuelLevel = parts[6].toDoubleOrNull() ?: 0.0,
                            batteryVoltage = parts[7].toDoubleOrNull() ?: 0.0
                        ))
                    }
                    parts.size >= 7 -> {
                        entries.add(CsvImportEntry(
                            timestamp = parts[0].toLongOrNull() ?: 0L,
                            rpm = parts[1].toDoubleOrNull() ?: 0.0,
                            speed = parts[2].toDoubleOrNull() ?: 0.0,
                            coolantTemp = parts[3].toDoubleOrNull() ?: 0.0,
                            throttle = parts[4].toDoubleOrNull() ?: 0.0,
                            fuelLevel = parts[5].toDoubleOrNull() ?: 0.0,
                            batteryVoltage = parts[6].toDoubleOrNull() ?: 0.0
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.w("OBDRepository", "Failed to parse CSV line: ${parts.joinToString(",")}", e)
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
                    lastServiceDate = System.currentTimeMillis(),
                    notes = item.notes
                )
            )
        }
    }

    fun updateMaintenanceNotes(type: MaintenanceType, notes: String) {
        prefs.edit().putString("maint_${type.name}_notes", notes).apply()
        scope.launch {
            maintenanceDao.updateNotes(type.name, notes)
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
                    lastServiceDate = prefs.getLong("maint_${type.name}_date", 0L),
                    notes = prefs.getString("maint_${type.name}_notes", "") ?: ""
                )
            } else { null }
        }
    }

    suspend fun loadMaintenanceItemsFromRoom(): List<MaintenanceItem> {
        val entities = maintenanceDao.getAllOnce()
        return entities.mapNotNull { entity ->
            MaintenanceType.entries.find { it.name == entity.type }?.let { type ->
                MaintenanceItem(
                    type = type,
                    lastServiceKm = entity.lastServiceKm,
                    intervalKm = entity.intervalKm,
                    lastServiceDate = entity.lastServiceDate,
                    notes = entity.notes
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
            edit.remove("maint_${type.name}_notes")
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
                } else { l100km }
                return FuelEconomyData(
                    currentL100km = l100km,
                    avgL100km = avgL100km,
                    currentKmL = kmL,
                    avgKmL = if (avgL100km > 0.5) { 100.0 / avgL100km } else { 0.0 },
                    currentMpgUs = mpgUs,
                    avgMpgUs = if (avgL100km > 0.5) { 235.214583 / avgL100km } else { 0.0 },
                    currentMpgUk = mpgUk,
                    avgMpgUk = if (avgL100km > 0.5) { 282.4809363 / avgL100km } else { 0.0 },
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
        return if (id != null) { CarProfile.fromId(id) ?: CarProfile.default() } else { CarProfile.default() }
    }

    fun setEmulatorMode(enabled: Boolean) {
        _emulatorMode.value = enabled
        prefs.edit().putBoolean("emulator_mode", enabled).apply()
        if (enabled) {
            connectEmulator()
        } else {
            disconnectEmulator()
        }
    }

    fun connectEmulator() {
        if (emulator != null) { return }
        emulator = OBDEmulator()
        _connectionState.value = OBDConnectionState.Connected
        _detectedProtocol.value = "OBD Emulator (Simulated)"
        resetTripAccumulators()
        startEmulatorPolling()
    }

    fun disconnectEmulator() {
        emulatorPollingJob?.cancel()
        emulatorPollingJob = null
        flushRecordedData()
        emulator?.disconnect()
        emulator = null
        _connectionState.value = OBDConnectionState.Disconnected
        _obdData.value = OBDData()
        trendRecorder.clear()
        _trendHistory.value = TrendHistory()
    }

    private fun startEmulatorPolling() {
        emulatorPollingJob?.cancel()
        emulatorPollingJob = scope.launch {
            while (isActive) {
                try {
                    emulator?.let { emu ->
                        val data = emu.generateData(_pollRate.value)
                        val mode22 = emu.generateMode22Data()

                        if (!isActive) { return@launch }
                        _obdData.value = data
                        _mode22Data.value = mode22

                        val now = System.currentTimeMillis()
                        updateTripData(data.speed, data.rpm, data.engineFuelRate, data.fuelLevel, now)
                        updateTrendIfNeeded(data, now)
                        updateWidgetPreferences(data, now)

                        recordConnectionSuccess()
                        checkAlerts()
                        if (_recordingActive.value) { recordData() }
                    }
                } catch (e: Exception) {
                    Log.e("OBDRepository", "Emulator polling error: ${e.message}")
                }
                delay(_pollRate.value)
            }
        }
    }

    fun processCANMessage(canId: String, data: ByteArray) {
        val hvacParsed = try {
            com.canopobd.protocol.BCMProtocol.CANParser.parseHVACMessage(canId, data)
        } catch (e: Exception) {
            null
        }

        hvacParsed?.let { hvac ->
            _climateReading.value = ClimateReading(
                driverTempCelsius = hvac.driverTemp.toInt(),
                passengerTempCelsius = hvac.passengerTemp.toInt(),
                fanSpeed = hvac.fanSpeed,
                isACEnabled = hvac.acCompressorActive,
                isAutoMode = hvac.autoModeActive,
                isRecirculation = hvac.recirculationActive,
                isFrontDefrost = hvac.frontDefrostActive,
                isRearDefrost = hvac.rearDefrostActive,
                outsideTemp = hvac.outsideTempCelsius,
                cabinTemp = hvac.cabinTempCelsius,
                acCompressorActive = hvac.acCompressorActive,
                timestamp = hvac.timestamp
            )
            Log.d("OBDRepository", "Updated climate from CAN: AC=${hvac.acCompressorActive}, Fan=${hvac.fanSpeed}")
        }

        val tpmsParsed = try {
            com.canopobd.protocol.BCMProtocol.CANParser.parseTPMSMessage(canId, data)
        } catch (e: Exception) {
            null
        }

        tpmsParsed?.let { tpms ->
            _tpmsReading.value = TPMSReading(
                frontLeftPSI = tpms.frontLeftPSI,
                frontRightPSI = tpms.frontRightPSI,
                rearLeftPSI = tpms.rearLeftPSI,
                rearRightPSI = tpms.rearRightPSI,
                frontLeftTemp = tpms.frontLeftTemp,
                frontRightTemp = tpms.frontRightTemp,
                rearLeftTemp = tpms.rearLeftTemp,
                rearRightTemp = tpms.rearRightTemp,
                timestamp = tpms.timestamp
            )
            Log.d("OBDRepository", "Updated TPMS from CAN: FL=${tpms.frontLeftPSI}psi, FR=${tpms.frontRightPSI}psi")
        }

        val tcmParsed = try {
            com.canopobd.protocol.BCMProtocol.CANParser.parseTCMMessage(canId, data)
        } catch (e: Exception) {
            null
        }

        tcmParsed?.let { tcm ->
            _tcmReading.value = TCMReading(
                currentGear = tcm.currentGear,
                oilTempCelsius = tcm.oilTempCelsius,
                pressureKpa = tcm.pressureKpa,
                inputShaftRpm = tcm.inputShaftRpm,
                outputShaftRpm = tcm.outputShaftRpm,
                sportMode = tcm.sportMode,
                manualMode = tcm.manualMode,
                transmissionError = tcm.transmissionError,
                timestamp = tcm.timestamp
            )
            Log.d("OBDRepository", "Updated TCM from CAN: Gear=${tcm.currentGear}, OilTemp=${tcm.oilTempCelsius}°C")
        }

        val ecmParsed = try {
            com.canopobd.protocol.BCMProtocol.CANParser.parseECMMessage(canId, data)
        } catch (e: Exception) {
            null
        }

        ecmParsed?.let { ecm ->
            _ecmReading.value = ECMReading(
                rpm = ecm.rpm,
                speedKmh = ecm.speedKmh,
                coolantTemp = ecm.coolantTemp,
                throttlePosition = ecm.throttlePosition,
                engineLoad = ecm.engineLoad,
                timestamp = ecm.timestamp
            )
            Log.d("OBDRepository", "Updated ECM from CAN: RPM=${ecm.rpm.toInt()}, Speed=${ecm.speedKmh.toInt()}km/h")
        }
    }

    fun getLastClimateReading(): ClimateReading = _climateReading.value
    fun getLastTPMSReading(): TPMSReading = _tpmsReading.value
    fun getLastTCMReading(): TCMReading = _tcmReading.value
    fun getLastECMReading(): ECMReading = _ecmReading.value
}
