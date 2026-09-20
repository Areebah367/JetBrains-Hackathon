package areebah.nyuad4jetbrains.project.planner

import areebah.nyuad4jetbrains.project.domain.AbuDhabiTime
import areebah.nyuad4jetbrains.project.domain.Event
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/** Where an event stands. Absent from [PlannerState.decisions] means [MAYBE]. */
enum class Decision { MAYBE, YES, NO }

/**
 * Everything the planner needs to decide what to suggest. Immutable: [accept] and [reject]
 * return a new state, and the derived lists are recomputed from it.
 *
 * @param budget total spend for the period, in AED.
 * @param decisions keyed by [Event.id]; an absent id is undecided.
 * @param userPrices prices the user typed, keyed by [Event.id]. These win over [Event.priceMin],
 *   because Ticketmaster supplies no prices at all and curated events are the only ones that
 *   carry a real figure.
 * @param assumedDuration how long an event is assumed to run, used **only** when [Event.end] is
 *   null. Curated events have an end; Ticketmaster events never do.
 */
data class PlannerState(
    val budget: Double,
    val events: List<Event>,
    val freeSlots: List<FreeSlot>,
    val decisions: Map<String, Decision> = emptyMap(),
    val userPrices: Map<String, Double> = emptyMap(),
    val assumedDuration: Duration = 2.hours,
)

/** The decision for [eventId], defaulting to [Decision.MAYBE] when it has not been decided. */
fun PlannerState.decisionOf(eventId: String): Decision = decisions[eventId] ?: Decision.MAYBE

/**
 * What [eventId] costs: the price the user typed, else [Event.priceMin], else null.
 *
 * Null means **unknown**, not free. The two are kept apart on purpose: an unknown price is shown
 * as "Price unknown" and counts as 0 against the budget (see [spent]), so such an event always
 * clears the budget filter — but it must never be presented to the user as free.
 */
fun PlannerState.priceOf(eventId: String): Double? =
    userPrices[eventId] ?: events.firstOrNull { it.id == eventId }?.priceMin

/** When [event] is assumed to finish: its own [Event.end] if it has one, else [assumedDuration] after it starts. */
fun PlannerState.endOf(event: Event): LocalDateTime = event.end ?: event.start.plus(assumedDuration)

/** The accepted events, soonest first. */
val PlannerState.accepted: List<Event>
    get() = events.filter { decisionOf(it.id) == Decision.YES }.sortedBy { it.start }

/** What the accepted events cost. An unknown price counts as 0, so it never blocks the budget. */
val PlannerState.spent: Double
    get() = accepted.sumOf { priceOf(it.id) ?: 0.0 }

/** [budget] minus [spent]. Can go negative if the user types a price above what is left. */
val PlannerState.remainingBudget: Double
    get() = budget - spent

/**
 * The undecided events worth suggesting. An event qualifies when it:
 * - is not sold out ([Event.soldOut]),
 * - fits entirely inside one free slot, from [Event.start] to [endOf],
 * - costs no more than [remainingBudget], counting an unknown price as 0,
 * - and does not overlap an already accepted event.
 *
 * Soonest first, then cheapest.
 */
val PlannerState.maybes: List<Event>
    get() {
        val takenSpans = accepted.map { it.start to endOf(it) }
        val left = remainingBudget

        return events
            .filter { decisionOf(it.id) == Decision.MAYBE }
            .filter { !it.soldOut }
            .filter { fitsInAFreeSlot(it) }
            .filter { (priceOf(it.id) ?: 0.0) <= left }
            .filter { event ->
                val span = event.start to endOf(event)
                takenSpans.none { overlaps(span, it) }
            }
            .sortedWith(compareBy({ it.start }, { priceOf(it.id) ?: 0.0 }))
    }

/**
 * Accepts [eventId] if it is currently a maybe, otherwise returns this state unchanged.
 *
 * [price] is what the user typed for this event, in AED. It is recorded in [userPrices] and used
 * from then on, which is how an event with no price from its source gets a real one. A typed
 * price above what is left is still accepted — the user chose it — which drives
 * [remainingBudget] negative and clears out the remaining maybes.
 */
fun PlannerState.accept(eventId: String, price: Double? = null): PlannerState {
    if (maybes.none { it.id == eventId }) return this

    return copy(
        decisions = decisions + (eventId to Decision.YES),
        userPrices = if (price == null) userPrices else userPrices + (eventId to price),
    )
}

/** Rejects [eventId] for good. It is never suggested again. */
fun PlannerState.reject(eventId: String): PlannerState =
    copy(decisions = decisions + (eventId to Decision.NO))

private fun PlannerState.fitsInAFreeSlot(event: Event): Boolean {
    val end = endOf(event)
    return freeSlots.any { event.start >= it.start && end <= it.end }
}

/** Half-open comparison, so an event starting exactly when another ends does not overlap it. */
private fun overlaps(
    a: Pair<LocalDateTime, LocalDateTime>,
    b: Pair<LocalDateTime, LocalDateTime>,
): Boolean = a.first < b.second && b.first < a.second

/** Abu Dhabi has no daylight saving, so adding to wall-clock time is unambiguous. */
private fun LocalDateTime.plus(duration: Duration): LocalDateTime =
    toInstant(AbuDhabiTime.zone).plus(duration).toLocalDateTime(AbuDhabiTime.zone)
