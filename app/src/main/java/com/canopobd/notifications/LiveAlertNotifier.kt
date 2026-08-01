package com.canopobd.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.canopobd.MainActivity
import com.canopobd.R
import com.canopobd.data.model.ActiveAlert
import com.canopobd.data.model.AlertSeverity
import com.canopobd.data.model.AlertType
import kotlinx.coroutines.flow.MutableStateFlow

class LiveAlertNotifier(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "live_obd_alerts"
        const val NOTIFICATION_BASE_ID = 5000
    }

    private val manager: NotificationManager? =
        context.getSystemService(NotificationManager::class.java)

    private val inFlightIds = MutableStateFlow<Set<String>>(emptySet())

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.alert_channel_desc)
            enableLights(true)
            enableVibration(true)
        }
        manager?.createNotificationChannel(channel)
    }

    fun notifyChanges(alerts: List<ActiveAlert>) {
        val currentKeys = alerts.map { it.type.name }.toSet()

        // Clear notifications for types no longer present
        val previouslyActive = inFlightIds.value
        previouslyActive.filter { it !in currentKeys }.forEach { key ->
            manager?.cancel(NOTIFICATION_BASE_ID + key.hashCode())
        }

        // Post new/updated notifications
        alerts.forEach { alert ->
            val notification = buildNotification(alert)
            val id = NOTIFICATION_BASE_ID + alert.type.name.hashCode()
            manager?.notify(id, notification)
        }

        inFlightIds.value = currentKeys
    }

    fun clearAll() {
        manager?.cancelAll()
        inFlightIds.value = emptySet()
    }

    private fun buildNotification(alert: ActiveAlert): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, alert.type.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val priority = when (alert.severity) {
            AlertSeverity.CRITICAL -> NotificationCompat.PRIORITY_MAX
            AlertSeverity.WARNING -> NotificationCompat.PRIORITY_HIGH
            AlertSeverity.INFO -> NotificationCompat.PRIORITY_DEFAULT
        }
        val color = when (alert.severity) {
            AlertSeverity.CRITICAL -> 0xFFD32F2F.toInt()
            AlertSeverity.WARNING -> 0xFFFF9100.toInt()
            AlertSeverity.INFO -> 0xFF00BCD4.toInt()
        }

        val title = context.getString(
            R.string.alert_notification_title,
            alert.type.label,
            severityLabel(alert.severity)
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(alert.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
            .setPriority(priority)
            .setColor(color)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun severityLabel(severity: AlertSeverity): String = when (severity) {
        AlertSeverity.CRITICAL -> context.getString(R.string.critical)
        AlertSeverity.WARNING -> context.getString(R.string.warning)
        AlertSeverity.INFO -> context.getString(R.string.alert)
    }
}
