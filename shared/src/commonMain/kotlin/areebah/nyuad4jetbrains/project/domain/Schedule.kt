package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** How far ahead to look. Ticketmaster books months out, so a week alone can look empty. */
enum class Horizon(val label: String, val days: Int) {
    WEEK("This week", 7),
    MONTH("This month", 30),
    ALL("Everything", 365),
}

/** An event together with the user's interests it matches. */
data class RankedEvent(val event: Event, val matches: List<String>)

data class DayEvents(val date: LocalDate, val events: List<RankedEvent>)

data class ScheduleFilters(
    val horizon: Horizon = Horizon.WEEK,
    /** Empty means every city. */
    val cities: Set<String> = emptySet(),
    val onlyMatching: Boolean = false,
    val hideSoldOut: Boolean = false,
)

/**
 * The events from [today] within the filters' horizon, grouped by day in date order.
 * Within a day, events that match the user's interests come first, then by start time.
 * With `onlyMatching`, non-matching events are hidden, unless the user has no interests set yet.
 */
fun buildSchedule(
    events: List<Event>,
    profile: InterestProfile,
    today: LocalDate,
    filters: ScheduleFilters = ScheduleFilters(),
): List<DayEvents> {
    val last = today.plus(filters.horizon.days - 1, DateTimeUnit.DAY)
    val ordering = compareByDescending<RankedEvent> { it.matches.size }.thenBy { it.event.start }
    return events
        .filter { it.date >= today && it.date <= last }
        .filter { filters.cities.isEmpty() || it.city in filters.cities }
        .filter { !filters.hideSoldOut || !it.soldOut }
        .map { RankedEvent(it, matchedInterests(it, profile)) }
        .filter { !filters.onlyMatching || profile.isEmpty || it.matches.isNotEmpty() }
        .groupBy { it.event.date }
        .entries
        .sortedBy { it.key }
        .map { (date, dayEvents) -> DayEvents(date, dayEvents.sortedWith(ordering)) }
}
