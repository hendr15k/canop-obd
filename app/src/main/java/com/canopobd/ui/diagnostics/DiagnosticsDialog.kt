package com.canopobd.ui.diagnostics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.canopobd.data.model.FreezeFrame
import com.canopobd.ui.theme.*

@Composable
fun DiagnosticsDialog(
    protocol: String,
    supportedPIDs: List<String>,
    freezeFrames: List<FreezeFrame>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f),
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
                        text = stringResource(R.string.diagnostics_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        SectionHeader(stringResource(R.string.diagnostics_protocol))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = canopoDark
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Memory, contentDescription = null, tint = canopoAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = protocol.ifBlank { "Wird ermittelt..." },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                            }
                        }
                    }

                    item {
                        SectionHeader("${stringResource(R.string.diagnostics_supported_pids)} (${supportedPIDs.size})")
                        if (supportedPIDs.isEmpty()) {
                            Text(
                                text = "Scanning...",
                                fontSize = 12.sp,
                                color = textSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            val pidChunks = supportedPIDs.chunked(8)
                            pidChunks.forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    chunk.forEach { pid ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = canopoDark
                                        ) {
                                            Text(
                                                text = pid,
                                                fontSize = 11.sp,
                                                color = canopoAccent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader(stringResource(R.string.diagnostics_freeze_frames))
                        if (freezeFrames.isEmpty()) {
                            Text(
                                text = stringResource(R.string.diagnostics_no_freeze_frames),
                                fontSize = 12.sp,
                                color = textSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            freezeFrames.forEach { frame ->
                                FreezeFrameCard(frame)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = canopoAccent,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun FreezeFrameCard(frame: FreezeFrame) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = canopoDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = gaugeOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = frame.dtc.code,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = gaugeOrange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = frame.dtc.description,
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
            if (frame.data.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    frame.data.forEach { (key, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "%.1f".format(value), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(text = key, fontSize = 10.sp, color = textSecondary)
                        }
                    }
                }
            }
        }
    }
}
