package com.canopobd.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.canopobd.data.model.*
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
    val brakeWearFrontLeft = MutableStateFlow(85)
    val brakeWearFrontRight = MutableStateFlow(85)
    val brakeWearRearLeft = MutableStateFlow(90)
    val brakeWearRearRight = MutableStateFlow(90)

    // Reifendruckueberwachung
    val tpmsFrontLeftPSI = MutableStateFlow(0.0)
    val tpmsFrontRightPSI = MutableStateFlow(0.0)
    val tpmsRearLeftPSI = MutableStateFlow(0.0)
    val tpmsRearRightPSI = MutableStateFlow(0.0)

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

    fun updateFromOBDData(data: OBDData) {
        // ABS-Radgeschwindigkeiten aus Fahrzeuggeschwindigkeit simuliert
        // Echte Radgeschwindigkeiten waeren ueber ABS-spezifische PIDs verfuegbar
        val speed = data.speed
        wheelSpeedFL.value = speed
        wheelSpeedFR.value = speed
        wheelSpeedRL.value = speed
        wheelSpeedRR.value = speed

        // Lenkwinkel (in manchen erweiterten PIDs verfuegbar)
        // steeringAngle.value = data.steeringAngle

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

        _safetyDTCs.value = safetyDtcList
    }

    fun toggleSafetySystems() {
        _showSafetySystems.value = !_showSafetySystems.value
    }

    fun dismissSafetySystems() {
        _showSafetySystems.value = false
    }

    private fun updateSummary() {
        val hasWheelSpeedDiff = abs(wheelSpeedFL.value - wheelSpeedFR.value) > AstraJSafetyThresholds.WHEEL_SPEED_DIFF_WARNING ||
                abs(wheelSpeedRL.value - wheelSpeedRR.value) > AstraJSafetyThresholds.WHEEL_SPEED_DIFF_WARNING

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
                rearRightPressure = tpmsRearRightPSI.value
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
