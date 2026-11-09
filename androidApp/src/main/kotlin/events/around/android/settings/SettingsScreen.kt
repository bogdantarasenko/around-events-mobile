package events.around.android.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.BackScaffold
import events.around.android.ui.components.openUrl
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.android.ui.theme.LocalDarkTheme
import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

private val Blue = Color(0xFF007AFF)
private val Pink = Color(0xFFFF2D55)
private val Purple = Color(0xFFAF52DE)
private val Orange = Color(0xFFFF9500)
private val Gray = Color(0xFF8E8E93)
private val GroupedLight = Color(0xFFF2F2F7)
private val GroupedDark = Color(0xFF000000)

/** Settings as in the designs: grey page, white rounded groups, rows with a coloured square icon and a chevron. */
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    val background = if (LocalDarkTheme.current) GroupedDark else GroupedLight

    BackScaffold(navigateBack, modifier.background(background), title = strings.settingsTitle) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 100.dp),
        ) {
            Group(strings.settingsPreferences) {
                if (state.cities.size > 1) {
                    ChoiceRow(
                        icon = Icons.Filled.Apartment,
                        tint = Blue,
                        title = strings.feedCity,
                        options = state.cities.map { it.slug to it.name },
                        selected = state.city ?: state.cities.first().slug,
                        onSelect = viewModel::onCitySelected,
                    )
                    Divider()
                }
                ChoiceRow(
                    icon = Icons.Filled.Palette,
                    tint = Pink,
                    title = strings.settingsAppearance,
                    options = ThemeMode.entries.map {
                        it to themeName(
                            it,
                            strings.themeSystem,
                            strings.themeLight,
                            strings.themeDark,
                        )
                    },
                    selected = state.theme,
                    onSelect = viewModel::onThemeSelected,
                )
                Divider()
                ChoiceRow(
                    icon = Icons.Filled.Language,
                    tint = Purple,
                    title = strings.settingsLanguage,
                    options = AppLanguage.entries.map { it to languageName(it, strings.languageSystem) },
                    selected = state.language,
                    onSelect = viewModel::onLanguageSelected,
                )
            }
            Group(strings.settingsResources) {
                Row(Icons.Filled.Add, Orange, strings.feedSubmit, external = true) { context.openUrl(state.siteUrl) }
                Divider()
                Row(Icons.Filled.Description, Gray, strings.settingsRules, external = true) {
                    context.openUrl(state.siteUrl.trimEnd('/') + "/rules")
                }
            }
            Column(Modifier.padding(start = 16.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    strings.settingsVersion(state.versionName),
                    strings.feedSubmitHint,
                    strings.settingsOpenSource,
                ).forEach {
                    Text(it, fontSize = 13.sp, color = AroundColors.secondary)
                }
            }
        }
    }
}

@Composable
private fun Group(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            title,
            fontSize = 17.sp,
            color = AroundColors.tertiary,
            modifier = Modifier.padding(start = 16.dp, top = 22.dp, bottom = 8.dp),
        )
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(AroundColors.surface),
            content = content,
        )
    }
}

@Composable
private fun Divider() {
    Box(Modifier.padding(start = 56.dp).fillMaxWidth().height(0.5.dp).background(AroundColors.separator))
}

@Composable
private fun Row(
    icon: ImageVector,
    tint: Color,
    title: String,
    value: String? = null,
    external: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().height(49.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
    ) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(tint), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Text(title, fontSize = 17.sp, color = AroundColors.ink, modifier = Modifier.weight(1f))
        value?.let { Text(it, fontSize = 17.sp, color = AroundColors.secondary, maxLines = 1) }
        Icon(
            if (external) Icons.AutoMirrored.Filled.OpenInNew else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AroundColors.chevron,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun <T> ChoiceRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(icon, tint, title, value = options.firstOrNull { it.first == selected }?.second) { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (option, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    open = false
                    onSelect(option)
                })
            }
        }
    }
}

private fun themeName(theme: ThemeMode, system: String, light: String, dark: String): String = when (theme) {
    ThemeMode.System -> system
    ThemeMode.Light -> light
    ThemeMode.Dark -> dark
}

/** Language names are written in the language itself, as in any language picker. */
private fun languageName(language: AppLanguage, system: String): String = when (language) {
    AppLanguage.System -> system
    AppLanguage.Ukrainian -> "Українська"
    AppLanguage.Russian -> "Русский"
    AppLanguage.Belarusian -> "Беларуская"
    AppLanguage.English -> "English"
}
