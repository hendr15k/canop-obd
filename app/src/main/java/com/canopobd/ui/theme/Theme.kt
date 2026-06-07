package com.canopobd.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.canopobd.data.model.AppThemeMode

// ============================================================================
// SHAPES — Geometric, modern, slightly rounded but with sharp edges
// ============================================================================
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

// ============================================================================
// MAIN THEME WRAPPER
// ============================================================================
@Composable
fun CanopObdTheme(
    appColors: AppColors = DefaultAppColors,
    appThemeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val effectiveDark = appThemeMode == AppThemeMode.DARK

    val colorScheme = if (effectiveDark) {
        darkColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.surfaceBlack,
            primaryContainer = appColors.primaryDeep,
            onPrimaryContainer = appColors.textPrimary,
            secondary = appColors.secondary,
            onSecondary = appColors.surfaceBlack,
            secondaryContainer = appColors.secondaryDeep,
            onSecondaryContainer = appColors.textPrimary,
            tertiary = appColors.accent,
            onTertiary = appColors.surfaceBlack,
            tertiaryContainer = appColors.accent,
            onTertiaryContainer = appColors.textPrimary,
            background = appColors.surfaceBlack,
            onBackground = appColors.textPrimary,
            surface = appColors.surfaceBase,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceRaised,
            onSurfaceVariant = appColors.textSecondary,
            surfaceTint = appColors.primary,
            inverseSurface = appColors.textPrimary,
            inverseOnSurface = appColors.surfaceBlack,
            inversePrimary = appColors.primaryDeep,
            error = appColors.critical,
            onError = appColors.textPrimary,
            errorContainer = appColors.critical,
            onErrorContainer = appColors.textPrimary,
            outline = appColors.borderDefault,
            outlineVariant = appColors.borderSubtle,
            scrim = appColors.surfaceOverlay
        )
    } else {
        lightColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.textPrimary,
            primaryContainer = appColors.primarySoft,
            onPrimaryContainer = appColors.surfaceBlack,
            secondary = appColors.secondary,
            onSecondary = appColors.textPrimary,
            secondaryContainer = appColors.secondarySoft,
            onSecondaryContainer = appColors.surfaceBlack,
            tertiary = appColors.accent,
            onTertiary = appColors.textPrimary,
            tertiaryContainer = appColors.accentSoft,
            onTertiaryContainer = appColors.surfaceBlack,
            background = appColors.surfaceBlack,
            onBackground = appColors.textPrimary,
            surface = appColors.surfaceBase,
            onSurface = appColors.textPrimary,
            surfaceVariant = appColors.surfaceRaised,
            onSurfaceVariant = appColors.textSecondary,
            surfaceTint = appColors.primary,
            inverseSurface = appColors.surfaceBlack,
            inverseOnSurface = appColors.textPrimary,
            inversePrimary = appColors.primaryDeep,
            error = appColors.critical,
            onError = appColors.textPrimary,
            errorContainer = appColors.criticalSoft,
            onErrorContainer = appColors.surfaceBlack,
            outline = appColors.borderDefault,
            outlineVariant = appColors.borderSubtle,
            scrim = appColors.surfaceOverlay
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.surfaceBlack.toArgb()
            window.navigationBarColor = appColors.surfaceBlack.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !effectiveDark
            insets.isAppearanceLightNavigationBars = !effectiveDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalAppColors provides appColors
            ) {
                content()
            }
        }
    )
}
