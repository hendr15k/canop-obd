package com.canopobd.data.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the previously uncovered mechanical analyzers:
 * oil, PCV, timing chain, coolant and turbo efficiency.
 */
class MechanicalAnalyzerTest {

    private lateinit var oil: OilConditionMonitor
    private lateinit var pcv: PCVMonitor
    private lateinit var chain: ChainTensionerAnalyzer
    private lateinit var coolant: CoolantSystemHealth
    private lateinit var turbo: TurboEfficiencyAnalyzer

    @Before
    fun setup() {
        oil = OilConditionMonitor()
        pcv = PCVMonitor()
        chain = ChainTensionerAnalyzer()
        coolant = CoolantSystemHealth()
        turbo = TurboEfficiencyAnalyzer()
    }

    @Test
    fun `oil warm idle stays healthy with life remaining`() {
        val result = oil.analyze(
            OilConditionMonitor.OilInput(
                oilTemp = 95.0,
                coolantTemp = 90.0,
                rpm = 800.0,
                engineLoad = 25.0,
                oilPressure = 2.0
            )
        )

        assertEquals(OilConditionMonitor.OilCondition.EXCELLENT, result.condition)
        assertTrue(result.remainingKm > 0)
        assertTrue(result.remainingDays > 0)
        assertTrue(result.oilType.isNotBlank())
    }

    @Test
    fun `oil overheating degrades temperature health`() {
        val warm = oil.analyze(OilConditionMonitor.OilInput(oilTemp = 95.0))
        val hot = oil.analyze(OilConditionMonitor.OilInput(oilTemp = 118.0))

        assertTrue(hot.temperatureHealth < warm.temperatureHealth)
    }

    @Test
    fun `oil overrun interval is critical`() {
        val result = oil.analyze(
            OilConditionMonitor.OilInput(oilTemp = 95.0, totalKm = 30_000.0, lastOilChangeKm = 0.0)
        )

        assertEquals(OilConditionMonitor.OilCondition.CRITICAL, result.condition)
        assertEquals(0, result.remainingKm)
    }

    @Test
    fun `pcv healthy input is healthy`() {
        val result = pcv.analyze(
            PCVMonitor.PCVInput(
                activeDTCs = emptyList(),
                mafRate = 5.0,
                mafExpectedAtRpm = 5.0,
                stft = 0.0,
                ltft = 0.0,
                oilConsumptionLPer1000Km = 0.1
            )
        )

        assertEquals(PCVMonitor.PCVHealth.HEALTHY, result.health)
        assertEquals(0.0, result.mafDeviation, 0.001)
        assertEquals(0.0, result.totalTrimDeviation, 0.001)
    }

    @Test
    fun `pcv excessive trims degrade score`() {
        val healthy = pcv.analyze(
            PCVMonitor.PCVInput(
                activeDTCs = emptyList(),
                mafRate = 5.0,
                mafExpectedAtRpm = 5.0,
                stft = 0.0,
                ltft = 0.0,
                oilConsumptionLPer1000Km = 0.1
            )
        )
        val lean = pcv.analyze(
            PCVMonitor.PCVInput(
                activeDTCs = emptyList(),
                mafRate = 5.0,
                mafExpectedAtRpm = 5.0,
                stft = 12.0,
                ltft = 8.0,
                oilConsumptionLPer1000Km = 0.1
            )
        )

        assertTrue(lean.healthScore < healthy.healthScore)
    }

    @Test
    fun `chain idle without symptoms is healthy`() {
        val result = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(activeDTCs = emptyList())
        )

        assertEquals(ChainTensionerAnalyzer.ChainTensionerHealth.HEALTHY, result.health)
        assertEquals(100, result.healthScore)
        assertEquals(0, result.rattlePenalty)
    }

    @Test
    fun `chain correlation dtc forces critical at low score`() {
        val result = chain.analyze(
            ChainTensionerAnalyzer.ChainTensionerInput(
                activeDTCs = listOf("P0016", "P0017", "P1345"),
                coldStartRattleDurationSec = 6.0
            )
        )

        assertEquals(ChainTensionerAnalyzer.ChainTensionerHealth.CRITICAL, result.health)
    }

    @Test
    fun `coolant operating temperature is healthy`() {
        val result = coolant.analyze(
            CoolantSystemHealth.CoolantInput(coolantTemp = 92.0, rpm = 2000.0)
        )

        assertTrue(result.healthScore in 0..100)
        assertEquals(0, result.leakProbability)
        assertTrue(result.diagnosis.isNotBlank())
    }

    @Test
    fun `coolant overheating reports overheating status`() {
        val result = coolant.analyze(CoolantSystemHealth.CoolantInput(coolantTemp = 118.0))

        assertEquals(CoolantSystemHealth.CoolantSystemStatus.OVERHEATING, result.status)
    }

    @Test
    fun `coolant cold engine never reports overheating`() {
        val result = coolant.analyze(
            CoolantSystemHealth.CoolantInput(coolantTemp = 20.0, engineRuntimeSec = 60.0)
        )

        assertTrue(result.status != CoolantSystemHealth.CoolantSystemStatus.OVERHEATING)
    }

    @Test
    fun `turbo matched boost is not failing`() {
        val result = turbo.analyze(
            TurboEfficiencyAnalyzer.TurboInput(
                boostActualBar = 0.7,
                boostTargetBar = 0.7,
                wastegateDuty = 40.0
            )
        )

        assertTrue(result.efficiency != TurboEfficiencyAnalyzer.TurboEfficiency.FAILING)
        assertTrue(result.healthScore in 0..100)
    }

    @Test
    fun `turbo overboost beyond max is failing`() {
        val result = turbo.analyze(
            TurboEfficiencyAnalyzer.TurboInput(
                boostActualBar = 1.5,
                boostTargetBar = 0.7,
                wastegateDuty = 40.0
            )
        )

        assertEquals(TurboEfficiencyAnalyzer.TurboEfficiency.FAILING, result.efficiency)
    }

    @Test
    fun `turbo severe underboost degrades efficiency`() {
        val healthy = turbo.analyze(
            TurboEfficiencyAnalyzer.TurboInput(
                boostActualBar = 0.7,
                boostTargetBar = 0.7,
                wastegateDuty = 40.0
            )
        )
        val leaking = turbo.analyze(
            TurboEfficiencyAnalyzer.TurboInput(
                boostActualBar = 0.2,
                boostTargetBar = 0.7,
                wastegateDuty = 40.0,
                throttle = 80.0
            )
        )

        assertTrue(leaking.healthScore < healthy.healthScore)
    }
}
