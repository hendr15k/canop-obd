package com.canopobd.ui.dashboard

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.model.BluetoothDeviceInfo
import com.canopobd.data.model.OBDConnectionState
import com.canopobd.data.model.OBDData
import com.canopobd.ui.components.CircularGauge
import com.canopobd.ui.components.GaugeRow
import com.canopobd.ui.theme.*

@Composable
fun DashboardScreen(
    connectionState: OBDConnectionState,
    obdData: OBDData,
    devices: List<BluetoothDeviceInfo>,
    showDevicePicker: Boolean,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onToggleDevicePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    var hasPermissions by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(canopoDark)
            .padding(16.dp)
    ) {
        // Header with connection status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "canop-obd",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )
                Text(
                    text = when (connectionState) {
                        is OBDConnectionState.Connected -> "ELM327 Connected"
                        is OBDConnectionState.Connecting -> "Connecting..."
                        is OBDConnectionState.Disconnected -> "Not connected"
                        is OBDConnectionState.Error -> "Error: ${connectionState.message}"
                    },
                    fontSize = 12.sp,
                    color = when (connectionState) {
                        is OBDConnectionState.Connected -> gaugeGreen
                        is OBDConnectionState.Error -> gaugeRed
                        else -> textSecondary
                    }
                )
            }

            Row {
                IconButton(onClick = onToggleDevicePicker) {
                    Icon(Icons.Default.BluetoothSearching, tint = canopoAccent)
                }
                if (connectionState is OBDConnectionState.Connected) {
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.Default.Close, tint = gaugeRed)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main gauges
        GaugeRow(
            rpm = obdData.rpm.toFloat(),
            speed = obdData.speed.toFloat(),
            temp = obdData.coolantTemp.toFloat()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Secondary gauges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SecondaryGauge(
                label = "Throttle",
                value = obdData.throttle,
                unit = "%",
                max = 100f
            )
            SecondaryGauge(
                label = "Engine Load",
                value = obdData.engineLoad,
                unit = "%",
                max = 100f
            )
            SecondaryGauge(
                label = "Fuel",
                value = obdData.fuelLevel,
                unit = "%",
                max = 100f
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Battery voltage footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Battery: %.1fV".format(obdData.batteryVoltage),
                fontSize = 12.sp,
                color = textSecondary
            )
        }
    }

    // Device picker bottom sheet
    if (showDevicePicker) {
        DevicePickerSheet(
            devices = devices,
            onSelect = onConnect,
            onDismiss = onToggleDevicePicker
        )
    }
}

@Composable
private fun SecondaryGauge(
    label: String,
    value: Double,
    unit: String,
    max: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(canopoSurface)
            .padding(12.dp)
    ) {
        Text(
            text = "%.0f%s".format(value.coerceIn(0.0, max.toDouble()), unit),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = textSecondary
        )
    }
}

@Composable
private fun DevicePickerSheet(
    devices: List<BluetoothDeviceInfo>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("OBD Adapter wählen", color = textPrimary)
        },
        text = {
            if (devices.isEmpty()) {
                Text("Keine gekoppelten Geräte gefunden.\nBitte koppele deinen ELM327 zuerst in den Android Bluetooth-Einstellungen.", color = textSecondary)
            } else {
                LazyColumn {
                    items(devices) { device ->
                        ListItem(
                            headlineContent = { Text(device.name, color = textPrimary) },
                            supportingContent = { Text(device.address, color = textSecondary) },
                            leadingContent = {
                                Icon(Icons.Default.Bluetooth, tint = canopoAccent)
                            },
                            modifier = Modifier.clickable { onSelect(device.address) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
