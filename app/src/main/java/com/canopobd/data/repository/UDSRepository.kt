package com.canopobd.data.repository

import android.util.Log
import com.canopobd.bluetooth.ELM327BTConnection
import com.canopobd.data.protocol.DIDValue
import com.canopobd.data.protocol.RoutineControlType
import com.canopobd.data.protocol.UDSClient
import com.canopobd.data.protocol.UDSSessionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.atomic.AtomicBoolean

class UDSRepository(private val connection: ELM327BTConnection) {

    companion object {
        private const val TAG = "UDSRepository"
        private const val ONE_BYTE_DID_RESPONSE_SIZE = 4
        private const val TWO_BYTE_DID_RESPONSE_SIZE = 5
        private const val FUEL_CONSUMPTION_SCALE = 20.0

        fun getAllSupportedDIDs(): List<String> = listOf(
            UDSConstants.GMOpelDIDs.ECU_INFO,
            UDSConstants.GMOpelDIDs.TORQUE,
            UDSConstants.GMOpelDIDs.BOOST_PRESSURE,
            UDSConstants.GMOpelDIDs.COOLANT_TEMP,
            UDSConstants.GMOpelDIDs.BATTERY_VOLTAGE,
            UDSConstants.GMOpelDIDs.FUEL_CONSUMPTION,
            UDSConstants.GMOpelDIDs.VIN,
            UDSConstants.GMOpelDIDs.CALIBRATION_ID,
            UDSConstants.GMOpelDIDs.CVN,
            UDSConstants.GMOpelDIDs.FUEL_PRESSURE,
            UDSConstants.GMOpelDIDs.INTAKE_AIR_TEMP,
            UDSConstants.GMOpelDIDs.AMBIENT_TEMP,
            UDSConstants.GMOpelDIDs.ENGINE_SPEED,
            UDSConstants.GMOpelDIDs.VEHICLE_SPEED,
            UDSConstants.GMOpelDIDs.THROTTLE_POSITION,
            UDSConstants.GMOpelDIDs.ENGINE_LOAD,
            UDSConstants.GMOpelDIDs.FUEL_LEVEL,
            UDSConstants.GMOpelDIDs.OIL_TEMP,
            UDSConstants.GMOpelDIDs.OIL_PRESSURE,
            UDSConstants.GMOpelDIDs.INJECTION_QUANTITY,
            UDSConstants.GMOpelDIDs.INJECTION_TIMING,
            UDSConstants.GMOpelDIDs.TURBO_BOOST,
            UDSConstants.GMOpelDIDs.TURBO_RPM,
            UDSConstants.GMOpelDIDs.WASTEGATE_DUTY,
            UDSConstants.GMOpelDIDs.CHARGE_AIR_TEMP,
            UDSConstants.GMOpelDIDs.CATALYST_TEMP,
            UDSConstants.GMOpelDIDs.WIDEBAND_LAMBDA
        )

        fun getDIDDescription(did: String): String {
            return when (did.uppercase()) {
                UDSConstants.GMOpelDIDs.ECU_INFO -> "ECU Info"
                UDSConstants.GMOpelDIDs.TORQUE -> "Engine Torque"
                UDSConstants.GMOpelDIDs.BOOST_PRESSURE -> "Boost Pressure"
                UDSConstants.GMOpelDIDs.COOLANT_TEMP -> "Coolant Temperature"
                UDSConstants.GMOpelDIDs.BATTERY_VOLTAGE -> "Battery Voltage"
                UDSConstants.GMOpelDIDs.FUEL_CONSUMPTION -> "Fuel Consumption"
                UDSConstants.GMOpelDIDs.VIN -> "VIN"
                UDSConstants.GMOpelDIDs.CALIBRATION_ID -> "Calibration ID"
                UDSConstants.GMOpelDIDs.CVN -> "Calibration Verification Number"
                UDSConstants.GMOpelDIDs.FUEL_PRESSURE -> "Fuel Pressure"
                UDSConstants.GMOpelDIDs.INTAKE_AIR_TEMP -> "Intake Air Temperature"
                UDSConstants.GMOpelDIDs.AMBIENT_TEMP -> "Ambient Air Temperature"
                UDSConstants.GMOpelDIDs.ENGINE_SPEED -> "Engine Speed"
                UDSConstants.GMOpelDIDs.VEHICLE_SPEED -> "Vehicle Speed"
                UDSConstants.GMOpelDIDs.THROTTLE_POSITION -> "Throttle Position"
                UDSConstants.GMOpelDIDs.ENGINE_LOAD -> "Engine Load"
                UDSConstants.GMOpelDIDs.FUEL_LEVEL -> "Fuel Level"
                UDSConstants.GMOpelDIDs.OIL_TEMP -> "Oil Temperature"
                UDSConstants.GMOpelDIDs.OIL_PRESSURE -> "Oil Pressure"
                UDSConstants.GMOpelDIDs.INJECTION_QUANTITY -> "Injection Quantity"
                UDSConstants.GMOpelDIDs.INJECTION_TIMING -> "Injection Timing"
                UDSConstants.GMOpelDIDs.TURBO_BOOST -> "Turbo Boost"
                UDSConstants.GMOpelDIDs.TURBO_RPM -> "Turbo RPM"
                UDSConstants.GMOpelDIDs.WASTEGATE_DUTY -> "Wastegate Duty"
                UDSConstants.GMOpelDIDs.CHARGE_AIR_TEMP -> "Charge Air Temperature"
                UDSConstants.GMOpelDIDs.CATALYST_TEMP -> "Catalyst Temperature"
                UDSConstants.GMOpelDIDs.WIDEBAND_LAMBDA -> "Wideband Lambda"
                else -> "Unknown DID ($did)"
            }
        }

        fun getDIDUnit(did: String): String {
            return when (did.uppercase()) {
                UDSConstants.GMOpelDIDs.TORQUE -> "Nm"
                UDSConstants.GMOpelDIDs.BOOST_PRESSURE,
                UDSConstants.GMOpelDIDs.FUEL_PRESSURE,
                UDSConstants.GMOpelDIDs.OIL_PRESSURE,
                UDSConstants.GMOpelDIDs.TURBO_BOOST -> "kPa"
                UDSConstants.GMOpelDIDs.COOLANT_TEMP,
                UDSConstants.GMOpelDIDs.INTAKE_AIR_TEMP,
                UDSConstants.GMOpelDIDs.AMBIENT_TEMP,
                UDSConstants.GMOpelDIDs.OIL_TEMP,
                UDSConstants.GMOpelDIDs.CHARGE_AIR_TEMP,
                UDSConstants.GMOpelDIDs.CATALYST_TEMP -> "\u00B0C"
                UDSConstants.GMOpelDIDs.BATTERY_VOLTAGE -> "V"
                UDSConstants.GMOpelDIDs.FUEL_CONSUMPTION -> "L/h"
                UDSConstants.GMOpelDIDs.ENGINE_SPEED,
                UDSConstants.GMOpelDIDs.TURBO_RPM -> "rpm"
                UDSConstants.GMOpelDIDs.VEHICLE_SPEED -> "km/h"
                UDSConstants.GMOpelDIDs.THROTTLE_POSITION,
                UDSConstants.GMOpelDIDs.ENGINE_LOAD,
                UDSConstants.GMOpelDIDs.WASTEGATE_DUTY -> "%"
                UDSConstants.GMOpelDIDs.FUEL_LEVEL -> "%"
                UDSConstants.GMOpelDIDs.INJECTION_QUANTITY -> "mg/stroke"
                UDSConstants.GMOpelDIDs.INJECTION_TIMING -> "\u00B0"
                UDSConstants.GMOpelDIDs.WIDEBAND_LAMBDA -> "\u03BB"
                else -> ""
            }
        }
    }

    private val udsClient = UDSClient(connection)
    private val sessionActive = AtomicBoolean(false)
    private val extendedSessionActive = AtomicBoolean(false)

    private suspend fun ensureSession(): Boolean {
        if (sessionActive.get()) return true
        return initializeSession().first()
    }

    fun initializeSession(): Flow<Boolean> = flow {
        try {
            Log.i(TAG, "Initializing UDS session...")
            val extendedResponse = udsClient.diagnosticSessionControl(UDSSessionType.EXTENDED).first()
            if (extendedResponse.isPositive) {
                extendedSessionActive.set(true)
                sessionActive.set(true)
                Log.i(TAG, "Extended session active")
                emit(true)
            } else {
                val defaultResponse = udsClient.diagnosticSessionControl(UDSSessionType.DEFAULT).first()
                sessionActive.set(defaultResponse.isPositive)
                extendedSessionActive.set(false)
                Log.w(TAG, "Extended session failed, default: ${defaultResponse.isPositive}")
                emit(defaultResponse.isPositive)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Session initialization error: ${e.message}")
            sessionActive.set(false)
            extendedSessionActive.set(false)
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun getVIN(): Flow<String> = flow {
        try {
            if (!ensureSession()) {
                emit("")
                return@flow
            }
            Log.d(TAG, "Reading VIN via UDS...")
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.VIN).collect { response ->
                if (response.isPositive) {
                    val didValue = udsClient.parseDIDValue(UDSConstants.GMOpelDIDs.VIN, response.data)
                    val vin = didValue.parsedValue?.toString()?.trim() ?: ""
                    Log.i(TAG, "VIN read: $vin")
                    emit(vin)
                } else {
                    Log.w(TAG, "VIN read failed")
                    emit("")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "VIN read error: ${e.message}")
            emit("")
        }
    }.flowOn(Dispatchers.IO)

    fun getCalibrationID(): Flow<String> = flow {
        try {
            if (!ensureSession()) {
                emit("")
                return@flow
            }
            Log.d(TAG, "Reading Calibration ID via UDS...")
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.CALIBRATION_ID).collect { response ->
                if (response.isPositive) {
                    val didValue = udsClient.parseDIDValue(UDSConstants.GMOpelDIDs.CALIBRATION_ID, response.data)
                    val calId = didValue.parsedValue?.toString()?.trim() ?: ""
                    Log.i(TAG, "Calibration ID read: $calId")
                    emit(calId)
                } else {
                    Log.w(TAG, "Calibration ID read failed")
                    emit("")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Calibration ID read error: ${e.message}")
            emit("")
        }
    }.flowOn(Dispatchers.IO)

    fun getTorque(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.TORQUE).collect { response ->
                if (response.isPositive && response.data.size >= TWO_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.size >= 2) {
                        val torqueRaw = (raw[0].toInt() and 0xFF) * 256 + (raw[1].toInt() and 0xFF)
                        val torqueNm = (torqueRaw - 500).toDouble()
                        emit(torqueNm)
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Torque read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getBoostPressure(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.BOOST_PRESSURE).collect { response ->
                if (response.isPositive && response.data.size >= TWO_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.size >= 2) {
                        val boostKpa = (raw[0].toInt() and 0xFF) * 256 + (raw[1].toInt() and 0xFF)
                        emit(boostKpa.toDouble())
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Boost pressure read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getBatteryVoltage(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.BATTERY_VOLTAGE).collect { response ->
                if (response.isPositive && response.data.size >= TWO_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.size >= 2) {
                        val voltageRaw = (raw[0].toInt() and 0xFF) * 256 + (raw[1].toInt() and 0xFF)
                        val voltageV = voltageRaw / 100.0
                        emit(voltageV)
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Battery voltage read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getFuelConsumption(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.FUEL_CONSUMPTION).collect { response ->
                if (response.isPositive && response.data.size >= TWO_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.size >= 2) {
                        val fuelRaw = (raw[0].toInt() and 0xFF) * 256 + (raw[1].toInt() and 0xFF)
                        val fuelLh = fuelRaw / FUEL_CONSUMPTION_SCALE
                        emit(fuelLh)
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fuel consumption read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getCoolantTemperature(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.COOLANT_TEMP).collect { response ->
                if (response.isPositive && response.data.size >= ONE_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.isNotEmpty()) {
                        val tempC = (raw[0].toInt() and 0xFF) - 40
                        emit(tempC.toDouble())
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Coolant temperature read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getEngineSpeed(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.ENGINE_SPEED).collect { response ->
                if (response.isPositive && response.data.size >= TWO_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.size >= 2) {
                        val rpmRaw = (raw[0].toInt() and 0xFF) * 256 + (raw[1].toInt() and 0xFF)
                        val rpm = rpmRaw.toDouble() / 4.0
                        emit(rpm)
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Engine speed read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getVehicleSpeed(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.VEHICLE_SPEED).collect { response ->
                if (response.isPositive && response.data.size >= ONE_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.isNotEmpty()) {
                        val speed = (raw[0].toInt() and 0xFF).toDouble()
                        emit(speed)
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vehicle speed read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getThrottlePosition(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.THROTTLE_POSITION).collect { response ->
                if (response.isPositive && response.data.size >= ONE_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.isNotEmpty()) {
                        val throttle = (raw[0].toInt() and 0xFF) * 100.0 / 255.0
                        emit(throttle)
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Throttle position read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getOilTemperature(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.OIL_TEMP).collect { response ->
                if (response.isPositive && response.data.size >= ONE_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.isNotEmpty()) {
                        val tempC = (raw[0].toInt() and 0xFF) - 40
                        emit(tempC.toDouble())
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Oil temperature read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun getFuelLevel(): Flow<Double?> = flow {
        try {
            if (!ensureSession()) {
                emit(null)
                return@flow
            }
            udsClient.readDataByIdentifier(UDSConstants.GMOpelDIDs.FUEL_LEVEL).collect { response ->
                if (response.isPositive && response.data.size >= ONE_BYTE_DID_RESPONSE_SIZE) {
                    val raw = response.data.drop(3)
                    if (raw.isNotEmpty()) {
                        val fuel = (raw[0].toInt() and 0xFF) * 100.0 / 255.0
                        emit(fuel)
                    } else {
                        emit(null)
                    }
                } else {
                    emit(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fuel level read error: ${e.message}")
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun readAllAvailableDIDs(): Flow<Map<String, DIDValue>> = flow<Map<String, DIDValue>> {
        try {
            if (!ensureSession()) {
                emit(emptyMap())
                return@flow
            }

            val didsToScan = listOf(
                UDSConstants.GMOpelDIDs.VIN,
                UDSConstants.GMOpelDIDs.CALIBRATION_ID,
                UDSConstants.GMOpelDIDs.TORQUE,
                UDSConstants.GMOpelDIDs.BOOST_PRESSURE,
                UDSConstants.GMOpelDIDs.COOLANT_TEMP,
                UDSConstants.GMOpelDIDs.BATTERY_VOLTAGE,
                UDSConstants.GMOpelDIDs.FUEL_CONSUMPTION,
                UDSConstants.GMOpelDIDs.ENGINE_SPEED,
                UDSConstants.GMOpelDIDs.VEHICLE_SPEED,
                UDSConstants.GMOpelDIDs.THROTTLE_POSITION,
                UDSConstants.GMOpelDIDs.OIL_TEMP,
                UDSConstants.GMOpelDIDs.FUEL_LEVEL,
                UDSConstants.GMOpelDIDs.ECU_INFO,
                UDSConstants.GMOpelDIDs.INTAKE_AIR_TEMP,
                UDSConstants.GMOpelDIDs.AMBIENT_TEMP,
                UDSConstants.GMOpelDIDs.OIL_PRESSURE,
                UDSConstants.GMOpelDIDs.TURBO_BOOST,
                UDSConstants.GMOpelDIDs.TURBO_RPM,
                UDSConstants.GMOpelDIDs.WASTEGATE_DUTY,
                UDSConstants.GMOpelDIDs.CHARGE_AIR_TEMP,
                UDSConstants.GMOpelDIDs.CATALYST_TEMP,
                UDSConstants.GMOpelDIDs.FUEL_PRESSURE,
                UDSConstants.GMOpelDIDs.INJECTION_QUANTITY,
                UDSConstants.GMOpelDIDs.INJECTION_TIMING
            )

            val results = mutableMapOf<String, DIDValue>()
            Log.i(TAG, "Scanning ${didsToScan.size} DIDs...")

            for (did in didsToScan) {
                try {
                    val response = udsClient.readDataByIdentifier(did).first()
                    if (response.isPositive) {
                        val didValue = udsClient.parseDIDValue(did, response.data)
                        results[did] = didValue
                        Log.v(TAG, "DID $did: ${didValue.parsedValue}")
                    }
                } catch (e: Exception) {
                    Log.v(TAG, "DID $did not available: ${e.message}")
                }
            }

            Log.i(TAG, "DID scan complete: ${results.size}/${didsToScan.size} available")
            emit(results)
        } catch (e: Exception) {
            Log.e(TAG, "DID scan error: ${e.message}")
            emit(emptyMap())
        }
    }.flowOn(Dispatchers.IO)

    fun clearDTCCodes(): Flow<Boolean> = flow {
        try {
            if (!ensureSession()) {
                emit(false)
                return@flow
            }
            udsClient.clearDTCInformation().collect { response ->
                Log.i(TAG, "Clear DTC result: ${response.isPositive}")
                emit(response.isPositive)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Clear DTC error: ${e.message}")
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun readDTCCodes(): Flow<List<Pair<String, Int>>> = flow<List<Pair<String, Int>>> {
        try {
            if (!ensureSession()) {
                emit(emptyList())
                return@flow
            }
            udsClient.readDTCInformation(0x02, 0xFF).collect { response ->
                if (response.isPositive) {
                    val dtcs = udsClient.parseDTCFromResponse(response.data)
                    Log.i(TAG, "Read ${dtcs.size} DTCs")
                    emit(dtcs)
                } else {
                    emit(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Read DTC error: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    fun requestSecurityAccess(level: Int, key: ByteArray? = null): Flow<Boolean> = flow {
        try {
            if (!ensureSession()) {
                emit(false)
                return@flow
            }
            udsClient.securityAccess(level, key).collect { response ->
                Log.i(TAG, "Security access level $level: ${response.isPositive}")
                emit(response.isPositive)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Security access error: ${e.message}")
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun executeRoutine(routineId: String): Flow<Boolean> = flow {
        try {
            if (!ensureSession()) {
                emit(false)
                return@flow
            }
            udsClient.routineControl(RoutineControlType.START, routineId).collect { response ->
                Log.i(TAG, "Routine $routineId result: ${response.isPositive}")
                emit(response.isPositive)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Routine execution error: ${e.message}")
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun resetECU(): Flow<Boolean> = flow {
        try {
            udsClient.ecuReset(0x01).collect { response ->
                Log.i(TAG, "ECU reset: ${response.isPositive}")
                sessionActive.set(false)
                extendedSessionActive.set(false)
                emit(response.isPositive)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ECU reset error: ${e.message}")
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun isSessionActive(): Boolean = sessionActive.get()

    fun isExtendedSessionActive(): Boolean = extendedSessionActive.get()

    fun endSession(): Flow<Boolean> = flow {
        try {
            val response = udsClient.diagnosticSessionControl(UDSSessionType.DEFAULT).first()
            sessionActive.set(false)
            extendedSessionActive.set(false)
            Log.i(TAG, "Session ended: ${response.isPositive}")
            emit(response.isPositive)
        } catch (e: Exception) {
            Log.e(TAG, "End session error: ${e.message}")
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    fun getUDSClient(): UDSClient = udsClient
}

object UDSConstants {
    object GMOpelDIDs {
        const val ECU_INFO = "F4F0"
        const val TORQUE = "F4B0"
        const val BOOST_PRESSURE = "F4C0"
        const val COOLANT_TEMP = "F4E0"
        const val BATTERY_VOLTAGE = "F4F1"
        const val FUEL_CONSUMPTION = "F480"
        const val VIN = "F190"
        const val CALIBRATION_ID = "F191"
        const val CVN = "F192"
        const val FUEL_PRESSURE = "F440"
        const val INTAKE_AIR_TEMP = "F425"
        const val AMBIENT_TEMP = "F42F"
        const val ENGINE_SPEED = "F437"
        const val VEHICLE_SPEED = "F438"
        const val THROTTLE_POSITION = "F449"
        const val ENGINE_LOAD = "F442"
        const val FUEL_LEVEL = "F42A"
        const val OIL_TEMP = "F43C"
        const val OIL_PRESSURE = "F441"
        const val INJECTION_QUANTITY = "F450"
        const val INJECTION_TIMING = "F451"
        const val TURBO_BOOST = "F460"
        const val TURBO_RPM = "F461"
        const val WASTEGATE_DUTY = "F462"
        const val CHARGE_AIR_TEMP = "F463"
        const val CATALYST_TEMP = "F470"
        const val WIDEBAND_LAMBDA = "F4A0"
    }

    object GMRoutines {
        const val CHECK_PROGRAMMING_PRECONDITIONS = "0201"
        const val ERASE_MEMORY = "FF00"
        const val CHECK_DTC = "0301"
        const val ENGINE_DATA_CLEAR = "DE00"
    }
}
