package com.canopobd.data.maintenance

import com.canopobd.data.model.MaintenanceType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Service für die Verwaltung von Wartungserinnerungen
 *
 * Historische Intervalle Opel Astra J 1.4 Turbo (A14NET):
 * - Ölwechsel: 15.000 km / 1 Jahr (Dexos2 5W-30 Pflicht)
 * - Zündkerzen: 60.000 km (NGK LZKR6AP-11G oder Bosch FR7HPP332)
 * - Luftfilter: 30.000 km
 * - Kühlmittel: 80.000 km / 5 Jahre
 * - Getriebeöl: 60.000-80.000 km
 * - Turbo-Inspektion: 60.000 km
 * - Timing-Kette: Prüfung ab 80.000 km
 */
class MaintenanceService {

    private val reminders = ConcurrentHashMap<String, MaintenanceReminder>()
    private val oilTempHistory = CopyOnWriteArrayList<OilTempHistoryEntry>()
    private val fuelConsumptionHistory = CopyOnWriteArrayList<FuelConsumptionEntry>()
    private val notifications = CopyOnWriteArrayList<MaintenanceReminderNotification>()

    companion object {
        const val MAX_OIL_TEMP_ENTRIES = 1000
        const val MAX_FUEL_ENTRIES = 500
        const val MAX_NOTIFICATIONS = 50

        // Astra J 1.4 Turbo spezifische Intervalle
        const val OIL_CHANGE_KM = 15000
        const val OIL_CHANGE_MONTHS = 12
        const val OIL_CHANGE_SEVERE_KM = 10000

        const val SPARK_PLUG_KM = 60000
        const val SPARK_PLUG_SEVERE_KM = 30000

        const val AIR_FILTER_KM = 30000
        const val AIR_FILTER_SEVERE_KM = 15000

        const val TIMING_CHAIN_CHECK_KM = 80000
        const val TIMING_CHAIN_CRITICAL_KM = 150000

        const val TURBO_INSPECTION_KM = 60000
        const val COOLANT_KM = 80000
        const val BRAKE_PADS_KM = 30000
        const val TRANSMISSION_FLUID_KM = 80000

        // Öltemperatur-Schwellenwerte (Optimum: 90-110°C)
        const val OIL_TEMP_OPTIMAL_MIN = 90.0
        const val OIL_TEMP_OPTIMAL_MAX = 110.0
        const val OIL_TEMP_WARNING = 120.0
        const val OIL_TEMP_CRITICAL = 130.0

        // Kraftstoffverbrauch-Sollwerte (A14NET: 5.9L/100km kombiniert)
        const val FUEL_CONSUMPTION_TARGET = 6.0
        const val FUEL_CONSUMPTION_WARNING = 7.5
        const val FUEL_CONSUMPTION_CRITICAL = 9.0

        // Kostenschätzungen (Workshop)
        const val OIL_CHANGE_COST = 100.0
        const val SPARK_PLUGS_COST = 130.0
        const val AIR_FILTER_COST = 50.0
        const val TURBO_INSPECTION_COST = 120.0
        const val COOLANT_COST = 120.0
        const val BRAKE_PADS_COST = 250.0
        const val TRANSMISSION_FLUID_COST = 150.0
        const val TIRES_COST = 600.0
        const val INSPECTION_COST = 150.0
        const val TIMING_CHAIN_COST = 1200.0
        const val TURBO_COST = 1200.0
        const val WASTEGATE_COST = 200.0
        const val MAF_SENSOR_COST = 200.0
        const val LAMBDA_SENSOR_COST = 250.0
    }

    /**
     * Initialisiert den Service mit Standard-Erinnerungen
     */
    fun initialize(currentKm: Int = 0, currentDate: Long = System.currentTimeMillis()): List<MaintenanceReminder> {
        val config = VehicleMaintenanceConfig(
            vehicleId = "astra_j_14t",
            vehicleName = "Opel Astra J 1.4 Turbo",
            engineCode = "A14NET/LUJ"
        )

        val defaultReminders = config.createDefaultReminders().map { reminder ->
            reminder.copy(currentKm = currentKm, currentDate = currentDate)
        }

        defaultReminders.forEach { reminder ->
            reminders[reminder.id] = reminder
        }

        return defaultReminders
    }

    /**
     * Fügt eine neue Erinnerung hinzu
     */
    fun addReminder(reminder: MaintenanceReminder) {
        reminders[reminder.id] = reminder
    }

    /**
     * Aktualisiert eine bestehende Erinnerung
     */
    fun updateReminder(reminder: MaintenanceReminder) {
        reminders[reminder.id] = reminder
    }

    /**
     * Entfernt eine Erinnerung
     */
    fun removeReminder(reminderId: String) {
        reminders.remove(reminderId)
    }

    /**
     * Gibt alle aktiven Erinnerungen zurück
     */
    fun getActiveReminders(): List<MaintenanceReminder> {
        return reminders.values.filter { it.isActive && !it.isCompleted }
    }

    /**
     * Gibt alle Erinnerungen zurück
     */
    fun getAllReminders(): List<MaintenanceReminder> {
        return reminders.values.toList()
    }

    /**
     * Gibt überfällige Erinnerungen zurück
     */
    fun getOverdueReminders(): List<MaintenanceReminder> {
        return reminders.values.filter { it.status == MaintenanceReminderStatus.OVERDUE }
    }

    /**
     * Gibt bald fällige Erinnerungen zurück
     */
    fun getDueSoonReminders(): List<MaintenanceReminder> {
        return reminders.values.filter { it.status == MaintenanceReminderStatus.DUE_SOON }
    }

    /**
     * Aktualisiert den Kilometerstand für alle Erinnerungen
     */
    fun updateCurrentKm(currentKm: Int) {
        reminders.values.forEach { reminder ->
            reminders[reminder.id] = reminder.copy(currentKm = currentKm)
        }
        checkReminders()
    }

    /**
     * Setzt eine Wartung als erledigt
     */
    fun completeMaintenance(type: MaintenanceType, km: Int, date: Long = System.currentTimeMillis()) {
        reminders.values.filter { it.type == type }.forEach { reminder ->
            reminders[reminder.id] = reminder.copy(
                lastServiceKm = km,
                lastServiceDate = date,
                currentKm = km,
                currentDate = date,
                isCompleted = true,
                completedDate = date,
                completedKm = km
            )
        }
        checkReminders()
    }

    /**
     * Setzt eine Erinnerung auf aktiv (nach Erledigung)
     */
    fun resetReminder(type: MaintenanceType, currentKm: Int, currentDate: Long = System.currentTimeMillis()) {
        reminders.values.filter { it.type == type }.forEach { reminder ->
            reminders[reminder.id] = reminder.copy(
                lastServiceKm = currentKm,
                lastServiceDate = currentDate,
                currentKm = currentKm,
                currentDate = currentDate,
                isCompleted = false,
                completedDate = null,
                completedKm = null
            )
        }
    }

    /**
     * Setzt die Fahrbedingungen für eine Erinnerung
     */
    fun setDrivingConditions(type: MaintenanceType, conditions: DrivingConditions) {
        reminders.values.filter { it.type == type }.forEach { reminder ->
            reminders[reminder.id] = reminder.copy(drivingConditions = conditions)
        }
    }

    /**
     * Prüft alle Erinnerungen und erstellt ggf. Benachrichtigungen
     */
    private fun checkReminders() {
        reminders.values.filter { it.isActive && !it.isCompleted }.forEach { reminder ->
            when (reminder.status) {
                MaintenanceReminderStatus.OVERDUE -> {
                    createNotification(reminder, "Überfällig! Sofort erledigen.")
                }
                MaintenanceReminderStatus.DUE_SOON -> {
                    createNotification(reminder, "Bald fällig - bitte terminieren.")
                }
                else -> { /* Keine Aktion */ }
            }
        }
    }

    /**
     * Erstellt eine Benachrichtigung
     */
    private fun createNotification(reminder: MaintenanceReminder, message: String) {
        if (notifications.any {
                it.reminderId == reminder.id && it.message == message && !it.isDismissed
            }) {
            return
        }
        val notification = MaintenanceReminderNotification(
            reminderId = reminder.id,
            type = reminder.type,
            title = reminder.title,
            message = message,
            priority = reminder.priority
        )
        notifications.add(0, notification)
        if (notifications.size > MAX_NOTIFICATIONS) {
            notifications.removeAt(notifications.lastIndex)
        }
    }

    /**
     * Gibt ungelesene Benachrichtigungen zurück
     */
    fun getUnreadNotifications(): List<MaintenanceReminderNotification> {
        return notifications.filter { !it.isRead && !it.isDismissed }
    }

    /**
     * Markiert eine Benachrichtigung als gelesen
     */
    fun markNotificationRead(notificationId: String) {
        notifications.forEachIndexed { index, notification ->
            if (notification.reminderId == notificationId) {
                notifications[index] = notification.copy(isRead = true)
            }
        }
    }

    /**
     * Verwirft eine Benachrichtigung
     */
    fun dismissNotification(notificationId: String) {
        notifications.forEachIndexed { index, notification ->
            if (notification.reminderId == notificationId) {
                notifications[index] = notification.copy(isDismissed = true)
            }
        }
    }

    // ===== Öltemperatur-Historie =====

    /**
     * Fügt einen Öltemperatur-Eintrag hinzu
     */
    fun addOilTempEntry(
        oilTempC: Double,
        coolantTempC: Double,
        engineLoad: Double,
        rpm: Double,
        speedKmh: Double
    ) {
        val entry = OilTempHistoryEntry(
            timestamp = System.currentTimeMillis(),
            oilTempC = oilTempC,
            coolantTempC = coolantTempC,
            engineLoad = engineLoad,
            rpm = rpm,
            speedKmh = speedKmh
        )
        oilTempHistory.add(entry)
        if (oilTempHistory.size > MAX_OIL_TEMP_ENTRIES) {
            oilTempHistory.removeAt(0)
        }
    }

    /**
     * Gibt die Öltemperatur-Historie zurück
     */
    fun getOilTempHistory(): List<OilTempHistoryEntry> = oilTempHistory.toList()

    /**
     * Berechnet die Öltemperatur-Statistik
     */
    fun getOilTempStatistics(): OilTempStatistics {
        if (oilTempHistory.isEmpty()) {
            return OilTempStatistics()
        }

        val temps = oilTempHistory.map { it.oilTempC }
        val avg = temps.average()
        val min = temps.minOrNull() ?: 0.0
        val max = temps.maxOrNull() ?: 0.0

        val optimalCount = temps.count { it in OIL_TEMP_OPTIMAL_MIN..OIL_TEMP_OPTIMAL_MAX }
        val optimalPercent = (optimalCount.toDouble() / temps.size * 100).roundToInt()

        val tooHotCount = temps.count { it > OIL_TEMP_WARNING }
        val tooHotPercent = (tooHotCount.toDouble() / temps.size * 100).roundToInt()

        val trend = calculateTrend(temps)

        return OilTempStatistics(
            averageTempC = avg,
            minTempC = min,
            maxTempC = max,
            optimalPercent = optimalPercent,
            tooHotPercent = tooHotPercent,
            entryCount = oilTempHistory.size,
            trend = trend,
            lastEntry = oilTempHistory.lastOrNull()
        )
    }

    /**
     * Gibt die durchschnittliche Betriebstemperatur zurück
     */
    fun getAverageOperatingTemp(): Double {
        val operatingTemps = oilTempHistory.filter { it.category == OilTempCategory.OPTIMAL }
        return if (operatingTemps.isNotEmpty()) {
            operatingTemps.map { it.oilTempC }.average()
        } else {
            0.0
        }
    }

    // ===== Kraftstoffverbrauch-Trend =====

    /**
     * Fügt einen Kraftstoffverbrauch-Eintrag hinzu
     */
    fun addFuelConsumptionEntry(
        consumptionL100km: Double,
        distanceKm: Double,
        fuelUsedLiters: Double,
        speedKmh: Double,
        rpm: Double,
        engineLoad: Double,
        isHighway: Boolean = false,
        isCity: Boolean = false
    ) {
        if (consumptionL100km <= 0 || consumptionL100km > 20) return // Ungültige Werte ignorieren

        val entry = FuelConsumptionEntry(
            timestamp = System.currentTimeMillis(),
            consumptionL100km = consumptionL100km,
            distanceKm = distanceKm,
            fuelUsedLiters = fuelUsedLiters,
            speedKmh = speedKmh,
            rpm = rpm,
            engineLoad = engineLoad,
            isHighway = isHighway,
            isCity = isCity
        )
        fuelConsumptionHistory.add(entry)
        if (fuelConsumptionHistory.size > MAX_FUEL_ENTRIES) {
            fuelConsumptionHistory.removeAt(0)
        }
    }

    /**
     * Gibt die Kraftstoffverbrauch-Historie zurück
     */
    fun getFuelConsumptionHistory(): List<FuelConsumptionEntry> = fuelConsumptionHistory.toList()

    /**
     * Berechnet die Kraftstoffverbrauch-Statistik
     */
    fun getFuelConsumptionStatistics(): FuelConsumptionStatistics {
        if (fuelConsumptionHistory.isEmpty()) {
            return FuelConsumptionStatistics()
        }

        val consumptions = fuelConsumptionHistory.map { it.consumptionL100km }
        val avg = consumptions.average()
        val min = consumptions.minOrNull() ?: 0.0
        val max = consumptions.maxOrNull() ?: 0.0

        val targetCount = consumptions.count { it <= FUEL_CONSUMPTION_TARGET }
        val targetPercent = (targetCount.toDouble() / consumptions.size * 100).roundToInt()

        val warningCount = consumptions.count { it > FUEL_CONSUMPTION_WARNING }
        val warningPercent = (warningCount.toDouble() / consumptions.size * 100).roundToInt()

        val trend = calculateTrend(consumptions)

        // Stadt vs. Autobahn Verbrauch
        val cityEntries = fuelConsumptionHistory.filter { it.isCity }
        val highwayEntries = fuelConsumptionHistory.filter { it.isHighway }
        val avgCity = if (cityEntries.isNotEmpty()) cityEntries.map { it.consumptionL100km }.average() else null
        val avgHighway = if (highwayEntries.isNotEmpty()) highwayEntries.map { it.consumptionL100km }.average() else null

        return FuelConsumptionStatistics(
            averageConsumption = avg,
            minConsumption = min,
            maxConsumption = max,
            targetPercent = targetPercent,
            warningPercent = warningPercent,
            entryCount = fuelConsumptionHistory.size,
            trend = trend,
            lastEntry = fuelConsumptionHistory.lastOrNull(),
            averageCityConsumption = avgCity,
            averageHighwayConsumption = avgHighway
        )
    }

    /**
     * Berechnet den Trend basierend auf den letzten Einträgen
     */
    private fun calculateTrend(values: List<Double>): TrendDirection {
        if (values.size < 5) return TrendDirection.UNKNOWN

        val recentValues = values.takeLast(10)
        val olderValues = values.take(maxOf(0, values.size - 20)).takeLast(10)

        if (olderValues.isEmpty()) return TrendDirection.UNKNOWN

        val recentAvg = recentValues.average()
        val olderAvg = olderValues.average()
        val diff = recentAvg - olderAvg
        val percentDiff = if (olderAvg != 0.0) abs(diff / olderAvg * 100) else 0.0

        return when {
            percentDiff < 5 -> TrendDirection.STABLE
            diff > 0 -> TrendDirection.WORSENING
            else -> TrendDirection.IMPROVING
        }
    }

    /**
     * Gibt eine Zusammenfassung aller Wartungen zurück
     */
    fun getMaintenanceSummary(): MaintenanceSummary {
        val allReminders = reminders.values.toList()

        val overdueCount = allReminders.count { it.status == MaintenanceReminderStatus.OVERDUE }
        val dueSoonCount = allReminders.count { it.status == MaintenanceReminderStatus.DUE_SOON }
        val upcomingCount = allReminders.count { it.status == MaintenanceReminderStatus.UPCOMING }
        val okCount = allReminders.count { it.status == MaintenanceReminderStatus.OK }
        val completedCount = allReminders.count { it.status == MaintenanceReminderStatus.COMPLETED }

        val overallStatus = when {
            overdueCount > 0 -> MaintenanceReminderStatus.OVERDUE
            dueSoonCount > 0 -> MaintenanceReminderStatus.DUE_SOON
            upcomingCount > 0 -> MaintenanceReminderStatus.UPCOMING
            else -> MaintenanceReminderStatus.OK
        }

        val nextService = allReminders
            .filter { it.isActive && !it.isCompleted }
            .minByOrNull { it.kmRemaining.coerceAtLeast(0) }

        return MaintenanceSummary(
            totalItems = allReminders.size,
            overdueCount = overdueCount,
            dueSoonCount = dueSoonCount,
            upcomingCount = upcomingCount,
            okCount = okCount,
            completedCount = completedCount,
            overallStatus = overallStatus,
            nextServiceType = nextService?.type,
            nextServiceKm = nextService?.kmRemaining ?: 0,
            nextServiceMonths = nextService?.monthsRemaining ?: 0
        )
    }

    /**
     * Berechnet die geschätzten Wartungskosten
     */
    fun estimateMaintenanceCosts(): MaintenanceCostEstimate {
        val overdueReminders = getOverdueReminders()
        val dueSoonReminders = getDueSoonReminders()

        // Geschätzte Kosten (in Euro)
        var totalCost = 0.0
        val details = mutableListOf<MaintenanceCostDetail>()

        overdueReminders.forEach { reminder ->
            val cost = estimateItemCost(reminder.type)
            totalCost += cost
            details.add(MaintenanceCostDetail(reminder.type, cost, true))
        }

        dueSoonReminders.forEach { reminder ->
            val cost = estimateItemCost(reminder.type)
            totalCost += cost
            details.add(MaintenanceCostDetail(reminder.type, cost, false))
        }

        return MaintenanceCostEstimate(
            totalEstimated = totalCost,
            urgentItems = overdueReminders.size,
            upcomingItems = dueSoonReminders.size,
            details = details
        )
    }

    /**
     * Schätzt die Kosten für eine bestimmte Wartung
     */
    private fun estimateItemCost(type: MaintenanceType): Double {
        return when (type) {
            MaintenanceType.OIL_CHANGE -> OIL_CHANGE_COST
            MaintenanceType.SPARK_PLUGS -> SPARK_PLUGS_COST
            MaintenanceType.AIR_FILTER -> AIR_FILTER_COST
            MaintenanceType.BRAKE_PADS -> BRAKE_PADS_COST
            MaintenanceType.TURBO_INSPECTION -> TURBO_INSPECTION_COST
            MaintenanceType.COOLANT -> COOLANT_COST
            MaintenanceType.TRANSMISSION_FLUID -> TRANSMISSION_FLUID_COST
            MaintenanceType.TIRES -> TIRES_COST
            MaintenanceType.INSPECTION -> INSPECTION_COST
            MaintenanceType.TURBO_BOOST_CHECK -> TURBO_INSPECTION_COST
            // Steuerkette A14NET: ca. 800-1500 EUR (eigene Konstante statt
            // Turbo-Inspektionskosten).
            MaintenanceType.TIMING_CHAIN -> TIMING_CHAIN_COST
        }
    }

    fun getMaintenanceSpec(type: MaintenanceType): AstraJ14TurboMaintenanceData.MaintenanceSpec? {
        return AstraJ14TurboMaintenanceData.getSpecForType(type)
    }

    /**
     * Exportiert die Erinnerungen als Liste für die Persistenz
     */
    fun exportReminders(): List<MaintenanceReminderExport> {
        return reminders.values.map { reminder ->
            MaintenanceReminderExport(
                id = reminder.id,
                type = reminder.type.name,
                lastServiceKm = reminder.lastServiceKm,
                lastServiceDate = reminder.lastServiceDate,
                intervalKm = reminder.intervalKm,
                intervalMonths = reminder.intervalMonths,
                drivingConditions = reminder.drivingConditions.name,
                isCompleted = reminder.isCompleted,
                completedDate = reminder.completedDate,
                completedKm = reminder.completedKm
            )
        }
    }

    /**
     * Importiert Erinnerungen aus einer Liste
     */
    fun importReminders(exports: List<MaintenanceReminderExport>) {
        exports.forEach { export ->
            try {
                val reminder = MaintenanceReminder(
                    id = export.id,
                    type = MaintenanceType.valueOf(export.type),
                    title = MaintenanceType.valueOf(export.type).name,
                    description = "",
                    priority = ReminderPriority.MEDIUM,
                    triggerType = ReminderTriggerType.KM_OR_TIME,
                    intervalKm = export.intervalKm,
                    intervalMonths = export.intervalMonths,
                    lastServiceKm = export.lastServiceKm,
                    lastServiceDate = export.lastServiceDate,
                    drivingConditions = DrivingConditions.valueOf(export.drivingConditions),
                    isCompleted = export.isCompleted,
                    completedDate = export.completedDate,
                    completedKm = export.completedKm
                )
                reminders[reminder.id] = reminder
            } catch (e: Exception) {
                // Ungültige Daten überspringen
            }
        }
    }
}

/**
 * Öltemperatur-Statistiken
 */
data class OilTempStatistics(
    val averageTempC: Double = 0.0,
    val minTempC: Double = 0.0,
    val maxTempC: Double = 0.0,
    val optimalPercent: Int = 0,
    val tooHotPercent: Int = 0,
    val entryCount: Int = 0,
    val trend: TrendDirection = TrendDirection.UNKNOWN,
    val lastEntry: OilTempHistoryEntry? = null
)

/**
 * Kraftstoffverbrauch-Statistiken
 */
data class FuelConsumptionStatistics(
    val averageConsumption: Double = 0.0,
    val minConsumption: Double = 0.0,
    val maxConsumption: Double = 0.0,
    val targetPercent: Int = 0,
    val warningPercent: Int = 0,
    val entryCount: Int = 0,
    val trend: TrendDirection = TrendDirection.UNKNOWN,
    val lastEntry: FuelConsumptionEntry? = null,
    val averageCityConsumption: Double? = null,
    val averageHighwayConsumption: Double? = null
)

/**
 * Geschätzte Wartungskosten
 */
data class MaintenanceCostEstimate(
    val totalEstimated: Double,
    val urgentItems: Int,
    val upcomingItems: Int,
    val details: List<MaintenanceCostDetail>
)

/**
 * Detail zu einer Wartungskosten-Schätzung
 */
data class MaintenanceCostDetail(
    val type: MaintenanceType,
    val estimatedCost: Double,
    val isUrgent: Boolean
)

/**
 * Export-Format für Erinnerungen
 */
data class MaintenanceReminderExport(
    val id: String,
    val type: String,
    val lastServiceKm: Int,
    val lastServiceDate: Long,
    val intervalKm: Int,
    val intervalMonths: Int,
    val drivingConditions: String,
    val isCompleted: Boolean,
    val completedDate: Long?,
    val completedKm: Int?
)
