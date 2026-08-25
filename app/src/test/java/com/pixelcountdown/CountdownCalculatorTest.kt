package com.pixelcountdown

import com.pixelcountdown.data.CountdownCalculator
import com.pixelcountdown.data.CountdownItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class CountdownCalculatorTest {

    @Test
    fun testPastDateReturnsFinished() {
        val now = 1000000L
        val target = 500000L
        val remaining = CountdownCalculator.calculateRemaining(target, now)

        assertTrue(remaining.isFinished)
        assertEquals(0L, remaining.years)
        assertEquals(0L, remaining.months)
        assertEquals(0L, remaining.days)
        assertEquals(0L, remaining.hours)
        assertEquals(0L, remaining.minutes)
        assertEquals(0L, remaining.seconds)
    }

    @Test
    fun testFutureDateCalculation() {
        val zone = ZoneId.systemDefault()
        val baseLdt = LocalDateTime.of(2026, 1, 1, 12, 0, 0)
        val nowMillis = baseLdt.atZone(zone).toInstant().toEpochMilli()

        // Target: 2 years, 3 months, 4 days, 5 hours, 6 minutes, 7 seconds later
        val targetLdt = baseLdt
            .plusYears(2)
            .plusMonths(3)
            .plusDays(4)
            .plusHours(5)
            .plusMinutes(6)
            .plusSeconds(7)
        val targetMillis = targetLdt.atZone(zone).toInstant().toEpochMilli()

        val remaining = CountdownCalculator.calculateRemaining(targetMillis, nowMillis)

        assertFalse(remaining.isFinished)
        assertEquals(2L, remaining.years)
        assertEquals(3L, remaining.months)
        assertEquals(4L, remaining.days)
        assertEquals(5L, remaining.hours)
        assertEquals(6L, remaining.minutes)
        assertEquals(7L, remaining.seconds)
    }

    @Test
    fun testItemSerialization() {
        val json = Json { ignoreUnknownKeys = true }
        val item = CountdownItem(
            id = "test-123",
            title = "Vacation in Tokyo",
            targetEpochMillis = 1750000000000L,
            createdAt = 1700000000000L,
            isPinnedToWidget = true
        )

        val encoded = json.encodeToString(item)
        val decoded = json.decodeFromString<CountdownItem>(encoded)

        assertEquals(item.id, decoded.id)
        assertEquals(item.title, decoded.title)
        assertEquals(item.targetEpochMillis, decoded.targetEpochMillis)
        assertEquals(item.isPinnedToWidget, decoded.isPinnedToWidget)
    }
}
