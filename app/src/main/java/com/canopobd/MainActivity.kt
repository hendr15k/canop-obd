package com.canopobd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.canopobd.R
import com.canopobd.data.model.ColorTheme
import com.canopobd.ui.dashboard.DashboardScreen
import com.canopobd.ui.theme.*
import com.canopobd.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(this, DashboardViewModel.Factory(application))[DashboardViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val colorTheme by viewModel.colorTheme.collectAsState()
            val appColors = remember(colorTheme) { colorTheme.toAppColors() }

            CanopObdTheme(appColors = appColors) {
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
    val readinessMonitor by viewModel.readinessMonitor.collectAsState()
    val detectedProtocol by viewModel.detectedProtocol.collectAsState()
    val supportedPIDs by viewModel.supportedPIDs.collectAsState()
    val freezeFrames by viewModel.freezeFrames.collectAsState()
    val alertConfig by viewModel.alertConfig.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val importedData by viewModel.importedData.collectAsState()

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
        onGetFuelTrimAnalysis = viewModel::getFuelTrimAnalysis
    )
}
