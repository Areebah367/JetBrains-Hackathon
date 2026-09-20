package areebah.nyuad4jetbrains.project.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TicketmasterDtoTest {
    private val response = """
        {
          "_embedded": {
            "events": [
              {
                "id": "e1", "name": "Big Concert", "url": "https://example.com/e1",
                "dates": {"start": {"localDate": "2026-09-25", "localTime": "20:30:00", "dateTime": "2026-09-25T16:30:00Z"}},
                "priceRanges": [{"type": "standard", "currency": "AED", "min": 150.0, "max": 450.0}],
                "classifications": [{"segment": {"id": "s", "name": "Music"}, "genre": {"id": "g", "name": "Rock"}}],
                "_embedded": {"venues": [{"name": "Etihad Arena", "city": {"name": "Abu Dhabi"}}]}
              },
              {"id": "e2", "name": "Sparse event", "dates": {"start": {"localDate": "2026-09-26"}}},
              {"id": "e3", "name": "No date", "dates": {"start": {}}},
              {
                "id": "e4", "name": "Unlabelled", "dates": {"start": {"localDate": "2026-09-27", "localTime": "19:00:00"}},
                "classifications": [{"segment": {"name": "Undefined"}, "genre": {"name": "Undefined"}}]
              }
            ]
          }
        }
    """.trimIndent()

    private val events = ticketmasterJson.decodeFromString<TmResponse>(response)
        .embedded?.events.orEmpty().mapNotNull { it.toEvent() }

    @Test
    fun aFullEventIsMappedCompletely() {
        val event = events.first { it.id == "e1" }
        assertEquals("Big Concert", event.name)
        assertEquals("2026-09-25T20:30", event.start.toString())
        assertTrue(event.timeKnown)
        assertEquals("Etihad Arena", event.venue)
        assertEquals("Music", event.category)
        assertEquals("Rock", event.genre)
        assertEquals(150.0, event.priceMin)
        assertEquals(450.0, event.priceMax)
        assertEquals("AED", event.currency)
    }

    @Test
    fun aMissingPriceStaysUnknownInsteadOfZero() {
        val event = events.first { it.id == "e2" }
        assertNull(event.priceMin)
        assertNull(event.priceMax)
    }

    @Test
    fun aMissingTimeIsFlaggedAsUnknown() {
        val event = events.first { it.id == "e2" }
        assertEquals(false, event.timeKnown)
    }

    @Test
    fun anEventWithoutADateIsDropped() {
        assertEquals(listOf("e1", "e2", "e4"), events.map { it.id })
    }

    @Test
    fun undefinedCategoriesBecomeNull() {
        val event = events.first { it.id == "e4" }
        assertNull(event.category)
        assertNull(event.genre)
    }

    @Test
    fun anEmptyResponseGivesNoEvents() {
        val parsed = ticketmasterJson.decodeFromString<TmResponse>("{}")
        assertTrue(parsed.embedded?.events.orEmpty().isEmpty())
    }
}
