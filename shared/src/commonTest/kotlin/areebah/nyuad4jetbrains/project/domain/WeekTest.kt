package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeekTest {
    private fun day(offset: Int) = testToday.plus(offset, DateTimeUnit.DAY)
    private val music = InterestProfile(selectedLabels = setOf("Music"))

    @Test
    fun onlyTheNextSevenDaysAreIncluded() {
        val events = listOf(
            testEvent("yesterday", "Old", date = day(-1)),
            testEvent("today", "Today", date = day(0)),
            testEvent("day7", "Last day", date = day(6)),
            testEvent("day8", "Too far", date = day(7)),
        )
        val ids = buildWeek(events, InterestProfile(), testToday).flatMap { it.events }.map { it.event.id }
        assertEquals(listOf("today", "day7"), ids)
    }

    @Test
    fun daysAreInDateOrderEvenIfEventsAreNot() {
        val events = listOf(
            testEvent("c", "C", date = day(3)),
            testEvent("a", "A", date = day(0)),
            testEvent("b", "B", date = day(1)),
        )
        assertEquals(listOf(day(0), day(1), day(3)), buildWeek(events, InterestProfile(), testToday).map { it.date })
    }

    @Test
    fun matchingEventsComeFirstWithinADay() {
        val events = listOf(
            testEvent("early-other", "Quiet talk", hour = 10),
            testEvent("late-music", "Big Concert", hour = 21, category = "Music"),
            testEvent("early-music", "Small Concert", hour = 12, category = "Music"),
        )
        val ids = buildWeek(events, music, testToday).single().events.map { it.event.id }
        assertEquals(listOf("early-music", "late-music", "early-other"), ids)
    }

    @Test
    fun onlyMatchingHidesTheRest() {
        val events = listOf(
            testEvent("music", "Concert", category = "Music"),
            testEvent("other", "Quiet talk"),
        )
        val ids = buildWeek(events, music, testToday, onlyMatching = true).flatMap { it.events }.map { it.event.id }
        assertEquals(listOf("music"), ids)
    }

    @Test
    fun onlyMatchingShowsEverythingWhenNoInterestsAreSetYet() {
        val events = listOf(testEvent("a", "A"), testEvent("b", "B"))
        val shown = buildWeek(events, InterestProfile(), testToday, onlyMatching = true).flatMap { it.events }
        assertEquals(2, shown.size)
    }

    @Test
    fun aWeekWithoutEventsIsEmpty() {
        assertTrue(buildWeek(emptyList(), music, testToday).isEmpty())
    }
}
