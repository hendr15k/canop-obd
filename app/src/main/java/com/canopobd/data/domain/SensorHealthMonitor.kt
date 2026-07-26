package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import com.canopobd.data.model.OBDData
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Umfassender Sensor-Gesundheits-Monitor für OBD-Systeme
 *
 * Implementiert:
 * - Range Validation (Plausibilitätsprüfung)
 * - Rate of Change Check (Änderungsrate)
 * - Plausibility Check (Kreuzvalidierung)
 * - Drift Detection (Drift-Erkennung)
 * - Health Score pro Sensor
 *
 * Basierend auf OBD-II Standards und Sensor-Validierungsmethoden:
 * - MAF Sensor: Hot-wire mass flow validation
 * - MAP/Boost Sensor: Absolute pressure validation
 * - Temperatursensoren: Range checks und Korrelation
 * - O2 Sensor: Cross-check validation
 */
class SensorHealthMonitor(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    // History for rate of change and drift detection
    private val sensorHistory = mutableMapOf<SensorType, MutableList<SensorReading>>()

    // Calibration baselines for drift detection
    private val calibrationBaselines = mutableMapOf<SensorType, Double>()

    // Diagnostic trouble codes detected
    private val sensorDTCs = mutableListOf<SensorDTC>()

    companion object {
        // Maximum rate of change thresholds (per second)
        private const val MAX_RPM_CHANGE = 1500.0 // rpm/s
        private const val MAX_SPEED_CHANGE = 50.0 // km/h/s
        private const val MAX_MAF_CHANGE = 25.0 // g/s per sample
        private const val MAX_BOOST_CHANGE = 0.3 // bar per sample
        private const val MAX_COOLANT_CHANGE = 2.0 // °C per sample
        private const val MAX_INTAKE_CHANGE = 3.0 // °C per sample
        private const val MAX_OIL_CHANGE = 2.0 // °C per sample
        private const val MAX_EGT_CHANGE = 50.0 // °C per sample
        private const val MAX_VOLTAGE_CHANGE = 0.5 // V per sample

        // History size for analysis
        private const val HISTORY_SIZE = 60 // samples to keep

        // Drift detection thresholds
        private const val DRIFT_THRESHOLD_MAF = 5.0 // g/s drift from baseline
        private const val DRIFT_THRESHOLD_COOLANT = 3.0 // °C drift from baseline
        private const val DRIFT_THRESHOLD_OIL = 5.0 // °C drift from baseline
        private const val DRIFT_THRESHOLD_BOOST = 0.15 // bar drift from baseline

        // Plausibility correlation thresholds
        private const val MAF_RPM_RATIO_MIN = 0.001 // min g/s per rpm
        private const val MAF_RPM_RATIO_MAX = 0.015 // max g/s per rpm
        private const val BOOST_LOAD_RATIO = 0.015 // bar per %
        private const val TEMP_CORRELATION_MIN = 0.7 // min correlation coeff

        // Physical limits for range validation
        private const val ABSOLUTE_MIN_TEMP = -40.0 // °C (sensor failure)
        private const val ABSOLUTE_MAX_TEMP = 150.0 // °C (overheat)
        private const val ABSOLUTE_MAX_RPM = 8000.0
        private const val ABSOLUTE_MAX_BOOST = 2.0 // bar
        private const val ABSOLUTE_MIN_VOLTAGE = 9.0 // V
        private const val ABSOLUTE_MAX_VOLTAGE = 16.0 // V
    }

    /**
     * Sensor types that can be monitored
     */
    enum class SensorType {
        RPM,
        SPEED,
        MAF,
        BOOST,
        COOLANT_TEMP,
        INTAKE_TEMP,
        OIL_TEMP,
        EGT,
        BATTERY_VOLTAGE,
        FUEL_LEVEL,
        THROTTLE,
        ENGINE_LOAD,
        O2_SENSOR,
        OBD_WIDGET_DATA
    }

    /**
     * Health status levels
     */
    enum class HealthStatus(val label: String, val colorHex: Long) {
        EXCELLENT("Ausgezeichnet", 0xFF00FF88),
        GOOD("Gut", 0xFF88FF44),
        FAIR("Befriedigend", 0xFFFFE066),
        POOR("Schlecht", 0xFFFF8C00),
        CRITICAL("Kritisch", 0xFFFF4444),
        UNAVAILABLE("Nicht verfügbar", 0xFF888888),
        UNKNOWN("Unbekannt", 0xFFAAAAAA)
    }

    /**
     * Validation issue types
     */
    enum class ValidationIssue {
        RANGE_OUT_OF_BOUNDS,
        RATE_OF_CHANGE_EXCEEDED,
        PLAUSIBILITY_FAILED,
        DRIFT_DETECTED,
        CORRELATION_FAILED,
        SENSOR_STUCK,
        SIGNAL_LOSS,
        INTERMITTENT_FAILURE
    }

    /**
     * Individual sensor reading with timestamp
     */
    data class SensorReading(
        val value: Double,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Health result for a single sensor
     */
    data class SensorHealth(
        val sensorType: SensorType,
        val status: HealthStatus,
        val healthScore: Int, // 0-100
        val currentValue: Double,
        val unit: String,
        val issues: List<ValidationIssue> = emptyList(),
        val warnings: List<String> = emptyList(),
        val diagnosis: String = "",
        val lastValidReading: SensorReading? = null,
        val readingCount: Int = 0,
        val failureCount: Int = 0,
        val driftFromBaseline: Double = 0.0,
        val stabilityScore: Int = 100 // 0-100, based on variance
    )

    /**
     * Overall sensor health summary
     */
    data class SensorHealthSummary(
        val overallHealthScore: Int, // 0-100
        val overallStatus: HealthStatus,
        val sensorHealths: Map<SensorType, SensorHealth>,
        val criticalIssues: List<String>,
        val recommendations: List<String>,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Sensor DTC for detected issues
     */
    data class SensorDTC(
        val code: String,
        val description: String,
        val sensorType: SensorType,
        val timestamp: Long,
        val severity: Int
    )

    /**
     * Plausibility check result
     */
    data class PlausibilityResult(
        val isPlausible: Boolean,
        val correlation: Double, // 0.0 - 1.0
        val expectedRange: ClosedFloatingPointRange<Double>,
        val actualValue: Double,
        val diagnosis: String
    )

    /**
     * Analyze all sensors from OBD data
     */
    fun analyzeSensors(data: OBDData): SensorHealthSummary {
        val sensorHealths = mutableMapOf<SensorType, SensorHealth>()
        val criticalIssues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        // Analyze each sensor
        val rpmHealth = analyzeRPM(data.rpm)
        val speedHealth = analyzeSpeed(data.speed)
        val mafHealth = analyzeMAF(data.mafRate, data.rpm)
        val boostHealth = analyzeBoost(data.intakePressure, data.barometricPressure, data.engineLoad)
        val coolantHealth = analyzeCoolantTemp(data.coolantTemp)
        val intakeHealth = analyzeIntakeTemp(data.intakeTemp, data.coolantTemp)
        val oilHealth = analyzeOilTemp(data.oilTemp)
        val batteryHealth = analyzeBatteryVoltage(data.batteryVoltage)
        val throttleHealth = analyzeThrottle(data.throttle, data.acceleratorPosD)
        val loadHealth = analyzeEngineLoad(data.engineLoad)
        val fuelHealth = analyzeFuelLevel(data.fuelLevel)
        val o2Health = analyzeO2Sensor(data.o2VoltageB1S1)

        // Collect all health results
        listOf(
            rpmHealth, speedHealth, mafHealth, boostHealth, coolantHealth,
            intakeHealth, oilHealth, batteryHealth, throttleHealth,
            loadHealth, fuelHealth, o2Health
        ).forEach { health ->
            sensorHealths[health.sensorType] = health
            if (health.status == HealthStatus.CRITICAL) {
                criticalIssues.add("${health.sensorType.name}: ${health.diagnosis}")
            }
            health.warnings.forEach { warnings ->
                recommendations.add(warnings)
            }
        }

        // Calculate overall health score
        val activeHealths = sensorHealths.values.filter {
            it.status != HealthStatus.UNAVAILABLE && it.status != HealthStatus.UNKNOWN
        }
        val overallScore = if (activeHealths.isNotEmpty()) {
            activeHealths.sumOf { it.healthScore } / activeHealths.size
        } else { 50 }

        val overallStatus = when {
            overallScore >= 90 -> HealthStatus.EXCELLENT
            overallScore >= 75 -> HealthStatus.GOOD
            overallScore >= 60 -> HealthStatus.FAIR
            overallScore >= 40 -> HealthStatus.POOR
            else -> HealthStatus.CRITICAL
        }

        return SensorHealthSummary(
            overallHealthScore = overallScore,
            overallStatus = overallStatus,
            sensorHealths = sensorHealths,
            criticalIssues = criticalIssues,
            recommendations = recommendations.take(5)
        )
    }

    /**
     * Analyze RPM sensor
     */
    fun analyzeRPM(rpm: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "RPM-Sensor funktioniert normal"
        var score = 100

        // Add to history for rate analysis
        addToHistory(SensorType.RPM, rpm)

        // Range validation
        when {
            rpm < 0 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 50
                diagnosis = "Ungültiger RPM-Wert: negativ"
            }
            rpm > ABSOLUTE_MAX_RPM -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 40
                diagnosis = "RPM unrealistisch hoch"
            }
            rpm > calibration.redlineRpm -> {
                warnings.add("Redline erreicht: ${rpm.toInt()} rpm")
                score -= 15
            }
        }

        // Rate of change check
        val rateCheck = checkRateOfChange(SensorType.RPM, MAX_RPM_CHANGE)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 20
            diagnosis = "RPM-Sprung erkannt"
        }

        // Plausibility check
        if (rpm > 0 && rpm < 300) {
            // Likely idle or cranking
            if (calibration.idleRpm.toDouble() !in (rpm - 200)..(rpm + 200)) {
                warnings.add("RPM ungewöhnlich für Leerlauf")
                score -= 10
            }
        }

        // Check for stuck sensor
        if (isSensorStuck(SensorType.RPM)) {
            issues.add(ValidationIssue.SENSOR_STUCK)
            score -= 30
            diagnosis = "RPM-Sensor möglicherweise klemmend"
        }

        // Drift detection
        val drift = detectDrift(SensorType.RPM)
        if (abs(drift) > 100) {
            issues.add(ValidationIssue.DRIFT_DETECTED)
            score -= 15
        }

        val status = when {
            issues.contains(ValidationIssue.RANGE_OUT_OF_BOUNDS) -> HealthStatus.CRITICAL
            issues.contains(ValidationIssue.RATE_OF_CHANGE_EXCEEDED) -> HealthStatus.POOR
            issues.contains(ValidationIssue.SENSOR_STUCK) -> HealthStatus.POOR
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            score >= 75 -> HealthStatus.GOOD
            else -> HealthStatus.FAIR
        }

        return SensorHealth(
            sensorType = SensorType.RPM,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = rpm,
            unit = "rpm",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = drift,
            stabilityScore = calculateStabilityScore(SensorType.RPM)
        )
    }

    /**
     * Analyze MAF (Mass Air Flow) sensor
     *
     * MAF sensor validation methods:
     * - Range check (0-255 g/s typical)
     * - Rate of change (physical limits)
     * - Correlation with RPM and throttle
     * - Drift from baseline
     * - Idle vs load correlation
     */
    fun analyzeMAF(maf: Double, rpm: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "MAF-Sensor funktioniert normal"
        var score = 100

        addToHistory(SensorType.MAF, maf)

        // Range validation
        when {
            maf < 0 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 50
                diagnosis = "MAF-Sensorfehler: negativer Wert"
            }
            maf > 255.0 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 40
                diagnosis = "MAF-Sensorfehler: Wert zu hoch"
            }
            maf > 150.0 && rpm < 3000 -> {
                warnings.add("MAF ungewöhnlich hoch für Drehzahl")
                score -= 15
            }
        }

        // MAF-RPM correlation check
        if (rpm > 500 && maf > 0) {
            val mafRpmRatio = maf / rpm
            when {
                mafRpmRatio < MAF_RPM_RATIO_MIN -> {
                    issues.add(ValidationIssue.PLAUSIBILITY_FAILED)
                    score -= 25
                    diagnosis = "MAF/RPM-Korrelation gestört"
                }
                mafRpmRatio > MAF_RPM_RATIO_MAX -> {
                    warnings.add("MAF/RPM-Verhältnis erhöht")
                    score -= 10
                }
            }
        }

        // Rate of change check
        val rateCheck = checkRateOfChange(SensorType.MAF, MAX_MAF_CHANGE)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 25
            diagnosis = "MAF-Sprung erkannt: mögliche Luftleck"
        }

        // Drift detection
        val drift = detectDrift(SensorType.MAF)
        if (abs(drift) > DRIFT_THRESHOLD_MAF) {
            issues.add(ValidationIssue.DRIFT_DETECTED)
            score -= 20
            diagnosis = "MAF-Drift erkannt: Sensorverschmutzung möglich"
            warnings.add("MAF-Sensor möglicherweise verschmutzt - Reinigung empfohlen")
        }

        // Check for stuck sensor
        if (isSensorStuck(SensorType.MAF)) {
            issues.add(ValidationIssue.SENSOR_STUCK)
            score -= 35
            diagnosis = "MAF-Sensor möglicherweise defekt"
        }

        // Known issue correlation for A14NET
        if (maf > 0 && maf < 1.5 && rpm > 800) {
            warnings.add("MAF für Leerlauf ungewöhnlich niedrig")
            score -= 10
        }

        val status = when {
            issues.contains(ValidationIssue.RANGE_OUT_OF_BOUNDS) -> HealthStatus.CRITICAL
            issues.contains(ValidationIssue.SENSOR_STUCK) -> HealthStatus.POOR
            issues.contains(ValidationIssue.PLAUSIBILITY_FAILED) -> HealthStatus.POOR
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            score >= 75 -> HealthStatus.GOOD
            else -> HealthStatus.FAIR
        }

        return SensorHealth(
            sensorType = SensorType.MAF,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = maf,
            unit = "g/s",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = drift,
            stabilityScore = calculateStabilityScore(SensorType.MAF)
        )
    }

    /**
     * Analyze MAP/Boost sensor
     *
     * MAP sensor validation:
     * - Absolute pressure range (20-250 kPa typical)
     * - Relative boost calculation
     * - Correlation with engine load
     * - Overboost/Underboost detection
     */
    fun analyzeBoost(
        intakePressure: Double,
        barometricPressure: Double,
        engineLoad: Double
    ): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "Ladedruck-Sensor funktioniert normal"
        var score = 100

        val baroKpa = if (barometricPressure > 0) { barometricPressure } else { 100.0 }
        val relativeBoostKpa = (intakePressure - baroKpa).coerceAtLeast(0.0)
        val relativeBoostBar = relativeBoostKpa / 100.0

        addToHistory(SensorType.BOOST, relativeBoostBar)

        // Range validation
        when {
            intakePressure < 20 -> {
                warnings.add("Saugrohrdruck ungewöhnlich niedrig")
                score -= 10
            }
            intakePressure > 250 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 30
                diagnosis = "Saugrohrdruck unrealistisch hoch"
            }
        }

        when {
            relativeBoostBar > ABSOLUTE_MAX_BOOST -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 40
                diagnosis = "Überladung erkannt: ${"%.2f".format(relativeBoostBar)} bar"
            }
            relativeBoostBar > calibration.overboostBar -> {
                warnings.add("Overboost aktiv: ${"%.2f".format(relativeBoostBar)} bar")
                score -= 15
            }
            relativeBoostBar > calibration.maxBoostBar -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 25
                diagnosis = "Ladedruck über Maximum"
            }
        }

        // Boost-Load correlation
        if (engineLoad > 30 && relativeBoostBar < 0.1) {
            warnings.add("Geringer Ladedruck bei hoher Last")
            score -= 15
        }
        if (relativeBoostBar > 0.3 && engineLoad < 20) {
            warnings.add("Ladedruck ohne Last")
            score -= 10
        }

        // Rate of change check
        val rateCheck = checkRateOfChange(SensorType.BOOST, MAX_BOOST_CHANGE)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 20
            diagnosis = "Ladedruck-Schwankungen erkannt"
        }

        // Drift detection
        val drift = detectDrift(SensorType.BOOST)
        if (abs(drift) > DRIFT_THRESHOLD_BOOST) {
            issues.add(ValidationIssue.DRIFT_DETECTED)
            score -= 15
        }

        // Check for wastegate issues via boost pattern
        val boostPattern = analyzeBoostPattern()
        if (boostPattern == BoostPattern.UNSTABLE) {
            warnings.add("Ladedruckverhalten instabil - Wastegate prüfen")
            score -= 15
        }

        val status = when {
            issues.contains(ValidationIssue.RANGE_OUT_OF_BOUNDS) -> HealthStatus.CRITICAL
            issues.contains(ValidationIssue.RATE_OF_CHANGE_EXCEEDED) -> HealthStatus.POOR
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            score >= 75 -> HealthStatus.GOOD
            else -> HealthStatus.FAIR
        }

        return SensorHealth(
            sensorType = SensorType.BOOST,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = relativeBoostBar,
            unit = "bar",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = drift,
            stabilityScore = calculateStabilityScore(SensorType.BOOST)
        )
    }

    /**
     * Analyze temperature sensors with range checks
     *
     * Temperature sensor validation:
     * - Absolute range (-40°C to sensor-specific max)
     * - Rate of change limits
     * - Correlation between related sensors
     * - Drift from warm-up baseline
     */
    fun analyzeCoolantTemp(temp: Double): SensorHealth {
        return analyzeTemperature(
            sensorType = SensorType.COOLANT_TEMP,
            temp = temp,
            minValid = ABSOLUTE_MIN_TEMP,
            maxValid = calibration.maxCoolantTempC,
            warningThreshold = calibration.maxCoolantTempC * 0.9,
            criticalThreshold = calibration.maxCoolantTempC,
            normalRange = 80.0..105.0,
            driftThreshold = DRIFT_THRESHOLD_COOLANT,
            sensorName = "Kühlmittel"
        )
    }

    fun analyzeIntakeTemp(intakeTemp: Double, coolantTemp: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "Ansaugtemperatur-Sensor funktioniert normal"
        var score = 100

        addToHistory(SensorType.INTAKE_TEMP, intakeTemp)

        // Range validation
        when {
            intakeTemp < ABSOLUTE_MIN_TEMP -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 50
                diagnosis = "Ansaugtemperatursensor fehlerhaft"
            }
            intakeTemp > 80 -> {
                warnings.add("Ansaugtemperatur hoch: ${intakeTemp.toInt()}°C")
                score -= 15
            }
        }

        // Correlation with coolant temperature
        if (coolantTemp > 70 && intakeTemp < coolantTemp - 30) {
            warnings.add("Ansaug-/Kühlmittel-Differenz ungewöhnlich")
            score -= 10
        }

        // Rate of change
        val rateCheck = checkRateOfChange(SensorType.INTAKE_TEMP, MAX_INTAKE_CHANGE)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 15
        }

        // Intercooler efficiency check
        if (intakeTemp > calibration.maxChargeAirTempC * 0.9) {
            warnings.add("Ladeluftkühler-Effizienz reduziert")
            score -= 15
        }

        val status = when {
            issues.contains(ValidationIssue.RANGE_OUT_OF_BOUNDS) -> HealthStatus.CRITICAL
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            score >= 75 -> HealthStatus.GOOD
            else -> HealthStatus.FAIR
        }

        return SensorHealth(
            sensorType = SensorType.INTAKE_TEMP,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = intakeTemp,
            unit = "°C",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = detectDrift(SensorType.INTAKE_TEMP),
            stabilityScore = calculateStabilityScore(SensorType.INTAKE_TEMP)
        )
    }

    fun analyzeOilTemp(oilTemp: Double): SensorHealth {
        return analyzeTemperature(
            sensorType = SensorType.OIL_TEMP,
            temp = oilTemp,
            minValid = ABSOLUTE_MIN_TEMP,
            maxValid = calibration.maxOilTempC,
            warningThreshold = calibration.maxOilTempC * 0.9,
            criticalThreshold = calibration.maxOilTempC,
            normalRange = calibration.optimalOilTempMin..calibration.optimalOilTempMax,
            driftThreshold = DRIFT_THRESHOLD_OIL,
            sensorName = "Öltemperatur"
        )
    }

    fun analyzeEGT(egt: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "Abgastemperatur-Sensor funktioniert normal"
        var score = 100

        addToHistory(SensorType.EGT, egt)

        when {
            egt < 0 -> {
                warnings.add("EGT ungewöhnlich niedrig")
                score -= 10
            }
            egt > calibration.maxEgtC -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 40
                diagnosis = "EGT kritisch: ${egt.toInt()}°C"
            }
            egt > calibration.maxEgtC * 0.9 -> {
                warnings.add("EGT Warnung: ${egt.toInt()}°C")
                score -= 20
            }
        }

        // Rate of change
        val rateCheck = checkRateOfChange(SensorType.EGT, MAX_EGT_CHANGE)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 15
        }

        val status = when {
            issues.contains(ValidationIssue.RANGE_OUT_OF_BOUNDS) -> HealthStatus.CRITICAL
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            score >= 75 -> HealthStatus.GOOD
            else -> HealthStatus.FAIR
        }

        return SensorHealth(
            sensorType = SensorType.EGT,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = egt,
            unit = "°C",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = detectDrift(SensorType.EGT),
            stabilityScore = calculateStabilityScore(SensorType.EGT)
        )
    }

    private fun analyzeTemperature(
        sensorType: SensorType,
        temp: Double,
        minValid: Double,
        maxValid: Double,
        warningThreshold: Double,
        criticalThreshold: Double,
        normalRange: ClosedFloatingPointRange<Double>,
        driftThreshold: Double,
        sensorName: String
    ): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "$sensorName-Sensor funktioniert normal"
        var score = 100

        addToHistory(sensorType, temp)

        // Range validation
        when {
            temp < minValid -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 50
                diagnosis = "$sensorName-Sensorfehler (Unterbereich)"
            }
            temp > maxValid -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 50
                diagnosis = "$sensorName-Sensorfehler (Oberbereich): ${temp.toInt()}°C"
            }
            temp > criticalThreshold -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 40
                diagnosis = "$sensorName kritisch: ${temp.toInt()}°C"
            }
            temp > warningThreshold -> {
                warnings.add("$sensorName Warnung: ${temp.toInt()}°C")
                score -= 15
            }
            temp !in normalRange && temp > 0 -> {
                warnings.add("$sensorName außerhalb Optimalbereich")
                score -= 5
            }
        }

        // Rate of change check
        val maxChange = when (sensorType) {
            SensorType.COOLANT_TEMP -> MAX_COOLANT_CHANGE
            SensorType.OIL_TEMP -> MAX_OIL_CHANGE
            else -> MAX_INTAKE_CHANGE
        }
        val rateCheck = checkRateOfChange(sensorType, maxChange)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 15
            diagnosis = "$sensorName-Änderung zu schnell"
        }

        // Drift detection
        val drift = detectDrift(sensorType)
        if (abs(drift) > driftThreshold) {
            issues.add(ValidationIssue.DRIFT_DETECTED)
            score -= 20
            diagnosis = "$sensorName-Sensor-Drift erkannt"
        }

        val status = when {
            issues.contains(ValidationIssue.RANGE_OUT_OF_BOUNDS) -> HealthStatus.CRITICAL
            issues.contains(ValidationIssue.RATE_OF_CHANGE_EXCEEDED) -> HealthStatus.POOR
            issues.contains(ValidationIssue.DRIFT_DETECTED) -> HealthStatus.POOR
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            score >= 75 -> HealthStatus.GOOD
            else -> HealthStatus.FAIR
        }

        return SensorHealth(
            sensorType = sensorType,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = temp,
            unit = "°C",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = drift,
            stabilityScore = calculateStabilityScore(sensorType)
        )
    }

    /**
     * Analyze battery voltage
     */
    fun analyzeBatteryVoltage(voltage: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "Batteriespannung normal"
        var score = 100

        addToHistory(SensorType.BATTERY_VOLTAGE, voltage)

        when {
            voltage < ABSOLUTE_MIN_VOLTAGE -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 40
                diagnosis = "Batteriespannung kritisch niedrig"
                warnings.add("Batterie laden oder ersetzen")
            }
            voltage < 11.5 -> {
                warnings.add("Batteriespannung niedrig: ${"%.1f".format(voltage)}V")
                score -= 20
            }
            voltage > ABSOLUTE_MAX_VOLTAGE -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 30
                diagnosis = "Batteriespannung zu hoch (Lichtmaschine?)"
            }
            voltage > 15.0 -> {
                warnings.add("Ladespannung erhöht")
                score -= 15
            }
        }

        // Rate of change check
        val rateCheck = checkRateOfChange(SensorType.BATTERY_VOLTAGE, MAX_VOLTAGE_CHANGE)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 20
        }

        val status = when {
            issues.contains(ValidationIssue.RANGE_OUT_OF_BOUNDS) -> HealthStatus.CRITICAL
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            score >= 75 -> HealthStatus.GOOD
            else -> HealthStatus.FAIR
        }

        return SensorHealth(
            sensorType = SensorType.BATTERY_VOLTAGE,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = voltage,
            unit = "V",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = detectDrift(SensorType.BATTERY_VOLTAGE),
            stabilityScore = calculateStabilityScore(SensorType.BATTERY_VOLTAGE)
        )
    }

    /**
     * Analyze speed sensor
     */
    fun analyzeSpeed(speed: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "Geschwindigkeitssensor funktioniert normal"
        var score = 100

        addToHistory(SensorType.SPEED, speed)

        when {
            speed < 0 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 30
            }
            speed > 300 -> {
                warnings.add("Geschwindigkeit unrealistisch")
                score -= 20
            }
        }

        // Rate of change check
        val rateCheck = checkRateOfChange(SensorType.SPEED, MAX_SPEED_CHANGE)
        if (!rateCheck.isValid) {
            issues.add(ValidationIssue.RATE_OF_CHANGE_EXCEEDED)
            score -= 10
        }

        val status = when {
            issues.isNotEmpty() -> HealthStatus.POOR
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            else -> HealthStatus.GOOD
        }

        return SensorHealth(
            sensorType = SensorType.SPEED,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = speed,
            unit = "km/h",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            driftFromBaseline = 0.0,
            stabilityScore = calculateStabilityScore(SensorType.SPEED)
        )
    }

    /**
     * Analyze throttle position
     */
    fun analyzeThrottle(throttle: Double, acceleratorPedal: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "Drosselklappe funktioniert normal"
        var score = 100

        addToHistory(SensorType.THROTTLE, throttle)

        when {
            throttle < 0 || throttle > 100 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 30
                diagnosis = "Drosselklappe ungültig"
            }
        }

        // Correlation with accelerator pedal
        if (acceleratorPedal > 50 && throttle < 20) {
            warnings.add("Drosselklappen-Verzögerung erkannt")
            score -= 15
        }

        val status = if (issues.isNotEmpty()) {
            HealthStatus.POOR
        } else if (warnings.isNotEmpty()) {
            HealthStatus.FAIR
        } else {
            HealthStatus.GOOD
        }

        return SensorHealth(
            sensorType = SensorType.THROTTLE,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = throttle,
            unit = "%",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            stabilityScore = calculateStabilityScore(SensorType.THROTTLE)
        )
    }

    /**
     * Analyze engine load
     */
    fun analyzeEngineLoad(load: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        var diagnosis = "Motorlast normal"
        var score = 100

        addToHistory(SensorType.ENGINE_LOAD, load)

        when {
            load < 0 || load > 100 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 30
                diagnosis = "Motorlast ungültig"
            }
            load > 95 -> {
                score -= 10
            }
        }

        val status = if (issues.isNotEmpty()) {
            HealthStatus.POOR
        } else if (score >= 90) {
            HealthStatus.EXCELLENT
        } else {
            HealthStatus.GOOD
        }

        return SensorHealth(
            sensorType = SensorType.ENGINE_LOAD,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = load,
            unit = "%",
            issues = issues,
            diagnosis = diagnosis,
            stabilityScore = calculateStabilityScore(SensorType.ENGINE_LOAD)
        )
    }

    /**
     * Analyze fuel level
     */
    fun analyzeFuelLevel(level: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "Kraftstoffstand normal"
        var score = 100

        when {
            level < 0 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 30
            }
            level < 10 -> {
                warnings.add("Kraftstoffstand kritisch niedrig")
                score -= 20
                diagnosis = "Bitte tanken!"
            }
            level < 20 -> {
                warnings.add("Kraftstoffstand niedrig")
                score -= 5
            }
        }

        val status = when {
            issues.isNotEmpty() -> HealthStatus.POOR
            warnings.isNotEmpty() -> HealthStatus.FAIR
            else -> HealthStatus.GOOD
        }

        return SensorHealth(
            sensorType = SensorType.FUEL_LEVEL,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = level,
            unit = "%",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis
        )
    }

    /**
     * Analyze O2 sensor
     */
    fun analyzeO2Sensor(voltage: Double): SensorHealth {
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<String>()
        var diagnosis = "O2-Sensor funktioniert normal"
        var score = 100

        addToHistory(SensorType.O2_SENSOR, voltage)

        when {
            voltage < 0 -> {
                issues.add(ValidationIssue.RANGE_OUT_OF_BOUNDS)
                score -= 30
                diagnosis = "O2-Sensorfehler"
            }
            voltage > 1.275 -> {
                warnings.add("O2-Sensor spannend hoch (fettes Gemisch)")
                score -= 10
            }
            voltage < 0.1 -> {
                warnings.add("O2-Sensor spannend niedrig (mageres Gemisch)")
                score -= 10
            }
        }

        // Check for stuck sensor (constant voltage)
        if (isSensorStuck(SensorType.O2_SENSOR, tolerance = 0.05)) {
            issues.add(ValidationIssue.SENSOR_STUCK)
            score -= 30
            diagnosis = "O2-Sensor klemmend oder erschöpft"
        }

        val status = when {
            issues.isNotEmpty() -> HealthStatus.POOR
            warnings.isNotEmpty() -> HealthStatus.FAIR
            score >= 90 -> HealthStatus.EXCELLENT
            else -> HealthStatus.GOOD
        }

        return SensorHealth(
            sensorType = SensorType.O2_SENSOR,
            status = status,
            healthScore = score.coerceIn(0, 100),
            currentValue = voltage,
            unit = "V",
            issues = issues,
            warnings = warnings,
            diagnosis = diagnosis,
            stabilityScore = calculateStabilityScore(SensorType.O2_SENSOR)
        )
    }

    // ========== Helper Methods ==========

    /**
     * Add reading to sensor history
     */
    private fun addToHistory(sensorType: SensorType, value: Double) {
        val history = sensorHistory.getOrPut(sensorType) { mutableListOf() }
        history.add(SensorReading(value))

        // Keep only recent history
        while (history.size > HISTORY_SIZE) {
            history.removeAt(0)
        }

        // Initialize baseline after enough samples
        if (history.size >= 10 && !calibrationBaselines.containsKey(sensorType)) {
            calibrationBaselines[sensorType] = history.takeLast(10).map { it.value }.average()
        }
    }

    /**
     * Check rate of change for a sensor
     */
    data class RateCheckResult(val isValid: Boolean, val changePerSecond: Double)

    private fun checkRateOfChange(sensorType: SensorType, maxChange: Double): RateCheckResult {
        val history = sensorHistory[sensorType] ?: return RateCheckResult(true, 0.0)
        if (history.size < 2) { return RateCheckResult(true, 0.0) }

        val last = history[history.size - 1]
        val prev = history[history.size - 2]

        val timeDiff = (last.timestamp - prev.timestamp) / 1000.0
        if (timeDiff <= 0) { return RateCheckResult(true, 0.0) }

        val change = abs(last.value - prev.value)
        val changePerSecond = change / timeDiff

        return RateCheckResult(
            isValid = changePerSecond <= maxChange,
            changePerSecond = changePerSecond
        )
    }

    /**
     * Detect sensor drift from baseline
     */
    private fun detectDrift(sensorType: SensorType): Double {
        val baseline = calibrationBaselines[sensorType] ?: return 0.0
        val history = sensorHistory[sensorType] ?: return 0.0
        if (history.isEmpty()) { return 0.0 }

        val recentAvg = history.takeLast(10).map { it.value }.average()
        return recentAvg - baseline
    }

    /**
     * Check if sensor is stuck (no variation)
     */
    private fun isSensorStuck(sensorType: SensorType, tolerance: Double = 0.5): Boolean {
        val history = sensorHistory[sensorType] ?: return false
        if (history.size < 5) { return false }

        val recentReadings = history.takeLast(5).map { it.value }
        val avg = recentReadings.average()
        val variance = recentReadings.map { (it - avg) * (it - avg) }.average()
        val stdDev = sqrt(variance)

        return stdDev < tolerance
    }

    /**
     * Calculate stability score based on variance
     */
    private fun calculateStabilityScore(sensorType: SensorType): Int {
        val history = sensorHistory[sensorType] ?: return 100
        if (history.size < 5) { return 100 }

        val recentReadings = history.takeLast(20).map { it.value }
        val avg = recentReadings.average()
        val variance = recentReadings.map { (it - avg) * (it - avg) }.average()
        val stdDev = sqrt(variance)

        // Normalize to 0-100 scale
        val normalizedStdDev = when (sensorType) {
            SensorType.RPM -> (stdDev / 500.0).coerceIn(0.0, 1.0)
            SensorType.SPEED -> (stdDev / 30.0).coerceIn(0.0, 1.0)
            SensorType.MAF -> (stdDev / 20.0).coerceIn(0.0, 1.0)
            SensorType.BOOST -> (stdDev / 0.2).coerceIn(0.0, 1.0)
            SensorType.COOLANT_TEMP -> (stdDev / 5.0).coerceIn(0.0, 1.0)
            SensorType.BATTERY_VOLTAGE -> (stdDev / 0.5).coerceIn(0.0, 1.0)
            else -> (stdDev / 10.0).coerceIn(0.0, 1.0)
        }

        return ((1.0 - normalizedStdDev) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Boost pattern analysis for wastegate health
     */
    enum class BoostPattern {
        STABLE, UNSTABLE, STICKY
    }

    private fun analyzeBoostPattern(): BoostPattern {
        val history = sensorHistory[SensorType.BOOST] ?: return BoostPattern.STABLE
        if (history.size < 10) { return BoostPattern.STABLE }

        val recentReadings = history.takeLast(10).map { it.value }
        val variance = recentReadings.map { it * it }.average() -
            (recentReadings.average() * recentReadings.average())

        return when {
            variance > 0.05 -> BoostPattern.UNSTABLE
            variance < 0.001 -> BoostPattern.STICKY
            else -> BoostPattern.STABLE
        }
    }

    /**
     * Cross-validate multiple sensors for plausibility
     */
    fun crossValidatePlausibility(data: OBDData): List<PlausibilityResult> {
        val results = mutableListOf<PlausibilityResult>()

        // MAF vs RPM plausibility
        if (data.rpm > 0 && data.mafRate > 0) {
            val ratio = data.mafRate / data.rpm
            val isPlausible = ratio in MAF_RPM_RATIO_MIN..MAF_RPM_RATIO_MAX
            results.add(PlausibilityResult(
                isPlausible = isPlausible,
                correlation = (ratio / MAF_RPM_RATIO_MAX).coerceIn(0.0, 1.0),
                expectedRange = (MAF_RPM_RATIO_MIN * data.rpm)..(MAF_RPM_RATIO_MAX * data.rpm),
                actualValue = data.mafRate,
                diagnosis = if (isPlausible) { "MAF/RPM Korrelation OK" } else { "MAF/RPM Korrelation gestört" }
            ))
        }

        // Coolant vs Intake temperature correlation
        if (data.coolantTemp > 0 && data.intakeTemp > -40) {
            val diff = data.coolantTemp - data.intakeTemp
            val isPlausible = diff in -30.0..50.0
            results.add(PlausibilityResult(
                isPlausible = isPlausible,
                correlation = (1.0 - abs(diff - 20.0) / 50.0).coerceIn(0.0, 1.0),
                expectedRange = (data.coolantTemp - 30.0)..(data.coolantTemp + 30.0),
                actualValue = data.intakeTemp,
                diagnosis = if (isPlausible) { "Temperatur-Korrelation OK" } else { "Temperatur-Korrelation gestört" }
            ))
        }

        // Boost vs Load plausibility
        val baroKpa = if (data.barometricPressure > 0) { data.barometricPressure } else { 100.0 }
        val relativeBoost = ((data.intakePressure - baroKpa) / 100.0).coerceAtLeast(0.0)
        if (data.engineLoad > 0) {
            val expectedBoost = data.engineLoad * BOOST_LOAD_RATIO * 1.2 // 120% for turbo
            val isPlausible = relativeBoost <= expectedBoost + 0.5
            results.add(PlausibilityResult(
                isPlausible = isPlausible,
                correlation = (relativeBoost / (expectedBoost + 0.5)).coerceIn(0.0, 1.0),
                expectedRange = 0.0..(expectedBoost + 0.5),
                actualValue = relativeBoost,
                diagnosis = if (isPlausible) { "Ladedruck/Last Korrelation OK" } else { "Ladedruck/Last Inkonsistenz" }
            ))
        }

        return results
    }

    /**
     * Reset all sensor history and baselines
     */
    fun resetCalibration() {
        sensorHistory.clear()
        calibrationBaselines.clear()
        sensorDTCs.clear()
    }

    /**
     * Get all detected sensor DTCs
     */
    fun getSensorDTCs(): List<SensorDTC> = sensorDTCs.toList()

    /**
     * Generate diagnostic recommendation based on health summary
     */
    fun generateRecommendation(summary: SensorHealthSummary): String {
        val critical = summary.criticalIssues
        if (critical.isEmpty()) {
            return "Alle Sensoren funktionieren im normalen Bereich. Keine Wartung erforderlich."
        }

        return critical.firstOrNull()?.let { issue ->
            when {
                issue.contains("MAF") -> "MAF-Sensor möglicherweise verschmutzt. Reinigung mit speziellem MAF-Reiniger empfohlen."
                issue.contains("Ladedruck") || issue.contains("Boost") -> "Wastegate-Funktion und Ladedrucksensor prüfen."
                issue.contains("Kühlmittel") -> "Kühlmittelstand und Kühlmitteltemperatursensor prüfen."
                issue.contains("Öltemperatur") -> "Ölstand und Öltemperatursensor prüfen."
                issue.contains("Batterie") -> "Batterie- und Lichtmaschinenspannung prüfen."
                issue.contains("O2") -> "O2-Sensor auf Funktion und Alter prüfen."
                else -> "Sensorproblem erkannt. Nähere Diagnose erforderlich."
            }
        } ?: "Sensorproblem erkannt. Nähere Diagnose erforderlich."
    }
}
