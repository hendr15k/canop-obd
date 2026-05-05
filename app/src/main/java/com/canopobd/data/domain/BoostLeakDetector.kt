package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

/**
 * Boost-Leck-Erkennung fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Erkennt Ladeluft-Lecks durch Analyse mehrerer Korrelationen:
 * - Wastegate-Duty vs tatsaechlichem Boost (Abweichung deutet auf Leck hin)
 * - Turbo-Drehzahl vs Ladedruck (hohe Drehzahl ohne Boost = Leck)
 * - Ladelufttemperatur-Delta (Einlass vs Auslass, untypische Differenzen)
 * - MAF-Lesungen bei bekannten RPM/Last-Punkten (unerwartet niedrig = Leck)
 *
 * Typische Leck-Ursachen A14NET:
 * - Schlauchverbindungen Ladeluftkuehler (nach 80.000+ km)
 * - Risse in Ladeluft-Schlaeuchen
 * - Defekte Dichtungen am Ansaugkrümmer
 * - Porose Wastegate-Unterdruckleitung
 * - Rissiger Ladeluftkuehler
 */
class BoostLeakDetector(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Schweregrad des erkannten Lecks
     */
    enum class LeakSeverity(val label: String, val colorHex: Long, val severity: Int) {
        NONE("Kein Leck", 0xFF00FF88, 0),
        MINOR("Kleines Leck", 0xFFFFE066, 1),
        MODERATE("Mittleres Leck", 0xFFFF8C00, 2),
        SEVERE("Grosses Leck", 0xFFFF4444, 3),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * Art des Lecks (Diagnose-Hilfe)
     */
    enum class LeakLocation(val label: String, val description: String) {
        CHARGE_PIPE("Ladeluft-Schlauch", "Verbindungsschlauch oder -schelle undicht"),
        INTERCOOLER("Ladeluftkuehler", "Ladeluftkuehler beschädigt oder gerissen"),
        INTAKE_MANIFOLD("Ansaugkrümmer", "Dichtungsleck am Ansaugkrümmer"),
        WASTEGATE_AREA("Wastegate-Bereich", "Unterdruckleitung oder Wastegate-Mechanik"),
        TURBO_OUTLET("Turbo-Ausgang", "Verbindung Turbo -> Ladeluftkuehler"),
        THROTTLE_BODY("Drosselklappe", "Dichtung an der Drosselklappeneinheit"),
        NONE("Kein Leck", "System dicht"),
        UNKNOWN("Unbekannt", "Nicht lokalisierbar")
    }

    /**
     * Eingabedaten fuer die Boost-Leck-Analyse
     */
    data class BoostLeakInput(
        val boostActualBar: Double,
        val boostTargetBar: Double,
        val wastegateDuty: Double,
        val turboRpm: Double = 0.0,
        val chargeAirTemp: Double = 0.0,
        val intakeTemp: Double = 0.0,
        val mafRate: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val throttle: Double = 0.0,
        val exhaustPressure: Double = 0.0,
        val stftB1: Double = 0.0,
        val ltftB1: Double = 0.0
    )

    /**
     * Ergebnis der Boost-Leck-Analyse
     */
    data class BoostLeakAnalysis(
        val severity: LeakSeverity,
        val likelyLocation: LeakLocation,
        val healthScore: Int,
        val boostDeviationPercent: Double,
        val turboBoostCorrelation: Double,
        val tempDeltaAnomaly: Double,
        val mafDeviation: Double,
        val confidencePercent: Int,
        val diagnosis: String,
        val recommendation: String,
        val detectedIndicators: List<String>
    )

    companion object {
        // Boost-Abweichungs-Schwellenwerte (%)
        private const val BOOST_DEVIATION_MINOR = 10.0
        private const val BOOST_DEVIATION_MODERATE = 20.0
        private const val BOOST_DEVIATION_SEVERE = 35.0

        // Wastegate-Duty vs Boost-Korrelation
        private const val WG_DUTY_HIGH_THRESHOLD = 80.0
        private const val WG_DUTY_LOW_THRESHOLD = 30.0

        // Turbo-RPM vs Boost-Korrelation
        private const val TURBO_RPM_HIGH = 150000.0
        private const val TURBO_RPM_VERY_HIGH = 180000.0

        // Ladelufttemperatur-Delta (Ansaug vs Ladeluft)
        private const val TEMP_DELTA_NORMAL_MAX = 25.0
        private const val TEMP_DELTA_WARNING = 35.0
        private const val TEMP_DELTA_ANOMALOUS = 15.0

        // MAF-Erwartungsbereich bei verschiedenen Lastpunkten (g/s)
        private const val MAF_IDLE_EXPECTED_MIN = 2.0
        private const val MAF_IDLE_EXPECTED_MAX = 5.0
        private const val MAF_2000RPM_EXPECTED_MIN = 8.0
        private const val MAF_2000RPM_EXPECTED_MAX = 20.0
        private const val MAF_3000RPM_EXPECTED_MIN = 15.0
        private const val MAF_3000RPM_EXPECTED_MAX = 40.0

        // Minimale Last fuer zuverlaessige Diagnose
        private const val MIN_LOAD_FOR_ANALYSIS = 30.0
        private const val MIN_THROTTLE_FOR_ANALYSIS = 25.0
        private const val MIN_RPM_FOR_ANALYSIS = 1500.0

        // Gewichtung der einzelnen Indikatoren (Summe = 100)
        private const val WEIGHT_BOOST_DEVIATION = 30
        private const val WEIGHT_TURBO_BOOST_CORR = 25
        private const val WEIGHT_TEMP_DELTA = 20
        private const val WEIGHT_MAF = 15
        private const val WEIGHT_FUEL_TRIM = 10
    }

    /**
     * Fuehrt eine vollstaendige Boost-Leck-Analyse durch
     */
    fun analyze(input: BoostLeakInput): BoostLeakAnalysis {
        val indicators = mutableListOf<String>()

        val isSufficientLoad = input.engineLoad >= MIN_LOAD_FOR_ANALYSIS &&
                input.throttle >= MIN_THROTTLE_FOR_ANALYSIS &&
                input.rpm >= MIN_RPM_FOR_ANALYSIS

        if (!isSufficientLoad) {
            return BoostLeakAnalysis(
                severity = LeakSeverity.UNKNOWN,
                likelyLocation = LeakLocation.UNKNOWN,
                healthScore = -1,
                boostDeviationPercent = 0.0,
                turboBoostCorrelation = 0.0,
                tempDeltaAnomaly = 0.0,
                mafDeviation = 0.0,
                confidencePercent = 0,
                diagnosis = "Nicht genuegend Last fuer Boost-Leck-Analyse. " +
                        "Mindestens ${MIN_LOAD_FOR_ANALYSIS.toInt()}% Last, " +
                        "${MIN_THROTTLE_FOR_ANALYSIS.toInt()}% Gas und " +
                        "${MIN_RPM_FOR_ANALYSIS.toInt()} RPM erforderlich.",
                recommendation = "Analyse bei hoeherer Last wiederholen (Vollgas-Messung empfohlen).",
                detectedIndicators = emptyList()
            )
        }

        // 1. Boost-Abweichung (Ist vs Soll)
        val (boostScore, boostDeviation) = evaluateBoostDeviation(input, indicators)

        // 2. Turbo-RPM vs Boost-Korrelation
        val (turboBoostScore, turboBoostCorrelation) = evaluateTurboBoostCorrelation(input, indicators)

        // 3. Ladelufttemperatur-Delta
        val (tempScore, tempDelta) = evaluateTemperatureDelta(input, indicators)

        // 4. MAF-Analyse
        val (mafScore, mafDeviation) = evaluateMAFReadings(input, indicators)

        // 5. Kraftstoff-Trim-Analyse (Leck -> Magerkorrektur)
        val trimScore = evaluateFuelTrims(input, indicators)

        // Gesamtbewertung
        val rawScore = (boostScore * WEIGHT_BOOST_DEVIATION +
                turboBoostScore * WEIGHT_TURBO_BOOST_CORR +
                tempScore * WEIGHT_TEMP_DELTA +
                mafScore * WEIGHT_MAF +
                trimScore * WEIGHT_FUEL_TRIM) / 100

        val healthScore = rawScore.coerceIn(0, 100)

        val severity = determineSeverity(healthScore, indicators.size)
        val confidence = calculateConfidence(input, indicators.size)
        val location = estimateLeakLocation(input, indicators)
        val diagnosis = generateDiagnosis(severity, location, input, indicators)
        val recommendation = generateRecommendation(severity, location, indicators)

        return BoostLeakAnalysis(
            severity = severity,
            likelyLocation = location,
            healthScore = healthScore,
            boostDeviationPercent = boostDeviation,
            turboBoostCorrelation = turboBoostCorrelation,
            tempDeltaAnomaly = tempDelta,
            mafDeviation = mafDeviation,
            confidencePercent = confidence,
            diagnosis = diagnosis,
            recommendation = recommendation,
            detectedIndicators = indicators
        )
    }

    /**
     * Bewertet Boost-Abweichung (Ist vs Soll)
     *
     * Ein Leck fuehrt zu Unterladung: Ist-Boost < Soll-Boost
     * Der Regler erhoeht den Wastegate-Duty (schliesst WG), aber
     * der Boost steigt nicht wie erwartet.
     */
    private fun evaluateBoostDeviation(
        input: BoostLeakInput,
        indicators: MutableList<String>
    ): Pair<Int, Double> {
        if (input.boostTargetBar <= 0.01) return 100 to 0.0

        val deviation = ((input.boostActualBar - input.boostTargetBar) / input.boostTargetBar) * 100.0
        val absDeviation = abs(deviation)

        // Nur Unterladung ist relevant fuer Leck-Erkennung
        if (deviation > 0) return 100 to deviation

        val score = when {
            absDeviation < BOOST_DEVIATION_MINOR -> 100
            absDeviation < BOOST_DEVIATION_MODERATE -> {
                indicators.add("Leichte Unterladung: ${"%.1f".format(absDeviation)}% unter Soll")
                (100 - (absDeviation - BOOST_DEVIATION_MINOR) / (BOOST_DEVIATION_MODERATE - BOOST_DEVIATION_MINOR) * 30).toInt()
            }
            absDeviation < BOOST_DEVIATION_SEVERE -> {
                indicators.add("Mittlere Unterladung: ${"%.1f".format(absDeviation)}% unter Soll")
                (70 - (absDeviation - BOOST_DEVIATION_MODERATE) / (BOOST_DEVIATION_SEVERE - BOOST_DEVIATION_MODERATE) * 40).toInt()
            }
            else -> {
                indicators.add("Schwere Unterladung: ${"%.1f".format(absDeviation)}% unter Soll")
                10
            }
        }

        // Zusaetzlicher Hinweis: Wastegate versucht zu kompensieren
        if (deviation < -BOOST_DEVIATION_MINOR && input.wastegateDuty < WG_DUTY_LOW_THRESHOLD) {
            indicators.add("Wastegate geschlossen (${input.wastegateDuty.toInt()}%), aber Boost niedrig")
        }

        return score.coerceIn(0, 100) to deviation
    }

    /**
     * Bewertet Turbo-Drehzahl vs Boost-Korrelation
     *
     * Bei einem Leck dreht der Turbo schneller als erwartet fuer
     * den tatsaechlich erreichten Ladedruck. Die verdichtete Luft
     * entweicht durch das Leck.
     */
    private fun evaluateTurboBoostCorrelation(
        input: BoostLeakInput,
        indicators: MutableList<String>
    ): Pair<Int, Double> {
        if (input.turboRpm <= 0 || input.boostActualBar <= 0) return 70 to 0.0

        // Erwarteter Boost bei gegebener Turbo-Drehzahl
        val expectedBoostPerRpm = calibration.normalBoostTargetBar / 100000.0
        val expectedBoost = input.turboRpm * expectedBoostPerRpm
        val correlation = if (expectedBoost > 0) {
            input.boostActualBar / expectedBoost
        } else 1.0

        val score = when {
            correlation >= 0.85 -> 100
            correlation >= 0.65 -> {
                indicators.add("Turbo-RPM/Boost-Korrelation niedrig: ${"%.0f".format(correlation * 100)}%")
                60
            }
            correlation >= 0.45 -> {
                indicators.add("Turbo-RPM/Boost-Korrelation stark niedrig: ${"%.0f".format(correlation * 100)}%")
                35
            }
            correlation >= 0.25 -> {
                indicators.add("Turbo dreht (${input.turboRpm.toInt()} rpm) ohne ausreichenden Boost")
                15
            }
            else -> {
                indicators.add("Turbo dreht hoch (${input.turboRpm.toInt()} rpm) aber fast kein Boost!")
                5
            }
        }

        // Turbo ueberdreht bei Leck
        if (input.turboRpm > TURBO_RPM_VERY_HIGH && input.boostActualBar < calibration.normalBoostTargetBar * 0.6) {
            indicators.add("Turbo-Ueberdrehung bei Unterladung (Leck-Verdacht)")
        }

        return score to correlation
    }

    /**
     * Bewertet Ladelufttemperatur-Delta
     *
     * Ein Leck nach dem Verdichter aber vor dem Ladeluftkuehler
     * kann zu untypischen Temperaturdifferenzen fuehren.
     * Ein Leck nach dem Ladeluftkuehler zeigt oft einen
     * geringeren als erwarteten Temperaturanstieg.
     */
    private fun evaluateTemperatureDelta(
        input: BoostLeakInput,
        indicators: MutableList<String>
    ): Pair<Int, Double> {
        if (input.chargeAirTemp <= 0 || input.intakeTemp <= 0) return 70 to 0.0

        val tempDelta = input.chargeAirTemp - input.intakeTemp

        val score = when {
            // Sehr niedriges Delta bei Last = Luft entweicht vor Messpunkt
            tempDelta < TEMP_DELTA_ANOMALOUS && input.engineLoad > 50 -> {
                indicators.add("Ladeluft-Temperaturdelta untypisch niedrig (${tempDelta.toInt()}°C)")
                40
            }
            // Normales Delta
            tempDelta <= TEMP_DELTA_NORMAL_MAX -> 100
            // Erhoehtes Delta
            tempDelta <= TEMP_DELTA_WARNING -> {
                indicators.add("Ladeluft-Temperaturdelta erhoeht (${tempDelta.toInt()}°C)")
                65
            }
            // Sehr hohes Delta
            else -> {
                indicators.add("Ladeluft-Temperaturdelta kritisch (${tempDelta.toInt()}°C)")
                30
            }
        }

        return score to tempDelta
    }

    /**
     * Bewertet MAF-Lesungen bei bekannten RPM/Last-Punkten
     *
     * Bei einem Leck VOR dem Verdichter (Ansaug-Seite) liest der
     * MAF-Sensor weniger Luft. Bei einem Leck NACH dem Verdichter
     * kann der MAF normal lesen, aber der Boost faellt ab.
     */
    private fun evaluateMAFReadings(
        input: BoostLeakInput,
        indicators: MutableList<String>
    ): Pair<Int, Double> {
        if (input.mafRate <= 0) return 70 to 0.0

        val expectedMafRange = when {
            input.rpm < 1200 -> MAF_IDLE_EXPECTED_MIN..MAF_IDLE_EXPECTED_MAX
            input.rpm < 2200 -> MAF_2000RPM_EXPECTED_MIN..MAF_2000RPM_EXPECTED_MAX
            input.rpm < 3500 -> MAF_3000RPM_EXPECTED_MIN..MAF_3000RPM_EXPECTED_MAX
            else -> {
                // Hochlast: MAF ~ proportional zu Last
                val expected = input.engineLoad / 100.0 * 80.0
                (expected * 0.6)..(expected * 1.3)
            }
        }

        val deviation = if (expectedMafRange.endInclusive > 0) {
            val expectedMid = (expectedMafRange.start + expectedMafRange.endInclusive) / 2.0
            ((input.mafRate - expectedMid) / expectedMid) * 100.0
        } else 0.0

        val score = when {
            input.mafRate in expectedMafRange -> 100
            input.mafRate < expectedMafRange.start -> {
                val percentBelow = ((expectedMafRange.start - input.mafRate) / expectedMafRange.start * 100.0)
                indicators.add("MAF niedrig: ${"%.1f".format(input.mafRate)} g/s (erwartet %.1f-%.1f)".format(
                    expectedMafRange.start, expectedMafRange.endInclusive))
                when {
                    percentBelow > 30 -> 25
                    percentBelow > 15 -> 50
                    else -> 75
                }
            }
            input.mafRate > expectedMafRange.endInclusive -> {
                // Uebermaessiger MAF bei Unterladung = Leck nach Turbo aber MAF misst volle Luftmenge
                if (input.boostActualBar < input.boostTargetBar * 0.8) {
                    indicators.add("MAF normal/hoch (${"%.1f".format(input.mafRate)} g/s) aber Boost niedrig")
                    45
                } else 85
            }
            else -> 80
        }

        return score to deviation
    }

    /**
     * Bewertet Kraftstoff-Trims als Leck-Indikator
     *
     * Ein Ladeluft-Leck fuehrt zu unverdichteter Luft die den
     * Motor erreicht (oder fehlender Luft bei Vor-Turbo-Leck).
     * Beides fuehrt zu Trim-Korrekturen.
     */
    private fun evaluateFuelTrims(input: BoostLeakInput, indicators: MutableList<String>): Int {
        val totalTrim = input.stftB1 + input.ltftB1
        val absTrim = abs(totalTrim)

        if (absTrim > 10.0 && totalTrim > 0) {
            indicators.add("Magerkorrektur erkannt (STFT+LTFT: ${"%.1f".format(totalTrim)}%)")
        }

        return when {
            absTrim > 15.0 && totalTrim > 0 -> 20
            absTrim > 10.0 && totalTrim > 0 -> 45
            absTrim > 5.0 && totalTrim > 0 -> 70
            else -> 100
        }
    }

    /**
     * Bestimmt den Schweregrad des Lecks
     */
    private fun determineSeverity(score: Int, indicatorCount: Int): LeakSeverity {
        return when {
            indicatorCount == 0 -> LeakSeverity.NONE
            score >= 80 -> LeakSeverity.NONE
            score >= 60 -> LeakSeverity.MINOR
            score >= 35 -> LeakSeverity.MODERATE
            score >= 0 -> LeakSeverity.SEVERE
            else -> LeakSeverity.UNKNOWN
        }
    }

    /**
     * Berechnet die Vertrauenswahrscheinlichkeit der Diagnose
     */
    private fun calculateConfidence(input: BoostLeakInput, indicatorCount: Int): Int {
        var confidence = 0

        // Mehr Indikatoren = hoehere Vertrauenswahrscheinlichkeit
        confidence += when (indicatorCount) {
            0 -> 20
            1 -> 40
            2 -> 60
            3 -> 75
            4 -> 85
            else -> 95
        }

        // Hoehere Last = zuverlaessigere Daten
        if (input.engineLoad > 60) confidence += 5
        if (input.throttle > 50) confidence += 5

        // Turbo-RPM vorhanden = zusaetzliche Datenquelle
        if (input.turboRpm > 0) confidence += 5

        return confidence.coerceIn(0, 100)
    }

    /**
     * Schaetzt die Leck-Position basierend auf Indikatoren
     *
     * Kombiniert die verschiedenen Indikatoren um die wahrscheinlichste
     * Position des Lecks zu bestimmen.
     */
    private fun estimateLeakLocation(input: BoostLeakInput, indicators: List<String>): LeakLocation {
        val boostDeviation = if (input.boostTargetBar > 0.01) {
            ((input.boostActualBar - input.boostTargetBar) / input.boostTargetBar) * 100.0
        } else 0.0

        val totalTrim = input.stftB1 + input.ltftB1

        return when {
            indicators.isEmpty() -> LeakLocation.NONE

            // MAF niedrig + Unterladung = Leck vor Turbo oder im Ansaugtrakt
            input.mafRate < MAF_IDLE_EXPECTED_MIN && boostDeviation < -BOOST_DEVIATION_MODERATE -> {
                LeakLocation.TURBO_OUTLET
            }

            // MAF normal/hoch + Unterladung = Leck nach Turbo (Ladeluft-Seite)
            input.mafRate >= MAF_IDLE_EXPECTED_MIN && boostDeviation < -BOOST_DEVIATION_MODERATE -> {
                when {
                    // Hohes Temp-Delta = Leck vor Ladeluftkuehler
                    input.chargeAirTemp - input.intakeTemp > TEMP_DELTA_NORMAL_MAX -> LeakLocation.CHARGE_PIPE
                    // Niedriges Temp-Delta = Leck nach Ladeluftkuehler
                    input.chargeAirTemp - input.intakeTemp < TEMP_DELTA_ANOMALOUS -> LeakLocation.INTAKE_MANIFOLD
                    else -> LeakLocation.CHARGE_PIPE
                }
            }

            // Wastegate-Kompensation aber Boost niedrig
            input.wastegateDuty < WG_DUTY_LOW_THRESHOLD && boostDeviation < -BOOST_DEVIATION_MINOR -> {
                LeakLocation.WASTEGATE_AREA
            }

            // Magerkorrektur deutet auf Ansaug-Leck hin
            totalTrim > 10.0 -> LeakLocation.INTAKE_MANIFOLD

            // Standard: Ladeluft-Schlauch (haeufigste Fehlerquelle)
            boostDeviation < -BOOST_DEVIATION_MINOR -> LeakLocation.CHARGE_PIPE

            else -> LeakLocation.UNKNOWN
        }
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        severity: LeakSeverity,
        location: LeakLocation,
        input: BoostLeakInput,
        indicators: List<String>
    ): String {
        return when (severity) {
            LeakSeverity.NONE -> {
                "Ladeluft-System dicht. Boost: ${"%.2f".format(input.boostActualBar)} bar " +
                        "(Soll: ${"%.2f".format(input.boostTargetBar)} bar). Keine Leck-Indikatoren."
            }
            LeakSeverity.MINOR -> {
                "Moegliches kleines Ladeluft-Leck erkannt. " +
                        "Bereich: ${location.label}. " +
                        "Boost-Abweichung: ${"%.1f".format(abs(input.boostActualBar - input.boostTargetBar) / input.boostTargetBar * 100.0)}%."
            }
            LeakSeverity.MODERATE -> {
                "Ladeluft-Leck erkannt (${indicators.size} Indikatoren). " +
                        "Wahrscheinliche Position: ${location.label} - ${location.description}. " +
                        "Boost: ${"%.2f".format(input.boostActualBar)} bar " +
                        "(Soll: ${"%.2f".format(input.boostTargetBar)} bar)."
            }
            LeakSeverity.SEVERE -> {
                "GROSSES Ladeluft-Leck! Boost nur ${"%.2f".format(input.boostActualBar)} bar " +
                        "statt ${"%.2f".format(input.boostTargetBar)} bar. " +
                        "Position: ${location.label}. " +
                        "${indicators.size} Indikatoren sprechen fuer ein massives Leck."
            }
            LeakSeverity.UNKNOWN -> {
                "Ladeluft-System nicht ausreichend analysierbar. " +
                        "Bitte bei hoeherer Last erneut messen."
            }
        }
    }

    /**
     * Generiert Empfehlung
     */
    private fun generateRecommendation(
        severity: LeakSeverity,
        location: LeakLocation,
        indicators: List<String>
    ): String {
        return when (severity) {
            LeakSeverity.NONE -> {
                "Keine Massnahmen erforderlich. Ladeluft-System ist dicht."
            }
            LeakSeverity.MINOR -> {
                "Ladeluft-Schlaeuche und -Schellen bei naechster Gelegenheit pruefen. " +
                        "Schwerpunkt: ${location.label}. " +
                        "Sichtpruefung auf Risse und lose Verbindungen."
            }
            LeakSeverity.MODERATE -> {
                "Ladeluft-System pruefen! ${location.label} untersuchen. " +
                        "Schlaeuche, Schellen und Dichtungen auf Beschädigung pruefen. " +
                        "Drucktest des Ladeluft-Systems empfohlen."
            }
            LeakSeverity.SEVERE -> {
                "SOFOERT Ladeluft-System pruefen! Grosses Leck bei ${location.label}. " +
                        "Weiterfahrt mit Leistungsverlust und moeglicher Turboschaedigung " +
                        "(Ueberdrehung). Werkstatt aufsuchen."
            }
            LeakSeverity.UNKNOWN -> {
                "Analyse bei hoeherer Motorlast wiederholen. " +
                        "Vollgas-Beschleunigung (3. Gang, 2000-5000 rpm) messen."
            }
        }
    }
}
