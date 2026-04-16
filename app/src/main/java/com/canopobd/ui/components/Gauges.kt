package com.canopobd.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Circular gauge for displaying OBD values
 */
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
    sweepAngle: Float = 270f
) {
    val clampedValue = value.coerceIn(minValue, maxValue)
    val fraction = (clampedValue - minValue) / (maxValue - minValue)

    val arcColor = when {
        fraction < 0.5f -> gaugeGreen
        fraction < 0.75f -> gaugeYellow
        fraction < 0.9f -> gaugeOrange
        else -> gaugeRed
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.toPx() * 0.08f
            val radius = (size.toPx() - strokeWidth) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            // Background arc
            drawArc(
                color = Color(0xFF2A2A3A),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Value arc
            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle * fraction,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Needle
            val needleAngle = Math.toRadians((startAngle + sweepAngle * fraction).toDouble())
            val needleLength = radius * 0.65f
            val needleX = center.x + (needleLength * cos(needleAngle)).toFloat()
            val needleY = center.y + (needleLength * sin(needleAngle)).toFloat()

            drawLine(
                color = Color.White,
                start = center,
                end = Offset(needleX, needleY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Center dot
            drawCircle(color = Color.White, radius = 8f, center = center)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (size / 5))
        ) {
            Text(
                text = "%.0f".format(clampedValue),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = unit,
                fontSize = 12.sp,
                color = textSecondary
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

/**
 * Row of three gauges for the main dashboard
 */
@Composable
fun GaugeRow(
    rpm: Float, speed: Float, temp: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CircularGauge(
            value = rpm,
            minValue = 0f,
            maxValue = 8000f,
            label = "RPM",
            unit = "rpm",
            size = 130.dp
        )
        CircularGauge(
            value = speed,
            minValue = 0f,
            maxValue = 260f,
            label = "Speed",
            unit = "km/h",
            size = 130.dp
        )
        CircularGauge(
            value = temp,
            minValue = -40f,
            maxValue = 215f,
            label = "Coolant",
            unit = "°C",
            size = 130.dp
        )
    }
}
