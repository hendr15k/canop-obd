package com.canopobd.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.BuildConfig
import com.canopobd.R
import com.canopobd.data.model.AppThemeMode
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.data.model.PollMode
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Suppress("UNUSED_PARAMETER")
@Composable
fun SettingsDialog(
    pollRate: Long,
    measurementUnit: MeasurementUnit,
    autoReconnect: Boolean,
    pollMode: PollMode,
    appThemeMode: AppThemeMode,
    emulatorMode: Boolean,
    onDismiss: () -> Unit,
    onPollRateChange: (Long) -> Unit,
    onUnitChange: (MeasurementUnit) -> Unit,
    onAutoReconnectChange: (Boolean) -> Unit,
    onPollModeChange: (PollMode) -> Unit,
    onSetAppThemeMode: (AppThemeMode) -> Unit,
    onSetEmulatorMode: (Boolean) -> Unit
) {
    val colors = LocalAppColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(AppRadius.lg),
            color = colors.surfaceDeep
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.gradientSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EINSTELLUNGEN",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_title),
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textPure,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(AppRadius.sm))
                                .background(colors.surfaceRaised)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                DividerLine()

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ----- POLL MODE -----
                    item {
                        SettingsSectionHeader(
                            icon = Icons.Filled.Speed,
                            title = stringResource(R.string.poll_rate)
                        )
                    }
                    item {
                        SegmentedSelector(
                            options = PollMode.entries.map { it.label },
                            selectedIndex = PollMode.entries.indexOf(pollMode),
                            onSelect = { onPollModeChange(PollMode.entries[it]) }
                        )
                    }

                    // ----- UNITS -----
                    item {
                        Spacer(Modifier.height(4.dp))
                        SettingsSectionHeader(
                            icon = Icons.Filled.Straighten,
                            title = stringResource(R.string.units)
                        )
                    }
                    item {
                        UnitSelector(
                            selectedUnit = measurementUnit,
                            onUnitSelected = onUnitChange
                        )
                    }

                    // ----- CONNECTION -----
                    item {
                        Spacer(Modifier.height(4.dp))
                        SettingsSectionHeader(
                            icon = Icons.Filled.Bluetooth,
                            title = stringResource(R.string.connection)
                        )
                    }
                    item {
                        SettingsToggleRow(
                            label = stringResource(R.string.auto_reconnect),
                            checked = autoReconnect,
                            accentColor = colors.success,
                            onCheckedChange = onAutoReconnectChange
                        )
                    }
                    item {
                        SettingsToggleRow(
                            label = stringResource(R.string.emulator_mode),
                            description = stringResource(R.string.emulator_mode_desc),
                            checked = emulatorMode,
                            accentColor = colors.warning,
                            onCheckedChange = onSetEmulatorMode
                        )
                    }

                    // ----- THEME -----
                    item {
                        Spacer(Modifier.height(4.dp))
                        SettingsSectionHeader(
                            icon = Icons.Filled.Palette,
                            title = stringResource(R.string.theme)
                        )
                    }
                    item {
                        SegmentedSelector(
                            options = AppThemeMode.entries.map { it.displayName },
                            selectedIndex = AppThemeMode.entries.indexOf(appThemeMode),
                            onSelect = { onSetAppThemeMode(AppThemeMode.entries[it]) }
                        )
                    }

                    // ----- ABOUT -----
                    item {
                        Spacer(Modifier.height(4.dp))
                        SettingsSectionHeader(
                            icon = Icons.Filled.Info,
                            title = stringResource(R.string.about)
                        )
                    }
                    item {
                        GlassCard(padding = 14.dp) {
                            DataRow(
                                label = stringResource(R.string.app_version),
                                value = "v${BuildConfig.VERSION_NAME}"
                            )
                            DividerLine(modifier = Modifier.padding(vertical = 4.dp))
                            DataRow(
                                label = stringResource(R.string.obd_protocol),
                                value = "ELM327"
                            )
                            DividerLine(modifier = Modifier.padding(vertical = 4.dp))
                            DataRow(
                                label = stringResource(R.string.android_version),
                                value = "API 26+"
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(AppRadius.sm))
                .background(colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SegmentedSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(
                        if (isSelected) { colors.gradientAccent } else { Brushes.Transparent }
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) { colors.surfaceBlack } else { colors.textSecondary },
                    fontWeight = if (isSelected) { FontWeight.Bold } else { FontWeight.Medium }
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.35f),
                checkedBorderColor = accentColor,
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.surfaceElevated,
                uncheckedBorderColor = colors.borderDefault
            )
        )
    }
}

@Composable
private fun UnitSelector(
    selectedUnit: MeasurementUnit,
    onUnitSelected: (MeasurementUnit) -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MeasurementUnit.entries.forEach { unit ->
            val isSelected = selectedUnit == unit
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppRadius.md))
                    .background(
                        if (isSelected) { colors.surfaceBase } else { colors.surfaceRaised }
                    )
                    .border(
                        width = if (isSelected) { 2.dp } else { 1.dp },
                        color = if (isSelected) { colors.primary } else { colors.borderSubtle },
                        shape = RoundedCornerShape(AppRadius.md)
                    )
                    .clickable { onUnitSelected(unit) }
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(AppRadius.sm))
                        .background(
                            if (isSelected) { colors.primary.copy(alpha = 0.18f) } else { colors.surfaceElevated }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (unit == MeasurementUnit.METRIC) { Icons.Filled.Speed } else { Icons.Filled.Thermostat },
                        contentDescription = null,
                        tint = if (isSelected) { colors.primary } else { colors.textTertiary },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = unit.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) { colors.textPure } else { colors.textPrimary },
                    fontWeight = if (isSelected) { FontWeight.Bold } else { FontWeight.Medium }
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (unit == MeasurementUnit.METRIC) { "km/h · °C" } else { "mph · °F" },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

private object Brushes {
    val Transparent: androidx.compose.ui.graphics.Brush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color.Transparent,
            androidx.compose.ui.graphics.Color.Transparent
        )
    )
}
