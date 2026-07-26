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
fun EGRHealthCard(
    egrData: EGRData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val valveColor by animateColorAsState(
        targetValue = when (egrData.valveStatus) {
            EGRValveStatus.NORMAL -> colors.gaugeGreen
            EGRValveStatus.WARNING -> colors.gaugeOrange
            EGRValveStatus.ERROR_HIGH -> colors.gaugeRed
            EGRValveStatus.NOT_SUPPORTED -> colors.textDim
        },
        animationSpec = tween(300),
        label = "valve_color"
    )

    val isError = egrData.valveStatus == EGRValveStatus.ERROR_HIGH

    val infiniteTransition = rememberInfiniteTransition(label = "egr_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isError) { 0.8f } else { 0.3f },
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
                if (isError) {
                    Modifier.border(
                        width = 2.dp,
                        color = colors.gaugeRed.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else { Modifier }
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
                        imageVector = Icons.Filled.Air,
                        contentDescription = null,
                        tint = valveColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.egr_system),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                ValveStatusBadge(
                    status = egrData.valveStatus,
                    color = valveColor,
                    colors = colors
                )
            }

            if (!egrData.isSupported) {
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
                            text = stringResource(R.string.egr_not_supported),
                            fontSize = 12.sp,
                            color = colors.textDim
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EGRValueBox(
                        label = stringResource(R.string.command),
                        value = "%.0f%%".format(egrData.commandedPercent),
                        subtext = stringResource(R.string.egr_position),
                        color = colors.accent,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )

                    EGRValueBox(
                        label = stringResource(R.string.error),
                        value = "%+.1f%%".format(egrData.errorPercent),
                        subtext = stringResource(R.string.egr_deviation),
                        color = when {
                            egrData.errorPercent > 15.0 -> colors.gaugeRed
                            egrData.errorPercent > 5.0 -> colors.gaugeOrange
                            else -> colors.gaugeGreen
                        },
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )

                    if (egrData.temperature > -40) {
                        EGRValueBox(
                            label = stringResource(R.string.temperatures),
                            value = "%.0f°C".format(egrData.temperature),
                            subtext = stringResource(R.string.egr_temp),
                            color = when {
                                egrData.temperature > 200 -> colors.gaugeRed
                                egrData.temperature > 150 -> colors.gaugeOrange
                                egrData.temperature > 50 -> colors.gaugeYellow
                                else -> colors.gaugeCyan
                            },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (egrData.errorPercent != 0.0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    EGRProgressBar(
                        errorPercent = egrData.errorPercent,
                        colors = colors
                    )
                }

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.gaugeRed.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = colors.gaugeRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.egr_error_warning),
                                fontSize = 11.sp,
                                color = colors.gaugeRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun ValveStatusBadge(
    status: EGRValveStatus,
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
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
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
private fun EGRValueBox(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = colors.textDim
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun EGRProgressBar(
    errorPercent: Double,
    colors: AppColors
) {
    val absError = kotlin.math.abs(errorPercent)
    val barColor = when {
        absError > 15.0 -> colors.gaugeRed
        absError > 5.0 -> colors.gaugeOrange
        else -> colors.gaugeGreen
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.egr_health),
                fontSize = 10.sp,
                color = colors.textSecondary
            )
            Text(
                text = when {
                    absError > 15.0 -> stringResource(R.string.critical)
                    absError > 5.0 -> stringResource(R.string.warning)
                    else -> stringResource(R.string.ok)
                },
                fontSize = 10.sp,
                color = barColor,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                    .fillMaxWidth((absError / 30.0).toFloat().coerceIn(0f, 1f))
                    .background(barColor)
            )
        }
    }
}
