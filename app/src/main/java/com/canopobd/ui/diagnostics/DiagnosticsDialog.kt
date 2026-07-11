package com.canopobd.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.FreezeFrame
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Composable
fun DiagnosticsDialog(
    protocol: String,
    supportedPIDs: List<String>,
    freezeFrames: List<FreezeFrame>,
    onDismiss: () -> Unit
) {
    var showProblemCases by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current

    if (showProblemCases) {
        DiagnosticDetailDialog(onDismiss = { showProblemCases = false })
    }

    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.diagnostics_title),
        eyebrow = "System-Diagnose",
        heightFraction = 0.9f
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().clickable { showProblemCases = true },
                    accentEdge = colors.primary,
                    padding = 12.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(AppRadius.sm))
                                .background(colors.primary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Biotech,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.diagnostics_problem_cases),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.diagnostics_problem_cases_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item { SectionHeader(title = stringResource(R.string.diagnostics_protocol), icon = Icons.Filled.Memory) }
            item {
                GlassCard(padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(AppRadius.sm))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Memory, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = protocol.ifBlank { stringResource(R.string.detecting_protocol) },
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Opel Astra J ECU Information
            item { SectionHeader(title = "ECU Adressen (GMLAN)", icon = Icons.Filled.Memory) }
            item {
                InfoTableCard(
                    items = listOf(
                        "7E0 → 7E8" to "ECM (Motor)",
                        "7E1 → 7E9" to "TCM (Getriebe)",
                        "7C0 → 7C8" to "BCM (Body Control)",
                        "7C3 → 7CB" to "IPC (Instrument)",
                        "7C2 → 7CA" to "ABS/ESP",
                        "7C5 → 7CD" to "SRS (Airbag)"
                    ),
                    valueColor = colors.success
                )
            }

            // PSA/Stellantis CAN IDs
            item { SectionHeader(title = "PSA/Stellantis CAN-IDs", icon = Icons.AutoMirrored.Filled.CompareArrows) }
            item {
                InfoTableCard(
                    items = listOf(
                        "74B" to "PORTEC (Fenster/Türen)",
                        "752" to "BMF (Body Module)",
                        "76B" to "BSI (Komfort)",
                        "240" to "DDM (Fahrertür)",
                        "340" to "PDM (Beifahrertür)",
                        "420" to "NAC/RCC (Radio/Navi)"
                    ),
                    valueColor = colors.info
                )
            }

            item { SectionHeader(title = "${stringResource(R.string.diagnostics_supported_pids)} (${supportedPIDs.size})", icon = Icons.AutoMirrored.Filled.List) }
            item {
                if (supportedPIDs.isEmpty()) {
                    Text(
                        text = stringResource(R.string.scanning_pids),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    val pidChunks = supportedPIDs.chunked(8)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        pidChunks.forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                chunk.forEach { pid ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(AppRadius.xs))
                                            .background(colors.surfaceRaised)
                                            .border(1.dp, colors.primary.copy(alpha = 0.3f), RoundedCornerShape(AppRadius.xs))
                                            .padding(horizontal = 6.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pid,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.primary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                repeat(8 - chunk.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // UDS Services
            item { SectionHeader(title = "UDS Dienste (ISO 14229)", icon = Icons.Filled.Code) }
            item {
                InfoTableCard(
                    items = listOf(
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
                    ),
                    valueColor = colors.warning
                )
            }

            // Mode 22 Extended PIDs
            item { SectionHeader(title = "Mode 22 Erweiterte PIDs", icon = Icons.Filled.Extension) }
            item {
                InfoTableCard(
                    items = listOf(
                        "F190" to "VIN (Vehicle ID)",
                        "F191" to "ECU Hardware Version",
                        "F192" to "ECU Software Version",
                        "F193" to "Calibration ID",
                        "F181" to "Application Software ID",
                        "F18C" to "ECU Serial Number"
                    ),
                    valueColor = colors.warning
                )
            }

            item { SectionHeader(title = stringResource(R.string.diagnostics_freeze_frames), icon = Icons.Filled.AcUnit) }
            item {
                if (freezeFrames.isEmpty()) {
                    Text(
                        text = stringResource(R.string.diagnostics_no_freeze_frames),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        freezeFrames.forEach { frame ->
                            FreezeFrameCard(frame)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun InfoTableCard(items: List<Pair<String, String>>, valueColor: Color) {
    val colors = LocalAppColors.current
    GlassCard(padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { (key, name) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = key,
                        fontFamily = FontFamily.Monospace,
                        color = valueColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = name,
                        color = colors.textTertiary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun FreezeFrameCard(frame: FreezeFrame) {
    val colors = LocalAppColors.current
    GlassCard(
        accentEdge = colors.warning,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(colors.warning.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = frame.dtc.code,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.warning,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = frame.dtc.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        if (frame.data.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            DividerLine()
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                frame.data.entries.take(4).forEach { (key, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f".format(value),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = key,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }
                }
            }
        }
    }
}
