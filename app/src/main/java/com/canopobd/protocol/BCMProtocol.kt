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
        const val BCM_PART_NUMBER = 0xF190
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
        fun passengerLevel1Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x10.toByte())
        fun passengerLevel2Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x20.toByte())
        fun passengerLevel3Frame() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x09.toByte(), 0x30.toByte())
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
