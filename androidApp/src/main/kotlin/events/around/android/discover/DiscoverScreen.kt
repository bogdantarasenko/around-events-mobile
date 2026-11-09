package events.around.android.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.BuildConfig
import events.around.android.EventDetail
import events.around.android.Events
import events.around.android.MapRoute
import events.around.android.OrganizerPage
import events.around.android.Search
import events.around.android.Settings
import events.around.android.ui.components.Avatar
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.EventRow
import events.around.android.ui.components.categoryEmoji
import events.around.android.ui.components.openUrl
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.City
import events.around.core.model.Event
import events.around.core.model.Organizer
import events.around.shared.domain.feed.CategoryCount
import events.around.shared.presentation.feed.DiscoverUiState
import events.around.shared.presentation.feed.DiscoverViewModel

private const val SHELF_ROWS = 3

private val CityGradients = listOf(
    listOf(Color(0xFF3F51B5), Color(0xFF8E24AA)),
    listOf(Color(0xFF00897B), Color(0xFF1E88E5)),
    listOf(Color(0xFFF57C00), Color(0xFFD81B60)),
    listOf(Color(0xFF43A047), Color(0xFF00897B)),
)

/** The home screen: the city's next events, categories, cities and organizers. */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    navigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    PullToRefreshBox(
        isRefreshing = state.isRefreshing && !state.isLoading,
        onRefresh = viewModel::refresh,
        modifier = modifier.fillMaxSize().statusBarsPadding(),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { Header(navigate) }
            when {
                state.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.upcoming.isEmpty() && (state.isEmpty || state.error != null) -> item {
                    Empty(state.error?.text(strings) ?: strings.feedEmptyText, viewModel::refresh)
                }
                else -> {
                    state.error?.let { error ->
                        item {
                            ErrorBanner(
                                error.text(strings),
                                actionLabel = strings.retry,
                                onAction = viewModel::refresh,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                    item {
                        UpcomingShelf(
                            state,
                            onOpenAll = { navigate(Events()) },
                            onOpen = { navigate(EventDetail(it)) },
                        )
                    }
                    if (state.categories.isNotEmpty()) {
                        item { CategoryGrid(state.categories) { navigate(Events(it)) } }
                    }
                    if (state.cities.size > 1) {
                        item { CityCarousel(state.cities, state.city?.slug, viewModel::selectCity) }
                    }
                    if (state.organizers.isNotEmpty()) {
                        item { OrganizerList(state.organizers) { navigate(OrganizerPage(it)) } }
                    }
                    item { SubmitCard() }
                }
            }
        }
    }
}

@Composable
private fun Header(navigate: (NavKey) -> Unit) {
    val strings = LocalAroundStrings.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { navigate(Settings) }) {
            Icon(Icons.Outlined.Settings, contentDescription = strings.settingsTitle)
        }
        Text(strings.tabFeed, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Box(Modifier.weight(1f))
        Row(Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            IconButton(onClick = { navigate(MapRoute) }) {
                Icon(Icons.Outlined.Map, contentDescription = strings.mapTitle)
            }
            IconButton(onClick = { navigate(Search) }) {
                Icon(Icons.Outlined.Search, contentDescription = strings.searchPlaceholder)
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (onClick != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/** The next events in columns of three, swiped sideways. */
@Composable
private fun UpcomingShelf(
    state: DiscoverUiState,
    onOpenAll: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val strings = LocalAroundStrings.current
    val columnWidth = (LocalConfiguration.current.screenWidthDp - 56).dp
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(state.city?.name ?: strings.tabFeed, strings.discoverUpcoming, onOpenAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.upcoming.chunked(SHELF_ROWS)) { column: List<Event> ->
                Column(Modifier.width(columnWidth)) {
                    column.forEachIndexed { index, event ->
                        EventRow(event, onClick = { onOpen(event.id) }, showsDay = true)
                        if (index < column.lastIndex) HorizontalDivider(Modifier.padding(start = 78.dp))
                    }
                }
            }
        }
    }
}

/** Emoji chips in two rows that scroll sideways together. */
@Composable
private fun CategoryGrid(
    categories: List<CategoryCount>,
    onOpen: (String) -> Unit,
) {
    val strings = LocalAroundStrings.current
    val rows = categories.withIndex().partition { it.index % 2 == 0 }.toList().map { part -> part.map { it.value } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(strings.discoverCategories)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { item ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            0.5.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(10.dp),
                                        )
                                        .clickable { onOpen(item.key) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Text(categoryEmoji(item.key))
                                    Text(categoryLabel(item.key, strings), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Square cards to switch the city; a gradient per city, as the backend has no city photos. */
@Composable
private fun CityCarousel(
    cities: List<City>,
    current: String?,
    onSelect: (String) -> Unit,
) {
    val strings = LocalAroundStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(strings.discoverCities)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(cities.size) { index ->
                val city = cities[index]
                Box(
                    Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(CityGradients[index % CityGradients.size]))
                        .clickable { onSelect(city.slug) }
                        .padding(12.dp),
                ) {
                    if (city.slug == current) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                    }
                    Column(Modifier.align(Alignment.BottomStart)) {
                        Text(city.name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                        if (city.upcomingCount > 0) {
                            Text(
                                strings.eventsCount("${city.upcomingCount}"),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganizerList(
    organizers: List<Organizer>,
    onOpen: (String) -> Unit,
) {
    val strings = LocalAroundStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(strings.discoverOrganizers)
        organizers.forEach { organizer ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    onOpen(
                        organizer.slug,
                    )
                }.padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Avatar(organizer.avatarUrl, organizer.name, size = 48.dp, cornerRadius = 10.dp)
                Column(Modifier.weight(1f)) {
                    Text(organizer.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        organizer.bio ?: strings.eventsCount("${organizer.upcomingCount}"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** The way to the mini-app for organizers: submitting happens there, not in the app. */
@Composable
private fun SubmitCard() {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { context.openUrl(BuildConfig.BASE_URL) }
            .padding(14.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column {
            Text(strings.feedSubmit, style = MaterialTheme.typography.titleMedium)
            Text(
                strings.feedSubmitHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Empty(
    message: String,
    onRetry: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 96.dp, horizontal = 24.dp),
    ) {
        Text(strings.feedEmptyTitle, style = MaterialTheme.typography.titleLarge)
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onRetry) { Text(strings.retry) }
    }
}
