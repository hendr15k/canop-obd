package com.canopobd.data.domain

import kotlin.math.abs

class LambdaBalanceAnalyzer {

    enum class LambdaStatus(
        val label: String,
        val description: String,
        val minLambda: Double,
        val maxLambda: Double
    ) {
        PERFECT("Optimal", "Lambda-Regelung arbeitet einwandfrei", 0.98, 1.02),
        SLIGHTLY_LEAN("Leicht mager", "Gemisch leicht zu mager", 1.02, 1.05),
        MODERATELY_LEAN("Mäßig mager", "Gemisch zu mager - Wartung prüfen", 1.05, 1.10),
        SLIGHTLY_RICH("Leicht fett", "Gemisch leicht zu fett", 0.95, 0.98),
        MODERATELY_RICH("Mäßig fett", "Gemisch zu fett - Wartung prüfen", 0.90, 0.95),
        STALLED("Stillstand", "Lambda-Regelung inaktiv", 0.96, 1.04),
        FAULTED("Gestört", "Lambda-Sonde defekt", 0.0, 10.0)
    }

    data class LambdaBalance(
        val avgLambda: Double = 1.0,
        val variance: Double = 0.0,
        val oscillationFreq: Double = 0.0,
        val oscillationAmplitude: Double = 0.0,
        val healthScore: Int = 100,
        val status: LambdaStatus = LambdaStatus.PERFECT
    )

    data class CatEfficiencyEstimate(
        val efficiencyPercent: Double = 0.0,
        val status: CatStatus = CatStatus.UNKNOWN,
        val recommendation: String = ""
    )

    enum class CatStatus(val label: String) {
        UNKNOWN("Unbekannt"),
        EXCELLENT("Ausgezeichnet"),
        GOOD("Gut"),
        DEGRADED("Vermindert"),
        FAILING("Versagend"),
        FAILED("Defekt")
    }

    companion object {
        private const val STOICHIOMETRIC_LAMBDA = 1.0
        private const val MIN_SAMPLES_FOR_ANALYSIS = 20
        private const val SAMPLE_HISTORY_SIZE = 100
        private const val CAT_EFFICIENCY_EXCELLENT = 95.0
        private const val CAT_EFFICIENCY_GOOD = 85.0
    }

    private val lambdaHistory = mutableListOf<Double>()
    private var lastLambdaValue = 1.0
    private var lambdaSwitchCount = 0

    fun analyzeLambdaSequence(samples: List<Double>): LambdaBalance {
        if (samples.size < MIN_SAMPLES_FOR_ANALYSIS) { return LambdaBalance() }

        val avgLambda = samples.average()
        val variance = calculateVariance(samples)
        val oscillationInfo = detectOscillation(samples)
        val status = determineStatus(avgLambda, variance)
        val healthScore = calculateHealthScore(avgLambda, variance)

        return LambdaBalance(
            avgLambda = avgLambda,
            variance = variance,
            oscillationFreq = oscillationInfo.second,
            oscillationAmplitude = oscillationInfo.first,
            healthScore = healthScore,
            status = status
        )
    }

    fun addLambdaSample(lambda: Double) {
        if (lambda <= 0 || lambda > 10) { return }

        if (lastLambdaValue > 0) {
            val crossedThreshold = (lastLambdaValue - STOICHIOMETRIC_LAMBDA) * (lambda - STOICHIOMETRIC_LAMBDA) < 0
            if (crossedThreshold) { lambdaSwitchCount++ }
        }

        lastLambdaValue = lambda
        lambdaHistory.add(lambda)

        if (lambdaHistory.size > SAMPLE_HISTORY_SIZE) {
            lambdaHistory.removeAt(0)
        }
    }

    fun analyzeCurrentSequence(): LambdaBalance {
        return analyzeLambdaSequence(lambdaHistory.toList())
    }

    fun detectOscillation(): Boolean {
        if (lambdaHistory.size < MIN_SAMPLES_FOR_ANALYSIS) { return true }
        val oscillationInfo = detectOscillation(lambdaHistory)
        return oscillationInfo.second in 0.5..2.0
    }

    fun getCatEfficiency(): Double {
        if (lambdaHistory.size < MIN_SAMPLES_FOR_ANALYSIS) { return 0.0 }

        val balance = analyzeCurrentSequence()
        val leanPeaks = lambdaHistory.count { it > 1.02 }
        val richPeaks = lambdaHistory.count { it < 0.98 }
        val totalSamples = lambdaHistory.size

        val leanRatio = leanPeaks.toDouble() / totalSamples
        val richRatio = richPeaks.toDouble() / totalSamples
        val asymmetry = abs(leanRatio - richRatio)

        val baseEfficiency = 100.0
        val variancePenalty = balance.variance * 1000
        val asymmetryPenalty = asymmetry * 20

        return (baseEfficiency - variancePenalty - asymmetryPenalty).coerceIn(0.0, 100.0)
    }

    fun estimateCatEfficiency(): CatEfficiencyEstimate {
        val efficiency = getCatEfficiency()

        val status = when {
            efficiency >= CAT_EFFICIENCY_EXCELLENT -> CatStatus.EXCELLENT
            efficiency >= CAT_EFFICIENCY_GOOD -> CatStatus.GOOD
            efficiency >= 70.0 -> CatStatus.DEGRADED
            efficiency >= 50.0 -> CatStatus.FAILING
            else -> CatStatus.FAILED
        }

        val recommendation = when (status) {
            CatStatus.EXCELLENT -> "Katalysator arbeitet einwandfrei"
            CatStatus.GOOD -> "Katalysator in gutem Zustand"
            CatStatus.DEGRADED -> "Katalysator-Wirkungsgrad vermindert - O2-Sensoren prüfen"
            CatStatus.FAILING -> "Katalysator versagt - Auspuffsystem prüfen"
            CatStatus.FAILED -> "Katalysator defekt - Werkstattdiagnose erforderlich"
            CatStatus.UNKNOWN -> "Weitere Fahrtdaten für Analyse erforderlich"
        }

        return CatEfficiencyEstimate(efficiency, status, recommendation)
    }

    private fun detectOscillation(samples: List<Double>): Pair<Double, Double> {
        if (samples.size < 10) { return Pair(0.0, 0.0) }

        val maxVal = samples.maxOrNull() ?: 1.0
        val minVal = samples.minOrNull() ?: 1.0
        val amplitude = (maxVal - minVal) / 2.0

        var zeroCrossings = 0
        var aboveMean = samples.first() > samples.average()

        for (value in samples) {
            val currentAbove = value > samples.average()
            if (currentAbove != aboveMean) {
                zeroCrossings++
                aboveMean = currentAbove
            }
        }

        val freqEstimate = if (zeroCrossings > 0) {
            zeroCrossings.toDouble() / (2.0 * samples.size) * 10.0
        } else { 0.0 }

        return Pair(amplitude, freqEstimate)
    }

    private fun calculateVariance(samples: List<Double>): Double {
        if (samples.size < 2) { return 0.0 }
        val avg = samples.average()
        return samples.sumOf { (it - avg) * (it - avg) } / samples.size
    }

    private fun determineStatus(avgLambda: Double, variance: Double): LambdaStatus {
        if (variance > 0.1) { return LambdaStatus.FAULTED }
        return when {
            avgLambda > 1.10 -> LambdaStatus.MODERATELY_LEAN
            avgLambda > 1.02 -> LambdaStatus.SLIGHTLY_LEAN
            avgLambda >= 0.98 -> LambdaStatus.PERFECT
            avgLambda >= 0.95 -> LambdaStatus.SLIGHTLY_RICH
            avgLambda >= 0.90 -> LambdaStatus.MODERATELY_RICH
            else -> LambdaStatus.MODERATELY_RICH
        }
    }

    private fun calculateHealthScore(avgLambda: Double, variance: Double): Int {
        var score = 100
        val deviation = abs(avgLambda - STOICHIOMETRIC_LAMBDA)
        score -= (deviation * 100).toInt().coerceAtMost(40)
        score -= (variance * 500).toInt().coerceAtMost(30)
        return score.coerceIn(0, 100)
    }

    fun reset() {
        lambdaHistory.clear()
        lambdaSwitchCount = 0
        lastLambdaValue = 1.0
    }
}
