package com.canopobd.ui.shiftlight

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.ShiftLightConfig
import com.canopobd.ui.theme.*

@Composable
fun ShiftLightDialog(
    config: ShiftLightConfig,
    currentRpm: Double,
    onDismiss: () -> Unit,
    onUpdateConfig: (ShiftLightConfig) -> Unit
) {
    var localConfig by remember { mutableStateOf(config) }
    var redlineRpm by remember { mutableFloatStateOf(config.redlineRpm.toFloat()) }
    var warningRpm by remember { mutableFloatStateOf(config.warningRpm.toFloat()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
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
                        text = "Schaltblitz",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Aktiviert", fontSize = 14.sp, color = textPrimary)
                    Switch(
                        checked = localConfig.enabled,
                        onCheckedChange = {
                            localConfig = localConfig.copy(enabled = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = gaugeGreen,
                            checkedTrackColor = gaugeGreen.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = canopoDark
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.0f rpm".format(currentRpm),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                currentRpm >= redlineRpm -> gaugeRed
                                currentRpm >= warningRpm -> gaugeYellow
                                else -> gaugeGreen
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        @Suppress("DEPRECATION")
                        LinearProgressIndicator(
                            progress = (currentRpm / 8000.0).toFloat().coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            color = when {
                                currentRpm >= redlineRpm -> gaugeRed
                                currentRpm >= warningRpm -> gaugeYellow
                                else -> gaugeGreen
                            },
                            trackColor = canopoSurface,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "0", fontSize = 10.sp, color = textDim)
                            Text(text = "Warnung: ${warningRpm.toInt()} rpm", fontSize = 10.sp, color = gaugeYellow)
                            Text(text = "Rot: ${redlineRpm.toInt()} rpm", fontSize = 10.sp, color = gaugeRed)
                            Text(text = "8000", fontSize = 10.sp, color = textDim)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Warn-Drehzahl", fontSize = 14.sp, color = textSecondary)
                Slider(
                    value = warningRpm,
                    onValueChange = {
                        warningRpm = it
                        localConfig = localConfig.copy(warningRpm = it.toInt())
                    },
                    valueRange = 3000f..7000f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = gaugeYellow,
                        activeTrackColor = gaugeYellow
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Rot-Drehzahl", fontSize = 14.sp, color = textSecondary)
                Slider(
                    value = redlineRpm,
                    onValueChange = {
                        redlineRpm = it
                        localConfig = localConfig.copy(redlineRpm = it.toInt())
                    },
                    valueRange = 4000f..8000f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = gaugeRed,
                        activeTrackColor = gaugeRed
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Blinken", fontSize = 14.sp, color = textPrimary)
                    Switch(
                        checked = localConfig.flashEnabled,
                        onCheckedChange = {
                            localConfig = localConfig.copy(flashEnabled = it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = gaugeGreen,
                            checkedTrackColor = gaugeGreen.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onUpdateConfig(localConfig)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = canopoAccent)
                ) {
                    Text("Speichern")
                }
            }
        }
    }
}
