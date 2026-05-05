package com.canopobd.data.domain

/**
 * Analysiert Kraftstoff-Trims für den A14NET
 * STFT = Short Term Fuel Trim (Kurzzeit-Korrekturen)
 * LTFT = Long Term Fuel Trim (Langzeit-Anpassungen)
 */
class FuelTrimAnalyzer {
    
    data class FuelTrimStatus(
        val stft: Double,
        val ltft: Double,
        val totalTrim: Double,
        val isLean: Boolean,
        val isRich: Boolean,
        val healthScore: Int,
        val diagnosis: String
    )
    
    companion object {
        private const val NORMAL_TRIM = 5.0      // Normaler Trim-Bereich
        private const val WARNING_TRIM = 10.0     // Warnung
        private const val PROBLEM_TRIM = 15.0    // Problem erkannt
    }
    
    fun analyze(stft: Double, ltft: Double): FuelTrimStatus {
        val totalTrim = stft + ltft
        val absTotal = kotlin.math.abs(totalTrim)
        
        val (healthScore, diagnosis) = when {
            absTotal > PROBLEM_TRIM -> {
                when {
                    totalTrim > 0 -> 20 to "System zu mager - Leck oder Sensorproblem"
                    else -> 20 to "System zu fett - Einspritzung oder Kraftstoffdruck"
                }
            }
            absTotal > WARNING_TRIM -> {
                when {
                    totalTrim > 0 -> 50 to "Leichte Trimabweichung (mager)"
                    else -> 50 to "Leichte Trimabweichung (fett)"
                }
            }
            absTotal > NORMAL_TRIM -> {
                70 to "Leicht erhöhter Trim - Wartung empfohlen"
            }
            else -> {
                100 to "Kraftstoffsystem optimal"
            }
        }
        
        return FuelTrimStatus(
            stft = stft,
            ltft = ltft,
            totalTrim = totalTrim,
            isLean = totalTrim > WARNING_TRIM,
            isRich = totalTrim < -WARNING_TRIM,
            healthScore = healthScore,
            diagnosis = diagnosis
        )
    }
    
    fun getRecommendedAction(status: FuelTrimStatus): String {
        return when {
            status.healthScore <= 20 -> 
                "Dringend: MAF-Sensor prüfen,可能在漏气或喷油器问题"
            status.healthScore <= 50 -> 
                " Empfohlen: Luftfilter, MAF-Sensor und O2-Sensoren prüfen"
            status.healthScore <= 70 -> 
                "Beobachten: Trimwerte im Auge behalten"
            else -> 
                " Keine Maßnahmen erforderlich"
        }
    }
}
