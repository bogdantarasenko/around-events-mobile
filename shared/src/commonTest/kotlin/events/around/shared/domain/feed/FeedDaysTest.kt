package events.around.shared.domain.feed

import events.around.core.model.City
import events.around.core.model.Event
import events.around.core.model.Recurrence
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedDaysTest {
    private fun event(
        id: Long,
        start: String,
        end: String? = null,
        time: String? = null,
        category: String? = null,
        recurrence: Recurrence? = null,
    ) = Event(
        id = id,
        title = "e$id",
        city = City("warsaw", "Warszawa"),
        startDate = start,
        endDate = end,
        startTime = time,
        category = category,
        recurrence = recurrence,
    )

    @Test
    fun `what is already on comes first, then each day in order`() {
        val days = feedDays(
            listOf(
                event(1, "2026-11-12"),
                event(2, "2026-11-10", time = "19:00"),
                event(3, "2026-11-10", time = "12:00"),
                // Started before today and still on.
                event(4, "2026-11-01", end = "2026-11-20"),
                // Over: the listing can be a little behind the clock.
                event(5, "2026-11-01", end = "2026-11-05"),
                event(6, "2026-11-10"),
            ),
            today = "2026-11-10",
        )

        assertEquals(
            listOf(
                null to listOf(4L),
                "2026-11-10" to listOf(3L, 2L, 6L),
                "2026-11-12" to listOf(1L),
            ),
            days.map { day -> day.date to day.events.map { it.id } },
        )
        assertEquals(listOf(true, false, false), days.map { it.isOngoing })
    }

    @Test
    fun `a repeating event sits on its next date`() {
        val weekly = event(1, "2026-11-02", recurrence = Recurrence("weekly", until = "2026-12-31"))

        assertEquals(listOf("2026-11-16"), feedDays(listOf(weekly), today = "2026-11-10").map { it.date })
    }

    @Test
    fun `category filter keeps one category, and the filter offers the common ones first`() {
        val events = listOf(
            event(1, "2026-11-11", category = "music"),
            event(2, "2026-11-11", category = "sport"),
            event(3, "2026-11-12", category = "sport"),
            event(4, "2026-11-12"),
        )

        assertEquals(
            listOf(2L, 3L),
            feedDays(events, today = "2026-11-10", category = "sport").flatMap { day -> day.events.map { it.id } },
        )
        assertEquals(listOf("sport", "music"), feedCategories(events))
    }

    @Test
    fun `next dates step by the rule from today until the end`() {
        val biweekly = event(1, "2026-11-01", recurrence = Recurrence("biweekly", until = "2026-12-01"))

        assertEquals(listOf("2026-11-15", "2026-11-29"), nextDates(biweekly, today = "2026-11-10"))
        assertEquals(emptyList(), nextDates(event(2, "2026-11-01"), today = "2026-11-10"))
    }

    @Test
    fun `the upcoming shelf skips what is already on and keeps feed order`() {
        val events = listOf(
            event(1, "2026-11-01", end = "2026-11-30"),
            event(2, "2026-11-12"),
            event(3, "2026-11-10", time = "18:00"),
            event(4, "2026-11-15"),
        )

        assertEquals(listOf(3L, 2L), upcomingEvents(events, today = "2026-11-10", count = 2).map { it.id })
    }

    @Test
    fun `search needs every word, in any field, ignoring case`() {
        val events = listOf(
            event(1, "2026-11-12").copy(title = "Концерт Boombox", venue = "Progresja"),
            event(2, "2026-11-12").copy(title = "Лекция", description = "В Progresja о джазе"),
            event(3, "2026-11-12").copy(title = "Boombox: встреча"),
        )

        assertEquals(listOf(1L), searchEvents(events, "boombox  PROGRESJA").map { it.id })
        assertEquals(emptyList(), searchEvents(events, "   "))
    }
}
