package events.around.android.ui.format

import events.around.core.model.EventStatus
import events.around.core.model.MyEvent
import events.around.i18n.AroundStrings
import events.around.shared.domain.events.EventGroupKind
import events.around.shared.domain.submission.FormField
import events.around.shared.presentation.ErrorKind
import events.around.shared.presentation.UiError
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun UiError.text(strings: AroundStrings): String = when (kind) {
    ErrorKind.Offline -> strings.apiOffline
    ErrorKind.SignedOut -> strings.loginExpired
    ErrorKind.NotFound -> strings.apiNotFound
    ErrorKind.TooMany -> strings.apiTooMany
    ErrorKind.Explained -> message ?: strings.genericError
    ErrorKind.Server -> strings.apiServerError(status?.toString().orEmpty())
    ErrorKind.Unknown -> strings.genericError
}

fun EventStatus.label(strings: AroundStrings): String = when (this) {
    EventStatus.Review -> strings.statusReview
    EventStatus.Queued -> strings.statusQueued
    EventStatus.Published -> strings.statusPublished
    EventStatus.Rejected -> strings.statusRejected
    EventStatus.Duplicate -> strings.statusDuplicate
    EventStatus.Cancelled -> strings.statusCancelled
}

fun EventGroupKind.label(strings: AroundStrings): String = when (this) {
    EventGroupKind.Review -> strings.groupReview
    EventGroupKind.Soon -> strings.groupSoon
    EventGroupKind.Off -> strings.groupOff
    EventGroupKind.Past -> strings.groupPast
}

fun categoryLabel(key: String, strings: AroundStrings): String = when (key) {
    "concert" -> strings.categoryConcert
    "standup" -> strings.categoryStandup
    "theatre" -> strings.categoryTheatre
    "party" -> strings.categoryParty
    "festival" -> strings.categoryFestival
    "exhibition" -> strings.categoryExhibition
    "kids" -> strings.categoryKids
    "market" -> strings.categoryMarket
    "food" -> strings.categoryFood
    "cinema" -> strings.categoryCinema
    "lecture" -> strings.categoryLecture
    "sport" -> strings.categorySport
    "other" -> strings.categoryOther
    else -> key
}

fun FormField.problem(strings: AroundStrings): String = when (this) {
    FormField.Title -> strings.problemTitle
    FormField.Category -> strings.problemCategory
    FormField.Poster -> strings.problemPoster
    FormField.StartDate -> strings.problemStartDate
    FormField.EndDate -> strings.problemEndDate
    FormField.RepeatUntil -> strings.problemRepeatUntil
    FormField.Venue -> strings.problemVenue
    FormField.Description -> strings.problemDescription
    FormField.Pricing -> strings.problemPricing
    FormField.PriceFrom -> strings.problemPriceFrom
    FormField.AcceptedRules -> strings.problemAcceptedRules
}

/** `1 December, Tue` in the interface language; null for a missing or broken date. */
fun formatDay(iso: String?, locale: Locale): String? {
    val date = iso?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    return DateTimeFormatter.ofPattern("d MMMM, EEE", locale).format(date)
}

fun formatShortDay(iso: String?, locale: Locale): String? {
    val date = iso?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    return DateTimeFormatter.ofPattern("d MMM", locale).format(date)
}

/** An ISO timestamp from the server (`2026-11-09T12:00:00+00:00`) as a local date. */
fun formatMoment(iso: String?, locale: Locale): String? {
    val moment = iso?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() } ?: return null
    return DateTimeFormatter.ofPattern(
        "d MMMM, HH:mm",
        locale,
    ).format(moment.atZoneSameInstant(java.time.ZoneId.systemDefault()))
}

/** `1 December, Tue · 19:00–21:00`, or a range for several days. */
fun MyEvent.whenText(strings: AroundStrings, locale: Locale): String {
    val start = formatDay(startDate, locale) ?: return strings.dateNone
    val time = listOfNotNull(startTime, endTime).joinToString("–")
    val days = if (endDate != null && endDate != startDate) {
        "$start — ${formatDay(endDate, locale) ?: endDate}"
    } else {
        start
    }
    return if (time.isEmpty()) days else "$days · $time"
}

fun MyEvent.priceText(strings: AroundStrings): String? {
    val price = priceFrom?.toDoubleOrNull()
    return when {
        price == 0.0 -> strings.free
        price != null -> strings.priceFrom(if (price % 1.0 == 0.0) price.toLong().toString() else price.toString())
        else -> null
    }
}
