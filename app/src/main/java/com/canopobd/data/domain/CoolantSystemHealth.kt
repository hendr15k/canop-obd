package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs

/**
 * Kuehlmittel-System-Analyse fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Ueberwacht das Kuehlsystem des A14NET:
 * - Thermostat-Oeffnungs-Erkennung
 * - Wasserpumpen-Effizienz-Schaetzung
 * - Kuehlmittel-Leck-Erkennung ueber Temperaturmuster
 *
 * Kuehlsystem-Spezifikationen A14NET:
 * - Kuehlmittelkapazitaet: 5.7 Liter
 * - Kuehlmitteltyp: Dex-Cool (Orangefarben)
 * - Thermostat-Oeffnungstemperatur: ca. 82-88°C
 * - Voll geoffnet: ca. 100-105°C
 * - Kuehlmitteltemperatur-Warnung: 105°C
 * - Kuehlmitteltemperatur-Kritisch: 110°C
 * - Wasserpumpe: Mechanisch (Riemenantrieb)
 * - Typische Wasserpumpen-Lebensdauer: 80.000-150.000 km
 *
 * Diagnose-Ansaetze:
 * - Thermostat: Temperaturaenderungsrate beim Aufwaermen
 * - Wasserpumpe: Temperatur-Differenz Einlass/Auslass
 * - Leck: Plötzliche Temperaturspitzen, instabiler Betrieb
 */
class CoolantSystemHealth(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Gesundheitsstatus des Kuehlmittelsystems
     */
    enum class CoolantSystemStatus(val label: String, val colorHex: Long, val severity: Int) {
        HEALTHY("Gesund", 0xFF00FF88, 0),
        THERMOSTAT_SLIGHT("Thermostat leicht verzögert", 0xFFFFE066, 1),
        THERMOSTAT_STUCK("Thermostat klemmt", 0xFFFF8C00, 2),
        WATER_PUMP_WEAR("Wasserpumpe Verschleiss", 0xFFFFE066, 1),
        WATER_PUMP_FAIL("Wasserpumpe defekt", 0xFFFF4444, 3),
        LEAK_SUSPECTED("Kuehlmittelverlust vermutet", 0xFFFF8C00, 2),
        OVERHEATING("Ueberhitzung!", 0xFFFF4444, 4),
        UNKNOWN("Unbekannt", 0xFFAAAAAA, -1)
    }

    /**
     * Thermostat-Status
     */
    enum class ThermostatState(val label: String) {
        CLOSED("Geschlossen"),       // Kaltstart-Phase
        OPENING("Oeffnet"),          // Uebergangsphase
        OPEN("Geoeffnet"),           // Normalbetrieb
        STUCK_CLOSED("Klemmt zu"),   // Defekt - geschlossen
        STUCK_OPEN("Klemmt offen"),  // Defekt - offen
        UNKNOWN("Unbekannt")
    }

    /**
     * Eingabedaten fuer die Kuehlmittel-Analyse
     */
    data class CoolantInput(
        val coolantTemp: Double,
        val oilTemp: Double = 0.0,
        val intakeTemp: Double = 0.0,
        val rpm: Double = 0.0,
        val engineLoad: Double = 0.0,
        val speed: Double = 0.0,
        val engineRuntimeSec: Double = 0.0,
        val totalKm: Double = 0.0,
        val recentCoolantTemps: List<Double> = emptyList(),
        val recentRpm: List<Double> = emptyList(),
        val recentEngineLoad: List<Double> = emptyList(),
        val heaterOutputTemp: Double = 0.0,
        val radiatorInletTemp: Double = 0.0,
        val radiatorOutletTemp: Double = 0.0,
        val ambientTemp: Double = 20.0
    )

    /**
     * Ergebnis der Kuehlmittel-Analyse
     */
    data class CoolantAnalysis(
        val status: CoolantSystemStatus,
        val healthScore: Int,
        val thermostatState: ThermostatState,
        val thermostatOpeningTemp: Double,
        val waterPumpEfficiency: Int,
        val leakProbability: Int,
        val coolantTempStable: Boolean,
        val diagnosis: String,
        val recommendation: String,
        val detectedIssues: List<String>
    )

    companion object {
        // Thermostat-Kalibrierung (°C)
        private const val THERMOSTAT_START_OPEN = 82.0
        private const val THERMOSTAT_FULL_OPEN = 100.0
        private const val THERMOSTAT_NORMAL_RANGE_MIN = 85.0
        private const val THERMOSTAT_NORMAL_RANGE_MAX = 100.0

        // Temperaturschwellenwerte (°C)
        private const val COOLANT_TEMP_WARNING = 105.0
        private const val COOLANT_TEMP_CRITICAL = 110.0
        private const val COOLANT_TEMP_OVERHEAT = 115.0

        // Aufwaerm-Raten (°C pro Minute)
        private const val WARMUP_RATE_NORMAL_MIN = 5.0
        private const val WARMUP_RATE_NORMAL_MAX = 20.0
        private const val WARMUP_RATE_TOO_FAST = 25.0
        private const val WARMUP_RATE_TOO_SLOW = 2.0

        // Wasserpumpen-Effizienz
        private const val RADIATOR_DELTA_NORMAL_MIN = 5.0
        private const val RADIATOR_DELTA_NORMAL_MAX = 15.0
        private const val RADIATOR_DELTA_LOW = 3.0

        // Kuehlmittelverlust-Indikatoren
        private const val TEMP_INSTABILITY_THRESHOLD = 5.0
        private const val TEMP_SPIKE_THRESHOLD = 8.0

        // Gewichtung (Summe = 100)
        private const val WEIGHT_THERMOSTAT = 30
        private const val WEIGHT_WATER_PUMP = 30
        private const val WEIGHT_LEAK = 25
        private const val WEIGHT_TEMPERATURE = 15
    }

    /**
     * Fuehrt eine vollstaendige Kuehlmittel-Analyse durch
     */
    fun analyze(input: CoolantInput): CoolantAnalysis {
        val issues = mutableListOf<String>()

        // 1. Thermostat-Analyse
        val (thermostatState, thermostatScore, openingTemp) = analyzeThermostat(input, issues)

        // 2. Wasserpumpen-Effizienz
        val (pumpEfficiency, pumpScore) = analyzeWaterPump(input, issues)

        // 3. Leck-Erkennung
        val (leakProbability, leakScore) = analyzeLeak(input, issues)

        // 4. Temperatur-Stabilitaet
        val (isStable, tempScore) = analyzeTemperatureStability(input, issues)

        // Gesamtbewertung
        val rawScore = (thermostatScore * WEIGHT_THERMOSTAT +
                pumpScore * WEIGHT_WATER_PUMP +
                leakScore * WEIGHT_LEAK +
                tempScore * WEIGHT_TEMPERATURE) / 100

        val healthScore = rawScore.coerceIn(0, 100)

        // Ueberhitzung hat Vorrang
        val status = when {
            input.coolantTemp > COOLANT_TEMP_OVERHEAT -> CoolantSystemStatus.OVERHEATING
            input.coolantTemp > COOLANT_TEMP_CRITICAL -> CoolantSystemStatus.OVERHEATING
            healthScore >= 80 -> CoolantSystemStatus.HEALTHY
            healthScore >= 60 -> when {
                issues.any { it.contains("Thermostat") } -> CoolantSystemStatus.THERMOSTAT_SLIGHT
                issues.any { it.contains("Wasserpumpe") } -> CoolantSystemStatus.WATER_PUMP_WEAR
                else -> CoolantSystemStatus.THERMOSTAT_SLIGHT
            }
            healthScore >= 35 -> when {
                issues.any { it.contains("Thermostat klemmt") } -> CoolantSystemStatus.THERMOSTAT_STUCK
                issues.any { it.contains("Kuehlmittel") } -> CoolantSystemStatus.LEAK_SUSPECTED
                issues.any { it.contains("Wasserpumpe defekt") } -> CoolantSystemStatus.WATER_PUMP_FAIL
                else -> CoolantSystemStatus.THERMOSTAT_STUCK
            }
            else -> when {
                leakProbability > 50 -> CoolantSystemStatus.LEAK_SUSPECTED
                pumpEfficiency < 30 -> CoolantSystemStatus.WATER_PUMP_FAIL
                else -> CoolantSystemStatus.THERMOSTAT_STUCK
            }
        }

        val diagnosis = generateDiagnosis(status, input, thermostatState, pumpEfficiency, leakProbability)
        val recommendation = generateRecommendation(status, input)

        return CoolantAnalysis(
            status = status,
            healthScore = healthScore,
            thermostatState = thermostatState,
            thermostatOpeningTemp = openingTemp,
            waterPumpEfficiency = pumpEfficiency,
            leakProbability = leakProbability,
            coolantTempStable = isStable,
            diagnosis = diagnosis,
            recommendation = recommendation,
            detectedIssues = issues
        )
    }

    /**
     * Analysiert das Thermostat-Verhalten
     *
     * Gesundes Thermostat:
     * - Bleibt geschlossen bis ~82°C
     * - Oeffnet graduell bis ~100°C
     * - Haelt Temperatur stabil zwischen 85-100°C
     *
     * Fehlerbilder:
     * - Klemmt geschlossen: Temperatur steigt ueber 105°C bei Normallast
     * - Klemmt offen: Temperatur steigt sehr langsam, nie stabil
     */
    private fun analyzeThermostat(
        input: CoolantInput,
        issues: MutableList<String>
    ): Triple<ThermostatState, Int, Double> {
        // Thermostat-Position basierend auf Temperatur
        val state = when {
            input.coolantTemp < THERMOSTAT_START_OPEN -> ThermostatState.CLOSED
            input.coolantTemp in THERMOSTAT_START_OPEN..THERMOSTAT_FULL_OPEN -> ThermostatState.OPENING
            input.coolantTemp >= THERMOSTAT_FULL_OPEN -> ThermostatState.OPEN
            else -> ThermostatState.UNKNOWN
        }

        // Pruefe auf Thermostat-Fehler
        val score: Int
        val openingTemp: Double

        when {
            // Thermostat klemmt geschlossen: Temperatur steigt trotz Normalbetrieb
            input.coolantTemp > COOLANT_TEMP_WARNING && input.engineLoad < 60 && input.rpm < 3000 -> {
                issues.add("Thermostat klemmt geschlossen: ${input.coolantTemp.toInt()}°C bei niedriger Last")
                return Triple(ThermostatState.STUCK_CLOSED, 15, 0.0)
            }

            // Thermostat klemmt offen: Temperatur steigt sehr langsam
            input.engineRuntimeSec > 600 && input.coolantTemp < 70.0 -> {
                issues.add("Thermostat klemmt offen: Nach ${(input.engineRuntimeSec / 60).toInt()} Min nur ${input.coolantTemp.toInt()}°C")
                return Triple(ThermostatState.STUCK_OPEN, 25, 0.0)
            }

            // Temperatur nie erreicht (bei Kaelte moeglich)
            input.engineRuntimeSec > 300 && input.coolantTemp < THERMOSTAT_START_OPEN -> {
                score = 70
                openingTemp = THERMOSTAT_START_OPEN
            }

            // Normalbetrieb: Temperatur im Sollbereich
            input.coolantTemp in THERMOSTAT_NORMAL_RANGE_MIN..THERMOSTAT_NORMAL_RANGE_MAX -> {
                score = 100
                openingTemp = input.coolantTemp
            }

            // Leicht erhoeht
            input.coolantTemp in THERMOSTAT_NORMAL_RANGE_MAX..COOLANT_TEMP_WARNING -> {
                score = 70
                openingTemp = input.coolantTemp
                issues.add("Kuehlmitteltemperatur leicht erhoeht: ${input.coolantTemp.toInt()}°C")
            }

            // Ueberhitzung bei hoher Last (normal)
            input.coolantTemp > COOLANT_TEMP_WARNING && input.engineLoad > 70 -> {
                score = 55
                openingTemp = input.coolantTemp
                issues.add("Kuehlmitteltemperatur hoch bei ${input.engineLoad.toInt()}% Last")
            }

            else -> {
                score = 60
                openingTemp = input.coolantTemp
            }
        }

        // Pruefe Aufwaerm-Rate wenn Moeglich
        if (input.recentCoolantTemps.size >= 5 && input.coolantTemp < THERMOSTAT_FULL_OPEN) {
            val warmupRate = estimateWarmupRate(input.recentCoolantTemps)
            when {
                warmupRate > WARMUP_RATE_TOO_FAST -> {
                    issues.add("Aufwaerm-Rate sehr hoch (${"%.1f".format(warmupRate)} °C/min)")
                }
                warmupRate < WARMUP_RATE_TOO_SLOW && input.engineRuntimeSec > 180 -> {
                    issues.add("Aufwaerm-Rate sehr niedrig (${"%.1f".format(warmupRate)} °C/min)")
                }
            }
        }

        return Triple(state, score, openingTemp)
    }

    /**
     * Analysiert die Wasserpumpen-Effizienz
     *
     * Die Wasserpumpe ist riemengetrieben am A14NET.
     * Verschleiss zeigt sich durch:
     * - Geringere Temperatur-Differenz am Kuehler
     * - Hohe Temperatur bei hoeherer Drehzahl (Kuehlung sollte besser sein)
     * - Instabile Temperatur bei konstanter Last
     */
    private fun analyzeWaterPump(
        input: CoolantInput,
        issues: MutableList<String>
    ): Pair<Int, Int> {
        // Basis-Effizienz
        var efficiency = 85

        // Pruefe Kuehler-Differenz (falls verfuegbar)
        if (input.radiatorInletTemp > 0 && input.radiatorOutletTemp > 0) {
            val radiatorDelta = input.radiatorInletTemp - input.radiatorOutletTemp
            when {
                radiatorDelta < RADIATOR_DELTA_LOW -> {
                    efficiency = 20
                    issues.add("Wasserpumpe defekt: Kuehler-Delta nur ${"%.1f".format(radiatorDelta)}°C")
                }
                radiatorDelta < RADIATOR_DELTA_NORMAL_MIN -> {
                    efficiency = 45
                    issues.add("Wasserpumpe Verschleiss: Kuehler-Delta niedrig (${"%.1f".format(radiatorDelta)}°C)")
                }
                radiatorDelta in RADIATOR_DELTA_NORMAL_MIN..RADIATOR_DELTA_NORMAL_MAX -> {
                    efficiency = 90
                }
                radiatorDelta > RADIATOR_DELTA_NORMAL_MAX -> {
                    efficiency = 70
                }
            }
        } else {
            // Ohne direkte Kuehler-Daten: Indirekte Analyse

            // Bei hoeherer Drehzahl sollte die Pumpe mehr foerdern
            if (input.rpm > 2500 && input.coolantTemp > COOLANT_TEMP_WARNING) {
                efficiency -= 30
                issues.add("Wasserpumpe foerdert nicht ausreichend bei ${input.rpm.toInt()} rpm")
            }

            // Temperatur sollte bei steigender Drehzahl fallen oder stabil bleiben
            if (input.recentCoolantTemps.size >= 3 && input.recentRpm.size >= 3) {
                val tempTrend = calculateTrend(input.recentCoolantTemps)
                val rpmTrend = calculateTrend(input.recentRpm)
                if (rpmTrend > 0 && tempTrend > 3.0) {
                    efficiency -= 20
                    issues.add("Temperatur steigt trotz hoeherer Drehzahl")
                }
            }

            // Laufleistungs-Verschleiss
            if (input.totalKm > 150000) {
                efficiency -= 10
            } else if (input.totalKm > 100000) {
                efficiency -= 5
            }
        }

        val score = when {
            efficiency >= 80 -> 100
            efficiency >= 60 -> 70
            efficiency >= 40 -> 45
            efficiency >= 20 -> 25
            else -> 10
        }

        return efficiency.coerceIn(0, 100) to score
    }

    /**
     * Erkennt moeglichen Kuehlmittelverlust
     *
     * Indikatoren fuer Leck:
     * - Plötzliche Temperaturspitzen ohne Lastaenderung
     * - Temperatur instabil bei konstanter Fahrt
     * - Oeltemperatur-Kuehlmitteltemperatur-Differenz untypisch
     */
    private fun analyzeLeak(
        input: CoolantInput,
        issues: MutableList<String>
    ): Pair<Int, Int> {
        var leakProbability = 0

        // Temperatur-Instabilitaet bei konstanter Last
        if (input.recentCoolantTemps.size >= 5) {
            val tempVariance = calculateVariance(input.recentCoolantTemps)
            if (tempVariance > TEMP_INSTABILITY_THRESHOLD) {
                leakProbability += 25
                issues.add("Kuehlmittel-Temperatur instabil (Varianz: ${"%.1f".format(tempVariance)}°C)")
            }
        }

        // Plötzliche Temperaturspitze
        if (input.recentCoolantTemps.size >= 2) {
            val tempChange = abs(input.recentCoolantTemps.last() - input.recentCoolantTemps.elementAt(input.recentCoolantTemps.size - 2))
            if (tempChange > TEMP_SPIKE_THRESHOLD && input.engineLoad < 50) {
                leakProbability += 40
                issues.add("Kuehlmittel-Temperatur plötzliche Aenderung: ${"%.1f".format(tempChange)}°C")
            }
        }

        // Oel-Kuehlmittel-Temperatur-Differenz untypisch
        if (input.oilTemp > 80 && input.coolantTemp > 80) {
            val oilCoolantDiff = input.oilTemp - input.coolantTemp
            if (oilCoolantDiff > 25) {
                leakProbability += 20
                issues.add("Oel-Kuehlmittel-Differenz hoch: ${oilCoolantDiff.toInt()}°C")
            }
        }

        // Hohe Temperatur bei niedriger Aussentemperatur
        if (input.coolantTemp > COOLANT_TEMP_WARNING && input.ambientTemp < 20) {
            leakProbability += 15
            issues.add("Ueberhitzung bei niedriger Aussentemperatur (${input.ambientTemp.toInt()}°C)")
        }

        leakProbability = leakProbability.coerceIn(0, 100)

        val score = when {
            leakProbability < 10 -> 100
            leakProbability < 30 -> 70
            leakProbability < 60 -> 40
            else -> 15
        }

        return leakProbability to score
    }

    /**
     * Analysiert Temperatur-Stabilitaet
     */
    private fun analyzeTemperatureStability(
        input: CoolantInput,
        issues: MutableList<String>
    ): Pair<Boolean, Int> {
        if (input.recentCoolantTemps.size < 3) return true to 70

        val variance = calculateVariance(input.recentCoolantTemps)
        val isStable = variance < TEMP_INSTABILITY_THRESHOLD

        val score = when {
            variance < 2.0 -> 100
            variance < TEMP_INSTABILITY_THRESHOLD -> 80
            variance < 10.0 -> 50
            else -> {
                issues.add("Kuehlmitteltemperatur sehr instabil")
                20
            }
        }

        return isStable to score
    }

    /**
     * Generiert Diagnosemeldung
     */
    private fun generateDiagnosis(
        status: CoolantSystemStatus,
        input: CoolantInput,
        thermostatState: ThermostatState,
        pumpEfficiency: Int,
        leakProbability: Int
    ): String {
        return when (status) {
            CoolantSystemStatus.HEALTHY -> {
                "Kuehlsystem gesund. Kuehlmittel: ${input.coolantTemp.toInt()}°C, " +
                        "Thermostat: ${thermostatState.label}."
            }
            CoolantSystemStatus.THERMOSTAT_SLIGHT -> {
                "Thermostat leicht verzögert. Kuehlmittel: ${input.coolantTemp.toInt()}°C. " +
                        "Sollbereich: ${THERMOSTAT_NORMAL_RANGE_MIN.toInt()}-${THERMOSTAT_NORMAL_RANGE_MAX.toInt()}°C."
            }
            CoolantSystemStatus.THERMOSTAT_STUCK -> {
                "Thermostat defekt (${thermostatState.label})! " +
                        "Temperatur: ${input.coolantTemp.toInt()}°C. " +
                        "Kuehlleistung eingeschraenkt."
            }
            CoolantSystemStatus.WATER_PUMP_WEAR -> {
                "Wasserpumpe Verschleiss erkannt. Effizienz: ${pumpEfficiency}%. " +
                        "Temperatur: ${input.coolantTemp.toInt()}°C."
            }
            CoolantSystemStatus.WATER_PUMP_FAIL -> {
                "Wasserpumpe defekt! Effizienz: ${pumpEfficiency}%. " +
                        "Kuehlleistung massiv eingeschraenkt. " +
                        "Temperatur: ${input.coolantTemp.toInt()}°C."
            }
            CoolantSystemStatus.LEAK_SUSPECTED -> {
                "Kuehlmittelverlust vermutet! Leck-Wahrscheinlichkeit: ${leakProbability}%. " +
                        "Temperatur: ${input.coolantTemp.toInt()}°C."
            }
            CoolantSystemStatus.OVERHEATING -> {
                "UEBERHITZUNG! Kuehlmitteltemperatur: ${input.coolantTemp.toInt()}°C! " +
                        "SOFOERT anhalten und Motor abkuehlen lassen!"
            }
            CoolantSystemStatus.UNKNOWN -> {
                "Kuehlsystem nicht analysierbar."
            }
        }
    }

    /**
     * Generiert Empfehlung
     */
    private fun generateRecommendation(status: CoolantSystemStatus, input: CoolantInput): String {
        return when (status) {
            CoolantSystemStatus.HEALTHY -> {
                "Keine Massnahmen erforderlich. " +
                        "Kuehlmittelstand regelmaessig pruefen. " +
                        "Naechster Wechsel: ${calibration.coolantIntervalKm} km."
            }
            CoolantSystemStatus.THERMOSTAT_SLIGHT -> {
                "Thermostat beobachten. Bei naechster Wartung pruefen lassen. " +
                        "Kuehlmittelstand kontrollieren."
            }
            CoolantSystemStatus.THERMOSTAT_STUCK -> {
                "Thermostat ersetzen lassen! " +
                        "Weiterfahrt nur mit eingeschraenkter Belastung moeglich."
            }
            CoolantSystemStatus.WATER_PUMP_WEAR -> {
                "Wasserpumpe bei naechster Wartung pruefen. " +
                        "Bei ${input.totalKm.toInt()} km ist Verschleiss typisch. " +
                        "Kuehlmittelstand haeufig kontrollieren."
            }
            CoolantSystemStatus.WATER_PUMP_FAIL -> {
                "SOFORT Werkstatt! Wasserpumpe ersetzen. " +
                        "Nicht weiterfahren - Motorueberhitzung droht!"
            }
            CoolantSystemStatus.LEAK_SUSPECTED -> {
                "Kuehlsystem dringend pruefen! Kuehlmittelstand kontrollieren. " +
                        "Auf Pfuetzen unter dem Fahrzeug achten. " +
                        "Drucktest des Kuehlsystems empfohlen."
            }
            CoolantSystemStatus.OVERHEATING -> {
                "SOFOERT anhalten! Motor abkuehlen lassen (min. 30 Min). " +
                        "Nicht Kuehlmitteldeckel oeffnen bei heissem Motor! " +
                        "Abschleppen lassen - nicht weiterfahren!"
            }
            CoolantSystemStatus.UNKNOWN -> {
                "Kuehlmittelstand manuell pruefen. Weitere Daten sammeln."
            }
        }
    }

    /**
     * Hilfsfunktion: Schaetzt Aufwaerm-Rate (°C/min)
     */
    private fun estimateWarmupRate(recentTemps: List<Double>): Double {
        if (recentTemps.size < 2) return 0.0
        val totalChange = recentTemps.last() - recentTemps.first()
        val intervalMin = (recentTemps.size - 1) * 0.5 // Angenommen 30s Intervall
        return if (intervalMin > 0) totalChange / intervalMin else 0.0
    }

    /**
     * Hilfsfunktion: Berechnet Trend einer Werteliste
     */
    private fun calculateTrend(values: List<Double>): Double {
        if (values.size < 2) return 0.0
        return values.last() - values.first()
    }

    /**
     * Hilfsfunktion: Berechnet Varianz einer Werteliste
     */
    private fun calculateVariance(values: List<Double>): Double {
        if (values.size < 2) return 0.0
        val avg = values.average()
        return values.sumOf { (it - avg) * (it - avg) } / values.size
    }

    /**
     * Hilfsfunktion: Erkennt Thermostat-Oeffnung basierend auf Temperaturverlauf
     */
    fun detectThermostatOpening(tempHistory: List<Double>): Int? {
        if (tempHistory.size < 5) return null

        for (i in 4 until tempHistory.size) {
            val window = tempHistory.subList(i - 4, i + 1)
            val rate = (window.last() - window.first()) / 4.0
            if (tempHistory[i] in THERMOSTAT_START_OPEN..THERMOSTAT_FULL_OPEN && rate < 0.5) {
                return i
            }
        }
        return null
    }
}
