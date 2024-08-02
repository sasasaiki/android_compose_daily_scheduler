package app.saiki.dailyscheduler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DailySchedule(
    modifier: Modifier = Modifier,
    timeLabel: @Composable (LocalDateTime) -> Unit = { StandardTimeLabel(time = it) },
    events: List<CalendarEvent>
) {
    val density = LocalDensity.current
    val minuteHeightDp = 2.dp
    val minuteHeightPx = with(density) {
        minuteHeightDp.roundToPx()
    }
    val hourHeightPx = minuteHeightPx * 60


    Column(modifier = Modifier.fillMaxHeight()) {
        Text(text = "hoge")
    }

    val now = LocalDateTime.now()
    val sideBarTimeLabels = @Composable {
        repeat(24) { i ->
            timeLabel(LocalDateTime.of(now.year, now.month, now.dayOfMonth, i, 0))
        }

    }

    Layout(
        content = sideBarTimeLabels,
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(state = rememberScrollState()),
        measurePolicy = { timeLabelMeasureables, constraints ->
            val totalHeight = hourHeightPx * timeLabelMeasureables.size

            println("hogehoge $constraints")
            val timeLabelPlacables: List<Placeable> = timeLabelMeasureables.map { measurable ->
                measurable.measure(
                    constraints.copy(
                        minHeight = 0,
                        maxHeight = hourHeightPx
                    )
                )
            }

            timeLabelPlacables.forEach {
                println("hogehoge height ${it.height} measruedheight${it.measuredHeight}")
            }


            layout(constraints.maxWidth, totalHeight) {
                timeLabelPlacables.forEachIndexed { index, placeable ->
                    placeable.place(
                        x = 0,
                        y = hourHeightPx * index,
                        zIndex = 0f
                    )
                }
            }
        }

    )
}

// なし
//  hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 0, maxHeight = 2400)

//.verticalScroll(state = rememberScrollState()),
// hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 0, maxHeight = Infinity)

// .fillMaxHeight()
//  hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 2400, maxHeight = 2400)

//.fillMaxHeight()
//.verticalScroll(state = rememberScrollState()),
//hogehoge Constraints(minWidth = 0, maxWidth = 1080, minHeight = 2400, maxHeight = Infinity)


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

    StandardTimeLabel(LocalDateTime.now())
}