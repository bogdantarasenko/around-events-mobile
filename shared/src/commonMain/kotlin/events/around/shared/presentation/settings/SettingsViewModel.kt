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
        settingsRepository.defaultOrganization,
        settingsRepository.defaultCity,
        site.props,
    ) { theme, language, organization, city, props ->
        SettingsUiState(
            theme = theme,
            language = language,
            versionName = config.versionName,
            defaultOrganization = organization,
            defaultCity = city,
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
            defaultOrganization = settingsRepository.defaultOrganization.value,
            defaultCity = settingsRepository.defaultCity.value,
            cities = site.props.value?.cities.orEmpty(),
            siteUrl = config.baseUrl,
        ),
    )

    fun onThemeSelected(theme: ThemeMode) = settingsRepository.setTheme(theme)

    fun onLanguageSelected(language: AppLanguage) = settingsRepository.setLanguage(language)

    fun onDefaultOrganizationChanged(organization: String) = settingsRepository.setDefaultOrganization(organization)

    fun onDefaultCitySelected(slug: String) = settingsRepository.setDefaultCity(slug)
}

data class SettingsUiState(
    val theme: ThemeMode,
    val language: AppLanguage,
    val versionName: String,
    val defaultOrganization: String = "",
    val defaultCity: String? = null,
    val cities: List<City> = emptyList(),
    /** For the rules page, `<siteUrl>/rules`. */
    val siteUrl: String = "",
)
