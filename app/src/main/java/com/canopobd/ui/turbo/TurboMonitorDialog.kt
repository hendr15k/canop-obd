package com.canopobd.ui.turbo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.CarProfile
import com.canopobd.data.model.OilData
import com.canopobd.data.model.TurboData
import com.canopobd.ui.theme.LocalAppColors
import kotlin.math.abs

@Composable
fun TurboMonitorDialog(
    turboData: TurboData,
    oilData: OilData,
    carProfile: CarProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Air,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.turbo_monitor_title),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    VehicleInfoCard(carProfile = carProfile, colors = colors)
                }
                item {
                    BoostPressureCard(
                        turboData = turboData,
                        carProfile = carProfile,
                        colors = colors
                    )
                }
                item {
                    WastegateCard(
                        turboData = turboData,
                        colors = colors
                    )
                }
                item {
                    TurboTemperaturesCard(
                        turboData = turboData,
                        colors = colors
                    )
                }
                item {
                    OilCard(
                        oilData = oilData,
                        colors = colors
                    )
                }
                item {
                    TurboHealthCard(
                        turboData = turboData,
                        colors = colors
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = colors.accent)
            }
        }
    )
}

@Composable
private fun VehicleInfoCard(carProfile: CarProfile, colors: com.canopobd.ui.theme.AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = carProfile.displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.highlight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${carProfile.engineCode} • ${carProfile.displacement}",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Text(
                text = carProfile.power,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Text(
                text = "${stringResource(R.string.turbo_type)}: ${carProfile.turboType}",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Text(
                text = "${stringResource(R.string.ecu)}: ${carProfile.ecuType}",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun BoostPressureCard(
    turboData: TurboData,
    carProfile: CarProfile,
    colors: com.canopobd.ui.theme.AppColors
) {
    val boostBar = turboData.boostPressure
    val targetBar = turboData.boostTarget
    val deviation = if (targetBar > 0) boostBar - targetBar else 0.0
    val maxGauge = carProfile.maxBoostGaugeBar

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.boost_pressure),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                if (turboData.overboostActive) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeOrange.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = stringResource(R.string.overboost_active),
                            fontSize = 10.sp,
                            color = colors.gaugeOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (turboData.underboostDetected) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeRed.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = stringResource(R.string.underboost_detected),
                            fontSize = 10.sp,
                            color = colors.gaugeRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "%.2f bar".format(boostBar),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = boostColor(boostBar, carProfile.normalBoostBar, colors)
                    )
                    Text(
                        text = "%.1f kPa".format((boostBar + 1.0) * 100),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${stringResource(R.string.target)}: %.2f bar".format(targetBar),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "${stringResource(R.string.deviation)}: %+.2f bar".format(deviation),
                        fontSize = 11.sp,
                        color = if (abs(deviation) > 0.15) colors.gaugeOrange else colors.gaugeGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surface)
            ) {
                val normalized = (boostBar / maxGauge).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(normalized)
                        .background(boostColor(boostBar, carProfile.normalBoostBar, colors))
                )
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0f, carProfile.normalBoostBar / maxGauge, carProfile.overboostBar / maxGauge, 1f).forEach { _ ->
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(colors.textDim.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.0", fontSize = 9.sp, color = colors.textDim)
                Text("Normal", fontSize = 9.sp, color = colors.gaugeGreen)
                Text("Overboost", fontSize = 9.sp, color = colors.gaugeOrange)
                Text("Max", fontSize = 9.sp, color = colors.textDim)
            }
        }
    }
}

@Composable
private fun WastegateCard(turboData: TurboData, colors: com.canopobd.ui.theme.AppColors) {
    val wgd = turboData.wastegateDutyCycle

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.wastegate_control),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "%.1f %%".format(wgd),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = stringResource(R.string.wastegate_hint),
                fontSize = 10.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surface)
            ) {
                val normalized = (wgd / 100f).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(normalized)
                        .background(colors.gaugeCyan)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0% (offen)", fontSize = 9.sp, color = colors.textDim)
                Text("50%", fontSize = 9.sp, color = colors.textDim)
                Text("100% (geschlossen)", fontSize = 9.sp, color = colors.textDim)
            }
        }
    }
}

@Composable
private fun TurboTemperaturesCard(
    turboData: TurboData,
    colors: com.canopobd.ui.theme.AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.turbo_temperatures),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TempItem(
                    label = stringResource(R.string.turbo_inlet),
                    temp = turboData.turboInletTemp,
                    unit = "°C",
                    colors = colors
                )
                TempItem(
                    label = stringResource(R.string.turbo_outlet),
                    temp = turboData.turboOutletTemp,
                    unit = "°C",
                    colors = colors
                )
                TempItem(
                    label = stringResource(R.string.charge_air_cooler),
                    temp = turboData.chargeAirCoolerTemp,
                    unit = "°C",
                    colors = colors
                )
            }

            if (turboData.chargeAirCoolerTemp > 0) {
                val iat = turboData.turboInletTemp
                val cac = turboData.chargeAirCoolerTemp
                if (iat > 0 && cac > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val cooling = iat - cac
                    Text(
                        text = "${stringResource(R.string.intercooler_efficiency)}: ${stringResource(R.string.temp_drop, cooling.toInt())}",
                        fontSize = 10.sp,
                        color = if (cooling > 20) colors.gaugeGreen else colors.gaugeOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun TempItem(
    label: String,
    temp: Double,
    unit: String,
    colors: com.canopobd.ui.theme.AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (temp > -40) "%.0f%s".format(temp, unit) else "—",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = tempColor(temp, colors)
        )
        Text(text = label, fontSize = 10.sp, color = colors.textSecondary)
    }
}

@Composable
private fun OilCard(oilData: OilData, colors: com.canopobd.ui.theme.AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.oil_status),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                if (oilData.consumptionWarning) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeOrange.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = stringResource(R.string.oil_consumption_warning),
                            fontSize = 10.sp,
                            color = colors.gaugeOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (oilData.temperature > -40) "%.0f°C".format(oilData.temperature) else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = tempColor(oilData.temperature, colors)
                    )
                    Text(stringResource(R.string.oil_temp), fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (oilData.pressure > 0) "%.1f bar".format(oilData.pressure) else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(stringResource(R.string.oil_pressure), fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${oilData.oilLifeRemaining}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (oilData.oilLifeRemaining > 20) colors.gaugeGreen else colors.gaugeOrange
                    )
                    Text(stringResource(R.string.oil_life), fontSize = 10.sp, color = colors.textSecondary)
                }
            }

            if (oilData.consumptionRateL1000km > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.oil_consumption_rate)}: %.2f L/1000km".format(oilData.consumptionRateL1000km),
                    fontSize = 10.sp,
                    color = if (oilData.consumptionRateL1000km > 0.5) colors.gaugeOrange else colors.gaugeGreen
                )
            }
        }
    }
}

@Composable
private fun TurboHealthCard(turboData: TurboData, colors: com.canopobd.ui.theme.AppColors) {
    val score = turboData.turboHealthScore
    val scoreColor = when {
        score >= 80 -> colors.gaugeGreen
        score >= 50 -> colors.gaugeOrange
        else -> colors.gaugeRed
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.turbo_health),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "$score / 100",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((score / 100f).toFloat())
                        .background(scoreColor)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = healthMessage(score),
                fontSize = 10.sp,
                color = scoreColor
            )
        }
    }
}

private fun boostColor(boost: Double, normal: Float, colors: com.canopobd.ui.theme.AppColors): Color {
    return when {
        boost > normal + 0.2 -> colors.gaugeOrange
        boost >= normal - 0.1 -> colors.gaugeGreen
        boost > 0.1 -> colors.gaugeYellow
        else -> colors.textSecondary
    }
}

private fun tempColor(temp: Double, colors: com.canopobd.ui.theme.AppColors): Color {
    return when {
        temp > 120 -> colors.gaugeRed
        temp > 100 -> colors.gaugeOrange
        temp > 80 -> colors.gaugeYellow
        temp > 0 -> colors.gaugeGreen
        else -> colors.textSecondary
    }
}

private fun healthMessage(score: Int): String {
    return when {
        score >= 90 -> "Turbolader in einwandfreiem Zustand"
        score >= 80 -> "Turbolader in gutem Zustand, leichte Verschleißerscheinungen"
        score >= 60 -> "Turbolader zeigt Verschleiß — regelmäßig prüfen"
        score >= 40 -> "Turbolader-Warnung — Ursache klären (Wastegate, DV, Leck)"
        else -> "Sofort prüfen — Turboschaden möglich!"
    }
}
