package events.around.shared.domain.events

import events.around.core.model.EventStatus
import events.around.core.model.MyEvent
import events.around.core.model.Repeat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

enum class EventGroupKind {
    Review,
    Soon,
    Off,
    Past,
}

data class EventGroup(
    val kind: EventGroupKind,
    val events: List<MyEvent>,
)

/**
 * The cabinet's groups, as in the mini-app: in review; upcoming; not listed (rejected, cancelled); past.
 * A duplicate counts as live — it is in the listing, just as another card. [today] is `yyyy-MM-dd`.
 */
fun groupEvents(events: List<MyEvent>, today: String, filter: EventStatus? = null): List<EventGroup> {
    val shown = if (filter == null) events else events.filter { it.status == filter }
    fun live(event: MyEvent) = event.status in LIVE

    // No date at all reads as upcoming: it hasn't happened as far as anyone knows.
    fun upcoming(event: MyEvent) = event.lastDay.let { it == null || it >= today }

    return listOf(
        EventGroup(EventGroupKind.Review, shown.filter { it.status == EventStatus.Review }),
        EventGroup(
            EventGroupKind.Soon,
            shown.filter { live(it) && upcoming(it) }.sortedBy { it.startDate.orEmpty() + it.startTime.orEmpty() },
        ),
        EventGroup(
            EventGroupKind.Off,
            shown.filter { it.status == EventStatus.Rejected || it.status == EventStatus.Cancelled },
        ),
        EventGroup(EventGroupKind.Past, shown.filter { live(it) && !upcoming(it) }),
    ).filter { it.events.isNotEmpty() }
}

private val LIVE = setOf(EventStatus.Published, EventStatus.Queued, EventStatus.Duplicate)

/** The next [count] dates of a repeating event, from [today] on; empty for a one-off. */
fun nextDates(event: MyEvent, today: String, count: Int = 3): List<String> {
    val recurrence = event.recurrence ?: return emptyList()
    val step = when (Repeat.fromApi(recurrence.freq)) {
        Repeat.Weekly -> 7
        Repeat.Biweekly -> 14
        null -> return emptyList()
    }
    val first = (recurrence.first ?: event.startDate)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: return emptyList()
    val until = runCatching { LocalDate.parse(recurrence.until) }.getOrNull() ?: return emptyList()
    val from = runCatching { LocalDate.parse(today) }.getOrNull() ?: return emptyList()

    return generateSequence(first) { it.plus(DatePeriod(days = step)) }
        .takeWhile { it <= until }
        .filter { it >= from }
        .take(count)
        .map { it.toString() }
        .toList()
}
