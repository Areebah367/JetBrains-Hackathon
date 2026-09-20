package areebah.nyuad4jetbrains.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import areebah.nyuad4jetbrains.project.domain.Event
import areebah.nyuad4jetbrains.project.data.RoutineBlock
import areebah.nyuad4jetbrains.project.planner.FreeSlot
import areebah.nyuad4jetbrains.project.planner.PlannerState
import areebah.nyuad4jetbrains.project.planner.accepted
import areebah.nyuad4jetbrains.project.planner.endOf
import areebah.nyuad4jetbrains.project.planner.maybes
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus

/** The window the planner treats as usable, matching `computeFreeSlots`' defaults. */
private const val DAY_START_HOUR = 9
private const val DAY_END_HOUR = 22

private val hourHeight = 44.dp
private val dayWidth = 116.dp
private val gutterWidth = 44.dp

/**
 * A week laid out as time columns: free time shaded, accepted events solid, suggestions faded.
 *
 * This is a drawing of data the app already has — it does not read anyone's calendar.
 */
@Composable
fun CalendarView(
    planner: PlannerState,
    freeSlots: List<FreeSlot>,
    busyBlocks: List<RoutineBlock>,
    today: LocalDate,
    days: Int = 7,
    onEventClick: (Event) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val dates = (0 until days).map { today.plus(it, DateTimeUnit.DAY) }
    val accepted = planner.accepted.toSet()
    val maybes = planner.maybes.take(30).toSet()
    val horizontal = rememberScrollState()

    Column(modifier.fillMaxSize()) {
        // Day headers stay aligned with the columns by sharing the same scroll state.
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(gutterWidth))
            Row(Modifier.horizontalScroll(horizontal)) {
                dates.forEach { date ->
                    Column(
                        modifier = Modifier.width(dayWidth).padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            date.day.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (date == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        Row(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            HourGutter()
            Row(Modifier.horizontalScroll(horizontal)) {
                dates.forEach { date ->
                    DayColumn(
                        date = date,
                        freeSlots = freeSlots.filter { it.start.date == date },
                        busyBlocks = busyBlocks.filter { it.start.date == date },
                        accepted = accepted.filter { it.date == date },
                        maybes = maybes.filter { it.date == date },
                        planner = planner,
                        onEventClick = onEventClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun HourGutter() {
    Column(Modifier.width(gutterWidth)) {
        (DAY_START_HOUR until DAY_END_HOUR).forEach { hour ->
            Box(Modifier.height(hourHeight).fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Text(
                    "$hour:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun DayColumn(
    date: LocalDate,
    freeSlots: List<FreeSlot>,
    busyBlocks: List<RoutineBlock>,
    accepted: List<Event>,
    maybes: List<Event>,
    planner: PlannerState,
    onEventClick: (Event) -> Unit,
) {
    val columnHeight = hourHeight * (DAY_END_HOUR - DAY_START_HOUR)
    val gridLine = MaterialTheme.colorScheme.outlineVariant

    Box(
        Modifier
            .width(dayWidth)
            .height(columnHeight)
            .border(width = 0.5.dp, color = gridLine),
    ) {
        // Hour lines
        Column(Modifier.fillMaxSize()) {
            repeat(DAY_END_HOUR - DAY_START_HOUR) {
                Box(Modifier.height(hourHeight).fillMaxWidth().border(0.5.dp, gridLine.copy(alpha = 0.4f)))
            }
        }

        // Free time behind everything, so blocks read as sitting inside it.
        freeSlots.forEach { slot ->
            Box(
                Modifier
                    .offset(y = topOffset(slot.start))
                    .height(spanHeight(slot.start, slot.end))
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)),
            )
        }

        // Busy time on top of the free shading, so it reads as carved out of the day.
        busyBlocks.forEach { block ->
            Box(
                Modifier
                    .offset(y = topOffset(block.start))
                    .height(spanHeight(block.start, block.end))
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Text(
                    block.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
                )
            }
        }

        maybes.forEach { event ->
            EventBlock(event, planner, MaterialTheme.colorScheme.tertiary, alpha = 0.35f, onEventClick)
        }
        accepted.forEach { event ->
            EventBlock(event, planner, MaterialTheme.colorScheme.primary, alpha = 1f, onEventClick)
        }
    }
}

@Composable
private fun EventBlock(
    event: Event,
    planner: PlannerState,
    color: Color,
    alpha: Float,
    onEventClick: (Event) -> Unit,
) {
    val end = planner.endOf(event)
    Box(
        Modifier
            .offset(y = topOffset(event.start))
            .height(spanHeight(event.start, end))
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = alpha))
            .clickable { onEventClick(event) },
    ) {
        Text(
            event.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (alpha > 0.5f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
        )
    }
}

/** How far down the column a time sits. Times before the window clamp to the top. */
private fun topOffset(time: LocalDateTime): Dp {
    val minutes = (time.hour - DAY_START_HOUR) * 60 + time.minute
    return hourHeight * (minutes.coerceAtLeast(0) / 60f)
}

/** The height of a span, clamped to the visible window and never smaller than a readable block. */
private fun spanHeight(start: LocalDateTime, end: LocalDateTime): Dp {
    val startMinutes = ((start.hour - DAY_START_HOUR) * 60 + start.minute).coerceAtLeast(0)
    val endMinutes = ((end.hour - DAY_START_HOUR) * 60 + end.minute)
        .coerceAtMost((DAY_END_HOUR - DAY_START_HOUR) * 60)
    val span = (endMinutes - startMinutes).coerceAtLeast(30)
    return hourHeight * (span / 60f)
}
