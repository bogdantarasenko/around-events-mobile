package events.around.android.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.BuildConfig
import events.around.android.EventDetail
import events.around.android.Settings
import events.around.android.ui.components.CategoryFilter
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.eventDays
import events.around.android.ui.components.openUrl
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.core.model.City
import events.around.shared.presentation.feed.DiscoverViewModel
import events.around.shared.presentation.feed.EventsViewModel

private val TAB_BAR_SPACE = 96.dp

/** The Home tab: the city's events by day, as "Picked for you" in the designs. */
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
    PullToRefreshBox(
        isRefreshing = cities.isRefreshing && !cities.isLoading,
        onRefresh = discover::refresh,
        modifier = modifier.fillMaxSize().statusBarsPadding(),
    ) {
        LazyColumn(contentPadding = PaddingValues(bottom = TAB_BAR_SPACE), modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
                ) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                    }
                    CityTitle(cities.city, cities.cities, discover::selectCity, Modifier.weight(1f))
                    HeaderActions(
                        HeaderAction(Icons.Outlined.Add, strings.feedSubmit) { context.openUrl(BuildConfig.BASE_URL) },
                        HeaderAction(Icons.Outlined.Settings, strings.settingsTitle) { navigate(Settings) },
                    )
                }
            }
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
                    Box(
                        Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
            }
            eventDays(state.days) { navigate(EventDetail(it)) }
        }
    }
}

@Composable
private fun CityTitle(
    city: City?,
    cities: List<City>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(enabled = cities.size > 1) { open = true },
        ) {
            Text(
                city?.name ?: strings.tabHome,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AroundColors.ink,
            )
            if (cities.size > 1) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = strings.feedCity,
                    tint = AroundColors.chevron,
                )
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
