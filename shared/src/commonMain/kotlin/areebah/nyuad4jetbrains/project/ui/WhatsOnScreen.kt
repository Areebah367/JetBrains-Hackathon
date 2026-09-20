package areebah.nyuad4jetbrains.project.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import areebah.nyuad4jetbrains.project.domain.Cities
import areebah.nyuad4jetbrains.project.domain.EventSource
import areebah.nyuad4jetbrains.project.domain.Horizon
import areebah.nyuad4jetbrains.project.domain.RankedEvent
import areebah.nyuad4jetbrains.project.domain.buildSchedule
import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.whats_on_title
import kotlinproject.shared.generated.resources.refresh
import kotlinproject.shared.generated.resources.only_my_interests
import kotlinproject.shared.generated.resources.empty_filtered
import kotlinproject.shared.generated.resources.empty_period
import kotlinproject.shared.generated.resources.matches
import kotlinproject.shared.generated.resources.sold_out
import kotlinproject.shared.generated.resources.source_ticketmaster
import kotlinproject.shared.generated.resources.source_community
import kotlinproject.shared.generated.resources.source_sample
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsOnScreen(
    state: UiState,
    onOnlyMatchingChange: (Boolean) -> Unit,
    onHorizonChange: (Horizon) -> Unit,
    onToggleCity: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = state.today
    val result = state.result
    val schedule = remember(result, state.profile, state.filters, today) {
        if (today == null || result == null) {
            emptyList()
        } else {
            buildSchedule(result.events, state.profile, today, state.filters)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.whats_on_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onRefresh, enabled = !state.loading) { Text(stringResource(Res.string.refresh)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Horizon.entries.forEach { horizon ->
                FilterChip(
                    selected = state.filters.horizon == horizon,
                    onClick = { onHorizonChange(horizon) },
                    label = { Text(horizonText(horizon)) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Cities.all.forEach { city ->
                FilterChip(
                    selected = city in state.filters.cities,
                    onClick = { onToggleCity(city) },
                    label = { Text(city) },
                )
            }
        }

        result?.notice?.let { notice ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            ) {
                Text(notice, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.only_my_interests),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = state.filters.onlyMatching, onCheckedChange = onOnlyMatchingChange)
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            schedule.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (state.filters.onlyMatching) {
                        stringResource(Res.string.empty_filtered)
                    } else {
                        stringResource(Res.string.empty_period)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                schedule.forEach { day ->
                    item {
                        Text(
                            dayText(day.date, today!!),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(day.events) { ranked -> EventCard(ranked) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(ranked: RankedEvent, modifier: Modifier = Modifier) {
    val event = ranked.event
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(event.name, style = MaterialTheme.typography.titleMedium)
            Text(
                listOfNotNull(timeText(event), event.venue, event.city).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(priceText(event), style = MaterialTheme.typography.bodyMedium)
                if (event.soldOut) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(stringResource(Res.string.sold_out)) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledLabelColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                }
            }
            if (ranked.matches.isNotEmpty()) {
                // `map` is inline, so the composable lookup is legal here; joinToString's lambda is not.
                val matchNames = ranked.matches.map { interestText(it) }.joinToString(", ")
                Text(
                    stringResource(Res.string.matches, matchNames),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                stringResource(
                    when (event.source) {
                        EventSource.TICKETMASTER -> Res.string.source_ticketmaster
                        EventSource.CURATED -> Res.string.source_community
                        EventSource.SAMPLE -> Res.string.source_sample
                    },
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
