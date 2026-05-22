package com.canopobd.data.maintenance

import com.canopobd.data.model.MaintenanceType

/**
 * Erweiterte Wartungserinnerungs-System für den Opel Astra J 1.4 Turbo (A14NET/LUJ)
 *
 * Recherchierte Wartungsintervalle:
 * - Ölwechsel (Dexos2 5W-30): 15.000 km / 12 Monate (Stadtverkehr: 10.000 km)
 * - Zündkerzen (NGK LZKR6AP-11G): 60.000 km (Kurzstrecke: 30.000 km)
 * - Luftfilter: 30.000 km (Staub: 15.000-20.000 km)
 * - Timing-Kette: Prüfung ab 80.000 km, kritisch ab 150.000 km
 * - Turbo-Inspektion: 60.000 km (BorgWarner KP39)
 * - Kühlmittel: Erst 5 Jahre/150.000 km, dann alle 2 Jahre/60.000 km
 * - Bremsbeläge: 30.000-40.000 km
 * - Getriebeöl: 60.000-80.000 km (Dexron VI ATF)
 */

/**
 * Prioritätsstufe für Wartungserinnerungen
 */
enum class ReminderPriority(val label: String, val colorHex: Long) {
    CRITICAL("Kritisch", 0xFFFF4444),
    HIGH("Hoch", 0xFFFF8C00),
    MEDIUM("Mittel", 0xFFFFE066),
    LOW("Niedrig", 0xFF22C55E),
    INFO("Info", 0xFF60A5FA)
}

/**
 * Art der Erinnerung (km-basiert, zeit-basiert oder beides)
 */
enum class ReminderTriggerType {
    KM_BASED,
    TIME_BASED,
    KM_OR_TIME,
    KM_AND_TIME
}

/**
 * Fahrbedingungen beeinflussen Wartungsintervalle
 */
enum class DrivingConditions(val label: String, val kmFactor: Double, val timeFactor: Double) {
    SEVERE("Stadtverkehr / Kurzstrecke", 0.7, 0.75),
    NORMAL("Normal (gemischt)", 1.0, 1.0),
    HIGHWAY("Überwiegend Autobahn", 1.2, 1.1)
}

/**
 * Erweitertes Wartungsintervall mit allen Details
 */
data class MaintenanceInterval(
    val type: MaintenanceType,
    val intervalKm: Int,
    val intervalMonths: Int,
    val severeIntervalKm: Int,
    val severeIntervalMonths: Int,
    val highwayIntervalKm: Int,
    val highwayIntervalMonths: Int,
    val description: String,
    val partNumber: String = "",
    val torqueSpec: String = "",
    val notes: String = ""
)

/**
 * Wartungserinnerung mit erweiterten Informationen
 */
data class MaintenanceReminder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: MaintenanceType,
    val title: String,
    val description: String,
    val priority: ReminderPriority,
    val triggerType: ReminderTriggerType,
    val intervalKm: Int,
    val intervalMonths: Int,
    val severeIntervalKm: Int = intervalKm,
    val severeIntervalMonths: Int = intervalMonths,
    val highwayIntervalKm: Int = intervalKm,
    val highwayIntervalMonths: Int = intervalMonths,
    val lastServiceKm: Int = 0,
    val lastServiceDate: Long = 0L,
    val currentKm: Int = 0,
    val currentDate: Long = System.currentTimeMillis(),
    val drivingConditions: DrivingConditions = DrivingConditions.NORMAL,
    val partNumber: String = "",
    val torqueSpec: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val completedDate: Long? = null,
    val completedKm: Int? = null
) {
    /**
     * Berechnet die verbleibenden Kilometer bis zum nächsten Service
     */
    val kmRemaining: Int
        get() {
            val effectiveInterval = when (drivingConditions) {
                DrivingConditions.SEVERE -> (intervalKm * drivingConditions.kmFactor).toInt()
                DrivingConditions.HIGHWAY -> (intervalKm * drivingConditions.kmFactor).toInt()
                DrivingConditions.NORMAL -> intervalKm
            }
            return (lastServiceKm + effectiveInterval) - currentKm
        }

    /**
     * Berechnet die verbleibenden Monate bis zum nächsten Service
     */
    val monthsRemaining: Int
        get() {
            if (lastServiceDate == 0L) return intervalMonths
            val effectiveInterval = when (drivingConditions) {
                DrivingConditions.SEVERE -> (intervalMonths * drivingConditions.timeFactor).toInt()
                DrivingConditions.HIGHWAY -> (intervalMonths * drivingConditions.timeFactor).toInt()
                DrivingConditions.NORMAL -> intervalMonths
            }
            val monthsSinceLastService = ((currentDate - lastServiceDate) / (30L * 24 * 60 * 60 * 1000)).toInt()
            return effectiveInterval - monthsSinceLastService
        }

    /**
     * Fortschritt in Prozent (0 = gerade gewechselt, 100 = fällig)
     */
    val progressPercent: Float
        get() {
            val effectiveInterval = when (drivingConditions) {
                DrivingConditions.SEVERE -> (intervalKm * drivingConditions.kmFactor).toInt()
                DrivingConditions.HIGHWAY -> (intervalKm * drivingConditions.kmFactor).toInt()
                DrivingConditions.NORMAL -> intervalKm
            }
            val used = currentKm - lastServiceKm
            return (used.toFloat() / effectiveInterval * 100f).coerceIn(0f, 100f)
        }

    /**
     * Aktueller Status der Wartungserinnerung
     */
    val status: MaintenanceReminderStatus
        get() = when {
            isCompleted -> MaintenanceReminderStatus.COMPLETED
            kmRemaining < 0 || monthsRemaining < 0 -> MaintenanceReminderStatus.OVERDUE
            kmRemaining < intervalKm * 0.1 || monthsRemaining < 2 -> MaintenanceReminderStatus.DUE_SOON
            kmRemaining < intervalKm * 0.25 || monthsRemaining < 3 -> MaintenanceReminderStatus.UPCOMING
            else -> MaintenanceReminderStatus.OK
        }

    /**
     * Ist die Erinnerung aktiv und muss beachtet werden?
     */
    val requiresAttention: Boolean
        get() = isActive && !isCompleted && (status == MaintenanceReminderStatus.OVERDUE || status == MaintenanceReminderStatus.DUE_SOON)

    /**
     * Titel für die UI-Anzeige
     */
    val displayTitle: String
        get() = when (status) {
            MaintenanceReminderStatus.OVERDUE -> "ÜBERFÄLLIG: $title"
            MaintenanceReminderStatus.DUE_SOON -> "FÄLLIG: $title"
            MaintenanceReminderStatus.UPCOMING -> "Bald fällig: $title"
            MaintenanceReminderStatus.OK -> title
            MaintenanceReminderStatus.COMPLETED -> "Erledigt: $title"
        }
}

/**
 * Status einer Wartungserinnerung
 */
enum class MaintenanceReminderStatus(val label: String, val colorHex: Long) {
    OK("OK", 0xFF22C55E),
    UPCOMING("Bald fällig", 0xFF60A5FA),
    DUE_SOON("Fällig", 0xFFFFE066),
    OVERDUE("Überfällig", 0xFFFF4444),
    COMPLETED("Erledigt", 0xFF22C55E)
}

/**
 * Öltemperatur-Historie-Eintrag für Trendanalyse
 */
data class OilTempHistoryEntry(
    val timestamp: Long,
    val oilTempC: Double,
    val coolantTempC: Double,
    val engineLoad: Double,
    val rpm: Double,
    val speedKmh: Double
) {
    /**
     * Ist die Öltemperatur im optimalen Bereich?
     */
    val isOptimal: Boolean
        get() = oilTempC in 90.0..110.0

    /**
     * Ist die Öltemperatur zu hoch?
     */
    val isOverheating: Boolean
        get() = oilTempC > 120.0

    /**
     * Kategorisierung der Öltemperatur
     */
    val category: OilTempCategory
        get() = when {
            oilTempC < 60.0 -> OilTempCategory.COLD
            oilTempC < 80.0 -> OilTempCategory.WARMING_UP
            oilTempC in 80.0..110.0 -> OilTempCategory.OPTIMAL
            oilTempC in 110.0..120.0 -> OilTempCategory.ELEVATED
            else -> OilTempCategory.OVERHEATING
        }
}

/**
 * Kategorien der Öltemperatur
 */
enum class OilTempCategory(val label: String, val colorHex: Long) {
    COLD("Kalt", 0xFF60A5FA),
    WARMING_UP("Aufwärmen", 0xFFFBBF24),
    OPTIMAL("Optimal", 0xFF22C55E),
    ELEVATED("Erhöht", 0xFFF97316),
    OVERHEATING("Überhitzung!", 0xFFFF4444)
}

/**
 * Kraftstoffverbrauch-Trend-Eintrag
 */
data class FuelConsumptionEntry(
    val timestamp: Long,
    val consumptionL100km: Double,
    val distanceKm: Double,
    val fuelUsedLiters: Double,
    val speedKmh: Double,
    val rpm: Double,
    val engineLoad: Double,
    val isHighway: Boolean = false,
    val isCity: Boolean = false
) {
    /**
     * Kategorisierung des Verbrauchs
     */
    val category: FuelConsumptionCategory
        get() = when {
            consumptionL100km < 5.0 -> FuelConsumptionCategory.EXCELLENT
            consumptionL100km < 6.5 -> FuelConsumptionCategory.GOOD
            consumptionL100km < 7.5 -> FuelConsumptionCategory.NORMAL
            consumptionL100km < 9.0 -> FuelConsumptionCategory.ELEVATED
            else -> FuelConsumptionCategory.HIGH
        }
}

/**
 * Kategorien des Kraftstoffverbrauchs
 */
enum class FuelConsumptionCategory(val label: String, val colorHex: Long) {
    EXCELLENT("Ausgezeichnet", 0xFF22C55E),
    GOOD("Gut", 0xFF4ADE80),
    NORMAL("Normal", 0xFF60A5FA),
    ELEVATED("Erhöht", 0xFFFBBF24),
    HIGH("Hoch", 0xFFFF4444)
}

/**
 * Trend-Richtung für Statistiken
 */
enum class TrendDirection {
    IMPROVING,
    STABLE,
    WORSENING,
    UNKNOWN
}

/**
 * Zusammenfassung der Wartungsstatus
 */
data class MaintenanceSummary(
    val totalItems: Int,
    val overdueCount: Int,
    val dueSoonCount: Int,
    val upcomingCount: Int,
    val okCount: Int,
    val completedCount: Int,
    val overallStatus: MaintenanceReminderStatus,
    val nextServiceType: MaintenanceType?,
    val nextServiceKm: Int,
    val nextServiceMonths: Int
)

/**
 * Wartungsprotokoll-Eintrag
 */
data class MaintenanceLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: MaintenanceType,
    val date: Long,
    val km: Int,
    val cost: Double = 0.0,
    val notes: String = "",
    val workshop: String = "",
    val partsReplaced: List<String> = emptyList()
)

/**
 * Fahrzeug-spezifische Wartungskonfiguration
 */
data class VehicleMaintenanceConfig(
    val vehicleId: String,
    val vehicleName: String,
    val engineCode: String,
    val drivingConditions: DrivingConditions = DrivingConditions.NORMAL,
    val customIntervals: Map<MaintenanceType, Int> = emptyMap(),
    val reminders: List<MaintenanceReminder> = emptyList(),
    val logEntries: List<MaintenanceLogEntry> = emptyList(),
    val oilTempHistory: List<OilTempHistoryEntry> = emptyList(),
    val fuelConsumptionHistory: List<FuelConsumptionEntry> = emptyList()
) {
    /**
     * Erstellt Wartungserinnerungen basierend auf dem Fahrzeugprofil
     */
    fun createDefaultReminders(): List<MaintenanceReminder> {
        val calibration = com.canopobd.data.model.AstraJ14TurboCalibration.INSTANCE
        return listOf(
            createOilChangeReminder(calibration),
            createSparkPlugReminder(calibration),
            createAirFilterReminder(calibration),
            createTimingChainReminder(calibration),
            createTurboInspectionReminder(calibration),
            createCoolantReminder(calibration),
            createBrakePadsReminder(),
            createTransmissionFluidReminder(calibration),
            createTireRotationReminder(),
            createInspectionReminder()
        )
    }

    private fun createOilChangeReminder(cal: com.canopobd.data.model.AstraJ14TurboCalibration) = MaintenanceReminder(
        type = MaintenanceType.OIL_CHANGE,
        title = "Ölwechsel",
        description = "Ölwechsel mit Dexos2 5W-30 (${cal.oilCapacityLiters}L inkl. Filter)",
        priority = ReminderPriority.HIGH,
        triggerType = ReminderTriggerType.KM_OR_TIME,
        intervalKm = customIntervals[MaintenanceType.OIL_CHANGE] ?: cal.oilChangeIntervalKm,
        intervalMonths = 12,
        severeIntervalKm = 10000,
        severeIntervalMonths = 8,
        highwayIntervalKm = 18000,
        highwayIntervalMonths = 14,
        partNumber = "Dexos2 5W-30 (4.5L)",
        notes = "Bei Stadtverkehr oder Kurzstrecke: 10.000 km / 8 Monate"
    )

    private fun createSparkPlugReminder(cal: com.canopobd.data.model.AstraJ14TurboCalibration) = MaintenanceReminder(
        type = MaintenanceType.SPARK_PLUGS,
        title = "Zündkerzen-Wechsel",
        description = "Zündkerzen wechseln (${cal.sparkPlugType})",
        priority = ReminderPriority.MEDIUM,
        triggerType = ReminderTriggerType.KM_BASED,
        intervalKm = customIntervals[MaintenanceType.SPARK_PLUGS] ?: cal.sparkPlugIntervalKm,
        intervalMonths = 48,
        severeIntervalKm = 30000,
        severeIntervalMonths = 24,
        highwayIntervalKm = 70000,
        highwayIntervalMonths = 60,
        partNumber = cal.sparkPlugType,
        torqueSpec = cal.sparkPlugTorque,
        notes = "Elektrodenabstand: ${cal.sparkPlugGap}mm"
    )

    private fun createAirFilterReminder(cal: com.canopobd.data.model.AstraJ14TurboCalibration) = MaintenanceReminder(
        type = MaintenanceType.AIR_FILTER,
        title = "Luftfilter-Wechsel",
        description = "Luftfilter element wechseln",
        priority = ReminderPriority.MEDIUM,
        triggerType = ReminderTriggerType.KM_BASED,
        intervalKm = customIntervals[MaintenanceType.AIR_FILTER] ?: cal.airFilterIntervalKm,
        intervalMonths = 24,
        severeIntervalKm = 15000,
        severeIntervalMonths = 12,
        highwayIntervalKm = 40000,
        highwayIntervalMonths = 36,
        notes = "Bei Staubigen Bedingungen häufiger prüfen"
    )

    private fun createTimingChainReminder(cal: com.canopobd.data.model.AstraJ14TurboCalibration) = MaintenanceReminder(
        type = MaintenanceType.TURBO_BOOST_CHECK,
        title = "Timing-Kette Prüfung",
        description = "Timing-Kette und Kettenspanner prüfen (P0340/P0341)",
        priority = ReminderPriority.HIGH,
        triggerType = ReminderTriggerType.KM_BASED,
        intervalKm = cal.timingChainIntervalKm / 2,
        intervalMonths = 60,
        severeIntervalKm = 60000,
        severeIntervalMonths = 48,
        highwayIntervalKm = 100000,
        highwayIntervalMonths = 84,
        notes = "Bekanntes Problem: Rattern bei Kaltstart ab 80.000 km"
    )

    private fun createTurboInspectionReminder(cal: com.canopobd.data.model.AstraJ14TurboCalibration) = MaintenanceReminder(
        type = MaintenanceType.TURBO_INSPECTION,
        title = "Turbo-Inspektion",
        description = "Turbolader (${cal.turbochargerType}) Inspektion",
        priority = ReminderPriority.MEDIUM,
        triggerType = ReminderTriggerType.KM_BASED,
        intervalKm = customIntervals[MaintenanceType.TURBO_INSPECTION] ?: 60000,
        intervalMonths = 48,
        severeIntervalKm = 45000,
        severeIntervalMonths = 36,
        highwayIntervalKm = 80000,
        highwayIntervalMonths = 72,
        notes = "Wastegate-Stellglied, Ölleitungen und Ladedruck prüfen"
    )

    private fun createCoolantReminder(cal: com.canopobd.data.model.AstraJ14TurboCalibration) = MaintenanceReminder(
        type = MaintenanceType.COOLANT,
        title = "Kühlmittel wechseln",
        description = "Kühlmittel erneuern (${cal.coolantCapacity}L)",
        priority = ReminderPriority.LOW,
        triggerType = ReminderTriggerType.KM_OR_TIME,
        intervalKm = customIntervals[MaintenanceType.COOLANT] ?: cal.coolantIntervalKm,
        intervalMonths = 60,
        severeIntervalKm = 60000,
        severeIntervalMonths = 48,
        highwayIntervalKm = 80000,
        highwayIntervalMonths = 72,
        notes = "Erster Wechsel nach 5 Jahren / 150.000 km, dann alle 2 Jahre"
    )

    private fun createBrakePadsReminder() = MaintenanceReminder(
        type = MaintenanceType.BRAKE_PADS,
        title = "Bremsbeläge prüfen",
        description = "Bremsbeläge vorne und hinten prüfen/wechseln",
        priority = ReminderPriority.HIGH,
        triggerType = ReminderTriggerType.KM_BASED,
        intervalKm = customIntervals[MaintenanceType.BRAKE_PADS] ?: 30000,
        intervalMonths = 24,
        severeIntervalKm = 20000,
        severeIntervalMonths = 18,
        highwayIntervalKm = 40000,
        highwayIntervalMonths = 36,
        notes = "Bei Stadtverkehr und bergigem Terrain häufiger prüfen"
    )

    private fun createTransmissionFluidReminder(cal: com.canopobd.data.model.AstraJ14TurboCalibration) = MaintenanceReminder(
        type = MaintenanceType.TRANSMISSION_FLUID,
        title = "Getriebeöl wechseln",
        description = "Getriebeöl erneuern (${cal.transmissionFluid})",
        priority = ReminderPriority.LOW,
        triggerType = ReminderTriggerType.KM_OR_TIME,
        intervalKm = customIntervals[MaintenanceType.TRANSMISSION_FLUID] ?: 80000,
        intervalMonths = 60,
        severeIntervalKm = 60000,
        severeIntervalMonths = 48,
        highwayIntervalKm = 100000,
        highwayIntervalMonths = 72,
        partNumber = cal.transmissionFluid,
        notes = "Manuelles Getriebe: 60.000-80.000 km, Automatik: 60.000 km"
    )

    private fun createTireRotationReminder() = MaintenanceReminder(
        type = MaintenanceType.TIRES,
        title = "Reifen-Wechsel/Rotation",
        description = "Reifen prüfen, wechseln oder rotieren",
        priority = ReminderPriority.MEDIUM,
        triggerType = ReminderTriggerType.KM_BASED,
        intervalKm = 15000,
        intervalMonths = 12,
        severeIntervalKm = 10000,
        severeIntervalMonths = 8,
        highwayIntervalKm = 20000,
        highwayIntervalMonths = 15,
        notes = "Profilmindeststand: 3mm, Rotation alle 10.000-15.000 km"
    )

    private fun createInspectionReminder() = MaintenanceReminder(
        type = MaintenanceType.INSPECTION,
        title = "Hauptuntersuchung (HU/AU)",
        description = "TÜV Hauptuntersuchung und Abgasuntersuchung",
        priority = ReminderPriority.CRITICAL,
        triggerType = ReminderTriggerType.TIME_BASED,
        intervalKm = 60000,
        intervalMonths = 24,
        severeIntervalKm = 60000,
        severeIntervalMonths = 24,
        highwayIntervalKm = 60000,
        highwayIntervalMonths = 24,
        notes = "Gesetzliche Pflichtuntersuchung alle 2 Jahre"
    )
}

/**
 * Erweiterte Wartungserinnerung mit Zeitstempel
 */
data class MaintenanceReminderNotification(
    val reminderId: String,
    val type: MaintenanceType,
    val title: String,
    val message: String,
    val priority: ReminderPriority,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDismissed: Boolean = false
)
