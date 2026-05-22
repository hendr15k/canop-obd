package com.canopobd.data.protocol

import android.util.Log
import com.canopobd.bluetooth.ELM327BTConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.CopyOnWriteArrayList

data class CANMessage(
    val timestamp: Long,
    val canId: String,
    val data: ByteArray,
    val isExtended: Boolean,
    val dlc: Int
) {
    val hexData: String
        get() = data.take(dlc).joinToString(" ") { "%02X".format(it) }

    val asciiRepresentation: String
        get() = data.take(dlc).map { b ->
            if (b.toInt() in 0x20..0x7E) b.toInt().toChar() else '.'
        }.joinToString("")

    val isValid: Boolean
        get() = canId.isNotEmpty() && data.isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CANMessage) return false
        return timestamp == other.timestamp && canId == other.canId &&
                data.contentEquals(other.data) && isExtended == other.isExtended && dlc == other.dlc
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + canId.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + isExtended.hashCode()
        result = 31 * result + dlc
        return result
    }
}

enum class CANFilterMode {
    STANDARD,
    EXTENDED,
    ALL
}

enum class CANBusSpeed(val baud: Int, val code: String) {
    K500(500000, "6"),
    K250(250000, "9"),
    K125(125000, "A"),
    K100(100000, "B"),
    K50(50000, "C"),
    K83(83333, "D")
}

class CANMonitor(private val connection: ELM327BTConnection) {
    private val messagesLock = Any()
    companion object {
        private const val TAG = "CANMonitor"
        private const val COMMAND_TIMEOUT_MS = 2000L
        private const val MAX_RETRY = 2

        private val ECM_ADDRESS = "7E0"
        private val TCM_ADDRESS = "7E1"
        private val BCM_ADDRESS = "7E2"
        private val ABS_ADDRESS = "7E3"
        private val RESPONSE_MASK = "7E8"

        val COMMON_IDS = mapOf(
            "7E0" to "ECM (Motorsteuergeraet)",
            "7E1" to "TCM (Getriebesteuergeraet)",
            "7E2" to "BCM (Karosseriesteuergeraet)",
            "7E3" to "ABS (Antiblockiersystem)",
            "7E4" to "IC (Instrumentencluster)",
            "7E5" to "HVAC (Klimasteuerung)",
            "7E8" to "ECM Response",
            "7E9" to "TCM Response",
            "7EA" to "BCM Response",
            "7EB" to "ABS Response"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    @Volatile
    private var isInitialized = false
    private var currentFilter: String? = null
    private var filterMode = CANFilterMode.ALL

    private val _messages = MutableStateFlow<List<CANMessage>>(emptyList())
    val messages: StateFlow<List<CANMessage>> = _messages.asStateFlow()
    private val messagesInternal = CopyOnWriteArrayList<CANMessage>()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _messagesPerSecond = MutableStateFlow(0)
    val messagesPerSecond: StateFlow<Int> = _messagesPerSecond.asStateFlow()

    private val maxMessageHistory = 500

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sendCommand("ATZ")
            delay(1000)
            sendCommand("ATE0")
            delay(100)
            sendCommand("ATL0")
            delay(100)
            sendCommand("ATS0")
            delay(100)
            sendCommand("ATH0")
            delay(100)
            sendCommand("ATSP6")
            delay(100)
            sendCommand("ATAT1")
            delay(100)
            sendCommand("ATST32")
            delay(100)
            sendCommand("ATFE1")
            delay(100)
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun startMonitoring(onMessage: ((CANMessage) -> Unit)? = null) {
        if (_isMonitoring.value) return
        _isMonitoring.value = true
        synchronized(messagesLock) {
            messagesInternal.clear()
            _messages.value = emptyList()
        }
        _errorMessage.value = null
        var messageCount = 0
        var lastSecond = System.currentTimeMillis() / 1000

        monitoringJob = scope.launch {
            while (isActive && _isMonitoring.value) {
                try {
                    val response = sendCommandWithTimeout("ATMA")
                    if (response.isNotBlank() && !response.contains("ERROR")) {
                        val parsedMessages = parseCANResponse(response)
                        parsedMessages.forEach { msg ->
                            synchronized(messagesLock) {
                                messagesInternal.add(0, msg)
                                if (messagesInternal.size > maxMessageHistory) {
                                    messagesInternal.removeAt(messagesInternal.size - 1)
                                }
                                _messages.value = messagesInternal.toList()
                            }
                            onMessage?.invoke(msg)
                            messageCount++

                            val currentSecond = System.currentTimeMillis() / 1000
                            if (currentSecond > lastSecond) {
                                _messagesPerSecond.value = messageCount
                                messageCount = 0
                                lastSecond = currentSecond
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (_isMonitoring.value) {
                        Log.w(TAG, "Monitoring error: ${e.message}")
                    }
                }
                delay(50)
            }
        }
    }

    fun stopMonitoring() {
        _isMonitoring.value = false
        monitoringJob?.cancel()
        monitoringJob = null
        _messagesPerSecond.value = 0
        scope.launch {
            try {
                sendCommand("")
                delay(100)
            } catch (e: Exception) {
                Log.w(TAG, "Stop monitoring error: ${e.message}")
            }
        }
    }

    suspend fun setFilter(canId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            currentFilter = canId.uppercase()
            val hexId = canId.uppercase().replace(" ", "").replace("0X", "")
            sendCommand("ATCF$hexId")
            delay(50)
            sendCommand("ATCM$hexId")
            delay(50)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Set filter failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun clearFilters(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            currentFilter = null
            sendCommand("ATCF000")
            delay(50)
            sendCommand("ATCM7FF")
            delay(50)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Clear filters failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun setFilterMode(mode: CANFilterMode) {
        filterMode = mode
    }

    fun clearMessages() {
        messagesInternal.clear()
        _messages.value = emptyList()
    }

    fun getMessagesById(canId: String): List<CANMessage> {
        return messagesInternal.filter { it.canId.equals(canId, ignoreCase = true) }
    }

    fun getUniqueCanIds(): Set<String> {
        return messagesInternal.map { it.canId }.toSet()
    }

    fun getMessageCount(): Int = _messages.value.size

    fun getMessagesPerSecond(): Flow<Int> = flow {
        var lastCount = 0
        var lastTime = System.currentTimeMillis()
        while (true) {
            delay(1000)
            val currentCount = _messages.value.size
            val currentTime = System.currentTimeMillis()
            val rate = currentCount - lastCount
            emit(rate)
            lastCount = currentCount
            lastTime = currentTime
        }
    }.flowOn(Dispatchers.Default)

    suspend fun sendCANFrame(canId: String, data: ByteArray): Flow<String> = flow {
        if (!isInitialized) {
            emit("ERROR: Not initialized")
            return@flow
        }
        if (data.size > 8) {
            emit("ERROR: Data too long (max 8 bytes)")
            return@flow
        }
        try {
            val hexId = canId.uppercase().replace(" ", "").replace("0X", "")
            val hexData = data.joinToString("") { "%02X".format(it) }
            val command = "${hexId}${hexData}"
            val response = sendCommandWithTimeout(command)
            emit(response)
        } catch (e: Exception) {
            emit("ERROR: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    suspend fun readExtendedPIDs(did: String): Flow<String> = flow {
        if (!isInitialized) {
            emit("ERROR: Not initialized")
            return@flow
        }
        try {
            val hexDid = did.uppercase().replace(" ", "").replace("0X", "")
            val command = "22$hexDid"
            val response = sendCommandWithTimeout(command)
            emit(response)
        } catch (e: Exception) {
            emit("ERROR: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    suspend fun requestStandardPID(canId: String, pid: String, dataLength: Int = 8): Flow<CANMessage?> = flow {
        if (!isInitialized) {
            emit(null)
            return@flow
        }
        try {
            val hexId = canId.uppercase().replace(" ", "").replace("0X", "")
            val hexPid = pid.uppercase().replace(" ", "").replace("0X", "")
            val command = "${hexId}${hexPid}"
            val response = sendCommandWithTimeout(command)
            val messages = parseCANResponse(response)
            if (messages.isNotEmpty()) {
                emit(messages.first())
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Request PID failed: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun readVIN(): Flow<String> = flow {
        try {
            sendCANFrame(ECM_ADDRESS, byteArrayOf(0x02, 0x09, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00)).collect { response ->
                emit(response)
            }
        } catch (e: Exception) {
            emit("ERROR: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    private fun parseCANResponse(response: String): List<CANMessage> {
        val messages = mutableListOf<CANMessage>()
        val cleaned = response.replace("\r", " ").replace("\n", " ")
            .replace(">", "").trim()

        if (cleaned.isBlank() || cleaned.contains("ERROR") ||
            cleaned.contains("UNABLE") || cleaned.contains("NO DATA")) {
            return messages
        }

        val parts = cleaned.split(" ").filter { it.isNotBlank() }
        var i = 0
        while (i < parts.size) {
            val part = parts[i]
            val canIdPattern = Regex("^[0-9A-Fa-f]{3,8}$")

            if (part.matches(canIdPattern) && (part.length == 3 || part.length == 4)) {
                val canId = part
                i++
                if (i < parts.size) {
                    val dlcByte = parts[i]
                    i++
                    val dlc = try {
                        dlcByte.toInt(16).coerceIn(0, 8)
                    } catch (e: Exception) { 0 }

                    val dataBytes = mutableListOf<Byte>()
                    repeat(dlc.coerceAtMost(8)) {
                        if (i < parts.size) {
                            try {
                                dataBytes.add(parts[i].toInt(16).toByte())
                                i++
                            } catch (e: Exception) {
                                Log.w(TAG, "Invalid hex byte at index $i: ${parts[i]}")
                            }
                        }
                    }

                    if (dataBytes.isNotEmpty()) {
                        messages.add(CANMessage(
                            timestamp = System.currentTimeMillis(),
                            canId = canId,
                            data = dataBytes.toByteArray(),
                            isExtended = canId.length > 4,
                            dlc = dataBytes.size
                        ))
                    }
                }
            } else {
                i++
            }
        }

        return messages
    }

    private suspend fun sendCommand(cmd: String): String = withContext(Dispatchers.IO) {
        connection.sendRawCommand(cmd)
    }

    private suspend fun sendCommandWithTimeout(cmd: String): String = withContext(Dispatchers.IO) {
        connection.sendRawCommand(cmd)
    }

    fun shutdown() {
        stopMonitoring()
        scope.cancel()
    }
}
