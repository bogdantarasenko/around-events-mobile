package events.around.shared.domain.feed

import events.around.core.model.Event
import events.around.core.model.Repeat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
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
fun feedCategories(events: List<Event>): List<String> = categoryCounts(events).map { it.key }

/** Each category in [events] with its number of events, the most common first. */
fun categoryCounts(events: List<Event>): List<CategoryCount> = events
    .mapNotNull { it.category }
    .groupingBy { it }
    .eachCount()
    .map { (key, count) -> CategoryCount(key, count) }
    .sortedWith(compareByDescending<CategoryCount> { it.count }.thenBy { it.key })

data class CategoryCount(
    val key: String,
    val count: Int,
)

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

/**
 * The next [count] events from [today] on, in the order the feed shows them: the "upcoming" shelf. What started
 * earlier and is still on (month-long exhibitions) would fill it every day, so it isn't counted.
 */
fun upcomingEvents(events: List<Event>, today: String, count: Int): List<Event> =
    feedDays(events, today).filterNot { it.isOngoing }.flatMap { it.events }.take(count)

/**
 * Events whose title, place, organizer or description contain every word of [query], ignoring case. The search
 * runs on the loaded listing: it is a few hundred events at most.
 */
fun searchEvents(events: List<Event>, query: String): List<Event> {
    val words = query.lowercase().split(' ').filter { it.isNotBlank() }
    if (words.isEmpty()) return emptyList()
    return events.filter { event ->
        val text = listOfNotNull(event.title, event.venue, event.address, event.organizer?.name, event.description)
            .joinToString(" ")
            .lowercase()
        words.all { it in text }
    }
}

/** When an event is on, for the map's time filter. */
enum class Period {
    All,
    Today,
    Tomorrow,

    /** The coming Saturday and Sunday; on a weekend, this one. */
    Weekend,
}

/** Whether [event] is on during [period], counted from [today] (`yyyy-MM-dd`): any of its days, any repeat. */
fun isDuring(event: Event, period: Period, today: String): Boolean {
    if (period == Period.All) return true
    val from = parse(today) ?: return false
    val days = when (period) {
        Period.All -> return true
        Period.Today -> listOf(from)
        Period.Tomorrow -> listOf(from.plus(DatePeriod(days = 1)))
        Period.Weekend -> weekend(from)
    }
    return days.any { day -> isOn(event, day, today) }
}

private fun weekend(from: LocalDate): List<LocalDate> {
    // Days to Saturday; 0 on Saturday, and Sunday counts as this weekend's second day.
    val toSaturday = (DayOfWeek.SATURDAY.isoDayNumber - from.dayOfWeek.isoDayNumber + DAYS_IN_WEEK) % DAYS_IN_WEEK
    if (from.dayOfWeek == DayOfWeek.SUNDAY) return listOf(from)
    val saturday = from.plus(DatePeriod(days = toSaturday))
    return listOf(saturday, saturday.plus(DatePeriod(days = 1)))
}

private fun isOn(event: Event, day: LocalDate, today: String): Boolean {
    if (event.recurrence != null) {
        return nextDates(event, today, count = MAX_REPEATS_CHECKED).any { it == day.toString() }
    }
    val start = event.startDate?.let(::parse) ?: return false
    val end = event.lastDay?.let(::parse) ?: start
    return day in start..end
}

private const val MAX_REPEATS_CHECKED = 8
