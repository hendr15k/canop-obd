package com.canopobd.ui.alerts

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.ActiveAlert
import com.canopobd.data.model.AlertConfig
import com.canopobd.data.model.AlertType
import com.canopobd.ui.theme.*

@Composable
fun AlertSettingsDialog(
    alertConfig: AlertConfig,
    activeAlerts: List<ActiveAlert>,
    onDismiss: () -> Unit,
    onUpdateConfig: (AlertConfig) -> Unit
) {
    var config by remember(alertConfig) { mutableStateOf(alertConfig) }

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
                        text = stringResource(R.string.alerts_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeAlerts.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.alerts_active),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    activeAlerts.forEach { alert ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = gaugeRed.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = gaugeRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = alert.message, fontSize = 12.sp, color = gaugeRed)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        AlertToggleRow(
                            label = stringResource(R.string.alerts_speed),
                            unit = "km/h",
                            enabled = config.speedWarningEnabled,
                            threshold = config.speedWarning,
                            onEnabledChange = { config = config.copy(speedWarningEnabled = it) },
                            onThresholdChange = { config = config.copy(speedWarning = it) }
                        )
                    }
                    item {
                        AlertToggleRow(
                            label = stringResource(R.string.alerts_coolant),
                            unit = "°C",
                            enabled = config.coolantWarningEnabled,
                            threshold = config.coolantWarning,
                            onEnabledChange = { config = config.copy(coolantWarningEnabled = it) },
                            onThresholdChange = { config = config.copy(coolantWarning = it) }
                        )
                    }
                    item {
                        AlertToggleRow(
                            label = stringResource(R.string.alerts_fuel),
                            unit = "%",
                            enabled = config.fuelWarningEnabled,
                            threshold = config.fuelWarning,
                            isLowAlert = true,
                            onEnabledChange = { config = config.copy(fuelWarningEnabled = it) },
                            onThresholdChange = { config = config.copy(fuelWarning = it) }
                        )
                    }
                    item {
                        AlertToggleRow(
                            label = stringResource(R.string.alerts_rpm),
                            unit = "rpm",
                            enabled = config.rpmWarningEnabled,
                            threshold = config.rpmWarning,
                            onEnabledChange = { config = config.copy(rpmWarningEnabled = it) },
                            onThresholdChange = { config = config.copy(rpmWarning = it) }
                        )
                    }
                    item {
                        AlertToggleRow(
                            label = stringResource(R.string.alerts_battery),
                            unit = "V",
                            enabled = config.batteryLowWarningEnabled,
                            threshold = config.batteryLowWarning,
                            isLowAlert = true,
                            onEnabledChange = { config = config.copy(batteryLowWarningEnabled = it) },
                            onThresholdChange = { config = config.copy(batteryLowWarning = it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onUpdateConfig(config) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = canopoAccent)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Speichern")
                }
            }
        }
    }
}

@Composable
private fun AlertToggleRow(
    label: String,
    unit: String,
    enabled: Boolean,
    threshold: Float,
    isLowAlert: Boolean = false,
    onEnabledChange: (Boolean) -> Unit,
    onThresholdChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = if (enabled) textPrimary else textSecondary
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = gaugeGreen,
                        checkedTrackColor = gaugeGreen.copy(alpha = 0.3f),
                        uncheckedThumbColor = textSecondary,
                        uncheckedTrackColor = canopoDark
                    )
                )
            }
            if (enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.alerts_threshold) + ":",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = threshold,
                        onValueChange = onThresholdChange,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isLowAlert) gaugeOrange else gaugeRed,
                            activeTrackColor = if (isLowAlert) gaugeOrange else gaugeRed
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "%.0f %s".format(threshold, unit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowAlert) gaugeOrange else gaugeRed
                    )
                }
            }
        }
    }
}
