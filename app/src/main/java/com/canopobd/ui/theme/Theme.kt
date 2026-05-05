package com.canopobd.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun CanopObdTheme(
    appColors: AppColors = DefaultAppColors,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = appColors.accent,
        secondary = appColors.highlight,
        tertiary = appColors.secondary,
        background = appColors.dark,
        surface = appColors.surface,
        surfaceVariant = appColors.surfaceVariant,
        onPrimary = appColors.textPrimary,
        onSecondary = appColors.textPrimary,
        onTertiary = appColors.textPrimary,
        onBackground = appColors.textPrimary,
        onSurface = appColors.textPrimary,
        onSurfaceVariant = appColors.textSecondary,
        error = appColors.gaugeRed,
        onError = appColors.textPrimary
    )
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.dark.toArgb()
            window.navigationBarColor = appColors.dark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalAppColors provides appColors
            ) {
                content()
            }
        }
    )
}
