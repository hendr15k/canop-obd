package com.canopobd.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.local.TripEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    trips: List<TripEntity>,
    onBack: () -> Unit,
    onDeleteTrip: (Long) -> Unit,
    onClearAll: () -> Unit,
    onShareCsv: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var compareMode by remember { mutableStateOf(false) }
    var selectedTripIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showCompareDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (compareMode && selectedTripIds.isNotEmpty()) {
                        Text("${selectedTripIds.size} ausgewählt", fontWeight = FontWeight.Bold)
                    } else if (compareMode) {
                        Text("Vergleichsmodus", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Fahrthistorie", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (compareMode) {
                            compareMode = false
                            selectedTripIds = emptySet()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                },
                actions = {
                    if (trips.isNotEmpty()) {
                        if (compareMode) {
                            IconButton(
                                onClick = { showCompareDialog = true },
                                enabled = selectedTripIds.size >= 2
                            ) {
                                Icon(Icons.Filled.Compare, "Vergleichen")
                            }
                        } else {
                            IconButton(onClick = { compareMode = true }) {
                                Icon(Icons.AutoMirrored.Filled.CompareArrows, "Vergleichen")
                            }
                            IconButton(onClick = { showClearAllDialog = true }) {
                                Icon(Icons.Filled.DeleteSweep, "Alle löschen")
                            }
                            IconButton(onClick = onShareCsv) {
                                Icon(Icons.Filled.Share, "Exportieren")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Route,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Keine Fahrten gespeichert",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Fahrten werden nach jeder Verbindung gespeichert",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!compareMode) {
                    item {
                        TripSummaryCard(trips)
                    }
                }

                items(trips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        compareMode = compareMode,
                        isSelected = selectedTripIds.contains(trip.id),
                        onSelectChange = { selected ->
                            selectedTripIds = if (selected) {
                                selectedTripIds + trip.id
                            } else {
                                selectedTripIds - trip.id
                            }
                        },
                        onDelete = { showDeleteDialog = trip.id }
                    )
                }
            }
        }
    }
    
    // Delete single trip dialog
    showDeleteDialog?.let { tripId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Fahrt löschen?") },
            text = { Text("Diese Fahrt wird unwiderruflich gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTrip(tripId)
                    showDeleteDialog = null
                }) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
    
    // Clear all dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Alle Fahrten löschen?") },
            text = { Text("${trips.size} Fahrten werden unwiderruflich gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearAllDialog = false
                }) {
                    Text("Alle löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Comparison dialog
    if (showCompareDialog) {
        val selectedTrips = trips.filter { selectedTripIds.contains(it.id) }
        if (selectedTrips.size >= 2) {
            TripComparisonDialog(
                trips = selectedTrips,
                onDismiss = { showCompareDialog = false }
            )
        }
    }
}

@Composable
private fun TripSummaryCard(trips: List<TripEntity>) {
    val totalDistance = trips.sumOf { it.distanceKm.toDouble() }
    val totalFuel = trips.sumOf { it.fuelUsedLiters.toDouble() }
    val avgSpeed = trips.map { it.avgSpeedKmh }.average()
    val maxSpeed = trips.maxOfOrNull { it.maxSpeedKmh } ?: 0f
    val totalDuration = trips.sumOf { it.endTime - it.startTime }
    val fuelPer100km = if (totalDistance > 0) (totalFuel / totalDistance * 100) else 0.0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Zusammenfassung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Filled.Route,
                    value = "%.1f km".format(totalDistance),
                    label = "Gesamtstrecke"
                )
                SummaryItem(
                    icon = Icons.Filled.LocalGasStation,
                    value = "%.1f L".format(totalFuel),
                    label = "Gesamtverbrauch"
                )
                SummaryItem(
                    icon = Icons.Filled.InvertColors,
                    value = "%.1f L".format(fuelPer100km),
                    label = "Ø L/100km"
                )
                SummaryItem(
                    icon = Icons.Filled.Speed,
                    value = "%.1f km/h".format(avgSpeed),
                    label = "Ø Geschw."
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Filled.FlashOn,
                    value = "%.0f km/h".format(maxSpeed),
                    label = "Max Geschw."
                )
                SummaryItem(
                    icon = Icons.Filled.Timer,
                    value = formatDuration(totalDuration),
                    label = "Gesamtzeit"
                )
                SummaryItem(
                    icon = Icons.Filled.DirectionsCar,
                    value = "${trips.size}",
                    label = "Fahrten"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TripCard(
    trip: TripEntity,
    compareMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectChange: (Boolean) -> Unit = {},
    onDelete: () -> Unit
) {
    val dateFormat = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN).withZone(ZoneId.systemDefault()) }
    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN).withZone(ZoneId.systemDefault()) }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (compareMode) it.clickable { onSelectChange(!isSelected) } else it },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (compareMode) {
                        androidx.compose.material3.Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSelectChange(it) }
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            dateFormat.format(Instant.ofEpochMilli(trip.startTime)),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${timeFormat.format(Instant.ofEpochMilli(trip.startTime))} - ${timeFormat.format(Instant.ofEpochMilli(trip.endTime))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                if (!compareMode) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            "Löschen",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TripDataChip(
                    icon = Icons.Filled.Route,
                    value = "%.1f km".format(trip.distanceKm)
                )
                TripDataChip(
                    icon = Icons.Filled.Speed,
                    value = "%.0f km/h".format(trip.avgSpeedKmh)
                )
                TripDataChip(
                    icon = Icons.Filled.LocalGasStation,
                    value = "%.1f L".format(trip.fuelUsedLiters)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TripDataChip(
                    icon = Icons.Filled.FlashOn,
                    value = "%.0f km/h".format(trip.maxSpeedKmh),
                    label = "Max"
                )
                TripDataChip(
                    icon = Icons.Filled.AvTimer,
                    value = "%.0f rpm".format(trip.avgRpm),
                    label = "Ø RPM"
                )
                TripDataChip(
                    icon = Icons.Filled.Build,
                    value = "%.0f rpm".format(trip.maxRpm),
                    label = "Max RPM"
                )
            }
            
            if (trip.vin.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "VIN: ${trip.vin}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun TripDataChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            if (label.isNotEmpty()) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

@Composable
private fun TripComparisonDialog(
    trips: List<TripEntity>,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN).withZone(ZoneId.systemDefault()) }

    data class TripMetric(
        val label: String,
        val values: List<String>,
        val winner: Int
    )

    val metrics = remember(trips) {
        val durationsMs = trips.map { it.endTime - it.startTime }
        val fuelsPer100 = trips.map {
            if (it.distanceKm > 0) (it.fuelUsedLiters.toDouble() / it.distanceKm * 100) else 0.0
        }

        fun bestIdx(values: List<Double>, preferLower: Boolean): Int {
            val filtered = values.filter { it > 0 }
            if (filtered.isEmpty()) return 0
            val best = if (preferLower) filtered.min() else filtered.max()
            return values.indexOfFirst { it == best }.takeIf { it >= 0 } ?: 0
        }

        val distanceValues = trips.map { it.distanceKm.toDouble() }
        val fuelValues = trips.map { it.fuelUsedLiters.toDouble() }
        val maxSpeedValues = trips.map { it.maxSpeedKmh.toDouble() }
        val maxRpmValues = trips.map { it.maxRpm.toDouble() }

        listOf(
            TripMetric("Strecke (km)", trips.map { "%.1f".format(it.distanceKm) }, bestIdx(distanceValues, preferLower = false)),
            TripMetric("Dauer", durationsMs.map { formatDuration(it) }, 0),
            TripMetric("Ø L/100km", fuelsPer100.map { "%.2f".format(it) }, bestIdx(fuelsPer100, preferLower = true)),
            TripMetric("Verbrauch (L)", trips.map { "%.1f".format(it.fuelUsedLiters) }, bestIdx(fuelValues, preferLower = true)),
            TripMetric("Ø Geschw. (km/h)", trips.map { "%.1f".format(it.avgSpeedKmh) }, 0),
            TripMetric("Max Geschw. (km/h)", trips.map { "%.0f".format(it.maxSpeedKmh) }, bestIdx(maxSpeedValues, preferLower = false)),
            TripMetric("Ø RPM", trips.map { "%.0f".format(it.avgRpm) }, 0),
            TripMetric("Max RPM", trips.map { "%.0f".format(it.maxRpm) }, bestIdx(maxRpmValues, preferLower = false))
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fahrt-Vergleich", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Spalten-Header mit Datum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Metrik", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    trips.forEachIndexed { i, trip ->
                        Text(
                            "Trip ${i + 1}\n${dateFormat.format(Instant.ofEpochMilli(trip.startTime))}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                metrics.forEach { metric ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            metric.label,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1.2f)
                        )
                        metric.values.forEachIndexed { i, value ->
                            val isWinner = i == metric.winner && metric.values.size > 1
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    value,
                                    fontSize = 13.sp,
                                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                if (isWinner) {
                                    Text(
                                        "✓",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}
