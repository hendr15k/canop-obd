package com.canopobd.ui.turbo

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.model.AstraJ14TurboCalibration
import com.canopobd.data.model.TurboCoolDownState
import com.canopobd.data.model.TurboData
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors
import kotlin.math.abs

data class ExtendedTurboData(
    val boostActualBar: Double = 0.0,
    val boostTargetBar: Double = 0.0,
    val wastegatePosition: Double = 0.0,
    val wastegateDutyCycle: Double = 0.0,
    val turboRpm: Double = 0.0,
    val chargeAirTemp: Double = 0.0,
    val intakeAirTemp: Double = 0.0,
    val egtCurrent: Double = 0.0,
    val egtPeak: Double = 0.0,
    val egtHistory: List<Double> = emptyList(),
    val boostLeakEfficiency: Int = 100,
    val boostLeakDetected: Boolean = false,
    val isOverboost: Boolean = false,
    val overboostRemaining: Int = 0,
    val engineLoad: Double = 0.0,
    val engineRpm: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ExtendedTurboMonitorDialog(
    extendedData: ExtendedTurboData,
    coolDownState: TurboCoolDownState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val calibration = remember { AstraJ14TurboCalibration.INSTANCE }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Air,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Erweiterte Turbo-Überwachung",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (coolDownState.isActive) {
                    item {
                        CoolDownBanner(coolDownState, colors)
                    }
                }

                item {
                    BoostAnalysisGauge(
                        actualBar = extendedData.boostActualBar,
                        targetBar = extendedData.boostTargetBar,
                        calibration = calibration,
                        isOverboost = extendedData.isOverboost,
                        overboostRemaining = extendedData.overboostRemaining,
                        colors = colors
                    )
                }

                item {
                    WastegateHealthCard(
                        position = extendedData.wastegatePosition,
                        dutyCycle = extendedData.wastegateDutyCycle,
                        calibration = calibration,
                        boostActualBar = extendedData.boostActualBar,
                        boostTargetBar = extendedData.boostTargetBar,
                        colors = colors
                    )
                }

                item {
                    TurboRpmCard(
                        rpm = extendedData.turboRpm,
                        calibration = calibration,
                        colors = colors
                    )
                }

                item {
                    ChargeAirTempCard(
                        chargeAirTemp = extendedData.chargeAirTemp,
                        intakeAirTemp = extendedData.intakeAirTemp,
                        calibration = calibration,
                        engineLoad = extendedData.engineLoad,
                        colors = colors
                    )
                }

                item {
                    EGTMonitoringCard(
                        currentEGT = extendedData.egtCurrent,
                        peakEGT = extendedData.egtPeak,
                        egtHistory = extendedData.egtHistory,
                        calibration = calibration,
                        colors = colors
                    )
                }

                item {
                    BoostLeakIndicatorCard(
                        efficiency = extendedData.boostLeakEfficiency,
                        leakDetected = extendedData.boostLeakDetected,
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
private fun CoolDownBanner(state: TurboCoolDownState, colors: AppColors) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cooldown_pulse"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.gaugeOrange.copy(alpha = 0.15f * alpha)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Whatshot,
                contentDescription = null,
                tint = colors.gaugeOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Turbo-Cooldown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gaugeOrange
                )
                Text(
                    text = state.statusMessage,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(state.progress.coerceIn(0f, 1f))
                            .background(colors.gaugeOrange)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.secondsRemaining}s verbleibend",
                    fontSize = 10.sp,
                    color = colors.gaugeOrange
                )
            }
        }
    }
}

@Composable
private fun BoostAnalysisGauge(
    actualBar: Double,
    targetBar: Double,
    calibration: AstraJ14TurboCalibration,
    isOverboost: Boolean,
    overboostRemaining: Int,
    colors: AppColors
) {
    val maxGaugeBar = calibration.overboostMaxBar
    val normalBar = calibration.normalBoostTargetBar
    val overboostBar = calibration.overboostBar
    val deviation = actualBar - targetBar
    val deviationPercent = if (targetBar > 0) (deviation / targetBar * 100.0) else 0.0

    @Suppress("UNUSED_VARIABLE")
    val _animatedBoost by animateFloatAsState(
        targetValue = actualBar.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "boost_actual"
    )

    @Suppress("UNUSED_VARIABLE")
    val _animatedTarget by animateFloatAsState(
        targetValue = targetBar.toFloat(),
        animationSpec = tween(400),
        label = "boost_target"
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            actualBar >= calibration.overboostMaxBar -> colors.gaugeRed
            actualBar >= overboostBar -> colors.gaugeOrange
            actualBar >= normalBar - 0.1 -> colors.gaugeGreen
            actualBar >= normalBar * 0.5 -> colors.gaugeYellow
            actualBar > 0.0 -> colors.gaugeCyan
            else -> colors.textSecondary
        },
        animationSpec = tween(300),
        label = "boost_status_color"
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Boost-Analyse",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                if (isOverboost) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeOrange.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "OVERBOOST ${overboostRemaining}s",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2, radius * 2)

                    drawArc(
                        color = colors.surfaceVariant,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val normalPos = (normalBar / maxGaugeBar * 260f).toFloat()
                    val overPos = (overboostBar / maxGaugeBar * 260f).toFloat()

                    drawArc(
                        color = colors.gaugeGreen.copy(alpha = 0.25f),
                        startAngle = 140f,
                        sweepAngle = normalPos,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = colors.gaugeYellow.copy(alpha = 0.25f),
                        startAngle = 140f + normalPos,
                        sweepAngle = overPos - normalPos,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = colors.gaugeRed.copy(alpha = 0.25f),
                        startAngle = 140f + overPos,
                        sweepAngle = 260f - overPos,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round)
                    )

                    val actualSweep = ((actualBar / maxGaugeBar).toFloat().coerceIn(0f, 1f)) * 260f
                    drawArc(
                        color = statusColor,
                        startAngle = 140f,
                        sweepAngle = actualSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val targetSweep = ((targetBar / maxGaugeBar).toFloat().coerceIn(0f, 1f)) * 260f
                    val targetAngle = 140f + targetSweep
                    val innerR = radius - strokeWidth / 2 - 4.dp.toPx()
                    val outerR = radius - strokeWidth / 2 + 4.dp.toPx()
                    val targetRad = Math.toRadians(targetAngle.toDouble())
                    drawLine(
                        color = colors.accent,
                        start = Offset(
                            center.x + (innerR * kotlin.math.cos(targetRad)).toFloat(),
                            center.y + (innerR * kotlin.math.sin(targetRad)).toFloat()
                        ),
                        end = Offset(
                            center.x + (outerR * kotlin.math.cos(targetRad)).toFloat(),
                            center.y + (outerR * kotlin.math.sin(targetRad)).toFloat()
                        ),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.2f".format(actualBar),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "bar",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BoostMetricColumn("Ist", "%.2f bar".format(actualBar), statusColor, colors)
                BoostMetricColumn("Soll", "%.2f bar".format(targetBar), colors.accent, colors)
                BoostMetricColumn(
                    "Diff",
                    "%+.3f bar".format(deviation),
                    if (abs(deviation) > 0.15) colors.gaugeOrange else colors.gaugeGreen,
                    colors
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Abweichung: %+.1f%%".format(deviationPercent),
                    fontSize = 10.sp,
                    color = when {
                        abs(deviationPercent) > 20 -> colors.gaugeRed
                        abs(deviationPercent) > 10 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    }
                )
                Text(
                    text = when {
                        abs(deviation) <= 0.05 -> "Optimal"
                        abs(deviation) <= 0.15 -> "Akzeptabel"
                        abs(deviation) <= 0.3 -> "Abweichung"
                        else -> "Kritisch"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        abs(deviation) <= 0.05 -> colors.gaugeGreen
                        abs(deviation) <= 0.15 -> colors.gaugeYellow
                        abs(deviation) <= 0.3 -> colors.gaugeOrange
                        else -> colors.gaugeRed
                    }
                )
            }
        }
    }
}

@Composable
private fun BoostMetricColumn(
    label: String,
    value: String,
    color: Color,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = colors.textDim)
    }
}

@Composable
private fun WastegateHealthCard(
    position: Double,
    dutyCycle: Double,
    calibration: AstraJ14TurboCalibration,
    boostActualBar: Double,
    boostTargetBar: Double,
    colors: AppColors
) {
    val healthyRange = calibration.wastegateHealthyDutyRange
    val isHealthyDuty = dutyCycle.toFloat() in healthyRange
    val isStuckOpen = dutyCycle >= calibration.wastegateStuckOpenDuty
    val isStuckClosed = dutyCycle <= calibration.wastegateStuckClosedDuty

    val isUnderboost = boostActualBar < boostTargetBar * 0.7 && boostTargetBar > 0.3
    val isOverboost = boostActualBar > calibration.overboostBar

    val healthPercent = remember(position, dutyCycle, isUnderboost, isOverboost) {
        var score = 100
        if (isStuckOpen) score -= 40
        if (isStuckClosed) score -= 40
        if (!isHealthyDuty) score -= 20
        if (isUnderboost) score -= 25
        if (isOverboost) score -= 30
        score.coerceIn(0, 100)
    }

    val animatedHealth by animateFloatAsState(
        targetValue = healthPercent / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wg_health"
    )

    val healthColor by animateColorAsState(
        targetValue = when {
            healthPercent >= 80 -> colors.gaugeGreen
            healthPercent >= 50 -> colors.gaugeOrange
            else -> colors.gaugeRed
        },
        animationSpec = tween(300),
        label = "wg_health_color"
    )

    val animatedPosition by animateFloatAsState(
        targetValue = (position / 100f).toFloat().coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wg_position"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                        Icons.Filled.Tune,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wastegate-Position",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(healthColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (healthPercent >= 80) "OK" else if (healthPercent >= 50) "WARNUNG" else "KRITISCH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = healthColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f%%".format(position),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(text = "Position", fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f%%".format(dutyCycle),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHealthyDuty) colors.gaugeGreen else colors.gaugeOrange
                    )
                    Text(text = "Duty Cycle", fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$healthPercent",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = healthColor
                    )
                    Text(text = "Gesundheit", fontSize = 10.sp, color = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Position",
                fontSize = 10.sp,
                color = colors.textDim
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(colors.surfaceVariant)
            ) {
                val barColor = when {
                    position > 90 -> colors.gaugeOrange
                    position < 10 -> colors.gaugeYellow
                    else -> colors.gaugeCyan
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedPosition)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(barColor.copy(alpha = 0.5f), barColor)
                            )
                        )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0% offen", fontSize = 8.sp, color = colors.textDim)
                Text("50%", fontSize = 8.sp, color = colors.textDim)
                Text("100% geschl.", fontSize = 8.sp, color = colors.textDim)
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        .fillMaxWidth(animatedHealth)
                        .background(healthColor)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duty Range: %.0f–%.0f%%".format(healthyRange.start, healthyRange.endInclusive),
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                Text(
                    text = "Gesundheit: $healthPercent/100",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = healthColor
                )
            }

            if (isStuckOpen || isStuckClosed || isUnderboost || isOverboost) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = healthColor.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (isStuckOpen) {
                            WarningLine("Wastegate klebt offen — Unterladung möglich", colors.gaugeRed, colors)
                        }
                        if (isStuckClosed) {
                            WarningLine("Wastegate klebt geschlossen — Überladung möglich!", colors.gaugeRed, colors)
                        }
                        if (isUnderboost && !isStuckOpen) {
                            WarningLine("Unterladung erkannt — Leck oder Verschleiß", colors.gaugeOrange, colors)
                        }
                        if (isOverboost && !isStuckClosed) {
                            WarningLine("Überladung — Wastegate-Aktuator prüfen", colors.gaugeOrange, colors)
                        }
                    }
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun WarningLine(text: String, color: Color, colors: AppColors) {
    Row(
        modifier = Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 10.sp, color = color)
    }
}

@Composable
private fun TurboRpmCard(
    rpm: Double,
    calibration: AstraJ14TurboCalibration,
    colors: AppColors
) {
    val maxRpm = calibration.turboSpeedMaxRpm.toDouble()
    val normalMax = calibration.turboSpeedNormalRangeRpm.endInclusive.toDouble()
    val normalMin = calibration.turboSpeedNormalRangeRpm.start.toDouble()
    val animatedRpm by animateFloatAsState(
        targetValue = (rpm / maxRpm).toFloat().coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "turbo_rpm"
    )

    val rpmColor by animateColorAsState(
        targetValue = when {
            rpm >= maxRpm * 0.95 -> colors.gaugeRed
            rpm >= normalMax -> colors.gaugeOrange
            rpm >= normalMin -> colors.gaugeGreen
            rpm >= calibration.turboSpeedIdleRpm.toDouble() -> colors.gaugeCyan
            else -> colors.textSecondary
        },
        animationSpec = tween(300),
        label = "turbo_rpm_color"
    )

    val isMaxWarning = rpm >= maxRpm * 0.95

    val infiniteTransition = rememberInfiniteTransition()
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rpm_warning_pulse"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isMaxWarning) colors.gaugeRed.copy(alpha = 0.08f * warningAlpha) else colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Speed,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Turbo-Drehzahl",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                if (isMaxWarning) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeRed.copy(alpha = 0.2f * warningAlpha)
                    ) {
                        Text(
                            text = "MAX WARNUNG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "%,d".format(rpm.toInt()),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = rpmColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RPM",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                val w = size.width
                val h = size.height
                val barH = 10.dp.toPx()
                val yCenter = h / 2

                drawRoundRect(
                    color = colors.surfaceVariant,
                    topLeft = Offset(0f, yCenter - barH / 2),
                    size = Size(w, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                )

                val normalEnd = (normalMax / maxRpm).toFloat()
                val warnEnd = 0.95f

                drawRoundRect(
                    color = colors.gaugeGreen.copy(alpha = 0.3f),
                    topLeft = Offset(0f, yCenter - barH / 2),
                    size = Size(w * (normalEnd / 1f), barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                )
                drawRoundRect(
                    color = colors.gaugeOrange.copy(alpha = 0.3f),
                    topLeft = Offset(w * normalEnd, yCenter - barH / 2),
                    size = Size(w * (warnEnd - normalEnd), barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                )
                drawRoundRect(
                    color = colors.gaugeRed.copy(alpha = 0.3f),
                    topLeft = Offset(w * warnEnd, yCenter - barH / 2),
                    size = Size(w * (1f - warnEnd), barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                )

                drawRoundRect(
                    color = rpmColor,
                    topLeft = Offset(0f, yCenter - barH / 2),
                    size = Size(w * animatedRpm, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
                )

                val indicatorX = w * animatedRpm
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(indicatorX.coerceIn(0f, w), yCenter)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Idle ~%,.0f".format(calibration.turboSpeedIdleRpm),
                    fontSize = 8.sp,
                    color = colors.textDim
                )
                Text(
                    text = "Normal %.0f–%.0f".format(normalMin, normalMax),
                    fontSize = 8.sp,
                    color = colors.gaugeGreen
                )
                Text(
                    text = "Max %.0f".format(maxRpm),
                    fontSize = 8.sp,
                    color = colors.gaugeRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val rpmPercent = if (maxRpm > 0) (rpm / maxRpm * 100.0) else 0.0
            val statusText = when {
                rpm <= 0 -> "Motor aus"
                rpm < calibration.turboSpeedIdleRpm -> "Unter Idle"
                rpm <= normalMax -> "Normalbereich"
                rpm <= maxRpm * 0.95 -> "Hoher Bereich"
                else -> "Über Drehzahl!"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = rpmColor
                )
                Text(
                    text = "%.1f%% des Maximums".format(rpmPercent),
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun ChargeAirTempCard(
    chargeAirTemp: Double,
    intakeAirTemp: Double,
    calibration: AstraJ14TurboCalibration,
    engineLoad: Double,
    colors: AppColors
) {
    val maxTemp = calibration.maxChargeAirTempC.toDouble()
    val normalMax = calibration.chargeAirTempNormalMax.toDouble()
    val wotMax = calibration.chargeAirTempWotMax.toDouble()

    val tempDrop = if (intakeAirTemp > 0 && chargeAirTemp > 0 && chargeAirTemp > intakeAirTemp) {
        chargeAirTemp - intakeAirTemp
    } else if (intakeAirTemp > 0 && chargeAirTemp > 0) {
        chargeAirTemp - intakeAirTemp
    } else 0.0

    val intercoolerEfficiency = when {
        intakeAirTemp <= 0 || chargeAirTemp <= 0 -> 0.0
        chargeAirTemp <= intakeAirTemp -> {
            val rise = chargeAirTemp - intakeAirTemp
            ((1.0 - (rise / 30.0)) * 100.0).coerceIn(0.0, 100.0)
        }
        else -> 100.0
    }

    val targetEfficiency = calibration.intercoolerEfficiencyTarget
    val isEfficient = intercoolerEfficiency >= targetEfficiency

    @Suppress("UNUSED_VARIABLE")
    val _animatedTemp by animateFloatAsState(
        targetValue = chargeAirTemp.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "charge_temp"
    )

    val tempColor by animateColorAsState(
        targetValue = when {
            chargeAirTemp >= maxTemp -> colors.gaugeRed
            chargeAirTemp >= wotMax -> colors.gaugeOrange
            chargeAirTemp >= normalMax -> colors.gaugeYellow
            chargeAirTemp > 0 -> colors.gaugeGreen
            else -> colors.textSecondary
        },
        animationSpec = tween(300),
        label = "charge_temp_color"
    )

    val efficiencyColor = when {
        intercoolerEfficiency >= 80 -> colors.gaugeGreen
        intercoolerEfficiency >= 60 -> colors.gaugeYellow
        intercoolerEfficiency >= 40 -> colors.gaugeOrange
        else -> colors.gaugeRed
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                        Icons.Filled.Thermostat,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ladelufttemperatur",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                if (!isEfficient && chargeAirTemp > 10) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeOrange.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "EFFIZIENZ NIEDRIG",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeOrange,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (chargeAirTemp > 0) "%.0f°C".format(chargeAirTemp) else "—",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = tempColor
                    )
                    Text(text = "Ladeluft", fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (intakeAirTemp > 0) "%.0f°C".format(intakeAirTemp) else "—",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary
                    )
                    Text(text = "Ansaugluft", fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (tempDrop != 0.0) "%+.0f°C".format(tempDrop) else "—",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tempDrop > 0) colors.gaugeGreen else colors.gaugeOrange
                    )
                    Text(text = "Differenz", fontSize = 10.sp, color = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Temperatur", fontSize = 10.sp, color = colors.textDim)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(colors.surfaceVariant)
            ) {
                val normalized = if (maxTemp > 0) {
                    (chargeAirTemp / (maxTemp * 1.2)).toFloat().coerceIn(0f, 1f)
                } else 0f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(normalized)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(colors.gaugeGreen, colors.gaugeYellow, colors.gaugeOrange, colors.gaugeRed)
                            )
                        )
                )
                val normalMarker = (normalMax / (maxTemp * 1.2)).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(normalMarker)
                        .background(colors.textDim.copy(alpha = 0.55f))
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0°C", fontSize = 8.sp, color = colors.textDim)
                Text("Normal max %.0f°C".format(normalMax), fontSize = 8.sp, color = colors.gaugeGreen)
                Text("WOT max %.0f°C".format(wotMax), fontSize = 8.sp, color = colors.gaugeOrange)
                Text("%.0f°C".format(maxTemp), fontSize = 8.sp, color = colors.gaugeRed)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ladeluftkühler-Effizienz",
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                                .fillMaxWidth((intercoolerEfficiency / 100.0).toFloat().coerceIn(0f, 1f))
                                .background(efficiencyColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%.0f%%".format(intercoolerEfficiency),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = efficiencyColor
                    )
                    Text(
                        text = "Ziel: %.0f%%".format(targetEfficiency),
                        fontSize = 9.sp,
                        color = colors.textDim
                    )
                }
            }
        }
    }
}

@Composable
private fun EGTMonitoringCard(
    currentEGT: Double,
    peakEGT: Double,
    egtHistory: List<Double>,
    calibration: AstraJ14TurboCalibration,
    colors: AppColors
) {
    val normalMax = calibration.egtNormalMax.toDouble()
    val wotMax = calibration.egtWotMax.toDouble()
    val critical = calibration.egtCritical.toDouble()

    val animatedEGT by animateFloatAsState(
        targetValue = currentEGT.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "egt_value"
    )

    val egtColor by animateColorAsState(
        targetValue = when {
            currentEGT >= critical -> colors.gaugeRed
            currentEGT >= wotMax -> colors.gaugeOrange
            currentEGT >= normalMax -> colors.gaugeYellow
            currentEGT >= 400 -> colors.gaugeGreen
            else -> colors.textSecondary
        },
        animationSpec = tween(300),
        label = "egt_color"
    )

    val isWarning = currentEGT >= wotMax
    val isCritical = currentEGT >= critical

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isCritical) colors.gaugeRed.copy(alpha = 0.06f) else colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EGT (Auslasstemperatur)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                if (isCritical) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeRed.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "KRITISCH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isWarning) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeOrange.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "WARNUNG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2, radius * 2)

                    drawArc(
                        color = colors.surfaceVariant,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val normalSweep = (normalMax / 1000.0 * 260f).toFloat()
                    drawArc(
                        color = colors.gaugeGreen.copy(alpha = 0.3f),
                        startAngle = 140f,
                        sweepAngle = normalSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round)
                    )

                    val wotSweep = (wotMax / 1000.0 * 260f).toFloat()
                    drawArc(
                        color = colors.gaugeOrange.copy(alpha = 0.3f),
                        startAngle = 140f + normalSweep,
                        sweepAngle = wotSweep - normalSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round)
                    )

                    val critSweep = (critical / 1000.0 * 260f).toFloat()
                    drawArc(
                        color = colors.gaugeRed.copy(alpha = 0.3f),
                        startAngle = 140f + wotSweep,
                        sweepAngle = critSweep - wotSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.3f, cap = StrokeCap.Round)
                    )

                    val actualSweep = (currentEGT / 1000.0 * 260f).toFloat().coerceIn(0f, 260f)
                    drawArc(
                        color = egtColor,
                        startAngle = 140f,
                        sweepAngle = actualSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.0f".format(animatedEGT),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = egtColor
                    )
                    Text(text = "°C", fontSize = 11.sp, color = colors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EGTStatItem("Aktuell", "%.0f°C".format(currentEGT), egtColor, colors)
                EGTStatItem("Peak", "%.0f°C".format(peakEGT), colors.gaugeOrange, colors)
                EGTStatItem(
                    "Normal",
                    "<%.0f°C".format(normalMax),
                    colors.gaugeGreen,
                    colors
                )
                EGTStatItem(
                    "Kritisch",
                    ">%.0f°C".format(critical),
                    colors.gaugeRed,
                    colors
                )
            }

            if (egtHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "EGT-Verlauf", fontSize = 10.sp, color = colors.textDim)
                Spacer(modifier = Modifier.height(6.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val padding = 2.dp.toPx()
                    val graphW = w - padding * 2
                    val graphH = h - padding * 2
                    val data = egtHistory.takeLast(30)

                    drawRoundRect(
                        color = colors.surfaceVariant,
                        topLeft = Offset(padding, padding),
                        size = Size(graphW, graphH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )

                    val warnY = padding + graphH * (1 - (wotMax / 1000.0)).toFloat()
                    drawLine(
                        color = colors.gaugeOrange.copy(alpha = 0.4f),
                        start = Offset(padding, warnY),
                        end = Offset(w - padding, warnY),
                        strokeWidth = 1.dp.toPx()
                    )

                    val points = data.mapIndexed { index, egt ->
                        val x = padding + (graphW / (data.size - 1).coerceAtLeast(1)) * index
                        val y = (padding + graphH * (1 - (egt / 1000.0).toFloat())).coerceIn(padding, h - padding)
                        Offset(x, y)
                    }

                    if (points.size > 1) {
                        for (i in 0 until points.size - 1) {
                            val avgEgt = (data[i] + data[i + 1]) / 2
                            val lineColor = when {
                                avgEgt >= critical -> colors.gaugeRed
                                avgEgt >= wotMax -> colors.gaugeOrange
                                else -> colors.gaugeGreen
                            }
                            drawLine(
                                color = lineColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    if (points.isNotEmpty()) {
                        drawCircle(
                            color = egtColor,
                            radius = 3.dp.toPx(),
                            center = points.last()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EGTStatItem(
    label: String,
    value: String,
    color: Color,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 9.sp, color = colors.textDim)
    }
}

@Composable
private fun BoostLeakIndicatorCard(
    efficiency: Int,
    leakDetected: Boolean,
    colors: AppColors
) {
    val animatedEfficiency by animateFloatAsState(
        targetValue = efficiency / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "leak_efficiency"
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            efficiency >= 95 -> colors.gaugeGreen
            efficiency >= 80 -> colors.gaugeYellow
            efficiency >= 60 -> colors.gaugeOrange
            else -> colors.gaugeRed
        },
        animationSpec = tween(300),
        label = "leak_status_color"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (leakDetected) colors.gaugeRed.copy(alpha = 0.06f) else colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Boost-Leck-Indikator",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                if (leakDetected) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeRed.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "LECK ERKANNT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "DICHT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 6.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        val topLeft = Offset(center.x - radius, center.y - radius)
                        val arcSize = Size(radius * 2, radius * 2)

                        drawArc(
                            color = colors.surfaceVariant,
                            startAngle = 140f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        drawArc(
                            color = statusColor,
                            startAngle = 140f,
                            sweepAngle = 260f * animatedEfficiency,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$efficiency",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(text = "%", fontSize = 8.sp, color = statusColor.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "System-Effizienz",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                                .fillMaxWidth(animatedEfficiency)
                                .background(statusColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    val statusText = when {
                        efficiency >= 95 -> "System einwandfrei — keine Leckage"
                        efficiency >= 80 -> "Leichte Druckverluste — überwachen"
                        efficiency >= 60 -> "Mehrere Leckstellen möglich — prüfen!"
                        else -> "Schwerer Boost-Leck — sofort prüfen!"
                    }
                    Text(
                        text = statusText,
                        fontSize = 9.sp,
                        color = statusColor
                    )
                }
            }
        }
    }
}
