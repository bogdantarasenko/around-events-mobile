package events.around.shared.data.settings

import com.russhwolf.settings.Settings
import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-local preferences. Nothing here goes to the server. */
class SettingsRepository(
    private val settings: Settings,
) {
    private val mutableTheme = MutableStateFlow(readEnum(THEME_KEY, ThemeMode.System))
    val theme: StateFlow<ThemeMode> = mutableTheme.asStateFlow()

    private val mutableLanguage = MutableStateFlow(readEnum(LANGUAGE_KEY, AppLanguage.System))
    val language: StateFlow<AppLanguage> = mutableLanguage.asStateFlow()

    private val mutableCity = MutableStateFlow(settings.getStringOrNull(CITY_KEY))

    /** The city whose listing the feed shows; null until the person picks one. */
    val city: StateFlow<String?> = mutableCity.asStateFlow()

    fun setCity(slug: String) {
        settings.putString(CITY_KEY, slug)
        mutableCity.value = slug
    }

    fun setTheme(theme: ThemeMode) {
        settings.putString(THEME_KEY, theme.name)
        mutableTheme.value = theme
    }

    fun setLanguage(language: AppLanguage) {
        settings.putString(LANGUAGE_KEY, language.name)
        mutableLanguage.value = language
    }

    // An unknown stored name (an enum case renamed or removed) falls back to the default instead of crashing.
    private inline fun <reified T : Enum<T>> readEnum(key: String, default: T): T =
        settings.getStringOrNull(key)
            ?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } }
            ?: default

    private companion object {
        const val THEME_KEY = "theme"
        const val LANGUAGE_KEY = "language"
        const val CITY_KEY = "city"
    }
}
