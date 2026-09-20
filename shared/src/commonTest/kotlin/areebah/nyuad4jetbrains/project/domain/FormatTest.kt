package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatTest {
    @Test
    fun priceIsUnknownWhenTheSourceGivesNone() {
        assertEquals("Price unknown", formatPrice(testEvent("1", "E")))
    }

    @Test
    fun zeroPriceIsFree() {
        assertEquals("Free", formatPrice(testEvent("1", "E", priceMin = 0.0, priceMax = 0.0)))
    }

    @Test
    fun aSinglePriceIsShownOnce() {
        assertEquals("AED 90", formatPrice(testEvent("1", "E", priceMin = 90.0, priceMax = 90.0)))
    }

    @Test
    fun aRangeShowsBothEnds() {
        assertEquals("AED 150–450", formatPrice(testEvent("1", "E", priceMin = 150.0, priceMax = 450.0)))
    }

    @Test
    fun timeIsPadded() {
        assertEquals("09:05", formatTime(testEvent("1", "E", hour = 9, minute = 5)))
    }

    @Test
    fun anUnknownTimeSaysTba() {
        assertEquals("Time TBA", formatTime(testEvent("1", "E").copy(timeKnown = false)))
    }

    @Test
    fun daysAreLabelledRelativeToToday() {
        assertEquals("Today", formatDay(testToday, testToday))
        assertEquals("Tomorrow", formatDay(testToday.plus(1, DateTimeUnit.DAY), testToday))
        assertEquals("Wed 23 Sep", formatDay(testToday.plus(3, DateTimeUnit.DAY), testToday))
    }
}
