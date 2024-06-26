package app.saiki.dailyscheduler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    events: List<PrimitiveCalenderEvent>,
    eventContent: @Composable (PrimitiveCalenderEvent) -> Unit = { StandardEventItem(event = it) }
) {
    println("saiki composition 呼ばれすぎてない？")

    val minuteHeight = 2.dp
//    val hourHeight = minuteHeight*60 これをやるとroundToPixelした時にずれる
    val targetHoursCount = 24
    val overlappingOffsetX = 40.dp // 重なりがある場合にどれくらいずらすか

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
                StandardSidebarTimeLabel(dateTime)
            }
        }
    }

    var currentDraggingEvent: PrimitiveCalenderEvent? by remember {
        mutableStateOf(null)
    }
    var yOffset: Float by remember {
        mutableFloatStateOf(0f)
    }


    val calenderEvents = @Composable {
        val groupedEvent = groupOverlappingEvents(events)
        val eventsWithOverlappingIndex = groupedEvent.flatMap { groupe ->
            groupe.mapIndexed { index, event ->
                CalendarEventWithOverlappingIndex(
                    group = CalendarEventWithOverlappingIndex.Group(index, groupe.size),
                    isDragging = false,
                    event = event
                )
            }
        }

        eventsWithOverlappingIndex.forEach { wrappedEvent ->
            Box(
                modifier = Modifier
                    .calenderEventModifier(wrappedEvent)
                    .draggable(
                        state = rememberDraggableState { delta ->
                            yOffset += delta
                        },
                        onDragStarted = {
                            currentDraggingEvent = wrappedEvent.event// TODO id持たせる
                        },
                        onDragStopped = {
                            currentDraggingEvent = null
                            yOffset = 0f
                        },
                        orientation = Vertical
                    )
            ) {
                eventContent(wrappedEvent.event)
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
        val fifteenMinutePx = minuteHeight.roundToPx() * 15

        println("saiki measure 呼ばれすぎてない？")
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
                    maxWidth = constraints.maxWidth - labelMaxWidth - overlappingOffsetX.roundToPx() * event.group.index - (event.group.size - 1 - event.group.index) * overlappingOffsetX.roundToPx(),
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
                val timePosY =
                    timeLabelYPositionMap[getZeroMinuteLocalDateTime(event.event.startTime)] ?: 0

                val eventY = timePosY + event.event.startTime.minute * minuteHeight.roundToPx()

                val (dragOffsetY, zIndex) = if (currentDraggingEvent == event.event) {
                    yOffset to 1f
                } else {
                    0f to 0f
                }

                val lastOffsetY = if (dragOffsetY != 0f) {
                    val offsetMinutes = dragOffsetY / minuteHeight.roundToPx()
                    val targetTime = event.event.startTime.plusMinutes(offsetMinutes.toLong())
                    val timeY = timeLabelYPositionMap[getZeroMinuteLocalDateTime(targetTime)] ?: 0
                    findClosestFiveMinute(targetTime) * minuteHeight.roundToPx() + timeY
                } else {
                    eventY
                }

                placeable.place(
                    x = labelMaxWidth + overlappingOffsetX.roundToPx() * event.group.index,
                    y = lastOffsetY,//eventY + dragOffsetY.roundToInt(),
                    zIndex = zIndex
                )
            }
        }
    }
}

private fun getZeroMinuteLocalDateTime(time: LocalDateTime): LocalDateTime? =
    LocalDateTime.of(
        time.year,
        time.month,
        time.dayOfMonth,
        time.hour,
        0
    )

private fun findClosestFiveMinute(dateTime: LocalDateTime): Int {
    val minute = dateTime.minute
    val tickMinutes = 5
    val remainder = minute % tickMinutes
    return if (remainder < tickMinutes / 2 + 1) {
        minute - remainder
    } else {
        minute + (tickMinutes - remainder)
    }
}

@Composable
fun StandardSidebarTimeLabel(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .padding(4.dp),
        text = time.format(HourLabelFormatter),
    )
}

val EventTimeFormatter = DateTimeFormatter.ofPattern("d:HH:mm")
val HourLabelFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
    val group: Group,
    val isDragging: Boolean,
    val event: PrimitiveCalenderEvent,
) {
    data class Group(
        val index: Int,
        val size: Int
    )
}

interface PrimitiveCalenderEvent {
    val id: EventId
    val startTime: LocalDateTime
    val endTime: LocalDateTime
}

@JvmInline
value class EventId(val value: String)


@Composable
private fun StandardEventItem(
    modifier: Modifier = Modifier,
    event: PrimitiveCalenderEvent
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
                text = event.id.value,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

fun groupOverlappingEvents(events: List<PrimitiveCalenderEvent>): List<List<PrimitiveCalenderEvent>> {
    if (events.isEmpty()) return emptyList()

    // イベントを開始時間でソートする
    val sortedEvents = events.sortedBy { it.startTime }

    val groupedEvents = mutableListOf<MutableList<PrimitiveCalenderEvent>>()
    var currentGroup = mutableListOf<PrimitiveCalenderEvent>()
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
    StandardEventItem(
        event = object : PrimitiveCalenderEvent {
            override val id = EventId("0")
            override val startTime = LocalDateTime.now()
            override val endTime = LocalDateTime.now().plusHours(1)
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewTimeLabel() {
    StandardSidebarTimeLabel(LocalDateTime.now())
}