package com.canopobd.ui.window

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.domain.WindowAction
import com.canopobd.data.domain.WindowControlMonitor
import com.canopobd.data.domain.WindowState
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

private const val SAFETY_HIGH = 85
private const val SAFETY_MEDIUM = 60

@Suppress("UNUSED_PARAMETER", "FunctionNaming", "LongMethod")
@Composable
fun WindowControlDialog(
    initialState: WindowState = WindowState(),
    onCommand: (WindowAction) -> Unit,
    onDismiss: () -> Unit,
    externalState: WindowState? = null,
    onWindowStateChange: ((WindowState) -> Unit)? = null
) {
    val colors = LocalAppColors.current
    var localState by remember { mutableStateOf(externalState ?: initialState) }
    val evaluation = remember(localState) {
        WindowControlMonitor.evaluate(localState).run {
            WindowControlMonitorEval(openWindowCount, safetyScore, warning)
        }
    }

    LaunchedEffect(externalState) {
        if (externalState != null) {
            localState = externalState
        }
    }

    fun sendAction(action: WindowAction) {
        localState = WindowControlMonitor.updateStateFromAction(localState, action)
        onWindowStateChange?.invoke(localState)
        onCommand(action)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { WindowControlDialogTitle(colors = colors, onDismiss = onDismiss) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WindowStatusSummary(evaluation = evaluation, colors = colors)

                if (evaluation.warning != null) {
                    WindowWarningBanner(warning = evaluation.warning, colors = colors)
                }

                WindowCard(
                    label = "Fahrer", position = localState.driverPos, colors = colors,
                    onUp = { sendAction(WindowAction.DRIVER_UP) },
                    onDown = { sendAction(WindowAction.DRIVER_DOWN) },
                    onStop = { sendAction(WindowAction.DRIVER_STOP) }
                )
                WindowCard(
                    label = "Beifahrer", position = localState.passengerPos, colors = colors,
                    onUp = { sendAction(WindowAction.PASSENGER_UP) },
                    onDown = { sendAction(WindowAction.PASSENGER_DOWN) },
                    onStop = { sendAction(WindowAction.PASSENGER_STOP) }
                )
                WindowCard(
                    label = "Hinten Links", position = localState.rearLeftPos, colors = colors,
                    onUp = { sendAction(WindowAction.REAR_LEFT_UP) },
                    onDown = { sendAction(WindowAction.REAR_LEFT_DOWN) },
                    onStop = { sendAction(WindowAction.REAR_LEFT_STOP) }
                )
                WindowCard(
                    label = "Hinten Rechts", position = localState.rearRightPos, colors = colors,
                    onUp = { sendAction(WindowAction.REAR_RIGHT_UP) },
                    onDown = { sendAction(WindowAction.REAR_RIGHT_DOWN) },
                    onStop = { sendAction(WindowAction.REAR_RIGHT_STOP) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WindowActionButton(
                        label = "Alle Hoch",
                        icon = Icons.Filled.KeyboardArrowUp,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                        onClick = { sendAction(WindowAction.ALL_UP) }
                    )
                    WindowActionButton(
                        label = "Alle Runter",
                        icon = Icons.Filled.KeyboardArrowDown,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                        onClick = { sendAction(WindowAction.ALL_DOWN) }
                    )
                }

                WindowInfoBanner(colors = colors)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schliessen", color = colors.accent)
            }
        }
    )
}

@Suppress("FunctionNaming")
@Composable
private fun WindowControlDialogTitle(colors: AppColors, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Window,
                contentDescription = null,
                tint = colors.gaugeCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Fenstersteuerung", color = colors.textPrimary, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowStatusSummary(
    evaluation: WindowControlMonitorEval,
    colors: AppColors
) {
    val scoreColor = when {
        evaluation.safetyScore >= SAFETY_HIGH -> colors.gaugeGreen
        evaluation.safetyScore >= SAFETY_MEDIUM -> colors.gaugeOrange
        else -> colors.warning
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Offene Fenster", color = colors.textDim, fontSize = 10.sp)
                Text(
                    "${evaluation.openWindowCount}/4",
                    color = colors.textPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Sicherheit", color = colors.textDim, fontSize = 10.sp)
                Text(
                    "${evaluation.safetyScore}%",
                    color = scoreColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

internal data class WindowControlMonitorEval(
    val openWindowCount: Int,
    val safetyScore: Int,
    val warning: String?
)

@Suppress("FunctionNaming")
@Composable
private fun WindowWarningBanner(warning: String, colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.warning.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(warning, color = colors.textSecondary, fontSize = 11.sp)
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowCard(
    label: String,
    position: Int,
    colors: AppColors,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (position > 0) {
                                colors.gaugeCyan.copy(alpha = 0.2f)
                            } else {
                                colors.surfaceElevated
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$position%",
                        color = if (position > 0) colors.gaugeCyan else colors.textDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WindowIconButton(Icons.Filled.KeyboardArrowUp, "Schliessen", colors, onUp)
                WindowIconButton(Icons.Filled.KeyboardArrowDown, "Oeffnen", colors, onDown)
                WindowIconButton(Icons.Filled.Pause, "Stopp", colors, onStop)
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowIconButton(
    icon: ImageVector,
    contentDesc: String,
    colors: AppColors,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.surfaceElevated,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDesc, tint = colors.accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowActionButton(
    label: String,
    icon: ImageVector,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isUp = label == "Alle Hoch"
    val bgColor by animateColorAsState(
        if (isUp) colors.gaugeGreen.copy(alpha = 0.15f) else colors.gaugeOrange.copy(alpha = 0.15f),
        label = "windowActionBg"
    )
    val tintColor = if (isUp) colors.gaugeGreen else colors.gaugeOrange

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = tintColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowInfoBanner(colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.gaugeCyan.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = colors.gaugeCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Fenstersteuerung ueber BCM (UDS DID 0xFF02)." +
                    " ELM327-Verbindung erforderlich.",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}
