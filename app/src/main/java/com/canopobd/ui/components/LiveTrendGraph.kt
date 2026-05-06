package com.canopobd.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
                        text = stringResource(R.string.live_trend),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = canopoHighlight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        TrendChart(
                            data = trendHistory.rpm,
                            label = "RPM",
                            unit = "rpm",
                            color = gaugeGreen,
                            maxValue = 8000f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        TrendChart(
                            data = trendHistory.speed,
                            label = "Speed",
                            unit = "km/h",
                            color = canopoAccent,
                            maxValue = 260f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        TrendChart(
                            data = trendHistory.coolantTemp,
                            label = "Coolant",
                            unit = "°C",
                            color = gaugeOrange,
                            maxValue = 215f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        TrendChart(
                            data = trendHistory.boostPressure,
                            label = "Boost Pressure",
                            unit = "kPa",
                            color = Color(0xFF4CAF50),
                            maxValue = 250f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        TrendChart(
                            data = trendHistory.wastegateDuty,
                            label = "Wastegate Duty",
                            unit = "%",
                            color = Color(0xFF9C27B0),
                            maxValue = 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        TrendChart(
                            data = trendHistory.turboRpm,
                            label = "Turbo RPM",
                            unit = "rpm",
                            color = Color(0xFFFF9800),
                            maxValue = 200000f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        TrendChart(
                            data = trendHistory.egtBank1,
                            label = "EGT Bank 1",
                            unit = "°C",
                            color = Color(0xFFF44336),
                            maxValue = 950f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    item {
                        TrendChart(
                            data = trendHistory.chargeAirTemp,
                            label = "Charge Air Temp",
                            unit = "°C",
                            color = Color(0xFF00BCD4),
                            maxValue = 80f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrendStat(label = "RPM", value = trendHistory.rpm.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = gaugeGreen)
                            TrendStat(label = "Speed", value = trendHistory.speed.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = canopoAccent)
                            TrendStat(label = "Coolant", value = trendHistory.coolantTemp.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = gaugeOrange)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrendStat(label = "Boost", value = trendHistory.boostPressure.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = Color(0xFF4CAF50))
                            TrendStat(label = "WGate", value = trendHistory.wastegateDuty.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = Color(0xFF9C27B0))
                            TrendStat(label = "T-RPM", value = trendHistory.turboRpm.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = Color(0xFFFF9800))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrendStat(label = "EGT", value = trendHistory.egtBank1.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = Color(0xFFF44336))
                            TrendStat(label = "Charge", value = trendHistory.chargeAirTemp.lastOrNull()?.value?.toInt()?.toString() ?: "--", color = Color(0xFF00BCD4))
                        }
                    }
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
    private val _boostPressureHistory = mutableListOf<TrendPoint>()
    private val _wastegateDutyHistory = mutableListOf<TrendPoint>()
    private val _turboRpmHistory = mutableListOf<TrendPoint>()
    private val _egtBank1History = mutableListOf<TrendPoint>()
    private val _chargeAirTempHistory = mutableListOf<TrendPoint>()

    fun record(
        rpm: Double,
        speed: Double,
        coolantTemp: Double,
        boostPressure: Double = 0.0,
        wastegateDuty: Double = 0.0,
        turboRpm: Double = 0.0,
        egtBank1: Double = 0.0,
        chargeAirTemp: Double = 0.0
    ) {
        val now = System.currentTimeMillis()
        if (_rpmHistory.isNotEmpty() && now - _rpmHistory.last().timestamp < 500) return

        _rpmHistory.add(TrendPoint(now, rpm.toFloat()))
        _speedHistory.add(TrendPoint(now, speed.toFloat()))
        _coolantHistory.add(TrendPoint(now, coolantTemp.toFloat()))
        _boostPressureHistory.add(TrendPoint(now, boostPressure.toFloat()))
        _wastegateDutyHistory.add(TrendPoint(now, wastegateDuty.toFloat()))
        _turboRpmHistory.add(TrendPoint(now, turboRpm.toFloat()))
        _egtBank1History.add(TrendPoint(now, egtBank1.toFloat()))
        _chargeAirTempHistory.add(TrendPoint(now, chargeAirTemp.toFloat()))

        while (_rpmHistory.size > maxPoints) _rpmHistory.removeAt(0)
        while (_speedHistory.size > maxPoints) _speedHistory.removeAt(0)
        while (_coolantHistory.size > maxPoints) _coolantHistory.removeAt(0)
        while (_boostPressureHistory.size > maxPoints) _boostPressureHistory.removeAt(0)
        while (_wastegateDutyHistory.size > maxPoints) _wastegateDutyHistory.removeAt(0)
        while (_turboRpmHistory.size > maxPoints) _turboRpmHistory.removeAt(0)
        while (_egtBank1History.size > maxPoints) _egtBank1History.removeAt(0)
        while (_chargeAirTempHistory.size > maxPoints) _chargeAirTempHistory.removeAt(0)
    }

    fun getHistory(): TrendHistory = TrendHistory(
        rpm = _rpmHistory.toList(),
        speed = _speedHistory.toList(),
        coolantTemp = _coolantHistory.toList(),
        boostPressure = _boostPressureHistory.toList(),
        wastegateDuty = _wastegateDutyHistory.toList(),
        turboRpm = _turboRpmHistory.toList(),
        egtBank1 = _egtBank1History.toList(),
        chargeAirTemp = _chargeAirTempHistory.toList()
    )

    fun clear() {
        _rpmHistory.clear()
        _speedHistory.clear()
        _coolantHistory.clear()
        _boostPressureHistory.clear()
        _wastegateDutyHistory.clear()
        _turboRpmHistory.clear()
        _egtBank1History.clear()
        _chargeAirTempHistory.clear()
    }
}

data class MultiLineData(
    val series: List<TrendPoint>,
    val label: String,
    val unit: String,
    val color: Color,
    val maxValue: Float
)

@Composable
fun MultiLineTrendChart(
    dataSeries: List<MultiLineData>,
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            dataSeries.forEach { series ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(series.color, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = series.label,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(colors.surfaceCard, RoundedCornerShape(8.dp))
        ) {
            val width = size.width
            val height = size.height
            val padding = 8.dp.toPx()
            val chartWidth = width - padding * 2
            val chartHeight = height - padding * 2
            
            dataSeries.forEach { data ->
                if (data.series.size < 2) return@forEach
                
                val maxVal = data.maxValue.coerceAtLeast(1f)
                val path = Path()
                var isFirst = true
                
                data.series.forEachIndexed { index, point ->
                    val x = padding + (index.toFloat() / (data.series.size - 1).coerceAtLeast(1)) * chartWidth
                    val y = padding + chartHeight - (point.value.coerceIn(0f, maxVal) / maxVal) * chartHeight
                    
                    if (isFirst) {
                        path.moveTo(x, y)
                        isFirst = false
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = data.color,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}
