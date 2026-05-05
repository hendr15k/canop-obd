package com.canopobd.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Primary palette — modern automotive dark
val canopoDark = Color(0xFF090910)
val canopoSurface = Color(0xFF12121C)
val canopoSurfaceVariant = Color(0xFF1A1A28)
val canopoPrimary = Color(0xFF1E40AF)
val canopoAccent = Color(0xFF60A5FA)
val canopoHighlight = Color(0xFF38BDF8)
val canopoSecondary = Color(0xFF818CF8)

// Status colors
val gaugeRed = Color(0xFFEF4444)
val gaugeOrange = Color(0xFFF97316)
val gaugeYellow = Color(0xFFFBBF24)
val gaugeGreen = Color(0xFF22C55E)
val gaugeCyan = Color(0xFF06B6D4)

// Gauge gradient colors
val gaugeRedGlow = Color(0xFFDC2626)
val gaugeOrangeGlow = Color(0xFFEA580C)
val gaugeGreenGlow = Color(0xFF16A34A)
val gaugeBlueGlow = Color(0xFF2563EB)

// Text colors
val textPrimary = Color(0xFFF8FAFC)
val textSecondary = Color(0xFF94A3B8)
val textDim = Color(0xFF475569)
val textMuted = Color(0xFF334155)

// Background surfaces
val surfaceCard = Color(0xFF0F0F18)
val surfaceElevated = Color(0xFF1A1A28)
val surfaceOverlay = Color(0xFF252535)

// Border colors
val borderSubtle = Color(0xFF1E293B)
val borderDefault = Color(0xFF334155)
val borderAccent = Color(0xFF60A5FA)

// Connection status colors
val connectionExcellent = gaugeGreen
val connectionGood = Color(0xFF4ADE80)
val connectionFair = gaugeYellow
val connectionPoor = gaugeRed

data class AppColors(
    val dark: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val accent: Color,
    val highlight: Color,
    val secondary: Color,
    val gaugeRed: Color,
    val gaugeOrange: Color,
    val gaugeYellow: Color,
    val gaugeGreen: Color,
    val gaugeCyan: Color,
    val gaugeRedGlow: Color,
    val gaugeOrangeGlow: Color,
    val gaugeGreenGlow: Color,
    val gaugeBlueGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDim: Color,
    val textMuted: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val surfaceOverlay: Color,
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderAccent: Color,
    val connectionExcellent: Color,
    val connectionGood: Color,
    val connectionFair: Color,
    val connectionPoor: Color
)

val DefaultAppColors = AppColors(
    dark = canopoDark,
    surface = canopoSurface,
    surfaceVariant = canopoSurfaceVariant,
    primary = canopoPrimary,
    accent = canopoAccent,
    highlight = canopoHighlight,
    secondary = canopoSecondary,
    gaugeRed = gaugeRed,
    gaugeOrange = gaugeOrange,
    gaugeYellow = gaugeYellow,
    gaugeGreen = gaugeGreen,
    gaugeCyan = gaugeCyan,
    gaugeRedGlow = gaugeRedGlow,
    gaugeOrangeGlow = gaugeOrangeGlow,
    gaugeGreenGlow = gaugeGreenGlow,
    gaugeBlueGlow = gaugeBlueGlow,
    textPrimary = textPrimary,
    textSecondary = textSecondary,
    textDim = textDim,
    textMuted = textMuted,
    surfaceCard = surfaceCard,
    surfaceElevated = surfaceElevated,
    surfaceOverlay = surfaceOverlay,
    borderSubtle = borderSubtle,
    borderDefault = borderDefault,
    borderAccent = borderAccent,
    connectionExcellent = connectionExcellent,
    connectionGood = connectionGood,
    connectionFair = connectionFair,
    connectionPoor = connectionPoor
)

fun androidx.compose.ui.graphics.Color.toAppColor(): Color = this

fun com.canopobd.data.model.ColorTheme.toAppColors(): AppColors = AppColors(
    dark = Color(primaryColor),
    surface = Color(surfaceColor),
    surfaceVariant = Color(surfaceColor).copy(alpha = 0.8f),
    primary = Color(primaryColor),
    accent = Color(accentColor),
    highlight = Color(primaryColor),
    secondary = Color(accentColor).copy(alpha = 0.8f),
    gaugeRed = Color(this.gaugeRed),
    gaugeOrange = Color(this.gaugeOrange),
    gaugeYellow = Color(this.gaugeYellow),
    gaugeGreen = Color(this.gaugeGreen),
    gaugeCyan = Color(0xFF06B6D4),
    gaugeRedGlow = Color(this.gaugeRed),
    gaugeOrangeGlow = Color(this.gaugeOrange),
    gaugeGreenGlow = Color(this.gaugeGreen),
    gaugeBlueGlow = Color(0xFF2563EB),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textDim = Color(0xFF475569),
    textMuted = Color(0xFF334155),
    surfaceCard = Color(primaryColor).copy(alpha = 0.1f),
    surfaceElevated = Color(primaryColor).copy(alpha = 0.15f),
    surfaceOverlay = Color(primaryColor).copy(alpha = 0.2f),
    borderSubtle = Color(primaryColor).copy(alpha = 0.3f),
    borderDefault = Color(primaryColor).copy(alpha = 0.5f),
    borderAccent = Color(accentColor),
    connectionExcellent = Color(this.gaugeGreen),
    connectionGood = Color(0xFF4ADE80),
    connectionFair = Color(this.gaugeYellow),
    connectionPoor = Color(this.gaugeRed)
)

val LocalAppColors = compositionLocalOf { DefaultAppColors }
