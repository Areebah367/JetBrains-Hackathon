package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.Secrets
import areebah.nyuad4jetbrains.project.domain.Event
import kotlinx.datetime.LocalDate
import kotlin.coroutines.cancellation.CancellationException

sealed interface EventsResult {
    val events: List<Event>

    /** Real events from Ticketmaster. */
    data class Live(override val events: List<Event>) : EventsResult

    /** Made-up events shown when real ones are unavailable, with the reason why. */
    data class Sample(override val events: List<Event>, val reason: String) : EventsResult
}

class EventRepository(
    private val api: EventsApi?,
    private val sampleEvents: (LocalDate) -> List<Event> = ::sampleEventsFor,
) {
    /** Loads the coming week. Falls back to sample events, and says why, so the app always has something to show. */
    suspend fun loadWeek(today: LocalDate, days: Int = 7): EventsResult {
        if (api == null) return sample(today, "No Ticketmaster API key is set")
        return try {
            val events = api.fetchEvents(today, days)
            if (events.isEmpty()) {
                sample(today, "Ticketmaster returned no Abu Dhabi events for this week")
            } else {
                EventsResult.Live(events)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            sample(today, "Could not load from Ticketmaster (${e.message ?: e::class.simpleName})")
        }
    }

    private fun sample(today: LocalDate, reason: String) = EventsResult.Sample(sampleEvents(today), reason)
}

/** Builds the repository the app uses, reading the key that the build generated from local.properties. */
fun defaultEventRepository(): EventRepository {
    val key = Secrets.TICKETMASTER_API_KEY
    val api = if (key.isBlank()) null else TicketmasterApi(createHttpClient(), key)
    return EventRepository(api)
}
