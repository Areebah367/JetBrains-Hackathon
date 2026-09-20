package areebah.nyuad4jetbrains.project.planner

import areebah.nyuad4jetbrains.project.calendar.BusyBlock
import areebah.nyuad4jetbrains.project.domain.AbuDhabiTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** A gap the user is free, in Abu Dhabi local time. [end] is exclusive. */
data class FreeSlot(val start: LocalDateTime, val end: LocalDateTime)

/**
 * The free gaps left in [days] days from [from], once [busy] is taken out of the
 * `dayStart..dayEnd` window of each day.
 *
 * Overlapping and back-to-back busy blocks are merged first, busy time outside the window is
 * ignored, and gaps shorter than [minLength] are dropped. An empty [busy] gives one slot per day.
 */
fun computeFreeSlots(
    busy: List<BusyBlock>,
    from: LocalDate,
    days: Int,
    dayStart: LocalTime = LocalTime(9, 0),
    dayEnd: LocalTime = LocalTime(22, 0),
    minLength: Duration = 60.minutes,
): List<FreeSlot> {
    if (days <= 0 || dayStart >= dayEnd) return emptyList()

    val merged = mergeBusy(busy)

    return (0 until days).flatMap { offset ->
        val day = from.plus(offset, DateTimeUnit.DAY)
        freeGapsIn(LocalDateTime(day, dayStart), LocalDateTime(day, dayEnd), merged, minLength)
    }
}

/** Sorts busy blocks and folds overlapping and back-to-back ones together. Empty blocks are dropped. */
private fun mergeBusy(busy: List<BusyBlock>): List<BusyBlock> {
    val ordered = busy.filter { it.start < it.end }.sortedBy { it.start }
    val merged = mutableListOf<BusyBlock>()
    for (block in ordered) {
        val last = merged.lastOrNull()
        // `<=` so blocks that touch (one ends exactly when the next starts) become one.
        if (last != null && block.start <= last.end) {
            if (block.end > last.end) merged[merged.lastIndex] = last.copy(end = block.end)
        } else {
            merged.add(block)
        }
    }
    return merged
}

/** Walks one day's window and collects what [merged] leaves behind. */
private fun freeGapsIn(
    windowStart: LocalDateTime,
    windowEnd: LocalDateTime,
    merged: List<BusyBlock>,
    minLength: Duration,
): List<FreeSlot> {
    val slots = mutableListOf<FreeSlot>()
    var cursor = windowStart

    for (block in merged) {
        if (block.end <= cursor) continue
        if (block.start >= windowEnd) break
        if (block.start > cursor) slots.addIfLongEnough(cursor, minOf(block.start, windowEnd), minLength)
        if (block.end > cursor) cursor = block.end
        if (cursor >= windowEnd) break
    }
    slots.addIfLongEnough(cursor, windowEnd, minLength)

    return slots
}

private fun MutableList<FreeSlot>.addIfLongEnough(
    start: LocalDateTime,
    end: LocalDateTime,
    minLength: Duration,
) {
    if (start < end && lengthOf(start, end) >= minLength) add(FreeSlot(start, end))
}

/** Abu Dhabi has no daylight saving, so this is a plain wall-clock difference. */
private fun lengthOf(start: LocalDateTime, end: LocalDateTime): Duration =
    end.toInstant(AbuDhabiTime.zone) - start.toInstant(AbuDhabiTime.zone)
