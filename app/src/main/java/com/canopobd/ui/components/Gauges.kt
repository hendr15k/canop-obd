package com.canopobd.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularGauge(
    value: Float,
    minValue: Float,
    maxValue: Float,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    startAngle: Float = 135f,
    sweepAngle: Float = 270f,
    accentColor: Color = gaugeGreen
) {
    val clampedValue = value.coerceIn(minValue, maxValue)
    val fraction = (clampedValue - minValue) / (maxValue - minValue)

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "gauge_anim"
    )

    val arcColor = when {
        animatedFraction > 0.9f -> gaugeRed
        animatedFraction > 0.75f -> gaugeOrange
        animatedFraction > 0.5f -> accentColor
        else -> accentColor.copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.toPx() * 0.06f
            val outerStroke = strokeWidth * 0.3f
            val radius = (size.toPx() - strokeWidth) / 2 - outerStroke
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            drawArc(
                color = Color(0xFF1E1E30),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            val tickCount = 30
            for (i in 0..tickCount) {
                val tickAngle = Math.toRadians((startAngle + sweepAngle * i / tickCount).toDouble())
                val tickStart = radius - strokeWidth * 0.3f
                val tickEnd = radius + strokeWidth * 0.3f
                val tickFraction = i.toFloat() / tickCount
                val tickColor = if (tickFraction <= animatedFraction) {
                    arcColor.copy(alpha = 0.25f)
                } else {
                    Color(0xFF2A2A3A)
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
                    strokeWidth = 1.5f
                )
            }

            val gradient = Brush.sweepGradient(
                colors = listOf(
                    arcColor.copy(alpha = 0.2f),
                    arcColor,
                    arcColor.copy(alpha = 0.8f)
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

            val needleAngle = Math.toRadians((startAngle + sweepAngle * animatedFraction).toDouble())
            val needleLength = radius * 0.6f
            val needleX = center.x + (needleLength * cos(needleAngle)).toFloat()
            val needleY = center.y + (needleLength * sin(needleAngle)).toFloat()

            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = center,
                end = Offset(needleX, needleY),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = center,
                end = Offset(needleX, needleY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            drawCircle(color = arcColor.copy(alpha = 0.4f), radius = 12f, center = center)
            drawCircle(color = Color.White, radius = 5f, center = center)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (size / 5))
        ) {
            Text(
                text = "%.0f".format(clampedValue),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Text(
                text = unit,
                fontSize = 11.sp,
                color = textSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = textDim,
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
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CircularGauge(value = rpm, minValue = 0f, maxValue = 8000f, label = "RPM", unit = "rpm", size = 130.dp)
        CircularGauge(value = speed, minValue = 0f, maxValue = 260f, label = "Speed", unit = speedUnit, size = 130.dp)
        CircularGauge(value = temp, minValue = -40f, maxValue = 215f, label = "Coolant", unit = tempUnit, size = 130.dp)
    }
}
