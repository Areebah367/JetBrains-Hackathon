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
    (0 until days).flatMap { offset ->
        val day = from.plus(offset, DateTimeUnit.DAY)
        listOf(
            busy(day, 9, 0, 10, 0),    // breakfast
            busy(day, 13, 0, 14, 0),   // lunch
            busy(day, 19, 0, 20, 0),   // dinner
            busy(day, 21, 0, 23, 59),  // sleep, from 21:00 to the end of the day
        )
    }

private fun busy(day: LocalDate, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) =
    BusyBlock(
        start = LocalDateTime(day, LocalTime(startHour, startMinute)),
        end = LocalDateTime(day, LocalTime(endHour, endMinute)),
    )
