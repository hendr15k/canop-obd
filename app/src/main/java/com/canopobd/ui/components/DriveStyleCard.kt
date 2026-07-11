package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

/**
 * DriveStyleCard - Fahrstil-Analysekarte (A14NET)
 *
 * Zeigt:
 * - Eco/Sport-Balance-Indikator
 * - RPM-Verteilungsdiagramm
 * - Overboost-Nutzungszähler
 * - Bremsnutzungsprozent
 * - Eco-Score (0-100)
 * - Sport-Score (0-100)
 * - Tipps auf Deutsch
 */
@Composable
fun DriveStyleCard(
    ecoScore: Int = 0,
    sportScore: Int = 0,
    overboostUsageCount: Int = 0,
    brakeUsagePercent: Double = 0.0,
    avgRpm: Double = 0.0,
    modifier: Modifier = Modifier,
    rpmDistribution: Map<String, Double> = emptyMap(),
    colors: AppColors = LocalAppColors.current
) {
    val balanceIndicator = remember(ecoScore, sportScore) {
        calculateBalanceIndicator(ecoScore, sportScore)
    }

    val tipText = remember(ecoScore, sportScore, overboostUsageCount, brakeUsagePercent) {
        getDriveStyleTip(ecoScore, sportScore, overboostUsageCount, brakeUsagePercent)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.drive_score_analysis_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                // Eco score badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.gaugeGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$ecoScore",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeGreen
                        )
                        Text(
                            text = "/100",
                            fontSize = 10.sp,
                            color = colors.gaugeGreen.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Eco/Sport balance indicator
            EcoSportBalance(
                ecoScore = ecoScore,
                sportScore = sportScore,
                balance = balanceIndicator,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // RPM Distribution
            RPMDistributionChart(
                distribution = rpmDistribution,
                avgRpm = avgRpm,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overboost counter
                DriveStyleStatItem(
                    label = stringResource(R.string.drive_style_overboost),
                    value = "$overboostUsageCount",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = when {
                        overboostUsageCount > 10 -> colors.gaugeOrange
                        overboostUsageCount > 5 -> colors.gaugeYellow
                        else -> colors.gaugeGreen
                    },
                    modifier = Modifier.weight(1f)
                )

                // Brake usage
                DriveStyleStatItem(
                    label = stringResource(R.string.drive_style_brakes),
                    value = "%.0f%%".format(brakeUsagePercent),
                    icon = Icons.Filled.Warning,
                    color = when {
                        brakeUsagePercent > 30 -> colors.gaugeRed
                        brakeUsagePercent > 20 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tip
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
                    Text(
                        text = tipText,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EcoSportBalance(
    ecoScore: Int,
    sportScore: Int,
    balance: Float,
    colors: AppColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ECO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.gaugeGreen
            )
            Text(
                text = "SPORT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.gaugeOrange
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Balance bar
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceVariant)
        ) {
            val barWidth = maxWidth

            // ECO side
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(balance)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.gaugeGreen,
                                colors.gaugeGreen.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            // Indicator line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .offset(x = (balance * barWidth.value).dp)
                    .background(colors.textPrimary)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Eco: $ecoScore",
                fontSize = 10.sp,
                color = colors.gaugeGreen
            )
            Text(
                text = "Sport: $sportScore",
                fontSize = 10.sp,
                color = colors.gaugeOrange
            )
        }
    }
}

@Composable
private fun RPMDistributionChart(
    distribution: Map<String, Double>,
    avgRpm: Double,
    colors: AppColors
) {
    val defaultDistribution = if (distribution.isEmpty()) {
        mapOf(
            "<2k" to 0.3,
            "2-3k" to 0.35,
            "3-4k" to 0.2,
            "4-5k" to 0.1,
            "5k+" to 0.05
        )
    } else distribution

    val maxPercent = defaultDistribution.values.maxOrNull()?.coerceAtLeast(0.01) ?: 1.0

    Column {
        Text(
            text = stringResource(R.string.drive_style_rpm_dist),
            fontSize = 11.sp,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Bar chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            defaultDistribution.forEach { (label, percent) ->
                val barHeight = ((percent / maxPercent) * 100).toFloat()
                val barColor = when (label) {
                    "<2k" -> colors.gaugeGreen
                    "2-3k" -> colors.gaugeCyan
                    "3-4k" -> colors.gaugeYellow
                    "4-5k" -> colors.gaugeOrange
                    "5k+" -> colors.gaugeRed
                    else -> colors.textSecondary
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "%.0f%%".format(percent * 100),
                        fontSize = 8.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((barHeight * 0.5).dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(barColor)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 8.sp,
                        color = colors.textDim
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ø RPM: %.0f".format(avgRpm),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun DriveStyleStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = appColors.textDim
            )
        }
    }
}

private val appColors: AppColors
    @Composable
    get() = LocalAppColors.current

private fun calculateBalanceIndicator(ecoScore: Int, sportScore: Int): Float {
    val total = (ecoScore + sportScore).coerceAtLeast(1)
    return (ecoScore.toFloat() / total).coerceIn(0f, 1f)
}

private fun getDriveStyleTip(
    ecoScore: Int,
    sportScore: Int,
    overboostCount: Int,
    brakePercent: Double
): String {
    return when {
        overboostCount > 10 -> "Overboost häufig genutzt — Kraftstoffverbrauch beachten"
        brakePercent > 30 -> "Bremsnutzung hoch — vorausschauend fahren spart Kraft"
        ecoScore > 70 && sportScore < 30 -> "Sehr sparsam! Weiter so — turbofreundlich"
        sportScore > 70 && ecoScore < 30 -> "Sportlich unterwegs — Ölwechselintervalle beachten"
        ecoScore > 50 && sportScore > 50 -> "Ausgewogener Fahrstil — ideal für Motorlebensdauer"
        else -> "Tip: Im RPM-Bereich 1500-3000 für optimale Effizienz bleiben"
    }
}
