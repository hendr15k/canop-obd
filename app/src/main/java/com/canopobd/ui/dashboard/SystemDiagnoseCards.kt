package com.canopobd.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.domain.*
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun SAIHealthCard(
    result: SecondaryAirAnalyzer.SAIAnalysis?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val isActive = result?.status?.isActive == true
    val healthScore = result?.healthScore ?: 0
    val color = when {
        result == null -> colors.textSecondary
        isActive && healthScore >= 80 -> colors.gaugeGreen
        healthScore >= 50 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Air,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sekundaerluft",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (result != null) "${healthScore}%" else "--",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Status",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            result == null -> "--"
                            isActive -> "Aktiv"
                            else -> "Inaktiv"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Betriebsdauer",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result?.status?.operationTimeSeconds?.let { "${it}s" } ?: "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Plausibilitaet",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (result?.operationPlausibility) {
                            true -> "OK"
                            false -> "Fehler"
                            else -> "--"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (result?.operationPlausibility) {
                            true -> colors.gaugeGreen
                            false -> colors.gaugeRed
                            else -> colors.textSecondary
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmissionsReadinessCard(
    result: EmissionsReadinessAnalyzer.ReadinessAnalysis?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val percentage = if (result != null) {
        if (result.totalCount > 0) (result.completedCount * 100) / result.totalCount else 0
    } else 0
    val color = when {
        result == null -> colors.textSecondary
        percentage > 80 -> colors.gaugeGreen
        percentage >= 50 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }
    val missingMonitors = result?.monitors
        ?.filter { !it.isComplete && it.isSupported }
        ?.map { it.monitor.label }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Poll,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Emissions-Bereitschaft",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }
                if (result != null) {
                    Text(
                        text = "${result.completedCount}/${result.totalCount}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bereitschaft",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$percentage%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = (percentage / 100f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f),
                )
            }

            if (!missingMonitors.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fehlende Monitore:",
                    fontSize = 10.sp,
                    color = colors.gaugeYellow,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    missingMonitors.take(4).forEach { name ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.gaugeYellow.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = name.take(10),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontSize = 9.sp,
                                color = colors.gaugeYellow,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (missingMonitors.size > 4) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.textDim.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = "+${missingMonitors.size - 4}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontSize = 9.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryAnalyzerCard(
    result: BatteryHealthAnalyzer.BatteryAnalysis?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val healthScore = result?.healthScore ?: 0
    val voltage = result?.status?.voltage ?: 0.0
    val soc = result?.status?.soc ?: -1
    val isCharging = result?.status?.isCharging ?: false
    val color = when {
        result == null -> colors.textSecondary
        healthScore > 70 -> colors.gaugeGreen
        healthScore >= 40 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryFull,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Batterie-Analyse",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (result != null) "${healthScore}%" else "--",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spannung",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (voltage > 0) "%.2fV".format(voltage) else "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ladestand",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (soc >= 0) "$soc%" else "Laden",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            soc < 0 -> colors.gaugeCyan
                            soc > 70 -> colors.gaugeGreen
                            soc >= 40 -> colors.gaugeYellow
                            else -> colors.gaugeRed
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ladesystem",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result?.chargingSystemHealth?.label?.take(8) ?: "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (result?.chargingSystemHealth) {
                            BatteryHealthAnalyzer.ChargingSystemHealth.HEALTHY -> colors.gaugeGreen
                            BatteryHealthAnalyzer.ChargingSystemHealth.WEAK -> colors.gaugeYellow
                            BatteryHealthAnalyzer.ChargingSystemHealth.FAULTY -> colors.gaugeRed
                            else -> colors.textSecondary
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EGRAnalyzerCard(
    result: EGRHealthAnalyzer.EGRAnalysis?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val healthScore = result?.healthScore ?: 0
    val deviation = result?.flowDeviation ?: 0.0
    val flowRate = result?.health?.flowRate ?: 0.0
    val color = when {
        result == null -> colors.textSecondary
        healthScore > 70 -> colors.gaugeGreen
        healthScore >= 40 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Air,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EGR-Analyse",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (result != null) "${healthScore}%" else "--",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Abweichung",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (deviation != 0.0) "%+.1f%%".format(deviation) else "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            kotlin.math.abs(deviation) > 20 -> colors.gaugeRed
                            kotlin.math.abs(deviation) > 10 -> colors.gaugeYellow
                            else -> color
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Durchfluss",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (flowRate > 0) "%.1f%%".format(flowRate) else "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Temperatur",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (result?.temperaturePlausibility == true) "OK" else "Fehler",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (result?.temperaturePlausibility) {
                            true -> colors.gaugeGreen
                            false -> colors.gaugeRed
                            else -> colors.textSecondary
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EVAPAnalyzerCard(
    result: EVAPSystemAnalyzer.EVAPAnalysis?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val healthScore = result?.healthScore ?: 0
    val purgeEfficiency = result?.purgeEfficiency ?: 0.0
    val pressureDeviation = result?.pressureDeviation ?: 0.0
    val color = when {
        result == null -> colors.textSecondary
        healthScore > 70 -> colors.gaugeGreen
        healthScore >= 40 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalGasStation,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EVAP-Analyse",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (result != null) "${healthScore}%" else "--",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Purge-Effizienz",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (purgeEfficiency > 0) "%.0f%%".format(purgeEfficiency) else "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            purgeEfficiency > 50 -> colors.gaugeOrange
                            purgeEfficiency > 0 -> colors.gaugeGreen
                            else -> colors.textSecondary
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Druck-Abw.",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (pressureDeviation > 0) "%.0f".format(pressureDeviation) else "--",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            pressureDeviation > 1000 -> colors.gaugeRed
                            pressureDeviation > 300 -> colors.gaugeYellow
                            else -> color
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Leck Status",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            result == null -> "--"
                            result.status.hasLeak -> "Leck!"
                            else -> "Dicht"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            result?.status?.hasLeak == true -> colors.gaugeRed
                            result != null -> colors.gaugeGreen
                            else -> colors.textSecondary
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TurboAnalyzerCard(
    spoolResult: TurboSpoolAnalyzer.SpoolAnalysis?,
    efficiencyResult: TurboEfficiencyAnalyzer.TurboEfficiencyAnalysis?,
    boostLeakResult: BoostLeakDetector.BoostLeakAnalysis?,
    wastegateResult: WastegateHealthAnalyzer.WastegateAnalysis?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val spoolHealth = spoolResult?.healthScore ?: 0
    val spoolColor = when {
        spoolResult == null -> colors.textSecondary
        spoolHealth > 70 -> colors.gaugeGreen
        spoolHealth >= 40 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }
    val spoolLabel = when {
        spoolResult == null -> "--"
        spoolHealth > 70 -> "Gut"
        spoolHealth >= 40 -> "OK"
        else -> "Warnung"
    }

    val efficiencyHealth = efficiencyResult?.healthScore ?: 0
    val efficiencyColor = when {
        efficiencyResult == null -> colors.textSecondary
        efficiencyHealth > 70 -> colors.gaugeGreen
        efficiencyHealth >= 40 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }
    val efficiencyLabel = when {
        efficiencyResult == null -> "--"
        efficiencyHealth > 70 -> "Gut"
        efficiencyHealth >= 40 -> "OK"
        else -> "Warnung"
    }

    val leakLabel = when (boostLeakResult?.severity) {
        null -> "--"
        BoostLeakDetector.LeakSeverity.NONE -> "OK"
        BoostLeakDetector.LeakSeverity.MINOR -> "Klein"
        BoostLeakDetector.LeakSeverity.MODERATE -> "Mittel"
        BoostLeakDetector.LeakSeverity.SEVERE -> "Gross!"
        else -> "?"
    }
    val leakColor = when (boostLeakResult?.severity) {
        null -> colors.textSecondary
        BoostLeakDetector.LeakSeverity.NONE -> colors.gaugeGreen
        BoostLeakDetector.LeakSeverity.MINOR -> colors.gaugeYellow
        BoostLeakDetector.LeakSeverity.MODERATE -> colors.gaugeOrange
        BoostLeakDetector.LeakSeverity.SEVERE -> colors.gaugeRed
        else -> colors.textSecondary
    }

    val wastegateHealth = wastegateResult?.healthScore ?: 0
    val wastegateColor = when {
        wastegateResult == null -> colors.textSecondary
        wastegateHealth > 70 -> colors.gaugeGreen
        wastegateHealth >= 40 -> colors.gaugeYellow
        else -> colors.gaugeRed
    }
    val wastegateLabel = when {
        wastegateResult == null -> "--"
        wastegateHealth > 70 -> "Gut"
        wastegateHealth >= 40 -> "OK"
        else -> "Problem"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Speed,
                    contentDescription = null,
                    tint = colors.gaugeOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Turbo-Analyse",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TurboMiniIndicator(
                    icon = Icons.Filled.TrendingUp,
                    title = "Spule",
                    value = spoolLabel,
                    color = spoolColor,
                    modifier = Modifier.weight(1f)
                )
                TurboMiniIndicator(
                    icon = Icons.Filled.ElectricBolt,
                    title = "Effizienz",
                    value = efficiencyLabel,
                    color = efficiencyColor,
                    modifier = Modifier.weight(1f)
                )
                TurboMiniIndicator(
                    icon = Icons.Filled.Air,
                    title = "Leck",
                    value = leakLabel,
                    color = leakColor,
                    modifier = Modifier.weight(1f)
                )
                TurboMiniIndicator(
                    icon = Icons.Filled.Settings,
                    title = "WG",
                    value = wastegateLabel,
                    color = wastegateColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TurboMiniIndicator(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 8.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
