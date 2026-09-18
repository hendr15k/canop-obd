package com.canopobd.data.domain

import com.canopobd.data.model.DTCResponse
import com.canopobd.data.model.DiagnosticTroubleCode
import com.canopobd.data.model.OBDData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosisReportBuilderTest {

    private fun storedResponse() = DTCResponse(
        codes = listOf(DiagnosticTroubleCode("P0300", "Zündaussetzer erkannt")),
        pendingCodes = listOf(DiagnosticTroubleCode("P0171", "Gemisch zu mager"))
    )

    @Test
    fun `empty state yields zero score and no findings`() {
        val report = DiagnosisReportBuilder.build(
            obdData = OBDData(),
            dtcResponse = null,
            freezeFrames = emptyList(),
            protocol = "",
            odometerKm = 0,
            appVersion = "1.8.0",
            generatedAt = 0L
        )

        assertEquals(0, report.overallScore)
        assertTrue(report.findings.isEmpty())
        assertTrue(report.storedCodes.isEmpty())
        assertEquals("unbekannt", report.vehicleVin)
        assertEquals("unbekannt", report.protocol)
    }

    @Test
    fun `dtc codes appear in report and warnings`() {
        val report = DiagnosisReportBuilder.build(
            obdData = OBDData(),
            dtcResponse = storedResponse(),
            freezeFrames = emptyList(),
            protocol = "ISO 15765-4 CAN",
            odometerKm = 123456,
            appVersion = "1.8.0",
            generatedAt = 0L
        )

        assertEquals(listOf("P0300 — Zündaussetzer erkannt"), report.storedCodes)
        assertEquals(listOf("P0171 — Gemisch zu mager"), report.pendingCodes)
        assertTrue(report.criticalWarnings.any { it.contains("P0300") })
    }

    @Test
    fun `findings feed overall score`() {
        val battery = BatteryHealthAnalyzer.BatteryAnalysis(
            status = com.canopobd.data.model.BatteryStatus(12.6, 80, com.canopobd.data.model.BatteryHealth.GOOD, false),
            healthScore = 100,
            voltageTrend = BatteryHealthAnalyzer.VoltageTrend.STABLE,
            rippleAmplitude = 0.05,
            chargingSystemHealth = BatteryHealthAnalyzer.ChargingSystemHealth.HEALTHY,
            estimatedCca = 600,
            diagnosis = "Batterie gesund",
            recommendation = "keine Maßnahme"
        )
        val pcv = PCVMonitor.PCVAnalysis(
            health = PCVMonitor.PCVHealth.PLUGGED,
            healthScore = 20,
            mafDeviation = 30.0,
            totalTrimDeviation = 15.0,
            oilConsumptionStatus = "hoch",
            diagnosis = "PCV verstopft",
            recommendation = "Ventil prüfen"
        )

        val report = DiagnosisReportBuilder.build(
            obdData = OBDData(),
            dtcResponse = null,
            freezeFrames = emptyList(),
            protocol = "CAN",
            odometerKm = 90000,
            appVersion = "1.8.0",
            snapshot = AnalyzerSnapshot(
                batteryAnalysis = battery,
                pcv = pcv
            ),
            generatedAt = 0L
        )

        assertEquals(2, report.findings.size)
        assertEquals(60, report.overallScore)
        assertTrue(report.criticalWarnings.any { it.contains("PCV") })
    }

    @Test
    fun `overheating coolant adds warning`() {
        val report = DiagnosisReportBuilder.build(
            obdData = OBDData(coolantTemp = 110.0),
            dtcResponse = null,
            freezeFrames = emptyList(),
            protocol = "CAN",
            odometerKm = 50000,
            appVersion = "1.8.0",
            generatedAt = 0L
        )

        assertTrue(report.criticalWarnings.any { it.contains("ueberhitzt") })
    }

    @Test
    fun `rendered text contains all sections`() {
        val report = DiagnosisReportBuilder.build(
            obdData = OBDData(vin = "W0LPE6EC0DG000001"),
            dtcResponse = storedResponse(),
            freezeFrames = emptyList(),
            protocol = "CAN",
            odometerKm = 100000,
            appVersion = "1.8.0",
            generatedAt = 0L
        )
        val text = DiagnosisReportBuilder.renderText(report)

        assertTrue(text.contains("canop-obd Diagnosebericht"))
        assertTrue(text.contains("Gesamtbewertung"))
        assertTrue(text.contains("P0300"))
        assertTrue(text.contains("W0LPE6EC0DG000001"))
    }

    @Test
    fun `unknown scores are excluded from overall`() {
        val warmup = EngineWarmupMonitor.WarmupAnalysis(
            phase = EngineWarmupMonitor.WarmupPhase.NO_DATA,
            healthScore = -5,
            warmupProgress = 0.0,
            recommendedMaxRpm = 3000,
            recommendedMaxBoostBar = 0.0,
            coldViolations = 0,
            diagnosis = "",
            recommendation = ""
        )
        val report = DiagnosisReportBuilder.build(
            obdData = OBDData(),
            dtcResponse = null,
            freezeFrames = emptyList(),
            protocol = "CAN",
            odometerKm = 1,
            appVersion = "1.8.0",
            snapshot = AnalyzerSnapshot(warmup = warmup),
            generatedAt = 0L
        )

        assertEquals(1, report.findings.size)
        assertEquals(0, report.overallScore)
    }
}
