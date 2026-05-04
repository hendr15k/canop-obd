package com.canopobd.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Primary palette — automotive dark
val canopoDark = Color(0xFF1A1A2E)
val canopoSurface = Color(0xFF16213E)
val canopoPrimary = Color(0xFF0F4C75)
val canopoAccent = Color(0xFF3282B8)
val canopoHighlight = Color(0xFF00D9FF)

// Gauge colors
val gaugeRed = Color(0xFFFF4444)
val gaugeOrange = Color(0xFFFF8800)
val gaugeYellow = Color(0xFFFFDD00)
val gaugeGreen = Color(0xFF44FF88)
val gaugeCyan = Color(0xFF00DDFF)

// Text
val textPrimary = Color(0xFFFFFFFF)
val textSecondary = Color(0xFFB0B0B0)
val textDim = Color(0xFF606060)

data class AppColors(
    val dark: Color,
    val surface: Color,
    val primary: Color,
    val accent: Color,
    val highlight: Color,
    val gaugeRed: Color,
    val gaugeOrange: Color,
    val gaugeYellow: Color,
    val gaugeGreen: Color,
    val gaugeCyan: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDim: Color
)

val DefaultAppColors = AppColors(
    dark = canopoDark,
    surface = canopoSurface,
    primary = canopoPrimary,
    accent = canopoAccent,
    highlight = canopoHighlight,
    gaugeRed = Color(0xFFFF4444),
    gaugeOrange = Color(0xFFFF8800),
    gaugeYellow = Color(0xFFFFDD00),
    gaugeGreen = Color(0xFF44FF88),
    gaugeCyan = Color(0xFF00DDFF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0B0B0),
    textDim = Color(0xFF606060)
)

fun androidx.compose.ui.graphics.Color.toAppColor(): Color = this

fun com.canopobd.data.model.ColorTheme.toAppColors(): AppColors = AppColors(
    dark = Color(primaryColor),
    surface = Color(surfaceColor),
    primary = Color(primaryColor),
    accent = Color(accentColor),
    highlight = Color(primaryColor),
    gaugeRed = Color(this.gaugeRed),
    gaugeOrange = Color(this.gaugeOrange),
    gaugeYellow = Color(this.gaugeYellow),
    gaugeGreen = Color(this.gaugeGreen),
    gaugeCyan = Color(0xFF00DDFF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0B0B0),
    textDim = Color(0xFF606060)
)

val LocalAppColors = compositionLocalOf { DefaultAppColors }
