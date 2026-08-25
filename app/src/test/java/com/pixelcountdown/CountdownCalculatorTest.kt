package com.pixelcountdown

import com.pixelcountdown.data.CountdownCalculator
import com.pixelcountdown.data.CountdownItem
import com.pixelcountdown.data.TimeRemaining
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
    fun testLeapYearCalculation() {
        val zone = ZoneId.systemDefault()
        // Feb 28, 2024 (Leap Year) to March 1, 2024
        val nowLdt = LocalDateTime.of(2024, 2, 28, 0, 0, 0)
        val targetLdt = LocalDateTime.of(2024, 3, 1, 0, 0, 0)
        
        val nowMillis = nowLdt.atZone(zone).toInstant().toEpochMilli()
        val targetMillis = targetLdt.atZone(zone).toInstant().toEpochMilli()

        val remaining = CountdownCalculator.calculateRemaining(targetMillis, nowMillis)
        
        assertEquals(0L, remaining.years)
        assertEquals(0L, remaining.months)
        assertEquals(2L, remaining.days) // Feb 28 -> Feb 29 -> Mar 1
    }

    @Test
    fun testMonthEndTransitions() {
        val zone = ZoneId.systemDefault()
        // Jan 31 to Feb 28
        val nowLdt = LocalDateTime.of(2023, 1, 31, 0, 0, 0)
        val targetLdt = LocalDateTime.of(2023, 2, 28, 0, 0, 0)
        
        val nowMillis = nowLdt.atZone(zone).toInstant().toEpochMilli()
        val targetMillis = targetLdt.atZone(zone).toInstant().toEpochMilli()

        val remaining = CountdownCalculator.calculateRemaining(targetMillis, nowMillis)
        
        assertEquals(0L, remaining.months)
        assertEquals(28L, remaining.days)
    }

    @Test
    fun testFormattedSummary() {
        val completed = TimeRemaining(0, 0, 0, 0, 0, 0, true)
        assertEquals("Completed", completed.formattedSummary())

        val partial = TimeRemaining(1, 2, 3, 4, 5, 6, false)
        assertEquals("1y 2mo 3d 4h 5m 6s", partial.formattedSummary())

        val short = TimeRemaining(0, 0, 0, 0, 0, 30, false)
        assertEquals("0h 0m 30s", short.formattedSummary())
    }

    @Test
    fun testItemFormatting() {
        // Use a fixed epoch millis to avoid timezone issues in comparison if possible, 
        // but ZoneId.systemDefault() makes it hard to be exact across environments.
        // We'll just verify it returns a non-empty string for now.
        val item = CountdownItem(
            title = "Test",
            targetEpochMillis = 1750000000000L
        )
        val formatted = item.formattedTargetDateTime()
        assertTrue(formatted.isNotEmpty())
        assertTrue(formatted.contains("2025")) // 1750000000000 is June 2025
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
