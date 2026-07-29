package com.canopobd.bluetooth

/**
 * Mode 22 Extended PIDs for GM/Opel vehicles with Bosch ME17 ECU
 *
 * Mode 22 is a manufacturer-specific mode (SAE J2190) that provides enhanced data
 * not available through standard Mode 01 PIDs.
 *
 * Command Format: 22XXXX (where XXXX is the 4-digit extended PID)
 * Response Format: 62XXXX (echoes the PID followed by data bytes)
 *
 * Research sources:
 * - SAE J2190 (Enhanced OBD-II)
 * - Bosch ME17.5.x/ME17.9.x ECU documentation
 * - GM/Opel vehicle OBD-II implementation
 */
object Mode22PIDs {

    // =========================================================================
    // GM/OPEL SPECIFIC MODE 22 PIDs (Manufacturer Enhanced Data)
    // =========================================================================

    // ----- Vehicle Information PIDs (Service 09 equivalent in Mode 22) -----

    /** VIN - Vehicle Identification Number (17 characters) */
    const val VIN = "22F190"

    /** Calibration ID - ECU Software Version */
    const val CALIBRATION_ID = "22F191"

    /** CVN - Calibration Verification Number */
    const val CALIBRATION_VERIFICATION = "22F192"

    // ----- Engine/Turbo PIDs (Bosch ME17 Enhanced Data) -----

    /** Engine Torque - Current engine torque in Nm */
    const val ENGINE_TORQUE = "220001"

    /** Turbo Boost Pressure - Actual boost in kPa (relative) */
    const val TURBO_BOOST_ACTUAL = "220002"

    /** Turbo Boost Pressure Target - Desired boost in kPa */
    const val TURBO_BOOST_TARGET = "220003"

    /** Wastegate Duty Cycle - WG valve position in % */
    const val WASTEGATE_DUTY = "220004"

    /** Turbo Speed - Turbocharger RPM */
    const val TURBO_SPEED = "220005"

    /** Turbo Inlet Temperature - Before turbo in °C */
    const val TURBO_INLET_TEMP = "220006"

    /** Turbo Outlet Temperature - After turbo in °C */
    const val TURBO_OUTLET_TEMP = "220007"

    /** Charge Air Temperature - After intercooler in °C */
    const val CHARGE_AIR_TEMP = "220008"

    /** VGT Position - Variable Geometry Turbo position % */
    const val VGT_POSITION = "220009"

    /** Turbo Compressor Efficiency - % */
    const val TURBO_EFFICIENCY = "22000A"

    // ----- Fuel System PIDs -----

    /** Fuel Rail Pressure - Direct injection pressure in kPa */
    const val FUEL_RAIL_PRESSURE = "221001"

    /** Fuel Temperature - Fuel temp in °C */
    const val FUEL_TEMP = "221002"

    /** Fuel Pressure - Fuel system pressure in kPa */
    const val FUEL_PRESSURE = "221003"

    /** Injection Quantity - Per cylinder in mg/stroke */
    const val INJECTION_QUANTITY = "221004"

    /** Injection Timing - Start of injection in ° */
    const val INJECTION_TIMING = "221005"

    // ----- Catalyst/Axhaust PIDs -----

    /** Catalyst Temperature Bank 1 Sensor 1 - Pre-cat in °C */
    const val CAT_TEMP_B1S1 = "222001"

    /** Catalyst Temperature Bank 1 Sensor 2 - Post-cat in °C */
    const val CAT_TEMP_B1S2 = "222002"

    /** Catalyst Temperature Bank 2 Sensor 1 - Pre-cat in °C */
    const val CAT_TEMP_B2S1 = "222003"

    /** Catalyst Temperature Bank 2 Sensor 2 - Post-cat in °C */
    const val CAT_TEMP_B2S2 = "222004"

    // ----- Sensor PIDs -----

    /** Ambient Air Temperature - Outside air temp in °C */
    const val AMBIENT_AIR_TEMP = "223001"

    /** Engine Oil Temperature - Engine oil temp in °C */
    const val ENGINE_OIL_TEMP = "223002"

    /** Engine Oil Pressure - Oil pressure in kPa */
    const val ENGINE_OIL_PRESSURE = "223003"

    /** Transmission Fluid Temperature - ATF temp in °C */
    const val TRANS_FLUID_TEMP = "223004"

    // ----- Knock/Sensor PIDs -----

    /** Knock Retard - Ignition retard due to knock in ° */
    const val KNOCK_RETARD = "224001"

    /** Octane Rating - Fuel octane rating */
    const val OCTANE_RATING = "224002"

    // ----- Lambda/AFR PIDs -----

    /** Wideband Lambda Bank 1 - Air-Fuel Ratio (14.7 = stoichiometric) */
    const val WIDEBAND_LAMBDA_B1 = "225001"

    /** Wideband Lambda Bank 2 - Air-Fuel Ratio */
    const val WIDEBAND_LAMBDA_B2 = "225002"

    /** Target Lambda - Desired air-fuel ratio */
    const val TARGET_LAMBDA = "225003"

    // ----- Transmission PIDs (if applicable) -----

    /** Gear Position - Current gear (P, R, N, D, 1-6) */
    const val GEAR_POSITION = "226001"

    /** Transmission Input Speed - Input shaft RPM */
    const val TRANS_INPUT_SPEED = "226002"

    /** Transmission Output Speed - Output shaft RPM */
    const val TRANS_OUTPUT_SPEED = "226003"

    // ----- Data class for Mode 22 responses -----
    data class Mode22PIDInfo(
        val code: String,
        val name: String,
        val unit: String,
        val byteCount: Int,
        val formula: (ByteArray) -> Double
    )

    // PID Definitions with formulas
    val PID_DEFINITIONS = mapOf(
        // Vehicle Info
        VIN to Mode22PIDInfo("F190", "VIN", "chars", 17) { b ->
            val vinStr = b.filter { it.toInt() in 0x20..0x7E }.map { it.toInt().toChar() }.joinToString("")
            if (vinStr.isNotEmpty()) vinStr.length.toDouble() else 0.0
        },
        CALIBRATION_ID to Mode22PIDInfo("F191", "Calibration ID", "", 16) { b ->
            val calStr = b.filter { it.toInt() in 0x20..0x7E }.map { it.toInt().toChar() }.joinToString("")
            if (calStr.isNotEmpty()) calStr.length.toDouble() else 0.0
        },
        CALIBRATION_VERIFICATION to Mode22PIDInfo("F192", "CVN", "", 4) { b ->
            if (b.size >= 4) {
                (((b[0].toInt() and 0xFF) shl 24) or
                    ((b[1].toInt() and 0xFF) shl 16) or
                    ((b[2].toInt() and 0xFF) shl 8) or
                    (b[3].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        },

        // Engine/Turbo (most important for turbo monitoring)
        ENGINE_TORQUE to Mode22PIDInfo("0001", "Engine Torque", "Nm", 2) { b ->
            if (b.size >= 2) {
                val value = (b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)
                // Convert to signed value, typically -500 to +500 Nm
                (value - 500).toDouble()
            } else { 0.0 }
        },
        TURBO_BOOST_ACTUAL to Mode22PIDInfo("0002", "Turbo Boost Actual", "kPa", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        },
        TURBO_BOOST_TARGET to Mode22PIDInfo("0003", "Turbo Boost Target", "kPa", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        },
        WASTEGATE_DUTY to Mode22PIDInfo("0004", "Wastegate Duty", "%", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF) * 100.0 / 255.0 } else { 0.0 }
        },
        TURBO_SPEED to Mode22PIDInfo("0005", "Turbo Speed", "RPM", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        },
        TURBO_INLET_TEMP to Mode22PIDInfo("0006", "Turbo Inlet Temp", "°C", 1) { b ->
            if (b.isNotEmpty()) { ((b[0].toInt() and 0xFF) - 40).toDouble() } else { 0.0 }
        },
        TURBO_OUTLET_TEMP to Mode22PIDInfo("0007", "Turbo Outlet Temp", "°C", 1) { b ->
            if (b.isNotEmpty()) { ((b[0].toInt() and 0xFF) - 40).toDouble() } else { 0.0 }
        },
        CHARGE_AIR_TEMP to Mode22PIDInfo("0008", "Charge Air Temp", "°C", 1) { b ->
            if (b.isNotEmpty()) { ((b[0].toInt() and 0xFF) - 40).toDouble() } else { 0.0 }
        },
        VGT_POSITION to Mode22PIDInfo("0009", "VGT Position", "%", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF) * 100.0 / 255.0 } else { 0.0 }
        },
        TURBO_EFFICIENCY to Mode22PIDInfo("000A", "Turbo Efficiency", "%", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF) * 100.0 / 255.0 } else { 0.0 }
        },

        // Fuel System
        FUEL_RAIL_PRESSURE to Mode22PIDInfo("1001", "Fuel Rail Pressure", "kPa", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        },
        FUEL_TEMP to Mode22PIDInfo("1002", "Fuel Temperature", "°C", 1) { b ->
            if (b.isNotEmpty()) { ((b[0].toInt() and 0xFF) - 40).toDouble() } else { 0.0 }
        },
        FUEL_PRESSURE to Mode22PIDInfo("1003", "Fuel Pressure", "kPa", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF).toDouble() } else { 0.0 }
        },
        INJECTION_QUANTITY to Mode22PIDInfo("1004", "Injection Quantity", "mg/stroke", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        },
        INJECTION_TIMING to Mode22PIDInfo("1005", "Injection Timing", "°", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF) - 500) / 2.0
            } else { 0.0 }
        },

        // Catalyst
        CAT_TEMP_B1S1 to Mode22PIDInfo("2001", "Cat Temp B1S1", "°C", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0
            } else { 0.0 }
        },
        CAT_TEMP_B1S2 to Mode22PIDInfo("2002", "Cat Temp B1S2", "°C", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0
            } else { 0.0 }
        },
        CAT_TEMP_B2S1 to Mode22PIDInfo("2003", "Cat Temp B2S1", "°C", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0
            } else { 0.0 }
        },
        CAT_TEMP_B2S2 to Mode22PIDInfo("2004", "Cat Temp B2S2", "°C", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0
            } else { 0.0 }
        },

        // Sensors
        AMBIENT_AIR_TEMP to Mode22PIDInfo("3001", "Ambient Air Temp", "°C", 1) { b ->
            if (b.isNotEmpty()) { ((b[0].toInt() and 0xFF) - 40).toDouble() } else { 0.0 }
        },
        ENGINE_OIL_TEMP to Mode22PIDInfo("3002", "Engine Oil Temp", "°C", 1) { b ->
            if (b.isNotEmpty()) { ((b[0].toInt() and 0xFF) - 40).toDouble() } else { 0.0 }
        },
        ENGINE_OIL_PRESSURE to Mode22PIDInfo("3003", "Engine Oil Pressure", "kPa", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF).toDouble() } else { 0.0 }
        },
        TRANS_FLUID_TEMP to Mode22PIDInfo("3004", "Trans Fluid Temp", "°C", 1) { b ->
            if (b.isNotEmpty()) { ((b[0].toInt() and 0xFF) - 40).toDouble() } else { 0.0 }
        },

        // Knock
        KNOCK_RETARD to Mode22PIDInfo("4001", "Knock Retard", "°", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF) / 2.0 } else { 0.0 }
        },
        OCTANE_RATING to Mode22PIDInfo("4002", "Octane Rating", "RON", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF).toDouble() } else { 0.0 }
        },

        // Lambda
        WIDEBAND_LAMBDA_B1 to Mode22PIDInfo("5001", "Wideband Lambda B1", "λ", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 32768.0
            } else { 0.0 }
        },
        WIDEBAND_LAMBDA_B2 to Mode22PIDInfo("5002", "Wideband Lambda B2", "λ", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 32768.0
            } else { 0.0 }
        },
        TARGET_LAMBDA to Mode22PIDInfo("5003", "Target Lambda", "λ", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF) / 200.0 } else { 0.0 }
        },

        // Transmission
        GEAR_POSITION to Mode22PIDInfo("6001", "Gear Position", "", 1) { b ->
            if (b.isNotEmpty()) { (b[0].toInt() and 0xFF).toDouble() } else { 0.0 }
        },
        TRANS_INPUT_SPEED to Mode22PIDInfo("6002", "Trans Input Speed", "RPM", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        },
        TRANS_OUTPUT_SPEED to Mode22PIDInfo("6003", "Trans Output Speed", "RPM", 2) { b ->
            if (b.size >= 2) {
                ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble()
            } else { 0.0 }
        }
    )

    // Get the most important PIDs for turbo monitoring
    val TURBO_MONITORING_PIDS = listOf(
        TURBO_BOOST_ACTUAL,
        TURBO_BOOST_TARGET,
        WASTEGATE_DUTY,
        TURBO_SPEED,
        CHARGE_AIR_TEMP,
        TURBO_INLET_TEMP,
        TURBO_OUTLET_TEMP,
        ENGINE_TORQUE,
        VGT_POSITION
    )

    // Get all available PID codes
    val ALL_PID_CODES = PID_DEFINITIONS.keys.toList()
}

/**
 * Response data class for Mode 22 queries
 */
data class Mode22Response(
    val pid: String,
    val rawResponse: String,
    val dataBytes: ByteArray,
    val value: Double,
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) { return true }
        if (javaClass != other?.javaClass) { return false }
        other as Mode22Response
        return pid == other.pid && rawResponse == other.rawResponse
    }

    override fun hashCode(): Int {
        var result = pid.hashCode()
        result = 31 * result + rawResponse.hashCode()
        result = 31 * result + dataBytes.contentHashCode()
        return result
    }
}

/**
 * Mode 22 Data container for turbo monitoring
 * Collects all turbo-related Mode 22 data
 */
data class Mode22TurboData(
    val turboBoostActual: Double = 0.0,
    val turboBoostTarget: Double = 0.0,
    val wastegateDuty: Double = 0.0,
    val turboSpeed: Double = 0.0,
    val chargeAirTemp: Double = 0.0,
    val turboInletTemp: Double = 0.0,
    val turboOutletTemp: Double = 0.0,
    val engineTorque: Double = 0.0,
    val vgtPosition: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculate boost deviation from target
     */
    val boostDeviation: Double
        get() = if (turboBoostTarget > 0) {
            ((turboBoostActual - turboBoostTarget) / turboBoostTarget) * 100.0
        } else { 0.0 }

    /**
     * Get relative boost (kPa above atmospheric)
     * Assumes 100 kPa atmospheric pressure
     */
    val relativeBoost: Double
        get() = (turboBoostActual - 100.0).coerceAtLeast(0.0)

    /**
     * Get boost in bar (relative)
     */
    val boostBar: Double
        get() = relativeBoost / 100.0

    /**
     * Check if there's an overboost condition (>1.3 bar relative)
     */
    val isOverboost: Boolean
        get() = boostBar > 1.3

    /**
     * Check if there's an underboost condition
     */
    val isUnderboost: Boolean
        get() = turboBoostActual <= 0 && turboBoostTarget > 0
}
