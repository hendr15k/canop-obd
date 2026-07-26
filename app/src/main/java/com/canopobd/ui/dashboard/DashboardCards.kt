package com.canopobd.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.domain.*
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.GaugeTypography
import com.canopobd.ui.theme.LocalAppColors
import kotlin.math.abs

// ============================================================================
// DASHBOARD CARDS v2.0 — Modern, glass-morphism style
// Each card uses the new design system: GlassCard, ProgressRing, StatusPill,
// ScorePill, etc. — for a consistent look across the dashboard.
// ============================================================================

// --- Helper: status color from health score --------------------------------
private fun healthColor(score: Int?, colors: AppColors): Color = when {
    score == null -> colors.textTertiary
    score >= 80 -> colors.success
    score >= 50 -> colors.warning
    else -> colors.critical
}

// ============================================================================
// OIL HEALTH CARD
// ============================================================================
@Composable
fun OilHealthCard(
    prediction: OilHealthPredictor.OilHealthPredictionResult,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val statusColor = Color(prediction.prediction.colorHex)

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = statusColor,
        padding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(
                progress = prediction.healthScore / 100f,
                size = 44.dp,
                strokeWidth = 4.dp,
                color = statusColor,
                centerText = "${prediction.healthScore}",
                centerTextColor = statusColor
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ÖL-GESUNDHEIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = prediction.prediction.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${prediction.kmSinceOilChange.toInt()} km",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniScore(label = "Thermal", score = prediction.thermalLoadScore, colors = colors, modifier = Modifier.weight(1f))
            MiniScore(label = "Profil", score = prediction.drivingPatternScore, colors = colors, modifier = Modifier.weight(1f))
            MiniScore(label = "Verbr.", score = prediction.consumptionScore, colors = colors, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniScore(label: String, score: Int, colors: AppColors, modifier: Modifier = Modifier) {
    val c = when {
        score >= 80 -> colors.success
        score >= 50 -> colors.warning
        else -> colors.critical
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(colors.surfaceRaised)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = score.toString(),
            style = GaugeTypography.valueSmall,
            color = c,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// SENSOR VALIDATION CARD
// ============================================================================
@Composable
fun SensorValidationCard(
    validationResult: ValidationResult,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val statusColor = when (validationResult) {
        is ValidationResult.Valid -> colors.success
        is ValidationResult.Suspicious -> colors.warning
        is ValidationResult.Invalid -> colors.critical
        is ValidationResult.Unavailable -> colors.textTertiary
    }
    val labelText = when (validationResult) {
        is ValidationResult.Valid -> "OK"
        is ValidationResult.Suspicious -> "Verdächtig"
        is ValidationResult.Invalid -> "Fehler"
        is ValidationResult.Unavailable -> "—"
    }
    val score = when (validationResult) {
        is ValidationResult.Valid -> 100
        is ValidationResult.Suspicious -> 60
        is ValidationResult.Invalid -> 20
        is ValidationResult.Unavailable -> 0
    }
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = statusColor,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = score / 100f,
                size = 44.dp,
                strokeWidth = 4.dp,
                color = statusColor,
                centerText = "$score",
                centerTextColor = statusColor
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SENSOREN",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.titleSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (validationResult) {
                        is ValidationResult.Suspicious -> validationResult.message
                        is ValidationResult.Invalid -> validationResult.message
                        else -> "Validiert"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================================
// DRIVE STYLE CARD
// ============================================================================
@Composable
fun DriveStyleCard(
    analysis: DriveStyleAnalyzer.DriveStyleAnalysis,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val score = analysis.ecoScore
    val scoreColor = healthColor(score, colors)
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = scoreColor,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.15f))
                    .border(1.5.dp, scoreColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = score.toString(),
                    style = GaugeTypography.valueSmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FAHRSTIL",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = analysis.driveStyle.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================================
// EFFICIENCY CARD
// ============================================================================
@Composable
fun EfficiencyCard(
    score: DrivingEfficiencyScorer.EfficiencyScore,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val overall = score.overall
    val scoreColor = healthColor(overall, colors)
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = scoreColor,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = overall / 100f,
                size = 44.dp,
                strokeWidth = 4.dp,
                color = scoreColor,
                centerText = "$overall",
                centerTextColor = scoreColor
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EFFIZIENZ",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${score.accelerationScore}/${score.cruisingScore}",
                    style = MaterialTheme.typography.titleSmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Beschl. / Schwung",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

// ============================================================================
// FUEL SYSTEM CARD
// ============================================================================
@Composable
fun FuelSystemCard(
    analysis: FuelSystemAnalyzer.FuelSystemAnalysis,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val score = analysis.healthScore
    val scoreColor = healthColor(score, colors)
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = scoreColor,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = score / 100f,
                size = 44.dp,
                strokeWidth = 4.dp,
                color = scoreColor,
                centerText = "$score",
                centerTextColor = scoreColor
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "KRAFTSTOFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = analysis.health.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "%.1f%% Rail-Abw.".format(analysis.fuelRailPressureDeviation),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

// ============================================================================
// KNOWN ISSUES CARD
// ============================================================================
@Composable
fun KnownIssuesCard(
    currentKm: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        accentEdge = colors.warning,
        padding = 14.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(colors.warning.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BEKANNTE PROBLEME",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Astra J 1.4T Schwachstellen",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPure,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Aktuell: $currentKm km",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
            StatusPill(text = "TIPPS", color = colors.warning)
        }
    }
}

// ============================================================================
// ANALYZER SUMMARY ROW — Compact summary of various analyzers
// ============================================================================
@Suppress("UNUSED_PARAMETER")
@Composable
fun AnalyzerSummaryRow(
    gearboxResult: com.canopobd.data.domain.M32GearboxMonitor.GearboxAnalysis?,
    chainTensionerResult: com.canopobd.data.domain.ChainTensionerAnalyzer.ChainTensionerAnalysis?,
    coolantResult: com.canopobd.data.domain.CoolantSystemHealth.CoolantAnalysis?,
    oilConditionResult: com.canopobd.data.domain.OilConditionMonitor.OilAnalysis?,
    pcvResult: com.canopobd.data.domain.PCVMonitor.PCVAnalysis?,
    lambdaResult: com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaBalance?,
    fuelConsumption: com.canopobd.data.domain.FuelConsumptionAnalyzer.FuelConsumptionData?,
    egtResult: com.canopobd.data.domain.EGTMonitor.EGTAnalysis?,
    sensorHealthSummary: com.canopobd.data.domain.SensorHealthMonitor.SensorHealthSummary?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    GlassCard(modifier = modifier.fillMaxWidth(), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(AppRadius.xs))
                    .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "ANALYZER",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnalyzerChip("Getr.", gearboxResult?.healthScore, Icons.Filled.Settings, colors, Modifier.weight(1f))
            AnalyzerChip("Kette", chainTensionerResult?.healthScore, Icons.Filled.Link, colors, Modifier.weight(1f))
            AnalyzerChip("Kühl.", coolantResult?.healthScore, Icons.Filled.Thermostat, colors, Modifier.weight(1f))
            AnalyzerChip("Öl", oilConditionResult?.healthScore, Icons.Filled.LocalGasStation, colors, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnalyzerChip("PCV", pcvResult?.healthScore, Icons.Filled.Air, colors, Modifier.weight(1f))
            AnalyzerChip("λ", null, Icons.Filled.Science, colors, Modifier.weight(1f))
            AnalyzerChip("EGT", egtResult?.healthScore, Icons.Filled.LocalFireDepartment, colors, Modifier.weight(1f))
            AnalyzerChip("Sens.", null, Icons.Filled.Sensors, colors, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AnalyzerChip(
    label: String,
    score: Int?,
    icon: ImageVector,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val c = healthColor(score, colors)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(colors.surfaceRaised)
            .border(1.dp, c.copy(alpha = 0.25f), RoundedCornerShape(AppRadius.sm))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = c,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = score?.toString() ?: "--",
            style = GaugeTypography.valueTiny,
            color = c,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// BATTERY HEALTH CARD
// ============================================================================
@Composable
fun BatteryHealthCard(
    analysis: com.canopobd.data.domain.BatteryHealthAnalyzer.BatteryAnalysis?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val score = analysis?.healthScore
    val c = healthColor(score, colors)
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(c.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.BatteryChargingFull,
                    contentDescription = null,
                    tint = c,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BATTERIE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(analysis?.status?.voltage ?: 0.0),
                        style = GaugeTypography.valueSmall,
                        color = c
                    )
                    Text(
                        text = " V",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Text(
                text = "${score ?: 0}%",
                style = MaterialTheme.typography.titleSmall,
                color = c,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// EGR HEALTH CARD
// ============================================================================
@Composable
fun EGRHealthCard(
    analysis: com.canopobd.data.domain.EGRHealthAnalyzer.EGRAnalysis?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val score = analysis?.healthScore
    val c = healthColor(score, colors)
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(c.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    tint = c,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EGR",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "%.1f%%".format(analysis?.flowDeviation ?: 0.0),
                    style = GaugeTypography.valueSmall,
                    color = c
                )
            }
            Text(
                text = "${score ?: 0}%",
                style = MaterialTheme.typography.titleSmall,
                color = c,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// EVAP CARD
// ============================================================================
@Composable
fun EVAPHealthCard(
    analysis: com.canopobd.data.domain.EVAPSystemAnalyzer.EVAPAnalysis?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val score = analysis?.healthScore
    val c = healthColor(score, colors)
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(c.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Cloud,
                    contentDescription = null,
                    tint = c,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EVAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "%.1f%%".format(analysis?.purgeEfficiency ?: 0.0),
                    style = GaugeTypography.valueSmall,
                    color = c
                )
            }
            Text(
                text = "${score ?: 0}%",
                style = MaterialTheme.typography.titleSmall,
                color = c,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// CHAIN TENSIONER WARNING CARD
// ============================================================================
@Composable
fun ChainTensionerWarningCard(
    state: com.canopobd.data.model.TimingChainState?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val hasRattle = state?.coldStartRattleDetected ?: false
    val c = if (hasRattle) colors.critical else if (state != null) colors.success else colors.textTertiary
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(c.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = c,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STEUERKETTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (hasRattle) "Rattern erkannt" else "OK",
                    style = GaugeTypography.valueSmall,
                    color = c
                )
            }
            if (hasRattle) {
                StatusPill(text = "AKTION", color = c, icon = Icons.Filled.Warning)
            }
        }
    }
}

// ============================================================================
// CRITICAL WARNING OVERLAY
// ============================================================================
@Composable
fun CriticalWarningOverlay(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissSeconds: Int = 10
) {
    val colors = LocalAppColors.current
    val secondsLeft = remember { mutableStateOf(autoDismissSeconds) }
    LaunchedEffect(Unit) {
        while (secondsLeft.value > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft.value--
        }
        onDismiss()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(AppRadius.lg))
                .background(colors.surfaceBase)
                .border(2.dp, colors.critical, RoundedCornerShape(AppRadius.lg))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.critical.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = colors.critical,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.critical,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Schließt in ${secondsLeft.value}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

// ============================================================================
// EMISSIONS STATUS CARD
// ============================================================================
@Composable
fun EmissionsStatusCard(
    analysis: com.canopobd.data.domain.EmissionsReadinessAnalyzer.ReadinessAnalysis?,
    isMilActive: Boolean,
    dtcCount: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val total = analysis?.totalCount ?: 0
    val completed = analysis?.completedCount ?: 0
    val pct = if (total > 0) completed.toFloat() / total else 0f
    val c = when {
        isMilActive -> colors.critical
        pct >= 0.9f -> colors.success
        pct >= 0.6f -> colors.warning
        else -> colors.critical
    }
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = pct,
                size = 44.dp,
                strokeWidth = 4.dp,
                color = c,
                centerText = "${(pct * 100).toInt()}",
                centerTextColor = c
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EMISSIONEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isMilActive) "MIL AKTIV" else "$completed/$total Monitore",
                    style = MaterialTheme.typography.titleSmall,
                    color = c,
                    fontWeight = FontWeight.Bold
                )
                if (dtcCount > 0) {
                    Text(
                        text = "$dtcCount Fehler",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.critical
                    )
                }
            }
        }
    }
}

// ============================================================================
// DRIVE MODE INDICATOR
// ============================================================================
@Composable
fun DriveModeIndicator(
    currentMode: String,
    sportMode: Boolean,
    manualMode: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val accent = when {
        sportMode -> colors.warning
        manualMode -> colors.primary
        else -> colors.secondary
    }
    GlassCard(
        modifier = modifier,
        accentEdge = accent,
        padding = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = accent, pulse = true)
            Spacer(Modifier.width(8.dp))
            Text(
                text = currentMode.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// TUNING STAGE INDICATOR
// ============================================================================
@Composable
fun TuningStageIndicator(
    stage: String,
    boost: Double,
    power: Double,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val stageColor = when {
        stage.contains("Stock", ignoreCase = true) -> colors.success
        stage.contains("1", ignoreCase = true) -> colors.primary
        stage.contains("2", ignoreCase = true) -> colors.warning
        else -> colors.accent
    }
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = stageColor,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(stageColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stage.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = stageColor,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TUNING",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stage,
                    style = MaterialTheme.typography.titleSmall,
                    color = stageColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.1f PS".format(power),
                    style = GaugeTypography.valueTiny,
                    color = stageColor
                )
                Text(
                    text = "%.2f bar".format(boost),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

// ============================================================================
// VEHICLE INFO CARD
// ============================================================================
@Suppress("UNUSED_PARAMETER")
@Composable
fun VehicleInfoCard(
    vin: String?,
    calibrationId: String?,
    cvn: String?,
    ecuName: String?,
    protocol: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = colors.primary,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FAHRZEUG",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = vin ?: "VIN unbekannt",
                    style = GaugeTypography.valueTiny,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (ecuName != null) {
                    Text(
                        text = ecuName,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ============================================================================
// SHIFT RECOMMENDATION
// ============================================================================
@Composable
fun ShiftRecommendation(
    currentGear: Int,
    recommendedAction: String,
    isCritical: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val c = if (isCritical) colors.critical else colors.warning
    GlassCard(
        modifier = modifier,
        accentEdge = c,
        padding = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(c.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentGear.toString(),
                    style = GaugeTypography.valueMedium,
                    color = c,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GANG",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = recommendedAction,
                    style = MaterialTheme.typography.titleSmall,
                    color = c,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// FUEL EFFICIENCY CARD
// ============================================================================
@Composable
fun FuelEfficiencyCard(
    l100km: Double,
    trend: List<Float>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val c = when {
        l100km < 7.0 -> colors.success
        l100km < 9.0 -> colors.primary
        l100km < 11.0 -> colors.warning
        else -> colors.critical
    }
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "VERBRAUCH",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.1f".format(l100km),
                        style = GaugeTypography.valueMedium,
                        color = c
                    )
                    Text(
                        text = " L/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            if (trend.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp)
                ) {
                    Sparkline(data = trend, color = c)
                }
            }
        }
    }
}

// ============================================================================
// EXTENDED PID VIEWER CARD
// ============================================================================
@Composable
fun ExtendedPIDViewerCard(
    pidCount: Int,
    activePids: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val c = colors.primary
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(c.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Sensors,
                    contentDescription = null,
                    tint = c,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ERWEITERTE PIDs",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$activePids / $pidCount aktiv",
                    style = MaterialTheme.typography.titleSmall,
                    color = c,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// TCM/ECM CAN STATUS CARD
// ============================================================================
@Suppress("UNUSED_PARAMETER")
@Composable
fun TCMECMCANStatusCard(
    tcmCurrentGear: Int,
    tcmOilTempCelsius: Double,
    tcmPressureKpa: Double,
    tcmSportMode: Boolean,
    tcmManualMode: Boolean,
    tcmError: String?,
    ecmRpm: Double,
    ecmSpeedKmh: Double,
    ecmCoolantTemp: Int,
    ecmThrottlePosition: Double,
    ecmEngineLoad: Double,
    lastUpdateTime: Long,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        accentEdge = colors.primary,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(AppRadius.xs))
                    .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Memory,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "TCM · ECM · CAN-BUS",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            StatusDot(color = if (tcmError == null) colors.success else colors.critical, pulse = tcmError == null)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactStat("Gear", "$tcmCurrentGear", colors, Modifier.weight(1f))
            CompactStat("RPM", "${ecmRpm.toInt()}", colors, Modifier.weight(1f))
            CompactStat("km/h", "${ecmSpeedKmh.toInt()}", colors, Modifier.weight(1f))
            CompactStat("Load", "${ecmEngineLoad.toInt()}%", colors, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactStat("Öl°C", "${tcmOilTempCelsius.toInt()}", colors, Modifier.weight(1f))
            CompactStat("Druck", "${tcmPressureKpa.toInt()}", colors, Modifier.weight(1f))
            CompactStat("Kühl", "$ecmCoolantTemp°", colors, Modifier.weight(1f))
            CompactStat("Gas", "${ecmThrottlePosition.toInt()}%", colors, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CompactStat(label: String, value: String, colors: AppColors, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(colors.surfaceRaised)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = GaugeTypography.valueTiny,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// FUEL TRIM CARD
// ============================================================================
@Composable
fun FuelTrimCard(
    stftBank1: Double,
    ltftBank1: Double,
    stftBank2: Double,
    ltftBank2: Double,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val totalTrim = abs(stftBank1 + ltftBank1 + stftBank2 + ltftBank2) / 2
    val c = when {
        totalTrim < 5.0 -> colors.success
        totalTrim < 10.0 -> colors.warning
        else -> colors.critical
    }
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        InlineSectionHeader(
            title = "KRAFTSTOFF-TRIMM",
            icon = Icons.Filled.Science
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FuelTrimCell("STFT B1", stftBank1, colors, Modifier.weight(1f))
            FuelTrimCell("LTFT B1", ltftBank1, colors, Modifier.weight(1f))
            FuelTrimCell("STFT B2", stftBank2, colors, Modifier.weight(1f))
            FuelTrimCell("LTFT B2", ltftBank2, colors, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FuelTrimCell(label: String, value: Double, colors: AppColors, modifier: Modifier = Modifier) {
    val c = when {
        abs(value) < 5.0 -> colors.success
        abs(value) < 10.0 -> colors.warning
        else -> colors.critical
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(colors.surfaceRaised)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "%+.1f".format(value),
            style = GaugeTypography.valueTiny,
            color = c,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// LAMBDA O2 SENSOR CARD
// ============================================================================
@Composable
fun LambdaO2SensorCard(
    preCatVoltage: Double,
    postCatVoltage: Double,
    crossCountRate: Int,
    heaterActive: Boolean,
    isHealthy: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val c = if (isHealthy) colors.success else colors.warning
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        InlineSectionHeader(
            title = "LAMBDA / O2",
            icon = Icons.Filled.Science
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LambdaCell("Vorkat", preCatVoltage, colors, Modifier.weight(1f))
            LambdaCell("Nachkat", postCatVoltage, colors, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cross-Count: $crossCountRate/s",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
            StatusPill(
                text = if (heaterActive) "Heizung AN" else "Heizung AUS",
                color = if (heaterActive) colors.success else colors.textTertiary
            )
        }
    }
}

@Composable
private fun LambdaCell(label: String, voltage: Double, colors: AppColors, modifier: Modifier = Modifier) {
    val c = if (voltage in 0.1..0.9) colors.success else colors.warning
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(colors.surfaceRaised)
            .padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "%.2f V".format(voltage),
            style = GaugeTypography.valueSmall,
            color = c,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// READINESS MONITOR CARD
// ============================================================================
@Composable
fun ReadinessMonitorCard(
    totalTests: Int,
    completedTests: Int,
    isMilActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val pct = if (totalTests > 0) completedTests.toFloat() / totalTests else 0f
    val c = when {
        isMilActive -> colors.critical
        pct >= 0.9f -> colors.success
        pct >= 0.6f -> colors.warning
        else -> colors.critical
    }
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = pct,
                size = 44.dp,
                strokeWidth = 4.dp,
                color = c,
                centerText = "${(pct * 100).toInt()}",
                centerTextColor = c
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "READINESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completedTests / $totalTests",
                    style = MaterialTheme.typography.titleSmall,
                    color = c,
                    fontWeight = FontWeight.Bold
                )
                if (isMilActive) {
                    Text(
                        text = "MIL aktiv",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.critical
                    )
                }
            }
        }
    }
}

// ============================================================================
// M32 GEARBOX CARD
// ============================================================================
@Composable
fun M32GearboxCard(
    analysis: com.canopobd.data.domain.M32GearboxMonitor.GearboxAnalysis?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val score = analysis?.healthScore
    val c = healthColor(score, colors)
    val hasIssues = (analysis?.detectedIssues?.size ?: 0) > 0
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = if (hasIssues) colors.critical else c,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(c.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = c,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GETRIEBE (M32)",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (hasIssues) "Probleme erkannt!" else analysis?.health?.name ?: "OK",
                    style = GaugeTypography.valueSmall,
                    color = if (hasIssues) colors.critical else c
                )
            }
            Text(
                text = "${score ?: 0}%",
                style = MaterialTheme.typography.titleSmall,
                color = c,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// TURBO DETAIL CARD
// ============================================================================
@Composable
fun TurboDetailCard(
    actualBoost: Double,
    targetBoost: Double,
    wastegatePos: Double,
    turboRpm: Int,
    isHealthy: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val c = if (isHealthy) colors.success else colors.warning
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = c,
        padding = 12.dp
    ) {
        InlineSectionHeader(
            title = "TURBO-DETAIL",
            icon = Icons.Filled.Air
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TurboStat("Ist", "%.2f".format(actualBoost), "bar", colors, Modifier.weight(1f), c)
            TurboStat("Soll", "%.2f".format(targetBoost), "bar", colors, Modifier.weight(1f), colors.textTertiary)
            TurboStat("WG", "${wastegatePos.toInt()}", "%", colors, Modifier.weight(1f), c)
            TurboStat("RPM", "$turboRpm", "", colors, Modifier.weight(1f), c)
        }
    }
}

@Composable
private fun TurboStat(
    label: String,
    value: String,
    unit: String,
    colors: AppColors,
    modifier: Modifier = Modifier,
    accent: Color = colors.textPrimary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(colors.surfaceRaised)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = GaugeTypography.valueTiny,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(1.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontSize = 8.sp
                )
            }
        }
    }
}
