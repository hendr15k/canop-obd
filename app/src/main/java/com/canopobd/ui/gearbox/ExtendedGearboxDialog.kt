package com.canopobd.ui.gearbox

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

data class GearboxTelemetry(
    val engineRpm: Double = 0.0,
    val vehicleSpeedKmh: Double = 0.0,
    val oilTempCelsius: Double = 0.0,
    val engineLoad: Double = 0.0
)

data class GearResult(
    val gear: Int,
    val ratio: Double,
    val rpmAtSpeed: Double,
    val confidence: Double
)

object M32GearRatios {
    const val FINAL_DRIVE = 4.18
    const val TIRE_CIRCUMFERENCE_M = 1.995

    val gearRatios = mapOf(
        1 to 3.727,
        2 to 2.044,
        3 to 1.357,
        4 to 1.034,
        5 to 0.825,
        6 to 0.667
    )

    fun detectGear(rpm: Double, speedKmh: Double): GearResult {
        if (rpm < 300 || speedKmh < 5) return GearResult(0, 0.0, 0.0, 0.0)

        val speedMs = speedKmh / 3.6
        val wheelRps = speedMs / TIRE_CIRCUMFERENCE_M
        val wheelRpm = wheelRps * 60.0

        var bestGear = 0
        var bestRatio = 0.0
        var bestDiff = Double.MAX_VALUE
        var bestConfidence = 0.0

        for ((gear, ratio) in gearRatios) {
            val expectedRpm = wheelRpm * ratio * FINAL_DRIVE
            val diff = kotlin.math.abs(rpm - expectedRpm)
            val tolerance = expectedRpm * 0.12

            if (diff < bestDiff && diff < tolerance) {
                bestDiff = diff
                bestGear = gear
                bestRatio = ratio
                bestConfidence = ((1.0 - diff / tolerance) * 100.0).coerceIn(0.0, 100.0)
            }
        }

        return GearResult(bestGear, bestRatio, wheelRpm * bestRatio * FINAL_DRIVE, bestConfidence)
    }

    fun rpmForGearAtSpeed(gear: Int, speedKmh: Double): Double {
        val ratio = gearRatios[gear] ?: return 0.0
        val speedMs = speedKmh / 3.6
        val wheelRps = speedMs / TIRE_CIRCUMFERENCE_M
        return wheelRps * 60.0 * ratio * FINAL_DRIVE
    }

    fun recommendedShiftRpm(gear: Int, load: Double): Int {
        val baseRpm = when (gear) {
            1 -> 3500
            2 -> 3800
            3 -> 4000
            4 -> 4200
            5 -> 4500
            else -> 0
        }
        val loadAdjustment = (load * 0.15).toInt()
        return (baseRpm + loadAdjustment).coerceIn(2500, 6000)
    }
}

@Composable
fun ExtendedGearboxDialog(
    telemetry: GearboxTelemetry,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val gearResult = remember(telemetry.engineRpm, telemetry.vehicleSpeedKmh) {
        M32GearRatios.detectGear(telemetry.engineRpm, telemetry.vehicleSpeedKmh)
    }

    val animatedRpm by animateFloatAsState(
        targetValue = telemetry.engineRpm.toFloat(),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rpm"
    )

    val shiftRpm = remember(gearResult.gear, telemetry.engineLoad) {
        M32GearRatios.recommendedShiftRpm(gearResult.gear, telemetry.engineLoad)
    }

    val shouldShift = telemetry.engineRpm >= shiftRpm && gearResult.gear > 0 && gearResult.gear < 6

    val gearColor by animateColorAsState(
        targetValue = when {
            telemetry.engineRpm >= 6000 -> Color(0xFFFF4444)
            telemetry.engineRpm >= 5500 -> Color(0xFFFF8C00)
            telemetry.engineRpm >= 4500 -> Color(0xFFFFE066)
            shouldShift -> Color(0xFFFFE066)
            else -> Color(0xFF44FF88)
        },
        animationSpec = tween(300),
        label = "gearColor"
    )

    val oilTempColor by animateColorAsState(
        targetValue = when {
            telemetry.oilTempCelsius > 100 -> Color(0xFFFF4444)
            telemetry.oilTempCelsius > 80 -> Color(0xFF44FF88)
            telemetry.oilTempCelsius > 0 -> Color(0xFF42A5F5)
            else -> Color(0xFF666666)
        },
        animationSpec = tween(300),
        label = "oilTemp"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Getriebe M32", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GearDisplay(
                        gear = gearResult.gear,
                        gearColor = gearColor,
                        shouldShift = shouldShift,
                        colors = colors
                    )
                }

                item {
                    RpmGearBar(
                        currentRpm = animatedRpm,
                        redlineRpm = 6500f,
                        shiftRpm = shiftRpm.toFloat(),
                        colors = colors
                    )
                }

                item {
                    GearboxInfoCard(
                        gearResult = gearResult,
                        telemetry = telemetry,
                        shiftRpm = shiftRpm,
                        shouldShift = shouldShift,
                        colors = colors
                    )
                }

                item {
                    OilTempCard(
                        tempCelsius = telemetry.oilTempCelsius,
                        tempColor = oilTempColor,
                        colors = colors
                    )
                }

                item {
                    Text(
                        "RPM / Gang Tabelle (bei 100 km/h)",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                itemsIndexed(M32GearRatios.gearRatios.keys.toList()) { _, gear ->
                    val rpm = M32GearRatios.rpmForGearAtSpeed(gear, 100.0)
                    val isActive = gear == gearResult.gear
                    GearRpmRow(
                        gear = gear,
                        rpm = rpm,
                        isActive = isActive,
                        colors = colors
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun GearDisplay(
    gear: Int,
    gearColor: Color,
    shouldShift: Boolean,
    colors: AppColors
) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldShift) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceCard),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (gear > 0) "$gear" else "N",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = gearColor.copy(alpha = pulseAlpha)
            )
            if (shouldShift) {
                Text(
                    text = "SCHALTEN!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF4444).copy(alpha = pulseAlpha)
                )
            }
        }
    }
}

@Composable
private fun RpmGearBar(
    currentRpm: Float,
    redlineRpm: Float,
    shiftRpm: Float,
    colors: AppColors
) {
    val progress = (currentRpm / redlineRpm).coerceIn(0f, 1f)
    val barColor = when {
        currentRpm >= redlineRpm -> Color(0xFFFF4444)
        currentRpm >= shiftRpm -> Color(0xFFFF8C00)
        currentRpm >= redlineRpm * 0.7f -> Color(0xFFFFE066)
        else -> Color(0xFF44FF88)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("RPM: ${currentRpm.toInt()}", color = colors.textSecondary, fontSize = 12.sp)
            Text("Schalten: ${shiftRpm}", color = colors.gaugeOrange, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF44FF88), barColor)
                        )
                    )
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun GearboxInfoCard(
    gearResult: GearResult,
    telemetry: GearboxTelemetry,
    shiftRpm: Int,
    shouldShift: Boolean,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Gang", if (gearResult.gear > 0) "${gearResult.gear}. Gang" else "Leerlauf", colors)
                InfoItem("Uebersetzung", if (gearResult.ratio > 0) "%.3f".format(gearResult.ratio) else "-", colors)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Konfidenz", "%.0f%%".format(gearResult.confidence), colors)
                InfoItem("Last", "%.0f%%".format(telemetry.engineLoad), colors)
            }
            if (shouldShift) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Empfehlung: In ${gearResult.gear + 1}. Gang schalten",
                    color = colors.gaugeOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OilTempCard(
    tempCelsius: Double,
    tempColor: Color,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                contentDescription = null,
                tint = tempColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Getriebeöl Temperatur", color = colors.textSecondary, fontSize = 12.sp)
                Text(
                    text = if (tempCelsius > 0) "%.1f°C".format(tempCelsius) else "N/A",
                    color = tempColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            when {
                tempCelsius > 100 -> Text("KRITISCH", color = Color(0xFFFF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                tempCelsius > 80 -> Text("WARNUNG", color = Color(0xFFFF8C00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                tempCelsius > 0 -> Text("OK", color = Color(0xFF44FF88), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun GearRpmRow(
    gear: Int,
    rpm: Double,
    isActive: Boolean,
    colors: AppColors
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) colors.accent.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(300),
        label = "bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$gear. Gang",
            color = if (isActive) colors.accent else colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = "%.3f".format(M32GearRatios.gearRatios[gear] ?: 0.0),
            color = colors.textSecondary,
            fontSize = 13.sp
        )
        Text(
            text = "${rpm.toInt()} RPM",
            color = if (isActive) colors.textPrimary else colors.textDim,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String, colors: AppColors) {
    Column {
        Text(label, color = colors.textDim, fontSize = 11.sp)
        Text(value, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
