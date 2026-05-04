package com.canopobd.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
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

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    private val pollPIDs = listOf(
        OBDPID.RPM, OBDPID.SPEED, OBDPID.COOLANT_TEMP, OBDPID.INTAKE_TEMP,
        OBDPID.THROTTLE, OBDPID.ENGINE_LOAD, OBDPID.FUEL_LEVEL,
        OBDPID.TIMING_ADVANCE, OBDPID.MAF_RATE, OBDPID.FUEL_PRESSURE,
        OBDPID.INTAKE_PRESSURE, OBDPID.RUN_TIME, OBDPID.FUEL_RAIL_PRESSURE,
        OBDPID.COMMANDED_EGR, OBDPID.EGR_TEMP, OBDPID.COMMANDED_EVAPORATIVE_PURGE,
        OBDPID.BAROMETRIC_PRESSURE, OBDPID.O2_VOLTAGE_B1S1, OBDPID.O2_VOLTAGE_B1S2,
        OBDPID.CATALYST_TEMP_B1S1
    )

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
            _connectionState.value = OBDConnectionState.Error("Bluetooth not available")
            return
        }
        scope.launch {
            _connectionState.value = OBDConnectionState.Connecting
            val device = bluetoothAdapter?.getRemoteDevice(address)
            if (device == null) {
                _connectionState.value = OBDConnectionState.Error("Device not found")
                return@launch
            }
            val result = conn.connect(device)
            if (result.isFailure) {
                _connectionState.value = OBDConnectionState.Error(result.exceptionOrNull()?.message ?: "Connection failed")
                return@launch
            }
            _connectionState.value = OBDConnectionState.Connected
            startPolling(conn)
            if (remoteBridge == null) {
                remoteBridge = RemoteBridge(context, conn)
            }
        }
    }

    fun disconnect() {
        stopRemoteServer()
        pollingJob?.cancel()
        connection?.disconnect()
        _connectionState.value = OBDConnectionState.Disconnected
        _obdData.value = OBDData()
        _dtcResponse.value = null
    }

    private fun startPolling(conn: ELM327BTConnection) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                val results = conn.readMultiplePIDs(pollPIDs)
                val batteryVoltage = conn.getBatteryVoltage() ?: _obdData.value.batteryVoltage
                _obdData.value = OBDData(
                    rpm = results[OBDPID.RPM] ?: _obdData.value.rpm,
                    speed = results[OBDPID.SPEED] ?: _obdData.value.speed,
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
                    timestamp = System.currentTimeMillis()
                )
                if (_recordingActive.value) recordData()
                delay(_pollRate.value)
            }
        }
    }

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

    fun setPollRate(rate: Long) { _pollRate.value = rate.coerceIn(100L, 2000L) }
    fun setMeasurementUnit(unit: MeasurementUnit) { _measurementUnit.value = unit }

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
