package com.canopobd.ui.turbo

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.TurboCoolDownState
import com.canopobd.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun TurboCoolDownBanner(
    coolDownState: TurboCoolDownState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!coolDownState.isActive) return

    val colors = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.gaugeOrange.copy(alpha = 0.2f * alpha)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Whatshot,
                contentDescription = null,
                tint = colors.gaugeOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.turbo_cooldown_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gaugeOrange
                )
                Text(
                    text = coolDownState.statusMessage,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(coolDownState.progress.coerceIn(0f, 1f))
                                .background(colors.gaugeOrange)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${coolDownState.secondsRemaining}s",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.gaugeOrange
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun TurboCoolDownDialog(
    coolDownState: TurboCoolDownState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = colors.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Whatshot,
                    contentDescription = null,
                    tint = colors.gaugeOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.turbo_cooldown_dialog_title),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.turbo_cooldown_explanation),
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(colors.gaugeOrange.copy(alpha = 0.2f * pulseAlpha)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${coolDownState.secondsRemaining}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeOrange
                        )
                        Text(
                            text = "Sekunden",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                @Suppress("DEPRECATION")
                val progressFloat = coolDownState.progress.coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = progressFloat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = colors.gaugeOrange,
                    trackColor = colors.surface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (coolDownState.triggeredByRpm) {
                    TriggerBadge(
                        icon = Icons.Filled.Speed,
                        text = stringResource(R.string.triggered_by_rpm),
                        colors = colors
                    )
                }
                if (coolDownState.triggeredByBoost) {
                    TriggerBadge(
                        icon = Icons.Filled.Air,
                        text = stringResource(R.string.triggered_by_boost),
                        colors = colors
                    )
                }
                if (coolDownState.triggeredBySpeed) {
                    TriggerBadge(
                        icon = Icons.Filled.DirectionsCar,
                        text = stringResource(R.string.triggered_by_speed),
                        colors = colors
                    )
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
private fun TriggerBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    colors: com.canopobd.ui.theme.AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = colors.surface.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.gaugeOrange,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 11.sp, color = colors.textSecondary)
        }
    }
}
