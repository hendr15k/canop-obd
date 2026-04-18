package com.canopobd.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.canopobd.data.model.OBDPID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * ELM327 OBD-II adapter via Bluetooth
 * Handles AT commands and PID requests
 */
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
        // Standard Bluetooth SPP UUID for OBD-II adapters
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    /**
     * Connect to a Bluetooth device (ELM327 adapter)
     */
    suspend fun connect(device: BluetoothDevice): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter.cancelDiscovery()
            socket?.connect()
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            _isConnected.value = true

            // Initialize ELM327
            initELM327()
            Result.success(Unit)
        } catch (e: IOException) {
            _isConnected.value = false
            Result.failure(e)
        }
    }

    /**
     * Initialize ELM327 with standard AT commands
     *
     * Inter-command delays are required: ELM327 needs processing time after
     * each command. After reset (ATZ) it may take up to 500ms to be ready.
     * Adaptive timing (ATAT1) means the adapter handles flow control for PIDs,
     * but AT commands still need small delays.
     */
    private suspend fun initELM327() {
        sendCommand("ATZ")       // Reset — wait 1s for full firmware boot
        delay(1000)
        sendCommand("ATI")       // Query device ID — verify it responded
        delay(200)
        sendCommand("ATE0")      // Echo off
        delay(100)
        sendCommand("ATL0")      // Linefeeds off
        delay(100)
        sendCommand("ATS0")      // Spaces off
        delay(100)
        sendCommand("ATH0")      // Headers off
        delay(100)
        sendCommand("ATSP0")     // Auto protocol
        delay(100)
        sendCommand("ATAT1")     // Adaptive timing on
    }

    /**
     * Send an OBD-II PID request and return parsed response
     */
    suspend fun requestPID(pid: OBDPID): Double? = withContext(Dispatchers.IO) {
        try {
            val response = sendCommand(pid.code)
            parseResponse(response, pid)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Read multiple PIDs in one pass (faster)
     */
    suspend fun readMultiplePIDs(pids: List<OBDPID>): Map<OBDPID, Double> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<OBDPID, Double>()
        for (pid in pids) {
            requestPID(pid)?.let { results[pid] = it }
        }
        results
    }

    /**
     * Send a raw command to ELM327
     */
    private suspend fun sendCommand(cmd: String): String = withContext(Dispatchers.IO) {
        val output = outputStream ?: throw IOException("Not connected")
        val input = inputStream ?: throw IOException("Not connected")

        // Send command with CR
        output.write("$cmd\r".toByteArray())
        output.flush()

        // Read response (ELM327 sends back echo + response + prompt)
        val buffer = ByteArray(256)
        val bytesRead = input.read(buffer)
        val response = String(buffer, 0, bytesRead, Charsets.US_ASCII)

        // Clean response: remove echo, spaces, and check for errors
        cleanResponse(response)
    }

    /**
     * Parse ELM327 response string
     */
    private fun parseResponse(response: String, pid: OBDPID): Double? {
        // Response format: "41 0C 1A F8" for RPM
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (hex.contains("ERROR") || hex.isEmpty()) return null

        // Strip the response code (41 for single PID)
        val dataHex = hex.drop(2)
        if (dataHex.length < pid.byteCount * 2) return null

        val bytes = ByteArray(pid.byteCount) { i ->
            dataHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }

        return pid.formula(bytes)
    }

    /**
     * Clean ELM327 response string
     */
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

    /**
     * Disconnect from adapter
     */
    fun disconnect() {
        scope.cancel()
        try {
            socket?.close()
        } catch (e: IOException) {
            // Ignore
        }
        socket = null
        inputStream = null
        outputStream = null
        _isConnected.value = false
    }

    /**
     * Get list of paired Bluetooth devices
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
    }
}
