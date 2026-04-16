package com.canopobd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
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
                    DashboardScreen(
                        connectionState = viewModel.connectionState,
                        obdData = viewModel.obdData,
                        devices = viewModel.devices,
                        showDevicePicker = viewModel.showDevicePicker,
                        onConnect = viewModel::connect,
                        onDisconnect = viewModel::disconnect,
                        onToggleDevicePicker = viewModel::toggleDevicePicker
                    )
                }
            }
        }
    }
}
