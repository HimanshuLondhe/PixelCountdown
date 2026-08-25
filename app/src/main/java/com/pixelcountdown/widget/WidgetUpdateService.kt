package com.pixelcountdown.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.pixelcountdown.R
import com.pixelcountdown.data.CountdownRepository

class WidgetUpdateService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var repo: CountdownRepository
    private var isScreenOn = true

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isScreenOn) {
                updateAllWidgets()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, PixelCountdownWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        if (appWidgetIds.isEmpty()) {
            stopSelf()
            return
        }

        for (appWidgetId in appWidgetIds) {
            PixelCountdownWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId, repo)
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                    handler.removeCallbacks(updateRunnable)
                    handler.post(updateRunnable)
                }
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    handler.removeCallbacks(updateRunnable)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repo = CountdownRepository.getInstance(this)
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
        
        createNotificationChannel()
        startAsForeground()
    }

    private fun startAsForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PixelTimer Active")
            .setContentText("Keeping your home screen widgets updated.")
            .setSmallIcon(R.drawable.ic_hourglass)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Widget Updates",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Required for live widget countdowns"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(screenReceiver)
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "widget_update_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
