package com.canopobd.data.protocol

import android.util.Log
import com.canopobd.bluetooth.ELM327BTConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UDSClient(private val connection: ELM327BTConnection) {

    companion object {
        private const val TAG = "UDSClient"
        private const val TIMEOUT_MS = 5000L
        private const val RETRY_DELAY_MS = 200L
        private const val MAX_RETRIES = 3
        private const val TESTER_PRESENT_INTERVAL_MS = 2000L

        private const val UDS_PHYSICAL_REQUEST = 0x7E0
        private const val UDS_PHYSICAL_RESPONSE = 0x7E8
        private const val UDS_FUNCTIONAL_REQUEST = 0x7DF
        private const val UDS_FUNCTIONAL_RESPONSE = 0x7E8
    }

    private var currentSession: UDSSessionType = UDSSessionType.DEFAULT
    private var securityLevel: Int = 0
    private var testerPresentActive = false

    private fun parseISOTPDelimiter(response: String): String {
        val cleaned = response.replace(" ", "").replace("\r", "").replace("\n", "").replace(">", "").trim()
        return cleaned.filter { it.isDigit() || it.isLetter() }
    }

    private fun extractDataFromResponse(response: String, serviceId: Int): ByteArray {
        val cleaned = parseISOTPDelimiter(response)
        if (cleaned.isEmpty()) return ByteArray(0)

        if (cleaned.length >= 4 && cleaned.substring(0, 2).toInt(16) == serviceId + 0x40) {
            return cleaned.substring(4).chunked(2).mapNotNull { hex ->
                if (hex.length == 2) {
                    try { hex.toInt(16).toByte() } catch (e: Exception) { null }
                } else null
            }.toByteArray()
        }

        if (cleaned.length >= 2 && cleaned.substring(0, 2).toInt(16) == serviceId + 0x7F) {
            return cleaned.substring(2).chunked(2).mapNotNull { hex ->
                if (hex.length == 2) {
                    try { hex.toInt(16).toByte() } catch (e: Exception) { null }
                } else null
            }.toByteArray()
        }

        return cleaned.chunked(2).mapNotNull { hex ->
            if (hex.length == 2) {
                try { hex.toInt(16).toByte() } catch (e: Exception) { null }
            } else null
        }.toByteArray()
    }

    private fun isPositiveResponse(response: ByteArray, serviceId: Int): Boolean {
        return response.isNotEmpty() && response[0].toInt() == serviceId + 0x40
    }

    private fun extractErrorCode(response: ByteArray): Int? {
        if (response.size >= 3 && response[0].toInt() == 0x7F) {
            return response[2].toInt()
        }
        return null
    }

    fun diagnosticSessionControl(sessionType: UDSSessionType): Flow<UDSResponse> = flow {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val command = "103${sessionType.value.toString(16).padStart(2, '0')}"
                Log.d(TAG, "UDS DiagnosticSessionControl: $command")
                val response = withContext(Dispatchers.IO) {
                    connection.sendRawCommand(command)
                }
                val cleanedResponse = parseISOTPDelimiter(response)
                val data = extractDataFromResponse(response, 0x10)

                if (isPositiveResponse(data, 0x10)) {
                    currentSession = sessionType
                    Log.i(TAG, "UDS Session changed to: ${sessionType.label}")
                    emit(UDSResponse(true, 0x10, data, rawResponse = cleanedResponse))
                    return@flow
                } else {
                    val errorCode = extractErrorCode(data)
                    Log.w(TAG, "UDS SessionControl failed: ${UDSError.fromCode(0x10, errorCode ?: 0).description}")
                    emit(UDSResponse(false, 0x10, data, errorCode, cleanedResponse))
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDS SessionControl error: ${e.message}")
                emit(UDSResponse(false, 0x10, ByteArray(0), null, e.message ?: ""))
            }
            retryCount++
            if (retryCount < MAX_RETRIES) delay(RETRY_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    fun readDataByIdentifier(did: String): Flow<UDSResponse> = flow {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val didBytes = did.replace(" ", "").uppercase()
                val command = "22$didBytes"
                Log.d(TAG, "UDS ReadDataByIdentifier: $command")
                val response = withContext(Dispatchers.IO) {
                    connection.sendRawCommand(command)
                }
                val cleanedResponse = parseISOTPDelimiter(response)
                val data = extractDataFromResponse(response, 0x22)

                if (isPositiveResponse(data, 0x22)) {
                    Log.v(TAG, "UDS DID $did read successfully")
                    emit(UDSResponse(true, 0x22, data, rawResponse = cleanedResponse))
                    return@flow
                } else {
                    val errorCode = extractErrorCode(data)
                    Log.w(TAG, "UDS ReadDID $did failed: ${UDSError.fromCode(0x22, errorCode ?: 0).description}")
                    emit(UDSResponse(false, 0x22, data, errorCode, cleanedResponse))
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDS ReadDID $did error: ${e.message}")
                emit(UDSResponse(false, 0x22, ByteArray(0), null, e.message ?: ""))
            }
            retryCount++
            if (retryCount < MAX_RETRIES) delay(RETRY_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    fun readDTCInformation(reportType: Int, dtcStatusMask: Int = 0xFF): Flow<UDSResponse> = flow {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val reportTypeHex = reportType.toString(16).padStart(2, '0')
                val maskHex = dtcStatusMask.toString(16).padStart(2, '0')
                val command = "19$reportTypeHex$maskHex"
                Log.d(TAG, "UDS ReadDTCInformation: $command")
                val response = withContext(Dispatchers.IO) {
                    connection.sendRawCommand(command)
                }
                val cleanedResponse = parseISOTPDelimiter(response)
                val data = extractDataFromResponse(response, 0x19)

                if (isPositiveResponse(data, 0x19)) {
                    Log.v(TAG, "UDS DTC read successfully")
                    emit(UDSResponse(true, 0x19, data, rawResponse = cleanedResponse))
                    return@flow
                } else {
                    val errorCode = extractErrorCode(data)
                    Log.w(TAG, "UDS ReadDTC failed: ${UDSError.fromCode(0x19, errorCode ?: 0).description}")
                    emit(UDSResponse(false, 0x19, data, errorCode, cleanedResponse))
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDS ReadDTC error: ${e.message}")
                emit(UDSResponse(false, 0x19, ByteArray(0), null, e.message ?: ""))
            }
            retryCount++
            if (retryCount < MAX_RETRIES) delay(RETRY_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    fun clearDTCInformation(groupOfDTC: Int = 0xFFFFFF): Flow<UDSResponse> = flow {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val groupHex = groupOfDTC.toString(16).uppercase().padStart(6, '0')
                val command = "14$groupHex"
                Log.d(TAG, "UDS ClearDTC: $command")
                val response = withContext(Dispatchers.IO) {
                    connection.sendRawCommand(command)
                }
                val cleanedResponse = parseISOTPDelimiter(response)
                val data = extractDataFromResponse(response, 0x14)

                if (isPositiveResponse(data, 0x14)) {
                    Log.i(TAG, "UDS DTCs cleared")
                    emit(UDSResponse(true, 0x14, data, rawResponse = cleanedResponse))
                    return@flow
                } else {
                    val errorCode = extractErrorCode(data)
                    Log.w(TAG, "UDS ClearDTC failed: ${UDSError.fromCode(0x14, errorCode ?: 0).description}")
                    emit(UDSResponse(false, 0x14, data, errorCode, cleanedResponse))
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDS ClearDTC error: ${e.message}")
                emit(UDSResponse(false, 0x14, ByteArray(0), null, e.message ?: ""))
            }
            retryCount++
            if (retryCount < MAX_RETRIES) delay(RETRY_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    fun securityAccess(level: Int, key: ByteArray? = null): Flow<UDSResponse> = flow {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val subFunction = if (key == null) {
                    (level * 2 - 1).toString(16).padStart(2, '0')
                } else {
                    (level * 2).toString(16).padStart(2, '0')
                }

                val command = if (key != null && key.isNotEmpty()) {
                    val keyHex = key.joinToString("") { "%02X".format(it) }
                    "27$subFunction$keyHex"
                } else {
                    "27$subFunction"
                }

                Log.d(TAG, "UDS SecurityAccess level $level: $command")
                val response = withContext(Dispatchers.IO) {
                    connection.sendRawCommand(command)
                }
                val cleanedResponse = parseISOTPDelimiter(response)
                val data = extractDataFromResponse(response, 0x27)

                if (isPositiveResponse(data, 0x27)) {
                    securityLevel = level
                    Log.i(TAG, "UDS SecurityAccess level $level granted")
                    emit(UDSResponse(true, 0x27, data, rawResponse = cleanedResponse))
                    return@flow
                } else {
                    val errorCode = extractErrorCode(data)
                    Log.w(TAG, "UDS SecurityAccess failed: ${UDSError.fromCode(0x27, errorCode ?: 0).description}")
                    emit(UDSResponse(false, 0x27, data, errorCode, cleanedResponse))
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDS SecurityAccess error: ${e.message}")
                emit(UDSResponse(false, 0x27, ByteArray(0), null, e.message ?: ""))
            }
            retryCount++
            if (retryCount < MAX_RETRIES) delay(RETRY_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    fun routineControl(routineType: RoutineControlType, routineId: String): Flow<UDSResponse> = flow {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val routineIdClean = routineId.replace(" ", "").uppercase()
                val command = "31${routineType.value.toString(16).padStart(2, '0')}$routineIdClean"
                Log.d(TAG, "UDS RoutineControl ${routineType.label}: $command")
                val response = withContext(Dispatchers.IO) {
                    connection.sendRawCommand(command)
                }
                val cleanedResponse = parseISOTPDelimiter(response)
                val data = extractDataFromResponse(response, 0x31)

                if (isPositiveResponse(data, 0x31)) {
                    Log.i(TAG, "UDS Routine ${routineType.label} completed")
                    emit(UDSResponse(true, 0x31, data, rawResponse = cleanedResponse))
                    return@flow
                } else {
                    val errorCode = extractErrorCode(data)
                    Log.w(TAG, "UDS RoutineControl failed: ${UDSError.fromCode(0x31, errorCode ?: 0).description}")
                    emit(UDSResponse(false, 0x31, data, errorCode, cleanedResponse))
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDS RoutineControl error: ${e.message}")
                emit(UDSResponse(false, 0x31, ByteArray(0), null, e.message ?: ""))
            }
            retryCount++
            if (retryCount < MAX_RETRIES) delay(RETRY_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    fun testerPresent(): Flow<UDSResponse> = flow {
        try {
            val subFunction = "00"
            val command = "3E$subFunction"
            val response = withContext(Dispatchers.IO) {
                connection.sendRawCommand(command)
            }
            val cleanedResponse = parseISOTPDelimiter(response)
            val data = extractDataFromResponse(response, 0x3E)

            if (isPositiveResponse(data, 0x3E)) {
                emit(UDSResponse(true, 0x3E, data, rawResponse = cleanedResponse))
            } else {
                val errorCode = extractErrorCode(data)
                emit(UDSResponse(false, 0x3E, data, errorCode, cleanedResponse))
            }
        } catch (e: Exception) {
            Log.e(TAG, "UDS TesterPresent error: ${e.message}")
            emit(UDSResponse(false, 0x3E, ByteArray(0), null, e.message ?: ""))
        }
    }.flowOn(Dispatchers.IO)

    fun writeDataByIdentifier(did: String, data: ByteArray): Flow<UDSResponse> = flow {
        var retryCount = 0
        while (retryCount < MAX_RETRIES) {
            try {
                val didBytes = did.replace(" ", "").uppercase()
                val dataHex = data.joinToString("") { "%02X".format(it) }
                val command = "2E$didBytes$dataHex"
                Log.d(TAG, "UDS WriteDataByIdentifier: $command")
                val response = withContext(Dispatchers.IO) {
                    connection.sendRawCommand(command)
                }
                val cleanedResponse = parseISOTPDelimiter(response)
                val responseData = extractDataFromResponse(response, 0x2E)

                if (isPositiveResponse(responseData, 0x2E)) {
                    Log.i(TAG, "UDS WriteDID $did successful")
                    emit(UDSResponse(true, 0x2E, responseData, rawResponse = cleanedResponse))
                    return@flow
                } else {
                    val errorCode = extractErrorCode(responseData)
                    Log.w(TAG, "UDS WriteDID failed: ${UDSError.fromCode(0x2E, errorCode ?: 0).description}")
                    emit(UDSResponse(false, 0x2E, responseData, errorCode, cleanedResponse))
                }
            } catch (e: Exception) {
                Log.e(TAG, "UDS WriteDID error: ${e.message}")
                emit(UDSResponse(false, 0x2E, ByteArray(0), null, e.message ?: ""))
            }
            retryCount++
            if (retryCount < MAX_RETRIES) delay(RETRY_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    fun communicationControl(controlType: Int, communicationType: Int): Flow<UDSResponse> = flow {
        try {
            val controlHex = controlType.toString(16).padStart(2, '0')
            val commHex = communicationType.toString(16).padStart(2, '0')
            val command = "28$controlHex$commHex"
            Log.d(TAG, "UDS CommunicationControl: $command")
            val response = withContext(Dispatchers.IO) {
                connection.sendRawCommand(command)
            }
            val cleanedResponse = parseISOTPDelimiter(response)
            val data = extractDataFromResponse(response, 0x28)

            if (isPositiveResponse(data, 0x28)) {
                emit(UDSResponse(true, 0x28, data, rawResponse = cleanedResponse))
            } else {
                val errorCode = extractErrorCode(data)
                emit(UDSResponse(false, 0x28, data, errorCode, cleanedResponse))
            }
        } catch (e: Exception) {
            Log.e(TAG, "UDS CommunicationControl error: ${e.message}")
            emit(UDSResponse(false, 0x28, ByteArray(0), null, e.message ?: ""))
        }
    }.flowOn(Dispatchers.IO)

    fun controlDTCSetting(dtcSettingType: Int): Flow<UDSResponse> = flow {
        try {
            val command = "85${dtcSettingType.toString(16).padStart(2, '0')}"
            Log.d(TAG, "UDS ControlDTCSetting: $command")
            val response = withContext(Dispatchers.IO) {
                connection.sendRawCommand(command)
            }
            val cleanedResponse = parseISOTPDelimiter(response)
            val data = extractDataFromResponse(response, 0x85)

            if (isPositiveResponse(data, 0x85)) {
                emit(UDSResponse(true, 0x85, data, rawResponse = cleanedResponse))
            } else {
                val errorCode = extractErrorCode(data)
                emit(UDSResponse(false, 0x85, data, errorCode, cleanedResponse))
            }
        } catch (e: Exception) {
            Log.e(TAG, "UDS ControlDTCSetting error: ${e.message}")
            emit(UDSResponse(false, 0x85, ByteArray(0), null, e.message ?: ""))
        }
    }.flowOn(Dispatchers.IO)

    fun ecuReset(resetType: Int): Flow<UDSResponse> = flow {
        try {
            val command = "11${resetType.toString(16).padStart(2, '0')}"
            Log.d(TAG, "UDS ECUReset: $command")
            val response = withContext(Dispatchers.IO) {
                connection.sendRawCommand(command)
            }
            val cleanedResponse = parseISOTPDelimiter(response)
            val data = extractDataFromResponse(response, 0x11)

            if (isPositiveResponse(data, 0x11)) {
                Log.i(TAG, "UDS ECU Reset successful")
                emit(UDSResponse(true, 0x11, data, rawResponse = cleanedResponse))
            } else {
                val errorCode = extractErrorCode(data)
                Log.w(TAG, "UDS ECU Reset failed: ${UDSError.fromCode(0x11, errorCode ?: 0).description}")
                emit(UDSResponse(false, 0x11, data, errorCode, cleanedResponse))
            }
        } catch (e: Exception) {
            Log.e(TAG, "UDS ECU Reset error: ${e.message}")
            emit(UDSResponse(false, 0x11, ByteArray(0), null, e.message ?: ""))
        }
    }.flowOn(Dispatchers.IO)

    fun getCurrentSession(): UDSSessionType = currentSession

    fun getSecurityLevel(): Int = securityLevel

    fun parseDIDValue(did: String, data: ByteArray): DIDValue {
        val rawData = data.drop(2).toByteArray()
        val parsed: Any? = when {
            rawData.size >= 17 && isPrintable(rawData) -> String(rawData, Charsets.US_ASCII).trim()
            rawData.size >= 2 -> parseNumericValue(rawData)
            rawData.size == 1 -> rawData[0].toInt() and 0xFF
            else -> null
        }
        return DIDValue(did, rawData, parsed, getDIDUnit(did))
    }

    private fun isPrintable(data: ByteArray): Boolean {
        return data.all { byte ->
            val intVal = byte.toInt() and 0xFF
            intVal in 0x20..0x7E || intVal == 0x00
        }
    }

    private fun parseNumericValue(data: ByteArray): Any {
        return when (data.size) {
            1 -> data[0].toInt() and 0xFF
            2 -> {
                val value = (data[0].toInt() and 0xFF) * 256 + (data[1].toInt() and 0xFF)
                if (data[0].toInt() and 0x80 != 0) value - 65536 else value
            }
            4 -> {
                val value = ((data[0].toInt() and 0xFF) shl 24) or
                        ((data[1].toInt() and 0xFF) shl 16) or
                        ((data[2].toInt() and 0xFF) shl 8) or
                        (data[3].toInt() and 0xFF)
                value.toDouble()
            }
            else -> data.joinToString("") { "%02X".format(it) }
        }
    }

    private fun getDIDUnit(did: String): String {
        return when (did.uppercase()) {
            "F190" -> ""
            "F191" -> ""
            "F4F0" -> ""
            "F4B0" -> "Nm"
            "F4C0" -> "kPa"
            "F4E0" -> "°C"
            "F4F1" -> "V"
            "F480" -> "L/h"
            else -> ""
        }
    }

    fun parseDTCFromResponse(data: ByteArray): List<Pair<String, Int>> {
        val dtcs = mutableListOf<Pair<String, Int>>()
        val dtcData = data.drop(2)
        var i = 0
        while (i + 3 < dtcData.size) {
            val dtcByte1 = dtcData[i].toInt() and 0xFF
            val dtcByte2 = dtcData[i + 1].toInt() and 0xFF
            val dtcByte3 = dtcData[i + 2].toInt() and 0xFF
            val status = dtcData[i + 3].toInt() and 0xFF

            val firstChar = when (dtcByte1 shr 6) {
                0 -> "P"
                1 -> "C"
                2 -> "B"
                3 -> "U"
                else -> "P"
            }
            val dtcCode = "$firstChar%02X%02X%02X".format(dtcByte1 and 0x3F, dtcByte2, dtcByte3)
            dtcs.add(dtcCode to status)
            i += 4
        }
        return dtcs
    }
}
