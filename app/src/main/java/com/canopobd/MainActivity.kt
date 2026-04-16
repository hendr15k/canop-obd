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
import com.canopobd.data.model.BluetoothDeviceInfo
import com.canopobd.data.model.OBDConnectionState
import com.canopobd.data.model.OBDData
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

                    DashboardScreen(
                        connectionState = connectionState,
                        obdData = obdData,
                        devices = devices,
                        showDevicePicker = showDevicePicker,
                        onConnect = viewModel::connect,
                        onDisconnect = viewModel::disconnect,
                        onToggleDevicePicker = viewModel::toggleDevicePicker
                    )
                }
            }
        }
    }
}
