package com.canopobd.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.BuildConfig
import com.canopobd.R
import com.canopobd.data.model.AppThemeMode
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.data.model.PollMode
import com.canopobd.ui.theme.*

@Composable
fun SettingsDialog(
    pollRate: Long,
    measurementUnit: MeasurementUnit,
    autoReconnect: Boolean,
    pollMode: PollMode,
    appThemeMode: AppThemeMode,
    onDismiss: () -> Unit,
    onPollRateChange: (Long) -> Unit,
    onUnitChange: (MeasurementUnit) -> Unit,
    onAutoReconnectChange: (Boolean) -> Unit,
    onPollModeChange: (PollMode) -> Unit,
    onSetAppThemeMode: (AppThemeMode) -> Unit
) {
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
                        text = stringResource(R.string.settings_title),
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn {
                    item {
                        Text(
                            text = stringResource(R.string.poll_rate),
                            color = textPrimary,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PollMode.entries.forEach { mode ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onPollModeChange(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (pollMode == mode) canopoAccent.copy(alpha = 0.2f) else canopoDark,
                                    border = if (pollMode == mode) androidx.compose.foundation.BorderStroke(2.dp, canopoAccent) else null
                                ) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 12.sp,
                                        color = if (pollMode == mode) canopoAccent else textSecondary,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        @Suppress("DEPRECATION")
                        Divider(color = canopoDark)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.units),
                            color = textPrimary,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        UnitSelector(
                            selectedUnit = measurementUnit,
                            onUnitSelected = onUnitChange
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        @Suppress("DEPRECATION")
                        Divider(color = canopoDark)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.connection),
                            color = textPrimary,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(canopoDark, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.auto_reconnect),
                                color = textPrimary,
                                fontSize = 14.sp
                            )
                            Switch(
                                checked = autoReconnect,
                                onCheckedChange = onAutoReconnectChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = gaugeGreen,
                                    checkedTrackColor = gaugeGreen.copy(alpha = 0.3f),
                                    uncheckedThumbColor = textSecondary,
                                    uncheckedTrackColor = canopoDark
                                )
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        @Suppress("DEPRECATION")
                        Divider(color = canopoDark)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.theme),
                            color = textPrimary,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSetAppThemeMode(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (appThemeMode == mode) canopoAccent.copy(alpha = 0.2f) else canopoDark,
                                    border = if (appThemeMode == mode) androidx.compose.foundation.BorderStroke(2.dp, canopoAccent) else null
                                ) {
                                    Text(
                                        text = mode.displayName,
                                        fontSize = 12.sp,
                                        color = if (appThemeMode == mode) canopoAccent else textSecondary,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        @Suppress("DEPRECATION")
                        Divider(color = canopoDark)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.about),
                            color = textPrimary,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(label = stringResource(R.string.app_version), value = BuildConfig.VERSION_NAME)
                        InfoRow(label = stringResource(R.string.obd_protocol), value = "ELM327")
                        InfoRow(label = stringResource(R.string.android_version), value = "API 26+")
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitSelector(
    selectedUnit: MeasurementUnit,
    onUnitSelected: (MeasurementUnit) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MeasurementUnit.entries.forEach { unit ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUnitSelected(unit) },
                shape = RoundedCornerShape(8.dp),
                color = if (selectedUnit == unit) canopoAccent.copy(alpha = 0.2f) else canopoDark,
                border = if (selectedUnit == unit) {
                    androidx.compose.foundation.BorderStroke(2.dp, canopoAccent)
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (unit == MeasurementUnit.METRIC) Icons.Filled.Speed else Icons.Filled.Thermostat,
                        contentDescription = null,
                        tint = if (selectedUnit == unit) canopoAccent else textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = unit.label,
                        color = if (selectedUnit == unit) canopoAccent else textSecondary,
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (unit == MeasurementUnit.METRIC) "km/h, °C" else "mph, °F",
                        color = textDim,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = textSecondary, fontSize = 12.sp)
        Text(text = value, color = textPrimary, fontSize = 12.sp)
    }
}
