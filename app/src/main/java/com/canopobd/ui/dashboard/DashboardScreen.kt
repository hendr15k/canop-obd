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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canopoDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
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
                        Icon(Icons.Filled.Search, contentDescription = null, tint = canopoAccent)
                    }
                    if (connectionState is OBDConnectionState.Connected) {
                        IconButton(onClick = onDisconnect) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = gaugeRed)
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
                SecondaryGauge(label = "Throttle", value = obdData.throttle, unit = "%", max = 100f)
                SecondaryGauge(label = "Engine Load", value = obdData.engineLoad, unit = "%", max = 100f)
                SecondaryGauge(label = "Fuel", value = obdData.fuelLevel, unit = "%", max = 100f)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "Battery: %.1fV".format(obdData.batteryVoltage),
                fontSize = 12.sp,
                color = textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Device picker as overlay dialog
        if (showDevicePicker) {
            DevicePickerDialog(
                devices = devices,
                onSelect = onConnect,
                onDismiss = onToggleDevicePicker
            )
        }
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
private fun DevicePickerDialog(
    devices: List<BluetoothDeviceInfo>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = canopoSurface,
        title = {
            Text("OBD Adapter wählen", color = textPrimary)
        },
        text = {
            if (devices.isEmpty()) {
                Text(
                    "Keine gekoppelten Geräte.\nBitte kopple deinen ELM327 in den Android Bluetooth-Einstellungen.",
                    color = textSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(devices) { device ->
                        DeviceListItem(
                            name = device.name,
                            address = device.address,
                            onClick = { onSelect(device.address) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = canopoAccent)
            }
        }
    )
}

@Composable
private fun DeviceListItem(
    name: String,
    address: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = canopoAccent,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textPrimary)
                Text(address, fontSize = 11.sp, color = textSecondary)
            }
        }
    }
}
