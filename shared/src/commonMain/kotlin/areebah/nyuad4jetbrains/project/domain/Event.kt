package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Where an event came from, so the UI can be honest about it. */
enum class EventSource {
    /** Live from the Ticketmaster Discovery API. */
    TICKETMASTER,

    /** Hand-entered community events (currently transcribed from Luma pages by the team). */
    CURATED,

    /** Made-up events, used only when no real source is available. */
    SAMPLE,
}

/** An event, with times in local UAE time. Missing details are null, never guessed. */
data class Event(
    val id: String,
    val name: String,
    val start: LocalDateTime,
    /** False when the source gave a date but no time ("time to be announced"). */
    val timeKnown: Boolean = true,
    /** When the event finishes, if the source says. Ticketmaster does not. */
    val end: LocalDateTime? = null,
    val city: String? = null,
    val venue: String? = null,
    val category: String? = null,
    val genre: String? = null,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val currency: String? = null,
    /** The event is full or sold out, so it should not be suggested as bookable. */
    val soldOut: Boolean = false,
    val url: String? = null,
    val source: EventSource = EventSource.TICKETMASTER,
) {
    val date: LocalDate get() = start.date
}

/** Abu Dhabi and Dubai are both UTC+4 all year (no daylight saving), which is the `Asia/Dubai` zone. */
object AbuDhabiTime {
    val zone: TimeZone = TimeZone.of("Asia/Dubai")

    fun today(): LocalDate = Clock.System.todayIn(zone)
}

/** The cities the app covers. */
object Cities {
    const val ABU_DHABI = "Abu Dhabi"
    const val DUBAI = "Dubai"
    val all = listOf(ABU_DHABI, DUBAI)
}
