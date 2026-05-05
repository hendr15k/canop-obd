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
import com.canopobd.data.model.*
import com.canopobd.ui.components.CircularGauge
import com.canopobd.ui.components.GaugeRow
import com.canopobd.ui.components.LiveTrendGraphDialog
import com.canopobd.ui.customization.DashboardCustomizationDialog
import com.canopobd.ui.datalog.DataLogDialog
import com.canopobd.ui.dtc.DTCDialog
import com.canopobd.ui.pid.PIDDialog
import com.canopobd.ui.remote.RemoteServerDialog
import com.canopobd.ui.settings.SettingsDialog
import com.canopobd.ui.theme.*
import com.canopobd.ui.tripcomputer.TripComputerDialog
import com.canopobd.ui.hud.HUDModeActivity
import com.canopobd.data.model.GPSTrip
import com.canopobd.data.model.OBDData
import kotlin.math.abs

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
    showCustomization: Boolean,
    showHUDMode: Boolean,
    showTrendGraph: Boolean,
    remoteServerRunning: Boolean,
    remoteServerIp: String,
    remoteServerPort: Int,
    remoteConnectedClients: Int,
    tripData: TripData,
    connectionStats: ConnectionStats,
    autoReconnect: Boolean,
    errorMessage: String?,
    colorTheme: ColorTheme,
    primaryGaugeIds: Set<String>,
    pollMode: PollMode,
    isGPSTracking: Boolean,
    currentTrip: GPSTrip?,
    trendHistory: TrendHistory,
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
    onToggleCustomization: () -> Unit,
    onToggleHUDMode: () -> Unit,
    onToggleTrendGraph: () -> Unit,
    onStartRemoteServer: (Int) -> Unit,
    onStopRemoteServer: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSetPollRate: (Long) -> Unit,
    onSetMeasurementUnit: (MeasurementUnit) -> Unit,
    onSetAutoReconnect: (Boolean) -> Unit,
    onSetPollMode: (PollMode) -> Unit,
    onResetTrip: () -> Unit,
    onGetStoredVin: () -> String,
    onGetExportData: () -> String,
    onClearRecordedData: () -> Unit,
    onSetColorTheme: (ColorTheme) -> Unit,
    onSetPrimaryGauges: (Set<String>) -> Unit,
    onStartGPSTracking: () -> Unit,
    onStopGPSTracking: () -> Unit,
    onExportGPX: () -> String,
    onExportKML: () -> String,
    onClearGPSTrips: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
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

    val gaugeMap = remember(obdData) { buildGaugeMap(obdData, measurementUnit) }

    Box(modifier = modifier.fillMaxSize().background(colors.dark)) {
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
                    onToggleTrendGraph = onToggleTrendGraph,
                    onToggleSettings = onToggleSettings,
                    onToggleCustomization = onToggleCustomization,
                    onToggleHUDMode = onToggleHUDMode,
                    onToggleDevicePicker = onToggleDevicePicker,
                    onToggleRemoteDialog = onToggleRemoteDialog,
                    onToggleDataLog = onToggleDataLog,
                    onTogglePIDScreen = onTogglePIDScreen,
                    onToggleDTCDialog = onToggleDTCDialog,
                    onDisconnect = onDisconnect,
                    recordingActive = recordingActive,
                    isGPSTracking = isGPSTracking,
                    onStartGPSTrack = onStartGPSTracking,
                    onStopGPSTrack = onStopGPSTracking
                )

                Spacer(modifier = Modifier.height(24.dp))

                val primaryIds = primaryGaugeIds.toList()
                val primaryGaugeData = primaryIds.mapNotNull { id -> gaugeMap[id] }.take(3)

                PrimaryGaugeRow(
                    gauges = primaryGaugeData,
                    totalSlots = 3,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(20.dp))

                SecondaryGaugeGrid(
                    gaugeMap = gaugeMap,
                    primaryIds = primaryGaugeIds
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Batt: %.1fV".format(obdData.batteryVoltage),
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "${pollMode.label} | Load: %.0f%%".format(obdData.absoluteLoadValue),
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = if (connectionStats.quality != ConnectionQuality.UNKNOWN) connectionStats.quality.label else "",
                        fontSize = 11.sp,
                        color = when (connectionStats.quality) {
                            ConnectionQuality.EXCELLENT, ConnectionQuality.GOOD -> colors.gaugeGreen
                            ConnectionQuality.FAIR -> colors.gaugeYellow
                            ConnectionQuality.POOR -> colors.gaugeRed
                            else -> colors.textSecondary
                        }
                    )
                }

                if (isGPSTracking && currentTrip != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GPS: %.1f km tracked".format(currentTrip.distanceKm),
                        fontSize = 11.sp,
                        color = colors.gaugeGreen,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
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
                pollMode = pollMode,
                onDismiss = onToggleSettings,
                onPollRateChange = onSetPollRate,
                onUnitChange = onSetMeasurementUnit,
                onAutoReconnectChange = onSetAutoReconnect,
                onPollModeChange = onSetPollMode
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
                isGPSTracking = isGPSTracking,
                currentTrip = currentTrip,
                onDismiss = onToggleTripComputer,
                onResetTrip = onResetTrip,
                onStartGPSTrack = onStartGPSTracking,
                onStopGPSTrack = onStopGPSTracking,
                onExportGPX = onExportGPX,
                onExportKML = onExportKML,
                onClearGPS = onClearGPSTrips
            )
        }

        if (showCustomization) {
            DashboardCustomizationDialog(
                currentTheme = colorTheme,
                primaryGaugeIds = primaryGaugeIds,
                onDismiss = onToggleCustomization,
                onThemeChange = onSetColorTheme,
                onPrimaryGaugesChange = onSetPrimaryGauges
            )
        }

        if (showHUDMode) {
            HUDModeActivity(
                obdData = obdData,
                measurementUnit = measurementUnit,
                onDismiss = onToggleHUDMode
            )
        }

        if (showTrendGraph) {
            LiveTrendGraphDialog(
                trendHistory = trendHistory,
                onDismiss = onToggleTrendGraph
            )
        }
    }
}

data class GaugeItem(
    val id: String,
    val label: String,
    val value: Float,
    val unit: String,
    val minValue: Float,
    val maxValue: Float,
    val color: Color,
    val isPercentage: Boolean = false
)

private fun buildGaugeMap(data: OBDData, unit: MeasurementUnit): Map<String, GaugeItem> = mapOf(
    "rpm" to GaugeItem("rpm", "RPM", data.rpm.toFloat(), "rpm", 0f, 8000f, Color(0xFF44FF88)),
    "speed" to GaugeItem("speed", "Speed", unit.convertSpeed(data.speed).toFloat(), unit.speedUnit, 0f, 260f, Color(0xFF42A5F5)),
    "coolant" to GaugeItem("coolant", "Coolant", unit.convertTemp(data.coolantTemp).toFloat(), unit.tempUnit, -40f, 215f, Color(0xFFFF8C00)),
    "throttle" to GaugeItem("throttle", "Throttle", data.throttle.toFloat(), "%", 0f, 100f, Color(0xFFFFE066)),
    "engine_load" to GaugeItem("engine_load", "Eng Load", data.engineLoad.toFloat(), "%", 0f, 100f, Color(0xFFFFD54F)),
    "fuel" to GaugeItem("fuel", "Fuel", data.fuelLevel.toFloat(), "%", 0f, 100f, Color(0xFFFF9800)),
    "timing" to GaugeItem("timing", "Timing", data.timingAdvance.toFloat(), "°", -64f, 64f, Color(0xFF00BCD4)),
    "maf" to GaugeItem("maf", "MAF", data.mafRate.toFloat(), "g/s", 0f, 500f, Color(0xFF9C27B0)),
    "intake_temp" to GaugeItem("intake_temp", "Intake", unit.convertTemp(data.intakeTemp).toFloat(), unit.tempUnit, -40f, 215f, Color(0xFFFF5722)),
    "fuel_trim" to GaugeItem("fuel_trim", "Fuel Trim", abs(data.shortTermFuelTrimB1 + data.longTermFuelTrimB1).toFloat(), "%", 0f, 50f, Color(0xFFFFAB40), isPercentage = true),
    "load" to GaugeItem("load", "Abs Load", data.absoluteLoadValue.toFloat(), "%", 0f, 100f, Color(0xFF69F0AE)),
    "fuel_rate" to GaugeItem("fuel_rate", "Fuel Rate", data.engineFuelRate.toFloat(), "L/h", 0f, 50f, Color(0xFFFF5252))
)

@Composable
private fun PrimaryGaugeRow(
    gauges: List<GaugeItem>,
    totalSlots: Int,
    colors: AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(totalSlots) { slot ->
            val gauge = gauges.getOrNull(slot)
            if (gauge != null) {
                CircularGauge(
                    value = gauge.value,
                    minValue = gauge.minValue,
                    maxValue = gauge.maxValue,
                    label = gauge.label,
                    unit = gauge.unit,
                    accentColor = gauge.color,
                    size = 130.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(65.dp))
                        .background(colors.surface.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add gauge",
                        tint = colors.textDim,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SecondaryGaugeGrid(
    gaugeMap: Map<String, GaugeItem>,
    primaryIds: Set<String>
) {
    val secondary = gaugeMap.entries
        .filter { it.key !in primaryIds }
        .take(6)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        secondary.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { (_, gauge) ->
                    SecondaryGauge(
                        label = gauge.label,
                        value = gauge.value.toDouble(),
                        unit = gauge.unit,
                        max = gauge.maxValue.toDouble(),
                        color = gauge.color,
                        isPercentage = gauge.isPercentage
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
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
    onToggleTrendGraph: () -> Unit,
    onToggleSettings: () -> Unit,
    onToggleCustomization: () -> Unit,
    onToggleHUDMode: () -> Unit,
    onToggleDevicePicker: () -> Unit,
    onToggleRemoteDialog: () -> Unit,
    onToggleDataLog: () -> Unit,
    onTogglePIDScreen: () -> Unit,
    onToggleDTCDialog: () -> Unit,
    onDisconnect: () -> Unit,
    recordingActive: Boolean,
    isGPSTracking: Boolean,
    onStartGPSTrack: () -> Unit,
    onStopGPSTrack: () -> Unit
) {
    val colors = LocalAppColors.current
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
                color = colors.highlight
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
                        is OBDConnectionState.Connected -> colors.gaugeGreen
                        is OBDConnectionState.Error -> colors.gaugeRed
                        else -> colors.textSecondary
                    }
                )
                if (connectionState is OBDConnectionState.Connected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    ConnectionQualityBadge(stats = connectionStats, colors = colors)
                }
                if (remoteServerRunning) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.Wifi, contentDescription = null, tint = colors.gaugeGreen, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.status_remote, remoteConnectedClients), fontSize = 11.sp, color = colors.gaugeGreen)
                }
            }
        }

                Row {
            IconButton(onClick = onToggleTripComputer) {
                Icon(Icons.Filled.DirectionsCar, contentDescription = stringResource(R.string.trip_title), tint = colors.textSecondary)
            }
            IconButton(onClick = onToggleTrendGraph) {
                Icon(Icons.Filled.ShowChart, contentDescription = "Trend", tint = colors.textSecondary)
            }
            IconButton(onClick = if (isGPSTracking) onStopGPSTrack else onStartGPSTrack) {
                Icon(
                    if (isGPSTracking) Icons.Filled.LocationOn else Icons.Filled.LocationSearching,
                    contentDescription = "GPS Track",
                    tint = if (isGPSTracking) colors.gaugeGreen else colors.textSecondary
                )
            }
            IconButton(onClick = onToggleCustomization) {
                Icon(Icons.Filled.Dashboard, contentDescription = stringResource(R.string.customize_dashboard), tint = colors.accent)
            }
            IconButton(onClick = onToggleSettings) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings), tint = colors.textSecondary)
            }
            IconButton(onClick = onToggleDevicePicker) {
                Icon(Icons.Filled.Bluetooth, contentDescription = stringResource(R.string.bluetooth), tint = colors.accent)
            }
            if (connectionState is OBDConnectionState.Connected) {
                IconButton(onClick = onToggleHUDMode) {
                    Icon(Icons.Filled.Tv, contentDescription = "HUD Mode", tint = colors.gaugeCyan)
                }
                IconButton(onClick = onToggleRemoteDialog) {
                    Icon(
                        if (remoteServerRunning) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        contentDescription = stringResource(R.string.remote_server),
                        tint = if (remoteServerRunning) colors.gaugeGreen else colors.accent
                    )
                }
                IconButton(onClick = onToggleDataLog) {
                    Icon(
                        if (recordingActive) Icons.Filled.FiberManualRecord else Icons.Filled.Analytics,
                        contentDescription = stringResource(R.string.data_log),
                        tint = if (recordingActive) colors.gaugeRed else colors.accent
                    )
                }
                IconButton(onClick = onTogglePIDScreen) {
                    Icon(Icons.Filled.Sensors, contentDescription = stringResource(R.string.sensors), tint = colors.accent)
                }
                IconButton(onClick = onToggleDTCDialog) {
                    Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.fault_codes), tint = colors.gaugeYellow)
                }
                IconButton(onClick = onDisconnect) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.disconnect), tint = colors.gaugeRed)
                }
            }
        }
    }
}

@Composable
private fun ConnectionQualityBadge(stats: ConnectionStats, colors: AppColors) {
    val color = when (stats.quality) {
        ConnectionQuality.EXCELLENT -> colors.gaugeGreen
        ConnectionQuality.GOOD -> colors.gaugeGreen.copy(alpha = 0.8f)
        ConnectionQuality.FAIR -> colors.gaugeYellow
        ConnectionQuality.POOR -> colors.gaugeRed
        else -> colors.textSecondary
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stats.quality.label, fontSize = 10.sp, color = color)
    }
}

@Composable
private fun SecondaryGauge(
    label: String,
    value: Double,
    unit: String,
    max: Double,
    color: Color,
    isPercentage: Boolean = false
) {
    val colors = LocalAppColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(12.dp)
    ) {
        Text(
            text = "%.0f%s".format(
                if (isPercentage) abs(value).coerceIn(0.0, max) else value.coerceIn(0.0, max),
                unit
            ),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(text = label, fontSize = 10.sp, color = colors.textSecondary)
    }
}

@Composable
private fun DevicePickerDialog(
    devices: List<BluetoothDeviceInfo>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(stringResource(R.string.choose_adapter), color = colors.textPrimary)
        },
        text = {
            if (devices.isEmpty()) {
                Text(stringResource(R.string.no_paired_devices), color = colors.textSecondary)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(devices) { device ->
                        DeviceListItem(
                            name = device.name,
                            address = device.address,
                            onClick = { onSelect(device.address) },
                            colors = colors
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.accent)
            }
        }
    )
}

@Composable
private fun DeviceListItem(
    name: String,
    address: String,
    onClick: () -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = colors.accent, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(address, fontSize = 11.sp, color = colors.textSecondary)
            }
        }
    }
}
