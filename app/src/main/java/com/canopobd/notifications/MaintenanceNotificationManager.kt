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
import com.canopobd.data.local.MaintenanceEntity

class MaintenanceNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "maintenance_reminders"
        const val CHANNEL_NAME = "Wartungserinnerungen"
        const val CHANNEL_DESC = "Erinnerungen für anstehende Wartungsarbeiten"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    data class MaintenanceReminder(
        val type: String,
        val title: String,
        val message: String,
        val remainingKm: Int,
        val urgency: Urgency
    )

    enum class Urgency { LOW, MEDIUM, HIGH, CRITICAL }

    fun checkMaintenanceReminders(
        maintenanceItems: List<MaintenanceEntity>,
        currentKm: Int
    ): List<MaintenanceReminder> {
        val reminders = mutableListOf<MaintenanceReminder>()
        
        for (item in maintenanceItems) {
            val remaining = item.intervalKm - (currentKm - item.lastServiceKm)
            
            when {
                remaining <= 0 -> {
                    reminders.add(MaintenanceReminder(
                        type = item.type,
                        title = "${item.type} überfällig!",
                        message = "Fällig seit ${-remaining} km. Bitte sofort durchführen.",
                        remainingKm = remaining,
                        urgency = Urgency.CRITICAL
                    ))
                }
                remaining <= 1000 -> {
                    reminders.add(MaintenanceReminder(
                        type = item.type,
                        title = "${item.type} fällig in $remaining km",
                        message = "Bitte zeitnah planen.",
                        remainingKm = remaining,
                        urgency = Urgency.HIGH
                    ))
                }
                remaining <= 3000 -> {
                    reminders.add(MaintenanceReminder(
                        type = item.type,
                        title = "${item.type} in $remaining km",
                        message = "Nächste Wartung in Kürze.",
                        remainingKm = remaining,
                        urgency = Urgency.MEDIUM
                    ))
                }
                remaining <= 5000 -> {
                    reminders.add(MaintenanceReminder(
                        type = item.type,
                        title = "${item.type} in $remaining km",
                        message = "Bald fällig.",
                        remainingKm = remaining,
                        urgency = Urgency.LOW
                    ))
                }
            }
        }
        
        return reminders.sortedBy { it.remainingKm }
    }

    fun showMaintenanceNotification(reminder: MaintenanceReminder, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (reminder.urgency) {
            Urgency.CRITICAL -> NotificationCompat.PRIORITY_HIGH
            Urgency.HIGH -> NotificationCompat.PRIORITY_HIGH
            Urgency.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            Urgency.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminder.title)
            .setContentText(reminder.message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    fun showAllReminders(reminders: List<MaintenanceReminder>) {
        reminders.forEachIndexed { index, reminder ->
            showMaintenanceNotification(reminder, 1000 + index)
        }
    }

    fun cancelAll() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancelAll()
    }
}
