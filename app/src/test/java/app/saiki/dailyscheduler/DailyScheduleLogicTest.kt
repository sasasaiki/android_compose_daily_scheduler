package app.saiki.dailyscheduler

import org.junit.Test
import java.time.LocalDateTime

class DailyScheduleLogicTest {
    @Test
    fun test() {
        // arrange
        val targetList = createDummyEvent(LocalDateTime.now())

        // act
        val result = groupOverlappingEvents(targetList)

        // assert
        result.forEach {
            println(it)
        }
    }
}


//fun groupOverlappingEvents(events: List<CalendarEvent>): List<List<CalendarEvent>> {
//    val sortedEvents = events.sortedBy { it.startTime }
//    val groupedEvents = mutableListOf<List<CalendarEvent>>()
//    var currentGroup = mutableListOf<CalendarEvent>()
//
//    for (event in sortedEvents) {
//        // 最初の一回
//        if (currentGroup.isEmpty()) {
//            currentGroup = mutableListOf(event)
//            continue
//        }
//
//        if (currentGroup.last().endTime.isBefore(event.startTime)) {
//            // 新しいグループを開始
//            if (currentGroup.isNotEmpty()) {
//                groupedEvents.add(currentGroup)
//            }
//        } else {
//            // 現在のグループに追加
//            currentGroup.add(event)
//        }
//    }
//
//    if (currentGroup.isNotEmpty()) {
//        groupedEvents.add(currentGroup)
//    }
//
//    return groupedEvents
//}