package com.canopobd.ui.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtendedMaintenanceDialog(
    currentKm: Int,
    onDismiss: () -> Unit,
    onCompleteService: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Service-Plan", "Nächste Wartung", "Kosten", "Erinnerungen")

    val services = remember(currentKm) {
        AstraJServicePlan.createServiceEntries(currentKm)
    }

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
                ExtendedHeader(currentKm, onDismiss)

                Spacer(modifier = Modifier.height(8.dp))

                ServiceOverviewSummary(services)

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
                                    color = if (selectedTab == index) { canopoAccent } else { textSecondary }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> ServicePlanTab(services, currentKm, onCompleteService)
                    1 -> NextServiceTab(services, currentKm)
                    2 -> CostEstimationTab(services)
                    3 -> ReminderConfigTab(services)
                }
            }
        }
    }
}

@Composable
private fun ExtendedHeader(currentKm: Int, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.DirectionsCar,
                contentDescription = null,
                tint = canopoAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Erweiterte Wartung",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )
                Text(
                    text = "Opel Astra J 1.4 Turbo (A14NET)",
                    fontSize = 11.sp,
                    color = textDim
                )
            }
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
                Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = textSecondary)
            }
        }
    }
}

@Composable
private fun ServiceOverviewSummary(services: List<ServiceEntry>) {
    val overdue = services.count { it.status == ServiceStatus.OVERDUE }
    val dueSoon = services.count { it.status == ServiceStatus.DUE_SOON }
    val ok = services.count { it.status == ServiceStatus.OK }

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
            SummaryChip(count = overdue, label = "Überfällig", color = gaugeRed)
            SummaryChip(count = dueSoon, label = "Fällig", color = gaugeYellow)
            SummaryChip(count = ok, label = "OK", color = gaugeGreen)
        }
    }
}

@Composable
private fun SummaryChip(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
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

//region Service Plan Tab

@Composable
private fun ServicePlanTab(
    services: List<ServiceEntry>,
    currentKm: Int,
    onCompleteService: (String, Int, Int) -> Unit
) {
    LazyColumn {
        itemsIndexed(services) { index, service ->
            ServiceEntryCard(
                service = service,
                currentKm = currentKm,
                onComplete = { onCompleteService(service.id, currentKm, service.intervalKm) }
            )
            if (index < services.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ServiceEntryCard(
    service: ServiceEntry,
    currentKm: Int,
    onComplete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var reminderEnabled by remember { mutableStateOf(service.reminderEnabled) }

    val statusColor = when (service.status) {
        ServiceStatus.OVERDUE -> gaugeRed
        ServiceStatus.DUE_SOON -> gaugeYellow
        ServiceStatus.UPCOMING -> gaugeCyan
        ServiceStatus.OK -> gaugeGreen
        ServiceStatus.COMPLETED -> textDim
    }

    val statusLabel = when (service.status) {
        ServiceStatus.OVERDUE -> "ÜBERFÄLLIG"
        ServiceStatus.DUE_SOON -> "FÄLLIG"
        ServiceStatus.UPCOMING -> "BALD"
        ServiceStatus.OK -> "OK"
        ServiceStatus.COMPLETED -> "ERLEDIGT"
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                service.icon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = service.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }
                        Text(
                            text = service.spec,
                            fontSize = 10.sp,
                            color = textDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    val kmRemaining = service.dueKm - currentKm
                    Text(
                        text = when {
                            kmRemaining < 0 -> "${-kmRemaining} km überschritten"
                            else -> "in ${String.format("%,d", kmRemaining)} km"
                        },
                        fontSize = 11.sp,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val progressValue = if (service.intervalKm > 0) {
                ((currentKm - service.lastServiceKm).toFloat() / service.intervalKm.toFloat()).coerceIn(0f, 1f)
            } else { 0f }
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = statusColor,
                trackColor = canopoSurface
            )

            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format("%,d", service.lastServiceKm)} km",
                    fontSize = 9.sp,
                    color = textDim
                )
                Text(
                    text = "${service.progressPercent.roundToInt()}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = "${String.format("%,d", service.dueKm)} km",
                    fontSize = 9.sp,
                    color = textDim
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = canopoSurface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                ServiceDetailRow("Intervall", "${String.format("%,d", service.intervalKm)} km / ${service.intervalMonths} Monate")
                ServiceDetailRow("Spezifikation", service.spec)
                ServiceDetailRow("Teile-Nr.", service.partNumber)

                if (service.alternatives.isNotEmpty()) {
                    ServiceDetailRow("Alternativen", service.alternatives)
                }

                if (service.criticalNote.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = gaugeRed.copy(alpha = 0.12f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = gaugeRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = service.criticalNote,
                                fontSize = 10.sp,
                                color = gaugeRed
                            )
                        }
                    }
                }

                if (service.technicalNote.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = canopoSurface.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                Icons.Filled.Build,
                                contentDescription = null,
                                tint = canopoAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = service.technicalNote,
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = if (reminderEnabled) { gaugeGreen } else { textDim },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Erinnerung",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = canopoAccent,
                                checkedTrackColor = canopoAccent.copy(alpha = 0.3f),
                                uncheckedThumbColor = textDim,
                                uncheckedTrackColor = canopoSurface
                            )
                        )
                    }
                    TextButton(onClick = onComplete) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = gaugeGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Erledigt", fontSize = 12.sp, color = gaugeGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceDetailRow(label: String, value: String) {
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

//endregion

//region Next Service Tab

@Composable
private fun NextServiceTab(services: List<ServiceEntry>, currentKm: Int) {
    val nextServices = remember(services, currentKm) {
        services
            .filter { it.status != ServiceStatus.COMPLETED && it.status != ServiceStatus.OK }
            .sortedBy { it.dueKm }
            .take(5)
    }

    LazyColumn {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = canopoDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nächste anstehende Wartungen",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sortiert nach Dringlichkeit",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (nextServices.isEmpty()) {
            item {
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
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = gaugeGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Alles OK!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = gaugeGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keine Wartungen anstehend.",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }
            }
        }

        itemsIndexed(nextServices) { index, service ->
            NextServiceCard(service, currentKm, index)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun NextServiceCard(service: ServiceEntry, currentKm: Int, index: Int) {
    val statusColor = when (service.status) {
        ServiceStatus.OVERDUE -> gaugeRed
        ServiceStatus.DUE_SOON -> gaugeYellow
        ServiceStatus.UPCOMING -> gaugeCyan
        ServiceStatus.OK -> gaugeGreen
        ServiceStatus.COMPLETED -> textDim
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = canopoDark
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                val kmRemaining = service.dueKm - currentKm
                Text(
                    text = when {
                        kmRemaining < 0 -> "Überfällig seit ${-kmRemaining} km"
                        else -> "Fällig bei ${String.format("%,d", service.dueKm)} km"
                    },
                    fontSize = 11.sp,
                    color = statusColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.0f", service.costWorkshop)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeOrange
                )
                Text(
                    text = "Werkstatt",
                    fontSize = 9.sp,
                    color = textDim
                )
            }
        }
    }
}

//endregion

//region Cost Estimation Tab

@Composable
private fun CostEstimationTab(services: List<ServiceEntry>) {
    val totalDiy = remember(services) { services.sumOf { it.costDiy } }
    val totalWorkshop = remember(services) { services.sumOf { it.costWorkshop } }
    val totalSavings = totalWorkshop - totalDiy

    LazyColumn {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = canopoDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kostenübersicht",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CostColumn("DIY", totalDiy, gaugeGreen)
                        CostColumn("Werkstatt", totalWorkshop, gaugeOrange)
                        CostColumn("Ersparnis", totalSavings, canopoAccent)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = gaugeGreen.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = gaugeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bis zu ${String.format("%.0f", totalSavings)} € sparen durch Selbermachen!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = gaugeGreen
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = "Kostenaufschlüsselung",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        itemsIndexed(services) { _, service ->
            CostDetailCard(service)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CostColumn(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${String.format("%.0f", amount)} €",
            fontSize = 20.sp,
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
private fun CostDetailCard(service: ServiceEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = canopoDark.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    service.icon,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = service.title,
                    fontSize = 12.sp,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DIY: ${String.format("%.0f", service.costDiy)} €",
                    fontSize = 10.sp,
                    color = gaugeGreen
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "WS: ${String.format("%.0f", service.costWorkshop)} €",
                    fontSize = 10.sp,
                    color = gaugeOrange
                )
            }
        }
    }
}

//endregion

//region Reminder Config Tab

@Composable
private fun ReminderConfigTab(services: List<ServiceEntry>) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = canopoDark
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = canopoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Erinnerungs-Konfiguration",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Konfigurieren Sie Erinnerungen für jeden Service. " +
                            "Sie werden vor Ablauf des Intervalls benachrichtigt.",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(services) { _, service ->
            ReminderItem(service)
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = canopoAccent)
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Erinnerung hinzufügen", fontSize = 14.sp)
            }
        }
    }

    if (showAddDialog) {
        AddCustomReminderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { showAddDialog = false }
        )
    }
}

@Composable
private fun ReminderItem(service: ServiceEntry) {
    var enabled by remember { mutableStateOf(service.reminderEnabled) }

    val statusColor = when (service.status) {
        ServiceStatus.OVERDUE -> gaugeRed
        ServiceStatus.DUE_SOON -> gaugeYellow
        ServiceStatus.UPCOMING -> gaugeCyan
        ServiceStatus.OK -> gaugeGreen
        ServiceStatus.COMPLETED -> textDim
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = canopoDark.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                Text(
                    text = "Erinnern bei: ${String.format("%,d", service.reminderThresholdKm)} km vor Fälligkeit",
                    fontSize = 10.sp,
                    color = textDim
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = canopoAccent,
                    checkedTrackColor = canopoAccent.copy(alpha = 0.3f),
                    uncheckedThumbColor = textDim,
                    uncheckedTrackColor = canopoSurface
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var intervalKm by remember { mutableStateOf("15000") }
    var intervalMonths by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = canopoSurface,
        title = { Text("Neue Erinnerung", color = textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bezeichnung") },
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
                    value = intervalKm,
                    onValueChange = { intervalKm = it.filter { c -> c.isDigit() } },
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
                OutlinedTextField(
                    value = intervalMonths,
                    onValueChange = { intervalMonths = it.filter { c -> c.isDigit() } },
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
            TextButton(onClick = onConfirm) {
                Text("Hinzufügen", color = canopoAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = textSecondary)
            }
        }
    )
}

//endregion

//region Data Model

enum class ServiceStatus {
    OVERDUE,
    DUE_SOON,
    UPCOMING,
    OK,
    COMPLETED
}

data class ServiceEntry(
    val id: String,
    val title: String,
    val spec: String,
    val partNumber: String,
    val alternatives: String,
    val intervalKm: Int,
    val intervalMonths: Int,
    val lastServiceKm: Int,
    val dueKm: Int,
    val status: ServiceStatus,
    val progressPercent: Float,
    val costDiy: Double,
    val costWorkshop: Double,
    val reminderEnabled: Boolean,
    val reminderThresholdKm: Int,
    val criticalNote: String,
    val technicalNote: String,
    val icon: ImageVector
)

object AstraJServicePlan {

    fun createServiceEntries(currentKm: Int): List<ServiceEntry> = listOf(
        createOilChange(currentKm),
        createTimingBelt(currentKm),
        createTransmissionOil(currentKm),
        createSparkPlugs(currentKm),
        createAirFilter(currentKm),
        createCabinFilter(currentKm),
        createCoolant(currentKm),
        createTurboInspectionVisual(currentKm),
        createTurboInspectionPressure(currentKm),
        createBrakePadsFront(currentKm),
        createBrakePadsRear(currentKm),
        createPCVValve(currentKm),
        createMAFSensor(currentKm)
    )

    private fun createOilChange(currentKm: Int): ServiceEntry {
        val intervalKm = 15000
        val intervalMonths = 12
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "oil_change",
            title = "Ölwechsel",
            spec = "Dexos2 5W-30 (4.5L inkl. Filter)",
            partNumber = "Opel 13538630 / Mann HU7019z",
            alternatives = "Mann HU7019z, Bosch P7024, Mahle OX353D",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 42.0,
            costWorkshop = 100.0,
            reminderEnabled = true,
            reminderThresholdKm = 1000,
            criticalNote = "Dexos2 5W-30 ist Pflicht! Verwendung anderer Öle kann Motorschäden verursachen.",
            technicalNote = "Bei Kurzstrecke/Stadtverkehr: Intervall auf 10.000 km / 8 Monate verkürzen. Ölfüllstand regelmäßig prüfen.",
            icon = Icons.Filled.LocalFireDepartment
        )
    }

    private fun createTimingBelt(currentKm: Int): ServiceEntry {
        val intervalKm = 150000
        val intervalMonths = 120
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "timing_chain",
            title = "Timing-Kette Prüfung",
            spec = "Steuerkette + Kettenspanner prüfen",
            partNumber = "Opel 12618087 / INA 421009710",
            alternatives = "Sachs 186726, SLM 24420398",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 0.0,
            costWorkshop = 150.0,
            reminderEnabled = true,
            reminderThresholdKm = 5000,
            criticalNote = "A14NET: Kettenspanner defekt oft ab 80.000km! Rattern bei Kaltstart = P0340/P0341 = SOFORT handeln!",
            technicalNote = "A14NET hat STOFFKETTE, kein Zahnriemen! Kettenspanner, Leitschienen und Spannschiene prüfen. Bei Verschleiß: kompletter Steuerkettensatz.",
            icon = Icons.Filled.Warning
        )
    }

    private fun createTransmissionOil(currentKm: Int): ServiceEntry {
        val intervalKm = 80000
        val intervalMonths = 48
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "transmission_oil",
            title = "Getriebeöl (M32)",
            spec = "75W-80 GL-4 (M32: 2.7L)",
            partNumber = "GM Fluid 1940182 / Febi 03861",
            alternatives = "Redline MTL, Motul 75W-80,ravenol 75W-80 GL-4",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 50.0,
            costWorkshop = 140.0,
            reminderEnabled = true,
            reminderThresholdKm = 3000,
            criticalNote = "Falsches Öl kann Getriebeschaden verursachen! Nur 75W-80 GL-4 für M32 verwenden!",
            technicalNote = "M32-Getriebe: Nur saugfähige Dichtung verwenden! Kein Dexron VI (Automatiköl) verwenden! Intervalle: 60.000-80.000 km je nach Nutzung.",
            icon = Icons.Filled.Build
        )
    }

    private fun createBrakePadsFront(currentKm: Int): ServiceEntry {
        val intervalKm = 30000
        val intervalMonths = 24
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "brake_pads_front",
            title = "Bremsbeläge vorne",
            spec = "286 mm Scheibendurchmesser",
            partNumber = "Opel 13501636",
            alternatives = "TRW D1428L, Akebono ACT1428, Brembo P50073",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 40.0,
            costWorkshop = 120.0,
            reminderEnabled = true,
            reminderThresholdKm = 2000,
            criticalNote = "Bremsen = Sicherheitsrelevant! Bei ungleichmäßigem Verschleiß Bremssattel prüfen.",
            technicalNote = "Bei viel Stadtverkehr/bergigem Terrain häufiger prüfen. Bremsflüssigkeit alle 2 Jahre wechseln.",
            icon = Icons.Filled.Warning
        )
    }

    private fun createBrakePadsRear(currentKm: Int): ServiceEntry {
        val intervalKm = 40000
        val intervalMonths = 36
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "brake_pads_rear",
            title = "Bremsbeläge hinten",
            spec = "258 mm Scheibendurchmesser",
            partNumber = "Opel 13501637",
            alternatives = "TRW D1429L, Akebono ACT1429, Brembo P50072",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 35.0,
            costWorkshop = 100.0,
            reminderEnabled = true,
            reminderThresholdKm = 2000,
            criticalNote = "",
            technicalNote = "Hintere Bremse wird weniger belastet, Verschleiß geringer. Trotzdem regelmäßig prüfen.",
            icon = Icons.Filled.Warning
        )
    }

    private fun createAirFilter(currentKm: Int): ServiceEntry {
        val intervalKm = 30000
        val intervalMonths = 24
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "air_filter",
            title = "Luftfilter",
            spec = "Saugmotorkasten A14NET",
            partNumber = "Opel 13536248",
            alternatives = "Mann C30132/1, Bosch F026400132, K&N 33-3003",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 25.0,
            costWorkshop = 50.0,
            reminderEnabled = true,
            reminderThresholdKm = 1000,
            criticalNote = "",
            technicalNote = "Einfacher Selberwechsel. Bei Staub/Schmutz häufiger prüfen. Performance-Filter (K&N) als Alternative.",
            icon = Icons.Filled.Speed
        )
    }

    private fun createSparkPlugs(currentKm: Int): ServiceEntry {
        val intervalKm = 60000
        val intervalMonths = 48
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "spark_plugs",
            title = "Zündkerzen",
            spec = "NGK LZKR6B-10E (Gap 0.7mm)",
            partNumber = "NGK LZKR6B-10E",
            alternatives = "Bosch FR7HPP332, Denso SC16HL11, Champion RC10PYPB4",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 50.0,
            costWorkshop = 130.0,
            reminderEnabled = true,
            reminderThresholdKm = 3000,
            criticalNote = "Bei Zündaussetzern sofort prüfen! Aussetzer können Kat beschädigen.",
            technicalNote = "Drehmoment: 20-25 Nm. Gap 0,7mm beachten! Bei Kurzstrecke: 30.000 km Intervall.",
            icon = Icons.Filled.LocalFireDepartment
        )
    }

    private fun createCoolant(currentKm: Int): ServiceEntry {
        val intervalKm = 150000
        val intervalMonths = 120
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "coolant",
            title = "Kühlmittel",
            spec = "Dex-Cool (orange, 5.7L System)",
            partNumber = "GM Dex-Cool 12378464",
            alternatives = "Opel 1940665, Pentosin 11-2025, ACDelco 10-9390",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 45.0,
            costWorkshop = 150.0,
            reminderEnabled = true,
            reminderThresholdKm = 5000,
            criticalNote = "Dex-Cool (orange) Pflicht! Kein anderes Kühlmittel mischen.",
            technicalNote = "Erstwechsel bei 150.000 km / 10 Jahre. Danach alle 40.000 km / 2 Jahre. Luft im System vermeiden!",
            icon = Icons.Filled.Speed
        )
    }

    private fun createTurboInspectionVisual(currentKm: Int): ServiceEntry {
        val intervalKm = 30000
        val intervalMonths = 24
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "turbo_visual",
            title = "Turbo-Inspektion (visuell)",
            spec = "BorgWarner KP39 - Sichtprüfung",
            partNumber = "N/A - Sichtprüfung",
            alternatives = "",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 0.0,
            costWorkshop = 80.0,
            reminderEnabled = true,
            reminderThresholdKm = 2000,
            criticalNote = "",
            technicalNote = "Ölleitungen auf Undichtigkeit prüfen, Wastegate-Function testen, Ladedruckschläuche auf Risse prüfen.",
            icon = Icons.Filled.Build
        )
    }

    private fun createTurboInspectionPressure(currentKm: Int): ServiceEntry {
        val intervalKm = 60000
        val intervalMonths = 48
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "turbo_pressure",
            title = "Turbo-Drucktest",
            spec = "BorgWarner KP39 - Ladedruck-Test",
            partNumber = "N/A - Drucktest",
            alternatives = "",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 0.0,
            costWorkshop = 120.0,
            reminderEnabled = true,
            reminderThresholdKm = 3000,
            criticalNote = "Ladedruckverlust = Leistungsverlust und erhöhter Verbrauch! Drucktest in Werkstatt empfohlen.",
            technicalNote = "OEM-Ladedruck: ~0.8 bar. Bei Underboost-Fehlern (P0299) sofort handeln. Wastegate-Stellglied prüfen.",
            icon = Icons.Filled.Speed
        )
    }

    private fun createCabinFilter(currentKm: Int): ServiceEntry {
        val intervalKm = 30000
        val intervalMonths = 24
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "cabin_filter",
            title = "Innenraumfilter",
            spec = "Aktivkohle oder Standard 215x189x30mm",
            partNumber = "Opel 13536247",
            alternatives = "Mann CU31006, Bosch F005CD472, K&N VF2002",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 20.0,
            costWorkshop = 45.0,
            reminderEnabled = true,
            reminderThresholdKm = 1000,
            criticalNote = "",
            technicalNote = "Aktivkohlefilter empfohlen für Allergiker. Einfacher Selberwechsel, ca. 10 Minuten.",
            icon = Icons.Filled.Build
        )
    }

    private fun createPCVValve(currentKm: Int): ServiceEntry {
        val intervalKm = 60000
        val intervalMonths = 48
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "pcv_valve",
            title = "PCV-Ventil",
            spec = "Kurbelgehäuse-Entlüftungsventil",
            partNumber = "Opel 55567298",
            alternatives = "Febi 18696, Vaico V40-0988, SKF VC 10010",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 25.0,
            costWorkshop = 60.0,
            reminderEnabled = true,
            reminderThresholdKm = 3000,
            criticalNote = "Defektes PCV kann Ölverbrauch und Leistungsverlust verursachen!",
            technicalNote = "Bei Öl im Ansaugtrakt oder erhöhtem Ölverbrauch sofort prüfen. Ventil auf Durchgängigkeit testen.",
            icon = Icons.Filled.Build
        )
    }

    private fun createMAFSensor(currentKm: Int): ServiceEntry {
        val intervalKm = 60000
        val intervalMonths = 48
        val lastKm = 0
        val dueKm = lastKm + intervalKm
        return ServiceEntry(
            id = "maf_sensor",
            title = "MAF-Sensor Reinigung",
            spec = "Mass airflow sensor - Hitzdraht",
            partNumber = "Opel 25175827",
            alternatives = "Bosch 0280218205, Denso 195500-0201",
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            lastServiceKm = lastKm,
            dueKm = dueKm,
            status = calcStatus(currentKm, lastKm, intervalKm, intervalMonths),
            progressPercent = calcProgress(currentKm, lastKm, intervalKm),
            costDiy = 15.0,
            costWorkshop = 80.0,
            reminderEnabled = true,
            reminderThresholdKm = 3000,
            criticalNote = "Verschmutzter MAF kann erhöhten Verbrauch und Leistungsverlust verursachen!",
            technicalNote = "Reinigung mit speziellem MAF-Reiniger. Kein Druckluft! Sensor vorsichtig einsprühen und trocknen lassen.",
            icon = Icons.Filled.Build
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun calcStatus(currentKm: Int, lastKm: Int, intervalKm: Int, intervalMonths: Int): ServiceStatus {
        val kmRemaining = (lastKm + intervalKm) - currentKm
        return when {
            kmRemaining < -500 -> ServiceStatus.OVERDUE
            kmRemaining < 1000 -> ServiceStatus.DUE_SOON
            kmRemaining < intervalKm / 4 -> ServiceStatus.UPCOMING
            else -> ServiceStatus.OK
        }
    }

    private fun calcProgress(currentKm: Int, lastKm: Int, intervalKm: Int): Float {
        if (intervalKm == 0) { return 0f }
        return ((currentKm - lastKm).toFloat() / intervalKm.toFloat()).coerceIn(0f, 1f)
    }
}

//endregion
