package com.pixelcountdown.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.pixelcountdown.data.CountdownItem
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object CalendarHelper {

    fun getIcsUid(item: CountdownItem): String {
        return item.calendarIcsUid ?: "pixeltimer-${item.id}@pixelcountdown.com"
    }

    private val utcFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)

    fun createIcsFile(context: Context, item: CountdownItem, isCancel: Boolean = false): Uri {
        val uid = getIcsUid(item)
        val nowUtc = utcFormatter.format(Instant.now())
        val startUtc = utcFormatter.format(Instant.ofEpochMilli(item.targetEpochMillis))
        // Event duration set to 1 hour
        val endUtc = utcFormatter.format(Instant.ofEpochMilli(item.targetEpochMillis + 3600000L))

        val method = if (isCancel) "CANCEL" else "REQUEST"
        val status = if (isCancel) "CANCELLED" else "CONFIRMED"

        val icsContent = buildString {
            append("BEGIN:VCALENDAR\r\n")
            append("VERSION:2.0\r\n")
            append("PRODID:-//PixelTimer//Countdown App//EN\r\n")
            append("METHOD:$method\r\n")
            append("BEGIN:VEVENT\r\n")
            append("UID:$uid\r\n")
            append("DTSTAMP:$nowUtc\r\n")
            append("DTSTART:$startUtc\r\n")
            append("DTEND:$endUtc\r\n")
            append("SUMMARY:${escapeIcsText(item.title)}\r\n")
            append("DESCRIPTION:Countdown timer created with PixelTimer\r\n")
            append("STATUS:$status\r\n")
            append("END:VEVENT\r\n")
            append("END:VCALENDAR\r\n")
        }

        val cacheDir = File(context.cacheDir, "calendar")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val fileName = if (isCancel) "cancel_event.ics" else "event.ics"
        val file = File(cacheDir, fileName)
        file.writeText(icsContent)

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun escapeIcsText(text: String): String {
        return text.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }

    fun launchCalendarApp(context: Context, item: CountdownItem, isCancel: Boolean = false) {
        val uri = createIcsFile(context, item, isCancel)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/calendar")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_TITLE, item.title)
        }

        val chooserTitle = if (isCancel) "Remove Calendar Event" else "Select Calendar App"
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
