package com.canopobd.ui.tpms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.LocalAppColors
import com.canopobd.ui.theme.AppColors
import kotlinx.coroutines.delay

data class TireData(
    val position: String,
    val pressure: Float = 0f,
    val temperature: Int = 0,
    val isLow: Boolean = false,
    val isHigh: Boolean = false,
    val sensorBattery: Int = 100,
    val isNok: Boolean = false
)

@Composable
fun TPMSDialog(
    onTPMSReset: () -> Unit,
    onDismiss: () -> Unit,
    isConnected: Boolean = true,
    tireData: List<TireData>? = null
) {
    val colors = LocalAppColors.current
    var isResetting by remember { mutableStateOf(false) }
    var resetProgress by remember { mutableStateOf(0f) }
    var lastResetTime by remember { mutableStateOf<Long?>(null) }

    val displayTires = tireData ?: listOf(
        TireData("Vorne Links", 230f, 25, false, false, 100),
        TireData("Vorne Rechts", 228f, 26, false, false, 98),
        TireData("Hinten Links", 232f, 24, false, false, 95),
        TireData("Hinten Rechts", 235f, 25, false, false, 100)
    )

    val avgPressure = displayTires.filter { it.pressure > 0 }.map { it.pressure }.average().toFloat()
    val maxPressure = displayTires.maxOfOrNull { it.pressure } ?: 0f
    val minPressure = displayTires.filter { it.pressure > 0 }.minOfOrNull { it.pressure } ?: 0f
    val pressureDiff = maxPressure - minPressure

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TireRepair,
                        contentDescription = null,
                        tint = colors.gaugeGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Reifendruck (TPMS)", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Durchschnittsdruck",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "${avgPressure.toInt()}",
                                color = colors.textPrimary,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                " kPa",
                                color = colors.textSecondary,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            "≈ ${(avgPressure / 6.895f).toInt()} PSI | ${(avgPressure / 100f).format(1)} bar",
                            color = colors.textDim,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        "Min", "${minPressure.toInt()}", "kPa",
                        colors.gaugeCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        "Max", "${maxPressure.toInt()}", "kPa",
                        colors.gaugeOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        "Diff", "${pressureDiff.toInt()}", "kPa",
                        if (pressureDiff > 30) colors.gaugeOrange else colors.gaugeGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    "Reifendruck",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TireCard(
                            tire = displayTires.getOrElse(0) { TireData("Vorne Links") },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                        TireCard(
                            tire = displayTires.getOrElse(1) { TireData("Vorne Rechts") },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TireCard(
                            tire = displayTires.getOrElse(2) { TireData("Hinten Links") },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                        TireCard(
                            tire = displayTires.getOrElse(3) { TireData("Hinten Rechts") },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (lastResetTime != null) {
                    val timeSinceReset = (System.currentTimeMillis() - lastResetTime!!) / 1000
                    Text(
                        "Letzter Reset: vor ${formatTimeSinceReset(timeSinceReset)}",
                        color = colors.textDim,
                        fontSize = 10.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        isResetting = true
                        onTPMSReset()
                    },
                    enabled = !isResetting && isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isResetting) {
                        CircularProgressIndicator(
                            progress = resetProgress,
                            modifier = Modifier.size(20.dp),
                            color = colors.textPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TPMS wird neu gelernt...")
                    } else {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TPMS neu lernen")
                    }
                }

                LaunchedEffect(isResetting) {
                    if (isResetting) {
                        repeat(100) { i ->
                            delay(50)
                            resetProgress = i / 100f
                        }
                        isResetting = false
                        resetProgress = 0f
                        lastResetTime = System.currentTimeMillis()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.gaugeCyan.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = colors.gaugeCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Nach Reifenwechsel oder Druckanpassung TPMS neu lernen aktivieren.",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schliessen", color = colors.accent)
            }
        }
    )
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = colors.textDim, fontSize = 10.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(unit, color = colors.textDim, fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

@Composable
private fun TireCard(
    tire: TireData,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        when {
            tire.isNok -> colors.gaugeRed
            tire.isLow -> colors.gaugeOrange
            tire.isHigh -> colors.gaugeYellow
            tire.pressure > 0 -> colors.gaugeGreen
            else -> colors.textDim
        },
        label = "tireStatus"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (tire.pressure > 0) "${tire.pressure.toInt()}" else "--",
                            color = statusColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "kPa",
                            color = colors.textDim,
                            fontSize = 10.sp
                        )
                    }
                }

                if (tire.isLow || tire.isNok) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Warnung",
                        tint = colors.gaugeOrange,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                tire.position,
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Thermostat,
                        contentDescription = null,
                        tint = colors.textDim,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        "${tire.temperature}°",
                        color = colors.textDim,
                        fontSize = 10.sp
                    )
                }
                if (tire.sensorBattery < 100) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.BatteryAlert,
                            contentDescription = null,
                            tint = colors.gaugeOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "${tire.sensorBattery}%",
                            color = colors.gaugeOrange,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

private fun Float.format(decimals: Int) = "%.${decimals}f".format(this)

private fun formatTimeSinceReset(seconds: Long): String {
    return when {
        seconds < 60 -> "wenigen Sekunden"
        seconds < 3600 -> "${seconds / 60} Minuten"
        else -> "${seconds / 3600} Stunden"
    }
}
