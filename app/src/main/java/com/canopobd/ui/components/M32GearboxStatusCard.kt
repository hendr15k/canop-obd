package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

/**
 * M32GearboxStatusCard - Getriebe-Statuskarte (Getrag M32)
 *
 * Das Getrag M32 6-Gang-Schaltgetriebe ist das Standardgetriebe des Astra J 1.4T.
 * Es ist bekannt für einige Schwachstellen, insbesondere Lager und Synchronringe.
 *
 * Zeigt:
 * - Getriebetemperatur (falls verfügbar)
 * - Input/Output-Speed-Verhältnis
 * - Gesundheitsindikatoren
 * - Wartungsintervall
 * - Deutsche Warnmeldungen
 */
@Composable
fun M32GearboxStatusCard(
    modifier: Modifier = Modifier,
    gearboxTempCelsius: Double = 0.0,
    inputSpeedRpm: Double = 0.0,
    outputSpeedRpm: Double = 0.0,
    healthScore: Int = 100,
    lastFluidChangeKm: Int = 0,
    currentKm: Int = 0,
    gearRattleDetected: Boolean = false,
    colors: AppColors = LocalAppColors.current
) {
    val healthStatus = remember(healthScore) { GearboxHealthStatus.fromScore(healthScore) }
    val healthColor = remember(healthStatus) { healthStatus.color(colors) }

    val isCritical = healthScore < 50
    val isWarning = healthScore in 50..79

    val infiniteTransition = rememberInfiniteTransition(label = "gearbox_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isCritical) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gearbox_pulse_alpha"
    )

    val animatedHealthColor by animateColorAsState(
        targetValue = healthColor,
        animationSpec = tween(300),
        label = "gearbox_health_color"
    )

    val gearRatio = remember(inputSpeedRpm, outputSpeedRpm) {
        if (outputSpeedRpm > 0) inputSpeedRpm / outputSpeedRpm else 0.0
    }

    val maintenanceInfo = remember(lastFluidChangeKm, currentKm) {
        val kmSince = currentKm - lastFluidChangeKm
        val intervalKm = 60000
        when {
            kmSince > intervalKm -> MaintenanceInfo("Überfällig!", Color(0xFFEF4444))
            kmSince > intervalKm - 5000 -> MaintenanceInfo("Bald fällig", Color(0xFFFBBF24))
            else -> MaintenanceInfo("OK (%,d km verbleibend)".format(intervalKm - kmSince), Color(0xFF22C55E))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCritical || gearRattleDetected) {
                    Modifier.border(
                        width = 2.dp,
                        color = colors.gaugeRed.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else if (isWarning) {
                    Modifier.border(
                        width = 1.dp,
                        color = colors.gaugeOrange.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.gearbox_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = animatedHealthColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(
                                    androidx.compose.foundation.shape.CircleShape
                                )
                                .background(animatedHealthColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = healthStatus.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedHealthColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Health score bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.gearbox_health_score),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$healthScore / 100",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedHealthColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((healthScore / 100f).toFloat())
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        animatedHealthColor.copy(alpha = 0.7f),
                                        animatedHealthColor
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.gearbox_temp),
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val tempColor = when {
                        gearboxTempCelsius > 100 -> colors.gaugeRed
                        gearboxTempCelsius > 80 -> colors.gaugeOrange
                        gearboxTempCelsius > 40 -> colors.gaugeGreen
                        else -> colors.textDim
                    }
                    Text(
                        text = if (gearboxTempCelsius > 0) "%.0f°C".format(gearboxTempCelsius) else "—",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = tempColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.gearbox_ratio),
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (gearRatio > 0) "%.2f:1".format(gearRatio) else "—",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedIndicator(
                    label = "Input",
                    rpm = inputSpeedRpm,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                SpeedIndicator(
                    label = "Output",
                    rpm = outputSpeedRpm,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Maintenance interval
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = maintenanceInfo.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.gearbox_fluid_maintenance),
                            fontSize = 10.sp,
                            color = colors.textDim
                        )
                        Text(
                            text = maintenanceInfo.text,
                            fontSize = 11.sp,
                            color = maintenanceInfo.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Rattle warning
            if (gearRattleDetected) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.gaugeRed.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = colors.gaugeRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.gearbox_rattle_warning),
                            fontSize = 11.sp,
                            color = colors.gaugeRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedIndicator(
    label: String,
    rpm: Double,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (rpm > 0) "%,d rpm".format(rpm.toInt()) else "—",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
    }
}

enum class GearboxHealthStatus(val label: String) {
    HEALTHY("Gesund"),
    WEAR_DETECTED("Verschleiß"),
    CRITICAL("Kritisch");

    fun color(colors: AppColors): Color = when (this) {
        HEALTHY -> colors.gaugeGreen
        WEAR_DETECTED -> colors.gaugeOrange
        CRITICAL -> colors.gaugeRed
    }

    companion object {
        fun fromScore(score: Int): GearboxHealthStatus = when {
            score >= 80 -> HEALTHY
            score >= 50 -> WEAR_DETECTED
            else -> CRITICAL
        }
    }
}

private data class MaintenanceInfo(
    val text: String,
    val color: Color
)
