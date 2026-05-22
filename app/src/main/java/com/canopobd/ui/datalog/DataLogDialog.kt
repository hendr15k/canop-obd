package com.canopobd.ui.datalog

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.canopobd.R
import com.canopobd.data.model.DataRecord
import com.canopobd.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DataLogDialog(
    recordedData: List<DataRecord>,
    isRecording: Boolean,
    onDismiss: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearData: () -> Unit,
    onExportData: () -> String
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

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
                        text = stringResource(R.string.datalog_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = if (isRecording) onStopRecording else onStartRecording,
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
                        onClick = {
                            val csv = onExportData()
                            if (csv.lines().size > 1) {
                                exportCsv(context, csv)
                            } else {
                                Toast.makeText(context, context.getString(R.string.datalog_no_data), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.datalog_export))
                    }

                    IconButton(onClick = onClearData) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.datalog_delete), tint = gaugeRed)
                    }
                }

                Text(
                    text = if (isRecording) stringResource(R.string.datalog_recording, recordedData.size) else stringResource(R.string.datalog_entries, recordedData.size),
                    fontSize = 12.sp,
                    color = if (isRecording) gaugeGreen else textSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = canopoDark,
                    contentColor = canopoAccent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Verlauf", color = textPrimary) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Trend", color = textPrimary) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> DataList(recordedData)
                    1 -> TrendGraph(recordedData)
                }
            }
        }
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
            items(recordedData.takeLast(100).reversed()) { record ->
                DataRecordItem(record)
            }
        }
    }
}

@Composable
private fun DataRecordItem(record: DataRecord) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(canopoDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dateFormat.format(Date(record.timestamp)),
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
                val maxRpm = recordedData.takeLast(100).maxOf { it.rpm }.coerceAtLeast(1.0)
                val minRpm = recordedData.takeLast(100).minOf { it.rpm }
                val range = (maxRpm - minRpm).coerceAtLeast(1.0)
                
                val path = Path()
                val points = recordedData.takeLast(100)
                
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
                val maxSpeed = recordedData.takeLast(100).maxOf { it.speed }.coerceAtLeast(1.0)
                
                val path = Path()
                val points = recordedData.takeLast(100)
                
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

private fun exportCsv(context: Context, csv: String) {
    try {
        val fileName = "canop_obd_log_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(csv)
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.datalog_export_as)))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.datalog_export_failed, e.message ?: "Unknown error"), Toast.LENGTH_SHORT).show()
    }
}
