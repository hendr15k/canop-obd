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
import com.canopobd.data.model.TurboData
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

data class OverboostHistory(
    val timestamp: Long,
    val boostPressure: Double,
    val durationSeconds: Int
)

@Composable
fun OverboostScreen(
    turboData: TurboData,
    overboostHistory: List<OverboostHistory> = emptyList(),
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
                    Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Überboost-Überwachung",
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
                    OverboostTimer(
                        isActive = turboData.overboostActive,
                        secondsRemaining = turboData.overboostSecondsRemaining,
                        maxDuration = turboData.overboostMaxDuration,
                        colors = colors
                    )
                }
                item {
                    BoostVsTarget(
                        currentBoost = turboData.boostPressure,
                        targetBoost = turboData.boostTarget,
                        colors = colors
                    )
                }
                item {
                    OverboostHistoryChart(
                        history = overboostHistory,
                        colors = colors
                    )
                }
                item {
                    OverboostStatus(
                        turboData = turboData,
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
private fun OverboostTimer(
    isActive: Boolean,
    secondsRemaining: Int,
    maxDuration: Int,
    colors: AppColors
) {
    val progress = if (maxDuration > 0) secondsRemaining.toFloat() / maxDuration else 0f
    val progressInverse = 1f - progress

    val timerColor = when {
        progressInverse < 0.3f -> colors.gaugeRed
        progressInverse < 0.6f -> colors.gaugeOrange
        else -> colors.gaugeGreen
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressInverse,
        animationSpec = tween(300),
        label = "timer_progress"
    )

    val infiniteTransition = rememberInfiniteTransition()
    @Suppress("UNUSED_VARIABLE")
    val _pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.7f else 0f,
        targetValue = if (isActive) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
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
                text = "Überboost-Zeit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = (size.minDimension / 2) - strokeWidth
                    val center = Offset(size.width / 2, size.height / 2)

                    drawCircle(
                        color = colors.surfaceVariant,
                        radius = radius + strokeWidth,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    if (isActive) {
                        drawArc(
                            color = timerColor.copy(alpha = 0.5f + 0.3f * animatedProgress),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    drawCircle(
                        color = colors.surfaceVariant,
                        radius = radius * 0.7f,
                        center = center
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isActive) "%d".format(secondsRemaining) else "—",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) timerColor else colors.textSecondary
                    )
                    Text(
                        text = "Sekunden",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isActive) {
                Text(
                    text = "Maximal: %d Sekunden".format(maxDuration),
                    fontSize = 11.sp,
                    color = timerColor
                )
            }
        }
    }
}

@Composable
private fun BoostVsTarget(
    currentBoost: Double,
    targetBoost: Double,
    colors: AppColors
) {
    val deviation = currentBoost - targetBoost
    val deviationColor = when {
        deviation > 0.2 -> colors.gaugeRed
        deviation > 0.1 -> colors.gaugeOrange
        deviation >= -0.1 -> colors.gaugeGreen
        deviation > -0.2 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }

    @Suppress("UNUSED_VARIABLE")
    val _animatedCurrent by animateFloatAsState(
        targetValue = currentBoost.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "current_boost"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Soll vs Ist",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                BoostComparisonItem(
                    label = "Soll",
                    value = targetBoost,
                    color = colors.accent,
                    modifier = Modifier.weight(1f)
                )
                BoostComparisonItem(
                    label = "Ist",
                    value = currentBoost,
                    color = deviationColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surfaceVariant)
            ) {
                val maxBoost = 2.0
                val targetNorm = (targetBoost / maxBoost).coerceIn(0.0, 1.0).toFloat()
                val currentNorm = (currentBoost / maxBoost).coerceIn(0.0, 1.0).toFloat()

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(targetNorm)
                        .background(colors.accent.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(currentNorm)
                        .background(deviationColor.copy(alpha = 0.7f))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.0 bar", fontSize = 9.sp, color = colors.textDim)
                Text("Soll", fontSize = 9.sp, color = colors.accent)
                Text("Ist", fontSize = 9.sp, color = deviationColor)
                Text("2.0 bar", fontSize = 9.sp, color = colors.textDim)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Abweichung: %+.2f bar".format(deviation),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = deviationColor
                )
                Text(
                    text = "Effizienz: %.0f%%".format((currentBoost.coerceAtLeast(0.01) / targetBoost) * 100),
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun BoostComparisonItem(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%.2f".format(value),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "bar",
            fontSize = 10.sp,
            color = color.copy(alpha = 0.7f)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = color.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun OverboostHistoryChart(
    history: List<OverboostHistory>,
    colors: AppColors
) {
    val sampleData = remember(history) {
        history.ifEmpty {
            listOf(
                OverboostHistory(System.currentTimeMillis() - 300000, 1.25, 8),
                OverboostHistory(System.currentTimeMillis() - 240000, 1.18, 6),
                OverboostHistory(System.currentTimeMillis() - 180000, 1.32, 9),
                OverboostHistory(System.currentTimeMillis() - 120000, 1.28, 7),
                OverboostHistory(System.currentTimeMillis() - 60000, 1.22, 5)
            )
        }
    }

    val maxBoost = sampleData.maxOfOrNull { it.boostPressure } ?: 1.5

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Überboost-Verlauf",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "%d Einträge".format(sampleData.size),
                    fontSize = 10.sp,
                    color = colors.textDim
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val w = size.width
                val h = size.height
                val padding = 8.dp.toPx()
                val graphW = w - padding * 2
                val graphH = h - padding * 2

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

                val targetY = (padding + graphH * 0.5f).toFloat()
                drawLine(
                    color = colors.accent.copy(alpha = 0.5f),
                    start = Offset(padding, targetY),
                    end = Offset(w - padding, targetY),
                    strokeWidth = 1.dp.toPx()
                )

                val points = sampleData.mapIndexed { index, entry ->
                    val x = padding + (graphW / (sampleData.size - 1).coerceAtLeast(1)) * index
                    val y = (padding + graphH * (1 - (entry.boostPressure / (maxBoost * 1.2)).coerceAtLeast(0.1))).toFloat()
                    Offset(x, y.coerceIn(padding, h - padding))
                }

                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val avgBoost = (sampleData[i].boostPressure + sampleData[i + 1].boostPressure) / 2
                        val lineColor = if (avgBoost > 1.2) colors.gaugeOrange else colors.gaugeGreen
                        drawLine(
                            color = lineColor,
                            start = p1,
                            end = p2,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                points.forEachIndexed { index, point ->
                    val entry = sampleData[index]
                    val dotColor = if (entry.boostPressure > 1.2) colors.gaugeOrange
                    else if (entry.boostPressure > 1.0) colors.gaugeYellow
                    else colors.gaugeGreen
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Alt", fontSize = 9.sp, color = colors.textDim)
                Text("Neu", fontSize = 9.sp, color = colors.textDim)
            }
        }
    }
}

@Composable
private fun OverboostStatus(
    turboData: TurboData,
    colors: AppColors
) {
    val statusColor = when {
        turboData.overboostActive -> colors.gaugeOrange
        turboData.underboostDetected -> colors.gaugeRed
        else -> colors.gaugeGreen
    }

    val statusText = when {
        turboData.overboostActive -> "Überboost aktiv"
        turboData.underboostDetected -> "Unterdruck erkannt"
        else -> "Normalbetrieb"
    }

    val infiniteTransition = rememberInfiniteTransition()
    @Suppress("UNUSED_VARIABLE")
    val _pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (turboData.overboostActive) 0.6f else 0f,
        targetValue = if (turboData.overboostActive) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_pulse"
    )

    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(300),
        label = "status_color"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = animatedColor.copy(alpha = if (turboData.overboostActive) 0.15f else 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    turboData.overboostActive -> Icons.Filled.ArrowUpward
                    turboData.underboostDetected -> Icons.Filled.ArrowDownward
                    else -> Icons.Filled.CheckCircle
                },
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
                Text(
                    text = "Boost: %.2f bar | Ziel: %.2f bar".format(
                        turboData.boostPressure,
                        turboData.boostTarget
                    ),
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}