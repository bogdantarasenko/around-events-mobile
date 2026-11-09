package events.around.android.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import events.around.shared.presentation.settings.SettingsUiState
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreenContent(
        state = state,
        navigateBack = navigateBack,
        onThemeSelected = viewModel::onThemeSelected,
        onLanguageSelected = viewModel::onLanguageSelected,
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    navigateBack: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(strings.settingsAppearance)
            ChoiceRow(
                options = ThemeMode.entries,
                selected = state.theme,
                label = { theme ->
                    when (theme) {
                        ThemeMode.System -> strings.themeSystem
                        ThemeMode.Light -> strings.themeLight
                        ThemeMode.Dark -> strings.themeDark
                    }
                },
                onSelect = onThemeSelected,
            )

            SectionTitle(strings.settingsLanguage)
            ChoiceRow(
                options = AppLanguage.entries,
                selected = state.language,
                label = { language -> languageName(language, strings.languageSystem) },
                onSelect = onLanguageSelected,
            )

            SectionTitle(strings.settingsAbout)
            Text(strings.settingsVersion(state.versionName), style = MaterialTheme.typography.bodyMedium)
            Text(
                strings.settingsOpenSource,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun <T> ChoiceRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

/** Language names are written in the language itself, as in any language picker. */
private fun languageName(language: AppLanguage, system: String): String = when (language) {
    AppLanguage.System -> system
    AppLanguage.Ukrainian -> "Українська"
    AppLanguage.Russian -> "Русский"
    AppLanguage.Belarusian -> "Беларуская"
    AppLanguage.English -> "English"
}
