package com.canopobd.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = canopoAccent,
    secondary = canopoHighlight,
    tertiary = canopoSecondary,
    background = canopoDark,
    surface = canopoSurface,
    surfaceVariant = canopoSurfaceVariant,
    onPrimary = textPrimary,
    onSecondary = textPrimary,
    onTertiary = textPrimary,
    onBackground = textPrimary,
    onSurface = textPrimary,
    onSurfaceVariant = textSecondary,
    error = gaugeRed,
    onError = textPrimary
)

@Composable
fun CanopObdTheme(
    appColors: AppColors = DefaultAppColors,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
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
