package com.canopobd.data.domain

class EmissionsReadinessAnalyzer {

    data class ReadinessInput(
        val readinessBits: Int = 0,
        val supportedPIDs: List<String> = emptyList(),
        val activeDTCs: List<String> = emptyList(),
        val engineRuntimeSeconds: Double = 0.0,
        val coolantTemp: Double = 0.0,
        val warmupsSinceClear: Int = 0,
        val distanceSinceClear: Double = 0.0
    )

    data class ReadinessAnalysis(
        val monitors: List<com.canopobd.data.model.EmissionsReadinessMonitor>,
        val completedCount: Int,
        val totalCount: Int,
        val allComplete: Boolean,
        val overallScore: Int,
        val emissionsStatus: EmissionsStatus,
        val diagnosis: String,
        val recommendation: String
    )

    enum class EmissionsStatus(val label: String, val colorHex: Long) {
        READY("Bereit", 0xFF00FF88),
        NOT_READY("Nicht bereit", 0xFFFFE066),
        INCOMPLETE("Unvollstaendig", 0xFFFF8C00),
        DTC_ACTIVE("Fehler aktiv", 0xFFFF4444)
    }

    companion object {
        private const val MISFIRE_BIT = 0
        private const val FUEL_SYSTEM_BIT = 1
        private const val COMPONENTS_BIT = 2
        private const val CATALYST_BIT = 3
        private const val EVAP_BIT = 5
        private const val SAI_BIT = 6
        private const val O2_SENSOR_BIT = 8
        private const val O2_HEATER_BIT = 9
        private const val EGR_BIT = 10

        private const val MIN_WARMUPS_FOR_READINESS = 1
        private const val MIN_DISTANCE_FOR_READINESS = 5.0
        private const val MIN_RUNTIME_FOR_READINESS = 60.0
    }

    fun analyze(input: ReadinessInput): ReadinessAnalysis {
        val monitors = parseReadinessBits(input.readinessBits)
        val completedCount = monitors.count { it.isComplete }
        val totalCount = monitors.size
        val allComplete = completedCount == totalCount
        val hasDTC = input.activeDTCs.isNotEmpty()

        val overallScore = calculateReadinessScore(monitors, hasDTC, input)
        val emissionsStatus = determineEmissionsStatus(monitors, hasDTC, input)
        val diagnosis = generateDiagnosis(monitors, emissionsStatus, input)
        val recommendation = generateRecommendation(monitors, emissionsStatus, input, hasDTC)

        return ReadinessAnalysis(
            monitors = monitors,
            completedCount = completedCount,
            totalCount = totalCount,
            allComplete = allComplete,
            overallScore = overallScore,
            emissionsStatus = emissionsStatus,
            diagnosis = diagnosis,
            recommendation = recommendation
        )
    }

    fun parseReadinessBits(bits: Int): List<com.canopobd.data.model.EmissionsReadinessMonitor> {
        return listOf(
            createMonitor(com.canopobd.data.model.MonitorType.MISFIRE, bits, MISFIRE_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.FUEL_SYSTEM, bits, FUEL_SYSTEM_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.COMPONENTS, bits, COMPONENTS_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.CATALYST, bits, CATALYST_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.O2_SENSOR, bits, O2_SENSOR_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.O2_HEATER, bits, O2_HEATER_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.EGR, bits, EGR_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.EVAP, bits, EVAP_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.SAI, bits, SAI_BIT, true),
            createMonitor(com.canopobd.data.model.MonitorType.GPF, bits, -1, false)
        )
    }

    private fun createMonitor(type: com.canopobd.data.model.MonitorType, bits: Int, bitPosition: Int, isSupported: Boolean): com.canopobd.data.model.EmissionsReadinessMonitor {
        val isComplete = if (bitPosition >= 0) {
            isSupported && ((bits shr bitPosition) and 1) == 1
        } else {
            false
        }
        return com.canopobd.data.model.EmissionsReadinessMonitor(
            monitor = type,
            isComplete = isComplete,
            isSupported = isSupported
        )
    }

    fun getReadinessPercentage(monitors: List<com.canopobd.data.model.EmissionsReadinessMonitor>): Double {
        val supported = monitors.filter { it.isSupported }
        if (supported.isEmpty()) {
            return 0.0
        }
        val completed = supported.count { it.isComplete }
        return (completed.toDouble() / supported.size) * 100.0
    }

    fun isInspectionReady(monitors: List<com.canopobd.data.model.EmissionsReadinessMonitor>, hasDTC: Boolean): Boolean {
        if (hasDTC) {
            return false
        }
        val supported = monitors.filter { it.isSupported }
        val notComplete = supported.count { !it.isComplete }
        return notComplete <= 1
    }

    private fun calculateReadinessScore(monitors: List<com.canopobd.data.model.EmissionsReadinessMonitor>, hasDTC: Boolean, input: ReadinessInput): Int {
        if (hasDTC) {
            return 20
        }

        val supported = monitors.filter { it.isSupported }
        if (supported.isEmpty()) {
            return 60
        }

        val completed = supported.count { it.isComplete }
        val baseScore = (completed.toDouble() / supported.size * 80).toInt()

        val criticalMonitors = listOf(
            com.canopobd.data.model.MonitorType.CATALYST,
            com.canopobd.data.model.MonitorType.O2_SENSOR,
            com.canopobd.data.model.MonitorType.MISFIRE
        )
        val criticalIncomplete = monitors.filter {
            it.monitor in criticalMonitors && !it.isComplete && it.isSupported
        }.size

        val penalty = criticalIncomplete * 10

        val runtimePenalty = when {
            input.engineRuntimeSeconds < MIN_RUNTIME_FOR_READINESS -> 10
            else -> 0
        }

        return (baseScore - penalty - runtimePenalty + 20).coerceIn(0, 100)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun determineEmissionsStatus(
        monitors: List<com.canopobd.data.model.EmissionsReadinessMonitor>,
        hasDTC: Boolean,
        input: ReadinessInput
    ): EmissionsStatus {
        if (hasDTC) {
            return EmissionsStatus.DTC_ACTIVE
        }

        val supported = monitors.filter { it.isSupported }
        val notComplete = supported.count { !it.isComplete }

        return when {
            notComplete == 0 -> EmissionsStatus.READY
            notComplete <= 2 -> EmissionsStatus.INCOMPLETE
            else -> EmissionsStatus.NOT_READY
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateDiagnosis(
        monitors: List<com.canopobd.data.model.EmissionsReadinessMonitor>,
        status: EmissionsStatus,
        input: ReadinessInput
    ): String {
        val completed = monitors.count { it.isComplete && it.isSupported }
        val total = monitors.count { it.isSupported }
        val incompleteNames = monitors
            .filter { !it.isComplete && it.isSupported }
            .map { it.monitor.label }

        return when (status) {
            EmissionsStatus.READY -> {
                "Alle $total Monitore abgeschlossen. Fahrzeug emissionssteuerfaehig."
            }
            EmissionsStatus.NOT_READY -> {
                "Nur $completed/$total Monitore abgeschlossen. " +
                    "Noch nicht abgeschlossen: ${incompleteNames.joinToString(", ")}."
            }
            EmissionsStatus.INCOMPLETE -> {
                "${incompleteNames.joinToString(", ")} noch nicht abgeschlossen. " +
                    "Weitere Fahrdaten noetig."
            }
            EmissionsStatus.DTC_ACTIVE -> {
                "Fehlercodes vorhanden. Emissionspruefung wird fehlschlagen. " +
                    "DTCs zuerst loesen."
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateRecommendation(
        monitors: List<com.canopobd.data.model.EmissionsReadinessMonitor>,
        status: EmissionsStatus,
        input: ReadinessInput,
        hasDTC: Boolean
    ): String {
        return when {
            hasDTC -> {
                "Fehlercodes loesen und Fahrtzyklus abschliessen. " +
                    "Danach erneut pruefen."
            }
            status == EmissionsStatus.READY -> {
                "Fahrzeug bereit fuer TUEV/AU. Alle Monitore bestanden."
            }
            input.warmupsSinceClear < MIN_WARMUPS_FOR_READINESS -> {
                "Noch ${MIN_WARMUPS_FOR_READINESS - input.warmupsSinceClear} " +
                    "Warmlaeufe noetig fuer Readiness-Reset. " +
                    "Normalen Fahrtzyklus durchfuehren."
            }
            input.distanceSinceClear < MIN_DISTANCE_FOR_READINESS -> {
                "Mindestens ${MIN_DISTANCE_FOR_READINESS.toInt()}km " +
                    "Fahrstrecke fuer Monitore noetig."
            }
            else -> {
                "Fahrzeug normal bis zur Betriebstemperatur fahren. " +
                    "Monitore schliessen sich automatisch. " +
                    "Idle-, Teillast- und Vollastphasen durchfahren."
            }
        }
    }
}
