package com.canopobd.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canopobd.data.domain.DriveMode
import com.canopobd.data.domain.DriveModeDetector
import com.canopobd.ui.theme.*

/**
 * DriveModeIndicator - Fahrmodus-Indikator fuer das Dashboard
 * 
 * Erkennt automatisch ECO/NORMAL/SPORT basierend auf:
 * - Throttle Position (PID 0x11)
 * - Engine Load (PID 0x04)
 * - RPM (PID 0x0C)
 * - Speed (PID 0x0D)
 * - Accelerator Pedal Position (PID 0x49)
 * - Throttle Actuator (PID 0x4C)
 * 
 * Visuelle Indikatoren mit Farben:
 * - ECO: Gruen (#22C55E)
 * - NORMAL: Blau (#60A5FA)
 * - SPORT: Rot/Orange (#F97316)
 */
@Composable
fun DriveModeIndicator(
    throttle: Double,
    rpm: Double,
    speed: Double,
    engineLoad: Double,
    acceleratorPedalD: Double,
    throttleActuator: Double,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showDetails: Boolean = false
) {
    val detectedMode = remember(throttle, rpm, speed, engineLoad, acceleratorPedalD, throttleActuator) {
        DriveModeDetector.detectMode(
            throttle = throttle,
            rpm = rpm,
            speed = speed,
            engineLoad = engineLoad,
            acceleratorPedalD = acceleratorPedalD,
            throttleActuator = throttleActuator
        )
    }

    val modeColor = when (detectedMode) {
        DriveMode.ECO -> gaugeGreen
        DriveMode.NORMAL -> canopoAccent
        DriveMode.SPORT -> gaugeOrange
    }

    val animatedColor by animateColorAsState(
        targetValue = modeColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "mode_color"
    )

    // Pulsating glow effect for SPORT mode
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (detectedMode == DriveMode.SPORT) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    if (compact) {
        CompactDriveModeIndicator(
            mode = detectedMode,
            color = animatedColor,
            pulseAlpha = pulseAlpha,
            modifier = modifier
        )
    } else {
        FullDriveModeIndicator(
            mode = detectedMode,
            color = animatedColor,
            pulseAlpha = pulseAlpha,
            throttle = throttle,
            rpm = rpm,
            speed = speed,
            engineLoad = engineLoad,
            showDetails = showDetails,
            modifier = modifier
        )
    }
}

@Composable
private fun CompactDriveModeIndicator(
    mode: DriveMode,
    color: Color,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    val modeText = when (mode) {
        DriveMode.ECO -> "ECO"
        DriveMode.NORMAL -> "NORM"
        DriveMode.SPORT -> "SPORT"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Glow effect for SPORT
            if (mode == DriveMode.SPORT) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = pulseAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = modeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun FullDriveModeIndicator(
    mode: DriveMode,
    color: Color,
    pulseAlpha: Float,
    throttle: Double,
    rpm: Double,
    speed: Double,
    engineLoad: Double,
    showDetails: Boolean,
    modifier: Modifier = Modifier
) {
    val modeText = when (mode) {
        DriveMode.ECO -> "ECO"
        DriveMode.NORMAL -> "NORMAL"
        DriveMode.SPORT -> "SPORT"
    }

    val modeDescription = when (mode) {
        DriveMode.ECO -> "Sparsamer Modus"
        DriveMode.NORMAL -> "Ausgewogener Modus"
        DriveMode.SPORT -> "Sportlicher Modus"
    }

    // Calculate mode indicators based on sensor data
    val throttleIntensity = (throttle / 100.0).coerceIn(0.0, 1.0)
    val rpmIntensity = (rpm / 8000.0).coerceIn(0.0, 1.0)
    val loadIntensity = (engineLoad / 100.0).coerceIn(0.0, 1.0)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = canopoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode header with glow
            Box(contentAlignment = Alignment.Center) {
                // Outer glow for SPORT
                if (mode == DriveMode.SPORT) {
                    Canvas(
                        modifier = Modifier.size(80.dp)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = pulseAlpha * 0.5f),
                                    Color.Transparent
                                ),
                                radius = size.minDimension / 2
                            ),
                            radius = size.minDimension / 2
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = modeText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = modeDescription,
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode selection bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeBar(
                    label = "ECO",
                    isActive = mode == DriveMode.ECO,
                    color = gaugeGreen,
                    onClick = {}
                )
                ModeBar(
                    label = "NORM",
                    isActive = mode == DriveMode.NORMAL,
                    color = canopoAccent,
                    onClick = {}
                )
                ModeBar(
                    label = "SPORT",
                    isActive = mode == DriveMode.SPORT,
                    color = gaugeOrange,
                    onClick = {}
                )
            }

            if (showDetails) {
                Spacer(modifier = Modifier.height(12.dp))

                // Real-time sensor indicators
                DriveModeIndicators(
                    throttle = throttleIntensity,
                    rpm = rpmIntensity,
                    load = loadIntensity,
                    mode = mode,
                    color = color
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Throttle", value = "%.0f%%".format(throttle), color = gaugeYellow)
                    StatItem(label = "RPM", value = "%.0f".format(rpm), color = gaugeCyan)
                    StatItem(label = "Load", value = "%.0f%%".format(engineLoad), color = gaugeOrange)
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun ModeBar(
    label: String,
    isActive: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isActive) color else color.copy(alpha = 0.3f),
        animationSpec = tween(200),
        label = "bar_color"
    )

    val animatedHeight by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bar_height"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height((40 * animatedHeight).dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(animatedColor)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isActive) animatedColor else textDim,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun DriveModeIndicators(
    throttle: Double,
    rpm: Double,
    load: Double,
    mode: DriveMode,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(canopoSurfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Text(
            text = "Erkennungs-Indikatoren",
            fontSize = 9.sp,
            color = textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Throttle Response Bar
        IndicatorRow(
            label = "Gaspedal-Ansprechverhalten",
            value = throttle,
            mode = mode,
            color = when {
                throttle < 0.3 -> gaugeGreen
                throttle < 0.6 -> gaugeYellow
                else -> gaugeRed
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // RPM indicator
        IndicatorRow(
            label = "Drehzahl-Band",
            value = rpm,
            mode = mode,
            color = when {
                rpm < 0.4 -> gaugeGreen  // ECO: low RPM
                rpm < 0.6 -> canopoAccent  // NORMAL
                else -> gaugeOrange  // SPORT: high RPM
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Engine Load indicator
        IndicatorRow(
            label = "Motorlast",
            value = load,
            mode = mode,
            color = when {
                load < 0.4 -> gaugeGreen
                load < 0.7 -> gaugeYellow
                else -> gaugeOrange
            }
        )
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun IndicatorRow(
    label: String,
    value: Double,
    mode: DriveMode,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                color = textSecondary
            )
            Text(
                text = "%.0f%%".format(value * 100),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(canopoDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.6f),
                                color
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            fontSize = 8.sp,
            color = textDim
        )
    }
}

/**
 * Erweiterter Fahrmodus-Detektor mit Opel-spezifischen Charakteristiken
 */
object OpelDriveModeDetector {

    // Opel-spezifische Schwellenwerte fuer A14NET Motor
    private const val ECO_SPEED_MIN = 20.0
    private const val ECO_SPEED_MAX = 140.0
    private const val ECO_RPM_MAX = 3000.0
    private const val ECO_LOAD_MAX = 40.0
    private const val ECO_THROTTLE_MAX = 30.0
    private const val ECO_THROTTLE_RESPONSE_MAX = 0.75

    private const val SPORT_RPM_MIN = 3500.0
    private const val SPORT_RPM_ABSOLUTE = 4500.0
    private const val SPORT_THROTTLE_MIN = 55.0
    private const val SPORT_LOAD_MIN = 60.0
    private const val SPORT_THROTTLE_RESPONSE_MIN = 0.90

    // Throttle-Reaktions-Score (0.0 = sehr träge, 1.0 = sehr direkt)
    fun calculateThrottleResponsiveness(
        acceleratorPedal: Double,
        throttlePosition: Double
    ): Double {
        if (acceleratorPedal < 1.0) return 0.5
        val ratio = throttlePosition / acceleratorPedal
        return ratio.coerceIn(0.0, 1.0)
    }

    // ECO-Score berechnen (0.0 = nicht ECO, 1.0 = sehr ECO)
    fun calculateEcoScore(
        throttle: Double,
        rpm: Double,
        speed: Double,
        engineLoad: Double,
        throttleResponse: Double
    ): Double {
        var score = 0.0

        // Niedrige Drehzahl = gut fuer ECO
        score += when {
            rpm < 2000 -> 0.3
            rpm < 2500 -> 0.2
            rpm < 3000 -> 0.1
            rpm > 4000 -> -0.3
            else -> 0.0
        }

        // Niedrige Last = gut fuer ECO
        score += when {
            engineLoad < 30 -> 0.2
            engineLoad < 40 -> 0.1
            engineLoad > 60 -> -0.2
            else -> 0.0
        }

        // Sanftes Gasgeben = gut fuer ECO
        score += when {
            throttle < 20 -> 0.2
            throttle < 30 -> 0.1
            throttle > 50 -> -0.2
            else -> 0.0
        }

        // Gedrosseltes Ansprechverhalten = gut fuer ECO
        if (throttleResponse < 0.8) score += 0.15
        if (throttleResponse > 0.9) score -= 0.15

        // Geschwindigkeitsfaktor
        if (speed in ECO_SPEED_MIN..ECO_SPEED_MAX) score += 0.05

        return score.coerceIn(0.0, 1.0)
    }

    // SPORT-Score berechnen (0.0 = nicht SPORT, 1.0 = sehr SPORT)
    @Suppress("UNUSED_PARAMETER")
    fun calculateSportScore(
        throttle: Double,
        rpm: Double,
        speed: Double,
        engineLoad: Double,
        throttleResponse: Double
    ): Double {
        var score = 0.0

        // Hohe Drehzahl = SPORT
        score += when {
            rpm > 4500 -> 0.3
            rpm > 4000 -> 0.2
            rpm > 3500 -> 0.1
            rpm < 2000 -> -0.2
            else -> 0.0
        }

        // Hohe Last = SPORT
        score += when {
            engineLoad > 70 -> 0.2
            engineLoad > 60 -> 0.1
            engineLoad < 30 -> -0.1
            else -> 0.0
        }

        // Aggressives Gasgeben = SPORT
        score += when {
            throttle > 70 -> 0.2
            throttle > 55 -> 0.1
            throttle < 20 -> -0.1
            else -> 0.0
        }

        // Direktes Ansprechverhalten = SPORT
        if (throttleResponse > 0.9) score += 0.15

        return score.coerceIn(0.0, 1.0)
    }

    /**
     * Erweiterten Fahrmodus mit Opel-spezifischer Logik erkennen
     */
    fun detectModeAdvanced(
        throttle: Double,
        rpm: Double,
        speed: Double,
        engineLoad: Double,
        acceleratorPedalD: Double,
        throttleActuator: Double
    ): DriveMode {
        val throttleResponse = calculateThrottleResponsiveness(acceleratorPedalD, throttleActuator)
        val ecoScore = calculateEcoScore(throttle, rpm, speed, engineLoad, throttleResponse)
        val sportScore = calculateSportScore(throttle, rpm, speed, engineLoad, throttleResponse)

        // Mode basierend auf Scores bestimmen
        return when {
            // Klare ECO-Indikatoren
            ecoScore > 0.5 && sportScore < 0.3 -> DriveMode.ECO
            
            // Klare SPORT-Indikatoren  
            sportScore > 0.5 && ecoScore < 0.3 -> DriveMode.SPORT
            
            // Direkte Erkennung basierend auf OBD-Daten
            rpm > SPORT_RPM_ABSOLUTE -> DriveMode.SPORT
            rpm > SPORT_RPM_MIN && throttle > SPORT_THROTTLE_MIN -> DriveMode.SPORT
            throttleResponse > SPORT_THROTTLE_RESPONSE_MIN && throttle > SPORT_THROTTLE_MIN -> DriveMode.SPORT
            
            speed > 30 && engineLoad < ECO_LOAD_MAX && throttle < ECO_THROTTLE_MAX && throttleResponse < ECO_THROTTLE_RESPONSE_MAX -> DriveMode.ECO
            
            // Standard-Fallback
            sportScore > ecoScore + 0.2 -> DriveMode.SPORT
            ecoScore > sportScore + 0.2 -> DriveMode.ECO
            else -> DriveMode.NORMAL
        }
    }

    /**
     * Drehzahl-basiertes Fahren-Score berechnen
     * 
     * Bewertet das Fahrverhalten basierend auf:
     * - Drehzahl-Ausnutzung
     * - Schaltverhalten
     * - Lastverteilung
     */
    fun calculateRPMDrivingScore(rpm: Double, throttle: Double, speed: Double): Int {
        var score = 100

        // RPM-Analyse
        when {
            rpm in 1500.0..2500.0 && throttle < 40 -> score += 10  // Optimaler ECO-Bereich
            rpm in 2000.0..3500.0 && throttle in 40.0..60.0 -> score += 5  // Guter Normalbetrieb
            rpm in 3000.0..5000.0 && throttle > 60 -> score += 5  // Angemessener Sportbetrieb
            rpm > 5500 -> score -= 15  // Zu hohe Drehzahl
            rpm < 1000 && throttle > 30 -> score -= 10  // Unnoetig hohe Last im niedrigen Bereich
        }

        // Geschwindigkeits-RPM-Korrelation
        if (speed > 0) {
            val expectedRpmForSpeed = speed * 30  // Grob-Approximation
            val rpmDeviation = kotlin.math.abs(rpm - expectedRpmForSpeed)
            when {
                rpmDeviation < 500 -> score += 5  // Niedrigste Abweichung
                rpmDeviation > 2000 -> score -= 10  // Starke Abweichung
            }
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Throttle-Response-Qualitaet bewerten
     * 
     * @return Qualitaets-Score von 0-100
     */
    fun evaluateThrottleResponseQuality(
        acceleratorPedal: Double,
        throttlePosition: Double,
        engineLoad: Double
    ): Int {
        if (acceleratorPedal < 1.0) return 50  // Kein Gas gegeben

        val responseRatio = if (acceleratorPedal > 0) {
            throttlePosition / acceleratorPedal
        } else 0.0

        var score = 50

        // Response-Verhaeltnis bewerten
        when {
            responseRatio in 0.85..0.95 -> score += 30  // Optimal (direkt aber nicht aggressiv)
            responseRatio > 0.95 -> score += 20  // Sehr direkt
            responseRatio in 0.70..0.85 -> score += 10  // Etwas gedrosselt (ECO-Mode?)
            responseRatio < 0.50 -> score -= 20  // Trage Ansprache
        }

        // Last-Kompensation
        if (engineLoad > 80 && responseRatio < 0.8) {
            score -= 15  // Hohe Last mit trager Response ist schlecht
        }

        return score.coerceIn(0, 100)
    }
}

/**
 * Compact variant for status bar display
 */
@Composable
fun DriveModeBadge(
    throttle: Double,
    rpm: Double,
    speed: Double,
    engineLoad: Double,
    acceleratorPedalD: Double,
    throttleActuator: Double,
    modifier: Modifier = Modifier
) {
    val mode = remember(throttle, rpm, speed, engineLoad, acceleratorPedalD, throttleActuator) {
        OpelDriveModeDetector.detectModeAdvanced(
            throttle = throttle,
            rpm = rpm,
            speed = speed,
            engineLoad = engineLoad,
            acceleratorPedalD = acceleratorPedalD,
            throttleActuator = throttleActuator
        )
    }

    val color = when (mode) {
        DriveMode.ECO -> gaugeGreen
        DriveMode.NORMAL -> canopoAccent
        DriveMode.SPORT -> gaugeOrange
    }

    val modeText = when (mode) {
        DriveMode.ECO -> "ECO"
        DriveMode.NORMAL -> "N"
        DriveMode.SPORT -> "S"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = modeText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontFamily = FontFamily.Monospace
        )
    }
}
