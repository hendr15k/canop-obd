package com.canopobd.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.canopobd.R
import com.canopobd.data.model.TrendHistory
import com.canopobd.data.model.TrendPoint
import com.canopobd.ui.theme.*

@Composable
fun LiveTrendGraphDialog(
    trendHistory: TrendHistory,
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
                        text = stringResource(R.string.live_trend),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TrendChart(
                    data = trendHistory.rpm,
                    label = "RPM",
                    unit = "rpm",
                    color = gaugeGreen,
                    maxValue = 8000f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TrendChart(
                    data = trendHistory.speed,
                    label = "Speed",
                    unit = "km/h",
                    color = canopoAccent,
                    maxValue = 260f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TrendChart(
                    data = trendHistory.coolantTemp,
                    label = "Coolant",
                    unit = "°C",
                    color = gaugeOrange,
                    maxValue = 215f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TrendStat(label = "RPM", value = trendHistory.rpm.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = gaugeGreen)
                    TrendStat(label = "Speed", value = trendHistory.speed.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = canopoAccent)
                    TrendStat(label = "Coolant", value = trendHistory.coolantTemp.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = gaugeOrange)
                }
            }
        }
    }
}

@Composable
private fun TrendChart(
    data: List<TrendPoint>,
    label: String,
    unit: String,
    color: Color,
    maxValue: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = textSecondary
            )
            val last = data.lastOrNull()
            Text(
                text = if (last != null) "%.0f %s".format(last.value, unit) else "-- $unit",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(canopoDark, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            if (data.size < 2) return@Canvas

            val minVal = 0f
            val range = (maxValue - minVal).coerceAtLeast(1f)
            val w = size.width
            val h = size.height

            val path = Path()
            data.forEachIndexed { index, point ->
                val x = w * index / (data.size - 1).coerceAtLeast(1)
                val y = h - ((point.value - minVal) / range * h).coerceIn(0f, h)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.5f)
            )
        }
    }
}

@Composable
private fun TrendStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = textSecondary)
    }
}

class TrendRecorder(private val maxPoints: Int = 60) {
    private val _rpmHistory = mutableListOf<TrendPoint>()
    private val _speedHistory = mutableListOf<TrendPoint>()
    private val _coolantHistory = mutableListOf<TrendPoint>()

    fun record(rpm: Double, speed: Double, coolantTemp: Double) {
        val now = System.currentTimeMillis()
        if (_rpmHistory.isNotEmpty() && now - _rpmHistory.last().timestamp < 500) return

        _rpmHistory.add(TrendPoint(now, rpm.toFloat()))
        _speedHistory.add(TrendPoint(now, speed.toFloat()))
        _coolantHistory.add(TrendPoint(now, coolantTemp.toFloat()))

        while (_rpmHistory.size > maxPoints) _rpmHistory.removeAt(0)
        while (_speedHistory.size > maxPoints) _speedHistory.removeAt(0)
        while (_coolantHistory.size > maxPoints) _coolantHistory.removeAt(0)
    }

    fun getHistory(): TrendHistory = TrendHistory(
        rpm = _rpmHistory.toList(),
        speed = _speedHistory.toList(),
        coolantTemp = _coolantHistory.toList()
    )

    fun clear() {
        _rpmHistory.clear()
        _speedHistory.clear()
        _coolantHistory.clear()
    }
}
