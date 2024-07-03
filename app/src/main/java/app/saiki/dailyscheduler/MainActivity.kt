package app.saiki.dailyscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.saiki.dailyscheduler.ui.theme.DailySchedulerComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val targetDateTime = LocalDateTime.now()

        setContent {
            DailySchedulerComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val scope = rememberCoroutineScope()

                    var list by remember {
                        mutableStateOf(
//                            createDummyEvent(targetDateTime)
                            createDummyEvents()
                        )
                    }
                    DailySchedule(
                        targetDate = targetDateTime,
                        events = list,
                        daysCount = 365,
                        modifier = Modifier.padding(innerPadding),
                        onFinishDragEvent = { event, targetTime ->
                            list = list.map {
                                if (event == it) {
                                    it.copy(
                                        startTime = targetTime.startTime,
                                        endTime = targetTime.endTime
                                    )
                                } else {
                                    it
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// これはよその持ち物
data class CalendarEvent(
    val title: String,
    override val id: EventId,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime
) : PrimitiveCalenderEvent

fun createDummyEvents(): List<CalendarEvent> {
    return List(365) {
        createDummyEvent(LocalDateTime.now().plusDays(it.toLong()), it)
    }.flatten()
}

fun createDummyEvent(targetDateTime: LocalDateTime, index: Int = 0): List<CalendarEvent> {
    return listOf(
        CalendarEvent(
            id = EventId("0"),
            startTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                10,
                0
            ),
            endTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                11,
                0
            ),
            title = "Meeting",
        ),
        CalendarEvent(
            id = EventId("1"),
            startTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                10,
                0
            ),
            endTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                11,
                0
            ),
            title = "完全に重なる",
        ),

        CalendarEvent(
            id = EventId("2"),
            startTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                10,
                30
            ),
            endTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                11,
                0
            ),
            title = "ちょっと重なる",
        ),
        CalendarEvent(
            id = EventId("3"),
            startTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                11,
                0
            ),
            endTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                12,
                0
            ),
            title = "Meeting",
        ),

        CalendarEvent(
            id = EventId("4"),
            startTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                12,
                0
            ),
            endTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                12,
                30
            ),
            title = "Meeting",
        ),
        CalendarEvent(
            id = EventId("5"),
            startTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                13,
                0
            ),
            endTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                14,
                0
            ),
            title = "Meeting",
        ),
        CalendarEvent(
            id = EventId("6"),
            startTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                15,
                0
            ),
            endTime = LocalDateTime.of(
                targetDateTime.year,
                targetDateTime.month,
                targetDateTime.dayOfMonth,
                17,
                15
            ),
            title = "Meeting",
        )
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DailySchedulerComposeTheme {
        Greeting("Android")
    }
}