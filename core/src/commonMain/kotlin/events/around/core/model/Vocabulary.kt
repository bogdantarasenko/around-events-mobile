package events.around.core.model

/** Category keys the backend accepts (`Submission::CATEGORIES`). Labels live in the i18n module. */
val CATEGORIES: List<String> = listOf(
    "concert",
    "standup",
    "theatre",
    "party",
    "festival",
    "exhibition",
    "kids",
    "market",
    "food",
    "cinema",
    "lecture",
    "sport",
    "other",
)

const val CATEGORY_OTHER = "other"
const val CATEGORY_KIDS = "kids"

/** Languages an event can be held in (`Submission::LANGUAGES`), named in the language itself. */
val EVENT_LANGUAGES: List<Pair<String, String>> = listOf(
    "uk" to "Українська",
    "ru" to "Русский",
    "be" to "Беларуская",
    "en" to "English",
    "pl" to "Polski",
)

enum class Repeat(val apiValue: String) {
    Weekly("weekly"),
    Biweekly("biweekly"),
    ;

    companion object {
        fun fromApi(value: String?): Repeat? = entries.firstOrNull { it.apiValue == value }
    }
}

/** How the backend limits a poster: 15 MB, JPEG, PNG or WebP. */
const val MAX_POSTER_BYTES: Long = 15L * 1024 * 1024
