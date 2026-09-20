package areebah.nyuad4jetbrains.project.planner

import areebah.nyuad4jetbrains.project.calendar.BusyBlock
import areebah.nyuad4jetbrains.project.domain.testToday
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private fun at(hour: Int, minute: Int = 0, date: LocalDate = testToday) =
    LocalDateTime(date, LocalTime(hour, minute))

private fun busy(fromHour: Int, toHour: Int, date: LocalDate = testToday) =
    BusyBlock(at(fromHour, date = date), at(toHour, date = date))

private fun slot(fromHour: Int, toHour: Int, date: LocalDate = testToday) =
    FreeSlot(at(fromHour, date = date), at(toHour, date = date))

class FreeSlotTest {

    @Test
    fun emptyCalendarGivesOneSlotPerDay() {
        val slots = computeFreeSlots(busy = emptyList(), from = testToday, days = 3)

        assertEquals(3, slots.size)
        assertEquals(
            listOf(
                slot(9, 22),
                slot(9, 22, testToday.plus(1, DateTimeUnit.DAY)),
                slot(9, 22, testToday.plus(2, DateTimeUnit.DAY)),
            ),
            slots,
        )
    }

    @Test
    fun oneBusyBlockSplitsTheDay() {
        val slots = computeFreeSlots(listOf(busy(12, 14)), testToday, days = 1)

        assertEquals(listOf(slot(9, 12), slot(14, 22)), slots)
    }

    @Test
    fun overlappingBusyBlocksAreMerged() {
        val slots = computeFreeSlots(listOf(busy(12, 15), busy(14, 17)), testToday, days = 1)

        assertEquals(listOf(slot(9, 12), slot(17, 22)), slots)
    }

    @Test
    fun aBusyBlockFullyInsideAnotherIsAbsorbed() {
        val slots = computeFreeSlots(listOf(busy(11, 18), busy(13, 14)), testToday, days = 1)

        assertEquals(listOf(slot(9, 11), slot(18, 22)), slots)
    }

    @Test
    fun backToBackBusyBlocksLeaveNoGapBetweenThem() {
        val slots = computeFreeSlots(listOf(busy(12, 14), busy(14, 16)), testToday, days = 1)

        assertEquals(listOf(slot(9, 12), slot(16, 22)), slots)
    }

    @Test
    fun unsortedBusyBlocksAreHandled() {
        val slots = computeFreeSlots(listOf(busy(18, 20), busy(11, 12)), testToday, days = 1)

        assertEquals(listOf(slot(9, 11), slot(12, 18), slot(20, 22)), slots)
    }

    @Test
    fun busyTimeBeforeTheDayWindowIsIgnored() {
        val slots = computeFreeSlots(listOf(busy(2, 7)), testToday, days = 1)

        assertEquals(listOf(slot(9, 22)), slots)
    }

    @Test
    fun busyTimeAfterTheDayWindowIsIgnored() {
        val slots = computeFreeSlots(listOf(BusyBlock(at(23), at(23, 59))), testToday, days = 1)

        assertEquals(listOf(slot(9, 22)), slots)
    }

    @Test
    fun busyBlockStraddlingTheWindowStartIsClipped() {
        val slots = computeFreeSlots(listOf(busy(7, 11)), testToday, days = 1)

        assertEquals(listOf(slot(11, 22)), slots)
    }

    @Test
    fun busyBlockStraddlingTheWindowEndIsClipped() {
        val slots = computeFreeSlots(listOf(busy(20, 23)), testToday, days = 1)

        assertEquals(listOf(slot(9, 20)), slots)
    }

    @Test
    fun aGapShorterThanMinLengthIsDropped() {
        // 12:00-12:30 is only 30 minutes, under the 60-minute default.
        val slots = computeFreeSlots(
            listOf(busy(9, 12), BusyBlock(at(12, 30), at(15))),
            testToday,
            days = 1,
        )

        assertEquals(listOf(slot(15, 22)), slots)
    }

    @Test
    fun aGapExactlyMinLengthIsKept() {
        val slots = computeFreeSlots(
            listOf(busy(9, 12), busy(13, 22)),
            testToday,
            days = 1,
            minLength = 60.minutes,
        )

        assertEquals(listOf(slot(12, 13)), slots)
    }

    @Test
    fun aFullyBusyDayGivesNoSlots() {
        val slots = computeFreeSlots(listOf(busy(8, 23)), testToday, days = 1)

        assertTrue(slots.isEmpty())
    }

    @Test
    fun busyBlocksApplyToTheRightDayOnly() {
        val tomorrow = testToday.plus(1, DateTimeUnit.DAY)
        val slots = computeFreeSlots(listOf(busy(12, 14, tomorrow)), testToday, days = 2)

        assertEquals(
            listOf(slot(9, 22), slot(9, 12, tomorrow), slot(14, 22, tomorrow)),
            slots,
        )
    }

    @Test
    fun aBusyBlockSpanningMidnightAffectsBothDays() {
        val tomorrow = testToday.plus(1, DateTimeUnit.DAY)
        val overnight = BusyBlock(at(20), at(10, date = tomorrow))

        val slots = computeFreeSlots(listOf(overnight), testToday, days = 2)

        assertEquals(listOf(slot(9, 20), slot(10, 22, tomorrow)), slots)
    }

    @Test
    fun aCustomDayWindowIsRespected() {
        val slots = computeFreeSlots(
            busy = emptyList(),
            from = testToday,
            days = 1,
            dayStart = LocalTime(18, 0),
            dayEnd = LocalTime(23, 0),
        )

        assertEquals(listOf(slot(18, 23)), slots)
    }

    @Test
    fun aLongerMinLengthDropsShortSlots() {
        val slots = computeFreeSlots(
            listOf(busy(11, 13)),
            testToday,
            days = 1,
            minLength = 3.hours,
        )

        // 9:00-11:00 is only 2 hours, so only the afternoon survives.
        assertEquals(listOf(slot(13, 22)), slots)
    }

    @Test
    fun zeroDaysGivesNothing() {
        assertTrue(computeFreeSlots(emptyList(), testToday, days = 0).isEmpty())
    }

    @Test
    fun aNegativeDayCountGivesNothing() {
        assertTrue(computeFreeSlots(emptyList(), testToday, days = -1).isEmpty())
    }

    @Test
    fun anInvertedDayWindowGivesNothing() {
        val slots = computeFreeSlots(
            busy = emptyList(),
            from = testToday,
            days = 1,
            dayStart = LocalTime(22, 0),
            dayEnd = LocalTime(9, 0),
        )

        assertTrue(slots.isEmpty())
    }

    @Test
    fun anEmptyBusyBlockIsIgnored() {
        val slots = computeFreeSlots(listOf(BusyBlock(at(12), at(12))), testToday, days = 1)

        assertEquals(listOf(slot(9, 22)), slots)
    }

    @Test
    fun slotsComeBackInChronologicalOrder() {
        val slots = computeFreeSlots(
            listOf(busy(11, 12), busy(15, 16)),
            testToday,
            days = 2,
        )

        assertEquals(slots.sortedBy { it.start }, slots)
    }
}
