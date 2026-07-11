package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

/**
 * TuningStageIndicator - Tuning-Stufen-Indikator (A14NET)
 *
 * Schätzt den aktuellen Tuning-Stadium basierend auf Ladedruck- und Leistungsdaten:
 * - Stock: Serienmäßig
 * - Stage 1: Leichtesuning (bis ~170 PS)
 * - Stage 2: Mittleres Tuning (bis ~200 PS)
 * - Stage 3: Aggressives Tuning (über 200 PS)
 *
 * Enthält:
 * - Aktuelle geschätzte Stufe
 * - Ladedruck-Vergleich
 * - Leistungsschätzung
 * - Kraftstoffempfehlung
 * - Warnung für M32-Getriebe-Grenzen
 */
@Composable
fun TuningStageIndicator(
    modifier: Modifier = Modifier,
    boostPressureBar: Double = 0.0,
    targetBoostBar: Double = 0.0,
    estimatedPowerHp: Double = 0.0,
    fuelOctane: Int = 95,
    colors: AppColors = LocalAppColors.current
) {
    val stage = remember(boostPressureBar, estimatedPowerHp) {
        TuningStage.estimate(boostPressureBar, estimatedPowerHp)
    }

    val stageColor = remember(stage) { stage.color(colors) }

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = stageColor.copy(alpha = 0.4f)
        )
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
                        tint = stageColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.tuning_stage_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                // Stage badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = stageColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = stage.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = stageColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage progression bar
            StageProgressBar(
                stage = stage,
                stageColor = stageColor,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Boost comparison
                TuningStatColumn(
                    label = stringResource(R.string.tuning_boost_comparison),
                    value = "%.2f bar".format(boostPressureBar),
                    subtext = "Soll: %.2f bar".format(targetBoostBar),
                    color = when {
                        boostPressureBar > targetBoostBar + 0.3 -> colors.gaugeOrange
                        boostPressureBar > targetBoostBar -> colors.gaugeYellow
                        else -> colors.gaugeGreen
                    },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Power estimate
                TuningStatColumn(
                    label = stringResource(R.string.tuning_power_estimate),
                    value = "%.0f PS".format(estimatedPowerHp),
                    subtext = "Serie: 140 PS",
                    color = when {
                        estimatedPowerHp > 200 -> colors.gaugeRed
                        estimatedPowerHp > 170 -> colors.gaugeOrange
                        estimatedPowerHp > 140 -> colors.gaugeYellow
                        else -> colors.gaugeGreen
                    },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Fuel recommendation
                TuningStatColumn(
                    label = stringResource(R.string.tuning_fuel_recommendation),
                    value = "$fuelOctane RON",
                    subtext = when (stage) {
                        TuningStage.STAGE_3 -> "98+ RON"
                        TuningStage.STAGE_2 -> "98 RON"
                        TuningStage.STAGE_1 -> "95+ RON"
                        TuningStage.STOCK -> "95 RON"
                    },
                    color = when {
                        stage == TuningStage.STAGE_3 && fuelOctane < 98 -> colors.gaugeRed
                        stage == TuningStage.STAGE_2 && fuelOctane < 98 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            // M32 gearbox warning
            if (stage.ordinal >= TuningStage.STAGE_2.ordinal) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.gaugeOrange.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = colors.gaugeOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.tuning_m32_warning),
                            fontSize = 11.sp,
                            color = colors.gaugeOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun StageProgressBar(
    stage: TuningStage,
    stageColor: Color,
    colors: AppColors
) {
    val stageProgress = when (stage) {
        TuningStage.STOCK -> 0.25f
        TuningStage.STAGE_1 -> 0.50f
        TuningStage.STAGE_2 -> 0.75f
        TuningStage.STAGE_3 -> 1.0f
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(stageProgress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colors.gaugeGreen,
                                colors.gaugeYellow,
                                colors.gaugeOrange,
                                colors.gaugeRed
                            )
                        )
                    )
            )

            // Stage markers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S1", "S2", "S3").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 7.sp,
                        color = colors.textPrimary.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Stock", fontSize = 8.sp, color = colors.textDim)
            Text("Stage 1", fontSize = 8.sp, color = colors.gaugeGreen)
            Text("Stage 2", fontSize = 8.sp, color = colors.gaugeOrange)
            Text("Stage 3", fontSize = 8.sp, color = colors.gaugeRed)
        }
    }
}

@Composable
private fun TuningStatColumn(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = subtext,
            fontSize = 9.sp,
            color = colors.textSecondary
        )
    }
}

enum class TuningStage(val label: String, val description: String) {
    STOCK("Stock", "Serienmäßig"),
    STAGE_1("Stage 1", "Leichtes Tuning"),
    STAGE_2("Stage 2", "Mittleres Tuning"),
    STAGE_3("Stage 3", "Aggressives Tuning");

    fun color(colors: AppColors): Color = when (this) {
        STOCK -> colors.gaugeGreen
        STAGE_1 -> colors.gaugeYellow
        STAGE_2 -> colors.gaugeOrange
        STAGE_3 -> colors.gaugeRed
    }

    companion object {
        fun estimate(boostBar: Double, powerHp: Double): TuningStage = when {
            powerHp > 200 || boostBar > 1.2 -> STAGE_3
            powerHp > 170 || boostBar > 0.9 -> STAGE_2
            powerHp > 145 || boostBar > 0.75 -> STAGE_1
            else -> STOCK
        }
    }
}
