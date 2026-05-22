package com.canopobd.ui.readiness

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.ReadinessMonitor
import com.canopobd.ui.theme.*

@Composable
fun ReadinessMonitorDialog(
    readiness: ReadinessMonitor,
    onDismiss: () -> Unit
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
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.readiness_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (readiness.allComplete) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = gaugeGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = gaugeGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.readiness_complete),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = gaugeGreen
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = gaugeYellow.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = gaugeYellow, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.readiness_progress, readiness.completedCount, readiness.totalCount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = gaugeYellow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                @Suppress("DEPRECATION")
                LinearProgressIndicator(
                    progress = readiness.completedCount.toFloat() / readiness.totalCount,
                    modifier = Modifier.fillMaxWidth().height(8.dp).background(canopoDark, RoundedCornerShape(4.dp)),
                    color = if (readiness.allComplete) gaugeGreen else gaugeYellow,
                    trackColor = canopoDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item { ReadinessRow(stringResource(R.string.readiness_misfire), readiness.misfire) }
                    item { ReadinessRow(stringResource(R.string.readiness_fuel_system), readiness.fuelSystem) }
                    item { ReadinessRow(stringResource(R.string.readiness_comprehensive), readiness.comprehensiveComponent) }
                    item { ReadinessRow(stringResource(R.string.readiness_catalyst), readiness.catalyst) }
                    item { ReadinessRow(stringResource(R.string.readiness_heated_catalyst), readiness.heatedCatalyst) }
                    item { ReadinessRow(stringResource(R.string.readiness_evap), readiness.evapSystem) }
                    item { ReadinessRow(stringResource(R.string.readiness_secondary_air), readiness.secondaryAirSystem) }
                    item { ReadinessRow(stringResource(R.string.readiness_ac), readiness.acSystemRefrigerant) }
                    item { ReadinessRow(stringResource(R.string.readiness_o2_sensor), readiness.oxygenSensor) }
                    item { ReadinessRow(stringResource(R.string.readiness_o2_heater), readiness.oxygenSensorHeater) }
                    item { ReadinessRow(stringResource(R.string.readiness_egr), readiness.egrSystem) }
                }
            }
        }
    }
}

@Composable
private fun ReadinessRow(label: String, complete: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (complete) gaugeGreen.copy(alpha = 0.08f) else canopoDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (complete) textPrimary else textSecondary
            )
            Icon(
                if (complete) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = if (complete) gaugeGreen else gaugeRed,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
