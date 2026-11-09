package events.around.android.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.EventDetail
import events.around.android.MiniApp
import events.around.android.Settings
import events.around.android.ui.components.CategoryFilter
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.eventDays
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.domain.miniapp.MiniAppScreen
import events.around.shared.presentation.feed.DiscoverViewModel
import events.around.shared.presentation.feed.EventsViewModel

/**
 * The Home tab: the city's events by day, as "Picked for you" in the designs. The large title is the city and
 * opens the city menu; "+" opens the mini-app to submit, the gear opens settings.
 */
@Composable
fun HomeFeedScreen(
    discover: DiscoverViewModel,
    events: EventsViewModel,
    navigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by events.state.collectAsStateWithLifecycle()
    val cities by discover.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            // The city next to the logo, as the brand next to the avatar in the designs.
            TopAppBar(
                title = { CityTitle(cities.city, cities.cities, discover::selectCity) },
                actions = {
                    IconButton(onClick = { navigate(MiniApp(MiniAppScreen.Submit)) }) {
                        Icon(Icons.Outlined.Add, contentDescription = strings.feedSubmit)
                    }
                    IconButton(onClick = { navigate(Settings) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = strings.settingsTitle)
                    }
                },
                scrollBehavior = scroll,
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = cities.isRefreshing && !cities.isLoading,
            onRefresh = discover::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
                (state.error ?: cities.error)?.let { error ->
                    item {
                        ErrorBanner(
                            error.text(strings),
                            actionLabel = strings.retry,
                            onAction = discover::refresh,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
                if (state.filters.isNotEmpty()) {
                    item { CategoryFilter(state.filters, state.filter, events::selectFilter) }
                }
                if (state.isLoading || cities.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                eventDays(state.days) { navigate(EventDetail(it)) }
            }
        }
    }
}
