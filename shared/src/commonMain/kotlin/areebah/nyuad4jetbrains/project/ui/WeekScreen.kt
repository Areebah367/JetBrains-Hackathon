package areebah.nyuad4jetbrains.project.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import areebah.nyuad4jetbrains.project.data.EventsResult
import areebah.nyuad4jetbrains.project.domain.RankedEvent
import areebah.nyuad4jetbrains.project.domain.buildWeek
import areebah.nyuad4jetbrains.project.domain.formatDay
import areebah.nyuad4jetbrains.project.domain.formatPrice
import areebah.nyuad4jetbrains.project.domain.formatTime

@Composable
fun WeekScreen(
    state: UiState,
    onOnlyMatchingChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = state.today
    val result = state.result
    val week = remember(result, state.profile, state.onlyMatching, today) {
        if (today == null || result == null) {
            emptyList()
        } else {
            buildWeek(result.events, state.profile, today, onlyMatching = state.onlyMatching)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Your next 7 days",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRefresh, enabled = !state.loading) { Text("Refresh") }
        }

        if (result is EventsResult.Sample) {
            SampleBanner(result.reason, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Only events that match my interests",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = state.onlyMatching, onCheckedChange = onOnlyMatchingChange)
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            week.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (state.onlyMatching) {
                        "Nothing matches your interests this week. Turn off the filter to see everything."
                    } else {
                        "No events found for the next 7 days."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                week.forEach { day ->
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

@Composable
private fun SampleBanner(reason: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Showing sample events", style = MaterialTheme.typography.titleSmall)
            Text(reason, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EventCard(ranked: RankedEvent, modifier: Modifier = Modifier) {
    val event = ranked.event
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(event.name, style = MaterialTheme.typography.titleMedium)
            Text(
                listOfNotNull(formatTime(event), event.venue).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(formatPrice(event), style = MaterialTheme.typography.bodyMedium)
            if (ranked.matches.isNotEmpty()) {
                Text(
                    "Matches: ${ranked.matches.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
