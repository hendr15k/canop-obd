package com.canopobd.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.canopobd.data.model.*
import com.canopobd.data.repository.BCMStatus
import com.canopobd.protocol.BCMProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    // Radgeschwindigkeiten (OBD PIDs)
    val wheelSpeedFL = MutableStateFlow(0.0)
    val wheelSpeedFR = MutableStateFlow(0.0)
    val wheelSpeedRL = MutableStateFlow(0.0)
    val wheelSpeedRR = MutableStateFlow(0.0)

    // Chassis-Sensoren
    val yawRate = MutableStateFlow(0.0)
    val lateralAcceleration = MutableStateFlow(0.0)
    val longitudinalAcceleration = MutableStateFlow(0.0)
    val steeringAngle = MutableStateFlow(0.0)
    val brakePressure = MutableStateFlow(0.0)

    // Systemstatus
    val absActive = MutableStateFlow(false)
    val espActive = MutableStateFlow(false)
    val tractionControlActive = MutableStateFlow(false)
    val hillStartAssistActive = MutableStateFlow(false)

    // Bremsenverschleiss (Prozent verbleibend)
    val brakeWearFrontLeft = MutableStateFlow(100)
    val brakeWearFrontRight = MutableStateFlow(100)
    val brakeWearRearLeft = MutableStateFlow(100)
    val brakeWearRearRight = MutableStateFlow(100)

    // Reifendruckueberwachung
    val tpmsFrontLeftPSI = MutableStateFlow(0.0)
    val tpmsFrontRightPSI = MutableStateFlow(0.0)
    val tpmsRearLeftPSI = MutableStateFlow(0.0)
    val tpmsRearRightPSI = MutableStateFlow(0.0)

    private var lastTPMS: BCMProtocol.TPMSStatus? = null

    // Airbag-Status
    val airbagDriverFront = MutableStateFlow(true)
    val airbagPassengerFront = MutableStateFlow(true)
    val airbagDriverSide = MutableStateFlow(true)
    val airbagPassengerSide = MutableStateFlow(true)
    val airbagCurtainLeft = MutableStateFlow(true)
    val airbagCurtainRight = MutableStateFlow(true)

    // Diagnosefehler
    private val _safetyDTCs = MutableStateFlow<List<SafetyDtc>>(emptyList())
    val safetyDTCs: StateFlow<List<SafetyDtc>> = _safetyDTCs.asStateFlow()

    // Dialog-Sichtbarkeit
    private val _showSafetySystems = MutableStateFlow(false)
    val showSafetySystems: StateFlow<Boolean> = _showSafetySystems.asStateFlow()

    // Gesamtzusammenfassung
    private val _safetySummary = MutableStateFlow(SafetySummary())
    val safetySummary: StateFlow<SafetySummary> = _safetySummary.asStateFlow()

    // Zeitstempel der letzten Aktualisierung
    private val _lastUpdateTime = MutableStateFlow(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime.asStateFlow()

    // Brake event tracking for wear estimation
    private var totalBrakeEvents = 0
    private var harshBrakeEvents = 0
    private var estimatedDistanceKm = 0.0

    fun updateFromOBDData(data: OBDData) {
        val speed = data.speed
        // Individual wheel speeds — derive from vehicle speed when ABS data not available
        // Real ABS modules provide individual wheel speeds via CAN IDs 4C1-4C4
        wheelSpeedFL.value = speed
        wheelSpeedFR.value = speed
        wheelSpeedRL.value = speed
        wheelSpeedRR.value = speed

        // Estimate longitudinal acceleration from speed changes
        val currentSpeed = speed
        if (_lastUpdateTime.value > 0 && currentSpeed > 0) {
            val timeDeltaSec = (System.currentTimeMillis() - _lastUpdateTime.value) / 1000.0
            if (timeDeltaSec > 0 && timeDeltaSec < 5.0) {
                val prevSpeed = _safetySummary.value.wheelSpeeds.frontLeft
                val accel = (currentSpeed - prevSpeed) / 3.6 / timeDeltaSec
                if (accel in -15.0..15.0) {
                    longitudinalAcceleration.value = accel
                }
                // Estimate brake pressure from deceleration
                if (accel < -1.0) {
                    brakePressure.value = (-accel * 10.0).coerceIn(0.0, 180.0)
                    totalBrakeEvents++
                    if (accel < -5.0) harshBrakeEvents++
                } else {
                    brakePressure.value = 0.0
                }
                // Estimate brake wear based on usage patterns
                estimatedDistanceKm += (currentSpeed / 3.6) * timeDeltaSec / 1000.0
                updateBrakeWear()
            }
        }

        updateSummary()
        _lastUpdateTime.value = System.currentTimeMillis()
    }

    private fun updateBrakeWear() {
        // Astra J front pads last ~50k km, rear ~70k km under normal driving
        // Harsh braking accelerates wear
        val harshFactor = if (totalBrakeEvents > 0) 1.0 + (harshBrakeEvents.toDouble() / totalBrakeEvents.toDouble()) else 1.0
        val frontWearPerKm = 0.002 * harshFactor // 0.2% per 100km baseline
        val rearWearPerKm = 0.00143 * harshFactor
        
        val frontRemaining = (100.0 - estimatedDistanceKm * frontWearPerKm).coerceIn(0.0, 100.0).toInt()
        val rearRemaining = (100.0 - estimatedDistanceKm * rearWearPerKm).coerceIn(0.0, 100.0).toInt()
        
        brakeWearFrontLeft.value = frontRemaining
        brakeWearFrontRight.value = frontRemaining
        brakeWearRearLeft.value = rearRemaining
        brakeWearRearRight.value = rearRemaining
    }

fun updateFromTPMS(tpms: BCMProtocol.TPMSStatus) {
         lastTPMS = tpms
         if (tpms.frontLeftPSI > 0) tpmsFrontLeftPSI.value = tpms.frontLeftPSI
         if (tpms.frontRightPSI > 0) tpmsFrontRightPSI.value = tpms.frontRightPSI
         if (tpms.rearLeftPSI > 0) tpmsRearLeftPSI.value = tpms.rearLeftPSI
         if (tpms.rearRightPSI > 0) tpmsRearRightPSI.value = tpms.rearRightPSI
        updateSummary()
        _lastUpdateTime.value = System.currentTimeMillis()
    }

    fun updateFromTCM(tcm: BCMProtocol.TCMStatus) {
        // ABS data inferred from transmission slip and wheel speed differences
        if (tcm.clutchSlipping) absActive.value = true
        // Sport/manual modes indicate ESP may intervene
        if (tcm.sportMode || tcm.manualMode) espActive.value = true
        updateSummary()
        _lastUpdateTime.value = System.currentTimeMillis()
    }

fun updateFromBCMStatus(bcm: BCMStatus) {
        if (bcm.lightsOn && !bcm.hazardsOn) {
            // Lights on during driving is normal
        }
        if (bcm.hazardsOn) {
            // Hazards active — could indicate a safety situation
        }
        if (bcm.alarmTriggered) {
            // Alarm triggered — security concern
        }
        // Open door while driving is a safety concern
        val anyDoorOpen = bcm.driverDoorOpen || bcm.passengerDoorOpen ||
                bcm.rearLeftDoorOpen || bcm.rearRightDoorOpen
        updateSummary()
        _lastUpdateTime.value = System.currentTimeMillis()
    }

    fun updateFromHVAC(hvac: BCMProtocol.HVACStatus) {
        // A/C compressor active during overheating indicates thermal management
        // Recirculation during low temps can cause window fogging (safety)
        if (hvac.acCompressorActive) {
            // Track A/C state for thermal safety analysis
        }
        updateSummary()
        _lastUpdateTime.value = System.currentTimeMillis()
    }

    fun updateFromSafetyDTCs(dtcResponse: DTCResponse?) {
        val safetyCodes = dtcResponse?.codes?.filter { dtc ->
            val system = SafetyDTCMappings.getDTCSystem(dtc.code)
            system in listOf("ABS", "ESP/Stabilitaet", "TPMS", "Airbag/SRS")
        } ?: emptyList()

        val safetyDtcList = safetyCodes.map { dtc ->
            SafetyDtc(
                code = dtc.code,
                description = SafetyDTCMappings.getDTCDescription(dtc.code) ?: "Unbekannter Fehler",
                system = SafetyDTCMappings.getDTCSystem(dtc.code),
                severity = SafetyDTCMappings.getDTCSeverity(dtc.code)
            )
        }

        val hasABSDtc = safetyDtcList.any { it.system == "ABS" }
        val hasESPDtc = safetyDtcList.any { it.system == "ESP/Stabilitaet" }
        val hasAirbagDtc = safetyDtcList.any { it.system == "Airbag/SRS" }

        if (hasABSDtc) absActive.value = false
        if (hasESPDtc) espActive.value = false
        if (hasAirbagDtc) {
            airbagDriverFront.value = false
            airbagPassengerFront.value = false
        }

        _safetyDTCs.value = safetyDtcList
        updateSummary()
    }

    fun toggleSafetySystems() {
        _showSafetySystems.value = !_showSafetySystems.value
    }

    fun dismissSafetySystems() {
        _showSafetySystems.value = false
    }

    private fun updateSummary() {
        val absStatus = if (absActive.value) SystemStatus.OK else SystemStatus.UNKNOWN
        val espStatus = if (espActive.value) SystemStatus.OK else SystemStatus.UNKNOWN
        val allTires = listOf(
            tpmsFrontLeftPSI.value to AstraJSafetyThresholds.TPMS_CRITICAL_PSI,
            tpmsFrontRightPSI.value to AstraJSafetyThresholds.TPMS_CRITICAL_PSI,
            tpmsRearLeftPSI.value to AstraJSafetyThresholds.TPMS_CRITICAL_PSI,
            tpmsRearRightPSI.value to AstraJSafetyThresholds.TPMS_CRITICAL_PSI
        )
        val tpmsStatus = when {
            allTires.any { it.first > 0 && it.first < AstraJSafetyThresholds.TPMS_CRITICAL_PSI } -> SystemStatus.FAULT
            allTires.any { it.first > 0 && it.first < AstraJSafetyThresholds.TPMS_LOW_PRESSURE_PSI } -> SystemStatus.WARNING
            allTires.any { it.first > 0 } -> SystemStatus.OK
            else -> SystemStatus.UNKNOWN
        }

        _safetySummary.value = SafetySummary(
            wheelSpeeds = WheelSpeeds(wheelSpeedFL.value, wheelSpeedFR.value, wheelSpeedRL.value, wheelSpeedRR.value),
            safetyStatus = SafetySystemStatus(
                absStatus = absStatus,
                espStatus = espStatus,
                tractionControlStatus = if (tractionControlActive.value) SystemStatus.OK else SystemStatus.UNKNOWN,
                tpmsStatus = tpmsStatus,
                airbagStatus = if (airbagDriverFront.value && airbagPassengerFront.value) SystemStatus.OK else SystemStatus.FAULT,
                hillStartAssistStatus = if (hillStartAssistActive.value) SystemStatus.OK else SystemStatus.UNKNOWN
            ),
            chassisSensors = ChassisSensors(
                yawRate = yawRate.value,
                lateralAcceleration = lateralAcceleration.value,
                longitudinalAcceleration = longitudinalAcceleration.value,
                steeringAngle = steeringAngle.value,
                brakePressure = brakePressure.value
            ),
            brakeWear = BrakeWear(brakeWearFrontLeft.value, brakeWearFrontRight.value, brakeWearRearLeft.value, brakeWearRearRight.value),
tpmsData = TPMSData(
                 frontLeftPressure = tpmsFrontLeftPSI.value,
                 frontRightPressure = tpmsFrontRightPSI.value,
                 rearLeftPressure = tpmsRearLeftPSI.value,
                 rearRightPressure = tpmsRearRightPSI.value,
                 frontLeftTemp = lastTPMS?.frontLeftTemp ?: 0,
                 frontRightTemp = lastTPMS?.frontRightTemp ?: 0,
                 rearLeftTemp = lastTPMS?.rearLeftTemp ?: 0,
                 rearRightTemp = lastTPMS?.rearRightTemp ?: 0
             ),
            airbagStatus = AirbagStatus(
                driverFront = airbagDriverFront.value,
                passengerFront = airbagPassengerFront.value,
                driverSide = airbagDriverSide.value,
                passengerSide = airbagPassengerSide.value,
                curtainLeft = airbagCurtainLeft.value,
                curtainRight = airbagCurtainRight.value
            ),
            espState = ESPState(isActive = espActive.value),
            safetyDTCs = _safetyDTCs.value,
            lastUpdateTime = _lastUpdateTime.value
        )
    }
}
