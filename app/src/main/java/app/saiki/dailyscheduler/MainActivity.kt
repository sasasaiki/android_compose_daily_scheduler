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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.saiki.dailyscheduler.ui.theme.DailySchedulerComposeTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val targetDateTime = LocalDateTime.now()
        setContent {
            DailySchedulerComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DailySchedule(
                        targetDate = targetDateTime,
                        events = createDummyEvent(targetDateTime),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

fun createDummyEvent(targetDateTime: LocalDateTime): List<CalendarEvent> {
    return listOf(
        CalendarEvent(
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
                13,
                0
            ),
            title = "Meeting",
        ), CalendarEvent(
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