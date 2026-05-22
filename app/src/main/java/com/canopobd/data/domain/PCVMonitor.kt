package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

/**
 * PCV-Ventil (Positive Crankcase Ventilation) Monitor fuer A14NET
 *
 * Das PCV-System des A14NET ist ein bekannter Schwachpunkt.
 * Bei Verschleiss oder Verstopfung treten folgende Probleme auf:
 *
 * - Erhoehter Oelverbrauch (bis zu 1L/1000km bei schweren Faellen)
 * - Blauer Rauch aus dem Auspuff
 * - Druckaufbau in der Kurbelgehaeuse
 * - Leistungsverlust durch gestoertes Kraftstoff-Luft-Gemisch
 * - Kraftstofftrimm-Abweichungen (STFT/LTFT)
 *
 * DTCs:
 * - P1100: PCV-Systemfehler
 * - P1101: Luftmassenmesser / PCV Korrelationsfehler
 *
 * Das PCV-System arbeitet mit Unterdruck vom Ansaugkrümmer.
 * Bei Verstopfung steigt der Kurbelgehaeusedruck und fuegt
 * Undichtigkeiten in Dichtungen (Riemenspanner, Ventildeckel).
 *
 * Typischer Verschleiss: 60.000-100.000 km
 * Empfohlene Pruefung: bei jedem Oelwechsel
 */
class PCVMonitor(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Gesundheitsstatus des PCV-Systems
     */
    enum class PCVHealth(val label: String, val colorHex: Long, val severity: Int) {
        HEALTHY("Gesund", 0xFF00FF88, 0),
        PLUGGED("Verstopft", 0xFFFF8C00, 1),
        LEAKING("Leckage", 0xFFFFE066, 1),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * PCV-Eingabedaten
     */
    data class PCVInput(
        val activeDTCs: List<String>,
        val mafRate: Double,
        val mafExpectedAtRpm: Double,
        val stft: Double,
        val ltft: Double,
        val oilConsumptionLPer1000Km: Double = 0.0,
        val intakeManifoldPressure: Double = 0.0,
        val barometricPressure: Double = 100.0,
        val rpm: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val engineLoad: Double = 0.0,
        val totalKm: Double = 0.0,
        val throttle: Double = 0.0
    )

    /**
     * PCV-Analyseergebnis
     */
    data class PCVAnalysis(
        val health: PCVHealth,
        val healthScore: Int,
        val mafDeviation: Double,
        val totalTrimDeviation: Double,
        val oilConsumptionStatus: String,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        // DTCs die auf PCV-Probleme hindeuten
        private val PCV_DTC_SET = setOf("P1100", "P1101")

        // Schwellenwerte
        private const val MAF_DEVIATION_WARNING = 15.0      // % Abweichung vom Soll
        private const val MAF_DEVIATION_CRITICAL = 30.0     // % kritische Abweichung
        private const val TOTAL_TRIM_WARNING = 8.0           // % Gesamttrimm-Schwelle
        private const val TOTAL_TRIM_CRITICAL = 15.0        // % kritische Trimmabweichung
        private const val OIL_CONSUMPTION_WARNING = 0.3     // L/1000km
        private const val OIL_CONSUMPTION_CRITICAL = 0.7    // L/1000km
        private const val TYPICAL_MILEAGE_FOR_PCV = 80000.0 // km

        // Gewichtung (Summe = 100)
        private const val WEIGHT_DTC = 30
        private const val WEIGHT_MAF = 20
        private const val WEIGHT_TRIM = 25
        private const val WEIGHT_OIL = 25
    }

    /**
     * Fuehrt eine PCV-Analyse durch
     */
    fun analyze(input: PCVInput): PCVAnalysis {
        // 1. DTC-Bewertung
        val dtcScore = evaluateDTCs(input.activeDTCs)

        // 2. MAF-Abweichung pruefen (PCV-Leckage fuehrt zu falschen MAF-Werten)
        val mafDeviation = calculateMAFDeviation(input.mafRate, input.mafExpectedAtRpm)
        val mafScore = evaluateMAFDeviation(mafDeviation)

        // 3. Kraftstofftrimm-Analyse
        val totalTrim = abs(input.stft + input.ltft)
        val trimScore = evaluateTrimDeviation(totalTrim)

        // 4. Oelverbrauchsanalyse
        val oilScore = evaluateOilConsumption(input.oilConsumptionLPer1000Km)
        val oilStatus = getOilConsumptionStatus(input.oilConsumptionLPer1000Km)

        // Gesamtbewertung
        val rawScore = (dtcScore * WEIGHT_DTC +
                mafScore * WEIGHT_MAF +
                trimScore * WEIGHT_TRIM +
                oilScore * WEIGHT_OIL) / 100

        // Laufleistungs-Faktor
        val mileageFactor = when {
            input.totalKm > TYPICAL_MILEAGE_FOR_PCV -> 0.85
            input.totalKm > TYPICAL_MILEAGE_FOR_PCV * 0.75 -> 0.9
            else -> 1.0
        }
        val adjustedScore = (rawScore * mileageFactor).toInt().coerceIn(0, 100)

        val health = determineHealth(adjustedScore, input.activeDTCs)
        val diagnosis = generateDiagnosis(health, input, mafDeviation, totalTrim)
        val recommendation = generateRecommendation(health, input)

        return PCVAnalysis(
            health = health,
            healthScore = adjustedScore,
            mafDeviation = mafDeviation,
            totalTrimDeviation = totalTrim,
            oilConsumptionStatus = oilStatus,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    /**
     * Bewertet PCV-relevante DTCs
     */
    private fun evaluateDTCs(dtcCodes: List<String>): Int {
        val pcvDTCs = dtcCodes.filter { code ->
            PCV_DTC_SET.any { known -> code.uppercase().contains(known) }
        }

        return when {
            pcvDTCs.size >= 2 -> 10  // Kritisch
            pcvDTCs.size == 1 -> 40  // Warnung
            else -> 100              // Kein Fehler
        }
    }

    /**
     * Berechnet MAF-Abweichung vom Sollwert
     *
     * PCV-Leckage fuehrt zu ungefilterter Luft im Ansaugsystem,
     * was den MAF-Sensor falsch lesen laesst.
     */
    private fun calculateMAFDeviation(actualMAF: Double, expectedMAF: Double): Double {
        if (expectedMAF <= 0.0) return 0.0
        return ((actualMAF - expectedMAF) / expectedMAF) * 100.0
    }

    /**
     * Bewertet MAF-Abweichung
     */
    private fun evaluateMAFDeviation(deviation: Double): Int {
        val absDeviation = abs(deviation)
        return when {
            absDeviation <= MAF_DEVIATION_WARNING -> 100
            absDeviation <= MAF_DEVIATION_CRITICAL -> {
                100 - ((absDeviation - MAF_DEVIATION_WARNING) /
                        (MAF_DEVIATION_CRITICAL - MAF_DEVIATION_WARNING) * 40).toInt()
            }
            else -> 30
        }
    }

    /**
     * Bewertet Kraftstofftrimm-Abweichung
     *
     * Bei PCV-Problemen koennen die Trimmwerte in beide Richtungen
     * abweichen, da ungemessene Luft eintritt (mager) oder
     * Oel dampf ins Gemisch gelangt (fett).
     */
    private fun evaluateTrimDeviation(totalTrim: Double): Int {
        return when {
            totalTrim <= TOTAL_TRIM_WARNING -> 100
            totalTrim <= TOTAL_TRIM_CRITICAL -> {
                100 - ((totalTrim - TOTAL_TRIM_WARNING) /
                        (TOTAL_TRIM_CRITICAL - TOTAL_TRIM_WARNING) * 40).toInt()
            }
            else -> 20
        }
    }

    /**
     * Bewertet Oelverbrauch
     */
    private fun evaluateOilConsumption(lPer1000Km: Double): Int {
        return when {
            lPer1000Km <= 0 -> 80  // Kein Datenpunkt
            lPer1000Km <= OIL_CONSUMPTION_WARNING -> 100
            lPer1000Km <= OIL_CONSUMPTION_CRITICAL -> {
                100 - ((lPer1000Km - OIL_CONSUMPTION_WARNING) /
                        (OIL_CONSUMPTION_CRITICAL - OIL_CONSUMPTION_WARNING) * 50).toInt()
            }
            else -> 15
        }
    }

    /**
     * Bestimmt Oelverbrauchs-Status
     */
    private fun getOilConsumptionStatus(lPer1000Km: Double): String {
        return when {
            lPer1000Km <= 0 -> "Keine Daten verfuegbar"
            lPer1000Km <= OIL_CONSUMPTION_WARNING -> "Normal (< 0.3 L/1000km)"
            lPer1000Km <= OIL_CONSUMPTION_CRITICAL -> "Erhoeht (0.3-0.7 L/1000km)"
            else -> "Kritisch (> 0.7 L/1000km)"
        }
    }

    /**
     * Bestimmt den PCV-Gesundheitsstatus
     */
    private fun determineHealth(score: Int, dtcCodes: List<String>): PCVHealth {
        val hasPCVDTC = dtcCodes.any { code ->
            PCV_DTC_SET.any { known -> code.uppercase().contains(known) }
        }

        return when {
            score >= 75 -> PCVHealth.HEALTHY
            hasPCVDTC && score < 40 -> {
                // Unterscheide zwischen Verstopfung und Leakage basierend auf Trimm
                PCVHealth.PLUGGED  // Standardannahme bei PCV-DTC
            }
            score >= 45 -> PCVHealth.PLUGGED
            else -> PCVHealth.PLUGGED
        }
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        health: PCVHealth,
        input: PCVInput,
        mafDeviation: Double,
        totalTrim: Double
    ): String {
        return when (health) {
            PCVHealth.HEALTHY -> {
                "PCV-System funktioniert normal. " +
                        "Kurbelgehaeuseentlueftung ist frei."
            }
            PCVHealth.PLUGGED -> {
                val issues = mutableListOf<String>()
                if (abs(mafDeviation) > MAF_DEVIATION_WARNING) {
                    issues.add("MAF-Abweichung: ${"%.1f".format(mafDeviation)}%")
                }
                if (totalTrim > TOTAL_TRIM_WARNING) {
                    issues.add("Trimmabweichung: ${"%.1f".format(totalTrim)}%")
                }
                if (input.oilConsumptionLPer1000Km > OIL_CONSUMPTION_WARNING) {
                    issues.add("Erhoehter Oelverbrauch")
                }
                val detail = if (issues.isNotEmpty()) " - ${issues.joinToString("; ")}" else ""
                "PCV-Ventil verstopft oder eingeschraenkt.$detail " +
                        "Kurbelgehaeusedruck kann erhoeht sein."
            }
            PCVHealth.LEAKING -> {
                "PCV-System hat eine Leckage. " +
                        "Uebermaessige Luft im Ansaugsystem fuehrt zu Falschluft."
            }
            PCVHealth.UNKNOWN -> {
                "PCV-Status nicht bestimmbar. Weitere Daten erforderlich."
            }
        }
    }

    /**
     * Generiert Wartungsempfehlung
     */
    private fun generateRecommendation(health: PCVHealth, input: PCVInput): String {
        return when (health) {
            PCVHealth.HEALTHY -> {
                if (input.totalKm > 50000) {
                    "PCV-Ventil bei naechstem Oelwechsel pruefen. " +
                            "Bei ${input.totalKm.toInt()} km routinemaessige Pruefung empfohlen."
                } else {
                    "Keine Massnahmen erforderlich. " +
                            "PCV-Ventil bei ${input.totalKm.toInt()} km noch in Ordnung."
                }
            }
            PCVHealth.PLUGGED -> {
                "PCV-Ventil und Zylinderkopfdeckel-Ventil ersetzen lassen. " +
                        "Nur Opel-OEM oder hochwertige Nachbauteile verwenden. " +
                        "Kurbelgehaeusedichtungen auf Dichtheit pruefen."
            }
            PCVHealth.LEAKING -> {
                "PCV-Leitungen und Anschluesse auf Risse und Dichtheit pruefen. " +
                        "Zylinderkopfdeckel-Dichtung kontrollieren."
            }
            PCVHealth.UNKNOWN -> {
                "Oeldruck- und Kurbelgehaeusedruck-Messung bei Werkstatt durchfuehren. " +
                        "PCV-Ventil visuell pruefen."
            }
        }
    }
}
