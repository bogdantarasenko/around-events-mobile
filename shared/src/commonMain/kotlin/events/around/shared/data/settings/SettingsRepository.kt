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

    private val mutableDefaultOrganization = MutableStateFlow(settings.getString(ORGANIZATION_KEY, ""))

    /** Fills "Organization" in a new submission. Set by hand, or taken from the last one sent. */
    val defaultOrganization: StateFlow<String> = mutableDefaultOrganization.asStateFlow()

    private val mutableDefaultCity = MutableStateFlow(settings.getStringOrNull(CITY_KEY))
    val defaultCity: StateFlow<String?> = mutableDefaultCity.asStateFlow()

    fun setDefaultOrganization(organization: String) {
        settings.putString(ORGANIZATION_KEY, organization.trim())
        mutableDefaultOrganization.value = organization.trim()
    }

    fun setDefaultCity(slug: String) {
        settings.putString(CITY_KEY, slug)
        mutableDefaultCity.value = slug
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
        const val ORGANIZATION_KEY = "default_organization"
        const val CITY_KEY = "default_city"
    }
}
