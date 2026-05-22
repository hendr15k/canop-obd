package com.canopobd.ui.ecoscore

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.*
import com.canopobd.ui.theme.*

@Composable
fun EcoScoreDialog(
    ecoScore: EcoScoreData,
    co2Data: CO2Data,
    fuelCost: FuelCostData,
    rangeEstimation: RangeEstimation,
    efficiency: FuelEfficiencyMetrics,
    drivingStyle: DrivingStyleAnalysis,
    tips: List<EcoTip>,
    onDismiss: () -> Unit,
    onSetFuelPrice: (Double) -> Unit
) {
    var fuelPriceText by remember { mutableStateOf("%.2f".format(fuelCost.fuelPricePerLiter)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
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
                        text = "ECO-Analyse",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schliessen", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    item {
                        EcoScoreCircle(ecoScore = ecoScore)
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    item {
                        CO2Section(co2Data = co2Data)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        KraftstoffkostenCard(
                            fuelCost = fuelCost,
                            fuelPriceText = fuelPriceText,
                            onFuelPriceTextChange = { fuelPriceText = it },
                            onSetFuelPrice = { price ->
                                fuelPriceText = "%.2f".format(price)
                                onSetFuelPrice(price)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        ReichweiteCard(rangeEstimation = rangeEstimation)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        FahrstilCard(drivingStyle = drivingStyle)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        OptimierungstippsHeader()
                    }

                    items(tips) { tip ->
                        OptimierungstippItem(tip = tip)
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EcoScoreCircle(ecoScore: EcoScoreData) {
    val score = ecoScore.overallScore.coerceIn(0, 100)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = score / 100f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val scoreColor = when {
        score >= 70 -> gaugeGreen
        score >= 40 -> gaugeYellow
        else -> gaugeRed
    }

    val subScoreLabels = listOf(
        "Effizienz" to ecoScore.efficiencyScore,
        "Sanftes Fahren" to ecoScore.smoothnessScore,
        "Gleitverhalten" to ecoScore.cruisingScore,
        "Trägheit" to ecoScore.momentumScore
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24.dp.toPx()
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

                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(gaugeRed, gaugeOrange, gaugeYellow, gaugeGreen)
                )

                drawArc(
                    brush = gradientBrush,
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
                    text = "$score",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Text(
                    text = ecoScore.grade,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            subScoreLabels.forEach { (label, value) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$value",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            value >= 70 -> gaugeGreen
                            value >= 40 -> gaugeYellow
                            else -> gaugeRed
                        }
                    )
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = textDim
                    )
                }
            }
        }
    }
}

@Composable
private fun CO2Section(co2Data: CO2Data) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Cloud, contentDescription = null, tint = gaugeCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CO2-Fußabdruck",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Fahrt", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "%.2f kg".format(co2Data.tripCO2Kg),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Pro km", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "%.0f g/km".format(co2Data.perKmCO2Kg * 1000),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Jährliche Schätzung", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "%,.0f kg".format(co2Data.annualEstimateKg),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Baum-Äquivalent", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "%.1f Bäume".format(co2Data.treesEquivalent),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = gaugeGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun KraftstoffkostenCard(
    fuelCost: FuelCostData,
    fuelPriceText: String,
    onFuelPriceTextChange: (String) -> Unit,
    onSetFuelPrice: (Double) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Euro, contentDescription = null, tint = gaugeYellow, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kraftstoffkosten",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fuelPriceText,
                    onValueChange = onFuelPriceTextChange,
                    label = { Text("Preis/Liter (EUR)", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    ),
                    leadingIcon = { Icon(Icons.Filled.LocalGasStation, contentDescription = null, tint = textDim, modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(8.dp)
                )
                Column {
                    Text(text = "Fahrtkosten", fontSize = 10.sp, color = textDim)
                    Text(
                        text = "%.2f EUR".format(fuelCost.tripCost),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Pro km", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "%.3f EUR".format(fuelCost.costPerKm),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Monatlich", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "%.2f EUR".format(fuelCost.monthlyCost),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Jährlich", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "%.2f EUR".format(fuelCost.annualCost),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    val price = fuelPriceText.toDoubleOrNull() ?: FuelCostData.DEFAULT_PRICE_EUR
                    onSetFuelPrice(price)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Preis aktualisieren", fontSize = 12.sp, color = canopoAccent)
            }
        }
    }
}

@Composable
private fun ReichweiteCard(rangeEstimation: RangeEstimation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Route, contentDescription = null, tint = gaugeGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reichweite",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${rangeEstimation.estimatedRangeKm} km",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = gaugeGreen,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Geschätzte Reichweite",
                fontSize = 12.sp,
                color = textDim,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Best Case", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "${rangeEstimation.bestCaseRangeKm} km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = gaugeGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Worst Case", fontSize = 12.sp, color = textDim)
                    Text(
                        text = "${rangeEstimation.worstCaseRangeKm} km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = gaugeRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val fuelPercent = rangeEstimation.fuelLevelPercent.coerceIn(0, 100)
            val fuelBarColor = when {
                fuelPercent > 50 -> gaugeGreen
                fuelPercent > 20 -> gaugeYellow
                else -> gaugeRed
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(textDim.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fuelPercent / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(fuelBarColor.copy(alpha = 0.7f), fuelBarColor)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tankinhalt: %.1f L".format(rangeEstimation.fuelLevelLiters),
                fontSize = 12.sp,
                color = textDim,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun FahrstilCard(drivingStyle: DrivingStyleAnalysis) {
    val styleName = when (drivingStyle.style) {
        DrivingStyle.ECONOMICAL -> "Ökonomisch"
        DrivingStyle.NORMAL -> "Normal"
        DrivingStyle.AGGRESSIVE -> "Aggressiv"
        DrivingStyle.SPORT -> "Sportlich"
    }

    val styleColor = when (drivingStyle.style) {
        DrivingStyle.ECONOMICAL -> gaugeGreen
        DrivingStyle.NORMAL -> gaugeYellow
        DrivingStyle.AGGRESSIVE -> gaugeOrange
        DrivingStyle.SPORT -> gaugeRed
    }

    val subScores = listOf(
        "Beschleunigen" to drivingStyle.accelerationScore,
        "Bremsen" to drivingStyle.brakingScore,
        "Gleitverhalten" to drivingStyle.cruisingScore,
        "Antizipation" to drivingStyle.anticipationScore
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Speed, contentDescription = null, tint = styleColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fahrstil-Analyse",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = styleName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = styleColor
                )
                Text(
                    text = "%.0f%%".format(drivingStyle.stylePercentage),
                    fontSize = 14.sp,
                    color = textSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val barProgress = drivingStyle.stylePercentage.coerceIn(0f, 100f) / 100f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(textDim.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(styleColor)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            subScores.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, fontSize = 13.sp, color = textSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(textDim.copy(alpha = 0.4f))
                        ) {
                            val scoreColor = when {
                                value >= 70 -> gaugeGreen
                                value >= 40 -> gaugeYellow
                                else -> gaugeRed
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(value / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(scoreColor)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$value",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptimierungstippsHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = gaugeYellow, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Optimierungstipps",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = canopoHighlight
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun OptimierungstippItem(tip: EcoTip) {
    val priorityColor = when (tip.priority) {
        TipPriority.HIGH -> gaugeRed
        TipPriority.MEDIUM -> gaugeYellow
        TipPriority.LOW -> gaugeGreen
    }

    val impactIcon = when (tip.impact) {
        TipImpact.FUEL_CONSUMPTION -> Icons.Filled.LocalGasStation
        TipImpact.TIRE_WEAR -> Icons.Filled.ChangeCircle
        TipImpact.BRAKE_WEAR -> Icons.Filled.Build
        TipImpact.ENGINE_LIFE -> Icons.Filled.Settings
        TipImpact.SAFETY -> Icons.Filled.Shield
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = canopoDark
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tip.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(impactIcon, contentDescription = null, tint = textDim, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tip.description,
                    fontSize = 12.sp,
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val priorityLabel = when (tip.priority) {
                        TipPriority.HIGH -> "HOCH"
                        TipPriority.MEDIUM -> "MITTEL"
                        TipPriority.LOW -> "NIEDRIG"
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = priorityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = priorityLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (tip.potentialSavingsPercent > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sparpotenzial: %.0f%%".format(tip.potentialSavingsPercent),
                            fontSize = 11.sp,
                            color = gaugeGreen
                        )
                    }
                }
            }
        }
    }
}
