package com.pixelcountdown.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.pixelcountdown.receiver.NotificationHelper
import com.pixelcountdown.widget.PixelCountdownWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CountdownRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("pixel_countdown_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _countdowns = MutableStateFlow<List<CountdownItem>>(loadCountdowns())
    val countdowns: StateFlow<List<CountdownItem>> = _countdowns.asStateFlow()

    private fun loadCountdowns(): List<CountdownItem> {
        val rawJson = prefs.getString(KEY_COUNTDOWNS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<CountdownItem>>(rawJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persist(list: List<CountdownItem>) {
        val rawJson = json.encodeToString(list)
        prefs.edit().putString(KEY_COUNTDOWNS, rawJson).apply()
        _countdowns.value = list
        notifyWidgetUpdate()
    }

    fun reorderCountdowns(list: List<CountdownItem>) {
        persist(list)
    }

    fun saveCountdown(item: CountdownItem): CountdownItem {
        val current = _countdowns.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        val itemToSave: CountdownItem
        if (index >= 0) {
            itemToSave = item
            current[index] = itemToSave
        } else {
            itemToSave = item
            current.add(itemToSave)
        }
        persist(current)

        // Schedule exact alarm for finish notification
        NotificationHelper.scheduleAlarm(context, itemToSave)
        return itemToSave
    }

    fun deleteCountdown(id: String) {
        val current = _countdowns.value.toMutableList()
        val itemToDelete = current.find { it.id == id }
        if (itemToDelete != null) {
            NotificationHelper.cancelAlarm(context, itemToDelete.id)
            current.remove(itemToDelete)
            persist(current)
        }
    }

    fun bindWidgetToCountdown(widgetId: Int, countdownId: String) {
        prefs.edit().putString(KEY_WIDGET_BIND_PREFIX + widgetId, countdownId).apply()
        notifyWidgetUpdate()
    }

    fun getBoundCountdownId(widgetId: Int): String? {
        return prefs.getString(KEY_WIDGET_BIND_PREFIX + widgetId, null)
    }

    fun getWidgetCountdown(widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): CountdownItem? {
        val list = _countdowns.value
        if (list.isEmpty()) return null

        // Priority 1: explicitly bound to this specific widgetId
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val boundId = getBoundCountdownId(widgetId)
            if (boundId != null) {
                val boundItem = list.find { it.id == boundId }
                if (boundItem != null) return boundItem
            }
        }

        // Priority 2: next upcoming countdown
        val now = System.currentTimeMillis()
        val upcoming = list.firstOrNull { it.targetEpochMillis > now }
        if (upcoming != null) return upcoming

        // Priority 3: first in list
        return list.firstOrNull()
    }

    fun notifyWidgetUpdate() {
        try {
            val intent = Intent(context, PixelCountdownWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                    ComponentName(context, PixelCountdownWidgetProvider::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_COUNTDOWNS = "saved_countdowns"
        private const val KEY_WIDGET_BIND_PREFIX = "widget_bind_"

        @Volatile
        private var INSTANCE: CountdownRepository? = null

        fun getInstance(context: Context): CountdownRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CountdownRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
