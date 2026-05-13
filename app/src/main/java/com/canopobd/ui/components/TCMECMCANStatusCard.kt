package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun TCMECMCANStatusCard(
    modifier: Modifier = Modifier,
    tcmCurrentGear: Int = 0,
    tcmOilTempCelsius: Int = 0,
    tcmPressureKpa: Int = 0,
    tcmSportMode: Boolean = false,
    tcmManualMode: Boolean = false,
    tcmError: Boolean = false,
    ecmRpm: Double = 0.0,
    ecmSpeedKmh: Double = 0.0,
    ecmCoolantTemp: Int = 0,
    ecmThrottlePosition: Double = 0.0,
    ecmEngineLoad: Double = 0.0,
    lastUpdateTime: Long = 0L,
    colors: AppColors = LocalAppColors.current
) {
    val isStale = remember(lastUpdateTime) {
        System.currentTimeMillis() - lastUpdateTime > 5000
    }

    val borderColor = when {
        tcmError -> colors.gaugeRed
        isStale -> colors.gaugeOrange
        else -> colors.accent
    }

    val infiniteTransition = rememberInfiniteTransition(label = "can_status_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (tcmError) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "can_pulse_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                Modifier.border(
                    width = if (tcmError || isStale) 1.5.dp else 1.dp,
                    color = borderColor.copy(alpha = if (tcmError || isStale) pulseAlpha else 0.8f),
                    shape = RoundedCornerShape(12.dp)
                )
            ),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Hub,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CAN TCM/ECM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isStale) colors.gaugeOrange else colors.gaugeGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isStale) "Warte..." else "Aktiv",
                        fontSize = 10.sp,
                        color = if (isStale) colors.gaugeOrange else colors.gaugeGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TCMStatusSection(
                    currentGear = tcmCurrentGear,
                    oilTempCelsius = tcmOilTempCelsius,
                    pressureKpa = tcmPressureKpa,
                    sportMode = tcmSportMode,
                    manualMode = tcmManualMode,
                    hasError = tcmError,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(colors.surfaceVariant)
                )

                ECMStatusSection(
                    rpm = ecmRpm,
                    speedKmh = ecmSpeedKmh,
                    coolantTemp = ecmCoolantTemp,
                    throttlePosition = ecmThrottlePosition,
                    engineLoad = ecmEngineLoad,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TCMStatusSection(
    currentGear: Int,
    oilTempCelsius: Int,
    pressureKpa: Int,
    sportMode: Boolean,
    manualMode: Boolean,
    hasError: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "TCM",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            if (hasError) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Fehler",
                    tint = colors.gaugeRed,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Gang",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                Text(
                    text = when (currentGear) {
                        0 -> "N"
                        -1 -> "R"
                        in 1..6 -> "$currentGear"
                        else -> "—"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasError) colors.gaugeRed else colors.accent
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Temp",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                val tempColor = when {
                    oilTempCelsius > 100 -> colors.gaugeRed
                    oilTempCelsius > 80 -> colors.gaugeOrange
                    oilTempCelsius > 0 -> colors.gaugeGreen
                    else -> colors.textDim
                }
                Text(
                    text = if (oilTempCelsius > 0) "${oilTempCelsius}°C" else "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = tempColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Druck",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                Text(
                    text = if (pressureKpa > 0) "${pressureKpa}kPa" else "—",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (sportMode) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.gaugeOrange.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "S",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.gaugeOrange,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                if (manualMode) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.accent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "M",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ECMStatusSection(
    rpm: Double,
    speedKmh: Double,
    coolantTemp: Int,
    throttlePosition: Double,
    engineLoad: Double,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Engineering,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "ECM",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "RPM",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                val rpmColor = when {
                    rpm > 6000 -> colors.gaugeRed
                    rpm > 5500 -> colors.gaugeOrange
                    rpm > 0 -> colors.gaugeGreen
                    else -> colors.textDim
                }
                Text(
                    text = if (rpm > 0) "${rpm.toInt()}" else "—",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = rpmColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Geschw.",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                Text(
                    text = if (speedKmh > 0) "${speedKmh.toInt()} km/h" else "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Kühlmittel",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                val coolColor = when {
                    coolantTemp > 105 -> colors.gaugeRed
                    coolantTemp > 95 -> colors.gaugeOrange
                    coolantTemp > 0 -> colors.gaugeGreen
                    else -> colors.textDim
                }
                Text(
                    text = if (coolantTemp > 0) "${coolantTemp}°C" else "—",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = coolColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Drossel",
                    fontSize = 9.sp,
                    color = colors.textDim
                )
                Text(
                    text = if (throttlePosition >= 0) "${(throttlePosition * 100).toInt()}%" else "—",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
fun TCMECMCANCompactCard(
    modifier: Modifier = Modifier,
    tcmOilTempCelsius: Int = 0,
    tcmCurrentGear: Int = 0,
    ecmRpm: Double = 0.0,
    colors: AppColors = LocalAppColors.current
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Gang",
                    fontSize = 8.sp,
                    color = colors.textDim
                )
                Text(
                    text = when (tcmCurrentGear) {
                        0 -> "N"
                        -1 -> "R"
                        in 1..6 -> "$tcmCurrentGear"
                        else -> "—"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(colors.textDim.copy(alpha = 0.3f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Getriebe",
                    fontSize = 8.sp,
                    color = colors.textDim
                )
                Text(
                    text = if (tcmOilTempCelsius > 0) "${tcmOilTempCelsius}°C" else "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        tcmOilTempCelsius > 100 -> colors.gaugeRed
                        tcmOilTempCelsius > 80 -> colors.gaugeOrange
                        tcmOilTempCelsius > 0 -> colors.gaugeGreen
                        else -> colors.textDim
                    }
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(colors.textDim.copy(alpha = 0.3f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RPM",
                    fontSize = 8.sp,
                    color = colors.textDim
                )
                val rpmColor = when {
                    ecmRpm > 6000 -> colors.gaugeRed
                    ecmRpm > 5500 -> colors.gaugeOrange
                    ecmRpm > 0 -> colors.gaugeGreen
                    else -> colors.textDim
                }
                Text(
                    text = if (ecmRpm > 0) "${ecmRpm.toInt()}" else "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = rpmColor
                )
            }
        }
    }
}
