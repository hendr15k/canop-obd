package com.canopobd.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
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
    var showProblemCases by remember { mutableStateOf(false) }

    if (showProblemCases) {
        DiagnosticDetailDialog(onDismiss = { showProblemCases = false })
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
                        text = stringResource(R.string.diagnostics_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Button to open problem cases dialog
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProblemCases = true },
                    shape = RoundedCornerShape(12.dp),
                    color = canopoAccent.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Biotech,
                            contentDescription = null,
                            tint = canopoAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.diagnostics_problem_cases),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = canopoAccent
                            )
                            Text(
                                text = stringResource(R.string.diagnostics_problem_cases_desc),
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = canopoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                    text = protocol.ifBlank { stringResource(R.string.detecting_protocol) },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                            }
                        }
                    }

                    // Opel Astra J ECU Information
                    item {
                        SectionHeader("ECU Adressen (GMLAN)")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = canopoDark
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                listOf(
                                    "7E0 -> 7E8" to "ECM (Motorsteuerung)",
                                    "7E1 -> 7E9" to "TCM (Getriebe)",
                                    "7C0 -> 7C8" to "BCM (Body Control)",
                                    "7C3 -> 7CB" to "IPC (Instrumentencluster)",
                                    "7C2 -> 7CA" to "ABS/ESP",
                                    "7C5 -> 7CD" to "SRS (Airbag)"
                                ).forEach { (address, name) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(address, fontFamily = FontFamily.Monospace, color = gaugeGreen, fontSize = 11.sp)
                                        Text(name, color = textDim, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // PSA/Stellantis CAN IDs
                    item {
                        SectionHeader("PSA/Stellantis CAN-IDs")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = canopoDark
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                listOf(
                                    "74B" to "PORTEC (Fenster/Tueren)",
                                    "752" to "BMF (Body Module)",
                                    "76B" to "BSI (Komfort)",
                                    "240" to "DDM (Fahrertuer)",
                                    "340" to "PDM (Beifahrertuer)",
                                    "420" to "NAC/RCC (Radio/Navi)"
                                ).forEach { (id, name) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(id, fontFamily = FontFamily.Monospace, color = gaugeCyan, fontSize = 11.sp)
                                        Text(name, color = textDim, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionHeader("${stringResource(R.string.diagnostics_supported_pids)} (${supportedPIDs.size})")
                        if (supportedPIDs.isEmpty()) {
                            Text(
                                text = stringResource(R.string.scanning_pids),
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

                    // UDS Services
                    item {
                        SectionHeader("UDS Dienste (ISO 14229)")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = canopoDark
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                listOf(
                                    "0x10" to "DiagnosticSessionControl",
                                    "0x11" to "ECUReset",
                                    "0x14" to "ClearDiagnosticInfo",
                                    "0x19" to "ReadDTCInformation",
                                    "0x22" to "ReadDataByIdentifier",
                                    "0x27" to "SecurityAccess",
                                    "0x2E" to "WriteDataByIdentifier",
                                    "0x2F" to "InputOutputControlByID",
                                    "0x31" to "RoutineControl",
                                    "0x3E" to "TesterPresent"
                                ).forEach { (code, name) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(code, fontFamily = FontFamily.Monospace, color = gaugeOrange, fontSize = 10.sp)
                                        Text(name, color = textDim, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Mode 22 Extended PIDs
                    item {
                        SectionHeader("Mode 22 Erweiterte PIDs")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = canopoDark
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                listOf(
                                    "F190" to "VIN (Vehicle ID)",
                                    "F191" to "ECU Hardware Version",
                                    "F192" to "ECU Software Version",
                                    "F193" to "Calibration ID",
                                    "F181" to "Application Software ID",
                                    "F18C" to "ECU Serial Number"
                                ).forEach { (pid, name) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("22$pid", fontFamily = FontFamily.Monospace, color = gaugeYellow, fontSize = 10.sp)
                                        Text(name, color = textDim, fontSize = 10.sp)
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
