package com.canopobd.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GaugeConfig {
    val DEFAULT_SIZE: Dp = 150.dp
    val ROW_SIZE: Dp = 130.dp
    val COMPACT_SIZE: Dp = 100.dp

    val START_ANGLE: Float = 135f
    val SWEEP_ANGLE: Float = 270f

    val STROKE_WIDTH_RATIO: Float = 0.08f
    val OUTER_STROKE_RATIO: Float = 0.25f

    val TICK_COUNT: Int = 36
    val TICK_STROKE_WIDTH: Float = 1.2f
    val TICK_INNER_RATIO: Float = 0.35f
    val TICK_OUTER_RATIO: Float = 0.25f
    val MAJOR_TICK_INTERVAL: Int = 6
    val MAJOR_TICK_SCALE: Float = 1.5f

    val NEEDLE_LENGTH_RATIO: Float = 0.65f
    val NEEDLE_STROKE_WIDTH: Float = 3f
    val NEEDLE_SHADOW_WIDTH: Float = 6f
    val NEEDLE_SHADOW_ALPHA: Float = 0.2f
    val NEEDLE_BASE_RADIUS: Float = 8f

    val CENTER_DOT_RADIUS: Float = 4f
    val CENTER_GLOW_RADIUS: Float = 14f

    val LABEL_OFFSET_RATIO: Float = 0.22f

    val WARNING_THRESHOLD_HIGH: Float = 0.9f
    val WARNING_THRESHOLD_MEDIUM: Float = 0.75f
    val WARNING_THRESHOLD_LOW: Float = 0.5f

    val VALUE_FONT_SIZE: Int = 28
    val UNIT_FONT_SIZE: Int = 12
    val LABEL_FONT_SIZE: Int = 11

    val RPM_MAX: Float = 8000f
    val SPEED_MAX: Float = 260f
    val TEMP_MIN: Float = -40f
    val TEMP_MAX: Float = 215f

    const val LABEL_RPM = "RPM"
    const val LABEL_SPEED = "km/h"
    const val LABEL_COOLANT = "°C"
}
