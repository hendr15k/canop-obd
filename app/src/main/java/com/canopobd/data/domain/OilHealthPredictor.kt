package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.ln

/**
 * Oelgesundheits-Vorhersage fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Erweiterte Oelzustandsanalyse ueber OilConditionMonitor hinaus.
 * Dieser Analyzer verwendet ein thermisches Belastungsmodell und
 * Fahrprofil-Analyse zur Vorhersage des Oelzustands.
 *
 * Analyse-Grundlagen:
 * - Zeit ueber Temperaturschwellenwerten (kumulativ)
 * - Thermisches Belastungsmodell basierend auf Arrhenius-Gleichung
 * - Oeldegradation-Modell basierend auf thermischem Stress
 * - Oelwechsel-Empfehlung basierend auf Fahrprofil
 *
 * A14NET Oel-Spezifikationen:
 * - Kapazitaet: 4.5 Liter (mit Filter)
 * - Empfohlen: Dexos2 5W-30 (GM Dexos2 Spezifikation)
 * - Alternativ: ACEA C3 5W-30 / A3/B4 5W-40
 * - Wechselintervall: 15.000 km (Normal)
 * - Reduziertes Intervall: 10.000 km (Sport/Schaerfe)
 * - Oelverbrauch: max 0.5L/1000 km (Normalwert)
 */
class OilHealthPredictor(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Vorhersage-Status fuer Oelgesundheit
     */
    enum class OilHealthPrediction(val label: String, val colorHex: Long, val severity: Int) {
        HEALTHY("Gesund", 0xFF00FF88, 0),
        MODERATE_STRESS("Mittlere Belastung", 0xFF88FF44, 1),
        HIGH_STRESS("Hohe Belastung", 0xFFFFE066, 2),
        DEGRADED("Verschlechtert", 0xFFFF8C00, 3),
        CRITICAL("Kritisch", 0xFFFF4444, 4),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * Fahrprofil-Kategorie fuer Oelbelastung
     */
    enum class DrivingPattern(val label: String, val thermalLoadFactor: Double) {
        EASY("Gemuetlich", 0.7),
        NORMAL("Normal", 1.0),
        SPORTY("Sportlich", 1.5),
        TRACK("Rennstrecke", 2.5),
        SHORT_TRIP("Kurzstrecke", 1.8),
        TOWING("Anhaengerlast", 2.0),
        UNKNOWN("Unbekannt", 1.0)
    }

    /**
     * Eingabedaten fuer die Oelgesundheits-Vorhersage
     */
    data class OilHealthInput(
        val oilTemp: Double,
        val coolantTemp: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val boostPressureKpa: Double = 0.0,
        val totalKm: Double = 0.0,
        val lastOilChangeKm: Double = 0.0,
        val lastOilChangeTimestamp: Long = 0L,
        val engineRuntimeSec: Double = 0.0,
        val drivingPattern: DrivingPattern = DrivingPattern.UNKNOWN,
        // Durations are supplied in seconds and converted to hours below.
        val timeAbove110C: Double = 0.0,
        val timeAbove115C: Double = 0.0,
        val timeAbove120C: Double = 0.0,
        val shortTripCount: Int = 0,
        val oilConsumptionLPer1000Km: Double = 0.0
    )

    /**
     * Ergebnis der Oelgesundheits-Vorhersage
     */
    data class OilHealthPredictionResult(
        val prediction: OilHealthPrediction,
        val healthScore: Int,
        val thermalStressIndex: Double,
        val degradationPercent: Double,
        val effectiveOilAgeKm: Double,
        val kmSinceOilChange: Double,
        val recommendedChangeKm: Int,
        val recommendedChangeDays: Int,
        val thermalLoadScore: Int,
        val drivingPatternScore: Int,
        val consumptionScore: Int,
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

        // Thermischer Belastungsindex-Grenzwerte
        private const val THERMAL_STRESS_LOW = 0.3
        private const val THERMAL_STRESS_MODERATE = 0.5
        private const val THERMAL_STRESS_HIGH = 0.7
        private const val THERMAL_STRESS_CRITICAL = 0.85

        // Arrhenius-Aktivierungskonstante fuer Oel-Oxidation
        private const val ARRHENIUS_FACTOR = 0.04

        // Degradations-Raten
        private const val DEGRADATION_RATE_PER_1000KM = 6.67
        private const val DEGRADATION_RATE_PER_HOUR_OVER_110 = 2.0
        private const val DEGRADATION_RATE_PER_HOUR_OVER_120 = 8.0

        // Oelwechsel-Intervalle (km)
        private const val OIL_CHANGE_NORMAL_KM = 15000.0
        private const val OIL_CHANGE_SEVERE_KM = 10000.0
        private const val OIL_CHANGE_TRACK_KM = 5000.0
        private const val OIL_CHANGE_MAX_DAYS = 365

        // Oelverbrauch-Schwellenwerte (L/1000 km)
        private const val OIL_CONSUMPTION_NORMAL = 0.3
        private const val OIL_CONSUMPTION_WARNING = 0.5
        private const val OIL_CONSUMPTION_CRITICAL = 1.0

        // Gewichtung (Summe = 100)
        private const val WEIGHT_THERMAL = 30
        private const val WEIGHT_DRIVING = 25
        private const val WEIGHT_CONSUMPTION = 20
        private const val WEIGHT_LIFE = 25
    }

    /**
     * Fuehrt eine vollstaendige Oelgesundheits-Vorhersage durch
     */
    fun analyze(input: OilHealthInput): OilHealthPredictionResult {
        // 1. Thermischer Belastungsindex
        val thermalStress = calculateThermalStressIndex(input)
        val thermalScore = evaluateThermalLoad(thermalStress, input)

        // 2. Fahrprofil-Bewertung
        val drivingScore = evaluateDrivingPattern(input)

        // 3. Oelverbrauch-Bewertung
        val consumptionScore = evaluateOilConsumption(input)

        // 4. Oellebensdauer
        val (effectiveAgeKm, degradation, recommendedKm, recommendedDays) = calculateDegradation(input)

        // Gesamtbewertung
        val rawScore = (thermalScore * WEIGHT_THERMAL +
            drivingScore * WEIGHT_DRIVING +
            consumptionScore * WEIGHT_CONSUMPTION +
            (100 - degradation.toInt().coerceAtMost(100)) * WEIGHT_LIFE) / 100

        val healthScore = rawScore.coerceIn(0, 100)

        val prediction = determinePrediction(healthScore, thermalStress, input)
        val diagnosis = generateDiagnosis(prediction, input, thermalStress, degradation)
        val recommendation = generateRecommendation(prediction, input, recommendedKm, recommendedDays)

        return OilHealthPredictionResult(
            prediction = prediction,
            healthScore = healthScore,
            thermalStressIndex = thermalStress,
            degradationPercent = degradation,
            effectiveOilAgeKm = effectiveAgeKm,
            kmSinceOilChange = input.totalKm - input.lastOilChangeKm,
            recommendedChangeKm = recommendedKm,
            recommendedChangeDays = recommendedDays,
            thermalLoadScore = thermalScore,
            drivingPatternScore = drivingScore,
            consumptionScore = consumptionScore,
            diagnosis = diagnosis,
            recommendation = recommendation,
            oilType = calibration.recommendedOil
        )
    }

    /**
     * Berechnet den thermischen Belastungsindex
     *
     * Basiert auf der Arrhenius-Gleichung fuer chemische Reaktionsraten:
     * Die Oel-Oxidationsrate verdoppelt sich etwa alle 10°C ueber 100°C.
     *
     * Faktoren:
     * - Aktuelle Oeltemperatur
     * - Kumulative Zeit ueber Schwellenwerten
     * - Motorlast (hoeher = mehr Waerme)
     */
    private fun calculateThermalStressIndex(input: OilHealthInput): Double {
        var stressIndex = 0.0

        // Aktuelle Temperatur-Belastung
        if (input.oilTemp > OIL_TEMP_OPTIMAL_MAX) {
            val tempExcess = input.oilTemp - OIL_TEMP_OPTIMAL_MAX
            stressIndex += tempExcess * ARRHENIUS_FACTOR
        }

        // Kumulative Belastung ueber 110°C (in Stunden)
        val hoursOver110 = input.timeAbove110C / 3600.0
        stressIndex += hoursOver110 * DEGRADATION_RATE_PER_HOUR_OVER_110 / 100.0

        // Kumulative Belastung ueber 115°C (in Stunden)
        val hoursOver115 = input.timeAbove115C / 3600.0
        stressIndex += hoursOver115 * DEGRADATION_RATE_PER_HOUR_OVER_110 * 1.5 / 100.0

        // Kumulative Belastung ueber 120°C (in Stunden)
        val hoursOver120 = input.timeAbove120C / 3600.0
        stressIndex += hoursOver120 * DEGRADATION_RATE_PER_HOUR_OVER_120 / 100.0

        // Lastfaktor
        if (input.engineLoad > 80) {
            stressIndex *= 1.3
        } else if (input.engineLoad > 60) {
            stressIndex *= 1.1
        }

        // Boost-Faktor (hoeherer Boost = mehr Waerme)
        val boostBar = input.boostPressureKpa / 100.0
        if (boostBar > calibration.normalBoostTargetBar) {
            stressIndex *= 1.2
        }

        return stressIndex.coerceIn(0.0, 1.0)
    }

    /**
     * Bewertet die thermische Belastung
     */
    private fun evaluateThermalLoad(stressIndex: Double, input: OilHealthInput): Int {
        // Basis-Score vom thermischen Belastungsindex
        val baseScore = when {
            stressIndex < THERMAL_STRESS_LOW -> 100
            stressIndex < THERMAL_STRESS_MODERATE -> 75
            stressIndex < THERMAL_STRESS_HIGH -> 50
            stressIndex < THERMAL_STRESS_CRITICAL -> 25
            else -> 10
        }

        // Zusaetzliche Strafe fuer aktuelle Ubertemperatur
        val currentTempPenalty = when {
            input.oilTemp > OIL_TEMP_CRITICAL -> -20
            input.oilTemp > OIL_TEMP_WARNING -> -10
            else -> 0
        }

        return (baseScore + currentTempPenalty).coerceIn(0, 100)
    }

    /**
     * Bewertet das Fahrprofil
     */
    private fun evaluateDrivingPattern(input: OilHealthInput): Int {
        val patternScore = when (input.drivingPattern) {
            DrivingPattern.EASY -> 100
            DrivingPattern.NORMAL -> 85
            DrivingPattern.SPORTY -> 55
            DrivingPattern.TRACK -> 25
            DrivingPattern.SHORT_TRIP -> 40
            DrivingPattern.TOWING -> 35
            DrivingPattern.UNKNOWN -> 70
        }

        // Kurzstrecken-Strafe (jede Kurzstrecke fuegt Feuchtigkeit in Oel ein)
        val shortTripPenalty = (input.shortTripCount * 2).coerceAtMost(20)

        return (patternScore - shortTripPenalty).coerceIn(0, 100)
    }

    /**
     * Bewertet Oelverbrauch
     */
    private fun evaluateOilConsumption(input: OilHealthInput): Int {
        val consumption = input.oilConsumptionLPer1000Km
        if (consumption <= 0) { return 70 } // Keine Daten

        return when {
            consumption <= OIL_CONSUMPTION_NORMAL -> 100
            consumption <= OIL_CONSUMPTION_WARNING -> 70
            consumption <= OIL_CONSUMPTION_CRITICAL -> 35
            else -> 10
        }
    }

    /**
     * Berechnet die Oeldegradation und effektive Oelalterung
     *
     * Das Degradationsmodell beruecksichtigt:
     * - Laufleistung seit letztem Wechsel
     * - Thermische Belastung (Arrhenius-Modell)
     * - Fahrprofil-Faktor
     * - Kurzstrecken-Belastung
     */
    private fun calculateDegradation(input: OilHealthInput): DegradationResult {
        val kmSinceChange = (input.totalKm - input.lastOilChangeKm).coerceAtLeast(0.0)

        // Basis-Degradation durch Laufleistung
        val mileageDegradation = (kmSinceChange / OIL_CHANGE_NORMAL_KM) * 100.0

        // Fahrprofil-Faktor
        val drivingFactor = input.drivingPattern.thermalLoadFactor

        // Kurzstrecken-Faktor (mehr Kurzstrecken = schnellere Degradation)
        val shortTripFactor = 1.0 + (input.shortTripCount * 0.02)

        // Thermischer Zusatzfaktor
        val thermalAddition = input.timeAbove110C / 3600.0 * DEGRADATION_RATE_PER_HOUR_OVER_110

        // Effektive Kilometer
        val effectiveKm = kmSinceChange * drivingFactor * shortTripFactor + thermalAddition * 1000.0

        // Gesamte Degradation
        val totalDegradation = (mileageDegradation + thermalAddition)
            .coerceIn(0.0, 100.0)

        // Empfohlenes Wechselintervall basierend auf Fahrprofil
        val recommendedKm = when (input.drivingPattern) {
            DrivingPattern.EASY -> OIL_CHANGE_NORMAL_KM.toInt()
            DrivingPattern.NORMAL -> OIL_CHANGE_NORMAL_KM.toInt()
            DrivingPattern.SPORTY -> OIL_CHANGE_SEVERE_KM.toInt()
            DrivingPattern.TRACK -> OIL_CHANGE_TRACK_KM.toInt()
            DrivingPattern.SHORT_TRIP -> OIL_CHANGE_SEVERE_KM.toInt()
            DrivingPattern.TOWING -> OIL_CHANGE_SEVERE_KM.toInt()
            DrivingPattern.UNKNOWN -> OIL_CHANGE_NORMAL_KM.toInt()
        }

        // Empfohlene Wechsel-Tage
        val recommendedDays = when (input.drivingPattern) {
            DrivingPattern.TRACK -> 180
            DrivingPattern.SHORT_TRIP -> 270
            else -> OIL_CHANGE_MAX_DAYS
        }

        return DegradationResult(effectiveKm, totalDegradation, recommendedKm, recommendedDays)
    }

    private data class DegradationResult(
        val effectiveKm: Double,
        val degradation: Double,
        val recommendedKm: Int,
        val recommendedDays: Int
    )

    /**
     * Bestimmt den Vorhersage-Status
     */
    private fun determinePrediction(
        score: Int,
        thermalStress: Double,
        input: OilHealthInput
    ): OilHealthPrediction {
        // Sofort kritisch bei ueberkritischer Temperatur
        if (input.oilTemp > OIL_TEMP_CRITICAL + 5) { return OilHealthPrediction.CRITICAL }

        // Kritisch bei hohem Oelverbrauch
        if (input.oilConsumptionLPer1000Km > OIL_CONSUMPTION_CRITICAL) { return OilHealthPrediction.CRITICAL }

        return when {
            thermalStress >= THERMAL_STRESS_CRITICAL && score < 30 -> OilHealthPrediction.CRITICAL
            score >= 80 -> OilHealthPrediction.HEALTHY
            score >= 65 -> OilHealthPrediction.MODERATE_STRESS
            score >= 45 -> OilHealthPrediction.HIGH_STRESS
            score >= 25 -> OilHealthPrediction.DEGRADED
            else -> OilHealthPrediction.CRITICAL
        }
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        prediction: OilHealthPrediction,
        input: OilHealthInput,
        thermalStress: Double,
        degradation: Double
    ): String {
        val kmSinceChange = input.totalKm - input.lastOilChangeKm
        return when (prediction) {
            OilHealthPrediction.HEALTHY -> {
                "Oel gesund. Temperatur: ${input.oilTemp.toInt()}°C, " +
                    "thermische Belastung: ${"%.0f".format(thermalStress * 100)}%. " +
                    "${kmSinceChange.toInt()} km seit letztem Wechsel."
            }
            OilHealthPrediction.MODERATE_STRESS -> {
                "Oel mittlere Belastung. Temperatur: ${input.oilTemp.toInt()}°C, " +
                    "Degradation: ${"%.1f".format(degradation)}%. " +
                    "Fahrprofil: ${input.drivingPattern.label}."
            }
            OilHealthPrediction.HIGH_STRESS -> {
                "Oel hohe Belastung! Temperatur: ${input.oilTemp.toInt()}°C, " +
                    "Degradation: ${"%.1f".format(degradation)}%. " +
                    "Thermischer Stress-Index: ${"%.0f".format(thermalStress * 100)}%. " +
                    "Oelwechsel vor ruecken."
            }
            OilHealthPrediction.DEGRADED -> {
                "Oel verschlechtert! Degradation: ${"%.1f".format(degradation)}%. " +
                    "Oelverbrauch: ${"%.2f".format(input.oilConsumptionLPer1000Km)} L/1000 km. " +
                    "Oelwechsel dringend empfohlen."
            }
            OilHealthPrediction.CRITICAL -> {
                "KRITISCH: Oel maximal belastet! " +
                    "Temperatur: ${input.oilTemp.toInt()}°C, Degradation: ${"%.1f".format(degradation)}%. " +
                    "Sofort Oelwechsel erforderlich!"
            }
            OilHealthPrediction.UNKNOWN -> {
                "Oelzustand nicht bestimmbar. Oelstand manuell pruefen."
            }
        }
    }

    /**
     * Generiert Empfehlung
     */
    private fun generateRecommendation(
        prediction: OilHealthPrediction,
        input: OilHealthInput,
        recommendedKm: Int,
        recommendedDays: Int
    ): String {
        val kmSinceChange = input.totalKm - input.lastOilChangeKm
        val kmRemaining = (recommendedKm - kmSinceChange).toInt().coerceAtLeast(0)

        return when (prediction) {
            OilHealthPrediction.HEALTHY -> {
                "Oelwechsel in ca. $kmRemaining km oder $recommendedDays Tagen. " +
                    "Oel: ${calibration.recommendedOil}."
            }
            OilHealthPrediction.MODERATE_STRESS -> {
                "Oelwechsel in $kmRemaining km empfohlen. " +
                    "Thermische Belastung minimieren (Schongang fahren). " +
                    "Oel: ${calibration.recommendedOil}."
            }
            OilHealthPrediction.HIGH_STRESS -> {
                "Oelwechsel bald durchfuehren (max. $kmRemaining km). " +
                    "Hohe Temperaturen vermeiden. " +
                    "Alternativ: ${calibration.alternativeOil} fuer besseren Schutz."
            }
            OilHealthPrediction.DEGRADED -> {
                "Oelwechsel SOFORT durchfuehren! " +
                    "Nur ${calibration.recommendedOil} verwenden. " +
                    "Oelfuellmenge: ${calibration.oilCapacityLiters} L. " +
                    "Oelstand vor Weiterfahrt pruefen."
            }
            OilHealthPrediction.CRITICAL -> {
                "KRITISCH: SOFORT Oelwechsel! Motor nicht weiter belasten! " +
                    "Oel: ${calibration.recommendedOil}, Menge: ${calibration.oilCapacityLiters} L. " +
                    "Oelfilter mitwechseln! Oelverbrauch: ${"%.2f".format(input.oilConsumptionLPer1000Km)} L/1000 km."
            }
            OilHealthPrediction.UNKNOWN -> {
                "Oelstand pruefen und Oelwechsel bei Bedarf durchfuehren. " +
                    "Oel: ${calibration.recommendedOil}."
            }
        }
    }

    /**
     * Hilfsfunktion: Berechnet die effektive Oeltemperatur-Gefahr
     * basierend auf der Arrhenius-Gleichung
     */
    fun calculateArrheniusRisk(oilTempCelsius: Double): Double {
        val referenceTemp = 100.0
        if (oilTempCelsius <= referenceTemp) { return 0.0 }
        val tempRise = oilTempCelsius - referenceTemp
        return (ln(2.0) * tempRise / 10.0).coerceIn(0.0, 10.0)
    }

    /**
     * Hilfsfunktion: Bestimmt ob Oelwechsel faellig ist
     */
    fun isOilChangeDue(input: OilHealthInput): Boolean {
        val kmSinceChange = input.totalKm - input.lastOilChangeKm
        val daysSinceChange = if (input.lastOilChangeTimestamp > 0) {
            ((System.currentTimeMillis() - input.lastOilChangeTimestamp) / (1000 * 60 * 60 * 24)).toInt()
        } else { 0 }

        return kmSinceChange >= OIL_CHANGE_SEVERE_KM ||
            daysSinceChange >= OIL_CHANGE_MAX_DAYS ||
            input.oilTemp > OIL_TEMP_CRITICAL
    }

    /**
     * Hilfsfunktion: Berechnet das angepasste Wechselintervall
     */
    fun getAdjustedInterval(pattern: DrivingPattern): Int {
        return when (pattern) {
            DrivingPattern.EASY -> OIL_CHANGE_NORMAL_KM.toInt()
            DrivingPattern.NORMAL -> OIL_CHANGE_NORMAL_KM.toInt()
            DrivingPattern.SPORTY -> OIL_CHANGE_SEVERE_KM.toInt()
            DrivingPattern.TRACK -> OIL_CHANGE_TRACK_KM.toInt()
            DrivingPattern.SHORT_TRIP -> OIL_CHANGE_SEVERE_KM.toInt()
            DrivingPattern.TOWING -> OIL_CHANGE_SEVERE_KM.toInt()
            DrivingPattern.UNKNOWN -> OIL_CHANGE_NORMAL_KM.toInt()
        }
    }
}
