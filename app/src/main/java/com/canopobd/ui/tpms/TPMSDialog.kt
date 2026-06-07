package com.canopobd.ui.tpms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.components.*
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

    val displayTires = tireData ?: emptyList()

    val validPressures = displayTires.filter { it.pressure > 0 }
    val avgPressure = if (validPressures.isNotEmpty()) validPressures.map { it.pressure }.average().toFloat() else 0f
    val maxPressure = displayTires.maxOfOrNull { it.pressure } ?: 0f
    val minPressure = validPressures.minOfOrNull { it.pressure } ?: 0f
    val pressureDiff = maxPressure - minPressure

    DialogShell(
        onDismiss = onDismiss,
        title = "Reifendruck (TPMS)",
        eyebrow = "Reifenüberwachung",
        heightFraction = 0.85f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avg pressure card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentEdge = colors.success,
                padding = 16.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(colors.success.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TireRepair,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "DURCHSCHNITTSDRUCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${avgPressure.toInt()}",
                                style = MaterialTheme.typography.displaySmall,
                                color = colors.success,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                " kPa",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textTertiary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Δ ${pressureDiff.toInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (pressureDiff > 30) colors.warning else colors.textTertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Differenz",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }
                }
            }

            // Tire visualization
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp
            ) {
                Text(
                    "REIFENPOSITIONEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(AppRadius.md))
                        .background(colors.surfaceElevated)
                ) {
                    // Car silhouette
                    CarSilhouette(
                        modifier = Modifier.fillMaxSize(),
                        frontLeft = displayTires.getOrNull(0),
                        frontRight = displayTires.getOrNull(1),
                        rearLeft = displayTires.getOrNull(2),
                        rearRight = displayTires.getOrNull(3),
                        colors = colors
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    displayTires.forEach { tire ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tire.position,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${tire.pressure.toInt()} kPa",
                                style = MaterialTheme.typography.titleSmall,
                                color = tireStatusColor(tire, colors),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Reset button
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isResetting) {
                    Column {
                        Text(
                            text = "TPMS-Reset läuft… ${(resetProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = resetProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = colors.surfaceRaised
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlineButton(
                            text = "Schließen",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            text = "TPMS Reset",
                            onClick = {
                                isResetting = true
                                resetProgress = 0f
                                onTPMSReset()
                            },
                            icon = Icons.Filled.Refresh,
                            gradient = colors.gradientAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (lastResetTime != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusDot(color = colors.success, pulse = false)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Letzter Reset: erfolgreich",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.success
                    )
                }
            }
        }

        LaunchedEffect(isResetting) {
            if (isResetting) {
                while (resetProgress < 1f) {
                    delay(50)
                    resetProgress = (resetProgress + 0.02f).coerceAtMost(1f)
                }
                isResetting = false
                lastResetTime = System.currentTimeMillis()
            }
        }
    }
}

@Composable
private fun CarSilhouette(
    modifier: Modifier = Modifier,
    frontLeft: TireData?,
    frontRight: TireData?,
    rearLeft: TireData?,
    rearRight: TireData?,
    colors: AppColors
) {
    Box(modifier = modifier) {
        // Car body outline
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(180.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(AppRadius.md))
                .background(colors.surfaceRaised)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
        ) {
            // Front indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .width(20.dp)
                    .height(2.dp)
                    .background(colors.primary.copy(alpha = 0.5f))
            )
            Text(
                text = "↑ VORNE",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                fontSize = 7.sp
            )
        }
        // Tire positions
        TireMarker(frontLeft, Alignment.TopStart, colors)
        TireMarker(frontRight, Alignment.TopEnd, colors)
        TireMarker(rearLeft, Alignment.BottomStart, colors)
        TireMarker(rearRight, Alignment.BottomEnd, colors)
    }
}

@Composable
private fun BoxScope.TireMarker(tire: TireData?, alignment: Alignment, colors: AppColors) {
    val c = tire?.let { tireStatusColor(it, colors) } ?: colors.textMuted
    Box(
        modifier = Modifier
            .align(alignment)
            .padding(6.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(c.copy(alpha = 0.3f))
            .border(2.dp, c, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = null,
            tint = c,
            modifier = Modifier.size(10.dp)
        )
    }
}

private fun tireStatusColor(tire: TireData, colors: AppColors): Color = when {
    tire.isNok -> colors.critical
    tire.isLow -> colors.warning
    tire.isHigh -> colors.warning
    tire.pressure > 0 -> colors.success
    else -> colors.textMuted
}
