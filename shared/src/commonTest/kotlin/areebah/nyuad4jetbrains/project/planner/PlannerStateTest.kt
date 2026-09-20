package areebah.nyuad4jetbrains.project.planner

import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.testEvent
import areebah.nyuad4jetbrains.project.domain.testToday
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private fun at(hour: Int, minute: Int = 0) = LocalDateTime(testToday, LocalTime(hour, minute))

/** The whole evening is free unless a test says otherwise. */
private val wholeEvening = listOf(FreeSlot(at(9), at(23)))

private fun stateOf(
    vararg events: Event,
    budget: Double = 1000.0,
    freeSlots: List<FreeSlot> = wholeEvening,
) = PlannerState(budget = budget, events = events.toList(), freeSlots = freeSlots)

class PlannerStateTest {

    // --- decisions -------------------------------------------------------------------------

    @Test
    fun anUndecidedEventIsAMaybe() {
        val state = stateOf(testEvent("a", "Gig", hour = 20))

        assertEquals(Decision.MAYBE, state.decisionOf("a"))
        assertEquals(listOf("a"), state.maybes.map { it.id })
    }

    @Test
    fun aRejectedEventNeverComesBack() {
        val state = stateOf(testEvent("a", "Gig", hour = 20)).reject("a")

        assertEquals(Decision.NO, state.decisionOf("a"))
        assertTrue(state.maybes.isEmpty())
        // Still gone after another event is accepted.
        assertTrue(state.accept("a").maybes.isEmpty())
        assertTrue(state.accept("a").accepted.isEmpty())
    }

    // --- free slots ------------------------------------------------------------------------

    @Test
    fun anEventOutsideEveryFreeSlotIsNeverAMaybe() {
        val state = stateOf(
            testEvent("late", "Late show", hour = 22),
            freeSlots = listOf(FreeSlot(at(9), at(12))),
        )

        assertTrue(state.maybes.isEmpty())
    }

    @Test
    fun anEventRunningPastTheEndOfItsSlotIsNeverAMaybe() {
        // Starts inside the slot, but the assumed two hours run past 21:00.
        val state = stateOf(
            testEvent("a", "Gig", hour = 20),
            freeSlots = listOf(FreeSlot(at(19), at(21))),
        )

        assertTrue(state.maybes.isEmpty())
    }

    @Test
    fun anEventExactlyFillingItsSlotIsAMaybe() {
        val state = stateOf(
            testEvent("a", "Gig", hour = 19),
            freeSlots = listOf(FreeSlot(at(19), at(21))),
        )

        assertEquals(listOf("a"), state.maybes.map { it.id })
    }

    // --- end time vs assumed duration ------------------------------------------------------

    @Test
    fun anEventWithAnEndUsesItInsteadOfTheAssumedDuration() {
        // A 30-minute event that would not fit if two hours were assumed.
        val short = testEvent("short", "Talk", hour = 20).copy(end = at(20, 30))
        val state = stateOf(short, freeSlots = listOf(FreeSlot(at(19), at(21))))

        assertEquals(at(20, 30), state.endOf(short))
        assertEquals(listOf("short"), state.maybes.map { it.id })
    }

    @Test
    fun anEventWithoutAnEndFallsBackToTheAssumedDuration() {
        val event = testEvent("a", "Gig", hour = 20)
        val state = stateOf(event)

        assertEquals(at(22), state.endOf(event))
    }

    @Test
    fun theAssumedDurationIsConfigurable() {
        val event = testEvent("a", "Gig", hour = 20)
        val state = stateOf(event).copy(assumedDuration = 3.hours)

        assertEquals(at(23), state.endOf(event))
    }

    // --- prices ----------------------------------------------------------------------------

    @Test
    fun anEventAboveTheRemainingBudgetIsNeverAMaybe() {
        val state = stateOf(testEvent("pricey", "Gala", priceMin = 5000.0), budget = 100.0)

        assertTrue(state.maybes.isEmpty())
    }

    @Test
    fun anEventCostingExactlyTheBudgetIsAMaybe() {
        val state = stateOf(testEvent("a", "Gig", priceMin = 100.0), budget = 100.0)

        assertEquals(listOf("a"), state.maybes.map { it.id })
    }

    @Test
    fun anUnknownPriceIsNullRatherThanZero() {
        val state = stateOf(testEvent("a", "Gig"))

        assertNull(state.priceOf("a"))
    }

    @Test
    fun anUnknownPriceCountsAsZeroAgainstTheBudget() {
        val state = stateOf(testEvent("a", "Gig"), budget = 50.0).accept("a")

        assertEquals(listOf("a"), state.accepted.map { it.id })
        assertEquals(0.0, state.spent)
        assertEquals(50.0, state.remainingBudget)
    }

    @Test
    fun anUnknownPriceAlwaysClearsTheBudgetFilter() {
        val state = stateOf(testEvent("a", "Gig"), budget = 0.0)

        assertEquals(listOf("a"), state.maybes.map { it.id })
    }

    @Test
    fun acceptReducesTheRemainingBudgetByThePrice() {
        val state = stateOf(testEvent("a", "Gig", priceMin = 250.0), budget = 1000.0).accept("a")

        assertEquals(250.0, state.spent)
        assertEquals(750.0, state.remainingBudget)
    }

    @Test
    fun aTypedPriceWinsOverTheSourcePrice() {
        val state = stateOf(testEvent("a", "Gig", priceMin = 100.0), budget = 1000.0)
            .accept("a", price = 320.0)

        assertEquals(320.0, state.priceOf("a"))
        assertEquals(680.0, state.remainingBudget)
    }

    @Test
    fun aTypedPriceGivesAnUnknownPriceEventARealCost() {
        val state = stateOf(testEvent("a", "Gig"), budget = 500.0).accept("a", price = 120.0)

        assertEquals(120.0, state.priceOf("a"))
        assertEquals(380.0, state.remainingBudget)
    }

    @Test
    fun aTypedPriceAboveTheBudgetIsStillAcceptedAndGoesNegative() {
        val state = stateOf(testEvent("a", "Gig"), budget = 100.0).accept("a", price = 400.0)

        assertEquals(Decision.YES, state.decisionOf("a"))
        assertEquals(-300.0, state.remainingBudget)
    }

    // --- sold out --------------------------------------------------------------------------

    @Test
    fun aSoldOutEventIsNeverAMaybe() {
        val state = stateOf(testEvent("a", "Gig", hour = 20).copy(soldOut = true))

        assertTrue(state.maybes.isEmpty())
    }

    // --- accepting -------------------------------------------------------------------------

    @Test
    fun acceptingSomethingThatIsNotAMaybeChangesNothing() {
        val unaffordable = stateOf(testEvent("a", "Gala", priceMin = 5000.0), budget = 10.0)

        assertEquals(unaffordable, unaffordable.accept("a"))
    }

    @Test
    fun acceptingAnUnknownIdChangesNothing() {
        val state = stateOf(testEvent("a", "Gig"))

        assertEquals(state, state.accept("nope"))
    }

    @Test
    fun anAcceptedEventLeavesTheMaybeList() {
        val state = stateOf(
            testEvent("a", "Gig", hour = 12),
            testEvent("b", "Show", hour = 20),
        ).accept("a")

        assertEquals(listOf("a"), state.accepted.map { it.id })
        assertEquals(listOf("b"), state.maybes.map { it.id })
    }

    @Test
    fun afterAYesOverlappingMaybesDisappear() {
        val state = stateOf(
            testEvent("a", "Gig", hour = 20),      // 20:00-22:00
            testEvent("b", "Clash", hour = 21),    // 21:00-23:00, overlaps
            testEvent("c", "Later", hour = 12),    // 12:00-14:00, clear
        ).accept("a")

        assertEquals(listOf("c"), state.maybes.map { it.id })
    }

    @Test
    fun anEventStartingExactlyWhenAnAcceptedOneEndsSurvives() {
        val state = stateOf(
            testEvent("a", "Gig", hour = 18),       // 18:00-20:00
            testEvent("b", "Next", hour = 20),      // 20:00-22:00, touches but does not overlap
        ).accept("a")

        assertEquals(listOf("b"), state.maybes.map { it.id })
    }

    @Test
    fun afterAYesTheNoLongerAffordableMaybesDisappear() {
        val state = stateOf(
            testEvent("a", "Gig", hour = 12, priceMin = 800.0),
            testEvent("b", "Show", hour = 20, priceMin = 500.0),
            testEvent("c", "Cheap", hour = 16, priceMin = 50.0),
            budget = 1000.0,
        ).accept("a")

        // 200 left: the 500 show is out, the 50 one stays.
        assertEquals(listOf("c"), state.maybes.map { it.id })
    }

    @Test
    fun twoOverlappingEventsCanNeverBothBeAccepted() {
        val state = stateOf(
            testEvent("a", "Gig", hour = 20),
            testEvent("b", "Clash", hour = 21),
        ).accept("a").accept("b")

        assertEquals(listOf("a"), state.accepted.map { it.id })
        assertEquals(Decision.MAYBE, state.decisionOf("b"))
    }

    // --- ordering --------------------------------------------------------------------------

    @Test
    fun maybesAreSoonestFirst() {
        val state = stateOf(
            testEvent("late", "Late", hour = 20),
            testEvent("early", "Early", hour = 10),
            testEvent("mid", "Mid", hour = 15),
        )

        assertEquals(listOf("early", "mid", "late"), state.maybes.map { it.id })
    }

    @Test
    fun maybesAtTheSameTimeAreCheapestFirst() {
        val state = stateOf(
            testEvent("dear", "Dear", hour = 20, priceMin = 300.0),
            testEvent("cheap", "Cheap", hour = 20, priceMin = 40.0),
            testEvent("unknown", "Unknown", hour = 20),
        )

        // An unknown price counts as 0, so it sorts first.
        assertEquals(listOf("unknown", "cheap", "dear"), state.maybes.map { it.id })
    }

    @Test
    fun acceptedEventsAreSoonestFirst() {
        val state = stateOf(
            testEvent("late", "Late", hour = 20),
            testEvent("early", "Early", hour = 10),
        ).accept("late").accept("early")

        assertEquals(listOf("early", "late"), state.accepted.map { it.id })
    }

    // --- immutability ----------------------------------------------------------------------

    @Test
    fun acceptAndRejectDoNotMutateTheOriginal() {
        val original = stateOf(testEvent("a", "Gig", hour = 20), testEvent("b", "Show", hour = 12))

        original.accept("a")
        original.reject("b")

        assertEquals(emptyMap(), original.decisions)
        assertEquals(2, original.maybes.size)
    }
}
