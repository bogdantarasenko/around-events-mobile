package events.around.shared.domain.events

import events.around.core.model.City
import events.around.core.model.EventStatus
import events.around.core.model.MyEvent
import events.around.core.model.Recurrence
import kotlin.test.Test
import kotlin.test.assertEquals

class EventGroupsTest {
    private fun event(id: Long, status: String, start: String?, end: String? = null) = MyEvent(
        id = id,
        statusName = status,
        title = "e$id",
        city = City("krakow", "Kraków"),
        startDate = start,
        endDate = end,
    )

    @Test
    fun `events fall into the cabinet's groups`() {
        val groups = groupEvents(
            listOf(
                event(1, "review", "2026-12-01"),
                event(2, "published", "2026-12-05"),
                event(3, "queued", "2026-11-20"),
                event(4, "rejected", "2026-12-01"),
                event(5, "cancelled", "2026-12-01"),
                event(6, "published", "2026-11-01"),
                // Multi-day: still on until its last day.
                event(7, "published", "2026-11-01", end = "2026-11-10"),
                event(8, "duplicate", "2026-11-12"),
            ),
            today = "2026-11-10",
        )

        assertEquals(
            listOf(
                EventGroupKind.Review to listOf(1L),
                EventGroupKind.Soon to listOf(7L, 8L, 3L, 2L),
                EventGroupKind.Off to listOf(4L, 5L),
                EventGroupKind.Past to listOf(6L),
            ),
            groups.map { group -> group.kind to group.events.map { it.id } },
        )
    }

    @Test
    fun `filter keeps one status and drops empty groups`() {
        val groups = groupEvents(
            listOf(event(1, "review", "2026-12-01"), event(2, "published", "2026-12-05")),
            today = "2026-11-10",
            filter = EventStatus.Published,
        )

        assertEquals(listOf(EventGroupKind.Soon), groups.map { it.kind })
    }

    @Test
    fun `next dates step by the rule from today until the end`() {
        val weekly = event(1, "published", "2026-11-02")
            .copy(recurrence = Recurrence(freq = "weekly", until = "2026-11-30", first = "2026-11-02"))

        assertEquals(listOf("2026-11-16", "2026-11-23", "2026-11-30"), nextDates(weekly, today = "2026-11-10"))
        assertEquals(
            listOf("2026-11-16", "2026-11-30"),
            nextDates(weekly.copy(recurrence = weekly.recurrence!!.copy(freq = "biweekly")), today = "2026-11-10"),
        )
        assertEquals(emptyList(), nextDates(event(2, "published", "2026-11-02"), today = "2026-11-10"))
    }
}
