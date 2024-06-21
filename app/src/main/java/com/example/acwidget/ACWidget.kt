package com.example.acwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews

class ACWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Set up periodic updates
        val intent = Intent(context, ACWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 60000, // 60 seconds interval
            60000,
            pendingIntent
        )
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Cancel periodic updates when the last widget is disabled
        val intent = Intent(context, ACWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.acwidget.TOGGLE_POWER") {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val prefs = context.getSharedPreferences("ACWidgetPrefs", Context.MODE_PRIVATE)
                val isPowerOn = prefs.getBoolean("isPowerOn_$appWidgetId", false)

                // Toggle the power state
                val newPowerState = !isPowerOn
                prefs.edit().putBoolean("isPowerOn_$appWidgetId", newPowerState).apply()

                // Update the widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.a_c_widget)
    views.setTextViewText(R.id.tvTemperature, "24°C")
    views.setTextViewText(R.id.tvMode, "Auto")
    views.setImageViewResource(R.id.ivMode, R.drawable.ic_mode)

    val prefs = context.getSharedPreferences("ACWidgetPrefs", Context.MODE_PRIVATE)
    val isPowerOn = prefs.getBoolean("isPowerOn_$appWidgetId", false)

    // Set the Button state
    views.setTextViewText(R.id.btnPower, if (isPowerOn) "Off" else "On")
    views.setInt(
        R.id.btnPower,
        "setBackgroundResource",
        if (isPowerOn) R.drawable.button_background_disabled else R.drawable.button_background_enabled
    )

    // Set up intent for Button
    val intent = Intent(context, ACWidget::class.java)
    intent.action = "com.example.acwidget.TOGGLE_POWER"
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.btnPower, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
