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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.canopobd.R
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
            CanopObdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.permissions_title),
                fontSize = 18.sp,
                color = canopoHighlight
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.permissions_message),
                fontSize = 14.sp,
                color = textSecondary,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onRequest) {
                Text(stringResource(R.string.permissions_grant), color = canopoAccent, fontSize = 16.sp)
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
    val remoteServerRunning by viewModel.remoteServerRunning.collectAsState()
    val remoteServerIp by viewModel.remoteServerIp.collectAsState()
    val remoteServerPort by viewModel.remoteServerPort.collectAsState()
    val remoteConnectedClients by viewModel.remoteConnectedClients.collectAsState()

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
        remoteServerRunning = remoteServerRunning,
        remoteServerIp = remoteServerIp,
        remoteServerPort = remoteServerPort,
        remoteConnectedClients = remoteConnectedClients,
        onConnect = viewModel::connect,
        onDisconnect = viewModel::disconnect,
        onToggleDevicePicker = viewModel::toggleDevicePicker,
        onToggleDTCDialog = viewModel::toggleDTCDialog,
        onClearDTCs = viewModel::clearDTCs,
        onToggleSettings = viewModel::toggleSettings,
        onToggleDataLog = viewModel::toggleDataLog,
        onTogglePIDScreen = viewModel::togglePIDScreen,
        onToggleRemoteDialog = viewModel::toggleRemoteDialog,
        onStartRemoteServer = viewModel::startRemoteServer,
        onStopRemoteServer = viewModel::stopRemoteServer,
        onStartRecording = viewModel::startRecording,
        onStopRecording = viewModel::stopRecording,
        onSetPollRate = viewModel::setPollRate,
        onSetMeasurementUnit = viewModel::setMeasurementUnit,
        onGetExportData = viewModel::getExportData,
        onClearRecordedData = viewModel::clearRecordedData
    )
}
