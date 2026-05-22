package com.canopobd.data.domain

import com.canopobd.data.model.BatteryHealth
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BatteryHealthAnalyzerTest {

    private lateinit var analyzer: BatteryHealthAnalyzer

    @Before
    fun setup() {
        analyzer = BatteryHealthAnalyzer()
    }

    private fun makeInput(
        voltage: Double = 12.6,
        engineRpm: Double = 0.0,
        alternatorDuty: Double = 0.0,
        controlModuleVoltage: Double = 0.0,
        voltageHistory: List<Double> = listOf(voltage),
        activeDTCs: List<String> = emptyList(),
        totalKm: Double = 20000.0
    ) = BatteryHealthAnalyzer.BatteryInput(
        voltageHistory = voltageHistory,
        currentVoltage = voltage,
        engineRpm = engineRpm,
        alternatorDuty = alternatorDuty,
        controlModuleVoltage = controlModuleVoltage,
        activeDTCs = activeDTCs,
        totalKm = totalKm
    )

    // --- Battery Voltage Status Tests ---

    @Test
    fun `good battery voltage returns healthy status`() {
        val result = analyzer.analyze(makeInput(voltage = 12.6, engineRpm = 0.0))
        assertEquals(BatteryHealth.GOOD, result.status.health)
        assertTrue(result.healthScore >= 80)
    }

    @Test
    fun `low battery voltage returns warning`() {
        val result = analyzer.analyze(makeInput(voltage = 11.7, engineRpm = 0.0))
        assertTrue(result.healthScore < 90)
    }

    @Test
    fun `critical battery voltage`() {
        val result = analyzer.analyze(makeInput(voltage = 11.3, engineRpm = 0.0))
        assertTrue(result.healthScore < 80)
    }

    // --- Charging System Tests ---

    @Test
    fun `charging system check - alternator working`() {
        val result = analyzer.analyze(
            makeInput(
                voltage = 14.2,
                engineRpm = 2000.0,
                alternatorDuty = 50.0,
                controlModuleVoltage = 14.2
            )
        )
        assertEquals(
            BatteryHealthAnalyzer.ChargingSystemHealth.HEALTHY,
            result.chargingSystemHealth
        )
    }

    @Test
    fun `charging system check - alternator weak`() {
        val result = analyzer.analyze(
            makeInput(
                voltage = 13.5,
                engineRpm = 2000.0,
                alternatorDuty = 85.0,
                controlModuleVoltage = 14.0
            )
        )
        assertEquals(
            BatteryHealthAnalyzer.ChargingSystemHealth.WEAK,
            result.chargingSystemHealth
        )
    }

    @Test
    fun `charging system check - alternator failing`() {
        val result = analyzer.analyze(
            makeInput(
                voltage = 13.0,
                engineRpm = 3000.0,
                alternatorDuty = 95.0,
                controlModuleVoltage = 14.0
            )
        )
        assertEquals(
            BatteryHealthAnalyzer.ChargingSystemHealth.FAULTY,
            result.chargingSystemHealth
        )
    }

    @Test
    fun `charging system unknown when engine off`() {
        val result = analyzer.analyze(makeInput(voltage = 12.6, engineRpm = 0.0))
        assertEquals(
            BatteryHealthAnalyzer.ChargingSystemHealth.UNKNOWN,
            result.chargingSystemHealth
        )
    }

    // --- SOC Calculation Tests ---

    @Test
    fun `SOC calculation - full charge at 12_7V`() {
        val soc = analyzer.estimateStateOfCharge(12.7, isCharging = false)
        assertEquals(100, soc)
    }

    @Test
    fun `SOC calculation - 50 percent at 12_2V`() {
        val soc = analyzer.estimateStateOfCharge(12.2, isCharging = false)
        assertEquals(50, soc)
    }

    @Test
    fun `SOC calculation - empty at 11_8V`() {
        val soc = analyzer.estimateStateOfCharge(11.8, isCharging = false)
        assertTrue(soc in 0..25)
    }

    @Test
    fun `SOC calculation - returns -1 when charging`() {
        val soc = analyzer.estimateStateOfCharge(14.0, isCharging = true)
        assertEquals(-1, soc)
    }

    @Test
    fun `SOC calculation - at 12_4V is 75 percent`() {
        val soc = analyzer.estimateStateOfCharge(12.4, isCharging = false)
        assertEquals(75, soc)
    }

    @Test
    fun `SOC calculation - at 12_0V is 25 percent`() {
        val soc = analyzer.estimateStateOfCharge(12.0, isCharging = false)
        assertEquals(25, soc)
    }

    @Test
    fun `SOC calculation - below 11_5V is 0 percent`() {
        val soc = analyzer.estimateStateOfCharge(11.0, isCharging = false)
        assertEquals(0, soc)
    }

    @Test
    fun `SOC calculation - above 12_7V is 100 percent`() {
        val soc = analyzer.estimateStateOfCharge(13.0, isCharging = false)
        assertEquals(100, soc)
    }

    // --- DTC Detection Tests ---

    @Test
    fun `detectChargingSystemDTCs filters charging related codes`() {
        val dtcs = listOf("P0562", "P0301", "P0563", "P0171", "P0620")
        val result = analyzer.detectChargingSystemDTCs(dtcs)
        assertEquals(3, result.size)
        assertTrue(result.contains("P0562"))
        assertTrue(result.contains("P0563"))
    }

    @Test
    fun `detectChargingSystemDTCs returns empty for unrelated codes`() {
        val dtcs = listOf("P0301", "P0171", "P0420")
        val result = analyzer.detectChargingSystemDTCs(dtcs)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detectChargingSystemDTCs handles case insensitivity`() {
        val dtcs = listOf("p0562", "P0621")
        val result = analyzer.detectChargingSystemDTCs(dtcs)
        assertEquals(2, result.size)
    }

    @Test
    fun `detectChargingSystemDTCs includes P0622`() {
        val dtcs = listOf("P0622")
        val result = analyzer.detectChargingSystemDTCs(dtcs)
        assertEquals(1, result.size)
    }

    // --- Health Score and Diagnosis Tests ---

    @Test
    fun `healthy battery has good diagnosis`() {
        val result = analyzer.analyze(makeInput(voltage = 12.6, engineRpm = 0.0))
        assertTrue(result.diagnosis.contains("Batterie in Ordnung"))
    }

    @Test
    fun `poor battery has renewal recommendation`() {
        val result = analyzer.analyze(makeInput(voltage = 11.4, engineRpm = 0.0))
        assertTrue(result.recommendation.isNotEmpty())
    }

    @Test
    fun `health score is in valid range`() {
        val result = analyzer.analyze(makeInput(voltage = 12.6))
        assertTrue(result.healthScore in 0..100)
    }

    // --- Voltage Trend Tests ---

    @Test
    fun `voltage trend is stable for constant history`() {
        val history = listOf(12.6, 12.6, 12.6, 12.6, 12.6)
        val result = analyzer.analyze(makeInput(voltage = 12.6, voltageHistory = history))
        assertEquals(BatteryHealthAnalyzer.VoltageTrend.STABLE, result.voltageTrend)
    }

    @Test
    fun `voltage trend is falling for decreasing history`() {
        val history = listOf(12.6, 12.4, 12.2, 12.0, 11.8)
        val result = analyzer.analyze(makeInput(voltage = 11.8, voltageHistory = history))
        assertEquals(BatteryHealthAnalyzer.VoltageTrend.FALLING, result.voltageTrend)
    }

    @Test
    fun `voltage trend is rising for increasing history`() {
        val history = listOf(12.0, 12.2, 12.4, 12.6, 12.8)
        val result = analyzer.analyze(makeInput(voltage = 12.8, voltageHistory = history))
        assertEquals(BatteryHealthAnalyzer.VoltageTrend.RISING, result.voltageTrend)
    }

    @Test
    fun `voltage trend is stable with insufficient history`() {
        val history = listOf(12.6, 12.6)
        val result = analyzer.analyze(makeInput(voltage = 12.6, voltageHistory = history))
        assertEquals(BatteryHealthAnalyzer.VoltageTrend.STABLE, result.voltageTrend)
    }

    // --- Ripple / Stability Tests ---

    @Test
    fun `ripple is zero for constant voltage history`() {
        val history = listOf(12.6, 12.6, 12.6, 12.6, 12.6)
        val result = analyzer.analyze(makeInput(voltage = 12.6, voltageHistory = history))
        assertEquals(0.0, result.rippleAmplitude, 0.001)
    }

    @Test
    fun `ripple reflects peak-to-peak of voltage history`() {
        val history = listOf(12.0, 13.0, 12.0, 13.0, 12.0)
        val result = analyzer.analyze(makeInput(voltage = 12.0, voltageHistory = history))
        assertTrue(result.rippleAmplitude > 0.5)
    }

    // --- DTC Impact Tests ---

    @Test
    fun `P0562 DTC lowers health score`() {
        val clean = analyzer.analyze(makeInput(voltage = 12.6, activeDTCs = emptyList()))
        val withDtc = analyzer.analyze(makeInput(voltage = 12.6, activeDTCs = listOf("P0562")))
        assertTrue(clean.healthScore > withDtc.healthScore)
    }

    @Test
    fun `P0563 DTC has stronger impact than P0562`() {
        val withP0562 = analyzer.analyze(makeInput(voltage = 12.6, activeDTCs = listOf("P0562")))
        val withP0563 = analyzer.analyze(makeInput(voltage = 12.6, activeDTCs = listOf("P0563")))
        assertTrue(withP0562.healthScore > withP0563.healthScore)
    }

    @Test
    fun `multiple charging DTCs compound penalty`() {
        val single = analyzer.analyze(makeInput(voltage = 12.6, activeDTCs = listOf("P0620")))
        val multiple = analyzer.analyze(
            makeInput(voltage = 12.6, activeDTCs = listOf("P0620", "P0621"))
        )
        assertTrue(single.healthScore >= multiple.healthScore)
    }

    // --- Estimated CCA Tests ---

    @Test
    fun `estimated CCA is high for new battery`() {
        val result = analyzer.analyze(makeInput(voltage = 12.7, totalKm = 10000.0))
        assertTrue(result.estimatedCca >= 600)
    }

    @Test
    fun `estimated CCA drops with high mileage`() {
        val lowKm = analyzer.analyze(makeInput(voltage = 12.7, totalKm = 10000.0))
        val highKm = analyzer.analyze(makeInput(voltage = 12.7, totalKm = 120000.0))
        assertTrue(lowKm.estimatedCca > highKm.estimatedCca)
    }

    @Test
    fun `estimated CCA drops with low voltage`() {
        val goodVoltage = analyzer.analyze(makeInput(voltage = 12.7, totalKm = 20000.0))
        val lowVoltage = analyzer.analyze(makeInput(voltage = 11.8, totalKm = 20000.0))
        assertTrue(goodVoltage.estimatedCca > lowVoltage.estimatedCca)
    }

    // --- Edge Cases ---

    @Test
    fun `empty voltage history does not crash`() {
        val result = analyzer.analyze(makeInput(voltage = 12.6, voltageHistory = emptyList()))
        assertNotNull(result)
        assertTrue(result.healthScore in 0..100)
    }

    @Test
    fun `analysis with all default inputs`() {
        val input = BatteryHealthAnalyzer.BatteryInput(
            voltageHistory = listOf(12.6),
            currentVoltage = 12.6
        )
        val result = analyzer.analyze(input)
        assertNotNull(result)
        assertEquals(BatteryHealth.GOOD, result.status.health)
    }

    @Test
    fun `overcharging voltage results in low voltage score`() {
        val result = analyzer.analyze(
            makeInput(voltage = 15.5, engineRpm = 3000.0, alternatorDuty = 50.0)
        )
        assertTrue(result.healthScore < 80)
    }

    @Test
    fun `BatteryInput defaults are correct`() {
        val input = BatteryHealthAnalyzer.BatteryInput(
            voltageHistory = emptyList(),
            currentVoltage = 0.0
        )
        assertEquals(0.0, input.engineRpm, 0.001)
        assertEquals(0.0, input.alternatorDuty, 0.001)
        assertEquals(0.0, input.controlModuleVoltage, 0.001)
        assertTrue(input.activeDTCs.isEmpty())
        assertEquals(0.0, input.coolantTemp, 0.001)
        assertEquals(0.0, input.totalKm, 0.001)
    }

    @Test
    fun `VoltageTrend enum has correct labels`() {
        assertEquals("Steigend", BatteryHealthAnalyzer.VoltageTrend.RISING.label)
        assertEquals("Stabil", BatteryHealthAnalyzer.VoltageTrend.STABLE.label)
        assertEquals("Fallend", BatteryHealthAnalyzer.VoltageTrend.FALLING.label)
        assertEquals("Schwankend", BatteryHealthAnalyzer.VoltageTrend.OSCILLATING.label)
    }

    @Test
    fun `ChargingSystemHealth enum has correct labels`() {
        assertEquals("Gesund", BatteryHealthAnalyzer.ChargingSystemHealth.HEALTHY.label)
        assertEquals("Schwach", BatteryHealthAnalyzer.ChargingSystemHealth.WEAK.label)
        assertEquals("Defekt", BatteryHealthAnalyzer.ChargingSystemHealth.FAULTY.label)
        assertEquals("Unbekannt", BatteryHealthAnalyzer.ChargingSystemHealth.UNKNOWN.label)
    }
}
