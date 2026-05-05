package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.domain.FuelTrimAnalyzer
import com.canopobd.ui.theme.*

@Composable
fun FuelTrimCard(
    trimData: FuelTrimAnalyzer.FuelTrimData,
    modifier: Modifier = Modifier,
    colors: AppColors = DefaultAppColors
) {
    val statusColor by animateColorAsState(
        targetValue = when (trimData.status) {
            FuelTrimAnalyzer.TrimStatus.OPTIMAL -> colors.gaugeGreen
            FuelTrimAnalyzer.TrimStatus.SLIGHTLY_LEAN,
            FuelTrimAnalyzer.TrimStatus.SLIGHTLY_RICH -> colors.gaugeYellow
            FuelTrimAnalyzer.TrimStatus.MODERATELY_LEAN,
            FuelTrimAnalyzer.TrimStatus.MODERATELY_RICH -> colors.gaugeOrange
            FuelTrimAnalyzer.TrimStatus.SEVERELY_LEAN,
            FuelTrimAnalyzer.TrimStatus.SEVERELY_RICH -> colors.gaugeRed
            else -> colors.textSecondary
        },
        animationSpec = tween(300),
        label = "statusColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.fuel_trim_analysis_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${trimData.healthScore}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TrimGauge(
                trim = trimData.combinedTrimBank1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 8.dp),
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrimValueCard(
                    label = stringResource(R.string.stft_label),
                    value = trimData.stftBank1,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
                TrimValueCard(
                    label = stringResource(R.string.ltft_label),
                    value = trimData.ltftBank1,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (trimData.status) {
                            FuelTrimAnalyzer.TrimStatus.OPTIMAL -> Icons.Filled.CheckCircle
                            FuelTrimAnalyzer.TrimStatus.SLIGHTLY_LEAN,
                            FuelTrimAnalyzer.TrimStatus.SLIGHTLY_RICH -> Icons.Filled.Info
                            else -> Icons.Filled.Warning
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = trimData.status.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                        Text(
                            text = trimData.status.description,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            if (trimData.stftBank2 != 0.0 || trimData.ltftBank2 != 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bank 2: STFT ${"%.1f".format(trimData.stftBank2)}% | LTFT ${"%.1f".format(trimData.ltftBank2)}%",
                    fontSize = 10.sp,
                    color = colors.textDim
                )
            }
        }
    }
}

@Composable
private fun TrimGauge(
    trim: Double,
    modifier: Modifier = Modifier,
    colors: AppColors
) {
    val animatedTrim by animateFloatAsState(
        targetValue = trim.toFloat().coerceIn(-20f, 20f),
        animationSpec = tween(500),
        label = "trimValue"
    )

    val barColor = when {
        trim > 10 -> colors.gaugeRed
        trim > 5 -> colors.gaugeOrange
        trim > 2 -> colors.gaugeYellow
        trim < -10 -> colors.gaugeRed
        trim < -5 -> colors.gaugeOrange
        trim < -2 -> colors.gaugeYellow
        else -> colors.gaugeGreen
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val barHeight = 20.dp.toPx()
        val centerY = size.height / 2
        val centerX = size.width / 2
        val maxBarWidth = size.width / 2 - 20.dp.toPx()

        drawLine(
            color = colors.surfaceVariant,
            start = Offset(20.dp.toPx(), centerY),
            end = Offset(size.width - 20.dp.toPx(), centerY),
            strokeWidth = barHeight,
            cap = StrokeCap.Round
        )

        drawLine(
            color = colors.gaugeGreen.copy(alpha = 0.3f),
            start = Offset(centerX, centerY - barHeight / 2),
            end = Offset(centerX, centerY + barHeight / 2),
            strokeWidth = 2.dp.toPx()
        )

        val leanZoneEnd = centerX - maxBarWidth * 0.25f
        drawLine(
            color = colors.gaugeGreen.copy(alpha = 0.2f),
            start = Offset(leanZoneEnd, centerY - barHeight / 2),
            end = Offset(leanZoneEnd, centerY + barHeight / 2),
            strokeWidth = 1.dp.toPx()
        )

        val richZoneStart = centerX + maxBarWidth * 0.25f
        drawLine(
            color = colors.gaugeGreen.copy(alpha = 0.2f),
            start = Offset(richZoneStart, centerY - barHeight / 2),
            end = Offset(richZoneStart, centerY + barHeight / 2),
            strokeWidth = 1.dp.toPx()
        )

        if (animatedTrim != 0f) {
            val indicatorPos = centerX + (animatedTrim / 20f) * maxBarWidth
            val clampedPos = indicatorPos.coerceIn(20.dp.toPx(), size.width - 20.dp.toPx())

            drawCircle(
                color = barColor,
                radius = barHeight / 2 + 4.dp.toPx(),
                center = Offset(clampedPos, centerY)
            )
            drawCircle(
                color = barColor,
                radius = barHeight / 2,
                center = Offset(clampedPos, centerY)
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Mager +${"%.0f".format(maxOf(trim, 0.0))}%",
                fontSize = 9.sp,
                color = colors.gaugeRed.copy(alpha = 0.7f)
            )
            Text(
                text = if (trim >= 0) "+${"%.1f".format(trim)}%" else "${"%.1f".format(trim)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
            Text(
                text = "Fett ${"%.0f".format(maxOf(-trim, 0.0))}%",
                fontSize = 9.sp,
                color = colors.gaugeYellow.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TrimValueCard(
    label: String,
    value: Double,
    modifier: Modifier = Modifier,
    colors: AppColors
) {
    val valueColor = when {
        value > 10 -> colors.gaugeRed
        value > 5 -> colors.gaugeOrange
        value > 2 -> colors.gaugeYellow
        value < -10 -> colors.gaugeRed
        value < -5 -> colors.gaugeOrange
        value < -2 -> colors.gaugeYellow
        else -> colors.gaugeGreen
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (value >= 0) "+${"%.1f".format(value)}%" else "${"%.1f".format(value)}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun FuelTrimCardCompact(
    stft: Double,
    ltft: Double,
    healthScore: Int,
    modifier: Modifier = Modifier,
    colors: AppColors = DefaultAppColors
) {
    val combinedTrim = stft + ltft
    val statusColor = when {
        combinedTrim > 10 -> colors.gaugeRed
        combinedTrim > 5 -> colors.gaugeOrange
        combinedTrim > 2 -> colors.gaugeYellow
        combinedTrim < -10 -> colors.gaugeRed
        combinedTrim < -5 -> colors.gaugeOrange
        combinedTrim < -2 -> colors.gaugeYellow
        else -> colors.gaugeGreen
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kraftstoff-Trim",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (combinedTrim >= 0) "+${"%.1f".format(combinedTrim)}" else "${"%.1f".format(combinedTrim)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "%",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                    )
                }
                Text(
                    text = "STFT ${"%.1f".format(stft)}% | LTFT ${"%.1f".format(ltft)}%",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.2f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$healthScore",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "Score",
                        fontSize = 9.sp,
                        color = colors.textDim
                    )
                }
            }
        }
    }
}
