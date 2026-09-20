package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Cities
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.EventSource
import kotlinx.datetime.LocalDateTime

/**
 * Community events entered by hand from event pages and listings the team looked at.
 *
 * Ticketmaster lists only large ticketed shows booked months ahead and returns no prices at all, so
 * this is where near-term events and real AED prices come from. Nothing here is scraped: a person
 * reads a page and types the details in. Keep it that way — Luma's and Partiful's terms only allow
 * access through their own interfaces.
 *
 * ## Dates are not all exact
 *
 * The weekly listings these came from tag events only by weekday ("fri", "sat"), and the batch that
 * was captured covered 2026-09-18 to 2026-09-20, which is already past. Recurring and undated
 * entries were therefore dated to a **plausible next occurrence** so the app has something to show.
 * Each one below says whether its date is exact or inferred. Before demoing, check them against the
 * real listings, and refresh the whole file once these dates fall into the past.
 */
fun curatedEvents(): List<Event> = lumaEvents + weeklyListings + bookableExperiences

/** Exact dates and prices, taken from the event pages themselves. */
private val lumaEvents = listOf(
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

/**
 * From a weekly city roundup. Prices and times are as listed; **dates are inferred** — the listing
 * gave only a weekday, so each is dated to the next occurrence of that weekday after 2026-09-20.
 */
private val weeklyListings = listOf(
    Event(
        id = "curated-minbash-pike",
        name = "Minbash × PIKE — limited-edition drinks",
        // Listed as running until 26 October; a mid-range date was chosen.
        start = LocalDateTime(2026, 9, 23, 10, 0),
        end = LocalDateTime(2026, 9, 23, 22, 0),
        city = Cities.DUBAI,
        venue = "Minbash, Umm Suqeim 1",
        category = "Food & drink",
        genre = "Coffee",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-the-ordinary-popup",
        name = "The Ordinary Pop-Up at Paus Club",
        // Listed as running until 1 October; a date inside that range was chosen.
        start = LocalDateTime(2026, 9, 24, 11, 0),
        end = LocalDateTime(2026, 9, 24, 20, 0),
        city = Cities.DUBAI,
        venue = "Paus Club",
        category = "Markets & pop-ups",
        genre = "Skincare & wellness",
        priceMin = 0.0,
        priceMax = 0.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-flower-basket-workshop",
        name = "Flower Basket Workshop",
        start = LocalDateTime(2026, 9, 25, 15, 0),
        end = LocalDateTime(2026, 9, 25, 18, 0),
        city = Cities.DUBAI,
        venue = "Tania's Teahouse, Dubai Hills",
        category = "Workshops",
        genre = "Flower arranging",
        priceMin = 290.0,
        priceMax = 290.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-pilates-sculpt-glow",
        name = "Pilates, Sculpt & Glow",
        start = LocalDateTime(2026, 9, 26, 10, 0),
        end = LocalDateTime(2026, 9, 26, 16, 30),
        city = Cities.DUBAI,
        venue = "House of Pilates, Nad Al Sheba 1",
        category = "Fitness",
        genre = "Pilates",
        // Listed as "from 150 AED", so the upper end is unknown.
        priceMin = 150.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-new-balance-1rebel",
        name = "New Balance x 1Rebel Race Day",
        start = LocalDateTime(2026, 9, 26, 14, 15),
        end = LocalDateTime(2026, 9, 26, 17, 30),
        city = Cities.DUBAI,
        venue = "1Rebel",
        category = "Fitness",
        genre = "Workout & brunch",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-shukran-uae",
        name = "Shukran, UAE — Dubai Camerata Singers",
        // The listing gave a start time but no end.
        start = LocalDateTime(2026, 9, 26, 19, 0),
        city = Cities.DUBAI,
        venue = "New Covent Garden Theatre, Mall of the Emirates",
        category = "Music",
        genre = "Choir concert",
        priceMin = 100.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-vera-market-sat",
        name = "VERA Community Market",
        start = LocalDateTime(2026, 9, 26, 10, 0),
        end = LocalDateTime(2026, 9, 26, 18, 0),
        city = Cities.DUBAI,
        venue = "VERA Wellness",
        category = "Markets & pop-ups",
        genre = "Fashion & wellness",
        // Free to enter; the classes inside cost AED 95.
        priceMin = 0.0,
        priceMax = 0.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-vera-market-sun",
        name = "VERA Community Market",
        start = LocalDateTime(2026, 9, 27, 10, 0),
        end = LocalDateTime(2026, 9, 27, 18, 0),
        city = Cities.DUBAI,
        venue = "VERA Wellness",
        category = "Markets & pop-ups",
        genre = "Fashion & wellness",
        priceMin = 0.0,
        priceMax = 0.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-ice-warrior-challenge",
        name = "Ice Warrior Challenge",
        start = LocalDateTime(2026, 9, 27, 6, 0),
        city = Cities.DUBAI,
        venue = "Ski Dubai",
        category = "Fitness",
        genre = "Obstacle course",
        priceMin = 200.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
)

/**
 * Bookable experiences rather than one-off events: the listing gave a price and a place but no date
 * at all. **Every date here is invented** to give the app content further out. Replace them with
 * real session times, or drop these, before the demo.
 */
private val bookableExperiences = listOf(
    Event(
        id = "curated-wakeflow-wakesurfing",
        name = "Wakesurfing session with WakeFlow",
        start = LocalDateTime(2026, 10, 3, 9, 0),
        end = LocalDateTime(2026, 10, 3, 10, 0),
        city = Cities.DUBAI,
        venue = "Dubai Harbour",
        category = "Sports",
        genre = "Wakesurfing",
        // Listed as "from AED 600/hour".
        priceMin = 600.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-sup-yoga-pilates",
        name = "SUP Yoga & Pilates at Level 77",
        start = LocalDateTime(2026, 10, 4, 8, 0),
        end = LocalDateTime(2026, 10, 4, 10, 45),
        city = Cities.DUBAI,
        venue = "Address Beach Resort, JBR",
        category = "Fitness",
        genre = "Yoga & pilates",
        priceMin = 377.0,
        priceMax = 377.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-goloco-all-access",
        name = "GoLoco All Access Pass",
        start = LocalDateTime(2026, 10, 2, 16, 0),
        end = LocalDateTime(2026, 10, 2, 20, 0),
        city = Cities.DUBAI,
        venue = "GoLoco Arena, Al Quoz",
        category = "Family",
        genre = "Bowling, laser tag & arcade",
        priceMin = 299.0,
        priceMax = 299.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
    Event(
        id = "curated-silversoul-ring-crafting",
        name = "Ring-Crafting Workshop",
        start = LocalDateTime(2026, 10, 3, 14, 0),
        end = LocalDateTime(2026, 10, 3, 16, 0),
        city = Cities.DUBAI,
        venue = "Ivy's Secret Garden, Al Quoz",
        category = "Workshops",
        genre = "Silver jewellery",
        priceMin = 430.0,
        priceMax = 430.0,
        currency = "AED",
        source = EventSource.CURATED,
    ),
)
