package com.canopobd.protocol

import android.util.Log
import com.canopobd.bluetooth.ELM327BTConnection

/**
 * GM/Opel BCM (Body Control Module) protocol implementation for Opel Astra J.
 * 
 * The BCM communicates over the HS-CAN bus at 500kbps. On the Astra J, the BCM
 * handles all comfort functions: central locking, windows, mirrors, heating,
 * lighting, and wipers.
 * 
 * CAN IDs used by BCM:
 * - 0x280: BCM -> CAN (comfort commands)
 * - 0x288: CAN -> BCM (comfort status requests)
 * - 0x380: BCM diagnostics (UDS)
 * - 0x388: BCM diagnostics response
 * - 0x480: Unknown (observed on Astra J)
 * 
 * Through the ELM327, we use UDS (Unified Diagnostic Services) on SID 0x2E 
 * (write data) and SID 0x22 (read data by DID) on the BCM.
 * 
 * BCM DIDs (Data Identifiers) for Astra J:
 * - 0xFF01: Central locking status
 * - 0xFF02: Window status
 * - 0xFF03: Mirror status
 * - 0xFF04: Lighting status
 * - 0xFF05: Heating status
 * - 0xFF06: Wiper status
 * - 0xFF10: Ambient light level
 */
object BCMProtocol {
    
    const val TAG = "BCMProtocol"
    
    // BCM CAN IDs
    const val BCM_TX_CAN_ID = "280"  // BCM sends to CAN
    const val BCM_RX_CAN_ID = "288"  // CAN sends to BCM
    
    // Diagnostic CAN IDs (for UDS over CAN)
    const val BCM_DIAG_TX = "388"    // Diagnostic requests to BCM
    const val BCM_DIAG_RX = "308"   // Diagnostic responses from BCM
    
    // BCM UDS Service IDs
    const val UDS_SID_DIAGNOSTIC_SESSION = 0x10
    const val UDS_SID_ECU_RESET = 0x11
    const val UDS_SID_CLEAR_DTC = 0x14
    const val UDS_SID_READ_DTC = 0x19
    const val UDS_SID_READ_DATA = 0x22     // Read DID
    const val UDS_SID_READ_MEMORY = 0xx23
    const val UDS_SID_SECURITY_ACCESS = 0x27
    const val UDS_SID_COMMUNICATION_CONTROL = 0x28
    const val UDS_SID_WRITE_DATA = 0x2E    // Write DID
    const val UDS_SID_ROUTINE_CONTROL = 0x31
    
    // BCM Data Identifiers (DIDs)
    object DIDs {
        const val DOOR_LOCK_STATUS = 0xFF01
        const val WINDOW_STATUS = 0xFF02
        const val MIRROR_STATUS = 0xFF03
        const val LIGHTING_STATUS = 0xFF04
        const val HEATING_STATUS = 0xFF05
        const val WIPER_STATUS = 0xFF06
        const val AMBIENT_LIGHT = 0xFF10
        const val BCM_PART_NUMBER = 0xF190
        const val BCM_HARDWARE = 0xF191
        const val BCM_SOFTWARE = 0xF192
        const val BCM_SERIAL = 0xF193
    }
    
    // Door lock commands (byte 0 = FF01[0])
    // Bit 0: Driver door lock (0=unlocked, 1=locked)
    // Bit 1: Passenger door lock
    // Bit 2: Rear left door lock
    // Bit 3: Rear right door lock
    // Bit 4: Tailgate lock
    // Bit 5: All doors locked
    object DoorLock {
        const val UNLOCK_DRIVER = 0x01  // 0x2EFF01 01
        const val LOCK_DRIVER = 0x11    // 0x2EFF01 11
        const val UNLOCK_ALL = 0x0F      // 0x2EFF01 0F
        const val LOCK_ALL = 0x1F        // 0x2EFF01 1F
        const val UNLOCK_TAILGATE = 0x20 // 0x2EFF01 20
        const val LOCK_TAILGATE = 0x30   // 0x2EFF01 30
    }
    
    // Window commands (byte 0 = FF02[0])
    // FF02[0] = driver window (0-100%)
    // FF02[1] = passenger window
    // FF02[2] = rear left window
    // FF02[3] = rear right window
    // FF02[4] = sunroof (if equipped)
    // FF02[5] = window lock (prevent rear windows)
    object Window {
        const val CMD_PREFIX = 0x2EFF02
        
        fun openDriver() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x02.toByte(), 0x64)
        fun closeDriver() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x02.toByte(), 0x00)
        fun stopDriver() = byteArrayOf(0x2E.toByte(), 0xFF.toByte(), 0x02.toByte(), 0xFF)
        
        fun openAll() = byteArrayOf(
            0x2E.toByte(), 0xFF.toByte(), 0x02.toByte(),
            0x64, 0x64, 0x64, 0x64
        )
        fun closeAll() = byteArrayOf(
            0x2E.toByte(), 0xFF.toByte(), 0x02.toByte(),
            0x00, 0x00, 0x00, 0x00
        )
    }
    
    // Mirror commands (byte 0 = FF03[0])
    // FF03[0] = left/right (0=left, 1=right)
    // FF03[1] = up/down position
    // FF03[2] = fold/unfold (0=folded, 1=unfolded)
    // FF03[3] = heating (0=off, 1=on)
    object Mirror {
        const val CMD_PREFIX = 0x2EFF03
        
        const val FOLD = 0x04
        const val UNFOLD = 0x05
        const val HEATING_ON = 0x08
        const val HEATING_OFF = 0x00
        
        fun moveLeft() = byteArrayOf(0x00)
        fun moveRight() = byteArrayOf(0x01)
        fun moveUp() = byteArrayOf(0x02)
        fun moveDown() = byteArrayOf(0x03)
    }
    
    // Lighting commands (byte 0 = FF04[0])
    // FF04[0] = parking lights (0=off, 1=on)
    // FF04[1] = DRL mode (0=auto, 1=manual on)
    // FF04[2] = fog lights
    // FF04[3] = high beams
    // FF04[4] = cornering lights
    // FF04[5] = ambient light level (0-100%)
    object Lighting {
        const val CMD_PREFIX = 0x2EFF04
        
        const val PARKING_ON = 0x01
        const val PARKING_OFF = 0x00
        const val DRL_AUTO = 0x00
        const val DRL_ON = 0x02
        const val DRL_OFF = 0x04
        const val CORNERING_ON = 0x10
        const val CORNERING_OFF = 0x00
        
        const val COMING_HOME_ENABLE = 0x20
        const val COMING_HOME_DISABLE = 0x00
        const val LEAVING_HOME_ENABLE = 0x40
        const val LEAVING_HOME_DISABLE = 0x00
        
        fun setAmbientLevel(level: Int): ByteArray {
            val clamped = level.coerceIn(0, 100)
            return byteArrayOf(
                0x2E.toByte(), 0xFF.toByte(), 0x04.toByte(), 0x05,
                clamped.toByte()
            )
        }
    }
    
    // Heating commands (byte 0 = FF05[0])
    // FF05[0] = rear window heating (0=off, 1=on)
    // FF05[1] = front window heating (defrost)
    // FF05[2] = steering wheel heating (0=off, 1-3=levels)
    // FF05[3] = seat heating driver (0=off, 1-3=levels)
    // FF05[4] = seat heating passenger
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
        
        const val SEAT_DRIVER_LEVEL_1 = 0x10
        const val SEAT_DRIVER_LEVEL_2 = 0x20
        const val SEAT_DRIVER_LEVEL_3 = 0x30
        const val SEAT_DRIVER_OFF = 0x00
        
        const val SEAT_PASSENGER_LEVEL_1 = 0x40
        const val SEAT_PASSENGER_LEVEL_2 = 0x80
        const val SEAT_PASSENGER_LEVEL_3 = 0xC0
        const val SEAT_PASSENGER_OFF = 0x00
    }
    
    // Wiper commands (byte 0 = FF06[0])
    // FF06[0] = wiper speed (0=off, 1=low, 2=high, 3=intermittent)
    // FF06[1] = rear wiper
    // FF06[2] = front wash
    // FF06[3] = rear wash
    // FF06[4] = auto rain sensor (0=off, 1=on)
    object Wiper {
        const val CMD_PREFIX = 0x2EFF06
        
        const val OFF = 0x00
        const val LOW = 0x01
        const val HIGH = 0x02
        const val INTERMITTENT = 0x03
        const val AUTO = 0x13
        
        const val REAR_OFF = 0x00
        const val REAR_ON = 0x04
        
        const val WASH_FRONT = 0x10
        const val WASH_REAR = 0x20
    }
    
    /**
     * Build a UDS diagnostic request for BCM.
     */
    fun buildUDSRequest(sid: Int, did: Int, data: ByteArray = byteArrayOf()): ByteArray {
        val didHigh = (did shr 8) and 0xFF
        val didLow = did and 0xFF
        
        return byteArrayOf(
            sid.toByte(),
            didHigh.toByte(),
            didLow.toByte()
        ) + data
    }
    
    /**
     * Build a UDS read DID request for BCM.
     */
    fun readBCMData(did: Int): ByteArray {
        return buildUDSRequest(UDS_SID_READ_DATA, did)
    }
    
    /**
     * Build a UDS write DID request for BCM.
     */
    fun writeBCMData(did: Int, value: ByteArray): ByteArray {
        return buildUDSRequest(UDS_SID_WRITE_DATA, did, value)
    }
    
    /**
     * Build a CAN frame for comfort command.
     */
    fun buildCANFrame(canId: String, data: ByteArray): String {
        val paddedData = data + ByteArray(8 - data.size) { 0 }
        val dataHex = paddedData.joinToString("") { 
            String.format("%02X", it) 
        }
        return "${canId}${dataHex}"
    }
    
    /**
     * Check if BCM is responding.
     */
    suspend fun checkBCMCommunication(conn: ELM327BTConnection): Boolean {
        return try {
            val response = conn.sendCommand("ATMA")  // Monitor all
            response != null && !response.contains("ERROR")
        } catch (e: Exception) {
            Log.e(TAG, "BCM communication check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get BCM part number via UDS.
     */
    suspend fun getBCMPartNumber(conn: ELM327BTConnection): String? {
        return try {
            val cmd = buildUDSRequest(UDS_SID_READ_DATA, DIDs.BCM_PART_NUMBER)
            val hexCmd = cmd.joinToString("") { String.format("%02X", it) }
            val response = conn.sendCommandWithTimeout("22${hexCmd.substring(2)}")
            parseStringResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read BCM part number: ${e.message}")
            null
        }
    }
    
    /**
     * Send door lock command.
     */
    suspend fun sendDoorLockCommand(conn: ELM327BTConnection, command: Int): Boolean {
        return try {
            val cmd = buildUDSRequest(UDS_SID_WRITE_DATA, DIDs.DOOR_LOCK_STATUS, byteArrayOf(command.toByte()))
            val hexCmd = cmd.joinToString("") { String.format("%02X", it) }
            val response = conn.sendCommandWithTimeout("22${hexCmd.substring(2)}")
            response != null && !response.contains("ERROR")
        } catch (e: Exception) {
            Log.e(TAG, "Door lock command failed: ${e.message}")
            false
        }
    }
    
    /**
     * Read door lock status.
     */
    suspend fun readDoorLockStatus(conn: ELM327BTConnection): Int? {
        return try {
            val cmd = readBCMData(DIDs.DOOR_LOCK_STATUS)
            val hexCmd = cmd.joinToString("") { String.format("%02X", it) }
            val response = conn.sendCommandWithTimeout("22${hexCmd.substring(2)}")
            parseByteResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read door lock status: ${e.message}")
            null
        }
    }
    
    private fun parseStringResponse(response: String?): String? {
        if (response == null) return null
        val clean = response.replace(" ", "").replace("\r", "").replace("\n", "")
        if (clean.contains("ERROR") || clean.length < 6) return null
        val dataStart = clean.indexOf("62")  // 0x62 = positive response to 0x22
        if (dataStart < 0) return null
        val hexData = clean.substring(dataStart + 2)
        return try {
            (0 until hexData.length step 2)
                .filter { it + 2 <= hexData.length }
                .mapNotNull { 
                    val byteStr = hexData.substring(it, it + 2)
                    val code = byteStr.toInt(16)
                    if (code in 0x20..0x7E) code.toChar() else null
                }
                .joinToString("")
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseByteResponse(response: String?): Int? {
        if (response == null) return null
        val clean = response.replace(" ", "").replace("\r", "").replace("\n", "")
        if (clean.contains("ERROR") || clean.length < 8) return null
        val dataStart = clean.indexOf("62")
        if (dataStart < 0) return null
        return try {
            clean.substring(dataStart + 6, dataStart + 8).toInt(16)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Mapping from ComfortAction enum to BCM commands.
 */
enum class BCMCommandType {
    UDS_WRITE,    // UDS write DID
    UDS_READ,     // UDS read DID
    CAN_FRAME,    // Direct CAN frame
    NONE          // Not supported / stub
}

data class BCMCommand(
    val did: Int,
    val value: ByteArray,
    val type: BCMCommandType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BCMCommand
        return did == other.did && value.contentEquals(other.value) && type == other.type
    }
    
    override fun hashCode(): Int {
        var result = did
        result = 31 * result + value.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

/**
 * Mapper from app's ComfortAction to BCM commands.
 */
object BCMCommandMapper {
    
    fun mapToBCMCommand(action: String, value: Any? = null): BCMCommand? {
        return when (action.uppercase()) {
            "LOCK" -> BCMCommand(
                BCMProtocol.DIDs.DOOR_LOCK_STATUS,
                byteArrayOf(BCMProtocol.DoorLock.LOCK_ALL.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "UNLOCK" -> BCMCommand(
                BCMProtocol.DIDs.DOOR_LOCK_STATUS,
                byteArrayOf(BCMProtocol.DoorLock.UNLOCK_ALL.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "UNLOCK_DRIVER" -> BCMCommand(
                BCMProtocol.DIDs.DOOR_LOCK_STATUS,
                byteArrayOf(BCMProtocol.DoorLock.UNLOCK_DRIVER.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "MIRROR_FOLD" -> BCMCommand(
                BCMProtocol.DIDs.MIRROR_STATUS,
                byteArrayOf(BCMProtocol.Mirror.FOLD.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "MIRROR_UNFOLD" -> BCMCommand(
                BCMProtocol.DIDs.MIRROR_STATUS,
                byteArrayOf(BCMProtocol.Mirror.UNFOLD.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "MIRROR_HEATING_ON" -> BCMCommand(
                BCMProtocol.DIDs.MIRROR_STATUS,
                byteArrayOf(BCMProtocol.Mirror.HEATING_ON.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "MIRROR_HEATING_OFF" -> BCMCommand(
                BCMProtocol.DIDs.MIRROR_STATUS,
                byteArrayOf(BCMProtocol.Mirror.HEATING_OFF.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "REAR_HEATING_ON" -> BCMCommand(
                BCMProtocol.DIDs.HEATING_STATUS,
                byteArrayOf(BCMProtocol.Heating.REAR_ON.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "REAR_HEATING_OFF" -> BCMCommand(
                BCMProtocol.DIDs.HEATING_STATUS,
                byteArrayOf(BCMProtocol.Heating.REAR_OFF.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "FRONT_HEATING_ON" -> BCMCommand(
                BCMProtocol.DIDs.HEATING_STATUS,
                byteArrayOf(BCMProtocol.Heating.FRONT_ON.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "FRONT_HEATING_OFF" -> BCMCommand(
                BCMProtocol.DIDs.HEATING_STATUS,
                byteArrayOf(BCMProtocol.Heating.FRONT_OFF.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "WIPER_OFF" -> BCMCommand(
                BCMProtocol.DIDs.WIPER_STATUS,
                byteArrayOf(BCMProtocol.Wiper.OFF.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "WIPER_LOW" -> BCMCommand(
                BCMProtocol.DIDs.WIPER_STATUS,
                byteArrayOf(BCMProtocol.Wiper.LOW.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "WIPER_HIGH" -> BCMCommand(
                BCMProtocol.DIDs.WIPER_STATUS,
                byteArrayOf(BCMProtocol.Wiper.HIGH.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "WIPER_AUTO" -> BCMCommand(
                BCMProtocol.DIDs.WIPER_STATUS,
                byteArrayOf(BCMProtocol.Wiper.AUTO.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "AMBIENT_LIGHT_INCREASE" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(0x00, 0x64),
                BCMCommandType.UDS_WRITE
            )
            "AMBIENT_LIGHT_DECREASE" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(0x00, 0x00),
                BCMCommandType.UDS_WRITE
            )
            "CORNERING_LIGHT_ON" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(BCMProtocol.Lighting.CORNERING_ON.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "CORNERING_LIGHT_OFF" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(BCMProtocol.Lighting.CORNERING_OFF.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "COMING_HOME_ON" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(BCMProtocol.Lighting.COMING_HOME_ENABLE.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "COMING_HOME_OFF" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(BCMProtocol.Lighting.COMING_HOME_DISABLE.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "LEAVING_HOME_ON" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(BCMProtocol.Lighting.LEAVING_HOME_ENABLE.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "LEAVING_HOME_OFF" -> BCMCommand(
                BCMProtocol.DIDs.LIGHTING_STATUS,
                byteArrayOf(BCMProtocol.Lighting.LEAVING_HOME_DISABLE.toByte()),
                BCMCommandType.UDS_WRITE
            )
            "READ_STATUS" -> BCMCommand(
                BCMProtocol.DIDs.DOOR_LOCK_STATUS,
                byteArrayOf(),
                BCMCommandType.UDS_READ
            )
            else -> {
                null  // Window commands need special handling
            }
        }
    }
}
