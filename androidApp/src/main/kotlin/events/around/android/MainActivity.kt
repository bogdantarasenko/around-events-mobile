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
import events.around.android.discover.DiscoverScreen
import events.around.android.event.EventDetailScreen
import events.around.android.events.EventsScreen
import events.around.android.home.HomeFeedScreen
import events.around.android.home.HomeScreen
import events.around.android.home.HomeTab
import events.around.android.map.MapScreen
import events.around.android.organizer.OrganizerScreen
import events.around.android.search.SearchScreen
import events.around.android.settings.SettingsScreen
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.strings.languageTagFor
import events.around.android.ui.strings.stringsFor
import events.around.android.ui.theme.AroundTheme
import events.around.shared.presentation.feed.DiscoverViewModel
import events.around.shared.presentation.feed.EventDetailViewModel
import events.around.shared.presentation.feed.EventsViewModel
import events.around.shared.presentation.feed.MapViewModel
import events.around.shared.presentation.feed.OrganizerViewModel
import events.around.shared.presentation.feed.SearchViewModel
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
                    // Activity-scoped: Discover loads the city for every other screen, and each tab keeps its place.
                    val discover = koinViewModel<DiscoverViewModel>()
                    HomeScreen(tabContent = { tab, selectTab ->
                        when (tab) {
                            HomeTab.Home -> HomeFeedScreen(
                                discover = discover,
                                events = koinViewModel<EventsViewModel>(key = "events-home") { parametersOf(null) },
                                navigate = { backStack.add(it) },
                            )
                            HomeTab.Discover -> DiscoverScreen(
                                viewModel = discover,
                                navigate = { backStack.add(it) },
                                onOpenMap = { selectTab(HomeTab.Map) },
                            )
                            HomeTab.Map -> MapScreen(
                                koinViewModel<MapViewModel>(),
                                navigateBack = null,
                                onOpenEvent = { backStack.add(EventDetail(it)) },
                            )
                        }
                    })
                }
                entry<Events> { route ->
                    val viewModel = koinViewModel<EventsViewModel>(key = "events-${route.category}") {
                        parametersOf(route.category)
                    }
                    EventsScreen(viewModel, navigateBack, onOpen = { backStack.add(it) })
                }
                entry<OrganizerPage> { route ->
                    val viewModel = koinViewModel<OrganizerViewModel>(key = "organizer-${route.slug}") {
                        parametersOf(route.slug)
                    }
                    OrganizerScreen(viewModel, navigateBack, onOpenEvent = { backStack.add(EventDetail(it)) })
                }
                entry<MapRoute> {
                    MapScreen(
                        koinViewModel<MapViewModel>(),
                        navigateBack,
                        onOpenEvent = { backStack.add(EventDetail(it)) },
                    )
                }
                entry<Search> {
                    SearchScreen(
                        koinViewModel<SearchViewModel>(),
                        navigateBack,
                        onOpenEvent = { backStack.add(EventDetail(it)) },
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
                    EventDetailScreen(
                        viewModel = viewModel,
                        navigateBack = navigateBack,
                        onOpenOrganizer = { backStack.add(OrganizerPage(it)) },
                    )
                }
            },
        )
    }
}
