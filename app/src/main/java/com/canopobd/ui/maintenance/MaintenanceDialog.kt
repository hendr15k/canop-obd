package com.canopobd.ui.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.maintenance.*
import com.canopobd.data.model.MaintenanceItem
import com.canopobd.data.model.MaintenanceType
import com.canopobd.ui.theme.*
import kotlin.math.roundToInt

/**
 * Erweitertes Wartungsmanagement-Dialog für den Opel Astra J 1.4 Turbo
 *
 * Features:
 * - Km-basierte Erinnerungen
 * - Zeit-basierte Erinnerungen
 * - Öltemperatur-Historie
 * - Kraftstoffverbrauch-Trend
 * - Fahrbedingungen
 * - Wartungskosten-Schätzung
 * - Benachrichtigungen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDialog(
    maintenanceItems: List<MaintenanceItem>,
    currentKm: Int,
    onDismiss: () -> Unit,
    onUpdateItem: (MaintenanceType, Int, Int) -> Unit,
    onResetItem: (MaintenanceType) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Wartungen", "Öl-Temperatur", "Verbrauch", "Kosten")

    // Erweitertes Service-Objekt
    val maintenanceService = remember(currentKm) {
        MaintenanceService().also { it.initialize(currentKm) }
    }
    val reminders = maintenanceService.getAllReminders()
    val oilTempStats = maintenanceService.getOilTempStatistics()
    val fuelStats = maintenanceService.getFuelConsumptionStatistics()
    val costEstimate = maintenanceService.estimateMaintenanceCosts()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.maintenance_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                // Km-Anzeige
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = canopoDark
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Speed, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.maintenance_odometer, currentKm.toString()),
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab-Navigation
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = canopoDark,
                    contentColor = canopoAccent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    color = if (selectedTab == index) { canopoAccent } else { textSecondary }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab-Inhalt
                when (selectedTab) {
                    0 -> MaintenanceTab(
                        reminders = reminders,
                        maintenanceItems = maintenanceItems,
                        currentKm = currentKm,
                        onUpdateItem = onUpdateItem,
                        onResetItem = onResetItem
                    )
                    1 -> OilTempTab(oilTempStats)
                    2 -> FuelConsumptionTab(fuelStats)
                    3 -> CostsTab(costEstimate)
                }
            }
        }
    }
}

/**
 * Tab für Wartungsarbeiten mit erweiterten Erinnerungen
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun MaintenanceTab(
    reminders: List<MaintenanceReminder>,
    maintenanceItems: List<MaintenanceItem>,
    currentKm: Int,
    onUpdateItem: (MaintenanceType, Int, Int) -> Unit,
    onResetItem: (MaintenanceType) -> Unit
) {
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var selectedReminder by remember { mutableStateOf<MaintenanceReminder?>(null) }

    LazyColumn {
        // Übersicht
        item {
            MaintenanceOverviewCard(reminders)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Erinnerungen nach Priorität sortiert
        val sortedReminders = reminders.sortedBy { it.priority.ordinal }

        items(sortedReminders) { reminder ->
            ExtendedMaintenanceReminderCard(
                reminder = reminder,
                currentKm = currentKm,
                onUpdate = { km, interval ->
                    onUpdateItem(reminder.type, km, interval)
                },
                onReset = { onResetItem(reminder.type) },
                onDetails = { selectedReminder = reminder }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Hinzufügen Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showAddReminderDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = canopoAccent)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Erinnerung hinzufügen", fontSize = 14.sp)
            }
        }
    }

    // Detail-Dialog
    selectedReminder?.let { reminder ->
        ReminderDetailDialog(
            reminder = reminder,
            onDismiss = { selectedReminder = null }
        )
    }

    // Hinzufügen-Dialog
    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { /* Erinnerung hinzufügen */ }
        )
    }
}

/**
 * Übersichtskarte mit Zusammenfassung
 */
@Composable
private fun MaintenanceOverviewCard(reminders: List<MaintenanceReminder>) {
    val overdueCount = reminders.count { it.status == MaintenanceReminderStatus.OVERDUE }
    val dueSoonCount = reminders.count { it.status == MaintenanceReminderStatus.DUE_SOON }
    val okCount = reminders.count { it.status == MaintenanceReminderStatus.OK }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Wartungsübersicht",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(
                    count = overdueCount,
                    label = "Überfällig",
                    color = gaugeRed,
                    icon = Icons.Filled.Warning
                )
                OverviewItem(
                    count = dueSoonCount,
                    label = "Fällig",
                    color = gaugeYellow,
                    icon = Icons.Filled.Schedule
                )
                OverviewItem(
                    count = okCount,
                    label = "OK",
                    color = gaugeGreen,
                    icon = Icons.Filled.CheckCircle
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nächste Wartung
            val nextReminder = reminders
                .filter { it.isActive && !it.isCompleted }
                .minByOrNull { it.kmRemaining.coerceAtLeast(0) }

            nextReminder?.let { reminder ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nächste Wartung:",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Text(
                        text = "${reminder.type.label} in ${reminder.kmRemaining.coerceAtLeast(0)} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = canopoAccent
                    )
                }
            }
        }
    }
}

/**
 * Einzelnes Element in der Übersicht
 */
@Composable
private fun OverviewItem(
    count: Int,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = textSecondary
        )
    }
}

/**
 * Erweiterte Wartungserinnerungs-Karte
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun ExtendedMaintenanceReminderCard(
    reminder: MaintenanceReminder,
    currentKm: Int,
    onUpdate: (Int, Int) -> Unit,
    onReset: () -> Unit,
    onDetails: () -> Unit
) {
    var showIntervalDialog by remember { mutableStateOf(false) }

    val statusColor = when (reminder.status) {
        MaintenanceReminderStatus.OVERDUE -> gaugeRed
        MaintenanceReminderStatus.DUE_SOON -> gaugeYellow
        MaintenanceReminderStatus.UPCOMING -> gaugeCyan
        MaintenanceReminderStatus.OK -> gaugeGreen
        MaintenanceReminderStatus.COMPLETED -> textDim
    }

    val statusText = when (reminder.status) {
        MaintenanceReminderStatus.OVERDUE -> "ÜBERFÄLLIG"
        MaintenanceReminderStatus.DUE_SOON -> "Bald fällig"
        MaintenanceReminderStatus.UPCOMING -> "Bald fällig"
        MaintenanceReminderStatus.OK -> "OK"
        MaintenanceReminderStatus.COMPLETED -> "Erledigt"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Hauptzeile mit Icon und Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        getIconForMaintenanceType(reminder.type),
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = reminder.displayTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "Letzter Service: ${reminder.lastServiceKm} km",
                            fontSize = 11.sp,
                            color = textDim
                        )
                        Text(
                            text = "Intervall: ${reminder.intervalKm} km / ${reminder.intervalMonths} Monate",
                            fontSize = 10.sp,
                            color = textDim
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                    Text(
                        text = if (reminder.kmRemaining >= 0) {
                            "${reminder.kmRemaining} km"
                        } else {
                            "${-reminder.kmRemaining} km überfällig"
                        },
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fortschrittsbalken
            LinearProgressIndicator(
                progress = { (reminder.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = statusColor,
                trackColor = canopoSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Zeit-basierte Anzeige
            if (reminder.triggerType == ReminderTriggerType.KM_OR_TIME ||
                reminder.triggerType == ReminderTriggerType.TIME_BASED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Zeit bis Fälligkeit:",
                        fontSize = 10.sp,
                        color = textDim
                    )
                    Text(
                        text = if (reminder.monthsRemaining > 0) {
                            "${reminder.monthsRemaining} Monate"
                        } else {
                            "${-reminder.monthsRemaining} Monate überfällig"
                        },
                        fontSize = 10.sp,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Aktionen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDetails) {
                    Text("Details", fontSize = 12.sp, color = canopoAccent)
                }
                TextButton(onClick = { showIntervalDialog = true }) {
                    Text("Bearbeiten", fontSize = 12.sp, color = canopoAccent)
                }
                TextButton(onClick = onReset) {
                    Text("Reset", fontSize = 12.sp, color = gaugeYellow)
                }
            }
        }
    }

    if (showIntervalDialog) {
        IntervalEditDialog(
            currentKm = reminder.lastServiceKm,
            currentInterval = reminder.intervalKm,
            onDismiss = { showIntervalDialog = false },
            onConfirm = { km, interval ->
                onUpdate(km, interval)
                showIntervalDialog = false
            }
        )
    }
}

/**
 * Detail-Dialog für eine Wartungserinnerung
 */
@Composable
private fun ReminderDetailDialog(
    reminder: MaintenanceReminder,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = canopoSurface,
        title = {
            Text(
                text = reminder.title,
                color = textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                DetailRow("Beschreibung", reminder.description)
                DetailRow("Status", reminder.status.label)
                DetailRow("Fortschritt", "${reminder.progressPercent.roundToInt()}%")
                DetailRow("Verbleibende km", "${reminder.kmRemaining} km")
                DetailRow("Verbleibende Monate", "${reminder.monthsRemaining} Monate")
                DetailRow("Intervall (km)", "${reminder.intervalKm} km")
                DetailRow("Intervall (Monate)", "${reminder.intervalMonths} Monate")
                DetailRow("Fahrbedingungen", reminder.drivingConditions.label)

                if (reminder.partNumber.isNotEmpty()) {
                    DetailRow("Teile", reminder.partNumber)
                }
                if (reminder.torqueSpec.isNotEmpty()) {
                    DetailRow("Drehmoment", reminder.torqueSpec)
                }
                if (reminder.notes.isNotEmpty()) {
                    DetailRow("Hinweise", reminder.notes)
                }

                // Km-basiert vs. Zeit-basiert
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TriggerBadge(
                        text = "Km-basiert",
                        isActive = reminder.triggerType == ReminderTriggerType.KM_BASED ||
                            reminder.triggerType == ReminderTriggerType.KM_OR_TIME ||
                            reminder.triggerType == ReminderTriggerType.KM_AND_TIME
                    )
                    TriggerBadge(
                        text = "Zeit-basiert",
                        isActive = reminder.triggerType == ReminderTriggerType.TIME_BASED ||
                            reminder.triggerType == ReminderTriggerType.KM_OR_TIME ||
                            reminder.triggerType == ReminderTriggerType.KM_AND_TIME
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = canopoAccent)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = textPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun TriggerBadge(text: String, isActive: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isActive) { canopoAccent.copy(alpha = 0.2f) } else { canopoDark }
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = if (isActive) { canopoAccent } else { textDim },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Tab für Öltemperatur-Historie
 */
@Composable
private fun OilTempTab(stats: OilTempStatistics) {
    LazyColumn {
        // Übersicht
        item {
            OilTempOverviewCard(stats)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Statistiken
        if (stats.entryCount > 0) {
            item {
                OilTempStatisticsCard(stats)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Temperatur-Zonen
            item {
                OilTempZonesCard(stats)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Trend
            item {
                OilTempTrendCard(stats)
            }
        } else {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.Thermostat,
                    title = "Keine Daten",
                    message = "Fahren Sie das Auto, um Öltemperatur-Daten zu sammeln."
                )
            }
        }
    }
}

@Composable
private fun OilTempOverviewCard(stats: OilTempStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Thermostat,
                    contentDescription = null,
                    tint = canopoAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Öltemperatur-Übersicht",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TempStatItem("Durchschnitt", "${String.format("%.1f", stats.averageTempC)}°C", canopoAccent)
                TempStatItem("Min", "${String.format("%.1f", stats.minTempC)}°C", gaugeGreen)
                TempStatItem("Max", "${String.format("%.1f", stats.maxTempC)}°C", gaugeRed)
            }
        }
    }
}

@Composable
private fun TempStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = textSecondary
        )
    }
}

@Composable
private fun OilTempStatisticsCard(stats: OilTempStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Betriebsstatistik",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Optimal-Zeit
            StatBar(
                label = "Optimal (90-110°C)",
                percent = stats.optimalPercent,
                color = gaugeGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Zu heiß
            StatBar(
                label = "Zu heiß (>120°C)",
                percent = stats.tooHotPercent,
                color = gaugeRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Messwerte:",
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Text(
                    text = "${stats.entryCount}",
                    fontSize = 12.sp,
                    color = textPrimary
                )
            }
        }
    }
}

@Composable
private fun StatBar(label: String, percent: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = textSecondary
            )
            Text(
                text = "$percent%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = canopoSurface,
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun OilTempZonesCard(stats: OilTempStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Temperatur-Zonen",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TempZoneIndicator("Kalt", "<60°C", Color(0xFF60A5FA))
                TempZoneIndicator("Aufwärmen", "60-80°C", Color(0xFFFBBF24))
                TempZoneIndicator("Optimal", "80-110°C", Color(0xFF22C55E))
                TempZoneIndicator("Erhöht", "110-120°C", Color(0xFFF97316))
                TempZoneIndicator("Kritisch", ">120°C", Color(0xFFFF4444))
            }
        }
    }
}

@Composable
private fun TempZoneIndicator(label: String, range: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            color = color
        )
        Text(
            text = range,
            fontSize = 8.sp,
            color = textDim
        )
    }
}

@Composable
private fun OilTempTrendCard(stats: OilTempStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (stats.trend) {
                    TrendDirection.IMPROVING -> Icons.AutoMirrored.Filled.TrendingDown
                    TrendDirection.WORSENING -> Icons.AutoMirrored.Filled.TrendingUp
                    TrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                    TrendDirection.UNKNOWN -> Icons.AutoMirrored.Filled.HelpOutline
                },
                contentDescription = null,
                tint = when (stats.trend) {
                    TrendDirection.IMPROVING -> gaugeGreen
                    TrendDirection.WORSENING -> gaugeRed
                    TrendDirection.STABLE -> gaugeYellow
                    TrendDirection.UNKNOWN -> textDim
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Trend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = when (stats.trend) {
                        TrendDirection.IMPROVING -> "Temperaturen stabilisieren sich"
                        TrendDirection.WORSENING -> "Temperaturen steigen - Überprüfung empfohlen"
                        TrendDirection.STABLE -> "Temperaturen stabil"
                        TrendDirection.UNKNOWN -> "Nicht genug Daten für Trendanalyse"
                    },
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
        }
    }
}

/**
 * Tab für Kraftstoffverbrauch-Trend
 */
@Composable
private fun FuelConsumptionTab(stats: FuelConsumptionStatistics) {
    LazyColumn {
        // Übersicht
        item {
            FuelOverviewCard(stats)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (stats.entryCount > 0) {
            // Statistiken
            item {
                FuelStatisticsCard(stats)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Stadt vs. Autobahn
            if (stats.averageCityConsumption != null || stats.averageHighwayConsumption != null) {
                item {
                    FuelComparisonCard(stats)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Trend
            item {
                FuelTrendCard(stats)
            }
        } else {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.LocalGasStation,
                    title = "Keine Verbrauchsdaten",
                    message = "Fahren Sie das Auto, um Verbrauchsdaten zu erfassen."
                )
            }
        }
    }
}

@Composable
private fun FuelOverviewCard(stats: FuelConsumptionStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocalGasStation,
                    contentDescription = null,
                    tint = canopoAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kraftstoffverbrauch",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FuelStatItem(
                    "Durchschnitt",
                    "${String.format("%.1f", stats.averageConsumption)} L/100km",
                    canopoAccent
                )
                FuelStatItem(
                    "Bestwert",
                    "${String.format("%.1f", stats.minConsumption)} L/100km",
                    gaugeGreen
                )
                FuelStatItem(
                    "Maximal",
                    "${String.format("%.1f", stats.maxConsumption)} L/100km",
                    gaugeRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sollwert-Vergleich
            val targetDiff = stats.averageConsumption - MaintenanceService.FUEL_CONSUMPTION_TARGET
            val targetColor = when {
                targetDiff <= -0.5 -> gaugeGreen
                targetDiff <= 0.5 -> gaugeYellow
                targetDiff <= 1.5 -> gaugeOrange
                else -> gaugeRed
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Abweichung vom Sollwert:",
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Text(
                    text = if (targetDiff >= 0) {
                        "+${String.format("%.1f", targetDiff)} L"
                    } else {
                        "${String.format("%.1f", targetDiff)} L"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = targetColor
                )
            }
        }
    }
}

@Composable
private fun FuelStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = textSecondary
        )
    }
}

@Composable
private fun FuelStatisticsCard(stats: FuelConsumptionStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Verbrauchsstatistik",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatBar(
                label = "Im Sollbereich (≤6.0 L)",
                percent = stats.targetPercent,
                color = gaugeGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatBar(
                label = "Über Verbrauchsgrenze (>7.5 L)",
                percent = stats.warningPercent,
                color = gaugeRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Erfasste Fahrten:",
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Text(
                    text = "${stats.entryCount}",
                    fontSize = 12.sp,
                    color = textPrimary
                )
            }
        }
    }
}

@Composable
private fun FuelComparisonCard(stats: FuelConsumptionStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Stadt vs. Autobahn",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.LocationCity,
                        contentDescription = null,
                        tint = gaugeOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stats.averageCityConsumption?.let { "${String.format("%.1f", it)} L" }
                            ?: "N/A",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeOrange
                    )
                    Text(
                        text = "Stadt",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Route,
                        contentDescription = null,
                        tint = gaugeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stats.averageHighwayConsumption?.let { "${String.format("%.1f", it)} L" }
                            ?: "N/A",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeGreen
                    )
                    Text(
                        text = "Autobahn",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FuelTrendCard(stats: FuelConsumptionStatistics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (stats.trend) {
                    TrendDirection.IMPROVING -> Icons.AutoMirrored.Filled.TrendingDown
                    TrendDirection.WORSENING -> Icons.AutoMirrored.Filled.TrendingUp
                    TrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                    TrendDirection.UNKNOWN -> Icons.AutoMirrored.Filled.HelpOutline
                },
                contentDescription = null,
                tint = when (stats.trend) {
                    TrendDirection.IMPROVING -> gaugeGreen
                    TrendDirection.WORSENING -> gaugeRed
                    TrendDirection.STABLE -> gaugeYellow
                    TrendDirection.UNKNOWN -> textDim
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Verbrauchstrend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = when (stats.trend) {
                        TrendDirection.IMPROVING -> "Verbrauch sinkt - gutes Fahrverhalten!"
                        TrendDirection.WORSENING -> "Verbrauch steigt - Fahrstil überprüfen"
                        TrendDirection.STABLE -> "Verbrauch stabil"
                        TrendDirection.UNKNOWN -> "Nicht genug Daten für Trendanalyse"
                    },
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
        }
    }
}

/**
 * Tab für Wartungskosten
 */
@Composable
private fun CostsTab(costEstimate: MaintenanceCostEstimate) {
    LazyColumn {
        // Gesamtkosten
        item {
            CostsOverviewCard(costEstimate)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Kosten-Details
        if (costEstimate.details.isNotEmpty()) {
            item {
                Text(
                    text = "Aufschlüsselung",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(costEstimate.details) { detail ->
                CostDetailItem(detail)
                Spacer(modifier = Modifier.height(4.dp))
            }
        } else {
            item {
                EmptyStateCard(
                    icon = Icons.Filled.CheckCircle,
                    title = "Keine anstehenden Kosten",
                    message = "Alle Wartungen sind aktuell."
                )
            }
        }

        // Tipp
        item {
            Spacer(modifier = Modifier.height(12.dp))
            TipsCard()
        }
    }
}

@Composable
private fun CostsOverviewCard(costEstimate: MaintenanceCostEstimate) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Euro,
                    contentDescription = null,
                    tint = canopoAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Geschätzte Wartungskosten",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.0f", costEstimate.totalEstimated)} €",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoAccent
                    )
                    Text(
                        text = "Gesamt geschätzt",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dringend:",
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Text(
                    text = "${costEstimate.urgentItems} Artikel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeRed
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Anstehend:",
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Text(
                    text = "${costEstimate.upcomingItems} Artikel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeYellow
                )
            }
        }
    }
}

@Composable
private fun CostDetailItem(detail: MaintenanceCostDetail) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = canopoDark.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    getIconForMaintenanceType(detail.type),
                    contentDescription = null,
                    tint = if (detail.isUrgent) { gaugeRed } else { gaugeYellow },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = detail.type.label,
                    fontSize = 12.sp,
                    color = textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (detail.isUrgent) {
                    Text(
                        text = "DRINGEND",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "~${String.format("%.0f", detail.estimatedCost)} €",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }
        }
    }
}

@Composable
private fun TipsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = gaugeYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tipps zur Kostenersparnis",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TipItem("Ölwechsel-Intervalle einhalten verhindert teure Motorschäden")
            TipItem("Luftfilter selbst wechseln spart ~20€ Arbeitszeit")
            TipItem("Zündkerzen-Tausch alle 60.000km hält Verbrauch niedrig")
            TipItem("Turbo-Inspektion verhindert Ladedruckverlust und Leistungsverlust")
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 10.sp,
            color = gaugeYellow,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            color = textSecondary
        )
    }
}

/**
 * Allgemeine leere Zustandskarte
 */
@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textDim,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = textDim,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dialog zum Hinzufügen einer neuen Erinnerung
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (MaintenanceReminder) -> Unit
) {
    var selectedType by remember { mutableStateOf(MaintenanceType.OIL_CHANGE) }
    var intervalKmText by remember { mutableStateOf("15000") }
    var intervalMonthsText by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = canopoSurface,
        title = { Text("Neue Erinnerung", color = textPrimary) },
        text = {
            Column {
                // Typ-Auswahl
                Text(
                    text = "Wartungsart:",
                    fontSize = 12.sp,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow {
                    items(MaintenanceType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = canopoAccent.copy(alpha = 0.2f),
                                selectedLabelColor = canopoAccent
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Km-Intervall
                OutlinedTextField(
                    value = intervalKmText,
                    onValueChange = { intervalKmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Intervall (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = canopoAccent,
                        focusedLabelColor = canopoAccent,
                        cursorColor = canopoAccent
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Monats-Intervall
                OutlinedTextField(
                    value = intervalMonthsText,
                    onValueChange = { intervalMonthsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Intervall (Monate)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = canopoAccent,
                        focusedLabelColor = canopoAccent,
                        cursorColor = canopoAccent
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val km = intervalKmText.toIntOrNull() ?: 15000
                val months = intervalMonthsText.toIntOrNull() ?: 12
                val reminder = MaintenanceReminder(
                    type = selectedType,
                    title = selectedType.label,
                    description = "",
                    priority = ReminderPriority.MEDIUM,
                    triggerType = ReminderTriggerType.KM_OR_TIME,
                    intervalKm = km,
                    intervalMonths = months
                )
                onConfirm(reminder)
                onDismiss()
            }) {
                Text("Hinzufügen", color = canopoAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = textSecondary)
            }
        }
    )
}

/**
 * Interval-Edit-Dialog (erweitert mit Monats-Option)
 */
@Composable
private fun IntervalEditDialog(
    currentKm: Int,
    currentInterval: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var kmText by remember { mutableStateOf(currentKm.toString()) }
    var intervalText by remember { mutableStateOf(currentInterval.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = canopoSurface,
        title = { Text(stringResource(R.string.maintenance_set_interval), color = textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = kmText,
                    onValueChange = { kmText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.maintenance_set_km)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = canopoAccent,
                        focusedLabelColor = canopoAccent,
                        cursorColor = canopoAccent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { intervalText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.maintenance_km)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = canopoAccent,
                        focusedLabelColor = canopoAccent,
                        cursorColor = canopoAccent
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val km = kmText.toIntOrNull() ?: currentKm
                val interval = intervalText.toIntOrNull() ?: currentInterval
                onConfirm(km, interval)
            }) {
                Text("OK", color = canopoAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = textSecondary)
            }
        }
    )
}

/**
 * Hilfsfunktion: Icon für Wartungstyp
 */
@Composable
private fun getIconForMaintenanceType(type: MaintenanceType) = when (type) {
    MaintenanceType.OIL_CHANGE -> Icons.Filled.OilBarrel
    MaintenanceType.TIRES -> Icons.Filled.TireRepair
    MaintenanceType.INSPECTION -> Icons.AutoMirrored.Filled.Assignment
    MaintenanceType.BRAKE_PADS -> Icons.Filled.Warning
    MaintenanceType.AIR_FILTER -> Icons.Filled.Air
    MaintenanceType.TRANSMISSION_FLUID -> Icons.Filled.Settings
    MaintenanceType.TURBO_INSPECTION -> Icons.Filled.Settings
    MaintenanceType.COOLANT -> Icons.Filled.Settings
    MaintenanceType.SPARK_PLUGS -> Icons.Filled.Settings
    MaintenanceType.TURBO_BOOST_CHECK -> Icons.Filled.Speed
    MaintenanceType.TIMING_CHAIN -> Icons.Filled.Warning
}
