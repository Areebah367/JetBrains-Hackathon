package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Event
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

/** Made-up events spread over the coming week, so the app can be demoed without a key or a connection. */
fun sampleEventsFor(today: LocalDate): List<Event> {
    fun at(dayOffset: Int, hour: Int, minute: Int = 0) =
        LocalDateTime(today.plus(dayOffset, DateTimeUnit.DAY), LocalTime(hour, minute))

    return listOf(
        Event("sample-1", "Sample: Live jazz night", at(0, 20), venue = "Sample Waterfront Hall", category = "Music", genre = "Jazz", priceMin = 120.0, priceMax = 200.0, currency = "AED"),
        Event("sample-2", "Sample: Stand-up comedy showcase", at(1, 19, 30), venue = "Sample Comedy Club", category = "Arts & Theatre", genre = "Comedy", priceMin = 90.0, priceMax = 90.0, currency = "AED"),
        Event("sample-3", "Sample: Beach football tournament", at(1, 16), venue = "Sample Beach Pitch", category = "Sports", genre = "Football", priceMin = 0.0, priceMax = 0.0, currency = "AED"),
        Event("sample-4", "Sample: Open-air film screening", at(2, 21), venue = "Sample Park", category = "Film", genre = "Family", priceMin = 45.0, priceMax = 45.0, currency = "AED"),
        Event("sample-5", "Sample: Electronic music festival", at(3, 22), venue = "Sample Arena", category = "Music", genre = "Dance/Electronic", priceMin = 250.0, priceMax = 600.0, currency = "AED"),
        Event("sample-6", "Sample: Family art workshop", at(4, 11), venue = "Sample Gallery", category = "Arts & Theatre", genre = "Family", priceMin = 60.0, priceMax = 60.0, currency = "AED"),
        Event("sample-7", "Sample: Ballet evening", at(5, 19), venue = "Sample Theatre", category = "Arts & Theatre", genre = "Ballet", priceMin = 150.0, priceMax = 400.0, currency = "AED"),
        Event("sample-8", "Sample: Basketball league game", at(5, 18), venue = "Sample Sports Hall", category = "Sports", genre = "Basketball"),
        Event("sample-9", "Sample: Classical orchestra concert", at(6, 20), timeKnown = true, venue = "Sample Concert Hall", category = "Music", genre = "Classical", priceMin = 100.0, priceMax = 300.0, currency = "AED"),
    )
}
