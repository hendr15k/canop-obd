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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.model.CarProfile
import com.canopobd.data.model.TurboData
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

data class BoostLeakResult(
    val efficiency: Int = 100,
    val leakDetected: Boolean = false,
    val leakSeverity: LeakSeverity = LeakSeverity.NONE,
    val boostPressureDrop: Double = 0.0,
    val targetPressure: Double = 0.0,
    val actualPressure: Double = 0.0,
    val testDuration: Int = 0,
    val recommendations: List<String> = emptyList()
)

enum class LeakSeverity { NONE, MINOR, MODERATE, SEVERE }

@Suppress("UNUSED_PARAMETER")
@Composable
fun BoostLeakTestScreen(
    turboData: TurboData,
    carProfile: CarProfile,
    isTestRunning: Boolean,
    leakResult: BoostLeakResult,
    onDismiss: () -> Unit,
    onStartTest: () -> Unit = {},
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
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Boost-Leck-Test",
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
                    BoostLeakGauge(
                        efficiency = leakResult.efficiency,
                        leakDetected = leakResult.leakDetected,
                        isTestRunning = isTestRunning,
                        colors = colors
                    )
                }
                item {
                    BoostLeakPressureComparison(
                        target = leakResult.targetPressure,
                        actual = leakResult.actualPressure,
                        drop = leakResult.boostPressureDrop,
                        colors = colors
                    )
                }
                item {
                    BoostEfficiencyHistory(colors = colors)
                }
                item {
                    LeakDiagnosisResult(
                        result = leakResult,
                        colors = colors
                    )
                }
                item {
                    LeakRecommendations(
                        result = leakResult,
                        colors = colors
                    )
                }
            }
        },
        confirmButton = {
            if (!isTestRunning) {
                TextButton(onClick = onStartTest) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test starten", color = colors.accent)
                }
            }
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = colors.accent)
            }
        }
    )
}

@Composable
private fun BoostLeakGauge(
    efficiency: Int,
    leakDetected: Boolean,
    isTestRunning: Boolean,
    colors: AppColors
) {
    val animatedEfficiency by animateFloatAsState(
        targetValue = (efficiency / 100f).toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "efficiency_gauge"
    )

    val infiniteTransition = rememberInfiniteTransition()
    val sweepPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "test_sweep"
    )

    val statusColor = when {
        isTestRunning -> colors.gaugeCyan
        efficiency >= 90 -> colors.gaugeGreen
        efficiency >= 70 -> colors.gaugeYellow
        efficiency >= 50 -> colors.gaugeOrange
        else -> colors.gaugeRed
    }

    val animatedStatusColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(400),
        label = "status_color"
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
                text = "Boost-Effizienz",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
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

                    if (isTestRunning) {
                        val sweep = sweepPulse * 270f
                        drawArc(
                            color = animatedStatusColor,
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    } else {
                        val sweep = animatedEfficiency * 270f
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    colors.gaugeRed,
                                    colors.gaugeOrange,
                                    colors.gaugeYellow,
                                    colors.gaugeGreen
                                ),
                                center = center
                            ),
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isTestRunning) "..." else "$efficiency",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedStatusColor
                    )
                    Text(
                        text = "%",
                        fontSize = 14.sp,
                        color = animatedStatusColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusChip(
                    label = "Kein Leck",
                    color = colors.gaugeGreen,
                    selected = !leakDetected && !isTestRunning,
                    colors = colors
                )
                StatusChip(
                    label = "Leck erkannt",
                    color = colors.gaugeRed,
                    selected = leakDetected,
                    colors = colors
                )
                if (isTestRunning) {
                    StatusChip(
                        label = "Prüfe...",
                        color = colors.gaugeCyan,
                        selected = true,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    color: Color,
    selected: Boolean,
    colors: AppColors
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) color.copy(alpha = 0.2f) else colors.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) color else colors.textDim,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BoostLeakPressureComparison(
    target: Double,
    actual: Double,
    drop: Double,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Druckvergleich",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PressureColumn(
                    label = "Soll",
                    value = target,
                    color = colors.accent,
                    colors = colors
                )
                PressureColumn(
                    label = "Ist",
                    value = actual,
                    color = when {
                        drop > 0.2 -> colors.gaugeRed
                        drop > 0.1 -> colors.gaugeOrange
                        drop > 0.05 -> colors.gaugeYellow
                        else -> colors.gaugeGreen
                    },
                    colors = colors
                )
                PressureColumn(
                    label = "Verlust",
                    value = drop,
                    color = when {
                        drop > 0.2 -> colors.gaugeRed
                        drop > 0.1 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    },
                    colors = colors,
                    isDrop = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val maxBar = 2.0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surfaceVariant)
            ) {
                val targetNorm = (target / maxBar).toFloat().coerceIn(0f, 1f)
                val actualNorm = (actual / maxBar).toFloat().coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(targetNorm)
                        .background(colors.accent.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(actualNorm)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colors.gaugeGreen.copy(alpha = 0.6f),
                                    when {
                                        drop > 0.1 -> colors.gaugeOrange
                                        else -> colors.gaugeGreen
                                    }
                                )
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.0 bar", fontSize = 9.sp, color = colors.textDim)
                Text("1.0 bar", fontSize = 9.sp, color = colors.textDim)
                Text("2.0 bar", fontSize = 9.sp, color = colors.textDim)
            }
        }
    }
}

@Composable
private fun PressureColumn(
    label: String,
    value: Double,
    color: Color,
    colors: AppColors,
    isDrop: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isDrop) "%.3f".format(value) else "%.2f".format(value),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.textSecondary
        )
        Text(
            text = "bar",
            fontSize = 9.sp,
            color = colors.textDim
        )
    }
}

@Composable
private fun BoostEfficiencyHistory(colors: AppColors) {
    val sampleData = remember {
        listOf(95, 92, 88, 91, 85, 78, 82, 76, 80, 72)
    }

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
                    text = "Historische Boost-Effizienz",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Letzte 10 Tests",
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

                val thresholds = listOf(50 to colors.gaugeRed, 70 to colors.gaugeOrange, 90 to colors.gaugeYellow)
                thresholds.forEach { (threshold, color) ->
                    val y = padding + graphH * (1f - threshold / 100f)
                    drawLine(
                        color = color.copy(alpha = 0.3f),
                        start = Offset(padding, y),
                        end = Offset(w - padding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val points = sampleData.mapIndexed { index, value ->
                    val x = padding + (graphW / (sampleData.size - 1).coerceAtLeast(1)) * index
                    val y = padding + graphH * (1f - value / 100f)
                    Offset(x, y)
                }

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = colors.accent,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 2.dp.toPx()
                    )
                }

                points.forEachIndexed { index, point ->
                    val value = sampleData[index]
                    val dotColor = when {
                        value >= 90 -> colors.gaugeGreen
                        value >= 70 -> colors.gaugeYellow
                        value >= 50 -> colors.gaugeOrange
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
                Text("Älter", fontSize = 9.sp, color = colors.textDim)
                Text("Neuer", fontSize = 9.sp, color = colors.textDim)
            }
        }
    }
}

@Composable
private fun LeakDiagnosisResult(
    result: BoostLeakResult,
    colors: AppColors
) {
    val (statusColor, statusText, statusIcon) = when (result.leakSeverity) {
        LeakSeverity.NONE -> Triple(colors.gaugeGreen, "Kein Leck erkannt", Icons.Filled.CheckCircle)
        LeakSeverity.MINOR -> Triple(colors.gaugeYellow, "Geringfügiges Leck", Icons.Filled.Warning)
        LeakSeverity.MODERATE -> Triple(colors.gaugeOrange, "Mittleres Leck erkannt", Icons.Filled.Error)
        LeakSeverity.SEVERE -> Triple(colors.gaugeRed, "Ernsthaftes Leck!", Icons.Filled.Dangerous)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = statusColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                if (result.testDuration > 0) {
                    Text(
                        text = "Testdauer: ${result.testDuration}s",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LeakRecommendations(
    result: BoostLeakResult,
    colors: AppColors
) {
    if (result.recommendations.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Build,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Empfehlungen",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            result.recommendations.forEach { rec ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = colors.accent,
                        modifier = Modifier.width(12.dp)
                    )
                    Text(
                        text = rec,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
