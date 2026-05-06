package com.canopobd.ui.comfort

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.LocalAppColors
import com.canopobd.ui.theme.AppColors

data class ComfortState(
    val centralLock: Boolean = true,
    val driverWindow: Float = 0f,
    val passengerWindow: Float = 0f,
    val rearLeftWindow: Float = 0f,
    val rearRightWindow: Float = 0f,
    val mirrorFold: Boolean = false,
    val mirrorHeating: Boolean = false,
    val rearWindowHeating: Boolean = false,
    val frontHeating: Boolean = false,
    val steeringWheelHeating: Boolean = false,
    val ambientLight: Int = 0,
    val daylightSensor: Boolean = false,
    val rainSensor: Int = 0,
    val wiperSpeed: Int = 0,
    val comingHome: Boolean = false,
    val leavingHome: Boolean = false,
    val corneringLight: Boolean = false,
    val drlMode: Int = 0
)

data class ComfortCommand(
    val action: ComfortAction,
    val value: Any? = null
)

enum class ComfortAction {
    LOCK, UNLOCK,
    WINDOW_DRIVER_UP, WINDOW_DRIVER_DOWN, WINDOW_DRIVER_STOP,
    WINDOW_PASSENGER_UP, WINDOW_PASSENGER_DOWN, WINDOW_PASSENGER_STOP,
    WINDOW_REAR_LEFT_UP, WINDOW_REAR_LEFT_DOWN, WINDOW_REAR_LEFT_STOP,
    WINDOW_REAR_RIGHT_UP, WINDOW_REAR_RIGHT_DOWN, WINDOW_REAR_RIGHT_STOP,
    WINDOW_ALL_UP, WINDOW_ALL_DOWN,
    MIRROR_FOLD, MIRROR_UNFOLD,
    MIRROR_HEATING_ON, MIRROR_HEATING_OFF,
    REAR_HEATING_ON, REAR_HEATING_OFF,
    FRONT_HEATING_ON, FRONT_HEATING_OFF,
    STEERING_HEATING_ON, STEERING_HEATING_OFF,
    AMBIENT_LIGHT_INCREASE, AMBIENT_LIGHT_DECREASE,
    COMING_HOME_ON, COMING_HOME_OFF,
    LEAVING_HOME_ON, LEAVING_HOME_OFF,
    CORNERING_LIGHT_ON, CORNERING_LIGHT_OFF,
    DRL_MODE_AUTO, DRL_MODE_ON, DRL_MODE_OFF,
    WIPER_OFF, WIPER_LOW, WIPER_MEDIUM, WIPER_HIGH, WIPER_AUTO
}

@Composable
fun ComfortControlDialog(
    onCommand: (ComfortCommand) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    var comfortState by remember { mutableStateOf(ComfortState()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Komfort-Steuerung", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Zentralverriegelung", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    CentralLockCard(
                        isLocked = comfortState.centralLock,
                        onLock = { onCommand(ComfortCommand(ComfortAction.LOCK)) },
                        onUnlock = { onCommand(ComfortCommand(ComfortAction.UNLOCK)) },
                        colors = colors
                    )
                }

                item {
                    Text("Fensterheber", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    WindowControlCard(
                        state = comfortState,
                        onCommand = { action, value ->
                            onCommand(ComfortCommand(action, value))
                            comfortState = updateWindowState(comfortState, action, value)
                        },
                        colors = colors
                    )
                }

                item {
                    Text("Spiegel", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    MirrorControlCard(
                        isFolded = comfortState.mirrorFold,
                        isHeatingOn = comfortState.mirrorHeating,
                        onFold = { onCommand(ComfortCommand(ComfortAction.MIRROR_FOLD)) },
                        onUnfold = { onCommand(ComfortCommand(ComfortAction.MIRROR_UNFOLD)) },
                        onHeatingToggle = {
                            if (comfortState.mirrorHeating) {
                                onCommand(ComfortCommand(ComfortAction.MIRROR_HEATING_OFF))
                            } else {
                                onCommand(ComfortCommand(ComfortAction.MIRROR_HEATING_ON))
                            }
                            comfortState = comfortState.copy(mirrorHeating = !comfortState.mirrorHeating)
                        },
                        colors = colors
                    )
                }

                item {
                    Text("Scheibenheizung", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    HeatingCard(
                        rearHeating = comfortState.rearWindowHeating,
                        frontHeating = comfortState.frontHeating,
                        steeringHeating = comfortState.steeringWheelHeating,
                        onRearToggle = {
                            onCommand(ComfortCommand(if (comfortState.rearWindowHeating) ComfortAction.REAR_HEATING_OFF else ComfortAction.REAR_HEATING_ON))
                            comfortState = comfortState.copy(rearWindowHeating = !comfortState.rearWindowHeating)
                        },
                        onFrontToggle = {
                            onCommand(ComfortCommand(if (comfortState.frontHeating) ComfortAction.FRONT_HEATING_OFF else ComfortAction.FRONT_HEATING_ON))
                            comfortState = comfortState.copy(frontHeating = !comfortState.frontHeating)
                        },
                        onSteeringToggle = {
                            onCommand(ComfortCommand(if (comfortState.steeringWheelHeating) ComfortAction.STEERING_HEATING_OFF else ComfortAction.STEERING_HEATING_ON))
                            comfortState = comfortState.copy(steeringWheelHeating = !comfortState.steeringWheelHeating)
                        },
                        colors = colors
                    )
                }

                item {
                    Text("Beleuchtung", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    LightingCard(
                        ambientLevel = comfortState.ambientLight,
                        comingHome = comfortState.comingHome,
                        leavingHome = comfortState.leavingHome,
                        corneringLight = comfortState.corneringLight,
                        drlMode = comfortState.drlMode,
                        onAmbientIncrease = {
                            if (comfortState.ambientLight < 10) {
                                onCommand(ComfortCommand(ComfortAction.AMBIENT_LIGHT_INCREASE))
                                comfortState = comfortState.copy(ambientLight = comfortState.ambientLight + 1)
                            }
                        },
                        onAmbientDecrease = {
                            if (comfortState.ambientLight > 0) {
                                onCommand(ComfortCommand(ComfortAction.AMBIENT_LIGHT_DECREASE))
                                comfortState = comfortState.copy(ambientLight = comfortState.ambientLight - 1)
                            }
                        },
                        onComingHomeToggle = {
                            onCommand(ComfortCommand(if (comfortState.comingHome) ComfortAction.COMING_HOME_OFF else ComfortAction.COMING_HOME_ON))
                            comfortState = comfortState.copy(comingHome = !comfortState.comingHome)
                        },
                        onLeavingHomeToggle = {
                            onCommand(ComfortCommand(if (comfortState.leavingHome) ComfortAction.LEAVING_HOME_OFF else ComfortAction.LEAVING_HOME_ON))
                            comfortState = comfortState.copy(leavingHome = !comfortState.leavingHome)
                        },
                        onCorneringToggle = {
                            onCommand(ComfortCommand(if (comfortState.corneringLight) ComfortAction.CORNERING_LIGHT_OFF else ComfortAction.CORNERING_LIGHT_ON))
                            comfortState = comfortState.copy(corneringLight = !comfortState.corneringLight)
                        },
                        onDrlChange = { mode ->
                            val action = when (mode) {
                                0 -> ComfortAction.DRL_MODE_AUTO
                                1 -> ComfortAction.DRL_MODE_ON
                                else -> ComfortAction.DRL_MODE_OFF
                            }
                            onCommand(ComfortCommand(action))
                            comfortState = comfortState.copy(drlMode = mode)
                        },
                        colors = colors
                    )
                }

                item {
                    Text("Scheibenwischer", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                item {
                    WiperControlCard(
                        speed = comfortState.wiperSpeed,
                        onSpeedChange = { newSpeed ->
                            val action = when (newSpeed) {
                                0 -> ComfortAction.WIPER_OFF
                                1 -> ComfortAction.WIPER_LOW
                                2 -> ComfortAction.WIPER_MEDIUM
                                3 -> ComfortAction.WIPER_HIGH
                                else -> ComfortAction.WIPER_AUTO
                            }
                            onCommand(ComfortCommand(action))
                            comfortState = comfortState.copy(wiperSpeed = newSpeed)
                        },
                        colors = colors
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Hinweis: Funktionen abhaengig von Fahrzeugausstattung verfuegbar.",
                        color = colors.textDim,
                        fontSize = 10.sp
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun CentralLockCard(
    isLocked: Boolean,
    onLock: () -> Unit,
    onUnlock: () -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = null,
                tint = if (isLocked) colors.gaugeGreen else colors.gaugeOrange,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onLock,
                colors = ButtonDefaults.buttonColors(containerColor = colors.gaugeGreen),
                modifier = Modifier.weight(1f)
            ) {
                Text("VERRIEGELN")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = colors.gaugeOrange),
                modifier = Modifier.weight(1f)
            ) {
                Text("ENTRIEGELN")
            }
        }
    }
}

@Composable
private fun WindowControlCard(
    state: ComfortState,
    onCommand: (ComfortAction, Any?) -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WindowButton("Fahrer", state.driverWindow, icons = listOf(
                    ComfortAction.WINDOW_DRIVER_UP to Icons.Filled.KeyboardArrowUp,
                    ComfortAction.WINDOW_DRIVER_DOWN to Icons.Filled.KeyboardArrowDown,
                    ComfortAction.WINDOW_DRIVER_STOP to Icons.Filled.Stop
                ), onCommand = onCommand, colors = colors)
                WindowButton("Beifahrer", state.passengerWindow, icons = listOf(
                    ComfortAction.WINDOW_PASSENGER_UP to Icons.Filled.KeyboardArrowUp,
                    ComfortAction.WINDOW_PASSENGER_DOWN to Icons.Filled.KeyboardArrowDown,
                    ComfortAction.WINDOW_PASSENGER_STOP to Icons.Filled.Stop
                ), onCommand = onCommand, colors = colors)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WindowButton("Hinten L", state.rearLeftWindow, icons = listOf(
                    ComfortAction.WINDOW_REAR_LEFT_UP to Icons.Filled.KeyboardArrowUp,
                    ComfortAction.WINDOW_REAR_LEFT_DOWN to Icons.Filled.KeyboardArrowDown,
                    ComfortAction.WINDOW_REAR_LEFT_STOP to Icons.Filled.Stop
                ), onCommand = onCommand, colors = colors)
                WindowButton("Hinten R", state.rearRightWindow, icons = listOf(
                    ComfortAction.WINDOW_REAR_RIGHT_UP to Icons.Filled.KeyboardArrowUp,
                    ComfortAction.WINDOW_REAR_RIGHT_DOWN to Icons.Filled.KeyboardArrowDown,
                    ComfortAction.WINDOW_REAR_RIGHT_STOP to Icons.Filled.Stop
                ), onCommand = onCommand, colors = colors)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onCommand(ComfortAction.WINDOW_ALL_UP, null) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, modifier = Modifier.size(16.dp))
                    Text("Alle Hoch", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onCommand(ComfortAction.WINDOW_ALL_DOWN, null) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                    Text("Alle Runter", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun WindowButton(
    label: String,
    progress: Float,
    icons: List<Pair<ComfortAction, androidx.compose.ui.graphics.vector.ImageVector>>,
    onCommand: (ComfortAction, Any?) -> Unit,
    colors: AppColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = colors.textSecondary, fontSize = 10.sp)
        Row {
            icons.forEach { (action, icon) ->
                IconButton(
                    onClick = { onCommand(action, null) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(icon, label, tint = colors.accent, modifier = Modifier.size(20.dp))
                }
            }
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.width(80.dp).height(4.dp),
            color = colors.gaugeGreen,
            trackColor = colors.surface
        )
    }
}

@Composable
private fun MirrorControlCard(
    isFolded: Boolean,
    isHeatingOn: Boolean,
    onFold: () -> Unit,
    onUnfold: () -> Unit,
    onHeatingToggle: () -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFold) {
                    Icon(Icons.Filled.FlipToBack, "Spiegel Einklappen", tint = colors.accent)
                }
                Text("Einklappen", color = colors.textDim, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onUnfold) {
                    Icon(Icons.Filled.FlipToFront, "Spiegel Ausklappen", tint = colors.accent)
                }
                Text("Ausklappen", color = colors.textDim, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onHeatingToggle) {
                    Icon(
                        Icons.Filled.WbSunny,
                        "Spiegelheizung",
                        tint = if (isHeatingOn) colors.gaugeOrange else colors.textDim
                    )
                }
                Text("Spiegelheizung", color = colors.textDim, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun HeatingCard(
    rearHeating: Boolean,
    frontHeating: Boolean,
    steeringHeating: Boolean,
    onRearToggle: () -> Unit,
    onFrontToggle: () -> Unit,
    onSteeringToggle: () -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeatingToggle("Heckscheibe", rearHeating, Icons.Filled.CarRental, onRearToggle, colors)
            HeatingToggle("Frontscheibe", frontHeating, Icons.Filled.Window, onFrontToggle, colors)
            HeatingToggle("Lenkrad", steeringHeating, Icons.Filled.TripOrigin, onSteeringToggle, colors)
        }
    }
}

@Composable
private fun HeatingToggle(
    label: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: () -> Unit,
    colors: AppColors
) {
    val activeColor by animateColorAsState(if (isActive) colors.gaugeOrange else colors.textDim, label = "heating")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = if (isActive) colors.gaugeOrange.copy(alpha = 0.2f) else colors.surface,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = activeColor, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = colors.textDim, fontSize = 10.sp)
    }
}

@Composable
private fun LightingCard(
    ambientLevel: Int,
    comingHome: Boolean,
    leavingHome: Boolean,
    corneringLight: Boolean,
    drlMode: Int,
    onAmbientIncrease: () -> Unit,
    onAmbientDecrease: () -> Unit,
    onComingHomeToggle: () -> Unit,
    onLeavingHomeToggle: () -> Unit,
    onCorneringToggle: () -> Unit,
    onDrlChange: (Int) -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        IconButton(onClick = onAmbientDecrease, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Remove, "Dimmen", tint = colors.accent)
                        }
                        Text("$ambientLevel", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onAmbientIncrease, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Add, "Heller", tint = colors.accent)
                        }
                    }
                    Text("Ambient", color = colors.textDim, fontSize = 10.sp)
                }
                LightingToggle("Coming Home", comingHome, Icons.Filled.Home, onComingHomeToggle, colors)
                LightingToggle("Leaving Home", leavingHome, Icons.Filled.Home, onLeavingHomeToggle, colors)
                LightingToggle("Eckenlicht", corneringLight, Icons.Filled.TripOrigin, onCorneringToggle, colors)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("DRL: ", color = colors.textDim, fontSize = 11.sp)
                listOf("Auto" to 0, "An" to 1, "Aus" to 2).forEach { (label, mode) ->
                    TextButton(
                        onClick = { onDrlChange(mode) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (drlMode == mode) colors.accent else colors.textDim
                        ),
                        modifier = Modifier.size(56.dp, 32.dp)
                    ) {
                        Text(label, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LightingToggle(
    label: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: () -> Unit,
    colors: AppColors
) {
    val activeColor by animateColorAsState(if (isActive) colors.gaugeYellow else colors.textDim, label = "light")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = if (isActive) colors.gaugeYellow.copy(alpha = 0.2f) else colors.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = activeColor, modifier = Modifier.size(20.dp))
            }
        }
        Text(label, color = colors.textDim, fontSize = 8.sp, maxLines = 1)
    }
}

@Composable
private fun WiperControlCard(
    speed: Int,
    onSpeedChange: (Int) -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                0 to Icons.Filled.Stop,
                1 to Icons.Filled.WaterDrop,
                2 to Icons.Filled.Grain,
                3 to Icons.Filled.Thunderstorm,
                -1 to Icons.Filled.AutoAwesome
            ).forEach { (mode, icon) ->
                val isSelected = if (mode == -1) false else speed == mode
                val selectedColor = when (mode) {
                    0 -> colors.textDim
                    1 -> colors.gaugeGreen
                    2 -> colors.gaugeYellow
                    3 -> colors.gaugeOrange
                    else -> colors.gaugeCyan
                }
                Surface(
                    onClick = { onSpeedChange(if (mode == -1) 0 else mode) },
                    shape = CircleShape,
                    color = if (isSelected) selectedColor.copy(alpha = 0.2f) else colors.surface,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            when (mode) {
                                -1 -> "Auto"
                                0 -> "Aus"
                                1 -> "Stufe 1"
                                2 -> "Stufe 2"
                                else -> "Stufe 3"
                            },
                            tint = if (isSelected) selectedColor else colors.textDim,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun updateWindowState(state: ComfortState, action: ComfortAction, value: Any?): ComfortState {
    return when (action) {
        ComfortAction.WINDOW_DRIVER_UP -> state.copy(driverWindow = (state.driverWindow + 0.2f).coerceAtMost(1f))
        ComfortAction.WINDOW_DRIVER_DOWN -> state.copy(driverWindow = (state.driverWindow - 0.2f).coerceAtLeast(0f))
        ComfortAction.WINDOW_DRIVER_STOP -> state
        ComfortAction.WINDOW_PASSENGER_UP -> state.copy(passengerWindow = (state.passengerWindow + 0.2f).coerceAtMost(1f))
        ComfortAction.WINDOW_PASSENGER_DOWN -> state.copy(passengerWindow = (state.passengerWindow - 0.2f).coerceAtLeast(0f))
        ComfortAction.WINDOW_PASSENGER_STOP -> state
        ComfortAction.WINDOW_REAR_LEFT_UP -> state.copy(rearLeftWindow = (state.rearLeftWindow + 0.2f).coerceAtMost(1f))
        ComfortAction.WINDOW_REAR_LEFT_DOWN -> state.copy(rearLeftWindow = (state.rearLeftWindow - 0.2f).coerceAtLeast(0f))
        ComfortAction.WINDOW_REAR_LEFT_STOP -> state
        ComfortAction.WINDOW_REAR_RIGHT_UP -> state.copy(rearRightWindow = (state.rearRightWindow + 0.2f).coerceAtMost(1f))
        ComfortAction.WINDOW_REAR_RIGHT_DOWN -> state.copy(rearRightWindow = (state.rearRightWindow - 0.2f).coerceAtLeast(0f))
        ComfortAction.WINDOW_REAR_RIGHT_STOP -> state
        ComfortAction.WINDOW_ALL_UP -> state.copy(
            driverWindow = 1f, passengerWindow = 1f, rearLeftWindow = 1f, rearRightWindow = 1f
        )
        ComfortAction.WINDOW_ALL_DOWN -> state.copy(
            driverWindow = 0f, passengerWindow = 0f, rearLeftWindow = 0f, rearRightWindow = 0f
        )
        else -> state
    }
}
