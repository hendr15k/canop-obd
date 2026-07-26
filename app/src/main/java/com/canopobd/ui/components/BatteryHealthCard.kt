package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.*
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun BatteryHealthCard(
    batteryData: BatteryData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val voltageColor by animateColorAsState(
        targetValue = when (batteryData.voltageStatus) {
            BatteryVoltageStatus.CHARGING -> colors.gaugeGreen
            BatteryVoltageStatus.GOOD -> colors.gaugeGreen
            BatteryVoltageStatus.LOW -> colors.gaugeOrange
            BatteryVoltageStatus.CRITICAL -> colors.gaugeRed
        },
        animationSpec = tween(300),
        label = "voltage_color"
    )

    val isCritical = batteryData.voltageStatus == BatteryVoltageStatus.CRITICAL

    val infiniteTransition = rememberInfiniteTransition(label = "battery_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isCritical) {
            0.8f
        } else {
            0.3f
        },
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
                if (isCritical) {
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
                        imageVector = Icons.Filled.BatteryChargingFull,
                        contentDescription = null,
                        tint = voltageColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.battery_health),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                TrendIndicator(
                    trend = batteryData.trend,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (batteryData.voltage > 0) {
                                "%.1f".format(batteryData.voltage)
                            } else {
                                "—"
                            },
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = voltageColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "V",
                            fontSize = 18.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = batteryData.voltageStatus.label,
                        fontSize = 12.sp,
                        color = voltageColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    SOCIndicator(
                        soc = batteryData.estimatedSOC,
                        status = batteryData.socStatus,
                        colors = colors
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ChargingStatusRow(
                batteryData = batteryData,
                colors = colors
            )

            if (batteryData.voltageHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                VoltageTrendChart(
                    voltageHistory = batteryData.voltageHistory,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun TrendIndicator(
    trend: BatteryTrend,
    colors: AppColors
) {
    val trendColor = when (trend) {
        BatteryTrend.RISING -> colors.gaugeGreen
        BatteryTrend.FALLING -> colors.gaugeOrange
        BatteryTrend.STABLE -> colors.textDim
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = trendColor.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = trend.icon,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = trendColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = trend.label,
                fontSize = 10.sp,
                color = trendColor
            )
        }
    }
}

@Composable
private fun SOCIndicator(
    soc: Int,
    status: SOCStatus,
    colors: AppColors
) {
    val socColor = when (status) {
        SOCStatus.GOOD -> colors.gaugeGreen
        SOCStatus.MODERATE -> colors.gaugeOrange
        SOCStatus.LOW -> colors.gaugeRed
    }

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                color = colors.surfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            val sweepAngle = (soc.coerceIn(0, 100) / 100f) * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        socColor.copy(alpha = 0.3f),
                        socColor
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (soc > 0) {
                    "$soc%"
                } else {
                    "—"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (soc > 0 && soc < 40) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = colors.gaugeOrange,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ChargingStatusRow(
    batteryData: BatteryData,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChargingInfoItem(
                label = stringResource(R.string.charging),
                value = if (batteryData.isCharging) {
                    stringResource(R.string.yes)
                } else {
                    stringResource(R.string.no)
                },
                valueColor = if (batteryData.isCharging) { colors.gaugeGreen } else { colors.textDim },
                colors = colors
            )

            Spacer(modifier = Modifier.width(12.dp))
            ChargingStatusDivider(colors)

            ChargingInfoItem(
                label = stringResource(R.string.alternator_duty),
                value = "%.0f%%".format(batteryData.alternatorDuty),
                valueColor = when {
                    batteryData.alternatorDuty > 80 -> colors.gaugeOrange
                    batteryData.alternatorDuty > 0 -> colors.gaugeGreen
                    else -> colors.textDim
                },
                colors = colors
            )

            Spacer(modifier = Modifier.width(12.dp))
            ChargingStatusDivider(colors)

            ChargingInfoItem(
                label = stringResource(R.string.module_voltage),
                value = if (batteryData.controlModuleVoltage > 0) {
                    "%.1fV".format(batteryData.controlModuleVoltage)
                } else {
                    "—"
                },
                valueColor = colors.textSecondary,
                colors = colors
            )
        }
    }
}

@Composable
private fun ChargingStatusDivider(colors: AppColors) {
    Spacer(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(colors.borderSubtle)
    )
}

@Composable
private fun ChargingInfoItem(
    label: String,
    value: String,
    valueColor: Color,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun VoltageTrendChart(
    voltageHistory: List<Float>,
    colors: AppColors
) {
    val lastReadings = voltageHistory.takeLast(20)

    Column {
        Text(
            text = stringResource(R.string.voltage_trend),
            fontSize = 10.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceVariant)
                .padding(4.dp)
        ) {
            if (lastReadings.size < 2) {
                return@Canvas
            }

            val minV = (lastReadings.min() - 0.5f).coerceAtLeast(10f)
            val maxV = (lastReadings.max() + 0.5f).coerceAtMost(16f)
            val range = maxV - minV

            lastReadings.forEachIndexed { index, voltage ->
                val x = size.width * index / (lastReadings.size - 1).coerceAtLeast(1)
                val y = size.height - ((voltage - minV) / range * size.height).coerceIn(0f, size.height)

                if (index > 0) {
                    val prevX = size.width * (index - 1) / (lastReadings.size - 1).coerceAtLeast(1)
                    val prevV = lastReadings[index - 1]
                    val prevY = size.height - ((prevV - minV) / range * size.height).coerceIn(0f, size.height)

                    val lineColor = when {
                        voltage >= 14.0f -> colors.gaugeGreen
                        voltage >= 12.6f -> colors.gaugeYellow
                        else -> colors.gaugeRed
                    }

                    drawLine(
                        color = lineColor,
                        start = Offset(prevX, prevY),
                        end = Offset(x, y),
                        strokeWidth = 2f
                    )
                }

                drawCircle(
                    color = when {
                        voltage >= 14.0f -> colors.gaugeGreen
                        voltage >= 12.6f -> colors.gaugeYellow
                        else -> colors.gaugeRed
                    },
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun BatteryHealthCardCompact(
    batteryData: BatteryData,
    modifier: Modifier = Modifier,
    colors: AppColors = LocalAppColors.current
) {
    val voltageColor = when (batteryData.voltageStatus) {
        BatteryVoltageStatus.CHARGING -> colors.gaugeGreen
        BatteryVoltageStatus.GOOD -> colors.gaugeGreen
        BatteryVoltageStatus.LOW -> colors.gaugeOrange
        BatteryVoltageStatus.CRITICAL -> colors.gaugeRed
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (batteryData.isCharging) {
                        Icons.Filled.BatteryChargingFull
                    } else {
                        Icons.Filled.BatteryFull
                    },
                    contentDescription = null,
                    tint = voltageColor,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (batteryData.voltage > 0) {
                            "%.1fV".format(batteryData.voltage)
                        } else {
                            "—"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = batteryData.voltageStatus.label,
                        fontSize = 10.sp,
                        color = voltageColor
                    )
                }
            }

            if (batteryData.estimatedSOC > 0) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${batteryData.estimatedSOC}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (batteryData.socStatus) {
                            SOCStatus.GOOD -> colors.gaugeGreen
                            SOCStatus.MODERATE -> colors.gaugeOrange
                            SOCStatus.LOW -> colors.gaugeRed
                        }
                    )
                }
            }
        }
    }
}
