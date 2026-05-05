package com.canopobd.ui.hud

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.data.model.MeasurementUnit
import com.canopobd.data.model.OBDData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HUDModeActivity(
    obdData: OBDData,
    measurementUnit: MeasurementUnit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        onDispose {
            window?.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val hudGreen = Color(0xFF00FF88)
    val hudCyan = Color(0xFF00DDFF)
    val hudOrange = Color(0xFFFF8800)
    val hudBg = Color(0xCC000000)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(hudBg)
                .systemBarsPadding()
                .padding(24.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Exit HUD",
                    tint = hudGreen.copy(alpha = 0.7f)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                val speedKmh = measurementUnit.convertSpeed(obdData.speed)
                val speedUnit = measurementUnit.speedUnit

                HUDSpeedGauge(
                    speed = speedKmh.toFloat(),
                    maxSpeed = 260f,
                    unit = speedUnit,
                    color = hudGreen
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HUDMetric(
                        label = "RPM",
                        value = "%.0f".format(obdData.rpm),
                        color = hudCyan
                    )
                    HUDMetric(
                        label = "TEMP",
                        value = "%.0f°".format(measurementUnit.convertTemp(obdData.coolantTemp)),
                        unit = measurementUnit.tempUnit,
                        color = if (obdData.coolantTemp > 100) Color(0xFFFF4444) else hudOrange
                    )
                    HUDMetric(
                        label = "LOAD",
                        value = "%.0f%%".format(obdData.engineLoad),
                        color = hudGreen
                    )
                    HUDMetric(
                        label = "THR",
                        value = "%.0f%%".format(obdData.throttle),
                        color = hudCyan
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HUDMetric(
                        label = "FUEL",
                        value = "%.0f%%".format(obdData.fuelLevel),
                        color = if (obdData.fuelLevel < 15) Color(0xFFFF4444) else hudOrange
                    )
                    HUDMetric(
                        label = "BATT",
                        value = "%.1fV".format(obdData.batteryVoltage),
                        color = hudGreen
                    )
                    HUDMetric(
                        label = "TIMING",
                        value = "%.1f°".format(obdData.timingAdvance),
                        color = hudCyan
                    )
                    HUDMetric(
                        label = "MAF",
                        value = "%.0f".format(obdData.mafRate),
                        unit = "g/s",
                        color = hudOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun HUDSpeedGauge(
    speed: Float,
    maxSpeed: Float,
    unit: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height
            val radius = size.width.coerceAtMost(size.height * 2) * 0.45f

            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 16f)
            )

            val fraction = (speed / maxSpeed).coerceIn(0f, 1f)
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f * fraction,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 16f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            val needleAngle = Math.toRadians((180.0 + 180.0 * fraction))
            val needleLen = radius * 0.75f
            val needleX = centerX + (needleLen * cos(needleAngle)).toFloat()
            val needleY = centerY + (needleLen * sin(needleAngle)).toFloat()

            drawLine(
                color = Color.White,
                start = Offset(centerX, centerY),
                end = Offset(needleX, needleY),
                strokeWidth = 6f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            drawCircle(
                color = Color.White,
                radius = 10f,
                center = Offset(centerX, centerY)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%.0f".format(speed),
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun HUDMetric(
    label: String,
    value: String,
    unit: String = "",
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color.copy(alpha = 0.6f)
        )
        Text(
            text = if (unit.isNotEmpty()) "$value $unit" else value,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
