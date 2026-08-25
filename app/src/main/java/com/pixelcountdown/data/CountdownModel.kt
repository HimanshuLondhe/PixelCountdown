package com.pixelcountdown.data

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@Serializable
data class CountdownItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetEpochMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun formattedTargetDateTime(): String {
        val zone = ZoneId.systemDefault()
        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(targetEpochMillis), zone)
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a")
        return ldt.format(formatter)
    }
}

data class TimeRemaining(
    val years: Long,
    val months: Long,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isFinished: Boolean
) {
    fun formattedSummary(): String {
        if (isFinished) return "Completed"
        val parts = mutableListOf<String>()
        if (years > 0) parts.add("${years}y")
        if (months > 0) parts.add("${months}mo")
        if (days > 0) parts.add("${days}d")
        parts.add("${hours}h")
        parts.add("${minutes}m")
        parts.add("${seconds}s")
        return parts.joinToString(" ")
    }
}

object CountdownCalculator {
    fun calculateRemaining(targetEpochMillis: Long, nowEpochMillis: Long = System.currentTimeMillis()): TimeRemaining {
        if (targetEpochMillis <= nowEpochMillis) {
            return TimeRemaining(
                years = 0,
                months = 0,
                days = 0,
                hours = 0,
                minutes = 0,
                seconds = 0,
                isFinished = true
            )
        }

        val zone = ZoneId.systemDefault()
        val nowLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowEpochMillis), zone)
        val targetLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(targetEpochMillis), zone)

        // Calculate Years
        var years = ChronoUnit.YEARS.between(nowLdt, targetLdt)
        var tempLdt = nowLdt.plusYears(years)
        if (tempLdt.isAfter(targetLdt)) {
            years--
            tempLdt = nowLdt.plusYears(years)
        }

        // Calculate Months
        var months = ChronoUnit.MONTHS.between(tempLdt, targetLdt)
        var tempLdt2 = tempLdt.plusMonths(months)
        if (tempLdt2.isAfter(targetLdt)) {
            months--
            tempLdt2 = tempLdt.plusMonths(months)
        }

        // Calculate Days
        var days = ChronoUnit.DAYS.between(tempLdt2, targetLdt)
        var tempLdt3 = tempLdt2.plusDays(days)
        if (tempLdt3.isAfter(targetLdt)) {
            days--
            tempLdt3 = tempLdt2.plusDays(days)
        }

        // Calculate Hours, Minutes, Seconds using Duration on remaining time
        val duration = Duration.between(tempLdt3, targetLdt)
        val totalSecs = maxOf(0L, duration.seconds)
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60

        return TimeRemaining(
            years = maxOf(0L, years),
            months = maxOf(0L, months),
            days = maxOf(0L, days),
            hours = maxOf(0L, hours),
            minutes = maxOf(0L, minutes),
            seconds = maxOf(0L, seconds),
            isFinished = false
        )
    }
}
