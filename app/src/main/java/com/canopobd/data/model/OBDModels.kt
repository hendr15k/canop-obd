package com.canopobd.data.model

/**
 * OBD-II PID definitions
 * Standard PIDs from SAE J1979
 */
enum class OBDPID(val code: String, val name: String, val unit: String, val bytes: Int, val formula: (ByteArray) -> Double) {
    RPM("010C", "Engine RPM", "rpm", 2, { b ->
        if (b.size >= 2) ((b[0] * 256 + b[1]) / 4.0) else 0.0
    }),
    SPEED("010D", "Vehicle Speed", "km/h", 1, { b ->
        if (b.isNotEmpty()) b[0].toDouble() else 0.0
    }),
    COOLANT_TEMP("0105", "Coolant Temperature", "°C", 1, { b ->
        if (b.isNotEmpty()) (b[0] - 40.0) else 0.0
    }),
    INTAKE_TEMP("010F", "Intake Air Temperature", "°C", 1, { b ->
        if (b.isNotEmpty()) (b[0] - 40.0) else 0.0
    }),
    THROTTLE("0111", "Throttle Position", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0] * 100.0 / 255.0) else 0.0
    }),
    ENGINE_LOAD("0104", "Engine Load", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0] * 100.0 / 255.0) else 0.0
    }),
    FUEL_LEVEL("012F", "Fuel Tank Level", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0] * 100.0 / 255.0) else 0.0
    }),
    BATTERY_VOLTAGE("ATRV", "Battery Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) b[0].toDouble() / 10.0 else 0.0
    });

    companion object {
        fun fromCode(code: String): OBDPID? = entries.find { it.code == code }
    }
}

/**
 * Live OBD data snapshot
 */
data class OBDData(
    val rpm: Double = 0.0,
    val speed: Double = 0.0,
    val coolantTemp: Double = 0.0,
    val intakeTemp: Double = 0.0,
    val throttle: Double = 0.0,
    val engineLoad: Double = 0.0,
    val fuelLevel: Double = 0.0,
    val batteryVoltage: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Connection state for OBD adapter
 */
sealed class OBDConnectionState {
    object Disconnected : OBDConnectionState()
    object Connecting : OBDConnectionState()
    object Connected : OBDConnectionState()
    data class Error(val message: String) : OBDConnectionState()
}

/**
 * Bluetooth device info
 */
data class BluetoothDeviceInfo(
    val name: String,
    val address: String
)
