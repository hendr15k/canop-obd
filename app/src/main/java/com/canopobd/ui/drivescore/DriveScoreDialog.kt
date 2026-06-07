package com.canopobd.ui.drivescore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.DriveScore
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Composable
fun DriveScoreDialog(
    score: DriveScore,
    sessionDuration: Long,
    harshAccels: Int,
    harshBrakes: Int,
    idleTimeSeconds: Long,
    avgRpm: Double,
    avgThrottle: Double,
    avgSpeed: Double,
    fuelConsumptionL100km: Double = 0.0,
    onDismiss: () -> Unit,
    onResetScore: () -> Unit
) {
    val colors = LocalAppColors.current
    val scoreColor = Color(score.color)
    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.drive_score_analysis_title),
        eyebrow = "Fahrstil-Analyse",
        heightFraction = 0.9f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero score
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(AppRadius.lg))
                    .background(colors.surfaceBase)
                    .border(1.dp, scoreColor.copy(alpha = 0.4f), RoundedCornerShape(AppRadius.lg)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(scoreColor.copy(alpha = 0.18f), Color.Transparent)
                            )
                        )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = score.grade,
                        style = MaterialTheme.typography.displayLarge,
                        color = scoreColor,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.drive_score_score_format, score.score),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textSecondary
                    )
                }
            }

            // Score sub-categories
            GlassCard(padding = 12.dp) {
                Text(
                    "TEIL-BEWERTUNGEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreMini(score.accelerationScore, stringResource(R.string.drive_score_acceleration), colors.success, Modifier.weight(1f))
                    ScoreMini(score.brakingScore, stringResource(R.string.drive_score_braking), colors.critical, Modifier.weight(1f))
                    ScoreMini(score.cruisingScore, stringResource(R.string.drive_score_cruising), colors.info, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreMini(score.idleScore, stringResource(R.string.drive_score_idle), colors.warning, Modifier.weight(1f))
                    ScoreMini(score.rpmScore, stringResource(R.string.drive_score_rpm), colors.warning, Modifier.weight(1f))
                    ScoreMini(score.throttleScore, stringResource(R.string.drive_score_throttle), colors.info, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreMini(score.boostScore, stringResource(R.string.drive_score_boost), colors.info, Modifier.weight(1f))
                    ScoreMini(score.ecoScore, stringResource(R.string.drive_score_eco), colors.success, Modifier.weight(1f))
                    ScoreMini(score.turboHealthScore, stringResource(R.string.drive_score_turbo_health), colors.warning, Modifier.weight(1f))
                }
            }

            // Session stats
            GlassCard(
                accentEdge = colors.primary,
                padding = 12.dp
            ) {
                InlineSectionHeader(
                    title = stringResource(R.string.drive_score_section_session),
                    icon = Icons.Filled.Schedule
                )
                Spacer(Modifier.height(8.dp))
                DataRow(label = stringResource(R.string.drive_score_duration), value = formatDuration(sessionDuration))
                DividerLine(modifier = Modifier.padding(vertical = 2.dp))
                DataRow(
                    label = stringResource(R.string.drive_score_avg_rpm),
                    value = stringResource(R.string.drive_score_avg_rpm_format, avgRpm)
                )
                DividerLine(modifier = Modifier.padding(vertical = 2.dp))
                DataRow(
                    label = stringResource(R.string.drive_score_avg_throttle),
                    value = stringResource(R.string.drive_score_avg_throttle_format, avgThrottle)
                )
                DividerLine(modifier = Modifier.padding(vertical = 2.dp))
                DataRow(
                    label = stringResource(R.string.drive_score_avg_speed),
                    value = stringResource(R.string.drive_score_avg_speed_format, avgSpeed)
                )
                DividerLine(modifier = Modifier.padding(vertical = 2.dp))
                DataRow(
                    label = stringResource(R.string.drive_score_harsh_accel),
                    value = "$harshAccels",
                    valueColor = if (harshAccels > 5) colors.warning else colors.textPrimary
                )
                DividerLine(modifier = Modifier.padding(vertical = 2.dp))
                DataRow(
                    label = stringResource(R.string.drive_score_harsh_brake),
                    value = "$harshBrakes",
                    valueColor = if (harshBrakes > 5) colors.warning else colors.textPrimary
                )
                DividerLine(modifier = Modifier.padding(vertical = 2.dp))
                DataRow(
                    label = stringResource(R.string.drive_score_idle_time),
                    value = formatDuration(idleTimeSeconds)
                )
                DividerLine(modifier = Modifier.padding(vertical = 2.dp))
                DataRow(
                    label = stringResource(R.string.drive_score_fuel_consumption),
                    value = if (fuelConsumptionL100km > 0) "%.1f L/100km".format(fuelConsumptionL100km) else "—"
                )
            }

            // Reset button
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlineButton(
                    text = stringResource(R.string.drive_score_reset),
                    onClick = onResetScore,
                    icon = Icons.Filled.Refresh,
                    accentColor = colors.warning,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ScoreMini(score: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(AppRadius.md))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.5.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, mins, secs)
    } else {
        "%d:%02d".format(mins, secs)
    }
}
