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
 */
object BCMProtocol {

    const val TAG = "BCMProtocol"

    const val BCM_TX_CAN_ID = "280"
    const val BCM_RX_CAN_ID = "288"
    const val BCM_DIAG_TX = "388"
    const val BCM_DIAG_RX = "308"

    const val UDS_SID_READ_DATA = 0x22
    const val UDS_SID_WRITE_DATA = 0x2E

    object DIDs {
        const val DOOR_LOCK_STATUS = 0xFF01
        const val WINDOW_STATUS = 0xFF02
        const val MIRROR_STATUS = 0xFF03
        const val LIGHTING_STATUS = 0xFF04
        const val HEATING_STATUS = 0xFF05
        const val WIPER_STATUS = 0xFF06
        const val AMBIENT_LIGHT = 0xFF10
        const val BCM_PART_NUMBER = 0xF190
    }

    object DoorLock {
        const val UNLOCK_DRIVER = 0x01
        const val LOCK_DRIVER = 0x11
        const val UNLOCK_ALL = 0x0F
        const val LOCK_ALL = 0x1F
        const val UNLOCK_TAILGATE = 0x20
        const val LOCK_TAILGATE = 0x30
    }

    object Window {
        const val CMD_PREFIX = 0x2EFF02
        fun openDriver() = hexToBytes("2EFF0264")
        fun closeDriver() = hexToBytes("2EFF0200")
        fun stopDriver() = hexToBytes("2EFF02FF")
        fun openAll() = hexToBytes("2EFF0264646464")
        fun closeAll() = hexToBytes("2EFF0200000000")
    }

    object Mirror {
        const val CMD_PREFIX = 0x2EFF03
        const val FOLD = 0x04
        const val UNFOLD = 0x05
        const val HEATING_ON = 0x08
        const val HEATING_OFF = 0x00
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
    }

    private fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            ((hex.substring(i * 2, i * 2 + 2).toInt(16) - 256) % 256).toByte()
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
            "LOCK" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.LOCK_ALL), BCMCommandType.UDS_WRITE)
            "UNLOCK" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.UNLOCK_ALL), BCMCommandType.UDS_WRITE)
            "UNLOCK_DRIVER" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, String.format("%02X", BCMProtocol.DoorLock.UNLOCK_DRIVER), BCMCommandType.UDS_WRITE)
            "MIRROR_FOLD" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.FOLD), BCMCommandType.UDS_WRITE)
            "MIRROR_UNFOLD" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.UNFOLD), BCMCommandType.UDS_WRITE)
            "MIRROR_HEATING_ON" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.HEATING_ON), BCMCommandType.UDS_WRITE)
            "MIRROR_HEATING_OFF" -> BCMCommand(BCMProtocol.DIDs.MIRROR_STATUS, String.format("%02X", BCMProtocol.Mirror.HEATING_OFF), BCMCommandType.UDS_WRITE)
            "REAR_HEATING_ON" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.REAR_ON), BCMCommandType.UDS_WRITE)
            "REAR_HEATING_OFF" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.REAR_OFF), BCMCommandType.UDS_WRITE)
            "FRONT_HEATING_ON" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.FRONT_ON), BCMCommandType.UDS_WRITE)
            "FRONT_HEATING_OFF" -> BCMCommand(BCMProtocol.DIDs.HEATING_STATUS, String.format("%02X", BCMProtocol.Heating.FRONT_OFF), BCMCommandType.UDS_WRITE)
            "WIPER_OFF" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.OFF), BCMCommandType.UDS_WRITE)
            "WIPER_LOW" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.LOW), BCMCommandType.UDS_WRITE)
            "WIPER_HIGH" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.HIGH), BCMCommandType.UDS_WRITE)
            "WIPER_AUTO" -> BCMCommand(BCMProtocol.DIDs.WIPER_STATUS, String.format("%02X", BCMProtocol.Wiper.AUTO), BCMCommandType.UDS_WRITE)
            "AMBIENT_LIGHT_INCREASE" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, "0064", BCMCommandType.UDS_WRITE)
            "AMBIENT_LIGHT_DECREASE" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, "0000", BCMCommandType.UDS_WRITE)
            "CORNERING_LIGHT_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.CORNERING_ON), BCMCommandType.UDS_WRITE)
            "CORNERING_LIGHT_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.CORNERING_OFF), BCMCommandType.UDS_WRITE)
            "COMING_HOME_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.COMING_HOME_ENABLE), BCMCommandType.UDS_WRITE)
            "COMING_HOME_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.COMING_HOME_DISABLE), BCMCommandType.UDS_WRITE)
            "LEAVING_HOME_ON" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.LEAVING_HOME_ENABLE), BCMCommandType.UDS_WRITE)
            "LEAVING_HOME_OFF" -> BCMCommand(BCMProtocol.DIDs.LIGHTING_STATUS, String.format("%02X", BCMProtocol.Lighting.LEAVING_HOME_DISABLE), BCMCommandType.UDS_WRITE)
            "READ_STATUS" -> BCMCommand(BCMProtocol.DIDs.DOOR_LOCK_STATUS, "", BCMCommandType.UDS_READ)
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
