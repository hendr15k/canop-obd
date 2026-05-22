package com.canopobd.ui.timingchain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.CarProfile
import com.canopobd.data.model.TimingChainPhase
import com.canopobd.data.model.TimingChainState
import com.canopobd.ui.theme.LocalAppColors
import com.canopobd.ui.theme.AppColors

@Composable
fun TimingChainMonitorDialog(
    chainState: TimingChainState,
    carProfile: CarProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val score = chainState.healthScore
    val scoreColor = chainScoreColor(score, colors)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.SettingsApplications,
                    contentDescription = null,
                    tint = scoreColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.timing_chain_title),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ChainHealthCard(chainState = chainState, colors = colors)
                }
                item {
                    ColdStartAnalysisCard(chainState = chainState, colors = colors)
                }
                item {
                    ChainPhaseCard(chainState = chainState, colors = colors)
                }
                item {
                    ChainInfoCard(carProfile = carProfile, colors = colors)
                }
                item {
                    ChainWarningsCard(chainState = chainState, colors = colors)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = colors.accent)
            }
        }
    )
}

@Composable
private fun ChainHealthCard(chainState: TimingChainState, colors: AppColors) {
    val score = chainState.healthScore
    val scoreColor = chainScoreColor(score, colors)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.chain_health_score),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when {
                            score >= 80 -> Icons.Filled.CheckCircle
                            score >= 50 -> Icons.Filled.Warning
                            else -> Icons.Filled.Error
                        },
                        contentDescription = null,
                        tint = scoreColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$score / 100",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((score / 100f).toFloat())
                        .background(scoreColor)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = chainHealthMessage(score),
                fontSize = 11.sp,
                color = scoreColor
            )
        }
    }
}

@Composable
private fun ColdStartAnalysisCard(chainState: TimingChainState, colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.cold_start_analysis),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                if (chainState.coldStartRattleDetected) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeOrange.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = colors.gaugeOrange,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.rattle_detected),
                                fontSize = 10.sp,
                                color = colors.gaugeOrange
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (chainState.avgRpmCold > 0) "%.0f".format(chainState.avgRpmCold) else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(stringResource(R.string.rpm_cold), fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (chainState.avgRpmWarm > 0) "%.0f".format(chainState.avgRpmWarm) else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.gaugeGreen
                    )
                    Text(stringResource(R.string.rpm_warm), fontSize = 10.sp, color = colors.textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (chainState.rpmDeviationCold > 0) "%.1f%%".format(chainState.rpmDeviationCold) else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = chainState.rpmDeviationCold.let { dev ->
                            when {
                                dev < 2.0 -> colors.gaugeGreen
                                dev < 5.0 -> colors.gaugeOrange
                                else -> colors.gaugeRed
                            }
                        }
                    )
                    Text(stringResource(R.string.rpm_variation), fontSize = 10.sp, color = colors.textSecondary)
                }
            }

            if (chainState.rattleDurationMs > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${stringResource(R.string.rattle_duration)}: ${chainState.rattleDurationMs}ms",
                    fontSize = 10.sp,
                    color = colors.gaugeOrange
                )
            }
        }
    }
}

@Composable
private fun ChainPhaseCard(chainState: TimingChainState, colors: AppColors) {
    val phaseColor = when (chainState.phase) {
        TimingChainPhase.HEALTHY, TimingChainPhase.STABLE -> colors.gaugeGreen
        TimingChainPhase.WARNING -> colors.gaugeOrange
        TimingChainPhase.CRITICAL, TimingChainPhase.COLD_RATTLE -> colors.gaugeRed
        else -> colors.textSecondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.current_status),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = phaseColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = chainState.phase.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = phaseColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = chainState.phase.description,
                fontSize = 11.sp,
                color = colors.textSecondary
            )

            if (chainState.statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chainState.statusMessage,
                    fontSize = 10.sp,
                    color = phaseColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.samples)}: ${chainState.recordedSamples}",
                fontSize = 10.sp,
                color = colors.textDim
            )
        }
    }
}

@Composable
private fun ChainInfoCard(@Suppress("UNUSED_PARAMETER") carProfile: CarProfile, colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.gaugeRed.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = colors.gaugeOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.timing_chain_warning),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gaugeOrange
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.chain_warning_text),
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ChainWarningsCard(chainState: TimingChainState, colors: AppColors) {
    val warnings = mutableListOf<Pair<String, String>>()

    if (chainState.coldStartRattleDetected) {
        warnings.add(
            Pair(
                stringResource(R.string.cold_rattle_symptom),
                stringResource(R.string.cold_rattle_action)
            )
        )
    }
    if (chainState.rpmDeviationCold > 5.0) {
        warnings.add(
            Pair(
                stringResource(R.string.high_rpm_variation),
                stringResource(R.string.high_rpm_action)
            )
        )
    }
    if (chainState.healthScore < 60) {
        warnings.add(
            Pair(
                stringResource(R.string.chain_health_low),
                stringResource(R.string.chain_health_action)
            )
        )
    }

    if (warnings.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.gaugeGreen.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = colors.gaugeGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.no_chain_issues),
                    fontSize = 11.sp,
                    color = colors.gaugeGreen
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.surface.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                warnings.forEach { (symptom, action) ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = colors.gaugeOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = symptom, fontSize = 10.sp, color = colors.gaugeOrange, fontWeight = FontWeight.Bold)
                            Text(text = action, fontSize = 9.sp, color = colors.textSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

private fun chainScoreColor(score: Int, colors: AppColors) = when {
    score >= 80 -> colors.gaugeGreen
    score >= 50 -> colors.gaugeOrange
    else -> colors.gaugeRed
}

private fun chainHealthMessage(score: Int): String {
    return when {
        score >= 90 -> "Steuerkette in ausgezeichnetem Zustand"
        score >= 80 -> "Steuerkette in gutem Zustand"
        score >= 70 -> "Leichte Verschleißerscheinungen — weiter beobachten"
        score >= 60 -> "Verschleiß erkannt — Inspektion bei nächster Wartung"
        score >= 50 -> "Steuerkette warnung —尽早 prüfen lassen"
        score >= 30 -> "Steuerkette kritisch — sofort prüfen!"
        else -> "Steuerkette defekt — nicht weiterfahren!"
    }
}
