package com.canopobd.protocol

/**
 * GM/Opel BCM (Body Control Module) protocol implementation for Opel Astra J.
 *
 * The BCM communicates over the HS-CAN bus at 500kbps. On the Astra J, the BCM
 * handles all comfort functions: central locking, windows, mirrors, heating,
 * lighting, and wipers.
 *
 * CAN IDs: 0x280 (BCM->CAN), 0x288 (CAN->BCM), 0x388 (BCM diagnostics)
 *
 * BCM DIDs (Data Identifiers):
 * - 0xFF01: Central locking, 0xFF02: Windows, 0xFF03: Mirrors
 * - 0xFF04: Lighting, 0xFF05: Heating, 0xFF06: Wipers
 * - 0xFF07: Horn, 0xFF08: Sunroof, 0xFF09: Seat heating
 * - 0xFF10: Ambient lighting, 0xFF11: Climate control
 */
object BCMProtocol {

    const val TAG = "BCMProtocol"

    const val BCM_TX_CAN_ID = "280"
    const val BCM_RX_CAN_ID = "288"
    const val BCM_DIAG_TX = "388"
    const val BCM_DIAG_RX = "308"

    const val UDS_SID_READ_DATA = 0x22
    const val UDS_SID_WRITE_DATA = 0x2E
    const val UDS_SID_IO_CONTROL = 0x2F
    const val UDS_SID_ROUTINE = 0x31
    const val UDS_SID_SESSION = 0x10
    const val UDS_SID_SECURITY = 0x27
    const val UDS_SID_TESTER_PRESENT = 0x3E

    // Opel Astra J ECU Addresses (GMLAN)
    object ECU {
        const val ECM_TX = "7E0"      // Engine Control Module
        const val ECM_RX = "7E8"
        const val TCM_TX = "7E1"       // Transmission Control Module
        const val TCM_RX = "7E9"
        const val BCM_TX = "7C0"       // Body Control Module
        const val BCM_RX = "7C8"
        const val IPC_TX = "7C3"       // Instrument Panel Cluster
        const val IPC_RX = "7CB"
        const val ABS_TX = "7C2"       // ABS Module
        const val ABS_RX = "7CA"
        const val SRS_TX = "7C5"       // Airbag Module
        const val SRS_RX = "7CD"
    }

    // PSA/Stellantis CAN IDs (Astra J uses some PSA components)
    object PSA_CAN {
        const val PORTEC_TX = "74B"    // Door Control Unit (Windows)
        const val BMF_TX = "752"       // Body Module Front
        const val BSI_TX = "76B"       // Built-in Systems Interface
        const val DDM_TX = "240"       // Driver Door Module
        const val PDM_TX = "340"       // Passenger Door Module
        const val RDM_TX = "440"       // Rear Door Module
    }

    object DIDs {
        const val DOOR_LOCK_STATUS = 0xFF01
        const val WINDOW_STATUS = 0xFF02
        const val MIRROR_STATUS = 0xFF03
        const val LIGHTING_STATUS = 0xFF04
        const val HEATING_STATUS = 0xFF05
        const val WIPER_STATUS = 0xFF06
        const val HORN_STATUS = 0xFF07
        const val SUNROOF_STATUS = 0xFF08
        const val SEAT_HEATING_STATUS = 0xFF09
        const val AMBIENT_LIGHT = 0xFF10
        const val CLIMATE_STATUS = 0xFF11
        const val BCM_PART_NUMBER = 0xF192
        const val VIN = 0xF190
        const val ECU_HARDWARE = 0xF191
        const val ECU_SOFTWARE = 0xF192
        const val CALIBRATION_ID = 0xF193
    }

    object DoorLock {
        const val UNLOCK_DRIVER = 0x01
        const val LOCK_DRIVER = 0x11
        const val UNLOCK_ALL = 0x0F
        const val LOCK_ALL = 0x1F
        const val UNLOCK_TAILGATE = 0x20
        const val LOCK_TAILGATE = 0x30
        const val UNLOCK_FUEL = 0x40

        // CAN Frame Commands (BMF/752)
        fun lockAllFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x01.toByte(), 0x1F.toByte())
        fun unlockAllFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x01.toByte(), 0x0F.toByte())
        fun unlockDriverFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x01.toByte(), 0x01.toByte())
    }

    object Window {
        const val CMD_PREFIX = 0x2EFF02
        
        const val CAN_ID_PORTEC = "74B"
        const val CAN_ID_BMF = "752"
        const val CAN_ID_BSI = "76B"
        
        // Window indices
        const val WINDOW_ALL = 0x00
        const val WINDOW_DRIVER = 0x01
        const val WINDOW_PASSENGER = 0x02
        const val WINDOW_REAR_LEFT = 0x03
        const val WINDOW_REAR_RIGHT = 0x04
        const val WINDOW_SUNROOF = 0x05
        
        // Directions (position in %)
        const val DIRECTION_UP = 0x00
        const val DIRECTION_DOWN = 0x64  // 100%
        const val DIRECTION_STOP = 0xFF
        
        // Preset positions
        const val POSITION_25 = 0x19  // 25%
        const val POSITION_50 = 0x32  // 50%
        const val POSITION_75 = 0x4B  // 75%
        
        fun openDriver() = hexToBytes("2EFF0264")
        fun closeDriver() = hexToBytes("2EFF0200")
        fun stopDriver() = hexToBytes("2EFF02FF")
        fun openPassenger() = hexToBytes("2EFF0264")
        fun closePassenger() = hexToBytes("2EFF0200")
        fun openRearLeft() = hexToBytes("2EFF0264")
        fun closeRearLeft() = hexToBytes("2EFF0200")
        fun openRearRight() = hexToBytes("2EFF0264")
        fun closeRearRight() = hexToBytes("2EFF0200")
        fun openAll() = hexToBytes("2EFF0264646464")
        fun closeAll() = hexToBytes("2EFF0200000000")
        
        fun buildDirectFrame(windowByte: Int, direction: Int): ByteArray {
            return byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x02.toByte(), windowByte.toByte(), direction.toByte())
        }
        
        fun buildPositionFrame(windowByte: Int, position: Int): ByteArray {
            val pos = position.coerceIn(0, 100)
            return byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x02.toByte(), windowByte.toByte(), pos.toByte())
        }
    }

    object Mirror {
        const val CMD_PREFIX = 0x2EFF03
        const val FOLD = 0x04
        const val UNFOLD = 0x05
        const val HEATING_ON = 0x08
        const val HEATING_OFF = 0x00
        const val LEFT_MIRROR = 0x01
        const val RIGHT_MIRROR = 0x02
        const val BOTH_MIRRORS = 0x03
        
        // Mirror movement directions
        const val MOVE_UP = 0x01
        const val MOVE_DOWN = 0x02
        const val MOVE_LEFT = 0x04
        const val MOVE_RIGHT = 0x08
        
        fun foldFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x03.toByte(), 0x04.toByte())
        fun unfoldFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x03.toByte(), 0x05.toByte())
        fun heatingOnFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x03.toByte(), 0x08.toByte())
        fun heatingOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x03.toByte(), 0x00.toByte())
        
        fun moveFrame(mirror: Int, direction: Int): ByteArray {
            return byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x03.toByte(), mirror.toByte(), direction.toByte())
        }
    }

    object Lighting {
        const val CMD_PREFIX = 0x2EFF04
        const val PARKING_ON = 0x01
        const val PARKING_OFF = 0x00
        const val DRL_ON = 0x02
        const val DRL_OFF = 0x04
        const val CORNERING_ON = 0x10
        const val CORNERING_OFF = 0x00
        const val COMING_HOME_ENABLE = 0x20
        const val COMING_HOME_DISABLE = 0x00
        const val LEAVING_HOME_ENABLE = 0x40
        const val LEAVING_HOME_DISABLE = 0x00
        const val FOG_LIGHTS_ON = 0x80
        const val FOG_LIGHTS_OFF = 0x00
        
        // Ambient lighting
        const val AMBIENT_OFF = 0x00
        const val AMBIENT_LOW = 0x32  // 50%
        const val AMBIENT_MEDIUM = 0x64  // 100%
        const val AMBIENT_HIGH = 0x96  // 150% (if supported)
        
        fun parkingOnFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), 0x01.toByte())
        fun parkingOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), 0x00.toByte())
        fun drlOnFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), 0x02.toByte())
        fun drlOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), 0x04.toByte())
        fun comingHomeFrame(enable: Boolean) = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), if (enable) 0x20.toByte() else 0x00.toByte())
        fun leavingHomeFrame(enable: Boolean) = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), if (enable) 0x40.toByte() else 0x00.toByte())
        fun fogLightsFrame(enable: Boolean) = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), if (enable) 0x80.toByte() else 0x00.toByte())
    }

    object Heating {
        const val CMD_PREFIX = 0x2EFF05
        const val REAR_ON = 0x01
        const val REAR_OFF = 0x00
        const val FRONT_ON = 0x02
        const val FRONT_OFF = 0x00
        const val STEERING_LEVEL_1 = 0x04
        const val STEERING_LEVEL_2 = 0x08
        const val STEERING_LEVEL_3 = 0x0C
        const val STEERING_OFF = 0x00
        
        fun rearOnFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x05.toByte(), 0x01.toByte())
        fun rearOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x05.toByte(), 0x00.toByte())
        fun frontOnFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x05.toByte(), 0x02.toByte())
        fun frontOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x05.toByte(), 0x00.toByte())
        fun steeringLevelFrame(level: Int) = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x05.toByte(), level.coerceIn(0, 3).toByte())
    }

    object Wiper {
        const val CMD_PREFIX = 0x2EFF06
        const val OFF = 0x00
        const val LOW = 0x01
        const val HIGH = 0x02
        const val INTERMITTENT = 0x03
        const val AUTO = 0x13
        const val REAR_ON = 0x04
        const val REAR_OFF = 0x00
        const val FRONT_WIPE = 0x01
        const val FRONT_WASH = 0x02
        
        fun frontOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x06.toByte(), 0x00.toByte())
        fun frontLowFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x06.toByte(), 0x01.toByte())
        fun frontHighFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x06.toByte(), 0x02.toByte())
        fun frontAutoFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x06.toByte(), 0x13.toByte())
        fun rearOnFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x06.toByte(), 0x04.toByte())
        fun rearOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x06.toByte(), 0x00.toByte())
    }

    object Horn {
        const val CMD_PREFIX = 0x2EFF07
        
        fun honkFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x07.toByte(), 0x01.toByte())
        fun stopFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x07.toByte(), 0x00.toByte())
    }

    object Sunroof {
        const val CMD_PREFIX = 0x2EFF08
        const val OPEN = 0x64
        const val CLOSE = 0x00
        const val STOP = 0xFF
        const val VENT = 0x32  // 50% vent position
        
        fun openFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x08.toByte(), 0x64.toByte())
        fun closeFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x08.toByte(), 0x00.toByte())
        fun stopFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x08.toByte(), 0xFF.toByte())
        fun ventFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x08.toByte(), 0x32.toByte())
    }

    object SeatHeating {
        const val CMD_PREFIX = 0x2EFF09
        const val DRIVER_LEVEL_1 = 0x01
        const val DRIVER_LEVEL_2 = 0x02
        const val DRIVER_LEVEL_3 = 0x03
        const val DRIVER_OFF = 0x00
        const val PASSENGER_LEVEL_1 = 0x04
        const val PASSENGER_LEVEL_2 = 0x08
        const val PASSENGER_LEVEL_3 = 0x0C
        const val PASSENGER_OFF = 0x00
        
        fun driverLevel1Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x01.toByte())
        fun driverLevel2Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x02.toByte())
        fun driverLevel3Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x03.toByte())
        fun driverOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x00.toByte())
        fun passengerLevel1Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), PASSENGER_LEVEL_1.toByte())
        fun passengerLevel2Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), PASSENGER_LEVEL_2.toByte())
        fun passengerLevel3Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), PASSENGER_LEVEL_3.toByte())
        fun passengerOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x00.toByte())
    }

    // UDS Session Types
    object Session {
        const val DEFAULT = 0x01
        const val PROGRAMMING = 0x02
        const val EXTENDED = 0x03
        const val EOL = 0x04  // End of Line
    }

    // UDS Security Access Levels
    object SecurityLevel {
        const val LEVEL_1 = 0x01  // Basic diagnostics
        const val LEVEL_3 = 0x03  // Extended diagnostics
        const val LEVEL_5 = 0x05  // Configuration
        const val LEVEL_7 = 0x07  // Calibration
        const val LEVEL_9 = 0x09  // Programming
    }

    // Common UDS Routine IDs
    object Routines {
        const val RESET_ADAPTIVES = 0x0201
        const val CLEAR_LEARN_VALUES = 0x0202
        const val STEERING_ANGLE_CALIBRATION = 0x0301
        const val TPMS_RESET = 0x0302
        const val OIL_RESET = 0x0303
        const val INSPECTION_RESET = 0x0304
        const val BRAKE_Pedal_RESET = 0x0305
        const val CLUTCH_RESET = 0x0306
    }

    // DTC (Diagnostic Trouble Codes) Services
    object DTC {
        // Mode 03 - Read DTCs
        const val MODE_READ = 0x03
        // Mode 07 - Read Pending DTCs
        const val MODE_PENDING = 0x07
        // Mode 0A - Read Permanent DTCs
        const val MODE_PERMANENT = 0x0A
        // Mode 04 - Clear DTCs
        const val MODE_CLEAR = 0x04
        
        // DTC Status Byte Masks
        const val STATUS_MALFUNCTION = 0x01
        const val STATUS_PENDING = 0x08
        const val STATUS_PERMANENT = 0x20
        
        fun buildClearDTCs() = "04"
        fun buildReadDTCs() = "03"
        fun buildReadPendingDTCs() = "07"
        fun buildReadPermanentDTCs() = "0A"
    }

    // Mode 01 PIDs (Live Data)
    object Mode01 {
        const val SUPPORTED_PIDS = 0x00
        const val DTC_STATUS = 0x01
        const val FUEL_SYSTEM_STATUS = 0x03
        const val ENGINE_LOAD = 0x04
        const val COOLANT_TEMP = 0x05
        const val FUEL_PRESSURE = 0x0A
        const val INTAKE_MAP = 0x0B
        const val ENGINE_RPM = 0x0C
        const val VEHICLE_SPEED = 0x0D
        const val TIMING_ADVANCE = 0x0E
        const val INTAKE_TEMP = 0x0F
        const val MAF_RATE = 0x10
        const val THROTTLE_POSITION = 0x11
        const val O2_VOLTAGE = 0x14
        const val O2_VOLTAGE_B1S2 = 0x15
        const val O2_VOLTAGE_B1S3 = 0x16
        const val O2_VOLTAGE_B1S4 = 0x17
        const val FUEL_LEVEL = 0x2F
        const val COMMANDED_EGR = 0x2C
        const val EGR_ERROR = 0x2D
        const val COMMANDED_EVAP = 0x2E
        const val FUEL_TANK_LEVEL = 0x2F
        const val ABSOLUTE_LOAD = 0x43
        const val RELATIVE_THROTTLE = 0x45
        const val AMBIENT_TEMP = 0x46
        const val ABSOLUTE_THROTTLE_B = 0x47
        const val ABSOLUTE_THROTTLE_C = 0x48
        const val ACC_PEDAL_D = 0x49
        const val ACC_PEDAL_E = 0x4A
        const val ACC_PEDAL_F = 0x4B
        const val THROTTLE_ACTUATOR = 0x4C
        const val RUN_TIME = 0x1F
        const val DISTANCE_MIL = 0x21
        const val DTC_CNT = 0x22
        const val FUEL_RAIL_PRESSURE = 0x59
        
        val PID_NAMES = mapOf(
            SUPPORTED_PIDS to "Supported PIDs",
            DTC_STATUS to "DTC Status",
            FUEL_SYSTEM_STATUS to "Fuel System Status",
            ENGINE_LOAD to "Engine Load (%)",
            COOLANT_TEMP to "Coolant Temp (°C)",
            FUEL_PRESSURE to "Fuel Pressure (kPa)",
            INTAKE_MAP to "Intake Manifold Pressure (kPa)",
            ENGINE_RPM to "Engine RPM",
            VEHICLE_SPEED to "Vehicle Speed (km/h)",
            TIMING_ADVANCE to "Timing Advance (°)",
            INTAKE_TEMP to "Intake Air Temp (°C)",
            MAF_RATE to "MAF Air Flow Rate (g/s)",
            THROTTLE_POSITION to "Throttle Position (%)",
            FUEL_LEVEL to "Fuel Tank Level (%)",
            COMMANDED_EGR to "Commanded EGR (%)",
            EGR_ERROR to "EGR Error (%)",
            RUN_TIME to "Engine Run Time (s)",
            DISTANCE_MIL to "Distance with MIL (km)"
        )
        
        fun buildPID(pid: Int): String = "01" + String.format("%02X", pid)
    }

    // Climate Control (HVAC)
    object Climate {
        const val CMD_PREFIX = 0x2EFF11
        const val AC_ON = 0x01
        const val AC_OFF = 0x00
        const val AUTO_MODE = 0x02
        const val DEFROST_FRONT = 0x04
        const val DEFROST_REAR = 0x08
        const val DEFROST_MIRROR = 0x10
        const val RECIRCULATION = 0x20
        const val AC_COMPRESSOR = 0x40
        const val BLOWER_SPEED_1 = 0x01
        const val BLOWER_SPEED_2 = 0x02
        const val BLOWER_SPEED_3 = 0x03
        const val BLOWER_SPEED_4 = 0x04
        const val BLOWER_SPEED_5 = 0x05
        const val BLOWER_SPEED_MAX = 0x06
        
        // Temperature (16 = 16°C, 32 = 32°C)
        const val TEMP_16C = 0x10
        const val TEMP_18C = 0x12
        const val TEMP_20C = 0x14
        const val TEMP_22C = 0x16
        const val TEMP_24C = 0x18
        const val TEMP_26C = 0x1A
        const val TEMP_28C = 0x1C
        
        // Zone selections
        const val ZONE_DRIVER = 0x01
        const val ZONE_PASSENGER = 0x02
        const val ZONE_REAR = 0x04
        const val ZONE_ALL = 0x07
        
        fun acOnFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), AC_ON.toByte())
        fun acOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), AC_OFF.toByte())
        fun recirculationFrame(enable: Boolean) = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), if (enable) RECIRCULATION.toByte() else AC_OFF.toByte())
        fun defrostFrontFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), DEFROST_FRONT.toByte())
        fun defrostFrontOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), AC_OFF.toByte())
        fun defrostRearFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), DEFROST_REAR.toByte())
        fun defrostRearOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), AC_OFF.toByte())
        fun defrostAllFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), (DEFROST_FRONT or DEFROST_REAR or DEFROST_MIRROR).toByte())
        fun defrostMirrorsOffFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), AC_OFF.toByte())
        fun blowerSpeedFrame(speed: Int) = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), (0x80 or speed.coerceIn(0, 6)).toByte())
        fun temperatureFrame(temp: Int) = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), temp.coerceIn(16, 32).toByte())
        fun autoModeFrame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x11.toByte(), AUTO_MODE.toByte())
    }

    // TPMS (Tire Pressure Monitoring System)
    object TPMS {
        const val ROUTINE_RESET = 0x0302
        const val ROUTINE_LEARN = 0x0307
        
        // Tire Pressure Thresholds (kPa)
        const val PRESSURE_LOW = 200  // ~29 psi
        const val PRESSURE_NORMAL = 230  // ~33 psi
        const val PRESSURE_HIGH = 250  // ~36 psi
        
        fun buildTPMSResetFrame() = "310302"
        fun buildTPMSLearnFrame() = "310307"
    }

    object CANParser {
        fun parseHVACMessage(canId: String, data: ByteArray): HVACStatus? {
            if (data.size < 8) return null
            
            return when (canId.uppercase()) {
                "7E5", "7ED", "420", "422" -> parseHVACStatusByte(data)
                else -> null
            }
        }
        
        private fun parseHVACStatusByte(data: ByteArray): HVACStatus {
            val byte0 = data.getOrNull(0)?.toInt()?.and(0xFF) ?: 0
            val byte1 = data.getOrNull(1)?.toInt()?.and(0xFF) ?: 0
            val byte2 = data.getOrNull(2)?.toInt()?.and(0xFF) ?: 0
            val byte3 = data.getOrNull(3)?.toInt()?.and(0xFF) ?: 0
            val byte4 = data.getOrNull(4)?.toInt()?.and(0xFF) ?: 0
            val byte5 = data.getOrNull(5)?.toInt()?.and(0xFF) ?: 0
            val byte6 = data.getOrNull(6)?.toInt()?.and(0xFF) ?: 0
            
            return HVACStatus(
                acCompressorActive = (byte0 and 0x01) != 0,
                fanSpeed = (byte1 and 0x0F),
                driverTemp = (byte2 - 64) * 0.5,
                passengerTemp = (byte3 - 64) * 0.5,
                autoModeActive = (byte0 and 0x02) != 0,
                recirculationActive = (byte0 and 0x04) != 0,
                frontDefrostActive = (byte0 and 0x08) != 0,
                rearDefrostActive = (byte0 and 0x10) != 0,
                rearLeftVent = (byte4 and 0x01) != 0,
                rearRightVent = (byte4 and 0x02) != 0,
                footVent = (byte4 and 0x04) != 0,
                faceVent = (byte4 and 0x08) != 0,
                outsideTempRaw = byte5,
                cabinTempRaw = byte6,
                timestamp = System.currentTimeMillis()
            )
        }
        
        fun parseTPMSMessage(canId: String, data: ByteArray): TPMSStatus? {
            if (data.size < 6) return null
            
            return when (canId.uppercase()) {
                "420", "422", "428" -> parseTPMSStatusByte(data)
                else -> null
            }
        }
        
        private fun parseTPMSStatusByte(data: ByteArray): TPMSStatus {
            val byte0 = data.getOrNull(0)?.toInt()?.and(0xFF) ?: 0
            val byte1 = data.getOrNull(1)?.toInt()?.and(0xFF) ?: 0
            val byte2 = data.getOrNull(2)?.toInt()?.and(0xFF) ?: 0
            val byte3 = data.getOrNull(3)?.toInt()?.and(0xFF) ?: 0
            val byte4 = data.getOrNull(4)?.toInt()?.and(0xFF) ?: 0
            val byte5 = data.getOrNull(5)?.toInt()?.and(0xFF) ?: 0
            val byte6 = data.getOrNull(6)?.toInt()?.and(0xFF) ?: 0
            val byte7 = data.getOrNull(7)?.toInt()?.and(0xFF) ?: 0

            val frontLeftPsi = if (byte0 > 0) byte0 * 0.25 else 0.0
            val frontRightPsi = if (byte1 > 0) byte1 * 0.25 else 0.0
            val rearLeftPsi = if (byte2 > 0) byte2 * 0.25 else 0.0
            val rearRightPsi = if (byte3 > 0) byte3 * 0.25 else 0.0

            val frontLeftTemp = if (byte4 in 1..200) byte4 - 40 else 0
            val frontRightTemp = if (byte5 in 1..200) byte5 - 40 else 0
            val rearLeftTemp = if (byte6 in 1..200) byte6 - 40 else 0
            val rearRightTemp = if (byte7 in 1..200) byte7 - 40 else 0

            return TPMSStatus(
                frontLeftPSI = frontLeftPsi,
                frontRightPSI = frontRightPsi,
                rearLeftPSI = rearLeftPsi,
                rearRightPSI = rearRightPsi,
                frontLeftTemp = frontLeftTemp,
                frontRightTemp = frontRightTemp,
                rearLeftTemp = rearLeftTemp,
                rearRightTemp = rearRightTemp,
                lowPressureWarning = (byte0 or byte1 or byte2 or byte3) == 0,
                systemError = (byte0 and byte1 and byte2 and byte3) == 0xFF,
                timestamp = System.currentTimeMillis()
            )
        }

        fun parseTCMMessage(canId: String, data: ByteArray): TCMStatus? {
            if (data.size < 8) return null
            
            return when (canId.uppercase()) {
                "7E1", "7E9", "424", "426" -> parseTCMStatusByte(data)
                else -> null
            }
        }
        
        private fun parseTCMStatusByte(data: ByteArray): TCMStatus {
            val byte0 = data.getOrNull(0)?.toInt() ?: 0
            val byte1 = data.getOrNull(1)?.toInt() ?: 0
            val byte2 = data.getOrNull(2)?.toInt() ?: 0
            val byte3 = data.getOrNull(3)?.toInt() ?: 0
            val byte4 = data.getOrNull(4)?.toInt() ?: 0
            val byte5 = data.getOrNull(5)?.toInt() ?: 0
            val byte6 = data.getOrNull(6)?.toInt() ?: 0
            
            val gear = when (byte0 and 0x0F) {
                0x01 -> 1
                0x02 -> 2
                0x03 -> 3
                0x04 -> 4
                0x05 -> 5
                0x06 -> 6
                else -> 0
            }

            val oilTemp = if ((byte1 and 0xFF) in 1..200) (byte1 and 0xFF) - 40 else 0
            val pressure = (byte2 and 0xFF) * 4
            
            return TCMStatus(
                currentGear = gear,
                oilTempCelsius = oilTemp,
                pressureKpa = pressure,
                inputShaftRpm = ((byte3.toInt() and 0xFF) * 256 + (byte4.toInt() and 0xFF)).toDouble(),
                outputShaftRpm = ((byte5.toInt() and 0xFF) * 256 + (byte6.toInt() and 0xFF)).toDouble(),
                clutchSlipping = (byte0 and 0x40) != 0,
                transmissionError = (byte0 and 0x80) != 0,
                sportMode = (byte0 and 0x20) != 0,
                manualMode = (byte0 and 0x10) != 0,
                timestamp = System.currentTimeMillis()
            )
        }

        fun parseECMMessage(canId: String, data: ByteArray): ECMStatus? {
            if (data.size < 8) return null
            
            return when (canId.uppercase()) {
                "7E0", "7E8", "430", "432" -> parseECMStatusByte(data)
                else -> null
            }
        }
        
        private fun parseECMStatusByte(data: ByteArray): ECMStatus {
            val byte0 = data.getOrNull(0)?.toInt() ?: 0
            val byte1 = data.getOrNull(1)?.toInt() ?: 0
            val byte2 = data.getOrNull(2)?.toInt() ?: 0
            val byte3 = data.getOrNull(3)?.toInt() ?: 0
            val byte4 = data.getOrNull(4)?.toInt() ?: 0
            val byte5 = data.getOrNull(5)?.toInt() ?: 0
            val byte6 = data.getOrNull(6)?.toInt() ?: 0
            
            val rpm = ((byte0.toInt() and 0xFF) * 256 + (byte1.toInt() and 0xFF)).toDouble() / 4.0
            val speed = ((byte2.toInt() and 0xFF) * 256 + (byte3.toInt() and 0xFF)).toDouble()
            val coolant = if (byte4 in 1..200) byte4 - 40 else 0
            val throttle = ((byte5.toInt() and 0xFF) * 100.0 / 255.0)
            val load = ((byte6.toInt() and 0xFF) * 100.0 / 255.0)
            
            return ECMStatus(
                rpm = rpm,
                speedKmh = speed,
                coolantTemp = coolant,
                throttlePosition = throttle,
                engineLoad = load,
                fuelLevel = 0.0,
                batteryVoltage = 0.0,
                knockRetard = 0.0,
                timingAdvance = 0.0,
                fuelPressure = 0.0,
                intakeTemp = 0,
                mafRate = 0.0,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    data class HVACStatus(
        val acCompressorActive: Boolean = false,
        val fanSpeed: Int = 0,
        val driverTemp: Double = 22.0,
        val passengerTemp: Double = 22.0,
        val autoModeActive: Boolean = false,
        val recirculationActive: Boolean = false,
        val frontDefrostActive: Boolean = false,
        val rearDefrostActive: Boolean = false,
        val rearLeftVent: Boolean = false,
        val rearRightVent: Boolean = false,
        val footVent: Boolean = false,
        val faceVent: Boolean = false,
        val outsideTempRaw: Int = 0,
        val cabinTempRaw: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val outsideTempCelsius: Int
            get() = if (outsideTempRaw in 1..200) outsideTempRaw - 50 else 0
            
        val cabinTempCelsius: Int
            get() = if (cabinTempRaw in 1..200) cabinTempRaw - 50 else 0
    }
    
    data class TPMSStatus(
        val frontLeftPSI: Double = 0.0,
        val frontRightPSI: Double = 0.0,
        val rearLeftPSI: Double = 0.0,
        val rearRightPSI: Double = 0.0,
        val frontLeftTemp: Int = 0,
        val frontRightTemp: Int = 0,
        val rearLeftTemp: Int = 0,
        val rearRightTemp: Int = 0,
        val lowPressureWarning: Boolean = false,
        val systemError: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class TCMStatus(
        val currentGear: Int = 0,
        val oilTempCelsius: Int = 0,
        val pressureKpa: Int = 0,
        val inputShaftRpm: Double = 0.0,
        val outputShaftRpm: Double = 0.0,
        val clutchSlipping: Boolean = false,
        val transmissionError: Boolean = false,
        val sportMode: Boolean = false,
        val manualMode: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class ECMStatus(
        val rpm: Double = 0.0,
        val speedKmh: Double = 0.0,
        val coolantTemp: Int = 0,
        val throttlePosition: Double = 0.0,
        val engineLoad: Double = 0.0,
        val fuelLevel: Double = 0.0,
        val batteryVoltage: Double = 0.0,
        val knockRetard: Double = 0.0,
        val timingAdvance: Double = 0.0,
        val fuelPressure: Double = 0.0,
        val intakeTemp: Int = 0,
        val mafRate: Double = 0.0,
        val timestamp: Long = System.currentTimeMillis()
    )

    // IPC (Instrument Panel Cluster) Controls
    object IPC {
        // IPC CAN IDs
        const val IPC_TX = "7C3"
        const val IPC_RX = "7CB"
        
        // IPC DIDs
        const val CLUSTER_CONFIG = 0xC100
        const val ODOMETER = 0xC200
        const val SERVICE_REMINDER = 0xC300
        const val UNITS_CONFIG = 0xC400
        
        // Service Interval DIDs
        const val OIL_LIFE_DISTANCE = 0xD001
        const val OIL_LIFE_TIME = 0xD002
        const val INSPECTION_DISTANCE = 0xD003
        const val INSPECTION_TIME = 0xD004
        
        // Unit Settings
        const val UNITS_METRIC = 0x01
        const val UNITS_IMPERIAL = 0x02
        const val UNITS_US = 0x03
        
        fun buildOdometerRead() = "22" + String.format("%02X%02X", (ODOMETER shr 8) and 0xFF, ODOMETER and 0xFF)
        fun buildUnitsRead() = "22" + String.format("%02X%02X", (UNITS_CONFIG shr 8) and 0xFF, UNITS_CONFIG and 0xFF)
        fun buildUnitsWrite(units: Int) = "2E" + String.format("%02X%02X%02X", (UNITS_CONFIG shr 8) and 0xFF, UNITS_CONFIG and 0xFF, units)
    }

    // Vehicle Configuration
    object VehicleConfig {
        const val MODEL_OPEL_ASTRA_J = "ASTRA_J"
        const val MODEL_OPEL_INSIGNIA = "INSIGNIA"
        const val MODEL_VAUXHALL_ASTRA = "VAUXHALL_ASTRA"
        
        // VIN Structure
        const val VIN_OFFSET_COUNTRY = 1
        const val VIN_OFFSET_MANUFACTURER = 2
        const val VIN_OFFSET_MODEL_YEAR = 9
        const val VIN_OFFSET_PLANT = 10
        
        // Country Codes
        const val COUNTRY_USA = "1"
        const val COUNTRY_CANADA = "2"
        const val COUNTRY_GERMANY = "W"
        const val COUNTRY_UK = "V"
        
        // Manufacturer Codes
        const val MFR_OPEL = "A"  // Opel/Vauxhall
        const val MFR_GM = "G"    // General Motors
        
        fun parseVIN(vin: String): Map<String, String> {
            if (vin.length != 17) return emptyMap()
            return mapOf(
                "country" to vin[0].toString(),
                "manufacturer" to vin.substring(1, 3),
                "modelYear" to vin[9].toString(),
                "plant" to vin[10].toString(),
                "serial" to vin.substring(11, 17)
            )
        }
        
        fun getYearCode(year: Char): Int {
            return when (year) {
                'A' -> 2010
                'B' -> 2011
                'C' -> 2012
                'D' -> 2013
                'E' -> 2014
                'F' -> 2015
                'G' -> 2016
                'H' -> 2017
                'J' -> 2018
                'K' -> 2019
                'L' -> 2020
                'M' -> 2021
                'N' -> 2022
                'P' -> 2023
                'R' -> 2024
                else -> 0
            }
        }
    }

    // Oil Reset Service
    object OilReset {
        const val ROUTINE_ID = 0x0303
        const val SERVICE_TYPE = 0xD800
        
        fun buildOilResetFrame() = "310303"
        fun buildInspectionResetFrame() = "310304"
    }

    // Readiness Monitor Status
    object Readiness {
        const val PID = 0x01
        
        const val MISFIRE = 0
        const val FUEL_SYSTEM = 1
        const val COMPONENTS = 2
        const val IGNITION = 3
        const val EMISSIONS = 4
        const val EVAP = 5
        const val O2_HEATER = 6
        const val O2_SENSOR = 7
        const val EGR = 8
        
        val MONITOR_NAMES = listOf(
            "Misfire", "Fuel System", "Components", "Ignition",
            "Emissions", "Evaporative System", "O2 Heater", "O2 Sensor", "EGR/VVT System"
        )
        
        fun buildReadinessRequest() = "0101"
        
        fun parseReadiness(data: Int): List<Boolean> {
            return (0..8).map { shift -> (data shr shift) and 1 == 0 }
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    fun buildUDSRead(did: Int): String {
        val didH = String.format("%02X", (did shr 8) and 0xFF)
        val didL = String.format("%02X", did and 0xFF)
        return "22$didH$didL"
    }

    fun buildUDSWrite(did: Int, data: String = ""): String {
        val didH = String.format("%02X", (did shr 8) and 0xFF)
        val didL = String.format("%02X", did and 0xFF)
        return "2E$didH$didL$data"
    }

    fun parseStringResponse(response: String?): String? {
        if (response == null) return null
        val clean = response.replace(" ", "").replace("\r", "").replace("\n", "")
        if (clean.contains("ERROR") || clean.length < 8) return null
        val dataStart = clean.indexOf("62")
        if (dataStart < 0) return null
        val hexData = clean.substring(dataStart + 6)
        return try {
            (0 until hexData.length step 2)
                .filter { it + 2 <= hexData.length }
                .mapNotNull {
                    val code = hexData.substring(it, it + 2).toInt(16)
                    if (code in 0x20..0x7E) code.toChar() else null
                }
                .joinToString("")
        } catch (_: Exception) { null }
    }

    fun parseByteResponse(response: String?): Int? {
        if (response == null) return null
        val clean = response.replace(" ", "").replace("\r", "").replace("\n", "")
        if (clean.contains("ERROR") || clean.length < 10) return null
        val dataStart = clean.indexOf("62")
        if (dataStart < 0) return null
        return try { clean.substring(dataStart + 8, dataStart + 10).toInt(16) } catch (_: Exception) { null }
    }
}

enum class BCMCommandType {
    UDS_WRITE,
    UDS_READ,
    NONE
}

data class BCMCommand(
    val did: Int,
    val value: String,
    val type: BCMCommandType
)

    object BCMCommandMapper {

    @Suppress("UNUSED_PARAMETER")
    fun mapToCommand(action: String, value: Any? = null): BCMCommand? {
        return when (action.uppercase()) {
            // Zentralverriegelung
            "LOCK" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.LOCK_ALL), BCMCommandType.UDS_WRITE)
            "UNLOCK" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.UNLOCK_ALL), BCMCommandType.UDS_WRITE)
            "UNLOCK_DRIVER" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.UNLOCK_DRIVER), BCMCommandType.UDS_WRITE)
            "UNLOCK_TAILGATE" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.UNLOCK_TAILGATE), BCMCommandType.UDS_WRITE)
            "UNLOCK_FUEL" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.UNLOCK_FUEL), BCMCommandType.UDS_WRITE)
            
            // Spiegel
            "MIRROR_FOLD" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.FOLD), BCMCommandType.UDS_WRITE)
            "MIRROR_UNFOLD" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.UNFOLD), BCMCommandType.UDS_WRITE)
            "MIRROR_HEATING_ON" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.HEATING_ON), BCMCommandType.UDS_WRITE)
            "MIRROR_HEATING_OFF" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.HEATING_OFF), BCMCommandType.UDS_WRITE)
            "MIRROR_MOVE_UP" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X%02X", BCMProtocol.Mirror.BOTH_MIRRORS, BCMProtocol.Mirror.MOVE_UP), BCMCommandType.UDS_WRITE)
            "MIRROR_MOVE_DOWN" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X%02X", BCMProtocol.Mirror.BOTH_MIRRORS, BCMProtocol.Mirror.MOVE_DOWN), BCMCommandType.UDS_WRITE)
            "MIRROR_MOVE_LEFT" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X%02X", BCMProtocol.Mirror.BOTH_MIRRORS, BCMProtocol.Mirror.MOVE_LEFT), BCMCommandType.UDS_WRITE)
            "MIRROR_MOVE_RIGHT" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X%02X", BCMProtocol.Mirror.BOTH_MIRRORS, BCMProtocol.Mirror.MOVE_RIGHT), BCMCommandType.UDS_WRITE)
            
            // Heizung
            "REAR_HEATING_ON" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.REAR_ON), BCMCommandType.UDS_WRITE)
            "REAR_HEATING_OFF" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.REAR_OFF), BCMCommandType.UDS_WRITE)
            "FRONT_HEATING_ON" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.FRONT_ON), BCMCommandType.UDS_WRITE)
            "FRONT_HEATING_OFF" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.FRONT_OFF), BCMCommandType.UDS_WRITE)
            "STEERING_HEATING_1" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.STEERING_LEVEL_1), BCMCommandType.UDS_WRITE)
            "STEERING_HEATING_2" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.STEERING_LEVEL_2), BCMCommandType.UDS_WRITE)
            "STEERING_HEATING_3" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.STEERING_LEVEL_3), BCMCommandType.UDS_WRITE)
            "STEERING_HEATING_OFF" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.STEERING_OFF), BCMCommandType.UDS_WRITE)
            
            // Scheibenwischer
            "WIPER_OFF" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.OFF), BCMCommandType.UDS_WRITE)
            "WIPER_LOW" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.LOW), BCMCommandType.UDS_WRITE)
            "WIPER_HIGH" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.HIGH), BCMCommandType.UDS_WRITE)
            "WIPER_AUTO" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.AUTO), BCMCommandType.UDS_WRITE)
            "WIPER_REAR_ON" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.REAR_ON), BCMCommandType.UDS_WRITE)
            "WIPER_REAR_OFF" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.REAR_OFF), BCMCommandType.UDS_WRITE)
            
            // Beleuchtung
            "AMBIENT_LIGHT_INCREASE" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, "0064", BCMCommandType.UDS_WRITE)
            "AMBIENT_LIGHT_DECREASE" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, "0000", BCMCommandType.UDS_WRITE)
            "AMBIENT_LIGHT_MAX" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, "0096", BCMCommandType.UDS_WRITE)
            "CORNERING_LIGHT_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.CORNERING_ON), BCMCommandType.UDS_WRITE)
            "CORNERING_LIGHT_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.CORNERING_OFF), BCMCommandType.UDS_WRITE)
            "COMING_HOME_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.COMING_HOME_ENABLE), BCMCommandType.UDS_WRITE)
            "COMING_HOME_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.COMING_HOME_DISABLE), BCMCommandType.UDS_WRITE)
            "LEAVING_HOME_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.LEAVING_HOME_ENABLE), BCMCommandType.UDS_WRITE)
            "LEAVING_HOME_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.LEAVING_HOME_DISABLE), BCMCommandType.UDS_WRITE)
            "PARKING_LIGHTS_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.PARKING_ON), BCMCommandType.UDS_WRITE)
            "PARKING_LIGHTS_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.PARKING_OFF), BCMCommandType.UDS_WRITE)
            "DRL_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.DRL_ON), BCMCommandType.UDS_WRITE)
            "DRL_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.DRL_OFF), BCMCommandType.UDS_WRITE)
            "FOG_LIGHTS_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.FOG_LIGHTS_ON), BCMCommandType.UDS_WRITE)
            "FOG_LIGHTS_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.FOG_LIGHTS_OFF), BCMCommandType.UDS_WRITE)
            
            // Horn
            "HORN" -> BCMCommand(BCMProtocol.DIDs.HORN_STATUS, "01", BCMCommandType.UDS_WRITE)
            "HORN_STOP" -> BCMCommand(BCMProtocol.DIDs.HORN_STATUS, "00", BCMCommandType.UDS_WRITE)
            
            // Sunroof
            "SUNROOF_OPEN" -> BCMCommand(BCMProtocol.DIDs.SUNROOF_STATUS, "64", BCMCommandType.UDS_WRITE)
            "SUNROOF_CLOSE" -> BCMCommand(BCMProtocol.DIDs.SUNROOF_STATUS, "00", BCMCommandType.UDS_WRITE)
            "SUNROOF_STOP" -> BCMCommand(BCMProtocol.DIDs.SUNROOF_STATUS, "FF", BCMCommandType.UDS_WRITE)
            "SUNROOF_VENT" -> BCMCommand(BCMProtocol.DIDs.SUNROOF_STATUS, "32", BCMCommandType.UDS_WRITE)
            
            // Sitzheizung
            "SEAT_DRIVER_HEAT_1" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.DRIVER_LEVEL_1), BCMCommandType.UDS_WRITE)
            "SEAT_DRIVER_HEAT_2" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.DRIVER_LEVEL_2), BCMCommandType.UDS_WRITE)
            "SEAT_DRIVER_HEAT_3" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.DRIVER_LEVEL_3), BCMCommandType.UDS_WRITE)
            "SEAT_DRIVER_OFF" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.DRIVER_OFF), BCMCommandType.UDS_WRITE)
            "SEAT_PASSENGER_HEAT_1" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.PASSENGER_LEVEL_1), BCMCommandType.UDS_WRITE)
            "SEAT_PASSENGER_HEAT_2" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.PASSENGER_LEVEL_2), BCMCommandType.UDS_WRITE)
            "SEAT_PASSENGER_HEAT_3" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.PASSENGER_LEVEL_3), BCMCommandType.UDS_WRITE)
            "SEAT_PASSENGER_OFF" -> BCMCommand(BCMProtocol.DIDs.SEAT_HEATING_STATUS, String.format("%02X", BCMProtocol.SeatHeating.PASSENGER_OFF), BCMCommandType.UDS_WRITE)
            
            // Status lesen
            "READ_STATUS" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, "", BCMCommandType.UDS_READ)
            "READ_WINDOW_STATUS" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "", BCMCommandType.UDS_READ)
            "READ_LIGHTING_STATUS" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, "", BCMCommandType.UDS_READ)
            "READ_HEATING_STATUS" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, "", BCMCommandType.UDS_READ)
            
            // Fenster
            "WINDOW_DRIVER_UP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "00", BCMCommandType.UDS_WRITE)
            "WINDOW_DRIVER_DOWN" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "64", BCMCommandType.UDS_WRITE)
            "WINDOW_DRIVER_STOP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "FF", BCMCommandType.UDS_WRITE)
            "WINDOW_PASSENGER_UP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "00", BCMCommandType.UDS_WRITE)
            "WINDOW_PASSENGER_DOWN" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "64", BCMCommandType.UDS_WRITE)
            "WINDOW_PASSENGER_STOP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "FF", BCMCommandType.UDS_WRITE)
            "WINDOW_REAR_LEFT_UP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "00", BCMCommandType.UDS_WRITE)
            "WINDOW_REAR_LEFT_DOWN" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "64", BCMCommandType.UDS_WRITE)
            "WINDOW_REAR_LEFT_STOP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "FF", BCMCommandType.UDS_WRITE)
            "WINDOW_REAR_RIGHT_UP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "00", BCMCommandType.UDS_WRITE)
            "WINDOW_REAR_RIGHT_DOWN" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "64", BCMCommandType.UDS_WRITE)
            "WINDOW_REAR_RIGHT_STOP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "FF", BCMCommandType.UDS_WRITE)
            "WINDOW_ALL_UP" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "00000000", BCMCommandType.UDS_WRITE)
            "WINDOW_ALL_DOWN" -> BCMCommand(BCMProtocol.DIDs.WINDOW_STATUS, "64646464", BCMCommandType.UDS_WRITE)
            
            // Klima
            "CLIMATE_AC_ON" -> BCMCommand(BCMProtocol.DIDs.CLIMATE_STATUS, "01", BCMCommandType.UDS_WRITE)
            "CLIMATE_AC_OFF" -> BCMCommand(BCMProtocol.DIDs.CLIMATE_STATUS, "00", BCMCommandType.UDS_WRITE)
            "CLIMATE_DEFROST_FRONT" -> BCMCommand(BCMProtocol.DIDs.CLIMATE_STATUS, "04", BCMCommandType.UDS_WRITE)
            "CLIMATE_DEFROST_REAR" -> BCMCommand(BCMProtocol.DIDs.CLIMATE_STATUS, "08", BCMCommandType.UDS_WRITE)
            "CLIMATE_DEFROST_ALL" -> BCMCommand(BCMProtocol.DIDs.CLIMATE_STATUS, "1C", BCMCommandType.UDS_WRITE)
            "CLIMATE_AUTO" -> BCMCommand(BCMProtocol.DIDs.CLIMATE_STATUS, "02", BCMCommandType.UDS_WRITE)
            
            // TPMS
            "TPMS_RESET" -> BCMCommand(0, "310302", BCMCommandType.NONE)
            
            // Oil/Inspection Reset
            "OIL_RESET" -> BCMCommand(0, "310303", BCMCommandType.NONE)
            "INSPECTION_RESET" -> BCMCommand(0, "310304", BCMCommandType.NONE)
            
            // DTCs
            "DTC_READ" -> BCMCommand(0, "03", BCMCommandType.NONE)
            "DTC_READ_PENDING" -> BCMCommand(0, "07", BCMCommandType.NONE)
            "DTC_CLEAR" -> BCMCommand(0, "04", BCMCommandType.NONE)
            "DTC_READ_PERMANENT" -> BCMCommand(0, "0A", BCMCommandType.NONE)
            
            else -> null
        }
    }

    fun actionToATCommand(action: String, value: Any? = null): String? {
        val cmd = mapToCommand(action, value) ?: return null
        val didH = String.format("%02X", (cmd.did shr 8) and 0xFF)
        val didL = String.format("%02X", cmd.did and 0xFF)
        val sid = if (cmd.type == BCMCommandType.UDS_WRITE) "2E" else "22"
        return "$sid$didH$didL${cmd.value}"
    }
}
