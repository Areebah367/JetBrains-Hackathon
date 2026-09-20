package areebah.nyuad4jetbrains.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.domain.formatDay
import areebah.nyuad4jetbrains.project.domain.formatPrice
import areebah.nyuad4jetbrains.project.domain.formatTime
import areebah.nyuad4jetbrains.project.planner.PlannerState
import areebah.nyuad4jetbrains.project.planner.accepted
import areebah.nyuad4jetbrains.project.planner.maybes
import areebah.nyuad4jetbrains.project.planner.priceOf
import areebah.nyuad4jetbrains.project.planner.remainingBudget
import areebah.nyuad4jetbrains.project.planner.spent

/** A tentative suggestion is shown faded; once accepted it becomes solid. */
private const val MAYBE_ALPHA = 0.55f

@Composable
fun PlanScreen(
    state: UiState,
    onBudgetChange: (Double) -> Unit,
    onAccept: (String, Double?) -> Unit,
    onReject: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val planner = state.planner
    val today = state.today
    var pricingEvent by remember { mutableStateOf<Event?>(null) }

    pricingEvent?.let { event ->
        PriceDialog(
            event = event,
            onDismiss = { pricingEvent = null },
            onConfirm = { price ->
                onAccept(event.id, price)
                pricingEvent = null
            },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { BudgetHeader(planner, state.budget, onBudgetChange, onReset) }
        item { FreeTimeNote(state) }

        if (planner.accepted.isNotEmpty()) {
            item { SectionTitle("Your plan") }
            items(planner.accepted, key = { it.id }) { event ->
                PlannedCard(event, planner, today, onReject)
            }
        }

        item { SectionTitle("Suggestions") }

        val maybes = planner.maybes
        if (maybes.isEmpty()) {
            item {
                Text(
                    if (planner.events.isEmpty()) {
                        "No events loaded yet."
                    } else {
                        "Nothing else fits your free time and budget."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(maybes, key = { it.id }) { event ->
                MaybeCard(
                    event = event,
                    planner = planner,
                    today = today,
                    onYes = {
                        // Ask for a price only when we do not already have one.
                        if (planner.priceOf(event.id) == null) pricingEvent = event else onAccept(event.id, null)
                    },
                    onNo = { onReject(event.id) },
                )
            }
        }
    }
}

@Composable
private fun BudgetHeader(
    planner: PlannerState,
    budget: Double,
    onBudgetChange: (Double) -> Unit,
    onReset: () -> Unit,
) {
    val remaining = planner.remainingBudget
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (remaining < 0) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Remaining budget", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "AED ${money(remaining)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "AED ${money(planner.spent)} planned across ${planner.accepted.size} event(s)",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                OutlinedTextField(
                    value = if (budget == 0.0) "" else money(budget),
                    onValueChange = { onBudgetChange(it.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Budget") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(130.dp),
                )
            }
            if (remaining < 0) {
                Text(
                    "You are over budget. Remove something, or raise the budget.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (planner.accepted.isNotEmpty()) {
                TextButton(onClick = onReset) { Text("Start over") }
            }
        }
    }
}

@Composable
private fun FreeTimeNote(state: UiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "${state.freeSlots.size} free slots over the next two weeks",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (state.calendarConnected) {
                    "From your phone's calendar."
                } else {
                    "Your calendar is not connected yet, so every day counts as free between 09:00 and 22:00."
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun MaybeCard(
    event: Event,
    planner: PlannerState,
    today: kotlinx.datetime.LocalDate?,
    onYes: () -> Unit,
    onNo: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().alpha(MAYBE_ALPHA)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            EventSummary(event, planner, today)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onYes, modifier = Modifier.weight(1f)) { Text("I'm in") }
                OutlinedButton(onClick = onNo, modifier = Modifier.weight(1f)) { Text("Not for me") }
            }
        }
    }
}

@Composable
private fun PlannedCard(
    event: Event,
    planner: PlannerState,
    today: kotlinx.datetime.LocalDate?,
    onReject: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            EventSummary(event, planner, today)
            TextButton(onClick = { onReject(event.id) }) { Text("Remove") }
        }
    }
}

@Composable
private fun EventSummary(event: Event, planner: PlannerState, today: kotlinx.datetime.LocalDate?) {
    Text(event.name, style = MaterialTheme.typography.titleMedium)
    Text(
        listOfNotNull(
            today?.let { formatDay(event.date, it) },
            formatTime(event),
            event.venue,
            event.city,
        ).joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
    )
    val typed = planner.userPrices[event.id]
    Text(
        when {
            typed != null -> "AED ${money(typed)} (you entered this)"
            else -> formatPrice(event)
        },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun PriceDialog(event: Event, onDismiss: () -> Unit, onConfirm: (Double?) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What does it cost?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "We don't have a price for \"${event.name}\". Enter what you expect to pay so your " +
                        "budget stays accurate, or skip it.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price in AED") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(text.toDoubleOrNull()) }) { Text("Add to plan") }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(null) }) { Text("Skip") }
        },
    )
}

private fun money(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else ((value * 100).toLong() / 100.0).toString()
