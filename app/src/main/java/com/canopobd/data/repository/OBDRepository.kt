package com.canopobd.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.SharedPreferences
import com.canopobd.bluetooth.ELM327BTConnection
import com.canopobd.bluetooth.RemoteBridge
import com.canopobd.data.model.*
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

    private val _colorTheme = MutableStateFlow(ColorTheme.CANOPO)
    val colorTheme: StateFlow<ColorTheme> = _colorTheme.asStateFlow()

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
        OBDPID.FUEL_AIR_EQUIV_RATIO
    )

    init {
        _pollRate.value = prefs.getLong("poll_rate", 500L)
        _autoReconnect.value = prefs.getBoolean("auto_reconnect", false)
        storedVin = prefs.getString("vin", "") ?: ""
        prefs.getString("color_theme", null)?.let {
            _colorTheme.value = ColorTheme.fromName(it)
        }
        prefs.getStringSet("primary_gauges", null)?.let { ids ->
            _primaryGaugeIds.value = ids
        }
        _pollMode.value = PollMode.valueOf(prefs.getString("poll_mode", "NORMAL") ?: "NORMAL")
    }

    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return try {
            bluetoothAdapter?.bondedDevices?.map { device ->
                BluetoothDeviceInfo(name = device.name ?: device.address, address = device.address)
            } ?: emptyList()
        } catch (_: SecurityException) {
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
        pollingJob?.cancel()
        connection?.disconnect()
        _connectionState.value = OBDConnectionState.Disconnected
        _obdData.value = OBDData()
        _dtcResponse.value = null
        saveTripData()
    }

    private fun resetConnectionStats() {
        _connectionStats.value = ConnectionStats()
    }

    private fun recordConnectionSuccess() {
        val s = _connectionStats.value
        _connectionStats.value = s.copy(
            successCount = s.successCount + 1,
            quality = ConnectionQuality.fromSuccessRate(
                (s.successCount + 1).toDouble() / (s.totalCount + 1)
            )
        )
    }

    private fun recordConnectionFailure() {
        val s = _connectionStats.value
        _connectionStats.value = s.copy(
            failureCount = s.failureCount + 1,
            quality = ConnectionQuality.fromSuccessRate(
                s.successCount.toDouble() / (s.totalCount + 1)
            )
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
                    vin = storedVin,
                    timestamp = now
                )

                recordConnectionSuccess()
                if (_recordingActive.value) recordData()
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
}
