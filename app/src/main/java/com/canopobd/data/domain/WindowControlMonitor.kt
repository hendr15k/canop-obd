package com.canopobd.data.domain

import com.canopobd.protocol.BCMProtocol

private const val FULLY_OPEN = 100
private const val FULLY_CLOSED = 0
private const val TOTAL_WINDOWS = 4
private const val THREE_WINDOWS_OPEN = 3

private const val SAFETY_SCORE_ALL_CLOSED = 100
private const val SAFETY_SCORE_ONE_OPEN = 85
private const val SAFETY_SCORE_TWO_OPEN = 70
private const val SAFETY_SCORE_THREE_OPEN = 55
private const val SAFETY_SCORE_ALL_OPEN = 40

const val AUTO_STOP_DELAY_NORMAL_MS = 4_000L
const val AUTO_STOP_DELAY_EXPRESS_MS = 1_500L

private const val PRESET_25 = 25
private const val PRESET_50 = 50
private const val PRESET_75 = 75
private const val PRESET_VENT = 20

private const val POSITION_DRIVER = 0x01
private const val POSITION_PASSENGER = 0x02
private const val POSITION_REAR_LEFT = 0x03
private const val POSITION_REAR_RIGHT = 0x04
private const val POSITION_SUNROOF = 0x05
private const val POSITION_ALL = 0x00

private const val WINDOW_STATUS_DID_RESPONSE_PREFIX_LEN = 4
private const val DID_HI = 0xFF
private const val DID_LO_WINDOW = 0x02
private const val STATUS_CLOSED_VALUE = 0xFF
private const val DATA_BYTE_INDEX_OFFSET = 1
private const val FIRST_DATA_BYTE_INDEX = 3
private const val BYTE_MASK = 0xFF

enum class WindowTarget {
    DRIVER, PASSENGER, REAR_LEFT, REAR_RIGHT, SUNROOF, ALL;

    fun toByte(): Int = when (this) {
        DRIVER -> POSITION_DRIVER
        PASSENGER -> POSITION_PASSENGER
        REAR_LEFT -> POSITION_REAR_LEFT
        REAR_RIGHT -> POSITION_REAR_RIGHT
        SUNROOF -> POSITION_SUNROOF
        ALL -> POSITION_ALL
    }

    companion object {
        fun fromByte(b: Int): WindowTarget? = when (b) {
            POSITION_DRIVER -> DRIVER
            POSITION_PASSENGER -> PASSENGER
            POSITION_REAR_LEFT -> REAR_LEFT
            POSITION_REAR_RIGHT -> REAR_RIGHT
            POSITION_SUNROOF -> SUNROOF
            POSITION_ALL -> ALL
            else -> null
        }
    }
}

data class WindowState(
    val driverPos: Int = FULLY_CLOSED,
    val passengerPos: Int = FULLY_CLOSED,
    val rearLeftPos: Int = FULLY_CLOSED,
    val rearRightPos: Int = FULLY_CLOSED,
    val sunroofPos: Int = FULLY_CLOSED
) {
    fun isFullyClosed(): Boolean =
        driverPos == FULLY_CLOSED && passengerPos == FULLY_CLOSED &&
            rearLeftPos == FULLY_CLOSED && rearRightPos == FULLY_CLOSED &&
            sunroofPos == FULLY_CLOSED

    fun isFullyOpen(): Boolean =
        driverPos == FULLY_OPEN && passengerPos == FULLY_OPEN &&
            rearLeftPos == FULLY_OPEN && rearRightPos == FULLY_OPEN &&
            sunroofPos == FULLY_OPEN

    fun anyOpen(): Boolean = driverPos > 0 || passengerPos > 0 || rearLeftPos > 0 ||
        rearRightPos > 0 || sunroofPos > 0

    fun positionFor(target: WindowTarget): Int = when (target) {
        WindowTarget.DRIVER -> driverPos
        WindowTarget.PASSENGER -> passengerPos
        WindowTarget.REAR_LEFT -> rearLeftPos
        WindowTarget.REAR_RIGHT -> rearRightPos
        WindowTarget.ALL -> if (anyOpen()) FULLY_OPEN else FULLY_CLOSED
        WindowTarget.SUNROOF -> sunroofPos
    }

    fun withPosition(target: WindowTarget, pos: Int): WindowState = when (target) {
        WindowTarget.DRIVER -> copy(driverPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN))
        WindowTarget.PASSENGER -> copy(passengerPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN))
        WindowTarget.REAR_LEFT -> copy(rearLeftPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN))
        WindowTarget.REAR_RIGHT -> copy(rearRightPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN))
        WindowTarget.SUNROOF -> copy(sunroofPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN))
        WindowTarget.ALL -> copy(
            driverPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN),
            passengerPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN),
            rearLeftPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN),
            rearRightPos = pos.coerceIn(FULLY_CLOSED, FULLY_OPEN),
            sunroofPos = sunroofPos
        )
    }
}

enum class WindowAction {
    DRIVER_UP, DRIVER_DOWN, DRIVER_STOP,
    PASSENGER_UP, PASSENGER_DOWN, PASSENGER_STOP,
    REAR_LEFT_UP, REAR_LEFT_DOWN, REAR_LEFT_STOP,
    REAR_RIGHT_UP, REAR_RIGHT_DOWN, REAR_RIGHT_STOP,
    ALL_UP, ALL_DOWN, ALL_STOP,
    ALL_VENTILATE,
    SUNROOF_OPEN, SUNROOF_CLOSE, SUNROOF_STOP, SUNROOF_VENT
}

data class WindowPositionPreset(
    val percent: Int,
    val label: String
)

object WindowPresets {
    val POSITIONS: List<WindowPositionPreset> = listOf(
        WindowPositionPreset(PRESET_25, "25%"),
        WindowPositionPreset(PRESET_50, "50%"),
        WindowPositionPreset(PRESET_75, "75%")
    )
    const val VENTILATE_PERCENT: Int = PRESET_VENT
    const val SUNROOF_VENT_PERCENT: Int = 50
}

data class WindowControlResult(
    val action: WindowAction,
    val hexCommand: String,
    val success: Boolean,
    val errorMessage: String? = null
)

object WindowControlMonitor {

    private val actionToCommand: Map<WindowAction, () -> ByteArray> = mapOf(
        WindowAction.DRIVER_UP to BCMProtocol.Window::closeDriver,
        WindowAction.DRIVER_DOWN to BCMProtocol.Window::openDriver,
        WindowAction.DRIVER_STOP to BCMProtocol.Window::stopDriver,
        WindowAction.PASSENGER_UP to BCMProtocol.Window::closePassenger,
        WindowAction.PASSENGER_DOWN to BCMProtocol.Window::openPassenger,
        WindowAction.PASSENGER_STOP to {
            BCMProtocol.Window.buildDirectFrame(
                BCMProtocol.Window.WINDOW_PASSENGER, BCMProtocol.Window.DIRECTION_STOP
            )
        },
        WindowAction.REAR_LEFT_UP to BCMProtocol.Window::closeRearLeft,
        WindowAction.REAR_LEFT_DOWN to BCMProtocol.Window::openRearLeft,
        WindowAction.REAR_LEFT_STOP to {
            BCMProtocol.Window.buildDirectFrame(
                BCMProtocol.Window.WINDOW_REAR_LEFT, BCMProtocol.Window.DIRECTION_STOP
            )
        },
        WindowAction.REAR_RIGHT_UP to BCMProtocol.Window::closeRearRight,
        WindowAction.REAR_RIGHT_DOWN to BCMProtocol.Window::openRearRight,
        WindowAction.REAR_RIGHT_STOP to {
            BCMProtocol.Window.buildDirectFrame(
                BCMProtocol.Window.WINDOW_REAR_RIGHT, BCMProtocol.Window.DIRECTION_STOP
            )
        },
        WindowAction.ALL_UP to BCMProtocol.Window::closeAll,
        WindowAction.ALL_DOWN to BCMProtocol.Window::openAll,
        WindowAction.ALL_STOP to {
            BCMProtocol.Window.buildDirectFrame(
                BCMProtocol.Window.WINDOW_ALL, BCMProtocol.Window.DIRECTION_STOP
            )
        },
        WindowAction.ALL_VENTILATE to {
            BCMProtocol.Window.buildPositionFrame(POSITION_ALL, WindowPresets.VENTILATE_PERCENT)
        },
        WindowAction.SUNROOF_OPEN to BCMProtocol.Sunroof::openFrame,
        WindowAction.SUNROOF_CLOSE to BCMProtocol.Sunroof::closeFrame,
        WindowAction.SUNROOF_STOP to BCMProtocol.Sunroof::stopFrame,
        WindowAction.SUNROOF_VENT to BCMProtocol.Sunroof::ventFrame
    )

    fun commandForAction(action: WindowAction): ByteArray =
        actionToCommand[action]?.invoke() ?: ByteArray(0)

    fun buildPositionCommand(windowByte: Int, percent: Int): ByteArray =
        BCMProtocol.Window.buildPositionFrame(windowByte, percent)

    fun buildSetPosition(target: WindowTarget, percent: Int): ByteArray =
        BCMProtocol.Window.buildPositionFrame(target.toByte(), percent.coerceIn(FULLY_CLOSED, FULLY_OPEN))

    private fun setSingleWindow(
        state: WindowState,
        action: WindowAction,
        pos: Int
    ): WindowState = when (action) {
        WindowAction.DRIVER_UP, WindowAction.DRIVER_DOWN -> state.copy(driverPos = pos)
        WindowAction.PASSENGER_UP, WindowAction.PASSENGER_DOWN -> state.copy(passengerPos = pos)
        WindowAction.REAR_LEFT_UP, WindowAction.REAR_LEFT_DOWN -> state.copy(rearLeftPos = pos)
        WindowAction.REAR_RIGHT_UP, WindowAction.REAR_RIGHT_DOWN -> state.copy(rearRightPos = pos)
        else -> state
    }

    @Suppress("ReturnCount")
    fun updateStateFromAction(state: WindowState, action: WindowAction): WindowState {
        if (action == WindowAction.SUNROOF_STOP) return state
        if (action.name.endsWith("_STOP")) return state
        if (action == WindowAction.ALL_VENTILATE) {
            return state.copy(
                driverPos = WindowPresets.VENTILATE_PERCENT,
                passengerPos = WindowPresets.VENTILATE_PERCENT,
                rearLeftPos = WindowPresets.VENTILATE_PERCENT,
                rearRightPos = WindowPresets.VENTILATE_PERCENT
            )
        }
        if (action == WindowAction.SUNROOF_OPEN) return state.copy(sunroofPos = FULLY_OPEN)
        if (action == WindowAction.SUNROOF_CLOSE) return state.copy(sunroofPos = FULLY_CLOSED)
        if (action == WindowAction.SUNROOF_VENT) return state.copy(sunroofPos = WindowPresets.SUNROOF_VENT_PERCENT)
        val pos = if (action.name.endsWith("_UP")) FULLY_CLOSED else FULLY_OPEN
        if (action.name.startsWith("ALL_")) {
            return state.copy(
                driverPos = pos,
                passengerPos = pos,
                rearLeftPos = pos,
                rearRightPos = pos
            )
        }
        return setSingleWindow(state, action, pos)
    }

    fun applyPositionPreset(state: WindowState, target: WindowTarget, percent: Int): WindowState =
        state.withPosition(target, percent)

    @Suppress("CyclomaticComplexMethod")
    fun evaluate(state: WindowState): WindowSafetyEvaluation {
        val windowPositions = listOf(
            state.driverPos, state.passengerPos, state.rearLeftPos, state.rearRightPos
        )
        val openCount = windowPositions.count { it > 0 }
        val sunroofOpen = state.sunroofPos > 0
        val totalOpen = openCount + if (sunroofOpen) 1 else 0

        val allOpen = openCount == TOTAL_WINDOWS
        val anyOpen = openCount > 0 || sunroofOpen

        val warning = when {
            allOpen && sunroofOpen -> "Alle Fenster + Schiebedach offen."
            allOpen -> "Alle Fenster offen — bei Regen oder Diebstahlgefahr schliessen."
            sunroofOpen && openCount > 0 -> "Schiebedach + $openCount Fenster offen."
            sunroofOpen -> "Schiebedach offen — vor Regen schliessen."
            anyOpen -> "$openCount Fenster offen — Sicherheit pruefen."
            else -> null
        }

        val safetyScore = when (totalOpen) {
            FULLY_CLOSED -> SAFETY_SCORE_ALL_CLOSED
            1 -> SAFETY_SCORE_ONE_OPEN
            2 -> SAFETY_SCORE_TWO_OPEN
            THREE_WINDOWS_OPEN -> SAFETY_SCORE_THREE_OPEN
            else -> SAFETY_SCORE_ALL_OPEN
        }

        return WindowSafetyEvaluation(
            openWindowCount = openCount,
            sunroofOpen = sunroofOpen,
            allOpen = allOpen,
            anyOpen = anyOpen,
            safetyScore = safetyScore,
            warning = warning
        )
    }

    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    fun parseStatusFromDidResponse(rawBytes: ByteArray): WindowState? {
        if (rawBytes.size < WINDOW_STATUS_DID_RESPONSE_PREFIX_LEN) return null
        val didHi = rawBytes[1].toInt() and BYTE_MASK
        val didLo = rawBytes[2].toInt() and BYTE_MASK
        if (didHi != DID_HI || didLo != DID_LO_WINDOW) return null
        val dataBytes = rawBytes.copyOfRange(FIRST_DATA_BYTE_INDEX, rawBytes.size)

        var parsed = WindowState()
        dataBytes.forEachIndexed { idx, byte ->
            val b = byte.toInt() and BYTE_MASK
            val windowByte = idx + DATA_BYTE_INDEX_OFFSET
            val target = WindowTarget.fromByte(windowByte) ?: return@forEachIndexed
            val pos = when {
                b == STATUS_CLOSED_VALUE || b == FULLY_CLOSED -> FULLY_CLOSED
                b > FULLY_OPEN -> FULLY_OPEN
                else -> b
            }
            parsed = when (target) {
                WindowTarget.DRIVER -> parsed.copy(driverPos = pos)
                WindowTarget.PASSENGER -> parsed.copy(passengerPos = pos)
                WindowTarget.REAR_LEFT -> parsed.copy(rearLeftPos = pos)
                WindowTarget.REAR_RIGHT -> parsed.copy(rearRightPos = pos)
                WindowTarget.SUNROOF -> parsed.copy(sunroofPos = pos)
                WindowTarget.ALL -> parsed
            }
        }
        return parsed
    }
}

data class WindowSafetyEvaluation(
    val openWindowCount: Int,
    val sunroofOpen: Boolean,
    val allOpen: Boolean,
    val anyOpen: Boolean,
    val safetyScore: Int,
    val warning: String?
)
