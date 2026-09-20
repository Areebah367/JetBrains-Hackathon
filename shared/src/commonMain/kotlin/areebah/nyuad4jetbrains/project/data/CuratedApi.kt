package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Event
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** What `GET /events` returns. Declared here so the server and the app share one definition. */
@Serializable
data class EventsResponse(val events: List<Event>)

/**
 * Reads curated events from the project's own Ktor server (`./gradlew :server:run`) instead of the
 * copy compiled into the app, so the list can be updated without shipping a new build.
 *
 * The server being absent is not an error: [EventRepository] falls back to the bundled list, which
 * is why the app still works on a phone with no laptop running beside it.
 */
class CuratedApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun fetchEvents(from: LocalDate): List<Event> {
        val response = client.get("$baseUrl/events") { parameter("from", from.toString()) }
        if (!response.status.isSuccess()) error("Curated API answered HTTP ${response.status.value}")
        return response.body<EventsResponse>().events
    }
}
