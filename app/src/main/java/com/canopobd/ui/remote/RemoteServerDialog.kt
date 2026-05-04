package com.canopobd.ui.remote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.bluetooth.RemoteBridge
import com.canopobd.ui.theme.*

@Composable
fun RemoteServerDialog(
    isRunning: Boolean,
    serverIp: String,
    serverPort: Int,
    connectedClients: Int,
    onDismiss: () -> Unit,
    onStartServer: (Int) -> Unit,
    onStopServer: () -> Unit
) {
    val context = LocalContext.current
    var portInput by remember { mutableStateOf(serverPort.toString()) }
    var customPort by remember { mutableStateOf(false) }

    LaunchedEffect(serverPort) {
        if (!customPort) {
            portInput = serverPort.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Wifi,
                            contentDescription = null,
                            tint = canopoAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Remote Server",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = canopoHighlight
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Handy als ELM327 Bridge für PC",
                    fontSize = 12.sp,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isRunning) {
                    RunningServerCard(
                        serverIp = serverIp,
                        serverPort = serverPort,
                        connectedClients = connectedClients,
                        onStopServer = onStopServer,
                        onCopyIp = { copyToClipboard(context, "$serverIp:$serverPort") }
                    )
                } else {
                    StartServerCard(
                        portInput = portInput,
                        onPortInputChange = {
                            portInput = it
                            customPort = true
                        },
                        onStartServer = {
                            val port = portInput.toIntOrNull() ?: RemoteBridge.DEFAULT_PORT
                            onStartServer(port)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                UsageInstructions()
            }
        }
    }
}

@Composable
private fun RunningServerCard(
    serverIp: String,
    serverPort: Int,
    connectedClients: Int,
    onStopServer: () -> Unit,
    onCopyIp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = gaugeGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = gaugeGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Server aktiv",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verbindung:",
                fontSize = 12.sp,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$serverIp:$serverPort",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onCopyIp) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "Kopieren",
                        tint = canopoAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$connectedClients PC(s) verbunden",
                fontSize = 12.sp,
                color = if (connectedClients > 0) gaugeGreen else textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStopServer,
                colors = ButtonDefaults.buttonColors(containerColor = gaugeRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Server stoppen")
            }
        }
    }
}

@Composable
private fun StartServerCard(
    portInput: String,
    onPortInputChange: (String) -> Unit,
    onStartServer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = canopoDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Router,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Port (Standard: ${RemoteBridge.DEFAULT_PORT})",
                fontSize = 12.sp,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = portInput,
                onValueChange = onPortInputChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = canopoAccent,
                    unfocusedBorderColor = textSecondary,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartServer,
                colors = ButtonDefaults.buttonColors(containerColor = gaugeGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Server starten")
            }
        }
    }
}

@Composable
private fun UsageInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = canopoDark.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Verwendung mit PC-Software",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = canopoAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            val exampleCommands = listOf(
                "ATZ - Reset",
                "010C - RPM abfragen",
                "ATRV - Batteriespannung"
            )

            exampleCommands.forEach { cmd ->
                Text(
                    text = "• $cmd",
                    fontSize = 12.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PC Software-Beispiele:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )

            Text(
                text = "• OBD Auto Doctor",
                fontSize = 11.sp,
                color = textSecondary,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "• ScanMaster",
                fontSize = 11.sp,
                color = textSecondary,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = "• Torque Pro (mit Adapter)",
                fontSize = 11.sp,
                color = textSecondary,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("OBD Server", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Kopiert: $text", Toast.LENGTH_SHORT).show()
}
