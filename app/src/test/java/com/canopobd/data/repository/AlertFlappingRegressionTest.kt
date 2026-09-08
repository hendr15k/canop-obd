package com.canopobd.data.repository

import com.canopobd.data.model.ActiveAlert
import com.canopobd.data.model.AlertSeverity
import com.canopobd.data.model.AlertType
import com.canopobd.notifications.LiveAlertNotifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the sustained-alert flapping bug.
 *
 * Bug: OBDRepository.checkAlerts replaced the whole active-alert set with the
 * cooldown-filtered list, so a still-true alert vanished on the next poll and
 * LiveAlertNotifier cancelled it — the notification flapped ~1 poll per
 * 60 s cooldown window instead of staying. AlertConfig.hysteresisSeconds was
 * persisted but never read.
 *
 * Fix: activeAlerts mirrors all currently true conditions on every poll;
 * only fresh triggers (or re-triggers after hysteresisSeconds) go to
 * alertEvents for (re-)notification.
 */
class AlertFlappingRegressionTest {

    private fun alert(type: AlertType = AlertType.COOLANT): ActiveAlert = ActiveAlert(
        type = type,
        severity = AlertSeverity.WARNING,
        value = 110f,
        threshold = 105f,
        message = "test"
    )

    @Test
    fun `sustained alert stays active across polls inside hysteresis window`() {
        val lastTrigger = mutableMapOf<AlertType, Long>()
        val hysteresisMs = 10_000L

        // Poll 1: fresh trigger -> active + notify.
        val (active1, notify1) = OBDRepository.partitionAlertNotifications(
            previousActive = emptyMap(),
            candidates = listOf(alert()),
            now = 0L,
            lastTrigger = lastTrigger,
            hysteresisMs = hysteresisMs
        )
        assertEquals(1, active1.size)
        assertEquals(1, notify1.size)

        // Poll 2 (same condition, 1 s later): still active, but NOT re-notified.
        val (active2, notify2) = OBDRepository.partitionAlertNotifications(
            previousActive = active1.associateBy { it.type },
            candidates = listOf(alert()),
            now = 1_000L,
            lastTrigger = lastTrigger,
            hysteresisMs = hysteresisMs
        )
        assertEquals(1, active2.size)
        assertTrue(notify2.isEmpty())
    }

    @Test
    fun `sustained alert re-notifies after hysteresis expiry`() {
        val lastTrigger = mutableMapOf<AlertType, Long>()
        val hysteresisMs = 10_000L

        val (active1, _) = OBDRepository.partitionAlertNotifications(
            previousActive = emptyMap(),
            candidates = listOf(alert()),
            now = 0L,
            lastTrigger = lastTrigger,
            hysteresisMs = hysteresisMs
        )
        val (_, notify2) = OBDRepository.partitionAlertNotifications(
            previousActive = active1.associateBy { it.type },
            candidates = listOf(alert()),
            now = 10_001L,
            lastTrigger = lastTrigger,
            hysteresisMs = hysteresisMs
        )
        assertEquals(1, notify2.size)
    }

    @Test
    fun `cleared condition drops from active set and re-trigger notifies immediately`() {
        val lastTrigger = mutableMapOf<AlertType, Long>()
        val hysteresisMs = 60_000L

        val (active1, _) = OBDRepository.partitionAlertNotifications(
            previousActive = emptyMap(),
            candidates = listOf(alert()),
            now = 0L,
            lastTrigger = lastTrigger,
            hysteresisMs = hysteresisMs
        )
        // Condition clears.
        val (active2, notify2) = OBDRepository.partitionAlertNotifications(
            previousActive = active1.associateBy { it.type },
            candidates = emptyList(),
            now = 1_000L,
            lastTrigger = lastTrigger,
            hysteresisMs = hysteresisMs
        )
        assertTrue(active2.isEmpty())
        assertTrue(notify2.isEmpty())

        // Same condition re-triggers 2 s later: stale stamp was dropped, so it
        // notifies immediately instead of waiting out the old hysteresis.
        val (active3, notify3) = OBDRepository.partitionAlertNotifications(
            previousActive = emptyMap(),
            candidates = listOf(alert()),
            now = 2_000L,
            lastTrigger = lastTrigger,
            hysteresisMs = hysteresisMs
        )
        assertEquals(1, active3.size)
        assertEquals(1, notify3.size)
    }

    @Test
    fun `notification ids are stable non-negative ordinals without collisions`() {
        val ids = AlertType.values().map { LiveAlertNotifier.notificationIdForKey(it.name) }
        assertTrue(ids.all { it >= LiveAlertNotifier.NOTIFICATION_BASE_ID })
        assertEquals(ids.size, ids.toSet().size)
        // Same type maps to the same id on every call.
        assertEquals(
            LiveAlertNotifier.notificationIdForKey(AlertType.EGT.name),
            LiveAlertNotifier.notificationIdForKey(AlertType.EGT.name)
        )
    }
}
