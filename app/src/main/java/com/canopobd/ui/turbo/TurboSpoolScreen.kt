package com.canopobd.ui.turbo

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.model.TurboData
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

data class SpoolData(
    val spoolTimeMs: Long = 0,
    val turboRpm: Double = 0.0,
    val boostPressure: Double = 0.0,
    val turboHealthScore: Int = 100,
    val isSpooling: Boolean = false,
    val spoolProgress: Float = 0f,
    val spoolHistory: List<Long> = emptyList()
)

@Composable
fun TurboSpoolScreen(
    turboData: TurboData,
    spoolData: SpoolData,
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
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Turbo-Spool-Anzeige",
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
                    TurboSpoolAnimation(
                        turboData = turboData,
                        spoolData = spoolData,
                        colors = colors
                    )
                }
                item {
                    SpoolTimeResult(
                        spoolTime = spoolData.spoolTimeMs,
                        isSpooling = spoolData.isSpooling,
                        colors = colors
                    )
                }
                item {
                    SpoolHistoryChart(
                        history = spoolData.spoolHistory,
                        colors = colors
                    )
                }
                item {
                    TurboHealthScore(
                        score = spoolData.turboHealthScore,
                        colors = colors
                    )
                }
                item {
                    TurboRpmGauge(
                        rpm = turboData.turboRpm,
                        isSpooling = spoolData.isSpooling,
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
private fun TurboSpoolAnimation(
    turboData: TurboData,
    spoolData: SpoolData,
    colors: AppColors
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "turbo_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val animatedRpm = (turboData.turboRpm / 200000.0).toFloat().coerceIn(0f, 1f)
    val boostColor = when {
        turboData.overboostActive -> colors.gaugeOrange
        turboData.boostPressure > 0.8 -> colors.gaugeGreen
        turboData.boostPressure > 0.3 -> colors.gaugeCyan
        turboData.boostPressure > 0.1 -> colors.accent
        else -> colors.textSecondary
    }

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
                text = "Turbo-Spool-Status",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val baseRadius = size.minDimension / 2 * 0.85f

                    if (spoolData.isSpooling) {
                        for (i in 0..2) {
                            val ringRadius = baseRadius * (0.6f + i * 0.15f) * pulseScale
                            drawCircle(
                                color = boostColor.copy(alpha = 0.15f - i * 0.04f),
                                radius = ringRadius,
                                center = center,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }

                    val rotationRad = Math.toRadians(rotation.toDouble())
                    val bladeCount = 12
                    for (i in 0 until bladeCount) {
                        val angle = rotationRad + (i * 2 * Math.PI / bladeCount)
                        val innerR = baseRadius * 0.2f
                        val outerR = baseRadius * (0.4f + animatedRpm * 0.4f)
                        val startX = center.x + (innerR * kotlin.math.cos(angle)).toFloat()
                        val startY = center.y + (innerR * kotlin.math.sin(angle)).toFloat()
                        val endX = center.x + (outerR * kotlin.math.cos(angle)).toFloat()
                        val endY = center.y + (outerR * kotlin.math.sin(angle)).toFloat()
                        drawLine(
                            color = boostColor.copy(alpha = 0.6f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    drawCircle(
                        color = colors.surfaceVariant,
                        radius = baseRadius * 0.15f,
                        center = center
                    )
                    drawCircle(
                        color = boostColor,
                        radius = baseRadius * 0.15f,
                        center = center
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%,d".format(turboData.turboRpm.toInt()),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = boostColor
                    )
                    Text(
                        text = "RPM",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    if (spoolData.isSpooling) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SPOOLING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpoolMetric(
                    label = "Druck",
                    value = "%.2f".format(turboData.boostPressure),
                    unit = "bar",
                    color = boostColor,
                    colors = colors
                )
                SpoolMetric(
                    label = "Zieldruck",
                    value = "%.2f".format(turboData.boostTarget),
                    unit = "bar",
                    color = colors.accent,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun SpoolMetric(
    label: String,
    value: String,
    unit: String,
    color: Color,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            fontSize = 10.sp,
            color = colors.textDim
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun SpoolTimeResult(
    spoolTime: Long,
    isSpooling: Boolean,
    colors: AppColors
) {
    val spoolTimeFormatted = if (spoolTime > 0) "%d ms".format(spoolTime) else "—"
    val statusColor = when {
        spoolTime in 1..800 -> colors.gaugeGreen
        spoolTime in 801..1500 -> colors.gaugeYellow
        spoolTime in 1501..2500 -> colors.gaugeOrange
        spoolTime > 2500 -> colors.gaugeRed
        else -> colors.textSecondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Spool-Zeit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zeit bis Volllast",
                    fontSize = 10.sp,
                    color = colors.textDim
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSpooling) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colors.gaugeCyan.copy(alpha = dotAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = spoolTimeFormatted,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpooling) colors.gaugeCyan else statusColor
                    )
                    Text(
                        text = if (isSpooling) "Läuft..." else getSpoolTimeDescription(spoolTime),
                        fontSize = 10.sp,
                        color = if (isSpooling) colors.gaugeCyan else statusColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun getSpoolTimeDescription(ms: Long): String = when {
    ms in 1..800 -> "Ausgezeichnet"
    ms in 801..1500 -> "Gut"
    ms in 1501..2500 -> "Befriedigend"
    ms > 2500 -> "Langsam"
    else -> "—"
}

@Composable
private fun SpoolHistoryChart(colors: AppColors, history: List<Long>) {
    val sampleData = remember {
        if (history.isEmpty()) listOf(1200L, 1150L, 1080L, 1300L, 980L, 1100L, 950L)
        else history
    }

    val avgTime = if (sampleData.isNotEmpty()) sampleData.average().toLong() else 0L

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
                    text = "Historische Spool-Zeiten",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Ø %d ms".format(avgTime),
                    fontSize = 11.sp,
                    color = colors.accent
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val w = size.width
                val h = size.height
                val padding = 4.dp.toPx()
                val graphW = w - padding * 2
                val graphH = h - padding * 2

                val minTime = (sampleData.minOrNull() ?: 0L) * 0.8f
                val maxTime = (sampleData.maxOrNull() ?: 3000L) * 1.1f
                val timeRange = (maxTime - minTime).coerceAtLeast(1f)

                listOf(0f, 1000f, 2000f, 3000f).forEach { threshold ->
                    val y = padding + graphH * (1f - (threshold - minTime) / timeRange)
                    if (y in padding..(h - padding)) {
                        drawLine(
                            color = colors.surfaceVariant.copy(alpha = 0.5f),
                            start = Offset(padding, y),
                            end = Offset(w - padding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                val points = sampleData.mapIndexed { index, time ->
                    val x = padding + (graphW / (sampleData.size - 1).coerceAtLeast(1)) * index
                    val y = padding + graphH * (1f - (time - minTime) / timeRange)
                    Offset(x, y.coerceIn(padding, h - padding))
                }

                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = colors.gaugeCyan,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                points.forEachIndexed { index, point ->
                    val time = sampleData[index]
                    val dotColor = when {
                        time < 1000 -> colors.gaugeGreen
                        time < 1500 -> colors.gaugeYellow
                        time < 2500 -> colors.gaugeOrange
                        else -> colors.gaugeRed
                    }
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
                Text("<1s", fontSize = 9.sp, color = colors.gaugeGreen)
                Text("1-1.5s", fontSize = 9.sp, color = colors.gaugeYellow)
                Text("1.5-2.5s", fontSize = 9.sp, color = colors.gaugeOrange)
                Text(">2.5s", fontSize = 9.sp, color = colors.gaugeRed)
            }
        }
    }
}

@Composable
private fun TurboHealthScore(score: Int, colors: AppColors) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(500),
        label = "health_score"
    )

    val scoreColor = when {
        animatedScore >= 90 -> colors.gaugeGreen
        animatedScore >= 75 -> colors.gaugeYellow
        animatedScore >= 50 -> colors.gaugeOrange
        else -> colors.gaugeRed
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = scoreColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Turbo-Gesundheit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$animatedScore",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Text(
                    text = "/100",
                    fontSize = 12.sp,
                    color = scoreColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TurboRpmGauge(
    rpm: Double,
    isSpooling: Boolean,
    colors: AppColors
) {
    val maxRpm = 200000.0
    val normalized = (rpm / maxRpm).coerceIn(0.0, 1.0).toFloat()
    val rpmColor = when {
        rpm > 180000 -> colors.gaugeRed
        rpm > 150000 -> colors.gaugeOrange
        rpm > 80000 -> colors.gaugeGreen
        rpm > 30000 -> colors.gaugeCyan
        else -> colors.textSecondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Turbolader-Drehzahl",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%,d RPM".format(rpm.toInt()),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = rpmColor
                )
                Text(
                    text = "%.0f%%".format(normalized * 100),
                    fontSize = 12.sp,
                    color = colors.textSecondary
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
                val animatedProgress by animateFloatAsState(
                    targetValue = normalized,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "rpm_progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(rpmColor)
                )
            }
        }
    }
}