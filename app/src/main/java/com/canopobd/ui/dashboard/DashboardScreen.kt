package com.canopobd.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.canopobd.ui.components.CompactGauge
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
import com.canopobd.ui.readiness.ReadinessMonitorDialog
import com.canopobd.ui.diagnostics.DiagnosticsDialog
import com.canopobd.ui.alerts.AlertSettingsDialog
import com.canopobd.ui.analysis.DataAnalysisDialog
import com.canopobd.data.model.GPSTrip
import com.canopobd.data.local.TripEntity
import com.canopobd.data.model.OBDData
import com.canopobd.data.model.ActiveAlert
import com.canopobd.data.model.AlertConfig
import com.canopobd.data.model.FreezeFrame
import com.canopobd.data.model.FuelTrimAnalysis
import com.canopobd.data.model.ReadinessMonitor
import com.canopobd.data.model.CsvImportEntry
import com.canopobd.data.model.CarProfile
import com.canopobd.data.domain.OilHealthPredictor
import com.canopobd.data.domain.ValidationResult
import com.canopobd.data.model.TurboData
import com.canopobd.data.model.OilData
import com.canopobd.data.model.TimingChainState
import com.canopobd.ui.carprofile.CarProfileDialog
import com.canopobd.ui.turbo.TurboMonitorDialog
import com.canopobd.ui.turbo.ExtendedTurboMonitorDialog
import com.canopobd.ui.turbo.ExtendedTurboData
import com.canopobd.ui.timingchain.TimingChainMonitorDialog
import com.canopobd.ui.gearbox.ExtendedGearboxDialog
import com.canopobd.ui.gearbox.GearboxTelemetry
import com.canopobd.ui.fuel.ExtendedFuelEconomyDialog
import com.canopobd.ui.maintenance.ExtendedMaintenanceDialog
import com.canopobd.ui.comfort.ComfortControlDialog
import com.canopobd.ui.comfort.ComfortCommand
import com.canopobd.ui.turbo.TurboCoolDownBanner
import com.canopobd.ui.turbo.TurboCoolDownDialog
import com.canopobd.data.model.TurboCoolDownState
import com.canopobd.ui.vehicleinfo.VehicleInfoDialog
import com.canopobd.ui.knownissues.KnownIssuesDialog
import com.canopobd.ui.dashboard.OilHealthCard
import com.canopobd.ui.dashboard.SensorValidationCard
import com.canopobd.ui.coding.AstraJCodingDialog
import com.canopobd.data.model.AstraJCodingModels
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
    showReadiness: Boolean,
    showDiagnostics: Boolean,
    showAlertSettings: Boolean,
    showDataAnalysis: Boolean,
    showFuelEconomy: Boolean,
    showMaintenance: Boolean,
    showPerformanceTest: Boolean,
    showTripHistory: Boolean,
    showPowerCalculator: Boolean,
    showDriveScore: Boolean,
    showShiftLight: Boolean,
    showVehicleInfo: Boolean,
    showKnownIssues: Boolean,
    carProfile: CarProfile,
    turboData: TurboData,
    oilData: OilData,
    timingChainState: TimingChainState,
    showTurboMonitor: Boolean,
    showTimingChainMonitor: Boolean,
    showCarProfile: Boolean,
    _showTurboCooldown: Boolean,
    turboCooldownState: TurboCoolDownState,
    maintenanceItems: List<com.canopobd.data.model.MaintenanceItem>,
    currentKm: Int,
    fuelEconomyData: com.canopobd.data.model.FuelEconomyData,
    performanceTestState: com.canopobd.data.model.PerformanceTestState,
    powerCalculation: com.canopobd.data.model.PowerCalculation,
    driveScore: com.canopobd.data.model.DriveScore,
    driveSession: com.canopobd.data.model.DriveSession,
    shiftLightConfig: com.canopobd.data.model.ShiftLightConfig,
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
    tripHistory: List<GPSTrip>,
    readinessMonitor: ReadinessMonitor,
    detectedProtocol: String,
    supportedPIDs: List<String>,
    freezeFrames: List<FreezeFrame>,
    alertConfig: AlertConfig,
    activeAlerts: List<ActiveAlert>,
    importedData: List<CsvImportEntry>,
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
    onToggleReadiness: () -> Unit,
    onToggleDiagnostics: () -> Unit,
    onToggleAlertSettings: () -> Unit,
    onToggleDataAnalysis: () -> Unit,
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
    onSetAlertConfig: (AlertConfig) -> Unit,
    onImportCsv: (String) -> Unit,
    onClearImported: () -> Unit,
    onGetFuelTrimAnalysis: () -> FuelTrimAnalysis,
    onToggleFuelEconomy: () -> Unit,
    onToggleMaintenance: () -> Unit,
    onTogglePerformanceTest: () -> Unit,
    onToggleTripHistory: () -> Unit,
    onSetMaintenanceItem: (com.canopobd.data.model.MaintenanceType, Int, Int) -> Unit,
    onResetMaintenanceItem: (com.canopobd.data.model.MaintenanceType) -> Unit,
    onStartPerfTest: (com.canopobd.data.model.PerformanceTestType) -> Unit,
    onStopPerfTest: () -> Unit,
    onClearTripHistory: () -> Unit,
    tripHistoryEntities: List<TripEntity>,
    onDeleteTrip: (Long) -> Unit,
    onShareTripCsv: () -> Unit,
    onTogglePowerCalculator: () -> Unit,
    onToggleDriveScore: () -> Unit,
    onToggleShiftLight: () -> Unit,
    onToggleVehicleInfo: () -> Unit,
    onToggleKnownIssues: () -> Unit,
    onUpdateShiftLightConfig: (com.canopobd.data.model.ShiftLightConfig) -> Unit,
    onResetDriveScore: () -> Unit,
    oilHealthPrediction: OilHealthPredictor.OilHealthPredictionResult,
    sensorValidationResult: ValidationResult,
    onToggleTurboMonitor: () -> Unit,
    onToggleTimingChainMonitor: () -> Unit,
    _onToggleCarProfile: () -> Unit,
    onToggleTurboCooldown: () -> Unit,
    onSelectCarProfile: (CarProfile) -> Unit,
    showExtendedGearbox: Boolean,
    showExtendedTurbo: Boolean,
    showExtendedFuel: Boolean,
    showExtendedMaintenance: Boolean,
    showComfortControl: Boolean,
    onToggleExtendedGearbox: () -> Unit,
    onToggleExtendedTurbo: () -> Unit,
    onToggleExtendedFuel: () -> Unit,
    onToggleExtendedMaintenance: () -> Unit,
    onToggleComfortControl: () -> Unit,
    onSendBCMCommand: (ComfortCommand) -> Unit,
    showCodingDialog: Boolean,
    codingInProgress: Boolean,
    codingResult: com.canopobd.data.model.AstraJCodingModels.CodingResult?,
    onToggleCodingDialog: () -> Unit,
    onApplyCodingOption: (com.canopobd.data.model.AstraJCodingModels.CodingOption, com.canopobd.data.model.AstraJCodingModels.CodingValue) -> Unit,
    onClearCodingResult: () -> Unit,
    appThemeMode: com.canopobd.data.model.AppThemeMode,
    onSetAppThemeMode: (com.canopobd.data.model.AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

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

    val isCoolantCritical = obdData.coolantTemp > 105
    val isRpmCritical = obdData.rpm > 6000

    val backgroundColor = when {
        isCoolantCritical -> colors.gaugeRed.copy(alpha = 0.15f)
        isRpmCritical -> colors.gaugeOrange.copy(alpha = 0.1f)
        else -> colors.dark
    }

    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
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
                    navController = navController,
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
                    onToggleReadiness = onToggleReadiness,
                    onToggleDiagnostics = onToggleDiagnostics,
                    onToggleAlertSettings = onToggleAlertSettings,
                    onToggleDataAnalysis = onToggleDataAnalysis,
                    onToggleFuelEconomy = onToggleFuelEconomy,
                    onToggleMaintenance = onToggleMaintenance,
                    onTogglePerformanceTest = onTogglePerformanceTest,
                    onToggleTripHistory = onToggleTripHistory,
                    onTogglePowerCalculator = onTogglePowerCalculator,
                    onToggleDriveScore = onToggleDriveScore,
                    onToggleShiftLight = onToggleShiftLight,
                    onToggleVehicleInfo = onToggleVehicleInfo,
                    onToggleKnownIssues = onToggleKnownIssues,
                    onToggleTurboMonitor = onToggleTurboMonitor,
                    onToggleTimingChainMonitor = onToggleTimingChainMonitor,
                    _onToggleCarProfile = _onToggleCarProfile,
                    onToggleTurboCooldown = onToggleTurboCooldown,
                    onToggleComfortControl = onToggleComfortControl,
                    onToggleCodingDialog = onToggleCodingDialog,
                    onDisconnect = onDisconnect,
                    recordingActive = recordingActive,
                    isGPSTracking = isGPSTracking,
                    activeAlerts = activeAlerts,
                    onStartGPSTrack = onStartGPSTracking,
                    onStopGPSTrack = onStopGPSTracking
                )

                if (activeAlerts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AlertBanner(alerts = activeAlerts, colors = colors)
                }

                if (turboCooldownState.isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TurboCoolDownBanner(
                        coolDownState = turboCooldownState,
                        onDismiss = onToggleTurboCooldown
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OilHealthCard(
                        prediction = oilHealthPrediction,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    SensorValidationCard(
                        validationResult = sensorValidationResult,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }

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
                        color = when {
                            obdData.batteryVoltage > 0 && obdData.batteryVoltage < 11.5f -> colors.gaugeRed
                            obdData.batteryVoltage > 0 && obdData.batteryVoltage < 12f -> colors.gaugeOrange
                            else -> colors.textSecondary
                        }
                    )
                    Text(
                        text = "${pollMode.label} | Load: %.0f%%".format(obdData.absoluteLoadValue),
                        fontSize = 12.sp,
                        color = when {
                            obdData.absoluteLoadValue > 90 -> colors.gaugeOrange
                            obdData.absoluteLoadValue > 80 -> colors.gaugeYellow
                            else -> colors.textSecondary
                        }
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

                if (connectionStats.quality == ConnectionQuality.POOR) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeRed.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.conn_quality_poor_message),
                            fontSize = 10.sp,
                            color = colors.gaugeRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        fun NavHostController.safePop() {
            try { popBackStack() } catch (_: Exception) {}
        }

        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {}
            composable("device_picker") {
                if (showDevicePicker) {
                    DevicePickerDialog(devices = devices, onSelect = onConnect, onDismiss = { onToggleDevicePicker(); navController.safePop() })
                }
            }
            composable("dtc") {
                if (showDTCDialog) {
                    DTCDialog(dtcResponse = dtcResponse, onDismiss = { onToggleDTCDialog(); navController.safePop() }, onClearDTCs = onClearDTCs)
                }
            }
            composable("settings") {
                if (showSettings) {
                    SettingsDialog(pollRate = pollRate, measurementUnit = measurementUnit, autoReconnect = autoReconnect, pollMode = pollMode, appThemeMode = appThemeMode, onDismiss = { onToggleSettings(); navController.safePop() }, onPollRateChange = onSetPollRate, onUnitChange = onSetMeasurementUnit, onAutoReconnectChange = onSetAutoReconnect, onPollModeChange = onSetPollMode, onSetAppThemeMode = onSetAppThemeMode)
                }
            }
            composable("data_log") {
                if (showDataLog) {
                    DataLogDialog(recordedData = recordedData, isRecording = recordingActive, onDismiss = { onToggleDataLog(); navController.safePop() }, onStartRecording = onStartRecording, onStopRecording = onStopRecording, onClearData = onClearRecordedData, onExportData = onGetExportData)
                }
            }
            composable("pids") {
                if (showPIDScreen) {
                    PIDDialog(obdData = obdData, measurementUnit = measurementUnit, onDismiss = { onTogglePIDScreen(); navController.safePop() })
                }
            }
            composable("remote_server") {
                if (showRemoteDialog) {
                    RemoteServerDialog(isRunning = remoteServerRunning, serverIp = remoteServerIp, serverPort = remoteServerPort, connectedClients = remoteConnectedClients, onDismiss = { onToggleRemoteDialog(); navController.safePop() }, onStartServer = onStartRemoteServer, onStopServer = onStopRemoteServer)
                }
            }
            composable("trip_computer") {
                if (showTripComputer) {
                    TripComputerDialog(tripData = tripData, measurementUnit = measurementUnit, vin = onGetStoredVin(), isGPSTracking = isGPSTracking, currentTrip = currentTrip, onDismiss = { onToggleTripComputer(); navController.safePop() }, onResetTrip = onResetTrip, onStartGPSTrack = onStartGPSTracking, onStopGPSTrack = onStopGPSTracking, onExportGPX = onExportGPX, onExportKML = onExportKML, onClearGPS = onClearGPSTrips)
                }
            }
            composable("customization") {
                if (showCustomization) {
                    DashboardCustomizationDialog(currentTheme = colorTheme, primaryGaugeIds = primaryGaugeIds, onDismiss = { onToggleCustomization(); navController.safePop() }, onThemeChange = onSetColorTheme, onPrimaryGaugesChange = onSetPrimaryGauges)
                }
            }
            composable("hud") {
                if (showHUDMode) {
                    HUDModeActivity(obdData = obdData, measurementUnit = measurementUnit, onDismiss = { onToggleHUDMode(); navController.safePop() })
                }
            }
            composable("trend_graph") {
                if (showTrendGraph) {
                    LiveTrendGraphDialog(trendHistory = trendHistory, onDismiss = { onToggleTrendGraph(); navController.safePop() })
                }
            }
            composable("readiness") {
                if (showReadiness) {
                    ReadinessMonitorDialog(readiness = readinessMonitor, onDismiss = { onToggleReadiness(); navController.safePop() })
                }
            }
            composable("diagnostics") {
                if (showDiagnostics) {
                    DiagnosticsDialog(protocol = detectedProtocol, supportedPIDs = supportedPIDs, freezeFrames = freezeFrames, onDismiss = { onToggleDiagnostics(); navController.safePop() })
                }
            }
            composable("alert_settings") {
                if (showAlertSettings) {
                    AlertSettingsDialog(alertConfig = alertConfig, activeAlerts = activeAlerts, onDismiss = { onToggleAlertSettings(); navController.safePop() }, onUpdateConfig = onSetAlertConfig)
                }
            }
            composable("data_analysis") {
                if (showDataAnalysis) {
                    DataAnalysisDialog(importedData = importedData, fuelTrimAnalysis = onGetFuelTrimAnalysis(), onDismiss = { onToggleDataAnalysis(); navController.safePop() }, onImportCsv = onImportCsv, onClearImported = onClearImported)
                }
            }
            composable("fuel_economy") {
                if (showFuelEconomy) {
                    com.canopobd.ui.fuel.FuelEconomyDialog(fuelEconomyData = fuelEconomyData, onDismiss = { onToggleFuelEconomy(); navController.safePop() })
                }
            }
            composable("maintenance") {
                if (showMaintenance) {
                    com.canopobd.ui.maintenance.MaintenanceDialog(maintenanceItems = maintenanceItems, currentKm = currentKm, onDismiss = { onToggleMaintenance(); navController.safePop() }, onUpdateItem = onSetMaintenanceItem, onResetItem = onResetMaintenanceItem)
                }
            }
            composable("performance_test") {
                if (showPerformanceTest) {
                    com.canopobd.ui.performance.PerformanceTestDialog(testState = performanceTestState, onDismiss = { onTogglePerformanceTest(); navController.safePop() }, onStartTest = onStartPerfTest, onStopTest = onStopPerfTest)
                }
            }
            composable("trip_history") {
                if (showTripHistory) {
                    com.canopobd.ui.trip.TripHistoryScreen(
                        trips = tripHistoryEntities,
                        onBack = { onToggleTripHistory(); navController.safePop() },
                        onDeleteTrip = onDeleteTrip,
                        onClearAll = onClearTripHistory,
                        onShareCsv = onShareTripCsv
                    )
                }
            }
            composable("power_calculator") {
                if (showPowerCalculator) {
                    com.canopobd.ui.power.PowerCalculatorDialog(calculation = powerCalculation, rpm = obdData.rpm, maf = obdData.mafRate, onDismiss = { onTogglePowerCalculator(); navController.safePop() })
                }
            }
            composable("drive_score") {
                if (showDriveScore) {
                    com.canopobd.ui.drivescore.DriveScoreDialog(score = driveScore, sessionDuration = (System.currentTimeMillis() - driveSession.startTime) / 1000, harshAccels = driveSession.harshAccels, harshBrakes = driveSession.harshBrakes, idleTimeSeconds = driveSession.idleTimeSeconds, avgRpm = driveSession.avgRpm, avgThrottle = driveSession.avgThrottle, avgSpeed = driveSession.avgSpeed, onDismiss = { onToggleDriveScore(); navController.safePop() }, onResetScore = onResetDriveScore)
                }
            }
            composable("shift_light") {
                if (showShiftLight) {
                    com.canopobd.ui.shiftlight.ShiftLightDialog(config = shiftLightConfig, currentRpm = obdData.rpm, onDismiss = { onToggleShiftLight(); navController.safePop() }, onUpdateConfig = onUpdateShiftLightConfig)
                }
            }
            composable("vehicle_info") {
                if (showVehicleInfo) {
                    VehicleInfoDialog(vin = onGetStoredVin(), onDismiss = { onToggleVehicleInfo(); navController.safePop() })
                }
            }
            composable("known_issues") {
                if (showKnownIssues) {
                    KnownIssuesDialog(onDismiss = { onToggleKnownIssues(); navController.safePop() })
                }
            }
            composable("turbo_monitor") {
                if (showTurboMonitor) {
                    TurboMonitorDialog(turboData = turboData, oilData = oilData, carProfile = carProfile, onDismiss = { onToggleTurboMonitor(); navController.safePop() })
                }
            }
            composable("timing_chain_monitor") {
                if (showTimingChainMonitor) {
                    TimingChainMonitorDialog(chainState = timingChainState, carProfile = carProfile, onDismiss = { onToggleTimingChainMonitor(); navController.safePop() })
                }
            }
            composable("turbo_cooldown") {
                if (_showTurboCooldown) {
                    TurboCoolDownDialog(
                        coolDownState = turboCooldownState,
                        onDismiss = { onToggleTurboCooldown(); navController.safePop() }
                    )
                }
            }
            composable("extended_car_profile") {
                if (showCarProfile) {
                    CarProfileDialog(currentProfile = carProfile, onSelectProfile = onSelectCarProfile, onDismiss = { _onToggleCarProfile(); navController.safePop() })
                }
            }
            composable("extended_gearbox") {
                if (showExtendedGearbox) {
                    ExtendedGearboxDialog(
                        telemetry = com.canopobd.ui.gearbox.GearboxTelemetry(
                            engineRpm = obdData.rpm,
                            vehicleSpeedKmh = obdData.speed,
                            oilTempCelsius = obdData.oilTemp,
                            engineLoad = obdData.engineLoad
                        ),
                        onDismiss = { onToggleExtendedGearbox(); navController.safePop() }
                    )
                }
            }
            composable("extended_turbo") {
                if (showExtendedTurbo) {
                    ExtendedTurboMonitorDialog(
                        extendedData = com.canopobd.ui.turbo.ExtendedTurboData(
                            boostActualBar = obdData.boostPressure / 100.0,
                            boostTargetBar = obdData.boostPressureTargetMode22 / 100.0,
                            wastegatePosition = obdData.wastegatePositionMode22,
                            wastegateDutyCycle = obdData.wastegateControl,
                            turboRpm = obdData.turboRpmMode22,
                            chargeAirTemp = obdData.chargeAirCoolerTemp,
                            intakeAirTemp = obdData.intakeTemp,
                            egtCurrent = obdData.egtBank1,
                            egtPeak = obdData.egtBank1,
                            engineLoad = obdData.engineLoad,
                            engineRpm = obdData.rpm
                        ),
                        coolDownState = turboCooldownState,
                        onDismiss = { onToggleExtendedTurbo(); navController.safePop() }
                    )
                }
            }
            composable("extended_fuel") {
                if (showExtendedFuel) {
                    ExtendedFuelEconomyDialog(
                        fuelEconomyData = fuelEconomyData,
                        fuelLevelPercent = obdData.fuelLevel,
                        maf = obdData.mafRate,
                        speed = obdData.speed,
                        onDismiss = { onToggleExtendedFuel(); navController.safePop() }
                    )
                }
            }
            composable("extended_maintenance") {
                if (showExtendedMaintenance) {
                    ExtendedMaintenanceDialog(
                        currentKm = currentKm,
                        onDismiss = { onToggleExtendedMaintenance(); navController.safePop() },
                        onCompleteService = { type, km, interval -> onSetMaintenanceItem(com.canopobd.data.model.MaintenanceType.valueOf(type), km, interval) }
                    )
                }
            }
            composable("comfort_control") {
                if (showComfortControl) {
                    ComfortControlDialog(
                        onCommand = onSendBCMCommand,
                        onDismiss = { onToggleComfortControl(); navController.safePop() }
                    )
                }
            }
            composable("astra_j_coding") {
                if (showCodingDialog) {
                    AstraJCodingDialog(
                        codingResult = codingResult,
                        codingInProgress = codingInProgress,
                        onDismiss = { onToggleCodingDialog(); navController.safePop() },
                        onApplyOption = onApplyCodingOption,
                        onClearResult = onClearCodingResult
                    )
                }
            }
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
    "rpm" to GaugeItem("rpm", "RPM", data.rpm.toFloat(), "rpm", 0f, 8000f,
        when {
            data.rpm > 6000 -> Color(0xFFFF4444)
            data.rpm > 5500 -> Color(0xFFFF8800)
            else -> Color(0xFF44FF88)
        }),
    "speed" to GaugeItem("speed", "Speed", unit.convertSpeed(data.speed).toFloat(), unit.speedUnit, 0f, 260f, Color(0xFF42A5F5)),
    "coolant" to GaugeItem("coolant", "Coolant", unit.convertTemp(data.coolantTemp).toFloat(), unit.tempUnit, -40f, 215f,
        when {
            data.coolantTemp > 110 -> Color(0xFFFF4444)
            data.coolantTemp > 100 -> Color(0xFFFF8800)
            data.coolantTemp > 80 -> Color(0xFF44FF88)
            data.coolantTemp < 40 -> Color(0xFF42A5F5)
            else -> Color(0xFFFF8C00)
        }),
    "throttle" to GaugeItem("throttle", "Throttle", data.throttle.toFloat(), "%", 0f, 100f, Color(0xFFFFE066)),
    "engine_load" to GaugeItem("engine_load", "Eng Load", data.engineLoad.toFloat(), "%", 0f, 100f, Color(0xFFFFD54F)),
    "fuel" to GaugeItem("fuel", "Fuel", data.fuelLevel.toFloat(), "%", 0f, 100f, Color(0xFFFF9800)),
    "timing" to GaugeItem("timing", "Timing", data.timingAdvance.toFloat(), "°", -64f, 64f, Color(0xFF00BCD4)),
    "maf" to GaugeItem("maf", "MAF", data.mafRate.toFloat(), "g/s", 0f, 500f, Color(0xFF9C27B0)),
    "intake_temp" to GaugeItem("intake_temp", "Intake", unit.convertTemp(data.intakeTemp).toFloat(), unit.tempUnit, -40f, 215f, Color(0xFFFF5722)),
    "fuel_trim" to GaugeItem("fuel_trim", "Fuel Trim", abs(data.shortTermFuelTrimB1 + data.longTermFuelTrimB1).toFloat(), "%", 0f, 50f, Color(0xFFFFAB40), isPercentage = true),
    "load" to GaugeItem("load", "Abs Load", data.absoluteLoadValue.toFloat(), "%", 0f, 100f, Color(0xFF69F0AE)),
    "fuel_rate" to GaugeItem("fuel_rate", "Fuel Rate", data.engineFuelRate.toFloat(), "L/h", 0f, 50f, Color(0xFFFF5252)),
    "accel_pedal" to GaugeItem("accel_pedal", "Accel Pedal", data.acceleratorPosD.toFloat(), "%", 0f, 100f, Color(0xFF00E5FF)),
    "hybrid_battery" to GaugeItem("hybrid_battery", "Hybrid Batt", data.hybridBatteryRemaining.toFloat(), "%", 0f, 100f,
        when {
            data.hybridBatteryRemaining < 20 -> Color(0xFFFF4444)
            data.hybridBatteryRemaining < 40 -> Color(0xFFFF8800)
            else -> Color(0xFF69F0AE)
        })
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
                    CompactGauge(
                        value = gauge.value.toDouble(),
                        label = gauge.label,
                        unit = gauge.unit,
                        max = gauge.maxValue.toDouble(),
                        color = gauge.color,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
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
    navController: NavHostController,
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
    onToggleReadiness: () -> Unit,
    onToggleDiagnostics: () -> Unit,
    onToggleAlertSettings: () -> Unit,
    onToggleDataAnalysis: () -> Unit,
    onToggleFuelEconomy: () -> Unit,
    onToggleMaintenance: () -> Unit,
    onTogglePerformanceTest: () -> Unit,
    onToggleTripHistory: () -> Unit,
    onTogglePowerCalculator: () -> Unit,
    onToggleDriveScore: () -> Unit,
    onToggleShiftLight: () -> Unit,
    onToggleVehicleInfo: () -> Unit,
    onToggleKnownIssues: () -> Unit,
    onToggleTurboMonitor: () -> Unit,
    onToggleTimingChainMonitor: () -> Unit,
    _onToggleCarProfile: () -> Unit,
    onToggleTurboCooldown: () -> Unit,
    onToggleComfortControl: () -> Unit,
    onToggleCodingDialog: () -> Unit,
    onDisconnect: () -> Unit,
    recordingActive: Boolean,
    isGPSTracking: Boolean,
    activeAlerts: List<ActiveAlert>,
    onStartGPSTrack: () -> Unit,
    onStopGPSTrack: () -> Unit
) {
    val colors = LocalAppColors.current
    val connectionColor = when (connectionState) {
        is OBDConnectionState.Connected -> colors.gaugeGreen
        is OBDConnectionState.Connecting -> colors.gaugeYellow
        is OBDConnectionState.Error -> colors.gaugeRed
        else -> colors.textSecondary
    }
    val connectionText = when (connectionState) {
        is OBDConnectionState.Connected -> stringResource(R.string.status_connected)
        is OBDConnectionState.Connecting -> stringResource(R.string.status_connecting)
        is OBDConnectionState.Disconnected -> stringResource(R.string.status_disconnected)
        is OBDConnectionState.Error -> (connectionState as OBDConnectionState.Error).message
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(connectionColor, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = connectionText,
                        fontSize = 12.sp,
                        color = connectionColor,
                        fontWeight = FontWeight.Medium
                    )
                    if (connectionState is OBDConnectionState.Connected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        ConnectionQualityBadge(stats = connectionStats, colors = colors)
                        if (remoteServerRunning) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Filled.Wifi, contentDescription = null, tint = colors.gaugeGreen, modifier = Modifier.size(14.dp))
                            Text(stringResource(R.string.status_remote, remoteConnectedClients), fontSize = 11.sp, color = colors.gaugeGreen)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionButton(
                    icon = Icons.Filled.DirectionsCar,
                    label = stringResource(R.string.trip_title),
                    color = colors.textSecondary,
                    onClick = { onToggleTripComputer(); navController.navigate("trip_computer") }
                )
                QuickActionButton(
                    icon = Icons.Filled.LocalGasStation,
                    label = stringResource(R.string.fuel_economy_title),
                    color = colors.textSecondary,
                    onClick = { onToggleFuelEconomy(); navController.navigate("fuel_economy") }
                )
                QuickActionButton(
                    icon = Icons.Filled.Build,
                    label = stringResource(R.string.maintenance_title),
                    color = colors.textSecondary,
                    onClick = { onToggleMaintenance(); navController.navigate("maintenance") }
                )
                QuickActionButton(
                    icon = Icons.Filled.Speed,
                    label = stringResource(R.string.perf_test_title),
                    color = colors.textSecondary,
                    onClick = { onTogglePerformanceTest(); navController.navigate("performance_test") }
                )
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    label = stringResource(R.string.trend),
                    color = colors.textSecondary,
                    onClick = { onToggleTrendGraph(); navController.navigate("trend_graph") }
                )
                QuickActionButton(
                    icon = Icons.Filled.Route,
                    label = stringResource(R.string.trip_history_title),
                    color = colors.textSecondary,
                    onClick = { onToggleTripHistory(); navController.navigate("trip_history") }
                )
                QuickActionButton(
                    icon = if (isGPSTracking) Icons.Filled.LocationOn else Icons.Filled.LocationSearching,
                    label = stringResource(R.string.gps_track),
                    color = if (isGPSTracking) colors.gaugeGreen else colors.textSecondary,
                    onClick = { if (isGPSTracking) onStopGPSTrack() else onStartGPSTrack() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionButton(
                    icon = Icons.Filled.Dashboard,
                    label = stringResource(R.string.customize_dashboard),
                    color = colors.accent,
                    onClick = { onToggleCustomization(); navController.navigate("customization") }
                )
                QuickActionButton(
                    icon = Icons.Filled.Settings,
                    label = stringResource(R.string.settings),
                    color = colors.textSecondary,
                    onClick = { onToggleSettings(); navController.navigate("settings") }
                )
                QuickActionButton(
                    icon = Icons.Filled.Bluetooth,
                    label = stringResource(R.string.bluetooth),
                    color = colors.accent,
                    onClick = { onToggleDevicePicker(); navController.navigate("device_picker") }
                )
                if (connectionState is OBDConnectionState.Connected) {
                    QuickActionButton(
                        icon = Icons.Filled.ElectricBolt,
                        label = stringResource(R.string.dashboard_power),
                        color = colors.gaugeYellow,
                        onClick = { onTogglePowerCalculator(); navController.navigate("power_calculator") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.SportsScore,
                        label = stringResource(R.string.dashboard_driving_style),
                        color = colors.gaugeGreen,
                        onClick = { onToggleDriveScore(); navController.navigate("drive_score") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.LightMode,
                        label = stringResource(R.string.dashboard_shift_light),
                        color = colors.gaugeOrange,
                        onClick = { onToggleShiftLight(); navController.navigate("shift_light") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Info,
                        label = stringResource(R.string.dashboard_vehicle_profile),
                        color = colors.highlight,
                        onClick = { _onToggleCarProfile(); navController.navigate("car_profile") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.BugReport,
                        label = stringResource(R.string.dashboard_known_issues),
                        color = colors.gaugeYellow,
                        onClick = onToggleKnownIssues
                    )
                    QuickActionButton(
                        icon = Icons.Filled.SettingsRemote,
                        label = "Komfort",
                        color = colors.gaugeCyan,
                        onClick = { onToggleComfortControl(); navController.navigate("comfort_control") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Code,
                        label = "Codierung",
                        color = colors.gaugeOrange,
                        onClick = { onToggleCodingDialog(); navController.navigate("astra_j_coding") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Air,
                        label = stringResource(R.string.dashboard_turbo),
                        color = colors.accent,
                        onClick = onToggleTurboMonitor
                    )
                    QuickActionButton(
                        icon = Icons.Filled.SettingsApplications,
                        label = stringResource(R.string.dashboard_timing_chain),
                        color = colors.accent,
                        onClick = { onToggleTimingChainMonitor(); navController.navigate("timing_chain_monitor") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Timer,
                        label = stringResource(R.string.dashboard_turbo_cooldown),
                        color = colors.gaugeCyan,
                        onClick = { onToggleTurboCooldown(); navController.navigate("turbo_cooldown") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Tv,
                        label = stringResource(R.string.hud_mode),
                        color = colors.gaugeCyan,
                        onClick = { onToggleHUDMode(); navController.navigate("hud") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Verified,
                        label = stringResource(R.string.readiness),
                        color = colors.gaugeGreen,
                        onClick = { onToggleReadiness(); navController.navigate("readiness") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Biotech,
                        label = stringResource(R.string.diagnostics),
                        color = colors.accent,
                        onClick = { onToggleDiagnostics(); navController.navigate("diagnostics") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Analytics,
                        label = stringResource(R.string.analysis),
                        color = colors.accent,
                        onClick = { onToggleDataAnalysis(); navController.navigate("data_analysis") }
                    )
                    QuickActionButton(
                        icon = if (activeAlerts.isNotEmpty()) Icons.Filled.NotificationImportant else Icons.Filled.Notifications,
                        label = stringResource(R.string.alerts),
                        color = if (activeAlerts.isNotEmpty()) colors.gaugeRed else colors.textSecondary,
                        onClick = { onToggleAlertSettings(); navController.navigate("alerts") }
                    )
                    QuickActionButton(
                        icon = if (remoteServerRunning) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        label = stringResource(R.string.remote_server),
                        color = if (remoteServerRunning) colors.gaugeGreen else colors.accent,
                        onClick = { onToggleRemoteDialog(); navController.navigate("remote_server") }
                    )
                    QuickActionButton(
                        icon = if (recordingActive) Icons.Filled.FiberManualRecord else Icons.Filled.Analytics,
                        label = stringResource(R.string.data_log),
                        color = if (recordingActive) colors.gaugeRed else colors.accent,
                        onClick = { onToggleDataLog(); navController.navigate("data_log") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Sensors,
                        label = stringResource(R.string.sensors),
                        color = colors.accent,
                        onClick = { onTogglePIDScreen(); navController.navigate("pids") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Warning,
                        label = stringResource(R.string.fault_codes),
                        color = colors.gaugeYellow,
                        onClick = { onToggleDTCDialog(); navController.navigate("dtc") }
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Close,
                        label = stringResource(R.string.disconnect),
                        color = colors.gaugeRed,
                        onClick = onDisconnect
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
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
private fun AlertBanner(alerts: List<ActiveAlert>, colors: AppColors) {
    val alert = alerts.firstOrNull() ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.gaugeRed.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.NotificationImportant,
                contentDescription = stringResource(R.string.alert),
                tint = colors.gaugeRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (alerts.size > 1) "${alert.message} (+${alerts.size - 1})" else alert.message,
                fontSize = 11.sp,
                color = colors.gaugeRed
            )
        }
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
