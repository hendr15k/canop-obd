package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors
import kotlin.math.abs

/**
 * PCVHealthCard - PCV-System-Überwachungskarte (A14NET)
 *
 * Das PCV-System (Positive Crankcase Ventilation) ist eine häufige Schwachstelle
 * des A14NET-Motors. Dieses Komponente zeigt:
 * - PCV-Gesundheitsstatus-Anzeige
 * - Ölverbrauch-Trend (letzte 5 Messwerte)
 * - MAF-Korrelationsstatus
 * - STFT-Abweichungs-Indikator
 * - Wartungsempfehlung
 * - "PCV prüfen" Button
 */
@Composable
fun PCVHealthCard(
    modifier: Modifier = Modifier,
    pcvHealthScore: Int = 100,
    oilConsumptionReadings: List<Double> = emptyList(),
    mafCorrelationOk: Boolean = true,
    stftDeviation: Double = 0.0,
    lastMaintenanceKm: Int = 0,
    currentKm: Int = 0,
    onPcvCheckClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val healthStatus = remember(pcvHealthScore) { PCVHealthStatus.fromScore(pcvHealthScore) }
    val healthColor = remember(healthStatus) { healthStatus.color(colors) }

    val isCritical = pcvHealthScore < 50
    val isWarning = pcvHealthScore in 50..79

    val infiniteTransition = rememberInfiniteTransition(label = "pcv_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isCritical) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pcv_pulse_alpha"
    )

    val animatedHealthColor by animateColorAsState(
        targetValue = healthColor,
        animationSpec = tween(300),
        label = "pcv_health_color"
    )

    val maintenanceRecommendation = remember(lastMaintenanceKm, currentKm, pcvHealthScore) {
        getPCVMaintenanceRecommendation(lastMaintenanceKm, currentKm, pcvHealthScore)
    }

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
        color = colors.surfaceCard
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
                        imageVector = Icons.Filled.Air,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.pcv_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                // Health status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = animatedHealthColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(
                                    androidx.compose.foundation.shape.CircleShape
                                )
                                .background(animatedHealthColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = healthStatus.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedHealthColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PCV Health score bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.pcv_health),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$pcvHealthScore / 100",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedHealthColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                            .fillMaxWidth((pcvHealthScore / 100f).toFloat())
                            .background(animatedHealthColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Oil consumption trend
            OilConsumptionTrend(
                readings = oilConsumptionReadings,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // MAF correlation
            PCVStatusRow(
                label = stringResource(R.string.pcv_maf_correlation),
                isOk = mafCorrelationOk,
                okText = "OK",
                warningText = "Abweichung",
                colors = colors
            )

            Spacer(modifier = Modifier.height(6.dp))

            // STFT deviation
            val stftOk = abs(stftDeviation) < 5.0
            PCVStatusRow(
                label = stringResource(R.string.pcv_stft_deviation),
                isOk = stftOk,
                okText = "%.1f%%".format(stftDeviation),
                warningText = "%.1f%% — Abweichung".format(stftDeviation),
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Maintenance recommendation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = colors.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = maintenanceRecommendation.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = maintenanceRecommendation.text,
                        fontSize = 11.sp,
                        color = maintenanceRecommendation.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Check button
            if (onPcvCheckClick != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onPcvCheckClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent.copy(alpha = 0.2f),
                        contentColor = colors.accent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.pcv_check_now),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OilConsumptionTrend(
    readings: List<Double>,
    colors: AppColors
) {
    val lastReadings = remember(readings) { readings.takeLast(5) }

    Column {
        Text(
            text = stringResource(R.string.pcv_oil_consumption_trend),
            fontSize = 11.sp,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (lastReadings.isEmpty()) {
            Text(
                text = "—",
                fontSize = 12.sp,
                color = colors.textDim
            )
        } else {
            // Mini sparkline graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surfaceVariant)
                    .padding(4.dp)
            ) {
                if (lastReadings.size < 2) return@Canvas

                val maxVal = lastReadings.max().coerceAtLeast(0.1)
                val minVal = lastReadings.min().coerceAtMost(maxVal - 0.01)
                val range = (maxVal - minVal).coerceAtLeast(0.01)
                val w = size.width
                val h = size.height

                val path = Path()
                lastReadings.forEachIndexed { index, value ->
                    val x = w * index / (lastReadings.size - 1).coerceAtLeast(1)
                    val y = h - ((value - minVal) / range * h).toFloat().coerceIn(0f, h)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = colors.gaugeCyan,
                    style = Stroke(width = 2f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "L/1000km",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                val avg = if (lastReadings.isNotEmpty()) lastReadings.average() else 0.0
                Text(
                    text = "Ø %.2f".format(avg),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        avg > 0.5 -> colors.gaugeRed
                        avg > 0.3 -> colors.gaugeOrange
                        else -> colors.gaugeGreen
                    }
                )
            }
        }
    }
}

@Composable
private fun PCVStatusRow(
    label: String,
    isOk: Boolean,
    okText: String,
    warningText: String,
    colors: AppColors
) {
    val statusColor = if (isOk) colors.gaugeGreen else colors.gaugeOrange

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = colors.textSecondary
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusColor.copy(alpha = 0.2f)
        ) {
            Text(
                text = if (isOk) okText else warningText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

enum class PCVHealthStatus(val label: String) {
    HEALTHY("Gesund"),
    WEAR_DETECTED("Verschleiß"),
    CRITICAL("Kritisch");

    fun color(colors: AppColors): Color = when (this) {
        HEALTHY -> colors.gaugeGreen
        WEAR_DETECTED -> colors.gaugeOrange
        CRITICAL -> colors.gaugeRed
    }

    companion object {
        fun fromScore(score: Int): PCVHealthStatus = when {
            score >= 80 -> HEALTHY
            score >= 50 -> WEAR_DETECTED
            else -> CRITICAL
        }
    }
}

private data class MaintenanceRecommendation(
    val text: String,
    val color: Color
)

private fun getPCVMaintenanceRecommendation(
    lastMaintenanceKm: Int,
    currentKm: Int,
    healthScore: Int
): MaintenanceRecommendation {
    val kmSinceLast = currentKm - lastMaintenanceKm
    val pcvIntervalKm = 60000

    return when {
        healthScore < 50 -> MaintenanceRecommendation(
            text = "PCV-Ventil sofort prüfen lassen!",
            color = Color(0xFFEF4444)
        )
        healthScore < 70 -> MaintenanceRecommendation(
            text = "PCV-Ventil bei nächster Wartung ersetzen",
            color = Color(0xFFF97316)
        )
        kmSinceLast > pcvIntervalKm -> MaintenanceRecommendation(
            text = "PCV-Wartungsintervall überschritten (%,d km)".format(kmSinceLast),
            color = Color(0xFFFBBF24)
        )
        kmSinceLast > pcvIntervalKm - 10000 -> MaintenanceRecommendation(
            text = "PCV-Wartung bald fällig",
            color = Color(0xFFFBBF24)
        )
        else -> MaintenanceRecommendation(
            text = "PCV-System in Ordnung",
            color = Color(0xFF22C55E)
        )
    }
}
