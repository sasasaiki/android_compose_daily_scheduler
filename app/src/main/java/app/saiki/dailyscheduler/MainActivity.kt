package app.saiki.dailyscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.saiki.dailyscheduler.WrappedCalendarEvent.*
import app.saiki.dailyscheduler.ui.theme.DailySchedulerComposeTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            var list by remember {
                mutableStateOf(createDummyEvents())
            }
            DailySchedulerComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DailySchedule(
                        events = list,
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
                        },
                    )
//                    SampleModifierLayout("Android", modifier = Modifier.padding(innerPadding))
//                    SampleMeasuredHeight(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// contentに何個要素が入っていても、contentを一つのmeassurableとして扱う
@Composable
fun SampleModifierLayout(name: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.End,
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .layout { measurable, constraints ->
                constraints.hasBoundedHeight
                val placeable = measurable.measure(constraints.copy(
                    minHeight = constraints.maxHeight,
                    maxHeight = constraints.maxHeight,
                ))
                val width = placeable.width
                val height = placeable.height
                val size = maxOf(width, height)
                layout(size, size) {
                    placeable.placeRelative(0, 0)
                }
            }
    ) {
        Text(
            text = "1",
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = "22222222222",
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}


//@Composable
//fun SampleMeasuredHeight(modifier: Modifier = Modifier) {
//    Layout(
//        modifier = Modifier
//            .size(100.dp)
//            .background(MaterialTheme.colorScheme.primary),
//        content = @Composable {
//            Text(
//                modifier = Modifier.size(300.dp),
//                text = "hoge"
//            )
//        }) { m, c ->
//        println("oya $c")
//        val p = m[0].measure(c.copy(maxWidth = 600))
//        println("ko ${p.height} ${p.measuredHeight}")
//
//
//        layout(300, 300) {
//            p.placeRelative(0, 0)
//        }
//    }
//}
fun createDummyEvents(): List<CalendarEvent> {
    return List(365) {
        createDummyEvent(LocalDateTime.now().plusDays(it.toLong()), it)
    }.flatten()
}

fun createDummyEvent(targetDateTime: LocalDateTime, index: Int = 0): List<CalendarEvent> {
    return listOf(
        CalendarEvent(
            id = "0",
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
            id = "1",
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
            id = "2",
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
            id = "3",
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
                11,
                45
            ),
            title = "Meeting",
        ),

        CalendarEvent(
            id = "4",
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
            id = "5",
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
                16,
                0
            ),
            title = "Meeting",
        ),
            CalendarEvent(
                id = "6",
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DailySchedulerComposeTheme {
        SampleModifierLayout("Android")
    }
}