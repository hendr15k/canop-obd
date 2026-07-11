package com.canopobd.ui.comfort

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.LocalAppColors
import com.canopobd.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val seatDriverHeating: Int = 0,  // 0=off, 1-3=level
    val seatPassengerHeating: Int = 0,
    val ambientLight: Int = 0,
    val daylightSensor: Boolean = false,
    val rainSensor: Int = 0,
    val wiperSpeed: Int = 0,
    val rearWiper: Boolean = false,
    val comingHome: Boolean = false,
    val leavingHome: Boolean = false,
    val corneringLight: Boolean = false,
    val parkingLights: Boolean = false,
    val fogLights: Boolean = false,
    val drlMode: Int = 0,
    val sunroofPosition: Int = 0,  // 0=closed, 1-100=open
    val hornActive: Boolean = false
)

data class ComfortCommand(
    val action: ComfortAction,
    val value: Any? = null
)

enum class ComfortAction {
    // Zentralverriegelung
    LOCK, UNLOCK, UNLOCK_DRIVER, UNLOCK_TAILGATE, UNLOCK_FUEL,
    
    // Fenster
    WINDOW_DRIVER_UP, WINDOW_DRIVER_DOWN, WINDOW_DRIVER_STOP,
    WINDOW_PASSENGER_UP, WINDOW_PASSENGER_DOWN, WINDOW_PASSENGER_STOP,
    WINDOW_REAR_LEFT_UP, WINDOW_REAR_LEFT_DOWN, WINDOW_REAR_LEFT_STOP,
    WINDOW_REAR_RIGHT_UP, WINDOW_REAR_RIGHT_DOWN, WINDOW_REAR_RIGHT_STOP,
    WINDOW_ALL_UP, WINDOW_ALL_DOWN,
    
    // Spiegel
    MIRROR_FOLD, MIRROR_UNFOLD,
    MIRROR_HEATING_ON, MIRROR_HEATING_OFF,
    MIRROR_MOVE_UP, MIRROR_MOVE_DOWN, MIRROR_MOVE_LEFT, MIRROR_MOVE_RIGHT,
    
    // Heizung
    REAR_HEATING_ON, REAR_HEATING_OFF,
    FRONT_HEATING_ON, FRONT_HEATING_OFF,
    STEERING_HEATING_ON, STEERING_HEATING_OFF,
    STEERING_HEATING_1, STEERING_HEATING_2, STEERING_HEATING_3,
    
    // Sitzheizung
    SEAT_DRIVER_HEAT_1, SEAT_DRIVER_HEAT_2, SEAT_DRIVER_HEAT_3, SEAT_DRIVER_OFF,
    SEAT_PASSENGER_HEAT_1, SEAT_PASSENGER_HEAT_2, SEAT_PASSENGER_HEAT_3, SEAT_PASSENGER_OFF,
    
    // Beleuchtung
    AMBIENT_LIGHT_INCREASE, AMBIENT_LIGHT_DECREASE, AMBIENT_LIGHT_MAX,
    COMING_HOME_ON, COMING_HOME_OFF,
    LEAVING_HOME_ON, LEAVING_HOME_OFF,
    CORNERING_LIGHT_ON, CORNERING_LIGHT_OFF,
    DRL_MODE_AUTO, DRL_MODE_ON, DRL_MODE_OFF,
    PARKING_LIGHTS_ON, PARKING_LIGHTS_OFF,
    FOG_LIGHTS_ON, FOG_LIGHTS_OFF,
    
    // Scheibenwischer
    WIPER_OFF, WIPER_LOW, WIPER_MEDIUM, WIPER_HIGH, WIPER_AUTO,
    WIPER_REAR_ON, WIPER_REAR_OFF,
    
    // Horn
    HORN, HORN_STOP,
    
    // Sunroof
    SUNROOF_OPEN, SUNROOF_CLOSE, SUNROOF_STOP, SUNROOF_VENT,
    
    // Custom
    CUSTOM_CAN_FRAME,
    CUSTOM_CAN_ID,
    CUSTOM_CAN_DATA
}

object WindowCommands {
    // Opel Astra J Window Control CAN IDs (PSA/Stellantis Architecture)
    // Based on research from arduino-psa-diag project
    
    // CAN IDs:
    // 74B (PORTEC) - Door Control Unit (handles windows)
    // 752 (BMF/BSI) - Body Module (central locking, comfort)
    // 76B (BCM) - Body Control Module
    
    const val CAN_ID_PORTEC = "74B"  // Door Control Unit
    const val CAN_ID_BMF = "752"     // Body Module
    const val CAN_ID_BCM = "76B"    // Body Control Module
    
    // Window Commands Format: 2E FF 02 [Window] [Direction]
    // 2E = UDS Write Data
    // FF 02 = Window Status DID
    // [Window] = 01-04 for individual windows, 00 for all
    // [Direction] = 00 up, 64 (100%) down, FF stop
    
    const val WINDOW_DRIVER_DOWN = "2E FF 02 01 64"
    const val WINDOW_DRIVER_UP = "2E FF 02 01 00"
    const val WINDOW_DRIVER_STOP = "2E FF 02 01 FF"
    
    const val WINDOW_PASSENGER_DOWN = "2E FF 02 02 64"
    const val WINDOW_PASSENGER_UP = "2E FF 02 02 00"
    const val WINDOW_PASSENGER_STOP = "2E FF 02 02 FF"
    
    const val WINDOW_REAR_LEFT_DOWN = "2E FF 02 03 64"
    const val WINDOW_REAR_LEFT_UP = "2E FF 02 03 00"
    const val WINDOW_REAR_LEFT_STOP = "2E FF 02 03 FF"
    
    const val WINDOW_REAR_RIGHT_DOWN = "2E FF 02 04 64"
    const val WINDOW_REAR_RIGHT_UP = "2E FF 02 04 00"
    const val WINDOW_REAR_RIGHT_STOP = "2E FF 02 04 FF"
    
    const val WINDOW_ALL_DOWN = "2E FF 02 00 64"
    const val WINDOW_ALL_UP = "2E FF 02 00 00"
    
    // Central Lock Commands (BMF - 752)
    const val LOCK_ALL = "2E FF 01 1F"
    const val UNLOCK_ALL = "2E FF 01 0F"
    const val UNLOCK_DRIVER = "2E FF 01 01"
    
    // Mirror Commands (BMF - 752)
    const val MIRROR_FOLD = "2E FF 03 04"
    const val MIRROR_UNFOLD = "2E FF 03 05"
    
    fun parseHexData(hexString: String): ByteArray {
        return hexString.split(" ")
            .filter { it.isNotEmpty() }
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
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
                    WindowControlInfoCard(colors = colors)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    WindowControlSimulatorCard(
                        onCommand = { action ->
                            onCommand(ComfortCommand(action))
                        },
                        onSendRealFrame = { canId, data ->
                            onCommand(ComfortCommand(ComfortAction.CUSTOM_CAN_FRAME, mapOf("canId" to canId, "data" to data)))
                        },
                        colors = colors
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CANFrameSenderCard(
                        onSend = { canId, data ->
                            onCommand(ComfortCommand(ComfortAction.CUSTOM_CAN_FRAME, mapOf("canId" to canId, "data" to data)))
                        },
                        onSendRealFrame = { canId, data ->
                            onCommand(ComfortCommand(ComfortAction.CUSTOM_CAN_FRAME, mapOf("canId" to canId, "data" to data)))
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
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schliessen", color = colors.accent)
            }
        }
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
            progress = { progress },
            modifier = Modifier.width(80.dp).height(4.dp),
            color = colors.gaugeGreen,
            trackColor = colors.surface
        )
    }
}

@Suppress("UNUSED_PARAMETER")
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

@Suppress("UNUSED_PARAMETER")
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

@Composable
private fun WindowControlInfoCard(colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.gaugeCyan.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.gaugeCyan.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = colors.gaugeCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "FENSTERSTEUERUNG HINWEIS",
                    color = colors.gaugeCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Die Fenstersteuerung ist eine Komfortfunktion und wird NICHT über Standard-OBD-II gesteuert.",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Für echte Fenstersteuerung wird benötigt:",
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier.padding(start = 8.dp)) {
                listOf(
                    "Direkter Zugriff auf Komfort-CAN (125 kbit/s)",
                    "BMF/BSI ECU Freischaltung (Seed/Key)",
                    "Spezieller CAN-Adapter (z.B. PCAN)"
                ).forEach { text ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("•", color = colors.gaugeCyan, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text, color = colors.textSecondary, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.gaugeOrange.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = colors.gaugeOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Diese App simuliert die UI für Entwicklungszwecke. CAN-Monitor zeigt aktive CAN-Nachrichten.",
                        color = colors.gaugeOrange,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WindowControlSimulatorCard(
    onCommand: (ComfortAction) -> Unit,
    onSendRealFrame: (canId: String, data: ByteArray) -> Unit,
    colors: AppColors
) {
    var activeLedDriver by remember { mutableStateOf(false) }
    var activeLedPassenger by remember { mutableStateOf(false) }
    var activeLedRearLeft by remember { mutableStateOf(false) }
    var activeLedRearRight by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var lastCommand by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    var blinkState by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Fenster-Simulator",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) colors.gaugeGreen else colors.gaugeOrange)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isConnected) "Verbunden" else "Nicht verbunden",
                        color = if (isConnected) colors.gaugeGreen else colors.gaugeOrange,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SimulatorWindowButton(
                    label = "Fahrer",
                    isActive = activeLedDriver,
                    isSending = isSending && blinkState,
                    onClick = {
                        activeLedDriver = true
                        isSending = true
                        lastCommand = "Fahrer Runter"
                        onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_DRIVER_DOWN))
                        onCommand(ComfortAction.WINDOW_DRIVER_DOWN)
                    },
                    colors = colors
                )
                SimulatorWindowButton(
                    label = "Beifahrer",
                    isActive = activeLedPassenger,
                    isSending = isSending && blinkState,
                    onClick = {
                        activeLedPassenger = true
                        isSending = true
                        lastCommand = "Beifahrer Runter"
                        onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_PASSENGER_DOWN))
                        onCommand(ComfortAction.WINDOW_PASSENGER_DOWN)
                    },
                    colors = colors
                )
                SimulatorWindowButton(
                    label = "Hinten L",
                    isActive = activeLedRearLeft,
                    isSending = isSending && blinkState,
                    onClick = {
                        activeLedRearLeft = true
                        isSending = true
                        lastCommand = "Hinten L Runter"
                        onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_REAR_LEFT_DOWN))
                        onCommand(ComfortAction.WINDOW_REAR_LEFT_DOWN)
                    },
                    colors = colors
                )
                SimulatorWindowButton(
                    label = "Hinten R",
                    isActive = activeLedRearRight,
                    isSending = isSending && blinkState,
                    onClick = {
                        activeLedRearRight = true
                        isSending = true
                        lastCommand = "Hinten R Runter"
                        onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_REAR_RIGHT_DOWN))
                        onCommand(ComfortAction.WINDOW_REAR_RIGHT_DOWN)
                    },
                    colors = colors
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        activeLedDriver = true
                        activeLedPassenger = true
                        activeLedRearLeft = true
                        activeLedRearRight = true
                        isSending = true
                        lastCommand = "Alle Runter"
                        onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_DRIVER_DOWN))
                        coroutineScope.launch {
                            delay(100)
                            onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_PASSENGER_DOWN))
                        }
                        coroutineScope.launch {
                            delay(200)
                            onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_REAR_LEFT_DOWN))
                        }
                        coroutineScope.launch {
                            delay(300)
                            onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_REAR_RIGHT_DOWN))
                        }
                        onCommand(ComfortAction.WINDOW_ALL_DOWN)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                    Text("Alle Runter", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        activeLedDriver = true
                        activeLedPassenger = true
                        activeLedRearLeft = true
                        activeLedRearRight = true
                        isSending = true
                        lastCommand = "Alle Hoch"
                        onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_DRIVER_UP))
                        coroutineScope.launch {
                            delay(100)
                            onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_PASSENGER_UP))
                        }
                        coroutineScope.launch {
                            delay(200)
                            onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_REAR_LEFT_UP))
                        }
                        coroutineScope.launch {
                            delay(300)
                            onSendRealFrame(WindowCommands.CAN_ID_PORTEC, WindowCommands.parseHexData(WindowCommands.WINDOW_REAR_RIGHT_UP))
                        }
                        onCommand(ComfortAction.WINDOW_ALL_UP)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp, null, modifier = Modifier.size(16.dp))
                    Text("Alle Hoch", fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.dark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Letzter Befehl:",
                            color = colors.textDim,
                            fontSize = 10.sp
                        )
                        Text(
                            lastCommand.ifEmpty { "---" },
                            color = colors.gaugeGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CAN-ID:",
                            color = colors.textDim,
                            fontSize = 10.sp
                        )
                        Text(
                            WindowCommands.CAN_ID_PORTEC,
                            color = colors.gaugeCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Status:",
                            color = colors.textDim,
                            fontSize = 10.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSending && blinkState) colors.gaugeGreen else colors.textDim)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isSending && blinkState) "Senden..." else "Bereit",
                                color = if (isSending && blinkState) colors.gaugeGreen else colors.textDim,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(isSending) {
        if (isSending) {
            repeat(6) {
                delay(100)
                blinkState = !blinkState
            }
            isSending = false
            blinkState = false
            activeLedDriver = false
            activeLedPassenger = false
            activeLedRearLeft = false
            activeLedRearRight = false
        }
    }
}

@Composable
private fun SimulatorWindowButton(
    label: String,
    isActive: Boolean,
    isSending: Boolean = false,
    onClick: () -> Unit,
    colors: AppColors
) {
    val ledColor by animateColorAsState(
        if (isSending) colors.gaugeGreen 
        else if (isActive) colors.gaugeGreen 
        else colors.textDim,
        label = "led"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            color = if (isSending || isActive) colors.gaugeGreen.copy(alpha = 0.2f) else colors.surfaceElevated,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSending || isActive) colors.gaugeGreen else colors.borderSubtle
            ),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.CarRental,
                    contentDescription = label,
                    tint = if (isSending || isActive) colors.gaugeGreen else colors.textDim,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            color = colors.textSecondary,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(ledColor)
        )
    }
}

@Composable
private fun CANFrameSenderCard(
    onSend: (String, String) -> Unit,
    onSendRealFrame: (canId: String, data: ByteArray) -> Unit,
    colors: AppColors
) {
    var canId by remember { mutableStateOf("752") }
    var canData by remember { mutableStateOf("02 10 03") }
    var responseText by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var blinkState by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CAN-Frame Sender",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSending && blinkState) colors.gaugeGreen else colors.textDim)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isSending) "Senden..." else "Bereit",
                        color = if (isSending && blinkState) colors.gaugeGreen else colors.textDim,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CAN-ID (hex)",
                        color = colors.textDim,
                        fontSize = 10.sp
                    )
                    OutlinedTextField(
                        value = canId,
                        onValueChange = { canId = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' }.take(3) },
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.borderSubtle,
                            focusedContainerColor = colors.dark,
                            unfocusedContainerColor = colors.dark
                        )
                    )
                }
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        "Daten (hex, space getrennt)",
                        color = colors.textDim,
                        fontSize = 10.sp
                    )
                    OutlinedTextField(
                        value = canData,
                        onValueChange = { canData = it.uppercase().filter { c -> c.isDigit() || c in 'A'..'F' || c == ' ' }.take(47) },
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.borderSubtle,
                            focusedContainerColor = colors.dark,
                            unfocusedContainerColor = colors.dark
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    isSending = true
                    responseText = "Sende CAN-ID: $canId, Daten: $canData"
                    onSend(canId, canData)
                    val byteData = canData.split(" ")
                        .filter { it.isNotEmpty() }
                        .map { it.toInt(16).toByte() }
                        .toByteArray()
                    onSendRealFrame(canId, byteData)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSending) colors.gaugeGreen else colors.accent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    null,
                    tint = if (isSending) colors.dark else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SENDEN",
                    fontWeight = FontWeight.Bold,
                    color = if (isSending) colors.dark else Color.White
                )
            }
            if (responseText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.gaugeGreen.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
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
                            responseText!!,
                            color = colors.gaugeGreen,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(isSending) {
        if (isSending) {
            repeat(6) {
                delay(100)
                blinkState = !blinkState
            }
            isSending = false
            blinkState = false
        }
    }
}
