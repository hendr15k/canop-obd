package com.canopobd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.ui.dashboard.DashboardScreen
import com.canopobd.ui.theme.CanopObdTheme
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
            }
        }
    }
}
