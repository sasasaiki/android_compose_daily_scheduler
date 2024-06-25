package app.saiki.dailyscheduler

import androidx.compose.foundation.BorderStroke
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DailySchedule(
    modifier: Modifier = Modifier,
    targetDate: LocalDateTime = LocalDateTime.now(),
    events: List<CalendarEvent>,
    eventContent: @Composable (CalendarEvent) -> Unit = { event ->
        EventItem(event = event)
    }
) {
    val minuteHeight = 1.dp
//    val hourHeight = minuteHeight*60 これをやるとroundToPixelした時にずれる
    val targetHoursCount = 24
    val overlappingOffsetX = 40.dp

    Column(modifier = Modifier.fillMaxHeight()) {
        Text(text = "hoge")
    }

    val sideBarTimeLabels = @Composable {
        repeat(targetHoursCount) { i ->
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
        val groupedEvent = groupOverlappingEvents(events)
        val eventsWithOverlappingIndex = groupedEvent.flatMap { groupe ->
            groupe.mapIndexed { index, event ->
                CalendarEventWithOverlappingIndex(
                    index, groupe.size, event
                )
            }
        }

        eventsWithOverlappingIndex.forEach { event ->
            Box(
                modifier = Modifier.calenderEventModifier(event)
            ) {
                eventContent(event.event)
            }

        }
    }

    val backGroundLines = @Composable {
        repeat(targetHoursCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            )
        }
    }

    Layout(
        contents = listOf(
            backGroundLines,
            sideBarTimeLabels,
            calenderEvents,
        ),
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(state = rememberScrollState())
            .background(MaterialTheme.colorScheme.surface),
    ) { (backGroundLineMeasureables, timeLabelMeasureables, eventMeasureables), constraints ->
        val hourHeightPx = minuteHeight.roundToPx() * 60

        println("saiki 呼ばれてる？")
        val totalHeight = hourHeightPx * timeLabelMeasureables.size
        var offsetY = 0
        val timeLabelYPositionMap = mutableMapOf<LocalDateTime, Int>()
        var labelMaxWidth = 0

        val placeablesLine = backGroundLineMeasureables.map {
            val placeable = it.measure(constraints.copy(minHeight = 1, maxHeight = 1))
            placeable
        }

        val placablesTimeLabelWithDate = timeLabelMeasureables.map { measurable ->
            val localDateTime = measurable.parentData as LocalDateTime
            timeLabelYPositionMap[localDateTime] = offsetY
            offsetY += hourHeightPx

            val placeable = measurable.measure(
                constraints
            )

            labelMaxWidth = maxOf(labelMaxWidth, placeable.width)

            localDateTime to placeable
        }
        println("saiki ----------------------------------------")
        println("saiki minuteHeight: ${minuteHeight.roundToPx()}")
        val placeablesWithEvents = eventMeasureables.map { measurable ->
            val event = measurable.parentData as CalendarEventWithOverlappingIndex
            val eventDurationMinutes =
                ChronoUnit.MINUTES.between(event.event.startTime, event.event.endTime)
            val eventHeight = (eventDurationMinutes * minuteHeight.roundToPx()).toInt()
            println("saiki ====================================")
            println("saiki eventStartTime: ${event.event.startTime}")
            println("saiki eventEndTime: ${event.event.endTime}")
            println("saiki eventHeight: $eventHeight")
            println("saiki eventDurationMinutes: $eventDurationMinutes")
            println("saiki =======================================")
            val placeable = measurable.measure(
                constraints.copy(
                    maxWidth = constraints.maxWidth - labelMaxWidth - overlappingOffsetX.roundToPx() * event.index - (event.groupCount - event.index) * overlappingOffsetX.roundToPx(),
                    minHeight = eventHeight,
                    maxHeight = eventHeight
                )
            )
            println("saiki placeable.height: ${placeable.height}")
            Pair(placeable, event)
        }
        layout(constraints.maxWidth, totalHeight) {
            placablesTimeLabelWithDate.forEachIndexed { index, (dateTime, placeable) ->
                val offSetY = timeLabelYPositionMap[dateTime] ?: 0
                placeable.place(0, offSetY)
                placeablesLine[index].place(0, offSetY)
            }
            placeablesWithEvents.forEach { (placeable, event) ->
                val timePosY = timeLabelYPositionMap[LocalDateTime.of(
                    event.event.startTime.year,
                    event.event.startTime.month,
                    event.event.startTime.dayOfMonth,
                    event.event.startTime.hour,
                    0
                )] ?: 0

                println("saiki eventStartTime: ${event.event.startTime}")
                val eventY = timePosY + event.event.startTime.minute * minuteHeight.roundToPx()
                println("saiki eventY: ${eventY}")
                placeable.place(
                    labelMaxWidth + overlappingOffsetX.roundToPx() * event.index,
                    eventY
                )
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
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f))
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
fun Modifier.calenderEventModifier(event: CalendarEventWithOverlappingIndex) = this.then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any = event
    }
)

data class CalendarEventWithOverlappingIndex(
    val index: Int,
    val groupCount: Int,
    val event: CalendarEvent,
)

data class CalendarEvent(
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

fun groupOverlappingEvents(events: List<CalendarEvent>): List<List<CalendarEvent>> {
    if (events.isEmpty()) return emptyList()

    // イベントを開始時間でソートする
    val sortedEvents = events.sortedBy { it.startTime }

    val groupedEvents = mutableListOf<MutableList<CalendarEvent>>()
    var currentGroup = mutableListOf<CalendarEvent>()
    currentGroup.add(sortedEvents[0])

    for (i in 1 until sortedEvents.size) {
        val currentEvent = sortedEvents[i]
        val lastEventInGroup = currentGroup.last()

        if (currentEvent.startTime < lastEventInGroup.endTime) {
            // イベントが重なっている場合、現在のグループに追加
            currentGroup.add(currentEvent)
        } else {
            // 重ならない場合、新しいグループを作成
            groupedEvents.add(currentGroup)
            currentGroup = mutableListOf()
            currentGroup.add(currentEvent)
        }
    }

    // 最後のグループを追加
    groupedEvents.add(currentGroup)

    return groupedEvents
}


@Preview
@Composable
private fun PreviewDaily() {
    DailySchedule(
        events =
        createDummyEvent(
            LocalDateTime.now()
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