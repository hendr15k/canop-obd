package com.canopobd.ui.turbo

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

data class EGTData(
    val currentEGT: Double = 0.0,
    val minEGT: Double = 0.0,
    val maxEGT: Double = 0.0,
    val avgEGT: Double = 0.0,
    val egtHistory: List<Double> = emptyList(),
    val warningThreshold: Double = 750.0,
    val criticalThreshold: Double = 850.0,
    val isWarning: Boolean = false,
    val isCritical: Boolean = false
)

@Composable
fun EGTMonitorScreen(
    egtData: EGTData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Thermostat,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EGT Überwachung",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    EGTGauge(
                        currentEGT = egtData.currentEGT,
                        warningThreshold = egtData.warningThreshold,
                        criticalThreshold = egtData.criticalThreshold,
                        colors = colors
                    )
                }
                item {
                    EGTThresholds(
                        currentEGT = egtData.currentEGT,
                        warningThreshold = egtData.warningThreshold,
                        criticalThreshold = egtData.criticalThreshold,
                        colors = colors
                    )
                }
                item {
                    EGTGraph(
                        history = egtData.egtHistory,
                        warningThreshold = egtData.warningThreshold,
                        criticalThreshold = egtData.criticalThreshold,
                        colors = colors
                    )
                }
                item {
                    EGTStatistics(
                        minEGT = egtData.minEGT,
                        maxEGT = egtData.maxEGT,
                        avgEGT = egtData.avgEGT,
                        currentEGT = egtData.currentEGT,
                        colors = colors
                    )
                }
                item {
                    EGTStatusWarning(
                        isWarning = egtData.isWarning,
                        isCritical = egtData.isCritical,
                        currentEGT = egtData.currentEGT,
                        colors = colors
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = colors.accent)
            }
        }
    )
}

@Composable
private fun EGTGauge(
    currentEGT: Double,
    warningThreshold: Double,
    criticalThreshold: Double,
    colors: AppColors
) {
    val maxEGT = 1000.0
    val egtColor by animateColorAsState(
        targetValue = when {
            currentEGT >= criticalThreshold -> colors.gaugeRed
            currentEGT >= warningThreshold -> colors.gaugeOrange
            currentEGT >= 600 -> colors.gaugeYellow
            currentEGT >= 300 -> colors.gaugeGreen
            else -> colors.textSecondary
        },
        animationSpec = tween(300),
        label = "egt_color"
    )

    val animatedEGT by animateFloatAsState(
        targetValue = currentEGT.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "egt_value"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = (currentEGT / maxEGT).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "egt_progress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EGT (Auslasstemperatur)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    drawArc(
                        color = colors.surfaceVariant,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val warningPos = ((warningThreshold / maxEGT) * 270f).toFloat()
                    val criticalPos = ((criticalThreshold / maxEGT) * 270f).toFloat()

                    drawArc(
                        color = colors.gaugeGreen.copy(alpha = 0.4f),
                        startAngle = 135f,
                        sweepAngle = warningPos - 135f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = colors.gaugeOrange.copy(alpha = 0.4f),
                        startAngle = warningPos,
                        sweepAngle = criticalPos - warningPos,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = colors.gaugeRed.copy(alpha = 0.4f),
                        startAngle = criticalPos,
                        sweepAngle = 270f + 135f - criticalPos,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val sweep = animatedProgress * 270f
                    drawArc(
                        color = egtColor,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.0f°C".format(animatedEGT),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = egtColor
                    )
                    Text(
                        text = "EGT",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EGTThresholdMarker(
                    label = "Warnung",
                    value = "%.0f°C".format(warningThreshold),
                    color = colors.gaugeOrange,
                    colors = colors
                )
                EGTThresholdMarker(
                    label = "Kritisch",
                    value = "%.0f°C".format(criticalThreshold),
                    color = colors.gaugeRed,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun EGTThresholdMarker(
    label: String,
    value: String,
    color: Color,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = colors.textDim)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun EGTThresholds(
    currentEGT: Double,
    warningThreshold: Double,
    criticalThreshold: Double,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Schwellwerte",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThresholdItem(
                    label = "Normal",
                    max = warningThreshold,
                    color = colors.gaugeGreen,
                    colors = colors
                )
                ThresholdItem(
                    label = "Warnung",
                    min = warningThreshold,
                    max = criticalThreshold,
                    color = colors.gaugeOrange,
                    colors = colors
                )
                ThresholdItem(
                    label = "Kritisch",
                    min = criticalThreshold,
                    color = colors.gaugeRed,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun ThresholdItem(
    label: String,
    min: Double? = null,
    max: Double? = null,
    color: Color,
    colors: AppColors
) {
    val rangeText = when {
        min != null && max != null -> "%.0f - %.0f°C".format(min, max)
        min != null -> "> %.0f°C".format(min)
        max != null -> "< %.0f°C".format(max)
        else -> label
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = colors.textDim)
        Text(text = rangeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun EGTGraph(
    history: List<Double>,
    warningThreshold: Double,
    criticalThreshold: Double,
    colors: AppColors
) {
    val chartData = remember(history) {
        history.takeLast(20).ifEmpty {
            List(20) { (300 + (it * 30)).toDouble() }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "EGT-Verlauf",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val w = size.width
                val h = size.height
                val padding = 8.dp.toPx()
                val graphW = w - padding * 2
                val graphH = h - padding * 2
                val maxEGT = 1000.0

                drawLine(
                    color = colors.surfaceVariant,
                    start = Offset(padding, padding),
                    end = Offset(padding, h - padding),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = colors.surfaceVariant,
                    start = Offset(padding, h - padding),
                    end = Offset(w - padding, h - padding),
                    strokeWidth = 1.dp.toPx()
                )

                val warnY = (padding + graphH * (1 - (warningThreshold / maxEGT).toFloat())).toFloat()
                drawLine(
                    color = colors.gaugeOrange.copy(alpha = 0.5f),
                    start = Offset(padding, warnY),
                    end = Offset(w - padding, warnY),
                    strokeWidth = 1.dp.toPx()
                )

                val critY = (padding + graphH * (1 - (criticalThreshold / maxEGT).toFloat())).toFloat()
                drawLine(
                    color = colors.gaugeRed.copy(alpha = 0.5f),
                    start = Offset(padding, critY),
                    end = Offset(w - padding, critY),
                    strokeWidth = 1.dp.toPx()
                )

                val points = chartData.mapIndexed { index, egt ->
                    val x = padding + (graphW / (chartData.size - 1).coerceAtLeast(1)) * index
                    val y = (padding + graphH * (1 - (egt / maxEGT).toFloat())).toFloat()
                    Offset(x, y.coerceIn(padding, h - padding))
                }

                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val avgEgt = (chartData[i] + chartData[i + 1]) / 2
                        val lineColor = when {
                            avgEgt >= criticalThreshold -> colors.gaugeRed
                            avgEgt >= warningThreshold -> colors.gaugeOrange
                            else -> colors.gaugeGreen
                        }
                        drawLine(
                            color = lineColor,
                            start = p1,
                            end = p2,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                points.forEachIndexed { index, point ->
                    val egt = chartData[index]
                    val dotColor = when {
                        egt >= criticalThreshold -> colors.gaugeRed
                        egt >= warningThreshold -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    }
                    drawCircle(
                        color = dotColor,
                        radius = 3.dp.toPx(),
                        center = point
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0°C", fontSize = 9.sp, color = colors.textDim)
                Text("500°C", fontSize = 9.sp, color = colors.textDim)
                Text("1000°C", fontSize = 9.sp, color = colors.textDim)
            }
        }
    }
}

@Composable
private fun EGTStatistics(
    minEGT: Double,
    maxEGT: Double,
    avgEGT: Double,
    currentEGT: Double,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EGTStatItem(label = "Min", value = "%.0f°C".format(minEGT), colors = colors)
            EGTStatItem(label = "Avg", value = "%.0f°C".format(avgEGT), colors = colors)
            EGTStatItem(label = "Max", value = "%.0f°C".format(maxEGT), colors = colors)
            EGTStatItem(label = "Ist", value = "%.0f°C".format(currentEGT), colors = colors)
        }
    }
}

@Composable
private fun EGTStatItem(
    label: String,
    value: String,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = colors.textDim)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
    }
}

@Composable
private fun EGTStatusWarning(
    isWarning: Boolean,
    isCritical: Boolean,
    currentEGT: Double,
    colors: AppColors
) {
    val hasAlert = isWarning || isCritical

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isCritical) colors.gaugeRed.copy(alpha = 0.15f)
        else if (isWarning) colors.gaugeOrange.copy(alpha = 0.15f)
        else colors.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isCritical -> Icons.Filled.Dangerous
                    isWarning -> Icons.Filled.Warning
                    else -> Icons.Filled.CheckCircle
                },
                contentDescription = null,
                tint = when {
                    isCritical -> colors.gaugeRed
                    isWarning -> colors.gaugeOrange
                    else -> colors.gaugeGreen
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = when {
                        isCritical -> "Kritisch!"
                        isWarning -> "Warnung"
                        else -> "Normal"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCritical -> colors.gaugeRed
                        isWarning -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    }
                )
                if (hasAlert) {
                    Text(
                        text = "EGT: %.0f°C - Überwachung erforderlich!".format(currentEGT),
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}