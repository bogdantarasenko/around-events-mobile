package events.around.android.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.BuildConfig
import events.around.android.EventDetail
import events.around.android.R
import events.around.android.Settings
import events.around.android.ui.components.CategoryFilter
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.eventDays
import events.around.android.ui.components.openUrl
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.City
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
    val context = LocalContext.current
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { CityTitle(cities.city, cities.cities, discover::selectCity) },
                navigationIcon = {
                    Image(
                        painterResource(R.drawable.around_logo),
                        contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp).size(32.dp),
                    )
                },
                actions = {
                    IconButton(onClick = { context.openUrl(BuildConfig.BASE_URL) }) {
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

@Composable
private fun CityTitle(
    city: City?,
    cities: List<City>,
    onSelect: (String) -> Unit,
) {
    val strings = LocalAroundStrings.current
    var open by remember { mutableStateOf(false) }
    Box {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(city?.name ?: strings.tabHome)
            if (cities.size > 1) {
                IconButton(onClick = { open = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = strings.feedCity)
                }
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            cities.forEach { option ->
                DropdownMenuItem(text = { Text(option.name) }, onClick = {
                    open = false
                    onSelect(option.slug)
                })
            }
        }
    }
}
