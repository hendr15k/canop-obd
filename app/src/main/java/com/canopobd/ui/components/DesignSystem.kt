package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.GaugeTypography
import com.canopobd.ui.theme.LocalAppColors

// ============================================================================
// DESIGN SYSTEM v2.0 — Comprehensive set of reusable building blocks.
// Every new screen and dialog should use these for visual consistency.
// ============================================================================

// --- Spacing tokens -------------------------------------------------------
object Spacing {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

// --- Card radii -----------------------------------------------------------
object AppRadius {
    val xs = 4.dp
    val sm = 6.dp
    val md = 10.dp
    val lg = 14.dp
    val xl = 20.dp
    val pill = 999.dp
}

// --- Spacing helpers -----------------------------------------------------
@Composable
fun ColumnScope.Gap(height: Dp = Spacing.md) = Spacer(modifier = Modifier.height(height))
@Composable
fun RowScope.Gap(width: Dp = Spacing.md) = Spacer(modifier = Modifier.width(width))

// ============================================================================
// CONTAINERS
// ============================================================================

// ---------------------------------------------------------------------------
// GLASS CARD — the main container for content blocks.
// ---------------------------------------------------------------------------
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentEdge: Color? = null,
    background: Brush? = null,
    border: BorderStroke? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(AppRadius.lg),
    padding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    val resolvedBg = background ?: colors.gradientCard
    val resolvedBorder = border
        ?: if (accentEdge != null) BorderStroke(1.dp, accentEdge.copy(alpha = 0.35f))
        else BorderStroke(1.dp, colors.borderSubtle)

    val containerMod = modifier
        .clip(shape)
        .background(colors.surfaceBase)
        .background(resolvedBg)
        .border(resolvedBorder, shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Surface(
        modifier = containerMod,
        color = Color.Transparent,
        shape = shape,
        contentColor = colors.textPrimary
    ) {
        Column(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// FLAT CARD — solid surface card, no gradient
// ---------------------------------------------------------------------------
@Composable
fun FlatCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(AppRadius.lg),
    padding: Dp = 14.dp,
    border: BorderStroke? = null,
    backgroundColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    val containerMod = modifier
        .clip(shape)
        .background(backgroundColor ?: colors.surfaceBase)
        .border(border ?: BorderStroke(1.dp, colors.borderSubtle), shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Surface(
        modifier = containerMod,
        color = Color.Transparent,
        shape = shape,
        contentColor = colors.textPrimary
    ) {
        Column(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// ACCENT CARD — colored top-bar indicator + glass card body
// ---------------------------------------------------------------------------
@Composable
fun AccentCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    onClick: (() -> Unit)? = null,
    padding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.lg))
            .background(colors.surfaceBase)
            .background(colors.gradientCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.lg))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.3f))
                    )
                )
        )
        Column(modifier = Modifier.padding(start = padding + 2.dp, end = padding, top = padding, bottom = padding)) {
            content()
        }
    }
}

// ============================================================================
// SECTION HEADERS
// ============================================================================

// ---------------------------------------------------------------------------
// SECTION HEADER — uppercase label with optional icon & "see all" action
// ---------------------------------------------------------------------------
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(AppRadius.xs))
                    .background(colors.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(AppRadius.xs))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// INLINE SECTION HEADER — for use inside cards
// ---------------------------------------------------------------------------
@Composable
fun InlineSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) trailing()
    }
}

// ============================================================================
// STATUS INDICATORS
// ============================================================================

// ---------------------------------------------------------------------------
// STATUS DOT — animated pulse for connection/live indicators
// ---------------------------------------------------------------------------
@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    pulse: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "status_pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val resolvedColor = if (pulse) color.copy(alpha = alpha) else color
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(resolvedColor)
    )
}

// ---------------------------------------------------------------------------
// PILL BADGE — small label with status color
// ---------------------------------------------------------------------------
@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    icon: ImageVector? = null
) {
    val bg = if (filled) color.copy(alpha = 0.18f) else Color.Transparent
    val border = if (filled) BorderStroke(1.dp, color.copy(alpha = 0.4f)) else BorderStroke(1.dp, color.copy(alpha = 0.6f))
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadius.pill),
        color = bg,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CHIP — small label with icon
// ---------------------------------------------------------------------------
@Composable
fun Chip(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val colors = LocalAppColors.current
    val resolvedAccent = accentColor ?: colors.primary
    val bg = if (selected) resolvedAccent.copy(alpha = 0.18f) else colors.surfaceRaised
    val border = if (selected) BorderStroke(1.dp, resolvedAccent.copy(alpha = 0.5f)) else BorderStroke(1.dp, colors.borderSubtle)
    val textColor = if (selected) resolvedAccent else colors.textSecondary

    Surface(
        modifier = modifier
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(AppRadius.pill),
        color = bg,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================================
// DATA DISPLAY COMPONENTS
// ============================================================================

// ---------------------------------------------------------------------------
// METRIC TILE — compact value display
// ---------------------------------------------------------------------------
@Composable
fun MetricTile(
    label: String,
    value: String,
    unit: String = "",
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    subValue: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val resolvedAccent = accentColor ?: colors.primary

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = resolvedAccent,
        padding = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedAccent,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = GaugeTypography.valueMedium,
                color = resolvedAccent,
                maxLines = 1
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        if (subValue != null) {
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------
// HERO STAT — large value display with label & unit
// ---------------------------------------------------------------------------
@Composable
fun HeroStat(
    label: String,
    value: String,
    unit: String = "",
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    icon: ImageVector? = null,
    status: String? = null,
    statusColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val resolvedAccent = accentColor ?: colors.primary
    val resolvedStatusColor = statusColor ?: resolvedAccent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.lg))
            .background(colors.surfaceRaised.copy(alpha = 0.5f))
            .border(1.dp, resolvedAccent.copy(alpha = 0.35f), RoundedCornerShape(AppRadius.lg))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = resolvedAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (status != null) {
                    StatusPill(text = status, color = resolvedStatusColor)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = GaugeTypography.valueXL,
                    color = resolvedAccent
                )
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DATA ROW — label / value table row
// ---------------------------------------------------------------------------
@Composable
fun DataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
    labelColor: Color? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor ?: colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = GaugeTypography.valueSmall,
            color = valueColor ?: colors.textPrimary
        )
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

// ---------------------------------------------------------------------------
// DIVIDER LINE
// ---------------------------------------------------------------------------
@Composable
fun DividerLine(
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color ?: colors.borderSubtle)
    )
}

// ---------------------------------------------------------------------------
// HORIZONTAL DIVIDER WITH LABEL (e.g. "ODER")
// ---------------------------------------------------------------------------
@Composable
fun DividerWithLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.borderSubtle)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.borderSubtle)
        )
    }
}

// ---------------------------------------------------------------------------
// KEY-VALUE BLOCK — vertical stack
// ---------------------------------------------------------------------------
@Composable
fun KeyValueBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.textPrimary
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised.copy(alpha = 0.5f))
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(12.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = GaugeTypography.valueMedium,
            color = resolved,
            maxLines = 1
        )
    }
}

// ============================================================================
// PROGRESS / GAUGE COMPONENTS
// ============================================================================

// ---------------------------------------------------------------------------
// PROGRESS BAR — thin horizontal progress with gradient
// ---------------------------------------------------------------------------
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color? = null,
    trackColor: Color? = null
) {
    val colors = LocalAppColors.current
    val resolvedColor = color ?: colors.primary
    val resolvedTrack = trackColor ?: resolvedColor.copy(alpha = 0.15f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "progress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(resolvedTrack)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(resolvedColor)
        )
    }
}

// ---------------------------------------------------------------------------
// LINEAR GAUGE — value with min/max labels
// ---------------------------------------------------------------------------
@Composable
fun LinearGauge(
    label: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    unit: String = "",
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    valueLabel: String? = null,
    warningThreshold: Float? = null,
    criticalThreshold: Float? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    val resolvedColor = accentColor ?: colors.primary
    val range = (maxValue - minValue).coerceAtLeast(0.001f)
    val fraction = ((value - minValue) / range).coerceIn(0f, 1f)

    val resolvedValueColor = when {
        criticalThreshold != null && value >= criticalThreshold -> colors.critical
        warningThreshold != null && value >= warningThreshold -> colors.warning
        else -> resolvedColor
    }

    GlassCard(
        modifier = modifier,
        onClick = onClick,
        accentEdge = resolvedValueColor,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueLabel ?: formatValue(value),
                style = GaugeTypography.valueSmall,
                color = resolvedValueColor
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        GradientProgressBar(
            progress = fraction,
            color = resolvedValueColor,
            height = 6.dp
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatValue(minValue),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
            Text(
                text = formatValue(maxValue),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
        }
    }
}

private fun formatValue(v: Float): String =
    if (v == v.toInt().toFloat()) v.toInt().toString() else "%.1f".format(v)

// ---------------------------------------------------------------------------
// PROGRESS RING — circular progress indicator with center text
// ---------------------------------------------------------------------------
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp,
    color: Color? = null,
    trackColor: Color? = null,
    centerText: String? = null,
    centerSubText: String? = null,
    centerTextColor: Color? = null
) {
    val colors = LocalAppColors.current
    val resolvedColor = color ?: colors.primary
    val resolvedTrack = trackColor ?: resolvedColor.copy(alpha = 0.15f)
    val resolvedCenter = centerTextColor ?: resolvedColor
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "ring_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = androidx.compose.ui.geometry.Size(
                size.toPx() - stroke,
                size.toPx() - stroke
            )
            val topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
            // Track
            drawArc(
                color = resolvedTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // Progress
            drawArc(
                brush = Brush.sweepGradient(listOf(resolvedColor, resolvedColor)),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        if (centerText != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerText,
                    style = GaugeTypography.valueSmall,
                    color = resolvedCenter,
                    fontWeight = FontWeight.Bold
                )
                if (centerSubText != null) {
                    Text(
                        text = centerSubText,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// METER — vertical bar meter with mark
// ---------------------------------------------------------------------------
@Composable
fun VerticalBarMeter(
    value: Float,
    minValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    width: Dp = 12.dp,
    color: Color? = null,
    trackColor: Color? = null,
    warningThreshold: Float? = null,
    criticalThreshold: Float? = null
) {
    val colors = LocalAppColors.current
    val resolvedColor = color ?: colors.primary
    val resolvedTrack = trackColor ?: resolvedColor.copy(alpha = 0.15f)
    val range = (maxValue - minValue).coerceAtLeast(0.001f)
    val fraction = ((value - minValue) / range).coerceIn(0f, 1f)

    val resolvedFill = when {
        criticalThreshold != null && value >= criticalThreshold -> colors.critical
        warningThreshold != null && value >= warningThreshold -> colors.warning
        else -> resolvedColor
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(width / 2))
            .background(resolvedTrack)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(resolvedFill, resolvedFill.copy(alpha = 0.6f))
                    )
                )
                .align(Alignment.BottomCenter)
        )
    }
}

// ============================================================================
// BUTTONS & CONTROLS
// ============================================================================

// ---------------------------------------------------------------------------
// GRADIENT BUTTON — primary CTA
// ---------------------------------------------------------------------------
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    gradient: Brush? = null
) {
    val colors = LocalAppColors.current
    val resolvedGradient = gradient ?: colors.gradientAccent
    val finalGradient = if (enabled) resolvedGradient else Brush.horizontalGradient(
        colors = listOf(colors.surfaceRaised, colors.surfaceElevated)
    )
    val textColor = if (enabled) colors.surfaceBlack else colors.textMuted

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(AppRadius.md))
            .background(finalGradient)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

// ---------------------------------------------------------------------------
// OUTLINE BUTTON — secondary action
// ---------------------------------------------------------------------------
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = LocalAppColors.current
    val resolvedAccent = accentColor ?: colors.primary
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(AppRadius.md))
            .border(
                width = 1.dp,
                color = if (enabled) resolvedAccent.copy(alpha = 0.6f) else colors.borderSubtle,
                shape = RoundedCornerShape(AppRadius.md)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) resolvedAccent else colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) resolvedAccent else colors.textMuted
            )
        }
    }
}

// ---------------------------------------------------------------------------
// ICON BUTTON — circular
// ---------------------------------------------------------------------------
@Composable
fun IconButtonBox(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    size: Dp = 36.dp,
    contentDescription: String? = null
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.primary
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(colors.surfaceRaised)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolved,
            modifier = Modifier.size(size / 2)
        )
    }
}

// ---------------------------------------------------------------------------
// QUICK TILE — large icon + label for menu/dashboard entry points
// ---------------------------------------------------------------------------
@Suppress("UNUSED_PARAMETER")
@Composable
fun QuickTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    isActive: Boolean = false,
    badgeColor: Color? = null,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val resolvedAccent = accentColor ?: colors.primary
    val activeBg = if (isActive) resolvedAccent.copy(alpha = 0.14f) else Color.Transparent
    val activeBorder = if (isActive) resolvedAccent.copy(alpha = 0.5f) else colors.borderSubtle

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceBase.copy(alpha = 0.5f))
            .background(activeBg)
            .border(1.dp, activeBorder, RoundedCornerShape(AppRadius.md))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(AppRadius.sm))
                        .background(resolvedAccent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = resolvedAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (badgeColor != null) {
                    StatusDot(
                        color = badgeColor,
                        size = 8.dp,
                        pulse = true,
                        modifier = Modifier.padding(top = 1.dp, end = 1.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) resolvedAccent else colors.textSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================================================
// STEPPER / SLIDER / TOGGLE
// ============================================================================

// ---------------------------------------------------------------------------
// STEPPER — +/- numeric stepper
// ---------------------------------------------------------------------------
@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 100,
    step: Int = 1,
    accentColor: Color? = null,
    label: String? = null,
    unit: String = ""
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.primary
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
        }
        StepperButton(
            icon = Icons.Filled.Remove,
            accentColor = resolved,
            enabled = value > min
        ) { onValueChange((value - step).coerceAtLeast(min)) }
        Spacer(Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value.toString(),
                style = GaugeTypography.valueMedium,
                color = colors.textPure,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotEmpty()) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        StepperButton(
            icon = Icons.Filled.Add,
            accentColor = resolved,
            enabled = value < max
        ) { onValueChange((value + step).coerceAtMost(max)) }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    accentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (enabled) accentColor.copy(alpha = 0.16f) else colors.surfaceElevated)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) accentColor else colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// SLIDER ROW — label + slider + value
// ---------------------------------------------------------------------------
@Composable
fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 100f,
    unit: String = "",
    accentColor: Color? = null,
    valueFormatter: ((Float) -> String)? = null
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.primary
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = (valueFormatter?.invoke(value) ?: "%.0f".format(value)) + (if (unit.isNotEmpty()) " $unit" else ""),
                style = GaugeTypography.valueSmall,
                color = resolved,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        androidx.compose.material3.Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = resolved,
                activeTrackColor = resolved,
                inactiveTrackColor = colors.surfaceRaised
            )
        )
    }
}

// ============================================================================
// TAB BAR / SEGMENTED CONTROL
// ============================================================================

// ---------------------------------------------------------------------------
// SEGMENTED CONTROL
// ---------------------------------------------------------------------------
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            val activeBrush: Brush = if (isSelected) colors.gradientAccent
            else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(activeBrush)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) colors.surfaceBlack else colors.textSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB BAR
// ---------------------------------------------------------------------------
@Composable
fun TabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceBase)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) resolved else colors.textTertiary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(2.dp)
                        .background(
                            if (isSelected) resolved else Color.Transparent,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

// ============================================================================
// LIST ITEMS
// ============================================================================

// ---------------------------------------------------------------------------
// LIST ITEM — icon + content + trailing
// ---------------------------------------------------------------------------
@Composable
fun ListItemBox(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    accentColor: Color? = null
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = GaugeTypography.valueSmall,
                color = resolved
            )
            Spacer(Modifier.width(6.dp))
        }
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ============================================================================
// MISC
// ============================================================================

// ---------------------------------------------------------------------------
// STATUS BAR
// ---------------------------------------------------------------------------
@Composable
fun StatusBar(
    connectionState: String,
    connectionColor: Color,
    modifier: Modifier = Modifier,
    rightContent: @Composable RowScope.() -> Unit = {}
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surfaceBase,
        contentColor = colors.textPrimary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(color = connectionColor, size = 8.dp, pulse = true)
            Spacer(Modifier.width(8.dp))
            Text(
                text = connectionState,
                style = MaterialTheme.typography.labelLarge,
                color = connectionColor,
                modifier = Modifier.weight(1f)
            )
            rightContent()
        }
    }
}

// ---------------------------------------------------------------------------
// EMPTY STATE
// ---------------------------------------------------------------------------
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val colors = LocalAppColors.current
    val resolvedAccent = accentColor ?: colors.primary
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(resolvedAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(resolvedAccent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedAccent,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            GradientButton(text = actionLabel, onClick = onAction, icon = Icons.AutoMirrored.Filled.ArrowForward)
        }
    }
}

// ---------------------------------------------------------------------------
// SCORE PILL — circular score with color
// ---------------------------------------------------------------------------
@Composable
fun ScorePill(
    score: Int,
    maxScore: Int = 100,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val colors = LocalAppColors.current
    val pct = score.toFloat() / maxScore
    val color = when {
        pct >= 0.85f -> colors.success
        pct >= 0.6f -> colors.primary
        pct >= 0.4f -> colors.warning
        else -> colors.critical
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(1.5.dp, color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = GaugeTypography.valueSmall,
                color = color,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "/$maxScore",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontSize = 8.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// TREND INDICATOR — up/down arrow with color
// ---------------------------------------------------------------------------
@Composable
fun TrendIndicator(
    delta: Float,
    modifier: Modifier = Modifier,
    unit: String = "%",
    inverted: Boolean = false // true = lower is better (e.g. fuel consumption)
) {
    val colors = LocalAppColors.current
    val isPositive = if (inverted) delta < 0 else delta > 0
    val color = when {
        delta == 0f -> colors.textTertiary
        isPositive -> colors.success
        else -> colors.critical
    }
    val arrow = if (delta > 0) "▲" else if (delta < 0) "▼" else "■"
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = arrow,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = "%.1f%s".format(kotlin.math.abs(delta), unit),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// DIALOG SHELL — reusable dialog wrapper with new design
// ============================================================================

// ---------------------------------------------------------------------------
// DIALOG SHELL — top-level wrapper for all dialogs
// ---------------------------------------------------------------------------
@Composable
fun DialogShell(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    eyebrow: String? = null,
    showCloseButton: Boolean = true,
    heightFraction: Float = 0.85f,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(heightFraction),
            shape = RoundedCornerShape(AppRadius.lg),
            color = colors.surfaceDeep
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (title != null || showCloseButton) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.gradientSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (eyebrow != null) {
                                    Text(
                                        text = eyebrow.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textTertiary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(2.dp))
                                }
                                if (title != null) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = colors.textPure,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (showCloseButton) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(AppRadius.sm))
                                        .background(colors.surfaceRaised)
                                        .clickable(onClick = onDismiss),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Schließen",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    DividerLine()
                }
                content()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TOGGLE ROW — settings-style switch row
// ---------------------------------------------------------------------------
@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    accentColor: Color? = null
) {
    val colors = LocalAppColors.current
    val resolved = accentColor ?: colors.success
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.md))
            .background(colors.surfaceRaised)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(AppRadius.md))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = resolved,
                checkedTrackColor = resolved.copy(alpha = 0.35f),
                checkedBorderColor = resolved,
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.surfaceElevated,
                uncheckedBorderColor = colors.borderDefault
            )
        )
    }
}

// --- Helper for animated color changes -----------------------------------
@Composable
fun animateColor(target: Color): Color {
    return animateColorAsState(target, animationSpec = tween(300), label = "anim_color").value
}
