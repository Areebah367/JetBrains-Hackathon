package areebah.nyuad4jetbrains.project.ui

import androidx.compose.runtime.Composable
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.Horizon
import areebah.nyuad4jetbrains.project.domain.Interests
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.currency_amount
import kotlinproject.shared.generated.resources.horizon_all
import kotlinproject.shared.generated.resources.horizon_month
import kotlinproject.shared.generated.resources.horizon_week
import kotlinproject.shared.generated.resources.interest_comedy
import kotlinproject.shared.generated.resources.interest_family
import kotlinproject.shared.generated.resources.interest_film
import kotlinproject.shared.generated.resources.interest_fitness
import kotlinproject.shared.generated.resources.interest_food
import kotlinproject.shared.generated.resources.interest_markets
import kotlinproject.shared.generated.resources.interest_music
import kotlinproject.shared.generated.resources.interest_networking
import kotlinproject.shared.generated.resources.interest_sports
import kotlinproject.shared.generated.resources.interest_theatre
import kotlinproject.shared.generated.resources.interest_wellness
import kotlinproject.shared.generated.resources.interest_workshops
import kotlinproject.shared.generated.resources.price_free
import kotlinproject.shared.generated.resources.price_unknown
import kotlinproject.shared.generated.resources.theme_dark
import kotlinproject.shared.generated.resources.theme_light
import kotlinproject.shared.generated.resources.theme_system
import kotlinproject.shared.generated.resources.time_tba
import kotlinproject.shared.generated.resources.today
import kotlinproject.shared.generated.resources.tomorrow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Translated versions of things the domain layer formats.
 *
 * The domain keeps returning plain values; only these composables know about language, so the
 * business rules stay testable without a Compose runtime.
 */

/** "Free", "AED 150", "AED 150–450" or "Price unknown", in the current language. */
@Composable
fun priceText(event: Event): String {
    val min = event.priceMin
    val max = event.priceMax
    if (min == null && max == null) return stringResource(Res.string.price_unknown)
    val low = min ?: max!!
    val high = max ?: min!!
    if (low == 0.0 && high == 0.0) return stringResource(Res.string.price_free)
    val amount = if (low == high) number(low) else "${number(low)}–${number(high)}"
    return stringResource(Res.string.currency_amount, amount)
}

@Composable
fun amountText(value: Double): String = stringResource(Res.string.currency_amount, number(value))

@Composable
fun timeText(event: Event): String =
    if (!event.timeKnown) {
        stringResource(Res.string.time_tba)
    } else {
        "${event.start.hour.toString().padStart(2, '0')}:${event.start.minute.toString().padStart(2, '0')}"
    }

@Composable
fun dayText(date: LocalDate, today: LocalDate): String = when (date) {
    today -> stringResource(Res.string.today)
    today.plus(1, DateTimeUnit.DAY) -> stringResource(Res.string.tomorrow)
    else -> {
        val weekday = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        "$weekday ${date.day} $month"
    }
}

@Composable
fun horizonText(horizon: Horizon): String = stringResource(
    when (horizon) {
        Horizon.WEEK -> Res.string.horizon_week
        Horizon.MONTH -> Res.string.horizon_month
        Horizon.ALL -> Res.string.horizon_all
    },
)

@Composable
fun themeModeText(mode: ThemeMode): String = stringResource(
    when (mode) {
        ThemeMode.SYSTEM -> Res.string.theme_system
        ThemeMode.LIGHT -> Res.string.theme_light
        ThemeMode.DARK -> Res.string.theme_dark
    },
)

/**
 * The translated name of an interest.
 *
 * [Interest.label] stays English because it is the identity used for matching and for the selected
 * set; only the displayed name changes with language.
 */
@Composable
fun interestText(label: String): String {
    val resource = interestLabels[label] ?: return label
    return stringResource(resource)
}

private val interestLabels: Map<String, StringResource> = mapOf(
    "Music" to Res.string.interest_music,
    "Sports" to Res.string.interest_sports,
    "Fitness" to Res.string.interest_fitness,
    "Comedy" to Res.string.interest_comedy,
    "Theatre & arts" to Res.string.interest_theatre,
    "Workshops" to Res.string.interest_workshops,
    "Food & drink" to Res.string.interest_food,
    "Markets & pop-ups" to Res.string.interest_markets,
    "Networking" to Res.string.interest_networking,
    "Wellness" to Res.string.interest_wellness,
    "Family" to Res.string.interest_family,
    "Film" to Res.string.interest_film,
)

/** Every interest on offer has a translation. Guards against adding one and forgetting the string. */
internal fun untranslatedInterests(): List<String> =
    Interests.all.map { it.label }.filter { it !in interestLabels }

private fun number(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else ((value * 100).toLong() / 100.0).toString()
