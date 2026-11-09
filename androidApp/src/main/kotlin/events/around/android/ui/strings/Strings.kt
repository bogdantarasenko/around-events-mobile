package events.around.android.ui.strings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import events.around.core.model.AppLanguage
import events.around.i18n.AroundStrings
import events.around.i18n.EnAroundStrings
import events.around.i18n.aroundStringsFor

val LocalAroundStrings = staticCompositionLocalOf<AroundStrings> { EnAroundStrings }

/** The chosen language, or the device's first locale when the choice is "System". */
@Composable
@ReadOnlyComposable
fun stringsFor(language: AppLanguage): AroundStrings {
    val tag = language.tag ?: LocalConfiguration.current.locales[0].toLanguageTag()
    return aroundStringsFor(tag)
}
