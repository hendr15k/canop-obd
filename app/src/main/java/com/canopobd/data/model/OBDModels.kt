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
    DISTANCE_MIL("0121", "Distance with MIL", "km", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0
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
    THROTTLE_ACTUATOR("0136", "Throttle Actuator", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
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
    DEMAND_TORQUE("0061", "Driver Demand Torque", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 125).toDouble() else 0.0
    }),
    ACTUAL_TORQUE("0062", "Actual Torque", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 125).toDouble() else 0.0
    }),
    REFERENCE_TORQUE("0063", "Reference Torque", "Nm", 2, { b ->
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
    }),
    ACCELERATOR_POS_E("011B", "Accelerator Pedal E", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    ENGINE_RUNTIME_MIL("015F", "Engine Runtime MIL On", "s", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    ALTERNATOR_DUTY("0153", "Alternator Duty Cycle", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    O2_VOLTAGE_B1S3("0145", "O2 Sensor B1S3 Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 200.0 else 0.0
    }),
    O2_VOLTAGE_B2S1("0146", "O2 Sensor B2S1 Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 200.0 else 0.0
    }),
    O2_VOLTAGE_B2S2("0147", "O2 Sensor B2S2 Voltage", "V", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 200.0 else 0.0
    }),
    INTAKE_AIR_TEMP_2("0156", "Intake Air Temp 2", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    TURBO_OIL_PRESSURE("0167", "Turbo Oil Pressure", "kPa", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    TURBO_INLET_TEMP("0168", "Turbo Inlet Temp", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    TURBO_OUTLET_TEMP("0169", "Turbo Outlet Temp", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    TURBO_WASTEGATE_B("016A", "Turbo Wastegate B", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    TURBO_BOOST_B("016B", "Turbo Boost B", "kPa", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    TURBO_VARIABLE_GEOM("016C", "Turbo VGT Position", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    TURBO_WATER_COOL("016D", "Turbo Water Cool Flow", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    TURBO_COMP_INLET_TEMP("016E", "Turbo Comp Inlet Temp", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    TURBO_COMP_OUTLET_TEMP("016F", "Turbo Comp Outlet Temp", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    TURBO_TURBINE_INLET_TEMP("0176", "Turbo Turbine Inlet Temp", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    TURBO_TURBINE_OUTLET_TEMP("017A", "Turbo Turbine Outlet Temp", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    TURBO_BOOST_ABS("017B", "Turbo Boost Absolute", "kPa", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    TURBO_ACTUATOR_DUTY("017E", "Turbo Actuator Duty", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    WARMUP_CATALYST("0150", "Warmup Catalyst Status", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    CATALYST_TEMP_B1S2("013D", "Catalyst Temp B1S2", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    CATALYST_TEMP_B2S1("0154", "Catalyst Temp B2S1", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    CATALYST_TEMP_B2S2("0155", "Catalyst Temp B2S2", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),

    // GM Mode 22 (Service $22) - Opel/Astra J spezifische DataIdentifiers
    ENGINE_TORQUE_MODE22("221001", "Motor-Drehmoment (Mode22)", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0
    }),
    REQUESTED_TORQUE_MODE22("221002", "Angefordertes Drehmoment (Mode22)", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0
    }),
    BOOST_PRESSURE_ACTUAL_MODE22("221008", "Boost-Druck Ist (Mode22)", "kPa", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    BOOST_PRESSURE_TARGET_MODE22("221009", "Boost-Druck Soll (Mode22)", "kPa", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    WASTEGATE_POSITION_MODE22("22100A", "Wastegate-Position (Mode22)", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    TURBO_RPM_MODE22("22100B", "Turbo-Drehzahl (Mode22)", "rpm", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    OIL_TEMP_MODE22("22100C", "Motoröl-Temperatur (Mode22)", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    COOLANT_TEMP_MODE22("22100D", "Kühlmittel-Temperatur (Mode22)", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    INTAKE_AIR_TEMP_MODE22("22100E", "Ansaugluft-Temperatur (Mode22)", "°C", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 40).toDouble() else 0.0
    }),
    FUEL_RAIL_PRESSURE_MODE22("22100F", "Einspritzdruck (Mode22)", "kPa", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) * 10.0 else 0.0
    }),
    INJECTOR_PULSE_WIDTH("221010", "Einspritzdauer (Mode22)", "ms", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 100.0 else 0.0
    }),
    VVT_INTAKE_MODE22("221015", "VVT-Ansaugseite (Mode22)", "°", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0
    }),
    VVT_EXHAUST_MODE22("221016", "VVT-Auslassseite (Mode22)", "°", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128).toDouble() else 0.0
    }),
    FUEL_CONSUMPTION_INSTANT("221018", "Kraftstoffverbrauch aktuell (Mode22)", "L/h", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)).toDouble() else 0.0
    }),
    FUEL_CONSUMPTION_AVERAGE("22101A", "Kraftstoffverbrauch Ø (Mode22)", "L/100km", 2, { b ->
        if (b.size >= 2) (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 10.0 else 0.0
    }),
    AFR_RATIO_MODE22("22101F", "Luft-Kraftstoff-Verhältnis (Mode22)", "", 2, { b ->
        if (b.size >= 2) 2.0 * (256.0 * (b[0].toInt() and 0xFF) + (b[1].toInt() and 0xFF)) / 65536.0 else 0.0
    }),

    // Z14XEL Mode 22 PIDs (Opel Astra J 1.4 N/A - Bosch ME17.9.2)
    THROTTLE_POSITION_MODE22_Z14("221012", "Drosselklappe (Z14XEL)", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    EGR_POSITION_MODE22_Z14("221011", "EGR-Stellung (Z14XEL)", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    MAP_SENSOR_MODE22_Z14("221013", "Ladedruck MAP (Z14XEL)", "kPa", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    PARKING_NEUTRAL_MODE22_Z14("221014", "P/N-Status (Z14XEL)", "", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    CATALYST_TEMP_B1S1_MODE22_Z14("221020", "Kat-Temp B1S1 (Z14XEL)", "°C", 2, { b ->
        if (b.size >= 2) ((b[0].toInt() and 0xFF) * 256 + (b[1].toInt() and 0xFF)) / 10.0 - 40.0 else 0.0
    }),
    STFT_MODE22_Z14("221024", "STFT Bank1 (Z14XEL)", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0
    }),
    LTFT_MODE22_Z14("221025", "LTFT Bank1 (Z14XEL)", "%", 1, { b ->
        if (b.isNotEmpty()) ((b[0].toInt() and 0xFF) - 128) * 100.0 / 128.0 else 0.0
    }),
    FUEL_PUMP_STATUS_Z14("221026", "Kraftstoffpumpe (Z14XEL)", "", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF).toDouble() else 0.0
    }),
    EVAP_PURGE_DUTY_Z14("221027", "EVAP-Purge (Z14XEL)", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    IDLE_AIR_CONTROL_Z14("221030", "Leerlauf-Luftregelung (Z14XEL)", "%", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) * 100.0 / 255.0 else 0.0
    }),
    KNOCK_RETARD_Z14("221031", "Klopfverstellung (Z14XEL)", "°", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 2.0 else 0.0
    }),
    IGNITION_DWELL_Z14("221032", "Zündverweilzeit (Z14XEL)", "ms", 1, { b ->
        if (b.isNotEmpty()) (b[0].toInt() and 0xFF) / 10.0 else 0.0
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
    val turboBoostVacuum: Double = 0.0,
    val acceleratorPosE: Double = 0.0,
    val engineRuntimeMil: Double = 0.0,
    val alternatorDuty: Double = 0.0,
    val o2VoltageB1S3: Double = 0.0,
    val o2VoltageB2S1: Double = 0.0,
    val o2VoltageB2S2: Double = 0.0,
    val intakeAirTemp2: Double = 0.0,
    val turboOilPressure: Double? = null,
    val turboInletTemp: Double = 0.0,
    val turboOutletTemp: Double = 0.0,
    val turboWastegateB: Double? = null,
    val turboBoostB: Double? = null,
    val turboVgtPosition: Double? = null,
    val turboWaterCoolFlow: Double? = null,
    val turboCompInletTemp: Double? = null,
    val turboCompOutletTemp: Double? = null,
    val turboTurbineInletTemp: Double? = null,
    val turboTurbineOutletTemp: Double? = null,
    val turboBoostAbsolute: Double = 0.0,
    val turboActuatorDuty: Double = 0.0,
    val warmupCatalyst: Double = 0.0,
    val catalystTempB1S2: Double = 0.0,
    val catalystTempB2S1: Double = 0.0,
    val catalystTempB2S2: Double = 0.0,
    // GM Mode 22 (Service $22) - Erweiterte Opel-spezifische Werte
    val engineTorqueMode22: Double = 0.0,
    val requestedTorqueMode22: Double = 0.0,
    val boostPressureActualMode22: Double = 0.0,
    val boostPressureTargetMode22: Double = 0.0,
    val wastegatePositionMode22: Double = 0.0,
    val turboRpmMode22: Double = 0.0,
    val oilTempMode22: Double = 0.0,
    val coolantTempMode22: Double = 0.0,
    val intakeAirTempMode22: Double = 0.0,
    val fuelRailPressureMode22: Double = 0.0,
    val injectorPulseWidth: Double = 0.0,
    val vvtIntakeMode22: Double = 0.0,
    val vvtExhaustMode22: Double = 0.0,
    val fuelConsumptionInstant: Double = 0.0,
    val fuelConsumptionAverage: Double = 0.0,
    val afrRatioMode22: Double = 0.0,
    val distanceWithMil: Double = 0.0
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

enum class AppThemeMode(val displayName: String) {
    DARK("Dunkel"),
    LIGHT("Hell"),
    SYSTEM("System");

    companion object {
        fun fromName(name: String): AppThemeMode =
            entries.find { it.name == name } ?: DARK
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
    val coolantTemp: List<TrendPoint> = emptyList(),
    val boostPressure: List<TrendPoint> = emptyList(),
    val wastegateDuty: List<TrendPoint> = emptyList(),
    val turboRpm: List<TrendPoint> = emptyList(),
    val egtBank1: List<TrendPoint> = emptyList(),
    val chargeAirTemp: List<TrendPoint> = emptyList()
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
    val batteryVoltage: Double,
    val intakeTemp: Double = 0.0,
    val oilTemp: Double = 0.0,
    val boostPressure: Double = 0.0,
    val barometricPressure: Double = 0.0,
    val wastegateDuty: Double = 0.0,
    val turboRpm: Double = 0.0,
    val egtBank1: Double = 0.0,
    val egtBank2: Double = 0.0,
    val chargeAirTemp: Double = 0.0,
    val mafRate: Double = 0.0,
    val engineLoad: Double = 0.0,
    val shortTermFuelTrimB1: Double = 0.0,
    val longTermFuelTrimB1: Double = 0.0,
    val timingAdvance: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null
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
    val batteryLowWarningEnabled: Boolean = true,
    val boostWarning: Float = 0.85f,
    val boostWarningEnabled: Boolean = false,
    val boostCritical: Float = 1.35f,
    val boostCriticalEnabled: Boolean = true,
    val egtWarning: Float = 850f,
    val egtWarningEnabled: Boolean = true,
    val egtCritical: Float = 950f,
    val egtCriticalEnabled: Boolean = true,
    val oilTempWarning: Float = 120f,
    val oilTempWarningEnabled: Boolean = true,
    val oilTempCritical: Float = 135f,
    val oilTempCriticalEnabled: Boolean = true,
    val turboSpeedWarning: Float = 180000f,
    val turboSpeedWarningEnabled: Boolean = false,
    val chargeAirTempWarning: Float = 65f,
    val chargeAirTempWarningEnabled: Boolean = false,
    val fuelTrimWarning: Float = 15f,
    val fuelTrimWarningEnabled: Boolean = false,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val hysteresisSeconds: Int = 10,
    val cooldownSeconds: Int = 60
)

data class ActiveAlert(
    val type: AlertType,
    val severity: AlertSeverity,
    val value: Float,
    val threshold: Float,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AlertSeverity { INFO, WARNING, CRITICAL }

enum class AlertType(val label: String) {
    SPEED("Geschwindigkeit"),
    COOLANT("Kühlmitteltemperatur"),
    FUEL("Kraftstoff"),
    RPM("Drehzahl"),
    BATTERY("Batterie"),
    BOOST("Ladedruck"),
    EGT("Abgastemperatur"),
    OIL_TEMP("Öltemperatur"),
    TURBO_SPEED("Turbo-Drehzahl"),
    CHARGE_AIR_TEMP("Ladelufttemperatur"),
    FUEL_TRIM("Kraftstofftrim")
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
    val currentKm: Int = 0,
    val notes: String = ""
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

/**
 * GPS-based acceleration test state with rich data.
 */
data class AccelerationPhase(
    val name: String,
    val timestamp: Long,
    val speedKmh: Double,
    val rpm: Int?
)

data class AccelerationRun(
    val timestamp: Long = System.currentTimeMillis(),
    val testType: PerformanceTestType = PerformanceTestType.ZERO_100,
    val timeSeconds: Double = 0.0,
    val valid: Boolean = false,
    val maxSpeedKmh: Double = 0.0,
    val maxAcceleration: Double = 0.0,
    val timeTo50Percent: Double? = null,
    val timeTo90Percent: Double? = null,
    val sampleCount: Int = 0,
    val phases: List<AccelerationPhase> = emptyList(),
    val gearShifts: List<Int> = emptyList(),
    val cancelled: Boolean = false
)

data class PowerCalculation(
    val horsepower: Double = 0.0,
    val torqueNm: Double = 0.0,
    val horsepowerMetric: Double = 0.0,
    val isValid: Boolean = false
) {
    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun calculate(mafGS: Double, rpm: Double, intakeTempC: Double = 25.0, veFactor: Double = 0.85): PowerCalculation {
            if (mafGS <= 0.0 || rpm <= 0.0 || rpm > 8000.0) {
                return PowerCalculation()
            }
            val airFlowKgH = mafGS * 3600.0
            val airFlowKgs = airFlowKgH / 3600.0
            val bmep = (airFlowKgs * 1.4 * 287.0 * (intakeTempC + 273.15)) / (0.85 * rpm * 0.5)
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
    val throttleScore: Int = 0,
    val boostScore: Int = 0,
    val ecoScore: Int = 0,
    val turboHealthScore: Int = 0
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
    val rpmSampleCount: Int = 0,
    val throttleSamples: Double = 0.0,
    val throttleSampleCount: Int = 0,
    val speedSamples: Double = 0.0,
    val speedSampleCount: Int = 0,
    val avgBoostBar: Double = 0.0,
    val maxBoostBar: Double = 0.0,
    val boostSamples: Double = 0.0,
    val boostSampleCount: Int = 0,
    val optimalBoostTime: Int = 0,
    val highBoostTime: Int = 0,
    val coastingInGearSamples: Int = 0,
    val deceleratingSamples: Int = 0,
    val rpmAbove4500Samples: Int = 0,
    val boostSumOfSquares: Double = 0.0,
    val wastegateDutySum: Double = 0.0,
    val wastegateSampleCount: Int = 0,
    val rpmRateSamples: Double = 0.0,
    val rpmRateSampleCount: Int = 0
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

data class TurboSpecificPreset(
    val id: String,
    val name: String,
    val gaugeIds: List<String>,
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

enum class FuelClassification(val label: String) {
    REGULAR_UNLEADED("Regular Unleaded (91 RON)"),
    SUPER_UNLEADED("Super Unleaded (95 RON)"),
    SUPER_PLUS("Super Plus (98 RON)"),
    ETHANOL_E10_COMPATIBLE("Ethanol E10 Compatible"),
    ETHANOL_E85("Ethanol E85 (Flex Fuel)"),
    DIESEL("Diesel"),
    DIESEL_ULTRA("Ultra Low Sulfur Diesel")
}

data class FuelConsumptionProfile(
    val cityL100: ClosedFloatingPointRange<Double> = 9.0..12.0,
    val highwayL100: ClosedFloatingPointRange<Double> = 6.5..8.0,
    val mixedL100: ClosedFloatingPointRange<Double> = 7.5..9.0,
    val tankLiters: Double = 56.0,
    val recommendedFuel: String = "Super 98 RON (min 95)",
    val fuelClassification: FuelClassification = FuelClassification.ETHANOL_E10_COMPATIBLE
)

data class TuningStage(
    val name: String,
    val powerKw: IntRange,
    val powerPs: IntRange,
    val torqueNm: IntRange,
    val boostBar: ClosedFloatingPointRange<Float>,
    val fuelRpm: String,
    val notes: String
)

data class TuningStageCalibration(
    val stage1: TuningStage,
    val stage2: TuningStage,
    val stage3: TuningStage
) {
    companion object {
        val ASTRA_J_14T = TuningStageCalibration(
            stage1 = TuningStage(
                name = "Stage 1 – ECU Remap",
                powerKw = 125..136,
                powerPs = 170..185,
                torqueNm = 260..280,
                boostBar = 0.7f..0.8f,
                fuelRpm = "4900-6000",
                notes = "ECU remap, drop-in air filter, downpipe"
            ),
            stage2 = TuningStage(
                name = "Stage 2 – Bolt-On",
                powerKw = 143..154,
                powerPs = 195..210,
                torqueNm = 290..310,
                boostBar = 0.9f..1.0f,
                fuelRpm = "5000-6200",
                notes = "Intercooler upgrade, intake, exhaust, upgraded injectors"
            ),
            stage3 = TuningStage(
                name = "Stage 3 – Big Turbo",
                powerKw = 169..191,
                powerPs = 230..260,
                torqueNm = 320..350,
                boostBar = 1.2f..1.5f,
                fuelRpm = "5200-6500",
                notes = "Turbo upgrade, fuel system, internals — built engine recommended"
            )
        )
    }
}

data class MaintenanceInterval(
    val item: MaintenanceType,
    val intervalKm: Int,
    val intervalMonths: Int = 0,
    val specification: String = "",
    val capacity: String = "",
    val partNumber: String = "",
    val notes: String = ""
)

data class ProblemMileageMap(
    val component: String,
    val typicalRangeStartKm: Int,
    val typicalRangeEndKm: Int,
    val severity: Int = 1,
    val description: String = ""
)

data class AstraJ14TurboCalibration(
    val redlineRpm: Int = 6500,
    val rpmWarning: Int = 5850,
    val idleRpm: Int = 750,
    val maxBoostKpa: Double = 180.0,
    val maxBoostBar: Double = 1.0,
    val overboostBar: Double = 1.2,
    val maxTorqueNm: Double = 200.0,
    val overboostTorqueNm: Double = 220.0,
    val maxPowerKw: Double = 103.0,
    val maxPowerHp: Double = 140.0,
    val maxEgtC: Double = 850.0,
    val maxOilTempC: Double = 120.0,
    val optimalOilTempMin: Double = 90.0,
    val optimalOilTempMax: Double = 110.0,
    val maxCoolantTempC: Double = 105.0,
    val maxChargeAirTempC: Double = 65.0,
    val minOilPressureIdle: Double = 1.0,
    val minOilPressureRpm: Double = 2.0,
    val maxTurboRpm: Int = 200000,
    val oilCapacityLiters: Double = 4.5,
    val turbochargerType: String = "BorgWarner KP39 (Single-Scroll, wastegate-geregelt)",
    val engineCode: String = "A14NET",
    val gmEngineCode: String = "LUJ",
    val fuelType: String = "Benzin (95 RON min / 98 empfohlen)",
    val fuelTankLiters: Double = 56.0,
    val batteryAh: Int = 70,
    val alternatorV: Double = 14.0,
    val coolantCapacity: Double = 5.7,
    val sparkPlugType: String = "NGK LZKR6AP-11G / Bosch FR7HPP332",
    val sparkPlugGap: Double = 0.7,
    val sparkPlugTorque: String = "20-25 Nm",
    val transmissionFluid: String = "Dexron VI ATF",
    val engineFamily: String = "GM Family 0 Gen III",
    val ecuType: String = "Bosch ME17.9.22 / Delco E78",
    val compressionRatio: String = "9.5:1",
    val displacement: String = "1364cc (1.4L)",
    val boreStroke: String = "72.5mm x 82.6mm",
    val valveConfig: String = "DOHC 16V, DCVCP Nockenwellen",
    val emissionStandard: String = "Euro 5",
    val fuelConsumptionCombined: Double = 6.0,
    val fuelConsumptionUrban: Double = 7.8,
    val fuelConsumptionExtraUrban: Double = 5.0,
    val co2Emissions: Int = 139,
    val topSpeed: Int = 207,
    val accel0to100: Double = 9.9,
    val recommendedOil: String = "Dexos2 5W-30",
    val alternativeOil: String = "ACEA C3 5W-30 / A3/B4 5W-40",
    val oilChangeIntervalKm: Int = 15000,
    val airFilterIntervalKm: Int = 30000,
    val sparkPlugIntervalKm: Int = 60000,
    val coolantIntervalKm: Int = 80000,
    val timingChainIntervalKm: Int = 150000,
    val vvtSystem: String = "DCVCP (Dual Continuous Variable Cam Phasing)",
    val overboostMaxBar: Double = 1.3,
    val overboostMaxDurationSeconds: Int = 10,
    val normalBoostTargetBar: Double = 0.7,
    val wastegateTargetPercent: Double = 45.0,
    val maxRpmSustained: Int = 6000,
    val powerCurvePeakRpm: Int = 5500,
    val torqueCurvePeakRpm: Int = 3000,
    val optimalRpmMin: Int = 1500,
    val optimalRpmMax: Int = 3000,
    val powerBandRpmMin: Int = 5000,
    val powerBandRpmMax: Int = 5500,
    val intercoolerEfficiencyTarget: Double = 85.0,
    val fuelConsumptionSport: Double = 8.5,
    val fuelConsumptionEco: Double = 5.5,
    val oilPressureIdle: Double = 1.0,
    val oilPressureRpm: Double = 2.5,
    val recommendedFuelOctane: Int = 98,
    val minFuelOctane: Int = 95,

    val wastegateIdleDutyMin: Float = 80.0f,
    val wastegateIdleDutyMax: Float = 95.0f,
    val wastegateWotDutyMin: Float = 25.0f,
    val wastegateWotDutyMax: Float = 60.0f,
    val wastegateHealthyDutyRange: ClosedFloatingPointRange<Float> = 30.0f..70.0f,
    val wastegateStuckOpenDuty: Float = 95.0f,
    val wastegateStuckClosedDuty: Float = 10.0f,

    val boostIdleVacuumKpa: Float = -65.0f,
    val boostIdleVacuumKpaMin: Float = -75.0f,
    val boostIdleVacuumKpaMax: Float = -55.0f,
    val boostNormalKpa: Float = 70.0f,
    val boostNormalKpaMin: Float = 60.0f,
    val boostNormalKpaMax: Float = 80.0f,
    val boostOverboostKpa: Float = 120.0f,
    val boostOverboostKpaMax: Float = 135.0f,

    val turboSpeedIdleRpm: Float = 8000.0f,
    val turboSpeedNormalRangeRpm: ClosedFloatingPointRange<Float> = 80000.0f..150000.0f,
    val turboSpeedMaxRpm: Float = 200000.0f,

    val chargeAirTempIdleOffset: Float = 10.0f,
    val chargeAirTempNormalMax: Float = 50.0f,
    val chargeAirTempWotMax: Float = 65.0f,

    val egtNormalMax: Float = 750.0f,
    val egtWotMax: Float = 850.0f,
    val egtCritical: Float = 950.0f,

    val fuelPressureIdleKpa: Float = 350.0f,
    val fuelPressureWotKpa: Float = 500.0f,
    val fuelRailPressureIdleKpa: ClosedFloatingPointRange<Float> = 3500.0f..4500.0f,
    val fuelRailPressureWotKpa: ClosedFloatingPointRange<Float> = 4000.0f..5500.0f,

    val stftNormalRange: ClosedFloatingPointRange<Float> = -5.0f..5.0f,
    val stftWarningRange: ClosedFloatingPointRange<Float> = -10.0f..10.0f,
    val ltftNormalRange: ClosedFloatingPointRange<Float> = -8.0f..8.0f,
    val ltftWarningRange: ClosedFloatingPointRange<Float> = -15.0f..15.0f,
    val ltftCritical: Float = 15.0f,

    val tuningStages: TuningStageCalibration = TuningStageCalibration.ASTRA_J_14T,
    val fuelConsumptionProfile: FuelConsumptionProfile = FuelConsumptionProfile()
) {
    fun getBoostBar(pressureKpa: Double): Double = pressureKpa / 100.0
    fun isRpmWarning(rpm: Double): Boolean = rpm >= rpmWarning
    fun isRpmRedline(rpm: Double): Boolean = rpm >= redlineRpm
    fun isBoostWarning(boost: Double): Boolean = boost >= maxBoostBar * 0.85
    fun isBoostOverboost(boost: Double): Boolean = boost >= overboostBar
    fun isBoostCritical(boost: Double): Boolean = boost >= maxBoostBar
    fun isEgtWarning(egt: Double): Boolean = egt >= maxEgtC * 0.9
    fun isEgtCritical(egt: Double): Boolean = egt >= maxEgtC
    fun isOilTempWarning(temp: Double): Boolean = temp >= maxOilTempC * 0.9
    fun isOilTempCritical(temp: Double): Boolean = temp >= maxOilTempC
    fun isCoolantWarning(temp: Double): Boolean = temp >= maxCoolantTempC * 0.95
    fun isCoolantCritical(temp: Double): Boolean = temp >= maxCoolantTempC
    fun isChargeAirTempWarning(temp: Double): Boolean = temp >= maxChargeAirTempC * 0.9
    fun getRpmPercent(rpm: Double): Double = (rpm / redlineRpm) * 100.0
    fun getBoostPercent(boost: Double): Double = (boost / maxBoostBar) * 100.0

    fun isMafNormal(mafGs: Double): Boolean = mafGs in 2.0..90.0
    fun isMafIdleNormal(mafGs: Double): Boolean = mafGs in 2.0..5.0
    fun isCoolantNormal(temp: Double): Boolean = temp in 80.0..105.0
    fun isOilPressureNormal(pressureBar: Double, rpm: Double): Boolean {
        val idleThreshold = minOilPressureIdle
        val runningThreshold = minOilPressureRpm
        val transitionRpm = 1500.0
        val factor = ((rpm - transitionRpm).coerceIn(-transitionRpm, transitionRpm) + transitionRpm) / (2 * transitionRpm)
        val interpolatedThreshold = idleThreshold + factor * (runningThreshold - idleThreshold)
        return pressureBar >= interpolatedThreshold
    }
    fun getFuelTrimStatus(stft: Double, ltft: Double): FuelTrimStatus {
        val total = stft + ltft
        return when {
            total > 15.0 -> FuelTrimStatus.LEAN
            total < -15.0 -> FuelTrimStatus.RICH
            kotlin.math.abs(total) > 10.0 -> FuelTrimStatus.WARNING
            else -> FuelTrimStatus.NORMAL
        }
    }

    companion object {
        val INSTANCE = AstraJ14TurboCalibration()
        val SUPPORTED_TURBO_PIDS = listOf(
            OBDPID.BOOST_PRESSURE, OBDPID.VGT_CONTROL, OBDPID.WASTEGATE_CONTROL,
            OBDPID.TURBO_RPM, OBDPID.CHARGE_AIR_COOLER_TEMP,
            OBDPID.EGT_BANK1, OBDPID.EGT_BANK2, OBDPID.OIL_TEMP,
            OBDPID.ACTUAL_TORQUE, OBDPID.DEMAND_TORQUE, OBDPID.REFERENCE_TORQUE,
            OBDPID.FUEL_RAIL_PRESSURE, OBDPID.INTAKE_PRESSURE,
            OBDPID.COOLANT_TEMP, OBDPID.INTAKE_TEMP, OBDPID.ENGINE_LOAD,
            OBDPID.BAROMETRIC_PRESSURE, OBDPID.SHORT_TERM_FUEL_TRIM_BANK1,
            OBDPID.LONG_TERM_FUEL_TRIM_BANK1
        )
        val DASHBOARD_PRESET = DashboardPreset(
            id = "astra_j_14_turbo",
            name = "Opel Astra J 1.4 Turbo",
            themeName = "CANOPO",
            primaryGaugeIds = setOf(
                "rpm",
                "speed",
                "boost",
                "coolant",
                "oil_temp",
                "charge_air",
                "throttle",
                "fuel_level",
                "battery",
                "torque",
                "wastegate"
            ),
            createdAt = System.currentTimeMillis()
        )
        val TURBO_SPECIFIC_PRESET = TurboSpecificPreset(
            id = "astra_j_14_turbo_monitor",
            name = "Astra J Turbo Monitoring",
            gaugeIds = listOf(
                "turbo_rpm",
                "boost_pressure",
                "wastegate_position",
                "charge_air_temp",
                "intake_air_temp",
                "turbo_health_score"
            ),
            createdAt = System.currentTimeMillis()
        )
        val RECOMMENDED_PIDS = listOf(
            OBDPID.RPM, OBDPID.SPEED, OBDPID.COOLANT_TEMP, OBDPID.THROTTLE,
            OBDPID.ENGINE_LOAD, OBDPID.BOOST_PRESSURE, OBDPID.EGT_BANK1,
            OBDPID.CHARGE_AIR_COOLER_TEMP, OBDPID.FUEL_LEVEL, OBDPID.BATTERY_VOLTAGE,
            OBDPID.MAF_RATE, OBDPID.ACTUAL_TORQUE, OBDPID.OIL_TEMP,
            OBDPID.TIMING_ADVANCE, OBDPID.INTAKE_TEMP, OBDPID.ENGINE_FUEL_RATE,
            OBDPID.WASTEGATE_CONTROL, OBDPID.INTAKE_PRESSURE, OBDPID.SHORT_TERM_FUEL_TRIM_BANK1,
            OBDPID.LONG_TERM_FUEL_TRIM_BANK1, OBDPID.O2_VOLTAGE_B1S1, OBDPID.O2_VOLTAGE_B1S2,
            OBDPID.BAROMETRIC_PRESSURE, OBDPID.FUEL_RAIL_PRESSURE
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
            MaintenanceItem(type = MaintenanceType.COOLANT, intervalKm = 80000),
            MaintenanceItem(type = MaintenanceType.BRAKE_PADS, intervalKm = 30000),
            MaintenanceItem(type = MaintenanceType.SPARK_PLUGS, intervalKm = 60000),
            MaintenanceItem(type = MaintenanceType.TURBO_BOOST_CHECK, intervalKm = 45000)
        )
        val KNOWN_ISSUES = listOf(
            KnownIssue("Kettenspanner", "Rattern bei Kaltstart, P0340/P0341/P1345", "80.000-150.000 km", "Oelqualitaetaet und Oelwechselintervalle einhalten, Kettenspanner ersetzen"),
            KnownIssue("MAF-Sensor", "Rauer Leerlauf, Leistungsverlust, P0100-P0103", "60.000-120.000 km", "MAF-Sensor mit speziellem Reiniger reinigen, Luftmassenmesser prufen"),
            KnownIssue("Wastegate-Stellglied", "Rasseln, Ladedruck-Schwankungen, P0234/P0235", "80.000-150.000 km", "Wastegate-Stellglied auf Freigang prufen, O-Ring kontrollieren"),
            KnownIssue("PCV-Ventil", "Oelverbrauch, blauer Rauch, P1100/P1101", "60.000-100.000 km", "Zylinderkopfhaube mit Ventilen ersetzen, Often PCV-Pruefung"),
            KnownIssue("Kuhlmittel-Temperaturfuhler", "Kalte Motorstartprobleme, P0116/P0117", "80.000-150.000 km", "Kuhlmittel-Temperatursensor ersetzen"),
            KnownIssue("Turbo-Ladedruck", "Leistungsverlust bei hoher Drehzahl", "100.000+ km", "Ladedrucksensor und Wastegate-Pruefung"),
            KnownIssue("Zundkerzen", "Zundungsaussetzer, schlechtes Startverhalten", "30.000-60.000 km", "Zundkerzen erneuern, Elektrodenabstand prufen")
        )
        val MAINTENANCE_INTERVALS = listOf(
            MaintenanceInterval(
                item = MaintenanceType.OIL_CHANGE,
                intervalKm = 15000,
                intervalMonths = 12,
                specification = "Dexos2 5W-30",
                capacity = "4.5L",
                notes = "Oelwechselintervall bei Sportfahrweise reduzieren"
            ),
            MaintenanceInterval(
                item = MaintenanceType.SPARK_PLUGS,
                intervalKm = 30000,
                specification = "NGK LZKR6B-10E",
                notes = "Elektrodenabstand 0.75mm"
            ),
            MaintenanceInterval(
                item = MaintenanceType.AIR_FILTER,
                intervalKm = 30000
            ),
            MaintenanceInterval(
                item = MaintenanceType.TURBO_BOOST_CHECK,
                intervalKm = 60000,
                specification = "Ladedruck-Prüfung",
                notes = "Ladedrucksensor und Wastegate-Stellglied prüfen, O-Ring kontrollieren"
            ),
            MaintenanceInterval(
                item = MaintenanceType.COOLANT,
                intervalKm = 150000,
                intervalMonths = 24,
                specification = "Dex-Cool (Orangefarben)",
                capacity = "5.7L",
                notes = "Erstwechsel 150.000 km, danach alle 40.000 km / 24 Monate"
            ),
            MaintenanceInterval(
                item = MaintenanceType.TURBO_INSPECTION,
                intervalKm = 30000,
                notes = "Visuelle Inspektion alle 30.000 km, Drucktest alle 60.000 km"
            ),
            MaintenanceInterval(
                item = MaintenanceType.TRANSMISSION_FLUID,
                intervalKm = 60000,
                specification = "75W-80 GL-4",
                capacity = "1.7-1.8L",
                notes = "Getrag M32 – Fruehzeitiger Verschleiss bei Pre-2012 Modellen"
            )
        )
        val PROBLEM_MILEAGE_MAP = listOf(
            ProblemMileageMap("Zuendspulen", 60000, 100000, 2, "Einzelaussetzer, P0300-P0304"),
            ProblemMileageMap("MAF-Sensor", 60000, 100000, 2, "Luftmassenmesser Verschmutzung/Defekt"),
            ProblemMileageMap("PCV-Ventil", 80000, 120000, 2, "Druckregelventil im Zylinderkopfdeckel"),
            ProblemMileageMap("Wasserpumpe", 80000, 150000, 3, "Kuehlmittelverlust, Lagergeraeusche"),
            ProblemMileageMap("Steuerkette", 100000, 150000, 3, "Kettenverlaengerung, Kaltstart-Rattern"),
            ProblemMileageMap("Turbolader", 120000, 180000, 3, "Lagerschaden, Oelleckage, Wastegate-Verschleiss"),
            ProblemMileageMap("Kolbenringe", 100000, 150000, 3, "Oelverbrauch, Kompressionsverlust"),
            ProblemMileageMap("Getriebe M32", 80000, 120000, 3, "Pre-2012 Modelle – Lager und Synchronringe"),
            ProblemMileageMap("Wastegate-Stellglied", 100000, 150000, 2, "Feder ermuedet, O-Ring poroes")
        )
    }
}

data class Z14XELCalibration(
    val redlineRpm: Int = 6200,
    val rpmWarning: Int = 5800,
    val idleRpm: Int = 750,
    val maxTorqueNm: Double = 135.0,
    val maxPowerKw: Double = 90.0,
    val maxPowerHp: Double = 122.0,
    val maxOilTempC: Double = 120.0,
    val optimalOilTempMin: Double = 90.0,
    val optimalOilTempMax: Double = 110.0,
    val maxCoolantTempC: Double = 105.0,
    val maxIntakeAirTempC: Double = 60.0,
    val oilCapacityLiters: Double = 4.5,
    val engineCode: String = "Z14XEL",
    val gmEngineCode: String = "LA14XER",
    val fuelType: String = "Benzin (95 RON min)",
    val fuelTankLiters: Double = 56.0,
    val batteryAh: Int = 70,
    val alternatorV: Double = 14.0,
    val coolantCapacity: Double = 5.7,
    val sparkPlugType: String = "NGK LZKR6A-11 / Bosch FR7LDE",
    val sparkPlugGap: Double = 0.8,
    val ecuType: String = "Bosch ME17.9.2",
    val compressionRatio: String = "11.0:1",
    val displacement: String = "1364cc (1.4L)",
    val boreStroke: String = "72.5mm x 82.6mm",
    val valveConfig: String = "DOHC 16V, DCVCP Nockenwellen",
    val emissionStandard: String = "Euro 5",
    val fuelConsumptionCombined: Double = 5.8,
    val fuelConsumptionUrban: Double = 7.5,
    val fuelConsumptionExtraUrban: Double = 4.8,
    val co2Emissions: Int = 135,
    val topSpeed: Int = 190,
    val accel0to100: Double = 10.5,
    val recommendedOil: String = "Dexos2 5W-30",
    val oilChangeIntervalKm: Int = 15000,
    val sparkPlugIntervalKm: Int = 60000,
    val airFilterIntervalKm: Int = 30000,
    val coolantIntervalKm: Int = 100000,
    val timingChainIntervalKm: Int = 150000,
    val vvtSystem: String = "DCVCP (Dual Continuous Variable Cam Phasing)",
    val maxRpmSustained: Int = 5800,
    val powerCurvePeakRpm: Int = 5200,
    val torqueCurvePeakRpm: Int = 3500,
    val optimalRpmMin: Int = 1500,
    val optimalRpmMax: Int = 3500,
    val powerBandRpmMin: Int = 4500,
    val powerBandRpmMax: Int = 5500
) {
    fun isRpmWarning(rpm: Double): Boolean = rpm >= rpmWarning
    fun isRpmRedline(rpm: Double): Boolean = rpm >= redlineRpm
    fun isOilTempWarning(temp: Double): Boolean = temp >= maxOilTempC * 0.9
    fun isOilTempCritical(temp: Double): Boolean = temp >= maxOilTempC
    fun isCoolantWarning(temp: Double): Boolean = temp >= maxCoolantTempC * 0.95
    fun isCoolantCritical(temp: Double): Boolean = temp >= maxCoolantTempC
    fun getRpmPercent(rpm: Double): Double = (rpm / redlineRpm) * 100.0
    fun isMafNormal(mafGs: Double): Boolean = mafGs in 2.0..70.0

    companion object {
        val INSTANCE = Z14XELCalibration()
        val RECOMMENDED_PIDS = listOf(
            OBDPID.RPM, OBDPID.SPEED, OBDPID.COOLANT_TEMP, OBDPID.THROTTLE,
            OBDPID.ENGINE_LOAD, OBDPID.EGT_BANK1, OBDPID.FUEL_LEVEL, OBDPID.BATTERY_VOLTAGE,
            OBDPID.MAF_RATE, OBDPID.ACTUAL_TORQUE, OBDPID.OIL_TEMP,
            OBDPID.TIMING_ADVANCE, OBDPID.INTAKE_TEMP, OBDPID.ENGINE_FUEL_RATE,
            OBDPID.INTAKE_PRESSURE, OBDPID.SHORT_TERM_FUEL_TRIM_BANK1,
            OBDPID.LONG_TERM_FUEL_TRIM_BANK1, OBDPID.O2_VOLTAGE_B1S1, OBDPID.O2_VOLTAGE_B1S2,
            OBDPID.BAROMETRIC_PRESSURE, OBDPID.FUEL_RAIL_PRESSURE, OBDPID.COMMANDED_EGR,
            OBDPID.THROTTLE_POSITION_MODE22_Z14, OBDPID.EGR_POSITION_MODE22_Z14,
            OBDPID.MAP_SENSOR_MODE22_Z14, OBDPID.IDLE_AIR_CONTROL_Z14,
            OBDPID.KNOCK_RETARD_Z14, OBDPID.IGNITION_DWELL_Z14,
            OBDPID.CATALYST_TEMP_B1S1_MODE22_Z14, OBDPID.STFT_MODE22_Z14,
            OBDPID.LTFT_MODE22_Z14, OBDPID.FUEL_PUMP_STATUS_Z14, OBDPID.EVAP_PURGE_DUTY_Z14
        )
        val DASHBOARD_PRESET = DashboardPreset(
            id = "astra_j_14_na",
            name = "Opel Astra J 1.4 (Z14XEL)",
            themeName = "CANOPO",
            primaryGaugeIds = setOf(
                "rpm", "speed", "coolant", "oil_temp", "throttle",
                "fuel_level", "battery", "torque", "maf", "intake_temp"
            ),
            createdAt = System.currentTimeMillis()
        )
        val ALERT_CONFIG = AlertConfig(
            speedWarning = 170f,
            speedWarningEnabled = false,
            coolantWarning = 105f,
            coolantWarningEnabled = true,
            fuelWarning = 10f,
            fuelWarningEnabled = true,
            rpmWarning = 5800f,
            rpmWarningEnabled = true,
            batteryLowWarning = 11.8f,
            batteryLowWarningEnabled = true
        )
    }
}

data class ColdStartState(
    val phase: ColdStartPhase = ColdStartPhase.NOT_STARTED,
    val coolantTempStart: Double = 0.0,
    val coolantTempCurrent: Double = 0.0,
    val rpmStart: Double = 0.0,
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val oilTempStart: Double = 0.0,
    val oilTempCurrent: Double = 0.0,
    val targetCoolantTemp: Double = 90.0,
    val chargeAirTemp: Double = 0.0,
    val engineLoad: Double = 0.0,
    val fuelSystemStatus: FuelSystemState = FuelSystemState.UNKNOWN
) {
    val warmupProgress: Float get() = when {
        coolantTempCurrent <= 0.0 -> 0f
        coolantTempCurrent >= targetCoolantTemp -> 1f
        else -> ((coolantTempCurrent + 40.0) / (targetCoolantTemp + 40.0)).toFloat().coerceIn(0f, 1f)
    }
    val estimatedTimeRemaining: Long get() = when {
        warmupProgress >= 1f -> 0L
        warmupProgress <= 0f -> 0L
        else -> ((1.0 - warmupProgress) * 300.0).toLong().coerceAtMost(300L)
    }
    val isTurboWarm: Boolean get() = oilTempCurrent >= 60.0
    val isReadyForBoost: Boolean get() = coolantTempCurrent >= 80.0 && oilTempCurrent >= 50.0
    val turboWarmupPercent: Float get() = when {
        oilTempCurrent <= 0.0 -> 0f
        oilTempCurrent >= 90.0 -> 1f
        else -> (oilTempCurrent / 90.0).toFloat().coerceIn(0f, 1f)
    }
}

enum class FuelTrimStatus { NORMAL, WARNING, LEAN, RICH }

data class KnownIssue(
    val name: String,
    val symptoms: String,
    val typicalMileage: String,
    val prevention: String
)

enum class FuelSystemState(val label: String) {
    UNKNOWN("Unbekannt"),
    OPEN_LOOP_NO_FAULT("Offene Schleife - kein Fehler"),
    OPEN_LOOP_FAULT("Offene Schleife - Fehler"),
    CLOSED_LOOP("Geschlossene Schleife"),
    OPEN_LOOP_ENGINE_OFF("Schleife offen - Motor aus")
}

data class TurboHealthMonitor(
    val boostPressureKpa: Double = 0.0,
    val wastegatePosition: Double = 0.0,
    val turboRpm: Double = 0.0,
    val chargeAirTempC: Double = 0.0,
    val intakeAirTempC: Double = 0.0,
    val ambientTempC: Double = 0.0,
    val barometricPressureKpa: Double = 100.0,
    val targetBoostKpa: Double = 60.0,
    val sampleCount: Int = 0
) {
    val boostBar: Double get() = boostPressureKpa / 100.0
    val baroBar: Double get() = barometricPressureKpa / 100.0
    val relativeBoostBar: Double get() = (boostBar - baroBar).coerceAtLeast(0.0)
    val targetBoostBar: Double get() = targetBoostKpa / 100.0
    val boostDeviationPercent: Double get() = if (targetBoostKpa > 0) ((relativeBoostBar * 100.0 - targetBoostKpa) / targetBoostKpa) * 100.0 else 0.0
    val isOverboost: Boolean get() = relativeBoostBar > 1.0
    val isUnderboost: Boolean get() = (relativeBoostBar < targetBoostBar * 0.5) && sampleCount > 10
    val wastegateHealth: WastegateHealth get() = when {
        wastegatePosition < 5.0 -> WastegateHealth.STUCK_CLOSED
        wastegatePosition > 95.0 -> WastegateHealth.STUCK_OPEN
        isUnderboost && wastegatePosition > 70.0 -> WastegateHealth.WASTEGATE_LEAK
        isOverboost && wastegatePosition < 30.0 -> WastegateHealth.WASTEGATE_STUCK
        else -> WastegateHealth.HEALTHY
    }
    val turboHealthStatus: TurboHealthStatus get() = when {
        isOverboost -> TurboHealthStatus.OVERBOOST
        isUnderboost -> TurboHealthStatus.UNDERBOOST
        wastegateHealth != WastegateHealth.HEALTHY -> TurboHealthStatus.WASTEGATE_ISSUE
        chargeAirTempC > 65.0 -> TurboHealthStatus.INTERCOOLER_EFFICIENCY
        else -> TurboHealthStatus.HEALTHY
    }
    val chargeAirEfficiencyPercent: Float get() = when {
        intakeAirTempC <= ambientTempC -> 100f
        chargeAirTempC > ambientTempC + 30 -> 0f
        else -> ((1.0 - ((chargeAirTempC - ambientTempC) / 30.0)) * 100).toFloat().coerceIn(0f, 100f)
    }
}

enum class WastegateHealth(val label: String, val severity: Int) {
    HEALTHY("Gesund", 0),
    WASTEGATE_LEAK("Wastegate undicht", 2),
    WASTEGATE_STUCK("Wastegate klemmt", 3),
    STUCK_CLOSED("Wastegate geschlossen", 3),
    STUCK_OPEN("Wastegate offen", 3)
}

enum class TurboHealthStatus(val label: String, val colorHex: Long) {
    HEALTHY("Turbo OK", 0xFF44FF88),
    OVERBOOST("Uberladung!", 0xFFFF4444),
    UNDERBOOST("Unterladung!", 0xFFFF8C00),
    WASTEGATE_ISSUE("Wastegate-Problem", 0xFFFFE066),
    INTERCOOLER_EFFICIENCY("Ladeluftkuhler-Problem", 0xFFFFAB40)
}

data class DTCDetails(
    val code: String,
    val description: String,
    val system: String,
    val severity: DTCSeverity,
    val possibleCauses: List<String>,
    val recommendedActions: List<String>,
    val isAstraJCommon: Boolean = false,
    val relatedCodes: List<String> = emptyList()
)

enum class DTCSeverity(val label: String, val colorHex: Long) {
    INFO("Info", 0xFF42A5F5),
    WARNING("Warnung", 0xFFFFE066),
    CRITICAL("Kritisch", 0xFFFF4444),
    PERFORMANCE("Leistung", 0xFFFF8C00)
}

enum class ChainHealth { UNKNOWN, GOOD, WARNING, CRITICAL }

enum class PCVHealth { UNKNOWN, GOOD, WEAK, FAILED }

enum class FuelSystemHealth { UNKNOWN, LEAN, RICH, NORMAL }

enum class DriveStyle(val label: String) {
    ECONOMICAL("Sparend"),
    BALANCED("Ausgewogen"),
    AGGRESSIVE("Sportlich")
}

enum class BoostStatus(val label: String) {
    LOW("Unterladung"),
    NORMAL("Normal"),
    HIGH("Erhoeht"),
    OVERBOOST("Ueberladung!")
}

data class BoostAnalysis(
    val actual: Double = 0.0,
    val target: Double = 0.0,
    val deviation: Double = 0.0,
    val status: BoostStatus = BoostStatus.NORMAL,
    val healthScore: Int = 100
)

data class WastegateAnalysisResult(
    val dutyCycle: Double = 0.0,
    val position: Double = 0.0,
    val status: String = "",
    val healthScore: Int = 100,
    val recommendations: List<String> = emptyList()
)

data class TurboHealthResult(
    val overallScore: Int = 100,
    val boostScore: Int = 100,
    val wastegateScore: Int = 100,
    val egtScore: Int = 100,
    val speedScore: Int = 100,
    val status: TurboHealthStatus = TurboHealthStatus.HEALTHY
)

data class ChainHealthResult(
    val healthScore: Int = 100,
    val chainHealth: ChainHealth = ChainHealth.UNKNOWN,
    val timingCorrelation: Double = 0.0,
    val hasDtcFault: Boolean = false,
    val recommendation: String = ""
)

data class VehicleWarning(
    val id: String,
    val priority: WarningPriority,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class WarningPriority { INFO, WARNING, CRITICAL }

data class ProcessedDTC(
    val code: String,
    val description: String,
    val severity: DTCSeverity,
    val category: String,
    val recommendation: String
)

data class Mode22Data(
    val pid: String,
    val value: Double,
    val unit: String,
    val rawBytes: ByteArray = ByteArray(0),
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Mode22Data) return false
        return pid == other.pid && value == other.value && unit == other.unit &&
                rawBytes.contentEquals(other.rawBytes) && timestamp == other.timestamp
    }
    override fun hashCode(): Int {
        var result = pid.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + rawBytes.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

enum class MonitorSupport(val label: String) {
    COMPLETE("Bestanden"),
    INCOMPLETE("Offen"),
    NOT_SUPPORTED("Nicht unterstützt");

    companion object {
        fun fromBooleans(completed: Boolean, supported: Boolean): MonitorSupport = when {
            !supported -> NOT_SUPPORTED
            completed -> COMPLETE
            else -> INCOMPLETE
        }
    }
}

data class ReadinessMonitorData(
    val monitors: List<ReadinessMonitorEntry> = emptyList(),
    val milOn: Boolean = false,
    val dtcCount: Int = 0
) {
    val completedCount: Int get() = monitors.count { it.status == MonitorSupport.COMPLETE }
    val incompleteCount: Int get() = monitors.count { it.status == MonitorSupport.INCOMPLETE }
    val supportedCount: Int get() = monitors.count { it.status != MonitorSupport.NOT_SUPPORTED }
    val totalCount: Int get() = monitors.size
    val progressPercent: Float get() = if (supportedCount == 0) 0f else completedCount.toFloat() / supportedCount
    val allComplete: Boolean get() = monitors.all { it.status == MonitorSupport.COMPLETE || it.status == MonitorSupport.NOT_SUPPORTED }
    val hasIncompleteMonitors: Boolean get() = monitors.any { it.status == MonitorSupport.INCOMPLETE }
}

data class ReadinessMonitorEntry(
    val name: String,
    val status: MonitorSupport
)

data class LambdaSensorData(
    val preCatVoltage: Double = 0.0,
    val preCatLambda: Double = 0.0,
    val preCatHeaterActive: Boolean = false,
    val postCatVoltage: Double = 0.0,
    val postCatLambda: Double = 0.0,
    val postCatHeaterActive: Boolean = false,
    val crossCountRate: Int = 0,
    val voltageHistory: List<Float> = emptyList(),
    val fuelTrimShort: Double = 0.0,
    val fuelTrimLong: Double = 0.0
) {
    val preCatStatus: LambdaStatus get() = when {
        preCatLambda in 0.95..1.05 -> LambdaStatus.IDEAL
        preCatLambda in 0.85..1.15 -> LambdaStatus.OK
        else -> LambdaStatus.DEVIATION
    }
    val postCatStatus: LambdaStatus get() = when {
        postCatVoltage in 0.3..0.7 -> LambdaStatus.IDEAL
        postCatVoltage in 0.1..0.9 -> LambdaStatus.OK
        else -> LambdaStatus.DEVIATION
    }
}

enum class LambdaStatus(val label: String) {
    IDEAL("Ideal"),
    OK("OK"),
    DEVIATION("Abweichung")
}

data class BatteryData(
    val voltage: Double = 0.0,
    val controlModuleVoltage: Double = 0.0,
    val estimatedSOC: Int = 0,
    val isCharging: Boolean = false,
    val voltageHistory: List<Float> = emptyList(),
    val alternatorDuty: Double = 0.0,
    val runTimeSeconds: Double = 0.0
) {
    val voltageStatus: BatteryVoltageStatus get() = when {
        voltage >= 14.0 -> BatteryVoltageStatus.CHARGING
        voltage >= 12.6 -> BatteryVoltageStatus.GOOD
        voltage >= 12.0 -> BatteryVoltageStatus.LOW
        else -> BatteryVoltageStatus.CRITICAL
    }
    val socStatus: SOCStatus get() = when {
        estimatedSOC >= 70 -> SOCStatus.GOOD
        estimatedSOC >= 40 -> SOCStatus.MODERATE
        else -> SOCStatus.LOW
    }
    val trend: BatteryTrend get() {
        if (voltageHistory.size < 3) return BatteryTrend.STABLE
        val recent = voltageHistory.takeLast(3)
        val avgRecent = recent.average()
        val older = voltageHistory.dropLast(3).takeLast(3)
        if (older.isEmpty()) return BatteryTrend.STABLE
        val avgOlder = older.average()
        return when {
            avgRecent - avgOlder > 0.15 -> BatteryTrend.RISING
            avgOlder - avgRecent > 0.15 -> BatteryTrend.FALLING
            else -> BatteryTrend.STABLE
        }
    }
}

enum class BatteryVoltageStatus(val label: String) {
    CHARGING("Lädt"),
    GOOD("Gut"),
    LOW("Niedrig"),
    CRITICAL("Kritisch")
}

enum class SOCStatus(val label: String) {
    GOOD("Gut"),
    MODERATE("Mäßig"),
    LOW("Niedrig")
}

enum class BatteryTrend(val label: String, val icon: String) {
    RISING("Steigend", "+"),
    FALLING("Fallend", "-"),
    STABLE("Stabil", "=")
}

data class EGRData(
    val commandedPercent: Double = 0.0,
    val errorPercent: Double = 0.0,
    val temperature: Double = 0.0,
    val isSupported: Boolean = true
) {
    val valveStatus: EGRValveStatus get() = when {
        !isSupported -> EGRValveStatus.NOT_SUPPORTED
        errorPercent > 15.0 -> EGRValveStatus.ERROR_HIGH
        errorPercent > 5.0 -> EGRValveStatus.WARNING
        else -> EGRValveStatus.NORMAL
    }
}

enum class EGRValveStatus(val label: String) {
    NORMAL("Normal"),
    WARNING("Warnung"),
    ERROR_HIGH("Fehler"),
    NOT_SUPPORTED("Nicht unterstützt")
}

data class EVAPData(
    val purgeDutyCycle: Double = 0.0,
    val tankPressure: Double = 0.0,
    val leakDetected: Boolean = false,
    val isSupported: Boolean = true
) {
    val systemStatus: EVAPSystemStatus get() = when {
        !isSupported -> EVAPSystemStatus.NOT_SUPPORTED
        leakDetected -> EVAPSystemStatus.LEAK_DETECTED
        purgeDutyCycle > 0.0 -> EVAPSystemStatus.ACTIVE
        else -> EVAPSystemStatus.STANDBY
    }
}

enum class EVAPSystemStatus(val label: String) {
    ACTIVE("Aktiv"),
    STANDBY("Bereit"),
    LEAK_DETECTED("Leck erkannt!"),
    NOT_SUPPORTED("Nicht unterstützt")
}

data class VehicleInfoData(
    val vin: String = "",
    val calibrationId: String = "",
    val ecuName: String = "",
    val ecuVersion: String = "",
    val cvn: String = "",
    val cvnValid: Boolean = true,
    val protocol: String = "",
    val supportedModes: List<String> = emptyList()
)

enum class BatteryHealth(val label: String, val severity: Int) {
    GOOD("Gut", 0),
    FAIR("Befriedigend", 1),
    POOR("Schlecht", 2),
    CRITICAL("Kritisch", 3)
}

data class BatteryStatus(
    val voltage: Double = 0.0,
    val soc: Int = 0,
    val health: BatteryHealth = BatteryHealth.GOOD,
    val isCharging: Boolean = false
)

enum class EGRStatus { CLOSED, OPEN, FAULT }

data class EGRHealth(
    val status: EGRStatus = EGRStatus.CLOSED,
    val flowRate: Double = 0.0,
    val errorPercent: Double = 0.0,
    val healthScore: Int = 0
)

enum class LeakSize { SMALL, MEDIUM, LARGE }

data class EVAPStatus(
    val purgeDuty: Double = 0.0,
    val tankPressure: Double = 0.0,
    val hasLeak: Boolean = false,
    val leakSize: LeakSize? = null
)

data class SAIStatus(
    val isActive: Boolean = false,
    val operationTimeSeconds: Long = 0L,
    val healthScore: Int = 0
)

enum class O2SensorType {
    PRECAT_WIDEBAND,
    POSTCAT_NARROWBAND
}

data class LambdaSensorStatus(
    val sensor: O2SensorType = O2SensorType.PRECAT_WIDEBAND,
    val voltage: Double = 0.0,
    val lambda: Double = 1.0,
    val heaterStatus: Boolean = true,
    val healthScore: Int = 0,
    val crossCountRate: Double = 0.0
)

enum class MonitorType(val label: String, val bitPosition: Int) {
    MISFIRE("Zundaussetzer", 0),
    FUEL_SYSTEM("Kraftstoffsystem", 1),
    COMPONENTS("Komponenten", 2),
    CATALYST("Katalysator", 3),
    O2_SENSOR("O2-Sensor", 8),
    O2_HEATER("O2-Heizung", 9),
    EGR("EGR-System", 10),
    EVAP("EVAP-System", 5),
    SAI("Sekundaerluft", 6),
    GPF("GPF-Filter", -1)
}

data class EmissionsReadinessMonitor(
    val monitor: MonitorType,
    val isComplete: Boolean = false,
    val isSupported: Boolean = true
)
