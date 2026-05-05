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
    }),
    SHORT_TERM_FUEL_TRIM_BANK1("0161", "STFT Bank 1", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0
    }),
    LONG_TERM_FUEL_TRIM_BANK1("0162", "LTFT Bank 1", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0
    }),
    SHORT_TERM_FUEL_TRIM_BANK2("0163", "STFT Bank 2", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0
    }),
    LONG_TERM_FUEL_TRIM_BANK2("0164", "LTFT Bank 2", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0
    }),
    FUEL_AIR_EQUIV_RATIO("0144", "Fuel Air Equiv Ratio", "", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 32768.0 else 0.0
    }),
    ABSOLUTE_THROTTLE_B("014D", "Throttle B", "%", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) * 100.0 / 255.0 else 0.0
    }),
    TIME_RUN_WITH_MIL("014E", "Time Run MIL On", "min", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    ACCELERATOR_POS_D("0151", "Accelerator Pedal D", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    THROTTLE_C("015D", "Throttle C", "%", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) * 100.0 / 255.0 else 0.0
    }),
    THROTTLE_ACTUATOR("015C", "Throttle Actuator", "%", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) * 100.0 / 255.0 else 0.0
    }),
    HYBRID_BATTERY_REMAINING("015B", "Hybrid Battery Remaining", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    BOOST_PRESSURE("0170", "Boost Pressure", "kPa", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 0.03125 else 0.0
    }),
    VGT_CONTROL("0171", "VGT Control", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    WASTEGATE_CONTROL("0172", "Wastegate Control", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    EXHAUST_PRESSURE("0173", "Exhaust Pressure", "kPa", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    TURBO_RPM("0174", "Turbo RPM", "rpm", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    CHARGE_AIR_COOLER_TEMP("0177", "Charge Air Cooler Temp", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    EGT_BANK1("0178", "Exhaust Gas Temp B1", "°C", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    EGT_BANK2("0179", "Exhaust Gas Temp B2", "°C", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    FUEL_SYSTEM_STATUS("0103", "Fuel System Status", "", 2, { b ->
        if (b.size >= 2) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    ACTUAL_TORQUE("0162", "Actual Torque", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 125).toDouble() else 0.0
    }),
    DEMAND_TORQUE("0161", "Driver Demand Torque", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 125).toDouble() else 0.0
    }),
    REFERENCE_TORQUE("0163", "Reference Torque", "Nm", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    ETHANOL_FUEL_PERCENT("0152", "Ethanol Fuel %", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    OIL_TEMP("015C", "Engine Oil Temperature", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    TURBO_BOOST_VACUUM("0175", "Turbo Boost Vacuum", "kPa", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 125).toDouble() else 0.0
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
    val shortTermFuelTrimB1: Double = 0.0,
    val longTermFuelTrimB1: Double = 0.0,
    val shortTermFuelTrimB2: Double = 0.0,
    val longTermFuelTrimB2: Double = 0.0,
    val fuelAirRatio: Double = 0.0,
    val acceleratorPosD: Double = 0.0,
    val throttleC: Double = 0.0,
    val throttleActuator: Double = 0.0,
    val hybridBatteryRemaining: Double = 0.0,
    val vin: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val boostPressure: Double = 0.0,
    val vgtControl: Double = 0.0,
    val wastegateControl: Double = 0.0,
    val exhaustPressure: Double = 0.0,
    val turboRpm: Double = 0.0,
    val chargeAirCoolerTemp: Double = 0.0,
    val egtBank1: Double = 0.0,
    val egtBank2: Double = 0.0,
    val fuelSystemStatus: Double = 0.0,
    val actualTorque: Double = 0.0,
    val demandTorque: Double = 0.0,
    val referenceTorque: Double = 0.0,
    val ethanolPercent: Double = 0.0,
    val oilTemp: Double = 0.0,
    val turboBoostVacuum: Double = 0.0
)

data class GaugeConfig(
    val id: String,
    val label: String,
    val unit: String,
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val color: Long = 0xFF00FF88,
    val visible: Boolean = true,
    val position: Int = 0,
    val isPrimary: Boolean = false
)

enum class ColorTheme(val displayName: String, val primaryColor: Long, val accentColor: Long, val surfaceColor: Long, val gaugeGreen: Long, val gaugeYellow: Long, val gaugeOrange: Long, val gaugeRed: Long) {
    CANOPO("Canopo Dark", 0xFF7B2FFF, 0xFF7B2FFF, 0xFF16213E, 0xFF00FF88, 0xFFFFE066, 0xFFFF8C00, 0xFFFF4444),
    BLUE_STEEL("Blue Steel", 0xFF1E88E5, 0xFF42A5F5, 0xFF0D1B2A, 0xFF4CAF50, 0xFFFFEB3B, 0xFFFF9800, 0xFFF44336),
    AMBER("Amber", 0xFFFFB300, 0xFFFFD54F, 0xFF1A1A1A, 0xFF69F0AE, 0xFFFFE082, 0xFFFFAB40, 0xFFFF5252),
    NEON("Neon", 0xFF00E5FF, 0xFF18FFFF, 0xFF0A0A0A, 0xFF00E676, 0xFFFFEA00, 0xFFFF9100, 0xFFFF1744);

    companion object {
        fun fromName(name: String): ColorTheme = entries.find { it.name == name } ?: CANOPO
    }
}

data class DashboardLayout(
    val name: String,
    val theme: ColorTheme,
    val gauges: List<GaugeConfig>
)

data class GPSLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val timestamp: Long
)

data class GPSTrip(
    val id: String,
    val startTime: Long,
    val endTime: Long = 0L,
    val locations: List<GPSLocation> = emptyList(),
    val distanceKm: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0
)

data class TrendPoint(
    val timestamp: Long,
    val value: Float
)

data class TrendHistory(
    val rpm: List<TrendPoint> = emptyList(),
    val speed: List<TrendPoint> = emptyList(),
    val coolantTemp: List<TrendPoint> = emptyList()
) {
    companion object {
        const val MAX_POINTS = 60
    }
}

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
    UNKNOWN("Unknown"),
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

enum class PollMode(val label: String, val pollInterval: Long) {
    FAST("Fast (50ms)", 50L),
    NORMAL("Normal (500ms)", 500L),
    ECO("Eco (2000ms)", 2000L)
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

data class ReadinessMonitor(
    val misfire: Boolean = false,
    val fuelSystem: Boolean = false,
    val comprehensiveComponent: Boolean = false,
    val catalyst: Boolean = false,
    val heatedCatalyst: Boolean = false,
    val evapSystem: Boolean = false,
    val secondaryAirSystem: Boolean = false,
    val acSystemRefrigerant: Boolean = false,
    val oxygenSensor: Boolean = false,
    val oxygenSensorHeater: Boolean = false,
    val egrSystem: Boolean = false
) {
    val allComplete: Boolean get() = misfire && fuelSystem && comprehensiveComponent &&
            catalyst && heatedCatalyst && evapSystem && secondaryAirSystem &&
            acSystemRefrigerant && oxygenSensor && oxygenSensorHeater && egrSystem

    val completedCount: Int get() = listOf(
        misfire, fuelSystem, comprehensiveComponent, catalyst, heatedCatalyst,
        evapSystem, secondaryAirSystem, acSystemRefrigerant, oxygenSensor,
        oxygenSensorHeater, egrSystem
    ).count { it }

    val totalCount: Int get() = 11
}

data class OBDProtocol(
    val id: Int,
    val name: String,
    val description: String
)

val OBD_PROTOCOLS = listOf(
    OBDProtocol(0, "AUTO", "Automatic"),
    OBDProtocol(1, "SAE J1850 PWM", "PWM 41.6 kbaud"),
    OBDProtocol(2, "SAE J1850 VPW", "VPW 10.4 kbaud"),
    OBDProtocol(3, "ISO 9141-2", "5 baud init"),
    OBDProtocol(4, "ISO 14230-4 KWP", "5 baud init"),
    OBDProtocol(5, "ISO 14230-4 KWP", "Fast init"),
    OBDProtocol(6, "ISO 15765-4 CAN", "11bit 500k"),
    OBDProtocol(7, "ISO 15765-4 CAN", "29bit 500k"),
    OBDProtocol(8, "ISO 15765-4 CAN", "11bit 250k"),
    OBDProtocol(9, "ISO 15765-4 CAN", "29bit 250k"),
    OBDProtocol(10, "SAE J1939 CAN", "29bit 250k"),
    OBDProtocol(11, "USER1 CAN", "11bit 125k"),
    OBDProtocol(12, "USER2 CAN", "11bit 50k")
)

data class AlertConfig(
    val speedWarning: Float = 130f,
    val speedWarningEnabled: Boolean = false,
    val coolantWarning: Float = 105f,
    val coolantWarningEnabled: Boolean = true,
    val fuelWarning: Float = 15f,
    val fuelWarningEnabled: Boolean = true,
    val rpmWarning: Float = 6000f,
    val rpmWarningEnabled: Boolean = false,
    val batteryLowWarning: Float = 11.5f,
    val batteryLowWarningEnabled: Boolean = true
)

data class ActiveAlert(
    val type: AlertType,
    val value: Float,
    val threshold: Float,
    val message: String
)

enum class AlertType(val label: String) {
    SPEED("Geschwindigkeit"),
    COOLANT("Kühlmitteltemperatur"),
    FUEL("Kraftstoff"),
    RPM("Drehzahl"),
    BATTERY("Batterie")
}

data class CsvImportEntry(
    val timestamp: Long,
    val rpm: Double,
    val speed: Double,
    val coolantTemp: Double,
    val throttle: Double,
    val fuelLevel: Double,
    val batteryVoltage: Double
)

data class FuelTrimAnalysis(
    val stftB1: Double,
    val ltftB1: Double,
    val stftB2: Double,
    val ltftB2: Double,
    val totalTrimB1: Double,
    val totalTrimB2: Double
) {
    val statusB1: String get() = when {
        totalTrimB1 > 10.0 -> "Mager (Lean)"
        totalTrimB1 < -10.0 -> "Fett (Rich)"
        else -> "OK"
    }
    val statusB2: String get() = when {
        totalTrimB2 > 10.0 -> "Mager (Lean)"
        totalTrimB2 < -10.0 -> "Fett (Rich)"
        else -> "OK"
    }
}

data class FuelEconomyData(
    val currentL100km: Double = 0.0,
    val avgL100km: Double = 0.0,
    val currentKmL: Double = 0.0,
    val avgKmL: Double = 0.0,
    val currentMpgUs: Double = 0.0,
    val avgMpgUs: Double = 0.0,
    val currentMpgUk: Double = 0.0,
    val avgMpgUk: Double = 0.0,
    val estimatedFromMaf: Boolean = false
) {
    companion object {
        fun fromL100km(l100km: Double): FuelEconomyData {
            if (l100km <= 0.0 || l100km.isNaN() || l100km.isInfinite() || l100km < 0.5 || l100km > 100.0) {
                return FuelEconomyData()
            }
            val kmL = 100.0 / l100km
            val mpgUs = 235.214583 / l100km
            val mpgUk = 282.4809363 / l100km
            return FuelEconomyData(
                currentL100km = l100km,
                avgL100km = l100km,
                currentKmL = kmL,
                avgKmL = kmL,
                currentMpgUs = mpgUs,
                avgMpgUs = mpgUs,
                currentMpgUk = mpgUk,
                avgMpgUk = mpgUk,
                estimatedFromMaf = true
            )
        }
    }
}

data class MaintenanceItem(
    val type: MaintenanceType,
    val lastServiceKm: Int = 0,
    val intervalKm: Int = 15000,
    val lastServiceDate: Long = 0L,
    val currentKm: Int = 0
) {
    val kmRemaining: Int get() = (lastServiceKm + intervalKm) - currentKm
    val status: MaintenanceStatus get() = when {
        kmRemaining < 0 -> MaintenanceStatus.OVERDUE
        kmRemaining < intervalKm * 0.1 -> MaintenanceStatus.DUE_SOON
        else -> MaintenanceStatus.OK
    }
}

enum class MaintenanceType(val label: String, val defaultInterval: Int) {
    OIL_CHANGE("Ölwechsel", 15000),
    TIRES("Reifen", 30000),
    INSPECTION("TÜV / AU", 60000),
    BRAKE_PADS("Bremsbeläge", 20000),
    AIR_FILTER("Luftfilter", 30000),
    TRANSMISSION_FLUID("Getriebeöl", 60000),
    TURBO_INSPECTION("Turbolader-Inspektion", 60000),
    COOLANT("Kühlmittel", 60000),
    SPARK_PLUGS("Zündkerzen", 30000),
    TURBO_BOOST_CHECK("Ladedruck prüfen", 45000)
}

enum class MaintenanceStatus {
    OK, DUE_SOON, OVERDUE
}

data class PerformanceResult(
    val timestamp: Long = System.currentTimeMillis(),
    val testType: PerformanceTestType,
    val timeSeconds: Double = 0.0,
    val valid: Boolean = false
)

enum class PerformanceTestType(val label: String, val startSpeedKmh: Double, val endSpeedKmh: Double) {
    ZERO_100("0–100 km/h", 0.0, 100.0),
    ZERO_200("0–200 km/h", 0.0, 200.0),
    HUNDRED_200("100–200 km/h", 100.0, 200.0)
}

data class PerformanceTestState(
    val isRunning: Boolean = false,
    val currentTestType: PerformanceTestType = PerformanceTestType.ZERO_100,
    val startTimeNanos: Long = 0L,
    val lastResult: PerformanceResult? = null,
    val history: List<PerformanceResult> = emptyList(),
    val statusMessage: String = ""
)

data class PowerCalculation(
    val horsepower: Double = 0.0,
    val torqueNm: Double = 0.0,
    val horsepowerMetric: Double = 0.0,
    val isValid: Boolean = false
) {
    companion object {
        fun calculate(mafGS: Double, rpm: Double, veFactor: Double = 0.85): PowerCalculation {
            if (mafGS <= 0.0 || rpm <= 0.0 || rpm > 8000.0) {
                return PowerCalculation()
            }
            val airFlowKgH = mafGS * 3600.0
            val airFlowKgs = airFlowKgH / 3600.0
            val bmep = (airFlowKgs * 1.4 * 287.0 * (25.0 + 273.15)) / (0.85 * rpm * 0.5)
            val torqueNm = (bmep * 0.5 * 0.002) * 1000.0
            val powerKw = (torqueNm * rpm) / 9549.0
            val hp = powerKw * 1.341
            val hpMetric = powerKw * 1.3596
            return PowerCalculation(
                horsepower = hp,
                torqueNm = torqueNm,
                horsepowerMetric = hpMetric,
                isValid = hp > 0.0 && hp < 2000.0 && torqueNm > 0.0 && torqueNm < 2000.0
            )
        }
    }
}

data class DriveScore(
    val score: Int = 0,
    val accelerationScore: Int = 0,
    val brakingScore: Int = 0,
    val cruisingScore: Int = 0,
    val idleScore: Int = 0,
    val rpmScore: Int = 0,
    val throttleScore: Int = 0
) {
    val grade: String get() = when {
        score >= 90 -> "A+"
        score >= 80 -> "A"
        score >= 70 -> "B"
        score >= 60 -> "C"
        score >= 50 -> "D"
        else -> "F"
    }
    val color: Long get() = when {
        score >= 80 -> 0xFF44FF88
        score >= 60 -> 0xFFFFE066
        else -> 0xFFFF4444
    }
}

data class DriveSession(
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val avgRpm: Double = 0.0,
    val maxRpm: Double = 0.0,
    val avgThrottle: Double = 0.0,
    val maxThrottle: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val idleTimeSeconds: Long = 0L,
    val harshAccels: Int = 0,
    val harshBrakes: Int = 0,
    val rpmSamples: Double = 0.0,
    val throttleSamples: Double = 0.0,
    val speedSamples: Double = 0.0
)

data class ShiftLightConfig(
    val enabled: Boolean = false,
    val redlineRpm: Int = 6500,
    val warningRpm: Int = 5500,
    val flashEnabled: Boolean = true,
    val soundEnabled: Boolean = false
)

data class DashboardPreset(
    val id: String,
    val name: String,
    val themeName: String,
    val primaryGaugeIds: Set<String>,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ColdStartPhase(val label: String, val description: String) {
    NOT_STARTED("Nicht gestartet", "Motor aus"),
    CRANKING("Starten", "Anlasser dreht"),
    WARMING_UP("Aufwärmen", "Motor wird warm"),
    OPEN_LOOP("Schleifchenbetrieb", "Lambdaregelung aus"),
    CLOSED_LOOP("Geschlossener Regelkreis", "Motor betriebsbereit"),
    READY("Bereit", "Volle Leistung verfügbar")
}

data class AstraJ14TurboCalibration(
    val redlineRpm: Int = 6500,
    val maxBoostKpa: Double = 180.0,
    val maxTorqueNm: Double = 200.0,
    val maxPowerKw: Double = 103.0,
    val maxEgtC: Double = 850.0,
    val maxOilTempC: Double = 120.0,
    val maxCoolantTempC: Double = 105.0,
    val maxChargeAirTempC: Double = 65.0,
    val idleRpm: Int = 750,
    val maxTurboRpm: Int = 200000,
    val oilCapacityLiters: Double = 4.5,
    val turbochargerType: String = "BorgWarner K03",
    val engineCode: String = "A14NET",
    val fuelType: String = "Benzin (95/98)"
) {
    fun getBoostBar(): Double = boostKpa / 100.0
    val boostKpa: Double get() = 100.0
    val maxBoostBar: Double get() = 1.8

    fun isRpmWarning(rpm: Double): Boolean = rpm >= redlineRpm * 0.9
    fun isRpmRedline(rpm: Double): Boolean = rpm >= redlineRpm
    fun isBoostWarning(boost: Double): Boolean = boost >= maxBoostBar * 0.85
    fun isBoostOverboost(boost: Double): Boolean = boost >= maxBoostBar
    fun isEgtWarning(egt: Double): Boolean = egt >= maxEgtC * 0.9
    fun isEgtCritical(egt: Double): Boolean = egt >= maxEgtC

    companion object {
        val INSTANCE = AstraJ14TurboCalibration()
        val SUPPORTED_TURBO_PIDS = listOf(
            OBDPID.BOOST_PRESSURE, OBDPID.VGT_CONTROL, OBDPID.WASTEGATE_CONTROL,
            OBDPID.TURBO_RPM, OBDPID.CHARGE_AIR_COOLER_TEMP,
            OBDPID.EGT_BANK1, OBDPID.EGT_BANK2, OBDPID.OIL_TEMP,
            OBDPID.ACTUAL_TORQUE, OBDPID.DEMAND_TORQUE, OBDPID.REFERENCE_TORQUE
        )
        val DASHBOARD_PRESET = DashboardPreset(
            id = "astra_j_14_turbo",
            name = "Opel Astra J 1.4 Turbo",
            themeName = "CANOPO",
            primaryGaugeIds = setOf("rpm", "boost", "coolant", "speed", "torque", "egt"),
            createdAt = System.currentTimeMillis()
        )
        val RECOMMENDED_PIDS = listOf(
            OBDPID.RPM, OBDPID.SPEED, OBDPID.COOLANT_TEMP, OBDPID.THROTTLE,
            OBDPID.ENGINE_LOAD, OBDPID.BOOST_PRESSURE, OBDPID.EGT_BANK1,
            OBDPID.CHARGE_AIR_COOLER_TEMP, OBDPID.FUEL_LEVEL, OBDPID.BATTERY_VOLTAGE,
            OBDPID.MAF_RATE, OBDPID.ACTUAL_TORQUE, OBDPID.OIL_TEMP,
            OBDPID.TIMING_ADVANCE, OBDPID.INTAKE_TEMP, OBDPID.ENGINE_FUEL_RATE
        )
        val ALERT_CONFIG = AlertConfig(
            speedWarning = 180f,
            speedWarningEnabled = false,
            coolantWarning = 105f,
            coolantWarningEnabled = true,
            fuelWarning = 10f,
            fuelWarningEnabled = true,
            rpmWarning = 5850f,
            rpmWarningEnabled = true,
            batteryLowWarning = 11.8f,
            batteryLowWarningEnabled = true
        )
        val MAINTENANCE_ITEMS = listOf(
            MaintenanceItem(type = MaintenanceType.OIL_CHANGE, intervalKm = 15000),
            MaintenanceItem(type = MaintenanceType.AIR_FILTER, intervalKm = 30000),
            MaintenanceItem(type = MaintenanceType.TURBO_INSPECTION, intervalKm = 60000),
            MaintenanceItem(type = MaintenanceType.COOLANT, intervalKm = 60000),
            MaintenanceItem(type = MaintenanceType.BRAKE_PADS, intervalKm = 30000),
            MaintenanceItem(type = MaintenanceType.SPARK_PLUGS, intervalKm = 30000),
            MaintenanceItem(type = MaintenanceType.TURBO_BOOST_CHECK, intervalKm = 45000)
        )
    }
}

data class ColdStartState(
    val phase: ColdStartPhase = ColdStartPhase.NOT_STARTED,
    val coolantTempStart: Double = 0.0,
    val coolantTempCurrent: Double = 0.0,
    val rpmStart: Double = 0.0,
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false
) {
    val warmupProgress: Float get() = when {
        coolantTempCurrent <= 0.0 -> 0f
        coolantTempCurrent >= 90.0 -> 1f
        else -> ((coolantTempCurrent + 40.0) / 130.0).toFloat().coerceIn(0f, 1f)
    }
    val estimatedTimeRemaining: Long get() = when {
        warmupProgress >= 1f -> 0L
        warmupProgress <= 0f -> 0L
        else -> ((1.0 - warmupProgress) * 300.0).toLong().coerceAtMost(300L)
    }
}
