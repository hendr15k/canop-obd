package com.canopobd.ui.dtc

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.DiagnosticTroubleCode
import com.canopobd.data.model.DTCResponse
import com.canopobd.ui.theme.*

@Composable
fun DTCDialog(
    dtcResponse: DTCResponse?,
    onDismiss: () -> Unit,
    onClearDTCs: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
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
                        text = "Diagnose Fehlerspeicher",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dtcResponse == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = canopoAccent)
                    }
                } else {
                    val allCodes = dtcResponse.codes + dtcResponse.pendingCodes
                    
                    if (allCodes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = gaugeGreen,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Keine Fehlercodes gefunden",
                                    color = gaugeGreen,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            if (dtcResponse.codes.isNotEmpty()) {
                                item {
                                    Text(
                                        "Gespeicherte Fehler (${dtcResponse.codes.size})",
                                        color = gaugeRed,
                                        fontSize = 14.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(dtcResponse.codes) { dtc ->
                                    DTCItem(dtc = dtc, isPending = false)
                                }
                            }

                            if (dtcResponse.pendingCodes.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Zyklische Fehler (${dtcResponse.pendingCodes.size})",
                                        color = gaugeYellow,
                                        fontSize = 14.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(dtcResponse.pendingCodes) { dtc ->
                                    DTCItem(dtc = dtc, isPending = true)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onClearDTCs,
                            colors = ButtonDefaults.buttonColors(containerColor = gaugeRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fehler löschen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DTCItem(dtc: DiagnosticTroubleCode, isPending: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) canopoDark else canopoSurface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                if (isPending) Icons.Filled.Warning else Icons.Filled.Error,
                contentDescription = null,
                tint = if (isPending) gaugeYellow else gaugeRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = dtc.code,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isPending) gaugeYellow else gaugeRed
                )
                Text(
                    text = dtc.description,
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
        }
    }
}
