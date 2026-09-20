package areebah.nyuad4jetbrains.project.planner

import areebah.nyuad4jetbrains.project.domain.Event
import kotlinx.datetime.LocalDateTime
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
fun PlannerState.decisionOf(eventId: String): Decision = TODO("feature/planner-engine")

/**
 * What [eventId] costs: the price the user typed, else [Event.priceMin], else null.
 *
 * Null means **unknown**, not free. The two are kept apart on purpose: an unknown price is shown
 * as "Price unknown" and counts as 0 against the budget (see [spent]), so an unknown-price event
 * always clears the budget filter — but it must never be presented to the user as free.
 */
fun PlannerState.priceOf(eventId: String): Double? = TODO("feature/planner-engine")

/** When [event] is assumed to finish: its own [Event.end] if it has one, else [assumedDuration] after it starts. */
fun PlannerState.endOf(event: Event): LocalDateTime = TODO("feature/planner-engine")

/** The accepted events, soonest first. */
val PlannerState.accepted: List<Event> get() = TODO("feature/planner-engine")

/** What the accepted events cost. An unknown price counts as 0, so it never blocks the budget. */
val PlannerState.spent: Double get() = TODO("feature/planner-engine")

/** [budget] minus [spent]. */
val PlannerState.remainingBudget: Double get() = TODO("feature/planner-engine")

/**
 * The undecided events worth suggesting. An event qualifies when it:
 * - is not sold out ([Event.soldOut]),
 * - fits entirely inside one free slot, from [Event.start] to [endOf],
 * - costs no more than [remainingBudget], counting an unknown price as 0,
 * - and does not overlap an already accepted event.
 *
 * Soonest first, then cheapest.
 */
val PlannerState.maybes: List<Event> get() = TODO("feature/planner-engine")

/**
 * Accepts [eventId] if it is currently a maybe, otherwise returns this state unchanged.
 *
 * [price] is what the user typed for this event, in AED. It is recorded in [userPrices] and used
 * from then on, which is how an event with no price from its source gets a real one.
 */
fun PlannerState.accept(eventId: String, price: Double? = null): PlannerState =
    TODO("feature/planner-engine")

/** Rejects [eventId] for good. It is never suggested again. */
fun PlannerState.reject(eventId: String): PlannerState = TODO("feature/planner-engine")
