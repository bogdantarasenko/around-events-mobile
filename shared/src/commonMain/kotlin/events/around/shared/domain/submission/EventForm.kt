package events.around.shared.domain.submission

import events.around.core.model.CATEGORIES
import events.around.core.model.CATEGORY_KIDS
import events.around.core.model.CATEGORY_OTHER
import events.around.core.model.MAX_POSTER_BYTES
import events.around.core.model.MyEvent
import events.around.core.model.Repeat
import kotlinx.serialization.Serializable

/**
 * The submission form, field for field as the mini-app's `form.ts`: the app must send exactly what the web form
 * sends, because the backend validates both with one request class.
 */
@Serializable
data class EventForm(
    val city: String = "",
    val title: String = "",
    // No default: a talk filed under "Concerts" is the price of an untouched list.
    val category: String = "",
    val categorySuggestion: String = "",
    val languages: List<String> = emptyList(),
    val withKids: Boolean = false,
    val startDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val endDate: String = "",
    val dayMode: DayMode = DayMode.OneDay,
    val repeat: Repeat = Repeat.Weekly,
    val repeatUntil: String = "",
    val venue: String = "",
    val address: String = "",
    val pricing: Pricing? = null,
    val priceFrom: String = "",
    val priceNote: String = "",
    val eventUrl: String = "",
    val ticketsUrl: String = "",
    val description: String = "",
    val organization: String = "",
    val acceptedRules: Boolean = false,
) {
    /** Typed by hand, as opposed to preset: city, category and the like don't count. */
    val hasContent: Boolean
        get() = listOf(
            title, description, venue, address, startDate, priceFrom, priceNote,
            eventUrl, ticketsUrl, categorySuggestion,
        ).any { it.isNotBlank() }
}

/** One day, several days or a repeating event: the three exclude each other. */
enum class DayMode {
    OneDay,
    MultiDay,
    Repeats,
}

/** Free, paid or not known yet. The third state is explicit: an empty price on "paid" is a forgotten field. */
enum class Pricing {
    Free,
    Paid,
    Unknown,
}

/** The fields a problem is shown next to, in the order the form shows them. */
enum class FormField {
    Title,
    Category,
    Poster,
    StartDate,
    EndDate,
    RepeatUntil,
    Venue,

    // Not `Description`: that collides with `NSObject.description` in Swift.
    DescriptionText,
    Pricing,
    PriceFrom,
    AcceptedRules,
}

/** The first problem that keeps the form from being sent, or null. Checked top to bottom, like the screen reads. */
fun EventForm.firstProblem(posterBytes: Long?): FormField? = eventProblem(posterBytes) ?: entryProblem()

// The same minimums the backend validates (`StoreSubmissionRequest`).
private const val MIN_TITLE = 2
private const val MIN_VENUE = 2
private const val MIN_DESCRIPTION = 10

private fun EventForm.eventProblem(posterBytes: Long?): FormField? = when {
    title.trim().length < MIN_TITLE -> FormField.Title
    category.isEmpty() -> FormField.Category
    posterBytes != null && posterBytes > MAX_POSTER_BYTES -> FormField.Poster
    startDate.isEmpty() -> FormField.StartDate
    dayMode == DayMode.MultiDay && endDate.isNotEmpty() && endDate < startDate -> FormField.EndDate
    // The six-month limit is the server's to check: it answers with its own message on this field.
    dayMode == DayMode.Repeats && (repeatUntil.isEmpty() || repeatUntil < startDate) -> FormField.RepeatUntil
    else -> null
}

private fun EventForm.entryProblem(): FormField? = when {
    venue.trim().length < MIN_VENUE -> FormField.Venue
    description.trim().length < MIN_DESCRIPTION -> FormField.DescriptionText
    pricing == null -> FormField.Pricing
    pricing == Pricing.Paid && priceFrom.isBlank() && priceNote.isBlank() -> FormField.PriceFrom
    !acceptedRules -> FormField.AcceptedRules
    else -> null
}

/**
 * Fields as Laravel expects them. Lists go out as `languages[]`, nested keys as `organizer[organization]`,
 * booleans as `1`/`0`; an empty list is left out because "no field" and "empty" mean the same to the server.
 */
fun EventForm.toParts(): List<Pair<String, String>> {
    val parts = mutableListOf<Pair<String, String>>()
    fun put(key: String, value: String) = parts.add(key to value)
    fun put(key: String, value: Boolean) = parts.add(key to if (value) "1" else "0")

    val multiDay = dayMode == DayMode.MultiDay && endDate.isNotBlank()
    val repeats = dayMode == DayMode.Repeats

    put("city", city)
    put("title", title.trim())
    put("category", category)
    put("category_suggestion", if (category == CATEGORY_OTHER) categorySuggestion.trim() else "")
    languages.forEach { put("languages[]", it) }
    put("with_kids", if (category == CATEGORY_KIDS) true else withKids)
    put("start_date", startDate)
    put("start_time", startTime.trim())
    put("end_time", endTime.trim())
    put("end_date", if (multiDay) endDate.trim() else "")
    put("repeat", if (repeats) repeat.apiValue else "")
    put("repeat_until", if (repeats) repeatUntil else "")
    put("venue", venue.trim())
    put("address", address.trim())
    put("is_free", pricing == Pricing.Free)
    put("price_from", if (pricing == Pricing.Paid) priceFrom.trim() else "")
    put("price_note", if (pricing == Pricing.Free || pricing == Pricing.Paid) priceNote.trim() else "")
    put("event_url", eventUrl.trim())
    put("tickets_url", if (pricing == Pricing.Unknown) "" else ticketsUrl.trim())
    put("description", description.trim())
    put("organizer[organization]", organization.trim())
    put("accepted_rules", acceptedRules)
    return parts
}

/**
 * The form filled from a past event. "Repeat" keeps everything but the dates, which come anew; "fix" and "edit"
 * keep the dates too. Accepting the rules is never inherited: each submission gives it again.
 */
fun MyEvent.toForm(keepDates: Boolean): EventForm {
    val free = priceFrom?.toDoubleOrNull() == 0.0
    val pricing = when {
        free -> Pricing.Free
        priceFrom != null || !priceNote.isNullOrEmpty() -> Pricing.Paid
        else -> Pricing.Unknown
    }
    val repeat = Repeat.fromApi(recurrence?.freq)
    val dayMode = when {
        keepDates && repeat != null -> DayMode.Repeats
        keepDates && endDate != null && endDate != startDate -> DayMode.MultiDay
        else -> DayMode.OneDay
    }
    return EventForm(
        city = city.slug,
        title = title,
        category = category?.takeIf { it in CATEGORIES }.orEmpty(),
        categorySuggestion = categorySuggestion.orEmpty(),
        languages = languages,
        withKids = withKids == true,
        startDate = if (keepDates) startDate.orEmpty() else "",
        startTime = startTime.orEmpty(),
        endTime = endTime.orEmpty(),
        endDate = if (keepDates) endDate.orEmpty() else "",
        dayMode = dayMode,
        repeat = repeat ?: Repeat.Weekly,
        repeatUntil = if (keepDates) recurrence?.until.orEmpty() else "",
        venue = venue.orEmpty(),
        address = address.orEmpty(),
        pricing = pricing,
        priceFrom = if (free) "" else priceFrom?.toDoubleOrNull()?.let(::plainNumber).orEmpty(),
        priceNote = priceNote.orEmpty(),
        eventUrl = eventUrl.orEmpty(),
        ticketsUrl = ticketsUrl.orEmpty(),
        description = description.orEmpty(),
        organization = organization.orEmpty(),
    )
}

/** `80.00` reads as `80`, `12.50` as `12.5`. */
private fun plainNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

/** Server field name → form field, so a rejection lands next to its field instead of at the bottom. */
fun serverField(name: String?): FormField? = when (name) {
    "title" -> FormField.Title
    "category" -> FormField.Category
    "start_date" -> FormField.StartDate
    "end_date" -> FormField.EndDate
    "repeat", "repeat_until" -> FormField.RepeatUntil
    "venue", "address" -> FormField.Venue
    "price_from", "event_url", "tickets_url" -> FormField.PriceFrom
    "description" -> FormField.DescriptionText
    "poster" -> FormField.Poster
    "accepted_rules" -> FormField.AcceptedRules
    else -> null
}
