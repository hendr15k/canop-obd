package com.canopobd.data.protocol

import android.util.Log
import com.canopobd.bluetooth.ELM327BTConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class Mode22DIDInfo(
    val code: String,
    val name: String,
    val category: DIDCategory,
    val unit: String,
    val byteCount: Int,
    val formula: (ByteArray) -> Double,
    val description: String = ""
)

enum class DIDCategory(val displayName: String) {
    ENGINE("Motor"),
    TURBO("Turbolader"),
    FUEL("Kraftstoff"),
    TRANSMISSION("Getriebe"),
    TEMPERATURE("Temperatur"),
    PRESSURE("Druck"),
    ELECTRICAL("Elektrik"),
    VEHICLE("Fahrzeug"),
    UNKNOWN("Unbekannt")
}

class Mode22Client(private val connection: ELM327BTConnection) {
    companion object {
        private const val TAG = "Mode22Client"
        private const val COMMAND_TIMEOUT_MS = 2000L
        private const val DID_DISCOVERY_TIMEOUT_MS = 5000L

        val GM_OPEL_DIDS = mapOf(
            "F4F0" to Mode22DIDInfo(
                "F4F0", "ECU Information", DIDCategory.VEHICLE, "", 16,
                { _ -> 0.0 }, "ECU Software Version und Kalibrierungs-ID"
            ),
            "F4B0" to Mode22DIDInfo(
                "F4B0", "Engine Torque", DIDCategory.ENGINE, "Nm", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF) - 500).toDouble() else 0.0 },
                "Motor Drehmoment aktuell"
            ),
            "F4C0" to Mode22DIDInfo(
                "F4C0", "Boost Pressure Target", DIDCategory.TURBO, "kPa", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0 },
                "Ziel-Ladedruck"
            ),
            "F4E0" to Mode22DIDInfo(
                "F4E0", "Coolant Temperature", DIDCategory.TEMPERATURE, "°C", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
                "Kuehlmitteltemperatur"
            ),
            "F4F1" to Mode22DIDInfo(
                "F4F1", "Battery Voltage", DIDCategory.ELECTRICAL, "V", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 10.0 else 0.0 },
                "Batteriespannung"
            ),
            "F480" to Mode22DIDInfo(
                "F480", "Fuel Consumption", DIDCategory.FUEL, "L/h", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 20.0 else 0.0 },
                "Kraftstoffverbrauch aktuell"
            ),
            "F190" to Mode22DIDInfo(
                "F190", "VIN", DIDCategory.VEHICLE, "", 17,
                { _ -> 0.0 }, "Fahrzeug-Identifizierungsnummer"
            ),
            "F18C" to Mode22DIDInfo(
                "F18C", "ECU Part Number", DIDCategory.VEHICLE, "", 8,
                { _ -> 0.0 }, "ECU Teilenummer"
            ),
            "F18D" to Mode22DIDInfo(
                "F18D", "ECU Hardware Version", DIDCategory.VEHICLE, "", 4,
                { _ -> 0.0 }, "ECU Hardware Version"
            )
        )

        val ENGINE_DATA_DIDS = mapOf(
            "0100" to Mode22DIDInfo(
                "0100", "RPM", DIDCategory.ENGINE, "rpm", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 4.0 else 0.0 },
                "Motordrehzahl"
            ),
            "0104" to Mode22DIDInfo(
                "0104", "Engine Load", DIDCategory.ENGINE, "%", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0 },
                "Motorlast"
            ),
            "0105" to Mode22DIDInfo(
                "0105", "Coolant Temp", DIDCategory.TEMPERATURE, "°C", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
                "Kuehlmitteltemperatur"
            ),
            "0106" to Mode22DIDInfo(
                "0106", "STFT Bank 1", DIDCategory.FUEL, "%", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0 },
                "Kurzzeit-Kraftstoffkorrektur Bank 1"
            ),
            "0107" to Mode22DIDInfo(
                "0107", "LTFT Bank 1", DIDCategory.FUEL, "%", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0 },
                "Langzeit-Kraftstoffkorrektur Bank 1"
            ),
            "010B" to Mode22DIDInfo(
                "010B", "Intake MAP", DIDCategory.PRESSURE, "kPa", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0 },
                "Ansaugrohr-Unterdruck"
            ),
            "010C" to Mode22DIDInfo(
                "010C", "RPM High", DIDCategory.ENGINE, "rpm", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 4.0 else 0.0 },
                "Motordrehzahl (High Resolution)"
            ),
            "010D" to Mode22DIDInfo(
                "010D", "Vehicle Speed", DIDCategory.VEHICLE, "km/h", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0 },
                "Fahrzeuggeschwindigkeit"
            ),
            "010F" to Mode22DIDInfo(
                "010F", "Intake Air Temp", DIDCategory.TEMPERATURE, "°C", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
                "Ansauglufttemperatur"
            ),
            "0111" to Mode22DIDInfo(
                "0111", "Throttle Position", DIDCategory.ENGINE, "%", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0 },
                "Drosselklappenstellung"
            ),
            "0114" to Mode22DIDInfo(
                "0114", "O2 Bank 1 Sensor 1 Voltage", DIDCategory.ENGINE, "V", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 200.0 else 0.0 },
                "Lambda-Spannung Bank 1 Sensor 1"
            )
        )

        val VEHICLE_DATA_DIDS = mapOf(
            "0200" to Mode22DIDInfo(
                "0200", "OBD Support", DIDCategory.VEHICLE, "", 4,
                { _ -> 0.0 }, "Unterstuetzte OBD-PIDs"
            ),
            "0201" to Mode22DIDInfo(
                "0201", "DTC Count", DIDCategory.VEHICLE, "", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0 },
                "Anzahl gespeicherter Fehlercodes"
            )
        )

        val TURBO_SPECIFIC_DIDS = mapOf(
            "220001" to Mode22DIDInfo(
                "220001", "Actual Torque", DIDCategory.ENGINE, "Nm", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 125).toDouble() else 0.0 },
                "Aktuelles Motordrehmoment"
            ),
            "220002" to Mode22DIDInfo(
                "220002", "Turbo Boost Actual", DIDCategory.TURBO, "kPa", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0 },
                "Aktueller Ladedruck"
            ),
            "220003" to Mode22DIDInfo(
                "220003", "Turbo Boost Target", DIDCategory.TURBO, "kPa", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0 },
                "Soll-Ladedruck"
            ),
            "220004" to Mode22DIDInfo(
                "220004", "Wastegate Duty", DIDCategory.TURBO, "%", 1,
                { b -> if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0 },
                "Wastegate-Stellung"
            ),
            "220005" to Mode22DIDInfo(
                "220005", "Turbo Speed", DIDCategory.TURBO, "rpm", 2,
                { b -> if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0 },
                "Turbolader-Drehzahl"
            ),
            "220006" to Mode22DIDInfo(
                "220006", "Turbo Inlet Temp", DIDCategory.TEMPERATURE, "°C", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
                "Turbo-Einlasstemperatur"
            ),
            "220007" to Mode22DIDInfo(
                "220007", "Turbo Outlet Temp", DIDCategory.TEMPERATURE, "°C", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
                "Turbo-Auslasstemperatur"
            ),
            "220008" to Mode22DIDInfo(
                "220008", "Charge Air Temp", DIDCategory.TEMPERATURE, "°C", 1,
                { b -> if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0 },
                "Ladelufttemperatur"
            )
        )

        val ALL_DIDS: Map<String, Mode22DIDInfo> =
            GM_OPEL_DIDS + ENGINE_DATA_DIDS + VEHICLE_DATA_DIDS + TURBO_SPECIFIC_DIDS
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _cachedValues = MutableStateFlow<Map<String, ByteArray>>(emptyMap())
    val cachedValues: StateFlow<Map<String, ByteArray>> = _cachedValues.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _discoveredDIDs = MutableStateFlow<List<String>>(emptyList())
    val discoveredDIDs: StateFlow<List<String>> = _discoveredDIDs.asStateFlow()

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            connection.sendRawCommand("ATZ")
            delay(1000)
            connection.sendRawCommand("ATE0")
            delay(100)
            connection.sendRawCommand("ATL0")
            delay(100)
            connection.sendRawCommand("ATS0")
            delay(100)
            connection.sendRawCommand("ATSP6")
            delay(100)
            connection.sendRawCommand("ATAT1")
            delay(100)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Init failed: ${e.message}")
            Result.failure(e)
        }
    }

    fun readDID(did: String): Flow<ByteArray?> = flow {
        try {
            val cleanDid = did.uppercase().replace(" ", "").removePrefix("0X").removePrefix("22")
            val command = "22$cleanDid"
            val response = connection.sendRawCommand(command)
            val data = parseDIDResponse(response, cleanDid)
            if (data != null) {
                val current = _cachedValues.value.toMutableMap()
                current[cleanDid] = data
                _cachedValues.value = current
                emit(data)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Read DID $did failed: ${e.message}")
            _lastError.value = e.message
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun readMultipleDIDs(dids: List<String>): Flow<Map<String, ByteArray?>> = flow {
        val results = mutableMapOf<String, ByteArray?>()
        for (did in dids) {
            val cleanDid = did.uppercase().replace(" ", "").removePrefix("0X").removePrefix("22")
            try {
                val command = "22$cleanDid"
                val response = connection.sendRawCommand(command)
                val data = parseDIDResponse(response, cleanDid)
                results[cleanDid] = data
                delay(50)
            } catch (e: Exception) {
                Log.w(TAG, "Read DID $did failed: ${e.message}")
                results[cleanDid] = null
            }
        }
        emit(results)
    }.flowOn(Dispatchers.IO)

    fun discoverAvailableDIDs(): Flow<List<String>> = flow {
        val discovered = mutableListOf<String>()
        val priorityDIDs = listOf(
            "F4F0", "F4B0", "F4C0", "F4E0", "F4F1", "F480",
            "F190", "F18C", "F18D", "220001", "220002", "220003",
            "220004", "220005", "220006", "220007", "220008",
            "0100", "0104", "0105", "010F", "0111", "010D"
        )
        for (did in priorityDIDs) {
            try {
                val command = "22$did"
                val response = connection.sendRawCommand(command)
                if (!response.contains("ERROR") && !response.contains("UNABLE") &&
                    !response.contains("NO DATA") && response.length > 10) {
                    discovered.add(did)
                }
                delay(100)
            } catch (e: Exception) {
                Log.w(TAG, "Discovery failed for $did: ${e.message}")
            }
        }
        _discoveredDIDs.value = discovered
        emit(discovered)
    }.flowOn(Dispatchers.IO)

    fun getParsedValue(did: String, rawData: ByteArray?): Double {
        if (rawData == null) return 0.0
        val info = ALL_DIDS[did.uppercase()]
        return info?.formula?.invoke(rawData) ?: 0.0
    }

    fun getDIDInfo(did: String): Mode22DIDInfo? {
        return ALL_DIDS[did.uppercase()]
    }

    fun getAllDIDCategories(): List<DIDCategory> {
        return ALL_DIDS.values.map { it.category }.distinct()
    }

    fun getDIDsByCategory(category: DIDCategory): List<Mode22DIDInfo> {
        return ALL_DIDS.values.filter { it.category == category }
    }

    fun formatValue(did: String, value: Double): String {
        val info = ALL_DIDS[did.uppercase()] ?: return "%.2f".format(value)
        return when (info.unit) {
            "%" -> "%.1f%%".format(value)
            "°C" -> "%.1f°C".format(value)
            "Nm" -> "%.0f Nm".format(value)
            "kPa" -> "%.1f kPa".format(value)
            "V" -> "%.1f V".format(value)
            "rpm" -> "%.0f rpm".format(value)
            "L/h" -> "%.2f L/h".format(value)
            else -> "%.2f".format(value)
        }
    }

    private fun parseDIDResponse(response: String, expectedDid: String): ByteArray? {
        val cleaned = response.replace("\r", " ").replace("\n", " ")
            .replace(">", "").trim().replace(" ", "")

        if (cleaned.contains("ERROR") || cleaned.contains("UNABLE") ||
            cleaned.contains("NO DATA") || cleaned.isBlank()) {
            return null
        }

        val expectedPrefix = "62${expectedDid.uppercase()}"
        if (!cleaned.uppercase().startsWith(expectedPrefix)) {
            val genericPrefix = "62"
            if (cleaned.uppercase().startsWith(genericPrefix) && cleaned.length > 2 + expectedDid.length) {
                val dataHex = cleaned.substring(2 + expectedDid.length)
                return parseHexString(dataHex)
            }
            return null
        }

        val dataHex = cleaned.substring(expectedPrefix.length)
        return parseHexString(dataHex)
    }

    private fun parseHexString(hex: String): ByteArray? {
        if (hex.length < 2 || hex.length % 2 != 0) return null
        return try {
            ByteArray(hex.length / 2) { i ->
                hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Parse hex failed: ${e.message}")
            null
        }
    }

    fun getCachedValue(did: String): ByteArray? {
        val cleanDid = did.uppercase().removePrefix("22")
        return _cachedValues.value[cleanDid]
    }

    fun getCachedParsedValue(did: String): Double {
        val rawData = getCachedValue(did)
        return getParsedValue(did, rawData)
    }

    fun clearCache() {
        _cachedValues.value = emptyMap()
    }

    fun shutdown() {
        scope.cancel()
    }
}
