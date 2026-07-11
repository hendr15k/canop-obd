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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.data.model.*
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.LocalAppColors

@Composable
fun EmissionsStatusCard(
    readiness: ReadinessMonitorData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: AppColors = LocalAppColors.current
) {
    val isReadyForInspection = readiness.allComplete && !readiness.milOn
    val cardColor by animateColorAsState(
        targetValue = when {
            readiness.milOn -> colors.gaugeRed
            isReadyForInspection -> colors.gaugeGreen
            readiness.hasIncompleteMonitors -> colors.gaugeOrange
            else -> colors.textDim
        },
        animationSpec = tween(300),
        label = "emissions_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "emissions_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (readiness.milOn) 0.8f else 0.3f,
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
                if (readiness.milOn || !isReadyForInspection) {
                    Modifier.border(
                        width = 1.dp,
                        color = cardColor.copy(alpha = pulseAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
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
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = cardColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.emissions_status),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                InspectionStatusBadge(
                    isReady = isReadyForInspection,
                    hasMil = readiness.milOn,
                    color = cardColor,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EmissionsProgressRing(
                    progress = readiness.progressPercent,
                    color = cardColor,
                    colors = colors
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.readiness_progress),
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ReadinessStatRow(
                        label = stringResource(R.string.complete),
                        value = readiness.completedCount.toString(),
                        color = colors.gaugeGreen,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    ReadinessStatRow(
                        label = stringResource(R.string.incomplete),
                        value = readiness.incompleteCount.toString(),
                        color = colors.gaugeOrange,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    ReadinessStatRow(
                        label = stringResource(R.string.not_supported),
                        value = (readiness.totalCount - readiness.supportedCount).coerceAtLeast(0).toString(),
                        color = colors.textDim,
                        colors = colors
                    )
                }
            }

            if (readiness.milOn) {
                Spacer(modifier = Modifier.height(12.dp))
                MILWarningSection(
                    dtcCount = readiness.dtcCount,
                    colors = colors
                )
            } else if (isReadyForInspection) {
                Spacer(modifier = Modifier.height(12.dp))
                ReadyForInspectionSection(colors = colors)
            } else if (readiness.hasIncompleteMonitors) {
                Spacer(modifier = Modifier.height(12.dp))
                NotReadySection(
                    incompleteCount = readiness.incompleteCount,
                    colors = colors
                )
            }

            if (readiness.monitors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MonitorSummaryRow(
                    monitors = readiness.monitors,
                    colors = colors
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun InspectionStatusBadge(
    isReady: Boolean,
    hasMil: Boolean,
    color: Color,
    colors: AppColors
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    hasMil -> Icons.Filled.Warning
                    isReady -> Icons.Filled.CheckCircle
                    else -> Icons.Filled.Schedule
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    hasMil -> stringResource(R.string.mil_active)
                    isReady -> stringResource(R.string.ready_for_inspection)
                    else -> stringResource(R.string.not_ready)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun EmissionsProgressRing(
    progress: Float,
    color: Color,
    colors: AppColors
) {
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            drawCircle(
                color = colors.surfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        color.copy(alpha = 0.4f),
                        color
                    )
                ),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = stringResource(R.string.ready),
                fontSize = 9.sp,
                color = colors.textDim
            )
        }
    }
}

@Composable
private fun ReadinessStatRow(
    label: String,
    value: String,
    color: Color,
    colors: AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MILWarningSection(
    dtcCount: Int,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.gaugeRed.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = colors.gaugeRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.mil_active),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gaugeRed
                )
                Text(
                    text = stringResource(R.string.dtc_count_warning, dtcCount),
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ReadyForInspectionSection(colors: AppColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.gaugeGreen.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = colors.gaugeGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.ready_for_inspection),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gaugeGreen
                )
                Text(
                    text = stringResource(R.string.inspection_hint),
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun NotReadySection(
    incompleteCount: Int,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.gaugeOrange.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = colors.gaugeOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.driving_cycle_required),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.gaugeOrange
                )
                Text(
                    text = stringResource(R.string.incomplete_monitors_hint, incompleteCount),
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun MonitorSummaryRow(
    monitors: List<ReadinessMonitorEntry>,
    colors: AppColors
) {
    Column {
        Text(
            text = stringResource(R.string.monitor_overview),
            fontSize = 10.sp,
            color = colors.textDim
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(monitors) { monitor ->
                MonitorDot(
                    monitor = monitor,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun MonitorDot(
    monitor: ReadinessMonitorEntry,
    colors: AppColors
) {
    val dotColor = when (monitor.status) {
        MonitorSupport.COMPLETE -> colors.gaugeGreen
        MonitorSupport.INCOMPLETE -> colors.gaugeOrange
        MonitorSupport.NOT_SUPPORTED -> colors.textDim
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(dotColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (monitor.status) {
                MonitorSupport.COMPLETE -> Icons.Filled.Check
                MonitorSupport.INCOMPLETE -> Icons.Filled.Close
                MonitorSupport.NOT_SUPPORTED -> Icons.Filled.Remove
            },
            contentDescription = null,
            tint = dotColor,
            modifier = Modifier.size(14.dp)
        )
    }
}
