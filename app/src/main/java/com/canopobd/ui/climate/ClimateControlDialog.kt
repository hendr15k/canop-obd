package com.canopobd.ui.climate

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.LocalAppColors
import com.canopobd.ui.theme.AppColors

data class ClimateState(
    val isACEnabled: Boolean = false,
    val isAutoMode: Boolean = true,
    val isRecirculation: Boolean = false,
    val isFrontDefrost: Boolean = false,
    val isRearDefrost: Boolean = false,
    val isMirrorDefrost: Boolean = false,
    val fanSpeed: Int = 3,
    val driverTemp: Int = 22,
    val passengerTemp: Int = 22,
    val syncEnabled: Boolean = false,
    val acCompressorActive: Boolean = false,
    val outsideTemp: Int = 18,
    val cabinTemp: Int = 23,
    val airQuality: Int = 100
)

enum class ClimateZone {
    DRIVER, PASSENGER, REAR, ALL
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun ClimateControlDialog(
    initialState: ClimateState = ClimateState(),
    onCommand: (ClimateCommand) -> Unit,
    onDismiss: () -> Unit,
    externalState: ClimateState? = null,
    onClimateStateChange: ((ClimateState) -> Unit)? = null
) {
    val colors = LocalAppColors.current
    var localState by remember { mutableStateOf(externalState ?: initialState) }
    var selectedZone by remember { mutableStateOf(ClimateZone.ALL) }

    LaunchedEffect(externalState) {
        if (externalState != null) {
            localState = externalState
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AcUnit,
                        contentDescription = null,
                        tint = colors.gaugeCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Klimaanlage", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Schliessen", tint = colors.textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Außentemperatur", color = colors.textDim, fontSize = 10.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "${localState.outsideTemp}",
                                        color = colors.textPrimary,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("°C", color = colors.textDim, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Innentemperatur", color = colors.textDim, fontSize = 10.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "${localState.cabinTemp}",
                                        color = colors.gaugeCyan,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("°C", color = colors.textDim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp))
                                }
                            }
                        }

                        if (localState.acCompressorActive) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.ElectricBolt,
                                    contentDescription = null,
                                    tint = colors.gaugeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Kompressor aktiv",
                                    color = colors.gaugeGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Text("Klimazonen", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClimateZone.entries.forEach { zone ->
                        ZoneChip(
                            zone = zone,
                            isSelected = selectedZone == zone,
                            onClick = { selectedZone = zone },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TemperatureControl(
                                label = "Fahrer",
                                temp = localState.driverTemp,
                                isActive = selectedZone == ClimateZone.DRIVER || selectedZone == ClimateZone.ALL,
                                onIncrease = {
                                    if (localState.driverTemp < 30) {
                                        localState = localState.copy(driverTemp = localState.driverTemp + 1)
                                        if (localState.syncEnabled) {
                                            localState = localState.copy(passengerTemp = localState.passengerTemp + 1)
                                        }
                                        onCommand(ClimateCommand.SET_TEMP_DRIVER)
                                    }
                                },
                                onDecrease = {
                                    if (localState.driverTemp > 16) {
                                        localState = localState.copy(driverTemp = localState.driverTemp - 1)
                                        if (localState.syncEnabled) {
                                            localState = localState.copy(passengerTemp = localState.passengerTemp - 1)
                                        }
                                        onCommand(ClimateCommand.SET_TEMP_DRIVER)
                                    }
                                },
                                colors = colors
                            )
                            VerticalDivider(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(100.dp),
                                color = colors.borderSubtle
                            )
                            TemperatureControl(
                                label = "Beifahrer",
                                temp = localState.passengerTemp,
                                isActive = selectedZone == ClimateZone.PASSENGER || selectedZone == ClimateZone.ALL,
                                onIncrease = {
                                    if (localState.passengerTemp < 30) {
                                        localState = localState.copy(passengerTemp = localState.passengerTemp + 1)
                                        onCommand(ClimateCommand.SET_TEMP_PASSENGER)
                                    }
                                },
                                onDecrease = {
                                    if (localState.passengerTemp > 16) {
                                        localState = localState.copy(passengerTemp = localState.passengerTemp - 1)
                                        onCommand(ClimateCommand.SET_TEMP_PASSENGER)
                                    }
                                },
                                colors = colors
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ClimateToggle(
                            label = "Sync",
                            icon = Icons.Filled.Sync,
                            isActive = localState.syncEnabled,
                            onToggle = {
                                localState = localState.copy(syncEnabled = !localState.syncEnabled)
                                onCommand(ClimateCommand.TOGGLE_SYNC)
                            },
                            colors = colors,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Text("Lüfter", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (localState.fanSpeed > 0) {
                                        localState = localState.copy(fanSpeed = localState.fanSpeed - 1)
                                        onCommand(ClimateCommand.FAN_SPEED_DOWN)
                                    }
                                },
                                enabled = localState.fanSpeed > 0
                            ) {
                                Icon(Icons.Filled.Remove, "Leiser", tint = colors.accent)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(6) { index ->
                                    val isActive = index < localState.fanSpeed
                                    Box(
                                        modifier = Modifier
                                            .size(width = 8.dp, height = (16 + index * 4).dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (isActive) colors.gaugeCyan else colors.textDim.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (localState.fanSpeed < 6) {
                                        localState = localState.copy(fanSpeed = localState.fanSpeed + 1)
                                        onCommand(ClimateCommand.FAN_SPEED_UP)
                                    }
                                },
                                enabled = localState.fanSpeed < 6
                            ) {
                                Icon(Icons.Filled.Add, "Lauter", tint = colors.accent)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ClimateButton(
                                label = "Aus",
                                isActive = localState.fanSpeed == 0,
                                onClick = {
                                    localState = localState.copy(fanSpeed = 0)
                                    onCommand(ClimateCommand.FAN_OFF)
                                },
                                colors = colors
                            )
                            ClimateButton(
                                label = "1",
                                isActive = localState.fanSpeed == 1,
                                onClick = {
                                    localState = localState.copy(fanSpeed = 1)
                                    onCommand(ClimateCommand.FAN_SPEED_1)
                                },
                                colors = colors
                            )
                            ClimateButton(
                                label = "3",
                                isActive = localState.fanSpeed == 3,
                                onClick = {
                                    localState = localState.copy(fanSpeed = 3)
                                    onCommand(ClimateCommand.FAN_SPEED_3)
                                },
                                colors = colors
                            )
                            ClimateButton(
                                label = "Max",
                                isActive = localState.fanSpeed == 6,
                                onClick = {
                                    localState = localState.copy(fanSpeed = 6)
                                    onCommand(ClimateCommand.FAN_MAX)
                                },
                                colors = colors
                            )
                        }
                    }
                }

                Text("Funktionen", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            ClimateToggle(
                                label = "A/C",
                                icon = Icons.Filled.AcUnit,
                                isActive = localState.isACEnabled,
                                onToggle = {
                                    localState = localState.copy(isACEnabled = !localState.isACEnabled)
                                    onCommand(if (localState.isACEnabled) ClimateCommand.AC_ON else ClimateCommand.AC_OFF)
                                },
                                colors = colors
                            )
                            ClimateToggle(
                                label = "Auto",
                                icon = Icons.Filled.AutoMode,
                                isActive = localState.isAutoMode,
                                onToggle = {
                                    localState = localState.copy(isAutoMode = !localState.isAutoMode)
                                    onCommand(ClimateCommand.AUTO_MODE)
                                },
                                colors = colors
                            )
                            ClimateToggle(
                                label = "Umlauf",
                                icon = Icons.Filled.Air,
                                isActive = localState.isRecirculation,
                                onToggle = {
                                    localState = localState.copy(isRecirculation = !localState.isRecirculation)
                                    onCommand(if (localState.isRecirculation) ClimateCommand.RECIRC_ON else ClimateCommand.RECIRC_OFF)
                                },
                                colors = colors
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ClimateToggle(
                                label = "Front",
                                icon = Icons.Filled.FrontHand,
                                isActive = localState.isFrontDefrost,
                                onToggle = {
                                    localState = localState.copy(isFrontDefrost = !localState.isFrontDefrost)
                                    onCommand(if (localState.isFrontDefrost) ClimateCommand.DEFROST_FRONT else ClimateCommand.DEFROST_FRONT_OFF)
                                },
                                colors = colors
                            )
                            ClimateToggle(
                                label = "Heck",
                                icon = Icons.Filled.DirectionsCar,
                                isActive = localState.isRearDefrost,
                                onToggle = {
                                    localState = localState.copy(isRearDefrost = !localState.isRearDefrost)
                                    onCommand(if (localState.isRearDefrost) ClimateCommand.DEFROST_REAR else ClimateCommand.DEFROST_REAR_OFF)
                                },
                                colors = colors
                            )
                            ClimateToggle(
                                label = "Spiegel",
                                icon = Icons.Filled.FlipToBack,
                                isActive = localState.isMirrorDefrost,
                                onToggle = {
                                    localState = localState.copy(isMirrorDefrost = !localState.isMirrorDefrost)
                                    onCommand(if (localState.isMirrorDefrost) ClimateCommand.DEFROST_MIRRORS else ClimateCommand.DEFROST_MIRRORS_OFF)
                                },
                                colors = colors
                            )
                        }
                    }
                }

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
                            "AC-Kompressor wird automatisch bei Frontscheibenenteisung aktiviert.",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
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
private fun ZoneChip(
    zone: ClimateZone,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val label = when (zone) {
        ClimateZone.DRIVER -> "Fahrer"
        ClimateZone.PASSENGER -> "Beif."
        ClimateZone.REAR -> "Hinten"
        ClimateZone.ALL -> "Alle"
    }
    val icon = when (zone) {
        ClimateZone.DRIVER -> Icons.Filled.Person
        ClimateZone.PASSENGER -> Icons.Filled.PersonOutline
        ClimateZone.REAR -> Icons.Filled.AirlineSeatReclineNormal
        ClimateZone.ALL -> Icons.Filled.Dashboard
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) colors.accent else colors.surfaceCard,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) colors.textPrimary else colors.textDim,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                color = if (isSelected) colors.textPrimary else colors.textDim,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TemperatureControl(
    label: String,
    temp: Int,
    isActive: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    colors: AppColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(label, color = colors.textDim, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                enabled = isActive && temp > 16,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isActive) colors.accent.copy(alpha = 0.2f) else colors.surface)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    "Kälter",
                    tint = if (isActive) colors.accent else colors.textDim
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$temp",
                    color = if (isActive) colors.textPrimary else colors.textDim,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("°C", color = colors.textDim, fontSize = 12.sp)
            }

            IconButton(
                onClick = onIncrease,
                enabled = isActive && temp < 30,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isActive) colors.gaugeOrange.copy(alpha = 0.2f) else colors.surface)
            ) {
                Icon(
                    Icons.Filled.Add,
                    "Wärmer",
                    tint = if (isActive) colors.gaugeOrange else colors.textDim
                )
            }
        }
    }
}

@Composable
private fun ClimateToggle(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onToggle: () -> Unit,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    val activeColor by animateColorAsState(
        when {
            label == "A/C" || label == "Auto" -> colors.gaugeCyan
            label == "Front" || label == "Heck" || label == "Spiegel" -> colors.gaugeOrange
            else -> colors.gaugeGreen
        },
        label = "climateToggle"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = if (isActive) activeColor.copy(alpha = 0.2f) else colors.surfaceElevated,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    label,
                    tint = if (isActive) activeColor else colors.textDim,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            color = if (isActive) activeColor else colors.textDim,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ClimateButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    colors: AppColors
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) colors.accent else colors.surfaceElevated
    ) {
        Text(
            label,
            color = if (isActive) colors.textPrimary else colors.textDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

sealed class ClimateCommand {
    object AC_ON : ClimateCommand()
    object AC_OFF : ClimateCommand()
    object AUTO_MODE : ClimateCommand()
    object RECIRC_ON : ClimateCommand()
    object RECIRC_OFF : ClimateCommand()
    object DEFROST_FRONT : ClimateCommand()
    object DEFROST_FRONT_OFF : ClimateCommand()
    object DEFROST_REAR : ClimateCommand()
    object DEFROST_REAR_OFF : ClimateCommand()
    object DEFROST_MIRRORS : ClimateCommand()
    object DEFROST_MIRRORS_OFF : ClimateCommand()
    object FAN_OFF : ClimateCommand()
    object FAN_SPEED_UP : ClimateCommand()
    object FAN_SPEED_DOWN : ClimateCommand()
    object FAN_SPEED_1 : ClimateCommand()
    object FAN_SPEED_3 : ClimateCommand()
    object FAN_MAX : ClimateCommand()
    object SET_TEMP_DRIVER : ClimateCommand()
    object SET_TEMP_PASSENGER : ClimateCommand()
    object TOGGLE_SYNC : ClimateCommand()
}
