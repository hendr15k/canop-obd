package com.canopobd.ui.diagnostics

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canopobd.R
import com.canopobd.data.domain.DiagnosisFinding
import com.canopobd.data.domain.DiagnosisReport
import com.canopobd.data.domain.DiagnosisReportBuilder
import com.canopobd.data.repository.DiagnosticSnapshotPoint
import com.canopobd.data.repository.PdfReportExporter
import com.canopobd.ui.components.AppRadius
import com.canopobd.ui.components.DialogShell
import com.canopobd.ui.components.GlassCard
import com.canopobd.ui.components.GradientButton
import com.canopobd.ui.components.SectionHeader
import com.canopobd.ui.components.StatusPill
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val REPORT_SCORE_CRITICAL = 40
private const val REPORT_SCORE_WARN = 70
private const val REPORT_SCORE_OK = 90
private const val REPORT_PDF_MAX_FINDINGS = 40
private const val REPORT_SHARE_TITLE_TEXT = "Diagnosebericht teilen"
private const val REPORT_SHARE_TITLE_PDF = "Diagnosebericht (PDF)"
private const val REPORT_LOG_TAG = "DiagnosisReport"

@Suppress("LongMethod")
@Composable
fun DiagnosisReportDialog(
    report: DiagnosisReport,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    val dateText = remember(report.generatedAt) { formatReportDate(report.generatedAt) }
    val scoreColor = scoreAccent(report.overallScore, colors)

    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.diagnosis_report_title),
        eyebrow = stringResource(R.string.diagnosis_report_eyebrow, dateText),
        heightFraction = 0.9f
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { ReportScoreCard(report = report, scoreColor = scoreColor) }
            item { SectionHeader(title = stringResource(R.string.diagnosis_report_dtcs), icon = Icons.Filled.Error) }
            item { ReportDtcSection(report = report) }
            item {
                SectionHeader(
                    title = stringResource(R.string.diagnosis_report_findings, report.findings.size),
                    icon = Icons.Filled.Analytics
                )
            }
            if (report.findings.isEmpty()) {
                item { ReportEmptyFindings() }
            } else {
                items(report.findings) { finding -> FindingRow(finding = finding) }
            }
            if (report.criticalWarnings.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.diagnosis_report_warnings),
                        icon = Icons.Filled.Warning
                    )
                }
                items(report.criticalWarnings) { warning -> ReportWarningRow(warning = warning) }
            }
            item { Spacer(Modifier.height(4.dp)) }
            item { ReportShareRow(report = report) }
        }
    }
}

@Composable
private fun ReportScoreCard(report: DiagnosisReport, scoreColor: Color) {
    val colors = LocalAppColors.current
    GlassCard(padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(scoreColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${report.overallScore}",
                    style = MaterialTheme.typography.titleLarge,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.diagnosis_report_score_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.diagnosis_report_meta,
                        report.vehicleVin,
                        report.odometerKm,
                        report.protocol
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ReportDtcSection(report: DiagnosisReport) {
    val colors = LocalAppColors.current
    if (report.storedCodes.isEmpty() && report.pendingCodes.isEmpty()) {
        GlassCard(padding = 12.dp) {
            Text(
                text = stringResource(R.string.diagnosis_report_no_dtcs),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.success
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            report.storedCodes.forEach { DtcReportRow(code = it, pending = false) }
            report.pendingCodes.forEach { DtcReportRow(code = it, pending = true) }
        }
    }
}

@Composable
private fun ReportEmptyFindings() {
    val colors = LocalAppColors.current
    GlassCard(padding = 12.dp) {
        Text(
            text = stringResource(R.string.diagnosis_report_no_findings),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun ReportWarningRow(warning: String) {
    val colors = LocalAppColors.current
    GlassCard(padding = 12.dp, accentEdge = colors.critical) {
        Text(
            text = warning,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun ReportShareRow(report: DiagnosisReport) {
    val context = LocalContext.current
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GradientButton(
            text = stringResource(R.string.diagnosis_report_share_text),
            onClick = { shareReportText(context, report) },
            icon = Icons.Filled.Share,
            gradient = colors.gradientAccent,
            modifier = Modifier.weight(1f)
        )
        GradientButton(
            text = stringResource(R.string.diagnosis_report_share_pdf),
            onClick = { shareReportPdf(context, report) },
            icon = Icons.Filled.PictureAsPdf,
            gradient = colors.gradientAccent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DtcReportRow(code: String, pending: Boolean) {
    val colors = LocalAppColors.current
    val accent = if (pending) colors.warning else colors.critical
    GlassCard(padding = 12.dp, accentEdge = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            StatusPill(text = if (pending) "PENDING" else "STORIERT", color = accent)
        }
    }
}

@Composable
private fun FindingRow(finding: DiagnosisFinding) {
    val colors = LocalAppColors.current
    val accent = scoreAccent(finding.healthScore, colors)
    GlassCard(padding = 12.dp, accentEdge = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = finding.system,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = finding.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "${finding.healthScore}",
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun scoreAccent(score: Int, colors: AppColors): Color = when {
    score < 0 -> colors.textTertiary
    score < REPORT_SCORE_CRITICAL -> colors.critical
    score < REPORT_SCORE_WARN -> colors.warning
    score < REPORT_SCORE_OK -> colors.info
    else -> colors.success
}

private fun formatReportDate(generatedAt: Long): String =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.GERMAN)
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(generatedAt))

private fun shareReportText(context: Context, report: DiagnosisReport) {
    val text = DiagnosisReportBuilder.renderText(report)
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, REPORT_SHARE_TITLE_TEXT))
    } catch (e: android.content.ActivityNotFoundException) {
        android.util.Log.e(REPORT_LOG_TAG, "No app to share report text", e)
    }
}

private fun shareReportPdf(context: Context, report: DiagnosisReport) {
    val codes = report.storedCodes.map { "[ST] $it" } + report.pendingCodes.map { "[PD] $it" }
    val points = report.findings.take(REPORT_PDF_MAX_FINDINGS).map {
        DiagnosticSnapshotPoint(label = it.system, value = "${it.healthScore}/100", unit = it.status)
    }
    val summary = PdfReportExporter.exportDiagnosticReport(
        context = context,
        dtcCodes = codes,
        dataPoints = points,
        fileNamePrefix = "diagnosebericht"
    )
    if (summary != null) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, summary.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, REPORT_SHARE_TITLE_PDF))
        } catch (e: android.content.ActivityNotFoundException) {
            android.util.Log.e(REPORT_LOG_TAG, "No app to share report PDF", e)
        }
    } else {
        Toast.makeText(context, "PDF-Export fehlgeschlagen", Toast.LENGTH_SHORT).show()
    }
}
