package com.canopobd.data.maintenance

import com.canopobd.data.model.MaintenanceType
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object MaintenanceScheduler {
    
    private val scheduledReminders = ConcurrentHashMap<String, ScheduledMaintenance>()
    private val maintenanceAlerts = CopyOnWriteArrayList<MaintenanceAlert>()
    
    // Store last service data for each maintenance type
    private val lastServiceData = ConcurrentHashMap<MaintenanceType, ServiceRecord>()
    
    const val ALERT_THRESHOLD_KM = 500
    const val ALERT_THRESHOLD_DAYS = 14
    
    data class ScheduledMaintenance(
        val id: String,
        val type: MaintenanceType,
        val title: String,
        val lastServiceKm: Int,
        val lastServiceDate: Long,
        val intervalKm: Int,
        val intervalMonths: Int,
        val nextDueKm: Int,
        val nextDueDate: Long,
        val priority: ReminderPriority,
        val status: MaintenanceReminderStatus,
        val costEstimate: CostEstimate,
        val partNumber: String,
        val alternatives: List<String>,
        val notes: String
    )
    
    data class CostEstimate(
        val diyMin: Double,
        val diyMax: Double,
        val workshopMin: Double,
        val workshopMax: Double,
        val laborHours: Double = 0.0
    ) {
        val averageDiy: Double get() = (diyMin + diyMax) / 2
        val averageWorkshop: Double get() = (workshopMin + workshopMax) / 2
        val savingsPotential: Double get() = averageWorkshop - averageDiy
    }
    
    data class MaintenanceAlert(
        val id: String,
        val type: MaintenanceType,
        val title: String,
        val message: String,
        val alertType: AlertType,
        val priority: ReminderPriority,
        val kmRemaining: Int,
        val daysRemaining: Int,
        val estimatedCost: Double,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    enum class AlertType {
        OVERDUE,
        CRITICAL,
        WARNING,
        INFO
    }
    
    fun initialize(currentKm: Int, currentDate: Long = System.currentTimeMillis()) {
        scheduledReminders.clear()
        maintenanceAlerts.clear()
        
        createOilChangeSchedule(currentKm, currentDate)
        createSparkPlugSchedule(currentKm, currentDate)
        createAirFilterSchedule(currentKm, currentDate)
        createCoolantSchedule(currentKm, currentDate)
        createTurboInspectionSchedule(currentKm, currentDate)
        createTransmissionFluidSchedule(currentKm, currentDate)
        createBrakePadsFrontSchedule(currentKm, currentDate)
        createBrakePadsRearSchedule(currentKm, currentDate)
        createTimingChainSchedule(currentKm, currentDate)
        createInspectionSchedule(currentKm, currentDate)
    }
    
    private fun createOilChangeSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 15000
        val intervalMonths = 12
        val lastServiceKm = findLastServiceKm(MaintenanceType.OIL_CHANGE, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.OIL_CHANGE, currentDate)
        
        scheduledReminders["oil_change"] = ScheduledMaintenance(
            id = "oil_change",
            type = MaintenanceType.OIL_CHANGE,
            title = "Ölwechsel",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.HIGH,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 35.0,
                diyMax = 50.0,
                workshopMin = 80.0,
                workshopMax = 120.0,
                laborHours = 0.5
            ),
            partNumber = "Opel 13538630 / Mann HU7019z",
            alternatives = listOf(
                "Mann HU7019z",
                "Bosch P7024",
                "Mahle OX353D",
                "Blue Print ADG02116"
            ),
            notes = "Dexos2 5W-30 Pflicht! 4.5L inkl. Filter. Bei Kurzstrecke/Stadtverkehr: 10.000km / 8 Monate"
        )
    }
    
    private fun createSparkPlugSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 60000
        val intervalMonths = 48
        val lastServiceKm = findLastServiceKm(MaintenanceType.SPARK_PLUGS, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.SPARK_PLUGS, currentDate)
        
        scheduledReminders["spark_plugs"] = ScheduledMaintenance(
            id = "spark_plugs",
            type = MaintenanceType.SPARK_PLUGS,
            title = "Zündkerzen",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.MEDIUM,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 40.0,
                diyMax = 60.0,
                workshopMin = 100.0,
                workshopMax = 150.0,
                laborHours = 1.0
            ),
            partNumber = "NGK LZKR6AP-11G",
            alternatives = listOf(
                "Bosch FR7HPP332",
                "Denso SC16HL11",
                "Champion RC10PYPB4"
            ),
            notes = "Gap 0.7mm! Drehmoment 20-25Nm. Bei Kurzstrecke: 30.000km"
        )
    }
    
    private fun createAirFilterSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 30000
        val intervalMonths = 24
        val lastServiceKm = findLastServiceKm(MaintenanceType.AIR_FILTER, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.AIR_FILTER, currentDate)
        
        scheduledReminders["air_filter"] = ScheduledMaintenance(
            id = "air_filter",
            type = MaintenanceType.AIR_FILTER,
            title = "Luftfilter",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.MEDIUM,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 20.0,
                diyMax = 35.0,
                workshopMin = 40.0,
                workshopMax = 60.0,
                laborHours = 0.25
            ),
            partNumber = "Opel 13536248",
            alternatives = listOf(
                "Mann C30132/1",
                "Bosch F026400132",
                "Mahle LX3053",
                "K&N 33-3003"
            ),
            notes = "Bei Staub/Schmutz häufiger prüfen. Einfacher Selberwechsel"
        )
    }
    
    private fun createCoolantSchedule(currentKm: Int, currentDate: Long) {
        val intervalKmFirst = 80000
        val intervalMonthsFirst = 60
        val intervalKmSubsequent = 40000
        val intervalMonthsSubsequent = 24
        val lastServiceKm = findLastServiceKm(MaintenanceType.COOLANT, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.COOLANT, currentDate)
        
        val isFirstChange = lastServiceKm == 0
        val intervalKm = if (isFirstChange) intervalKmFirst else intervalKmSubsequent
        val intervalMonths = if (isFirstChange) intervalMonthsFirst else intervalMonthsSubsequent
        
        scheduledReminders["coolant"] = ScheduledMaintenance(
            id = "coolant",
            type = MaintenanceType.COOLANT,
            title = "Kühlmittel",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.LOW,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 30.0,
                diyMax = 55.0,
                workshopMin = 100.0,
                workshopMax = 200.0,
                laborHours = 1.0
            ),
            partNumber = "GM Dex-Cool 12378464",
            alternatives = listOf(
                "Opel 1940665",
                "Pentosin 11-2025-3090-104",
                "ACDelco 10-9390"
            ),
            notes = "Dex-Cool (orange) Pflicht! 5.7L System. Erster Wechsel 5J/80.000km, dann alle 2J/40.000km"
        )
    }
    
    private fun createTurboInspectionSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 60000
        val intervalMonths = 48
        val lastServiceKm = findLastServiceKm(MaintenanceType.TURBO_INSPECTION, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.TURBO_INSPECTION, currentDate)
        
        scheduledReminders["turbo_inspection"] = ScheduledMaintenance(
            id = "turbo_inspection",
            type = MaintenanceType.TURBO_INSPECTION,
            title = "Turbo-Inspektion",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.MEDIUM,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 0.0,
                diyMax = 0.0,
                workshopMin = 80.0,
                workshopMax = 150.0,
                laborHours = 1.5
            ),
            partNumber = "N/A - Sichtprüfung",
            alternatives = emptyList(),
            notes = "BorgWarner KP39: Ölleitungen, Wastegate, Ladedruck prüfen. Werkstatt empfohlen"
        )
    }
    
    private fun createTransmissionFluidSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 60000
        val intervalMonths = 48
        val lastServiceKm = findLastServiceKm(MaintenanceType.TRANSMISSION_FLUID, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.TRANSMISSION_FLUID, currentDate)
        
        scheduledReminders["transmission_fluid"] = ScheduledMaintenance(
            id = "transmission_fluid",
            type = MaintenanceType.TRANSMISSION_FLUID,
            title = "Getriebeöl",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.LOW,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 40.0,
                diyMax = 60.0,
                workshopMin = 100.0,
                workshopMax = 180.0,
                laborHours = 0.5
            ),
            partNumber = "GM Fluid 1940182",
            alternatives = listOf(
                "Dexron VI ATF",
                "ACDelco 10-9395",
                "Mobil 1 1940658"
            ),
            notes = "Dexron VI ATF. M32 Getriebe: 2.7L. Nur saugfähige Dichtung verwenden!"
        )
    }
    
    private fun createBrakePadsFrontSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 30000
        val intervalMonths = 24
        val lastServiceKm = findLastServiceKm(MaintenanceType.BRAKE_PADS, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.BRAKE_PADS, currentDate)
        
        scheduledReminders["brake_pads_front"] = ScheduledMaintenance(
            id = "brake_pads_front",
            type = MaintenanceType.BRAKE_PADS,
            title = "Bremsbeläge vorne",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.HIGH,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 30.0,
                diyMax = 50.0,
                workshopMin = 80.0,
                workshopMax = 150.0,
                laborHours = 0.75
            ),
            partNumber = "Opel 13501636",
            alternatives = listOf(
                "TRW D1428L",
                "Akebono ACT1428",
                "Bosch BC1428",
                "Brembo P50073"
            ),
            notes = "286mm Scheibendurchmesser. Bei viel Stadtverkehr/bergig häufiger prüfen"
        )
    }
    
    private fun createBrakePadsRearSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 40000
        val intervalMonths = 36
        val lastServiceKm = findLastServiceKm(MaintenanceType.BRAKE_PADS, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.BRAKE_PADS, currentDate)
        
        scheduledReminders["brake_pads_rear"] = ScheduledMaintenance(
            id = "brake_pads_rear",
            type = MaintenanceType.BRAKE_PADS,
            title = "Bremsbeläge hinten",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.HIGH,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 25.0,
                diyMax = 40.0,
                workshopMin = 70.0,
                workshopMax = 120.0,
                laborHours = 0.5
            ),
            partNumber = "Opel 13501637",
            alternatives = listOf(
                "TRW D1429L",
                "Akebono ACT1429",
                "Bosch BC1429",
                "Brembo P50072"
            ),
            notes = "258mm Scheibendurchmesser. Seltener als vorne, aber trotzdem wichtig"
        )
    }
    
    private fun createTimingChainSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 80000
        val intervalMonths = 60
        val lastServiceKm = findLastServiceKm(MaintenanceType.TURBO_BOOST_CHECK, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.TURBO_BOOST_CHECK, currentDate)
        
        scheduledReminders["timing_chain"] = ScheduledMaintenance(
            id = "timing_chain",
            type = MaintenanceType.TURBO_BOOST_CHECK,
            title = "Timing-Kette Prüfung",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.CRITICAL,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 0.0,
                diyMax = 0.0,
                workshopMin = 80.0,
                workshopMax = 150.0,
                laborHours = 2.0
            ),
            partNumber = "N/A - Prüfung",
            alternatives = emptyList(),
            notes = "A14NET bekanntes Problem: Rattern bei Kaltstart ab 80.000km! P0340/P0341 Fehlercodes beachten"
        )
    }
    
    private fun createInspectionSchedule(currentKm: Int, currentDate: Long) {
        val intervalKm = 60000
        val intervalMonths = 24
        val lastServiceKm = findLastServiceKm(MaintenanceType.INSPECTION, currentKm)
        val lastServiceDate = findLastServiceDate(MaintenanceType.INSPECTION, currentDate)
        
        scheduledReminders["inspection"] = ScheduledMaintenance(
            id = "inspection",
            type = MaintenanceType.INSPECTION,
            title = "TÜV / AU",
            lastServiceKm = lastServiceKm,
            lastServiceDate = lastServiceDate,
            intervalKm = intervalKm,
            intervalMonths = intervalMonths,
            nextDueKm = lastServiceKm + intervalKm,
            nextDueDate = addMonths(lastServiceDate, intervalMonths),
            priority = ReminderPriority.CRITICAL,
            status = calculateStatus(currentKm, currentDate, lastServiceKm, lastServiceDate, intervalKm, intervalMonths),
            costEstimate = CostEstimate(
                diyMin = 0.0,
                diyMax = 0.0,
                workshopMin = 100.0,
                workshopMax = 200.0,
                laborHours = 0.0
            ),
            partNumber = "N/A - Pflichtuntersuchung",
            alternatives = emptyList(),
            notes = "Gesetzliche Pflicht! Alle 2 Jahre. HU + AU (Abgas)"
        )
    }
    
    fun calculateStatus(
        currentKm: Int,
        currentDate: Long,
        lastServiceKm: Int,
        lastServiceDate: Long,
        intervalKm: Int,
        intervalMonths: Int
    ): MaintenanceReminderStatus {
        val kmRemaining = (lastServiceKm + intervalKm) - currentKm
        val daysRemaining = calculateDaysRemaining(currentDate, lastServiceDate, intervalMonths)
        
        return when {
            kmRemaining < -ALERT_THRESHOLD_KM || daysRemaining < -30 -> MaintenanceReminderStatus.OVERDUE
            kmRemaining < ALERT_THRESHOLD_KM || daysRemaining < 30 -> MaintenanceReminderStatus.DUE_SOON
            kmRemaining < intervalKm / 4 || daysRemaining < 90 -> MaintenanceReminderStatus.UPCOMING
            else -> MaintenanceReminderStatus.OK
        }
    }
    
    fun calculateDaysRemaining(currentDate: Long, lastServiceDate: Long, intervalMonths: Int): Int {
        val nextDueDate = addMonths(lastServiceDate, intervalMonths)
        val diffMs = nextDueDate - currentDate
        return (diffMs / (24 * 60 * 60 * 1000)).toInt()
    }
    
    fun getAllScheduledMaintenance(): List<ScheduledMaintenance> {
        return scheduledReminders.values.toList()
    }
    
    fun getScheduledMaintenance(id: String): ScheduledMaintenance? {
        return scheduledReminders[id]
    }
    
    fun getOverdueMaintenance(): List<ScheduledMaintenance> {
        return scheduledReminders.values.filter { it.status == MaintenanceReminderStatus.OVERDUE }
    }
    
    fun getDueSoonMaintenance(): List<ScheduledMaintenance> {
        return scheduledReminders.values.filter { it.status == MaintenanceReminderStatus.DUE_SOON }
    }
    
    fun getUpcomingMaintenance(): List<ScheduledMaintenance> {
        return scheduledReminders.values.filter { it.status == MaintenanceReminderStatus.UPCOMING }
    }
    
    fun getMaintenanceByType(type: MaintenanceType): ScheduledMaintenance? {
        return scheduledReminders.values.find { it.type == type }
    }
    
    fun completeMaintenance(id: String, km: Int, date: Long = System.currentTimeMillis()) {
        scheduledReminders[id]?.let { current ->
            scheduledReminders[id] = current.copy(
                lastServiceKm = km,
                lastServiceDate = date,
                nextDueKm = km + current.intervalKm,
                nextDueDate = addMonths(date, current.intervalMonths),
                status = MaintenanceReminderStatus.OK
            )
        }
    }
    
    fun generateAlerts(currentKm: Int, currentDate: Long = System.currentTimeMillis()) {
        maintenanceAlerts.clear()
        
        scheduledReminders.values.forEach { scheduled ->
            val kmRemaining = scheduled.nextDueKm - currentKm
            val daysRemaining = calculateDaysRemaining(currentDate, scheduled.lastServiceDate, scheduled.intervalMonths)
            
            val alertType = when {
                kmRemaining < -ALERT_THRESHOLD_KM || daysRemaining < -30 -> AlertType.OVERDUE
                kmRemaining < 0 || daysRemaining < 0 -> AlertType.CRITICAL
                kmRemaining < ALERT_THRESHOLD_KM || daysRemaining < 30 -> AlertType.WARNING
                kmRemaining < scheduled.intervalKm / 4 || daysRemaining < 90 -> AlertType.INFO
                else -> null
            }
            
            alertType?.let { type ->
                val priority = when (type) {
                    AlertType.OVERDUE -> ReminderPriority.CRITICAL
                    AlertType.CRITICAL -> ReminderPriority.HIGH
                    AlertType.WARNING -> ReminderPriority.MEDIUM
                    AlertType.INFO -> ReminderPriority.LOW
                }
                
                val message = when (type) {
                    AlertType.OVERDUE -> "${scheduled.title} ist überfällig! Sofort erledigen."
                    AlertType.CRITICAL -> "${scheduled.title} ist überfällig: ${-kmRemaining}km bzw. ${-daysRemaining} Tage"
                    AlertType.WARNING -> "${scheduled.title} in Kürze fällig: ${kmRemaining}km oder ${daysRemaining} Tage"
                    AlertType.INFO -> "${scheduled.title} in ~${scheduled.intervalKm / 4}km oder ${scheduled.intervalMonths / 4} Monaten fällig"
                }
                
                maintenanceAlerts.add(MaintenanceAlert(
                    id = "${scheduled.id}_alert",
                    type = scheduled.type,
                    title = scheduled.title,
                    message = message,
                    alertType = type,
                    priority = priority,
                    kmRemaining = kmRemaining,
                    daysRemaining = daysRemaining,
                    estimatedCost = scheduled.costEstimate.averageWorkshop
                ))
            }
        }
    }
    
    fun getActiveAlerts(): List<MaintenanceAlert> {
        return maintenanceAlerts.filter { it.alertType != AlertType.INFO }.sortedByDescending { it.priority.ordinal }
    }
    
    fun getAllAlerts(): List<MaintenanceAlert> {
        return maintenanceAlerts.sortedByDescending { it.priority.ordinal }
    }
    
    fun getTotalEstimatedCosts(): MaintenanceCostSummary {
        val urgentAlerts = maintenanceAlerts.filter { it.alertType == AlertType.OVERDUE || it.alertType == AlertType.CRITICAL }
        val warningAlerts = maintenanceAlerts.filter { it.alertType == AlertType.WARNING }
        val infoAlerts = maintenanceAlerts.filter { it.alertType == AlertType.INFO }
        
        return MaintenanceCostSummary(
            urgentTotal = urgentAlerts.sumOf { it.estimatedCost },
            warningTotal = warningAlerts.sumOf { it.estimatedCost },
            infoTotal = infoAlerts.sumOf { it.estimatedCost },
            potentialSavings = scheduledReminders.values.sumOf { it.costEstimate.savingsPotential * 0.5 },
            urgentCount = urgentAlerts.size,
            warningCount = warningAlerts.size,
            infoCount = infoAlerts.size
        )
    }
    
    fun getNextService(): ScheduledMaintenance? {
        return scheduledReminders.values
            .filter { it.status != MaintenanceReminderStatus.COMPLETED }
            .minByOrNull { it.nextDueKm }
    }
    
    fun getMaintenanceTimeline(currentKm: Int): List<TimelineEntry> {
        val entries = mutableListOf<TimelineEntry>()
        
        scheduledReminders.values.forEach { scheduled ->
            val kmRemaining = scheduled.nextDueKm - currentKm
            entries.add(TimelineEntry(
                id = scheduled.id,
                type = scheduled.type,
                title = scheduled.title,
                dueKm = scheduled.nextDueKm,
                kmRemaining = kmRemaining,
                daysRemaining = calculateDaysRemaining(System.currentTimeMillis(), scheduled.lastServiceDate, scheduled.intervalMonths),
                status = scheduled.status,
                priority = scheduled.priority,
                estimatedCost = scheduled.costEstimate.averageWorkshop
            ))
        }
        
        return entries.sortedBy { it.dueKm }
    }
    
    data class TimelineEntry(
        val id: String,
        val type: MaintenanceType,
        val title: String,
        val dueKm: Int,
        val kmRemaining: Int,
        val daysRemaining: Int,
        val status: MaintenanceReminderStatus,
        val priority: ReminderPriority,
        val estimatedCost: Double
    )
    
    data class MaintenanceCostSummary(
        val urgentTotal: Double,
        val warningTotal: Double,
        val infoTotal: Double,
        val potentialSavings: Double,
        val urgentCount: Int,
        val warningCount: Int,
        val infoCount: Int
    ) {
        val total: Double get() = urgentTotal + warningTotal + infoTotal
    }
    
    private fun findLastServiceKm(type: MaintenanceType, currentKm: Int): Int {
        return lastServiceData[type]?.lastServiceKm ?: 0
    }
    
    private fun findLastServiceDate(type: MaintenanceType, currentDate: Long): Long {
        return lastServiceData[type]?.lastServiceDate ?: (currentDate - (30L * 24 * 60 * 60 * 1000))
    }
    
    /**
     * Updates the last service record for a maintenance type.
     * Call this when a service is completed.
     */
    fun updateLastService(type: MaintenanceType, serviceKm: Int, serviceDate: Long) {
        lastServiceData[type] = ServiceRecord(serviceKm, serviceDate)
    }
    
    /**
     * Clears the last service record for a maintenance type.
     */
    fun clearLastService(type: MaintenanceType) {
        lastServiceData.remove(type)
    }
    
    /**
     * Record to store last service information
     */
    private data class ServiceRecord(
        val lastServiceKm: Int,
        val lastServiceDate: Long
    )
    
    private fun addMonths(timestamp: Long, months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }
}
