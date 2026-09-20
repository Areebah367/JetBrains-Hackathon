package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** An event, with times in Abu Dhabi local time. Missing details are null, never guessed. */
data class Event(
    val id: String,
    val name: String,
    val start: LocalDateTime,
    /** False when the source gave a date but no time ("time to be announced"). */
    val timeKnown: Boolean = true,
    val venue: String? = null,
    val category: String? = null,
    val genre: String? = null,
    val priceMin: Double? = null,
    val priceMax: Double? = null,
    val currency: String? = null,
    val url: String? = null,
) {
    val date: LocalDate get() = start.date
}

/** Abu Dhabi is UTC+4 all year (no daylight saving), which is the `Asia/Dubai` zone. */
object AbuDhabiTime {
    val zone: TimeZone = TimeZone.of("Asia/Dubai")

    fun today(): LocalDate = Clock.System.todayIn(zone)
}
