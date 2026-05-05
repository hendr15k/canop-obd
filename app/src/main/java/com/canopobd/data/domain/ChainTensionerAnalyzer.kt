package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Kettenstraffer-Analyse fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Der A14NET Motor (GM Family 0 Gen III) ist bekannt fuer Probleme mit dem
 * hydraulischen Kettenspanner, insbesondere bei hoeheren Laufleistungen.
 *
 * Bekannte DTCs:
 * - P0016: Kurbelwellen-/Nockenwellenposition Sensor A Korrelation Bank 1
 * - P0017: Kurbelwellen-/Nockenwellenposition Sensor B Korrelation Bank 1
 * - P0340: Nockenwellenposition Sensorkreisfehler
 * - P1345: Kurbelwellenposition - Nockenwellenposition Phasenabweichung
 *
 * Die Analyse basiert auf:
 * - DTC-Vorhandensein (kritisch)
 * - Kaltstart-Rattern-Dauer
 * - RPM-Stabilitaet im Leerlauf
 * - Zuendwinkel-Varianz
 *
 * Typische Verschleissgrenzen:
 * - Kettenverlaengerung ab ~80.000 km moeglich
 * - Kritischer Verschleiss ab ~150.000 km
 * - Hydraulischer Straffer Versagen: plötzlicher Totalausfall
 */
class ChainTensionerAnalyzer(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Gesundheitsstatus des Kettenspanners
     */
    enum class ChainTensionerHealth(val label: String, val colorHex: Long, val severity: Int) {
        HEALTHY("Gesund", 0xFF00FF88, 0),
        WEAR_DETECTED("Verschleiss erkannt", 0xFFFFE066, 1),
        CRITICAL("Kritisch", 0xFFFF4444, 2),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * Eingabedaten fuer die Kettenanalyse
     */
    data class ChainTensionerInput(
        val activeDTCs: List<String>,
        val coldStartRattleDurationSec: Double = 0.0,
        val idleRpmVariance: Double = 0.0,
        val timingAdvanceVariance: Double = 0.0,
        val currentRpm: Double = 0.0,
        val timingAdvance: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val engineRuntimeSec: Double = 0.0,
        val totalKm: Double = 0.0
    )

    /**
     * Ergebnis der Kettenstraffer-Analyse
     */
    data class ChainTensionerAnalysis(
        val health: ChainTensionerHealth,
        val healthScore: Int,
        val dtcPenalty: Int,
        val rattlePenalty: Int,
        val rpmStabilityPenalty: Int,
        val timingVariancePenalty: Int,
        val diagnosis: String,
        val recommendation: String,
        val chainElongationEstimate: String
    )

    companion object {
        // DTCs die auf Kettenprobleme hindeuten
        private val CHAIN_DTC_SET = setOf("P0016", "P0017", "P0340", "P0341", "P1345")

        // Schwellenwerte
        private const val MAX_COLD_START_RATTLE_SEC = 2.0
        private const val CRITICAL_RATTLE_SEC = 5.0
        private const val MAX_IDLE_RPM_VARIANCE = 15.0
        private const val WARNING_IDLE_RPM_VARIANCE = 30.0
        private const val MAX_TIMING_VARIANCE = 2.0
        private const val WARNING_TIMING_VARIANCE = 4.0
        private const val HIGH_MILEAGE_KM = 100000.0
        private const val CRITICAL_MILEAGE_KM = 150000.0

        // Gewichtung der Einzelbewertungen (Summe = 100)
        private const val WEIGHT_DTC = 40
        private const val WEIGHT_RATTLE = 25
        private const val WEIGHT_RPM_STABILITY = 20
        private const val WEIGHT_TIMING_VARIANCE = 15
    }

    /**
     * Fuehrt eine vollstaendige Kettenstraffer-Analyse durch.
     *
     * @param input Die Eingabedaten fuer die Analyse
     * @return ChainTensionerAnalysis mit Gesamtbewertung und Diagnose
     */
    fun analyze(input: ChainTensionerInput): ChainTensionerAnalysis {
        // 1. DTC-Bewertung (hoechste Gewichtung)
        val (dtcScore, dtcPenalty) = evaluateDTCs(input.activeDTCs)

        // 2. Kaltstart-Rattern-Analyse
        val (rattleScore, rattlePenalty) = evaluateColdStartRattle(input.coldStartRattleDurationSec)

        // 3. Leerlauf-RPM-Stabilitaet
        val (rpmScore, rpmPenalty) = evaluateRpmStability(input.idleRpmVariance, input.currentRpm)

        // 4. Zuendwinkel-Varianz
        val (timingScore, timingPenalty) = evaluateTimingVariance(input.timingAdvanceVariance)

        // Gesamtbewertung berechnen
        val rawScore = (dtcScore * WEIGHT_DTC +
                rattleScore * WEIGHT_RATTLE +
                rpmScore * WEIGHT_RPM_STABILITY +
                timingScore * WEIGHT_TIMING_VARIANCE) / 100

        // Laufleistungs-Faktor beruecksichtigen
        val mileageFactor = when {
            input.totalKm > CRITICAL_MILEAGE_KM -> 0.8
            input.totalKm > HIGH_MILEAGE_KM -> 0.9
            else -> 1.0
        }
        val adjustedScore = (rawScore * mileageFactor).toInt().coerceIn(0, 100)

        val health = determineHealth(adjustedScore, input.activeDTCs)
        val diagnosis = generateDiagnosis(health, input)
        val recommendation = generateRecommendation(health, input)
        val elongationEstimate = estimateChainElongation(input)

        return ChainTensionerAnalysis(
            health = health,
            healthScore = adjustedScore,
            dtcPenalty = dtcPenalty,
            rattlePenalty = rattlePenalty,
            rpmStabilityPenalty = rpmPenalty,
            timingVariancePenalty = timingPenalty,
            diagnosis = diagnosis,
            recommendation = recommendation,
            chainElongationEstimate = elongationEstimate
        )
    }

    /**
     * Bewertet vorhandene DTCs auf Kettenrelevanz
     */
    private fun evaluateDTCs(dtcCodes: List<String>): Pair<Int, Int> {
        val chainDTCs = dtcCodes.filter { code ->
            CHAIN_DTC_SET.any { known -> code.uppercase().contains(known) }
        }

        return when {
            // Mehrere Ketten-DTCs: Kritisch
            chainDTCs.size >= 3 -> 0 to WEIGHT_DTC
            // Zwei DTCs: Schwerwiegender Fehler
            chainDTCs.size == 2 -> 10 to (WEIGHT_DTC - 10)
            // Ein DTC: Deutliches Warnsignal
            chainDTCs.size == 1 -> {
                val severity = when (chainDTCs.first().uppercase()) {
                    "P0016", "P0017" -> 30 // Korrelation - kritischer
                    "P0340", "P0341" -> 40 // Sensorfehler
                    "P1345" -> 25          // Phasenabweichung
                    else -> 35
                }
                severity to (WEIGHT_DTC - severity)
            }
            // Keine Ketten-DTCs
            else -> 100 to 0
        }
    }

    /**
     * Bewertet Kaltstart-Rattern-Dauer
     *
     * Typischerweise ist Rattern bei kaltem Start ein erstes Anzeichen
     * fuer Kettenverschleiss. Der A14NET Straffer ist hydraulisch
     * und benoetigt Oeldruck zum Spannen.
     */
    private fun evaluateColdStartRattle(rattleDurationSec: Double): Pair<Int, Int> {
        return when {
            rattleDurationSec <= 0.0 -> 100 to 0
            rattleDurationSec <= MAX_COLD_START_RATTLE_SEC -> {
                val penalty = ((rattleDurationSec / MAX_COLD_START_RATTLE_SEC) * 15).toInt()
                (100 - penalty) to penalty
            }
            rattleDurationSec <= CRITICAL_RATTLE_SEC -> {
                val penalty = 25 + ((rattleDurationSec - MAX_COLD_START_RATTLE_SEC) /
                        (CRITICAL_RATTLE_SEC - MAX_COLD_START_RATTLE_SEC) * 25).toInt()
                (100 - penalty).coerceAtLeast(0) to penalty.coerceAtMost(WEIGHT_RATTLE)
            }
            else -> 0 to WEIGHT_RATTLE
        }
    }

    /**
     * Bewertet RPM-Stabilitaet im Leerlauf
     *
     * Bei Kettenschwaeche schwankt die Leerlaufdrehzahl durch
     * geaenderte Steuerzeiten (Phasenverschiebung)
     */
    private fun evaluateRpmStability(idleVariance: Double, currentRpm: Double): Pair<Int, Int> {
        // Nur im Leerlauf bewerten
        val isIdle = currentRpm in 600.0..1200.0

        if (!isIdle && currentRpm > 0) {
            return 80 to 0 // Nicht im Leerlauf, neutrale Bewertung
        }

        return when {
            idleVariance <= MAX_IDLE_RPM_VARIANCE -> 100 to 0
            idleVariance <= WARNING_IDLE_RPM_VARIANCE -> {
                val penalty = ((idleVariance - MAX_IDLE_RPM_VARIANCE) /
                        (WARNING_IDLE_RPM_VARIANCE - MAX_IDLE_RPM_VARIANCE) * 20).toInt()
                (100 - penalty) to penalty
            }
            else -> {
                val penalty = 20 + ((idleVariance - WARNING_IDLE_RPM_VARIANCE) / 20.0).toInt()
                (100 - penalty).coerceAtLeast(0) to penalty.coerceAtMost(WEIGHT_RPM_STABILITY)
            }
        }
    }

    /**
     * Bewertet Zuendwinkel-Varianz
     *
     * Schwankungen im Zundwinkel deuten auf inkorrekte Nockenwellenposition hin,
     * was bei geloester oder ueberdehnter Steuerkette auftreten kann.
     */
    private fun evaluateTimingVariance(variance: Double): Pair<Int, Int> {
        return when {
            variance <= MAX_TIMING_VARIANCE -> 100 to 0
            variance <= WARNING_TIMING_VARIANCE -> {
                val penalty = ((variance - MAX_TIMING_VARIANCE) /
                        (WARNING_TIMING_VARIANCE - MAX_TIMING_VARIANCE) * 20).toInt()
                (100 - penalty) to penalty
            }
            else -> {
                val penalty = 20 + ((variance - WARNING_TIMING_VARIANCE) / 3.0).toInt()
                (100 - penalty).coerceAtLeast(0) to penalty.coerceAtMost(WEIGHT_TIMING_VARIANCE)
            }
        }
    }

    /**
     * Bestimmt den Gesundheitsstatus basierend auf dem Gesamtscore
     */
    private fun determineHealth(score: Int, dtcCodes: List<String>): ChainTensionerHealth {
        // Kritische DTCs ueberschreiben den Score
        val hasCriticalDTC = dtcCodes.any { code ->
            val upper = code.uppercase()
            upper.contains("P0016") || upper.contains("P0017") || upper.contains("P1345")
        }
        val hasSensorDTC = dtcCodes.any { code ->
            val upper = code.uppercase()
            upper.contains("P0340") || upper.contains("P0341")
        }

        return when {
            hasCriticalDTC && score < 30 -> ChainTensionerHealth.CRITICAL
            hasSensorDTC -> ChainTensionerHealth.WEAR_DETECTED
            score >= 75 -> ChainTensionerHealth.HEALTHY
            score >= 45 -> ChainTensionerHealth.WEAR_DETECTED
            else -> ChainTensionerHealth.CRITICAL
        }
    }

    /**
     * Generiert eine Diagnosemeldung auf Deutsch
     */
    private fun generateDiagnosis(health: ChainTensionerHealth, input: ChainTensionerInput): String {
        return when (health) {
            ChainTensionerHealth.HEALTHY -> {
                "Steuerkette und hydraulischer Kettenspanner fonctionieren normal. " +
                        "Keine Anzeichen fuer Verschleiss."
            }
            ChainTensionerHealth.WEAR_DETECTED -> {
                val issues = mutableListOf<String>()
                if (input.coldStartRattleDurationSec > MAX_COLD_START_RATTLE_SEC) {
                    issues.add("Kaltstart-Rattern (${input.coldStartRattleDurationSec.toInt()}s)")
                }
                if (input.idleRpmVariance > WARNING_IDLE_RPM_VARIANCE) {
                    issues.add("Leerlauf-Schwankungen")
                }
                if (input.timingAdvanceVariance > WARNING_TIMING_VARIANCE) {
                    issues.add("Zundwinkel-Abweichung")
                }
                val issueText = if (issues.isNotEmpty()) issues.joinToString(", ") else "Leichte Abweichungen"
                "Kettenverschleiss erkannt: $issueText. " +
                        "Regelmaessige Ueberpruefung empfohlen."
            }
            ChainTensionerHealth.CRITICAL -> {
                "KRITISCH: Steuerkette oder Kettenspanner defekt! " +
                        "Sofortige Werkstatt erforderlich. " +
                        "Motor kann bei Weiterfahren schweren Schaden nehmen."
            }
            ChainTensionerHealth.UNKNOWN -> {
                "Nicht genuegend Daten fuer eine zuverlaessige Diagnose. " +
                        "Weitere Messungen erforderlich."
            }
        }
    }

    /**
     * Generiert Empfehlungen auf Deutsch
     */
    private fun generateRecommendation(health: ChainTensionerHealth, input: ChainTensionerInput): String {
        return when (health) {
            ChainTensionerHealth.HEALTHY -> {
                if (input.totalKm > HIGH_MILEAGE_KM) {
                    "Bei ${input.totalKm.toInt()} km: Kettenspanner bei naechster " +
                            "Grosswartung pruefen lassen. Oelwechselintervalle einhalten."
                } else {
                    "Keine Massnahmen erforderlich. " +
                            "Oelwechselintervalle (15.000 km mit Dexos2 5W-30) einhalten."
                }
            }
            ChainTensionerHealth.WEAR_DETECTED -> {
                "Kettenspanner und Steuerkette bei Werkstatt pruefen lassen. " +
                        "Oelqualitaet pruefen - mind. Dexos2 5W-30. " +
                        "Bei ${input.totalKm.toInt()} km sollte eine Inspektion erfolgen."
            }
            ChainTensionerHealth.CRITICAL -> {
                "SOFORT Werkstatt aufsuchen! Steuerkette und Kettenspanner " +
                        "muessen sofort ersetzt werden. " +
                        "Nur kurze, vorsichtige Fahrten zum Servicestandort."
            }
            ChainTensionerHealth.UNKNOWN -> {
                "Weitere Daten sammeln. Motor warmfahren und Leerlauf-Stabilitaet " +
                        "beobachten. Kaltstart-Rattern dokumentieren."
            }
        }
    }

    /**
     * Schaetzt die Kettenverlaengerung basierend auf verfuegbaren Daten
     */
    private fun estimateChainElongation(input: ChainTensionerInput): String {
        val baseWearPercent = when {
            input.totalKm < 50000 -> 5
            input.totalKm < 100000 -> 15
            input.totalKm < 150000 -> 35
            input.totalKm < 200000 -> 60
            else -> 80
        }

        val rattleAddition = when {
            input.coldStartRattleDurationSec > CRITICAL_RATTLE_SEC -> 20
            input.coldStartRattleDurationSec > MAX_COLD_START_RATTLE_SEC -> 10
            else -> 0
        }

        val varianceAddition = when {
            input.timingAdvanceVariance > WARNING_TIMING_VARIANCE -> 15
            input.timingAdvanceVariance > MAX_TIMING_VARIANCE -> 5
            else -> 0
        }

        val totalWear = (baseWearPercent + rattleAddition + varianceAddition).coerceAtMost(100)
        return "Geschaetzter Verschleiss: ~${totalWear}% bei ${input.totalKm.toInt()} km"
    }
}
