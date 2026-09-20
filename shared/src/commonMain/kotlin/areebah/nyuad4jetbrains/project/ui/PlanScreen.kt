package areebah.nyuad4jetbrains.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.planner.Decision
import areebah.nyuad4jetbrains.project.planner.PlannerState
import areebah.nyuad4jetbrains.project.planner.accepted
import areebah.nyuad4jetbrains.project.planner.decisionOf
import areebah.nyuad4jetbrains.project.planner.maybes
import areebah.nyuad4jetbrains.project.planner.priceOf
import areebah.nyuad4jetbrains.project.planner.remainingBudget
import areebah.nyuad4jetbrains.project.planner.spent
import kotlinproject.shared.generated.resources.Res
import kotlinproject.shared.generated.resources.view_list
import kotlinproject.shared.generated.resources.view_calendar
import kotlinproject.shared.generated.resources.legend_busy
import kotlinproject.shared.generated.resources.legend_in_plan
import kotlinproject.shared.generated.resources.legend_suggestion
import kotlinproject.shared.generated.resources.your_plan
import kotlinproject.shared.generated.resources.suggestions
import kotlinproject.shared.generated.resources.no_events_loaded
import kotlinproject.shared.generated.resources.nothing_fits
import kotlinproject.shared.generated.resources.remaining_budget
import kotlinproject.shared.generated.resources.planned_summary
import kotlinproject.shared.generated.resources.budget
import kotlinproject.shared.generated.resources.over_budget
import kotlinproject.shared.generated.resources.start_over
import kotlinproject.shared.generated.resources.free_slots_count
import kotlinproject.shared.generated.resources.free_from_calendar
import kotlinproject.shared.generated.resources.free_no_calendar
import kotlinproject.shared.generated.resources.im_in
import kotlinproject.shared.generated.resources.not_for_me
import kotlinproject.shared.generated.resources.remove
import kotlinproject.shared.generated.resources.price_dialog_title
import kotlinproject.shared.generated.resources.price_dialog_field
import kotlinproject.shared.generated.resources.price_dialog_body
import kotlinproject.shared.generated.resources.add_to_plan
import kotlinproject.shared.generated.resources.skip
import kotlinproject.shared.generated.resources.price_you_entered
import org.jetbrains.compose.resources.stringResource

/** A tentative suggestion is shown faded; once accepted it becomes solid. */
private const val MAYBE_ALPHA = 0.55f

@OptIn(ExperimentalMaterial3Api::class)
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
    var showCalendar by rememberSaveable { mutableStateOf(false) }

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

    Column(modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SegmentedButton(
                selected = !showCalendar,
                onClick = { showCalendar = false },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text(stringResource(Res.string.view_list)) }
            SegmentedButton(
                selected = showCalendar,
                onClick = { showCalendar = true },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text(stringResource(Res.string.view_calendar)) }
        }

        if (showCalendar && today != null) {
            CalendarLegend()
            CalendarView(
                planner = planner,
                freeSlots = state.freeSlots,
                busyBlocks = state.busyBlocks,
                today = today,
                onEventClick = { event ->
                    if (planner.decisionOf(event.id) != Decision.YES) {
                        if (planner.priceOf(event.id) == null) pricingEvent = event else onAccept(event.id, null)
                    }
                },
            )
        } else {
            PlanList(
                state = state,
                planner = planner,
                today = today,
                onBudgetChange = onBudgetChange,
                onAccept = onAccept,
                onReject = onReject,
                onReset = onReset,
                onNeedsPrice = { pricingEvent = it },
            )
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LegendDot(MaterialTheme.colorScheme.primary, stringResource(Res.string.legend_in_plan))
        LegendDot(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f), stringResource(Res.string.legend_suggestion))
        LegendDot(MaterialTheme.colorScheme.surfaceVariant, stringResource(Res.string.legend_busy))
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PlanList(
    state: UiState,
    planner: PlannerState,
    today: kotlinx.datetime.LocalDate?,
    onBudgetChange: (Double) -> Unit,
    onAccept: (String, Double?) -> Unit,
    onReject: (String) -> Unit,
    onReset: () -> Unit,
    onNeedsPrice: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { BudgetHeader(planner, state.budget, onBudgetChange, onReset) }
        item { FreeTimeNote(state) }

        if (planner.accepted.isNotEmpty()) {
            item { SectionTitle(stringResource(Res.string.your_plan)) }
            items(planner.accepted, key = { it.id }) { event ->
                PlannedCard(event, planner, today, onReject)
            }
        }

        item { SectionTitle(stringResource(Res.string.suggestions)) }

        val maybes = planner.maybes
        if (maybes.isEmpty()) {
            item {
                Text(
                    if (planner.events.isEmpty()) {
                        stringResource(Res.string.no_events_loaded)
                    } else {
                        stringResource(Res.string.nothing_fits)
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
                        if (planner.priceOf(event.id) == null) onNeedsPrice(event) else onAccept(event.id, null)
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
                    Text(stringResource(Res.string.remaining_budget), style = MaterialTheme.typography.labelLarge)
                    Text(
                        amountText(remaining),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(Res.string.planned_summary, amountText(planner.spent), planner.accepted.size),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                OutlinedTextField(
                    value = if (budget == 0.0) "" else money(budget),
                    onValueChange = { onBudgetChange(it.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 0.0) },
                    label = { Text(stringResource(Res.string.budget)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(130.dp),
                )
            }
            if (remaining < 0) {
                Text(
                    stringResource(Res.string.over_budget),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (planner.accepted.isNotEmpty()) {
                TextButton(onClick = onReset) { Text(stringResource(Res.string.start_over)) }
            }
        }
    }
}

@Composable
private fun FreeTimeNote(state: UiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(Res.string.free_slots_count, state.freeSlots.size),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                if (state.calendarConnected) {
                    stringResource(Res.string.free_from_calendar)
                } else {
                    stringResource(Res.string.free_no_calendar)
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
                Button(onClick = onYes, modifier = Modifier.weight(1f)) { Text(stringResource(Res.string.im_in)) }
                OutlinedButton(onClick = onNo, modifier = Modifier.weight(1f)) { Text(stringResource(Res.string.not_for_me)) }
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
            TextButton(onClick = { onReject(event.id) }) { Text(stringResource(Res.string.remove)) }
        }
    }
}

@Composable
private fun EventSummary(event: Event, planner: PlannerState, today: kotlinx.datetime.LocalDate?) {
    Text(event.name, style = MaterialTheme.typography.titleMedium)
    Text(
        listOfNotNull(
            today?.let { dayText(event.date, it) },
            timeText(event),
            event.venue,
            event.city,
        ).joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
    )
    val typed = planner.userPrices[event.id]
    Text(
        if (typed != null) stringResource(Res.string.price_you_entered, amountText(typed)) else priceText(event),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun PriceDialog(event: Event, onDismiss: () -> Unit, onConfirm: (Double?) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.price_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.price_dialog_body, event.name),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(Res.string.price_dialog_field)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(text.toDoubleOrNull()) }) { Text(stringResource(Res.string.add_to_plan)) }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(null) }) { Text(stringResource(Res.string.skip)) }
        },
    )
}

private fun money(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else ((value * 100).toLong() / 100.0).toString()
