package com.canopobd.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.SharedPreferences
import com.canopobd.R
import com.canopobd.MainActivity

class OBDWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs: SharedPreferences = context.getSharedPreferences("canop_obd_prefs", Context.MODE_PRIVATE)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_obd)

            val rpm = prefs.getFloat("widget_rpm", 0f)
            val speed = prefs.getFloat("widget_speed", 0f)
            val coolant = prefs.getFloat("widget_coolant", 0f)
            val load = prefs.getFloat("widget_load", 0f)
            val fuel = prefs.getFloat("widget_fuel", 0f)
            val unitKmh = prefs.getBoolean("unit_metric", true)

            views.setTextViewText(R.id.widget_rpm, "%.0f".format(rpm))
            views.setTextViewText(R.id.widget_speed, "%.0f".format(speed))
            views.setTextViewText(R.id.widget_speed_unit, if (unitKmh) "km/h" else "mph")
            views.setTextViewText(R.id.widget_coolant, "%.0f°C".format(coolant))
            views.setTextViewText(R.id.widget_load, "%.0f%%".format(load))
            views.setTextViewText(R.id.widget_fuel, "%.0f%%".format(fuel))

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
