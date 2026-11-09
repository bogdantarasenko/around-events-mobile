package events.around.shared.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.AppLanguage
import events.around.core.model.City
import events.around.core.model.ThemeMode
import events.around.shared.AppConfig
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.data.site.SiteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    site: SiteRepository,
    config: AppConfig,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        settingsRepository.theme,
        settingsRepository.language,
        settingsRepository.city,
        site.props,
    ) { theme, language, city, props ->
        SettingsUiState(
            theme = theme,
            language = language,
            versionName = config.versionName,
            city = city,
            cities = props?.cities.orEmpty(),
            siteUrl = config.baseUrl,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(
            theme = settingsRepository.theme.value,
            language = settingsRepository.language.value,
            versionName = config.versionName,
            city = settingsRepository.city.value,
            cities = site.props.value?.cities.orEmpty(),
            siteUrl = config.baseUrl,
        ),
    )

    fun onThemeSelected(theme: ThemeMode) = settingsRepository.setTheme(theme)

    fun onLanguageSelected(language: AppLanguage) = settingsRepository.setLanguage(language)

    fun onCitySelected(slug: String) = settingsRepository.setCity(slug)
}

data class SettingsUiState(
    val theme: ThemeMode,
    val language: AppLanguage,
    val versionName: String,
    /** The stored city; the feed falls back to the first one while this is null. */
    val city: String? = null,
    val cities: List<City> = emptyList(),
    /** The mini-app outside Telegram, where events are submitted; also `<siteUrl>/rules`. */
    val siteUrl: String = "",
)
