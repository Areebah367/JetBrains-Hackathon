package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Cities
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.EventSource
import kotlinx.datetime.LocalDateTime

/**
 * Community events entered by hand from event pages the team looked at.
 *
 * Ticketmaster only lists large ticketed shows booked months ahead and returns no prices, so this
 * fills the near-term gap and gives the app real prices to work with. Nothing here is scraped: the
 * team reads a page and types the details in. Keep it that way — Luma's terms only allow access
 * through their own interfaces.
 *
 * These have fixed real dates, so refresh them when they fall into the past.
 */
fun curatedEvents(): List<Event> = listOf(
    Event(
        id = "curated-heart2heart-3",
        name = "Heart2Heart Dubai Relationship Circle — Edition 3: Come Back to the Room",
        start = LocalDateTime(2026, 9, 20, 16, 0),
        end = LocalDateTime(2026, 9, 20, 18, 0),
        city = Cities.DUBAI,
        venue = "Taj Dubai",
        category = "Wellness",
        genre = "Relationships",
        priceMin = 120.0,
        priceMax = 120.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-sohum-open-mic",
        name = "Sohum Open Mic — Music, Poetry & More",
        start = LocalDateTime(2026, 9, 20, 17, 0),
        end = LocalDateTime(2026, 9, 20, 20, 0),
        city = Cities.DUBAI,
        venue = "Sohum Wellness Sanctuary",
        category = "Music",
        genre = "Open mic",
        priceMin = 0.0,
        priceMax = 0.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-ignyte-founder-sessions-3",
        name = "Ignyte x TheBlock.: Founder Sessions 3",
        start = LocalDateTime(2026, 9, 23, 11, 0),
        end = LocalDateTime(2026, 9, 23, 13, 30),
        city = Cities.DUBAI,
        venue = "TheBlock. Street, One Central",
        category = "Networking",
        genre = "Startup pitch",
        priceMin = 0.0,
        priceMax = 0.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-entrepreneurs-sunset-yacht",
        name = "Entrepreneurs' Sunset Yacht",
        start = LocalDateTime(2026, 9, 23, 15, 30),
        end = LocalDateTime(2026, 9, 23, 19, 30),
        city = Cities.DUBAI,
        venue = "Dubai Harbour",
        category = "Networking",
        genre = "Entrepreneurs",
        // The page showed a waiting list and no ticket price.
        soldOut = true,
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-bocasu-spoken-word",
        name = "Spoken Word Open Mic — Bocasu",
        start = LocalDateTime(2026, 9, 23, 19, 30),
        end = LocalDateTime(2026, 9, 23, 22, 30),
        city = Cities.DUBAI,
        venue = "BOCASU",
        category = "Theatre & arts",
        genre = "Spoken word",
        priceMin = 0.0,
        priceMax = 0.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
)
