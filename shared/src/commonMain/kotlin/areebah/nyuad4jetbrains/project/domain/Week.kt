package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** An event together with the user's interests it matches. */
data class RankedEvent(val event: Event, val matches: List<String>)

data class DayEvents(val date: LocalDate, val events: List<RankedEvent>)

/**
 * The events in the [days] days starting at [today], grouped by day in date order.
 * Within a day, events that match the user's interests come first, then by start time.
 * With [onlyMatching] on, non-matching events are hidden, unless the user has no interests set yet.
 */
fun buildWeek(
    events: List<Event>,
    profile: InterestProfile,
    today: LocalDate,
    days: Int = 7,
    onlyMatching: Boolean = false,
): List<DayEvents> {
    val last = today.plus(days - 1, DateTimeUnit.DAY)
    val ordering = compareByDescending<RankedEvent> { it.matches.size }.thenBy { it.event.start }
    return events
        .filter { it.date >= today && it.date <= last }
        .map { RankedEvent(it, matchedInterests(it, profile)) }
        .filter { !onlyMatching || profile.isEmpty || it.matches.isNotEmpty() }
        .groupBy { it.event.date }
        .entries
        .sortedBy { it.key }
        .map { (date, dayEvents) -> DayEvents(date, dayEvents.sortedWith(ordering)) }
}
