package com.canopobd.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.canopobd.data.model.BluetoothDeviceInfo
import com.canopobd.data.model.OBDConnectionState
import com.canopobd.data.model.OBDData
import com.canopobd.data.repository.OBDRepository
import kotlinx.coroutines.flow.*

@SuppressLint("MissingPermission")
class DashboardViewModel(
    context: Context
) : ViewModel() {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val repository = OBDRepository(bluetoothManager?.adapter)

    val connectionState: StateFlow<OBDConnectionState> = repository.connectionState
    val obdData: StateFlow<OBDData> = repository.obdData

    private val _devices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val devices: StateFlow<List<BluetoothDeviceInfo>> = _devices.asStateFlow()

    private val _showDevicePicker = MutableStateFlow(false)
    val showDevicePicker: StateFlow<Boolean> = _showDevicePicker.asStateFlow()

    init {
        refreshDevices()
    }

    fun refreshDevices() {
        _devices.value = repository.getPairedDevices()
    }

    fun connect(deviceAddress: String) {
        _showDevicePicker.value = false
        repository.connect(deviceAddress)
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun toggleDevicePicker() {
        _showDevicePicker.value = !_showDevicePicker.value
        if (_showDevicePicker.value) refreshDevices()
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(context.applicationContext) as T
            }
        }
    }
}
