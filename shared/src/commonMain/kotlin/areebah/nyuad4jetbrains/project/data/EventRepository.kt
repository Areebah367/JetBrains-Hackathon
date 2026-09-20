package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.Secrets
import areebah.nyuad4jetbrains.project.domain.Event
import kotlinx.datetime.LocalDate
import kotlin.coroutines.cancellation.CancellationException

/**
 * What one load produced. [events] always has something in it so the app is never blank, and
 * [notice] explains anything the user should know about where it came from.
 */
data class EventsResult(
    val events: List<Event>,
    val ticketmasterCount: Int,
    val curatedCount: Int,
    val usingSamples: Boolean = false,
    val notice: String? = null,
)

class EventRepository(
    private val api: EventsApi?,
    private val curated: () -> List<Event> = ::curatedEvents,
    private val sampleEvents: (LocalDate) -> List<Event> = ::sampleEventsFor,
) {
    /**
     * Loads curated community events plus whatever Ticketmaster has. Ticketmaster failing is not
     * fatal: the curated list still stands on its own, and the notice says what went wrong.
     */
    suspend fun load(today: LocalDate, days: Int = 365): EventsResult {
        val curatedEvents = curated()
        val (live, notice) = fetchLive(today, days)

        val merged = (curatedEvents + live)
            .distinctBy { it.id }
            .sortedBy { it.start }

        if (merged.isEmpty()) {
            return EventsResult(
                events = sampleEvents(today),
                ticketmasterCount = 0,
                curatedCount = 0,
                usingSamples = true,
                notice = notice ?: "No events available, showing samples",
            )
        }
        return EventsResult(
            events = merged,
            ticketmasterCount = live.size,
            curatedCount = curatedEvents.size,
            notice = notice,
        )
    }

    private suspend fun fetchLive(today: LocalDate, days: Int): Pair<List<Event>, String?> {
        if (api == null) return emptyList<Event>() to "No Ticketmaster API key set, showing community events only"
        return try {
            val events = api.fetchEvents(today, days)
            if (events.isEmpty()) {
                emptyList<Event>() to "Ticketmaster has no UAE events in this period"
            } else {
                events to null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList<Event>() to "Could not reach Ticketmaster (${e.message ?: e::class.simpleName})"
        }
    }
}

/** Builds the repository the app uses, reading the key that the build generated from local.properties. */
fun defaultEventRepository(): EventRepository {
    val key = Secrets.TICKETMASTER_API_KEY
    val api = if (key.isBlank()) null else TicketmasterApi(createHttpClient(), key)
    return EventRepository(api)
}
