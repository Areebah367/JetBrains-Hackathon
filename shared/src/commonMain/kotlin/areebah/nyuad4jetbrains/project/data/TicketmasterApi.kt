package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.AbuDhabiTime
import areebah.nyuad4jetbrains.project.domain.Event
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

/** A source of upcoming events. Kept as an interface so tests and the repository can fake it. */
interface EventsApi {
    /** Events starting in the [days] days from [from], in UAE time. */
    suspend fun fetchEvents(from: LocalDate, days: Int): List<Event>
}

/**
 * Ticketmaster Discovery API (the regular one, not the International one, which no longer issues keys).
 *
 * Queries the whole of the UAE rather than one city, which covers both Abu Dhabi and Dubai. Measured
 * on 2026-09-20: 76 events, 33 in Abu Dhabi and 43 in Dubai, the earliest three weeks out, and none
 * carrying price information. Treat it as the source for big ticketed shows, not for this weekend.
 */
class TicketmasterApi(
    private val client: HttpClient,
    private val apiKey: String,
    private val countryCode: String = "AE",
) : EventsApi {

    override suspend fun fetchEvents(from: LocalDate, days: Int): List<Event> {
        val zone = AbuDhabiTime.zone
        val start = from.atStartOfDayIn(zone)
        val end = from.plus(days, DateTimeUnit.DAY).atStartOfDayIn(zone)

        val response = client.get(EVENTS_URL) {
            parameter("apikey", apiKey)
            parameter("countryCode", countryCode)
            parameter("startDateTime", start.toString())
            parameter("endDateTime", end.toString())
            parameter("sort", "date,asc")
            parameter("size", 100)
        }
        if (!response.status.isSuccess()) {
            error("Ticketmaster answered HTTP ${response.status.value}")
        }
        return response.body<TmResponse>().embedded?.events.orEmpty().mapNotNull { it.toEvent() }
    }

    private companion object {
        const val EVENTS_URL = "https://app.ticketmaster.com/discovery/v2/events.json"
    }
}

fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) { json(ticketmasterJson) }
    install(HttpTimeout) { requestTimeoutMillis = 15_000 }
}
