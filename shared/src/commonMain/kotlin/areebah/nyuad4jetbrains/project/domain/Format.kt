package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun formatTime(event: Event): String =
    if (!event.timeKnown) {
        "Time TBA"
    } else {
        "${event.start.hour.toString().padStart(2, '0')}:${event.start.minute.toString().padStart(2, '0')}"
    }

/** "Free", "AED 150", "AED 150–450", or "Price unknown". Never guesses a price. */
fun formatPrice(event: Event): String {
    val min = event.priceMin
    val max = event.priceMax
    if (min == null && max == null) return "Price unknown"
    val low = min ?: max!!
    val high = max ?: min!!
    if (low == 0.0 && high == 0.0) return "Free"
    val currency = event.currency?.let { "$it " }.orEmpty()
    return if (low == high) "$currency${number(low)}" else "$currency${number(low)}–${number(high)}"
}

fun formatDay(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Today"
    today.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
    else -> {
        val weekday = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        "$weekday ${date.dayOfMonth} $month"
    }
}

private fun number(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
