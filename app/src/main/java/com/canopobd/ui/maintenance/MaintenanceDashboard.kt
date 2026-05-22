package com.canopobd.ui.maintenance

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.maintenance.MaintenanceScheduler
import com.canopobd.data.maintenance.MaintenanceScheduler.CostEstimate
import com.canopobd.data.maintenance.MaintenanceScheduler.MaintenanceAlert
import com.canopobd.data.maintenance.MaintenanceScheduler.ScheduledMaintenance
import com.canopobd.data.maintenance.MaintenanceScheduler.TimelineEntry
import com.canopobd.data.maintenance.MaintenanceReminderStatus
import com.canopobd.data.maintenance.PartDatabase
import com.canopobd.data.maintenance.PartDatabase.AlternativePart
import com.canopobd.data.maintenance.PartDatabase.PartInfo
import com.canopobd.data.maintenance.ReminderPriority
import com.canopobd.data.model.MaintenanceType
import com.canopobd.ui.theme.canopoAccent
import com.canopobd.ui.theme.canopoDark
import com.canopobd.ui.theme.canopoHighlight
import com.canopobd.ui.theme.canopoSurface
import com.canopobd.ui.theme.gaugeCyan
import com.canopobd.ui.theme.gaugeGreen
import com.canopobd.ui.theme.gaugeOrange
import com.canopobd.ui.theme.gaugeRed
import com.canopobd.ui.theme.gaugeYellow
import com.canopobd.ui.theme.textDim
import com.canopobd.ui.theme.textPrimary
import com.canopobd.ui.theme.textSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceDashboard(
    currentKm: Int,
    onDismiss: () -> Unit,
    onCompleteMaintenance: (String, Int) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Timeline", "Service", "Kosten", "Teile")
    val scheduler = remember { MaintenanceScheduler.apply { initialize(currentKm) } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DashboardHeader(currentKm, onDismiss)
                Spacer(modifier = Modifier.height(8.dp))

                MaintenanceStatusBadges(scheduler)
                Spacer(modifier = Modifier.height(8.dp))

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
                                    color = if (selectedTab == index) canopoAccent else textSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> TimelineTab(scheduler, currentKm)
                    1 -> ServiceItemsTab(scheduler, currentKm, onCompleteMaintenance)
                    2 -> CostTrackerTab(scheduler)
                    3 -> PartsDatabaseTab()
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(currentKm: Int, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Build,
                contentDescription = null,
                tint = canopoAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Wartungs-Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = canopoHighlight
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Speed, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${String.format("%,d", currentKm)} km",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = canopoAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Schlie\u00dfen", tint = textSecondary)
            }
        }
    }
}

@Composable
private fun MaintenanceStatusBadges(scheduler: MaintenanceScheduler) {
    val overdue = scheduler.getOverdueMaintenance().size
    val dueSoon = scheduler.getDueSoonMaintenance().size
    val upcoming = scheduler.getUpcomingMaintenance().size

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = canopoDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusBadge(count = overdue, label = "\u00dcberf\u00e4llig", color = gaugeRed, icon = Icons.Filled.Warning)
            StatusBadge(count = dueSoon, label = "F\u00e4llig", color = gaugeYellow, icon = Icons.Filled.Schedule)
            StatusBadge(count = upcoming, label = "Bald f\u00e4llig", color = gaugeCyan, icon = Icons.Filled.Event)
        }
    }
}

@Composable
private fun StatusBadge(
    count: Int,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = textSecondary
        )
    }
}

@Composable
private fun TimelineTab(scheduler: MaintenanceScheduler, currentKm: Int) {
    val timeline = remember { scheduler.getMaintenanceTimeline(currentKm) }

    LazyColumn {
        item {
            TimelineHeaderCard(scheduler, currentKm)
            Spacer(modifier = Modifier.height(12.dp))
        }

        itemsIndexed(timeline) { index, entry ->
            TimelineEntryCard(entry, currentKm, isLast = index == timeline.lastIndex)
        }
    }
}

@Composable
private fun TimelineHeaderCard(scheduler: MaintenanceScheduler, currentKm: Int) {
    val nextService = scheduler.getNextService()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timeline, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wartungs-Timeline",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            nextService?.let { service ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "N\u00e4chster Service:",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Text(
                        text = service.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = canopoAccent
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "F\u00e4llig bei:",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Text(
                        text = "${String.format("%,d", service.nextDueKm)} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (service.status) {
                            MaintenanceReminderStatus.OVERDUE -> gaugeRed
                            MaintenanceReminderStatus.DUE_SOON -> gaugeYellow
                            else -> textPrimary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineEntryCard(
    entry: TimelineEntry,
    currentKm: Int,
    isLast: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (entry.status) {
        MaintenanceReminderStatus.OVERDUE -> gaugeRed
        MaintenanceReminderStatus.DUE_SOON -> gaugeYellow
        MaintenanceReminderStatus.UPCOMING -> gaugeCyan
        MaintenanceReminderStatus.OK -> gaugeGreen
        MaintenanceReminderStatus.COMPLETED -> textDim
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight(if (expanded) 0.95f else 0.7f)
                        .background(canopoDark)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(8.dp),
            color = canopoDark
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                entry.kmRemaining < 0 -> "\u00dcberf\u00e4llig: ${-entry.kmRemaining}km"
                                else -> "in ${String.format("%,d", entry.kmRemaining)}km"
                            },
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = textDim,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = ((currentKm - (entry.dueKm - 15000)).toFloat() / 15000).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = statusColor,
                        trackColor = canopoSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Gesch\u00e4tzt: ${String.format("%.0f", entry.estimatedCost)}\u20AC",
                            fontSize = 10.sp,
                            color = textDim
                        )
                        Text(
                            text = "${entry.daysRemaining} Tage",
                            fontSize = 10.sp,
                            color = textDim
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceItemsTab(
    scheduler: MaintenanceScheduler,
    currentKm: Int,
    onCompleteMaintenance: (String, Int) -> Unit
) {
    val allItems = remember { scheduler.getAllScheduledMaintenance() }
    var selectedType by remember { mutableStateOf<MaintenanceType?>(null) }

    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service-\u00dcbersicht",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "${allItems.size} Eintr\u00e4ge",
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        val sortedItems = allItems.sortedBy { it.status.ordinal }
        items(sortedItems) { item ->
            ServiceItemCard(
                item = item,
                currentKm = currentKm,
                onComplete = { onCompleteMaintenance(item.id, currentKm) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ServiceItemCard(
    item: ScheduledMaintenance,
    currentKm: Int,
    onComplete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (item.status) {
        MaintenanceReminderStatus.OVERDUE -> gaugeRed
        MaintenanceReminderStatus.DUE_SOON -> gaugeYellow
        MaintenanceReminderStatus.UPCOMING -> gaugeCyan
        MaintenanceReminderStatus.OK -> gaugeGreen
        MaintenanceReminderStatus.COMPLETED -> textDim
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(10.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "Intervall: ${String.format("%,d", item.intervalKm)} km / ${item.intervalMonths} Monate",
                            fontSize = 10.sp,
                            color = textDim
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (item.status) {
                            MaintenanceReminderStatus.OVERDUE -> "\u00dcBERF\u00c4LLIG"
                            MaintenanceReminderStatus.DUE_SOON -> "F\u00c4LLIG"
                            MaintenanceReminderStatus.UPCOMING -> "BALD"
                            MaintenanceReminderStatus.OK -> "OK"
                            MaintenanceReminderStatus.COMPLETED -> "ERLEDIGT"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    val kmRemaining = item.nextDueKm - currentKm
                    Text(
                        text = "${String.format("%,d", kmRemaining)} km",
                        fontSize = 11.sp,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val progressValue = ((currentKm - item.lastServiceKm).toFloat() / item.intervalKm.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = progressValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = statusColor,
                trackColor = canopoSurface
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = canopoSurface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                DetailRow("OEM Teilenummer", item.partNumber)
                if (item.alternatives.isNotEmpty()) {
                    DetailRow("Alternativen", item.alternatives.joinToString(", "))
                }
                DetailRow("Letzter Service", "${String.format("%,d", item.lastServiceKm)} km")
                DetailRow("N\u00e4chster Service", "${String.format("%,d", item.nextDueKm)} km")
                DetailRow("Kosten DIY", "${String.format("%.0f", item.costEstimate.diyMin)}-${String.format("%.0f", item.costEstimate.diyMax)}\u20AC")
                DetailRow("Kosten Werkstatt", "${String.format("%.0f", item.costEstimate.workshopMin)}-${String.format("%.0f", item.costEstimate.workshopMax)}\u20AC")
                DetailRow("Ersparnis DIY", "${String.format("%.0f", item.costEstimate.savingsPotential)}\u20AC")

                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = canopoSurface.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.notes,
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onComplete) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = gaugeGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Erledigt", fontSize = 12.sp, color = gaugeGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = textSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = textPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CostTrackerTab(scheduler: MaintenanceScheduler) {
    val costSummary = remember { scheduler.getTotalEstimatedCosts() }
    val allItems = remember { scheduler.getAllScheduledMaintenance() }

    LazyColumn {
        item {
            CostOverviewCard(costSummary)
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Text(
                text = "Kostenaufschl\u00fcsselung",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val costItems = allItems.filter {
            it.status == MaintenanceReminderStatus.OVERDUE ||
            it.status == MaintenanceReminderStatus.DUE_SOON ||
            it.status == MaintenanceReminderStatus.UPCOMING
        }.sortedBy { it.status.ordinal }

        items(costItems) { item ->
            CostItemRow(item)
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            DiySavingsCard(allItems)
        }
    }
}

@Composable
private fun CostOverviewCard(summary: MaintenanceScheduler.MaintenanceCostSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Euro, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kosten-\u00dcbersicht",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${String.format("%.0f", summary.total)}\u20AC",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = canopoAccent,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Gesamt gesch\u00e4tzt (Werkstatt)",
                fontSize = 11.sp,
                color = textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Dringend", fontSize = 10.sp, color = textSecondary)
                    Text(
                        text = "${String.format("%.0f", summary.urgentTotal)}\u20AC (${summary.urgentCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeRed
                    )
                }
                Column {
                    Text(text = "Bald f\u00e4llig", fontSize = 10.sp, color = textSecondary)
                    Text(
                        text = "${String.format("%.0f", summary.warningTotal)}\u20AC (${summary.warningCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeYellow
                    )
                }
                Column {
                    Text(text = "Geplant", fontSize = 10.sp, color = textSecondary)
                    Text(
                        text = "${String.format("%.0f", summary.infoTotal)}\u20AC (${summary.infoCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun CostItemRow(item: ScheduledMaintenance) {
    val statusColor = when (item.status) {
        MaintenanceReminderStatus.OVERDUE -> gaugeRed
        MaintenanceReminderStatus.DUE_SOON -> gaugeYellow
        MaintenanceReminderStatus.UPCOMING -> gaugeCyan
        else -> textDim
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = canopoDark.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.title,
                    fontSize = 12.sp,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    Text(
                        text = "DIY: ${String.format("%.0f", item.costEstimate.averageDiy)}\u20AC",
                        fontSize = 10.sp,
                        color = gaugeGreen
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WS: ${String.format("%.0f", item.costEstimate.averageWorkshop)}\u20AC",
                        fontSize = 10.sp,
                        color = gaugeOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun DiySavingsCard(items: List<ScheduledMaintenance>) {
    val totalSavings = items.sumOf { it.costEstimate.savingsPotential }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Build, contentDescription = null, tint = gaugeGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DIY-Ersparnis-Potenzial",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bis zu ${String.format("%.0f", totalSavings)}\u20AC sparen durch Selbermachen!",
                fontSize = 13.sp,
                color = gaugeGreen,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val easyDiy = items.filter { it.costEstimate.diyMin > 0 && it.costEstimate.laborHours <= 0.5 }
            if (easyDiy.isNotEmpty()) {
                Text(
                    text = "Einfache DIY-Wartungen:",
                    fontSize = 11.sp,
                    color = textSecondary
                )
                easyDiy.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\u2022",
                            fontSize = 10.sp,
                            color = gaugeGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.title} - spare ${String.format("%.0f", item.costEstimate.savingsPotential)}\u20AC",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartsDatabaseTab() {
    var searchQuery by remember { mutableStateOf("") }
    val allParts = remember { PartDatabase.allParts }
    val filteredParts = remember(searchQuery) {
        if (searchQuery.isBlank()) allParts
        else PartDatabase.searchPart(searchQuery)
    }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Teil suchen...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = canopoAccent,
                focusedLabelColor = canopoAccent,
                cursorColor = canopoAccent,
                unfocusedBorderColor = textDim,
                unfocusedLabelColor = textDim
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredParts) { part ->
                PartCard(part)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun PartCard(part: PartInfo) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(10.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = part.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    )
                    Text(
                        text = part.oemPartNumber,
                        fontSize = 10.sp,
                        color = canopoAccent
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = part.priceRange.format(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = gaugeGreen
                    )
                    Text(
                        text = "${part.alternatives.size} Alternativen",
                        fontSize = 9.sp,
                        color = textDim
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = canopoSurface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Spezifikationen",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary
                )
                Text(
                    text = part.specifications,
                    fontSize = 10.sp,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (part.alternatives.isNotEmpty()) {
                    Text(
                        text = "Verf\u00fcgbare Alternativen",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    part.alternatives.forEach { alt ->
                        AlternativePartRow(alt)
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Wo kaufen?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                part.whereToBuy.forEach { source ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = when (source.type) {
                                PartDatabase.SourceType.OEM_DEALER -> gaugeOrange
                                PartDatabase.SourceType.ONLINE_SHOP -> canopoAccent
                                PartDatabase.SourceType.AUTO_PARTS_RETAILER -> gaugeGreen
                                PartDatabase.SourceType.WRECKING_YARD -> textDim
                            },
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = source.name,
                            fontSize = 10.sp,
                            color = textPrimary
                        )
                        if (source.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${source.notes})",
                                fontSize = 9.sp,
                                color = textDim
                            )
                        }
                    }
                }

                if (part.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (part.notes.contains("PROBLEM") || part.notes.contains("Pflicht") || part.notes.contains("PFlicht"))
                            gaugeRed.copy(alpha = 0.15f) else canopoSurface.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                if (part.notes.contains("PROBLEM")) Icons.Filled.Warning else Icons.Filled.Info,
                                contentDescription = null,
                                tint = if (part.notes.contains("PROBLEM")) gaugeRed else canopoAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = part.notes,
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlternativePartRow(alt: AlternativePart) {
    val qualityColor = when (alt.qualityRating) {
        PartDatabase.QualityRating.OEM -> gaugeGreen
        PartDatabase.QualityRating.ORIGINAL_EQUIVALENT -> gaugeGreen
        PartDatabase.QualityRating.AFTERMARKET_PREMIUM -> canopoAccent
        PartDatabase.QualityRating.AFTERMARKET_BUDGET -> gaugeYellow
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = canopoSurface.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(
                    text = alt.brand,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = alt.partNumber,
                    fontSize = 9.sp,
                    color = textDim
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = qualityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = alt.qualityRating.label,
                        fontSize = 8.sp,
                        color = qualityColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = alt.priceRange.format(),
                    fontSize = 10.sp,
                    color = textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
