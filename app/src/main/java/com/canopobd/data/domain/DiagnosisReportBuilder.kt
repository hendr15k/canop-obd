package com.canopobd.data.domain

import com.canopobd.data.model.DTCResponse
import com.canopobd.data.model.FreezeFrame
import com.canopobd.data.model.OBDData
import java.time.Instant

data class DiagnosisFinding(
    val system: String,
    val status: String,
    val healthScore: Int,
    val detail: String
)

data class DiagnosisReport(
    val generatedAt: Long,
    val appVersion: String,
    val vehicleVin: String,
    val odometerKm: Int,
    val protocol: String,
    val storedCodes: List<String>,
    val pendingCodes: List<String>,
    val freezeFrameCount: Int,
    val findings: List<DiagnosisFinding>,
    val overallScore: Int,
    val criticalWarnings: List<String>
)

data class AnalyzerSnapshot(
    val batteryAnalysis: BatteryHealthAnalyzer.BatteryAnalysis? = null,
    val egrAnalysis: EGRHealthAnalyzer.EGRAnalysis? = null,
    val evapAnalysis: EVAPSystemAnalyzer.EVAPAnalysis? = null,
    val saiAnalysis: SecondaryAirAnalyzer.SAIAnalysis? = null,
    val lambdaAnalysis: LambdaO2SensorAnalyzer.LambdaAnalysis? = null,
    val emissionsReadiness: EmissionsReadinessAnalyzer.ReadinessAnalysis? = null,
    val oilCondition: OilConditionMonitor.OilAnalysis? = null,
    val pcv: PCVMonitor.PCVAnalysis? = null,
    val gearbox: M32GearboxMonitor.GearboxAnalysis? = null,
    val chain: ChainTensionerAnalyzer.ChainTensionerAnalysis? = null,
    val egt: EGTMonitor.EGTAnalysis? = null,
    val coolant: CoolantSystemHealth.CoolantAnalysis? = null,
    val oilPrediction: OilHealthPredictor.OilHealthPredictionResult? = null,
    val turboSpool: TurboSpoolAnalyzer.SpoolAnalysis? = null,
    val turboEfficiency: TurboEfficiencyAnalyzer.TurboEfficiencyAnalysis? = null,
    val boostLeak: BoostLeakDetector.BoostLeakAnalysis? = null,
    val wastegate: WastegateHealthAnalyzer.WastegateAnalysis? = null,
    val sensorHealth: SensorHealthMonitor.SensorHealthSummary? = null,
    val fuelSystem: FuelSystemAnalyzer.FuelSystemAnalysis? = null,
    val warmup: EngineWarmupMonitor.WarmupAnalysis? = null,
    val driveStyle: DriveStyleAnalyzer.DriveStyleAnalysis? = null,
    val efficiency: DrivingEfficiencyScorer.EfficiencyScore? = null
)

@Suppress("TooManyFunctions")
object DiagnosisReportBuilder {

    private const val SCORE_CRITICAL_MAX = 39
    private const val SCORE_WARN_MAX = 69
    private const val SCORE_OK_MAX = 89
    private const val SCORE_MIN = 0
    private const val SCORE_MAX = 100
    private const val COOLANT_OVERHEAT_C = 105.0

    @Suppress("LongParameterList")
    fun build(
        obdData: OBDData,
        dtcResponse: DTCResponse?,
        freezeFrames: List<FreezeFrame>,
        protocol: String,
        odometerKm: Int,
        appVersion: String,
        snapshot: AnalyzerSnapshot = AnalyzerSnapshot(),
        generatedAt: Long = System.currentTimeMillis()
    ): DiagnosisReport {
        val findings = collectFindings(snapshot)
        val scores = findings.map { it.healthScore }.filter { it in SCORE_MIN..SCORE_MAX }
        return DiagnosisReport(
            generatedAt = generatedAt,
            appVersion = appVersion,
            vehicleVin = obdData.vin.ifBlank { "unbekannt" },
            odometerKm = odometerKm,
            protocol = protocol.ifBlank { "unbekannt" },
            storedCodes = dtcResponse?.codes?.map { "${it.code} — ${it.description}" } ?: emptyList(),
            pendingCodes = dtcResponse?.pendingCodes?.map { "${it.code} — ${it.description}" }
                ?: emptyList(),
            freezeFrameCount = freezeFrames.size,
            findings = findings,
            overallScore = if (scores.isEmpty()) 0 else scores.sum() / scores.size,
            criticalWarnings = collectWarnings(obdData, dtcResponse, findings)
        )
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    private fun collectFindings(s: AnalyzerSnapshot): List<DiagnosisFinding> = buildList {
        addEmissionFindings(s, this)
        addMechanicalFindings(s, this)
        addTurboFindings(s, this)
        addMiscFindings(s, this)
    }

    private fun addEmissionFindings(s: AnalyzerSnapshot, out: MutableList<DiagnosisFinding>) {
        s.batteryAnalysis?.let {
            out.add(DiagnosisFinding("Batterie", it.status.health.name, it.healthScore, it.diagnosis))
        }
        s.egrAnalysis?.let {
            out.add(DiagnosisFinding("AGR", it.health.status.name, it.healthScore, it.diagnosis))
        }
        s.evapAnalysis?.let {
            out.add(DiagnosisFinding("EVAP", statusOf(it.healthScore), it.healthScore, it.diagnosis))
        }
        s.saiAnalysis?.let {
            out.add(DiagnosisFinding("Sekundaerluft", statusOf(it.healthScore), it.healthScore, it.diagnosis))
        }
        s.lambdaAnalysis?.let {
            val score = it.overallHealthScore
            out.add(DiagnosisFinding("Lambda/Kat", statusOf(score), score, it.diagnosis))
        }
        s.emissionsReadiness?.let {
            val status = if (it.allComplete) "BEREIT" else "OFFEN"
            out.add(DiagnosisFinding("Readiness", status, it.overallScore, it.diagnosis))
        }
    }

    private fun addMechanicalFindings(s: AnalyzerSnapshot, out: MutableList<DiagnosisFinding>) {
        s.oilCondition?.let {
            out.add(DiagnosisFinding("Oel", it.condition.name, it.healthScore, it.diagnosis))
        }
        s.pcv?.let { out.add(DiagnosisFinding("PCV", it.health.name, it.healthScore, it.diagnosis)) }
        s.gearbox?.let {
            out.add(DiagnosisFinding("Getriebe M32", it.health.name, it.healthScore, it.diagnosis))
        }
        s.chain?.let {
            out.add(DiagnosisFinding("Steuerkette", it.health.name, it.healthScore, it.diagnosis))
        }
        s.egt?.let {
            out.add(DiagnosisFinding("Abgastemperatur", it.status.name, it.healthScore, it.diagnosis))
        }
        s.coolant?.let {
            out.add(DiagnosisFinding("Kuehlsystem", it.status.name, it.healthScore, it.diagnosis))
        }
        s.oilPrediction?.let {
            out.add(DiagnosisFinding("Oel-Prognose", it.prediction.name, it.healthScore, it.diagnosis))
        }
    }

    private fun addTurboFindings(s: AnalyzerSnapshot, out: MutableList<DiagnosisFinding>) {
        s.turboSpool?.let {
            out.add(DiagnosisFinding("Turbo-Ansprung", it.status.name, it.healthScore, it.diagnosis))
        }
        s.turboEfficiency?.let {
            out.add(DiagnosisFinding("Turbo-Effizienz", it.efficiency.name, it.healthScore, it.diagnosis))
        }
        s.boostLeak?.let {
            out.add(DiagnosisFinding("Ladeluftsystem", it.severity.name, it.healthScore, it.diagnosis))
        }
        s.wastegate?.let {
            out.add(DiagnosisFinding("Wastegate", it.condition.name, it.healthScore, it.diagnosis))
        }
    }

    private fun addMiscFindings(s: AnalyzerSnapshot, out: MutableList<DiagnosisFinding>) {
        s.sensorHealth?.let {
            out.add(
                DiagnosisFinding(
                    "Sensoren",
                    it.overallStatus.name,
                    it.overallHealthScore,
                    it.criticalIssues.firstOrNull() ?: "Alle Sensoren plausibel"
                )
            )
        }
        s.fuelSystem?.let {
            out.add(DiagnosisFinding("Kraftstoffsystem", it.health.name, it.healthScore, it.diagnosis))
        }
        s.warmup?.let {
            out.add(DiagnosisFinding("Kaltstartschutz", it.phase.name, it.healthScore, it.diagnosis))
        }
        s.driveStyle?.let {
            out.add(DiagnosisFinding("Fahrstil", it.driveStyle.name, it.ecoScore, it.feedback))
        }
        s.efficiency?.let {
            out.add(
                DiagnosisFinding("Fahreffizienz", statusOf(it.overall), it.overall, it.tips.firstOrNull() ?: "")
            )
        }
    }

    private fun collectWarnings(
        obdData: OBDData,
        dtcResponse: DTCResponse?,
        findings: List<DiagnosisFinding>
    ): List<String> = buildList {
        dtcResponse?.codes?.forEach { add("${it.code}: ${it.description}") }
        findings.filter { it.healthScore in SCORE_MIN..SCORE_CRITICAL_MAX && it.detail.isNotBlank() }
            .forEach { add("${it.system}: ${it.detail}") }
        if (obdData.coolantTemp > COOLANT_OVERHEAT_C) {
            add("Kuehlmittel ueberhitzt (${obdData.coolantTemp.toInt()} °C)")
        }
    }

    fun renderText(report: DiagnosisReport): String {
        val sb = StringBuilder()
        sb.appendLine("canop-obd Diagnosebericht")
        sb.appendLine("Erstellt: ${Instant.ofEpochMilli(report.generatedAt)}")
        sb.appendLine("App: v${report.appVersion} | VIN: ${report.vehicleVin} | km: ${report.odometerKm}")
        sb.appendLine("Protokoll: ${report.protocol}")
        sb.appendLine()
        sb.appendLine("Gesamtbewertung: ${report.overallScore}/100 (${statusOf(report.overallScore)})")
        sb.appendLine()
        appendCodes(sb, report)
        sb.appendLine("Freeze Frames: ${report.freezeFrameCount}")
        sb.appendLine()
        appendFindings(sb, report)
        appendWarnings(sb, report)
        return sb.toString()
    }

    private fun appendCodes(sb: StringBuilder, report: DiagnosisReport) {
        if (report.storedCodes.isEmpty() && report.pendingCodes.isEmpty()) {
            sb.appendLine("Fehlercodes: keine gespeichert")
        } else {
            sb.appendLine("Fehlercodes (gespeichert):")
            report.storedCodes.forEach { sb.appendLine("  [ST] $it") }
            report.pendingCodes.forEach { sb.appendLine("  [PD] $it") }
        }
    }

    private fun appendFindings(sb: StringBuilder, report: DiagnosisReport) {
        if (report.findings.isEmpty()) {
            sb.appendLine("Systembefunde: keine Analysedaten (noch keine Fahrt aufgezeichnet)")
        } else {
            sb.appendLine("Systembefunde:")
            report.findings.forEach {
                sb.appendLine("  - ${it.system}: ${it.status} (${it.healthScore}/100)")
                if (it.detail.isNotBlank()) sb.appendLine("      ${it.detail}")
            }
        }
    }

    private fun appendWarnings(sb: StringBuilder, report: DiagnosisReport) {
        if (report.criticalWarnings.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Kritische Hinweise:")
            report.criticalWarnings.forEach { sb.appendLine("  ! $it") }
        }
    }

    private fun statusOf(score: Int): String = when {
        score < 0 -> "KEINE DATEN"
        score <= SCORE_CRITICAL_MAX -> "KRITISCH"
        score <= SCORE_WARN_MAX -> "AUFFAELLIG"
        score <= SCORE_OK_MAX -> "OK"
        else -> "GUT"
    }
}
