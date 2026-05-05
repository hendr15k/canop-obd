package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.domain.DrivingEfficiencyScorer
import com.canopobd.data.domain.FuelConsumptionAnalyzer
import com.canopobd.ui.theme.*

@Composable
fun FuelEfficiencyCard(
    fuelData: FuelConsumptionAnalyzer.FuelConsumptionData,
    efficiencyScore: DrivingEfficiencyScorer.EfficiencyScore,
    modifier: Modifier = Modifier,
    colors: AppColors = DefaultAppColors
) {
    val grade = remember(efficiencyScore.overall) {
        DrivingEfficiencyScorer().getEfficiencyGrade(efficiencyScore.overall)
    }

    val gradeColor by animateColorAsState(
        targetValue = when {
            efficiencyScore.overall >= 80 -> colors.gaugeGreen
            efficiencyScore.overall >= 60 -> colors.gaugeYellow
            else -> colors.gaugeRed
        },
        animationSpec = tween(300),
        label = "gradeColor"
    )

    val trend = remember(fuelData.instantL100km) {
        if (fuelData.avgL100km > 0 && fuelData.instantL100km > 0) {
            val diff = fuelData.instantL100km - fuelData.avgL100km
            when {
                diff < -0.5 -> "↓"
                diff > 0.5 -> "↑"
                else -> "→"
            }
        } else "→"
    }

    val trendColor = when (trend) {
        "↓" -> colors.gaugeGreen
        "↑" -> colors.gaugeRed
        else -> colors.textSecondary
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                        imageVector = Icons.Filled.LocalGasStation,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kraftstoffeffizienz",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = gradeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$grade",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.fuel_economy_current),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (fuelData.instantL100km > 0) "%.1f".format(fuelData.instantL100km) else "--.-",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = " L/100km",
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = trend,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.fuel_economy_avg),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = if (fuelData.avgL100km > 0) "%.1f".format(fuelData.avgL100km) else "--.-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                    Text(
                        text = "L/100km",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            EfficiencyGauge(
                score = efficiencyScore.overall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EfficiencyStatChip(
                    label = "Beschl.",
                    value = efficiencyScore.accelerationScore,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
                EfficiencyStatChip(
                    label = "Kreuzf.",
                    value = efficiencyScore.cruisingScore,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
                EfficiencyStatChip(
                    label = "Brems.",
                    value = efficiencyScore.brakingScore,
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = colors.gaugeYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        efficiencyScore.tips.take(1).forEach { tip: String ->
                            Text(
                                text = tip,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            if (fuelData.totalFuelUsed > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Strecke: ${"%.1f".format(fuelData.tripDistance)} km",
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Text(
                        text = "Verbrauch: ${"%.1f".format(fuelData.totalFuelUsed)} L",
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                }
            }
        }
    }
}

@Composable
private fun EfficiencyGauge(
    score: Int,
    modifier: Modifier = Modifier,
    colors: AppColors
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(500),
        label = "gaugeScore"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.height - strokeWidth) / 2
        val centerX = size.width / 2
        val centerY = size.height / 2

        val startAngle = 135f
        val sweepAngle = 270f

        drawArc(
            color = colors.surfaceVariant,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(centerX - radius, 0f),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val progressAngle = (animatedScore / 100f) * sweepAngle
        val progressColor = when {
            animatedScore >= 80 -> colors.gaugeGreen
            animatedScore >= 60 -> colors.gaugeYellow
            else -> colors.gaugeRed
        }

        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    progressColor.copy(alpha = 0.8f),
                    progressColor
                )
            ),
            startAngle = startAngle,
            sweepAngle = progressAngle,
            useCenter = false,
            topLeft = Offset(centerX - radius, 0f),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawCircle(
            color = progressColor,
            radius = strokeWidth / 2,
            center = Offset(
                centerX + radius * kotlin.math.cos(Math.toRadians((startAngle + progressAngle).toDouble())).toFloat(),
                centerY + radius * kotlin.math.sin(Math.toRadians((startAngle + progressAngle).toDouble())).toFloat()
            )
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "/100",
                fontSize = 10.sp,
                color = colors.textDim
            )
        }
    }
}

@Composable
private fun EfficiencyStatChip(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    colors: AppColors
) {
    val chipColor = when {
        value >= 80 -> colors.gaugeGreen
        value >= 60 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = chipColor.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$value",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = chipColor
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = colors.textDim
            )
        }
    }
}

@Composable
fun FuelEfficiencyCardCompact(
    instantConsumption: Double,
    avgConsumption: Double,
    efficiencyRating: FuelConsumptionAnalyzer.EfficiencyRating,
    modifier: Modifier = Modifier,
    colors: AppColors = DefaultAppColors
) {
    val ratingColor = when (efficiencyRating) {
        FuelConsumptionAnalyzer.EfficiencyRating.EXCELLENT -> colors.gaugeGreen
        FuelConsumptionAnalyzer.EfficiencyRating.GOOD -> colors.gaugeCyan
        FuelConsumptionAnalyzer.EfficiencyRating.AVERAGE -> colors.gaugeYellow
        FuelConsumptionAnalyzer.EfficiencyRating.POOR -> colors.gaugeOrange
        FuelConsumptionAnalyzer.EfficiencyRating.CRITICAL -> colors.gaugeRed
        else -> colors.textSecondary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Verbrauch",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (instantConsumption > 0) "%.1f".format(instantConsumption) else "--.-",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = " L/100km",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ratingColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = efficiencyRating.grade.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ratingColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Ø ${if (avgConsumption > 0) "%.1f".format(avgConsumption) else "--.-"}",
                    fontSize = 10.sp,
                    color = colors.textDim
                )
            }
        }
    }
}
