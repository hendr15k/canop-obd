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
import com.canopobd.data.model.*
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun EVAPSystemCard(
    evapData: EVAPData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val systemColor by animateColorAsState(
        targetValue = when (evapData.systemStatus) {
            EVAPSystemStatus.ACTIVE -> colors.gaugeGreen
            EVAPSystemStatus.STANDBY -> colors.gaugeCyan
            EVAPSystemStatus.LEAK_DETECTED -> colors.gaugeRed
            EVAPSystemStatus.NOT_SUPPORTED -> colors.textDim
        },
        animationSpec = tween(300),
        label = "evap_color"
    )

    val isLeak = evapData.systemStatus == EVAPSystemStatus.LEAK_DETECTED

    val infiniteTransition = rememberInfiniteTransition(label = "evap_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isLeak) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isLeak) {
                    Modifier.border(
                        width = 2.dp,
                        color = colors.gaugeRed.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalGasStation,
                        contentDescription = null,
                        tint = systemColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.evap_system),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                SystemStatusBadge(
                    status = evapData.systemStatus,
                    color = systemColor,
                    colors = colors
                )
            }

            if (!evapData.isSupported) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.textDim.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = colors.textDim,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.evap_not_supported),
                            fontSize = 12.sp,
                            color = colors.textDim
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PurgeDutyCycleSection(
                        purgeDutyCycle = evapData.purgeDutyCycle,
                        isActive = evapData.systemStatus == EVAPSystemStatus.ACTIVE,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )

                    TankPressureSection(
                        tankPressure = evapData.tankPressure,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isLeak) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LeakWarningSection(colors = colors)
                }

                Spacer(modifier = Modifier.height(12.dp))

                EVAPExplanationText(colors = colors)
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun SystemStatusBadge(
    status: EVAPSystemStatus,
    color: Color,
    colors: AppColors
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (status) {
                    EVAPSystemStatus.LEAK_DETECTED -> Icons.Filled.Warning
                    EVAPSystemStatus.ACTIVE -> Icons.Filled.PlayArrow
                    else -> Icons.Filled.Pause
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun PurgeDutyCycleSection(
    purgeDutyCycle: Double,
    isActive: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.purge_duty_cycle),
                    fontSize = 10.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colors.gaugeGreen, RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "%.0f%%".format(purgeDutyCycle),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    purgeDutyCycle > 50 -> colors.gaugeOrange
                    purgeDutyCycle > 0 -> colors.gaugeGreen
                    else -> colors.textDim
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(colors.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((purgeDutyCycle / 100.0).toFloat())
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    colors.gaugeGreen.copy(alpha = 0.6f),
                                    if (isActive) colors.gaugeGreen else colors.textDim
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun TankPressureSection(
    tankPressure: Double,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val pressureColor = when {
        tankPressure > 10.0 -> colors.gaugeRed
        tankPressure > 5.0 -> colors.gaugeOrange
        tankPressure < -10.0 -> colors.gaugeRed
        tankPressure < -5.0 -> colors.gaugeOrange
        else -> colors.gaugeGreen
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.tank_pressure),
                fontSize = 10.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (tankPressure != 0.0) "%.1f".format(tankPressure) else "—",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = pressureColor
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "mbar",
                    fontSize = 12.sp,
                    color = colors.textDim,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    tankPressure > 5.0 -> stringResource(R.string.pressure_high)
                    tankPressure < -5.0 -> stringResource(R.string.pressure_low)
                    else -> stringResource(R.string.pressure_normal)
                },
                fontSize = 10.sp,
                color = pressureColor
            )
        }
    }
}

@Composable
private fun LeakWarningSection(colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.gaugeRed.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = colors.gaugeRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.evap_leak_detected),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gaugeRed
                )
                Text(
                    text = stringResource(R.string.evap_leak_action),
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun EVAPExplanationText(colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = colors.textDim,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.evap_description),
                fontSize = 10.sp,
                color = colors.textDim
            )
        }
    }
}
