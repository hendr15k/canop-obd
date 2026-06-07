package com.canopobd.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.*
import com.canopobd.ui.components.*
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
import com.canopobd.ui.coding.AstraJCodingDialog
import com.canopobd.data.model.AstraJCodingModels
import com.canopobd.ui.profile.QuickActionsDialog
import com.canopobd.ui.profile.VehicleProfileManagerDialog
import com.canopobd.ui.profile.SavedProfile
import com.canopobd.ui.tpms.TPMSDialog
import com.canopobd.ui.climate.ClimateControlDialog
import com.canopobd.ui.climate.ClimateCommand
import com.canopobd.ui.components.TCMECMCANStatusCard
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
    tpmsData: List<com.canopobd.ui.tpms.TireData>?,
    showClimateControl: Boolean,
    onToggleClimateControl: () -> Unit,
    onSendClimateCommand: (com.canopobd.ui.climate.ClimateCommand) -> Unit,
    tcmReading: com.canopobd.data.repository.OBDRepository.TCMReading,
    ecmReading: com.canopobd.data.repository.OBDRepository.ECMReading,
    safetySummary: com.canopobd.data.model.SafetySummary = com.canopobd.data.model.SafetySummary(),
    ecoScoreData: com.canopobd.data.model.EcoScoreData = com.canopobd.data.model.EcoScoreData(),
    gpsSpeedKmh: Double = 0.0,
    accelerationRun: com.canopobd.data.model.AccelerationRun? = null,
    onToggleSafetySystems: () -> Unit = {},
    onToggleEcoScore: () -> Unit = {},
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
        isCoolantCritical -> colors.critical.copy(alpha = 0.08f)
        isRpmCritical -> colors.warning.copy(alpha = 0.06f)
        else -> colors.surfaceBlack
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
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
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
                        dtcResponse = dtcResponse,
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

                // ----- HERO STATS (RPM + Speed side-by-side) -----------------
                item {
                    HeroStatsRow(obdData = obdData, measurementUnit = measurementUnit, colors = colors)
                }

                // ----- ANALYSIS ROW -----------------------------------------
                item {
                    SectionHeader(
                        title = stringResource(R.string.analysis),
                        icon = Icons.Filled.Analytics
                    )
                }

                item {
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
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                // ----- GAUGES SECTION ---------------------------------------
                item {
                    SectionHeader(
                        title = stringResource(R.string.gauges),
                        icon = Icons.Filled.Speed,
                        actionLabel = stringResource(R.string.customize_dashboard),
                        onAction = onToggleCustomization
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
                    TCMECMCANStatusCard(
                        tcmCurrentGear = tcmReading.currentGear,
                        tcmOilTempCelsius = tcmReading.oilTempCelsius,
                        tcmPressureKpa = tcmReading.pressureKpa,
                        tcmSportMode = tcmReading.sportMode,
                        tcmManualMode = tcmReading.manualMode,
                        tcmError = tcmReading.transmissionError,
                        ecmRpm = ecmReading.rpm,
                        ecmSpeedKmh = ecmReading.speedKmh,
                        ecmCoolantTemp = ecmReading.coolantTemp.toInt(),
                        ecmThrottlePosition = ecmReading.throttlePosition,
                        ecmEngineLoad = ecmReading.engineLoad,
                        lastUpdateTime = tcmReading.timestamp,
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
                }
            }
        }

        // ----- DIALOGS (unchanged) -----
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
            com.canopobd.ui.performance.PerformanceTestDialog(testState = performanceTestState, gpsSpeedKmh = gpsSpeedKmh, accelerationRun = accelerationRun, onDismiss = onTogglePerformanceTest, onStartTest = onStartPerfTest, onStopTest = onStopPerfTest)
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
            com.canopobd.ui.drivescore.DriveScoreDialog(score = driveScore, sessionDuration = (System.currentTimeMillis() - driveSession.startTime) / 1000, harshAccels = driveSession.harshAccels, harshBrakes = driveSession.harshBrakes, idleTimeSeconds = driveSession.idleTimeSeconds, avgRpm = driveSession.avgRpm, avgThrottle = driveSession.avgThrottle, avgSpeed = driveSession.avgSpeed, fuelConsumptionL100km = fuelConsumption?.avgL100km ?: 0.0, onDismiss = onToggleDriveScore, onResetScore = onResetDriveScore)
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
                isConnected = connectionState == OBDConnectionState.Connected,
                tireData = tpmsData
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
            data.rpm > 6000 -> Color(0xFFFF3D57)
            data.rpm > 5500 -> Color(0xFFFF9100)
            else -> Color(0xFF00E676)
        }),
    "speed" to GaugeItem("speed", "Speed", unit.convertSpeed(data.speed).toFloat(), unit.speedUnit, 0f, 260f, Color(0xFF2979FF)),
    "coolant" to GaugeItem("coolant", "Coolant", unit.convertTemp(data.coolantTemp).toFloat(), unit.tempUnit, -40f, 215f,
        when {
            data.coolantTemp > 110 -> Color(0xFFFF3D57)
            data.coolantTemp > 100 -> Color(0xFFFF9100)
            data.coolantTemp > 80 -> Color(0xFF00E676)
            data.coolantTemp < 40 -> Color(0xFF00BCD4)
            else -> Color(0xFFFF9100)
        }),
    "throttle" to GaugeItem("throttle", "Throttle", data.throttle.toFloat(), "%", 0f, 100f, Color(0xFFFFC107)),
    "engine_load" to GaugeItem("engine_load", "Eng Load", data.engineLoad.toFloat(), "%", 0f, 100f, Color(0xFFFFC107)),
    "fuel" to GaugeItem("fuel", "Fuel", data.fuelLevel.toFloat(), "%", 0f, 100f, Color(0xFFFF9100)),
    "timing" to GaugeItem("timing", "Timing", data.timingAdvance.toFloat(), "°", -64f, 64f, Color(0xFF00BCD4)),
    "maf" to GaugeItem("maf", "MAF", data.mafRate.toFloat(), "g/s", 0f, 500f, Color(0xFFE91E63)),
    "intake_temp" to GaugeItem("intake_temp", "Intake", unit.convertTemp(data.intakeTemp).toFloat(), unit.tempUnit, -40f, 215f, Color(0xFFFF5722)),
    "fuel_trim" to GaugeItem("fuel_trim", "Fuel Trim", abs(data.shortTermFuelTrimB1 + data.longTermFuelTrimB1).toFloat(), "%", 0f, 50f, Color(0xFFFFAB40), isPercentage = true),
    "load" to GaugeItem("load", "Abs Load", data.absoluteLoadValue.toFloat(), "%", 0f, 100f, Color(0xFF69F0AE)),
    "fuel_rate" to GaugeItem("fuel_rate", "Fuel Rate", data.engineFuelRate.toFloat(), "L/h", 0f, 50f, Color(0xFFFF3D57)),
    "accel_pedal" to GaugeItem("accel_pedal", "Accel Pedal", data.acceleratorPosD.toFloat(), "%", 0f, 100f, Color(0xFF00E5FF)),
    "hybrid_battery" to GaugeItem("hybrid_battery", "Hybrid Batt", data.hybridBatteryRemaining.toFloat(), "%", 0f, 100f,
        when {
            data.hybridBatteryRemaining < 20 -> Color(0xFFFF3D57)
            data.hybridBatteryRemaining < 40 -> Color(0xFFFF9100)
            else -> Color(0xFF69F0AE)
        })
)

// ---------------------------------------------------------------------------
// HERO STATS — Big RPM + Speed display with glow effect
// ---------------------------------------------------------------------------
@Composable
private fun HeroStatsRow(obdData: OBDData, measurementUnit: MeasurementUnit, colors: AppColors) {
    val rpmColor = when {
        obdData.rpm > 6000 -> colors.critical
        obdData.rpm > 5500 -> colors.warning
        else -> colors.primary
    }
    val speedColor = colors.secondary
    val speedValue = measurementUnit.convertSpeed(obdData.speed)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.lg))
            .background(colors.surfaceBase)
            .background(colors.gradientCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.lg))
    ) {
        // Hero glow background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf<Color>(
                            colors.primary.copy(alpha = 0.10f),
                            Color.Transparent,
                            colors.secondary.copy(alpha = 0.10f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RPM block
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppRadius.md))
                    .background(colors.surfaceRaised.copy(alpha = 0.6f))
                    .border(1.dp, rpmColor.copy(alpha = 0.4f), RoundedCornerShape(AppRadius.md))
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(color = rpmColor, size = 6.dp, pulse = true)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "RPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = obdData.rpm.toInt().toString(),
                    style = GaugeTypography.valueXL,
                    color = rpmColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "U/min",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
            // Speed block
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppRadius.md))
                    .background(colors.surfaceRaised.copy(alpha = 0.6f))
                    .border(1.dp, speedColor.copy(alpha = 0.4f), RoundedCornerShape(AppRadius.md))
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(color = speedColor, size = 6.dp, pulse = true)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "SPEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = speedValue.toInt().toString(),
                    style = GaugeTypography.valueXL,
                    color = speedColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = measurementUnit.speedUnit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

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
                    size = 120.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(AppRadius.pill))
                        .background(colors.surfaceRaised.copy(alpha = 0.4f))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.pill)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add gauge",
                            tint = colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "LEER",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryGaugeGrid(
    gaugeMap: Map<String, GaugeItem>,
    primaryIds: Set<String>,
    @Suppress("UNUSED_PARAMETER") colors: AppColors
) {
    val secondary = gaugeMap.entries
        .filter { it.key !in primaryIds }
        .take(6)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        secondary.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (_, gauge) ->
                    CompactGauge(
                        value = gauge.value.toDouble(),
                        label = gauge.label,
                        unit = gauge.unit,
                        max = gauge.maxValue.toDouble(),
                        color = gauge.color,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DASHBOARD HEADER — Hero status with quick-access action bar
// ---------------------------------------------------------------------------
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
    @Suppress("UNUSED_PARAMETER") onToggleVehicleInfo: () -> Unit,
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
    dtcResponse: DTCResponse?,
    onStartGPSTrack: () -> Unit,
    onStopGPSTrack: () -> Unit
) {
    val colors = LocalAppColors.current
    val connectionColor = when {
        emulatorMode -> colors.warning
        connectionState is OBDConnectionState.Connected -> colors.success
        connectionState is OBDConnectionState.Connecting -> colors.caution
        connectionState is OBDConnectionState.Error -> colors.critical
        else -> colors.textSecondary
    }
    val connectionText = when {
        emulatorMode -> stringResource(R.string.status_emulator)
        connectionState is OBDConnectionState.Connected -> stringResource(R.string.status_connected)
        connectionState is OBDConnectionState.Connecting -> stringResource(R.string.status_connecting)
        connectionState is OBDConnectionState.Disconnected -> stringResource(R.string.status_disconnected)
        connectionState is OBDConnectionState.Error -> connectionState.message
        else -> stringResource(R.string.status_disconnected)
    }

    // Top status pill
    GlassCard(
        padding = 12.dp,
        accentEdge = connectionColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(colors.gradientAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "C",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.surfaceBlack,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "canop-obd",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPure,
                        fontWeight = FontWeight.Bold
                    )
                    if (emulatorMode) {
                        Spacer(Modifier.width(6.dp))
                        StatusPill(text = "SIM", color = colors.warning)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(color = connectionColor, size = 6.dp, pulse = true)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = connectionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = connectionColor
                    )
                    if (connectionState is OBDConnectionState.Connected && !emulatorMode) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "· ${connectionStats.quality.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )
                    }
                    if (remoteServerRunning) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "· ${remoteConnectedClients} PC",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.success
                        )
                    }
                }
            }
            IconButton(onClick = onToggleSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = colors.textTertiary
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Primary quick-action row (most used)
    GlassCard(padding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickTile(
                icon = Icons.Filled.DirectionsCar,
                label = "Trip",
                onClick = onToggleTripComputer
            )
            QuickTile(
                icon = Icons.Filled.LocalGasStation,
                label = "Verbrauch",
                onClick = onToggleFuelEconomy
            )
            QuickTile(
                icon = Icons.Filled.Build,
                label = "Wartung",
                onClick = onToggleMaintenance
            )
            QuickTile(
                icon = Icons.Filled.Speed,
                label = "0-100",
                onClick = onTogglePerformanceTest
            )
            QuickTile(
                icon = Icons.AutoMirrored.Filled.ShowChart,
                label = "Trend",
                onClick = onToggleTrendGraph
            )
        }
        Spacer(Modifier.height(10.dp))
        DividerLine()
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickTile(
                icon = Icons.Filled.Route,
                label = "Historie",
                onClick = onToggleTripHistory
            )
            QuickTile(
                icon = if (isGPSTracking) Icons.Filled.LocationOn else Icons.Filled.LocationSearching,
                label = "GPS",
                isActive = isGPSTracking,
                accentColor = if (isGPSTracking) colors.success else null,
                onClick = { if (isGPSTracking) onStopGPSTrack() else onStartGPSTrack() }
            )
            QuickTile(
                icon = Icons.Filled.Dashboard,
                label = "Layout",
                accentColor = colors.accent,
                onClick = onToggleCustomization
            )
            QuickTile(
                icon = Icons.Filled.FlashOn,
                label = "Quick",
                accentColor = colors.warning,
                onClick = onToggleQuickActions
            )
            QuickTile(
                icon = Icons.Filled.Bluetooth,
                label = "BT",
                accentColor = colors.primary,
                onClick = onToggleDevicePicker
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    // Secondary quick-action row (extended features)
    GlassCard(padding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickTile(
                icon = Icons.Filled.Sensors,
                label = "Sensoren",
                accentColor = colors.primary,
                onClick = onTogglePIDScreen
            )
            QuickTile(
                icon = Icons.Filled.Warning,
                label = "DTC",
                accentColor = colors.caution,
                badgeColor = if (dtcResponse != null && dtcResponse.codes.isNotEmpty()) colors.critical else null,
                onClick = onToggleDTCDialog
            )
            QuickTile(
                icon = Icons.Filled.Analytics,
                label = "Daten",
                accentColor = colors.primary,
                onClick = onToggleDataAnalysis
            )
            QuickTile(
                icon = Icons.Filled.Verified,
                label = "Ready",
                accentColor = colors.success,
                onClick = onToggleReadiness
            )
            QuickTile(
                icon = Icons.Filled.Biotech,
                label = "Diagnose",
                accentColor = colors.primary,
                onClick = onToggleDiagnostics
            )
        }
        Spacer(Modifier.height(10.dp))
        DividerLine()
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickTile(
                icon = Icons.Filled.Air,
                label = "Turbo",
                accentColor = colors.primary,
                onClick = onToggleTurboMonitor
            )
            QuickTile(
                icon = Icons.Filled.SettingsApplications,
                label = "Kette",
                accentColor = colors.primary,
                onClick = onToggleTimingChainMonitor
            )
            QuickTile(
                icon = Icons.Filled.ElectricBolt,
                label = "Power",
                accentColor = colors.caution,
                onClick = onTogglePowerCalculator
            )
            QuickTile(
                icon = Icons.Filled.SportsScore,
                label = "Stil",
                accentColor = colors.success,
                onClick = onToggleDriveScore
            )
            QuickTile(
                icon = Icons.Filled.LightMode,
                label = "Blitz",
                accentColor = colors.warning,
                onClick = onToggleShiftLight
            )
        }
        Spacer(Modifier.height(10.dp))
        DividerLine()
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickTile(
                icon = Icons.Filled.Info,
                label = "Fahrzeug",
                accentColor = colors.primary,
                onClick = onToggleVehicleInfo
            )
            QuickTile(
                icon = Icons.Filled.BugReport,
                label = "Probleme",
                accentColor = colors.caution,
                onClick = onToggleKnownIssues
            )
            QuickTile(
                icon = Icons.Filled.SettingsRemote,
                label = "Komfort",
                accentColor = colors.info,
                onClick = onToggleComfortControl
            )
            QuickTile(
                icon = Icons.Filled.Code,
                label = "Codier.",
                accentColor = colors.warning,
                onClick = onToggleCodingDialog
            )
            QuickTile(
                icon = Icons.Filled.Tv,
                label = "HUD",
                accentColor = colors.info,
                onClick = onToggleHUDMode
            )
        }
        Spacer(Modifier.height(10.dp))
        DividerLine()
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickTile(
                icon = if (recordingActive) Icons.Filled.FiberManualRecord else Icons.Filled.Analytics,
                label = "Log",
                isActive = recordingActive,
                accentColor = if (recordingActive) colors.critical else colors.primary,
                onClick = onToggleDataLog
            )
            QuickTile(
                icon = if (remoteServerRunning) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                label = "Remote",
                isActive = remoteServerRunning,
                accentColor = if (remoteServerRunning) colors.success else colors.primary,
                onClick = onToggleRemoteDialog
            )
            QuickTile(
                icon = Icons.Filled.Timer,
                label = "Cooldown",
                accentColor = colors.info,
                onClick = onToggleTurboCooldown
            )
            QuickTile(
                icon = if (activeAlerts.isNotEmpty()) Icons.Filled.NotificationImportant else Icons.Filled.Notifications,
                label = "Alarm",
                isActive = activeAlerts.isNotEmpty(),
                accentColor = if (activeAlerts.isNotEmpty()) colors.critical else colors.textTertiary,
                onClick = onToggleAlertSettings
            )
            QuickTile(
                icon = Icons.Filled.Close,
                label = "Trennen",
                accentColor = colors.critical,
                onClick = onDisconnect
            )
        }
    }
}

@Composable
private fun AlertBanner(alerts: List<ActiveAlert>, colors: AppColors) {
    val alert = alerts.firstOrNull() ?: return
    val alertColor = colors.critical
    AccentCard(
        accentColor = alertColor,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(alertColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationImportant,
                    contentDescription = null,
                    tint = alertColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WARNUNG",
                    style = MaterialTheme.typography.labelSmall,
                    color = alertColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (alerts.size > 1) "${alert.message} (+${alerts.size - 1})" else alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SYSTEM DIAGNOSE — Battery / EGR / EVAP / SAI / Emissions / Turbo mini cards
// ---------------------------------------------------------------------------
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
    GlassCard(padding = 12.dp) {
        SectionHeaderInline(title = "Systemdiagnose", icon = Icons.Filled.MedicalServices)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val batteryColor = when {
                batteryAnalysis == null -> colors.textTertiary
                batteryAnalysis.healthScore >= 80 -> colors.success
                batteryAnalysis.healthScore >= 50 -> colors.caution
                else -> colors.critical
            }
            val egrColor = when {
                egrAnalysis == null -> colors.textTertiary
                egrAnalysis.healthScore >= 80 -> colors.success
                egrAnalysis.healthScore >= 50 -> colors.caution
                else -> colors.critical
            }
            val evapColor = when {
                evapAnalysis == null -> colors.textTertiary
                evapAnalysis.healthScore >= 80 -> colors.success
                evapAnalysis.healthScore >= 50 -> colors.caution
                else -> colors.critical
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

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val saiColor = when {
                saiAnalysis == null -> colors.textTertiary
                saiAnalysis.healthScore >= 80 -> colors.success
                saiAnalysis.healthScore >= 50 -> colors.caution
                else -> colors.critical
            }
            val emPct = if (emissionsReadiness != null) {
                if (emissionsReadiness.totalCount > 0) (emissionsReadiness.completedCount * 100) / emissionsReadiness.totalCount else 0
            } else 0
            val emColor = when {
                emissionsReadiness == null -> colors.textTertiary
                emPct >= 80 -> colors.success
                emPct >= 50 -> colors.caution
                else -> colors.critical
            }
            val turboColor = when {
                turboSpoolResult == null && turboEfficiencyResult == null && boostLeakResult == null && wastegateResult == null -> colors.textTertiary
                else -> colors.success
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
            MiniStatusCard(
                label = "Turbo",
                icon = Icons.Filled.Air,
                value = when {
                    wastegateResult != null && wastegateResult.healthScore < 70 -> "WG!"
                    boostLeakResult?.severity?.severity ?: -1 > 0 -> "LEAK"
                    turboEfficiencyResult != null -> "OK"
                    else -> "—"
                },
                subValue = null,
                color = turboColor,
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SectionHeaderInline(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    val colors = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised.copy(alpha = 0.5f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(AppRadius.md))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, maxLines = 1)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = GaugeTypography.valueSmall,
                color = color,
                maxLines = 1
            )
            if (subValue != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            } else if (progress != null) {
                Spacer(Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f)
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
    GlassCard(padding = 10.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterStat(
                label = "BATT",
                value = "%.1fV".format(obdData.batteryVoltage),
                color = when {
                    obdData.batteryVoltage in 12f..13.5f -> colors.success
                    obdData.batteryVoltage in 11.5f..12f -> colors.warning
                    obdData.batteryVoltage > 0 && obdData.batteryVoltage < 11.5f -> colors.critical
                    else -> colors.textTertiary
                }
            )
            FooterStat(
                label = "LOAD",
                value = "%.0f%%".format(obdData.absoluteLoadValue),
                color = when {
                    obdData.absoluteLoadValue > 90 -> colors.warning
                    obdData.absoluteLoadValue > 80 -> colors.caution
                    else -> colors.textPrimary
                }
            )
            FooterStat(
                label = "MODE",
                value = pollMode.label,
                color = colors.primary
            )
            FooterStat(
                label = "CONN",
                value = if (connectionStats.quality != ConnectionQuality.UNKNOWN) connectionStats.quality.label else "—",
                color = when (connectionStats.quality) {
                    ConnectionQuality.EXCELLENT, ConnectionQuality.GOOD -> colors.success
                    ConnectionQuality.FAIR -> colors.caution
                    ConnectionQuality.POOR -> colors.critical
                    else -> colors.textTertiary
                }
            )
        }
        if (isGPSTracking && currentTrip != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StatusDot(color = colors.success, size = 6.dp, pulse = true)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "GPS aktiv · %.1f km".format(currentTrip.distanceKm),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.success
                )
            }
        }
        if (connectionStats.quality == ConnectionQuality.POOR) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(colors.critical.copy(alpha = 0.12f))
                    .border(1.dp, colors.critical.copy(alpha = 0.3f), RoundedCornerShape(AppRadius.sm))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.conn_quality_poor_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.critical,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FooterStat(label: String, value: String, color: Color) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontSize = 9.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = GaugeTypography.valueTiny,
            color = color
        )
    }
}

// ---------------------------------------------------------------------------
// DEVICE PICKER
// ---------------------------------------------------------------------------
@Composable
private fun DevicePickerDialog(
    devices: List<BluetoothDeviceInfo>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceBase,
        titleContentColor = colors.textPure,
        textContentColor = colors.textSecondary,
        title = {
            Text(stringResource(R.string.choose_adapter), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            if (devices.isEmpty()) {
                Text(stringResource(R.string.no_paired_devices))
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
                Text(stringResource(R.string.cancel), color = colors.primary)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.sm))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(AppRadius.sm))
                .background(colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Bluetooth,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            Text(address, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
        }
    }
}
