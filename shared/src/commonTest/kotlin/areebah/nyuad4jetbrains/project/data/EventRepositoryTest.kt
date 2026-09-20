package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Cities
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.EventSource
import areebah.nyuad4jetbrains.project.domain.testEvent
import areebah.nyuad4jetbrains.project.domain.testToday
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EventRepositoryTest {
    private class FakeApi(private val block: () -> List<Event>) : EventsApi {
        override suspend fun fetchEvents(from: LocalDate, days: Int): List<Event> = block()
    }

    private val curated = listOf(testEvent("curated-1", "Community night", hour = 18))
    private val samples = listOf(testEvent("sample-1", "Sample"))

    private fun repository(api: EventsApi?, curatedEvents: List<Event> = curated) =
        EventRepository(api, curated = { curatedEvents }, sampleEvents = { samples })

    @Test
    fun curatedAndLiveEventsAreMergedInStartOrder() = runTest {
        val live = listOf(testEvent("tm-1", "Big show", hour = 21))
        val result = repository(FakeApi { live }).load(testToday)
        assertEquals(listOf("curated-1", "tm-1"), result.events.map { it.id })
        assertEquals(1, result.curatedCount)
        assertEquals(1, result.ticketmasterCount)
        assertNull(result.notice)
        assertTrue(!result.usingSamples)
    }

    @Test
    fun withoutAKeyTheCuratedEventsStillShowWithANotice() = runTest {
        val result = repository(api = null).load(testToday)
        assertEquals(listOf("curated-1"), result.events.map { it.id })
        assertEquals(0, result.ticketmasterCount)
        assertTrue(result.notice!!.contains("API key"))
        assertTrue(!result.usingSamples)
    }

    @Test
    fun aTicketmasterFailureDoesNotLoseTheCuratedEvents() = runTest {
        val result = repository(FakeApi { error("Ticketmaster answered HTTP 401") }).load(testToday)
        assertEquals(listOf("curated-1"), result.events.map { it.id })
        assertTrue(result.notice!!.contains("HTTP 401"))
    }

    @Test
    fun anEmptyTicketmasterAnswerIsExplained() = runTest {
        val result = repository(FakeApi { emptyList() }).load(testToday)
        assertTrue(result.notice!!.contains("no UAE events"))
        assertEquals(listOf("curated-1"), result.events.map { it.id })
    }

    @Test
    fun samplesAppearOnlyWhenThereIsNothingElse() = runTest {
        val result = repository(api = null, curatedEvents = emptyList()).load(testToday)
        assertTrue(result.usingSamples)
        assertEquals(listOf("sample-1"), result.events.map { it.id })
    }

    @Test
    fun duplicateIdsAreKeptOnce() = runTest {
        val sameId = listOf(testEvent("curated-1", "Duplicate from Ticketmaster"))
        val result = repository(FakeApi { sameId }).load(testToday)
        assertEquals(1, result.events.size)
        assertEquals("Community night", result.events.single().name)
    }

    @Test
    fun theCuratedListCoversBothSourcesOfTruthWeRelyOn() {
        val events = curatedEvents()
        assertTrue(events.isNotEmpty())
        assertTrue(events.all { it.source == EventSource.CURATED })
        assertTrue(events.all { it.city in Cities.all })
        assertTrue(events.any { it.priceMin != null }, "at least one curated event should carry a real price")
        assertTrue(events.all { it.end != null }, "curated events come from pages that show an end time")
    }

    @Test
    fun theBuiltInSampleEventsFillTheWeek() {
        val events = sampleEventsFor(testToday)
        assertTrue(events.size >= 5)
        assertTrue(events.all { it.date >= testToday })
        assertTrue(events.all { it.source == EventSource.SAMPLE })
    }
}
