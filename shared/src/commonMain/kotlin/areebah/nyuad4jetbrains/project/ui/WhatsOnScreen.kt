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
import areebah.nyuad4jetbrains.project.domain.formatDay
import areebah.nyuad4jetbrains.project.domain.formatPrice
import areebah.nyuad4jetbrains.project.domain.formatTime

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
            Text("What's on", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onRefresh, enabled = !state.loading) { Text("Refresh") }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Horizon.entries.forEach { horizon ->
                FilterChip(
                    selected = state.filters.horizon == horizon,
                    onClick = { onHorizonChange(horizon) },
                    label = { Text(horizon.label) },
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
                "Only my interests",
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
                        "Nothing matches your interests here. Turn off the filter, or look further ahead."
                    } else {
                        "No events in this period. Try looking further ahead."
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
                            formatDay(day.date, today!!),
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
                listOfNotNull(formatTime(event), event.venue, event.city).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(formatPrice(event), style = MaterialTheme.typography.bodyMedium)
                if (event.soldOut) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("Full") },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledLabelColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                }
            }
            if (ranked.matches.isNotEmpty()) {
                Text(
                    "Matches: ${ranked.matches.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                when (event.source) {
                    EventSource.TICKETMASTER -> "Ticketmaster"
                    EventSource.CURATED -> "Community"
                    EventSource.SAMPLE -> "Sample data"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
