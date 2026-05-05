package com.canopobd.data.domain

import kotlin.math.abs
import kotlin.math.sqrt

class FuelTrimAnalyzer {

    data class FuelTrimStatus(
        val stft: Double,
        val ltft: Double,
        val totalTrim: Double,
        val isLean: Boolean,
        val isRich: Boolean,
        val healthScore: Int,
        val diagnosis: String
    )

    enum class TrimStatus(
        val label: String,
        val description: String,
        val minTrim: Double,
        val maxTrim: Double
    ) {
        OPTIMAL("Optimal", "Kraftstoffsystem im optimalen Bereich", -3.0, 3.0),
        SLIGHTLY_LEAN("Leicht mager", "Leichte Magerkorrektur aktiv", 3.0, 8.0),
        MODERATELY_LEAN("Mäßig mager", "Mäßige Magerkorrektur - Wartung empfohlen", 8.0, 12.0),
        SEVERELY_LEAN("Stark mager", "Starke Magerkorrektur - Problem erkannt", 12.0, 100.0),
        SLIGHTLY_RICH("Leicht fett", "Leichte Fettkorrektur aktiv", -8.0, -3.0),
        MODERATELY_RICH("Mäßig fett", "Mäßige Fettkorrektur - Wartung empfohlen", -12.0, -8.0),
        SEVERELY_RICH("Stark fett", "Starke Fettkorrektur - Problem erkannt", -100.0, -12.0)
    }

    data class FuelTrimData(
        val stftBank1: Double = 0.0,
        val ltftBank1: Double = 0.0,
        val stftBank2: Double = 0.0,
        val ltftBank2: Double = 0.0,
        val combinedTrimBank1: Double = 0.0,
        val combinedTrimBank2: Double = 0.0,
        val status: TrimStatus = TrimStatus.OPTIMAL,
        val healthScore: Int = 100
    ) {
        val totalTrim: Double get() = combinedTrimBank1
        val isLean: Boolean get() = combinedTrimBank1 > 5.0
        val isRich: Boolean get() = combinedTrimBank1 < -5.0
        val diagnosis: String get() = when {
            status == TrimStatus.OPTIMAL -> "Kraftstoffsystem arbeitet optimal"
            status.label.contains("mager") -> "System tendiert zu magerem Gemisch"
            status.label.contains("fett") -> "System tendiert zu fettem Gemisch"
            else -> "Gemischzusammensetzung ausserhalb Normalbereich"
        }
    }

    data class TrimAnalysisResult(
        val data: FuelTrimData,
        val isCorrectionNeeded: Boolean,
        val diagnosis: String,
        val recommendedAction: String,
        val probableCauses: List<String>
    )

    companion object {
        private const val NORMAL_TRIM = 5.0
        private const val WARNING_TRIM = 10.0
        private const val PROBLEM_TRIM = 15.0

        private const val OPTIMAL_TRIM_MAX = 5.0
        private const val WARNING_TRIM_MIN = 5.0
        private const val WARNING_TRIM_MAX = 10.0
        private const val PROBLEM_TRIM_MIN = 10.0
        private const val CRITICAL_TRIM_MIN = 15.0

        private const val BANK_ASYMMETRY_WARNING = 5.0
        private const val BANK_ASYMMETRY_CRITICAL = 10.0

        private const val LTFT_DRIFT_THRESHOLD = 8.0
        private const val TRIM_SAMPLES_FOR_TREND = 10
    }

    private val stftHistoryBank1 = mutableListOf<Double>()
    private val ltftHistoryBank1 = mutableListOf<Double>()
    private val stftHistoryBank2 = mutableListOf<Double>()
    private val ltftHistoryBank2 = mutableListOf<Double>()

    fun analyze(stft: Double, ltft: Double): FuelTrimStatus {
        val totalTrim = stft + ltft
        val absTotal = abs(totalTrim)

        val (healthScore, diagnosis) = when {
            absTotal > PROBLEM_TRIM -> {
                when {
                    totalTrim > 0 -> 20 to "System zu mager - Leck oder Sensorproblem"
                    else -> 20 to "System zu fett - Einspritzung oder Kraftstoffdruck"
                }
            }
            absTotal > WARNING_TRIM -> {
                when {
                    totalTrim > 0 -> 50 to "Leichte Trimabweichung (mager)"
                    else -> 50 to "Leichte Trimabweichung (fett)"
                }
            }
            absTotal > NORMAL_TRIM -> {
                70 to "Leicht erhöhter Trim - Wartung empfohlen"
            }
            else -> {
                100 to "Kraftstoffsystem optimal"
            }
        }

        return FuelTrimStatus(
            stft = stft,
            ltft = ltft,
            totalTrim = totalTrim,
            isLean = totalTrim > WARNING_TRIM,
            isRich = totalTrim < -WARNING_TRIM,
            healthScore = healthScore,
            diagnosis = diagnosis
        )
    }

    fun getRecommendedAction(status: FuelTrimStatus): String {
        return when {
            status.healthScore <= 20 ->
                "Dringend: MAF-Sensor prüfen, Ansaug-Leck oder Einspritzungsproblem"
            status.healthScore <= 50 ->
                "Empfohlen: Luftfilter, MAF-Sensor und O2-Sensoren prüfen"
            status.healthScore <= 70 ->
                "Beobachten: Trimwerte im Auge behalten"
            else ->
                "Keine Maßnahmen erforderlich"
        }
    }

    fun analyzeTrims(stft: Double?, ltft: Double?): FuelTrimData {
        val stftB1 = stft ?: 0.0
        val ltftB1 = ltft ?: 0.0

        updateHistory(stftB1, ltftB1)

        val combinedTrim = stftB1 + ltftB1
        val status = determineStatus(combinedTrim)
        val healthScore = calculateHealthScore(combinedTrim)

        return FuelTrimData(
            stftBank1 = stftB1,
            ltftBank1 = ltftB1,
            stftBank2 = 0.0,
            ltftBank2 = 0.0,
            combinedTrimBank1 = combinedTrim,
            combinedTrimBank2 = 0.0,
            status = status,
            healthScore = healthScore
        )
    }

    fun analyzeTrimsBothBanks(
        stftB1: Double?,
        ltftB1: Double?,
        stftB2: Double?,
        ltftB2: Double?
    ): FuelTrimData {
        val stft1 = stftB1 ?: 0.0
        val ltft1 = ltftB1 ?: 0.0
        val stft2 = stftB2 ?: 0.0
        val ltft2 = ltftB2 ?: 0.0

        updateHistory(stft1, ltft1)

        val combined1 = stft1 + ltft1
        val combined2 = stft2 + ltft2
        val worstTrim = maxOf(abs(combined1), abs(combined2))
        val status = determineStatus(worstTrim)
        val healthScore = calculateHealthScoreBothBanks(combined1, combined2)

        return FuelTrimData(
            stftBank1 = stft1,
            ltftBank1 = ltft1,
            stftBank2 = stft2,
            ltftBank2 = ltft2,
            combinedTrimBank1 = combined1,
            combinedTrimBank2 = combined2,
            status = status,
            healthScore = healthScore
        )
    }

    fun analyzeComplete(): TrimAnalysisResult {
        val trimData = if (stftHistoryBank1.isNotEmpty()) {
            analyzeTrims(stftHistoryBank1.last(), ltftHistoryBank1.last())
        } else {
            FuelTrimData()
        }

        val isCorrectionNeeded = isTrimCorrectionNeeded()
        val diagnosis = generateDiagnosis(trimData)
        val recommendedAction = getRecommendedActionData(trimData)
        val probableCauses = identifyProbableCauses(trimData)

        return TrimAnalysisResult(
            data = trimData,
            isCorrectionNeeded = isCorrectionNeeded,
            diagnosis = diagnosis,
            recommendedAction = recommendedAction,
            probableCauses = probableCauses
        )
    }

    fun isTrimCorrectionNeeded(): Boolean {
        if (stftHistoryBank1.size < 3) return false
        val recentStft = stftHistoryBank1.takeLast(3)
        val avgStft = recentStft.average()
        return abs(avgStft) > WARNING_TRIM_MIN
    }

    fun getRecommendedAction(data: FuelTrimData): String {
        return when {
            data.status == TrimStatus.OPTIMAL -> "Keine Maßnahmen erforderlich"
            data.status == TrimStatus.SLIGHTLY_LEAN -> "Luftfilter und MAF-Sensor bei nächster Wartung prüfen"
            data.status == TrimStatus.SLIGHTLY_RICH -> "Einspritzventile auf Undichtigkeiten prüfen"
            data.status == TrimStatus.MODERATELY_LEAN -> "MAF-Sensor reinigen, Leck im Ansaugtrakt suchen"
            data.status == TrimStatus.MODERATELY_RICH -> "Einspritzventile und Kraftstoffdruck prüfen"
            data.status == TrimStatus.SEVERELY_LEAN -> "Dringend: MAF-Sensor, Ansaug-Leck und O2-Sensoren prüfen"
            data.status == TrimStatus.SEVERELY_RICH -> "Dringend: Einspritzventile, Kraftstoffdruck und O2-Sensoren prüfen"
            else -> "Weitere Diagnose erforderlich"
        }
    }

    private fun getRecommendedActionData(data: FuelTrimData): String {
        return getRecommendedAction(data)
    }

    fun getTrimTrend(): TrimTrend {
        if (stftHistoryBank1.size < TRIM_SAMPLES_FOR_TREND) {
            return TrimTrend(TrimTrendDirection.STABLE, 0.0, 0)
        }

        val halfSize = stftHistoryBank1.size / 2
        val firstHalfAvg = stftHistoryBank1.take(halfSize).average()
        val secondHalfAvg = stftHistoryBank1.takeLast(halfSize).average()
        val change = secondHalfAvg - firstHalfAvg

        val direction = when {
            change < -2.0 -> TrimTrendDirection.TREND_RICH
            change > 2.0 -> TrimTrendDirection.TREND_LEAN
            else -> TrimTrendDirection.STABLE
        }

        return TrimTrend(direction, change, stftHistoryBank1.size)
    }

    fun detectStftInstability(): Boolean {
        if (stftHistoryBank1.size < 5) return false
        val recent = stftHistoryBank1.takeLast(5)
        val avg = recent.average()
        val variance = recent.sumOf { (it - avg) * (it - avg) } / recent.size
        val stdDev = sqrt(variance)
        return stdDev > 3.0
    }

    fun detectLtftDrift(): Boolean {
        if (ltftHistoryBank1.size < TRIM_SAMPLES_FOR_TREND) return false
        val halfSize = ltftHistoryBank1.size / 2
        val firstHalfAvg = ltftHistoryBank1.take(halfSize).average()
        val secondHalfAvg = ltftHistoryBank1.takeLast(halfSize).average()
        val drift = abs(secondHalfAvg - firstHalfAvg)
        return drift > LTFT_DRIFT_THRESHOLD
    }

    enum class TrimTrendDirection { TREND_LEAN, TREND_RICH, STABLE }
    data class TrimTrend(val direction: TrimTrendDirection, val magnitude: Double, val samples: Int)

    private fun updateHistory(stft: Double, ltft: Double) {
        stftHistoryBank1.add(stft)
        ltftHistoryBank1.add(ltft)
        if (stftHistoryBank1.size > TRIM_SAMPLES_FOR_TREND * 2) {
            stftHistoryBank1.removeAt(0)
            ltftHistoryBank1.removeAt(0)
        }
    }

    private fun determineStatus(trim: Double): TrimStatus {
        return when {
            trim > CRITICAL_TRIM_MIN -> TrimStatus.SEVERELY_LEAN
            trim > PROBLEM_TRIM_MIN -> TrimStatus.MODERATELY_LEAN
            trim > WARNING_TRIM_MIN -> TrimStatus.SLIGHTLY_LEAN
            trim < -CRITICAL_TRIM_MIN -> TrimStatus.SEVERELY_RICH
            trim < -PROBLEM_TRIM_MIN -> TrimStatus.MODERATELY_RICH
            trim < -WARNING_TRIM_MIN -> TrimStatus.SLIGHTLY_RICH
            else -> TrimStatus.OPTIMAL
        }
    }

    private fun calculateHealthScore(trim: Double): Int {
        val absTrim = abs(trim)
        return when {
            absTrim <= OPTIMAL_TRIM_MAX -> 100
            absTrim <= WARNING_TRIM_MAX -> (100 - ((absTrim - OPTIMAL_TRIM_MAX) / 5.0 * 30)).toInt().coerceIn(70, 100)
            absTrim <= PROBLEM_TRIM_MIN -> (70 - ((absTrim - WARNING_TRIM_MAX) / 5.0 * 30)).toInt().coerceIn(40, 70)
            absTrim <= CRITICAL_TRIM_MIN -> (40 - ((absTrim - PROBLEM_TRIM_MIN) / 5.0 * 25)).toInt().coerceIn(15, 40)
            else -> (15 - ((absTrim - CRITICAL_TRIM_MIN) / 10.0 * 10)).toInt().coerceIn(0, 15)
        }
    }

    private fun calculateHealthScoreBothBanks(trim1: Double, trim2: Double): Int {
        val worstTrim = maxOf(abs(trim1), abs(trim2))
        val asymmetry = abs(trim1 - trim2)
        val baseScore = calculateHealthScore(worstTrim)
        val asymmetryPenalty = when {
            asymmetry > BANK_ASYMMETRY_CRITICAL -> 15
            asymmetry > BANK_ASYMMETRY_WARNING -> 8
            else -> 0
        }
        return (baseScore - asymmetryPenalty).coerceIn(0, 100)
    }

    private fun generateDiagnosis(data: FuelTrimData): String {
        return when {
            data.status == TrimStatus.OPTIMAL -> "Kraftstoffsystem arbeitet im optimalen Bereich"
            data.status.label.contains("mager") ->
                "System tendiert zu magerem Gemisch. STFT: ${"%.1f".format(data.stftBank1)}%, LTFT: ${"%.1f".format(data.ltftBank1)}%"
            data.status.label.contains("fett") ->
                "System tendiert zu fettem Gemisch. STFT: ${"%.1f".format(data.stftBank1)}%, LTFT: ${"%.1f".format(data.ltftBank1)}%"
            else -> "Gemischzusammensetzung außerhalb Normalbereich"
        }
    }

    private fun identifyProbableCauses(data: FuelTrimData): List<String> {
        val causes = mutableListOf<String>()
        when {
            data.combinedTrimBank1 > 5.0 -> {
                causes.add("Leck im Ansaugtrakt nach MAF")
                causes.add("Verschmutzter MAF-Sensor")
                causes.add("Kraftstoffdruck zu niedrig")
            }
            data.combinedTrimBank1 < -5.0 -> {
                causes.add("Einspritzventile undicht")
                causes.add("Kraftstoffdruck zu hoch")
                causes.add("MAF-Sensor verschmutzt")
            }
        }
        if (detectStftInstability()) causes.add("STFT-Instabilität - O2-Sonde prüfen")
        if (detectLtftDrift()) causes.add("LTFT-Drift - Langzeit-Problem")
        return causes
    }

    fun reset() {
        stftHistoryBank1.clear()
        ltftHistoryBank1.clear()
        stftHistoryBank2.clear()
        ltftHistoryBank2.clear()
    }
}
