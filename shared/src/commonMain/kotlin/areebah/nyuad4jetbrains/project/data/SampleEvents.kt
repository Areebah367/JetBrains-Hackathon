package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Cities
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.EventSource
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

/** Last-resort made-up events, used only when neither curated nor live events are available. */
fun sampleEventsFor(today: LocalDate): List<Event> {
    fun at(dayOffset: Int, hour: Int, minute: Int = 0) =
        LocalDateTime(today.plus(dayOffset, DateTimeUnit.DAY), LocalTime(hour, minute))

    fun sample(
        id: String,
        name: String,
        start: LocalDateTime,
        city: String,
        venue: String,
        category: String,
        genre: String,
        priceMin: Double? = null,
        priceMax: Double? = null,
    ) = Event(
        id = id,
        name = name,
        start = start,
        city = city,
        venue = venue,
        category = category,
        genre = genre,
        priceMin = priceMin,
        priceMax = priceMax,
        currency = if (priceMin != null) "AED" else null,
        source = EventSource.SAMPLE,
    )

    return listOf(
        sample("sample-1", "Sample: Live jazz night", at(0, 20), Cities.ABU_DHABI, "Sample Waterfront Hall", "Music", "Jazz", 120.0, 200.0),
        sample("sample-2", "Sample: Stand-up comedy showcase", at(1, 19, 30), Cities.DUBAI, "Sample Comedy Club", "Comedy", "Stand-up", 90.0, 90.0),
        sample("sample-3", "Sample: Beach football tournament", at(1, 16), Cities.ABU_DHABI, "Sample Beach Pitch", "Sports", "Football", 0.0, 0.0),
        sample("sample-4", "Sample: Open-air film screening", at(2, 21), Cities.DUBAI, "Sample Park", "Film", "Family", 45.0, 45.0),
        sample("sample-5", "Sample: Electronic music festival", at(3, 22), Cities.ABU_DHABI, "Sample Arena", "Music", "Dance/Electronic", 250.0, 600.0),
        sample("sample-6", "Sample: Founder networking breakfast", at(4, 9), Cities.DUBAI, "Sample Co-working", "Networking", "Startup", 0.0, 0.0),
        sample("sample-7", "Sample: Ballet evening", at(5, 19), Cities.ABU_DHABI, "Sample Theatre", "Theatre & arts", "Ballet", 150.0, 400.0),
        sample("sample-8", "Sample: Basketball league game", at(5, 18), Cities.DUBAI, "Sample Sports Hall", "Sports", "Basketball"),
        sample("sample-9", "Sample: Classical orchestra concert", at(6, 20), Cities.ABU_DHABI, "Sample Concert Hall", "Music", "Classical", 100.0, 300.0),
    )
}
