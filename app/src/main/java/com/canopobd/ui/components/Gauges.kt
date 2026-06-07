package com.canopobd.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

private val GaugeFont = FontFamily.Monospace

// ============================================================================
// GAUGE COMPONENTS v2.0
// Modern telemetry-style gauges with glow, gradient, smooth animations.
// ============================================================================

// ---------------------------------------------------------------------------
// HERO CIRCULAR GAUGE — primary gauge for RPM/Speed/Coolant
// Animated arc with gradient, tick marks, glow effect, center value
// ---------------------------------------------------------------------------
@Composable
fun CircularGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    size: Dp = GaugeConfig.DEFAULT_SIZE.dp,
    startAngle: Float = GaugeConfig.START_ANGLE,
    sweepAngle: Float = GaugeConfig.SWEEP_ANGLE,
    accentColor: Color? = null,
    warningThreshold: Float = GaugeConfig.WARNING_THRESHOLD_MEDIUM,
    criticalThreshold: Float = GaugeConfig.WARNING_THRESHOLD_HIGH,
    showValue: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val resolvedAccent = accentColor ?: colors.primary
    val clampedValue = value.coerceIn(minValue, maxValue)
    val range = (maxValue - minValue).coerceAtLeast(0.001f)
    val fraction = (clampedValue - minValue) / range

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gauge_anim"
    )

    // Color: gradient from base → warning → critical as fraction grows
    val (arcColor, glowColor) = when {
        animatedFraction > criticalThreshold -> colors.critical to colors.criticalGlow
        animatedFraction > warningThreshold -> colors.warning to colors.warningGlow
        else -> resolvedAccent to resolvedAccent
    }

    Box(
        modifier = modifier
            .size(size)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.toPx() * GaugeConfig.STROKE_WIDTH_RATIO
            val outerStroke = strokeWidth * GaugeConfig.OUTER_STROKE_RATIO
            val radius = (size.toPx() - strokeWidth) / 2 - outerStroke
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            // Background track
            drawArc(
                color = colors.surfaceElevated,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            // Tick marks
            for (i in 0..GaugeConfig.TICK_COUNT) {
                val isMajor = i % GaugeConfig.MAJOR_TICK_INTERVAL == 0
                val tickAngle = Math.toRadians(
                    (startAngle + sweepAngle * i / GaugeConfig.TICK_COUNT).toDouble()
                )
                val tickScale = if (isMajor) GaugeConfig.MAJOR_TICK_SCALE else 1f
                val tickStart = radius - strokeWidth * GaugeConfig.TICK_INNER_RATIO * tickScale
                val tickEnd = radius + strokeWidth * GaugeConfig.TICK_OUTER_RATIO * tickScale
                val tickFraction = i.toFloat() / GaugeConfig.TICK_COUNT
                val tickColor = if (tickFraction <= animatedFraction) {
                    arcColor.copy(alpha = if (isMajor) 0.7f else 0.35f)
                } else {
                    colors.borderDefault.copy(alpha = if (isMajor) 0.5f else 0.25f)
                }
                drawLine(
                    color = tickColor,
                    start = Offset(
                        center.x + (tickStart * cos(tickAngle)).toFloat(),
                        center.y + (tickStart * sin(tickAngle)).toFloat()
                    ),
                    end = Offset(
                        center.x + (tickEnd * cos(tickAngle)).toFloat(),
                        center.y + (tickEnd * sin(tickAngle)).toFloat()
                    ),
                    strokeWidth = GaugeConfig.TICK_STROKE_WIDTH * (if (isMajor) 1.5f else 1f),
                    cap = StrokeCap.Round
                )
            }

            // Progress arc with sweep gradient
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.3f),
                    arcColor.copy(alpha = 0.85f),
                    arcColor,
                    arcColor.copy(alpha = 0.95f),
                    glowColor.copy(alpha = 0.3f)
                ),
                center = center
            )
            drawArc(
                brush = gradient,
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedFraction,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Center glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = GaugeConfig.CENTER_GLOW_RADIUS * 2
                ),
                radius = GaugeConfig.CENTER_GLOW_RADIUS * 2,
                center = center
            )

            // Needle marker on the arc tip
            val needleAngle = Math.toRadians(
                (startAngle + sweepAngle * animatedFraction).toDouble()
            )
            val needleLength = radius
            val needleX = center.x + (needleLength * cos(needleAngle)).toFloat()
            val needleY = center.y + (needleLength * sin(needleAngle)).toFloat()
            drawCircle(
                color = arcColor,
                radius = GaugeConfig.NEEDLE_BASE_RADIUS,
                center = Offset(needleX, needleY)
            )
            drawCircle(
                color = colors.surfaceBlack,
                radius = GaugeConfig.NEEDLE_BASE_RADIUS * 0.5f,
                center = Offset(needleX, needleY)
            )
        }

        if (showValue) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (size / 6))
            ) {
                Text(
                    text = "%.0f".format(clampedValue),
                    style = GaugeTypography.valueLarge,
                    color = arcColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// GAUGE ROW — three primary gauges
// ---------------------------------------------------------------------------
@Composable
fun GaugeRow(
    rpm: Float,
    speed: Float,
    temp: Float,
    speedUnit: String,
    tempUnit: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularGauge(
            value = rpm,
            minValue = 0f,
            maxValue = GaugeConfig.RPM_MAX,
            label = GaugeConfig.LABEL_RPM,
            unit = "rpm",
            size = GaugeConfig.ROW_SIZE.dp,
            accentColor = colors_primary_safe()
        )
        CircularGauge(
            value = speed,
            minValue = 0f,
            maxValue = GaugeConfig.SPEED_MAX,
            label = GaugeConfig.LABEL_SPEED,
            unit = speedUnit,
            size = GaugeConfig.ROW_SIZE.dp,
            accentColor = colors_secondary_safe()
        )
        CircularGauge(
            value = temp,
            minValue = GaugeConfig.TEMP_MIN,
            maxValue = GaugeConfig.TEMP_MAX,
            label = GaugeConfig.LABEL_COOLANT,
            unit = tempUnit,
            size = GaugeConfig.ROW_SIZE.dp,
            accentColor = colors_warning_safe()
        )
    }
}

@Composable
private fun colors_primary_safe(): Color = LocalAppColors.current.primary
@Composable
private fun colors_secondary_safe(): Color = LocalAppColors.current.secondary
@Composable
private fun colors_warning_safe(): Color = LocalAppColors.current.warning

// ---------------------------------------------------------------------------
// COMPACT GAUGE — small value tile with progress arc
// ---------------------------------------------------------------------------
@Composable
fun CompactGauge(
    value: Double,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = "",
    max: Double = 100.0,
    color: Color? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val resolved = color ?: colors.primary
    val intensity = (value / max.coerceAtLeast(0.01)).toFloat().coerceIn(0f, 1f)
    val displayValue = kotlin.math.abs(value).coerceIn(0.0, max)

    // Color shifts to warning/critical when too high
    val resolvedColor = when {
        intensity > 0.9 -> colors.critical
        intensity > 0.75 -> colors.warning
        else -> resolved
    }

    val animatedIntensity by animateFloatAsState(
        targetValue = intensity,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compact_gauge"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceBase)
            .border(
                1.dp,
                resolvedColor.copy(alpha = 0.25f + animatedIntensity * 0.25f),
                RoundedCornerShape(AppRadius.md)
            )
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "%.0f".format(displayValue),
                    style = GaugeTypography.valueMedium,
                    color = resolvedColor
                )
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            // Mini progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(resolvedColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedIntensity)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    resolvedColor.copy(alpha = 0.7f),
                                    resolvedColor
                                )
                            )
                        )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HALF-RING GAUGE — gauge as a half circle (semicircle)
// ---------------------------------------------------------------------------
@Composable
fun HalfRingGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    label: String,
    valueLabel: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp,
    accentColor: Color? = null,
    warningThreshold: Float = 0.7f,
    criticalThreshold: Float = 0.9f
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.primary
    val range = (maxValue - minValue).coerceAtLeast(0.001f)
    val fraction = ((value - minValue) / range).coerceIn(0f, 1f)

    val resolvedColor = when {
        fraction > criticalThreshold -> colors.critical
        fraction > warningThreshold -> colors.warning
        else -> resolved
    }

    val animated by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "half_ring"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            resolvedColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = (size.toPx() - stroke) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            // Background track
            drawArc(
                color = colors.surfaceElevated,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        resolvedColor.copy(alpha = 0.6f),
                        resolvedColor
                    ),
                    center = center
                ),
                startAngle = 180f,
                sweepAngle = 180f * animated,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = size / 12)
        ) {
            Text(
                text = valueLabel ?: "%.0f".format(value),
                style = GaugeTypography.valueLarge,
                color = resolvedColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// SPARKLINE — minimal line chart for trend data
// ---------------------------------------------------------------------------
@Composable
fun Sparkline(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color? = null,
    fillAlpha: Float = 0.25f,
    strokeWidth: Float = 2f
) {
    val colors = LocalAppColors.current
    val resolved = color ?: colors.primary
    val animatedData = remember(data) {
        data.mapIndexed { i, _ -> i.toFloat() / (data.size - 1).coerceAtLeast(1).toFloat() }
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        if (data.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val minV = data.min()
        val maxV = data.max()
        val range = (maxV - minV).coerceAtLeast(0.001f)
        val points = data.mapIndexed { i, v ->
            val x = animatedData[i] * w
            val y = h - ((v - minV) / range) * h
            Offset(x, y)
        }
        // Fill area
        val fillPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(points.first().x, h)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(resolved.copy(alpha = fillAlpha), Color.Transparent)
            )
        )
        // Line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = resolved,
                start = points[i],
                end = points[i + 1],
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
        // End dot
        drawCircle(
            color = resolved,
            radius = 3f,
            center = points.last()
        )
    }
}

// ---------------------------------------------------------------------------
// GAUGE CONFIG
// ---------------------------------------------------------------------------
object GaugeConfig {
    const val DEFAULT_SIZE = 160
    const val ROW_SIZE = 120
    const val START_ANGLE = 135f
    const val SWEEP_ANGLE = 270f
    const val STROKE_WIDTH_RATIO = 0.12f
    const val OUTER_STROKE_RATIO = 1.0f
    const val TICK_COUNT = 30
    const val MAJOR_TICK_INTERVAL = 5
    const val MAJOR_TICK_SCALE = 1.5f
    const val TICK_INNER_RATIO = 0.5f
    const val TICK_OUTER_RATIO = 0.7f
    const val TICK_STROKE_WIDTH = 2f
    const val WARNING_THRESHOLD_LOW = 0.5f
    const val WARNING_THRESHOLD_MEDIUM = 0.7f
    const val WARNING_THRESHOLD_HIGH = 0.9f
    const val CENTER_GLOW_RADIUS = 40f
    const val NEEDLE_BASE_RADIUS = 6f
    const val VALUE_FONT_SIZE = 28
    const val UNIT_FONT_SIZE = 11
    const val LABEL_FONT_SIZE = 10
    const val RPM_MAX = 8000f
    const val SPEED_MAX = 260f
    const val TEMP_MIN = -40f
    const val TEMP_MAX = 130f
    const val LABEL_RPM = "Engine"
    const val LABEL_SPEED = "Speed"
    const val LABEL_COOLANT = "Coolant"
    const val NEEDLE_LENGTH_RATIO = 0.85f
    const val NEEDLE_SHADOW_ALPHA = 0.3f
    const val NEEDLE_SHADOW_WIDTH = 3f
    const val NEEDLE_STROKE_WIDTH = 2f
    const val CENTER_DOT_RADIUS = 4f
}

// ---------------------------------------------------------------------------
// TREND GAUGE — gauge with embedded sparkline
// ---------------------------------------------------------------------------
@Composable
fun TrendGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    trend: List<Float>,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.primary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.0f".format(value),
                    style = GaugeTypography.valueMedium,
                    color = resolved
                )
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Sparkline(
                    data = trend,
                    color = resolved
                )
            }
        }
    }
}
