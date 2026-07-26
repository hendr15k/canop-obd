package com.canopobd.ui.fuel

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.FuelEconomyData
import com.canopobd.ui.theme.*

private const val TANK_CAPACITY_LITERS = 56.0
private const val WORKSHOP_COMBINED_L100KM = 5.9
private const val CO2_FACTOR_GRAM_PER_LITER = 2310.0

data class FuelHistoryEntry(
    val timestamp: Long,
    val litersFilled: Double,
    val costPerLiter: Double,
    val odometerKm: Double,
    val l100km: Double
) {
    val totalCost: Double get() = litersFilled * costPerLiter
}

@Composable
fun ExtendedFuelEconomyDialog(
    fuelEconomyData: FuelEconomyData,
    fuelLevelPercent: Double = 0.0,
    maf: Double = 0.0,
    speed: Double = 0.0,
    onDismiss: () -> Unit
) {
    var fuelPrice by remember { mutableStateOf("1.85") }
    var fuelPriceDouble by remember { mutableDoubleStateOf(1.85) }
    var expandedSection by remember { mutableStateOf<String?>(null) }

    val fuelHistory = remember {
        mutableListOf(
            FuelHistoryEntry(System.currentTimeMillis() - 86400000L * 7, 42.5, 1.82, 85420.0, 7.2),
            FuelHistoryEntry(System.currentTimeMillis() - 86400000L * 14, 38.0, 1.79, 84980.0, 6.8),
            FuelHistoryEntry(System.currentTimeMillis() - 86400000L * 21, 44.2, 1.85, 84510.0, 7.5),
            FuelHistoryEntry(System.currentTimeMillis() - 86400000L * 28, 40.1, 1.88, 84020.0, 7.0)
        )
    }

    val currentL100km = if (fuelEconomyData.currentL100km > 0) {
        fuelEconomyData.currentL100km
    } else if (maf > 0 && speed > 0) {
        val fuelRateLph = (maf * 3.6) / 0.75
        (fuelRateLph * 36.0) / speed
    } else {
        0.0
    }

    val avgL100km = if (fuelEconomyData.avgL100km > 0) {
        fuelEconomyData.avgL100km
    } else {
        currentL100km
    }

    val currentFuelLiters = TANK_CAPACITY_LITERS * fuelLevelPercent / 100.0
    val rangeKm = if (avgL100km > 0) {
        currentFuelLiters / avgL100km * 100.0
    } else {
        0.0
    }
    val tripCost = if (avgL100km > 0) {
        avgL100km / 100.0 * fuelPriceDouble
    } else {
        0.0
    }
    val co2PerKm = if (currentL100km > 0) {
        (currentL100km / 100.0) * CO2_FACTOR_GRAM_PER_LITER
    } else {
        0.0
    }
    val consumptionDelta = currentL100km - WORKSHOP_COMBINED_L100KM

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verbrauchsanalyse",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schliessen", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (currentL100km == 0.0 && avgL100km == 0.0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.LocalGasStation, contentDescription = null,
                                tint = textDim, modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Keine Verbrauchsdaten verfuegbar", fontSize = 16.sp, color = textDim)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fahre los, um Verbrauchsdaten zu erfassen",
                                fontSize = 13.sp, color = textDim
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item { ConsumptionGaugeSection(currentL100km = currentL100km) }
                        item { TripAverageSection(avgL100km = avgL100km, currentL100km = currentL100km) }
                        item {
                            RangeCard(
                                rangeKm = rangeKm,
                                fuelLevelPercent = fuelLevelPercent,
                                fuelLiters = currentFuelLiters
                            )
                        }
                        item {
                            FuelCostCard(
                                avgL100km = avgL100km,
                                fuelPrice = fuelPrice,
                                onFuelPriceChange = { fuelPrice = it; fuelPriceDouble = it.toDoubleOrNull() ?: 1.85 },
                                tripCostPer100km = tripCost
                            )
                        }
                        item { CO2Section(co2PerKm = co2PerKm, avgL100km = avgL100km) }
                        item {
                            WorkshopComparisonCard(
                                currentL100km = currentL100km,
                                workshopL100km = WORKSHOP_COMBINED_L100KM,
                                delta = consumptionDelta
                            )
                        }
                        item {
                            FuelHistorySection(
                                history = fuelHistory,
                                expanded = expandedSection == "history",
                                onToggle = {
                                    expandedSection = if (expandedSection == "history") { null } else { "history" }
                                }
                            )
                        }
                        item {
                            OptimizationTipsSection(
                                currentL100km = currentL100km,
                                avgL100km = avgL100km,
                                consumptionDelta = consumptionDelta
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumptionGaugeSection(currentL100km: Double) {
    val animatedProgress = remember { Animatable(0f) }
    val maxGauge = 15.0
    val targetProgress = (currentL100km / maxGauge).coerceIn(0.0, 1.0)

    LaunchedEffect(currentL100km) {
        animatedProgress.animateTo(
            targetValue = targetProgress.toFloat(),
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    val gaugeColor = when {
        currentL100km <= 5.0 -> gaugeGreen
        currentL100km <= 7.0 -> gaugeCyan
        currentL100km <= 9.0 -> gaugeYellow
        currentL100km <= 12.0 -> gaugeOrange
        else -> gaugeRed
    }

    val efficiencyLabel = when {
        currentL100km <= 0 -> "Keine Daten"
        currentL100km <= 5.0 -> "Ausgezeichnet"
        currentL100km <= 7.0 -> "Sehr gut"
        currentL100km <= 9.0 -> "Gut"
        currentL100km <= 12.0 -> "Mittel"
        else -> "Hoch"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aktueller Verbrauch",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    drawArc(
                        color = textDim.copy(alpha = 0.4f),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val brush = Brush.sweepGradient(
                        colors = listOf(gaugeRed, gaugeOrange, gaugeYellow, gaugeGreen, gaugeCyan)
                    )

                    drawArc(
                        brush = brush,
                        startAngle = 150f,
                        sweepAngle = 240f * animatedProgress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f".format(currentL100km),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeColor
                    )
                    Text(
                        text = "L/100km",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = gaugeColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = efficiencyLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gaugeColor
                )
            }
        }
    }
}

@Composable
private fun TripAverageSection(avgL100km: Double, currentL100km: Double) {
    val trend = if (currentL100km > 0 && avgL100km > 0) {
        currentL100km - avgL100km
    } else { 0.0 }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null,
                    tint = canopoAccent, modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Durchschnitt Fahrt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "Durchschnitt", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.1f L/100km".format(avgL100km),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Tendenz", fontSize = 11.sp, color = textDim)
                    val trendColor = when {
                        trend > 0.5 -> gaugeRed
                        trend < -0.5 -> gaugeGreen
                        else -> gaugeYellow
                    }
                    val trendIcon = when {
                        trend > 0.5 -> "▲"
                        trend < -0.5 -> "▼"
                        else -> "●"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trendIcon,
                            fontSize = 14.sp,
                            color = trendColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%+.1f".format(trend),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RangeCard(rangeKm: Double, fuelLevelPercent: Double, fuelLiters: Double) {
    val rangeColor = when {
        rangeKm > 400 -> gaugeGreen
        rangeKm > 250 -> gaugeYellow
        rangeKm > 100 -> gaugeOrange
        else -> gaugeRed
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Route, contentDescription = null,
                    tint = gaugeCyan, modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reichweite",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = canopoHighlight
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "%.0f km".format(rangeKm),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = rangeColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            @Suppress("DEPRECATION")
            LinearProgressIndicator(
                progress = (fuelLevelPercent / 100.0).toFloat().coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = rangeColor,
                trackColor = textDim.copy(alpha = 0.4f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "%.1f L / %.0f L".format(fuelLiters, TANK_CAPACITY_LITERS),
                    fontSize = 12.sp, color = textSecondary
                )
                Text(
                    text = "%.0f%% Tank".format(fuelLevelPercent),
                    fontSize = 12.sp, color = textSecondary
                )
            }
        }
    }
}

@Composable
private fun FuelCostCard(
    avgL100km: Double,
    fuelPrice: String,
    onFuelPriceChange: (String) -> Unit,
    tripCostPer100km: Double
) {
    val costPerKm = if (avgL100km > 0) { avgL100km / 100.0 * (fuelPrice.toDoubleOrNull() ?: 1.85) } else { 0.0 }
    val monthlyEstimate = costPerKm * 1200.0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Euro, contentDescription = null,
                    tint = gaugeYellow, modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kraftstoffkosten",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fuelPrice,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("^\\d{0,2}[.,]?\\d{0,2}$"))) {
                        onFuelPriceChange(newValue.replace(',', '.'))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Preis pro Liter (EUR)") },
                prefix = { Text("€ ", color = gaugeYellow) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = gaugeYellow,
                    unfocusedBorderColor = textDim,
                    focusedLabelColor = gaugeYellow,
                    cursorColor = gaugeYellow,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textSecondary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Pro 100km", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.2f EUR".format(tripCostPer100km),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Pro km", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.3f EUR".format(costPerKm),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Schaetzung/Monat", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.0f EUR".format(monthlyEstimate),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun CO2Section(co2PerKm: Double, avgL100km: Double) {
    val co2Color = when {
        co2PerKm <= 120 -> gaugeGreen
        co2PerKm <= 160 -> gaugeYellow
        co2PerKm <= 200 -> gaugeOrange
        else -> gaugeRed
    }

    val co2Rating = when {
        co2PerKm <= 0 -> "Keine Daten"
        co2PerKm <= 120 -> "Umweltfreundlich"
        co2PerKm <= 160 -> "Durchschnittlich"
        co2PerKm <= 200 -> "Uberdurchschnittlich"
        else -> "Hohe Emissionen"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Cloud, contentDescription = null,
                    tint = gaugeCyan, modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CO2-Emissionen",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Aktuell pro km", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.0f g/km".format(co2PerKm),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = co2Color
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = co2Color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = co2Rating,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = co2Color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val yearlyKm = 15000.0
            val yearlyCO2 = co2PerKm * yearlyKm / 1000.0
            val euLimit = 130.0
            val deviation = co2PerKm - euLimit

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Jahres-EM (15t km)", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.1f t CO2".format(yearlyCO2 / 1000.0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "EU-Limit: 130 g/km", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%+.0f g/km".format(deviation),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (deviation > 0) { gaugeRed } else { gaugeGreen }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkshopComparisonCard(
    currentL100km: Double,
    workshopL100km: Double,
    delta: Double
) {
    val barMax = 15.0
    val barProgress = remember(currentL100km) {
        Animatable(0f)
    }
    LaunchedEffect(currentL100km) {
        barProgress.animateTo(
            targetValue = (currentL100km / barMax).toFloat().coerceIn(0f, 1f),
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    val workshopProgress = (workshopL100km / barMax).toFloat().coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null,
                    tint = canopoSecondary, modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Werkstatt-Vergleich",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Dein Verbrauch", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.1f L/100km".format(currentL100km),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (delta > 0) { gaugeOrange } else { gaugeGreen }
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Werkswert (kombi.)", fontSize = 11.sp, color = textDim)
                    Text(
                        text = "%.1f L/100km".format(workshopL100km),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barHeight = 8.dp.toPx()
                    val y = (size.height - barHeight) / 2f

                    drawRoundRect(
                        color = textDim.copy(alpha = 0.4f),
                        topLeft = Offset(0f, y),
                        size = Size(size.width, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )

                    drawRoundRect(
                        color = gaugeCyan.copy(alpha = 0.4f),
                        topLeft = Offset(0f, y),
                        size = Size(size.width * workshopProgress, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )

                    drawRoundRect(
                        color = if (delta > 0) { gaugeOrange } else { gaugeGreen },
                        topLeft = Offset(0f, y),
                        size = Size(size.width * barProgress.value, barHeight / 1.5f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val percentDiff = if (workshopL100km > 0) { delta / workshopL100km * 100.0 } else { 0.0 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val diffColor = when {
                    percentDiff <= 0 -> gaugeGreen
                    percentDiff <= 10 -> gaugeYellow
                    else -> gaugeRed
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = diffColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (delta > 0) {
                            "+%.1f%% ueber Werkswert".format(percentDiff)
                        } else {
                            "%.1f%% unter Werkswert".format(-percentDiff)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = diffColor
                    )
                }
            }
        }
    }
}

@Composable
private fun FuelHistorySection(
    history: List<FuelHistoryEntry>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column {
            Surface(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.History, contentDescription = null,
                        tint = gaugeOrange, modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tankhistorie",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = canopoHighlight
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${history.size} Eintraege",
                        fontSize = 12.sp,
                        color = textDim
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (expanded) { Icons.Filled.ExpandLess } else { Icons.Filled.ExpandMore },
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = borderSubtle, thickness = 1.dp)

                history.forEach { entry ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "%.1f L".format(entry.litersFilled),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "%.0f km".format(entry.odometerKm),
                                    fontSize = 11.sp,
                                    color = textDim
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "%.2f EUR".format(entry.totalCost),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = gaugeYellow
                                )
                                Text(
                                    text = "%.1f L/100km".format(entry.l100km),
                                    fontSize = 12.sp,
                                    color = when {
                                        entry.l100km <= 6.0 -> gaugeGreen
                                        entry.l100km <= 8.0 -> gaugeYellow
                                        else -> gaugeOrange
                                    }
                                )
                            }
                        }
                    }
                    if (history.isNotEmpty() && entry != history.last()) {
                        HorizontalDivider(
                            color = borderSubtle.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OptimizationTipsSection(
    currentL100km: Double,
    avgL100km: Double,
    consumptionDelta: Double
) {
    data class Tip(val icon: String, val title: String, val description: String, val color: Color)

    val tips = buildList {
        if (currentL100km > 8.0) {
            add(Tip("🐢", "Sanftes Beschleunigen",
                "Vermeide starkes Beschleunigen. Das 1.4 Turbo-System arbeitet effizienter bei 2000-2500 RPM.",
                gaugeGreen))
        }
        if (avgL100km > WORKSHOP_COMBINED_L100KM + 1.5) {
            add(Tip("🛣️", "Reifendruck pruefen",
                "Niedriger Reifendruck erhoht den Verbrauch um bis zu 0.3 L/100km. Ideal: 2.4 bar vorne, 2.2 bar hinten.",
                gaugeCyan))
        }
        add(Tip("⛽", "Kraftstoffqualitaet",
            "Reiner Super (RON 95) verbessert die Verbrennung und kann den Verbrauch um 2-3% senken.",
            gaugeYellow))
        if (currentL100km > 10.0) {
            add(Tip("🔧", "Luftfilter pruefen",
                "Ein verstopfter Luftfilter reduziert die Leistung und erhoeht den Verbrauch bis zu 10%.",
                gaugeOrange))
        }
        add(Tip("💡", "Start-Stopp aktivieren",
            "Das Start-Stopp-System spart Kraftstoff im Stadtverkehr. Bei Ausfall: UDS-Check empfohlen.",
            canopoSecondary))
        if (consumptionDelta > 2.0) {
            add(Tip("⚠️", "Turbo-Check empfohlen",
                "Verbrauch >2L ueber Werkswert. Pruefe Ladedruck (0.6-1.0 bar) und Boost-Lecks via OBD.",
                gaugeRed))
        }
        add(Tip("🌡️", "Betriebstemperatur",
            "Kalter Motor verbraucht bis zu 50% mehr. Kurzstrecke unter 5km ist besonders ineffizient.",
            gaugeCyan))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Lightbulb, contentDescription = null,
                    tint = gaugeGreen, modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Optimierungstipps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            tips.forEach { tip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = tip.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tip.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tip.color
                        )
                        Text(
                            text = tip.description,
                            fontSize = 11.sp,
                            color = textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
