package com.canopobd.ui.drivescore

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.domain.FuelTrimAnalyzer
import com.canopobd.data.model.FuelTrimAnalysis
import com.canopobd.ui.theme.*
import kotlin.math.abs
import kotlin.math.min

/**
 * Dialog zur Anzeige detaillierter Kraftstoff-Trim-Daten für den A14NET Motor.
 *
 * Kraftstoff-Trim-Interpretation:
 * - STFT (Short Term Fuel Trim): Kurzfristige Korrekturen der Kraftstoffzufuhr
 * - LTFT (Long Term Fuel Trim): Langfristige Anpassungen basierend auf STFT-Mittelwerten
 * - Positive Werte = System fügt Kraftstoff hinzu (kompensiert für mageres Gemisch)
 * - Negative Werte = System reduziert Kraftstoff (kompensiert für fettes Gemisch)
 *
 * Normalwerte A14NET:
 * - Ideal: ±5% (System optimal)
 * - Akzeptabel: ±5-10% (leichte Abweichung)
 * - Warnung: ±10-15% (Wartung empfohlen)
 * - Problem: >±15% (sofortige Diagnose erforderlich)
 *
 * Typische A14NET-Probleme:
 * - MAF-Sensor Verschmutzung (besonders nach 60-100tkm)
 * - PCV-Ventil Defekt (Ölverbrauch, blauer Rauch)
 * - Wastegate-Stellglied-Probleme (Ladedruckschwankungen)
 * - Ansaugkrümmer-Lecks (besonders am Boost-Bereich)
 * - Kraftstoffdruck-Probleme
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun FuelTrimDialog(
    fuelTrimAnalysis: FuelTrimAnalysis?,
    stftBank1: Double,
    ltftBank1: Double,
    stftBank2: Double,
    ltftBank2: Double,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analyzer = remember { FuelTrimAnalyzer() }

    // Analyse für beide Bänke berechnen
    val statusB1 = remember(stftBank1, ltftBank1) {
        analyzer.analyze(stftBank1, ltftBank1)
    }
    val statusB2 = remember(stftBank2, ltftBank2) {
        analyzer.analyze(stftBank2, ltftBank2)
    }
    val actionB1 = remember(statusB1) {
        analyzer.getRecommendedAction(statusB1)
    }
    val actionB2 = remember(statusB2) {
        analyzer.getRecommendedAction(statusB2)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
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
                            Icons.Filled.LocalGasStation,
                            contentDescription = null,
                            tint = canopoHighlight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.fuel_trim_analysis_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = canopoHighlight
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Info-Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = canopoAccent.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = canopoAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A14NET: Normaler Bereich ±5%%, Warnung >±10%%, Problem >±15%%",
                            fontSize = 11.sp,
                            color = canopoAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Gesamt-Systemstatus
                    item {
                        SystemStatusCard(
                            statusB1 = statusB1,
                            statusB2 = statusB2
                        )
                    }

                    // STFT/LTFT Detail-Anzeige Bank 1
                    item {
                        TrimBankCard(
                            bankLabel = "Bank 1 (Zylinder 1-4)",
                            stft = stftBank1,
                            ltft = ltftBank1,
                            totalTrim = statusB1.totalTrim,
                            status = statusB1.diagnosis,
                            action = actionB1,
                            isLean = statusB1.isLean,
                            isRich = statusB1.isRich,
                            healthScore = statusB1.healthScore
                        )
                    }

                    // STFT/LTFT Detail-Anzeige Bank 2
                    item {
                        TrimBankCard(
                            bankLabel = "Bank 2 (Zylinder 5-8)",
                            stft = stftBank2,
                            ltft = ltftBank2,
                            totalTrim = statusB2.totalTrim,
                            status = statusB2.diagnosis,
                            action = actionB2,
                            isLean = statusB2.isLean,
                            isRich = statusB2.isRich,
                            healthScore = statusB2.healthScore
                        )
                    }

                    // Wartungsempfehlungen
                    item {
                        MaintenanceRecommendationsCard(
                            statusB1 = statusB1,
                            statusB2 = statusB2
                        )
                    }

                    // Technische Referenz
                    item {
                        TechnicalReferenceCard()
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemStatusCard(
    statusB1: FuelTrimAnalyzer.FuelTrimStatus,
    statusB2: FuelTrimAnalyzer.FuelTrimStatus
) {
    val overallHealth = min(statusB1.healthScore, statusB2.healthScore)
    val healthColor = when {
        overallHealth >= 80 -> gaugeGreen
        overallHealth >= 50 -> gaugeYellow
        overallHealth >= 30 -> gaugeOrange
        else -> gaugeRed
    }
    val statusText = when {
        overallHealth >= 80 -> "Kraftstoffsystem OPTIMAL"
        overallHealth >= 50 -> "Leichte Abweichung — Wartung beobachten"
        overallHealth >= 30 -> "Warnung — Wartung empfohlen"
        else -> "Problem erkannt — Diagnose erforderlich!"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Großes Status-Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(healthColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$overallHealth",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = healthColor
                    )
                    Text(
                        text = "Score",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statusText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = healthColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrimBadge(
                    label = "B1 Gesamt",
                    value = statusB1.totalTrim,
                    color = when {
                        abs(statusB1.totalTrim) < 5 -> gaugeGreen
                        abs(statusB1.totalTrim) < 10 -> gaugeYellow
                        abs(statusB1.totalTrim) < 15 -> gaugeOrange
                        else -> gaugeRed
                    }
                )
                TrimBadge(
                    label = "B2 Gesamt",
                    value = statusB2.totalTrim,
                    color = when {
                        abs(statusB2.totalTrim) < 5 -> gaugeGreen
                        abs(statusB2.totalTrim) < 10 -> gaugeYellow
                        abs(statusB2.totalTrim) < 15 -> gaugeOrange
                        else -> gaugeRed
                    }
                )
            }
        }
    }
}

@Composable
private fun TrimBadge(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%+.1f%%".format(value),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = textSecondary
        )
    }
}

@Composable
private fun TrimBankCard(
    bankLabel: String,
    stft: Double,
    ltft: Double,
    totalTrim: Double,
    status: String,
    action: String,
    isLean: Boolean,
    isRich: Boolean,
    healthScore: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header mit Bank-Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (bankLabel.contains("1")) Icons.Filled.Numbers else Icons.Filled.Numbers,
                        contentDescription = null,
                        tint = canopoAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bankLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoAccent
                    )
                }

                // Richtung-Pfeil
                val directionIcon = when {
                    totalTrim > 3 -> Icons.AutoMirrored.Filled.TrendingUp
                    totalTrim < -3 -> Icons.AutoMirrored.Filled.TrendingDown
                    else -> Icons.AutoMirrored.Filled.TrendingFlat
                }
                val directionColor = when {
                    totalTrim > 3 -> gaugeOrange
                    totalTrim < -3 -> gaugeCyan
                    else -> gaugeGreen
                }
                Icon(
                    directionIcon,
                    contentDescription = null,
                    tint = directionColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // STFT und LTFT Balken
            TrimBarRow(
                label = "STFT",
                subLabel = "Kurzzeit-Trim",
                value = stft,
                maxRange = 25.0
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrimBarRow(
                label = "LTFT",
                subLabel = "Langzeit-Trim",
                value = ltft,
                maxRange = 25.0
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Gesamt-Trim mit speziellem Balken
            TrimBarRow(
                label = "GESAMT",
                subLabel = "STFT + LTFT",
                value = totalTrim,
                maxRange = 30.0,
                isHighlighted = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = borderSubtle, thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Status-Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusIcon = when {
                    isLean -> Icons.Filled.Warning
                    isRich -> Icons.Filled.Warning
                    healthScore >= 80 -> Icons.Filled.CheckCircle
                    healthScore >= 50 -> Icons.Filled.Info
                    else -> Icons.Filled.Error
                }
                val statusColor = when {
                    isLean -> gaugeOrange
                    isRich -> gaugeCyan
                    healthScore >= 80 -> gaugeGreen
                    healthScore >= 50 -> gaugeYellow
                    else -> gaugeRed
                }
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Empfehlung
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Build,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = action,
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }
        }
    }
}

@Composable
private fun TrimBarRow(
    label: String,
    subLabel: String,
    value: Double,
    maxRange: Double,
    isHighlighted: Boolean = false
) {
    val barColor = when {
        abs(value) < 5 -> gaugeGreen
        abs(value) < 10 -> gaugeYellow
        abs(value) < 15 -> gaugeOrange
        else -> gaugeRed
    }

    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "trim_value"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) canopoHighlight else textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subLabel,
                    fontSize = 10.sp,
                    color = textDim
                )
            }
            Text(
                text = "%+.1f%%".format(value),
                fontSize = if (isHighlighted) 16.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Fortschrittsbalken mit Mittellinie
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isHighlighted) 18.dp else 12.dp)
                .clip(RoundedCornerShape(if (isHighlighted) 4.dp else 3.dp))
                .background(surfaceCard)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val barHeight = size.height
                val maxBarWidth = centerX

                // Mittellinie (0%)
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, barHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Warnmarkierungen
                val warningLow = maxBarWidth * 0.33f // ±10%
                val warningHigh = maxBarWidth * 0.5f // ±15%

                listOf(centerX - warningLow, centerX + warningLow,
                    centerX - warningHigh, centerX + warningHigh).forEach { x ->
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(x, 0f),
                        end = Offset(x, barHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Farbiger Balken
                val normalizedValue = (animatedValue / maxRange).toFloat().coerceIn(-1f, 1f)
                val barWidth = maxBarWidth * abs(normalizedValue)

                if (normalizedValue > 0) {
                    // Zu mager (fügt Kraftstoff hinzu)
                    drawRect(
                        color = barColor.copy(alpha = 0.8f),
                        topLeft = Offset(centerX, 0f),
                        size = Size(barWidth, barHeight)
                    )
                } else {
                    // Zu fett (reduziert Kraftstoff)
                    drawRect(
                        color = barColor.copy(alpha = 0.8f),
                        topLeft = Offset(centerX - barWidth, 0f),
                        size = Size(barWidth, barHeight)
                    )
                }

                // Glow-Effekt
                if (abs(animatedValue) > 5f) {
                    val glowWidth = maxBarWidth * abs(normalizedValue)
                    if (normalizedValue > 0) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(barColor.copy(alpha = 0.4f), Color.Transparent),
                                startX = centerX,
                                endX = centerX + glowWidth
                            ),
                            topLeft = Offset(centerX, 0f),
                            size = Size(glowWidth, barHeight)
                        )
                    } else {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, barColor.copy(alpha = 0.4f)),
                                startX = centerX - glowWidth,
                                endX = centerX
                            ),
                            topLeft = Offset(centerX - glowWidth, 0f),
                            size = Size(glowWidth, barHeight)
                        )
                    }
                }
            }

            // Bereichs-Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("-${maxRange.toInt()}", fontSize = 8.sp, color = textMuted)
                Text("-15%", fontSize = 8.sp, color = gaugeRed.copy(alpha = 0.6f))
                Text("-10%", fontSize = 8.sp, color = gaugeOrange.copy(alpha = 0.6f))
                Text("0%", fontSize = 8.sp, color = textSecondary)
                Text("+10%", fontSize = 8.sp, color = gaugeOrange.copy(alpha = 0.6f))
                Text("+15%", fontSize = 8.sp, color = gaugeRed.copy(alpha = 0.6f))
                Text("+${maxRange.toInt()}", fontSize = 8.sp, color = textMuted)
            }
        }
    }
}

@Composable
private fun MaintenanceRecommendationsCard(
    statusB1: FuelTrimAnalyzer.FuelTrimStatus,
    statusB2: FuelTrimAnalyzer.FuelTrimStatus
) {
    val recommendations = buildList {
        // A14NET-spezifische Empfehlungen basierend auf Trim-Mustern
        when {
            statusB1.isLean && statusB2.isLean -> {
                add(Recommendation(
                    "Luftansaugung prüfen",
                    "Beide Bänke mager: Mögliche Undichtheit im Ansaugtrakt (Leck)",
                    Icons.Filled.Air,
                    gaugeOrange
                ))
                add(Recommendation(
                    "MAF-Sensor reinigen",
                    " Verschmutzter MAF kann falsche Luftmasse melden",
                    Icons.Filled.Sensors,
                    gaugeYellow
                ))
            }
            statusB1.isRich && statusB2.isRich -> {
                add(Recommendation(
                    "Kraftstoffdruck prüfen",
                    "Beide Bänke fett: Zu hoher Kraftstoffdruck oder Einspritzung",
                    Icons.Filled.LocalGasStation,
                    gaugeOrange
                ))
                add(Recommendation(
                    "O2-Sensoren prüfen",
                    "Defekte O2-Sensoren können Gemisch nicht korrekt regeln",
                    Icons.Filled.Sensors,
                    gaugeYellow
                ))
            }
            statusB1.healthScore < 50 -> {
                add(Recommendation(
                    "MAF-Sensor (A14NET)",
                    "Typisches Problem bei 60-100tkm: Reinigen oder ersetzen",
                    Icons.Filled.Sensors,
                    gaugeOrange
                ))
            }
            statusB1.totalTrim > 5 -> {
                add(Recommendation(
                    "PCV-Ventil prüfen",
                    "A14NET bekannt für PCV-Probleme ab 60tkm (Ölverbrauch)",
                    Icons.Filled.Build,
                    gaugeYellow
                ))
            }
            statusB1.totalTrim < -5 -> {
                add(Recommendation(
                    "Kraftstoffdruckregler",
                    "Zu hoher Druck oder Einspritzdüsen undicht",
                    Icons.Filled.LocalGasStation,
                    gaugeYellow
                ))
            }
            else -> {
                add(Recommendation(
                    "Alles in Ordnung",
                    "Trim-Werte im normalen Bereich für den A14NET",
                    Icons.Filled.CheckCircle,
                    gaugeGreen
                ))
            }
        }

        // Generelle Empfehlungen
        add(Recommendation(
            "Regelmäßige Ölwechsel",
            "A14NET: Alle 15.000 km mit Dexos2 5W-30",
            Icons.Filled.OilBarrel,
            canopoAccent
        ))

        if (statusB1.healthScore < 80 || statusB2.healthScore < 80) {
            add(Recommendation(
                "Fehlerspeicher prüfen",
                "DTCs können die Ursache für abweichende Trims sein",
                Icons.Filled.ErrorOutline,
                gaugeOrange
            ))
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Recommend,
                    contentDescription = null,
                    tint = canopoHighlight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Wartungsempfehlungen A14NET",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            recommendations.forEach { rec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        rec.icon,
                        contentDescription = null,
                        tint = rec.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = rec.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = rec.description,
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalReferenceCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Technische Referenz",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ReferenceRow("STFT", "Kurzzeit-Korrektur (±100% max)", gaugeCyan)
                ReferenceRow("LTFT", "Langzeit-Speicher (adaptiv)", gaugeCyan)
                ReferenceRow("+ Wert", "Mehr Kraftstoff (mageres Gemisch)", gaugeOrange)
                ReferenceRow("- Wert", "Weniger Kraftstoff (fettes Gemisch)", gaugeCyan)
                HorizontalDivider(color = borderSubtle, thickness = 0.5.dp)
                ReferenceRow("A14NET ECU", "Bosch ME17.9.22 / Delco E78", textSecondary)
                ReferenceRow("Normal", "±5% im geschlossenen Regelkreis", gaugeGreen)
                ReferenceRow("Warnung", "±10-15% — Wartung prüfen", gaugeYellow)
                ReferenceRow("Problem", ">±15% — Diagnose sofort", gaugeRed)
            }
        }
    }
}

@Composable
private fun ReferenceRow(label: String, description: String, labelColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor
        )
        Text(
            text = description,
            fontSize = 10.sp,
            color = textDim
        )
    }
}

private data class Recommendation(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
