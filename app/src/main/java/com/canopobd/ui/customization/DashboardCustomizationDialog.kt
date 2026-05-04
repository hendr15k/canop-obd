package com.canopobd.ui.customization

import androidx.compose.foundation.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.ColorTheme
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
    GaugeOption("fuel_rate", "Fuel Rate", "L/h", "Kraftstoffverbrauch")
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
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
                        text = "Dashboard Anpassen",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = canopoDark,
                    contentColor = canopoAccent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Farben", color = if (selectedTab == 0) textPrimary else textSecondary) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Gauges", color = if (selectedTab == 1) textPrimary else textSecondary) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Layout", color = if (selectedTab == 2) textPrimary else textSecondary) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
    LazyColumn {
        item {
            Text(
                text = "Farbschema wählen",
                fontSize = 14.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        items(ColorTheme.entries.toList()) { theme ->
            val isSelected = currentTheme == theme
            val themeColor = Color(theme.primaryColor)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onThemeChange(theme) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) themeColor.copy(alpha = 0.15f) else canopoDark,
                border = if (isSelected) BorderStroke(2.dp, themeColor) else null
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(theme.primaryColor))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = theme.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) themeColor else textPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(
                                    Color(theme.primaryColor),
                                    Color(theme.accentColor),
                                    Color(theme.gaugeGreen),
                                    Color(theme.gaugeYellow)
                                ).forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                    )
                                }
                            }
                        }
                    }
                    if (isSelected) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = themeColor, modifier = Modifier.size(24.dp))
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
    LazyColumn {
        item {
            Text(
                text = "Haupt-Gauges auswählen (3 für große Anzeige)",
                fontSize = 14.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        items(availableGauges) { gauge ->
            val isPrimary = gauge.id in primaryGaugeIds
            val isDisabled = !isPrimary && primaryGaugeIds.size >= 3

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable(enabled = !isDisabled) {
                        val newSet = if (isPrimary) {
                            primaryGaugeIds - gauge.id
                        } else if (primaryGaugeIds.size < 3) {
                            primaryGaugeIds + gauge.id
                        } else primaryGaugeIds

                        if (isPrimary || primaryGaugeIds.size < 3) {
                            onPrimaryGaugesChange(newSet)
                        }
                    },
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isPrimary -> canopoAccent.copy(alpha = 0.15f)
                    isDisabled -> canopoDark.copy(alpha = 0.5f)
                    else -> canopoDark
                },
                border = if (isPrimary) BorderStroke(1.dp, canopoAccent) else null
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = gauge.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isPrimary -> canopoAccent
                                isDisabled -> textDim
                                else -> textPrimary
                            }
                        )
                        Text(
                            text = gauge.description,
                            fontSize = 11.sp,
                            color = textDim
                        )
                    }
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = {
                            if (!isDisabled) {
                                val newSet = if (isPrimary) {
                                    primaryGaugeIds - gauge.id
                                } else if (primaryGaugeIds.size < 3) {
                                    primaryGaugeIds + gauge.id
                                } else primaryGaugeIds
                                onPrimaryGaugesChange(newSet)
                            }
                        },
                        enabled = !isDisabled,
                        colors = CheckboxDefaults.colors(
                            checkedColor = canopoAccent,
                            uncheckedColor = textSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LayoutPreview(primaryGaugeIds: Set<String>) {
    Column {
        Text(
            text = "Vorschau",
            fontSize = 14.sp,
            color = textSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp),
            color = canopoDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
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
                                    .background(canopoSurface)
                                    .border(3.dp, gaugeColor(gauge.id), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gauge.id.uppercase().take(3),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = canopoAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gauge.label.take(10),
                                fontSize = 9.sp,
                                color = textSecondary
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(canopoSurface.copy(alpha = 0.3f))
                                    .border(1.dp, textDim.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "---",
                                    fontSize = 12.sp,
                                    color = textDim
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Leer",
                                fontSize = 9.sp,
                                color = textDim
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = canopoDark
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Sekundär-Gauges (6 Stück):",
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val secondaryGauges = availableGauges.filter { it.id !in primaryGaugeIds }.take(6)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    secondaryGauges.take(3).forEach { g ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            color = canopoSurface
                        ) {
                            Text(
                                text = g.label.take(8),
                                fontSize = 10.sp,
                                color = textSecondary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun gaugeColor(id: String): Color = when (id) {
    "rpm", "speed", "coolant" -> gaugeGreen
    "throttle", "engine_load" -> gaugeYellow
    "fuel", "fuel_trim" -> gaugeOrange
    else -> gaugeRed
}
