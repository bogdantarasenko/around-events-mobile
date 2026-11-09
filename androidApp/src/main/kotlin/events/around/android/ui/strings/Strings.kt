package events.around.android.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import events.around.core.model.AppLanguage
import events.around.i18n.AroundStrings
import events.around.i18n.EnAroundStrings
import events.around.i18n.aroundStringsFor
import java.util.Locale

val LocalAroundStrings = staticCompositionLocalOf<AroundStrings> { EnAroundStrings }

/** The interface language as a locale, for formatting dates in the same language as the strings. */
val LocalAppLocale = staticCompositionLocalOf<Locale> { Locale.ENGLISH }

/** The chosen language's tag, or the device's first locale when the choice is "System". */
@Composable
@ReadOnlyComposable
fun languageTagFor(language: AppLanguage): String =
    language.tag ?: LocalConfiguration.current.locales[0].toLanguageTag()

fun stringsFor(tag: String): AroundStrings = aroundStringsFor(tag)
