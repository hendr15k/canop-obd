package com.canopobd.ui.dtc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canopobd.R
import com.canopobd.data.model.DiagnosticTroubleCode
import com.canopobd.data.model.DTCResponse
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Composable
fun DTCDialog(
    dtcResponse: DTCResponse?,
    onDismiss: () -> Unit,
    onClearDTCs: () -> Unit
) {
    val colors = LocalAppColors.current
    val allCount = (dtcResponse?.codes?.size ?: 0) + (dtcResponse?.pendingCodes?.size ?: 0)
    val eyebrow = if (allCount > 0) "$allCount Codes gefunden" else null

    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.dtc_title),
        eyebrow = eyebrow,
        heightFraction = 0.85f
    ) {
        if (dtcResponse == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Lese Fehlercodes…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary
                    )
                }
            }
        } else {
            val allCodes = dtcResponse.codes + dtcResponse.pendingCodes
            if (allCodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(40.dp))
                                .background(colors.success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.dtc_none_found),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.success
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Keine Fehler im Steuergerät",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (dtcResponse.codes.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.dtc_stored, dtcResponse.codes.size),
                                icon = Icons.Filled.Error
                            )
                        }
                        items(dtcResponse.codes) { dtc ->
                            DTCItem(dtc = dtc, isPending = false)
                        }
                    }
                    if (dtcResponse.pendingCodes.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.dtc_cyclic, dtcResponse.pendingCodes.size),
                                icon = Icons.Filled.Warning
                            )
                        }
                        items(dtcResponse.pendingCodes) { dtc ->
                            DTCItem(dtc = dtc, isPending = true)
                        }
                    }
                }
                DividerLine()
                Box(modifier = Modifier.padding(16.dp)) {
                    GradientButton(
                        text = stringResource(R.string.dtc_clear),
                        onClick = onClearDTCs,
                        icon = Icons.Filled.Delete,
                        gradient = colors.gradientCritical,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DTCItem(dtc: DiagnosticTroubleCode, isPending: Boolean) {
    val colors = LocalAppColors.current
    val c = if (isPending) colors.warning else colors.critical
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, c.copy(alpha = 0.3f), RoundedCornerShape(AppRadius.md))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(AppRadius.sm))
                .background(c.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isPending) Icons.Filled.Warning else Icons.Filled.Error,
                contentDescription = null,
                tint = c,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dtc.code,
                    style = MaterialTheme.typography.titleSmall,
                    color = c,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                StatusPill(
                    text = if (isPending) "PENDING" else "STORIERT",
                    color = c
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = dtc.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}
