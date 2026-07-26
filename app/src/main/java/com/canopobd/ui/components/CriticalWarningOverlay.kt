package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.R
import com.canopobd.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

/**
 * CriticalWarningOverlay - Überlagerung für kritische Warnungen (A14NET)
 *
 * Vollbild-Overlay für kritische Zustände mit:
 * - Warnsymbol und Meldung auf Deutsch
 * - Empfohlene Maßnahme
 * - Schließen-Button
 * - Automatisches Ausblenden nach 10 Sekunden für nicht-kritische Warnungen
 */
@Composable
fun CriticalWarningOverlay(
    isVisible: Boolean,
    severity: WarningSeverity,
    message: String,
    recommendedAction: String,
    modifier: Modifier = Modifier,
    isCritical: Boolean = true,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val colors = LocalAppColors.current

    val overlayColor = when (severity) {
        WarningSeverity.CRITICAL -> colors.gaugeRed
        WarningSeverity.HIGH -> colors.gaugeOrange
        WarningSeverity.MEDIUM -> colors.gaugeYellow
        WarningSeverity.LOW -> colors.gaugeCyan
    }

    val infiniteTransition = rememberInfiniteTransition(label = "overlay_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "overlay_pulse_alpha"
    )

    val animatedOverlayColor by animateColorAsState(
        targetValue = overlayColor,
        animationSpec = tween(300),
        label = "overlay_color"
    )

    // Auto-dismiss timer for non-critical warnings
    val autoDismissTimeMs = if (!isCritical) 10_000L else 0L
    LaunchedEffect(isVisible, isCritical) {
        if (autoDismissTimeMs > 0) {
            delay(autoDismissTimeMs)
            onDismiss()
        }
    }

    // Full-screen overlay
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Warning icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(animatedOverlayColor.copy(alpha = pulseAlpha * 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = animatedOverlayColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Severity badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = animatedOverlayColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = severity.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedOverlayColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Warning message
            Text(
                text = message,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recommended action
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = animatedOverlayColor.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = animatedOverlayColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = recommendedAction,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dismiss button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedOverlayColor.copy(alpha = 0.3f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.overlay_dismiss),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Auto-dismiss countdown hint (for non-critical)
            if (!isCritical) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.overlay_auto_dismiss),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

enum class WarningSeverity(val label: String) {
    CRITICAL("KRITISCH"),
    HIGH("HOCH"),
    MEDIUM("MITTEL"),
    LOW("NIEDRIG")
}
