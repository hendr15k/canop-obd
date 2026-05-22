package com.canopobd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.repository.TurboMonitoringData
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun ExtendedPIDViewerCard(
    data: TurboMonitoringData,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Speed,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Erweiterte PID-Daten",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.accent.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Mode $22",
                        color = colors.accent,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PIDValueDisplay(
                    label = "Drehmoment",
                    value = data.engineTorque,
                    unit = "Nm",
                    progress = data.engineTorque?.let { (it / 220.0).coerceIn(0.0, 1.0).toFloat() },
                    progressColor = colors.gaugeYellow,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                PIDValueDisplay(
                    label = "Ladedruck",
                    value = data.relativeBoostBar,
                    unit = "bar",
                    progress = data.relativeBoostBar?.let { (it / 1.5).coerceIn(0.0, 1.0).toFloat() },
                    progressColor = colors.gaugeGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PIDValueDisplay(
                    label = "Wastegate",
                    value = data.wastegateDuty,
                    unit = "%",
                    progress = data.wastegateDuty?.let { (it / 100.0).toFloat() },
                    progressColor = when {
                        data.wastegateDuty > 80 -> colors.gaugeOrange
                        data.wastegateDuty < 30 -> colors.gaugeRed
                        else -> colors.gaugeYellow
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                PIDValueDisplay(
                    label = "Turbo-Drehzahl",
                    value = data.turboSpeed.takeIf { it > 0 },
                    unit = "rpm",
                    displayMultiplier = 0.001,
                    displayUnit = "k rpm",
                    progress = data.turboSpeed.takeIf { it > 0 }?.let { (it / 200000.0).coerceIn(0.0, 1.0).toFloat() },
                    progressColor = colors.accent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PIDValueDisplay(
                    label = "Ladeluft",
                    value = data.chargeAirTemp,
                    unit = "°C",
                    progress = data.chargeAirTemp.takeIf { it > 0 }?.let { ((it + 20) / 80.0).coerceIn(0.0, 1.0).toFloat() },
                    progressColor = when {
                        data.chargeAirTemp > 55 -> colors.gaugeRed
                        data.chargeAirTemp > 40 -> colors.gaugeOrange
                        else -> colors.gaugeCyan
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                PIDValueDisplay(
                    label = "Abweichung",
                    value = data.boostDeviation.takeIf { it != 0.0 },
                    unit = "%",
                    progress = data.boostDeviation.takeIf { it != 0.0 }?.let { kotlin.math.abs(it / 30.0).coerceIn(0.0, 1.0).toFloat() },
                    progressColor = when {
                        data.boostDeviation > 20 -> colors.gaugeRed
                        data.boostDeviation < -20 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    },
                    showSign = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (data.isOverboost || data.isUnderboost) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (data.isOverboost) colors.gaugeRed.copy(alpha = 0.2f)
                    else colors.gaugeOrange.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (data.isOverboost) colors.gaugeRed else colors.gaugeOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (data.isOverboost) "Uberladung erkannt!" else "Unterladung erkannt!",
                            color = if (data.isOverboost) colors.gaugeRed else colors.gaugeOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = colors.surface
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Temperatur-Details",
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TemperatureRow("Turbo-Einlass", data.turboInletTemp, colors)
                        TemperatureRow("Turbo-Auslass", data.turboOutletTemp, colors)
                        TemperatureRow("Ladeluft", data.chargeAirTemp, colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun PIDValueDisplay(
    label: String,
    value: Double?,
    unit: String,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    progressColor: Color = LocalAppColors.current.accent,
    displayMultiplier: Double = 1.0,
    displayUnit: String? = null,
    showSign: Boolean = false
) {
    val colors = LocalAppColors.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = colors.textSecondary,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (value != null) {
                val displayValue = value * displayMultiplier
                val formattedValue = if (showSign && displayValue > 0) {
                    "+%.1f".format(displayValue)
                } else {
                    "%.1f".format(displayValue)
                }
                Text(
                    text = formattedValue,
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = displayUnit ?: unit,
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
            } else {
                Text(
                    text = "--",
                    color = colors.textSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = displayUnit ?: unit,
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
            }

            if (progress != null) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(horizontal = 4.dp),
                    color = progressColor,
                    trackColor = colors.surface,
                )
            }
        }
    }
}

@Composable
private fun TemperatureRow(
    label: String,
    value: Double,
    colors: com.canopobd.ui.theme.AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 9.sp
        )
        Text(
            text = if (value > -40) "%.0f°C".format(value) else "--",
            color = when {
                value > 65 -> colors.gaugeRed
                value > 50 -> colors.gaugeOrange
                value > 0 -> colors.textPrimary
                else -> colors.textSecondary
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
