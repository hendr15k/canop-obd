package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

class SensorValidator(private val calibration: AstraJ14TurboCalibration?) {

    private val mafHistory = mutableListOf<Double>()
    private val rpmHistory = mutableListOf<Double>()
    private val iatHistory = mutableListOf<Double>()
    private val coolantHistory = mutableListOf<Double>()
    private val throttleHistory = mutableListOf<Double>()

    companion object {
        private const val HISTORY_SIZE = 10
        private const val MAX_MAF_CHANGE = 20.0  // g/s per sample
        private const val MAX_RPM_CHANGE = 800.0  // RPM per sample
        private const val MAX_IAT_CHANGE = 15.0    // °C per sample
        private const val MAX_COOLANT_CHANGE = 10.0 // °C per sample
        private const val SENSOR_HISTORY_SIZE = 50
    }

    fun addMaf(maf: Double) {
        mafHistory.add(maf)
        if (mafHistory.size > SENSOR_HISTORY_SIZE) mafHistory.removeAt(0)
    }

    fun addRpm(rpm: Double) {
        rpmHistory.add(rpm)
        if (rpmHistory.size > SENSOR_HISTORY_SIZE) rpmHistory.removeAt(0)
    }

    fun addIat(temp: Double) {
        iatHistory.add(temp)
        if (iatHistory.size > SENSOR_HISTORY_SIZE) iatHistory.removeAt(0)
    }

    fun addCoolant(temp: Double) {
        coolantHistory.add(temp)
        if (coolantHistory.size > SENSOR_HISTORY_SIZE) coolantHistory.removeAt(0)
    }

    fun validateMaf(maf: Double?, previousMaf: Double? = null): ValidationResult {
        if (maf == null) return ValidationResult.Unavailable

        // Range Check
        if (maf < 0 || maf > 150) return ValidationResult.Invalid("MAF außerhalb Bereich")

        // Rate of Change Check
        if (previousMaf != null) {
            val change = abs(maf - previousMaf)
            if (change > MAX_MAF_CHANGE) {
                return ValidationResult.Suspicious("MAF-Sprung: ${"%.1f".format(change)} g/s")
            }
        }

        // History-based anomaly detection
        if (mafHistory.size >= 5) {
            val recentMaf = mafHistory.takeLast(5)
            val avg = recentMaf.average()
            val variance = recentMaf.map { (it - avg) * (it - avg) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            if (stdDev > 0 && abs(maf - avg) > 3 * stdDev) {
                return ValidationResult.Suspicious("MAF-Abweichung von Trend")
            }
        }

        // Normal Range Check - safely handle null calibration
        calibration?.let {
            if (!it.isMafNormal(maf)) {
                return ValidationResult.Suspicious("MAF ${"%.1f".format(maf)} g/s außerhalb Normalbereich")
            }
        }

        return ValidationResult.Valid
    }

    @Suppress("UNUSED_PARAMETER")
    fun validateBoost(boostBar: Double?, barometric: Double?): ValidationResult {
        if (boostBar == null) return ValidationResult.Unavailable

        // Overboost Check (>1.3 bar ist kritisch beim A14NET)
        if (boostBar > 1.35) return ValidationResult.Invalid("Kritische Überladung!")
        if (boostBar > 1.2) return ValidationResult.Suspicious("Overboost aktiv")

        return ValidationResult.Valid
    }

    fun validateCoolant(temp: Double?): ValidationResult {
        if (temp == null) return ValidationResult.Unavailable

        if (temp < -40) return ValidationResult.Invalid("Sensorfehler (zu kalt)")
        if (temp > 105) return ValidationResult.Invalid("Motor überhitzt!")
        if (temp > 100) return ValidationResult.Suspicious("Kühlmittel warnung")

        return ValidationResult.Valid
    }

    fun validateRpm(rpm: Double?): ValidationResult {
        if (rpm == null) return ValidationResult.Unavailable

        if (rpm < 0 || rpm > 7000) return ValidationResult.Invalid("RPM ungültig")
        if (rpm > 6500) return ValidationResult.Suspicious("Redline erreicht!")

        // Rate of Change Check
        if (rpmHistory.size >= 2) {
            val recent = rpmHistory.takeLast(2)
            val change = abs(rpm - recent.first())
            if (change > MAX_RPM_CHANGE && rpm > 1000) {
                return ValidationResult.Suspicious("RPM-Sprung: ${change.toInt()} RPM")
            }
        }

        // Stuck RPM detection - same value for extended period at non-idle
        if (rpmHistory.size >= 10) {
            val recent = rpmHistory.takeLast(10)
            val allSame = recent.all { it == recent.first() }
            val variance = recent.map { it - recent.average() }.map { it * it }.average()
            val stdDev = kotlin.math.sqrt(variance)
            if (stdDev < 5.0 && rpm > 1500 && allSame) {
                return ValidationResult.Suspicious("RPM-Signal blockiert")
            }
        }

        return ValidationResult.Valid
    }

    fun validateIat(temp: Double?,maf: Double? = null): ValidationResult {
        if (temp == null) return ValidationResult.Unavailable

        if (temp < -50 || temp > 80) return ValidationResult.Invalid("IAT Sensorfehler")

        // Rate of Change Check
        if (iatHistory.size >= 2) {
            val change = abs(temp - iatHistory.last())
            if (change > MAX_IAT_CHANGE) {
                return ValidationResult.Suspicious("IAT-Sprung: ${"%.1f".format(change)}°C")
            }
        }

        // Correlation with MAF - low IAT with high MAF should correlate
        maf?.let {
            if (it > 30.0 && temp > 40.0) {
                return ValidationResult.Suspicious("IAT/MAF-Korrelation gestört")
            }
        }

        return ValidationResult.Valid
    }

    fun validateThrottle(position: Double?): ValidationResult {
        if (position == null) return ValidationResult.Unavailable

        if (position < 0 || position > 100) return ValidationResult.Invalid("Drosselklappe außerhalb Bereich")

        throttleHistory.add(position)
        if (throttleHistory.size > SENSOR_HISTORY_SIZE) throttleHistory.removeAt(0)

        // Stuck throttle detection
        if (throttleHistory.size >= 10) {
            val variance = throttleHistory.map { it - throttleHistory.average() }.map { it * it }.average()
            val stdDev = kotlin.math.sqrt(variance)
            if (stdDev < 0.5 && position > 5.0) {
                return ValidationResult.Suspicious("Drosselklappe klemmt")
            }
        }

        return ValidationResult.Valid
    }

    fun validateFuelTrim(stft: Double?, ltft: Double?): ValidationResult {
        if (stft == null && ltft == null) return ValidationResult.Unavailable

        val stftVal = stft ?: 0.0
        val ltftVal = ltft ?: 0.0
        val total = abs(stftVal) + abs(ltftVal)

        if (total > 40) return ValidationResult.Invalid("Kraftstoff-Trim kritisch")
        if (total > 25) return ValidationResult.Suspicious("Kraftstoff-Trim erhöht")

        return ValidationResult.Valid
    }

    fun validateTurboSpeed(rpm: Double?): ValidationResult {
        if (rpm == null) return ValidationResult.Unavailable

        if (rpm < 0 || rpm > 250000) return ValidationResult.Invalid("Turbo-RPM ungültig")
        if (rpm > 200000) return ValidationResult.Suspicious("Turbo Überdrehzahl")

        return ValidationResult.Valid
    }

    fun getSensorHealthSummary(): Map<String, ValidationResult> {
        return mapOf(
            "MAF" to (mafHistory.lastOrNull()?.let { validateMaf(it) } ?: ValidationResult.Unavailable),
            "RPM" to (rpmHistory.lastOrNull()?.let { validateRpm(it) } ?: ValidationResult.Unavailable),
            "IAT" to (iatHistory.lastOrNull()?.let { validateIat(it) } ?: ValidationResult.Unavailable),
            "Coolant" to (coolantHistory.lastOrNull()?.let { validateCoolant(it) } ?: ValidationResult.Unavailable)
        )
    }

    fun reset() {
        mafHistory.clear()
        rpmHistory.clear()
        iatHistory.clear()
        coolantHistory.clear()
        throttleHistory.clear()
    }
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Suspicious(val message: String) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
    data object Unavailable : ValidationResult()
}
