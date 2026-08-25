package com.pixelcountdown.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pixelcountdown.MainActivity
import com.pixelcountdown.R
import com.pixelcountdown.data.CountdownItem

object NotificationHelper {

    const val CHANNEL_ID = "pixel_countdown_channel"
    const val ACTION_COUNTDOWN_ALARM = "com.pixelcountdown.ACTION_COUNTDOWN_ALARM"
    const val EXTRA_ITEM_ID = "extra_item_id"
    const val EXTRA_ITEM_TITLE = "extra_item_title"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAlarm(context: Context, item: CountdownItem) {
        val now = System.currentTimeMillis()
        if (item.targetEpochMillis <= now) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CountdownAlarmReceiver::class.java).apply {
            action = ACTION_COUNTDOWN_ALARM
            putExtra(EXTRA_ITEM_ID, item.id)
            putExtra(EXTRA_ITEM_TITLE, item.title)
        }

        val requestCode = item.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        item.targetEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        item.targetEpochMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    item.targetEpochMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for strict permission environments
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                item.targetEpochMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, itemId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CountdownAlarmReceiver::class.java).apply {
            action = ACTION_COUNTDOWN_ALARM
        }
        val requestCode = itemId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun showCompletionNotification(context: Context, itemId: String, title: String) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_hourglass)
            .setContentTitle("🎉 $title has arrived!")
            .setContentText("Your countdown timer has reached zero.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(itemId.hashCode(), notification)
    }
}
