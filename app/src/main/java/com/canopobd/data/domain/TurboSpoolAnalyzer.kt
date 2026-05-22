package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Turbo-Spule-Analyse fuer Opel Astra J 1.4 Turbo (A14NET)
 *
 * Misst die Turbo-Responszeit und analysiert das Spule-Verhalten:
 * - Zeit von der Gaseinlegung bis 80% Boost
 * - Turbo-Lag-Messung
 * - Spule-Up-Zeit-Trend
 * - Vergleich zur Basislinie (2 Sekunden neuer Turbo)
 *
 * Technische Hintergrund:
 * Der BorgWarner KP39 ist ein Fixed-Geometry-Turbo mit pneumatischem Wastegate.
 * Typisches Spuleverhalten:
 * - Neu/optimal: 1.2-2.0 Sekunden bis 80% Boost
 * - Erschlaegelt: 2.5-4.0 Sekunden
 * - Kritisch: > 4.0 Sekunden
 *
 * Kritische Faktoren:
 * - Wastegate-Duty im Leerlauf (sollte 80-95% sein)
 * - Turbo-Drehzahl Aufbau (0-80% innerhalb 1.5 Sekunden)
 * - Boost-Druck Aufbau (0-70 kPa in 2 Sekunden)
 */
class TurboSpoolAnalyzer(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Turbo-Spule-Status
     */
    enum class SpoolStatus(val label: String, val colorHex: Long, val severity: Int) {
        OPTIMAL("Optimal", 0xFF00FF88, 0),
        GOOD("Gut", 0xFF88FF44, 0),
        SLOW_SPOOL("Langsame Spule", 0xFFFFE066, 1),
        POOR("Schlecht", 0xFFFF8C00, 2),
        CRITICAL("Kritisch", 0xFFFF4444, 3),
        INSUFFICIENT_DATA("Unvollstaendig", 0xFFAAAAAA, -1)
    }

    /**
     * Eingabedaten fuer die Spule-Analyse
     */
    data class SpoolInput(
        val throttleApplication: Double,
        val boostAtThrottleApplication: Double,
        val boostAt80Percent: Double,
        val targetBoostAt80: Double,
        val spoolTimeSeconds: Double,
        val wastegateDutyAtSpool: Double,
        val wastegateDutyIdle: Double,
        val turboRpmAtSpool: Double,
        val rpmAtThrottleApplication: Double,
        val rpmAt80PercentBoost: Double,
        val engineLoad: Double = 0.0,
        val intakeTemp: Double = 0.0,
        val boostPressureKpa: Double = 0.0
    )

    /**
     * Ergebnis der Spule-Analyse
     */
    data class SpoolAnalysis(
        val status: SpoolStatus,
        val healthScore: Int,
        val spoolTimeSeconds: Double,
        val expectedSpoolTime: Double,
        val spoolDeviation: Double,
        val wastegateResponse: Double,
        val turboAcceleration: Double,
        val diagnosis: String,
        val recommendation: String,
        val trendIndicator: SpoolTrend
    )

    /**
     * Spule-Trend (fuer Langzeitanalyse)
     */
    enum class SpoolTrend(val label: String) {
        IMPROVING("Besser werdend"),
        STABLE("Stabil"),
        DEGRADING("Verschlechtend"),
        UNKNOWN("Unbekannt")
    }

    companion object {
        // Spule-Zeit-Schwellenwerte (Sekunden)
        private const val SPOOL_OPTIMAL_MAX = 2.0
        private const val SPOOL_GOOD_MAX = 2.5
        private const val SPOOL_SLOW_MAX = 3.5
        private const val SPOOL_CRITICAL_MAX = 4.0

        // Erwartete Spule-Zeit bei Neuzustand (Sekunden)
        private const val NEW_TURBO_SPOOL_TIME = 1.5

        // Wastegate-Duty-Schwellenwerte
        private const val WG_DUTY_IDLE_MIN = 80.0
        private const val WG_DUTY_IDLE_MAX = 95.0
        private const val WG_DUTY_SPOOL_MAX = 60.0

        // Turbo-Drehzahl-Acceleration (rpm/s)
        private const val TURBO_ACCEL_OPTIMAL = 50000.0
        private const val TURBO_ACCEL_GOOD = 35000.0
        private const val TURBO_ACCEL_SLOW = 20000.0

        // Boost-Druck-Schwellenwerte (kPa)
        private const val BOOST_80_KPA = 80.0
        private const val BOOST_FULL_KPA = 100.0

        // Gewichtung
        private const val WEIGHT_SPOOL_TIME = 40
        private const val WEIGHT_WG_RESPONSE = 25
        private const val WEIGHT_TURBO_ACCEL = 25
        private const val WEIGHT_LOAD_FACTOR = 10
    }

    /**
     * Fuehrt eine vollstaendige Turbo-Spule-Analyse durch
     */
    fun analyze(input: SpoolInput): SpoolAnalysis {
        val spoolTime = input.spoolTimeSeconds
        val expectedTime = calculateExpectedSpoolTime(input)
        val spoolDeviation = if (expectedTime > 0) {
            ((spoolTime - expectedTime) / expectedTime) * 100.0
        } else 0.0

        // 1. Wastegate-Response-Zeit
        val wgResponse = evaluateWastegateResponse(input)

        // 2. Turbo-Acceleration
        val turboAccel = evaluateTurboAcceleration(input)

        // 3. Spule-Status bestimmen
        val status = determineSpoolStatus(spoolTime, wgResponse, turboAccel, input)

        // 4. Gesundheits-Score
        val healthScore = calculateHealthScore(spoolTime, wgResponse, turboAccel, input)

        // 5. Trend-Indikator (basisierend auf Abweichung)
        val trend = determineTrend(spoolDeviation)

        val diagnosis = generateDiagnosis(status, spoolTime, expectedTime, input)
        val recommendation = generateRecommendation(status, input)

        return SpoolAnalysis(
            status = status,
            healthScore = healthScore,
            spoolTimeSeconds = spoolTime,
            expectedSpoolTime = expectedTime,
            spoolDeviation = spoolDeviation,
            wastegateResponse = wgResponse,
            turboAcceleration = turboAccel,
            diagnosis = diagnosis,
            recommendation = recommendation,
            trendIndicator = trend
        )
    }

    /**
     * Berechnet die erwartete Spule-Zeit basierend auf Last und Bedingungen
     */
    private fun calculateExpectedSpoolTime(input: SpoolInput): Double {
        // Basis-Zeit fuer neuen Turbo
        var expectedTime = NEW_TURBO_SPOOL_TIME

        // Last-Faktor: Hohe Last = laengerer Spule noenotig
        if (input.engineLoad > 70) {
            expectedTime *= 1.3
        } else if (input.engineLoad > 50) {
            expectedTime *= 1.1
        }

        // Kaeltefaktor: Kalter Motor = langsamer Spule
        if (input.intakeTemp < 20) {
            expectedTime *= 1.4
        } else if (input.intakeTemp < 30) {
            expectedTime *= 1.2
        }

        // Wastegate-Response-Faktor
        if (input.wastegateDutyIdle < WG_DUTY_IDLE_MIN) {
            expectedTime *= 1.2
        }

        return expectedTime.coerceIn(1.0, 5.0)
    }

    /**
     * Bewertet Wastegate-Response
     *
     * Bei einem gesunden System wechselt der Wastegate von
     * Leerlauf-Duty (hoch) zu Spule-Duty (niedrig) innerhalb
     * weniger als 0.5 Sekunden.
     */
    private fun evaluateWastegateResponse(input: SpoolInput): Double {
        val response = input.wastegateDutyIdle - input.wastegateDutyAtSpool
        return response.coerceIn(0.0, 100.0)
    }

    /**
     * Bewertet Turbo-Drehzahl-Acceleration während der Spule
     */
    private fun evaluateTurboAcceleration(input: SpoolInput): Double {
        if (input.spoolTimeSeconds <= 0 || input.rpmAt80PercentBoost <= input.rpmAtThrottleApplication) {
            return 0.0
        }
        return (input.rpmAt80PercentBoost - input.rpmAtThrottleApplication) / input.spoolTimeSeconds
    }

    /**
     * Bestimmt den Spule-Status
     */
    private fun determineSpoolStatus(
        spoolTime: Double,
        wgResponse: Double,
        turboAccel: Double,
        input: SpoolInput
    ): SpoolStatus {
        // Kritisch: sehr hohe Spule-Zeit
        if (spoolTime > SPOOL_CRITICAL_MAX) return SpoolStatus.CRITICAL

        // Kritisch: schlechte Wastegate-Response
        if (wgResponse < 20 && input.wastegateDutyIdle > WG_DUTY_IDLE_MIN) {
            return SpoolStatus.CRITICAL
        }

        // Kritisch: sehr niedrige Turbo-Acceleration
        if (turboAccel < TURBO_ACCEL_SLOW && spoolTime > SPOOL_SLOW_MAX) {
            return SpoolStatus.CRITICAL
        }

        // Schlecht: hohe Spule-Zeit
        if (spoolTime > SPOOL_SLOW_MAX) return SpoolStatus.POOR

        // Schlecht: mangelhafte Wastegate-Response
        if (wgResponse < 30) return SpoolStatus.POOR

        // Langsame Spule
        if (spoolTime > SPOOL_GOOD_MAX) return SpoolStatus.SLOW_SPOOL

        // Optimal: alle Kriterien erfuillt
        if (spoolTime <= SPOOL_OPTIMAL_MAX && wgResponse >= 40 && turboAccel >= TURBO_ACCEL_GOOD) {
            return SpoolStatus.OPTIMAL
        }

        return SpoolStatus.GOOD
    }

    /**
     * Berechnet den Gesundheits-Score
     */
    private fun calculateHealthScore(
        spoolTime: Double,
        wgResponse: Double,
        turboAccel: Double,
        input: SpoolInput
    ): Int {
        val spoolScore = when {
            spoolTime <= SPOOL_OPTIMAL_MAX -> 100
            spoolTime <= SPOOL_GOOD_MAX -> 85
            spoolTime <= SPOOL_SLOW_MAX -> 50
            else -> 20
        }

        val wgScore = when {
            wgResponse >= 50 -> 100
            wgResponse >= 30 -> 70
            wgResponse >= 20 -> 40
            else -> 15
        }

        val accelScore = when {
            turboAccel >= TURBO_ACCEL_OPTIMAL -> 100
            turboAccel >= TURBO_ACCEL_GOOD -> 80
            turboAccel >= TURBO_ACCEL_SLOW -> 40
            else -> 15
        }

        val loadFactor = when {
            input.engineLoad > 70 -> 0.9
            input.engineLoad > 50 -> 0.95
            else -> 1.0
        }

        val weightedScore = (spoolScore * WEIGHT_SPOOL_TIME +
                wgScore * WEIGHT_WG_RESPONSE +
                accelScore * WEIGHT_TURBO_ACCEL) / 100.0

        return (weightedScore * loadFactor * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Bestimmt den Spule-Trend
     */
    private fun determineTrend(spoolDeviation: Double): SpoolTrend {
        return when {
            spoolDeviation < -20 -> SpoolTrend.IMPROVING
            spoolDeviation > 30 -> SpoolTrend.DEGRADING
            else -> SpoolTrend.STABLE
        }
    }

    /**
     * Generiert Diagnose
     */
    private fun generateDiagnosis(
        status: SpoolStatus,
        spoolTime: Double,
        expectedTime: Double,
        input: SpoolInput
    ): String {
        return when (status) {
            SpoolStatus.OPTIMAL -> {
                "Turbo-Spule optimal: ${"%.2f".format(spoolTime)}s (Soll: ${"%.2f".format(expectedTime)}s). " +
                        "Wastegate-Response: ${"%.0f".format(input.wastegateDutyIdle - input.wastegateDutyAtSpool)}%."
            }
            SpoolStatus.GOOD -> {
                "Turbo-Spule gut: ${"%.2f".format(spoolTime)}s. " +
                        "Leichte Verbesserungsmoeglichkeiten."
            }
            SpoolStatus.SLOW_SPOOL -> {
                "Langsame Turbo-Spule: ${"%.2f".format(spoolTime)}s (Soll: ${"%.2f".format(expectedTime)}s). " +
                        "Wastegate-Response: ${"%.0f".format(input.wastegateDutyIdle - input.wastegateDutyAtSpool)}%."
            }
            SpoolStatus.POOR -> {
                "Turbo-Spule schlecht: ${"%.2f".format(spoolTime)}s. " +
                        "Wastegate-Response mangelhaft. Pruefung empfohlen."
            }
            SpoolStatus.CRITICAL -> {
                "KRITISCH: Turbo-Spule versagt! ${"%.2f".format(spoolTime)}s (Soll: ${"%.2f".format(expectedTime)}s). " +
                        "Wastegate-Response: ${"%.0f".format(input.wastegateDutyIdle - input.wastegateDutyAtSpool)}%. " +
                        "Turbo-Acceleration: ${"%.0f".format(input.turboRpmAtSpool)} rpm/s."
            }
            SpoolStatus.INSUFFICIENT_DATA -> {
                "Unvollstaendige Daten fuer Spule-Analyse."
            }
        }
    }

    /**
     * Generiert Empfehlung
     */
    private fun generateRecommendation(status: SpoolStatus, input: SpoolInput): String {
        return when (status) {
            SpoolStatus.OPTIMAL, SpoolStatus.GOOD -> {
                "Turbo-Spule funktioniert optimal. Regulaerer Wartungsplan empfohlen."
            }
            SpoolStatus.SLOW_SPOOL -> {
                "Turbo-Inspektion empfohlen. Wastegate und Unterdruckleitungen pruefen. " +
                        "Oelstand pruefen (${input.boostPressureKpa} kPa)."
            }
            SpoolStatus.POOR -> {
                "Turbo-Health pruefen. Waagen-Response und Druckluft systematisch testen. " +
                        "Oelwechsel mit Dexos2 5W-30 durchfuehren."
            }
            SpoolStatus.CRITICAL -> {
                "SOFORT Werkstatt! Turbo-Spule kritisch. " +
                        "Waagen-Duty-Cycle und -Druck messen. " +
                        "Turbo-Defekt kann zu Motorschaden fuehren."
            }
            SpoolStatus.INSUFFICIENT_DATA -> {
                "Vollgas-Beschleunigung (3. Gang, 2000-5000 rpm) fuer Analyse durchfuehren."
            }
        }
    }

    /**
     * Fuehrt eine Vergleichsanalyse gegen eine Basislinie durch
     */
    fun compareWithBaseline(
        currentSpoolTime: Double,
        baselineSpoolTime: Double = NEW_TURBO_SPOOL_TIME
    ): Pair<Boolean, Double> {
        val deviation = ((currentSpoolTime - baselineSpoolTime) / baselineSpoolTime) * 100.0
        val isDegraded = deviation > 20.0
        return isDegraded to deviation
    }

    /**
     * Schaetzt den erwarteten Spule-Zustand basierend auf Kilometer
     */
    fun estimateSpoolDegradation(totalKm: Double): SpoolStatus {
        return when {
            totalKm < 30000 -> SpoolStatus.OPTIMAL
            totalKm < 80000 -> SpoolStatus.GOOD
            totalKm < 150000 -> SpoolStatus.SLOW_SPOOL
            totalKm < 200000 -> SpoolStatus.POOR
            else -> SpoolStatus.CRITICAL
        }
    }
}