package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.*
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors
import kotlin.math.abs

@Composable
fun LambdaO2SensorCard(
    lambdaData: LambdaSensorData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val preCatStatusColor by animateColorAsState(
        targetValue = when (lambdaData.preCatStatus) {
            LambdaStatus.IDEAL -> colors.gaugeGreen
            LambdaStatus.OK -> colors.gaugeYellow
            LambdaStatus.DEVIATION -> colors.gaugeRed
        },
        animationSpec = tween(300),
        label = "precat_color"
    )

    val postCatStatusColor by animateColorAsState(
        targetValue = when (lambdaData.postCatStatus) {
            LambdaStatus.IDEAL -> colors.gaugeGreen
            LambdaStatus.OK -> colors.gaugeYellow
            LambdaStatus.DEVIATION -> colors.gaugeRed
        },
        animationSpec = tween(300),
        label = "postcat_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (lambdaData.preCatStatus == LambdaStatus.DEVIATION || lambdaData.postCatStatus == LambdaStatus.DEVIATION) {
                    Modifier.border(
                        width = 1.dp,
                        color = colors.gaugeRed.copy(alpha = 0.5f),
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
                        tint = colors.gaugeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.lambda_o2_sensors),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                if (lambdaData.crossCountRate > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.gaugeCyan.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = colors.gaugeCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.cross_count_rate, lambdaData.crossCountRate),
                                fontSize = 10.sp,
                                color = colors.gaugeCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreCatSensorSection(
                    lambdaData = lambdaData,
                    statusColor = preCatStatusColor,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                PostCatSensorSection(
                    lambdaData = lambdaData,
                    statusColor = postCatStatusColor,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            if (lambdaData.voltageHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                O2VoltageChart(
                    voltageHistory = lambdaData.voltageHistory,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FuelTrimSummary(
                stft = lambdaData.fuelTrimShort,
                ltft = lambdaData.fuelTrimLong,
                colors = colors
            )
        }
    }
}

@Composable
private fun PreCatSensorSection(
    lambdaData: LambdaSensorData,
    statusColor: Color,
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
                    text = stringResource(R.string.pre_cat),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                StatusBadge(status = lambdaData.preCatStatus, color = statusColor, colors = colors)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (lambdaData.preCatLambda > 0) { "%.2f".format(lambdaData.preCatLambda) } else { "—" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "λ",
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.voltage), fontSize = 9.sp, color = colors.textDim)
                    Text(
                        text = "%.3fV".format(lambdaData.preCatVoltage),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.heater), fontSize = 9.sp, color = colors.textDim)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (lambdaData.preCatHeaterActive) { colors.gaugeOrange } else { colors.textDim },
                                    RoundedCornerShape(3.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lambdaData.preCatHeaterActive) { stringResource(R.string.active) } else { stringResource(R.string.inactive) },
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCatSensorSection(
    lambdaData: LambdaSensorData,
    statusColor: Color,
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
                    text = stringResource(R.string.post_cat),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                StatusBadge(status = lambdaData.postCatStatus, color = statusColor, colors = colors)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (lambdaData.postCatVoltage > 0) { "%.3f".format(lambdaData.postCatVoltage) } else { "—" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "V",
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.lambda_short), fontSize = 9.sp, color = colors.textDim)
                    Text(
                        text = if (lambdaData.postCatLambda > 0) { "%.2f".format(lambdaData.postCatLambda) } else { "—" },
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.heater), fontSize = 9.sp, color = colors.textDim)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (lambdaData.postCatHeaterActive) { colors.gaugeOrange } else { colors.textDim },
                                    RoundedCornerShape(3.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lambdaData.postCatHeaterActive) { stringResource(R.string.active) } else { stringResource(R.string.inactive) },
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun StatusBadge(
    status: LambdaStatus,
    color: Color,
    colors: AppColors
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = status.label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun O2VoltageChart(
    voltageHistory: List<Float>,
    colors: AppColors
) {
    Column {
        Text(
            text = stringResource(R.string.voltage_history),
            fontSize = 10.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceVariant)
                .padding(4.dp)
        ) {
            if (voltageHistory.size < 2) { return@Canvas }

            val maxV = 1.0f
            val minV = 0.0f
            val range = maxV - minV

            val path = Path()
            voltageHistory.forEachIndexed { index, voltage ->
                val x = size.width * index / (voltageHistory.size - 1).coerceAtLeast(1)
                val y = size.height - ((voltage - minV) / range * size.height).coerceIn(0f, size.height)
                if (index == 0) { path.moveTo(x, y) } else { path.lineTo(x, y) }
            }

            drawPath(
                path = path,
                color = androidx.compose.ui.graphics.Color(0xFF06B6D4),
                style = Stroke(width = 2f)
            )

            val stoichY = size.height * 0.45f
            drawLine(
                color = colors.gaugeGreen.copy(alpha = 0.3f),
                start = Offset(0f, stoichY),
                end = Offset(size.width, stoichY),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun FuelTrimSummary(
    stft: Double,
    ltft: Double,
    colors: AppColors
) {
    val totalTrim = stft + ltft
    val trimColor = when {
        abs(totalTrim) > 10.0 -> colors.gaugeRed
        abs(totalTrim) > 5.0 -> colors.gaugeOrange
        else -> colors.gaugeGreen
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stringResource(R.string.short_term_fuel_trim), fontSize = 9.sp, color = colors.textDim)
                Text(
                    text = "%+.1f%%".format(stft),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        abs(stft) > 8.0 -> colors.gaugeRed
                        abs(stft) > 4.0 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    }
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.total_trim), fontSize = 9.sp, color = colors.textDim)
                Text(
                    text = "%+.1f%%".format(totalTrim),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = trimColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.long_term_fuel_trim), fontSize = 9.sp, color = colors.textDim)
                Text(
                    text = "%+.1f%%".format(ltft),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        abs(ltft) > 8.0 -> colors.gaugeRed
                        abs(ltft) > 4.0 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    }
                )
            }
        }
    }
}
