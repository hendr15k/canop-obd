package com.canopobd.ui.shiftlight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canopobd.R
import com.canopobd.data.model.ShiftLightConfig
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Composable
fun ShiftLightDialog(
    config: ShiftLightConfig,
    currentRpm: Double,
    onDismiss: () -> Unit,
    onUpdateConfig: (ShiftLightConfig) -> Unit
) {
    var localConfig by remember(config) { mutableStateOf(config) }
    var redlineRpm by remember(config) { mutableFloatStateOf(config.redlineRpm.toFloat()) }
    var warningRpm by remember(config) { mutableFloatStateOf(config.warningRpm.toFloat()) }
    val colors = LocalAppColors.current
    val currentColor = when {
        currentRpm >= redlineRpm -> colors.critical
        currentRpm >= warningRpm -> colors.warning
        else -> colors.success
    }

    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.shift_light_title),
        eyebrow = "Schaltblitz Konfigurator",
        heightFraction = 0.85f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToggleRow(
                label = stringResource(R.string.shift_light_enabled),
                checked = localConfig.enabled,
                onCheckedChange = { localConfig = localConfig.copy(enabled = it) },
                accentColor = colors.success
            )

            // Current RPM display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(AppRadius.lg))
                    .background(colors.surfaceBase)
                    .border(2.dp, currentColor.copy(alpha = 0.4f), RoundedCornerShape(AppRadius.lg)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(currentColor.copy(alpha = 0.15f), androidx.compose.ui.graphics.Color.Transparent)
                            )
                        )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.0f".format(currentRpm),
                        style = MaterialTheme.typography.displayLarge,
                        color = currentColor,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "U/min",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textTertiary
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(colors.surfaceRaised)
                    ) {
                        Box(modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((currentRpm / 8000.0).toFloat().coerceIn(0f, 1f))
                            .background(currentColor)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                        Text(
                            stringResource(R.string.shift_light_warning_rpm, warningRpm.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.warning
                        )
                        Text(
                            stringResource(R.string.shift_light_redline_rpm, redlineRpm.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.critical
                        )
                        Text("8000", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                    }
                }
            }

            GlassCard(padding = 12.dp) {
                Text(
                    stringResource(R.string.shift_light_warning_threshold),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.warning,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Slider(
                    value = warningRpm,
                    onValueChange = {
                        warningRpm = it
                        localConfig = localConfig.copy(warningRpm = it.toInt())
                    },
                    valueRange = 3000f..7000f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.warning,
                        activeTrackColor = colors.warning,
                        inactiveTrackColor = colors.surfaceRaised
                    )
                )
            }

            GlassCard(padding = 12.dp) {
                Text(
                    stringResource(R.string.shift_light_redline_threshold),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.critical,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Slider(
                    value = redlineRpm,
                    onValueChange = {
                        redlineRpm = it
                        localConfig = localConfig.copy(redlineRpm = it.toInt())
                    },
                    valueRange = 4000f..8000f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.critical,
                        activeTrackColor = colors.critical,
                        inactiveTrackColor = colors.surfaceRaised
                    )
                )
            }

            ToggleRow(
                label = stringResource(R.string.shift_light_flash),
                checked = localConfig.flashEnabled,
                onCheckedChange = { localConfig = localConfig.copy(flashEnabled = it) },
                accentColor = colors.success
            )

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = stringResource(R.string.shift_light_save),
                onClick = {
                    onUpdateConfig(localConfig)
                    onDismiss()
                },
                icon = Icons.Filled.Save,
                gradient = colors.gradientSuccess,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
