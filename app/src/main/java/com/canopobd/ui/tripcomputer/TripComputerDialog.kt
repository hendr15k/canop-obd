package com.canopobd.ui.tripcomputer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canopobd.R
import com.canopobd.data.model.GPSTrip
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.data.model.TripData
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*
import java.util.concurrent.TimeUnit

@Suppress("UNUSED_PARAMETER")
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
    val colors = LocalAppColors.current

    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.trip_title),
        eyebrow = "Fahrtdaten & Statistik",
        heightFraction = 0.9f
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (vin.isNotBlank()) {
                item { VinCard(vin = vin, onCopy = { copyVin(context, vin) }) }
            }

            item { SectionHeader(title = stringResource(R.string.trip_time_distance), icon = Icons.Filled.Schedule) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyValueBlock(
                        label = stringResource(R.string.trip_duration),
                        value = formatDuration(tripData.durationSeconds),
                        modifier = Modifier.weight(1f),
                        accentColor = colors.primary
                    )
                    KeyValueBlock(
                        label = stringResource(R.string.trip_distance),
                        value = formatDistance(tripData.distanceKm, measurementUnit),
                        modifier = Modifier.weight(1f),
                        accentColor = colors.secondary
                    )
                }
            }

            item {
                Spacer(Modifier.height(2.dp))
                SectionHeader(title = stringResource(R.string.trip_speed), icon = Icons.Filled.Speed)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyValueBlock(
                        label = stringResource(R.string.trip_avg_speed),
                        value = formatSpeed(tripData.avgSpeedKmh, measurementUnit),
                        modifier = Modifier.weight(1f)
                    )
                    KeyValueBlock(
                        label = stringResource(R.string.trip_max_speed),
                        value = formatSpeed(tripData.maxSpeedKmh, measurementUnit),
                        modifier = Modifier.weight(1f),
                        accentColor = colors.warning
                    )
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyValueBlock(
                        label = stringResource(R.string.trip_avg_rpm),
                        value = "${tripData.avgRpm.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                    KeyValueBlock(
                        label = stringResource(R.string.trip_max_rpm),
                        value = "${tripData.maxRpm.toInt()}",
                        modifier = Modifier.weight(1f),
                        accentColor = colors.critical
                    )
                }
            }

            item {
                Spacer(Modifier.height(2.dp))
                SectionHeader(title = stringResource(R.string.trip_consumption_label), icon = Icons.Filled.LocalGasStation)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyValueBlock(
                        label = stringResource(R.string.trip_fuel_used),
                        value = "%.1f L".format(tripData.totalFuelUsed),
                        modifier = Modifier.weight(1f)
                    )
                    KeyValueBlock(
                        label = stringResource(R.string.trip_consumption),
                        value = "%.1f L/100".format(tripData.avgFuelRate),
                        modifier = Modifier.weight(1f),
                        accentColor = colors.success
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(title = stringResource(R.string.trip_gps_track), icon = Icons.Filled.LocationOn)
            }
            item {
                GlassCard(
                    accentEdge = if (isGPSTracking) { colors.success } else { colors.textTertiary },
                    padding = 12.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(AppRadius.sm))
                                .background(
                                    if (isGPSTracking) {
                                        colors.success.copy(alpha = 0.18f)
                                    } else {
                                        colors.surfaceElevated
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isGPSTracking) { Icons.Filled.LocationOn } else { Icons.Filled.LocationSearching },
                                contentDescription = null,
                                tint = if (isGPSTracking) { colors.success } else { colors.textTertiary },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isGPSTracking) { "GPS aktiv" } else { "GPS inaktiv" },
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isGPSTracking) { colors.success } else { colors.textPrimary }
                            )
                            if (currentTrip != null) {
                                Text(
                                    text = "${"%.1f".format(currentTrip.distanceKm)} km · ${currentTrip.locations.size} Punkte",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary
                                )
                            }
                        }
                        GradientButton(
                            text = if (isGPSTracking) { stringResource(R.string.trip_stop) } else { "Start" },
                            onClick = { if (isGPSTracking) { onStopGPSTrack() } else { onStartGPSTrack() } },
                            gradient = if (isGPSTracking) { colors.gradientSuccess } else { colors.gradientAccent }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlineButton(
                        text = stringResource(R.string.trip_reset),
                        onClick = onResetTrip,
                        icon = Icons.Filled.Refresh,
                        accentColor = colors.warning,
                        modifier = Modifier.weight(1f)
                    )
                    OutlineButton(
                        text = "GPX",
                        onClick = { exportFile(context, onExportGPX(), "trip.gpx", "application/gpx+xml") },
                        icon = Icons.Filled.FileDownload,
                        modifier = Modifier.weight(1f)
                    )
                    OutlineButton(
                        text = "KML",
                        onClick = { exportFile(context, onExportKML(), "trip.kml", "application/vnd.google-earth.kml+xml") },
                        icon = Icons.Filled.FileDownload,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun VinCard(vin: String, onCopy: () -> Unit) {
    val colors = LocalAppColors.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentEdge = colors.primary,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.trip_vin),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = vin,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPure,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButtonBox(icon = Icons.Filled.ContentCopy, onClick = onCopy, accentColor = colors.primary)
        }
    }
}

private fun copyVin(context: Context, vin: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("VIN", vin)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "VIN kopiert", Toast.LENGTH_SHORT).show()
}

private fun exportFile(context: Context, content: String, filename: String, mime: String) {
    try {
        val cacheFile = java.io.File(context.cacheDir, filename)
        cacheFile.writeText(content)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            cacheFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(intent, "Export"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export fehlgeschlagen: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun formatDuration(seconds: Long): String {
    val h = TimeUnit.SECONDS.toHours(seconds)
    val m = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val s = seconds % 60
    return if (h > 0) { "%dh %02dm".format(h, m) } else { "%dm %02ds".format(m, s) }
}

private fun formatDistance(km: Double, unit: MeasurementUnit): String =
    "%.1f %s".format(unit.convertDistance(km), unit.distanceUnit())

private fun formatSpeed(kmh: Double, unit: MeasurementUnit): String =
    "%.0f %s".format(unit.convertSpeed(kmh), unit.speedUnit)
