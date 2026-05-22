package com.canopobd.data.domain

/**
 * Analysiert Wastegate-Funktion für BorgWarner KP39 beim A14NET
 * Der A14NET hat einen fest-geometrierten Turbo mit pneumatisch geregeltem Wastegate
 */
class WastegateHealthAnalyzer {

    // History for trend analysis
    private val dutyHistory = mutableListOf<Double>()
    private val boostDeviationHistory = mutableListOf<Double>()
    
    private class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D) {
        operator fun component1() = first
        operator fun component2() = second
        operator fun component3() = third
        operator fun component4() = fourth
    }

    enum class WastegateCondition {
        HEALTHY,              // Normal
        STUCK_OPEN,           // Mechanisch offen (Undicht)
        STUCK_CLOSED,         // Mechanisch geschlossen (Klemmt)
        WASTEGATE_LEAK,       // Unterdruck-Leck
        SOLENOID_ISSUE,       // Magnetventil Problem
        SENSOR_FAULT,         // Positionssensor defekt
        UNKNOWN               // Nicht diagnostizierbar
    }

    data class WastegateAnalysis(
        val condition: WastegateCondition,
        val currentDutyPercent: Double,
        val avgDutyPercent: Double,
        val boostDeviation: Double,  // Soll-Ist Differenz in %
        val healthScore: Int,
        val diagnosis: String,
        val recommendation: String,
        val trend: WastegateTrend = WastegateTrend.STABLE
    )

    enum class WastegateTrend(val label: String) {
        IMPROVING("Besser werdend"),
        STABLE("Stabil"),
        DEGRADING("Verschlechternd"),
        UNKNOWN("Unbekannt")
    }

    companion object {
        private const val DUTY_IDLE_MIN = 80.0    // WG offen im Leerlauf
        private const val DUTY_WOT_MAX = 60.0    // WG geschlossen bei Vollast
        private const val DUTY_STUCK_OPEN = 95.0  // WG fast immer offen
        private const val DUTY_STUCK_CLOSED = 5.0 // WG fast immer geschlossen
        private const val NORMAL_BOOST_DEV = 15.0 // 15% Abweichung tolerierbar
        private const val HISTORY_SIZE = 20
    }

    fun analyze(
        wastegateDuty: Double,
        avgWastegateDuty: Double,
        targetBoost: Double,
        actualBoost: Double,
        rpm: Double,
        engineLoad: Double
    ): WastegateAnalysis {
        // Input validation with safe defaults
        val duty = wastegateDuty.coerceIn(0.0, 100.0)
        val avgDuty = avgWastegateDuty.coerceIn(0.0, 100.0)
        val target = targetBoost.coerceAtLeast(0.0)
        val actual = actualBoost.coerceIn(-0.5, 2.0)
        
        // Update history for trend analysis
        updateHistory(duty, target, actual)

        val boostDeviation = if (target > 0.01) {
            ((actual - target) / target) * 100.0
        } else 0.0

        val (condition, healthScore, diagnosis, recommendation) = when {
            // Stuck Open - duty too high for extended period
            duty > DUTY_STUCK_OPEN -> {
                Quadruple(WastegateCondition.STUCK_OPEN, 20, 
                    "Wastegate blockiert offen - Ladedruckverlust", 
                    "Wastegate-Mechanismus und Unterdruckleitung prüfen")
            }

            // Stuck Closed  
            duty < DUTY_STUCK_CLOSED -> {
                Quadruple(WastegateCondition.STUCK_CLOSED, 30,
                    "Wastegate blockiert geschlossen - Überladung möglich",
                    "Wastegate-Aktuator auf Freigang prüfen")
            }

            // Leak Detection: Underboost + High Duty (WG trying to close but can't)
            boostDeviation < -NORMAL_BOOST_DEV && duty < 50.0 -> {
                Quadruple(WastegateCondition.WASTEGATE_LEAK, 40,
                    "Wastegate undicht - Unterladung",
                    "Wastegate-Sitz und Rückholfeder prüfen")
            }

            // Overboost + Low Duty (WG can't open enough)
            boostDeviation > NORMAL_BOOST_DEV && duty > 70.0 -> {
                Quadruple(WastegateCondition.STUCK_CLOSED, 45,
                    "Wastegate schließt nicht richtig",
                    "Unterdruckdose und Ventil prüfen")
            }

            // Abnormal Pattern at Load
            engineLoad > 60.0 && duty > 70.0 && boostDeviation > 10.0 -> {
                Quadruple(WastegateCondition.SOLENOID_ISSUE, 50,
                    "Wastegate-Regelung abnormal",
                    "Magnetventil und ECU-Signal prüfen")
            }

            // Check for sensor fault - duty stuck at specific value
            isDutyStuck() -> {
                Quadruple(WastegateCondition.SENSOR_FAULT, 55,
                    "Wastegate-Signal unplausibel - Sensorfehler",
                    "Wastegate-Positionssensor und Verkabelung prüfen")
            }

            // Normal Operation
            duty in DUTY_STUCK_CLOSED..DUTY_STUCK_OPEN -> {
                Quadruple(WastegateCondition.HEALTHY, 100,
                    "Wastegate funktioniert normal",
                    "Keine Maßnahmen erforderlich")
            }

            else -> {
                Quadruple(WastegateCondition.UNKNOWN, 60,
                    "Diagnose nicht eindeutig",
                    "Weitere Daten sammeln")
            }
        }

        // Determine trend
        val trend = determineTrend()

        return WastegateAnalysis(
            condition = condition,
            currentDutyPercent = duty,
            avgDutyPercent = avgDuty,
            boostDeviation = boostDeviation,
            healthScore = healthScore,
            diagnosis = diagnosis,
            recommendation = recommendation,
            trend = trend
        )
    }

    /**
     * Check if duty cycle is stuck at a specific value (sensor issue)
     */
    private fun isDutyStuck(): Boolean {
        if (dutyHistory.size < 10) return false
        val recent = dutyHistory.takeLast(10)
        val variance = recent.map { it - recent.average() }.map { it * it }.average()
        val stdDev = kotlin.math.sqrt(variance)
        return stdDev < 1.0 && recent.all { it in 40.0..60.0 }
    }

    /**
     * Update history for trend analysis
     */
    private fun updateHistory(duty: Double, targetBoost: Double, actualBoost: Double) {
        dutyHistory.add(duty)
        if (dutyHistory.size > HISTORY_SIZE) dutyHistory.removeAt(0)
        
        val deviation = if (targetBoost > 0.01) {
            ((actualBoost - targetBoost) / targetBoost) * 100.0
        } else 0.0
        boostDeviationHistory.add(deviation)
        if (boostDeviationHistory.size > HISTORY_SIZE) boostDeviationHistory.removeAt(0)
    }

    /**
     * Determine wastegate trend based on history
     */
    private fun determineTrend(): WastegateTrend {
        if (boostDeviationHistory.size < 5) return WastegateTrend.UNKNOWN
        
        val firstHalf = boostDeviationHistory.take(boostDeviationHistory.size / 2).average()
        val secondHalf = boostDeviationHistory.drop(boostDeviationHistory.size / 2).average()
        val change = secondHalf - firstHalf

        return when {
            change < -15.0 -> WastegateTrend.IMPROVING
            change > 20.0 -> WastegateTrend.DEGRADING
            else -> WastegateTrend.STABLE
        }
    }

    fun addWastegateDutyReading(duty: Double) {
        dutyHistory.add(duty.coerceIn(0.0, 100.0))
        if (dutyHistory.size > HISTORY_SIZE) dutyHistory.removeAt(0)
    }

    fun addBoostReading(actualBoost: Double, targetBoost: Double) {
        val deviation = if (targetBoost > 0.01) {
            ((actualBoost - targetBoost) / targetBoost) * 100.0
        } else 0.0
        boostDeviationHistory.add(deviation)
        if (boostDeviationHistory.size > HISTORY_SIZE) boostDeviationHistory.removeAt(0)
    }

    fun getAverageDuty(): Double {
        return if (dutyHistory.isNotEmpty()) dutyHistory.average() else 0.0
    }

    /**
     * Reset history data
     */
    fun reset() {
        dutyHistory.clear()
        boostDeviationHistory.clear()
    }
}
