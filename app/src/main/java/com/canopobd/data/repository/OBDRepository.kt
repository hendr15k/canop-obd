package com.canopobd.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import com.canopobd.bluetooth.ELM327BTConnection
import com.canopobd.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@SuppressLint("MissingPermission")
class OBDRepository(
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val connection = ELM327BTConnection(bluetoothAdapter!!)

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

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    private val pollPIDs = listOf(
        OBDPID.RPM,
        OBDPID.SPEED,
        OBDPID.COOLANT_TEMP,
        OBDPID.INTAKE_TEMP,
        OBDPID.THROTTLE,
        OBDPID.ENGINE_LOAD,
        OBDPID.FUEL_LEVEL,
        OBDPID.TIMING_ADVANCE,
        OBDPID.MAF_RATE,
        OBDPID.FUEL_PRESSURE,
        OBDPID.INTAKE_PRESSURE,
        OBDPID.RUN_TIME,
        OBDPID.FUEL_RAIL_PRESSURE,
        OBDPID.COMMANDED_EGR,
        OBDPID.EGR_TEMP,
        OBDPID.COMMANDED_EVAPORATIVE_PURGE,
        OBDPID.BAROMETRIC_PRESSURE,
        OBDPID.O2_VOLTAGE_B1S1,
        OBDPID.O2_VOLTAGE_B1S2,
        OBDPID.CATALYST_TEMP_B1S1
    )

    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return bluetoothAdapter?.bondedDevices?.map { device ->
            BluetoothDeviceInfo(
                name = device.name ?: device.address,
                address = device.address
            )
        } ?: emptyList()
    }

    fun connect(address: String) {
        scope.launch {
            _connectionState.value = OBDConnectionState.Connecting

            val device = bluetoothAdapter?.getRemoteDevice(address)
            if (device == null) {
                _connectionState.value = OBDConnectionState.Error("Device not found")
                return@launch
            }

            val result = connection.connect(device)
            if (result.isFailure) {
                _connectionState.value = OBDConnectionState.Error(result.exceptionOrNull()?.message ?: "Connection failed")
                return@launch
            }

            _connectionState.value = OBDConnectionState.Connected
            startPolling()
        }
    }

    fun disconnect() {
        pollingJob?.cancel()
        connection.disconnect()
        _connectionState.value = OBDConnectionState.Disconnected
        _obdData.value = OBDData()
        _dtcResponse.value = null
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                val results = connection.readMultiplePIDs(pollPIDs)
                val batteryVoltage = connection.getBatteryVoltage() ?: _obdData.value.batteryVoltage
                
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

                if (_recordingActive.value) {
                    recordData()
                }

                delay(_pollRate.value)
            }
        }
    }

    fun readDTCs() {
        scope.launch {
            _dtcResponse.value = connection.readDTCs()
        }
    }

    fun clearDTCs() {
        scope.launch {
            val success = connection.clearDTCs()
            if (success) {
                _dtcResponse.value = DTCResponse(emptyList())
            }
        }
    }

    fun startRecording() {
        _recordingActive.value = true
        _recordedData.value = emptyList()
    }

    fun stopRecording() {
        _recordingActive.value = false
    }

    private fun recordData() {
        val data = _obdData.value
        val record = DataRecord(
            timestamp = data.timestamp,
            rpm = data.rpm,
            speed = data.speed,
            coolantTemp = data.coolantTemp,
            throttle = data.throttle,
            fuelLevel = data.fuelLevel,
            batteryVoltage = data.batteryVoltage
        )
        _recordedData.value = _recordedData.value + record
    }

    fun setPollRate(rate: Long) {
        _pollRate.value = rate.coerceIn(100L, 2000L)
    }

    fun setMeasurementUnit(unit: MeasurementUnit) {
        _measurementUnit.value = unit
    }

    fun exportToCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("Timestamp,RPM,Speed (km/h),Coolant Temp (°C),Throttle (%),Fuel Level (%),Battery Voltage (V)")
        for (record in _recordedData.value) {
            sb.appendLine("${record.timestamp},${record.rpm.toInt()},${record.speed.toInt()},${record.coolantTemp.toInt()},${record.throttle.toInt()},${record.fuelLevel.toInt()},${record.batteryVoltage}")
        }
        return sb.toString()
    }

    fun clearRecordedData() {
        _recordedData.value = emptyList()
    }
}
