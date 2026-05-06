package com.canopobd.data.repository

import android.util.Log
import com.canopobd.bluetooth.ELM327BTConnection
import com.canopobd.data.protocol.CANMessage
import com.canopobd.data.protocol.CANMonitor
import com.canopobd.data.protocol.CANFilterMode
import com.canopobd.data.protocol.Mode22Client
import com.canopobd.data.protocol.Mode22DIDInfo
import com.canopobd.data.protocol.DIDCategory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class TransmissionData(
    val gear: Int? = null,
    val oilTemp: Double? = null,
    val pressure: Double? = null,
    val inputSpeed: Double? = null,
    val outputSpeed: Double? = null,
    val clutchStatus: String? = null
)

data class BCMStatus(
    val doorsLocked: Boolean = false,
    val driverDoorOpen: Boolean = false,
    val passengerDoorOpen: Boolean = false,
    val rearLeftDoorOpen: Boolean = false,
    val rearRightDoorOpen: Boolean = false,
    val trunkOpen: Boolean = false,
    val hoodOpen: Boolean = false,
    val lightsOn: Boolean = false,
    val hazardsOn: Boolean = false,
    val alarmActive: Boolean = false,
    val alarmTriggered: Boolean = false
)

data class ExtendedPIDData(
    val engineTorque: Double? = null,
    val boostPressureTarget: Double? = null,
    val turboWastegateDuty: Double? = null,
    val turboSpeed: Double? = null,
    val coolantTemp: Double? = null,
    val batteryVoltage: Double? = null,
    val fuelConsumption: Double? = null,
    val vin: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val boostBar: Double?
        get() = boostPressureTarget?.let { if (it > 0) (it - 100.0) / 100.0 else null }

    val relativeBoostBar: Double?
        get() = boostPressureTarget?.let { if (it > 0) (it - 100.0) / 100.0 else null }

    val torquePercent: Double?
        get() = engineTorque?.let { if (it > 0) (it / 220.0) * 100.0 else null }
}

data class TurboMonitoringData(
    val boostActual: Double = 0.0,
    val boostTarget: Double = 0.0,
    val wastegateDuty: Double = 0.0,
    val turboSpeed: Double = 0.0,
    val chargeAirTemp: Double = 0.0,
    val turboInletTemp: Double = 0.0,
    val turboOutletTemp: Double = 0.0,
    val engineTorque: Double = 0.0,
    val coolantTemp: Double = 0.0,
    val intakeTemp: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val boostDeviation: Double
        get() = if (boostTarget > 0) ((boostActual - boostTarget) / boostTarget) * 100.0 else 0.0

    val relativeBoostBar: Double
        get() = ((boostActual - 100.0) / 100.0).coerceAtLeast(0.0)

    val isOverboost: Boolean
        get() = relativeBoostBar > 1.3

    val isUnderboost: Boolean
        get() = boostActual > 0 && boostTarget > 0 && boostDeviation < -20.0
}

class CANRepository(private val connection: ELM327BTConnection) {
    companion object {
        private const val TAG = "CANRepository"
        private const val POLL_INTERVAL_MS = 500L
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val canMonitor = CANMonitor(connection)
    val mode22Client = Mode22Client(connection)

    private val _extendedPIDData = MutableStateFlow(ExtendedPIDData())
    val extendedPIDData: StateFlow<ExtendedPIDData> = _extendedPIDData.asStateFlow()

    private val _turboMonitoringData = MutableStateFlow(TurboMonitoringData())
    val turboMonitoringData: StateFlow<TurboMonitoringData> = _turboMonitoringData.asStateFlow()

    private val _transmissionData = MutableStateFlow<TransmissionData?>(null)
    val transmissionData: StateFlow<TransmissionData?> = _transmissionData.asStateFlow()

    private val _bcmStatus = MutableStateFlow<BCMStatus?>(null)
    val bcmStatus: StateFlow<BCMStatus?> = _bcmStatus.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var pollingJob: Job? = null

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val canResult = canMonitor.initialize()
            val mode22Result = mode22Client.initialize()
            if (canResult.isFailure || mode22Result.isFailure) {
                Result.failure(canResult.exceptionOrNull() ?: mode22Result.exceptionOrNull() ?: Exception("Init failed"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Init failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun startExtendedPIDPolling() {
        if (_isMonitoring.value) return
        _isMonitoring.value = true
        var consecutiveFailures = 0

        pollingJob = scope.launch {
            while (isActive && _isMonitoring.value) {
                try {
                    updateExtendedPIDs()
                    updateTurboData()
                    consecutiveFailures = 0
                } catch (e: Exception) {
                    consecutiveFailures++
                    Log.e(TAG, "Extended PID polling error ($consecutiveFailures): ${e.message}")
                    if (consecutiveFailures >= 5) {
                        Log.e(TAG, "Too many polling failures, stopping extended PID polling")
                        stopExtendedPIDPolling()
                        break
                    }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopExtendedPIDPolling() {
        _isMonitoring.value = false
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun updateExtendedPIDs() = withContext(Dispatchers.IO) {
        try {
            val dids = listOf("F4B0", "F4C0", "F4E0", "F4F1", "F480")
            mode22Client.readMultipleDIDs(dids).collect { results ->
                if (results.isNotEmpty()) {
                    val engineTorque = results["F4B0"]?.let { parseTorque(it) }
                    val boostTarget = results["F4C0"]?.let { parsePressure(it) }
                    val coolantTemp = results["F4E0"]?.let { parseTemperature(it) }
                    val batteryVoltage = results["F4F1"]?.let { parseVoltage(it) }
                    val fuelConsumption = results["F480"]?.let { parseFuelConsumption(it) }

                    _extendedPIDData.value = _extendedPIDData.value.copy(
                        engineTorque = engineTorque,
                        boostPressureTarget = boostTarget,
                        coolantTemp = coolantTemp,
                        batteryVoltage = batteryVoltage,
                        fuelConsumption = fuelConsumption,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateExtendedPIDs error: ${e.message}")
        }
    }

    private suspend fun updateTurboData() = withContext(Dispatchers.IO) {
        try {
            val dids = listOf("220001", "220002", "220003", "220004", "220005", "220006", "220007", "220008")
            mode22Client.readMultipleDIDs(dids).collect { results ->
                if (results.isNotEmpty()) {
                    _turboMonitoringData.value = TurboMonitoringData(
                        engineTorque = results["220001"]?.let { parseTorque(it) } ?: 0.0,
                        boostActual = results["220002"]?.let { parsePressure(it) } ?: 0.0,
                        boostTarget = results["220003"]?.let { parsePressure(it) } ?: 0.0,
                        wastegateDuty = results["220004"]?.let { parsePercent(it) } ?: 0.0,
                        turboSpeed = results["220005"]?.let { parseSpeed(it) } ?: 0.0,
                        turboInletTemp = results["220006"]?.let { parseTemperature(it) } ?: 0.0,
                        turboOutletTemp = results["220007"]?.let { parseTemperature(it) } ?: 0.0,
                        chargeAirTemp = results["220008"]?.let { parseTemperature(it) } ?: 0.0,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateTurboData error: ${e.message}")
        }
    }

    fun getEngineTorque(): Flow<Double?> = flow {
        mode22Client.readDID("F4B0").collect { data ->
            emit(data?.let { parseTorque(it) })
        }
    }.flowOn(Dispatchers.IO)

    fun getBoostPressureTarget(): Flow<Double?> = flow {
        mode22Client.readDID("F4C0").collect { data ->
            emit(data?.let { parsePressure(it) })
        }
    }.flowOn(Dispatchers.IO)

    fun getTurboWastegateDuty(): Flow<Double?> = flow {
        mode22Client.readDID("220004").collect { data ->
            emit(data?.let { parsePercent(it) })
        }
    }.flowOn(Dispatchers.IO)

    fun getTransmissionData(): Flow<TransmissionData?> = flow {
        val gearDid = "226001"
        mode22Client.readDID(gearDid).collect { data ->
            if (data != null && data.isNotEmpty()) {
                val gear = data[0].toInt() and 0xFF
                val td = TransmissionData(gear = gear)
                _transmissionData.value = td
                emit(td)
            } else {
                emit(null)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun getBCMStatus(): StateFlow<BCMStatus?> = _bcmStatus

    fun readVIN(): Flow<String?> = flow {
        mode22Client.readDID("F190").collect { data ->
            emit(data?.let { parseVIN(it) })
        }
    }.flowOn(Dispatchers.IO)

    fun getDIDInfo(did: String): Mode22DIDInfo? = mode22Client.getDIDInfo(did)

    fun getAvailableDIDs(): List<Mode22DIDInfo> = Mode22Client.ALL_DIDS.values.toList()

    fun getDIDsByCategory(category: DIDCategory): List<Mode22DIDInfo> =
        mode22Client.getDIDsByCategory(category)

    fun discoverDIDs(): Flow<List<String>> = mode22Client.discoverAvailableDIDs()

    fun startCANMonitoring(onMessage: (CANMessage) -> Unit) {
        canMonitor.startMonitoring(onMessage)
    }

    fun stopCANMonitoring() {
        canMonitor.stopMonitoring()
    }

    fun getCANMessages(): StateFlow<List<CANMessage>> = canMonitor.messages

    fun isCANMonitoring(): StateFlow<Boolean> = canMonitor.isMonitoring

    fun clearCANMessages() {
        canMonitor.clearMessages()
    }

    suspend fun setCANFilter(canId: String): Result<Unit> {
        return canMonitor.setFilter(canId)
    }

    suspend fun clearCANFilters(): Result<Unit> {
        return canMonitor.clearFilters()
    }

    private fun parseTorque(data: ByteArray): Double {
        if (data.size < 2) return 0.0
        return ((data[0].toInt() and 0xFF) * 256 + (data[1].toInt() and 0xFF) - 500).toDouble()
    }

    private fun parsePressure(data: ByteArray): Double {
        if (data.size < 2) return 0.0
        return ((data[0].toInt() and 0xFF) * 256 + (data[1].toInt() and 0xFF)).toDouble()
    }

    private fun parseTemperature(data: ByteArray): Double {
        if (data.isEmpty()) return 0.0
        return ((data[0].toInt() and 0xFF) - 40).toDouble()
    }

    private fun parseVoltage(data: ByteArray): Double {
        if (data.isEmpty()) return 0.0
        return (data[0].toInt() and 0xFF) / 10.0
    }

    private fun parsePercent(data: ByteArray): Double {
        if (data.isEmpty()) return 0.0
        return (data[0].toInt() and 0xFF) * 100.0 / 255.0
    }

    private fun parseSpeed(data: ByteArray): Double {
        if (data.size < 2) return 0.0
        return ((data[0].toInt() and 0xFF) * 256 + (data[1].toInt() and 0xFF)).toDouble()
    }

    private fun parseFuelConsumption(data: ByteArray): Double {
        if (data.size < 2) return 0.0
        return ((data[0].toInt() and 0xFF) * 256 + (data[1].toInt() and 0xFF)) / 20.0
    }

    private fun parseVIN(data: ByteArray): String {
        return data.filter { it.toInt() in 0x20..0x7E }
            .map { it.toInt().toChar() }
            .joinToString("")
            .trim()
    }

    fun shutdown() {
        stopExtendedPIDPolling()
        canMonitor.shutdown()
        mode22Client.shutdown()
        scope.cancel()
    }
}
