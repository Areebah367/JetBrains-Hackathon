package areebah.nyuad4jetbrains.project.data

import areebah.nyuad4jetbrains.project.domain.Event
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Only the parts of the Ticketmaster Discovery API response that this app uses. */
internal val ticketmasterJson = Json { ignoreUnknownKeys = true }

@Serializable
internal data class TmResponse(
    @SerialName("_embedded") val embedded: TmEmbedded? = null,
)

@Serializable
internal data class TmEmbedded(val events: List<TmEvent> = emptyList())

@Serializable
internal data class TmEvent(
    val id: String,
    val name: String,
    val url: String? = null,
    val dates: TmDates? = null,
    val priceRanges: List<TmPriceRange>? = null,
    val classifications: List<TmClassification>? = null,
    @SerialName("_embedded") val embedded: TmEventEmbedded? = null,
)

@Serializable
internal data class TmDates(val start: TmStart? = null)

@Serializable
internal data class TmStart(val localDate: String? = null, val localTime: String? = null)

@Serializable
internal data class TmPriceRange(val min: Double? = null, val max: Double? = null, val currency: String? = null)

@Serializable
internal data class TmClassification(val segment: TmNamed? = null, val genre: TmNamed? = null)

@Serializable
internal data class TmNamed(val name: String? = null)

@Serializable
internal data class TmEventEmbedded(val venues: List<TmVenue>? = null)

@Serializable
internal data class TmVenue(val name: String? = null)

/** Returns null for events without a usable date, since they cannot be placed in the week. */
internal fun TmEvent.toEvent(): Event? {
    val start = dates?.start ?: return null
    val date = start.localDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    val time = start.localTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    val classification = classifications?.firstOrNull()
    val prices = priceRanges.orEmpty()

    return Event(
        id = id,
        name = name,
        start = LocalDateTime(date, time ?: LocalTime(0, 0)),
        timeKnown = time != null,
        venue = embedded?.venues?.firstOrNull()?.name,
        category = classification?.segment?.name.usable(),
        genre = classification?.genre?.name.usable(),
        priceMin = prices.mapNotNull { it.min }.minOrNull(),
        priceMax = prices.mapNotNull { it.max }.maxOrNull(),
        currency = prices.firstNotNullOfOrNull { it.currency },
        url = url,
    )
}

/** Ticketmaster fills unknown categories with the word "Undefined". */
private fun String?.usable(): String? = this?.takeUnless { it.isBlank() || it.equals("Undefined", ignoreCase = true) }
