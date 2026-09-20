package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleTest {
    private fun day(offset: Int) = testToday.plus(offset, DateTimeUnit.DAY)
    private val music = InterestProfile(selectedLabels = setOf("Music"))

    private fun ids(events: List<Event>, profile: InterestProfile = InterestProfile(), filters: ScheduleFilters = ScheduleFilters()) =
        buildSchedule(events, profile, testToday, filters).flatMap { it.events }.map { it.event.id }

    @Test
    fun theWeekHorizonKeepsOnlySevenDays() {
        val events = listOf(
            testEvent("yesterday", "Old", date = day(-1)),
            testEvent("today", "Today", date = day(0)),
            testEvent("day7", "Last day", date = day(6)),
            testEvent("day8", "Too far", date = day(7)),
        )
        assertEquals(listOf("today", "day7"), ids(events))
    }

    @Test
    fun aLongerHorizonReachesFurtherOut() {
        val events = listOf(
            testEvent("soon", "Soon", date = day(2)),
            testEvent("threeWeeks", "Ticketmaster show", date = day(21)),
        )
        assertEquals(listOf("soon"), ids(events))
        assertEquals(
            listOf("soon", "threeWeeks"),
            ids(events, filters = ScheduleFilters(horizon = Horizon.MONTH)),
        )
    }

    @Test
    fun daysAreInDateOrderEvenIfEventsAreNot() {
        val events = listOf(
            testEvent("c", "C", date = day(3)),
            testEvent("a", "A", date = day(0)),
            testEvent("b", "B", date = day(1)),
        )
        assertEquals(listOf(day(0), day(1), day(3)), buildSchedule(events, InterestProfile(), testToday).map { it.date })
    }

    @Test
    fun matchingEventsComeFirstWithinADay() {
        val events = listOf(
            testEvent("early-other", "Quiet talk", hour = 10),
            testEvent("late-music", "Big Concert", hour = 21, category = "Music"),
            testEvent("early-music", "Small Concert", hour = 12, category = "Music"),
        )
        assertEquals(listOf("early-music", "late-music", "early-other"), ids(events, music))
    }

    @Test
    fun onlyMatchingHidesTheRest() {
        val events = listOf(
            testEvent("music", "Concert", category = "Music"),
            testEvent("other", "Quiet talk"),
        )
        assertEquals(listOf("music"), ids(events, music, ScheduleFilters(onlyMatching = true)))
    }

    @Test
    fun onlyMatchingShowsEverythingWhenNoInterestsAreSetYet() {
        val events = listOf(testEvent("a", "A"), testEvent("b", "B"))
        assertEquals(2, ids(events, InterestProfile(), ScheduleFilters(onlyMatching = true)).size)
    }

    @Test
    fun noCityFilterMeansBothCities() {
        val events = listOf(
            testEvent("auh", "A", city = Cities.ABU_DHABI),
            testEvent("dxb", "D", city = Cities.DUBAI),
        )
        assertEquals(listOf("auh", "dxb"), ids(events).sorted())
    }

    @Test
    fun aCityFilterKeepsOnlyThatCity() {
        val events = listOf(
            testEvent("auh", "A", city = Cities.ABU_DHABI),
            testEvent("dxb", "D", city = Cities.DUBAI),
            testEvent("nowhere", "N", city = null),
        )
        assertEquals(listOf("dxb"), ids(events, filters = ScheduleFilters(cities = setOf(Cities.DUBAI))))
    }

    @Test
    fun soldOutEventsCanBeHidden() {
        val events = listOf(
            testEvent("open", "Open", hour = 10),
            testEvent("full", "Full", hour = 11, soldOut = true),
        )
        assertEquals(listOf("open", "full"), ids(events))
        assertEquals(listOf("open"), ids(events, filters = ScheduleFilters(hideSoldOut = true)))
    }

    @Test
    fun anEmptyListGivesAnEmptySchedule() {
        assertTrue(buildSchedule(emptyList(), music, testToday).isEmpty())
    }
}
