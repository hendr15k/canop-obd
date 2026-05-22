package com.canopobd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.canopobd.R
import com.canopobd.ui.dashboard.DashboardScreen
import com.canopobd.ui.theme.*
import com.canopobd.ui.comfort.ComfortCommand
import com.canopobd.ui.update.UpdateDialog
import com.canopobd.ui.profile.SavedProfile
import com.canopobd.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(this, DashboardViewModel.Factory(application))[DashboardViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            val colorTheme by viewModel.colorTheme.collectAsState()
            val appThemeMode by viewModel.appThemeMode.collectAsState()
            val isInitialized by viewModel.isInitialized.collectAsState()
            val appColors = remember(colorTheme, appThemeMode) { colorTheme.toAppColors(appThemeMode) }

            CanopObdTheme(appColors = appColors, appThemeMode = appThemeMode) {
                if (!isInitialized) {
                    SplashScreen(appColors = appColors)
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = appColors.dark
                    ) {
                        MainContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(appColors: AppColors) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.dark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "canop-obd",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.accent
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OBD-II Diagnose",
                fontSize = 14.sp,
                color = appColors.textSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = appColors.accent,
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
private fun MainContent(viewModel: DashboardViewModel) {
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var permissionsGranted by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
        if (permissionsGranted) {
            viewModel.onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            launcher.launch(requiredPermissions.toTypedArray())
        } else {
            viewModel.onPermissionsGranted()
        }
    }

    if (permissionsGranted) {
        DashboardContent(viewModel = viewModel)
    } else {
        PermissionRequiredScreen(onRequest = { launcher.launch(requiredPermissions.toTypedArray()) })
    }
}

@Composable
private fun PermissionRequiredScreen(onRequest: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.permissions_title),
                fontSize = 18.sp,
                color = colors.highlight
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.permissions_message),
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onRequest) {
                Text(stringResource(R.string.permissions_grant), color = colors.accent, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun DashboardContent(viewModel: DashboardViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val obdData by viewModel.obdData.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val showDevicePicker by viewModel.showDevicePicker.collectAsState()
    val dtcResponse by viewModel.dtcResponse.collectAsState()
    val recordingActive by viewModel.recordingActive.collectAsState()
    val recordedData by viewModel.recordedData.collectAsState()
    val pollRate by viewModel.pollRate.collectAsState()
    val measurementUnit by viewModel.measurementUnit.collectAsState()
    val showDTCDialog by viewModel.showDTCDialog.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showDataLog by viewModel.showDataLog.collectAsState()
    val showPIDScreen by viewModel.showPIDScreen.collectAsState()
    val showRemoteDialog by viewModel.showRemoteDialog.collectAsState()
    val showTripComputer by viewModel.showTripComputer.collectAsState()
    val showCustomization by viewModel.showCustomization.collectAsState()
    val showHUDMode by viewModel.showHUDMode.collectAsState()
    val showTrendGraph by viewModel.showTrendGraph.collectAsState()
    val showReadiness by viewModel.showReadiness.collectAsState()
    val showDiagnostics by viewModel.showDiagnostics.collectAsState()
    val showAlertSettings by viewModel.showAlertSettings.collectAsState()
    val showDataAnalysis by viewModel.showDataAnalysis.collectAsState()
    val remoteServerRunning by viewModel.remoteServerRunning.collectAsState()
    val remoteServerIp by viewModel.remoteServerIp.collectAsState()
    val remoteServerPort by viewModel.remoteServerPort.collectAsState()
    val remoteConnectedClients by viewModel.remoteConnectedClients.collectAsState()
    val tripData by viewModel.tripData.collectAsState()
    val connectionStats by viewModel.connectionStats.collectAsState()
    val autoReconnect by viewModel.autoReconnect.collectAsState()
    val lastError by viewModel.lastError.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val primaryGaugeIds by viewModel.primaryGaugeIds.collectAsState()
    val pollMode by viewModel.pollMode.collectAsState()
    val isGPSTracking by viewModel.isGPSTracking.collectAsState()
    val currentTrip by viewModel.currentTrip.collectAsState()
    val trendHistory by viewModel.trendHistory.collectAsState()
    val tripHistoryEntities by viewModel.tripHistoryEntities.collectAsState()
    val readinessMonitor by viewModel.readinessMonitor.collectAsState()
    val detectedProtocol by viewModel.detectedProtocol.collectAsState()
    val showFuelEconomy by viewModel.showFuelEconomy.collectAsState()
    val showMaintenance by viewModel.showMaintenance.collectAsState()
    val showPerformanceTest by viewModel.showPerformanceTest.collectAsState()
    val showTripHistory by viewModel.showTripHistory.collectAsState()
    val maintenanceItems by viewModel.maintenanceItems.collectAsState()
    val currentKm by viewModel.currentKm.collectAsState()
    val fuelEconomyData by viewModel.fuelEconomyData.collectAsState()
    val performanceTestState by viewModel.performanceTestState.collectAsState()
    val showPowerCalculator by viewModel.showPowerCalculator.collectAsState()
    val showDriveScore by viewModel.showDriveScore.collectAsState()
    val showShiftLight by viewModel.showShiftLight.collectAsState()
    val showVehicleInfo by viewModel.showVehicleInfo.collectAsState()
    val showKnownIssues by viewModel.showKnownIssues.collectAsState()
    val powerCalculation by viewModel.powerCalculation.collectAsState()
    val driveScore by viewModel.driveScore.collectAsState()
    val driveSession by viewModel.driveSession.collectAsState()
    val shiftLightConfig by viewModel.shiftLightConfig.collectAsState()
    val supportedPIDs by viewModel.supportedPIDs.collectAsState()
    val freezeFrames by viewModel.freezeFrames.collectAsState()
    val alertConfig by viewModel.alertConfig.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val importedData by viewModel.importedData.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val carProfile by viewModel.carProfile.collectAsState()
    val turboData by viewModel.turboData.collectAsState()
    val oilData by viewModel.oilData.collectAsState()
    val timingChainState by viewModel.timingChainState.collectAsState()
    val showTurboMonitor by viewModel.showTurboMonitor.collectAsState()
    val showTimingChainMonitor by viewModel.showTimingChainMonitor.collectAsState()
    val showCarProfile by viewModel.showCarProfile.collectAsState()
    val showTurboCooldown by viewModel.showTurboCooldown.collectAsState()
    val turboCooldownState by viewModel.turboCooldownState.collectAsState()
    val oilHealthPrediction by viewModel.oilHealthPrediction.collectAsState()
    val sensorValidationResult by viewModel.sensorValidationResult.collectAsState()
    val driveStyleResult by viewModel.driveStyleResult.collectAsState()
    val drivingEfficiencyResult by viewModel.drivingEfficiencyResult.collectAsState()
    val fuelSystemResult by viewModel.fuelSystemResult.collectAsState()
    val gearboxResult by viewModel.gearboxResult.collectAsState()
    val chainTensionerResult by viewModel.chainTensionerResult.collectAsState()
    val coolantResult by viewModel.coolantResult.collectAsState()
    val oilConditionResult by viewModel.oilConditionResult.collectAsState()
    val pcvResult by viewModel.pcvResult.collectAsState()
    val lambdaResult by viewModel.lambdaBalanceData.collectAsState()
    val fuelConsumption by viewModel.fuelConsumptionData.collectAsState()
    val egtResult by viewModel.egtResult.collectAsState()
    val sensorHealthSummary by viewModel.sensorHealthSummary.collectAsState()
    val turboSpoolResult by viewModel.turboSpoolResult.collectAsState()
    val turboEfficiencyResult by viewModel.turboEfficiencyResult.collectAsState()
    val boostLeakResult by viewModel.boostLeakResult.collectAsState()
    val wastegateResult by viewModel.wastegateResult.collectAsState()
    val batteryAnalysis by viewModel.batteryAnalysis.collectAsState()
    val egrAnalysis by viewModel.egrAnalysis.collectAsState()
    val evapAnalysis by viewModel.evapAnalysis.collectAsState()
    val saiAnalysis by viewModel.saiAnalysis.collectAsState()
    val emissionsReadiness by viewModel.emissionsReadiness.collectAsState()

    // Extended Feature State
    val showExtendedGearbox by viewModel.showExtendedGearbox.collectAsState()
    val showExtendedTurbo by viewModel.showExtendedTurbo.collectAsState()
    val showExtendedFuel by viewModel.showExtendedFuel.collectAsState()
    val showExtendedMaintenance by viewModel.showExtendedMaintenance.collectAsState()
    val showComfortControl by viewModel.showComfortControl.collectAsState()
    val showQuickActions by viewModel.showQuickActions.collectAsState()
    val showVehicleProfileManager by viewModel.showVehicleProfileManager.collectAsState()
    val showCodingDialog by viewModel.showCodingDialog.collectAsState()
    val codingInProgress by viewModel.codingInProgress.collectAsState()
    val codingResult by viewModel.codingResult.collectAsState()
    val emulatorMode by viewModel.emulatorMode.collectAsState()

    // TPMS & Climate State
    val showTPMSDialog by viewModel.showTPMSDialog.collectAsState()
    val showClimateControl by viewModel.showClimateControl.collectAsState()

    // Safety & ECO State
    val showSafetySystems by viewModel.showSafetySystems.collectAsState()
    val safetySummary by viewModel.safetySummary.collectAsState()
    val showEcoScoreDialog by viewModel.showEcoScoreDialog.collectAsState()
    val ecoScoreData by viewModel.ecoScoreData.collectAsState()
    val gpsSpeedForTest by viewModel.gpsSpeedForTest.collectAsState()
    val currentAccelerationRun by viewModel.currentAccelerationRun.collectAsState()
    val co2Data by viewModel.co2Data.collectAsState()
    val fuelCostData by viewModel.fuelCostData.collectAsState()
    val rangeEstimation by viewModel.rangeEstimation.collectAsState()
    val drivingStyleAnalysis by viewModel.drivingStyleAnalysis.collectAsState()
    val ecoTips by viewModel.ecoTips.collectAsState()
    var csvShareContent by remember { mutableStateOf<String?>(null) }
    val activityContext = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(csvShareContent) {
        csvShareContent?.let { content ->
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(android.content.Intent.EXTRA_TEXT, content)
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                activityContext.startActivity(android.content.Intent.createChooser(intent, "Fahrthistorie exportieren"))
            } catch (e: android.content.ActivityNotFoundException) {
                android.util.Log.e("MainActivity", "No app to handle CSV share", e)
            }
            csvShareContent = null
        }
    }

    val update = availableUpdate
    if (showUpdateDialog && update != null) {
        UpdateDialog(
            update = update,
            onDismiss = viewModel::dismissUpdateDialog,
            onSkipVersion = viewModel::skipUpdateVersion
        )
    }

    if (showSafetySystems) {
        com.canopobd.ui.safety.SafetySystemsDialog(
            safetySummary = safetySummary,
            onDismiss = viewModel::dismissSafetySystems
        )
    }

    if (showEcoScoreDialog) {
        com.canopobd.ui.ecoscore.EcoScoreDialog(
            ecoScore = ecoScoreData,
            co2Data = co2Data,
            fuelCost = fuelCostData,
            rangeEstimation = rangeEstimation,
            efficiency = com.canopobd.data.model.FuelEfficiencyMetrics(),
            drivingStyle = drivingStyleAnalysis,
            tips = ecoTips,
            onDismiss = viewModel::dismissEcoScore,
            onSetFuelPrice = viewModel::setFuelPrice
        )
    }

    if (showTPMSDialog) {
        com.canopobd.ui.tpms.TPMSDialog(
            onTPMSReset = viewModel::onTPMSReset,
            onDismiss = viewModel::toggleTPMSDialog,
            isConnected = connectionState == com.canopobd.data.model.OBDConnectionState.Connected
        )
    }

    if (showClimateControl) {
        com.canopobd.ui.climate.ClimateControlDialog(
            initialState = viewModel.climateState.value,
            onCommand = viewModel::onSendClimateCommand,
            onDismiss = viewModel::toggleClimateControl,
            externalState = viewModel.climateState.value,
            onClimateStateChange = { newState ->
                viewModel.updateClimateState(newState)
            }
        )
    }

    DashboardScreen(
        connectionState = connectionState,
        obdData = obdData,
        devices = devices,
        showDevicePicker = showDevicePicker,
        dtcResponse = dtcResponse,
        recordingActive = recordingActive,
        recordedData = recordedData,
        pollRate = pollRate,
        measurementUnit = measurementUnit,
        showDTCDialog = showDTCDialog,
        showSettings = showSettings,
        showDataLog = showDataLog,
        showPIDScreen = showPIDScreen,
        showRemoteDialog = showRemoteDialog,
        showTripComputer = showTripComputer,
        showCustomization = showCustomization,
        showHUDMode = showHUDMode,
        showTrendGraph = showTrendGraph,
        showReadiness = showReadiness,
        showDiagnostics = showDiagnostics,
        showAlertSettings = showAlertSettings,
        showDataAnalysis = showDataAnalysis,
        showFuelEconomy = showFuelEconomy,
        showMaintenance = showMaintenance,
        showPerformanceTest = showPerformanceTest,
        showTripHistory = showTripHistory,
        showPowerCalculator = showPowerCalculator,
        showDriveScore = showDriveScore,
        showShiftLight = showShiftLight,
        showVehicleInfo = showVehicleInfo,
        showKnownIssues = showKnownIssues,
        maintenanceItems = maintenanceItems,
        currentKm = currentKm,
        fuelEconomyData = fuelEconomyData,
        performanceTestState = performanceTestState,
        powerCalculation = powerCalculation,
        driveScore = driveScore,
        driveSession = driveSession,
        shiftLightConfig = shiftLightConfig,
        remoteServerRunning = remoteServerRunning,
        remoteServerIp = remoteServerIp,
        remoteServerPort = remoteServerPort,
        remoteConnectedClients = remoteConnectedClients,
        tripData = tripData,
        connectionStats = connectionStats,
        autoReconnect = autoReconnect,
        errorMessage = lastError,
        colorTheme = colorTheme,
        primaryGaugeIds = primaryGaugeIds,
        pollMode = pollMode,
        isGPSTracking = isGPSTracking,
        currentTrip = currentTrip,
        trendHistory = trendHistory,
        readinessMonitor = readinessMonitor,
        detectedProtocol = detectedProtocol,
        supportedPIDs = supportedPIDs,
        freezeFrames = freezeFrames,
        alertConfig = alertConfig,
        activeAlerts = activeAlerts,
        importedData = importedData,
        onConnect = viewModel::connect,
        onDisconnect = viewModel::disconnect,
        onToggleDevicePicker = viewModel::toggleDevicePicker,
        onToggleDTCDialog = viewModel::toggleDTCDialog,
        onClearDTCs = viewModel::clearDTCs,
        onToggleSettings = viewModel::toggleSettings,
        onToggleDataLog = viewModel::toggleDataLog,
        onTogglePIDScreen = viewModel::togglePIDScreen,
        onToggleRemoteDialog = viewModel::toggleRemoteDialog,
        onToggleTripComputer = viewModel::toggleTripComputer,
        onToggleCustomization = viewModel::toggleCustomization,
        onToggleHUDMode = viewModel::toggleHUDMode,
        onToggleTrendGraph = viewModel::toggleTrendGraph,
        onToggleReadiness = viewModel::toggleReadiness,
        onToggleDiagnostics = viewModel::toggleDiagnostics,
        onToggleAlertSettings = viewModel::toggleAlertSettings,
        onToggleDataAnalysis = viewModel::toggleDataAnalysis,
        onStartRemoteServer = viewModel::startRemoteServer,
        onStopRemoteServer = viewModel::stopRemoteServer,
        onStartRecording = viewModel::startRecording,
        onStopRecording = viewModel::stopRecording,
        onSetPollRate = viewModel::setPollRate,
        onSetMeasurementUnit = viewModel::setMeasurementUnit,
        onSetAutoReconnect = viewModel::setAutoReconnect,
        onSetPollMode = viewModel::setPollMode,
        onResetTrip = viewModel::resetTrip,
        onGetStoredVin = viewModel::getStoredVin,
        onGetExportData = viewModel::getExportData,
        onClearRecordedData = viewModel::clearRecordedData,
        onSetColorTheme = viewModel::setColorTheme,
        onSetPrimaryGauges = viewModel::setPrimaryGauges,
        onStartGPSTracking = viewModel::startGPSTracking,
        onStopGPSTracking = viewModel::stopGPSTracking,
        onExportGPX = viewModel::exportTripToGPX,
        onExportKML = viewModel::exportTripToKML,
        onClearGPSTrips = viewModel::clearGPSTripHistory,
        onSetAlertConfig = viewModel::setAlertConfig,
        onImportCsv = viewModel::importCsvData,
        onClearImported = viewModel::clearImportedData,
        onGetFuelTrimAnalysis = viewModel::getFuelTrimAnalysis,
        onToggleFuelEconomy = viewModel::toggleFuelEconomy,
        onToggleMaintenance = viewModel::toggleMaintenance,
        onTogglePerformanceTest = viewModel::togglePerformanceTest,
        onToggleTripHistory = viewModel::toggleTripHistory,
        onSetMaintenanceItem = viewModel::setMaintenanceItem,
        onResetMaintenanceItem = viewModel::resetMaintenanceItem,
        onStartPerfTest = viewModel::startPerformanceTest,
        onStopPerfTest = viewModel::stopPerformanceTest,
        onClearTripHistory = viewModel::clearGPSTripHistory,
        tripHistoryEntities = tripHistoryEntities,
        onDeleteTrip = viewModel::deleteTrip,
        onShareTripCsv = {
            viewModel.exportTripHistoryToCsv { csv -> csvShareContent = csv }
        },
        onTogglePowerCalculator = viewModel::togglePowerCalculator,
        onToggleDriveScore = viewModel::toggleDriveScore,
        onToggleShiftLight = viewModel::toggleShiftLight,
        onToggleVehicleInfo = viewModel::toggleVehicleInfo,
        onToggleKnownIssues = viewModel::toggleKnownIssues,
        onUpdateShiftLightConfig = viewModel::updateShiftLightConfig,
        onResetDriveScore = viewModel::resetDriveScore,
        oilHealthPrediction = oilHealthPrediction,
        sensorValidationResult = sensorValidationResult,
        driveStyleResult = driveStyleResult,
        drivingEfficiencyResult = drivingEfficiencyResult,
        fuelSystemResult = fuelSystemResult,
        gearboxResult = gearboxResult,
        chainTensionerResult = chainTensionerResult,
        coolantResult = coolantResult,
        oilConditionResult = oilConditionResult,
        pcvResult = pcvResult,
        lambdaResult = lambdaResult,
        fuelConsumption = fuelConsumption,
        egtResult = egtResult,
        sensorHealthSummary = sensorHealthSummary,
        turboSpoolResult = turboSpoolResult,
        turboEfficiencyResult = turboEfficiencyResult,
        boostLeakResult = boostLeakResult,
        wastegateResult = wastegateResult,
        batteryAnalysis = batteryAnalysis,
        egrAnalysis = egrAnalysis,
        evapAnalysis = evapAnalysis,
        saiAnalysis = saiAnalysis,
        emissionsReadiness = emissionsReadiness,
        onToggleTurboMonitor = viewModel::toggleTurboMonitor,
        onToggleTimingChainMonitor = viewModel::toggleTimingChainMonitor,
        _onToggleCarProfile = viewModel::toggleCarProfile,
        onToggleTurboCooldown = viewModel::toggleTurboCooldown,
        onSelectCarProfile = viewModel::selectCarProfile,
        showExtendedGearbox = showExtendedGearbox,
        showExtendedTurbo = showExtendedTurbo,
        showExtendedFuel = showExtendedFuel,
        showExtendedMaintenance = showExtendedMaintenance,
        showComfortControl = showComfortControl,
        onToggleExtendedGearbox = viewModel::toggleExtendedGearbox,
        showQuickActions = showQuickActions,
        showVehicleProfileManager = showVehicleProfileManager,
        onToggleQuickActions = viewModel::toggleQuickActions,
        onToggleVehicleProfileManager = viewModel::toggleVehicleProfileManager,
        onExecuteQuickAction = viewModel::executeQuickAction,
        onLoadProfile = { _ -> },
        currentVehicleProfile = null,
        onToggleExtendedTurbo = viewModel::toggleExtendedTurbo,
        onToggleExtendedFuel = viewModel::toggleExtendedFuel,
        onToggleExtendedMaintenance = viewModel::toggleExtendedMaintenance,
        onToggleComfortControl = viewModel::toggleComfortControl,
        onSendBCMCommand = viewModel::onSendBCMCommand,
        showCodingDialog = showCodingDialog,
        codingInProgress = codingInProgress,
        codingResult = codingResult,
        onToggleCodingDialog = viewModel::toggleCodingDialog,
        onApplyCodingOption = viewModel::applyCodingOption,
        onClearCodingResult = viewModel::clearCodingResult,
        showTPMSDialog = showTPMSDialog,
        onToggleTPMSDialog = viewModel::toggleTPMSDialog,
        onTPMSReset = viewModel::onTPMSReset,
        tpmsData = viewModel.tpmsData.value,
        showClimateControl = showClimateControl,
        onToggleClimateControl = viewModel::toggleClimateControl,
        onSendClimateCommand = viewModel::onSendClimateCommand,
        appThemeMode = appThemeMode,
        onSetAppThemeMode = viewModel::setAppThemeMode,
        emulatorMode = emulatorMode,
        onSetEmulatorMode = viewModel::setEmulatorMode,
        carProfile = carProfile,
        turboData = turboData,
        oilData = oilData,
        timingChainState = timingChainState,
        showTurboMonitor = showTurboMonitor,
        showTimingChainMonitor = showTimingChainMonitor,
        showCarProfile = showCarProfile,
        _showTurboCooldown = showTurboCooldown,
        turboCooldownState = turboCooldownState,
        tcmReading = viewModel.tcmReading.value,
        ecmReading = viewModel.ecmReading.value,
        safetySummary = safetySummary,
        ecoScoreData = ecoScoreData,
        gpsSpeedKmh = gpsSpeedForTest,
        accelerationRun = currentAccelerationRun,
        onToggleSafetySystems = viewModel::toggleSafetySystems,
        onToggleEcoScore = viewModel::toggleEcoScore
    )
}
