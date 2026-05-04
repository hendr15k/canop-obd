package com.canopobd.ui.dashboard

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.BluetoothDeviceInfo
import com.canopobd.data.model.ConnectionQuality
import com.canopobd.data.model.ConnectionStats
import com.canopobd.data.model.DTCResponse
import com.canopobd.data.model.DataRecord
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.data.model.OBDConnectionState
import com.canopobd.data.model.OBDData
import com.canopobd.data.model.TripData
import com.canopobd.ui.components.CircularGauge
import com.canopobd.ui.components.GaugeRow
import com.canopobd.ui.datalog.DataLogDialog
import com.canopobd.ui.dtc.DTCDialog
import com.canopobd.ui.pid.PIDDialog
import com.canopobd.ui.remote.RemoteServerDialog
import com.canopobd.ui.settings.SettingsDialog
import com.canopobd.ui.theme.*
import com.canopobd.ui.tripcomputer.TripComputerDialog

@Composable
fun DashboardScreen(
    connectionState: OBDConnectionState,
    obdData: OBDData,
    devices: List<BluetoothDeviceInfo>,
    showDevicePicker: Boolean,
    dtcResponse: DTCResponse?,
    recordingActive: Boolean,
    recordedData: List<DataRecord>,
    pollRate: Long,
    measurementUnit: MeasurementUnit,
    showDTCDialog: Boolean,
    showSettings: Boolean,
    showDataLog: Boolean,
    showPIDScreen: Boolean,
    showRemoteDialog: Boolean,
    showTripComputer: Boolean,
    remoteServerRunning: Boolean,
    remoteServerIp: String,
    remoteServerPort: Int,
    remoteConnectedClients: Int,
    tripData: TripData,
    connectionStats: ConnectionStats,
    autoReconnect: Boolean,
    errorMessage: String?,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onToggleDevicePicker: () -> Unit,
    onToggleDTCDialog: () -> Unit,
    onClearDTCs: () -> Unit,
    onToggleSettings: () -> Unit,
    onToggleDataLog: () -> Unit,
    onTogglePIDScreen: () -> Unit,
    onToggleRemoteDialog: () -> Unit,
    onToggleTripComputer: () -> Unit,
    onStartRemoteServer: (Int) -> Unit,
    onStopRemoteServer: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSetPollRate: (Long) -> Unit,
    onSetMeasurementUnit: (MeasurementUnit) -> Unit,
    onSetAutoReconnect: (Boolean) -> Unit,
    onResetTrip: () -> Unit,
    onGetStoredVin: () -> String,
    onGetExportData: () -> String,
    onClearRecordedData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(canopoDark)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                DashboardHeader(
                    connectionState = connectionState,
                    connectionStats = connectionStats,
                    remoteServerRunning = remoteServerRunning,
                    remoteConnectedClients = remoteConnectedClients,
                    onToggleTripComputer = onToggleTripComputer,
                    onToggleSettings = onToggleSettings,
                    onToggleDevicePicker = onToggleDevicePicker,
                    onToggleRemoteDialog = onToggleRemoteDialog,
                    onToggleDataLog = onToggleDataLog,
                    onTogglePIDScreen = onTogglePIDScreen,
                    onToggleDTCDialog = onToggleDTCDialog,
                    onDisconnect = onDisconnect,
                    recordingActive = recordingActive
                )

                Spacer(modifier = Modifier.height(24.dp))

                GaugeRow(
                    rpm = obdData.rpm.toFloat(),
                    speed = measurementUnit.convertSpeed(obdData.speed).toFloat(),
                    temp = measurementUnit.convertTemp(obdData.coolantTemp).toFloat(),
                    speedUnit = measurementUnit.speedUnit,
                    tempUnit = measurementUnit.tempUnit
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SecondaryGauge(label = "Throttle", value = obdData.throttle, unit = "%", max = 100f)
                    SecondaryGauge(label = "Engine Load", value = obdData.engineLoad, unit = "%", max = 100f)
                    SecondaryGauge(label = "Fuel", value = obdData.fuelLevel, unit = "%", max = 100f)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SecondaryGauge(label = "Timing", value = obdData.timingAdvance, unit = "°", max = 50f)
                    SecondaryGauge(label = "MAF", value = obdData.mafRate, unit = "g/s", max = 500f)
                    SecondaryGauge(label = "Intake", value = measurementUnit.convertTemp(obdData.intakeTemp), unit = measurementUnit.tempUnit, max = 300f)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Battery: %.1fV | Poll: ${pollRate}ms | Load: %.0f%%".format(obdData.batteryVoltage, obdData.absoluteLoadValue),
                    fontSize = 12.sp,
                    color = textSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        if (showDevicePicker) {
            DevicePickerDialog(
                devices = devices,
                onSelect = onConnect,
                onDismiss = onToggleDevicePicker
            )
        }

        if (showDTCDialog) {
            DTCDialog(
                dtcResponse = dtcResponse,
                onDismiss = onToggleDTCDialog,
                onClearDTCs = onClearDTCs
            )
        }

        if (showSettings) {
            SettingsDialog(
                pollRate = pollRate,
                measurementUnit = measurementUnit,
                autoReconnect = autoReconnect,
                onDismiss = onToggleSettings,
                onPollRateChange = onSetPollRate,
                onUnitChange = onSetMeasurementUnit,
                onAutoReconnectChange = onSetAutoReconnect
            )
        }

        if (showDataLog) {
            DataLogDialog(
                recordedData = recordedData,
                isRecording = recordingActive,
                onDismiss = onToggleDataLog,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onClearData = onClearRecordedData,
                onExportData = onGetExportData
            )
        }

        if (showPIDScreen) {
            PIDDialog(
                obdData = obdData,
                measurementUnit = measurementUnit,
                onDismiss = onTogglePIDScreen
            )
        }

        if (showRemoteDialog) {
            RemoteServerDialog(
                isRunning = remoteServerRunning,
                serverIp = remoteServerIp,
                serverPort = remoteServerPort,
                connectedClients = remoteConnectedClients,
                onDismiss = onToggleRemoteDialog,
                onStartServer = onStartRemoteServer,
                onStopServer = onStopRemoteServer
            )
        }

        if (showTripComputer) {
            TripComputerDialog(
                tripData = tripData,
                measurementUnit = measurementUnit,
                vin = onGetStoredVin(),
                onDismiss = onToggleTripComputer,
                onResetTrip = onResetTrip
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    connectionState: OBDConnectionState,
    connectionStats: ConnectionStats,
    remoteServerRunning: Boolean,
    remoteConnectedClients: Int,
    onToggleTripComputer: () -> Unit,
    onToggleSettings: () -> Unit,
    onToggleDevicePicker: () -> Unit,
    onToggleRemoteDialog: () -> Unit,
    onToggleDataLog: () -> Unit,
    onTogglePIDScreen: () -> Unit,
    onToggleDTCDialog: () -> Unit,
    onDisconnect: () -> Unit,
    recordingActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = canopoHighlight
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (connectionState) {
                        is OBDConnectionState.Connected -> stringResource(R.string.status_connected)
                        is OBDConnectionState.Connecting -> stringResource(R.string.status_connecting)
                        is OBDConnectionState.Disconnected -> stringResource(R.string.status_disconnected)
                        is OBDConnectionState.Error -> (connectionState as OBDConnectionState.Error).message
                    },
                    fontSize = 12.sp,
                    color = when (connectionState) {
                        is OBDConnectionState.Connected -> gaugeGreen
                        is OBDConnectionState.Error -> gaugeRed
                        else -> textSecondary
                    }
                )
                if (connectionState is OBDConnectionState.Connected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    ConnectionQualityBadge(stats = connectionStats)
                }
                if (remoteServerRunning) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.Wifi,
                        contentDescription = null,
                        tint = gaugeGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.status_remote, remoteConnectedClients),
                        fontSize = 11.sp,
                        color = gaugeGreen
                    )
                }
            }
        }

        Row {
            IconButton(onClick = onToggleTripComputer) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = stringResource(R.string.trip_title), tint = textSecondary)
            }
            IconButton(onClick = onToggleSettings) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings), tint = textSecondary)
            }
            IconButton(onClick = onToggleDevicePicker) {
                Icon(Icons.Filled.Bluetooth, contentDescription = stringResource(R.string.bluetooth), tint = canopoAccent)
            }
            if (connectionState is OBDConnectionState.Connected) {
                IconButton(onClick = onToggleRemoteDialog) {
                    Icon(
                        if (remoteServerRunning) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        contentDescription = stringResource(R.string.remote_server),
                        tint = if (remoteServerRunning) gaugeGreen else canopoAccent
                    )
                }
                IconButton(onClick = onToggleDataLog) {
                    Icon(
                        if (recordingActive) Icons.Filled.FiberManualRecord else Icons.Filled.Analytics,
                        contentDescription = stringResource(R.string.data_log),
                        tint = if (recordingActive) gaugeRed else canopoAccent
                    )
                }
                IconButton(onClick = onTogglePIDScreen) {
                    Icon(Icons.Filled.Sensors, contentDescription = stringResource(R.string.sensors), tint = canopoAccent)
                }
                IconButton(onClick = onToggleDTCDialog) {
                    Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.fault_codes), tint = gaugeYellow)
                }
                IconButton(onClick = onDisconnect) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.disconnect), tint = gaugeRed)
                }
            }
        }
    }
}

@Composable
private fun ConnectionQualityBadge(stats: ConnectionStats) {
    val color = when (stats.quality) {
        ConnectionQuality.EXCELLENT -> gaugeGreen
        ConnectionQuality.GOOD -> gaugeGreen.copy(alpha = 0.8f)
        ConnectionQuality.FAIR -> gaugeYellow
        ConnectionQuality.POOR -> gaugeRed
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stats.quality.label,
            fontSize = 10.sp,
            color = color
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
private fun DevicePickerDialog(
    devices: List<BluetoothDeviceInfo>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = canopoSurface,
        title = {
            Text(stringResource(R.string.choose_adapter), color = textPrimary)
        },
        text = {
            if (devices.isEmpty()) {
                Text(stringResource(R.string.no_paired_devices), color = textSecondary)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
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
                Text(stringResource(R.string.cancel), color = canopoAccent)
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
                Icons.Filled.Bluetooth,
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
