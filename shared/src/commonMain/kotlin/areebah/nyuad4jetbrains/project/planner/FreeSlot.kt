package areebah.nyuad4jetbrains.project.planner

import areebah.nyuad4jetbrains.project.calendar.BusyBlock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** A gap the user is free, in Abu Dhabi local time. [end] is exclusive. */
data class FreeSlot(val start: LocalDateTime, val end: LocalDateTime)

/**
 * The free gaps left in [days] days from [from], once [busy] is taken out of the
 * `dayStart..dayEnd` window of each day.
 *
 * Overlapping and back-to-back busy blocks are merged first, and gaps shorter than
 * [minLength] are dropped.
 *
 * Implemented on branch `feature/free-slots`.
 */
fun computeFreeSlots(
    busy: List<BusyBlock>,
    from: LocalDate,
    days: Int,
    dayStart: LocalTime = LocalTime(9, 0),
    dayEnd: LocalTime = LocalTime(22, 0),
    minLength: Duration = 60.minutes,
): List<FreeSlot> = TODO("feature/free-slots")
