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

data class WindowState(
    val driverPos: Int = FULLY_CLOSED,
    val passengerPos: Int = FULLY_CLOSED,
    val rearLeftPos: Int = FULLY_CLOSED,
    val rearRightPos: Int = FULLY_CLOSED
) {
    fun isFullyClosed(): Boolean =
        driverPos == FULLY_CLOSED && passengerPos == FULLY_CLOSED &&
            rearLeftPos == FULLY_CLOSED && rearRightPos == FULLY_CLOSED

    fun isFullyOpen(): Boolean =
        driverPos == FULLY_OPEN && passengerPos == FULLY_OPEN &&
            rearLeftPos == FULLY_OPEN && rearRightPos == FULLY_OPEN

    fun anyOpen(): Boolean = driverPos > 0 || passengerPos > 0 || rearLeftPos > 0 || rearRightPos > 0
}

enum class WindowAction {
    DRIVER_UP, DRIVER_DOWN, DRIVER_STOP,
    PASSENGER_UP, PASSENGER_DOWN, PASSENGER_STOP,
    REAR_LEFT_UP, REAR_LEFT_DOWN, REAR_LEFT_STOP,
    REAR_RIGHT_UP, REAR_RIGHT_DOWN, REAR_RIGHT_STOP,
    ALL_UP, ALL_DOWN, ALL_STOP
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
        }
    )

    fun commandForAction(action: WindowAction): ByteArray =
        actionToCommand[action]?.invoke() ?: ByteArray(0)

    fun buildPositionCommand(windowByte: Int, percent: Int): ByteArray =
        BCMProtocol.Window.buildPositionFrame(windowByte, percent)

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
        if (action.name.endsWith("_STOP")) return state
        val pos = if (action.name.endsWith("_UP")) FULLY_CLOSED else FULLY_OPEN
        if (action.name.startsWith("ALL_")) return WindowState(pos, pos, pos, pos)
        return setSingleWindow(state, action, pos)
    }

    fun evaluate(state: WindowState): WindowSafetyEvaluation {
        val openCount = listOf(
            state.driverPos, state.passengerPos, state.rearLeftPos, state.rearRightPos
        ).count { it > 0 }

        val allOpen = openCount == TOTAL_WINDOWS
        val anyOpen = openCount > 0

        val warning = when {
            allOpen -> "Alle Fenster offen — bei Regen oder Diebstahlgefahr schliessen."
            anyOpen -> "$openCount Fenster offen — Sicherheit pruefen."
            else -> null
        }

        val safetyScore = when (openCount) {
            FULLY_CLOSED -> SAFETY_SCORE_ALL_CLOSED
            1 -> SAFETY_SCORE_ONE_OPEN
            2 -> SAFETY_SCORE_TWO_OPEN
            THREE_WINDOWS_OPEN -> SAFETY_SCORE_THREE_OPEN
            else -> SAFETY_SCORE_ALL_OPEN
        }

        return WindowSafetyEvaluation(
            openWindowCount = openCount,
            allOpen = allOpen,
            anyOpen = anyOpen,
            safetyScore = safetyScore,
            warning = warning
        )
    }
}

data class WindowSafetyEvaluation(
    val openWindowCount: Int,
    val allOpen: Boolean,
    val anyOpen: Boolean,
    val safetyScore: Int,
    val warning: String?
)
