package events.around.shared.domain.feed

import events.around.core.model.Event
import events.around.core.model.Repeat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** One heading of the feed: a day, or the events that started earlier and are still on. */
data class FeedDay(
    /** `yyyy-MM-dd`; null for [isOngoing]. */
    val date: String?,
    val isOngoing: Boolean,
    val events: List<Event>,
)

/**
 * The listing laid out by day, as people read a poster: what is already on first, then each day from [today]
 * on. A repeating event sits on its next date. [category] null keeps every category. [today] is `yyyy-MM-dd`.
 */
fun feedDays(events: List<Event>, today: String, category: String? = null): List<FeedDay> {
    val shown = if (category == null) events else events.filter { it.category == category }
    val byDate = shown
        .mapNotNull { event -> occurrence(event, today)?.let { it to event } }
        .filter { (date, event) -> date >= today || (event.lastDay ?: date) >= today }
        .groupBy({ (date, _) -> if (date < today) null else date }, { (_, event) -> event })

    val ongoing = byDate[null]?.let { listOf(FeedDay(null, isOngoing = true, it.sortedBy(::timeKey))) }.orEmpty()
    val days = byDate.entries
        .mapNotNull { (date, dayEvents) -> date?.let { FeedDay(it, isOngoing = false, dayEvents.sortedBy(::timeKey)) } }
        .sortedBy { it.date }
    return ongoing + days
}

/** Categories present in [events], the most common first: the filter offers only what there is. */
fun feedCategories(events: List<Event>): List<String> = events
    .mapNotNull { it.category }
    .groupingBy { it }
    .eachCount()
    .entries
    .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
    .map { it.key }

/** The next [count] dates of a repeating event, from [today] on; empty for a one-off. */
fun nextDates(event: Event, today: String, count: Int = 3): List<String> {
    val recurrence = event.recurrence ?: return emptyList()
    val step = when (Repeat.fromApi(recurrence.freq)) {
        Repeat.Weekly -> DAYS_IN_WEEK
        Repeat.Biweekly -> DAYS_IN_WEEK * 2
        null -> return emptyList()
    }
    val first = (recurrence.first ?: event.startDate)?.let(::parse) ?: return emptyList()
    val until = parse(recurrence.until) ?: return emptyList()
    val from = parse(today) ?: return emptyList()

    return generateSequence(first) { it.plus(DatePeriod(days = step)) }
        .takeWhile { it <= until }
        .filter { it >= from }
        .take(count)
        .map { it.toString() }
        .toList()
}

// A repeating event is placed on its next date; anything else on its start date.
private fun occurrence(event: Event, today: String): String? =
    nextDates(event, today, count = 1).firstOrNull() ?: event.startDate

// No time sorts last within the day: "sometime that day" reads after the ones with a time.
private fun timeKey(event: Event): String = event.startTime ?: "~"

private fun parse(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()

private const val DAYS_IN_WEEK = 7
