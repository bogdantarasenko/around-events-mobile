package events.around.android.ui.format

import events.around.core.model.Event
import events.around.i18n.AroundStrings
import events.around.shared.domain.feed.FeedDay
import events.around.shared.presentation.ErrorKind
import events.around.shared.presentation.UiError
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun UiError.text(strings: AroundStrings): String = when (kind) {
    ErrorKind.Offline -> strings.apiOffline
    ErrorKind.NotFound -> strings.apiNotFound
    ErrorKind.TooMany -> strings.apiTooMany
    ErrorKind.Server -> strings.apiServerError(status?.toString().orEmpty())
    ErrorKind.Unknown -> strings.genericError
}

/** Imported events carry the source's own words; an unknown one is shown as it came, capitalised. */
fun categoryLabel(key: String, strings: AroundStrings): String =
    categoryLabels[key]?.invoke(strings) ?: key.replaceFirstChar { it.uppercase() }

private val categoryLabels: Map<String, (AroundStrings) -> String> = mapOf(
    "concert" to { it.categoryConcert },
    "music" to { it.categoryMusic },
    "standup" to { it.categoryStandup },
    "theatre" to { it.categoryTheatre },
    "party" to { it.categoryParty },
    "festival" to { it.categoryFestival },
    "exhibition" to { it.categoryExhibition },
    "kids" to { it.categoryKids },
    "market" to { it.categoryMarket },
    "food" to { it.categoryFood },
    "cinema" to { it.categoryCinema },
    "lecture" to { it.categoryLecture },
    "learning" to { it.categoryLearning },
    "sport" to { it.categorySport },
    "games" to { it.categoryGames },
    "creative" to { it.categoryCreative },
    "other" to { it.categoryOther },
)

/** `1 December, Tue` in the interface language; null for a missing or broken date. */
fun formatDay(iso: String?, locale: Locale): String? {
    val date = iso?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    return DateTimeFormatter.ofPattern("d MMMM, EEE", locale).format(date)
}

/** The heading of a feed day: today, tomorrow, or the date. */
fun FeedDay.heading(strings: AroundStrings, locale: Locale): String {
    val today = LocalDate.now()
    return when (date) {
        null -> strings.feedOngoing
        today.toString() -> strings.feedToday
        today.plusDays(1).toString() -> strings.feedTomorrow
        else -> formatDay(date, locale) ?: date.orEmpty()
    }
}

/** `19:00–21:00`, or null when the event gave no time. */
fun Event.timeText(): String? = listOfNotNull(startTime, endTime).joinToString("–").ifEmpty { null }

/** `1 December, Tue · 19:00–21:00`, or a range for several days. */
fun Event.whenText(strings: AroundStrings, locale: Locale): String {
    val start = formatDay(startDate, locale) ?: return strings.dateNone
    val days = if (endDate != null && endDate != startDate) {
        "$start — ${formatDay(endDate, locale) ?: endDate}"
    } else {
        start
    }
    return timeText()?.let { "$days · $it" } ?: days
}

fun Event.priceText(strings: AroundStrings): String? {
    val price = priceFrom?.toDoubleOrNull()
    return when {
        price == 0.0 -> strings.free
        price != null -> {
            val amount = if (price % 1.0 == 0.0) price.toLong().toString() else price.toString()
            strings.priceFrom("$amount ${currencySign(currency)}")
        }
        else -> null
    }
}

/** Links come as people typed them: `t.me/name` without a scheme is common. */
fun linkOf(raw: String?): String? {
    val link = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return if ("://" in link) link else "https://$link"
}

// Polish złoty is how the listing's own posts write it.
private fun currencySign(code: String?): String = when (code) {
    null, "PLN" -> "zł"
    "EUR" -> "€"
    "USD" -> "$"
    else -> code
}
