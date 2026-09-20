package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.calendar.BusyBlock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

/**
 * A stand-in for the phone's calendar: the daily routine most people actually have.
 *
 * The real readers exist (`AndroidCalendarReader`, and EventKit on iOS), but neither is wired up
 * yet, so this gives the planner realistic busy time to work around instead of treating whole days
 * as free. Swap it for a real [areebah.nyuad4jetbrains.project.calendar.CalendarReader] result and
 * nothing downstream changes.
 *
 * Inside the planner's 09:00-22:00 window this leaves 10:00-13:00, 14:00-19:00 and 20:00-21:00 free.
 */
fun demoBusyBlocks(from: LocalDate, days: Int): List<BusyBlock> =
    demoRoutine(from, days).map { BusyBlock(it.start, it.end) }

/** A busy block the calendar can label, so the user sees *why* a slot is taken. */
data class RoutineBlock(val label: String, val start: LocalDateTime, val end: LocalDateTime)

fun demoRoutine(from: LocalDate, days: Int): List<RoutineBlock> =
    (0 until days).flatMap { offset ->
        val day = from.plus(offset, DateTimeUnit.DAY)
        listOf(
            block("Breakfast", day, 9, 10),
            block("Lunch", day, 13, 14),
            block("Dinner", day, 19, 20),
            block("Sleep", day, 21, 24),
        )
    }

/** [endHour] 24 means the end of the day, which LocalTime cannot express directly. */
private fun block(label: String, day: LocalDate, startHour: Int, endHour: Int) = RoutineBlock(
    label = label,
    start = LocalDateTime(day, LocalTime(startHour, 0)),
    end = LocalDateTime(day, if (endHour >= 24) LocalTime(23, 59) else LocalTime(endHour, 0)),
)
