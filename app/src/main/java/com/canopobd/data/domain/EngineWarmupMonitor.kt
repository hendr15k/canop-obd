package com.canopobd.data.domain

import com.canopobd.data.model.AstraJ14TurboCalibration

/**
 * Motor-Aufwaerm- und Kaltstartschutz-Analyse fuer Opel Astra J 1.4 Turbo (A14NET).
 *
 * Der A14NET ist im kalten Zustand besonders empfindlich:
 * - Unverbrannter Kraftstoff spült den Ölfilm von den Zylinderwänden
 *   (Kraftstoffeintrag ins Oel bei Kaltstarts unter 60 °C Oeltemperatur)
 * - Der BorgWarner KP39-Turbolader erhält erst bei warmem Oel volle Schmierung;
 *   Volllast mit kaltem Oel erhöht das Lagerverschleiss-Risiko deutlich
 * - Hohe Drehzahlen bei kaltem Kuehlmittel beschleunigen Steuerkettenverschleiss
 *
 * Das Modell kennt drei Phasen:
 * - COLD: Kuehlmittel < 60 °C — Schonung, Drehzahl unter 3000, kein Boost
 * - WARMING: 60-85 °C — moderater Betrieb, Drehzahl unter 4500, Boost unter 0,5 bar
 * - WARM: > 85 °C — Freigabe, mit Wassertemperatur-Stabilitaetspruefung
 */
class EngineWarmupMonitor(
    private val calibration: AstraJ14TurboCalibration = AstraJ14TurboCalibration.INSTANCE
) {

    /**
     * Aufwaermphase des Motors.
     */
    enum class WarmupPhase(val label: String, val severity: Int) {
        COLD("Kalt", 1),
        WARMING("Aufwaermphase", 1),
        WARM("Betriebswarm", 0),
        NO_DATA("Keine Daten", -1)
    }

    /**
     * Eingabedaten fuer die Aufwaerm-Analyse.
     */
    data class WarmupInput(
        val coolantTemp: Double,
        val oilTemp: Double = 0.0,
        val rpm: Double = 0.0,
        val boostBar: Double = 0.0,
        val engineLoad: Double = 0.0,
        val engineRuntimeSec: Double = 0.0,
        val speed: Double = 0.0
    )

    /**
     * Ergebnis der Aufwaerm-Analyse.
     */
    data class WarmupAnalysis(
        val phase: WarmupPhase,
        val healthScore: Int,
        val warmupProgress: Double,
        val recommendedMaxRpm: Int,
        val recommendedMaxBoostBar: Double,
        val coldViolations: Int,
        val diagnosis: String,
        val recommendation: String
    )

    companion object {
        private const val COLD_THRESHOLD = 60.0
        private const val WARM_THRESHOLD = 85.0
        private const val COLD_MAX_RPM = 3000.0
        private const val WARMING_MAX_RPM = 4500.0
        private const val COLD_MAX_BOOST_BAR = 0.0
        private const val WARMING_MAX_BOOST_BAR = 0.5
        private const val COLD_MAX_LOAD = 50.0
        private const val HIGH_REV_FRACTION_OF_REDLINE = 0.5
        private const val VIOLATION_PENALTY = 20
        private const val HIGH_REV_PENALTY = 10

        private const val RECOMMENDED_MAX_RPM_COLD = 3000
        private const val RECOMMENDED_MAX_RPM_WARMING = 4500
    }

    fun analyze(input: WarmupInput): WarmupAnalysis {
        val phase = determinePhase(input.coolantTemp)
        val progress = calculateProgress(input.coolantTemp)
        val violations = countViolations(input, phase)
        val score = calculateScore(input, phase, violations)
        val maxRpm = recommendedMaxRpm(phase)
        val maxBoost = recommendedMaxBoost(phase)
        val diagnosis = generateDiagnosis(phase, input, violations)
        val recommendation = generateRecommendation(phase, violations)

        return WarmupAnalysis(
            phase = phase,
            healthScore = score,
            warmupProgress = progress,
            recommendedMaxRpm = maxRpm,
            recommendedMaxBoostBar = maxBoost,
            coldViolations = violations,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    private fun determinePhase(coolantTemp: Double): WarmupPhase {
        return when {
            coolantTemp <= 0.0 -> WarmupPhase.NO_DATA
            coolantTemp < COLD_THRESHOLD -> WarmupPhase.COLD
            coolantTemp < WARM_THRESHOLD -> WarmupPhase.WARMING
            else -> WarmupPhase.WARM
        }
    }

    private fun calculateProgress(coolantTemp: Double): Double {
        if (coolantTemp <= 0.0) return 0.0
        return (coolantTemp / WARM_THRESHOLD).coerceIn(0.0, 1.0)
    }

    private fun countViolations(input: WarmupInput, phase: WarmupPhase): Int {
        var violations = 0
        when (phase) {
            WarmupPhase.COLD -> {
                if (input.rpm > COLD_MAX_RPM) violations++
                if (input.boostBar > COLD_MAX_BOOST_BAR) violations++
                if (input.engineLoad > COLD_MAX_LOAD) violations++
            }
            WarmupPhase.WARMING -> {
                if (input.rpm > WARMING_MAX_RPM) violations++
                if (input.boostBar > WARMING_MAX_BOOST_BAR) violations++
            }
            WarmupPhase.WARM, WarmupPhase.NO_DATA -> {
            }
        }
        return violations
    }

    @Suppress("ReturnCount", "MagicNumber")
    private fun calculateScore(input: WarmupInput, phase: WarmupPhase, violations: Int): Int {
        if (phase == WarmupPhase.NO_DATA) return 0
        if (input.rpm <= 0) return 100
        var score = 100
        score -= violations * VIOLATION_PENALTY
        if (phase == WarmupPhase.COLD && input.rpm > calibration.redlineRpm * HIGH_REV_FRACTION_OF_REDLINE) {
            score -= HIGH_REV_PENALTY
        }
        return score.coerceIn(0, 100)
    }

    private fun recommendedMaxRpm(phase: WarmupPhase): Int {
        return when (phase) {
            WarmupPhase.COLD -> RECOMMENDED_MAX_RPM_COLD
            WarmupPhase.WARMING -> RECOMMENDED_MAX_RPM_WARMING
            WarmupPhase.WARM -> calibration.redlineRpm.toInt()
            WarmupPhase.NO_DATA -> RECOMMENDED_MAX_RPM_COLD
        }
    }

    private fun recommendedMaxBoost(phase: WarmupPhase): Double {
        return when (phase) {
            WarmupPhase.COLD -> COLD_MAX_BOOST_BAR
            WarmupPhase.WARMING -> WARMING_MAX_BOOST_BAR
            WarmupPhase.WARM -> calibration.normalBoostTargetBar
            WarmupPhase.NO_DATA -> COLD_MAX_BOOST_BAR
        }
    }

    private fun generateDiagnosis(phase: WarmupPhase, input: WarmupInput, violations: Int): String {
        return when (phase) {
            WarmupPhase.NO_DATA -> "Keine Kühlmitteltemperatur verfügbar."
            WarmupPhase.COLD ->
                "Motor kalt (${input.coolantTemp.toInt()} °C). " +
                    "Bitte unter $RECOMMENDED_MAX_RPM_COLD U/min bleiben und kein Boost anfordern." +
                    if (violations > 0) " $violations Kaltstart-Verstösse erkannt!" else ""
            WarmupPhase.WARMING ->
                "Motor in Aufwärmphase (${input.coolantTemp.toInt()} °C). " +
                    "Moderater Betrieb bis $RECOMMENDED_MAX_RPM_WARMING U/min." +
                    if (violations > 0) " $violations Verstösse erkannt." else ""
            WarmupPhase.WARM -> "Motor betriebswarm (${input.coolantTemp.toInt()} °C). Volle Leistung freigegeben."
        }
    }

    private fun generateRecommendation(phase: WarmupPhase, violations: Int): String {
        return when {
            phase == WarmupPhase.NO_DATA -> "Kühlmittelsensor prüfen."
            violations > 0 -> "Drehzahl und Boost reduzieren, bis der Motor betriebswarm ist."
            phase == WarmupPhase.COLD -> "Schonend warmfahren, kein Vollgas im kalten Zustand."
            phase == WarmupPhase.WARMING -> "Weiter moderat warmfahren."
            else -> "Keine Massnahmen erforderlich."
        }
    }
}
