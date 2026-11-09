package events.around.core.model

enum class ThemeMode {
    System,
    Light,
    Dark,
}

/** Interface languages. [System] follows the device; the rest are BCP 47 tags the i18n module knows. */
enum class AppLanguage(val tag: String?) {
    System(null),
    Ukrainian("uk"),
    Russian("ru"),
    Belarusian("be"),
    English("en"),
}
