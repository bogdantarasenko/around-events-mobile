package events.around.core.model

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
