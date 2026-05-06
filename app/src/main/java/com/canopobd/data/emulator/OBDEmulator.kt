package com.canopobd.data.emulator

import com.canopobd.bluetooth.Mode22TurboData
import com.canopobd.data.model.OBDData
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max

class OBDEmulator(
    private val vehicleType: VehicleType = VehicleType.OPEL_ASTRA_J_14T
) {
    private var simulationTime = 0L
    private var engineRunning = true
    private var throttlePosition = 0.0
    private var targetRpm = 850.0
    private var currentRpm = 850.0
    private var currentSpeed = 0.0
    private var currentCoolantTemp = 25.0
    private var currentFuelLevel = 75.0
    private var currentBoost = 0.0
    private var driveCyclePhase = 0

    enum class VehicleType {
        OPEL_ASTRA_J_14T,
        GENERIC_TURBO,
        GENERIC_NA
    }

    fun connect() {
        engineRunning = true
        simulationTime = 0
        throttlePosition = 0.0
        targetRpm = 850.0
        currentRpm = 850.0
        currentSpeed = 0.0
        currentCoolantTemp = 25.0
        currentFuelLevel = 75.0
        currentBoost = 0.0
        driveCyclePhase = 0
    }

    fun disconnect() {
        engineRunning = false
    }

    fun generateData(pollIntervalMs: Long = 500): OBDData {
        simulationTime += pollIntervalMs

        updateSimulation()

        val timeSeconds = simulationTime / 1000.0
        val noise = { base: Double, amplitude: Double ->
            base + (sin(timeSeconds * 3.7) * amplitude * 0.3 + cos(timeSeconds * 2.3) * amplitude * 0.2)
        }

        return OBDData(
            rpm = currentRpm,
            speed = currentSpeed,
            coolantTemp = currentCoolantTemp,
            intakeTemp = currentCoolantTemp - 5 + sin(timeSeconds * 0.1) * 3,
            throttle = throttlePosition,
            engineLoad = calculateEngineLoad(),
            fuelLevel = currentFuelLevel,
            batteryVoltage = 12.8 + sin(timeSeconds * 0.05) * 0.2,
            timingAdvance = calculateTimingAdvance(),
            mafRate = calculateMafRate(),
            fuelPressure = 350.0 + throttlePosition * 150,
            intakePressure = calculateIntakePressure(),
            runTime = timeSeconds,
            fuelRailPressure = 3800.0 + throttlePosition * 1500 + sin(timeSeconds * 2) * 100,
            commandedEGR = if (currentCoolantTemp > 80) (5.0 + sin(timeSeconds * 0.5) * 2) else 0.0,
            egrTemp = if (currentCoolantTemp > 80) currentCoolantTemp + 15 + sin(timeSeconds) * 5 else 30.0,
            commandedEvapPurge = if (currentCoolantTemp > 60) 15.0 + throttlePosition * 10 else 5.0,
            barometricPressure = 101.0 + sin(timeSeconds * 0.01) * 2,
            o2VoltageB1S1 = 0.45 + sin(timeSeconds * 4) * 0.35,
            o2VoltageB1S2 = 0.45 + sin(timeSeconds * 4 + 1) * 0.35,
            catalystTemp = currentCoolantTemp + 50 + throttlePosition * 100,
            controlModuleVoltage = 13.8 + sin(timeSeconds * 0.1) * 0.3,
            absoluteLoadValue = calculateEngineLoad(),
            engineFuelRate = calculateFuelRate(),
            shortTermFuelTrimB1 = -1.5 + sin(timeSeconds * 0.3) * 2,
            longTermFuelTrimB1 = -0.8 + sin(timeSeconds * 0.1) * 1,
            shortTermFuelTrimB2 = -1.2 + sin(timeSeconds * 0.25) * 1.5,
            longTermFuelTrimB2 = -0.5 + sin(timeSeconds * 0.15) * 0.8,
            fuelAirRatio = 1.0 + sin(timeSeconds * 4) * 0.05,
            acceleratorPosD = throttlePosition,
            throttleC = throttlePosition * 0.98,
            throttleActuator = throttlePosition * 1.02,
            hybridBatteryRemaining = 0.0,
            vin = "W0LSHGE1SB1234567",
            timestamp = System.currentTimeMillis(),
            boostPressure = currentBoost,
            vgtControl = 45.0 + throttlePosition * 20,
            wastegateControl = calculateWastegateDuty(),
            exhaustPressure = 105.0 + currentBoost * 0.5,
            turboRpm = calculateTurboRpm(),
            chargeAirCoolerTemp = currentCoolantTemp + 10 + currentBoost * 15 + sin(timeSeconds * 0.5) * 5,
            egtBank1 = calculateEgt(),
            egtBank2 = calculateEgt() + sin(timeSeconds * 0.3) * 20,
            fuelSystemStatus = 1.0,
            actualTorque = calculateTorque(),
            demandTorque = calculateDemandTorque(),
            referenceTorque = 200.0,
            ethanolPercent = 10.0,
            oilTemp = currentCoolantTemp - 5 + sin(timeSeconds * 0.2) * 3,
            turboBoostVacuum = if (currentBoost < 0) currentBoost * 100 else 0.0,
            acceleratorPosE = throttlePosition * 0.95,
            engineRuntimeMil = if (currentCoolantTemp > 80) 120.0 else 0.0,
            alternatorDuty = 55.0 + throttlePosition * 30 + sin(timeSeconds * 0.5) * 5,
            o2VoltageB1S3 = 0.45 + sin(timeSeconds * 4 + 2) * 0.3,
            o2VoltageB2S1 = 0.45 + sin(timeSeconds * 4 + 0.5) * 0.35,
            o2VoltageB2S2 = 0.45 + sin(timeSeconds * 4 + 1.5) * 0.35,
            intakeAirTemp2 = currentCoolantTemp - 8,
            turboOilPressure = 1.5 + currentRpm / 4000.0 + currentBoost * 0.3,
            turboInletTemp = calculateTurboInletTemp(),
            turboOutletTemp = calculateTurboOutletTemp(),
            turboWastegateB = calculateWastegateDuty(),
            turboBoostB = currentBoost * 100,
            turboVgtPosition = 45.0 + throttlePosition * 25,
            turboWaterCoolFlow = 60.0 + currentBoost * 20,
            turboCompInletTemp = currentCoolantTemp + 5,
            turboCompOutletTemp = currentCoolantTemp + 20 + currentBoost * 25,
            turboTurbineInletTemp = calculateTurbineInletTemp(),
            turboTurbineOutletTemp = calculateTurbineOutletTemp(),
            turboBoostAbsolute = 101.0 + currentBoost * 100,
            turboActuatorDuty = calculateWastegateDuty(),
            warmupCatalyst = min(100.0, currentCoolantTemp / 90.0 * 100),
            catalystTempB1S2 = currentCoolantTemp + 60 + throttlePosition * 80,
            catalystTempB2S1 = currentCoolantTemp + 58 + throttlePosition * 82,
            catalystTempB2S2 = currentCoolantTemp + 62 + throttlePosition * 78,
            engineTorqueMode22 = calculateTorque(),
            requestedTorqueMode22 = calculateDemandTorque(),
            boostPressureActualMode22 = currentBoost * 100,
            boostPressureTargetMode22 = calculateTargetBoost() * 100,
            wastegatePositionMode22 = calculateWastegateDuty(),
            turboRpmMode22 = calculateTurboRpm(),
            oilTempMode22 = currentCoolantTemp - 5 + sin(timeSeconds * 0.2) * 3,
            coolantTempMode22 = currentCoolantTemp,
            intakeAirTempMode22 = currentCoolantTemp - 5 + sin(timeSeconds * 0.1) * 3,
            fuelRailPressureMode22 = 3800.0 + throttlePosition * 1500,
            injectorPulseWidth = 2.5 + throttlePosition * 4 + currentRpm / 2000,
            vvtIntakeMode22 = sin(timeSeconds * 0.3) * 15,
            vvtExhaustMode22 = sin(timeSeconds * 0.25 + 1) * 10,
            fuelConsumptionInstant = calculateFuelRate(),
            fuelConsumptionAverage = 6.5 + throttlePosition * 3,
            afrRatioMode22 = 14.7 + sin(timeSeconds * 4) * 0.5,
            distanceWithMil = simulationTime / 1000.0 * currentSpeed / 3600.0
        )
    }

    fun generateMode22Data(): Mode22TurboData {
        val timeSeconds = simulationTime / 1000.0
        return Mode22TurboData(
            turboBoostActual = currentBoost * 100,
            turboBoostTarget = calculateTargetBoost() * 100,
            wastegateDuty = calculateWastegateDuty(),
            turboSpeed = calculateTurboRpm(),
            engineTorque = calculateTorque(),
            timestamp = System.currentTimeMillis()
        )
    }

    fun setThrottle(position: Double) {
        throttlePosition = position.coerceIn(0.0, 100.0)
        targetRpm = when {
            throttlePosition < 1 -> 850.0
            throttlePosition < 10 -> 850.0 + throttlePosition * 100
            throttlePosition < 30 -> 850.0 + throttlePosition * 120
            throttlePosition < 60 -> 2500.0 + (throttlePosition - 30) * 50
            throttlePosition < 80 -> 4000.0 + (throttlePosition - 60) * 75
            else -> 5500.0 + (throttlePosition - 80) * 50
        }
        targetRpm = targetRpm.coerceIn(850.0, 6500.0)
    }

    fun setSpeed(speed: Double) {
        currentSpeed = speed.coerceIn(0.0, 220.0)
    }

    fun triggerDriveCycle() {
        driveCyclePhase = (driveCyclePhase + 1) % 8
    }

    private fun updateSimulation() {
        currentRpm += (targetRpm - currentRpm) * 0.1
        currentRpm += (Math.random() - 0.5) * 20

        if (currentCoolantTemp < 90) {
            currentCoolantTemp += 0.02
        }

        if (throttlePosition > 10 && currentRpm > 2000) {
            currentBoost = min(1.0, throttlePosition / 100 * 0.8 + (currentRpm - 2000) / 8000 * 0.2)
            currentBoost += (Math.random() - 0.5) * 0.02
        } else {
            currentBoost = max(-0.3, currentBoost - 0.02)
        }

        currentSpeed = when {
            throttlePosition < 1 -> max(0.0, currentSpeed - 0.5)
            throttlePosition < 20 -> min(50.0, currentSpeed + 0.3)
            throttlePosition < 50 -> min(100.0, currentSpeed + 0.5)
            throttlePosition < 80 -> min(160.0, currentSpeed + 0.8)
            else -> min(210.0, currentSpeed + 1.0)
        }
        currentSpeed += (Math.random() - 0.5) * 0.5

        currentFuelLevel = max(0.0, currentFuelLevel - throttlePosition * 0.00001)
    }

    private fun calculateEngineLoad(): Double {
        return when {
            currentRpm < 1000 -> 15.0 + throttlePosition * 0.5
            currentRpm < 3000 -> 25.0 + throttlePosition * 0.6 + (currentRpm - 1000) / 2000 * 20
            currentRpm < 5000 -> 45.0 + throttlePosition * 0.7
            else -> 60.0 + throttlePosition * 0.8
        }
    }

    private fun calculateTimingAdvance(): Double {
        return when {
            currentRpm < 1000 -> 5.0 + throttlePosition * 0.1
            currentRpm < 3000 -> 10.0 + throttlePosition * 0.15
            currentRpm < 5000 -> 25.0 - (currentRpm - 3000) / 2000 * 10 + throttlePosition * 0.1
            else -> 15.0 - (currentRpm - 5000) / 1500 * 5
        }
    }

    private fun calculateMafRate(): Double {
        return when {
            currentRpm < 1000 -> 2.0 + throttlePosition * 0.1
            currentRpm < 3000 -> 3.0 + throttlePosition * 0.2 + (currentRpm - 1000) / 2000 * 20
            currentRpm < 5000 -> 8.0 + throttlePosition * 0.8
            else -> 15.0 + throttlePosition * 1.2
        }
    }

    private fun calculateIntakePressure(): Double {
        return 100.0 + currentBoost * 100
    }

    private fun calculateFuelRate(): Double {
        return when {
            currentRpm < 1000 -> 0.5 + throttlePosition * 0.05
            currentRpm < 3000 -> 1.0 + throttlePosition * 0.15
            currentRpm < 5000 -> 2.5 + throttlePosition * 0.3
            else -> 4.0 + throttlePosition * 0.4
        }
    }

    private fun calculateTorque(): Double {
        return when {
            currentRpm < 1500 -> 80.0 + (currentRpm - 850) / 650 * 100
            currentRpm < 3000 -> 180.0 + (currentRpm - 1500) / 1500 * 40
            currentRpm < 4500 -> 220.0 - (currentRpm - 3000) / 1500 * 20
            else -> 200.0 - (currentRpm - 4500) / 2000 * 50
        } * (1 + throttlePosition / 200)
    }

    private fun calculateDemandTorque(): Double {
        return throttlePosition * 2.2
    }

    private fun calculateTurboRpm(): Double {
        return when {
            currentBoost <= 0 -> 8000.0
            currentBoost < 0.5 -> 8000 + currentBoost * 100000
            currentBoost < 0.8 -> 50000 + (currentBoost - 0.5) / 0.3 * 50000
            else -> 100000 + (currentBoost - 0.8) / 0.2 * 80000
        } + (Math.random() - 0.5) * 2000
    }

    private fun calculateEgt(): Double {
        return 200.0 + currentBoost * 400 + currentRpm / 10 + throttlePosition * 2
    }

    private fun calculateWastegateDuty(): Double {
        return when {
            currentBoost <= 0 -> 90.0
            currentBoost < 0.3 -> 85.0 - currentBoost * 50
            currentBoost < 0.7 -> 70.0 - (currentBoost - 0.3) * 50
            else -> 50.0 - (currentBoost - 0.7) * 30
        } + (Math.random() - 0.5) * 2
    }

    private fun calculateTargetBoost(): Double {
        return when {
            throttlePosition < 30 -> 0.0
            throttlePosition < 60 -> 0.3 + (throttlePosition - 30) / 30 * 0.3
            throttlePosition < 90 -> 0.6 + (throttlePosition - 60) / 30 * 0.25
            else -> 0.85 + (throttlePosition - 90) / 10 * 0.15
        }
    }

    private fun calculateTurboInletTemp(): Double {
        return currentCoolantTemp + 30 + currentBoost * 50
    }

    private fun calculateTurboOutletTemp(): Double {
        return currentCoolantTemp + 60 + currentBoost * 80
    }

    private fun calculateTurbineInletTemp(): Double {
        return calculateEgt() + 50
    }

    private fun calculateTurbineOutletTemp(): Double {
        return calculateTurbineInletTemp() - 50
    }
}
