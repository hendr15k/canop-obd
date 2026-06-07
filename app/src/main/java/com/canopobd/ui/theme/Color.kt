package com.canopobd.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.canopobd.data.model.AppThemeMode

// ============================================================================
// CANOP-OBD DESIGN SYSTEM v2.0
// Style: "Modern Automotive Tech"
// Inspired by high-end vehicle HMI / performance telemetry interfaces
// ============================================================================

// --- Surface palette: layered depth with subtle blue undertone ---------------
private val SpaceBlackC = Color(0xFF05070D)
private val DeepSpaceC = Color(0xFF0A0E18)
private val CarbonFiberC = Color(0xFF11151F)
private val GraphiteDarkC = Color(0xFF161B27)
private val GraphiteC = Color(0xFF1C2230)
private val GraphiteLightC = Color(0xFF252C3A)
private val OverlayGlassC = Color(0xFF2D3548)

// --- Primary brand: Electric Cyan ------------------------------------------
private val ElectricCyanC = Color(0xFF00E5FF)
private val ElectricCyanSoftC = Color(0xFF4DE9FF)
private val ElectricCyanDeepC = Color(0xFF0097A7)
private val ElectricCyanGlowC = Color(0xFF18FFFF)

// --- Secondary: Plasma Blue -----------------------------------------------
private val PlasmaBlueC = Color(0xFF2979FF)
private val PlasmaBlueSoftC = Color(0xFF5B8DEF)
private val PlasmaBlueDeepC = Color(0xFF1565C0)

// --- Accent: Magenta/Neon Pink for highlights -----------------------------
private val NeonMagentaC = Color(0xFFE91E63)
private val NeonMagentaSoftC = Color(0xFFFF4081)

// --- Status colors with glow variants -------------------------------------
private val SignalRedC = Color(0xFFFF3D57)
private val SignalRedGlowC = Color(0xFFFF1744)
private val SignalRedSoftC = Color(0xFFFF6B7E)
private val SignalOrangeC = Color(0xFFFF9100)
private val SignalOrangeGlowC = Color(0xFFFF6D00)
private val SignalOrangeSoftC = Color(0xFFFFAB40)
private val SignalAmberC = Color(0xFFFFC107)
private val SignalAmberSoftC = Color(0xFFFFD54F)
private val SignalGreenC = Color(0xFF00E676)
private val SignalGreenGlowC = Color(0xFF00C853)
private val SignalGreenSoftC = Color(0xFF69F0AE)
private val SignalCyanC = Color(0xFF00BCD4)
private val SignalCyanSoftC = Color(0xFF4DD0E1)

// --- Text hierarchy --------------------------------------------------------
private val TextPureC = Color(0xFFF5F9FF)
private val TextPrimaryC = Color(0xFFE8EEF7)
private val TextSecondaryC = Color(0xFFA8B3C7)
private val TextTertiaryC = Color(0xFF6B7791)
private val TextMutedC = Color(0xFF4A5468)
private val TextDisabledC = Color(0xFF2F374A)

// --- Borders / dividers ----------------------------------------------------
private val BorderHairlineC = Color(0x14FFFFFF)
private val BorderSubtleC = Color(0x1FFFFFFF)
private val BorderDefaultC = Color(0x33FFFFFF)
private val BorderStrongC = Color(0x4DFFFFFF)
private val BorderAccentC = Color(0xFF00E5FF)
private val BorderAccentSoftC = Color(0x6600E5FF)

// --- Data grid backgrounds (for charts) -----------------------------------
private val GridMajorC = Color(0x22FFFFFF)
private val GridMinorC = Color(0x0FFFFFFF)

// --- Gradients (Brushes) --------------------------------------------------
val GradientHero = Brush.linearGradient(
    colors = listOf(DeepSpaceC, CarbonFiberC, DeepSpaceC)
)

val GradientAccent = Brush.linearGradient(
    colors = listOf(ElectricCyanC, PlasmaBlueC)
)

val GradientAccentVertical = Brush.verticalGradient(
    colors = listOf(ElectricCyanC, ElectricCyanDeepC)
)

val GradientWarning = Brush.linearGradient(
    colors = listOf(SignalOrangeC, SignalOrangeGlowC)
)

val GradientCritical = Brush.linearGradient(
    colors = listOf(SignalRedC, SignalRedGlowC)
)

val GradientSuccess = Brush.linearGradient(
    colors = listOf(SignalGreenC, SignalGreenGlowC)
)

val GradientSurface = Brush.verticalGradient(
    colors = listOf(GraphiteDarkC, CarbonFiberC, DeepSpaceC)
)

val GradientSurfaceSubtle = Brush.verticalGradient(
    colors = listOf(GraphiteC.copy(alpha = 0.6f), CarbonFiberC.copy(alpha = 0.3f))
)

val GradientCard = Brush.linearGradient(
    colors = listOf(
        Color(0x14FFFFFF),
        Color(0x06FFFFFF),
        Color(0x0AFFFFFF)
    )
)

val GradientGlow = Brush.radialGradient(
    colors = listOf(ElectricCyanC.copy(alpha = 0.20f), Color.Transparent)
)

val GradientHeroGlow = Brush.radialGradient(
    colors = listOf(PlasmaBlueC.copy(alpha = 0.18f), Color.Transparent)
)

val GradientRadialGreen = Brush.radialGradient(
    colors = listOf(SignalGreenC.copy(alpha = 0.25f), Color.Transparent)
)

val GradientRadialRed = Brush.radialGradient(
    colors = listOf(SignalRedC.copy(alpha = 0.30f), Color.Transparent)
)

// --- Data class ------------------------------------------------------------
data class AppColors(
    // New naming (v2)
    val surfaceBlack: Color,
    val surfaceDeep: Color,
    val surfaceBase: Color,
    val surfaceRaised: Color,
    val surfaceElevated: Color,
    val surfaceLight: Color,
    val surfaceGlass: Color,
    val surfaceOverlay: Color,
    val primary: Color,
    val primarySoft: Color,
    val primaryDeep: Color,
    val primaryGlow: Color,
    val secondary: Color,
    val secondarySoft: Color,
    val secondaryDeep: Color,
    val accent: Color,
    val accentSoft: Color,
    val critical: Color,
    val criticalGlow: Color,
    val criticalSoft: Color,
    val warning: Color,
    val warningGlow: Color,
    val warningSoft: Color,
    val caution: Color,
    val cautionSoft: Color,
    val success: Color,
    val successGlow: Color,
    val successSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val textPure: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val borderHairline: Color,
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderAccent: Color,
    val borderAccentSoft: Color,
    val gridMajor: Color,
    val gridMinor: Color,
    val gradientHero: Brush,
    val gradientAccent: Brush,
    val gradientAccentVertical: Brush,
    val gradientWarning: Brush,
    val gradientCritical: Brush,
    val gradientSuccess: Brush,
    val gradientSurface: Brush,
    val gradientSurfaceSubtle: Brush,
    val gradientCard: Brush,
    val gradientGlow: Brush,
    val gradientHeroGlow: Brush,
    val gradientRadialGreen: Brush,
    val gradientRadialRed: Brush,
    val connectionExcellent: Color,
    val connectionGood: Color,
    val connectionFair: Color,
    val connectionPoor: Color,
    // Backwards-compatible aliases (old naming)
    val surfaceVariant: Color,
    val surfaceCard: Color,
    val surface: Color,
    val dark: Color,
    val highlight: Color,
    val gaugeRed: Color,
    val gaugeOrange: Color,
    val gaugeYellow: Color,
    val gaugeGreen: Color,
    val gaugeCyan: Color,
    val gaugeAccent: Color,
    val gaugeRedGlow: Color,
    val gaugeOrangeGlow: Color,
    val gaugeGreenGlow: Color,
    val gaugeBlueGlow: Color,
    val textDim: Color,
    val borderDefaultLegacy: Color,
    val borderAccentLegacy: Color
)

private fun AppColors.legacy(): AppColors = this

// --- Default dark palette --------------------------------------------------
val DefaultAppColors = AppColors(
    surfaceBlack = SpaceBlackC,
    surfaceDeep = DeepSpaceC,
    surfaceBase = CarbonFiberC,
    surfaceRaised = GraphiteDarkC,
    surfaceElevated = GraphiteC,
    surfaceLight = GraphiteLightC,
    surfaceGlass = OverlayGlassC,
    surfaceOverlay = Color(0xCC0A0E18),
    primary = ElectricCyanC,
    primarySoft = ElectricCyanSoftC,
    primaryDeep = ElectricCyanDeepC,
    primaryGlow = ElectricCyanGlowC,
    secondary = PlasmaBlueC,
    secondarySoft = PlasmaBlueSoftC,
    secondaryDeep = PlasmaBlueDeepC,
    accent = NeonMagentaC,
    accentSoft = NeonMagentaSoftC,
    critical = SignalRedC,
    criticalGlow = SignalRedGlowC,
    criticalSoft = SignalRedSoftC,
    warning = SignalOrangeC,
    warningGlow = SignalOrangeGlowC,
    warningSoft = SignalOrangeSoftC,
    caution = SignalAmberC,
    cautionSoft = SignalAmberSoftC,
    success = SignalGreenC,
    successGlow = SignalGreenGlowC,
    successSoft = SignalGreenSoftC,
    info = SignalCyanC,
    infoSoft = SignalCyanSoftC,
    textPure = TextPureC,
    textPrimary = TextPrimaryC,
    textSecondary = TextSecondaryC,
    textTertiary = TextTertiaryC,
    textMuted = TextMutedC,
    textDisabled = TextDisabledC,
    borderHairline = BorderHairlineC,
    borderSubtle = BorderSubtleC,
    borderDefault = BorderDefaultC,
    borderStrong = BorderStrongC,
    borderAccent = BorderAccentC,
    borderAccentSoft = BorderAccentSoftC,
    gridMajor = GridMajorC,
    gridMinor = GridMinorC,
    gradientHero = GradientHero,
    gradientAccent = GradientAccent,
    gradientAccentVertical = GradientAccentVertical,
    gradientWarning = GradientWarning,
    gradientCritical = GradientCritical,
    gradientSuccess = GradientSuccess,
    gradientSurface = GradientSurface,
    gradientSurfaceSubtle = GradientSurfaceSubtle,
    gradientCard = GradientCard,
    gradientGlow = GradientGlow,
    gradientHeroGlow = GradientHeroGlow,
    gradientRadialGreen = GradientRadialGreen,
    gradientRadialRed = GradientRadialRed,
    connectionExcellent = SignalGreenC,
    connectionGood = SignalGreenSoftC,
    connectionFair = SignalAmberC,
    connectionPoor = SignalRedC,
    // Legacy aliases
    surfaceVariant = GraphiteDarkC,
    surfaceCard = CarbonFiberC,
    surface = DeepSpaceC,
    dark = SpaceBlackC,
    highlight = ElectricCyanC,
    gaugeRed = SignalRedC,
    gaugeOrange = SignalOrangeC,
    gaugeYellow = SignalAmberC,
    gaugeGreen = SignalGreenC,
    gaugeCyan = SignalCyanC,
    gaugeAccent = ElectricCyanC,
    gaugeRedGlow = SignalRedGlowC,
    gaugeOrangeGlow = SignalOrangeGlowC,
    gaugeGreenGlow = SignalGreenGlowC,
    gaugeBlueGlow = PlasmaBlueC,
    textDim = TextTertiaryC,
    borderDefaultLegacy = BorderDefaultC,
    borderAccentLegacy = BorderAccentC
).legacy()

// --- Light palette ---------------------------------------------------------
val DefaultLightAppColors = AppColors(
    surfaceBlack = Color(0xFFF8FAFC),
    surfaceDeep = Color(0xFFFFFFFF),
    surfaceBase = Color(0xFFF1F5F9),
    surfaceRaised = Color(0xFFE2E8F0),
    surfaceElevated = Color(0xFFCBD5E1),
    surfaceLight = Color(0xFF94A3B8),
    surfaceGlass = Color(0xFFFFFFFF),
    surfaceOverlay = Color(0xE6FFFFFF),
    primary = Color(0xFF0097A7),
    primarySoft = Color(0xFF00BCD4),
    primaryDeep = Color(0xFF006064),
    primaryGlow = Color(0xFF00E5FF),
    secondary = Color(0xFF1976D2),
    secondarySoft = Color(0xFF42A5F5),
    secondaryDeep = Color(0xFF0D47A1),
    accent = Color(0xFFC2185B),
    accentSoft = Color(0xFFE91E63),
    critical = Color(0xFFD32F2F),
    criticalGlow = Color(0xFFF44336),
    criticalSoft = Color(0xFFEF5350),
    warning = Color(0xFFEF6C00),
    warningGlow = Color(0xFFFF6D00),
    warningSoft = Color(0xFFFF9800),
    caution = Color(0xFFF57C00),
    cautionSoft = Color(0xFFFFB300),
    success = Color(0xFF2E7D32),
    successGlow = Color(0xFF43A047),
    successSoft = Color(0xFF66BB6A),
    info = Color(0xFF00838F),
    infoSoft = Color(0xFF0097A7),
    textPure = Color(0xFF0F172A),
    textPrimary = Color(0xFF1E293B),
    textSecondary = Color(0xFF475569),
    textTertiary = Color(0xFF64748B),
    textMuted = Color(0xFF94A3B8),
    textDisabled = Color(0xFFCBD5E1),
    borderHairline = Color(0x14000000),
    borderSubtle = Color(0x1F000000),
    borderDefault = Color(0x33000000),
    borderStrong = Color(0x4D000000),
    borderAccent = Color(0xFF0097A7),
    borderAccentSoft = Color(0x660097A7),
    gridMajor = Color(0x22000000),
    gridMinor = Color(0x0F000000),
    gradientHero = Brush.linearGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFFFFFFF))
    ),
    gradientAccent = Brush.linearGradient(
        colors = listOf(Color(0xFF0097A7), Color(0xFF1976D2))
    ),
    gradientAccentVertical = Brush.verticalGradient(
        colors = listOf(Color(0xFF0097A7), Color(0xFF006064))
    ),
    gradientWarning = Brush.linearGradient(
        colors = listOf(Color(0xFFEF6C00), Color(0xFFFF6D00))
    ),
    gradientCritical = Brush.linearGradient(
        colors = listOf(Color(0xFFD32F2F), Color(0xFFF44336))
    ),
    gradientSuccess = Brush.linearGradient(
        colors = listOf(Color(0xFF2E7D32), Color(0xFF43A047))
    ),
    gradientSurface = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC), Color(0xFFF1F5F9))
    ),
    gradientSurfaceSubtle = Brush.verticalGradient(
        colors = listOf(Color(0xFFFAFAFA), Color(0xFFF5F5F5))
    ),
    gradientCard = Brush.linearGradient(
        colors = listOf(
            Color(0x08000000),
            Color(0x04000000),
            Color(0x0A000000)
        )
    ),
    gradientGlow = Brush.radialGradient(
        colors = listOf(Color(0x330097A7), Color.Transparent)
    ),
    gradientHeroGlow = Brush.radialGradient(
        colors = listOf(Color(0x331976D2), Color.Transparent)
    ),
    gradientRadialGreen = Brush.radialGradient(
        colors = listOf(Color(0x332E7D32), Color.Transparent)
    ),
    gradientRadialRed = Brush.radialGradient(
        colors = listOf(Color(0x33D32F2F), Color.Transparent)
    ),
    connectionExcellent = Color(0xFF2E7D32),
    connectionGood = Color(0xFF43A047),
    connectionFair = Color(0xFFF57C00),
    connectionPoor = Color(0xFFD32F2F),
    // Legacy aliases
    surfaceVariant = Color(0xFFF0F0F0),
    surfaceCard = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    dark = Color(0xFFF5F5F5),
    highlight = Color(0xFF1976D2),
    gaugeRed = Color(0xFFD32F2F),
    gaugeOrange = Color(0xFFEF6C00),
    gaugeYellow = Color(0xFFF57C00),
    gaugeGreen = Color(0xFF2E7D32),
    gaugeCyan = Color(0xFF00838F),
    gaugeAccent = Color(0xFF60A5FA),
    gaugeRedGlow = Color(0xFFF44336),
    gaugeOrangeGlow = Color(0xFFFF6D00),
    gaugeGreenGlow = Color(0xFF43A047),
    gaugeBlueGlow = Color(0xFF1976D2),
    textDim = Color(0xFF64748B),
    borderDefaultLegacy = Color(0xFFCBD5E1),
    borderAccentLegacy = Color(0xFF0097A7)
).legacy()

fun androidx.compose.ui.graphics.Color.toAppColor(): Color = this

fun com.canopobd.data.model.ColorTheme.toAppColors(
    mode: AppThemeMode = AppThemeMode.DARK
): AppColors {
    val base = if (mode == AppThemeMode.LIGHT) DefaultLightAppColors else DefaultAppColors
    val pColor = Color(primaryColor)
    val aColor = Color(accentColor)
    return base.copy(
        primary = pColor,
        primarySoft = pColor.copy(alpha = 0.8f),
        primaryGlow = aColor,
        secondary = aColor,
        secondarySoft = aColor.copy(alpha = 0.8f),
        accent = aColor,
        borderAccent = aColor,
        borderAccentLegacy = aColor
    )
}

val LocalAppColors = compositionLocalOf { DefaultAppColors }

// --- Backwards-compatible top-level aliases (old naming) -----------------
// These exist so that the 360+ references in pre-redesign screens keep
// compiling. New code should use the AppColors properties instead.
val canopoDark = SpaceBlackC
val canopoSurface = CarbonFiberC
val canopoSurfaceVariant = GraphiteDarkC
val canopoPrimary = ElectricCyanC
val canopoAccent = ElectricCyanC
val canopoHighlight = PlasmaBlueC
val canopoSecondary = NeonMagentaC
val textPrimary = TextPrimaryC
val textSecondary = TextSecondaryC
val textDim = TextTertiaryC
val textMuted = TextMutedC
val gaugeRed = SignalRedC
val gaugeOrange = SignalOrangeC
val gaugeYellow = SignalAmberC
val gaugeGreen = SignalGreenC
val gaugeCyan = SignalCyanC
val gaugeRedGlow = SignalRedGlowC
val gaugeOrangeGlow = SignalOrangeGlowC
val gaugeGreenGlow = SignalGreenGlowC
val gaugeBlueGlow = PlasmaBlueC
val gaugeRedSoft = SignalRedSoftC
val gaugeOrangeSoft = SignalOrangeSoftC
val gaugeGreenSoft = SignalGreenSoftC
val surfaceCard = CarbonFiberC
val surface = DeepSpaceC
val surfaceVariant = GraphiteDarkC
val dark = SpaceBlackC
val highlight = ElectricCyanC
val borderSubtle = BorderSubtleC
val borderDefault = BorderDefaultC
val borderAccent = BorderAccentC
