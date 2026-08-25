package com.pixelcountdown.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pixelcountdown.data.CountdownRepository

class CountdownAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationHelper.ACTION_COUNTDOWN_ALARM -> {
                val itemId = intent.getStringExtra(NotificationHelper.EXTRA_ITEM_ID) ?: ""
                val title = intent.getStringExtra(NotificationHelper.EXTRA_ITEM_TITLE) ?: "Countdown"
                NotificationHelper.showCompletionNotification(context, itemId, title)

                // Refresh Home Screen Widget
                val repo = CountdownRepository.getInstance(context)
                repo.notifyWidgetUpdate()
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Reschedule all future alarms after device restart
                val repo = CountdownRepository.getInstance(context)
                val countdowns = repo.countdowns.value
                val now = System.currentTimeMillis()
                for (item in countdowns) {
                    if (item.targetEpochMillis > now) {
                        NotificationHelper.scheduleAlarm(context, item)
                    }
                }
                repo.notifyWidgetUpdate()
            }
        }
    }
}
