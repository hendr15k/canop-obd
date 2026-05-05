package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.OilData
import com.canopobd.data.model.TurboData
import com.canopobd.data.model.CarProfile
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors
import kotlin.math.abs
import kotlin.math.min

/**
 * TurboHealthCard - Komponente für das Dashboard
 * Zeigt Ladedruck (Soll/Ist), Wastegate %, Turbotemperaturen, Health Score
 * mit Farbcodierung und Alarmen bei kritischen Werten
 * 
 * Speziell kalibriert für BorgWarner KP39 beim A14NET Motor:
 * - Normaler Ladedruck: 0.6 - 0.7 bar
 * - Overboost: bis 1.2 - 1.3 bar (max 10s)
 * - Wastegate: 80-95% im Leerlauf, 25-60% bei Vollast
 * - Öltemperatur: 90-110°C optimal, max 120°C
 * - Ladelufttemperatur: max 65°C
 */
@Composable
fun TurboHealthCard(
    turboData: TurboData,
    oilData: OilData,
    carProfile: CarProfile,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val healthScore = calculateTurboHealthScore(turboData, oilData, carProfile)
    val healthColor = getHealthColor(healthScore)
    val healthLabel = getHealthLabel(healthScore)
    val healthDescription = getHealthDescription(healthScore, turboData)
    
    val isCritical = healthScore < 50
    val isWarning = healthScore in 50..79
    
    // Pulsing animation for critical state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
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
                        text = stringResource(R.string.turbo_health),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                
                // Health Score Badge
                HealthScoreBadge(
                    score = healthScore,
                    color = animatedHealthColor,
                    colors = colors
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Boost Section
            BoostSection(
                turboData = turboData,
                carProfile = carProfile,
                colors = colors
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Wastegate and Temperatures Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wastegate Section
                WastegateSection(
                    wastegateDuty = turboData.wastegateDutyCycle,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                
                // Temperatures Section
                TemperaturesSection(
                    turboData = turboData,
                    oilData = oilData,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Health Status Bar
            Spacer(modifier = Modifier.height(12.dp))
            HealthStatusBar(
                score = healthScore,
                color = animatedHealthColor,
                label = healthLabel,
                description = healthDescription,
                colors = colors
            )
            
            // Critical Warnings
            if (isCritical || isWarning) {
                Spacer(modifier = Modifier.height(8.dp))
                CriticalWarnings(
                    turboData = turboData,
                    oilData = oilData,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun HealthScoreBadge(
    score: Int,
    color: Color,
    colors: AppColors
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = score.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "/100",
                fontSize = 12.sp,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BoostSection(
    turboData: TurboData,
    carProfile: CarProfile,
    colors: AppColors
) {
    val boostActual = turboData.boostPressure
    val boostTarget = turboData.boostTarget
    val deviation = if (boostTarget > 0) boostActual - boostTarget else 0.0
    val maxGauge = carProfile.maxBoostGaugeBar
    
    val boostColor = when {
        turboData.overboostActive -> colors.gaugeOrange
        turboData.underboostDetected -> colors.gaugeRed
        boostActual > carProfile.normalBoostBar + 0.15 -> colors.gaugeOrange
        boostActual >= carProfile.normalBoostBar - 0.1 -> colors.gaugeGreen
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
            
            // Status badges
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (turboData.overboostActive) {
                    StatusBadge(
                        text = stringResource(R.string.overboost_active),
                        color = colors.gaugeOrange
                    )
                }
                if (turboData.underboostDetected) {
                    StatusBadge(
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
                Text(
                    text = "%.1f kPa rel.".format((boostActual) * 100),
                    fontSize = 10.sp,
                    color = colors.textDim
                )
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
        
        // Boost gauge bar
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colors.surfaceVariant)
        ) {
            val barWidthPx = constraints.maxWidth.toFloat()
            val normalStart = (carProfile.normalBoostBar - 0.1f) / maxGauge
            val normalEnd = (carProfile.normalBoostBar + 0.15f) / maxGauge
            val normalStartPx = normalStart * barWidthPx
            val normalWidthPx = (normalEnd - normalStart) * barWidthPx
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalStart.coerceAtLeast(0f))
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .offset(x = normalStartPx.toInt().dp)
                    .width(normalWidthPx.toInt().dp)
                    .background(colors.gaugeGreen.copy(alpha = 0.3f))
            )
            
            // Actual boost indicator
            val normalized = (boostActual / maxGauge).toFloat().coerceIn(0f, 1f)
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
            Text("Max", fontSize = 8.sp, color = colors.textDim)
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: Color
) {
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
private fun WastegateSection(
    wastegateDuty: Double,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val wgdColor = when {
        wastegateDuty > 95 || wastegateDuty < 5 -> colors.gaugeRed
        wastegateDuty > 80 -> colors.gaugeOrange
        wastegateDuty < 30 -> colors.gaugeOrange
        else -> colors.gaugeCyan
    }
    
    // Convert duty cycle to position (inverted: 100% duty = WG closed = high position)
    val wgPosition = (100 - wastegateDuty).coerceIn(0.0, 100.0)
    
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                tint = wgdColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.wastegate),
                fontSize = 11.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "%.1f%%".format(wastegateDuty),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = wgdColor
        )
        
        Text(
            text = if (wastegateDuty > 70) "Offen" else if (wastegateDuty < 40) "Geschlossen" else "Teils",
            fontSize = 9.sp,
            color = colors.textDim
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Mini position bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surfaceVariant)
        ) {
            val posNormalized = (wgPosition / 100.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(posNormalized)
                    .background(wgdColor)
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
private fun TemperaturesSection(
    turboData: TurboData,
    oilData: OilData,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val oilTemp = oilData.temperature
    val chargeAirTemp = turboData.chargeAirCoolerTemp
    
    val oilTempColor = when {
        oilTemp > 120 -> colors.gaugeRed
        oilTemp > 110 -> colors.gaugeOrange
        oilTemp > 100 -> colors.gaugeYellow
        oilTemp > 80 -> colors.gaugeGreen
        oilTemp > 40 -> colors.gaugeCyan
        oilTemp > 0 -> colors.textSecondary
        else -> colors.textDim
    }
    
    val chargeTempColor = when {
        chargeAirTemp > 65 -> colors.gaugeRed
        chargeAirTemp > 55 -> colors.gaugeOrange
        chargeAirTemp > 45 -> colors.gaugeYellow
        chargeAirTemp > 20 -> colors.gaugeGreen
        chargeAirTemp > 0 -> colors.textSecondary
        else -> colors.textDim
    }
    
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                tint = colors.gaugeOrange,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.temperatures),
                fontSize = 11.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Oil Temperature
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = oilTempColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Öl:",
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
            Text(
                text = if (oilTemp > -40) "%.0f°C".format(oilTemp) else "—",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = oilTempColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Charge Air Temperature
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Air,
                    contentDescription = null,
                    tint = chargeTempColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ladeluft:",
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
            Text(
                text = if (chargeAirTemp > -40) "%.0f°C".format(chargeAirTemp) else "—",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = chargeTempColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Intercooler efficiency indicator
        if (turboData.turboInletTemp > 0 && chargeAirTemp > 0) {
            val cooling = turboData.turboInletTemp - chargeAirTemp
            val efficiency = when {
                cooling > 25 -> 100
                cooling > 20 -> 80
                cooling > 15 -> 60
                cooling > 10 -> 40
                else -> 20
            }
            val efficiencyColor = when {
                efficiency > 80 -> colors.gaugeGreen
                efficiency > 60 -> colors.gaugeYellow
                else -> colors.gaugeOrange
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ladeluftkühler:",
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$efficiency%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = efficiencyColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${cooling.toInt()}°C Δ)",
                        fontSize = 9.sp,
                        color = colors.textDim
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthStatusBar(
    score: Int,
    color: Color,
    label: String,
    description: String,
    colors: AppColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Health bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((score / 100f).toFloat())
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.7f),
                                color
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun CriticalWarnings(
    turboData: TurboData,
    oilData: OilData,
    colors: AppColors
) {
    val warnings = mutableListOf<Pair<String, Color>>()
    
    // Check various conditions
    if (turboData.overboostActive) {
        warnings.add("⚠️ " + stringResource(R.string.overboost_warning) to colors.gaugeOrange)
    }
    if (turboData.underboostDetected) {
        warnings.add("❌ " + stringResource(R.string.underboost_warning) to colors.gaugeRed)
    }
    if (oilData.temperature > 110) {
        warnings.add("🔥 " + stringResource(R.string.oil_temp_high_warning) to colors.gaugeRed)
    }
    if (oilData.temperature > 100 && oilData.temperature <= 110) {
        warnings.add("⚡ " + stringResource(R.string.oil_temp_warm_warning) to colors.gaugeOrange)
    }
    if (turboData.chargeAirCoolerTemp > 55) {
        warnings.add("🌡️ " + stringResource(R.string.charge_air_high_warning) to colors.gaugeOrange)
    }
    if (oilData.consumptionWarning) {
        warnings.add("💧 " + stringResource(R.string.oil_consumption_warning) to colors.gaugeOrange)
    }
    
    if (warnings.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.gaugeRed.copy(alpha = 0.1f)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                warnings.forEach { (text, color) ->
                    Text(
                        text = text,
                        fontSize = 11.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Berechnet den Turbo-Gesundheitsscore (0-100)
 * Kalibriert für BorgWarner KP39 / A14NET
 */
private fun calculateTurboHealthScore(
    turboData: TurboData,
    oilData: OilData,
    carProfile: CarProfile
): Int {
    var score = 100
    
    // Boost-related deductions
    if (turboData.overboostActive) {
        score -= 30 // Overboost is concerning but acceptable for short periods
    }
    
    if (turboData.underboostDetected) {
        score -= 40 // Underboost indicates potential issues
    }
    
    // Boost deviation
    if (turboData.boostTarget > 0) {
        val deviation = abs(turboData.boostPressure - turboData.boostTarget)
        when {
            deviation > 0.3 -> score -= 25
            deviation > 0.2 -> score -= 15
            deviation > 0.15 -> score -= 10
            deviation > 0.1 -> score -= 5
        }
    }
    
    // Wastegate duty cycle analysis
    // KP39: 80-95% at idle (WG open), 25-60% at WOT (WG closed)
    val wgd = turboData.wastegateDutyCycle
    when {
        wgd > 98 || wgd < 2 -> score -= 30 // Stuck position
        wgd > 95 || wgd < 10 -> score -= 20 // Abnormal position
        wgd in 10.0..95.0 -> {
            // Normal range, no deduction
        }
    }
    
    // Oil temperature analysis
    // Optimal: 90-110°C, Max: 120°C
    val oilTemp = oilData.temperature
    when {
        oilTemp > 130 -> score -= 40 // Critical
        oilTemp > 120 -> score -= 30 // Very high
        oilTemp > 110 -> score -= 15 // High
        oilTemp > 100 -> score -= 5 // Slightly high
        oilTemp < 40 && oilTemp > 0 -> score -= 10 // Too cold for proper lubrication
    }
    
    // Charge air temperature
    // Max recommended: 65°C
    when {
        turboData.chargeAirCoolerTemp > 70 -> score -= 15
        turboData.chargeAirCoolerTemp > 65 -> score -= 10
        turboData.chargeAirCoolerTemp > 55 -> score -= 5
    }
    
    // Oil consumption warning
    if (oilData.consumptionWarning) {
        score -= 15
    }
    
    return score.coerceIn(0, 100)
}

/**
 * Gibt die Farbe für den Gesundheitsscore zurück
 */
private fun getHealthColor(score: Int): Color {
    return when {
        score >= 90 -> Color(0xFF22C55E) // Green
        score >= 75 -> Color(0xFF84CC16) // Lime
        score >= 60 -> Color(0xFFFBBF24) // Yellow
        score >= 40 -> Color(0xFFF97316) // Orange
        else -> Color(0xFFEF4444) // Red
    }
}

/**
 * Gibt das Label für den Gesundheitsscore zurück
 */
private fun getHealthLabel(score: Int): String {
    return when {
        score >= 90 -> "Ausgezeichnet"
        score >= 75 -> "Gut"
        score >= 60 -> "Befriedigend"
        score >= 40 -> "Warnung"
        else -> "Kritisch"
    }
}

/**
 * Gibt die Beschreibung für den Gesundheitsscore zurück
 */
private fun getHealthDescription(score: Int, turboData: TurboData): String {
    return when {
        score >= 90 -> "Turbolader funktioniert einwandfrei"
        score >= 75 -> "Leichte Abweichungen, normaler Verschleiß"
        score >= 60 -> "Überwachung empfohlen"
        score >= 40 -> "Prüfung erforderlich - Ursache klären"
        else -> "Sofort prüfen - Motorschaden möglich!"
    }
}

/**
 * Compact version of TurboHealthCard for smaller displays
 */
@Composable
fun TurboHealthCardCompact(
    turboData: TurboData,
    oilData: OilData,
    carProfile: CarProfile,
    modifier: Modifier = Modifier,
    colors: AppColors = LocalAppColors.current
) {
    val healthScore = calculateTurboHealthScore(turboData, oilData, carProfile)
    val healthColor = getHealthColor(healthScore)
    
    val isCritical = healthScore < 50
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_compact")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_compact"
    )
    
    Surface(
        modifier = modifier
            .then(
                if (isCritical) {
                    Modifier.border(
                        width = 2.dp,
                        color = colors.gaugeRed.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Turbo icon and boost
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Air,
                    contentDescription = null,
                    tint = healthColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = "%.2f bar".format(turboData.boostPressure),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "WG: %.0f%%".format(turboData.wastegateDutyCycle),
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }
            
            // Health score
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(healthColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$healthScore",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = healthColor
                )
            }
            
            // Temperatures
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (oilData.temperature > 0) "%.0f°C".format(oilData.temperature) else "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        oilData.temperature > 110 -> colors.gaugeRed
                        oilData.temperature > 100 -> colors.gaugeOrange
                        oilData.temperature > 80 -> colors.gaugeGreen
                        else -> colors.textSecondary
                    }
                )
                Text(
                    text = "Öl",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
            }
        }
    }
}
