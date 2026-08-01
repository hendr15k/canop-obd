package com.canopobd.ui.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.local.TripEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class AggregateBucket(
    val label: String,
    val tripCount: Int,
    val totalKm: Double,
    val totalDurationMs: Long,
    val totalFuel: Double,
    val maxSpeedKmh: Float,
    val avgSpeedKmh: Float
) {
    val avgL100: Double get() = if (totalKm > 0) totalFuel / totalKm * 100.0 else 0.0
}

private fun buildBuckets(trips: List<TripEntity>, now: LocalDate): List<AggregateBucket> {
    val today = now
    val start7 = today.minusDays(6)
    val start30 = today.minusDays(29)
    val startYear = today.withDayOfYear(1)

    val tripDateOf = { t: TripEntity ->
        Instant.ofEpochMilli(t.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val isIn = { date: LocalDate, from: LocalDate, to: LocalDate -> !date.isBefore(from) && !date.isAfter(to) }

    fun bucketOf(from: LocalDate, to: LocalDate, label: String): AggregateBucket {
        val inRange = trips.filter { isIn(tripDateOf(it), from, to) }
        return AggregateBucket(
            label = label,
            tripCount = inRange.size,
            totalKm = inRange.sumOf { it.distanceKm.toDouble() },
            totalDurationMs = inRange.sumOf { (it.endTime - it.startTime).coerceAtLeast(0L) },
            totalFuel = inRange.sumOf { it.fuelUsedLiters.toDouble() },
            maxSpeedKmh = inRange.maxOfOrNull { it.maxSpeedKmh } ?: 0f,
            avgSpeedKmh = if (inRange.isEmpty()) 0f else inRange.map { it.avgSpeedKmh }.average().toFloat()
        )
    }

    return listOf(
        bucketOf(start7, today, "7 Tage"),
        bucketOf(start30, today, "30 Tage"),
        bucketOf(startYear, today, "Jahr")
    )
}

private data class DailyPoint(
    val date: LocalDate,
    val label: String,
    val distanceKm: Double,
    val tripCount: Int
)

private fun buildDailyBreakdown(trips: List<TripEntity>, today: LocalDate): List<DailyPoint> {
    val days = (0..6).map { today.minusDays((6 - it).toLong()) }
    val tripDateOf = { t: TripEntity ->
        Instant.ofEpochMilli(t.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val fmt = DateTimeFormatter.ofPattern("EEE", Locale.GERMAN)
    return days.map { d ->
        val same = trips.filter { tripDateOf(it) == d }
        DailyPoint(
            date = d,
            label = fmt.format(d),
            distanceKm = same.sumOf { it.distanceKm.toDouble() },
            tripCount = same.size
        )
    }
}

@Composable
fun TripStatisticsDialog(
    trips: List<TripEntity>,
    onDismiss: () -> Unit,
    onShareReport: (String) -> Unit
) {
    val today = remember { LocalDate.now() }
    val buckets = remember(trips) { buildBuckets(trips, today) }
    val daily = remember(trips) { buildDailyBreakdown(trips, today) }
    val totalTrips = trips.size
    val totalKm = trips.sumOf { it.distanceKm.toDouble() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.QueryStats,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "STATISTIK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            stringResource(R.string.trip_statistics_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
                    }
                }

                HorizontalDivider()

                if (trips.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.QueryStats,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.trip_stats_no_data),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            OverviewCard(
                                totalTrips = totalTrips,
                                totalKm = totalKm
                            )
                        }

                        item {
                            DailyBreakdownCard(daily = daily)
                        }

                        item {
                            PeriodBucketsCard(buckets = buckets)
                        }

                        item {
                            val reportText = buildReportText(trips, buckets, today)
                            Button(
                                onClick = { onShareReport(reportText) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.trip_stats_share))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(totalTrips: Int, totalKm: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.trip_stats_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BigStat(
                    icon = Icons.Filled.DirectionsCar,
                    value = "$totalTrips",
                    label = stringResource(R.string.trip_stats_total_trips)
                )
                BigStat(
                    icon = Icons.Filled.Route,
                    value = "%.1f km".format(totalKm),
                    label = stringResource(R.string.trip_stats_total_distance)
                )
            }
        }
    }
}

@Composable
private fun BigStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PeriodBucketsCard(buckets: List<AggregateBucket>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Zeiträume",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            buckets.forEach { b ->
                PeriodRow(b)
                if (b !== buckets.last()) Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun PeriodRow(b: AggregateBucket) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                b.label,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${b.tripCount} ${if (b.tripCount == 1) "Fahrt" else "Fahrten"}",
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MiniStat("Strecke", "%.1f km".format(b.totalKm))
            MiniStat("Dauer", formatCompactDuration(b.totalDurationMs))
            MiniStat("Ø Vmax", "%.0f km/h".format(b.maxSpeedKmh))
            MiniStat("Sprit", "%.1f L".format(b.totalFuel))
        }
        if (b.totalKm > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Ø ${"%.2f".format(b.avgL100)} L/100 km",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun DailyBreakdownCard(daily: List<DailyPoint>) {
    val max = daily.maxOf { it.distanceKm }
    val chartColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.trip_stats_daily_breakdown),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val gridLines = 4
                val w = size.width
                val h = size.height
                for (g in 1..gridLines) {
                    val y = h * g / gridLines
                    drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }
                if (max > 0 && daily.any { it.distanceKm > 0 }) {
                    val n = daily.size
                    val spacing = w / n
                    val barWidth = spacing * 0.55f
                    daily.forEachIndexed { idx, p ->
                        val v = if (max > 0) p.distanceKm.toFloat() else 0f
                        val barHeight = (v / max.toFloat()) * h * 0.9f
                        val x = idx * spacing + (spacing - barWidth) / 2
                        drawRoundRect(
                            color = chartColor,
                            topLeft = Offset(x, h - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                    }
                } else {
                    drawLine(gridColor, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 2f)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daily.forEach { p ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            p.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            if (p.distanceKm > 0) "%.1f".format(p.distanceKm) else "–",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

private fun formatCompactDuration(ms: Long): String {
    val totalMinutes = ms / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "—"
    }
}

private fun buildReportText(
    trips: List<TripEntity>,
    buckets: List<AggregateBucket>,
    today: LocalDate
): String {
    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val sb = StringBuilder()
    sb.appendLine("canop-obd — Fahrtenstatistik")
    sb.appendLine("Stand: ${dateFmt.format(today)}")
    sb.appendLine("---")
    sb.appendLine("Fahrten gesamt: ${trips.size}")
    sb.appendLine("Strecke gesamt: %.1f km".format(trips.sumOf { it.distanceKm.toDouble() }))
    sb.appendLine()
    buckets.forEach { b ->
        sb.appendLine("[${b.label}] ${b.tripCount} Fahrten, %.1f km, %s, %.1f L".format(
            b.totalKm,
            formatCompactDuration(b.totalDurationMs),
            b.totalFuel
        ))
    }
    return sb.toString()
}
