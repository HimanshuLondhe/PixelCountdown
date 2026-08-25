package com.pixelcountdown.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
}
