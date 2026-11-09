package events.around.shared.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import events.around.shared.AppConfig
import events.around.shared.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    config: AppConfig,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        settingsRepository.theme,
        settingsRepository.language,
    ) { theme, language ->
        SettingsUiState(theme = theme, language = language, versionName = config.versionName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(
            theme = settingsRepository.theme.value,
            language = settingsRepository.language.value,
            versionName = config.versionName,
        ),
    )

    fun onThemeSelected(theme: ThemeMode) = settingsRepository.setTheme(theme)

    fun onLanguageSelected(language: AppLanguage) = settingsRepository.setLanguage(language)
}

data class SettingsUiState(
    val theme: ThemeMode,
    val language: AppLanguage,
    val versionName: String,
)
