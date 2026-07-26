package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.*
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun ReadinessMonitorCard(
    readinessData: ReadinessMonitorData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val isComplete = readinessData.allComplete
    val hasIncomplete = readinessData.hasIncompleteMonitors

    val cardColor by animateColorAsState(
        targetValue = when {
            isComplete -> colors.gaugeGreen
            hasIncomplete -> colors.gaugeOrange
            else -> colors.textDim
        },
        animationSpec = tween(300),
        label = "card_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "readiness_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (hasIncomplete && !isComplete) { 0.8f } else { 0.3f },
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (hasIncomplete && !isComplete) {
                    Modifier.border(
                        width = 1.dp,
                        color = colors.gaugeOrange.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else { Modifier }
            ),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = cardColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.readiness_monitors),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = cardColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${readinessData.completedCount}/${readinessData.supportedCount}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = cardColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.complete_short),
                            fontSize = 10.sp,
                            color = cardColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProgressBarWithLabels(
                progress = readinessData.progressPercent,
                color = cardColor,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (readinessData.milOn) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.gaugeRed.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = colors.gaugeRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.mil_on_warning, readinessData.dtcCount),
                            fontSize = 12.sp,
                            color = colors.gaugeRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(readinessData.monitors) { monitor ->
                    MonitorChip(monitor = monitor, colors = colors)
                }
            }
        }
    }
}

@Composable
private fun ProgressBarWithLabels(
    progress: Float,
    color: Color,
    colors: AppColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.overall_progress),
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colors.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.7f),
                                color
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.not_ready_short), fontSize = 9.sp, color = colors.textDim)
            Text(stringResource(R.string.ready), fontSize = 9.sp, color = colors.textDim)
        }
    }
}

@Composable
private fun MonitorChip(
    monitor: ReadinessMonitorEntry,
    colors: AppColors
) {
    val chipColor = when (monitor.status) {
        MonitorSupport.COMPLETE -> colors.gaugeGreen
        MonitorSupport.INCOMPLETE -> colors.gaugeOrange
        MonitorSupport.NOT_SUPPORTED -> colors.textDim
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = chipColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(chipColor, CircleShape)
            )
            Text(
                text = monitor.name,
                fontSize = 10.sp,
                color = when (monitor.status) {
                    MonitorSupport.NOT_SUPPORTED -> colors.textDim
                    else -> colors.textPrimary
                }
            )
            if (monitor.status != MonitorSupport.NOT_SUPPORTED) {
                Icon(
                    imageVector = when (monitor.status) {
                        MonitorSupport.COMPLETE -> Icons.Filled.Check
                        else -> Icons.Filled.Close
                    },
                    contentDescription = null,
                    tint = chipColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun ReadinessMonitorCardCompact(
    readinessData: ReadinessMonitorData,
    modifier: Modifier = Modifier,
    colors: AppColors = LocalAppColors.current
) {
    val isComplete = readinessData.allComplete
    val cardColor = when {
        isComplete -> colors.gaugeGreen
        readinessData.hasIncompleteMonitors -> colors.gaugeOrange
        else -> colors.textDim
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = cardColor,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.readiness),
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "${readinessData.completedCount}/${readinessData.supportedCount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            ReadinessMiniProgress(
                progress = readinessData.progressPercent,
                color = cardColor,
                colors = colors
            )
        }
    }
}

@Composable
private fun ReadinessMiniProgress(
    progress: Float,
    color: Color,
    colors: AppColors
) {
    Box(
        modifier = Modifier
            .size(48.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                color = colors.surfaceVariant,
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }

        Text(
            text = "${(progress * 100).toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
