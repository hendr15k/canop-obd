package com.canopobd.data.domain

/**
 * Analysiert Wastegate-Funktion für BorgWarner KP39 beim A14NET
 * Der A14NET hat einen fest-geometrierten Turbo mit pneumatisch geregeltem Wastegate
 */
class WastegateHealthAnalyzer {

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
        val recommendation: String
    )

    companion object {
        private const val DUTY_IDLE_MIN = 80.0    // WG offen im Leerlauf
        private const val DUTY_WOT_MAX = 60.0    // WG geschlossen bei Vollast
        private const val DUTY_STUCK_OPEN = 95.0  // WG fast immer offen
        private const val DUTY_STUCK_CLOSED = 5.0 // WG fast immer geschlossen
        private const val NORMAL_BOOST_DEV = 15.0 // 15% Abweichung tolerierbar
    }

    fun analyze(
        wastegateDuty: Double,
        avgWastegateDuty: Double,
        targetBoost: Double,
        actualBoost: Double,
        rpm: Double,
        engineLoad: Double
    ): WastegateAnalysis {
        val boostDeviation = if (targetBoost > 0) {
            ((actualBoost - targetBoost) / targetBoost) * 100.0
        } else 0.0

        val (condition, healthScore, diagnosis, recommendation) = when {
            // Stuck Open
            wastegateDuty > DUTY_STUCK_OPEN -> {
                Quadruple(WastegateCondition.STUCK_OPEN, 20, 
                    "Wastegate blockiert offen - Ladedruckverlust", 
                    "Wastegate-Mechanismus und Unterdruckleitung prüfen")
            }

            // Stuck Closed  
            wastegateDuty < DUTY_STUCK_CLOSED -> {
                Quadruple(WastegateCondition.STUCK_CLOSED, 30,
                    "Wastegate blockiert geschlossen - Überladung möglich",
                    "Wastegate-Aktuator auf Freigang prüfen")
            }

            // Leak Detection: Underboost + High Duty
            boostDeviation < -NORMAL_BOOST_DEV && wastegateDuty < 50.0 -> {
                Quadruple(WastegateCondition.WASTEGATE_LEAK, 40,
                    "Wastegate undicht - Unterladung",
                    "Wastegate-Sitz und Rückholfeder prüfen")
            }

            // Overboost + Low Duty
            boostDeviation > NORMAL_BOOST_DEV && wastegateDuty > 70.0 -> {
                Quadruple(WastegateCondition.STUCK_CLOSED, 45,
                    "Wastegate schließt nicht richtig",
                    "Unterdruckdose und Ventil prüfen")
            }

            // Abnormal Pattern at Load
            engineLoad > 60.0 && wastegateDuty > 70.0 && boostDeviation > 10.0 -> {
                Quadruple(WastegateCondition.SOLENOID_ISSUE, 50,
                    "Wastegate-Regelung abnormal",
                    "Magnetventil und ECU-Signal prüfen")
            }

            // Normal Operation
            wastegateDuty in DUTY_STUCK_CLOSED..DUTY_STUCK_OPEN -> {
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

        return WastegateAnalysis(
            condition = condition,
            currentDutyPercent = wastegateDuty,
            avgDutyPercent = avgWastegateDuty,
            boostDeviation = boostDeviation,
            healthScore = healthScore,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }
}
