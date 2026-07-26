package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration

/**
 * Oelzustands-Monitor fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Das A14NET-Ölverwaltungssystem:
 * - Kapazitaet: 4.5 Liter (mit Filter)
 * - Empfohlenes Oel: Dexos2 5W-30 (Spezifikation GM Dexos2)
 * - Alternatives Oel: ACEA C3 5W-30 / A3/B4 5W-40
 * - Oelwechselintervall: 15.000 km (Dexos2-Anforderung)
 * - Normaler Verbrauch: bis zu 0.5L/1000km
 *
 * Oeltemperatur-Bereiche:
 * - Optimal: 90-110°C (bei normaler Fahrweise)
 * - Warnung: >115°C (bei hoher Last/hoher Aussentemperatur)
 * - Kritisch: >120°C (Sofort handeln)
 * - Kalter Start: <60°C (hoher Verschleiss)
 *
 * Oeldruck-Werte:
 * - Leerlauf (>800 rpm): min. 1.0 bar
 * - Betrieb (>2000 rpm): min. 2.0-2.5 bar
 * - Maximal: ~4.5-5.0 bar (kalt, hohe RPM)
 *
 * Der Oelzustand wird durch folgende Faktoren beeinflusst:
 * - Laufzeit und Laufleistung
 * - Betriebstemperatur (hoeher = schneller Alterung)
 * - Kurzstreckenbetrieb (hohe Feuchtigkeit im Oel)
 * - Kraftstoffanteil im Oel (bei Direkteinspritzung hoeher)
 */
class OilConditionMonitor(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Oelzustands-Status
     */
    enum class OilCondition(val label: String, val colorHex: Long, val severity: Int) {
        EXCELLENT("Ausgezeichnet", 0xFF00FF88, 0),
        GOOD("Gut", 0xFF88FF44, 0),
        FAIR("Befriedigend", 0xFFFFE066, 1),
        POOR("Schlecht", 0xFFFF8C00, 2),
        CRITICAL("Kritisch", 0xFFFF4444, 3),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * Eingabedaten fuer die Oelanalyse
     */
    data class OilInput(
        val oilTemp: Double,
        val oilPressure: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val speed: Double = 0.0,
        val totalKm: Double = 0.0,
        val lastOilChangeKm: Double = 0.0,
        val lastOilChangeTimestamp: Long = 0L,
        val engineRuntimeSec: Double = 0.0,
        val oilConsumptionLPer1000Km: Double = 0.0,
        val tripType: TripType = TripType.UNKNOWN
    )

    /**
     * Fahrten-Typ (fuer Oelverschmutzungs-Schaetzung)
     */
    enum class TripType(val label: String) {
        SHORT_DISTANCE("Kurzstrecke"), // <10km
        CITY("Stadtverkehr"), // 10-50km
        SUBURBAN("Vorstadt"), // 50-100km
        HIGHWAY("Autobahn"), // >100km
        UNKNOWN("Unbekannt")
    }

    /**
     * Ergebnis der Oelanalyse
     */
    data class OilAnalysis(
        val condition: OilCondition,
        val healthScore: Int,
        val oilLifeRemaining: Double,
        val remainingKm: Int,
        val remainingDays: Int,
        val temperatureHealth: Int,
        val pressureHealth: Int,
        val contaminationRisk: Int,
        val diagnosis: String,
        val recommendation: String,
        val oilType: String
    )

    companion object {
        // Oeltemperatur-Schwellenwerte (°C)
        private const val OIL_TEMP_OPTIMAL_MIN = 90.0
        private const val OIL_TEMP_OPTIMAL_MAX = 110.0
        private const val OIL_TEMP_WARNING = 115.0
        private const val OIL_TEMP_CRITICAL = 120.0
        private const val OIL_TEMP_COLD = 60.0

        // Oeldruck-Schwellenwerte (bar)
        private const val OIL_PRESSURE_IDLE_MIN = 1.0
        private const val OIL_PRESSURE_RPM_MIN = 2.0
        private const val OIL_PRESSURE_HIGH = 4.5

        // Oelwechsel-Intervalle (km und Tage)
        private const val OIL_CHANGE_INTERVAL_KM = 15000.0
        private const val OIL_CHANGE_INTERVAL_DAYS = 365
        private const val OIL_CHANGE_CRITICAL_KM = 20000.0

        // Oellebensdauer-Schaetzung
        private const val OIL_LIFE_BASE_KM = 15000.0
        private const val OIL_LIFE_BASE_DAYS = 365.0

        // Gewichtung (Summe = 100)
        private const val WEIGHT_TEMPERATURE = 25
        private const val WEIGHT_PRESSURE = 25
        private const val WEIGHT_LIFE = 30
        private const val WEIGHT_CONTAMINATION = 20
    }

    /**
     * Fuehrt eine vollstaendige Oelzustands-Analyse durch
     */
    fun analyze(input: OilInput): OilAnalysis {
        // 1. Temperatur-Gesundheit
        val tempHealth = evaluateTemperature(input.oilTemp, input.coolantTemp)

        // 2. Druck-Gesundheit
        val pressureHealth = evaluatePressure(input.oilPressure, input.rpm)

        // 3. Oellebensdauer
        val (lifeRemaining, remainingKm, remainingDays) = evaluateOilLife(
            input.totalKm, input.lastOilChangeKm, input.lastOilChangeTimestamp,
            input.oilTemp, input.tripType
        )

        // 4. Verunreinigungs-Risiko
        val contaminationRisk = evaluateContaminationRisk(
            input.totalKm, input.lastOilChangeKm, input.tripType,
            input.oilTemp, input.engineRuntimeSec
        )

        // Gesamtbewertung
        val rawScore = (tempHealth * WEIGHT_TEMPERATURE +
            pressureHealth * WEIGHT_PRESSURE +
            lifeRemaining.toInt() * WEIGHT_LIFE +
            (100 - contaminationRisk) * WEIGHT_CONTAMINATION) / 100

        val adjustedScore = rawScore.coerceIn(0, 100)

        val condition = determineCondition(adjustedScore, remainingKm, remainingDays)
        val diagnosis = generateDiagnosis(condition, input, tempHealth, pressureHealth)
        val recommendation = generateRecommendation(condition, input, remainingKm, remainingDays)

        return OilAnalysis(
            condition = condition,
            healthScore = adjustedScore,
            oilLifeRemaining = lifeRemaining,
            remainingKm = remainingKm,
            remainingDays = remainingDays,
            temperatureHealth = tempHealth,
            pressureHealth = pressureHealth,
            contaminationRisk = contaminationRisk,
            diagnosis = diagnosis,
            recommendation = recommendation,
            oilType = calibration.recommendedOil
        )
    }

    /**
     * Bewertet Oeltemperatur
     */
    @Suppress("UNUSED_PARAMETER")
    private fun evaluateTemperature(oilTemp: Double, coolantTemp: Double): Int {
        if (oilTemp <= 0) { return 50 } // Keine Daten

        return when {
            oilTemp > OIL_TEMP_CRITICAL -> 5
            oilTemp > OIL_TEMP_WARNING -> 25
            oilTemp > OIL_TEMP_OPTIMAL_MAX -> 60
            oilTemp in OIL_TEMP_OPTIMAL_MIN..OIL_TEMP_OPTIMAL_MAX -> 100
            oilTemp > OIL_TEMP_COLD -> 75
            oilTemp > 40 -> 60 // Aufwaermphase
            else -> 40 // Sehr kalt
        }
    }

    /**
     * Bewertet Oeldruck
     */
    private fun evaluatePressure(pressure: Double, rpm: Double): Int {
        if (pressure <= 0) { return 50 } // Keine Daten (kein Drucksensor)

        return when {
            // Kritisch niedrig
            rpm > 1000 && pressure < OIL_PRESSURE_IDLE_MIN * 0.5 -> 5
            // Niedrig im Leerlauf
            rpm < 1500 && pressure < OIL_PRESSURE_IDLE_MIN -> 25
            // Niedrig bei RPM
            rpm > 2000 && pressure < OIL_PRESSURE_RPM_MIN -> 30
            // Normal
            pressure in OIL_PRESSURE_IDLE_MIN..OIL_PRESSURE_HIGH -> 95
            // Zu hoch (moeglichstes Verstopfung oder Sensorfehler)
            pressure > OIL_PRESSURE_HIGH -> 60
            else -> 70
        }
    }

    /**
     * Berechnet verbleibende Oellebensdauer
     *
     * Beruecksichtigt:
     * - Laufleistung seit letztem Wechsel
     * - Zeit seit letztem Wechsel
     * - Betriebstemperatur (hoeher = schneller Abbau)
     * - Fahrprofil (Kurzstrecke = schlechter)
     */
    private fun evaluateOilLife(
        totalKm: Double,
        lastChangeKm: Double,
        lastChangeTimestamp: Long,
        oilTemp: Double,
        tripType: TripType
    ): Triple<Double, Int, Int> {
        val kmSinceChange = totalKm - lastChangeKm
        val daysSinceChange = if (lastChangeTimestamp > 0) {
            ((System.currentTimeMillis() - lastChangeTimestamp) / (1000 * 60 * 60 * 24)).toInt()
        } else { 0 }

        // Lebensdauer basierend auf Kilometern
        val kmLifePercent = ((OIL_LIFE_BASE_KM - kmSinceChange) / OIL_LIFE_BASE_KM * 100.0)

        // Lebensdauer basierend auf Zeit
        val timeLifePercent = ((OIL_LIFE_BASE_DAYS - daysSinceChange) / OIL_LIFE_BASE_DAYS * 100.0)

        // Temperatur-Faktor (hoeher = schneller Alterung)
        val tempFactor = when {
            oilTemp > OIL_TEMP_CRITICAL -> 0.67
            oilTemp > OIL_TEMP_WARNING -> 0.83
            oilTemp > OIL_TEMP_OPTIMAL_MAX -> 0.95
            else -> 1.0
        }

        // Fahrprofil-Faktor
        val tripFactor = when (tripType) {
            TripType.SHORT_DISTANCE -> 0.85 // Kurzstrecke belastet Oel mehr
            TripType.CITY -> 0.9
            TripType.SUBURBAN -> 1.0
            TripType.HIGHWAY -> 1.1 // Autobahn ist besser fuer Oel
            TripType.UNKNOWN -> 1.0
        }

        // Minimale Lebensdauer (basiert auf dem schlechteren Wert)
        val baseLife = minOf(kmLifePercent, timeLifePercent)
        val adjustedLife = (baseLife * tempFactor * tripFactor).coerceIn(0.0, 100.0)

        // Verbleibende Kilometer
        val remainingKm = (OIL_LIFE_BASE_KM - kmSinceChange).toInt().coerceAtLeast(0)

        // Verbleibende Tage
        val remainingDays = (OIL_LIFE_BASE_DAYS - daysSinceChange).toInt().coerceAtLeast(0)

        return Triple(adjustedLife, remainingKm, remainingDays)
    }

    /**
     * Bewertet Verunreinigungs-Risiko
     */
    private fun evaluateContaminationRisk(
        totalKm: Double,
        lastChangeKm: Double,
        tripType: TripType,
        oilTemp: Double,
        runtimeSec: Double
    ): Int {
        var risk = 0

        // Laufleistung seit Wechsel
        val kmSinceChange = totalKm - lastChangeKm
        when {
            kmSinceChange > OIL_CHANGE_CRITICAL_KM -> risk += 40
            kmSinceChange > OIL_CHANGE_INTERVAL_KM -> risk += 20
            kmSinceChange > OIL_CHANGE_INTERVAL_KM * 0.8 -> risk += 10
        }

        // Kurzstrecken-Fahrten (hoeherer Kraftstoffanteil im Oel)
        if (tripType == TripType.SHORT_DISTANCE) {
            risk += 15
        }

        // Hohe Betriebstemperatur (beschleunigte Alterung)
        if (oilTemp > OIL_TEMP_WARNING) {
            risk += 15
        }

        // Laufzeit-Belastung
        val hoursRunning = runtimeSec / 3600.0
        if (hoursRunning > 200) { risk += 5 }

        return risk.coerceIn(0, 100)
    }

    /**
     * Bestimmt Oelzustands-Status
     */
    private fun determineCondition(score: Int, remainingKm: Int, remainingDays: Int): OilCondition {
        return when {
            remainingKm <= 0 || remainingDays <= 0 -> OilCondition.CRITICAL
            score >= 85 -> OilCondition.EXCELLENT
            score >= 70 -> OilCondition.GOOD
            score >= 50 -> OilCondition.FAIR
            score >= 30 -> OilCondition.POOR
            else -> OilCondition.CRITICAL
        }
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        condition: OilCondition,
        input: OilInput,
        tempHealth: Int,
        pressureHealth: Int
    ): String {
        return when (condition) {
            OilCondition.EXCELLENT -> {
                "Oelzustand ausgezeichnet. Dexos2 5W-30 ist optimal geschuetzt."
            }
            OilCondition.GOOD -> {
                "Oelzustand gut. Regelmassiger Betrieb im normalen Temperaturbereich."
            }
            OilCondition.FAIR -> {
                val issues = mutableListOf<String>()
                if (tempHealth < 60) { issues.add("Oeltemperatur ${input.oilTemp.toInt()}°C") }
                if (pressureHealth < 60) { issues.add("Oeldruck pruefen") }
                val detail = if (issues.isNotEmpty()) { " - ${issues.joinToString(", ")}" } else { "" }
                "Oelzustand befriedigend.$detail Wechsel bald empfohlen."
            }
            OilCondition.POOR -> {
                "Oelzustand schlecht! Oelwechsel dringend empfohlen. " +
                    "Bei ${input.totalKm.toInt()} km: Kraftstoffverdunnung moeglich."
            }
            OilCondition.CRITICAL -> {
                "KRITISCH: Oelwechsel sofort erforderlich! " +
                    "Motorlauf ohne frisches Oel fuehrt zu Schaden."
            }
            OilCondition.UNKNOWN -> {
                "Oelzustand nicht bestimmbar. Oelstand und -qualitaet pruefen."
            }
        }
    }

    /**
     * Generiert Wartungsempfehlung
     */
    @Suppress("UNUSED_PARAMETER")
    private fun generateRecommendation(
        condition: OilCondition,
        input: OilInput,
        remainingKm: Int,
        remainingDays: Int
    ): String {
        return when (condition) {
            OilCondition.EXCELLENT, OilCondition.GOOD -> {
                val nextChange = when {
                    remainingKm < 3000 -> "in ca. $remainingKm km oder $remainingDays Tagen"
                    remainingKm < 5000 -> "bald (ca. $remainingKm km)"
                    else -> "bei ca. $remainingKm km Restlaufstrecke"
                }
                "Nächster Oelwechsel $nextChange. " +
                    "Verwenden Sie ausschliesslich ${calibration.recommendedOil}."
            }
            OilCondition.FAIR -> {
                "Oelwechsel empfohlen bei $remainingKm km Restlaufstrecke. " +
                    "Bitte ${calibration.recommendedOil} verwenden. " +
                    "Alternativ: ${calibration.alternativeOil}."
            }
            OilCondition.POOR -> {
                "SOFORT Oelwechsel durchfuehren! " +
                    "Nur ${calibration.recommendedOil} verwenden. " +
                    "Oelstand vor Fahrtantritt pruefen."
            }
            OilCondition.CRITICAL -> {
                "KRITISCH: SOFORT Oelwechsel! " +
                    "Motor nicht weiter betreiben ohne frisches Oel. " +
                    "${calibration.recommendedOil} ist vorgeschrieben. " +
                    "Oelstand bei jedem Start pruefen."
            }
            OilCondition.UNKNOWN -> {
                "Oelstand manuell pruefen. " +
                    "Bei naechstem Halt: ${calibration.recommendedOil} nachfuellen oder wechseln."
            }
        }
    }
}
