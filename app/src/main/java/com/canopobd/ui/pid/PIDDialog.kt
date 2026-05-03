package com.canopobd.ui.pid

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
import com.canopobd.data.model.OBDData
import com.canopobd.ui.theme.*

@Composable
fun PIDDialog(
    obdData: OBDData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
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
                        text = "Erweiterte Sensoren",
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Schließen", tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    item {
                        PIDSection(title = "Motor")
                    }
                    items(
                        listOf(
                            Triple("Timing Advance", "%.1f°".format(obdData.timingAdvance), gaugeGreen),
                            Triple("MAF Rate", "%.1f g/s".format(obdData.mafRate), gaugeGreen),
                            Triple("Fuel Pressure", "%.0f kPa".format(obdData.fuelPressure), gaugeYellow),
                            Triple("Intake Pressure", "%.0f kPa".format(obdData.intakePressure), gaugeGreen),
                            Triple("Engine Runtime", "%.0f s".format(obdData.runTime), textSecondary)
                        )
                    ) { (label, value, color) ->
                        PIDRow(label = label, value = value, color = color)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        PIDSection(title = "Emissionen")
                    }
                    items(
                        listOf(
                            Triple("Commanded EGR", "%.1f%%".format(obdData.commandedEGR), gaugeGreen),
                            Triple("EGR Temperature", "%.0f°C".format(obdData.egrTemp), gaugeYellow),
                            Triple("Evap Purge", "%.1f%%".format(obdData.commandedEvapPurge), gaugeGreen),
                            Triple("Barometric", "%.0f kPa".format(obdData.barometricPressure), gaugeGreen)
                        )
                    ) { (label, value, color) ->
                        PIDRow(label = label, value = value, color = color)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        PIDSection(title = "Lambda / O2")
                    }
                    items(
                        listOf(
                            Triple("O2 B1S1 Voltage", "%.3f V".format(obdData.o2VoltageB1S1), gaugeYellow),
                            Triple("O2 B1S2 Voltage", "%.3f V".format(obdData.o2VoltageB1S2), gaugeYellow)
                        )
                    ) { (label, value, color) ->
                        PIDRow(label = label, value = value, color = color)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        PIDSection(title = "Katalysator")
                    }
                    items(
                        listOf(
                            Triple("Catalyst Temp B1S1", "%.1f°C".format(obdData.catalystTemp), gaugeOrange)
                        )
                    ) { (label, value, color) ->
                        PIDRow(label = label, value = value, color = color)
                    }
                }
            }
        }
    }
}

@Composable
private fun PIDSection(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        color = canopoAccent,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun PIDRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .background(canopoDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = textSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = color
        )
    }
}
