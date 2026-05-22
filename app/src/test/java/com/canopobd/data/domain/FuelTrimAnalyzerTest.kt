package com.canopobd.data.domain

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FuelTrimAnalyzerTest {

    private lateinit var analyzer: FuelTrimAnalyzer

    @Before
    fun setup() {
        analyzer = FuelTrimAnalyzer()
    }

    // --- analyze(stft, ltft) tests ---

    @Test
    fun `analyze with normal fuel trims returns OK status`() {
        val result = analyzer.analyze(2.0, 3.0)
        assertEquals(5.0, result.totalTrim, 0.001)
        assertFalse(result.isLean)
        assertFalse(result.isRich)
        assertEquals(100, result.healthScore)
        assertEquals("Kraftstoffsystem optimal", result.diagnosis)
    }

    @Test
    fun `analyze with lean condition detected`() {
        val result = analyzer.analyze(15.0, 10.0)
        assertEquals(25.0, result.totalTrim, 0.001)
        assertTrue(result.isLean)
        assertFalse(result.isRich)
        assertEquals(20, result.healthScore)
    }

    @Test
    fun `analyze with rich condition detected`() {
        val result = analyzer.analyze(-15.0, -10.0)
        assertEquals(-25.0, result.totalTrim, 0.001)
        assertFalse(result.isLean)
        assertTrue(result.isRich)
        assertEquals(20, result.healthScore)
    }

    @Test
    fun `analyze with strongly lean condition`() {
        val result = analyzer.analyze(25.0, 0.0)
        assertEquals(25.0, result.totalTrim, 0.001)
        assertTrue(result.isLean)
        assertEquals(20, result.healthScore)
    }

    @Test
    fun `analyze with critically high trim`() {
        val result = analyzer.analyze(28.0, 0.0)
        assertEquals(28.0, result.totalTrim, 0.001)
        assertTrue(result.isLean)
        assertEquals(20, result.healthScore)
    }

    @Test
    fun `analyze with zero trims returns optimal`() {
        val result = analyzer.analyze(0.0, 0.0)
        assertEquals(0.0, result.totalTrim, 0.001)
        assertFalse(result.isLean)
        assertFalse(result.isRich)
        assertEquals(100, result.healthScore)
    }

    @Test
    fun `analyze total trim is sum of stft and ltft`() {
        val result = analyzer.analyze(4.0, -1.0)
        assertEquals(3.0, result.totalTrim, 0.001)
    }

    @Test
    fun `analyze at warning boundary - moderate lean`() {
        val result = analyzer.analyze(8.0, 5.0)
        assertEquals(13.0, result.totalTrim, 0.001)
        assertTrue(result.isLean)
        assertEquals(50, result.healthScore)
    }

    // --- getRecommendedAction(FuelTrimStatus) tests ---

    @Test
    fun `recommended action for healthy status is no measures`() {
        val status = analyzer.analyze(2.0, 2.0)
        assertEquals("Keine Maßnahmen erforderlich", analyzer.getRecommendedAction(status))
    }

    @Test
    fun `recommended action for moderate warning recommends checking`() {
        val status = analyzer.analyze(8.0, 0.0)
        val action = analyzer.getRecommendedAction(status)
        assertTrue(action.contains("Luftfilter") || action.contains("Beobachten"))
    }

    @Test
    fun `recommended action for severe problem recommends urgent check`() {
        val status = analyzer.analyze(20.0, 0.0)
        val action = analyzer.getRecommendedAction(status)
        assertTrue(action.contains("Dringend") || action.contains("MAF-Sensor"))
    }

    // --- analyzeTrims tests ---

    @Test
    fun `analyzeTrims with normal values returns OPTIMAL status`() {
        val data = analyzer.analyzeTrims(2.0, 2.0)
        assertEquals(2.0, data.stftBank1, 0.001)
        assertEquals(2.0, data.ltftBank1, 0.001)
        assertEquals(4.0, data.combinedTrimBank1, 0.001)
        assertEquals(FuelTrimAnalyzer.TrimStatus.OPTIMAL, data.status)
        assertEquals(100, data.healthScore)
    }

    @Test
    fun `analyzeTrims with null values uses zero defaults`() {
        val data = analyzer.analyzeTrims(null, null)
        assertEquals(0.0, data.stftBank1, 0.001)
        assertEquals(0.0, data.ltftBank1, 0.001)
        assertEquals(0.0, data.combinedTrimBank1, 0.001)
        assertEquals(FuelTrimAnalyzer.TrimStatus.OPTIMAL, data.status)
    }

    @Test
    fun `analyzeTrims detects severely lean`() {
        val data = analyzer.analyzeTrims(10.0, 10.0)
        assertEquals(FuelTrimAnalyzer.TrimStatus.SEVERELY_LEAN, data.status)
    }

    @Test
    fun `analyzeTrims detects severely rich`() {
        val data = analyzer.analyzeTrims(-10.0, -10.0)
        assertEquals(FuelTrimAnalyzer.TrimStatus.SEVERELY_RICH, data.status)
    }

    @Test
    fun `analyzeTrims detects moderately lean`() {
        val data = analyzer.analyzeTrims(7.0, 7.0)
        assertEquals(FuelTrimAnalyzer.TrimStatus.MODERATELY_LEAN, data.status)
    }

    @Test
    fun `analyzeTrims detects slightly lean`() {
        val data = analyzer.analyzeTrims(5.0, 5.0)
        assertEquals(FuelTrimAnalyzer.TrimStatus.SLIGHTLY_LEAN, data.status)
    }

    @Test
    fun `analyzeTrims detects slightly rich`() {
        val data = analyzer.analyzeTrims(-5.0, -5.0)
        assertEquals(FuelTrimAnalyzer.TrimStatus.SLIGHTLY_RICH, data.status)
    }

    @Test
    fun `analyzeTrims health score degrades with higher trim`() {
        val optimal = analyzer.analyzeTrims(1.0, 1.0)
        val moderate = analyzer.analyzeTrims(6.0, 6.0)
        val severe = analyzer.analyzeTrims(12.0, 12.0)
        assertTrue(optimal.healthScore > moderate.healthScore)
        assertTrue(moderate.healthScore > severe.healthScore)
    }

    // --- analyzeTrimsBothBanks tests ---

    @Test
    fun `analyzeTrimsBothBanks normal both banks`() {
        val data = analyzer.analyzeTrimsBothBanks(2.0, 2.0, -1.0, -1.0)
        assertEquals(4.0, data.combinedTrimBank1, 0.001)
        assertEquals(-2.0, data.combinedTrimBank2, 0.001)
        assertEquals(FuelTrimAnalyzer.TrimStatus.OPTIMAL, data.status)
    }

    @Test
    fun `analyzeTrimsBothBanks worst bank determines status`() {
        val data = analyzer.analyzeTrimsBothBanks(1.0, 1.0, 12.0, 5.0)
        assertEquals(FuelTrimAnalyzer.TrimStatus.SEVERELY_LEAN, data.status)
    }

    @Test
    fun `analyzeTrimsBothBanks with null values defaults to zero`() {
        val data = analyzer.analyzeTrimsBothBanks(null, null, null, null)
        assertEquals(0.0, data.combinedTrimBank1, 0.001)
        assertEquals(0.0, data.combinedTrimBank2, 0.001)
    }

    @Test
    fun `analyzeTrimsBothBanks asymmetry penalizes health score`() {
        val symmetric = analyzer.analyzeTrimsBothBanks(5.0, 5.0, 5.0, 5.0)
        val asymmetric = analyzer.analyzeTrimsBothBanks(5.0, 5.0, -8.0, -8.0)
        assertTrue(symmetric.healthScore >= asymmetric.healthScore)
    }

    // --- analyzeComplete tests ---

    @Test
    fun `analyzeComplete with empty history returns default data`() {
        val result = analyzer.analyzeComplete()
        assertEquals(0.0, result.data.stftBank1, 0.001)
        assertEquals(0.0, result.data.ltftBank1, 0.001)
        assertFalse(result.isCorrectionNeeded)
    }

    @Test
    fun `analyzeComplete after adding data uses last sample`() {
        analyzer.analyzeTrims(3.0, 4.0)
        analyzer.analyzeTrims(6.0, 7.0)
        val result = analyzer.analyzeComplete()
        assertEquals(6.0, result.data.stftBank1, 0.001)
        assertEquals(7.0, result.data.ltftBank1, 0.001)
    }

    @Test
    fun `analyzeComplete includes diagnosis and recommendation`() {
        analyzer.analyzeTrims(10.0, 10.0)
        val result = analyzer.analyzeComplete()
        assertTrue(result.diagnosis.isNotEmpty())
        assertTrue(result.recommendedAction.isNotEmpty())
    }

    // --- isTrimCorrectionNeeded tests ---

    @Test
    fun `isTrimCorrectionNeeded returns false with fewer than 3 samples`() {
        analyzer.analyzeTrims(10.0, 0.0)
        analyzer.analyzeTrims(10.0, 0.0)
        assertFalse(analyzer.isTrimCorrectionNeeded())
    }

    @Test
    fun `isTrimCorrectionNeeded returns true when average exceeds threshold`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(10.0, 0.0)
        }
        assertTrue(analyzer.isTrimCorrectionNeeded())
    }

    @Test
    fun `isTrimCorrectionNeeded returns false when average is low`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(2.0, 1.0)
        }
        assertFalse(analyzer.isTrimCorrectionNeeded())
    }

    // --- getRecommendedAction(FuelTrimData) tests ---

    @Test
    fun `recommended action data for optimal status`() {
        val data = analyzer.analyzeTrims(1.0, 1.0)
        val action = analyzer.getRecommendedAction(data)
        assertEquals("Keine Maßnahmen erforderlich", action)
    }

    @Test
    fun `recommended action data for slightly lean`() {
        val data = analyzer.analyzeTrims(9.0, 0.0)
        val action = analyzer.getRecommendedAction(data)
        assertTrue(action.contains("Luftfilter") || action.contains("MAF"))
    }

    @Test
    fun `recommended action data for severely lean`() {
        val data = analyzer.analyzeTrims(10.0, 10.0)
        val action = analyzer.getRecommendedAction(data)
        assertTrue(action.contains("Dringend"))
    }

    @Test
    fun `recommended action data for severely rich`() {
        val data = analyzer.analyzeTrims(-10.0, -10.0)
        val action = analyzer.getRecommendedAction(data)
        assertTrue(action.contains("Dringend"))
    }

    // --- getTrimTrend tests ---

    @Test
    fun `getTrimTrend returns stable with insufficient samples`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(2.0, 0.0)
        }
        val trend = analyzer.getTrimTrend()
        assertEquals(FuelTrimAnalyzer.TrimTrendDirection.STABLE, trend.direction)
        assertEquals(0, trend.samples)
    }

    @Test
    fun `getTrimTrend detects leaning trend`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(1.0, 0.0)
        }
        for (i in 1..6) {
            analyzer.analyzeTrims(10.0, 0.0)
        }
        val trend = analyzer.getTrimTrend()
        assertEquals(FuelTrimAnalyzer.TrimTrendDirection.TREND_LEAN, trend.direction)
        assertTrue(trend.magnitude > 2.0)
    }

    @Test
    fun `getTrimTrend detects richening trend`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(10.0, 0.0)
        }
        for (i in 1..6) {
            analyzer.analyzeTrims(-1.0, 0.0)
        }
        val trend = analyzer.getTrimTrend()
        assertEquals(FuelTrimAnalyzer.TrimTrendDirection.TREND_RICH, trend.direction)
        assertTrue(trend.magnitude < -2.0)
    }

    @Test
    fun `getTrimTrend stable when values consistent`() {
        for (i in 1..12) {
            analyzer.analyzeTrims(3.0, 0.0)
        }
        val trend = analyzer.getTrimTrend()
        assertEquals(FuelTrimAnalyzer.TrimTrendDirection.STABLE, trend.direction)
    }

    // --- detectStftInstability tests ---

    @Test
    fun `detectStftInstability returns false with fewer than 5 samples`() {
        for (i in 1..4) {
            analyzer.analyzeTrims(2.0, 0.0)
        }
        assertFalse(analyzer.detectStftInstability())
    }

    @Test
    fun `detectStftInstability returns false for stable readings`() {
        for (i in 1..6) {
            analyzer.analyzeTrims(5.0, 0.0)
        }
        assertFalse(analyzer.detectStftInstability())
    }

    @Test
    fun `detectStftInstability returns true for highly variable readings`() {
        val values = listOf(2.0, 15.0, 2.0, 15.0, 2.0)
        for (v in values) {
            analyzer.analyzeTrims(v, 0.0)
        }
        assertTrue(analyzer.detectStftInstability())
    }

    // --- detectLtftDrift tests ---

    @Test
    fun `detectLtftDrift returns false with fewer than 10 samples`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(0.0, 2.0)
        }
        assertFalse(analyzer.detectLtftDrift())
    }

    @Test
    fun `detectLtftDrift returns false when stable`() {
        for (i in 1..12) {
            analyzer.analyzeTrims(0.0, 3.0)
        }
        assertFalse(analyzer.detectLtftDrift())
    }

    @Test
    fun `detectLtftDrift returns true when drifting significantly`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(0.0, 0.0)
        }
        for (i in 1..8) {
            analyzer.analyzeTrims(0.0, 12.0)
        }
        assertTrue(analyzer.detectLtftDrift())
    }

    // --- reset tests ---

    @Test
    fun `reset clears history and corrections`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(10.0, 0.0)
        }
        assertTrue(analyzer.isTrimCorrectionNeeded())

        analyzer.reset()
        assertFalse(analyzer.isTrimCorrectionNeeded())
    }

    @Test
    fun `reset clears trend data`() {
        for (i in 1..5) {
            analyzer.analyzeTrims(1.0, 0.0)
        }
        for (i in 1..6) {
            analyzer.analyzeTrims(10.0, 0.0)
        }
        analyzer.reset()
        val trend = analyzer.getTrimTrend()
        assertEquals(FuelTrimAnalyzer.TrimTrendDirection.STABLE, trend.direction)
    }

    // --- FuelTrimData computed properties ---

    @Test
    fun `FuelTrimData isLean true when combinedTrim above 5`() {
        val data = FuelTrimAnalyzer.FuelTrimData(combinedTrimBank1 = 6.0)
        assertTrue(data.isLean)
    }

    @Test
    fun `FuelTrimData isRich true when combinedTrim below -5`() {
        val data = FuelTrimAnalyzer.FuelTrimData(combinedTrimBank1 = -6.0)
        assertTrue(data.isRich)
    }

    @Test
    fun `FuelTrimData diagnosis for optimal status`() {
        val data = FuelTrimAnalyzer.FuelTrimData(
            status = FuelTrimAnalyzer.TrimStatus.OPTIMAL
        )
        assertEquals("Kraftstoffsystem arbeitet optimal", data.diagnosis)
    }

    @Test
    fun `FuelTrimData diagnosis for lean status contains mager`() {
        val data = FuelTrimAnalyzer.FuelTrimData(
            status = FuelTrimAnalyzer.TrimStatus.MODERATELY_LEAN
        )
        assertTrue(data.diagnosis.contains("mager"))
    }

    @Test
    fun `FuelTrimData diagnosis for rich status contains fettes`() {
        val data = FuelTrimAnalyzer.FuelTrimData(
            status = FuelTrimAnalyzer.TrimStatus.MODERATELY_RICH
        )
        assertTrue(data.diagnosis.contains("fett"))
    }

    // --- TrimStatus enum tests ---

    @Test
    fun `TrimStatus optimal range is within plus minus 3`() {
        assertEquals(-3.0, FuelTrimAnalyzer.TrimStatus.OPTIMAL.minTrim, 0.001)
        assertEquals(3.0, FuelTrimAnalyzer.TrimStatus.OPTIMAL.maxTrim, 0.001)
    }

    @Test
    fun `TrimStatus labels are non-empty`() {
        FuelTrimAnalyzer.TrimStatus.entries.forEach { status ->
            assertTrue(status.label.isNotEmpty())
            assertTrue(status.description.isNotEmpty())
        }
    }

    // --- History limit tests ---

    @Test
    fun `history is capped at max size`() {
        for (i in 1..30) {
            analyzer.analyzeTrims(i.toDouble(), 0.0)
        }
        val trend = analyzer.getTrimTrend()
        assertTrue(trend.samples <= 20)
    }
}
