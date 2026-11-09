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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.SectionHeader
import events.around.android.ui.components.openUrl
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.cities.size > 1) {
                SectionHeader(strings.feedCity)
                ChoiceRow(
                    options = state.cities.map { it.slug },
                    selected = state.city ?: state.cities.first().slug,
                    label = { slug -> state.cities.first { it.slug == slug }.name },
                    onSelect = viewModel::onCitySelected,
                )
            }

            SectionHeader(strings.settingsAppearance)
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
                onSelect = viewModel::onThemeSelected,
            )

            SectionHeader(strings.settingsLanguage)
            ChoiceRow(
                options = AppLanguage.entries,
                selected = state.language,
                label = { language -> languageName(language, strings.languageSystem) },
                onSelect = viewModel::onLanguageSelected,
            )

            SectionHeader(strings.settingsAbout)
            TextButton(onClick = { context.openUrl(state.siteUrl) }) { Text(strings.feedSubmit) }
            Hint(strings.feedSubmitHint)
            TextButton(
                onClick = { context.openUrl(state.siteUrl.trimEnd('/') + "/rules") },
            ) { Text(strings.settingsRules) }
            Text(strings.settingsVersion(state.versionName), style = MaterialTheme.typography.bodyMedium)
            Hint(strings.settingsOpenSource)
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
