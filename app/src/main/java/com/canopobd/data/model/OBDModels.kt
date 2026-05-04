package com.canopobd.data.model

enum class OBDPID(
    val code: String,
    val displayName: String,
    val unit: String,
    val byteCount: Int,
    val formula: (ByteArray) -> Double
) {
    RPM("010C", "Engine RPM", "rpm", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 4.0 else 0.0
    }),
    SPEED("010D", "Vehicle Speed", "km/h", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    COOLANT_TEMP("0105", "Coolant Temperature", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    INTAKE_TEMP("010F", "Intake Air Temperature", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    THROTTLE("0111", "Throttle Position", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    ENGINE_LOAD("0104", "Engine Load", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    FUEL_LEVEL("012F", "Fuel Tank Level", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    BATTERY_VOLTAGE("ATRV", "Battery Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 10.0 else 0.0
    }),
    TIMING_ADVANCE("010E", "Timing Advance", "°", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 2.0 - 64.0 else 0.0
    }),
    MAF_RATE("0110", "MAF Air Flow Rate", "g/s", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 100.0 else 0.0
    }),
    FUEL_PRESSURE("010A", "Fuel Pressure", "kPa", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 3.0 else 0.0
    }),
    INTAKE_PRESSURE("010B", "Intake Manifold Pressure", "kPa", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    RUN_TIME("011F", "Engine Run Time", "s", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    DISTANCE_MIL("0121", "Distance with MIL", "km", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    FUEL_RAIL_PRESSURE("012A", "Fuel Rail Pressure", "kPa", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) * 0.079 else 0.0
    }),
    COMMANDED_EGR("012C", "Commanded EGR", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    EGR_TEMP("012D", "EGR Temperature", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    COMMANDED_EVAPORATIVE_PURGE("012E", "Commanded Evap Purge", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    FUEL_TANK_LEVEL_INPUT("0130", "Fuel Tank Level Input", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    WARMUPS_SINCE_DTC_CLEAR("0131", "Warmups since DTC Clear", "", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    DISTANCE_SINCE_DTC_CLEAR("0132", "Distance since DTC Clear", "km", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 4.0 else 0.0
    }),
    BAROMETRIC_PRESSURE("0133", "Barometric Pressure", "kPa", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    CATALYST_TEMP_B1S1("013C", "Catalyst Temp B1S1", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    O2_VOLTAGE_B1S1("0114", "O2 Sensor B1S1 Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 200.0 else 0.0
    }),
    O2_VOLTAGE_B1S2("0115", "O2 Sensor B1S2 Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 200.0 else 0.0
    }),
    CONTROL_MODULE_VOLTAGE("0142", "Control Module Voltage", "V", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 1000.0 else 0.0
    }),
    ABSOLUTE_LOAD_VALUE("0143", "Absolute Load Value", "%", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) * 100.0 / 255.0 else 0.0
    }),
    ENGINE_FUEL_RATE("015E", "Engine Fuel Rate", "L/h", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 20.0 else 0.0
    });

    companion object {
        fun fromCode(code: String): OBDPID? = entries.find { it.code == code }
    }
}

data class OBDData(
    val rpm: Double = 0.0,
    val speed: Double = 0.0,
    val coolantTemp: Double = 0.0,
    val intakeTemp: Double = 0.0,
    val throttle: Double = 0.0,
    val engineLoad: Double = 0.0,
    val fuelLevel: Double = 0.0,
    val batteryVoltage: Double = 0.0,
    val timingAdvance: Double = 0.0,
    val mafRate: Double = 0.0,
    val fuelPressure: Double = 0.0,
    val intakePressure: Double = 0.0,
    val runTime: Double = 0.0,
    val fuelRailPressure: Double = 0.0,
    val commandedEGR: Double = 0.0,
    val egrTemp: Double = 0.0,
    val commandedEvapPurge: Double = 0.0,
    val barometricPressure: Double = 0.0,
    val o2VoltageB1S1: Double = 0.0,
    val o2VoltageB1S2: Double = 0.0,
    val catalystTemp: Double = 0.0,
    val controlModuleVoltage: Double = 0.0,
    val absoluteLoadValue: Double = 0.0,
    val engineFuelRate: Double = 0.0,
    val vin: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class DiagnosticTroubleCode(
    val code: String,
    val description: String,
    val pending: Boolean = false
)

data class DTCResponse(
    val codes: List<DiagnosticTroubleCode>,
    val pendingCodes: List<DiagnosticTroubleCode> = emptyList()
)

data class FreezeFrame(
    val dtc: DiagnosticTroubleCode,
    val data: Map<String, Double>
)

data class DataRecord(
    val timestamp: Long,
    val rpm: Double,
    val speed: Double,
    val coolantTemp: Double,
    val throttle: Double,
    val fuelLevel: Double,
    val batteryVoltage: Double
)

data class TripData(
    val startTime: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0L,
    val distanceKm: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxRpm: Double = 0.0,
    val avgRpm: Double = 0.0,
    val sampleCount: Long = 0L,
    val totalFuelUsed: Double = 0.0,
    val avgFuelRate: Double = 0.0,
    val fuelStartLevel: Double = 0.0,
    val fuelEndLevel: Double = 0.0,
    val vin: String = ""
)

enum class ConnectionQuality(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor");

    companion object {
        fun fromSuccessRate(rate: Double): ConnectionQuality = when {
            rate >= 0.9 -> EXCELLENT
            rate >= 0.7 -> GOOD
            rate >= 0.5 -> FAIR
            else -> POOR
        }
    }
}

data class ConnectionStats(
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val quality: ConnectionQuality = ConnectionQuality.EXCELLENT
) {
    val totalCount: Int get() = successCount + failureCount
    val successRate: Double get() = if (totalCount == 0) 1.0 else successCount.toDouble() / totalCount
}

enum class MeasurementUnit(val label: String, val speedFactor: Double, val speedUnit: String, val tempFactor: Double, val tempOffset: Double, val tempUnit: String) {
    METRIC("Metric", 1.0, "km/h", 1.0, 0.0, "°C"),
    IMPERIAL("Imperial", 0.621371, "mph", 1.8, 32.0, "°F");

    fun convertSpeed(kmh: Double): Double = kmh * speedFactor
    fun convertTemp(celsius: Double): Double = celsius * tempFactor + tempOffset
    fun convertDistance(km: Double): Double = km * speedFactor
    fun speedLabel(): String = speedUnit
    fun tempLabel(): String = tempUnit
    fun distanceUnit(): String = if (this == METRIC) "km" else "mi"
}

sealed class OBDConnectionState {
    object Disconnected : OBDConnectionState()
    object Connecting : OBDConnectionState()
    object Connected : OBDConnectionState()
    data class Error(val message: String) : OBDConnectionState()
}

data class BluetoothDeviceInfo(
    val name: String,
    val address: String
)
