package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Fahrstil-Analyse fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Analysiert das Fahrverhalten basierend auf OBD-Daten und berechnet
 * differenzierte Bewertungen fuer verschiedene Aspekte der Fahrweise.
 *
 * Bewertete Aspekte:
 * - RPM-Verteilung (Stadt vs Autobahn)
 * - Gaspedal-Glattheit (Schwarmerei)
 * - Bremsverhalten
 * - Overboost-Haeufigkeit
 * - Schaltpunkte (fuer Schaltgetriebe)
 *
 * Das A14NET-Optimalfenster:
 * - Eco-Bereich: 1500-3000 rpm (max. Drehmoment bei ~2000 rpm)
 * - Power-Band: 4500-5500 rpm
 * - Redline: 6500 rpm
 * - Ueberholreserve bei 3000-4000 rpm
 */
class DriveStyleAnalyzer(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Fahrstil-Kategorien
     */
    enum class DriveStyle(val label: String, val description: String) {
        ECO("Umweltfreundlich", "Spritsparende Fahrweise mit optimalem Verbrauch"),
        BALANCED("Ausgewogen", "Gleichgewicht zwischen Komfort und Effizienz"),
        SPORTLICH("Sportlich", "Leistungsorientierte Fahrweise"),
        AGGRESSIV("Aggressiv", "Sehr beanspruchende Fahrweise - erhoehter Verschleiss")
    }

    /**
     * Eingabedaten fuer die Fahrstil-Analyse
     */
    data class DriveStyleInput(
        val rpmHistory: List<Double>,
        val throttleHistory: List<Double>,
        val speedHistory: List<Double>,
        val brakeHistory: List<Double> = emptyList(),
        val boostHistory: List<Double> = emptyList(),
        val boostDutyHistory: List<Double> = emptyList(),
        val engineLoadHistory: List<Double> = emptyList(),
        val sessionDurationSec: Long = 0L,
        val totalKm: Double = 0.0,
        val isManualTransmission: Boolean = true
    )

    /**
     * Fahrstil-Ergebnis
     */
    data class DriveStyleAnalysis(
        val driveStyle: DriveStyle,
        val ecoScore: Int,
        val sportScore: Int,
        val wearScore: Int,
        val rpmDistribution: RPMDistribution,
        val throttleSmoothness: Int,
        val brakingPattern: Int,
        val overboostFrequency: Double,
        val shiftQuality: Int,
        val feedback: String,
        val detailedFeedback: List<String>
    )

    /**
     * RPM-Verteilungsanalyse
     */
    data class RPMDistribution(
        val percentBelowOptimal: Double,   // % Zeit unter optimal-RPM
        val percentOptimal: Double,        // % Zeit im optimalen Bereich
        val percentPowerBand: Double,      // % Zeit im Power-Band
        val percentRedline: Double,        // % Zeit im roten Bereich
        val avgRPM: Double,
        val maxRPM: Double
    )

    companion object {
        // RPM-Bereiche fuer A14NET
        private const val RPM_IDLE_MAX = 900.0
        private const val RPM_OPTIMAL_MIN = 1500.0
        private const val RPM_OPTIMAL_MAX = 3000.0
        private const val RPM_POWER_MIN = 4500.0
        private const val RPM_POWER_MAX = 5500.0
        private const val RPM_REDLINE = 6500.0
        private const val RPM_AGGRESSIVE = 5000.0

        // Throttle-Schwellenwerte
        private const val THROTTLE_ECO_MAX = 30.0
        private const val THROTTLE_NORMAL_MAX = 60.0
        private const val THROTTLE_AGGRESSIVE = 80.0

        // Speed-Kategorisierung
        private const val SPEED_CITY_MAX = 50.0
        private const val SPEED_SUBURBAN_MAX = 80.0
        private const val SPEED_HIGHWAY_MIN = 80.0

        // Overboost-Schwellen
        private const val OVERBOOST_THRESHOLD_BAR = 1.0
    }

    /**
     * Fuehrt eine vollstaendige Fahrstil-Analyse durch
     */
    fun analyze(input: DriveStyleInput): DriveStyleAnalysis {
        if (input.rpmHistory.isEmpty()) {
            return createEmptyAnalysis()
        }

        // 1. RPM-Verteilung
        val rpmDistribution = analyzeRPMdistribution(input.rpmHistory)

        // 2. Eco-Score berechnen
        val ecoScore = calculateEcoScore(input, rpmDistribution)

        // 3. Sport-Score berechnen
        val sportScore = calculateSportScore(input, rpmDistribution)

        // 4. Verschleiss-Score berechnen (hoeher = mehr Verschleiss)
        val wearScore = calculateWearScore(input, rpmDistribution)

        // 5. Gaspedal-Glattheit
        val throttleSmoothness = calculateThrottleSmoothness(input.throttleHistory)

        // 6. Bremsverhalten
        val brakingPattern = analyzeBrakingPattern(input.brakeHistory, input.speedHistory)

        // 7. Overboost-Frequenz
        val overboostFreq = calculateOverboostFrequency(input.boostHistory)

        // 8. Schaltqualitaet (bei Schaltgetriebe)
        val shiftQuality = if (input.isManualTransmission) {
            analyzeShiftQuality(input.rpmHistory, input.speedHistory)
        } else {
            80 // Automatikgetriebe: Standardbewertung
        }

        // Fahrstil bestimmen
        val driveStyle = determineDriveStyle(ecoScore, sportScore, wearScore)

        // Feedback generieren
        val (feedback, detailedFeedback) = generateFeedback(
            driveStyle, rpmDistribution, throttleSmoothness, overboostFreq, wearScore
        )

        return DriveStyleAnalysis(
            driveStyle = driveStyle,
            ecoScore = ecoScore,
            sportScore = sportScore,
            wearScore = wearScore,
            rpmDistribution = rpmDistribution,
            throttleSmoothness = throttleSmoothness,
            brakingPattern = brakingPattern,
            overboostFrequency = overboostFreq,
            shiftQuality = shiftQuality,
            feedback = feedback,
            detailedFeedback = detailedFeedback
        )
    }

    /**
     * Analysiert RPM-Verteilung
     */
    private fun analyzeRPMdistribution(rpmHistory: List<Double>): RPMDistribution {
        val totalSamples = rpmHistory.size.toDouble()
        if (totalSamples < 0.5) return RPMDistribution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        val belowOptimal = rpmHistory.count { it < RPM_OPTIMAL_MIN }.toDouble() / totalSamples * 100.0
        val optimal = rpmHistory.count { it in RPM_OPTIMAL_MIN..RPM_OPTIMAL_MAX }.toDouble() / totalSamples * 100.0
        val powerBand = rpmHistory.count { it in RPM_POWER_MIN..RPM_POWER_MAX }.toDouble() / totalSamples * 100.0
        val redline = rpmHistory.count { it > RPM_REDLINE }.toDouble() / totalSamples * 100.0

        return RPMDistribution(
            percentBelowOptimal = belowOptimal,
            percentOptimal = optimal,
            percentPowerBand = powerBand,
            percentRedline = redline,
            avgRPM = rpmHistory.average(),
            maxRPM = rpmHistory.max()
        )
    }

    /**
     * Berechnet Eco-Score (hoeher = umweltfreundlicher)
     */
    private fun calculateEcoScore(input: DriveStyleInput, rpmDist: RPMDistribution): Int {
        var score = 50

        // RPM-Bewertung: optimaler Bereich ist am besten
        score += (rpmDist.percentOptimal * 0.3).toInt().coerceIn(-10, 30)
        score -= (rpmDist.percentRedline * 2.0).toInt().coerceIn(0, 25)
        score -= (rpmDist.percentPowerBand * 0.5).toInt().coerceIn(0, 15)

        // Throttle-Bewertung: sanftes Fahren ist besser
        val avgThrottle = if (input.throttleHistory.isNotEmpty()) input.throttleHistory.average() else 0.0
        score += when {
            avgThrottle < THROTTLE_ECO_MAX -> 15
            avgThrottle < THROTTLE_NORMAL_MAX -> 5
            else -> -10
        }

        // Boost-Nutzung: sparsam ist besser
        val avgBoost = if (input.boostHistory.isNotEmpty()) input.boostHistory.average() else 0.0
        score -= if (avgBoost > 0.5) 10 else 0

        return score.coerceIn(0, 100)
    }

    /**
     * Berechnet Sport-Score (hoeher = sportlicher)
     */
    private fun calculateSportScore(input: DriveStyleInput, rpmDist: RPMDistribution): Int {
        var score = 30

        // Hohe RPM = sportlich
        score += (rpmDist.percentPowerBand * 1.5).toInt().coerceIn(0, 30)
        score += (rpmDist.percentRedline * 3.0).toInt().coerceIn(0, 20)

        // Aggressives Throttle
        val avgThrottle = if (input.throttleHistory.isNotEmpty()) input.throttleHistory.average() else 0.0
        score += when {
            avgThrottle > THROTTLE_AGGRESSIVE -> 20
            avgThrottle > THROTTLE_NORMAL_MAX -> 10
            else -> 0
        }

        // Boost-Nutzung
        val avgBoost = if (input.boostHistory.isNotEmpty()) input.boostHistory.average() else 0.0
        score += (avgBoost * 15).toInt().coerceIn(0, 20)

        return score.coerceIn(0, 100)
    }

    /**
     * Berechnet Verschleiss-Score (hoeher = mehr Verschleiss)
     */
    private fun calculateWearScore(input: DriveStyleInput, rpmDist: RPMDistribution): Int {
        var score = 20

        // Hohe RPM erhoehen Verschleiss
        score += (rpmDist.percentRedline * 4.0).toInt().coerceIn(0, 30)
        score += (rpmDist.percentPowerBand * 0.8).toInt().coerceIn(0, 15)

        // Kalte Motor-Phase (erste 5 Minuten) mit hoher Last
        // (wuerde hier mit additional context ergaenzt werden)

        // Aggressives Fahren
        val avgThrottle = if (input.throttleHistory.isNotEmpty()) input.throttleHistory.average() else 0.0
        score += when {
            avgThrottle > THROTTLE_AGGRESSIVE -> 15
            avgThrottle > THROTTLE_NORMAL_MAX -> 5
            else -> 0
        }

        // UeberBOOST-Haeufigkeit
        val avgBoost = if (input.boostHistory.isNotEmpty()) input.boostHistory.average() else 0.0
        score += if (avgBoost > OVERBOOST_THRESHOLD_BAR) 10 else 0

        return score.coerceIn(0, 100)
    }

    /**
     * Berechnet Gaspedal-Glattheit (0-100, hoeher = glatter)
     */
    private fun calculateThrottleSmoothness(throttleHistory: List<Double>): Int {
        if (throttleHistory.size < 3) return 80

        // Berechne standardabweichung der Throttle-Aenderungen
        val changes = throttleHistory.zipWithNext().map { abs(it.second - it.first) }
        val avgChange = changes.average()
        val variance = changes.map { (it - avgChange) * (it - avgChange) }.average()
        val stdDev = sqrt(variance)

        return when {
            stdDev < 3.0 -> 95   // Sehr glatt
            stdDev < 8.0 -> 80   // Glatte Fahrweise
            stdDev < 15.0 -> 60  // Normal
            stdDev < 25.0 -> 40  // Rau
            else -> 20           // Sehr aggressiv
        }
    }

    /**
     * Analysiert Bremsverhalten
     */
    private fun analyzeBrakingPattern(brakeHistory: List<Double>, speedHistory: List<Double>): Int {
        if (brakeHistory.isEmpty() || speedHistory.size < 2) return 70

        val harshBrakes = brakeHistory.count { it > 70.0 }
        val totalBrakes = brakeHistory.size

        val harshRatio = if (totalBrakes > 0) harshBrakes.toDouble() / totalBrakes else 0.0

        return when {
            harshRatio < 0.05 -> 95  // Sanftes Bremsen
            harshRatio < 0.15 -> 80  // Normal
            harshRatio < 0.30 -> 55  // Haertere Bremsen
            else -> 30               // Sehr aggressiv
        }
    }

    /**
     * Berechnet Overboost-Frequenz
     */
    private fun calculateOverboostFrequency(boostHistory: List<Double>): Double {
        if (boostHistory.isEmpty()) return 0.0

        val overboostCount = boostHistory.count { it > OVERBOOST_THRESHOLD_BAR }
        return (overboostCount.toDouble() / boostHistory.size) * 100.0
    }

    /**
     * Analysiert Schaltqualitaet (fuer Schaltgetriebe)
     */
    private fun analyzeShiftQuality(rpmHistory: List<Double>, speedHistory: List<Double>): Int {
        if (rpmHistory.size < 10 || speedHistory.size < 10) return 70

        // Erkenne Schaltpunkte durch RPM-Einbrueche bei steigender Geschwindigkeit
        var shiftCount = 0
        var harshShifts = 0

        for (i in 2 until rpmHistory.size) {
            val rpmDrop = rpmHistory[i - 1] - rpmHistory[i]
            val speedIncrease = if (i < speedHistory.size) speedHistory[i] - speedHistory[i - 1] else 0.0

            // RPM-Einbruch bei steigender Geschwindigkeit = Schaltung
            if (rpmDrop > 500 && speedIncrease > 0) {
                shiftCount++
                // Harte Schaltung: RPM-Einbruch > 1500 oder RPM > 5000 vor Schaltung
                if (rpmDrop > 1500 || rpmHistory[i - 1] > RPM_AGGRESSIVE) {
                    harshShifts++
                }
            }
        }

        if (shiftCount == 0) return 70

        val harshShiftRatio = harshShifts.toDouble() / shiftCount
        return when {
            harshShiftRatio < 0.1 -> 95  // Sanfte Schaltungen
            harshShiftRatio < 0.25 -> 75 // Normal
            harshShiftRatio < 0.5 -> 50  // Haerte Schaltungen
            else -> 30                    // Sehr aggressiv
        }
    }

    /**
     * Bestimmt den Fahrstil
     */
    private fun determineDriveStyle(ecoScore: Int, sportScore: Int, wearScore: Int): DriveStyle {
        return when {
            wearScore > 70 -> DriveStyle.AGGRESSIV
            ecoScore >= 75 && sportScore < 40 -> DriveStyle.ECO
            sportScore >= 60 && ecoScore < 50 -> DriveStyle.SPORTLICH
            else -> DriveStyle.BALANCED
        }
    }

    /**
     * Generiert Feedback auf Deutsch
     */
    private fun generateFeedback(
        style: DriveStyle,
        rpmDist: RPMDistribution,
        throttleSmoothness: Int,
        overboostFreq: Double,
        wearScore: Int
    ): Pair<String, List<String>> {
        val detailedFeedback = mutableListOf<String>()

        val mainFeedback = when (style) {
            DriveStyle.ECO -> {
                "Ihre Fahrweise ist umweltfreundlich und sparsam. " +
                        "Der Motor wird wenig beansprucht."
            }
            DriveStyle.BALANCED -> {
                "Ausgewogene Fahrweise mit gutem Gleichgewicht " +
                        "zwischen Leistung und Verbrauch."
            }
            DriveStyle.SPORTLICH -> {
                "Sportliche Fahrweise mit hoeherer Motorbelastung. " +
                        "Regelmassige Wartung ist wichtig."
            }
            DriveStyle.AGGRESSIV -> {
                "Aggressives Fahrverhalten fuehrt zu erhoehtem Verschleiss. " +
                        "Wartungsintervalle sollten eingehalten werden."
            }
        }

        // Detailliertes Feedback
        if (rpmDist.percentOptimal > 60) {
            detailedFeedback.add("Gute RPM-Verteilung: ${rpmDist.percentOptimal.toInt()}% im optimalen Bereich")
        } else if (rpmDist.percentBelowOptimal > 50) {
            detailedFeedback.add("Haeufig zu niedrige Drehzahl - Motor sollte hoeher belastet werden")
        }

        if (rpmDist.percentRedline > 5) {
            detailedFeedback.add("Warnung: ${rpmDist.percentRedline.toInt()}% im roten Bereich - Verschleiss!")
        }

        if (throttleSmoothness < 50) {
            detailedFeedback.add("Gaspedal-Bedienung ist rau - sanfteres Fahren empfohlen")
        }

        if (overboostFreq > 15.0) {
            detailedFeedback.add("Overboost-Haeufigkeit: ${overboostFreq.toInt()}% - Turbo wird stark beansprucht")
        }

        if (wearScore > 60) {
            detailedFeedback.add("Erhoehter Verschleiss erkannt - Oelwechselintervall verkuerzen")
        }

        return mainFeedback to detailedFeedback
    }

    /**
     * Erstellt eine leere Analyse wenn keine Daten vorhanden sind
     */
    private fun createEmptyAnalysis(): DriveStyleAnalysis {
        return DriveStyleAnalysis(
            driveStyle = DriveStyle.BALANCED,
            ecoScore = 50,
            sportScore = 50,
            wearScore = 50,
            rpmDistribution = RPMDistribution(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            throttleSmoothness = 50,
            brakingPattern = 50,
            overboostFrequency = 0.0,
            shiftQuality = 50,
            feedback = "Nicht genuegend Daten fuer eine Fahrstil-Analyse.",
            detailedFeedback = listOf("Bitte laengere Fahrt aufzeichnen.")
        )
    }
}
