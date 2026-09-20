package areebah.nyuad4jetbrains.project.calendar

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * A stretch of time the user is already busy, in Abu Dhabi local time.
 *
 * [end] is exclusive, so a block ending at 10:00 leaves 10:00 free.
 */
data class BusyBlock(val start: LocalDateTime, val end: LocalDateTime)

/** The outcome of reading the device calendar. Denial and failure are values, not exceptions. */
sealed interface CalendarResult {
    data class Success(val busy: List<BusyBlock>) : CalendarResult

    /** The user has not granted calendar access. The caller falls back to manual free slots. */
    data object PermissionDenied : CalendarResult

    /** No calendar on this device, or the read failed. [reason] is for logging, not for the user. */
    data class Unavailable(val reason: String) : CalendarResult
}

/**
 * Reads busy time from the device calendar. Read-only: nothing is ever written back.
 *
 * Implementations ignore all-day events and report times in the `Asia/Dubai` zone.
 */
interface CalendarReader {
    /** Busy blocks covering [days] days starting at [from], inclusive. */
    suspend fun busyBlocks(from: LocalDate, days: Int): CalendarResult
}
