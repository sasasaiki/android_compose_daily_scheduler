package app.saiki.dailyscheduler

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.saiki.dailyscheduler.WrappedCalendarEvent.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DailySchedule(
    modifier: Modifier = Modifier,
    events: List<CalendarEvent>,
    onFinishDragEvent: (CalendarEvent, DragState.Dragging) -> Unit,
    timeLabel: @Composable (LocalDateTime) -> Unit = { StandardTimeLabel(time = it) },
    eventContent: @Composable (WrappedCalendarEvent) -> Unit = { EventItem(event = it) },
) {
    val density = LocalDensity.current
    val minuteHeightDp = 2.dp
    val minuteHeightPx = with(density) {
        minuteHeightDp.roundToPx()
    }
    val hourHeightPx = minuteHeightPx * 60

    val now = LocalDateTime.now()
    val timeLabelCount = 24
    val sideBarTimeLabels = @Composable {
        repeat(timeLabelCount) { i ->
            val dateTime = LocalDateTime.of(now.year, now.month, now.dayOfMonth, i, 0)
            Box(
                modifier = Modifier.timeLabelDataModifier(dateTime)
            ) {
                timeLabel(dateTime)
            }
        }
    }

    val backGroundLines = @Composable {
        repeat(timeLabelCount) { i ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        }
    }

    var wrappedEvents by remember(events) {
        println("event$events")
        mutableStateOf(
            groupOverlappingEvents(events).flatMap { group ->
                group.mapIndexed { index, event ->
                    WrappedCalendarEvent(
                        group = Group(index = index, size = group.size),
                        data = event
                    )
                }
            }
        )
    }

    var draggingItemYOffset by remember {
        mutableFloatStateOf(0f)
    }
    val eventContents = @Composable {
        wrappedEvents.forEach { wrappedEvent ->
            Box(
                modifier = Modifier
                    .calenderEventModifier(wrappedEvent)
                    .draggable(
                        state = rememberDraggableState { delta ->
                            draggingItemYOffset += delta
                        },
                        onDragStarted = {
                            wrappedEvents = wrappedEvents.map {
                                if (it.data == wrappedEvent.data) {
                                    it.copy(
                                        dragState = DragState.Dragging(
                                            startTime = it.data.startTime,
                                            endTime = it.data.endTime
                                        )
                                    )
                                } else {
                                    it
                                }
                            }
                        },
                        onDragStopped = {
                            draggingItemYOffset = 0f
                            wrappedEvents = wrappedEvents.map {
                                if (it.dragState is DragState.Dragging) {
                                    onFinishDragEvent(it.data, it.dragState)
                                    it.copy(dragState = DragState.None)
                                } else {
                                    it
                                }
                            }
                        },
                        orientation = Orientation.Vertical
                    )
            ) {
                eventContent(wrappedEvent)
            }
        }
    }
    Layout(
        contents = listOf(
            sideBarTimeLabels,
            backGroundLines,
            eventContents
        ),
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(state = rememberScrollState()),
        measurePolicy = { (timeLabelMeasureables, backGroundLinesMeasureables, eventMeasureables), constraints ->
            val totalHeight = hourHeightPx * timeLabelMeasureables.size

            var labelMaxWidth = 0
            val timeLabelPlacablesWithDataTime: List<Pair<Placeable, LocalDateTime>> =
                timeLabelMeasureables.map { measurable ->
                    val placeable = measurable.measure(
                        constraints.copy(
                            minHeight = 0,
                            maxHeight = hourHeightPx
                        )
                    )
                    labelMaxWidth = maxOf(labelMaxWidth, placeable.width)
                    placeable to (measurable.parentData as LocalDateTime)
                }

            val linePlaceables = backGroundLinesMeasureables.map {
                it.measure(
                    constraints.copy(
                        minHeight = 0,
                        maxHeight = hourHeightPx
                    )
                )
            }

            val eventPlaceablesWithEvent = eventMeasureables.map { measurable ->
                val event = measurable.parentData as WrappedCalendarEvent
                val eventDurationMinutes =
                    ChronoUnit.MINUTES.between(event.data.startTime, event.data.endTime)
                val eventHeight = (eventDurationMinutes * minuteHeightPx).toInt()
                val eventWidth = (constraints.maxWidth - labelMaxWidth) / event.group.size
                measurable.measure(
                    constraints.copy(
                        minWidth = eventWidth,
                        maxWidth = eventWidth,
                        minHeight = eventHeight,
                        maxHeight = eventHeight
                    )
                ) to event
            }

            layout(constraints.maxWidth, totalHeight) {
                val dataTimeYMap = hashMapOf<LocalDateTime, Int>()
                timeLabelPlacablesWithDataTime.forEachIndexed { index, (placeable, dateTime) ->
                    val y = hourHeightPx * index
                    placeable.place(
                        x = 0,
                        y = y,
                    )
                    linePlaceables[index].place(
                        x = 0,
                        y = y,
                    )
                    dataTimeYMap[dateTime] = y
                }

                eventPlaceablesWithEvent.forEach { (placeable, event) ->
                    val (y, z) = if (event.dragState is DragState.None) {
                        dataTimeYMap.getOrDefault(
                            event.data.startTime.getZeroMinuteLocalDateTime(),
                            0
                        ) + event.data.startTime.minute * minuteHeightPx to 0f
                    } else {
                        val offsetMinute = draggingItemYOffset / (minuteHeightPx.toFloat())
                        val draggingStartTime =
                            event.data.startTime.plusMinutes(offsetMinute.toLong())
                        val targetHourY = dataTimeYMap.getOrDefault(
                            draggingStartTime.getZeroMinuteLocalDateTime(),
                            0
                        )
                        val closestTargetMinute = findClosestFiveMinute(draggingStartTime)

                        wrappedEvents = wrappedEvents.map {
                            if (it.data == event.data) {
                                val targetStartTime =
                                    draggingStartTime
                                        .getZeroMinuteLocalDateTime()
                                        .plusMinutes(closestTargetMinute.toLong())
                                val targetEndTime = targetStartTime.plusMinutes(
                                    ChronoUnit.MINUTES.between(
                                        event.data.startTime,
                                        event.data.endTime
                                    )
                                )
                                it.copy(
                                    dragState = DragState.Dragging(
                                        startTime = targetStartTime,
                                        endTime = targetEndTime,
                                    )
                                )
                            } else {
                                it
                            }
                        }

                        targetHourY + closestTargetMinute * minuteHeightPx to 1f
                    }
                    placeable.place(
                        x = labelMaxWidth + (placeable.width * event.group.index),
                        y = y,
                        zIndex = z,
                    )
                }
            }
        }

    )
}


private fun findClosestFiveMinute(dateTime: LocalDateTime): Int {
    val minute = dateTime.minute
    val tickMinutes = 15
    val remainder = minute % tickMinutes
    return if (remainder < tickMinutes / 2 + 1) {
        minute - remainder
    } else {
        minute + (tickMinutes - remainder)
    }
}

private fun LocalDateTime.getZeroMinuteLocalDateTime(): LocalDateTime =
    LocalDateTime.of(
        this.year,
        this.month,
        this.dayOfMonth,
        this.hour,
        0
    )

// なし
//  hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 0, maxHeight = 2400)

//.verticalScroll(state = rememberScrollState()),
// hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 0, maxHeight = Infinity)

// .fillMaxHeight()
//  hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 2400, maxHeight = 2400)

//.fillMaxHeight()
//.verticalScroll(state = rememberScrollState()),
//hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 2400, maxHeight = Infinity)

// Parent Data Modifier の作成
fun Modifier.calenderEventModifier(event: WrappedCalendarEvent) = this.then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any = event
    }
)

// Parent Data Modifier の作成
fun Modifier.timeLabelDataModifier(date: LocalDateTime) = this.then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any = date
    }
)


fun groupOverlappingEvents(events: List<CalendarEvent>): List<List<CalendarEvent>> {
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


@Composable
fun EventItem(
    modifier: Modifier = Modifier,
    event: WrappedCalendarEvent
) {
    Card(
        modifier = modifier
            .fillMaxSize(),
        colors = CardDefaults
            .cardColors()
            .copy(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = if (event.dragState is DragState.Dragging)
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.5f
                    )
                else
                    MaterialTheme.colorScheme.primary
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)),
    ) {
        Column(modifier = modifier.padding(8.dp)) {
            val (startTime, endTime) = if (event.dragState is DragState.Dragging) {
                event.dragState.startTime to event.dragState.endTime
            } else {
                event.data.startTime to event.data.endTime
            }
            Text(
                text = "${
                    startTime.format(EventTimeFormatter)
                } - ${
                    endTime.format(EventTimeFormatter)
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
fun StandardTimeLabel(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = Modifier
//            .fillMaxHeight()
//            .fillMaxWidth()
//            .padding(4.dp)
            .background(Color.Cyan),
        text = time.format(HourLabelFormatter),
    )
}

private val EventTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val HourLabelFormatter = DateTimeFormatter.ofPattern("HH:mm")


data class WrappedCalendarEvent(
    val group: Group,
    val dragState: DragState = DragState.None,
    val data: CalendarEvent
) {
    data class CalendarEvent(
        val id: String,
        val title: String,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime
    )

    data class Group(
        val size: Int,
        val index: Int,
    )

    sealed class DragState {
        data object None : DragState()
        data class Dragging(
            val startTime: LocalDateTime,
            val endTime: LocalDateTime
        ) : DragState()
    }
}

//@Preview
//@Composable
//private fun PreviewDaily() {
//    DailySchedule(
//        events = listOf(
//            CalendarEvent(
//                id = "",
//                title = "Event Title",
//                startTime = LocalDateTime.now(),
//                endTime = LocalDateTime.now().plusHours(1)
//            )
//        )
//
//    )
//}

@Preview
@Composable
private fun PreviewEvent() {
    EventItem(
        event = WrappedCalendarEvent(
            group = Group(0, 0),
            data = CalendarEvent(
                id = "",
                title = "Event Title",
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1)
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewTimeLabel() {

    StandardTimeLabel(LocalDateTime.now())
}