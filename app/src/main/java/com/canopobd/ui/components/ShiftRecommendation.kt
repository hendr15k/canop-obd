@file:OptIn(ExperimentalMaterial3Api::class)

package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.domain.DriveMode
import com.canopobd.data.domain.DriveModeDetector
import com.canopobd.ui.theme.LocalAppColors

/**
 * Shift Recommendation Component for Turbo Petrol Engines
 * 
 * Provides optimal shift point recommendations based on:
 * - Current gear (estimated from RPM/Speed ratio)
 * - Engine RPM
 * - Engine Load / Throttle position
 * - Detected drive mode (ECO / NORMAL / SPORT)
 * 
 * A14NET (Opel Astra J 1.4 Turbo) specific calibration:
 * - Max torque: 200 Nm @ 1850-4900 RPM (peak ~3000)
 * - Max power: 140 PS @ 4900-6000 RPM (peak 5500)
 * - Redline: 6500 RPM
 * 
 * Recommended shift points:
 * - ECO mode: 2000-2500 RPM for max efficiency
 * - NORMAL mode: 2500-3500 RPM (torque plateau)
 * - SPORT mode: 4500-5500 RPM (power band)
 * - Max power: 5000-5500 RPM
 */
object ShiftRecommendationEngine {

    // ============================================================
    // A14NET Turbo - Engine Characteristics
    // ============================================================
    data class EngineCalibration(
        val redlineRpm: Int = 6500,
        val rpmWarning: Int = 5850,
        val peakTorqueRpm: Int = 3000,
        val peakPowerRpm: Int = 5500,
        val torquePlateauMinRpm: Int = 1850,
        val torquePlateauMaxRpm: Int = 4900,
        val powerBandMinRpm: Int = 5000,
        val powerBandMaxRpm: Int = 6000,
        // Gear estimation: approximate RPM at 100 km/h in each gear (6-speed manual)
        val speedPerGearRpm100: Map<Int, Int> = mapOf(
            1 to 3500,  // 1st gear ~45 km/h max
            2 to 2500,  // 2nd gear ~75 km/h max
            3 to 2000,  // 3rd gear ~110 km/h max
            4 to 1600,  // 4th gear ~145 km/h max
            5 to 1300,  // 5th gear ~180 km/h max
            6 to 1100   // 6th gear ~220 km/h max
        )
    )

    // Default A14NET calibration
    val A14NET_CALIBRATION = EngineCalibration()

    /**
     * Estimated current gear based on RPM and speed ratio
     * Uses the A14NET typical gear ratios at ~100 km/h as reference points
     */
    fun estimateGear(rpm: Double, speedKmh: Double, calibration: EngineCalibration = A14NET_CALIBRATION): Int {
        if (speedKmh < 5 || rpm < 800) return 0  // Invalid / stationary
        if (rpm > 7000) return 6  // High RPM must be low gear

        // Calculate ratio: rpm per km/h (indicates gear)
        val rpmPerKmh = rpm / speedKmh
        
        // Determine gear based on RPM per km/h ratio
        // Lower ratio = higher gear (more speed per RPM)
        return when {
            rpmPerKmh > 80  -> 1  // Very high RPM for speed = 1st
            rpmPerKmh > 50  -> 2  // High RPM = 2nd
            rpmPerKmh > 35  -> 3  // Medium-high = 3rd
            rpmPerKmh > 25  -> 4  // Medium = 4th
            rpmPerKmh > 18  -> 5  // Medium-low = 5th
            rpmPerKmh > 10  -> 6  // Low = 6th (overdrive)
            else -> 6
        }
    }

    /**
     * Shift recommendation mode based on driving style
     */
    enum class ShiftMode(val label: String, val description: String) {
        ECO("ECO", "Sparsparmodus - frueh schalten"),
        EFFICIENCY("EFFIZIENZ", "Optimaler Wirkungsgrad"),
        TORQUE("DREHMOMENT", "Drehmoment optimiert"),
        POWER("LEISTUNG", "Leistungsoptimiert"),
        REDLINE("REDLINE", "Nahe Drehzahlgrenze!");
    }

    /**
     * Full shift recommendation result
     */
    data class ShiftRecommendation(
        val shouldShift: Boolean = false,
        val shiftNow: Boolean = false,
        val shiftMode: ShiftMode = ShiftMode.ECO,
        val targetRpm: Int = 0,
        val currentGear: Int = 0,
        val nextGear: Int = 0,
        val recommendation: String = "",
        val rpmPercent: Float = 0f,
        val isInPowerBand: Boolean = false,
        val isInTorquePlateau: Boolean = false,
        val isNearRedline: Boolean = false,
        val urgency: ShiftUrgency = ShiftUrgency.NONE
    )

    enum class ShiftUrgency { NONE, SOON, NOW, CRITICAL }

    /**
     * Detect driving mode from telemetry
     */
    fun detectDriveMode(
        throttle: Double,
        rpm: Double,
        speed: Double,
        engineLoad: Double,
        acceleratorPedalD: Double = throttle,
        throttleActuator: Double = throttle
    ): DriveMode {
        return DriveModeDetector.detectMode(
            throttle = throttle,
            rpm = rpm,
            speed = speed,
            engineLoad = engineLoad,
            acceleratorPedalD = acceleratorPedalD,
            throttleActuator = throttleActuator
        )
    }

    /**
     * Main calculation: determine shift recommendation
     * 
     * @param rpm Current engine RPM
     * @param speed Vehicle speed in km/h
     * @param engineLoad Engine load in percent
     * @param throttle Throttle position in percent
     * @param calibration Engine-specific calibration
     * @param targetMode Forced shift mode (or null for auto)
     * @param minGearToConsider Minimum gear to show recommendation (skip 1st)
     * @param maxGear Maximum gear (prevents showing shift from 6th)
     */
    fun calculateRecommendation(
        rpm: Double,
        speed: Double,
        engineLoad: Double,
        throttle: Double,
        calibration: EngineCalibration = A14NET_CALIBRATION,
        targetMode: ShiftMode? = null,
        minGearToConsider: Int = 2,
        maxGear: Int = 6
    ): ShiftRecommendation {
        if (rpm < 500 || speed < 2) {
            return ShiftRecommendation()  // Invalid state
        }

        val currentGear = estimateGear(rpm, speed, calibration)
        
        // Skip recommendations for 1st gear or beyond max
        if (currentGear < minGearToConsider || currentGear >= maxGear) {
            return ShiftRecommendation(
                currentGear = currentGear,
                rpmPercent = (rpm / calibration.redlineRpm).toFloat()
            )
        }

        val nextGear = (currentGear + 1).coerceAtMost(maxGear)

        // Determine shift mode
        val mode = targetMode ?: when {
            rpm >= calibration.rpmWarning -> ShiftMode.REDLINE
            rpm >= calibration.powerBandMinRpm -> ShiftMode.POWER
            rpm >= 3500 && throttle > 50 -> ShiftMode.TORQUE
            rpm >= 2500 && engineLoad > 50 -> ShiftMode.TORQUE
            else -> ShiftMode.ECO
        }

        // Calculate optimal shift RPM based on mode
        val (targetRpm, shouldShift, urgency) = when (mode) {
            ShiftMode.REDLINE -> Triple(
                calibration.rpmWarning - 200,
                rpm >= calibration.rpmWarning - 200,
                if (rpm >= calibration.rpmWarning) ShiftUrgency.CRITICAL 
                else if (rpm >= calibration.rpmWarning - 300) ShiftUrgency.NOW 
                else ShiftUrgency.SOON
            )
            ShiftMode.POWER -> Triple(
                calibration.peakPowerRpm,
                rpm >= calibration.peakPowerRpm - 200,
                if (rpm >= calibration.peakPowerRpm) ShiftUrgency.NOW 
                else if (rpm >= calibration.peakPowerRpm - 300) ShiftUrgency.SOON 
                else ShiftUrgency.NONE
            )
            ShiftMode.TORQUE -> Triple(
                3000,
                rpm >= 2500,
                if (rpm >= 3000) ShiftUrgency.NOW 
                else if (rpm >= 2500) ShiftUrgency.SOON 
                else ShiftUrgency.NONE
            )
            ShiftMode.EFFICIENCY -> Triple(
                2250,
                rpm >= 2000,
                if (rpm >= 2250) ShiftUrgency.NOW 
                else if (rpm >= 2000) ShiftUrgency.SOON 
                else ShiftUrgency.NONE
            )
            ShiftMode.ECO -> Triple(
                2000,
                rpm >= 1800,
                if (rpm >= 2000) ShiftUrgency.NOW 
                else if (rpm >= 1800) ShiftUrgency.SOON 
                else ShiftUrgency.NONE
            )
        }

        // Generate recommendation text
        val recommendation = when (mode) {
            ShiftMode.REDLINE -> "HOCHSCHALTEN! Drehzahlgrenze!"
            ShiftMode.POWER -> if (shouldShift) "Leistungsgrenze - Schalten!" else "Naehe Leistungsmaximum"
            ShiftMode.TORQUE -> if (shouldShift) "Optimaler Drehmoment-Bereich" else "Drehmoment-Bereich"
            ShiftMode.EFFICIENCY -> if (shouldShift) "Effizienter Schaltpunkt" else "Im Wirkungsgrad-Optimum"
            ShiftMode.ECO -> if (shouldShift) "Sparsam schalten" else "ECO-Bereich"
        }

        val rpmPercent = (rpm / calibration.redlineRpm).toFloat()
        val isInPowerBand = rpm in calibration.powerBandMinRpm.toDouble()..calibration.powerBandMaxRpm.toDouble()
        val isInTorquePlateau = rpm in calibration.torquePlateauMinRpm.toDouble()..calibration.torquePlateauMaxRpm.toDouble()
        val isNearRedline = rpm >= calibration.rpmWarning - 500

        return ShiftRecommendation(
            shouldShift = shouldShift,
            shiftNow = urgency == ShiftUrgency.NOW || urgency == ShiftUrgency.CRITICAL,
            shiftMode = mode,
            targetRpm = targetRpm,
            currentGear = currentGear,
            nextGear = nextGear,
            recommendation = recommendation,
            rpmPercent = rpmPercent,
            isInPowerBand = isInPowerBand,
            isInTorquePlateau = isInTorquePlateau,
            isNearRedline = isNearRedline,
            urgency = urgency
        )
    }

    /**
     * Get recommended RPM range for a specific mode and gear
     */
    fun getRecommendedRpmRange(
        mode: ShiftMode,
        calibration: EngineCalibration = A14NET_CALIBRATION
    ): Pair<Int, Int> = when (mode) {
        ShiftMode.ECO -> Pair(1500, 2000)
        ShiftMode.EFFICIENCY -> Pair(2000, 2500)
        ShiftMode.TORQUE -> Pair(2500, 3500)
        ShiftMode.POWER -> Pair(4500, 5500)
        ShiftMode.REDLINE -> Pair(5500, 6200)
    }

    /**
     * Calculate fuel efficiency estimate based on current operating point
     * Returns relative efficiency 0.0-1.0 where 1.0 is optimal
     */
    fun estimateRelativeEfficiency(
        rpm: Double,
        throttle: Double,
        calibration: EngineCalibration = A14NET_CALIBRATION
    ): Double {
        if (rpm < 800) return 0.0
        
        // Turbo petrol efficiency zones (relative to max)
        // Optimal efficiency is typically 1500-3000 RPM with moderate throttle
        val rpmEfficiency = when {
            rpm < 1000 -> 0.3
            rpm < 1500 -> 0.6
            rpm < 2000 -> 0.85
            rpm < 2500 -> 0.95  // Sweet spot for ECO
            rpm < 3000 -> 0.9
            rpm < 3500 -> 0.85
            rpm < 4000 -> 0.75
            rpm < 4500 -> 0.7
            rpm < 5000 -> 0.65
            rpm < 5500 -> 0.6
            rpm < 6000 -> 0.5
            else -> 0.35
        }
        
        val throttleEfficiency = when {
            throttle < 20 -> 1.0   // Light load - lean burn
            throttle < 40 -> 0.9
            throttle < 60 -> 0.8
            throttle < 80 -> 0.7
            else -> 0.6            // Full throttle - rich mixture
        }
        
        return rpmEfficiency * throttleEfficiency
    }
}

/**
 * Main Composable: Shift Recommendation Display
 * 
 * Features:
 * - Large up-arrow with pulsing animation when shift recommended
 * - Current gear display
 * - RPM percentage bar
 * - Mode indicator (ECO/ TORQUE / POWER)
 * - Recommendation text
 * 
 * @param rpm Current engine RPM
 * @param speed Current vehicle speed (km/h)
 * @param engineLoad Engine load (%)
 * @param throttle Throttle position (%)
 * @param calibration Optional engine-specific calibration
 * @param compactMode Show minimal compact version
 * @param onModeChange Callback when user selects shift mode
 */
@Composable
fun ShiftRecommendationDisplay(
    rpm: Double,
    speed: Double,
    engineLoad: Double,
    throttle: Double,
    calibration: ShiftRecommendationEngine.EngineCalibration = ShiftRecommendationEngine.A14NET_CALIBRATION,
    compactMode: Boolean = false,
    onModeChange: ((ShiftRecommendationEngine.ShiftMode) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val recommendation = remember(rpm, speed, engineLoad, throttle, calibration) {
        ShiftRecommendationEngine.calculateRecommendation(
            rpm = rpm,
            speed = speed,
            engineLoad = engineLoad,
            throttle = throttle,
            calibration = calibration
        )
    }

    val efficiency = remember(rpm, throttle, calibration) {
        ShiftRecommendationEngine.estimateRelativeEfficiency(rpm, throttle, calibration)
    }

    if (compactMode) {
        CompactShiftIndicator(
            recommendation = recommendation,
            efficiency = efficiency,
            modifier = modifier
        )
    } else {
        FullShiftRecommendation(
            recommendation = recommendation,
            efficiency = efficiency,
            onModeChange = onModeChange,
            rpm = rpm,
            modifier = modifier
        )
    }
}

@Composable
private fun CompactShiftIndicator(
    recommendation: ShiftRecommendationEngine.ShiftRecommendation,
    efficiency: Double,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    
    // Pulsing animation when shift recommended
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (recommendation.shouldShift) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = if (recommendation.shouldShift) 1f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val backgroundColor = when {
        recommendation.urgency == ShiftRecommendationEngine.ShiftUrgency.CRITICAL -> colors.gaugeRed
        recommendation.shiftNow -> colors.gaugeGreen
        recommendation.shouldShift -> colors.gaugeYellow
        recommendation.isInTorquePlateau -> colors.gaugeGreen.copy(alpha = 0.6f)
        else -> colors.surfaceVariant
    }

    val textColor = when {
        recommendation.urgency == ShiftRecommendationEngine.ShiftUrgency.CRITICAL -> Color.White
        recommendation.shouldShift -> colors.dark
        else -> colors.textSecondary
    }

    Surface(
        modifier = modifier
            .scale(if (recommendation.shouldShift) pulseScale else 1f),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = pulseAlpha)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Shift arrow icon
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Schalten",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )

            // Recommendation text
            Text(
                text = when {
                    recommendation.urgency == ShiftRecommendationEngine.ShiftUrgency.CRITICAL -> "HOCHSCHALTEN!"
                    recommendation.shiftNow -> "Schalten!"
                    recommendation.shouldShift -> recommendation.recommendation
                    recommendation.currentGear > 0 -> "G${recommendation.currentGear}"
                    else -> "--"
                },
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (recommendation.shouldShift) FontWeight.Bold else FontWeight.Normal
            )

            // Efficiency indicator
            if (efficiency > 0) {
                Text(
                    text = "${(efficiency * 100).toInt()}%",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun FullShiftRecommendation(
    recommendation: ShiftRecommendationEngine.ShiftRecommendation,
    efficiency: Double,
    onModeChange: ((ShiftRecommendationEngine.ShiftMode) -> Unit)?,
    rpm: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "shiftAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (recommendation.shiftNow) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (recommendation.shouldShift) 0.8f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val accentColor = when {
        recommendation.urgency == ShiftRecommendationEngine.ShiftUrgency.CRITICAL -> colors.gaugeRed
        recommendation.shiftMode == ShiftRecommendationEngine.ShiftMode.POWER -> colors.gaugeOrange
        recommendation.shiftMode == ShiftRecommendationEngine.ShiftMode.TORQUE -> colors.gaugeYellow
        recommendation.shouldShift -> colors.gaugeGreen
        recommendation.isInTorquePlateau -> colors.gaugeGreen
        else -> colors.accent
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(300),
        label = "accentColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulseScale),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            width = if (recommendation.shouldShift) 2.dp else 1.dp,
            color = accentColor.copy(alpha = glowAlpha)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Current gear indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gear display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GANG",
                        fontSize = 11.sp,
                        color = colors.textDim
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (recommendation.currentGear > 0) 
                            recommendation.currentGear.toString() else "--",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (recommendation.currentGear > 0) 
                            colors.textPrimary else colors.textDim
                    )
                    if (recommendation.nextGear > recommendation.currentGear) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = recommendation.nextGear.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                }

                // Mode indicator
                if (onModeChange != null) {
                    ModeSelector(
                        currentMode = recommendation.shiftMode,
                        onModeChange = onModeChange,
                        colors = colors
                    )
                } else {
                    ModeBadge(
                        mode = recommendation.shiftMode,
                        color = animatedAccentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RPM Bar with zones
            RpmProgressBar(
                rpm = rpm,
                targetRpm = recommendation.targetRpm.toFloat(),
                calibration = ShiftRecommendationEngine.A14NET_CALIBRATION,
                accentColor = animatedAccentColor,
                showTargetMarker = recommendation.shouldShift
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Efficiency meter
            EfficiencyBar(
                efficiency = efficiency,
                colors = colors
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main recommendation
            if (recommendation.shouldShift) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recommendation.recommendation,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            } else {
                // Status text
                Text(
                    text = when {
                        recommendation.isInTorquePlateau -> "Im Drehmoment-Optimalbereich"
                        recommendation.isInPowerBand -> "Im Leistungsbereich"
                        recommendation.isNearRedline -> "Warnung: Nahe Drehzahlgrenze!"
                        rpm < 1500 -> "Zu niedrige Drehzahl"
                        else -> "Normaler Betrieb"
                    },
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Info row: shift zone hints
            if (recommendation.currentGear >= 2 && !recommendation.shouldShift) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShiftZoneChip(
                        label = "ECO",
                        rpmRange = "2000",
                        isActive = recommendation.shiftMode == ShiftRecommendationEngine.ShiftMode.ECO,
                        colors = colors
                    )
                    ShiftZoneChip(
                        label = "DREHM.",
                        rpmRange = "3000",
                        isActive = recommendation.shiftMode == ShiftRecommendationEngine.ShiftMode.TORQUE,
                        colors = colors
                    )
                    ShiftZoneChip(
                        label = "LEIST.",
                        rpmRange = "5500",
                        isActive = recommendation.shiftMode == ShiftRecommendationEngine.ShiftMode.POWER,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeBadge(
    mode: ShiftRecommendationEngine.ShiftMode,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = mode.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ModeSelector(
    currentMode: ShiftRecommendationEngine.ShiftMode,
    onModeChange: (ShiftRecommendationEngine.ShiftMode) -> Unit,
    colors: com.canopobd.ui.theme.AppColors
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ShiftRecommendationEngine.ShiftMode.entries.forEach { mode ->
            val isActive = mode == currentMode
            FilterChip(
                selected = isActive,
                onClick = { onModeChange(mode) },
                label = {
                    Text(
                        text = mode.label,
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.accent.copy(alpha = 0.3f),
                    selectedLabelColor = colors.accent
                ),
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

@Composable
private fun RpmProgressBar(
    rpm: Double,
    targetRpm: Float,
    calibration: ShiftRecommendationEngine.EngineCalibration,
    accentColor: Color,
    showTargetMarker: Boolean
) {
    val colors = LocalAppColors.current
    val progress = (rpm / calibration.redlineRpm).toFloat().coerceIn(0f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${rpm.toInt()} RPM",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            if (showTargetMarker) {
                Text(
                    text = "Ziel: $targetRpm.toInt() RPM",
                    fontSize = 12.sp,
                    color = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surface)
        ) {
            // Colored zones background
            Row(modifier = Modifier.fillMaxSize()) {
                // ECO zone (0-25%)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.25f)
                        .background(colors.gaugeGreen.copy(alpha = 0.15f))
                )
                // Normal zone (25-50%)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.25f)
                        .background(colors.gaugeYellow.copy(alpha = 0.15f))
                )
                // Power zone (50-75%)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.25f)
                        .background(colors.gaugeOrange.copy(alpha = 0.15f))
                )
                // Redline zone (75-100%)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.25f)
                        .background(colors.gaugeRed.copy(alpha = 0.15f))
                )
            }

            // Current RPM progress
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colors.gaugeGreen,
                                colors.gaugeYellow,
                                colors.gaugeOrange,
                                colors.gaugeRed
                            )
                        )
                    )
                    .padding(2.dp)
            )

            // RPM markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0", "2k", "4k", "6k", "6.5k").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 8.sp,
                        color = colors.textDim,
                        modifier = Modifier.padding(top = 22.dp)
                    )
                }
            }
        }

        // RPM zone labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ECO", fontSize = 9.sp, color = colors.gaugeGreen)
            Text("NORMAL", fontSize = 9.sp, color = colors.gaugeYellow)
            Text("POWER", fontSize = 9.sp, color = colors.gaugeOrange)
            Text("LIMIT", fontSize = 9.sp, color = colors.gaugeRed)
        }
    }
}

@Composable
private fun EfficiencyBar(
    efficiency: Double,
    colors: com.canopobd.ui.theme.AppColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Wirkungsgrad",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
            Text(
                text = "${(efficiency * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    efficiency > 0.85 -> colors.gaugeGreen
                    efficiency > 0.6 -> colors.gaugeYellow
                    else -> colors.gaugeOrange
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(efficiency.toFloat())
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                colors.gaugeRed,
                                colors.gaugeOrange,
                                colors.gaugeYellow,
                                colors.gaugeGreen
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ShiftZoneChip(
    label: String,
    rpmRange: String,
    isActive: Boolean,
    colors: com.canopobd.ui.theme.AppColors
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isActive) 
            colors.accent.copy(alpha = 0.2f) 
        else 
            colors.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) colors.accent else colors.textSecondary
            )
            Text(
                text = "$rpmRange RPM",
                fontSize = 8.sp,
                color = colors.textDim
            )
        }
    }
}
