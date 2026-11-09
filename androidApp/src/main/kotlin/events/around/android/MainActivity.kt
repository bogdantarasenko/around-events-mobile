package events.around.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import events.around.android.feed.EventDetailScreen
import events.around.android.feed.FeedScreen
import events.around.android.settings.SettingsScreen
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.strings.languageTagFor
import events.around.android.ui.strings.stringsFor
import events.around.android.ui.theme.AroundTheme
import events.around.shared.presentation.feed.EventDetailViewModel
import events.around.shared.presentation.feed.FeedViewModel
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            val settings by settingsViewModel.state.collectAsStateWithLifecycle()
            val languageTag = languageTagFor(settings.language)

            AroundTheme(themeMode = settings.theme) {
                CompositionLocalProvider(
                    LocalAroundStrings provides stringsFor(languageTag),
                    LocalAppLocale provides Locale.forLanguageTag(languageTag),
                ) {
                    AroundNavigation(rememberNavBackStack(Home))
                }
            }
        }
    }

    @Composable
    private fun AroundNavigation(backStack: NavBackStack<NavKey>) {
        val navigateBack: () -> Unit = {
            if (backStack.size > 1) backStack.removeLastOrNull() else finish()
        }
        NavDisplay(
            backStack = backStack,
            onBack = navigateBack,
            entryProvider = entryProvider {
                entry<Home> {
                    // Activity-scoped: the feed keeps its place while an event is open.
                    FeedScreen(
                        viewModel = koinViewModel<FeedViewModel>(),
                        onOpenEvent = { backStack.add(EventDetail(it)) },
                        onOpenSettings = { backStack.add(Settings) },
                    )
                }
                entry<Settings> {
                    SettingsScreen(navigateBack = navigateBack)
                }
                entry<EventDetail> { route ->
                    // Keyed by event: each event gets its own view model.
                    val viewModel = koinViewModel<EventDetailViewModel>(key = "event-${route.eventId}") {
                        parametersOf(route.eventId)
                    }
                    EventDetailScreen(viewModel = viewModel, navigateBack = navigateBack)
                }
            },
        )
    }
}
