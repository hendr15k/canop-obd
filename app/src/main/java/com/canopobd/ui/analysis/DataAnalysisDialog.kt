package com.canopobd.ui.analysis

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.CsvImportEntry
import com.canopobd.data.model.FuelTrimAnalysis
import com.canopobd.ui.theme.*

@Composable
fun DataAnalysisDialog(
    importedData: List<CsvImportEntry>,
    fuelTrimAnalysis: FuelTrimAnalysis,
    onDismiss: () -> Unit,
    onImportCsv: (String) -> Unit,
    onClearImported: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                onImportCsv(reader.readText())
            }
        }
    }

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
                        text = stringResource(R.string.analysis_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = canopoDark,
                    contentColor = canopoAccent
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.analysis_import_csv), color = if (selectedTab == 0) textPrimary else textSecondary) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.analysis_fuel_trim), color = if (selectedTab == 1) textPrimary else textSecondary) })
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> CsvImportTab(
                        importedData = importedData,
                        onImport = {
                            fileLauncher.launch(arrayOf("text/csv", "text/plain", "application/octet-stream"))
                        },
                        onClear = onClearImported
                    )
                    1 -> FuelTrimTab(analysis = fuelTrimAnalysis)
                }
            }
        }
    }
}

@Composable
private fun CsvImportTab(
    importedData: List<CsvImportEntry>,
    onImport: () -> Unit,
    onClear: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onImport,
                colors = ButtonDefaults.buttonColors(containerColor = canopoAccent),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.analysis_import_csv))
            }
            if (importedData.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.maintenance_reset), tint = gaugeRed)
                }
            }
        }

        if (importedData.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.analysis_imported, importedData.size),
                fontSize = 12.sp,
                color = gaugeGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            ImportedDataChart(importedData)
        } else {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.InsertDriveFile, contentDescription = null, tint = textDim, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.analysis_no_data), color = textSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ImportedDataChart(data: List<CsvImportEntry>) {
    Column {
        Text("RPM", fontSize = 11.sp, color = textSecondary)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(canopoDark, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            if (data.size < 2) return@Canvas
            val maxRpm = data.maxOf { it.rpm }.coerceAtLeast(1.0)
            val path = Path()
            data.forEachIndexed { i, entry ->
                val x = size.width * i / (data.size - 1)
                val y = size.height - (entry.rpm / maxRpm * size.height).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = gaugeGreen, style = Stroke(width = 2f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Speed", fontSize = 11.sp, color = textSecondary)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(canopoDark, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            if (data.size < 2) return@Canvas
            val maxSpeed = data.maxOf { it.speed }.coerceAtLeast(1.0)
            val path = Path()
            data.forEachIndexed { i, entry ->
                val x = size.width * i / (data.size - 1)
                val y = size.height - (entry.speed / maxSpeed * size.height).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = canopoAccent, style = Stroke(width = 2f))
        }
    }
}

@Composable
private fun FuelTrimTab(analysis: FuelTrimAnalysis) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                text = stringResource(R.string.analysis_fuel_trim),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = canopoHighlight
            )
        }

        item {
            Text("Bank 1", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = canopoAccent)
        }

        item {
            TrimRow(stringResource(R.string.stft_label), analysis.stftB1)
            TrimRow(stringResource(R.string.ltft_label), analysis.ltftB1)
            TrimRow(stringResource(R.string.analysis_total_trim), analysis.totalTrimB1)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = when {
                    analysis.totalTrimB1 > 10.0 || analysis.totalTrimB1 < -10.0 -> gaugeOrange.copy(alpha = 0.15f)
                    else -> gaugeGreen.copy(alpha = 0.1f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (analysis.totalTrimB1 in -10.0..10.0) Icons.Filled.Check else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (analysis.totalTrimB1 in -10.0..10.0) gaugeGreen else gaugeOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.analysis_status)}: ${analysis.statusB1}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (analysis.totalTrimB1 in -10.0..10.0) gaugeGreen else gaugeOrange
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            @Suppress("DEPRECATION")
            Divider(color = canopoDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Bank 2", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = canopoAccent)
        }

        item {
            TrimRow(stringResource(R.string.stft_label), analysis.stftB2)
            TrimRow(stringResource(R.string.ltft_label), analysis.ltftB2)
            TrimRow(stringResource(R.string.analysis_total_trim), analysis.totalTrimB2)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = when {
                    analysis.totalTrimB2 > 10.0 || analysis.totalTrimB2 < -10.0 -> gaugeOrange.copy(alpha = 0.15f)
                    else -> gaugeGreen.copy(alpha = 0.1f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (analysis.totalTrimB2 in -10.0..10.0) Icons.Filled.Check else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (analysis.totalTrimB2 in -10.0..10.0) gaugeGreen else gaugeOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.analysis_status)}: ${analysis.statusB2}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (analysis.totalTrimB2 in -10.0..10.0) gaugeGreen else gaugeOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun TrimRow(label: String, value: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = textSecondary)
        Text(
            text = "%+.1f%%".format(value),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                value > 10.0 || value < -10.0 -> gaugeOrange
                else -> gaugeGreen
            }
        )
    }
}
