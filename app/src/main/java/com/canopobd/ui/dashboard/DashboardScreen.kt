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
import com.canopobd.ui.dashboard.DriveStyleCard
import com.canopobd.ui.dashboard.EfficiencyCard
import com.canopobd.ui.dashboard.FuelSystemCard
import com.canopobd.ui.dashboard.SAIHealthCard
import com.canopobd.ui.dashboard.EmissionsReadinessCard
import com.canopobd.ui.dashboard.BatteryAnalyzerCard
import com.canopobd.ui.dashboard.EGRAnalyzerCard
import com.canopobd.ui.dashboard.EVAPAnalyzerCard
import com.canopobd.ui.dashboard.TurboAnalyzerCard
import com.canopobd.ui.coding.AstraJCodingDialog
import com.canopobd.data.model.AstraJCodingModels
import com.canopobd.ui.profile.QuickActionsDialog
import com.canopobd.ui.profile.VehicleProfileManagerDialog
import com.canopobd.ui.profile.SavedProfile
import com.canopobd.ui.tpms.TPMSDialog
import com.canopobd.ui.climate.ClimateControlDialog
import com.canopobd.ui.climate.ClimateCommand
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
    emulatorMode: Boolean,
    isGPSTracking: Boolean,
    currentTrip: GPSTrip?,
    trendHistory: TrendHistory,
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
    driveStyleResult: com.canopobd.data.domain.DriveStyleAnalyzer.DriveStyleAnalysis,
    drivingEfficiencyResult: com.canopobd.data.domain.DrivingEfficiencyScorer.EfficiencyScore,
    fuelSystemResult: com.canopobd.data.domain.FuelSystemAnalyzer.FuelSystemAnalysis,
    gearboxResult: com.canopobd.data.domain.M32GearboxMonitor.GearboxAnalysis?,
    chainTensionerResult: com.canopobd.data.domain.ChainTensionerAnalyzer.ChainTensionerAnalysis?,
    coolantResult: com.canopobd.data.domain.CoolantSystemHealth.CoolantAnalysis?,
    oilConditionResult: com.canopobd.data.domain.OilConditionMonitor.OilAnalysis?,
    pcvResult: com.canopobd.data.domain.PCVMonitor.PCVAnalysis?,
    lambdaResult: com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaBalance?,
    fuelConsumption: com.canopobd.data.domain.FuelConsumptionAnalyzer.FuelConsumptionData?,
    egtResult: com.canopobd.data.domain.EGTMonitor.EGTAnalysis?,
    sensorHealthSummary: com.canopobd.data.domain.SensorHealthMonitor.SensorHealthSummary?,
    turboSpoolResult: com.canopobd.data.domain.TurboSpoolAnalyzer.SpoolAnalysis?,
    turboEfficiencyResult: com.canopobd.data.domain.TurboEfficiencyAnalyzer.TurboEfficiencyAnalysis?,
    boostLeakResult: com.canopobd.data.domain.BoostLeakDetector.BoostLeakAnalysis?,
    wastegateResult: com.canopobd.data.domain.WastegateHealthAnalyzer.WastegateAnalysis?,
    batteryAnalysis: com.canopobd.data.domain.BatteryHealthAnalyzer.BatteryAnalysis?,
    egrAnalysis: com.canopobd.data.domain.EGRHealthAnalyzer.EGRAnalysis?,
    evapAnalysis: com.canopobd.data.domain.EVAPSystemAnalyzer.EVAPAnalysis?,
    saiAnalysis: com.canopobd.data.domain.SecondaryAirAnalyzer.SAIAnalysis?,
    emissionsReadiness: com.canopobd.data.domain.EmissionsReadinessAnalyzer.ReadinessAnalysis?,
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
    showQuickActions: Boolean,
    showVehicleProfileManager: Boolean,
    onToggleQuickActions: () -> Unit,
    onToggleVehicleProfileManager: () -> Unit,
    onExecuteQuickAction: (String) -> Unit,
    onLoadProfile: (SavedProfile) -> Unit,
    currentVehicleProfile: com.canopobd.data.model.VehicleProfile?,
    showCodingDialog: Boolean,
    codingInProgress: Boolean,
    codingResult: com.canopobd.data.model.AstraJCodingModels.CodingResult?,
    onToggleCodingDialog: () -> Unit,
    onApplyCodingOption: (com.canopobd.data.model.AstraJCodingModels.CodingOption, com.canopobd.data.model.AstraJCodingModels.CodingValue) -> Unit,
    onClearCodingResult: () -> Unit,
    appThemeMode: com.canopobd.data.model.AppThemeMode,
    onSetAppThemeMode: (com.canopobd.data.model.AppThemeMode) -> Unit,
    onSetEmulatorMode: (Boolean) -> Unit,
    showTPMSDialog: Boolean,
    onToggleTPMSDialog: () -> Unit,
    onTPMSReset: () -> Unit,
    showClimateControl: Boolean,
    onToggleClimateControl: () -> Unit,
    onSendClimateCommand: (com.canopobd.ui.climate.ClimateCommand) -> Unit,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    DashboardHeader(
                        connectionState = connectionState,
                        connectionStats = connectionStats,
                        remoteServerRunning = remoteServerRunning,
                        remoteConnectedClients = remoteConnectedClients,
                        emulatorMode = emulatorMode,
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
                        onToggleQuickActions = onToggleQuickActions,
                        onToggleVehicleProfileManager = onToggleVehicleProfileManager,
                        onDisconnect = onDisconnect,
                        recordingActive = recordingActive,
                        isGPSTracking = isGPSTracking,
                        activeAlerts = activeAlerts,
                        onStartGPSTrack = onStartGPSTracking,
                        onStopGPSTrack = onStopGPSTracking
                    )
                }

                if (activeAlerts.isNotEmpty()) {
                    item {
                        AlertBanner(alerts = activeAlerts, colors = colors)
                    }
                }

                if (turboCooldownState.isActive) {
                    item {
                        TurboCoolDownBanner(
                            coolDownState = turboCooldownState,
                            onDismiss = onToggleTurboCooldown
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = stringResource(R.string.analysis).uppercase(),
                        icon = Icons.Filled.Analytics,
                        colors = colors
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DriveStyleCard(
                            analysis = driveStyleResult,
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                        EfficiencyCard(
                            score = drivingEfficiencyResult,
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                        FuelSystemCard(
                            analysis = fuelSystemResult,
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    KnownIssuesCard(currentKm = currentKm)
                }

                item {
                    AnalyzerSummaryRow(
                        gearboxResult = gearboxResult,
                        chainTensionerResult = chainTensionerResult,
                        coolantResult = coolantResult,
                        oilConditionResult = oilConditionResult,
                        pcvResult = pcvResult,
                        lambdaResult = lambdaResult,
                        fuelConsumption = fuelConsumption,
                        egtResult = egtResult,
                        sensorHealthSummary = sensorHealthSummary
                    )
                }

                item {
                    SystemDiagnoseCard(
                        batteryAnalysis = batteryAnalysis,
                        egrAnalysis = egrAnalysis,
                        evapAnalysis = evapAnalysis,
                        saiAnalysis = saiAnalysis,
                        emissionsReadiness = emissionsReadiness,
                        turboSpoolResult = turboSpoolResult,
                        turboEfficiencyResult = turboEfficiencyResult,
                        boostLeakResult = boostLeakResult,
                        wastegateResult = wastegateResult,
                        colors = colors
                    )
                }

                item {
                    SectionHeader(
                        title = stringResource(R.string.gauges).uppercase(),
                        icon = Icons.Filled.Speed,
                        colors = colors
                    )
                }

                item {
                    val primaryIds = primaryGaugeIds.toList()
                    val primaryGaugeData = primaryIds.mapNotNull { id -> gaugeMap[id] }.take(3)

                    PrimaryGaugeRow(
                        gauges = primaryGaugeData,
                        totalSlots = 3,
                        colors = colors
                    )
                }

                item {
                    SecondaryGaugeGrid(
                        gaugeMap = gaugeMap,
                        primaryIds = primaryGaugeIds,
                        colors = colors
                    )
                }

                item {
                    DashboardFooter(
                        obdData = obdData,
                        pollMode = pollMode,
                        connectionStats = connectionStats,
                        isGPSTracking = isGPSTracking,
                        currentTrip = currentTrip,
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showDevicePicker) {
            DevicePickerDialog(devices = devices, onSelect = onConnect, onDismiss = onToggleDevicePicker)
        }
        if (showDTCDialog) {
            DTCDialog(dtcResponse = dtcResponse, onDismiss = onToggleDTCDialog, onClearDTCs = onClearDTCs)
        }
        if (showSettings) {
            SettingsDialog(pollRate = pollRate, measurementUnit = measurementUnit, autoReconnect = autoReconnect, pollMode = pollMode, appThemeMode = appThemeMode, emulatorMode = emulatorMode, onDismiss = onToggleSettings, onPollRateChange = onSetPollRate, onUnitChange = onSetMeasurementUnit, onAutoReconnectChange = onSetAutoReconnect, onPollModeChange = onSetPollMode, onSetAppThemeMode = onSetAppThemeMode, onSetEmulatorMode = onSetEmulatorMode)
        }
        if (showDataLog) {
            DataLogDialog(recordedData = recordedData, isRecording = recordingActive, onDismiss = onToggleDataLog, onStartRecording = onStartRecording, onStopRecording = onStopRecording, onClearData = onClearRecordedData, onExportData = onGetExportData)
        }
        if (showPIDScreen) {
            PIDDialog(obdData = obdData, measurementUnit = measurementUnit, onDismiss = onTogglePIDScreen)
        }
        if (showRemoteDialog) {
            RemoteServerDialog(isRunning = remoteServerRunning, serverIp = remoteServerIp, serverPort = remoteServerPort, connectedClients = remoteConnectedClients, onDismiss = onToggleRemoteDialog, onStartServer = onStartRemoteServer, onStopServer = onStopRemoteServer)
        }
        if (showTripComputer) {
            TripComputerDialog(tripData = tripData, measurementUnit = measurementUnit, vin = onGetStoredVin(), isGPSTracking = isGPSTracking, currentTrip = currentTrip, onDismiss = onToggleTripComputer, onResetTrip = onResetTrip, onStartGPSTrack = onStartGPSTracking, onStopGPSTrack = onStopGPSTracking, onExportGPX = onExportGPX, onExportKML = onExportKML, onClearGPS = onClearGPSTrips)
        }
        if (showCustomization) {
            DashboardCustomizationDialog(currentTheme = colorTheme, primaryGaugeIds = primaryGaugeIds, onDismiss = onToggleCustomization, onThemeChange = onSetColorTheme, onPrimaryGaugesChange = onSetPrimaryGauges)
        }
        if (showHUDMode) {
            HUDModeActivity(obdData = obdData, measurementUnit = measurementUnit, onDismiss = onToggleHUDMode)
        }
        if (showTrendGraph) {
            LiveTrendGraphDialog(trendHistory = trendHistory, onDismiss = onToggleTrendGraph)
        }
        if (showReadiness) {
            ReadinessMonitorDialog(readiness = readinessMonitor, onDismiss = onToggleReadiness)
        }
        if (showDiagnostics) {
            DiagnosticsDialog(protocol = detectedProtocol, supportedPIDs = supportedPIDs, freezeFrames = freezeFrames, onDismiss = onToggleDiagnostics)
        }
        if (showAlertSettings) {
            AlertSettingsDialog(alertConfig = alertConfig, activeAlerts = activeAlerts, onDismiss = onToggleAlertSettings, onUpdateConfig = onSetAlertConfig)
        }
        if (showDataAnalysis) {
            DataAnalysisDialog(importedData = importedData, fuelTrimAnalysis = onGetFuelTrimAnalysis(), onDismiss = onToggleDataAnalysis, onImportCsv = onImportCsv, onClearImported = onClearImported)
        }
        if (showFuelEconomy) {
            com.canopobd.ui.fuel.FuelEconomyDialog(fuelEconomyData = fuelEconomyData, onDismiss = onToggleFuelEconomy)
        }
        if (showMaintenance) {
            com.canopobd.ui.maintenance.MaintenanceDialog(maintenanceItems = maintenanceItems, currentKm = currentKm, onDismiss = onToggleMaintenance, onUpdateItem = onSetMaintenanceItem, onResetItem = onResetMaintenanceItem)
        }
        if (showPerformanceTest) {
            com.canopobd.ui.performance.PerformanceTestDialog(testState = performanceTestState, onDismiss = onTogglePerformanceTest, onStartTest = onStartPerfTest, onStopTest = onStopPerfTest)
        }
        if (showTripHistory) {
            com.canopobd.ui.trip.TripHistoryScreen(
                trips = tripHistoryEntities,
                onBack = onToggleTripHistory,
                onDeleteTrip = onDeleteTrip,
                onClearAll = onClearTripHistory,
                onShareCsv = onShareTripCsv
            )
        }
        if (showPowerCalculator) {
            com.canopobd.ui.power.PowerCalculatorDialog(calculation = powerCalculation, rpm = obdData.rpm, maf = obdData.mafRate, onDismiss = onTogglePowerCalculator)
        }
        if (showDriveScore) {
            com.canopobd.ui.drivescore.DriveScoreDialog(score = driveScore, sessionDuration = (System.currentTimeMillis() - driveSession.startTime) / 1000, harshAccels = driveSession.harshAccels, harshBrakes = driveSession.harshBrakes, idleTimeSeconds = driveSession.idleTimeSeconds, avgRpm = driveSession.avgRpm, avgThrottle = driveSession.avgThrottle, avgSpeed = driveSession.avgSpeed, onDismiss = onToggleDriveScore, onResetScore = onResetDriveScore)
        }
        if (showShiftLight) {
            com.canopobd.ui.shiftlight.ShiftLightDialog(config = shiftLightConfig, currentRpm = obdData.rpm, onDismiss = onToggleShiftLight, onUpdateConfig = onUpdateShiftLightConfig)
        }
        if (showVehicleInfo) {
            VehicleInfoDialog(vin = onGetStoredVin(), onDismiss = onToggleVehicleInfo)
        }
        if (showKnownIssues) {
            KnownIssuesDialog(onDismiss = onToggleKnownIssues)
        }
        if (showTurboMonitor) {
            TurboMonitorDialog(turboData = turboData, oilData = oilData, carProfile = carProfile, onDismiss = onToggleTurboMonitor)
        }
        if (showTimingChainMonitor) {
            TimingChainMonitorDialog(chainState = timingChainState, carProfile = carProfile, onDismiss = onToggleTimingChainMonitor)
        }
        if (_showTurboCooldown) {
            TurboCoolDownDialog(
                coolDownState = turboCooldownState,
                onDismiss = onToggleTurboCooldown
            )
        }
        if (showCarProfile) {
            CarProfileDialog(currentProfile = carProfile, onSelectProfile = onSelectCarProfile, onDismiss = _onToggleCarProfile)
        }
        if (showExtendedGearbox) {
            ExtendedGearboxDialog(
                telemetry = com.canopobd.ui.gearbox.GearboxTelemetry(
                    engineRpm = obdData.rpm,
                    vehicleSpeedKmh = obdData.speed,
                    oilTempCelsius = obdData.oilTemp,
                    engineLoad = obdData.engineLoad
                ),
                onDismiss = onToggleExtendedGearbox
            )
        }
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
                    egtPeak = obdData.egtBank1
                ),
                coolDownState = turboCooldownState,
                onDismiss = onToggleExtendedTurbo
            )
        }
        if (showExtendedFuel) {
            ExtendedFuelEconomyDialog(
                fuelEconomyData = fuelEconomyData,
                fuelLevelPercent = obdData.fuelLevel,
                maf = obdData.mafRate,
                speed = obdData.speed,
                onDismiss = onToggleExtendedFuel
            )
        }
        if (showExtendedMaintenance) {
            ExtendedMaintenanceDialog(
                currentKm = currentKm,
                onDismiss = onToggleExtendedMaintenance,
                onCompleteService = { type, km, interval -> onSetMaintenanceItem(com.canopobd.data.model.MaintenanceType.valueOf(type), km, interval) }
            )
        }
        if (showComfortControl) {
            ComfortControlDialog(
                onCommand = onSendBCMCommand,
                onDismiss = onToggleComfortControl
            )
        }
        if (showCodingDialog) {
            AstraJCodingDialog(
                codingResult = codingResult,
                codingInProgress = codingInProgress,
                onDismiss = onToggleCodingDialog,
                onApplyOption = onApplyCodingOption,
                onClearResult = onClearCodingResult
            )
        }
        if (showTPMSDialog) {
            TPMSDialog(
                onTPMSReset = onTPMSReset,
                onDismiss = onToggleTPMSDialog,
                isConnected = connectionState == OBDConnectionState.Connected
            )
        }
        if (showClimateControl) {
            ClimateControlDialog(
                initialState = com.canopobd.ui.climate.ClimateState(),
                onCommand = onSendClimateCommand,
                onDismiss = onToggleClimateControl
            )
        }
        if (showQuickActions) {
            QuickActionsDialog(
                onDismiss = onToggleQuickActions,
                onExecuteAction = { actionId ->
                    onExecuteQuickAction(actionId)
                    onToggleQuickActions()
                },
                onNavigateTo = { destination ->
                    onToggleQuickActions()
                    when (destination) {
                        "dtc" -> onToggleDTCDialog()
                        "readiness" -> onToggleReadiness()
                        "dashboard" -> {}
                        "settings" -> onToggleSettings()
                        "maintenance" -> onToggleMaintenance()
                        "vehicle_info" -> onToggleVehicleInfo()
                        "datalog_export" -> onToggleDataLog()
                        "tpms" -> onToggleTPMSDialog()
                        "climate" -> onToggleClimateControl()
                        else -> {}
                    }
                }
            )
        }
        if (showVehicleProfileManager) {
            VehicleProfileManagerDialog(
                onDismiss = onToggleVehicleProfileManager,
                onLoadProfile = { profile ->
                    onLoadProfile(profile)
                    onToggleVehicleProfileManager()
                },
                onExportProfile = { _ ->
                    onToggleVehicleProfileManager()
                },
                currentProfile = currentVehicleProfile
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
                    size = 140.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(70.dp))
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
    primaryIds: Set<String>,
    colors: AppColors
) {
    val secondary = gaugeMap.entries
        .filter { it.key !in primaryIds }
        .take(6)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    connectionState: OBDConnectionState,
    connectionStats: ConnectionStats,
    remoteServerRunning: Boolean,
    remoteConnectedClients: Int,
    emulatorMode: Boolean,
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
    onToggleQuickActions: () -> Unit,
    onToggleVehicleProfileManager: () -> Unit,
    onDisconnect: () -> Unit,
    recordingActive: Boolean,
    isGPSTracking: Boolean,
    activeAlerts: List<ActiveAlert>,
    onStartGPSTrack: () -> Unit,
    onStopGPSTrack: () -> Unit
) {
    val colors = LocalAppColors.current
    val connectionColor = when {
        emulatorMode -> colors.gaugeOrange
        connectionState is OBDConnectionState.Connected -> colors.gaugeGreen
        connectionState is OBDConnectionState.Connecting -> colors.gaugeYellow
        connectionState is OBDConnectionState.Error -> colors.gaugeRed
        else -> colors.textSecondary
    }
    val connectionText = when {
        emulatorMode -> stringResource(R.string.status_emulator)
        connectionState is OBDConnectionState.Connected -> stringResource(R.string.status_connected)
        connectionState is OBDConnectionState.Connecting -> stringResource(R.string.status_connecting)
        connectionState is OBDConnectionState.Disconnected -> stringResource(R.string.status_disconnected)
        connectionState is OBDConnectionState.Error -> (connectionState as OBDConnectionState.Error).message
        else -> stringResource(R.string.status_disconnected)
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
                    if (emulatorMode) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = colors.gaugeOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "SIM",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.gaugeOrange,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = connectionText,
                        fontSize = 12.sp,
                        color = connectionColor,
                        fontWeight = FontWeight.Medium
                    )
                    if (connectionState is OBDConnectionState.Connected || emulatorMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        if (!emulatorMode) {
                            ConnectionQualityBadge(stats = connectionStats, colors = colors)
                        }
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
                    onClick = onToggleTripComputer
                )
                QuickActionButton(
                    icon = Icons.Filled.LocalGasStation,
                    label = stringResource(R.string.fuel_economy_title),
                    color = colors.textSecondary,
                    onClick = onToggleFuelEconomy
                )
                QuickActionButton(
                    icon = Icons.Filled.Build,
                    label = stringResource(R.string.maintenance_title),
                    color = colors.textSecondary,
                    onClick = onToggleMaintenance
                )
                QuickActionButton(
                    icon = Icons.Filled.Speed,
                    label = stringResource(R.string.perf_test_title),
                    color = colors.textSecondary,
                    onClick = onTogglePerformanceTest
                )
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    label = stringResource(R.string.trend),
                    color = colors.textSecondary,
                    onClick = onToggleTrendGraph
                )
                QuickActionButton(
                    icon = Icons.Filled.Route,
                    label = stringResource(R.string.trip_history_title),
                    color = colors.textSecondary,
                    onClick = onToggleTripHistory
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
                    onClick = onToggleCustomization
                )
                QuickActionButton(
                    icon = Icons.Filled.Settings,
                    label = stringResource(R.string.settings),
                    color = colors.textSecondary,
                    onClick = onToggleSettings
                )
                QuickActionButton(
                    icon = Icons.Filled.FlashOn,
                    label = "Quick",
                    color = colors.gaugeOrange,
                    onClick = onToggleQuickActions
                )
                QuickActionButton(
                    icon = Icons.Filled.Bluetooth,
                    label = stringResource(R.string.bluetooth),
                    color = colors.accent,
                    onClick = onToggleDevicePicker
                )
                if (connectionState is OBDConnectionState.Connected) {
                    QuickActionButton(
                        icon = Icons.Filled.ElectricBolt,
                        label = stringResource(R.string.dashboard_power),
                        color = colors.gaugeYellow,
                        onClick = onTogglePowerCalculator
                    )
                    QuickActionButton(
                        icon = Icons.Filled.SportsScore,
                        label = stringResource(R.string.dashboard_driving_style),
                        color = colors.gaugeGreen,
                        onClick = onToggleDriveScore
                    )
                    QuickActionButton(
                        icon = Icons.Filled.LightMode,
                        label = stringResource(R.string.dashboard_shift_light),
                        color = colors.gaugeOrange,
                        onClick = onToggleShiftLight
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Info,
                        label = stringResource(R.string.dashboard_vehicle_profile),
                        color = colors.highlight,
                        onClick = _onToggleCarProfile
                    )
                    QuickActionButton(
                        icon = Icons.Filled.DirectionsCar,
                        label = "Profile",
                        color = colors.gaugeGreen,
                        onClick = onToggleVehicleProfileManager
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
                        onClick = onToggleComfortControl
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Code,
                        label = "Codierung",
                        color = colors.gaugeOrange,
                        onClick = onToggleCodingDialog
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
                        onClick = onToggleTimingChainMonitor
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Timer,
                        label = stringResource(R.string.dashboard_turbo_cooldown),
                        color = colors.gaugeCyan,
                        onClick = onToggleTurboCooldown
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Tv,
                        label = stringResource(R.string.hud_mode),
                        color = colors.gaugeCyan,
                        onClick = onToggleHUDMode
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Verified,
                        label = stringResource(R.string.readiness),
                        color = colors.gaugeGreen,
                        onClick = onToggleReadiness
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Biotech,
                        label = stringResource(R.string.diagnostics),
                        color = colors.accent,
                        onClick = onToggleDiagnostics
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Analytics,
                        label = stringResource(R.string.analysis),
                        color = colors.accent,
                        onClick = onToggleDataAnalysis
                    )
                    QuickActionButton(
                        icon = if (activeAlerts.isNotEmpty()) Icons.Filled.NotificationImportant else Icons.Filled.Notifications,
                        label = stringResource(R.string.alerts),
                        color = if (activeAlerts.isNotEmpty()) colors.gaugeRed else colors.textSecondary,
                        onClick = onToggleAlertSettings
                    )
                    QuickActionButton(
                        icon = if (remoteServerRunning) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        label = stringResource(R.string.remote_server),
                        color = if (remoteServerRunning) colors.gaugeGreen else colors.accent,
                        onClick = onToggleRemoteDialog
                    )
                    QuickActionButton(
                        icon = if (recordingActive) Icons.Filled.FiberManualRecord else Icons.Filled.Analytics,
                        label = stringResource(R.string.data_log),
                        color = if (recordingActive) colors.gaugeRed else colors.accent,
                        onClick = onToggleDataLog
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Sensors,
                        label = stringResource(R.string.sensors),
                        color = colors.accent,
                        onClick = onTogglePIDScreen
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Warning,
                        label = stringResource(R.string.fault_codes),
                        color = colors.gaugeYellow,
                        onClick = onToggleDTCDialog
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
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, colors: AppColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textDim,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textDim
        )
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
                    items(devices, key = { it.address }) { device ->
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

@Composable
private fun SystemDiagnoseCard(
    batteryAnalysis: com.canopobd.data.domain.BatteryHealthAnalyzer.BatteryAnalysis?,
    egrAnalysis: com.canopobd.data.domain.EGRHealthAnalyzer.EGRAnalysis?,
    evapAnalysis: com.canopobd.data.domain.EVAPSystemAnalyzer.EVAPAnalysis?,
    saiAnalysis: com.canopobd.data.domain.SecondaryAirAnalyzer.SAIAnalysis?,
    emissionsReadiness: com.canopobd.data.domain.EmissionsReadinessAnalyzer.ReadinessAnalysis?,
    turboSpoolResult: com.canopobd.data.domain.TurboSpoolAnalyzer.SpoolAnalysis?,
    turboEfficiencyResult: com.canopobd.data.domain.TurboEfficiencyAnalyzer.TurboEfficiencyAnalysis?,
    boostLeakResult: com.canopobd.data.domain.BoostLeakDetector.BoostLeakAnalysis?,
    wastegateResult: com.canopobd.data.domain.WastegateHealthAnalyzer.WastegateAnalysis?,
    colors: AppColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MedicalServices,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Systemdiagnose",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val batteryColor = when {
                    batteryAnalysis == null -> colors.textSecondary
                    batteryAnalysis.healthScore >= 80 -> colors.gaugeGreen
                    batteryAnalysis.healthScore >= 50 -> colors.gaugeYellow
                    else -> colors.gaugeRed
                }
                val egrColor = when {
                    egrAnalysis == null -> colors.textSecondary
                    egrAnalysis.healthScore >= 80 -> colors.gaugeGreen
                    egrAnalysis.healthScore >= 50 -> colors.gaugeYellow
                    else -> colors.gaugeRed
                }
                val evapColor = when {
                    evapAnalysis == null -> colors.textSecondary
                    evapAnalysis.healthScore >= 80 -> colors.gaugeGreen
                    evapAnalysis.healthScore >= 50 -> colors.gaugeYellow
                    else -> colors.gaugeRed
                }

                MiniStatusCard(
                    label = "Batterie",
                    icon = Icons.Filled.BatteryChargingFull,
                    value = "%.1fV".format(batteryAnalysis?.status?.voltage ?: 0.0),
                    subValue = "${batteryAnalysis?.healthScore?.toInt() ?: 0}%",
                    color = batteryColor,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                MiniStatusCard(
                    label = "EGR",
                    icon = Icons.Filled.Eco,
                    value = "%.1f%%".format(egrAnalysis?.flowDeviation ?: 0.0),
                    subValue = "${egrAnalysis?.healthScore?.toInt() ?: 0}%",
                    color = egrColor,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                MiniStatusCard(
                    label = "EVAP",
                    icon = Icons.Filled.Cloud,
                    value = "%.1f%%".format(evapAnalysis?.purgeEfficiency ?: 0.0),
                    subValue = "${evapAnalysis?.healthScore?.toInt() ?: 0}%",
                    color = evapColor,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val saiColor = when {
                    saiAnalysis == null -> colors.textSecondary
                    saiAnalysis.healthScore >= 80 -> colors.gaugeGreen
                    saiAnalysis.healthScore >= 50 -> colors.gaugeYellow
                    else -> colors.gaugeRed
                }
                val emPct = if (emissionsReadiness != null) {
                    if (emissionsReadiness.totalCount > 0) (emissionsReadiness.completedCount * 100) / emissionsReadiness.totalCount else 0
                } else 0
                val emColor = when {
                    emissionsReadiness == null -> colors.textSecondary
                    emPct >= 80 -> colors.gaugeGreen
                    emPct >= 50 -> colors.gaugeYellow
                    else -> colors.gaugeRed
                }
                val turboColor = when {
                    turboSpoolResult == null && turboEfficiencyResult == null && boostLeakResult == null && wastegateResult == null -> colors.textSecondary
                    else -> colors.gaugeGreen
                }

                MiniStatusCard(
                    label = "SAI",
                    icon = Icons.Filled.Air,
                    value = if (saiAnalysis?.status?.isActive == true) "An" else "Aus",
                    subValue = "${saiAnalysis?.healthScore?.toInt() ?: 0}%",
                    color = saiColor,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                MiniStatusCard(
                    label = "Abgas",
                    icon = Icons.Filled.Verified,
                    value = "${emPct}%",
                    subValue = null,
                    color = emColor,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    progress = emPct / 100f
                )

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Air,
                                contentDescription = null,
                                tint = turboColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Turbo", fontSize = 9.sp, color = colors.textSecondary, maxLines = 1)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(4.dp).background(if (turboSpoolResult != null) colors.gaugeGreen else colors.textDim, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Spool", fontSize = 8.sp, color = colors.textSecondary, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(4.dp).background(if (turboEfficiencyResult != null) colors.gaugeGreen else colors.textDim, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Effiz.", fontSize = 8.sp, color = colors.textSecondary, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(4.dp).background(if (boostLeakResult != null) colors.gaugeGreen else colors.textDim, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Leak", fontSize = 8.sp, color = colors.textSecondary, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(4.dp).background(if (wastegateResult != null) colors.gaugeGreen else colors.textDim, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("WG", fontSize = 8.sp, color = colors.textSecondary, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatusCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    subValue: String?,
    color: Color,
    colors: AppColors,
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 9.sp, color = colors.textSecondary, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            if (subValue != null) {
                Text(
                    text = subValue,
                    fontSize = 9.sp,
                    color = color
                )
            } else if (progress != null) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun DashboardFooter(
    obdData: OBDData,
    pollMode: PollMode,
    connectionStats: ConnectionStats,
    isGPSTracking: Boolean,
    currentTrip: GPSTrip?,
    colors: AppColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Batt: %.1fV".format(obdData.batteryVoltage),
                fontSize = 12.sp,
                color = when {
                    obdData.batteryVoltage in 12f..13.5f -> colors.gaugeGreen
                    obdData.batteryVoltage in 11.5f..12f -> colors.gaugeOrange
                    obdData.batteryVoltage > 0 && obdData.batteryVoltage < 11.5f -> colors.gaugeRed
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
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (connectionStats.quality == ConnectionQuality.POOR) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = colors.gaugeRed.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.conn_quality_poor_message),
                    fontSize = 10.sp,
                    color = colors.gaugeRed,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
