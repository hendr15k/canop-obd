package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.ui.theme.AppColors
import com.canopobd.ui.theme.GaugeTypography
import com.canopobd.ui.theme.LocalAppColors

// ============================================================================
// DESIGN SYSTEM v2.0 — Reusable building blocks
// All new screens should use these components for visual consistency.
// ============================================================================

// --- Spacing tokens -------------------------------------------------------
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
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

// ---------------------------------------------------------------------------
// GLASS CARD — the main container for content blocks.
// Slight gradient + subtle border + optional accent edge.
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
        // Left accent bar
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// METRIC TILE — compact value display (label + value + unit + optional sub)
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
                modifier = Modifier.weight(1f)
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
                color = color
            )
        }
    }
}

// ---------------------------------------------------------------------------
// QUICK TILE — large icon + label for menu/dashboard entry points
// ---------------------------------------------------------------------------
@Composable
fun QuickTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    isActive: Boolean = false,
    badgeColor: Color? = null,
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
            .padding(vertical = 12.dp, horizontal = 6.dp)
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
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// GRADIENT BUTTON — primary CTA with accent gradient
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
// HORIZONTAL DIVIDER with optional label
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
// DATA ROW — label / value table row (for info lists)
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
// STATUS BAR — top header for the app (connection state, status pills)
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
// EMPTY STATE — shown for unconnected / no-data scenarios
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
                .size(72.dp)
                .clip(CircleShape)
                .background(resolvedAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolvedAccent,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            GradientButton(text = actionLabel, onClick = onAction, icon = Icons.Filled.ArrowForward)
        }
    }
}

// --- Helper for animated color changes -----------------------------------
@Composable
fun animateColor(target: Color): Color {
    return animateColorAsState(target, animationSpec = tween(300), label = "anim_color").value
}
