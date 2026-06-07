package com.canopobd.ui.power

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canopobd.R
import com.canopobd.data.model.PowerCalculation
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Composable
fun PowerCalculatorDialog(
    calculation: PowerCalculation,
    rpm: Double,
    maf: Double,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.power_title),
        eyebrow = "Leistungsrechner",
        heightFraction = 0.75f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Info row
            GlassCard(padding = 12.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip(label = "RPM", value = "%.0f".format(rpm), colors = colors, modifier = Modifier.weight(1f))
                    InfoChip(label = "MAF", value = "%.1f g/s".format(maf), colors = colors, modifier = Modifier.weight(1f))
                }
            }

            if (!calculation.isValid) {
                Spacer(Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceRaised),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Speed,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.power_start_engine),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.power_rpm_maf_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
                Spacer(Modifier.weight(1f))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerCard(
                        modifier = Modifier.weight(1f),
                        label = "PS",
                        value = "%.0f".format(calculation.horsepowerMetric),
                        subtitle = stringResource(R.string.power_metric),
                        color = colors.success
                    )
                    PowerCard(
                        modifier = Modifier.weight(1f),
                        label = "HP",
                        value = "%.0f".format(calculation.horsepower),
                        subtitle = stringResource(R.string.power_us),
                        color = colors.info
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PowerCard(
                        modifier = Modifier.weight(1f),
                        label = "Nm",
                        value = "%.0f".format(calculation.torqueNm),
                        subtitle = stringResource(R.string.power_torque),
                        color = colors.warning
                    )
                    PowerCard(
                        modifier = Modifier.weight(1f),
                        label = "kW",
                        value = "%.0f".format(calculation.horsepower / 1.341),
                        subtitle = "Leistung",
                        color = colors.info
                    )
                }

                GlassCard(
                    accentEdge = colors.success,
                    padding = 10.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.power_estimated_maf),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.lg))
            .background(colors.surfaceBase)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(AppRadius.lg))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = color,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = colors.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
    }
}
