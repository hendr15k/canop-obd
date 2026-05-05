package com.canopobd.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.canopobd.data.model.DTCResponse
import com.canopobd.data.model.DiagnosticTroubleCode
import com.canopobd.data.model.FreezeFrame
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
            "P0101" to "Mass Air Flow Circuit Range/Performance Problem",
            "P0102" to "Mass Air Flow Circuit Low Input",
            "P0103" to "Mass Air Flow Circuit High Input",
            "P0110" to "Intake Air Temperature Circuit Malfunction",
            "P0111" to "Intake Air Temperature Circuit Low Input",
            "P0112" to "Intake Air Temperature Circuit High Input",
            "P0113" to "Intake Air Temperature Circuit Intermittent",
            "P0115" to "Engine Coolant Temperature Circuit Malfunction",
            "P0116" to "Engine Coolant Temperature Range/Performance",
            "P0117" to "Engine Coolant Temperature Low Input",
            "P0118" to "Engine Coolant Temperature High Input",
            "P0120" to "Throttle Position Sensor Circuit Malfunction",
            "P0121" to "Throttle Position Sensor Range/Performance",
            "P0122" to "Throttle Position Sensor Circuit Low Input",
            "P0123" to "Throttle Position Sensor Circuit High Input",
            "P0130" to "O2 Sensor Circuit Malfunction (Bank 1 Sensor 1)",
            "P0131" to "O2 Sensor Circuit Low Voltage (Bank 1 Sensor 1)",
            "P0132" to "O2 Sensor Circuit High Voltage (Bank 1 Sensor 1)",
            "P0133" to "O2 Sensor Slow Response (Bank 1 Sensor 1)",
            "P0134" to "O2 Sensor No Activity Detected (Bank 1 Sensor 1)",
            "P0135" to "O2 Sensor Heater Circuit Malfunction (Bank 1 Sensor 1)",
            "P0136" to "O2 Sensor Circuit Malfunction (Bank 1 Sensor 2)",
            "P0137" to "O2 Sensor Circuit Low Voltage (Bank 1 Sensor 2)",
            "P0138" to "O2 Sensor Circuit High Voltage (Bank 1 Sensor 2)",
            "P0170" to "Fuel System Too Rich (Bank 1)",
            "P0171" to "Fuel System Too Lean (Bank 1)",
            "P0172" to "Fuel System Too Rich (Bank 1)",
            "P0173" to "Fuel System Too Rich (Bank 2)",
            "P0174" to "Fuel System Too Lean (Bank 2)",
            "P0175" to "Fuel System Too Rich (Bank 2)",
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
            "P0326" to "Knock Sensor 1 Range/Performance",
            "P0330" to "Knock Sensor 2 Circuit Malfunction",
            "P0335" to "Crankshaft Position Sensor Circuit Malfunction",
            "P0336" to "Crankshaft Position Sensor Range/Performance",
            "P0340" to "Camshaft Position Sensor Circuit Malfunction",
            "P0341" to "Camshaft Position Sensor Range/Performance",
            "P0400" to "EGR Flow Malfunction",
            "P0401" to "EGR Insufficient Flow Detected",
            "P0402" to "EGR Excessive Flow Detected",
            "P0403" to "EGR Control Circuit Malfunction",
            "P0404" to "EGR Control Range/Performance",
            "P0405" to "EGR Sensor A Circuit Low",
            "P0406" to "EGR Sensor A Circuit High",
            "P0407" to "EGR Sensor B Circuit Low",
            "P0408" to "EGR Sensor B Circuit High",
            "P0420" to "Catalyst System Efficiency Below Threshold (Bank 1)",
            "P0421" to "Catalyst System Efficiency Below Threshold (Bank 1)",
            "P0422" to "Catalyst System Efficiency Below Threshold (Bank 1)",
            "P0430" to "Catalyst System Efficiency Below Threshold (Bank 2)",
            "P0440" to "Evaporative Emission System Malfunction",
            "P0441" to "Evaporative Emission System Purge Flow Malfunction",
            "P0442" to "Evaporative Emission System Leak Detected (Small Leak)",
            "P0443" to "Evaporative Emission System Purge Control Circuit Malfunction",
            "P0444" to "Evaporative Emission System Purge Control Circuit Low",
            "P0445" to "Evaporative Emission System Purge Control Circuit High",
            "P0446" to "Evaporative Emission System Vent Control Malfunction",
            "P0447" to "Evaporative Emission System Vent Control Circuit Low",
            "P0448" to "Evaporative Emission System Vent Control Circuit High",
            "P0450" to "Evaporative Emission System Pressure Sensor Malfunction",
            "P0451" to "Evaporative Emission System Pressure Sensor Range/Performance",
            "P0452" to "Evaporative Emission System Pressure Sensor Low Input",
            "P0453" to "Evaporative Emission System Pressure Sensor High Input",
            "P0455" to "Evaporative Emission System Leak Detected (Gross Leak)",
            "P0456" to "Evaporative Emission System Leak Detected (Very Small Leak)",
            "P0500" to "Vehicle Speed Sensor Malfunction",
            "P0501" to "Vehicle Speed Sensor Range/Performance",
            "P0502" to "Vehicle Speed Sensor Low Input",
            "P0503" to "Vehicle Speed Sensor Intermittent/Bumpy",
            "P0505" to "Idle Control System Malfunction",
            "P0506" to "Idle Control System RPM Lower Than Expected",
            "P0507" to "Idle Control System RPM Higher Than Expected",
            "P0508" to "Idle Control System RPM Too Low",
            "P0509" to "Idle Control System RPM Too High",
            "P0510" to "Throttle Position Sensor Malfunction",
            "P0600" to "Serial Communication Link Malfunction",
            "P0601" to "Control Module Read Only Memory (ROM) Error",
            "P0602" to "Control Module Programming Error",
            "P0603" to "Control Module Keep Alive Memory (KAM) Error",
            "P0604" to "Control Module Random Access Memory (RAM) Error",
            "P0605" to "Control Module Read Only Memory (ROM) Error",
            "P0606" to "PCM Processor Fault",
            "P0700" to "Transmission Control System Malfunction",
            "P0703" to "Torque Converter Clutch Solenoid Circuit Malfunction",
            "P0705" to "Transmission Range Sensor Circuit Malfunction",
            "P0707" to "Transmission Range Sensor Low Input",
            "P0708" to "Transmission Range Sensor High Input",
            "P0710" to "Transmission Fluid Temperature Sensor Malfunction",
            "P0715" to "Input/Turbine Speed Sensor Circuit Malfunction",
            "P0717" to "Input/Turbine Speed Sensor No Signal",
            "P0720" to "Output Speed Sensor Circuit Malfunction",
            "P0722" to "Output Speed Sensor No Signal",
            "P0725" to "Engine Speed Input Circuit Malfunction",
            "P0730" to "Incorrect Gear Ratio",
            "P0731" to "Gear 1 Incorrect Ratio",
            "P0732" to "Gear 2 Incorrect Ratio",
            "P0733" to "Gear 3 Incorrect Ratio",
            "P0734" to "Gear 4 Incorrect Ratio",
            "P0735" to "Gear 5 Incorrect Ratio",
            "P0740" to "Torque Converter Clutch Solenoid Circuit Malfunction",
            "P0741" to "Torque Converter Clutch Solenoid Performance",
            "P0742" to "Torque Converter Clutch Solenoid Stuck On",
            "P0743" to "Torque Converter Clutch Solenoid Circuit Electrical",
            "P0750" to "Shift Solenoid A Malfunction",
            "P0751" to "Shift Solenoid A Performance/No Shift",
            "P0752" to "Shift Solenoid A Stuck On",
            "P0753" to "Shift Solenoid A Electrical",
            "P0755" to "Shift Solenoid B Malfunction",
            "P0756" to "Shift Solenoid B Performance/No Shift",
            "P0757" to "Shift Solenoid B Stuck On",
            "P0758" to "Shift Solenoid B Electrical",
            "C0000" to "TCS Malfunction",
            "C0035" to "Left Front Wheel Speed Sensor Malfunction",
            "C0040" to "Right Front Wheel Speed Sensor Malfunction",
            "C0045" to "Left Rear Wheel Speed Sensor Malfunction",
            "C0050" to "Right Rear Wheel Speed Sensor Malfunction",
            "C0060" to "Left Front ABS Solenoid Malfunction",
            "C0065" to "Right Front ABS Solenoid Malfunction",
            "C0070" to "Left Rear ABS Solenoid Malfunction",
            "C0075" to "Right Rear ABS Solenoid Malfunction",
            "C0080" to "ABS Pump Motor Malfunction",
            "C0085" to "ABS Pump Motor Speed Sensor Malfunction",
            "C0090" to "Left Front Wheel Speed Sensor Signal Malfunction",
            "B0001" to "Driver Airbag Circuit Resistance Low",
            "B0002" to "Driver Airbag Circuit Resistance High",
            "B0003" to "Driver Airbag Circuit Open",
            "B0004" to "Driver Airbag Circuit Short to Ground",
            "B0005" to "Driver Airbag Circuit Short to Power",
            "B0010" to "Passenger Airbag Circuit Resistance Low",
            "B0011" to "Passenger Airbag Circuit Resistance High",
            "B0100" to "Interior Lamp Circuit Malfunction",
            "B0101" to "Headlamp Relay Circuit Malfunction",
            "U0001" to "High Speed CAN Communication Bus Malfunction",
            "U0100" to "Lost Communication With ECM/PCM",
            "U0101" to "Lost Communication With TCM",
            "U0121" to "Lost Communication With ABS Module",
            "U0140" to "Lost Communication With BCM",
            "U0155" to "Lost Communication With Instrument Cluster"
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

    suspend fun readVIN(): String = withContext(Dispatchers.IO) {
        try {
            sendCommand("0902")
            delay(200)
            val response = sendCommandWithTimeout("0902")
            parseVIN(response)
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseVIN(response: String): String {
        val hex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
        if (hex.contains("ERROR") || hex.isEmpty()) return ""
        val cleanHex = hex.drop(6)
        if (cleanHex.isEmpty()) return ""
        val chars = cleanHex.chunked(2).mapNotNull { byteStr ->
            if (byteStr.length == 2) {
                val intValue = byteStr.toInt(16)
                if (intValue in 0x20..0x7E) intValue.toChar() else null
            } else null
        }
        return chars.joinToString("")
    }

    suspend fun readFreezeFrames(): List<FreezeFrame> = withContext(Dispatchers.IO) {
        try {
            val response = sendCommandWithTimeout("02")
            if (response.contains("ERROR") || response.isBlank()) return@withContext emptyList()
            val dtcHex = response.replace(" ", "").replace("\r", "").replace("\n", "").trim()
            val dtcChars = dtcHex.drop(4).chunked(4)
            val frames = mutableListOf<FreezeFrame>()
            for (chunk in dtcChars) {
                if (chunk.length == 4) {
                    val firstChar = when (chunk[0]) {
                        '0' -> "P0"; '1' -> "P1"; '2' -> "P2"; '3' -> "P3"
                        '4' -> "C0"; '5' -> "C1"; '6' -> "C2"; '7' -> "C3"
                        '8' -> "B0"; '9' -> "B1"; 'A', 'a' -> "B2"; 'B', 'b' -> "B3"
                        'C', 'c' -> "U0"; 'D', 'd' -> "U1"; 'E', 'e' -> "U2"; 'F', 'f' -> "U3"
                        else -> "P0"
                    }
                    val code = "$firstChar${chunk.substring(1)}"
                    val description = DTC_DESCRIPTIONS[code] ?: "Unknown fault code"
                    val data = mutableMapOf<String, Double>()
                    try {
                        val rpmResp = sendCommandWithTimeout("020C")
                        if (!rpmResp.contains("ERROR")) {
                            val rpmHex = rpmResp.replace(" ", "").drop(6)
                            if (rpmHex.length >= 4) {
                                data["RPM"] = ((rpmHex.substring(0, 2).toInt(16) * 256 + rpmHex.substring(2, 4).toInt(16)) / 4.0)
                            }
                        }
                        val speedResp = sendCommandWithTimeout("020D")
                        if (!speedResp.contains("ERROR")) {
                            val speedHex = speedResp.replace(" ", "").drop(4)
                            if (speedHex.length >= 2) {
                                data["Speed"] = speedHex.substring(0, 2).toInt(16).toDouble()
                            }
                        }
                        val coolResp = sendCommandWithTimeout("0205")
                        if (!coolResp.contains("ERROR")) {
                            val coolHex = coolResp.replace(" ", "").drop(4)
                            if (coolHex.length >= 2) {
                                data["Coolant"] = (coolHex.substring(0, 2).toInt(16) - 40).toDouble()
                            }
                        }
                    } catch (_: Exception) {}
                    frames.add(FreezeFrame(DiagnosticTroubleCode(code, description), data))
                }
            }
            frames
        } catch (_: Exception) {
            emptyList()
        }
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

    suspend fun sendRawCommand(cmd: String): String = withContext(Dispatchers.IO) {
        sendCommandWithTimeout(cmd)
    }
}
