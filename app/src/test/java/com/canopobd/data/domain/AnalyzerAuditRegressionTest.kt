package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Regression tests for audit findings (v1.8.0 dev):
 *
 * 1. PCVMonitor: a PCV-relevant DTC (P1100/P1101) must never yield
 *    PCVHealth.HEALTHY, even if the remaining sub-scores are perfect
 *    (previously score >= 75 short-circuited to HEALTHY, ignoring the DTC).
 * 2. ChainTensionerAnalyzer: penalty gradation was clamped by
 *    coerceAtMost(weight) so mid-range values always produced the same
 *    flat penalty/score; penalties must actually rise with severity.
 * 3. SensorHealthMonitor: analyzeEGT existed but was never wired into
 *    analyzeSensors, so EGT health never appeared in the summary.
 */
class AnalyzerAuditRegressionTest {

    private lateinit var pcv: PCVMonitor
    private lateinit var chain: ChainTensionerAnalyzer
    private lateinit var sensorMonitor: SensorHealthMonitor

    @Before
    fun setup() {
        pcv = PCVMonitor()
        chain = ChainTensionerAnalyzer()
        sensorMonitor = SensorHealthMonitor()
    }

    private fun healthyPcvInput() = PCVMonitor.PCVInput(
        activeDTCs = emptyList(),
        mafRate = 5.0,
        mafExpectedAtRpm = 5.0,
        stft = 0.0,
        ltft = 0.0,
        oilConsumptionLPer1000Km = 0.1
    )

    @Test
    fun `pcv dtc never yields healthy`() {
        val result = pcv.analyze(healthyPcvInput().copy(activeDTCs = listOf("P1100")))

        assertEquals(PCVMonitor.PCVHealth.PLUGGED, result.health)
    }

    @Test
    fun `pcv without dtc and healthy values stays healthy`() {
        val result = pcv.analyze(healthyPcvInput())

        assertEquals(PCVMonitor.PCVHealth.HEALTHY, result.health)
        assertTrue(result.healthScore >= 75)
    }

    @Test
    fun `chain rattle penalty rises with duration`() {
        val mild = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = emptyList(),
                coldStartRattleDurationSec = 2.5
            )
        )
        val severe = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = emptyList(),
                coldStartRattleDurationSec = 4.5
            )
        )

        assertTrue(mild.rattlePenalty > 0)
        assertTrue(severe.rattlePenalty > mild.rattlePenalty)
        assertTrue(severe.healthScore < mild.healthScore)
    }

    @Test
    fun `chain rpm stability penalty rises beyond warning threshold`() {
        val warn = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = emptyList(),
                currentRpm = 800.0,
                idleRpmVariance = 31.0
            )
        )
        val worse = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = emptyList(),
                currentRpm = 800.0,
                idleRpmVariance = 70.0
            )
        )

        assertTrue(worse.rpmStabilityPenalty > warn.rpmStabilityPenalty)
    }

    @Test
    fun `chain timing variance penalty rises beyond warning threshold`() {
        val warn = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = emptyList(),
                timingAdvanceVariance = 4.5
            )
        )
        val worse = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = emptyList(),
                timingAdvanceVariance = 16.0
            )
        )

        assertTrue(worse.timingVariancePenalty > warn.timingVariancePenalty)
    }

    @Test
    fun `sensor summary contains egt`() {
        val summary = sensorMonitor.analyzeSensors(com.canopobd.data.model.OBDData())

        assertTrue(summary.sensorHealths.containsKey(SensorHealthMonitor.SensorType.EGT))
    }

    @Test
    fun `sensor summary flags critical egt`() {
        val summary = sensorMonitor.analyzeSensors(com.canopobd.data.model.OBDData(egtBank1 = 1200.0))

        assertEquals(
            SensorHealthMonitor.HealthStatus.CRITICAL,
            summary.sensorHealths[SensorHealthMonitor.SensorType.EGT]?.status
        )
        assertTrue(summary.criticalIssues.any { it.startsWith("EGT") })
    }

    @Test
    fun `lambda analysis exposes overall health score`() {
        val analyzer = LambdaO2SensorAnalyzer()
        val healthy = analyzer.analyze(
            LambdaO2SensorAnalyzer.LambdaInput(
                o2VoltageB1S1 = 0.45,
                o2VoltageB1S2 = 0.6,
                coolantTemp = 90.0,
                rpm = 2000.0,
                voltageHistoryB1S1 = List(20) { if (it % 2 == 0) 0.2 else 0.8 }
            )
        )
        val broken = analyzer.analyze(LambdaO2SensorAnalyzer.LambdaInput())

        assertTrue(healthy.overallHealthScore in 0..100)
        assertTrue(broken.overallHealthScore in 0..100)
        assertTrue(healthy.overallHealthScore > broken.overallHealthScore)
    }
}
