package app.saiki.dailyscheduler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DailySchedule(
    modifier: Modifier = Modifier,
    events: List<CalendarEvent>
) {
    val hourHeight = 60.dp


    Column(modifier = Modifier.fillMaxHeight()) {
        Text(text = "hoge")
    }

    val now = LocalDateTime.now()
    val sideBarTimeLabels = @Composable {
        repeat(24) { i ->
            SidebarTimeLabel(LocalDateTime.of(now.year, now.month, now.dayOfMonth, i, 0))
        }
    }
    val calenderEvents = @Composable {
        events.sortedBy { it.startTime }.forEach { event ->
            EventItem(event = event)
        }
    }

    Layout(
        contents = listOf(
            sideBarTimeLabels,
            calenderEvents,
        ),
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(state = rememberScrollState())
            .background(MaterialTheme.colorScheme.secondary),
    ) { (timeLabelMeasureables, eventMeasureables), constraints ->
        val totalHeight = hourHeight.roundToPx() * 24
        val placablesTimeLabel = timeLabelMeasureables.map { measurable ->
            measurable.measure(
                constraints.copy(
                    minHeight = hourHeight.roundToPx(),
                    maxHeight = hourHeight.roundToPx()
                )
            )
        }
//
//        val placeablesWithEvents = measureables.map { measurable ->
//            val event = measurable.parentData as Event
//            val eventDurationMinutes = ChronoUnit.MINUTES.between(event.start, event.end)
//            val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
//            val placeable = measurable.measure(
//                constraints.copy(
//                    minHeight = eventHeight,
//                    maxHeight = eventHeight
//                )
//            )
//            Pair(placeable, event)
//        }
        layout(constraints.maxWidth, totalHeight) {
            placablesTimeLabel.forEachIndexed { index, placeable ->
                placeable.place(0, hourHeight.roundToPx() * index)
            }
//            placeablesWithEvents.forEach { (placeable, event) ->
//                val eventOffsetMinutes =
//                    ChronoUnit.MINUTES.between(LocalTime.MIN, event.start.toLocalTime())
//                val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
//                placeable.place(0, eventY)
//            }
        }
    }
}


@Composable
fun EventItem(
    modifier: Modifier = Modifier,
    event: CalendarEvent
) {
    Card(
        colors = CardDefaults
            .cardColors()
            .copy(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary
            ),
    ) {
        Column(modifier = modifier.padding(8.dp)) {
            Text(
                text = "${event.startTime.format(EventTimeFormatter)} - ${
                    event.endTime.format(
                        EventTimeFormatter
                    )
                }",
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = "Event Title",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun SidebarTimeLabel(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = Modifier.padding(4.dp),
        text = time.format(HourLabelFormatter),
    )
}

private val EventTimeFormatter = DateTimeFormatter.ofPattern("d:HH:mm")
private val HourLabelFormatter = DateTimeFormatter.ofPattern("HH:mm")


data class CalendarEvent(
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

@Preview
@Composable
private fun PreviewDaily() {
    DailySchedule(
        events = listOf(
            CalendarEvent(
                title = "Event Title",
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1)
            )
        )
    )
}

@Preview
@Composable
private fun PreviewEvent() {
    EventItem(
        event = CalendarEvent(
            title = "Event Title",
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewTimeLabel() {
    SidebarTimeLabel(LocalDateTime.now())
}