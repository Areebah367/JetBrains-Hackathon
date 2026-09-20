package areebah.nyuad4jetbrains.project.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

/** A Sunday, so "today" plus 0..6 days covers Sunday to Saturday. */
val testToday = LocalDate(2026, 9, 20)

fun testEvent(
    id: String,
    name: String,
    date: LocalDate = testToday,
    hour: Int = 20,
    minute: Int = 0,
    city: String? = Cities.ABU_DHABI,
    category: String? = null,
    genre: String? = null,
    priceMin: Double? = null,
    priceMax: Double? = null,
    currency: String? = "AED",
    soldOut: Boolean = false,
) = Event(
    id = id,
    name = name,
    start = LocalDateTime(date, LocalTime(hour, minute)),
    city = city,
    category = category,
    genre = genre,
    priceMin = priceMin,
    priceMax = priceMax,
    currency = currency,
    soldOut = soldOut,
)
