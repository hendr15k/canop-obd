package com.canopobd.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val GaugeFont = FontFamily.Monospace

@Composable
fun CircularGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    size: Dp = GaugeConfig.DEFAULT_SIZE,
    startAngle: Float = GaugeConfig.START_ANGLE,
    sweepAngle: Float = GaugeConfig.SWEEP_ANGLE,
    accentColor: Color = gaugeGreen
) {
    val colors = LocalAppColors.current
    val clampedValue = value.coerceIn(minValue, maxValue)
    val fraction = (clampedValue - minValue) / (maxValue - minValue)

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gauge_anim"
    )

    val arcColor = when {
        animatedFraction > GaugeConfig.WARNING_THRESHOLD_HIGH -> colors.gaugeRed
        animatedFraction > GaugeConfig.WARNING_THRESHOLD_MEDIUM -> colors.gaugeOrange
        animatedFraction > GaugeConfig.WARNING_THRESHOLD_LOW -> accentColor
        else -> accentColor.copy(alpha = 0.85f)
    }

    val glowColor = when {
        animatedFraction > GaugeConfig.WARNING_THRESHOLD_HIGH -> colors.gaugeRedGlow
        animatedFraction > GaugeConfig.WARNING_THRESHOLD_MEDIUM -> colors.gaugeOrangeGlow
        else -> colors.gaugeGreenGlow
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.toPx() * GaugeConfig.STROKE_WIDTH_RATIO
            val outerStroke = strokeWidth * GaugeConfig.OUTER_STROKE_RATIO
            val radius = (size.toPx() - strokeWidth) / 2 - outerStroke
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            val bgArcColor = colors.surfaceVariant
            drawArc(
                color = bgArcColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            for (i in 0..GaugeConfig.TICK_COUNT) {
                val isMajor = i % GaugeConfig.MAJOR_TICK_INTERVAL == 0
                val tickAngle = Math.toRadians((startAngle + sweepAngle * i / GaugeConfig.TICK_COUNT).toDouble())
                val tickScale = if (isMajor) GaugeConfig.MAJOR_TICK_SCALE else 1f
                val tickStart = radius - strokeWidth * GaugeConfig.TICK_INNER_RATIO * tickScale
                val tickEnd = radius + strokeWidth * GaugeConfig.TICK_OUTER_RATIO * tickScale
                val tickFraction = i.toFloat() / GaugeConfig.TICK_COUNT
                val tickColor = if (tickFraction <= animatedFraction) {
                    arcColor.copy(alpha = if (isMajor) 0.5f else 0.2f)
                } else {
                    colors.borderSubtle.copy(alpha = if (isMajor) 0.6f else 0.3f)
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

            val gradientColors = listOf(
                glowColor.copy(alpha = 0.1f),
                arcColor.copy(alpha = 0.4f),
                arcColor,
                arcColor.copy(alpha = 0.9f)
            )

            val gradient = Brush.sweepGradient(
                colors = gradientColors,
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

            val needleAngle = Math.toRadians((startAngle + sweepAngle * animatedFraction).toDouble())
            val needleLength = radius * GaugeConfig.NEEDLE_LENGTH_RATIO
            val needleX = center.x + (needleLength * cos(needleAngle)).toFloat()
            val needleY = center.y + (needleLength * sin(needleAngle)).toFloat()

            val needlePath = Path().apply {
                moveTo(center.x, center.y)
                lineTo(needleX, needleY)
            }

            drawPath(
                path = needlePath,
                color = Color.White.copy(alpha = GaugeConfig.NEEDLE_SHADOW_ALPHA),
                style = Stroke(
                    width = GaugeConfig.NEEDLE_SHADOW_WIDTH,
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = needlePath,
                color = Color.White,
                style = Stroke(
                    width = GaugeConfig.NEEDLE_STROKE_WIDTH,
                    cap = StrokeCap.Round
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.5f), Color.Transparent),
                    center = center,
                    radius = GaugeConfig.CENTER_GLOW_RADIUS * 2
                ),
                radius = GaugeConfig.CENTER_GLOW_RADIUS * 2,
                center = center
            )

            drawCircle(
                color = arcColor,
                radius = GaugeConfig.NEEDLE_BASE_RADIUS,
                center = center
            )

            drawCircle(
                color = Color.White,
                radius = GaugeConfig.CENTER_DOT_RADIUS,
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (size / 5))
        ) {
            Text(
                text = "%.0f".format(clampedValue),
                fontSize = GaugeConfig.VALUE_FONT_SIZE.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontFamily = GaugeFont,
                letterSpacing = 1.sp
            )
            Text(
                text = unit,
                fontSize = GaugeConfig.UNIT_FONT_SIZE.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = GaugeConfig.LABEL_FONT_SIZE.sp,
                color = colors.textDim,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

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
            size = GaugeConfig.ROW_SIZE,
            accentColor = gaugeCyan
        )
        CircularGauge(
            value = speed,
            minValue = 0f,
            maxValue = GaugeConfig.SPEED_MAX,
            label = GaugeConfig.LABEL_SPEED,
            unit = speedUnit,
            size = GaugeConfig.ROW_SIZE,
            accentColor = gaugeBlueGlow
        )
        CircularGauge(
            value = temp,
            minValue = GaugeConfig.TEMP_MIN,
            maxValue = GaugeConfig.TEMP_MAX,
            label = GaugeConfig.LABEL_COOLANT,
            unit = tempUnit,
            size = GaugeConfig.ROW_SIZE,
            accentColor = gaugeOrange
        )
    }
}

@Composable
fun CompactGauge(
    value: Double,
    label: String,
    unit: String = "",
    max: Double = 100.0,
    color: Color = gaugeGreen,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val intensity = (value / max.coerceAtLeast(0.01)).toFloat().coerceIn(0f, 1f)
    val displayValue = abs(value).coerceIn(0.0, max)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.borderSubtle.copy(alpha = 0.5f + intensity * 0.3f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "%.0f".format(displayValue),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = GaugeFont
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = colors.textDim,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        color = colors.surfaceVariant,
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(intensity)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.7f),
                                    color
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
