package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.testEvent
import areebah.nyuad4jetbrains.project.domain.testToday
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EventRepositoryTest {
    private class FakeApi(private val block: () -> List<Event>) : EventsApi {
        override suspend fun fetchEvents(from: LocalDate, days: Int): List<Event> = block()
    }

    private val sample = listOf(testEvent("sample", "Sample"))
    private fun repository(api: EventsApi?) = EventRepository(api, sampleEvents = { sample })

    @Test
    fun withoutAKeyItUsesSampleEventsAndSaysWhy() = runTest {
        val result = repository(api = null).loadWeek(testToday)
        assertIs<EventsResult.Sample>(result)
        assertEquals(sample, result.events)
        assertTrue(result.reason.contains("API key"))
    }

    @Test
    fun realEventsAreReturnedAsLive() = runTest {
        val real = listOf(testEvent("real", "Real"))
        val result = repository(FakeApi { real }).loadWeek(testToday)
        assertIs<EventsResult.Live>(result)
        assertEquals(real, result.events)
    }

    @Test
    fun anEmptyAnswerFallsBackToSamples() = runTest {
        val result = repository(FakeApi { emptyList() }).loadWeek(testToday)
        assertIs<EventsResult.Sample>(result)
        assertTrue(result.reason.contains("no Abu Dhabi events"))
    }

    @Test
    fun aFailureFallsBackToSamplesWithTheError() = runTest {
        val result = repository(FakeApi { error("Ticketmaster answered HTTP 401") }).loadWeek(testToday)
        assertIs<EventsResult.Sample>(result)
        assertTrue(result.reason.contains("HTTP 401"))
    }

    @Test
    fun theBuiltInSampleEventsFillTheWeek() {
        val events = sampleEventsFor(testToday)
        assertTrue(events.size >= 5)
        assertTrue(events.all { it.date >= testToday })
    }
}
