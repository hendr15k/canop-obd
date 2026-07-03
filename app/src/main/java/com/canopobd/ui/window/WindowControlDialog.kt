package com.canopobd.ui.window

import androidx.compose.animation.AnimatedVisibility
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
import com.canopobd.data.domain.WindowPresets
import com.canopobd.data.domain.WindowState
import com.canopobd.data.domain.WindowTarget
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

private const val SAFETY_HIGH = 85
private const val SAFETY_MEDIUM = 60

private const val WINDOW_INDEX_DRIVER = 1
private const val WINDOW_INDEX_PASSENGER = 2
private const val WINDOW_INDEX_REAR_LEFT = 3
private const val WINDOW_INDEX_REAR_RIGHT = 4

private val WINDOW_TARGETS: List<Triple<WindowTarget, String, Int>> = listOf(
    Triple(WindowTarget.DRIVER, "Fahrer", WINDOW_INDEX_DRIVER),
    Triple(WindowTarget.PASSENGER, "Beifahrer", WINDOW_INDEX_PASSENGER),
    Triple(WindowTarget.REAR_LEFT, "Hinten Links", WINDOW_INDEX_REAR_LEFT),
    Triple(WindowTarget.REAR_RIGHT, "Hinten Rechts", WINDOW_INDEX_REAR_RIGHT)
)

@Suppress("UNUSED_PARAMETER", "FunctionNaming", "LongMethod", "LongParameterList")
@Composable
fun WindowControlDialog(
    initialState: WindowState = WindowState(),
    onCommand: (WindowAction) -> Unit,
    onDismiss: () -> Unit,
    onSetPosition: (WindowTarget, Int) -> Unit = { _, _ -> },
    onVentilateAll: () -> Unit = {},
    onToggleChildLock: () -> Unit = {},
    onPollStatus: () -> Unit = {},
    externalState: WindowState? = null,
    onWindowStateChange: ((WindowState) -> Unit)? = null,
    childLock: Boolean = false,
    isMoving: Boolean = false
) {
    val colors = LocalAppColors.current
    var localState by remember { mutableStateOf(externalState ?: initialState) }
    val evaluation = remember(localState) {
        WindowControlMonitor.evaluate(localState).run {
            WindowControlMonitorEval(openWindowCount, safetyScore, warning)
        }
    }
    var pendingAction by remember { mutableStateOf<WindowAction?>(null) }

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

    if (pendingAction != null) {
        WindowConfirmDialog(
            action = pendingAction!!,
            onConfirm = {
                sendAction(pendingAction!!)
                pendingAction = null
            },
            onDismiss = { pendingAction = null }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            WindowDialogTitle(
                colors = colors,
                isMoving = isMoving,
                childLock = childLock,
                onDismiss = onDismiss,
                onToggleChildLock = onToggleChildLock
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WindowStatusBar(
                    evaluation = evaluation,
                    isMoving = isMoving,
                    childLock = childLock,
                    colors = colors,
                    onPollStatus = onPollStatus
                )

                if (childLock) {
                    ChildLockBanner(colors = colors, onToggleChildLock = onToggleChildLock)
                }

                if (evaluation.warning != null) {
                    WindowWarningBanner(warning = evaluation.warning, colors = colors)
                }

                WINDOW_TARGETS.forEach { (target, label, windowIndex) ->
                    val position = localState.positionFor(target)
                    WindowCard(
                        target = target,
                        label = label,
                        windowIndex = windowIndex,
                        position = position,
                        isLocked = childLock,
                        colors = colors,
                        onUp = { pendingAction = actionForTarget(target, true) },
                        onDown = { pendingAction = actionForTarget(target, false) },
                        onStop = { pendingAction = WindowAction.ALL_STOP },
                        onSetPosition = { percent -> onSetPosition(target, percent) }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WindowActionButton(
                        label = "Alle Hoch",
                        icon = Icons.Filled.KeyboardArrowUp,
                        color = colors.gaugeGreen,
                        modifier = Modifier.weight(1f),
                        enabled = !childLock,
                        onClick = { pendingAction = WindowAction.ALL_UP }
                    )
                    WindowActionButton(
                        label = "Alle Runter",
                        icon = Icons.Filled.KeyboardArrowDown,
                        color = colors.gaugeOrange,
                        modifier = Modifier.weight(1f),
                        enabled = !childLock,
                        onClick = { pendingAction = WindowAction.ALL_DOWN }
                    )
                    WindowActionButton(
                        label = "Lüften",
                        icon = Icons.Filled.Air,
                        color = colors.gaugeCyan,
                        modifier = Modifier.weight(1f),
                        enabled = !childLock,
                        onClick = onVentilateAll
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

private fun actionForTarget(target: WindowTarget, isUp: Boolean): WindowAction = when (target) {
    WindowTarget.DRIVER -> if (isUp) WindowAction.DRIVER_UP else WindowAction.DRIVER_DOWN
    WindowTarget.PASSENGER -> if (isUp) WindowAction.PASSENGER_UP else WindowAction.PASSENGER_DOWN
    WindowTarget.REAR_LEFT -> if (isUp) WindowAction.REAR_LEFT_UP else WindowAction.REAR_LEFT_DOWN
    WindowTarget.REAR_RIGHT -> if (isUp) WindowAction.REAR_RIGHT_UP else WindowAction.REAR_RIGHT_DOWN
    WindowTarget.ALL -> if (isUp) WindowAction.ALL_UP else WindowAction.ALL_DOWN
}

@Suppress("FunctionNaming")
@Composable
private fun WindowDialogTitle(
    colors: AppColors,
    isMoving: Boolean,
    childLock: Boolean,
    onDismiss: () -> Unit,
    onToggleChildLock: () -> Unit
) {
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
            Column {
                Text("Fenstersteuerung", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                if (isMoving) {
                    AnimatedVisibility(visible = true) {
                        Text(
                            "Bewegung läuft…",
                            color = colors.gaugeCyan,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleChildLock) {
                Icon(
                    imageVector = if (childLock) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = "Kindersicherung",
                    tint = if (childLock) colors.warning else colors.textSecondary
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
            }
        }
    }
}

@Suppress("FunctionNaming", "UNUSED_PARAMETER")
@Composable
private fun WindowStatusBar(
    evaluation: WindowControlMonitorEval,
    isMoving: Boolean,
    childLock: Boolean,
    colors: AppColors,
    onPollStatus: () -> Unit
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
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Offen", color = colors.textDim, fontSize = 10.sp)
                    Text(
                        "${evaluation.openWindowCount}/4",
                        color = colors.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Sicherheit", color = colors.textDim, fontSize = 10.sp)
                    Text(
                        "${evaluation.safetyScore}%",
                        color = scoreColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onPollStatus, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Status pollen",
                        tint = colors.gaugeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text("Status", color = colors.textDim, fontSize = 9.sp)
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun ChildLockBanner(colors: AppColors, onToggleChildLock: () -> Unit) {
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
                Icons.Filled.Lock,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Kindersicherung aktiv — Befehle gesperrt",
                color = colors.textSecondary,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onToggleChildLock) {
                Text("Aufheben", color = colors.warning, fontSize = 11.sp)
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

@Suppress("FunctionNaming", "UNUSED_PARAMETER", "LongParameterList", "LongMethod")
@Composable
private fun WindowCard(
    target: WindowTarget,
    label: String,
    windowIndex: Int,
    position: Int,
    isLocked: Boolean,
    colors: AppColors,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onStop: () -> Unit,
    onSetPosition: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            label,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Fenster $windowIndex",
                            color = colors.textDim,
                            fontSize = 9.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WindowIconButton(
                        Icons.Filled.KeyboardArrowUp,
                        "Schliessen",
                        colors,
                        enabled = !isLocked,
                        onClick = onUp
                    )
                    WindowIconButton(
                        Icons.Filled.KeyboardArrowDown,
                        "Oeffnen",
                        colors,
                        enabled = !isLocked,
                        onClick = onDown
                    )
                    WindowIconButton(
                        Icons.Filled.Stop,
                        "Stopp",
                        colors,
                        enabled = !isLocked,
                        onClick = onStop
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WindowPresets.POSITIONS.forEach { preset ->
                    WindowPresetChip(
                        label = preset.label,
                        percent = preset.percent,
                        colors = colors,
                        enabled = !isLocked,
                        modifier = Modifier.weight(1f),
                        onClick = { onSetPosition(preset.percent) }
                    )
                }
            }
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowPresetChip(
    label: String,
    percent: Int,
    colors: AppColors,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) colors.surfaceElevated else colors.surfaceElevated.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                color = if (enabled) colors.textPrimary else colors.textDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "auf $percent%",
                color = if (enabled) colors.textDim else colors.textDim.copy(alpha = 0.5f),
                fontSize = 8.sp
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowIconButton(
    icon: ImageVector,
    contentDesc: String,
    colors: AppColors,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        if (enabled) colors.accent else colors.textDim.copy(alpha = 0.4f),
        label = "windowIconBtnTint"
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = colors.surfaceElevated,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDesc, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowActionButton(
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (enabled) color.copy(alpha = 0.15f) else color.copy(alpha = 0.05f),
        label = "windowActionBg"
    )
    val tintColor = if (enabled) color else color.copy(alpha = 0.4f)
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = tintColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                "BCM UDS DID 0xFF02 — ELM327 erforderlich." +
                    " Auto-Stopp nach 4s.",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Suppress("FunctionNaming")
@Composable
private fun WindowConfirmDialog(
    action: WindowAction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val label = when (action) {
        WindowAction.DRIVER_UP, WindowAction.PASSENGER_UP,
        WindowAction.REAR_LEFT_UP, WindowAction.REAR_RIGHT_UP -> "schliessen"
        WindowAction.DRIVER_DOWN, WindowAction.PASSENGER_DOWN,
        WindowAction.REAR_LEFT_DOWN, WindowAction.REAR_RIGHT_DOWN -> "öffnen"
        WindowAction.ALL_UP -> "alle schliessen"
        WindowAction.ALL_DOWN -> "alle öffnen"
        WindowAction.ALL_STOP -> "alle stoppen"
        else -> "ausführen"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text("Bestätigung", color = colors.textPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Fenster wirklich $label ?",
                color = colors.textSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ausführen", color = colors.gaugeCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = colors.textSecondary)
            }
        }
    )
}
