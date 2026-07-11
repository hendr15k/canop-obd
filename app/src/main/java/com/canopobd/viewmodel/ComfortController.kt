package com.canopobd.viewmodel

import android.util.Log
import com.canopobd.data.domain.*
import com.canopobd.protocol.BCMCommandMapper
import com.canopobd.protocol.BCMProtocol
import com.canopobd.ui.comfort.ComfortCommand
import com.canopobd.ui.climate.ClimateCommand
import com.canopobd.ui.climate.ClimateState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComfortController(
    private val scope: CoroutineScope,
    private val sendRawCommand: suspend (String) -> String?
) {

    companion object {
        private const val TAG = "ComfortController"
    }

    // --- Climate State ---
    val climateState = MutableStateFlow(ClimateState())

    // --- Window State ---
    val windowState = MutableStateFlow(WindowState())
    val windowChildLock = MutableStateFlow(false)
    val windowIsMoving = MutableStateFlow(false)
    val windowExpressMode = MutableStateFlow(false)
    private var windowAutoStopJob: Job? = null

    fun sendClimateCommand(command: ClimateCommand) {
        scope.launch(Dispatchers.IO) {
            val frame = when (command) {
                is ClimateCommand.AC_ON -> {
                    climateState.value = climateState.value.copy(isACEnabled = true)
                    bytesToHex(BCMProtocol.Climate.acOnFrame())
                }
                is ClimateCommand.AC_OFF -> {
                    climateState.value = climateState.value.copy(isACEnabled = false)
                    bytesToHex(BCMProtocol.Climate.acOffFrame())
                }
                is ClimateCommand.AUTO_MODE -> {
                    climateState.value = climateState.value.copy(isAutoMode = true, fanSpeed = 3)
                    bytesToHex(BCMProtocol.Climate.autoModeFrame())
                }
                is ClimateCommand.RECIRC_ON -> {
                    climateState.value = climateState.value.copy(isRecirculation = true)
                    bytesToHex(BCMProtocol.Climate.recirculationFrame(true))
                }
                is ClimateCommand.RECIRC_OFF -> {
                    climateState.value = climateState.value.copy(isRecirculation = false)
                    bytesToHex(BCMProtocol.Climate.recirculationFrame(false))
                }
                is ClimateCommand.DEFROST_FRONT -> {
                    climateState.value = climateState.value.copy(isFrontDefrost = true)
                    bytesToHex(BCMProtocol.Climate.defrostFrontFrame())
                }
                is ClimateCommand.DEFROST_FRONT_OFF -> {
                    climateState.value = climateState.value.copy(isFrontDefrost = false)
                    bytesToHex(BCMProtocol.Climate.defrostFrontOffFrame())
                }
                is ClimateCommand.DEFROST_REAR -> {
                    climateState.value = climateState.value.copy(isRearDefrost = true)
                    bytesToHex(BCMProtocol.Climate.defrostRearFrame())
                }
                is ClimateCommand.DEFROST_REAR_OFF -> {
                    climateState.value = climateState.value.copy(isRearDefrost = false)
                    bytesToHex(BCMProtocol.Climate.defrostRearOffFrame())
                }
                is ClimateCommand.DEFROST_MIRRORS -> {
                    climateState.value = climateState.value.copy(isMirrorDefrost = true)
                    bytesToHex(BCMProtocol.Climate.defrostAllFrame())
                }
                is ClimateCommand.DEFROST_MIRRORS_OFF -> {
                    climateState.value = climateState.value.copy(isMirrorDefrost = false)
                    bytesToHex(BCMProtocol.Climate.defrostMirrorsOffFrame())
                }
                is ClimateCommand.FAN_OFF -> {
                    climateState.value = climateState.value.copy(fanSpeed = 0)
                    bytesToHex(BCMProtocol.Climate.blowerSpeedFrame(0))
                }
                is ClimateCommand.FAN_SPEED_UP -> {
                    val newSpeed = (climateState.value.fanSpeed + 1).coerceAtMost(6)
                    climateState.value = climateState.value.copy(fanSpeed = newSpeed)
                    bytesToHex(BCMProtocol.Climate.blowerSpeedFrame(newSpeed))
                }
                is ClimateCommand.FAN_SPEED_DOWN -> {
                    val newSpeed = (climateState.value.fanSpeed - 1).coerceAtLeast(0)
                    climateState.value = climateState.value.copy(fanSpeed = newSpeed)
                    bytesToHex(BCMProtocol.Climate.blowerSpeedFrame(newSpeed))
                }
                is ClimateCommand.FAN_SPEED_1 -> {
                    climateState.value = climateState.value.copy(fanSpeed = 1)
                    bytesToHex(BCMProtocol.Climate.blowerSpeedFrame(1))
                }
                is ClimateCommand.FAN_SPEED_3 -> {
                    climateState.value = climateState.value.copy(fanSpeed = 3)
                    bytesToHex(BCMProtocol.Climate.blowerSpeedFrame(3))
                }
                is ClimateCommand.FAN_MAX -> {
                    climateState.value = climateState.value.copy(fanSpeed = 6)
                    bytesToHex(BCMProtocol.Climate.blowerSpeedFrame(6))
                }
                is ClimateCommand.SET_TEMP_DRIVER -> {
                    bytesToHex(BCMProtocol.Climate.temperatureFrame(climateState.value.driverTemp))
                }
                is ClimateCommand.SET_TEMP_PASSENGER -> {
                    bytesToHex(BCMProtocol.Climate.temperatureFrame(climateState.value.passengerTemp))
                }
                is ClimateCommand.TOGGLE_SYNC -> {
                    val newState = climateState.value.copy(syncEnabled = !climateState.value.syncEnabled)
                    climateState.value = newState
                    Log.d(TAG, "Sync toggled: ${newState.syncEnabled}")
                    null
                }
            }
            frame?.let { sendRawCommand(it) }
        }
    }

    fun updateClimateState(state: ClimateState) {
        climateState.value = state
    }

    fun updateWindowState(state: WindowState) {
        windowState.value = state
    }

    fun toggleWindowChildLock() {
        windowChildLock.value = !windowChildLock.value
    }

    fun toggleWindowExpressMode() {
        windowExpressMode.value = !windowExpressMode.value
    }

    fun sendWindowPosition(target: WindowTarget, percent: Int) {
        if (windowChildLock.value) return
        scope.launch(Dispatchers.IO) {
            val frame = WindowControlMonitor.buildSetPosition(target, percent)
            windowState.value = WindowControlMonitor.applyPositionPreset(windowState.value, target, percent)
            sendRawCommand(bytesToHex(frame))
        }
    }

    fun sendWindowVentilateAll() {
        if (windowChildLock.value) return
        scope.launch(Dispatchers.IO) {
            val frame = WindowControlMonitor.commandForAction(WindowAction.ALL_VENTILATE)
            windowState.value = WindowControlMonitor.updateStateFromAction(windowState.value, WindowAction.ALL_VENTILATE)
            sendRawCommand(bytesToHex(frame))
            scheduleWindowAutoStop()
        }
    }

    fun sendSunroofCommand(action: WindowAction) {
        if (windowChildLock.value && action != WindowAction.SUNROOF_CLOSE) return
        scope.launch(Dispatchers.IO) {
            val frame = WindowControlMonitor.commandForAction(action)
            windowState.value = WindowControlMonitor.updateStateFromAction(windowState.value, action)
            sendRawCommand(bytesToHex(frame))
            if (action.name.startsWith("SUNROOF_") && action != WindowAction.SUNROOF_STOP) {
                scheduleWindowAutoStop()
            }
        }
    }

    fun sendWindowCommand(command: WindowAction) {
        if (windowChildLock.value && command != WindowAction.ALL_UP) return
        scope.launch(Dispatchers.IO) {
            val frame = WindowControlMonitor.commandForAction(command)
            windowState.value = WindowControlMonitor.updateStateFromAction(windowState.value, command)
            sendRawCommand(bytesToHex(frame))
            if (command.name.endsWith("_UP") || command.name.endsWith("_DOWN")) {
                scheduleWindowAutoStop()
            }
        }
    }

    private fun scheduleWindowAutoStop() {
        windowAutoStopJob?.cancel()
        val delayMs = if (windowExpressMode.value) {
            AUTO_STOP_DELAY_EXPRESS_MS
        } else {
            AUTO_STOP_DELAY_NORMAL_MS
        }
        windowIsMoving.value = true
        windowAutoStopJob = scope.launch(Dispatchers.IO) {
            try {
                delay(delayMs)
                val stopFrame = WindowControlMonitor.commandForAction(WindowAction.ALL_STOP)
                sendRawCommand(bytesToHex(stopFrame))
            } catch (_: kotlinx.coroutines.CancellationException) {
            } finally {
                windowIsMoving.value = false
            }
        }
    }

    fun pollWindowStatus(sendRawCommand: suspend (String) -> String?) {
        scope.launch(Dispatchers.IO) {
            val raw = sendRawCommand("22FF02") ?: return@launch
            val cleaned = raw.replace(" ", "").replace("\r", "").replace("\n", "")
            if (cleaned.length < 8) return@launch
            val bytes = try {
                cleaned.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            } catch (_: NumberFormatException) {
                return@launch
            }
            val parsed = WindowControlMonitor.parseStatusFromDidResponse(bytes)
            if (parsed != null) {
                windowState.value = parsed
            }
        }
    }

    fun sendBCMCommand(command: ComfortCommand) {
        scope.launch(Dispatchers.IO) {
            val action = BCMCommandMapper.actionToATCommand(command.action.name, command.value)
            if (action != null) {
                sendRawCommand(action)
            } else {
                android.util.Log.w(
                    "ComfortController",
                    "Unmapped BCM action: ${command.action.name} (no AT command for this vehicle)"
                )
            }
        }
    }

    fun executeQuickAction(actionId: String, repository: com.canopobd.data.repository.OBDRepository) {
        scope.launch(Dispatchers.IO) {
            when (actionId) {
                "dtc_clear" -> repository.clearDTCs()
                "dtc_read" -> repository.readDTCs()
                "vin_read" -> repository.getStoredVin()
                "tpms_reset" -> repository.sendRawCommand("310302")
                "oil_reset" -> repository.sendRawCommand("310303")
                "inspection_reset" -> repository.sendRawCommand("310304")
                else -> {
                    val atCmd = BCMCommandMapper.actionToATCommand(actionId.uppercase())
                    if (atCmd != null) {
                        repository.sendRawCommand(atCmd)
                    }
                }
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
}
