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
import com.canopobd.data.model.CarProfile
import com.canopobd.data.model.TurboData
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors
import kotlin.math.abs
import kotlin.math.min

/**
 * TurboDetailCard - Erweiterte Turbo-Überwachungskarte (A14NET / BorgWarner KP39)
 *
 * Zeigt:
 * - Boost-Gauge (Ist vs Soll) mit Normal/Overboost-Bereich
 * - Wastegate-Duty-Cycle-Balken
 * - Turbo-Drehzahl-Indikator
 * - Abgastemperatur (EGT) mit Farbcodierung
 * - Turbo-Effizienz in Prozent
 * - Turbo-Gesundheitsscore (0-100)
 * - Trend-Pfeile (verbessernd/verschlechternd)
 * - Gesundheitstext auf Deutsch
 */
@Composable
fun TurboDetailCard(
    turboData: TurboData,
    carProfile: CarProfile,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val healthScore = remember(turboData, carProfile) {
        calculateTurboDetailHealthScore(turboData, carProfile)
    }
    val healthColor = remember(healthScore) { getTurboDetailHealthColor(healthScore) }
    val healthLabel = remember(healthScore) { getTurboDetailHealthLabel(healthScore) }
    val healthDescription = remember(healthScore) { getTurboDetailHealthDescription(healthScore) }

    val isCritical = healthScore < 50
    val isWarning = healthScore in 50..79

    val infiniteTransition = rememberInfiniteTransition(label = "turbo_detail_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isCritical) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val animatedHealthColor by animateColorAsState(
        targetValue = healthColor,
        animationSpec = tween(300),
        label = "health_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCritical) {
                    Modifier.border(
                        width = 2.dp,
                        color = colors.gaugeRed.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else if (isWarning) {
                    Modifier.border(
                        width = 1.dp,
                        color = colors.gaugeOrange.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        onClick = onClick ?: {}
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
                        imageVector = Icons.Filled.Air,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.turbo_detail_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                // Health score badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = animatedHealthColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$healthScore",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedHealthColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "/100",
                            fontSize = 12.sp,
                            color = animatedHealthColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Boost Gauge (Ist vs Soll)
            TurboBoostGaugeSection(
                boostActual = turboData.boostPressure,
                boostTarget = turboData.boostTarget,
                maxBoost = carProfile.maxBoostGaugeBar.toDouble(),
                normalBoost = carProfile.normalBoostBar.toDouble(),
                overboostActive = turboData.overboostActive,
                underboostDetected = turboData.underboostDetected,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Wastegate duty cycle bar
            WastegateDutyBar(
                wastegateDuty = turboData.wastegateDutyCycle,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Turbo speed indicator and EGT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TurboSpeedIndicator(
                    turboRpm = turboData.turboRpm,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                egtTemperatureDisplay(
                    egtCelsius = turboData.turboInletTemp,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Efficiency and health
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Turbo efficiency
                TurboEfficiencyIndicator(
                    inletTemp = turboData.turboInletTemp,
                    outletTemp = turboData.turboOutletTemp,
                    chargeAirTemp = turboData.chargeAirCoolerTemp,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                // Trend and health text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.turbo_health),
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val trendIcon = getTurboHealthTrend(healthScore)
                        val trendColor = when {
                            healthScore >= 80 -> colors.gaugeGreen
                            healthScore >= 50 -> colors.gaugeYellow
                            else -> colors.gaugeRed
                        }
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = healthLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedHealthColor
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = healthDescription,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TurboBoostGaugeSection(
    boostActual: Double,
    boostTarget: Double,
    maxBoost: Double,
    normalBoost: Double,
    overboostActive: Boolean,
    underboostDetected: Boolean,
    colors: AppColors
) {
    val deviation = if (boostTarget > 0) boostActual - boostTarget else 0.0

    val boostColor = when {
        overboostActive -> colors.gaugeOrange
        underboostDetected -> colors.gaugeRed
        boostActual > normalBoost + 0.15 -> colors.gaugeOrange
        boostActual >= normalBoost - 0.1 -> colors.gaugeGreen
        boostActual > 0.1 -> colors.gaugeYellow
        else -> colors.textSecondary
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.boost_pressure),
                fontSize = 11.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (overboostActive) {
                    TurboStatusBadge(
                        text = stringResource(R.string.overboost_active),
                        color = colors.gaugeOrange
                    )
                }
                if (underboostDetected) {
                    TurboStatusBadge(
                        text = stringResource(R.string.underboost_detected),
                        color = colors.gaugeRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Boost values
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.2f".format(boostActual),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = boostColor
                    )
                    Text(
                        text = " bar",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Soll: ",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "%.2f bar".format(boostTarget),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Δ: ",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "%+.2f bar".format(deviation),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            abs(deviation) > 0.2 -> colors.gaugeRed
                            abs(deviation) > 0.15 -> colors.gaugeOrange
                            abs(deviation) > 0.1 -> colors.gaugeYellow
                            else -> colors.gaugeGreen
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colors.surfaceVariant)
        ) {
            val boxWidthDp = maxWidth

            val normalStart = ((normalBoost - 0.1) / maxBoost).coerceAtLeast(0.0)
            val normalEnd = ((normalBoost + 0.15) / maxBoost).coerceAtMost(1.0)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalStart.toFloat())
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .offset(x = (normalStart * boxWidthDp.value).dp)
                    .width(((normalEnd - normalStart) * boxWidthDp.value).dp)
                    .background(colors.gaugeGreen.copy(alpha = 0.3f))
            )

            // Actual boost indicator
            val normalized = (boostActual / maxBoost).coerceIn(0.0, 1.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalized)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                boostColor.copy(alpha = 0.6f),
                                boostColor
                            )
                        )
                    )
            )
        }

        // Scale labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0.0", fontSize = 8.sp, color = colors.textDim)
            Text("Normal", fontSize = 8.sp, color = colors.gaugeGreen)
            Text("Overboost", fontSize = 8.sp, color = colors.gaugeOrange)
            Text("%.1f".format(maxBoost), fontSize = 8.sp, color = colors.textDim)
        }
    }
}

@Composable
private fun TurboStatusBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.25f)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun WastegateDutyBar(
    wastegateDuty: Double,
    colors: AppColors
) {
    val wgColor = when {
        wastegateDuty > 95 || wastegateDuty < 5 -> colors.gaugeRed
        wastegateDuty > 80 -> colors.gaugeOrange
        wastegateDuty < 30 -> colors.gaugeOrange
        else -> colors.gaugeCyan
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = wgColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.wastegate),
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "%.1f%%".format(wastegateDuty),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = wgColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surfaceVariant)
        ) {
            val posNormalized = ((100 - wastegateDuty).coerceIn(0.0, 100.0) / 100.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(posNormalized)
                    .background(wgColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Offen", fontSize = 8.sp, color = colors.textDim)
            Text("100%", fontSize = 8.sp, color = colors.textDim)
        }
    }
}

@Composable
private fun TurboSpeedIndicator(
    turboRpm: Double,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val maxTurboRpm = 200000.0
    val normalized = (turboRpm / maxTurboRpm).coerceIn(0.0, 1.0).toFloat()
    val turboColor = when {
        turboRpm > 180000 -> colors.gaugeRed
        turboRpm > 150000 -> colors.gaugeOrange
        turboRpm > 80000 -> colors.gaugeGreen
        turboRpm > 30000 -> colors.gaugeCyan
        else -> colors.textSecondary
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Speed,
                contentDescription = null,
                tint = turboColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.turbo_rpm),
                fontSize = 10.sp,
                color = colors.textDim
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (turboRpm > 0) "%,d".format(turboRpm.toInt()) else "—",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = turboColor
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
                    .fillMaxWidth(normalized)
                    .background(turboColor)
            )
        }
    }
}

@Composable
private fun egtTemperatureDisplay(
    egtCelsius: Double,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val egtColor = when {
        egtCelsius > 850 -> colors.gaugeRed
        egtCelsius > 750 -> colors.gaugeOrange
        egtCelsius > 600 -> colors.gaugeGreen
        egtCelsius > 300 -> colors.gaugeCyan
        else -> colors.textSecondary
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                tint = egtColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "EGT",
                fontSize = 10.sp,
                color = colors.textDim
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (egtCelsius > -40) "%.0f°C".format(egtCelsius) else "—",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = egtColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = when {
                egtCelsius > 850 -> "Kritisch!"
                egtCelsius > 750 -> "Hoch"
                egtCelsius > 600 -> "Normal"
                egtCelsius > 300 -> "Kalt"
                else -> "—"
            },
            fontSize = 9.sp,
            color = egtColor
        )
    }
}

@Composable
private fun TurboEfficiencyIndicator(
    inletTemp: Double,
    outletTemp: Double,
    chargeAirTemp: Double,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val efficiency = remember(inletTemp, outletTemp, chargeAirTemp) {
        if (outletTemp > inletTemp && inletTemp > 0 && outletTemp > 0) {
            val cooling = outletTemp - chargeAirTemp
            when {
                cooling > 25 -> 100
                cooling > 20 -> 80
                cooling > 15 -> 60
                cooling > 10 -> 40
                else -> 20
            }
        } else 50
    }

    val effColor = when {
        efficiency > 80 -> colors.gaugeGreen
        efficiency > 60 -> colors.gaugeYellow
        else -> colors.gaugeOrange
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.turbo_intercooler_eff),
            fontSize = 10.sp,
            color = colors.textDim
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$efficiency%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = effColor
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
                    .fillMaxWidth((efficiency / 100f).toFloat())
                    .background(effColor)
            )
        }
    }
}

/**
 * Berechnet den erweiterten Turbo-Gesundheitsscore (0-100)
 * Kalibriert für BorgWarner KP39 / A14NET
 */
private fun calculateTurboDetailHealthScore(
    turboData: TurboData,
    carProfile: CarProfile
): Int {
    var score = 100

    if (turboData.overboostActive) score -= 25
    if (turboData.underboostDetected) score -= 35

    if (turboData.boostTarget > 0) {
        val deviation = abs(turboData.boostPressure - turboData.boostTarget)
        when {
            deviation > 0.3 -> score -= 20
            deviation > 0.2 -> score -= 12
            deviation > 0.15 -> score -= 8
            deviation > 0.1 -> score -= 4
        }
    }

    val wgd = turboData.wastegateDutyCycle
    when {
        wgd > 98 || wgd < 2 -> score -= 25
        wgd > 95 || wgd < 10 -> score -= 15
    }

    when {
        turboData.chargeAirCoolerTemp > 70 -> score -= 12
        turboData.chargeAirCoolerTemp > 65 -> score -= 8
        turboData.chargeAirCoolerTemp > 55 -> score -= 4
    }

    return score.coerceIn(0, 100)
}

private fun getTurboDetailHealthColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF22C55E)
        score >= 75 -> Color(0xFF84CC16)
        score >= 60 -> Color(0xFFFBBF24)
        score >= 40 -> Color(0xFFF97316)
        else -> Color(0xFFEF4444)
    }
}

private fun getTurboDetailHealthLabel(score: Int): String = when {
    score >= 90 -> "Ausgezeichnet"
    score >= 75 -> "Gut"
    score >= 60 -> "Befriedigend"
    score >= 40 -> "Warnung"
    else -> "Kritisch"
}

private fun getTurboDetailHealthDescription(score: Int): String = when {
    score >= 90 -> "Turbolader in einwandfreiem Zustand"
    score >= 75 -> "Leichte Verschleißerscheinungen"
    score >= 60 -> "Überwachung empfohlen"
    score >= 40 -> "Prüfung erforderlich"
    else -> "Sofort prüfen!"
}

private fun getTurboHealthTrend(score: Int): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        score >= 80 -> Icons.Filled.TrendingUp
        score >= 50 -> Icons.Filled.TrendingFlat
        else -> Icons.Filled.TrendingDown
    }
}
