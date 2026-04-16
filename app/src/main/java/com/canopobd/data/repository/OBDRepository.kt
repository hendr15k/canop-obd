package com.canopobd.data.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import com.canopobd.bluetooth.ELM327BTConnection
import com.canopobd.data.model.BluetoothDeviceInfo
import com.canopobd.data.model.OBDConnectionState
import com.canopobd.data.model.OBDData
import com.canopobd.data.model.OBDPID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Repository for OBD data — single source of truth for connection + data
 */
@SuppressLint("MissingPermission")
class OBDRepository(
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val connection = ELM327BTConnection(bluetoothAdapter!!)

    private val _connectionState = MutableStateFlow<OBDConnectionState>(OBDConnectionState.Disconnected)
    val connectionState: StateFlow<OBDConnectionState> = _connectionState.asStateFlow()

    private val _obdData = MutableStateFlow(OBDData())
    val obdData: StateFlow<OBDData> = _obdData.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    /**
     * All PIDs to poll each cycle
     */
    private val pollPIDs = listOf(
        OBDPID.RPM,
        OBDPID.SPEED,
        OBDPID.COOLANT_TEMP,
        OBDPID.INTAKE_TEMP,
        OBDPID.THROTTLE,
        OBDPID.ENGINE_LOAD,
        OBDPID.FUEL_LEVEL
    )

    /**
     * Get list of paired OBD devices
     */
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return bluetoothAdapter?.bondedDevices?.map { device ->
            BluetoothDeviceInfo(
                name = device.name ?: device.address,
                address = device.address
            )
        } ?: emptyList()
    }

    /**
     * Connect to a device by address and start polling
     */
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

    /**
     * Disconnect and stop polling
     */
    fun disconnect() {
        pollingJob?.cancel()
        connection.disconnect()
        _connectionState.value = OBDConnectionState.Disconnected
        _obdData.value = OBDData()
    }

    /**
     * Start continuous PID polling
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                val results = connection.readMultiplePIDs(pollPIDs)
                _obdData.value = OBDData(
                    rpm = results[OBDPID.RPM] ?: _obdData.value.rpm,
                    speed = results[OBDPID.SPEED] ?: _obdData.value.speed,
                    coolantTemp = results[OBDPID.COOLANT_TEMP] ?: _obdData.value.coolantTemp,
                    intakeTemp = results[OBDPID.INTAKE_TEMP] ?: _obdData.value.intakeTemp,
                    throttle = results[OBDPID.THROTTLE] ?: _obdData.value.throttle,
                    engineLoad = results[OBDPID.ENGINE_LOAD] ?: _obdData.value.engineLoad,
                    fuelLevel = results[OBDPID.FUEL_LEVEL] ?: _obdData.value.fuelLevel,
                    timestamp = System.currentTimeMillis()
                )
                delay(500) // Poll every 500ms
            }
        }
    }
}
