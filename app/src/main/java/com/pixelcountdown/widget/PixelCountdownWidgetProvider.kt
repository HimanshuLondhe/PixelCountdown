package com.pixelcountdown.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.pixelcountdown.MainActivity
import com.pixelcountdown.R
import com.pixelcountdown.data.CountdownCalculator
import com.pixelcountdown.data.CountdownRepository

class PixelCountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val repo = CountdownRepository.getInstance(context)

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, repo)
        }
        
        // Ensure service is running if we have widgets
        if (appWidgetIds.isNotEmpty()) {
            context.startService(Intent(context, WidgetUpdateService::class.java))
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        context.startService(Intent(context, WidgetUpdateService::class.java))
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.stopService(Intent(context, WidgetUpdateService::class.java))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            val repo = CountdownRepository.getInstance(context)
            if (ids != null) {
                for (id in ids) {
                    updateWidget(context, appWidgetManager, id, repo)
                }
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.pixelcountdown.ACTION_REFRESH_WIDGET"
        const val ACTION_SELECT_WIDGET_TIMER = "com.pixelcountdown.ACTION_SELECT_WIDGET_TIMER"
        private const val TAG = "PixelCountdownWidget"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            repo: CountdownRepository
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_pixel_countdown)
                val activeItem = repo.getWidgetCountdown(appWidgetId)

                // Setup click intent to open main activity with widget selection action
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_SELECT_WIDGET_TIMER
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                if (activeItem == null) {
                    // Empty state: prompt to tap and create one
                    views.setTextViewText(R.id.widget_title, context.getString(R.string.no_countdown))
                    views.setTextViewText(R.id.widget_target_date, context.getString(R.string.tap_to_set))
                    views.setViewVisibility(R.id.widget_units_container, View.GONE)
                    views.setViewVisibility(R.id.widget_completed_banner, View.GONE)
                } else {
                    views.setTextViewText(R.id.widget_title, activeItem.title)
                    views.setTextViewText(R.id.widget_target_date, activeItem.formattedTargetDateTime())

                    val remaining = CountdownCalculator.calculateRemaining(activeItem.targetEpochMillis)
                    if (remaining.isFinished) {
                        views.setViewVisibility(R.id.widget_units_container, View.GONE)
                        views.setViewVisibility(R.id.widget_completed_banner, View.VISIBLE)
                    } else {
                        views.setViewVisibility(R.id.widget_units_container, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_completed_banner, View.GONE)

                        views.setTextViewText(R.id.widget_val_years, remaining.years.toString())
                        views.setTextViewText(R.id.widget_val_months, remaining.months.toString())
                        views.setTextViewText(R.id.widget_val_days, remaining.days.toString())
                        views.setTextViewText(R.id.widget_val_hours, remaining.hours.toString())
                        views.setTextViewText(R.id.widget_val_mins, remaining.minutes.toString())
                        views.setTextViewText(R.id.widget_val_secs, remaining.seconds.toString())
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget $appWidgetId", e)
            }
        }
    }
}
