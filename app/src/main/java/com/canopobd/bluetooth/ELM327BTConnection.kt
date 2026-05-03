package com.canopobd.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.canopobd.data.model.DTCResponse
import com.canopobd.data.model.DiagnosticTroubleCode
import com.canopobd.data.model.OBDPID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class ELM327BTConnection(
    private val bluetoothAdapter: BluetoothAdapter
) {
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val MAX_RETRIES = 3

        private val DTC_DESCRIPTIONS = mapOf(
            "P0100" to "Mass Air Flow Circuit Malfunction",
            "P0101" to "Mass Air Flow Circuit Range/Performance",
            "P0102" to "Mass Air Flow Circuit Low Input",
            "P0103" to "Mass Air Flow Circuit High Input",
            "P0110" to "Intake Air Temperature Circuit Malfunction",
            "P0115" to "Engine Coolant Temperature Circuit Malfunction",
            "P0120" to "Throttle Position Sensor Circuit Malfunction",
            "P0130" to "O2 Sensor Circuit Malfunction (Bank 1 Sensor 1)",
            "P0135" to "O2 Sensor Heater Circuit Malfunction (Bank 1 Sensor 1)",
            "P0171" to "System Too Lean (Bank 1)",
            "P0172" to "System Too Rich (Bank 1)",
            "P0300" to "Random/Multiple Cylinder Misfire Detected",
            "P0301" to "Cylinder 1 Misfire Detected",
            "P0302" to "Cylinder 2 Misfire Detected",
            "P0303" to "Cylinder 3 Misfire Detected",
            "P0304" to "Cylinder 4 Misfire Detected",
            "P0305" to "Cylinder 5 Misfire Detected",
            "P0306" to "Cylinder 6 Misfire Detected",
            "P0307" to "Cylinder 7 Misfire Detected",
            "P0308" to "Cylinder 8 Misfire Detected",
            "P0325" to "Knock Sensor 1 Circuit Malfunction",
            "P0335" to "Crankshaft Position Sensor Circuit Malfunction",
            "P0400" to "EGR Flow Malfunction",
            "P0401" to "EGR Insufficient Flow Detected",
            "P0420" to "Catalyst System Efficiency Below Threshold (Bank 1)",
            "P0440" to "Evaporative Emission System Malfunction",
            "P0442" to "Evaporative Emission System Leak Detected (Small Leak)",
            "P0446" to "Evaporative Emission System Vent Control Malfunction",
            "P0455" to "Evaporative Emission System Leak Detected (Gross Leak)",
            "P0500" to "Vehicle Speed Sensor Malfunction",
            "P0505" to "Idle Control System Malfunction",
            "P0507" to "Idle Control System RPM Higher Than Expected",
            "P0600" to "Serial Communication Link Malfunction",
            "P0700" to "Transmission Control System Malfunction"
        )
    }

    suspend fun connect(device: BluetoothDevice): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter.cancelDiscovery()

            withTimeout(15_000L) {
                socket?.connect()
            }
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            _isConnected.value = true

            initELM327()
            Result.success(Unit)
        } catch (e: Exception) {
            _isConnected.value = false
            Result.failure(e)
        }
    }

    private suspend fun initELM327() {
        sendCommand("ATZ")
        delay(1000)
        sendCommand("ATI")
        delay(200)
        sendCommand("ATE0")
        delay(100)
        sendCommand("ATL0")
        delay(100)
        sendCommand("ATS0")
        delay(100)
        sendCommand("ATH0")
        delay(100)
        sendCommand("ATSP0")
        delay(100)
        sendCommand("ATAT1")
    }

    suspend fun requestPID(pid: OBDPID): Double? = withContext(Dispatchers.IO) {
        repeat(MAX_RETRIES) { attempt ->
            try {
                val response = sendCommandWithTimeout(pid.code)
                val result = parseResponse(response, pid)
                if (result != null) return@withContext result
            } catch (_: Exception) { }

            if (attempt < MAX_RETRIES - 1) delay(100L)
        }
        null
    }

    suspend fun readMultiplePIDs(pids: List<OBDPID>): Map<OBDPID, Double> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<OBDPID, Double>()
        for (pid in pids) {
            requestPID(pid)?.let { results[pid] = it }
        }
        results
    }

    suspend fun readDTCs(): DTCResponse = withContext(Dispatchers.IO) {
        val codes = mutableListOf<DiagnosticTroubleCode>()
        val pendingCodes = mutableListOf<DiagnosticTroubleCode>()

        try {
            val response = sendCommandWithTimeout("03")
            val dtcs = parseDTCCodes(response, false)
            codes.addAll(dtcs)
        } catch (_: Exception) { }

        try {
            val pendingResponse = sendCommandWithTimeout("07")
            val pending = parseDTCCodes(pendingResponse, true)
            pendingCodes.addAll(pending)
        } catch (_: Exception) { }

        DTCResponse(codes, pendingCodes)
    }

    suspend fun clearDTCs(): Boolean = withContext(Dispatchers.IO) {
        try {
            sendCommandWithTimeout("04")
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun parseDTCCodes(response: String, pending: Boolean): List<DiagnosticTroubleCode> {
        val codes = mutableListOf<DiagnosticTroubleCode>()
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        
        if (hex.contains("ERROR") || hex.isEmpty()) return codes
        
        val cleanHex = hex.drop(2)
        val chars = cleanHex.chunked(4)
        
        for (chunk in chars) {
            if (chunk.length == 4) {
                val firstChar = when (chunk[0]) {
                    '0' -> "P0"
                    '1' -> "P1"
                    '2' -> "P2"
                    '3' -> "P3"
                    '4' -> "C0"
                    '5' -> "C1"
                    '6' -> "C2"
                    '7' -> "C3"
                    '8' -> "B0"
                    '9' -> "B1"
                    'A', 'a' -> "B2"
                    'B', 'b' -> "B3"
                    'C', 'c' -> "U0"
                    'D', 'd' -> "U1"
                    'E', 'e' -> "U2"
                    'F', 'f' -> "U3"
                    else -> "P0"
                }
                val code = "$firstChar${chunk.substring(1)}"
                val description = DTC_DESCRIPTIONS[code] ?: "Unknown fault code"
                codes.add(DiagnosticTroubleCode(code, description, pending))
            }
        }
        return codes
    }

    suspend fun getBatteryVoltage(): Double? {
        return try {
            val response = sendCommandWithTimeout("ATRV")
            parseVoltageResponse(response)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVoltageResponse(response: String): Double? {
        val cleaned = response.replace("V", "").replace("v", "").trim()
        return cleaned.toDoubleOrNull()
    }

    private suspend fun sendCommand(cmd: String): String = withContext(Dispatchers.IO) {
        sendCommandWithTimeout(cmd)
    }

    private suspend fun sendCommandWithTimeout(cmd: String): String = withContext(Dispatchers.IO) {
        val output = outputStream ?: throw IOException("Not connected")
        val input = inputStream ?: throw IOException("Not connected")

        try { while (input.available() > 0) input.read(ByteArray(64)) } catch (_: Exception) { }

        output.write("$cmd\r".toByteArray())
        output.flush()

        val deadline = System.currentTimeMillis() + 3_000L
        val responseBuilder = StringBuilder()

        while (System.currentTimeMillis() < deadline) {
            if (input.available() > 0) {
                val buffer = ByteArray(256)
                val bytesRead = withContext(Dispatchers.IO) {
                    input.read(buffer)
                }
                if (bytesRead > 0) {
                    responseBuilder.append(String(buffer, 0, bytesRead, Charsets.US_ASCII))
                    if (responseBuilder.contains(">")) break
                }
            } else {
                delay(50L)
            }
        }

        cleanResponse(responseBuilder.toString())
    }

    private fun parseResponse(response: String, pid: OBDPID): Double? {
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (hex.contains("ERROR") || hex.isEmpty()) return null

        val dataHex = hex.drop(2)
        if (dataHex.length < pid.byteCount * 2) return null

        val bytes = ByteArray(pid.byteCount) { i ->
            dataHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }

        return pid.formula(bytes)
    }

    private fun cleanResponse(response: String): String {
        return response
            .replace("\r", " ")
            .replace("\n", " ")
            .replace(" ", "")
            .replace(">", "")
            .trim()
            .filter { it.isDigit() || it.isLetter() || it == ' ' || it == ':' }
            .trim()
    }

    fun disconnect() {
        scope.cancel()
        try {
            socket?.close()
        } catch (e: IOException) { }
        socket = null
        inputStream = null
        outputStream = null
        _isConnected.value = false
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
    }
}
