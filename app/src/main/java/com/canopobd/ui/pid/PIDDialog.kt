package com.canopobd.ui.pid

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canopobd.R
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.data.model.OBDData
import com.canopobd.ui.components.*
import com.canopobd.ui.theme.*

@Composable
fun PIDDialog(
    obdData: OBDData,
    measurementUnit: MeasurementUnit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val motorItems = listOf(
        Triple("Timing Advance", "%.1f°".format(obdData.timingAdvance), colors.success),
        Triple("MAF Rate", "%.1f g/s".format(obdData.mafRate), colors.success),
        Triple("Fuel Pressure", "%.0f%%".format(obdData.fuelPressure), colors.warning),
        Triple("Intake Pressure", "%.0f kPa".format(obdData.intakePressure), colors.success),
        Triple("Engine Runtime", formatRuntime(obdData.runTime.toInt()), colors.textSecondary),
        Triple("Fuel Rail Pressure", "%.0f kPa".format(obdData.fuelRailPressure), colors.warning),
        Triple("Absolute Load", "%.0f%%".format(obdData.absoluteLoadValue), colors.success)
    )
    val emissionItems = listOf(
        Triple("Commanded EGR", "%.1f%%".format(obdData.commandedEGR), colors.success),
        Triple("EGR Temperature", "%.0f%s".format(obdData.egrTemp, measurementUnit.tempUnit), colors.warning),
        Triple("Evap Purge", "%.1f%%".format(obdData.commandedEvapPurge), colors.success),
        Triple("Barometric", "%.0f kPa".format(obdData.barometricPressure), colors.success),
        Triple("Fuel Rate", "%.2f L/h".format(obdData.engineFuelRate), colors.warning)
    )
    val lambdaItems = listOf(
        Triple("STFT B1", "%.1f%%".format(obdData.shortTermFuelTrimB1), colors.success),
        Triple("LTFT B1", "%.1f%%".format(obdData.longTermFuelTrimB1), colors.success),
        Triple("STFT B2", "%.1f%%".format(obdData.shortTermFuelTrimB2), colors.success),
        Triple("LTFT B2", "%.1f%%".format(obdData.longTermFuelTrimB2), colors.success)
    )
    val catalystItems = listOf(
        Triple("Catalyst Temp", "%.0f%s".format(obdData.catalystTemp, measurementUnit.tempUnit), colors.textPrimary),
        Triple("Cat Temp B1S2", "%.0f%s".format(obdData.catalystTempB1S2, measurementUnit.tempUnit), colors.textPrimary),
        Triple("Cat Temp B2S1", "%.0f%s".format(obdData.catalystTempB2S1, measurementUnit.tempUnit), colors.textPrimary),
        Triple("Cat Temp B2S2", "%.0f%s".format(obdData.catalystTempB2S2, measurementUnit.tempUnit), colors.textPrimary)
    )

    DialogShell(
        onDismiss = onDismiss,
        title = stringResource(R.string.pid_title),
        eyebrow = "Live OBD-II Sensordaten",
        heightFraction = 0.9f
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SectionHeader(title = stringResource(R.string.pid_section_motor), icon = Icons.Filled.Memory)
            }
            items(motorItems) { (label, value, color) ->
                PIDRow(label = label, value = value, color = color)
            }
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(title = stringResource(R.string.pid_section_emissions), icon = Icons.Filled.Air)
            }
            items(emissionItems) { (label, value, color) ->
                PIDRow(label = label, value = value, color = color)
            }
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(title = stringResource(R.string.pid_section_lambda), icon = Icons.Filled.Science)
            }
            items(lambdaItems) { (label, value, color) ->
                PIDRow(label = label, value = value, color = color)
            }
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(title = stringResource(R.string.pid_section_catalyst), icon = Icons.Filled.LocalFireDepartment)
            }
            items(catalystItems) { (label, value, color) ->
                PIDRow(label = label, value = value, color = color)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PIDRow(label: String, value: String, color: Color) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatRuntime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%dh %dm".format(h, m) else "%dm %ds".format(m, s)
}
