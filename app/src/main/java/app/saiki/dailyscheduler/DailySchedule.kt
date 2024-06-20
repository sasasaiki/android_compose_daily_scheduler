package app.saiki.dailyscheduler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun DailySchedule(
    modifier: Modifier = Modifier,
    targetDate: LocalDateTime = LocalDateTime.now(),
    events: List<CalendarEvent>,
    eventContent: @Composable (CalendarEvent) -> Unit = { event ->
        EventItem(event = event)
    }
) {
    val hourHeight = 60.dp


    Column(modifier = Modifier.fillMaxHeight()) {
        Text(text = "hoge")
    }

    val sideBarTimeLabels = @Composable {
        repeat(24) { i ->
            val dateTime = LocalDateTime.of(
                targetDate.year,
                targetDate.month,
                targetDate.dayOfMonth,
                i,
                0
            )
            Box(
                modifier = Modifier.timeLabelDataModifier(
                    dateTime
                )
            ) {
                SidebarTimeLabel(dateTime)
            }
        }
    }
    val calenderEvents = @Composable {
        events.sortedBy { it.startTime }.forEach { event ->
            // EventItem固定でいいならBoxなしで直接つけても動く
            Box(modifier = Modifier.calenderEventModifier(event)) {
                eventContent(event)
            }
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
            .background(MaterialTheme.colorScheme.surface),
    ) { (timeLabelMeasureables, eventMeasureables), constraints ->
        println("saiki 呼ばれてる？")
        val totalHeight = hourHeight.roundToPx() * timeLabelMeasureables.size
        var offsetY = 0
        val yPositionMap = mutableMapOf<LocalDateTime, Int>()
        var labelMaxWidth = 0
        val placablesTimeLabel = timeLabelMeasureables.map { measurable ->
            yPositionMap[(measurable.parentData as LocalDateTime)] = offsetY
            offsetY += hourHeight.roundToPx()

            val placeable = measurable.measure(
                constraints.copy(
                    minHeight = hourHeight.roundToPx(),
                    maxHeight = hourHeight.roundToPx()
                )
            )

            labelMaxWidth = maxOf(labelMaxWidth, placeable.width)

            placeable
        }

        val placeablesWithEvents = eventMeasureables.map { measurable ->
            val event = measurable.parentData as CalendarEvent
            val eventDurationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
            val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
            println("saiki ====================================")
            println("saiki eventStartTime: ${event.startTime}")
            println("saiki eventEndTime: ${event.endTime}")
            println("saiki eventHeight: $eventHeight")
            println("saiki =======================================")
            val placeable = measurable.measure(
                constraints.copy(
                    maxWidth = constraints.maxWidth - labelMaxWidth,
                    minHeight = eventHeight,
                    maxHeight = eventHeight
                )
            )
            println("saiki placeable.height: ${placeable.height}")
            Pair(placeable, event)
        }
        layout(constraints.maxWidth, totalHeight) {
            placablesTimeLabel.forEachIndexed { index, placeable ->
                placeable.place(0, hourHeight.roundToPx() * index)
            }
            placeablesWithEvents.forEach { (placeable, event) ->
                val eventOffsetMinutes =
                    ChronoUnit.MINUTES.between(LocalTime.MIN, event.startTime.toLocalTime())
                val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
                placeable.place(labelMaxWidth, eventY)
            }
        }
    }
}


@Composable
fun EventItem(
    modifier: Modifier = Modifier,
    event: CalendarEvent
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize(), // ここをMaxにしないとlayoutでどう測ってもwrapになってしまうので注意
        colors = CardDefaults
            .cardColors()
            .copy(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary
            )
    ) {
        Column(
            modifier = modifier
                .padding(8.dp)
        ) {
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
        modifier = Modifier
            .padding(4.dp),
        text = time.format(HourLabelFormatter),
    )
}

private val EventTimeFormatter = DateTimeFormatter.ofPattern("d:HH:mm")
private val HourLabelFormatter = DateTimeFormatter.ofPattern("HH:mm")

// Parent Data の定義
class TimeLabelDataModifier(val date: LocalDateTime) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return date
    }
}

// Parent Data Modifier の作成
fun Modifier.timeLabelDataModifier(date: LocalDateTime) = this.then(
    TimeLabelDataModifier(date)
)


// Parent Data Modifier の作成
fun Modifier.calenderEventModifier(event: CalendarEvent) = this.then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any = event
    }
)

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