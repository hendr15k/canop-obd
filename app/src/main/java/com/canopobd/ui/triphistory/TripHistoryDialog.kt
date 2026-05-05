package com.canopobd.ui.triphistory

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
import com.canopobd.data.model.GPSTrip
import com.canopobd.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripHistoryDialog(
    trips: List<GPSTrip>,
    onDismiss: () -> Unit,
    onClearHistory: () -> Unit
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
                        text = stringResource(R.string.trip_history_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    Row {
                        if (trips.isNotEmpty()) {
                            IconButton(onClick = onClearHistory) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.trip_history_clear), tint = gaugeRed, modifier = Modifier.size(20.dp))
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (trips.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Route, contentDescription = null, tint = textDim, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.trip_history_no_trips),
                                fontSize = 16.sp,
                                color = textDim
                            )
                        }
                    }
                } else {
                    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN) }

                    LazyColumn {
                        items(trips.sortedByDescending { it.startTime }) { trip ->
                            TripCard(trip = trip, dateFormat = dateFormat)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripCard(trip: GPSTrip, dateFormat: SimpleDateFormat) {
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
                    Icon(Icons.Filled.Route, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = dateFormat.format(Date(trip.startTime)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = if (trip.endTime > 0) {
                                "Dauer: ${formatDuration(trip.endTime - trip.startTime)}"
                            } else {
                                "Läuft…"
                            },
                            fontSize = 11.sp,
                            color = textDim
                        )
                    }
                }
                Text(
                    text = "%.1f km".format(trip.distanceKm),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip(icon = Icons.Filled.Speed, label = stringResource(R.string.trip_max), value = "%.0f km/h".format(trip.maxSpeedKmh))
                StatChip(icon = Icons.Filled.Speed, label = "Ø", value = "%.0f km/h".format(trip.avgSpeedKmh))
                StatChip(icon = Icons.Filled.LocationOn, label = stringResource(R.string.trip_points), value = "${trip.locations.size}")
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = textDim, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$label: $value", fontSize = 11.sp, color = textSecondary)
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
