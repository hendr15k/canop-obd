package com.canopobd.ui.maintenance

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.canopobd.data.model.MaintenanceItem
import com.canopobd.data.model.MaintenanceStatus
import com.canopobd.data.model.MaintenanceType
import com.canopobd.ui.theme.*

@Composable
fun MaintenanceDialog(
    maintenanceItems: List<MaintenanceItem>,
    currentKm: Int,
    onDismiss: () -> Unit,
    onUpdateItem: (MaintenanceType, Int, Int) -> Unit,
    onResetItem: (MaintenanceType) -> Unit
) {
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
                        text = stringResource(R.string.maintenance_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            text = "Kilometerstand: $currentKm ${stringResource(R.string.maintenance_km)}",
                            fontSize = 14.sp,
                            color = textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(MaintenanceType.entries) { type ->
                        val item = maintenanceItems.find { it.type == type }
                            ?: MaintenanceItem(type = type, lastServiceKm = currentKm, currentKm = currentKm)
                        MaintenanceItemRow(
                            item = item,
                            currentKm = currentKm,
                            onUpdate = { km, interval -> onUpdateItem(type, km, interval) },
                            onReset = { onResetItem(type) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceItemRow(
    item: MaintenanceItem,
    currentKm: Int,
    onUpdate: (Int, Int) -> Unit,
    onReset: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val kmRemaining = item.lastServiceKm + item.intervalKm - currentKm

    val statusColor = when (item.status) {
        MaintenanceStatus.OVERDUE -> gaugeRed
        MaintenanceStatus.DUE_SOON -> gaugeYellow
        MaintenanceStatus.OK -> gaugeGreen
    }

    val statusText = when (item.status) {
        MaintenanceStatus.OVERDUE -> stringResource(R.string.maintenance_overdue)
        MaintenanceStatus.DUE_SOON -> stringResource(R.string.maintenance_due_soon)
        MaintenanceStatus.OK -> stringResource(R.string.maintenance_ok)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = canopoDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (item.type) {
                            MaintenanceType.OIL_CHANGE -> Icons.Filled.OilBarrel
                            MaintenanceType.TIRES -> Icons.Filled.TireRepair
                            MaintenanceType.INSPECTION -> Icons.Filled.Assignment
                            MaintenanceType.BRAKE_PADS -> Icons.Filled.Warning
                            MaintenanceType.AIR_FILTER -> Icons.Filled.Air
                            MaintenanceType.TRANSMISSION_FLUID -> Icons.Filled.Settings
                            MaintenanceType.TURBO_INSPECTION -> Icons.Filled.Settings
                            MaintenanceType.COOLANT -> Icons.Filled.Settings
                            MaintenanceType.SPARK_PLUGS -> Icons.Filled.Settings
                            MaintenanceType.TURBO_BOOST_CHECK -> Icons.Filled.Settings
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.type.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = stringResource(R.string.maintenance_last_service) + ": ${item.lastServiceKm} km",
                            fontSize = 11.sp,
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
                        text = if (kmRemaining >= 0) {
                            stringResource(R.string.maintenance_km_remaining, "%,d".format(kmRemaining))
                        } else {
                            stringResource(R.string.maintenance_km_remaining, "%,d".format(-kmRemaining))
                        },
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = ((kmRemaining.toFloat() / item.intervalKm) * 100f).coerceIn(0f, 100f) / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = statusColor,
                trackColor = canopoSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showDialog = true }) {
                    Text(stringResource(R.string.maintenance_set_interval), fontSize = 12.sp, color = canopoAccent)
                }
                TextButton(onClick = onReset) {
                    Text(stringResource(R.string.maintenance_reset), fontSize = 12.sp, color = gaugeYellow)
                }
            }
        }
    }

    if (showDialog) {
        IntervalEditDialog(
            currentKm = item.lastServiceKm,
            currentInterval = item.intervalKm,
            onDismiss = { showDialog = false },
            onConfirm = { km, interval ->
                onUpdate(km, interval)
                showDialog = false
            }
        )
    }
}

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
