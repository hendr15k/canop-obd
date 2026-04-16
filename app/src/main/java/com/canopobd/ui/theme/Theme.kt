package com.canopobd.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = canopoAccent,
    secondary = canopoHighlight,
    tertiary = canopoPrimary,
    background = canopoDark,
    surface = canopoSurface,
    onPrimary = textPrimary,
    onSecondary = textPrimary,
    onTertiary = textPrimary,
    onBackground = textPrimary,
    onSurface = textPrimary,
    surfaceVariant = Color(0xFF0D0D1A),
    onSurfaceVariant = textSecondary,
    error = gaugeRed,
    onError = Color.White
)

@Composable
fun canop-obdTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = canopoDark.toArgb()
            window.navigationBarColor = canopoDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
