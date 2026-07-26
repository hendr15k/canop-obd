package com.canopobd.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canopobd.R
import com.canopobd.data.model.ColorTheme
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

data class GaugeOption(
    val id: String,
    val label: String,
    val unit: String,
    val description: String
)

private val availableGauges = listOf(
    GaugeOption("rpm", "Engine RPM", "rpm", "Motor RPM"),
    GaugeOption("speed", "Vehicle Speed", "km/h", "Geschwindigkeit"),
    GaugeOption("coolant", "Coolant Temp", "°C", "Kühlmitteltemperatur"),
    GaugeOption("throttle", "Throttle Position", "%", "Drosselklappe"),
    GaugeOption("engine_load", "Engine Load", "%", "Motorlast"),
    GaugeOption("fuel", "Fuel Level", "%", "Kraftstoff"),
    GaugeOption("timing", "Timing Advance", "°", "Zündzeitpunkt"),
    GaugeOption("maf", "MAF Rate", "g/s", "Lufftmassen-Durchfluss"),
    GaugeOption("intake_temp", "Intake Temp", "°C", "Ansaugluft"),
    GaugeOption("fuel_trim", "Fuel Trim B1", "%", "Kraftstoffkorrektur"),
    GaugeOption("load", "Absolute Load", "%", "Motorlast absolut"),
    GaugeOption("fuel_rate", "Fuel Rate", "L/h", "Kraftstoffverbrauch"),
    GaugeOption("accel_pedal", "Accel Pedal", "%", "Gaspedal"),
    GaugeOption("hybrid_battery", "Hybrid Batt", "%", "Hybrid-Batterie")
)

@Composable
fun DashboardCustomizationDialog(
    currentTheme: ColorTheme,
    primaryGaugeIds: Set<String>,
    onDismiss: () -> Unit,
    onThemeChange: (ColorTheme) -> Unit,
    onPrimaryGaugesChange: (Set<String>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.customize_dashboard_title),
        eyebrow = "Dashboard Anpassen",
        heightFraction = 0.9f
    ) {
        Column {
            TabBar(
                tabs = listOf(
                    stringResource(R.string.tab_colors),
                    stringResource(R.string.tab_gauges),
                    stringResource(R.string.tab_layout)
                ),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
            DividerLine()
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> ThemeSelector(
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange
                    )
                    1 -> GaugeSelector(
                        primaryGaugeIds = primaryGaugeIds,
                        onPrimaryGaugesChange = onPrimaryGaugesChange
                    )
                    2 -> LayoutPreview(
                        primaryGaugeIds = primaryGaugeIds
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(
    currentTheme: ColorTheme,
    onThemeChange: (ColorTheme) -> Unit
) {
    val colors = LocalAppColors.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SectionHeader(
                title = stringResource(R.string.color_themes_title),
                icon = Icons.Filled.Palette
            )
        }
        items(ColorTheme.entries.toList()) { theme ->
            val isSelected = currentTheme == theme
            val themeColor = Color(theme.primaryColor)
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeChange(theme) },
                accentEdge = if (isSelected) { themeColor } else { null },
                padding = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(theme.primaryColor),
                                            Color(theme.accentColor)
                                        )
                                    )
                                )
                                .border(2.dp, themeColor, CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) { themeColor } else { colors.textPrimary },
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    Color(theme.primaryColor),
                                    Color(theme.accentColor),
                                    Color(theme.gaugeGreen),
                                    Color(theme.gaugeYellow)
                                ).forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(1.dp, colors.borderSubtle, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(themeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GaugeSelector(
    primaryGaugeIds: Set<String>,
    onPrimaryGaugesChange: (Set<String>) -> Unit
) {
    val colors = LocalAppColors.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SectionHeader(
                title = stringResource(R.string.select_gauges_title),
                icon = Icons.Filled.Speed
            )
        }
        items(availableGauges) { gauge ->
            val isPrimary = gauge.id in primaryGaugeIds
            val isDisabled = !isPrimary && primaryGaugeIds.size >= 3
            val c = if (isDisabled) { colors.textMuted } else if (isPrimary) { colors.primary } else { colors.textSecondary }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppRadius.md))
                    .background(colors.surfaceRaised)
                    .border(
                        width = if (isPrimary) { 1.5.dp } else { 1.dp },
                        color = if (isPrimary) { colors.primary.copy(alpha = 0.5f) } else { colors.borderSubtle },
                        shape = RoundedCornerShape(AppRadius.md)
                    )
                    .clickable(enabled = !isDisabled) {
                        val newSet = if (isPrimary) {
                            primaryGaugeIds - gauge.id
                        } else if (primaryGaugeIds.size < 3) {
                            primaryGaugeIds + gauge.id
                        } else {
                            primaryGaugeIds
                        }
                        if (isPrimary || primaryGaugeIds.size < 3) {
                            onPrimaryGaugesChange(newSet)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPrimary) { colors.primary } else { Color.Transparent })
                        .border(2.dp, c, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPrimary) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = colors.surfaceBlack,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gauge.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDisabled) { colors.textMuted } else { colors.textPrimary }
                    )
                    Text(
                        text = gauge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
                Text(
                    text = gauge.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun LayoutPreview(primaryGaugeIds: Set<String>) {
    val colors = LocalAppColors.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(
                title = stringResource(R.string.layout_preview_title),
                icon = Icons.Filled.Dashboard
            )
        }
        item {
            GlassCard(
                padding = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selected = availableGauges.filter { it.id in primaryGaugeIds }.take(3)
                    repeat(3) { i ->
                        val gauge = selected.getOrNull(i)
                        if (gauge != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceRaised)
                                        .border(3.dp, gaugeColor(gauge.id), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gauge.id.uppercase().take(3),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = gauge.label.take(10),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceRaised.copy(alpha = 0.4f))
                                        .border(1.dp, colors.borderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "—",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textMuted
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.empty_slot),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionHeader(
                title = stringResource(R.string.secondary_gauges_label),
                icon = Icons.Filled.GridView
            )
        }
        item {
            val secondaryGauges = availableGauges.filter { it.id !in primaryGaugeIds }.take(6)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                secondaryGauges.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { g ->
                            KeyValueBlock(
                                label = g.label.take(8),
                                value = g.unit,
                                modifier = Modifier.weight(1f),
                                accentColor = colors.primary
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun gaugeColor(id: String): Color {
    val colors = LocalAppColors.current
    return when (id) {
        "rpm", "speed", "coolant" -> colors.success
        "throttle", "engine_load" -> colors.warning
        "fuel", "fuel_trim", "hybrid_battery" -> colors.warning
        "maf", "accel_pedal" -> colors.info
        else -> colors.critical
    }
}
