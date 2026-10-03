package com.canopobd.ui.datalog

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.canopobd.R
import com.canopobd.data.model.DataRecord
import com.canopobd.data.repository.DataExporter
import com.canopobd.data.repository.ExportFormat
import com.canopobd.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// Dialog- und Picker-Geometrie sowie die Trend-Fenstergroesse als benannte
// Konstanten, damit Detekt das Layout nicht als Magic Number beanstandet.
private const val DIALOG_WIDTH_FRACTION = 0.95f
private const val DIALOG_HEIGHT_FRACTION = 0.85f
private const val FORMAT_PICKER_WIDTH_FRACTION = 0.9f
private const val MAX_TREND_POINTS = 100

@Suppress("UNUSED_PARAMETER")
@Composable
fun DataLogDialog(
    recordedData: List<DataRecord>,
    isRecording: Boolean,
    onDismiss: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearData: () -> Unit,
    // Kept for source/binary compatibility with existing callers. The CSV
    // callback must never be invoked again: generation happens after the user
    // confirms a format in the picker.
    onExportData: () -> String
) {
    DataLogDialogContent(
        recordedData = recordedData,
        isRecording = isRecording,
        onDismiss = onDismiss,
        onStartRecording = onStartRecording,
        onStopRecording = onStopRecording,
        onClearData = onClearData
    )
}

@Composable
private fun DataLogDialogContent(
    recordedData: List<DataRecord>,
    isRecording: Boolean,
    onDismiss: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearData: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var showFormatPicker by remember { mutableStateOf(false) }

    // Use the same GPS validation as the exporter; never combine different samples.
    val hasValidGps = recordedData.any { DataExporter.hasValidGps(it) }

    // Keep compatibility with existing callers without invoking the legacy CSV callback.
    LaunchedEffect(hasValidGps) {
        if (!hasValidGps && selectedFormat == ExportFormat.GPX_WITH_OBD) {
            selectedFormat = ExportFormat.CSV
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DataLogPanel(
                state = DataLogUi(
                    recordedData = recordedData,
                    isRecording = isRecording,
                    selectedTab = selectedTab,
                    onToggleRecording = if (isRecording) onStopRecording else onStartRecording,
                    onClearData = onClearData,
                    onSelectTab = { selectedTab = it }
                ),
                onDismiss = onDismiss,
                onExportClick = {
                    selectedFormat = ExportFormat.CSV
                    // Legacy-Callback wird bewusst NICHT aufgerufen: die
                    // Erzeugung passiert erst nach Bestaetigung.
                    showFormatPicker = true
                }
            )

            if (showFormatPicker) {
                ExportFormatPickerHost(
                    recordedData = recordedData,
                    selectedFormat = selectedFormat,
                    gpxEnabled = hasValidGps,
                    onFormatChange = { selectedFormat = it },
                    onDismiss = { showFormatPicker = false }
                )
            }
        }
    }
}

@Composable
private fun DataLogPanel(
    state: DataLogUi,
    onDismiss: () -> Unit,
    onExportClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(DIALOG_WIDTH_FRACTION)
            .fillMaxHeight(DIALOG_HEIGHT_FRACTION),
        shape = RoundedCornerShape(16.dp),
        color = canopoSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DataLogHeader(onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(8.dp))

            DataLogActionRow(
                isRecording = state.isRecording,
                exportEnabled = state.recordedData.isNotEmpty(),
                onToggleRecording = state.onToggleRecording,
                onExportClick = onExportClick,
                onClearData = state.onClearData
            )

            DataLogEntryCount(isRecording = state.isRecording, count = state.recordedData.size)

            Spacer(modifier = Modifier.height(8.dp))

            DataLogTabs(selectedTab = state.selectedTab, onSelectTab = state.onSelectTab)

            Spacer(modifier = Modifier.height(8.dp))

            DataLogBody(selectedTab = state.selectedTab, recordedData = state.recordedData)
        }
    }
}

// Alle Anzeigen- und Aktions-Callbacks als eine einzige Struktur, damit das
// Layout-Composable unter der Detekt-Parametergrenze von 8 bleibt.
private data class DataLogUi(
    val recordedData: List<DataRecord>,
    val isRecording: Boolean,
    val selectedTab: Int,
    val onToggleRecording: () -> Unit,
    val onClearData: () -> Unit,
    val onSelectTab: (Int) -> Unit
)

@Composable
private fun DataLogBody(selectedTab: Int, recordedData: List<DataRecord>) {
    when (selectedTab) {
        0 -> DataList(recordedData)
        else -> TrendGraph(recordedData)
    }
}

@Composable
private fun DataLogHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.datalog_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = canopoHighlight
        )
        IconButton(onClick = onDismiss) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.close),
                tint = textSecondary
            )
        }
    }
}

@Composable
private fun DataLogActionRow(
    isRecording: Boolean,
    exportEnabled: Boolean,
    onToggleRecording: () -> Unit,
    onExportClick: () -> Unit,
    onClearData: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onToggleRecording,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) gaugeRed else gaugeGreen
            ),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isRecording) "Stop" else "Start")
        }

        OutlinedButton(
            onClick = onExportClick,
            enabled = exportEnabled,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.datalog_export))
        }

        IconButton(onClick = onClearData) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.datalog_delete),
                tint = gaugeRed
            )
        }
    }
}

@Composable
private fun DataLogEntryCount(isRecording: Boolean, count: Int) {
    val countRes = if (isRecording) R.string.datalog_recording else R.string.datalog_entries
    Text(
        text = stringResource(countRes, count),
        fontSize = 12.sp,
        color = if (isRecording) gaugeGreen else textSecondary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DataLogTabs(selectedTab: Int, onSelectTab: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = canopoDark,
        contentColor = canopoAccent
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onSelectTab(0) },
            text = { Text("Verlauf", color = textPrimary) }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onSelectTab(1) },
            text = { Text("Trend", color = textPrimary) }
        )
    }
}

@Composable
private fun DataList(recordedData: List<DataRecord>) {
    if (recordedData.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.datalog_no_data), color = textSecondary, fontSize = 14.sp)
        }
    } else {
        LazyColumn {
            items(recordedData.takeLast(MAX_TREND_POINTS).reversed()) { record ->
                DataRecordItem(record)
            }
        }
    }
}

@Composable
private fun DataRecordItem(record: DataRecord) {
    val dateFormat =
        remember { DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()).withZone(ZoneId.systemDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(canopoDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dateFormat.format(Instant.ofEpochMilli(record.timestamp)),
            fontSize = 11.sp,
            color = textDim
        )
        Text(
            text = stringResource(R.string.datalog_rpm_format, record.rpm.toInt().toString()),
            fontSize = 12.sp,
            color = gaugeGreen,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "${record.speed.toInt()} km/h",
            fontSize = 12.sp,
            color = textPrimary
        )
        Text(
            text = "${record.coolantTemp.toInt()}°C",
            fontSize = 12.sp,
            color = gaugeOrange
        )
    }
}

@Composable
private fun TrendGraph(recordedData: List<DataRecord>) {
    if (recordedData.size < 2) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.datalog_need_2_points), color = textSecondary, fontSize = 14.sp)
        }
    } else {
        Column {
            Text(
                text = stringResource(R.string.datalog_rpm_trend),
                fontSize = 12.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(canopoDark.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val maxRpm = recordedData.takeLast(MAX_TREND_POINTS).maxOf { it.rpm }.coerceAtLeast(1.0)
                val minRpm = recordedData.takeLast(MAX_TREND_POINTS).minOf { it.rpm }
                val range = (maxRpm - minRpm).coerceAtLeast(1.0)

                val path = Path()
                val points = recordedData.takeLast(MAX_TREND_POINTS)

                points.forEachIndexed { index, record ->
                    val x = size.width * index / (points.size - 1).coerceAtLeast(1)
                    val y = size.height - ((record.rpm - minRpm) / range * size.height).toFloat()

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = gaugeGreen,
                    style = Stroke(width = 3f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.datalog_speed_trend),
                fontSize = 12.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(canopoDark.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val maxSpeed = recordedData.takeLast(MAX_TREND_POINTS).maxOf { it.speed }.coerceAtLeast(1.0)

                val path = Path()
                val points = recordedData.takeLast(MAX_TREND_POINTS)

                points.forEachIndexed { index, record ->
                    val x = size.width * index / (points.size - 1).coerceAtLeast(1)
                    val y = size.height - (record.speed / maxSpeed * size.height).toFloat()

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = canopoAccent,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

// Haelt den Export-Zustand und die Share-Koroutine getrennt vom reinen Layout
// des Pickers. Bestaetigen startet die Vorbereitung auf IO und den Chooser auf
// dem Main-Thread; der Busy-Zustand wird immer per finally zurueckgesetzt.
@Composable
private fun ExportFormatPickerHost(
    recordedData: List<DataRecord>,
    selectedFormat: ExportFormat,
    gpxEnabled: Boolean,
    onFormatChange: (ExportFormat) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    ExportFormatPicker(
        selectedFormat = selectedFormat,
        gpxEnabled = gpxEnabled,
        isExporting = isExporting,
        onSelect = onFormatChange,
        onConfirm = {
            isExporting = true
            val format = selectedFormat
            val records = recordedData.toList()
            launchShare(
                scope = scope,
                context = context,
                records = records,
                format = format,
                onSuccess = onDismiss,
                onSettled = { isExporting = false }
            )
        },
        onCancel = { if (!isExporting) onDismiss() }
    )
}

@Composable
private fun ExportFormatPicker(
    selectedFormat: ExportFormat,
    gpxEnabled: Boolean,
    isExporting: Boolean,
    onSelect: (ExportFormat) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    // A real modal Dialog: it owns its own window, so the back button and taps
    // outside dismiss only the picker (onCancel) and never fall through to the
    // outer data-log dialog. usePlatformDefaultWidth keeps our width fraction;
    // the inner column scrolls so the buttons stay reachable on short screens.
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .testTag("export_format_picker")
                .fillMaxWidth(FORMAT_PICKER_WIDTH_FRACTION),
            shape = RoundedCornerShape(16.dp),
            color = canopoSurface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.datalog_export_pick_format),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = canopoHighlight
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExportFormatOptions(
                    selectedFormat = selectedFormat,
                    gpxEnabled = gpxEnabled,
                    selectionEnabled = !isExporting,
                    onSelect = onSelect
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExportPickerActions(
                    isExporting = isExporting,
                    onConfirm = onConfirm,
                    onCancel = onCancel
                )
            }
        }
    }
}

@Composable
private fun ExportFormatOptions(
    selectedFormat: ExportFormat,
    gpxEnabled: Boolean,
    selectionEnabled: Boolean,
    onSelect: (ExportFormat) -> Unit
) {
    ExportFormat.entries.forEach { format ->
        ExportFormatRow(
            format = format,
            selected = format == selectedFormat,
            enabled = (format != ExportFormat.GPX_WITH_OBD || gpxEnabled) && selectionEnabled,
            onSelect = onSelect
        )
    }
}

@Composable
private fun ExportFormatRow(
    format: ExportFormat,
    selected: Boolean,
    enabled: Boolean,
    onSelect: (ExportFormat) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("export_format_${format.name}")
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = { onSelect(format) }
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = format.label,
                color = if (enabled) textPrimary else textDim,
                fontSize = 14.sp
            )
            Text(
                text = stringResource(exportFormatDescriptionRes(format)),
                color = if (enabled) textSecondary else textDim,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ExportPickerActions(
    isExporting: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onCancel, enabled = !isExporting) {
            Text(stringResource(R.string.datalog_export_cancel))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onConfirm, enabled = !isExporting) {
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(stringResource(R.string.datalog_export_confirm))
        }
    }
}

private fun exportFormatDescriptionRes(format: ExportFormat): Int = when (format) {
    ExportFormat.CSV -> R.string.datalog_export_format_desc_csv
    ExportFormat.JSON -> R.string.datalog_export_format_desc_json
    ExportFormat.GPX_WITH_OBD -> R.string.datalog_export_format_desc_gpx
    ExportFormat.TORQUE_CSV -> R.string.datalog_export_format_desc_torque
}

// Export-Erzeugung + Dateischreiben muessen nicht auf dem Main-Thread laufen
// (viele Datensaetze), deshalb withContext(IO). Der Share-Chooser hingegen muss
// auf dem Main-Thread starten; startActivity bleibt daher nach dem withContext.
private fun launchShare(
    scope: CoroutineScope,
    context: Context,
    records: List<DataRecord>,
    format: ExportFormat,
    onSuccess: () -> Unit,
    onSettled: () -> Unit
) {
    scope.launch {
        try {
            val uri = withContext(Dispatchers.IO) { prepareExportFile(context, records, format) }
            context.startActivity(buildShareIntent(context, uri, format))
            onSuccess()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.datalog_export_failed, error.message ?: ""),
                Toast.LENGTH_SHORT
            ).show()
        } finally {
            onSettled()
        }
    }
}

private fun prepareExportFile(context: Context, records: List<DataRecord>, format: ExportFormat): Uri {
    val content = DataExporter.export(records, format)
    val fileName = "canop_obd_log_${System.currentTimeMillis()}.${format.extension}"
    val file = File(context.cacheDir, fileName)
    file.writeText(content)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

private fun buildShareIntent(context: Context, uri: Uri, format: ExportFormat): Intent {
    val label = uri.lastPathSegment.orEmpty()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = format.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(label, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    return Intent.createChooser(intent, context.getString(R.string.datalog_export_as))
}
