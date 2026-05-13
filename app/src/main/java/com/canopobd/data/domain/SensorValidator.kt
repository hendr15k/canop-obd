package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

class SensorValidator(private val calibration: AstraJ14TurboCalibration) {

    private val mafHistory = mutableListOf<Double>()
    private val rpmHistory = mutableListOf<Double>()

    companion object {
        private const val HISTORY_SIZE = 10
        private const val MAX_MAF_CHANGE = 20.0  // g/s per sample
    }

    fun addMaf(maf: Double) {
        mafHistory.add(maf)
        if (mafHistory.size > HISTORY_SIZE) mafHistory.removeAt(0)
    }

    fun addRpm(rpm: Double) {
        rpmHistory.add(rpm)
        if (rpmHistory.size > HISTORY_SIZE) rpmHistory.removeAt(0)
    }

    fun validateMaf(maf: Double?, previousMaf: Double? = null): ValidationResult {
        if (maf == null) return ValidationResult.Unavailable

        // Range Check
        if (maf < 0 || maf > 100) return ValidationResult.Invalid("MAF außerhalb Bereich")

        // Rate of Change Check
        if (previousMaf != null) {
            val change = abs(maf - previousMaf)
            if (change > MAX_MAF_CHANGE) {
                return ValidationResult.Suspicious("MAF-Sprung: ${"%.1f".format(change)} g/s")
            }
        }

        // Normal Range Check
        if (!calibration.isMafNormal(maf)) {
            return ValidationResult.Suspicious("MAF ${"%.1f".format(maf)} g/s außerhalb Normalbereich")
        }

        return ValidationResult.Valid
    }

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

        return ValidationResult.Valid
    }
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Suspicious(val message: String) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
    data object Unavailable : ValidationResult()
}
