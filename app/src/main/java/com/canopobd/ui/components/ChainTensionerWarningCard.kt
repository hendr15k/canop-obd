package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * ChainTensionerWarningCard - Warnkarte für den Steuerkettenspanner (A14NET)
 *
 * Zeigt den Kettenspanner-Zustand mit Farbcodierung:
 * - GRÜN: Gesund (Health Score >= 80)
 * - GELB: Verschleiß erkannt (50-79)
 * - ROT: Kritisch (< 50)
 *
 * Enthält: Gesundheitsstatus, Restlebensdauer, Warnhinweis (DE),
 *          Datum der letzten Prüfung, Diagnose-Button
 */
@Composable
fun ChainTensionerWarningCard(
    healthScore: Int,
    modifier: Modifier = Modifier,
    lastCheckedTimestamp: Long = 0L,
    estimatedRemainingLifeKm: Int = 0,
    rattleDetected: Boolean = false,
    onDiagnosticClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val healthStatus = remember(healthScore) { ChainHealthStatus.fromScore(healthScore) }
    val healthColor = remember(healthStatus) { healthStatus.color(colors) }
    val healthLabel = remember(healthStatus) { healthStatus.label }

    val isCritical = healthScore < 50
    val isWarning = healthScore in 50..79

    val infiniteTransition = rememberInfiniteTransition(label = "chain_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isCritical) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chain_pulse_alpha"
    )

    val animatedHealthColor by animateColorAsState(
        targetValue = healthColor,
        animationSpec = tween(300),
        label = "chain_health_color"
    )

    val lastCheckedText = remember(lastCheckedTimestamp) {
        if (lastCheckedTimestamp > 0L) {
            val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())
            fmt.format(Instant.ofEpochMilli(lastCheckedTimestamp))
        } else {
            "—"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCritical || rattleDetected) {
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
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        tint = animatedHealthColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.chain_tensioner_title),
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
                                .clip(CircleShape)
                                .background(animatedHealthColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = healthLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedHealthColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Health score bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.chain_health_score),
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "$healthScore / 100",
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
                            .fillMaxWidth((healthScore / 100f).toFloat())
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        animatedHealthColor.copy(alpha = 0.7f),
                                        animatedHealthColor
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Remaining life and last checked
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.chain_estimated_life),
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (estimatedRemainingLifeKm > 0) {
                            String.format(Locale.GERMAN, "%d km", estimatedRemainingLifeKm)
                        } else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            estimatedRemainingLifeKm < 20000 -> colors.gaugeRed
                            estimatedRemainingLifeKm < 50000 -> colors.gaugeOrange
                            else -> colors.gaugeGreen
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.chain_last_checked),
                        fontSize = 10.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = lastCheckedText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }

            // Warning message
            if (isCritical || isWarning || rattleDetected) {
                Spacer(modifier = Modifier.height(12.dp))

                val warningText = when {
                    rattleDetected -> stringResource(R.string.chain_rattle_active)
                    healthStatus == ChainHealthStatus.CRITICAL ->
                        stringResource(R.string.chain_status_critical)
                    healthStatus == ChainHealthStatus.WEAR_DETECTED ->
                        stringResource(R.string.chain_status_wear)
                    else -> ""
                }
                val warningColor = when {
                    rattleDetected -> colors.gaugeRed
                    healthStatus == ChainHealthStatus.CRITICAL -> colors.gaugeRed
                    healthStatus == ChainHealthStatus.WEAR_DETECTED -> colors.gaugeOrange
                    else -> colors.textSecondary
                }

                if (warningText.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = warningColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = warningColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = warningText,
                                fontSize = 11.sp,
                                color = warningColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Diagnostic button
            if (onDiagnosticClick != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDiagnosticClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedHealthColor.copy(alpha = 0.2f),
                        contentColor = animatedHealthColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.chain_check_now),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Kettenspanner-Gesundheitsstatus
 */
enum class ChainHealthStatus(val label: String) {
    HEALTHY("Gesund"),
    WEAR_DETECTED("Verschleiß"),
    CRITICAL("Kritisch");

    fun color(colors: AppColors): Color = when (this) {
        HEALTHY -> colors.gaugeGreen
        WEAR_DETECTED -> colors.gaugeOrange
        CRITICAL -> colors.gaugeRed
    }

    companion object {
        fun fromScore(score: Int): ChainHealthStatus = when {
            score >= 80 -> HEALTHY
            score >= 50 -> WEAR_DETECTED
            else -> CRITICAL
        }
    }
}
