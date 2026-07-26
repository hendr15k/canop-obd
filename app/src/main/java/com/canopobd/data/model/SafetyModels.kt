package com.canopobd.data.model

// ──────────────────────────────────────────────────────────────────────────────
// Sicherheitssysteme — Opel Astra J Datenmodelle
// ABS, ESP, Reifendruck, Airbag, Bremsenverschleiss
// ──────────────────────────────────────────────────────────────────────────────

enum class SystemStatus(val label: String, val description: String) {
    OK("OK", "System einwandfrei"),
    WARNING("Warnung", "Aufmerksamkeit erforderlich"),
    FAULT("Fehler", "Systemfehler erkannt"),
    DISABLED("Deaktiviert", "System deaktiviert"),
    UNKNOWN("Unbekannt", "Status nicht ermittelbar")
}

enum class LevelStatus(val label: String) {
    NORMAL("Normal"),
    LOW("Niedrig"),
    CRITICAL("Kritisch"),
    UNKNOWN("Unbekannt")
}

enum class DtcSeverity(val label: String, val description: String) {
    INFO("Information", "Keine Massnahme erforderlich"),
    WARNING("Warnung", "Ueberpruefung empfohlen"),
    CRITICAL("Kritisch", "Sofortige Massnahme erforderlich")
}

enum class ESPSelectedMode(val label: String, val description: String) {
    STANDARD("Standard", "ESP vollstaendig aktiv"),
    SPORT("Sport", "ESP mit erhohtem Schwellenwert"),
    OFF("Aus", "ESP deaktiviert (nur bei geringer Geschwindigkeit)")
}

data class WheelSpeeds(
    val frontLeft: Double = 0.0,
    val frontRight: Double = 0.0,
    val rearLeft: Double = 0.0,
    val rearRight: Double = 0.0,
    val unit: String = "km/h"
) {
    val maxSpeed: Double get() = maxOf(frontLeft, frontRight, rearLeft, rearRight)
    val minSpeed: Double get() = minOf(frontLeft, frontRight, rearLeft, rearRight)
    val speedDifference: Double get() = maxSpeed - minSpeed
    val frontAxleDiff: Double get() = kotlin.math.abs(frontLeft - frontRight)
    val rearAxleDiff: Double get() = kotlin.math.abs(rearLeft - rearRight)

    fun isWheelSpeedWarning(): Boolean =
        speedDifference > AstraJSafetyThresholds.WHEEL_SPEED_DIFF_WARNING

    fun isWheelSpeedCritical(): Boolean =
        speedDifference > AstraJSafetyThresholds.WHEEL_SPEED_DIFF_CRITICAL
}

data class SafetySystemStatus(
    val absStatus: SystemStatus = SystemStatus.UNKNOWN,
    val espStatus: SystemStatus = SystemStatus.UNKNOWN,
    val tractionControlStatus: SystemStatus = SystemStatus.UNKNOWN,
    val tpmsStatus: SystemStatus = SystemStatus.UNKNOWN,
    val airbagStatus: SystemStatus = SystemStatus.UNKNOWN,
    val hillStartAssistStatus: SystemStatus = SystemStatus.UNKNOWN,
    val brakeAssistStatus: SystemStatus = SystemStatus.UNKNOWN,
    val brakeFluidLevel: LevelStatus = LevelStatus.UNKNOWN,
    val parkingBrakeStatus: Boolean = false
) {
    val activeWarnings: List<String> get() = buildList {
        if (absStatus == SystemStatus.WARNING || absStatus == SystemStatus.FAULT) add("ABS")
        if (espStatus == SystemStatus.WARNING || espStatus == SystemStatus.FAULT) add("ESP")
        if (tractionControlStatus == SystemStatus.WARNING || tractionControlStatus == SystemStatus.FAULT) add("TC")
        if (tpmsStatus == SystemStatus.WARNING || tpmsStatus == SystemStatus.FAULT) add("TPMS")
        if (airbagStatus == SystemStatus.WARNING || airbagStatus == SystemStatus.FAULT) add("Airbag")
        if (brakeFluidLevel == LevelStatus.LOW || brakeFluidLevel == LevelStatus.CRITICAL) add("Bremsfluessigkeit")
    }

    val hasCriticalFault: Boolean get() =
        absStatus == SystemStatus.FAULT ||
            espStatus == SystemStatus.FAULT ||
            airbagStatus == SystemStatus.FAULT ||
            brakeFluidLevel == LevelStatus.CRITICAL
}

data class ChassisSensors(
    val yawRate: Double = 0.0,
    val lateralAcceleration: Double = 0.0,
    val longitudinalAcceleration: Double = 0.0,
    val steeringAngle: Double = 0.0,
    val brakePressure: Double = 0.0,
    val pitchAngle: Double = 0.0
) {
    val isYawRateWarning: Boolean get() =
        kotlin.math.abs(yawRate) > AstraJSafetyThresholds.YAW_RATE_WARNING

    val isLateralAccelWarning: Boolean get() =
        kotlin.math.abs(lateralAcceleration) > AstraJSafetyThresholds.LATERAL_ACCEL_WARNING

    val isSteeringAngleValid: Boolean get() =
        kotlin.math.abs(steeringAngle) <= AstraJSafetyThresholds.STEERING_ANGLE_MAX

    val totalAcceleration: Double get() =
        kotlin.math.sqrt(lateralAcceleration * lateralAcceleration + longitudinalAcceleration * longitudinalAcceleration)
}

data class BrakeWear(
    val frontLeft: Int = 100,
    val frontRight: Int = 100,
    val rearLeft: Int = 100,
    val rearRight: Int = 100,
    val estimatedKmRemaining: Int = 15000
) {
    val minFront: Int get() = minOf(frontLeft, frontRight)
    val minRear: Int get() = minOf(rearLeft, rearRight)
    val overallMin: Int get() = minOf(minFront, minRear)

    val isFrontWarning: Boolean get() =
        minFront < AstraJSafetyThresholds.BRAKE_WEAR_WARNING

    val isFrontCritical: Boolean get() =
        minFront < AstraJSafetyThresholds.BRAKE_WEAR_CRITICAL

    val isRearWarning: Boolean get() =
        minRear < AstraJSafetyThresholds.BRAKE_WEAR_WARNING

    val isRearCritical: Boolean get() =
        minRear < AstraJSafetyThresholds.BRAKE_WEAR_CRITICAL

    val needsService: Boolean get() = isFrontWarning || isRearWarning
}

data class TPMSData(
    val frontLeftPressure: Double = 0.0,
    val frontRightPressure: Double = 0.0,
    val rearLeftPressure: Double = 0.0,
    val rearRightPressure: Double = 0.0,
    val frontLeftTemp: Int = 0,
    val frontRightTemp: Int = 0,
    val rearLeftTemp: Int = 0,
    val rearRightTemp: Int = 0,
    val unit: String = "PSI",
    val systemType: String = "Direct"
) {
    val pressures: List<Double> get() = listOf(frontLeftPressure, frontRightPressure, rearLeftPressure, rearRightPressure)
    val temperatures: List<Int> get() = listOf(frontLeftTemp, frontRightTemp, rearLeftTemp, rearRightTemp)

    val avgPressure: Double get() = pressures.average()
    val maxPressure: Double get() = pressures.max()
    val minPressure: Double get() = pressures.min()
    val pressureDifference: Double get() = maxPressure - minPressure

    val maxTemp: Double get() = temperatures.maxOrNull()?.toDouble() ?: 0.0

    val isLowPressure: Boolean get() = when (unit) {
        "PSI" -> minPressure < AstraJSafetyThresholds.TPMS_LOW_PRESSURE_PSI && minPressure > 0.0
        "bar" -> minPressure < (AstraJSafetyThresholds.TPMS_LOW_PRESSURE_PSI * 0.0689476) && minPressure > 0.0
        else -> false
    }

    val isCriticalPressure: Boolean get() = when (unit) {
        "PSI" -> minPressure < AstraJSafetyThresholds.TPMS_CRITICAL_PSI && minPressure > 0.0
        "bar" -> minPressure < (AstraJSafetyThresholds.TPMS_CRITICAL_PSI * 0.0689476) && minPressure > 0.0
        else -> false
    }

    val isHighPressure: Boolean get() = when (unit) {
        "PSI" -> maxPressure > AstraJSafetyThresholds.TPMS_HIGH_PRESSURE_PSI
        "bar" -> maxPressure > (AstraJSafetyThresholds.TPMS_HIGH_PRESSURE_PSI * 0.0689476)
        else -> false
    }

    val defaultPressure: Double get() = when (unit) {
        "PSI" -> AstraJSafetyThresholds.TPMS_DEFAULT_PSI
        "bar" -> AstraJSafetyThresholds.TPMS_DEFAULT_PSI * 0.0689476
        else -> 0.0
    }
}

data class SafetyDtc(
    val code: String,
    val description: String,
    val system: String,
    val severity: DtcSeverity,
    val isPending: Boolean = false,
    val isPermanent: Boolean = false,
    val freezeFrameAvailable: Boolean = false
) {
    val isCritical: Boolean get() = severity == DtcSeverity.CRITICAL
    val displayCode: String get() = code.uppercase()
}

data class ESPState(
    val isActive: Boolean = false,
    val isSportMode: Boolean = false,
    val interventionLevel: Int = 0,
    val torqueReduction: Int = 0,
    val selectedMode: ESPSelectedMode = ESPSelectedMode.STANDARD
) {
    val isFullyActive: Boolean get() = isActive && selectedMode == ESPSelectedMode.STANDARD
    val isDisabled: Boolean get() = selectedMode == ESPSelectedMode.OFF
    val isIntervening: Boolean get() = isActive && interventionLevel > 0
}

data class AirbagStatus(
    val driverFront: Boolean = true,
    val passengerFront: Boolean = true,
    val driverSide: Boolean = true,
    val passengerSide: Boolean = true,
    val curtainLeft: Boolean = true,
    val curtainRight: Boolean = true,
    val driverKnee: Boolean = true,
    val pretensionerDriver: Boolean = true,
    val pretensionerPassenger: Boolean = true,
    val systemReady: Boolean = true,
    val passengerAirbagDisabled: Boolean = false
) {
    val allAirbagsReady: Boolean get() =
        driverFront && passengerFront && driverSide && passengerSide &&
            curtainLeft && curtainRight && driverKnee &&
            pretensionerDriver && pretensionerPassenger && systemReady

    val hasFault: Boolean get() = !allAirbagsReady

    val faultCount: Int get() = listOf(
        driverFront, passengerFront, driverSide, passengerSide,
        curtainLeft, curtainRight, driverKnee,
        pretensionerDriver, pretensionerPassenger, systemReady
    ).count { !it }
}

// ──────────────────────────────────────────────────────────────────────────────
// Schwellenwerte fuer Opel Astra J
// ──────────────────────────────────────────────────────────────────────────────

object AstraJSafetyThresholds {
    // Bremsenverschleiss
    const val BRAKE_WEAR_WARNING = 30
    const val BRAKE_WEAR_CRITICAL = 15
    const val BRAKE_FLUID_MIN_BAR = 1.5

    // Reifendruck (Werkseinstellung typischerweise 32-36 PSI / 2.2-2.5 bar)
    const val TPMS_LOW_PRESSURE_PSI = 28.0
    const val TPMS_CRITICAL_PSI = 24.0
    const val TPMS_HIGH_PRESSURE_PSI = 42.0
    const val TPMS_DEFAULT_PSI = 32.0

    // Fahrwerk / Chassis
    const val YAW_RATE_WARNING = 30.0
    const val LATERAL_ACCEL_WARNING = 0.8
    const val STEERING_ANGLE_MAX = 720.0

    // Radgeschwindigkeiten
    const val WHEEL_SPEED_DIFF_WARNING = 5.0
    const val WHEEL_SPEED_DIFF_CRITICAL = 10.0
}

// ──────────────────────────────────────────────────────────────────────────────
// Uebersicht aller Sicherheitssysteme
// ──────────────────────────────────────────────────────────────────────────────

data class SafetySummary(
    val overallStatus: SystemStatus = SystemStatus.UNKNOWN,
    val wheelSpeeds: WheelSpeeds = WheelSpeeds(),
    val safetyStatus: SafetySystemStatus = SafetySystemStatus(),
    val chassisSensors: ChassisSensors = ChassisSensors(),
    val brakeWear: BrakeWear = BrakeWear(),
    val tpmsData: TPMSData = TPMSData(),
    val airbagStatus: AirbagStatus = AirbagStatus(),
    val espState: ESPState = ESPState(),
    val safetyDTCs: List<SafetyDtc> = emptyList(),
    val lastUpdateTime: Long = 0L
) {
    val hasWarnings: Boolean
        get() = safetyStatus.absStatus == SystemStatus.WARNING ||
            safetyStatus.espStatus == SystemStatus.WARNING ||
            safetyStatus.tpmsStatus == SystemStatus.WARNING ||
            brakeWear.frontLeft < AstraJSafetyThresholds.BRAKE_WEAR_WARNING

    val hasCritical: Boolean
        get() = safetyStatus.absStatus == SystemStatus.FAULT ||
            safetyStatus.espStatus == SystemStatus.FAULT ||
            safetyStatus.airbagStatus == SystemStatus.FAULT ||
            brakeWear.frontLeft < AstraJSafetyThresholds.BRAKE_WEAR_CRITICAL

    val dtcCount: Int get() = safetyDTCs.size
    val criticalDtcCount: Int get() = safetyDTCs.count { it.isCritical }
    val activeWarningCount: Int get() = safetyStatus.activeWarnings.size
}

// ──────────────────────────────────────────────────────────────────────────────
// DTC-Zuordnungstabellen fuer Astra J Sicherheitssysteme
// ──────────────────────────────────────────────────────────────────────────────

object SafetyDTCMappings {

    // ABS-Fehlercodes (C-codes)
    val ABS_DTCS = mapOf(
        "C0035" to "Linker Vorderrad-Geschwindigkeitssensor Stromkreis",
        "C0040" to "Rechter Vorderrad-Geschwindigkeitssensor Stromkreis",
        "C0045" to "Linker Hinterrad-Geschwindigkeitssensor Stromkreis",
        "C0050" to "Rechter Hinterrad-Geschwindigkeitssensor Stromkreis",
        "C0110" to "ABS-Pumpenmotor Stromkreis",
        "C0161" to "Bremslichtschalter Stromkreis",
        "C0036" to "Linker Vorderrad-Geschwindigkeitssensor Bereich/Leistung",
        "C0041" to "Rechter Vorderrad-Geschwindigkeitssensor Bereich/Leistung"
    )

    // ESP-Fehlercodes
    val ESP_DTCS = mapOf(
        "C0235" to "Gierraten-Sensor Stromkreis",
        "C0236" to "Querbeschleunigungssensor Stromkreis",
        "C0241" to "Lenkwinkelsensor Stromkreis",
        "C0245" to "Gierraten-Sensor Leistung",
        "C0251" to "ESP-Aus-Schalter Stromkreis"
    )

    // Airbag-Fehlercodes (B-codes)
    val AIRBAG_DTCS = mapOf(
        "B0016" to "Fahrer-Frontalstufe 1 Ausloesekontrolle",
        "B0017" to "Fahrer-Frontalstufe 2 Ausloesekontrolle",
        "B0020" to "Beifahrer-Frontalstufe 1 Ausloesekontrolle",
        "B0022" to "Linke Seitenairbag Ausloesekontrolle",
        "B0023" to "Rechte Seitenairbag Ausloesekontrolle",
        "B0050" to "Fahrer-Gurtstraffer Stromkreis",
        "B0051" to "Beifahrer-Gurtstraffer Stromkreis",
        "B1000" to "Sensing and Diagnostic Module interner Fehler"
    )

    // TPMS-Fehlercodes
    val TPMS_DTCS = mapOf(
        "C0750" to "Linker Vorderer Reifendrucksensor",
        "C0755" to "Rechter Vorderer Reifendrucksensor",
        "C0760" to "Linker Hinterer Reifendrucksensor",
        "C0765" to "Rechter Hinterer Reifendrucksensor",
        "C0775" to "TPMS-Empfaengermodul"
    )

    private val ALL_DTCS: Map<String, String> = ABS_DTCS + ESP_DTCS + AIRBAG_DTCS + TPMS_DTCS

    fun getDTCSeverity(code: String): DtcSeverity {
        val normalized = code.uppercase()
        val plainCode = normalized.substringBefore(" ").trim()
        val embeddedDesc = normalized.substringAfter(" ", "").trim()
        val lookedUpDesc = ALL_DTCS[plainCode]?.uppercase() ?: ""
        val desc = if (lookedUpDesc.isNotEmpty()) lookedUpDesc else embeddedDesc
        return when {
            normalized.startsWith("B0") -> DtcSeverity.CRITICAL
            normalized.startsWith("C0") && (desc.contains("CIRCUIT") || desc.contains("MODULE") || desc.contains("PUMPE") || desc.contains("BREMSE")) -> DtcSeverity.CRITICAL
            normalized.startsWith("C0") && (desc.contains("RANGE") || desc.contains("PERFORMANCE") || desc.contains("LEISTUNG") || desc.contains("BEREICH")) -> DtcSeverity.WARNING
            normalized.startsWith("C0") -> DtcSeverity.CRITICAL
            else -> DtcSeverity.INFO
        }
    }

    fun getDTCSystem(code: String): String {
        val normalized = code.uppercase()
        val description = ALL_DTCS[normalized]?.uppercase() ?: ""
        return when {
            normalized.startsWith("B") -> "Airbag/SRS"
            normalized.startsWith("C") && isABSCode(description) -> "ABS"
            normalized.startsWith("C") && isESPCode(description) -> "ESP/Stabilitaet"
            normalized.startsWith("C") && isTPMSCode(description) -> "TPMS"
            else -> "Unbekannt"
        }
    }

    private fun isABSCode(desc: String): Boolean = desc.let {
        it.contains("GESCHWINDIGKEIT") || it.contains("ABS") ||
            it.contains("PUMPE") || it.contains("BREMSE")
    }

    private fun isESPCode(desc: String): Boolean = desc.let {
        it.contains("ESP") || it.contains("GIERRATEN") ||
            it.contains("BESCHLEUNIGUNG") || it.contains("LENKWINKEL") ||
            it.contains("QUER")
    }

    private fun isTPMSCode(desc: String): Boolean = desc.let {
        it.contains("REIFEN") || it.contains("TPMS") || it.contains("REIFENDRUCK")
    }

    fun getDTCDescription(code: String): String? = ALL_DTCS[code.uppercase()]

    fun isKnownDTC(code: String): Boolean = ALL_DTCS.containsKey(code.uppercase())
}
