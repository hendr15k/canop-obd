package com.canopobd.ui.safety

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.*
import com.canopobd.ui.theme.*

@Composable
fun SafetySystemsDialog(
    safetySummary: SafetySummary,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Räder", Icons.Filled.Speed),
        TabItem("ESP/ABS", Icons.Filled.Warning),
        TabItem("Bremsen", Icons.Filled.Build),
        TabItem("Airbag", Icons.Filled.HealthAndSafety)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sicherheitssysteme",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schliessen", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = canopoDark,
                    contentColor = canopoHighlight,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(tab.label, fontSize = 12.sp)
                                }
                            },
                            selectedContentColor = canopoHighlight,
                            unselectedContentColor = textDim
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn {
                        when (selectedTab) {
                            0 -> item { WheelSpeedsCard(safetySummary.wheelSpeeds) }
                            1 -> item { ESPABSCard(safetySummary) }
                            2 -> item { BrakeWearCard(safetySummary.brakeWear, safetySummary.safetyStatus) }
                            3 -> item { AirbagCard(safetySummary.airbagStatus) }
                        }
                    }
                }
            }
        }
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
private fun WheelSpeedsCard(wheelSpeeds: WheelSpeeds) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Radgeschwindigkeiten",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WheelItem(
                    modifier = Modifier.weight(1f),
                    label = "VL",
                    speed = wheelSpeeds.frontLeft,
                    unit = wheelSpeeds.unit,
                    color = wheelSpeedColor(kotlin.math.abs(wheelSpeeds.frontLeft - wheelSpeeds.frontRight))
                )
                WheelItem(
                    modifier = Modifier.weight(1f),
                    label = "VR",
                    speed = wheelSpeeds.frontRight,
                    unit = wheelSpeeds.unit,
                    color = wheelSpeedColor(kotlin.math.abs(wheelSpeeds.frontLeft - wheelSpeeds.frontRight))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WheelItem(
                    modifier = Modifier.weight(1f),
                    label = "HL",
                    speed = wheelSpeeds.rearLeft,
                    unit = wheelSpeeds.unit,
                    color = wheelSpeedColor(kotlin.math.abs(wheelSpeeds.rearLeft - wheelSpeeds.rearRight))
                )
                WheelItem(
                    modifier = Modifier.weight(1f),
                    label = "HR",
                    speed = wheelSpeeds.rearRight,
                    unit = wheelSpeeds.unit,
                    color = wheelSpeedColor(kotlin.math.abs(wheelSpeeds.rearLeft - wheelSpeeds.rearRight))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = canopoSurface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SensorRow(
                        label = "Achse vorne Differenz",
                        value = "%.1f %s".format(wheelSpeeds.frontAxleDiff, wheelSpeeds.unit),
                        color = wheelSpeedColor(wheelSpeeds.frontAxleDiff)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Achse hinten Differenz",
                        value = "%.1f %s".format(wheelSpeeds.rearAxleDiff, wheelSpeeds.unit),
                        color = wheelSpeedColor(wheelSpeeds.rearAxleDiff)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Maximale Differenz",
                        value = "%.1f %s".format(wheelSpeeds.speedDifference, wheelSpeeds.unit),
                        color = wheelSpeedColor(wheelSpeeds.speedDifference)
                    )
                }
            }
        }
    }
}

@Composable
private fun WheelItem(
    modifier: Modifier = Modifier,
    label: String,
    speed: Double,
    unit: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = canopoSurface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = textDim
                )
                Text(
                    text = "%.1f %s".format(speed, unit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        }
    }
}

@Composable
private fun ESPABSCard(safetySummary: SafetySummary) {
    val status = safetySummary.safetyStatus
    val sensors = safetySummary.chassisSensors
    val espState = safetySummary.espState

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Stabilitaetskontrolle",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SystemStatusRow(
                label = "ABS",
                status = status.absStatus,
                activeLabel = "ABS aktiv",
                okLabel = "ABS OK",
                faultLabel = "ABS Stoerung"
            )
            Spacer(modifier = Modifier.height(8.dp))
            SystemStatusRow(
                label = "ESP",
                status = status.espStatus,
                activeLabel = "ESP aktiv",
                okLabel = "ESP OK",
                faultLabel = "ESP Stoerung"
            )
            Spacer(modifier = Modifier.height(8.dp))
            SystemStatusRow(
                label = "Traktionskontrolle",
                status = status.tractionControlStatus,
                activeLabel = "TC aktiv",
                okLabel = "TC OK",
                faultLabel = "TC Stoerung"
            )
            Spacer(modifier = Modifier.height(8.dp))
            SystemStatusRow(
                label = "Hill Start Assist",
                status = status.hillStartAssistStatus,
                activeLabel = "HSA aktiv",
                okLabel = "HSA OK",
                faultLabel = "HSA Stoerung"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chassis-Sensoren",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = canopoSurface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SensorRow(
                        label = "Gierrate",
                        value = "%.1f °/s".format(sensors.yawRate),
                        color = if (sensors.isYawRateWarning) gaugeYellow else gaugeGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Querbeschleunigung",
                        value = "%.2f g".format(sensors.lateralAcceleration),
                        color = if (sensors.isLateralAccelWarning) gaugeYellow else gaugeGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Lenkwinkel",
                        value = "%.1f °".format(sensors.steeringAngle),
                        color = if (sensors.isSteeringAngleValid) gaugeGreen else gaugeRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Bremsdruck",
                        value = "%.1f bar".format(sensors.brakePressure),
                        color = textSecondary
                    )
                }
            }

            if (espState.isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = canopoSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ESP-Modus",
                            fontSize = 12.sp,
                            color = textDim
                        )
                        Text(
                            text = espState.selectedMode.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = canopoHighlight
                        )
                        if (espState.isIntervening) {
                            Spacer(modifier = Modifier.height(4.dp))
                            SensorRow(
                                label = "Eingriffslevel",
                                value = "%d%%".format(espState.interventionLevel),
                                color = gaugeYellow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SensorRow(
                                label = "Drehmomentreduzierung",
                                value = "%d%%".format(espState.torqueReduction),
                                color = gaugeOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrakeWearCard(brakeWear: BrakeWear, safetyStatus: SafetySystemStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Bremsenverschleiss",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Vorne",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textDim,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BrakePadRow(label = "Links", wear = brakeWear.frontLeft)
            Spacer(modifier = Modifier.height(6.dp))
            BrakePadRow(label = "Rechts", wear = brakeWear.frontRight)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hinten",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textDim,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BrakePadRow(label = "Links", wear = brakeWear.rearLeft)
            Spacer(modifier = Modifier.height(6.dp))
            BrakePadRow(label = "Rechts", wear = brakeWear.rearRight)

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = canopoSurface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SensorRow(
                        label = "Geschätzte Restkm",
                        value = "%,d km".format(brakeWear.estimatedKmRemaining),
                        color = when {
                            brakeWear.estimatedKmRemaining < 1000 -> gaugeRed
                            brakeWear.estimatedKmRemaining < 5000 -> gaugeYellow
                            else -> gaugeGreen
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Bremsfluessigkeit",
                        value = when (safetyStatus.brakeFluidLevel) {
                            LevelStatus.NORMAL -> "Normal"
                            LevelStatus.LOW -> "Niedrig"
                            LevelStatus.CRITICAL -> "Kritisch"
                            LevelStatus.UNKNOWN -> "Unbekannt"
                        },
                        color = when (safetyStatus.brakeFluidLevel) {
                            LevelStatus.NORMAL -> gaugeGreen
                            LevelStatus.LOW -> gaugeYellow
                            LevelStatus.CRITICAL -> gaugeRed
                            LevelStatus.UNKNOWN -> textDim
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BrakePadRow(label: String, wear: Int) {
    val barColor = when {
        wear > 50 -> gaugeGreen
        wear > 30 -> gaugeYellow
        else -> gaugeRed
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textSecondary,
            modifier = Modifier.width(48.dp)
        )
        LinearProgressIndicator(
            progress = wear / 100f,
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = canopoSurface,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "%d%%".format(wear),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = barColor,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
private fun AirbagCard(airbagStatus: AirbagStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Airbag-System",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AirbagIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Fahrer frontal",
                    ready = airbagStatus.driverFront
                )
                AirbagIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Beifahrer frontal",
                    ready = airbagStatus.passengerFront
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AirbagIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Fahrer Seite",
                    ready = airbagStatus.driverSide
                )
                AirbagIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Beifahrer Seite",
                    ready = airbagStatus.passengerSide
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AirbagIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Gardine links",
                    ready = airbagStatus.curtainLeft
                )
                AirbagIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Gardine rechts",
                    ready = airbagStatus.curtainRight
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AirbagIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Fahrer Knie",
                    ready = airbagStatus.driverKnee
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gurtstraffer",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textDim,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = canopoSurface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SensorRow(
                        label = "Fahrer-Gurtstraffer",
                        value = if (airbagStatus.pretensionerDriver) "Bereit" else "Fehler",
                        color = if (airbagStatus.pretensionerDriver) gaugeGreen else gaugeRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Beifahrer-Gurtstraffer",
                        value = if (airbagStatus.pretensionerPassenger) "Bereit" else "Fehler",
                        color = if (airbagStatus.pretensionerPassenger) gaugeGreen else gaugeRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = canopoSurface
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SensorRow(
                        label = "Systemstatus",
                        value = if (airbagStatus.systemReady) "Bereit" else "Stoerung",
                        color = if (airbagStatus.systemReady) gaugeGreen else gaugeRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SensorRow(
                        label = "Beifahrer-Airbag",
                        value = if (airbagStatus.passengerAirbagDisabled) "Deaktiviert" else "Aktiv",
                        color = textSecondary
                    )
                }
            }

            if (airbagStatus.hasFault) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = gaugeRed.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Error, contentDescription = null, tint = gaugeRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "%d Airbag(s) mit Fehler".format(airbagStatus.faultCount),
                            fontSize = 12.sp,
                            color = gaugeRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AirbagIndicator(
    modifier: Modifier = Modifier,
    label: String,
    ready: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = canopoSurface
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (ready) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = null,
                tint = if (ready) gaugeGreen else gaugeRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = textSecondary
            )
        }
    }
}

@Composable
private fun SystemStatusRow(
    label: String,
    status: SystemStatus,
    activeLabel: String,
    okLabel: String,
    faultLabel: String
) {
    val (displayText, dotColor) = when (status) {
        SystemStatus.OK -> okLabel to gaugeGreen
        SystemStatus.WARNING -> activeLabel to gaugeYellow
        SystemStatus.FAULT -> faultLabel to gaugeRed
        SystemStatus.DISABLED -> "Deaktiviert" to textDim
        SystemStatus.UNKNOWN -> "Unbekannt" to textDim
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = displayText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = dotColor
        )
    }
}

@Composable
private fun SensorRow(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textDim,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

private fun wheelSpeedColor(diff: Double): Color = when {
    diff > AstraJSafetyThresholds.WHEEL_SPEED_DIFF_CRITICAL -> gaugeRed
    diff > AstraJSafetyThresholds.WHEEL_SPEED_DIFF_WARNING -> gaugeYellow
    else -> gaugeGreen
}
