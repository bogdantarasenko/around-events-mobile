package events.around.android.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.OrganizerPage
import events.around.android.Search
import events.around.android.Settings
import events.around.android.home.CityTitle
import events.around.android.ui.components.Avatar
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.core.model.Organizer
import events.around.shared.presentation.feed.DiscoverViewModel

/**
 * Discover: the Home bar — logo, city, settings — over the city's organizers instead of its events. Each row
 * opens the organizer's page.
 */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    navigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { CityTitle(state.city, state.cities, viewModel::selectCity) },
                actions = {
                    IconButton(onClick = { navigate(Search) }) {
                        Icon(Icons.Outlined.Search, contentDescription = strings.searchPlaceholder)
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
            isRefreshing = state.isRefreshing && !state.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
                state.error?.let { error ->
                    item {
                        ErrorBanner(
                            error.text(strings),
                            actionLabel = strings.retry,
                            onAction = viewModel::refresh,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
                if (state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.organizers.isEmpty()) {
                    item {
                        Text(
                            strings.discoverOrganizersEmpty,
                            color = AroundColors.secondary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        )
                    }
                }
                items(state.organizers.size, key = { state.organizers[it].slug }) { index ->
                    OrganizerRow(
                        state.organizers[index],
                        onClick = { navigate(OrganizerPage(state.organizers[index].slug)) },
                        showsSeparator = index < state.organizers.lastIndex,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

/** "Featured calendars" in the designs: a 60dp picture, the name in 17sp and a grey line about them. */
@Composable
private fun OrganizerRow(
    organizer: Organizer,
    onClick: () -> Unit,
    showsSeparator: Boolean,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Column(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            Avatar(organizer.avatarUrl, organizer.name, size = 60.dp, cornerRadius = 10.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    organizer.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = AroundColors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    organizer.bio ?: strings.eventsCount("${organizer.upcomingCount}"),
                    fontSize = 15.sp,
                    color = AroundColors.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (showsSeparator) {
            Box(Modifier.padding(start = 73.dp).fillMaxWidth().height(0.5.dp).background(AroundColors.separator))
        }
    }
}
