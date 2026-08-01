package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * M32-Getriebe-Monitor fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Der Astra J 1.4 Turbo wurde hauptsaechlich mit dem Getriebe M32
 * (6-Gang-Schaltgetriebe) von General Motors ausgestattet.
 *
 * Technische Daten des M32:
 * - Typ: 6-Gang-Schaltgetriebe, Quereinbau
 * - Getriebeöl: Dexron VI ATF oder spezifisches GM-Getriebeöl
 * - Oelwechselintervall: 60.000 km (trotz "Lifetime"-Fuellung)
 * - Bekanntes Problem: Laagerschaeden (v.a. 1. Gang und Rueckwaertsgang)
 *
 * Typische Probleme des M32:
 * - 1. Gang: Einrueckschwierigkeiten, Kratzen beim Schalten
 * - Laegergeraeusche: Surren/Rasseln im Leerlauf (Getriebe)
 * - Oelverbrauch durch undichte Dichtungen
 * - Verschleiss der Synchronringe
 *
 * Ueberwachte Parameter:
 * - Schaltqualitaet (RPM-Einbrueche)
 * - RPM/Geschwindigkeits-Verhaeltnis-Anomalien
 * - Getriebetemperatur (falls verfuegbar)
 * - Input/Output-Speed-Sensoren
 */
class M32GearboxMonitor(
    calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Getriebe-Gesundheitsstatus
     */
    enum class GearboxHealth(val label: String, val colorHex: Long, val severity: Int) {
        HEALTHY("Gesund", 0xFF00FF88, 0),
        EARLY_WEAR("Fruehverschleiss", 0xFFFFE066, 1),
        WEAR_DETECTED("Verschleiss erkannt", 0xFFFF8C00, 2),
        CRITICAL("Kritisch", 0xFFFF4444, 3),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * Erkannte Getriebe-Probleme
     */
    enum class GearboxIssue(val label: String, val description: String) {
        BEARING_WEAR("Lager-Verschleiss", "Kugellager zeigen Verschleisserscheinungen"),
        SYNCHRO_WEAR("Synchronring-Verschleiss", "Synchronringe sind verschlissen"),
        GEAR_WHINE("Getriebeheulen", "Characteristic Laegergeraeusche"),
        OEL_LECK("Oelundichkeit", "Getriebeöl tritt aus"),
        SHIFT_QUALITY("Schaltproblem", "Einruecken von Gaengen erschwert"),
        RPM_ANOMALY("RPM-Anomalie", "Drehzahl/Geschwindigkeits-Verhaeltnis unplausibel")
    }

    /**
     * Eingabedaten fuer die Getriebe-Analyse
     */
    data class GearboxInput(
        val rpmHistory: List<Double>,
        val speedHistory: List<Double>,
        val gearPosition: Int = 0,
        val clutchPosition: Double = 0.0,
        val transmissionTemp: Double = 0.0,
        val inputSpeed: Double = 0.0,
        val outputSpeed: Double = 0.0,
        val totalKm: Double = 0.0,
        val sessionDurationSec: Long = 0L,
        val activeDTCs: List<String> = emptyList()
    )

    /**
     * Ergebnis der Getriebe-Analyse
     */
    data class GearboxAnalysis(
        val health: GearboxHealth,
        val healthScore: Int,
        val detectedIssues: List<GearboxIssue>,
        val shiftQualityScore: Int,
        val rpmSpeedRatioAnomaly: Int,
        val bearingWearIndicator: Int,
        val oilConditionScore: Int,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        // Getriebe-Uebersetzungsverhaeltnisse M32 (A14NET)
        // Diese Werte sind naeherungsweise und koennen variieren
        private val GEAR_RATIOS = mapOf(
            1 to 3.727,
            2 to 2.097,
            3 to 1.393,
            4 to 1.062,
            5 to 0.858,
            6 to 0.698,
            0 to 3.182 // Rueckwaerts
        )
        private const val FINAL_DRIVE = 3.940
        // Engine RPM / vehicle speed includes wheel circumference. The Astra
        // J tyre sizes produce roughly 8.4 wheel RPM per km/h.
        private const val WHEEL_RPM_PER_KMH = 8.4

        // Schwellenwerte
        private const val RATIO_TOLERANCE = 0.08 // 8% Toleranz fuer RPM/Geschwindigkeit
        private const val MAX_RPM_DROP_SHIFT = 2000.0
        private const val MIN_RPM_DROP_SHIFT = 300.0
        private const val WHINE_RPM_MIN = 2000.0
        private const val WHINE_RPM_MAX = 4000.0
        private const val TRANSMISSION_TEMP_WARNING = 100.0
        private const val TRANSMISSION_TEMP_CRITICAL = 120.0
        private const val TYPICAL_WEAR_KM = 80000.0

        // Gewichtung (Summe = 100)
        private const val WEIGHT_SHIFT_QUALITY = 30
        private const val WEIGHT_RPM_RATIO = 30
        private const val WEIGHT_BEARING = 25
        private const val WEIGHT_OIL = 15
    }

    /**
     * Fuehrt eine vollstaendige Getriebe-Analyse durch
     */
    fun analyze(input: GearboxInput): GearboxAnalysis {
        val detectedIssues = mutableListOf<GearboxIssue>()

        // 1. Schaltqualitaet
        val shiftScore = evaluateShiftQuality(input.rpmHistory, input.speedHistory)
        if (shiftScore < 50) detectedIssues.add(GearboxIssue.SYNCHRO_WEAR)

        // 2. RPM/Geschwindigkeits-Verhaeltnis
        val (ratioAnomaly, ratioScore) = evaluateRpmSpeedRatio(
            input.rpmHistory, input.speedHistory, input.gearPosition
        )
        if (ratioScore < 40) detectedIssues.add(GearboxIssue.RPM_ANOMALY)

        // 3. Lager-Verschleiss-Indikator
        val bearingWear = evaluateBearingWear(
            input.rpmHistory, input.speedHistory, input.totalKm
        )
        if (bearingWear > 60) detectedIssues.add(GearboxIssue.BEARING_WEAR)

        // 4. Getriebeöl-Zustand
        val oilScore = evaluateTransmissionOil(
            input.transmissionTemp, input.totalKm
        )

        // 5. Getriebe-Temperatur
        if (input.transmissionTemp > TRANSMISSION_TEMP_CRITICAL) {
            detectedIssues.add(GearboxIssue.OEL_LECK)
        }

        // Gesamtbewertung
        val rawScore = (shiftScore * WEIGHT_SHIFT_QUALITY +
            ratioScore * WEIGHT_RPM_RATIO +
            (100 - bearingWear) * WEIGHT_BEARING +
            oilScore * WEIGHT_OIL) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)

        val health = determineHealth(adjustedScore, input.totalKm, detectedIssues)
        val diagnosis = generateDiagnosis(health, detectedIssues, input)
        val recommendation = generateRecommendation(health, detectedIssues, input)

        return GearboxAnalysis(
            health = health,
            healthScore = adjustedScore,
            detectedIssues = detectedIssues,
            shiftQualityScore = shiftScore,
            rpmSpeedRatioAnomaly = ratioAnomaly,
            bearingWearIndicator = bearingWear,
            oilConditionScore = oilScore,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    /**
     * Bewertet Schaltqualitaet durch RPM-Einbrueche
     */
    private fun evaluateShiftQuality(rpmHistory: List<Double>, speedHistory: List<Double>): Int {
        if (rpmHistory.size < 20 || speedHistory.size < 20) return 70

        var shiftCount = 0
        var goodShifts = 0
        var harshShifts = 0
        var missedShifts = 0

        for (i in 3 until rpmHistory.size - 1) {
            val rpmBefore = rpmHistory[i - 1]
            val rpmAfter = rpmHistory[i]
            val rpmDrop = rpmBefore - rpmAfter

            // Schaltung erkannt: RPM-Einbruch bei steigender Geschwindigkeit
            if (rpmDrop > MIN_RPM_DROP_SHIFT && speedHistory[i] >= speedHistory[i - 1]) {
                shiftCount++

                when {
                    rpmDrop > MAX_RPM_DROP_SHIFT -> harshShifts++
                    rpmDrop in MIN_RPM_DROP_SHIFT..MIN_RPM_DROP_SHIFT * 1.5 -> missedShifts++
                    else -> goodShifts++
                }
            }
        }

        if (shiftCount == 0) return 70

        val goodRatio = goodShifts.toDouble() / shiftCount
        val harshRatio = harshShifts.toDouble() / shiftCount

        return when {
            goodRatio > 0.8 -> 95 // Ausgezeichnete Schaltungen
            goodRatio > 0.6 -> 80 // Gute Schaltungen
            harshRatio > 0.3 -> 40 // Viele harte Schaltungen
            harshRatio > 0.15 -> 55 // Einige harte Schaltungen
            else -> 65 // Durchschnittlich
        }
    }

    /**
     * Bewertet RPM/Geschwindigkeits-Verhaeltnis-Anomalien
     */
    private fun evaluateRpmSpeedRatio(
        rpmHistory: List<Double>,
        speedHistory: List<Double>,
        gear: Int
    ): Pair<Int, Int> {
        if (rpmHistory.size < 10 || speedHistory.size < 10) return 0 to 70

        val expectedRatio = if (gear > 0 && gear in GEAR_RATIOS) {
            GEAR_RATIOS.getValue(gear) * FINAL_DRIVE * WHEEL_RPM_PER_KMH
        } else {
            // Verwende Durchschnitts-Verhaeltnis wenn Gang nicht bekannt
            GEAR_RATIOS.values.average() * FINAL_DRIVE * WHEEL_RPM_PER_KMH
        }

        var anomalousReadings = 0
        var totalReadings = 0

        for (i in rpmHistory.indices) {
            if (speedHistory[i] > 5.0 && rpmHistory[i] > 500) {
                val actualRatio = rpmHistory[i] / speedHistory[i]
                val deviation = abs(actualRatio - expectedRatio) / expectedRatio

                if (deviation > RATIO_TOLERANCE) {
                    anomalousReadings++
                }
                totalReadings++
            }
        }

        if (totalReadings == 0) return 0 to 70

        val anomalyPercentage = (anomalousReadings.toDouble() / totalReadings * 100).toInt()
        val score = when {
            anomalyPercentage < 5 -> 95
            anomalyPercentage < 15 -> 75
            anomalyPercentage < 30 -> 50
            else -> 25
        }

        return anomalyPercentage to score
    }

    /**
     * Bewertet Lager-Verschleiss-Indikatoren
     *
     * Typische Anzeichen fuer M32-Lager-Verschleiss:
     * - Rhythmisches Surren bei bestimmten RPM
     * - Erhoehte Vibration bei konstanter Geschwindigkeit
     * - Geraeusche die sich mit RPM aendern
     */
    private fun evaluateBearingWear(
        rpmHistory: List<Double>,
        speedHistory: List<Double>,
        totalKm: Double
    ): Int {
        if (rpmHistory.size < 20) return 20

        var wearIndicator = 0

        // 1. Laufleistungs-Faktor
        wearIndicator += when {
            totalKm > 150000 -> 40
            totalKm > TYPICAL_WEAR_KM -> 20
            totalKm > 50000 -> 10
            else -> 0
        }

        // 2. RPM-Schwankungsanalyse (unrundes Laufen)
        val rpmStdDev = calculateStdDev(rpmHistory.takeLast(50))
        if (rpmStdDev > 100) wearIndicator += 10
        if (rpmStdDev > 200) wearIndicator += 10

        // 3. Geschwindigkeits-Schwankungen (bei konstantem RPM)
        val speedStdDev = calculateStdDev(speedHistory.takeLast(50))
        if (speedStdDev > 5) wearIndicator += 10
        if (speedStdDev > 10) wearIndicator += 10

        return wearIndicator.coerceIn(0, 100)
    }

    /**
     * Bewertet Getriebeöl-Zustand
     */
    private fun evaluateTransmissionOil(temp: Double, totalKm: Double): Int {
        var score = 100

        // Laufleistung: Oelwechsel bei 60.000 km
        val kmSinceLastChange = totalKm % 60000.0
        if (kmSinceLastChange > 45000) {
            score -= 30
        } else if (kmSinceLastChange > 30000) {
            score -= 15
        }

        // Temperatur
        when {
            temp > TRANSMISSION_TEMP_CRITICAL -> score -= 40
            temp > TRANSMISSION_TEMP_WARNING -> score -= 20
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Berechnet Standardabweichung
     */
    private fun calculateStdDev(values: List<Double>): Double {
        if (values.size < 2) return 0.0
        val avg = values.average()
        val variance = values.map { (it - avg) * (it - avg) }.average()
        return sqrt(variance)
    }

    /**
     * Bestimmt Gesundheitsstatus
     */
    private fun determineHealth(
        score: Int,
        totalKm: Double,
        issues: List<GearboxIssue>
    ): GearboxHealth {
        val hasCriticalIssue = issues.any {
            it == GearboxIssue.BEARING_WEAR || it == GearboxIssue.RPM_ANOMALY
        }

        return when {
            score >= 80 -> GearboxHealth.HEALTHY
            score >= 60 && totalKm > TYPICAL_WEAR_KM -> GearboxHealth.EARLY_WEAR
            score >= 45 -> GearboxHealth.WEAR_DETECTED
            hasCriticalIssue || score < 30 -> GearboxHealth.CRITICAL
            else -> GearboxHealth.WEAR_DETECTED
        }
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        health: GearboxHealth,
        issues: List<GearboxIssue>,
        input: GearboxInput
    ): String {
        return when (health) {
            GearboxHealth.HEALTHY -> {
                "M32-Getriebe funktioniert normal. " +
                    "Schaltqualitaet und Lager sind in Ordnung."
            }
            GearboxHealth.EARLY_WEAR -> {
                "Erste Anzeichen von Getriebeverschleiss bei ${input.totalKm.toInt()} km. " +
                    "Laegergeraeusche oder Schaltprobleme koennen auftreten."
            }
            GearboxHealth.WEAR_DETECTED -> {
                val issueNames = issues.map { it.label }
                "Getriebeverschleiss erkannt: ${issueNames.joinToString(", ")}. " +
                    "Wartung dringend empfohlen."
            }
            GearboxHealth.CRITICAL -> {
                "KRITISCH: Getriebe erfordert sofortige Reparatur! " +
                    "Weiterfahren kann zu Totalschaden fuehren."
            }
            GearboxHealth.UNKNOWN -> {
                "Getriebe-Status nicht bestimmbar. Weitere Daten erforderlich."
            }
        }
    }

    /**
     * Generiert Wartungsempfehlung
     */
    private fun generateRecommendation(
        health: GearboxHealth,
        issues: List<GearboxIssue>,
        input: GearboxInput
    ): String {
        val kmSinceLastChange = input.totalKm % 60000.0
        val issueDetail = if (issues.isNotEmpty()) " Probleme: ${issues.joinToString(", ") { it.label }}." else ""

        return when (health) {
            GearboxHealth.HEALTHY -> {
                if (kmSinceLastChange > 45000) {
                    "Getriebeöl-Wechsel empfohlen (Dexron VI ATF). " +
                        "Nächstes Intervall: ${(60000 - kmSinceLastChange).toInt()} km."
                } else {
                    "Keine Massnahmen erforderlich. " +
                        "Getriebeöl-Wechsel bei 60.000 km Intervall."
                }
            }
            GearboxHealth.EARLY_WEAR -> {
                "Bei ${input.totalKm.toInt()} km: Getriebeöl-Wechsel durchfuehren. " +
                    "Laegergeraeusche beobachten. " +
                    "Empfohlenes Öl: Dexron VI ATF.$issueDetail"
            }
            GearboxHealth.WEAR_DETECTED -> {
                "Getriebe-Inspizierung bei Fachwerkstatt empfohlen. " +
                    "Laeger und Synchronringe pruefen. " +
                    "Getriebeöl-Wechsel mit Qualitaetsöl.$issueDetail"
            }
            GearboxHealth.CRITICAL -> {
                "SOFORT Werkstatt aufsuchen! " +
                    "Getriebe braucht sofortige Reparatur oder Austausch. " +
                    "Nur noetigste Fahrten.$issueDetail"
            }
            GearboxHealth.UNKNOWN -> {
                "Getriebeölstand und -zustand pruefen. " +
                    "Gerausche bei verschiedenen Geschwindigkeiten dokumentieren."
            }
        }
    }
}
