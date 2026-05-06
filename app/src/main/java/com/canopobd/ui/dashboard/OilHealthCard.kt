package com.canopobd.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.domain.OilHealthPredictor
import com.canopobd.data.domain.ValidationResult
import com.canopobd.ui.theme.AppColors

@Composable
fun OilHealthCard(
    prediction: OilHealthPredictor.OilHealthPredictionResult,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val statusColor = Color(prediction.prediction.colorHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard,
        tonalElevation = 2.dp,
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OilHealthRing(
                        score = prediction.healthScore,
                        color = statusColor,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Öl-Gesundheit",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = prediction.prediction.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${prediction.healthScore}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "km: ${prediction.kmSinceOilChange.toInt()}",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreChip(label = "Thermal", score = prediction.thermalLoadScore, colors = colors)
                ScoreChip(label = "Fahrprofil", score = prediction.drivingPatternScore, colors = colors)
                ScoreChip(label = "Verbrauch", score = prediction.consumptionScore, colors = colors)
            }

            if (prediction.prediction.severity >= 2) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = prediction.recommendation.take(80),
                        fontSize = 10.sp,
                        color = statusColor,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreChip(
    label: String,
    score: Int,
    colors: AppColors
) {
    val chipColor = when {
        score >= 80 -> colors.gaugeGreen
        score >= 60 -> colors.gaugeYellow
        score >= 40 -> colors.gaugeOrange
        else -> colors.gaugeRed
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = chipColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$score",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = chipColor
            )
        }
    }
}

@Composable
fun OilHealthRing(
    score: Int,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 6f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "oil_health_ring"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val topLeft = Offset(
                (size.width - canvasSize) / 2 + strokeWidth / 2,
                (size.height - canvasSize) / 2 + strokeWidth / 2
            )
            val ringSize = Size(canvasSize - strokeWidth, canvasSize - strokeWidth)

            drawArc(
                color = color.copy(alpha = 0.12f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = ringSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = ringSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$score",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun SensorValidationCard(
    validationResult: ValidationResult,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val (statusColor, label, description) = when (validationResult) {
        is ValidationResult.Valid -> Triple(colors.gaugeGreen, "Sensor OK", "Alle Sensorwerte im normalen Bereich")
        is ValidationResult.Suspicious -> Triple(colors.gaugeOrange, "Auffällig", validationResult.message)
        is ValidationResult.Invalid -> Triple(colors.gaugeRed, "Fehler", validationResult.message)
        is ValidationResult.Unavailable -> Triple(colors.textSecondary, "N/A", "Keine Sensordaten verfügbar")
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ValidationRing(
                result = validationResult,
                color = statusColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sensor-Validierung",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun ValidationRing(
    result: ValidationResult,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 6f
) {
    val targetProgress = when (result) {
        is ValidationResult.Valid -> 1f
        is ValidationResult.Suspicious -> 0.65f
        is ValidationResult.Invalid -> 0.3f
        is ValidationResult.Unavailable -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600),
        label = "validation_ring"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val topLeft = Offset(
                (size.width - canvasSize) / 2 + strokeWidth / 2,
                (size.height - canvasSize) / 2 + strokeWidth / 2
            )
            val ringSize = Size(canvasSize - strokeWidth, canvasSize - strokeWidth)

            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = ringSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = ringSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ValidationBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
