package com.canopobd.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

@SuppressLint("MissingPermission")
class RemoteBridge(
    private val context: Context,
    private val elmConnection: ELM327BTConnection
) {
    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning

    private val _connectedClients = MutableStateFlow(0)
    val connectedClients: StateFlow<Int> = _connectedClients

    private val _serverPort = MutableStateFlow(DEFAULT_PORT)
    val serverPort: StateFlow<Int> = _serverPort

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val clients = mutableListOf<ClientHandler>()

    companion object {
        const val DEFAULT_PORT = 35000
        const val DEFAULT_HOST = "192.168.4.1"
    }

    fun getLocalIpAddress(): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipInt = wifiManager.connectionInfo.ipAddress
            return String.format(
                "%d.%d.%d.%d",
                ipInt and 0xff,
                ipInt shr 8 and 0xff,
                ipInt shr 16 and 0xff,
                ipInt shr 24 and 0xff
            )
        } catch (e: Exception) {
            return "0.0.0.0"
        }
    }

    fun startServer(port: Int = DEFAULT_PORT): Result<Int> {
        return try {
            stopServer()
            serverSocket = ServerSocket(port)
            _serverPort.value = port
            _isServerRunning.value = true

            serverJob = scope.launch {
                while (isActive && serverSocket?.isClosed == false) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        if (clientSocket != null) {
                            val handler = ClientHandler(clientSocket)
                            clients.add(handler)
                            _connectedClients.value = clients.size
                            handler.start()
                        }
                    } catch (e: Exception) {
                        if (serverSocket?.isClosed == false) {
                            delay(100)
                        }
                    }
                }
            }
            Result.success(port)
        } catch (e: Exception) {
            _isServerRunning.value = false
            Result.failure(e)
        }
    }

    fun stopServer() {
        serverJob?.cancel()
        serverJob = null
        clients.forEach { it.close() }
        clients.clear()
        _connectedClients.value = 0
        try {
            serverSocket?.close()
        } catch (_: Exception) { }
        serverSocket = null
        _isServerRunning.value = false
    }

    private inner class ClientHandler(
        private val socket: Socket
    ) {
        private var writer: PrintWriter? = null
        private var reader: BufferedReader? = null
        private var isRunning = true

        fun start() {
            scope.launch {
                try {
                    writer = PrintWriter(socket.getOutputStream(), true)
                    reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.US_ASCII))

                    sendPrompt()

                    while (isRunning && socket.isConnected && !socket.isClosed) {
                        val line = reader?.readLine() ?: break
                        if (line.isNotEmpty()) {
                            handleCommand(line.trim())
                        }
                    }
                } catch (e: Exception) {
                } finally {
                    close()
                }
            }
        }

        private fun handleCommand(cmd: String) {
            val response = when {
                cmd.equals("ATRV", ignoreCase = true) -> {
                    scope.launch {
                        val voltage = elmConnection.getBatteryVoltage()
                        writer?.println(if (voltage != null) "${voltage}V" else "0V")
                        sendPrompt()
                    }
                    return
                }
                cmd.equals("ATZ", ignoreCase = true) -> "ELM327 v1.5"
                cmd.equals("ATI", ignoreCase = true) -> "ELM327 v1.5"
                cmd.equals("ATE0", ignoreCase = true) -> "OK"
                cmd.equals("ATL0", ignoreCase = true) -> "OK"
                cmd.equals("ATS0", ignoreCase = true) -> "OK"
                cmd.equals("ATH0", ignoreCase = true) -> "OK"
                cmd.equals("ATSP0", ignoreCase = true) -> "OK"
                cmd.equals("ATAT1", ignoreCase = true) -> "OK"
                cmd.startsWith("01") || cmd.startsWith("02") || cmd.startsWith("03") || 
                cmd.startsWith("04") || cmd.startsWith("05") || cmd.startsWith("06") || 
                cmd.startsWith("07") || cmd.startsWith("08") || cmd.startsWith("09") -> {
                    scope.launch {
                        val response = sendPIDCommand(cmd)
                        writer?.println(response)
                        sendPrompt()
                    }
                    return
                }
                else -> "?"
            }
            writer?.println(response)
            sendPrompt()
        }

        private suspend fun sendPIDCommand(pid: String): String {
            return try {
                val response = elmConnection.sendRawCommand(pid)
                cleanResponse(response)
            } catch (e: Exception) {
                "ERROR"
            }
        }

        private fun cleanResponse(response: String): String {
            return response
                .replace("\r", "")
                .replace("\n", " ")
                .replace(">", "")
                .trim()
                .filter { it.isDigit() || it.isLetter() || it == ' ' || it == ':' }
                .trim()
        }

        private fun sendPrompt() {
            writer?.print("> ")
            writer?.flush()
        }

        fun close() {
            isRunning = false
            try {
                writer?.close()
                reader?.close()
                socket.close()
            } catch (_: Exception) { }
            clients.remove(this)
            _connectedClients.value = clients.size
        }
    }

    suspend fun sendRawCommand(cmd: String): String {
        val output = elmConnection.getOutputStream() ?: throw IllegalStateException("Not connected")
        val input = elmConnection.getInputStream() ?: throw IllegalStateException("Not connected")

        try { while (input.available() > 0) input.read(ByteArray(64)) } catch (_: Exception) { }

        output.write("$cmd\r".toByteArray())
        output.flush()

        val deadline = System.currentTimeMillis() + 3_000L
        val responseBuilder = StringBuilder()

        while (System.currentTimeMillis() < deadline) {
            if (input.available() > 0) {
                val buffer = ByteArray(256)
                val bytesRead = input.read(buffer)
                if (bytesRead > 0) {
                    responseBuilder.append(String(buffer, 0, bytesRead, Charsets.US_ASCII))
                    if (responseBuilder.contains(">")) break
                }
            } else {
                delay(50L)
            }
        }

        return responseBuilder.toString()
    }
}

private fun ELM327BTConnection.getOutputStream() = try {
    val field = ELM327BTConnection::class.java.getDeclaredField("outputStream")
    field.isAccessible = true
    field.get(this) as? java.io.OutputStream
} catch (_: Exception) { null }

private fun ELM327BTConnection.getInputStream() = try {
    val field = ELM327BTConnection::class.java.getDeclaredField("inputStream")
    field.isAccessible = true
    field.get(this) as? java.io.InputStream
} catch (_: Exception) { null }

private fun ELM327BTConnection.sendRawCommand(cmd: String): String {
    val output = getOutputStream() ?: throw IllegalStateException("Not connected")
    val input = getInputStream() ?: throw IllegalStateException("Not connected")

    try { while (input.available() > 0) input.read(ByteArray(64)) } catch (_: Exception) { }

    output.write("$cmd\r".toByteArray())
    output.flush()

    val deadline = System.currentTimeMillis() + 3_000L
    val responseBuilder = StringBuilder()

    while (System.currentTimeMillis() < deadline) {
        if (input.available() > 0) {
            val buffer = ByteArray(256)
            val bytesRead = input.read(buffer)
            if (bytesRead > 0) {
                responseBuilder.append(String(buffer, 0, bytesRead, Charsets.US_ASCII))
                if (responseBuilder.contains(">")) break
            }
        } else {
            Thread.sleep(50)
        }
    }

    return responseBuilder.toString()
        .replace("\r", " ")
        .replace("\n", " ")
        .replace(" ", "")
        .replace(">", "")
        .trim()
        .filter { it.isDigit() || it.isLetter() || it == ' ' || it == ':' }
        .trim()
}
