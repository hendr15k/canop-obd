package com.canopobd.data.maintenance

import com.canopobd.data.model.MaintenanceType
import org.junit.Assert.assertEquals
import org.junit.Test

class MaintenanceReminderTest {

    @Test
    fun `time based reminder ignores unrelated odometer`() {
        val reminder = MaintenanceReminder(
            type = MaintenanceType.INSPECTION,
            title = "Inspection",
            description = "",
            priority = ReminderPriority.MEDIUM,
            triggerType = ReminderTriggerType.TIME_BASED,
            intervalKm = 60_000,
            intervalMonths = 24,
            currentKm = 100_000,
            currentDate = 1_000L
        )

        assertEquals(MaintenanceReminderStatus.OK, reminder.status)
    }

    @Test
    fun `severe driving uses configured severe interval`() {
        val reminder = MaintenanceReminder(
            type = MaintenanceType.OIL_CHANGE,
            title = "Oil",
            description = "",
            priority = ReminderPriority.HIGH,
            triggerType = ReminderTriggerType.KM_BASED,
            intervalKm = 15_000,
            intervalMonths = 12,
            severeIntervalKm = 10_000,
            drivingConditions = DrivingConditions.SEVERE,
            currentKm = 10_001
        )

        assertEquals(MaintenanceReminderStatus.OVERDUE, reminder.status)
    }
}
