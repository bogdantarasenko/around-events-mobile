package events.around.i18n

val aroundStrings: Map<String, AroundStrings> = mapOf(
    Locales.En to EnAroundStrings,
    Locales.Uk to UkAroundStrings,
    Locales.Ru to RuAroundStrings,
    Locales.Be to BeAroundStrings,
)

/** Strings for a BCP 47 tag such as `uk-UA`: exact tag first, then the language, then English. */
fun aroundStringsFor(languageTag: String): AroundStrings =
    aroundStrings[languageTag]
        ?: aroundStrings[languageTag.substringBefore('-').substringBefore('_')]
        ?: EnAroundStrings

expect fun String.format(vararg args: Any): String
