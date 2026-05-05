package com.canopobd.ui.tripcomputer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.data.model.TripData
import com.canopobd.data.model.GPSTrip
import com.canopobd.ui.theme.*
import java.util.concurrent.TimeUnit

@Composable
fun TripComputerDialog(
    tripData: TripData,
    measurementUnit: MeasurementUnit,
    vin: String,
    isGPSTracking: Boolean,
    currentTrip: GPSTrip?,
    onDismiss: () -> Unit,
    onResetTrip: () -> Unit,
    onStartGPSTrack: () -> Unit,
    onStopGPSTrack: () -> Unit,
    onExportGPX: () -> String,
    onExportKML: () -> String,
    onClearGPS: () -> Unit
) {
    val context = LocalContext.current

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
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.trip_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (vin.isNotBlank()) {
                    VinCard(vin = vin, onCopy = { copyVin(context, vin) })
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn {
                    item {
                        SectionTitle(stringResource(R.string.trip_time_distance))
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                label = stringResource(R.string.trip_duration),
                                value = formatDuration(tripData.durationSeconds),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = stringResource(R.string.trip_distance),
                                value = formatDistance(tripData.distanceKm, measurementUnit),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { SectionTitle(stringResource(R.string.trip_speed)) }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                label = stringResource(R.string.trip_avg_speed),
                                value = formatSpeed(tripData.avgSpeedKmh, measurementUnit),
                                color = gaugeGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = stringResource(R.string.trip_max_speed),
                                value = formatSpeed(tripData.maxSpeedKmh, measurementUnit),
                                color = gaugeOrange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { SectionTitle("Motor") }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                label = stringResource(R.string.trip_avg_rpm),
                                value = "%.0f rpm".format(tripData.avgRpm),
                                color = gaugeYellow,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = stringResource(R.string.trip_max_rpm),
                                value = "%.0f rpm".format(tripData.maxRpm),
                                color = gaugeRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { SectionTitle(stringResource(R.string.trip_fuel_used)) }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                label = stringResource(R.string.trip_consumption_label),
                                value = if (tripData.distanceKm > 0.5) {
                                    "%.1f L/100km".format(tripData.totalFuelUsed / (tripData.distanceKm / 100.0))
                                } else {
                                    "-- L/100km"
                                },
                                color = gaugeGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = stringResource(R.string.trip_total),
                                value = "%.1f L".format(tripData.totalFuelUsed),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    item {
                        Button(
                            onClick = onResetTrip,
                            colors = ButtonDefaults.buttonColors(containerColor = gaugeRed.copy(alpha = 0.8f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.trip_reset))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { SectionTitle(stringResource(R.string.trip_gps_track)) }

                    item {
                        if (isGPSTracking && currentTrip != null) {
                            GPSStatusCard(trip = currentTrip, onStop = onStopGPSTrack)
                        } else {
                            Button(
                                onClick = onStartGPSTrack,
                                colors = ButtonDefaults.buttonColors(containerColor = gaugeGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GPS Track starten")
                            }
                        }
                    }

                    if (currentTrip != null && currentTrip.locations.isNotEmpty()) {
                        item {
                            val ctx = LocalContext.current
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val gpx = onExportGPX()
                                        if (gpx.isNotBlank()) {
                                            val file = File(ctx.cacheDir, "trip_${currentTrip.id}.gpx")
                                            file.writeText(gpx)
                                            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
                                            ctx.startActivity(Intent(Intent.ACTION_SEND).apply {
                                                type = "application/gpx+xml"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }.let { Intent.createChooser(it, "Export GPX") })
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("GPX", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val kml = onExportKML()
                                        if (kml.isNotBlank()) {
                                            val file = File(ctx.cacheDir, "trip_${currentTrip.id}.kml")
                                            file.writeText(kml)
                                            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
                                            ctx.startActivity(Intent(Intent.ACTION_SEND).apply {
                                                type = "application/vnd.google-earth.kml+xml"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }.let { Intent.createChooser(it, "Export KML") })
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("KML", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = onClearGPS,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        color = canopoAccent,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = textPrimary
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .background(canopoDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = textSecondary
        )
    }
}

@Composable
private fun VinCard(vin: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(canopoDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.trip_vin),
                fontSize = 10.sp,
                color = textSecondary
            )
            Text(
                text = vin.take(17),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
        }
        IconButton(onClick = onCopy) {
            Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.trip_copy_vin), tint = canopoAccent, modifier = Modifier.size(20.dp))
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = TimeUnit.SECONDS.toHours(seconds)
    val m = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatSpeed(kmh: Double, unit: MeasurementUnit = MeasurementUnit.METRIC): String {
    val value = unit.convertSpeed(kmh)
    return "%.0f %s".format(value, unit.speedUnit)
}

private fun formatDistance(km: Double, unit: MeasurementUnit = MeasurementUnit.METRIC): String {
    val value = unit.convertDistance(km)
    return "%.1f %s".format(value, unit.distanceUnit())
}

private fun copyVin(context: Context, vin: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("VIN", vin)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, context.getString(R.string.trip_copy_vin), Toast.LENGTH_SHORT).show()
}

@Composable
private fun GPSStatusCard(trip: GPSTrip, onStop: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = gaugeGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = gaugeGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.trip_gps_tracking), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = gaugeGreen)
                }
                IconButton(onClick = onStop) {
                    Icon(Icons.Filled.Stop, contentDescription = stringResource(R.string.trip_stop), tint = gaugeRed, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%.1f km".format(trip.distanceKm), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Distance", fontSize = 10.sp, color = textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%.0f km/h".format(trip.maxSpeedKmh), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = gaugeOrange)
                    Text("Max Speed", fontSize = 10.sp, color = textSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${trip.locations.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = canopoAccent)
                    Text("Points", fontSize = 10.sp, color = textSecondary)
                }
            }
        }
    }
}
