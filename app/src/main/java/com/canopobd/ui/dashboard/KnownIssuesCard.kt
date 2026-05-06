package com.canopobd.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.model.AstraJ14TurboCalibration
import com.canopobd.data.model.KnownIssue
import com.canopobd.data.model.ProblemMileageMap
import com.canopobd.ui.theme.*

@Composable
fun KnownIssuesCard(
    currentKm: Int,
    modifier: Modifier = Modifier
) {
    val issues = AstraJ14TurboCalibration.KNOWN_ISSUES
    val mileageMap = AstraJ14TurboCalibration.PROBLEM_MILEAGE_MAP

    val overdue = mileageMap.filter { currentKm >= it.typicalRangeEndKm }
        .sortedByDescending { currentKm - it.typicalRangeEndKm }
        .take(2)

    val upcoming = mileageMap.filter { currentKm < it.typicalRangeEndKm }
        .sortedBy { it.typicalRangeStartKm - currentKm }
        .take(5)

    val critical = upcoming.filter { it.typicalRangeStartKm - currentKm < 20000 }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceCard),
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
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = when {
                            overdue.isNotEmpty() -> gaugeRed
                            critical.isNotEmpty() -> gaugeOrange
                            else -> gaugeYellow
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bekannte Probleme",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${overdue.size + upcoming.size} Einträge",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                    if (overdue.isNotEmpty()) {
                        Text(
                            text = "${overdue.size} überfällig!",
                            fontSize = 10.sp,
                            color = gaugeRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (overdue.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                overdue.forEach { item ->
                    OverdueItem(item, currentKm)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (critical.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bald fällig:",
                    fontSize = 11.sp,
                    color = gaugeYellow,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                critical.take(2).forEach { item ->
                    MileageProgressItem(item, currentKm, isWarning = true)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (upcoming.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(upcoming.take(5)) { item ->
                        CompactIssueChip(item, currentKm)
                    }
                }
            }

            if (issues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Häufige Probleme beim A14NET:",
                    fontSize = 11.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(issues.take(3)) { issue ->
                        KnownIssueChip(issue)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverdueItem(item: ProblemMileageMap, currentKm: Int) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = gaugeRed.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = gaugeRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = item.component,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeRed
                )
                Text(
                    text = "${item.typicalRangeEndKm - currentKm} km überfällig · ${item.description.take(40)}",
                    fontSize = 10.sp,
                    color = gaugeRed.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MileageProgressItem(item: ProblemMileageMap, currentKm: Int, isWarning: Boolean) {
    val progress = ((currentKm.toFloat() - item.typicalRangeStartKm) / (item.typicalRangeEndKm - item.typicalRangeStartKm).toFloat()).coerceIn(0f, 1f)
    val color = if (isWarning) gaugeYellow else gaugeOrange
    val remaining = item.typicalRangeEndKm - currentKm

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.component,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
            Text(
                text = if (remaining > 0) "~${remaining / 1000}k km" else "Überfällig",
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun CompactIssueChip(item: ProblemMileageMap, currentKm: Int) {
    val progress = ((currentKm.toFloat() - item.typicalRangeStartKm) / (item.typicalRangeEndKm - item.typicalRangeStartKm).toFloat()).coerceIn(0f, 1f)
    val color = when {
        progress > 0.8f -> gaugeRed
        progress > 0.6f -> gaugeOrange
        else -> gaugeYellow
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.component.take(14),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color = color,
                trackColor = color.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 9.sp,
                color = color
            )
        }
    }
}

@Composable
private fun KnownIssueChip(issue: KnownIssue) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = surfaceCard.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = null,
                    tint = gaugeOrange,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = issue.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Symptome: ${issue.symptoms}",
                    fontSize = 10.sp,
                    color = textSecondary,
                    maxLines = 2
                )
                Text(
                    text = "Tipp: ${issue.prevention}",
                    fontSize = 10.sp,
                    color = gaugeGreen,
                    maxLines = 2
                )
            } else {
                Text(
                    text = issue.symptoms.take(30) + "...",
                    fontSize = 10.sp,
                    color = textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AnalyzerSummaryRow(
    gearboxResult: com.canopobd.data.domain.M32GearboxMonitor.GearboxAnalysis?,
    chainTensionerResult: com.canopobd.data.domain.ChainTensionerAnalyzer.ChainTensionerAnalysis?,
    coolantResult: com.canopobd.data.domain.CoolantSystemHealth.CoolantAnalysis?,
    oilConditionResult: com.canopobd.data.domain.OilConditionMonitor.OilAnalysis?,
    pcvResult: com.canopobd.data.domain.PCVMonitor.PCVAnalysis?,
    lambdaResult: com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaBalance?,
    fuelConsumption: com.canopobd.data.domain.FuelConsumptionAnalyzer.FuelConsumptionData?,
    egtResult: com.canopobd.data.domain.EGTMonitor.EGTAnalysis?,
    sensorHealthSummary: com.canopobd.data.domain.SensorHealthMonitor.SensorHealthSummary?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GearboxMiniCard(result = gearboxResult, modifier = Modifier.weight(1f))
        ChainMiniCard(result = chainTensionerResult, modifier = Modifier.weight(1f))
        CoolantMiniCard(result = coolantResult, modifier = Modifier.weight(1f))
        OilMiniCard(result = oilConditionResult, modifier = Modifier.weight(1f))
        PCVMiniCard(result = pcvResult, modifier = Modifier.weight(1f))
        LambdaMiniCard(result = lambdaResult, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FuelMiniCard(result = fuelConsumption, modifier = Modifier.weight(1f))
        EGTMiniCard(result = egtResult, modifier = Modifier.weight(1f))
        SensorMiniCard(result = sensorHealthSummary, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.weight(2f))
    }
}

@Composable
private fun GearboxMiniCard(
    result: com.canopobd.data.domain.M32GearboxMonitor.GearboxAnalysis?,
    modifier: Modifier = Modifier
) {
    val health = result?.health
    val color = when (health) {
        com.canopobd.data.domain.M32GearboxMonitor.GearboxHealth.HEALTHY -> gaugeGreen
        com.canopobd.data.domain.M32GearboxMonitor.GearboxHealth.EARLY_WEAR,
        com.canopobd.data.domain.M32GearboxMonitor.GearboxHealth.WEAR_DETECTED -> gaugeYellow
        com.canopobd.data.domain.M32GearboxMonitor.GearboxHealth.CRITICAL -> gaugeRed
        else -> textSecondary
    }

    MiniCard(
        icon = Icons.Filled.Settings,
        title = "Getriebe",
        value = result?.health?.label?.take(8) ?: "--",
        color = color,
        modifier = modifier
    )
}

@Composable
private fun ChainMiniCard(
    result: com.canopobd.data.domain.ChainTensionerAnalyzer.ChainTensionerAnalysis?,
    modifier: Modifier = Modifier
) {
    val health = result?.health
    val color = when (health) {
        com.canopobd.data.domain.ChainTensionerAnalyzer.ChainTensionerHealth.HEALTHY -> gaugeGreen
        com.canopobd.data.domain.ChainTensionerAnalyzer.ChainTensionerHealth.WEAR_DETECTED -> gaugeYellow
        com.canopobd.data.domain.ChainTensionerAnalyzer.ChainTensionerHealth.CRITICAL -> gaugeRed
        else -> textSecondary
    }

    MiniCard(
        icon = Icons.Filled.Link,
        title = "Kette",
        value = result?.health?.label?.take(8) ?: "--",
        color = color,
        modifier = modifier
    )
}

@Composable
private fun CoolantMiniCard(
    result: com.canopobd.data.domain.CoolantSystemHealth.CoolantAnalysis?,
    modifier: Modifier = Modifier
) {
    val status = result?.status
    val color = when (status) {
        com.canopobd.data.domain.CoolantSystemHealth.CoolantSystemStatus.HEALTHY -> gaugeGreen
        com.canopobd.data.domain.CoolantSystemHealth.CoolantSystemStatus.THERMOSTAT_SLIGHT,
        com.canopobd.data.domain.CoolantSystemHealth.CoolantSystemStatus.WATER_PUMP_WEAR,
        com.canopobd.data.domain.CoolantSystemHealth.CoolantSystemStatus.LEAK_SUSPECTED -> gaugeYellow
        com.canopobd.data.domain.CoolantSystemHealth.CoolantSystemStatus.THERMOSTAT_STUCK,
        com.canopobd.data.domain.CoolantSystemHealth.CoolantSystemStatus.WATER_PUMP_FAIL,
        com.canopobd.data.domain.CoolantSystemHealth.CoolantSystemStatus.OVERHEATING -> gaugeRed
        else -> textSecondary
    }

    MiniCard(
        icon = Icons.Filled.Thermostat,
        title = "Kühlmittel",
        value = result?.status?.label?.take(8) ?: "--",
        color = color,
        modifier = modifier
    )
}

@Composable
private fun OilMiniCard(
    result: com.canopobd.data.domain.OilConditionMonitor.OilAnalysis?,
    modifier: Modifier = Modifier
) {
    val cond = result?.condition
    val color = when (cond) {
        com.canopobd.data.domain.OilConditionMonitor.OilCondition.EXCELLENT,
        com.canopobd.data.domain.OilConditionMonitor.OilCondition.GOOD -> gaugeGreen
        com.canopobd.data.domain.OilConditionMonitor.OilCondition.FAIR,
        com.canopobd.data.domain.OilConditionMonitor.OilCondition.POOR -> gaugeYellow
        com.canopobd.data.domain.OilConditionMonitor.OilCondition.CRITICAL -> gaugeRed
        else -> textSecondary
    }

    MiniCard(
        icon = Icons.Filled.OilBarrel,
        title = "Öl",
        value = cond?.label?.take(8) ?: "--",
        color = color,
        modifier = modifier
    )
}

@Composable
private fun PCVMiniCard(
    result: com.canopobd.data.domain.PCVMonitor.PCVAnalysis?,
    modifier: Modifier = Modifier
) {
    val health = result?.health
    val color = when (health) {
        com.canopobd.data.domain.PCVMonitor.PCVHealth.HEALTHY -> gaugeGreen
        com.canopobd.data.domain.PCVMonitor.PCVHealth.PLUGGED,
        com.canopobd.data.domain.PCVMonitor.PCVHealth.LEAKING -> gaugeYellow
        else -> textSecondary
    }

    MiniCard(
        icon = Icons.Filled.Air,
        title = "PCV",
        value = health?.label?.take(8) ?: "--",
        color = color,
        modifier = modifier
    )
}

@Composable
private fun LambdaMiniCard(
    result: com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaBalance?,
    modifier: Modifier = Modifier
) {
    val status = result?.status
    val color = when (status) {
        com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaStatus.PERFECT -> gaugeGreen
        com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaStatus.SLIGHTLY_RICH,
        com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaStatus.SLIGHTLY_LEAN,
        com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaStatus.MODERATELY_RICH,
        com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaStatus.MODERATELY_LEAN -> gaugeYellow
        com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaStatus.STALLED,
        com.canopobd.data.domain.LambdaBalanceAnalyzer.LambdaStatus.FAULTED -> gaugeRed
        else -> textSecondary
    }

    MiniCard(
        icon = Icons.Filled.Science,
        title = "Lambda",
        value = status?.name?.replace("_", " ")?.take(8) ?: "--",
        color = color,
        modifier = modifier
    )
}

@Composable
private fun FuelMiniCard(
    result: com.canopobd.data.domain.FuelConsumptionAnalyzer.FuelConsumptionData?,
    modifier: Modifier = Modifier
) {
    val avg = result?.avgL100km
    val display = avg?.let { "%.1f".format(it) } ?: "--"

    MiniCard(
        icon = Icons.Filled.LocalGasStation,
        title = "Verbrauch",
        value = display,
        color = if (avg != null && avg < 10) gaugeGreen else gaugeYellow,
        modifier = modifier
    )
}

@Composable
private fun EGTMiniCard(
    result: com.canopobd.data.domain.EGTMonitor.EGTAnalysis?,
    modifier: Modifier = Modifier
) {
    val maxEgt = result?.estimatedEgtMax ?: 0.0
    val display = if (maxEgt > 0) "${maxEgt.toInt()}°" else "--"
    val color = when {
        maxEgt > 850 -> gaugeRed
        maxEgt > 800 -> gaugeOrange
        maxEgt > 0 -> gaugeGreen
        else -> textSecondary
    }

    MiniCard(
        icon = Icons.Filled.Speed,
        title = "EGT",
        value = display,
        color = color,
        modifier = modifier
    )
}

@Composable
private fun SensorMiniCard(
    result: com.canopobd.data.domain.SensorHealthMonitor.SensorHealthSummary?,
    modifier: Modifier = Modifier
) {
    val total = result?.sensorHealths?.size ?: 0
    val ok = result?.sensorHealths?.count {
        it.value.status.label == "OK"
    } ?: 0

    MiniCard(
        icon = Icons.Filled.Sensors,
        title = "Sensoren",
        value = "$ok/$total",
        color = if (ok == total && total > 0) gaugeGreen else gaugeYellow,
        modifier = modifier
    )
}

@Composable
private fun MiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = surfaceCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
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
                color = textSecondary,
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
