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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.Cities
import events.around.android.CityPage
import events.around.android.EventDetail
import events.around.android.Events
import events.around.android.OrganizerPage
import events.around.android.Search
import events.around.android.Settings
import events.around.android.city.cityGradient
import events.around.android.home.HeaderAction
import events.around.android.home.HeaderActions
import events.around.android.ui.components.Avatar
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.EventRow
import events.around.android.ui.components.categoryIcon
import events.around.android.ui.components.categoryTint
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.core.model.City
import events.around.core.model.Event
import events.around.core.model.Organizer
import events.around.shared.domain.feed.CategoryCount
import events.around.shared.presentation.feed.DiscoverUiState
import events.around.shared.presentation.feed.DiscoverViewModel

private const val SHELF_ROWS = 3
private const val SHADE_START = 0.5f

// The design's 333dp column on a 393dp screen: 20 margin, 20 gap, 20 of the next column.
private const val SHELF_PEEK_DP = 60

private val TAB_BAR_SPACE = 100.dp
private val ShadeEnd = Color.Black.copy(alpha = 0.45f)

/** Discover as in the designs: the city's upcoming events, categories, cities and organizers. */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    navigate: (NavKey) -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    PullToRefreshBox(
        isRefreshing = state.isRefreshing && !state.isLoading,
        onRefresh = viewModel::refresh,
        modifier = modifier.fillMaxSize().statusBarsPadding(),
    ) {
        LazyColumn(contentPadding = PaddingValues(bottom = TAB_BAR_SPACE), modifier = Modifier.fillMaxSize()) {
            item { Header(navigate, onOpenMap) }
            when {
                state.isLoading -> item {
                    Box(
                        Modifier.fillMaxWidth().padding(64.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
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
                                modifier = Modifier.padding(horizontal = 20.dp),
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
                        item {
                            CategoryGrid(
                                state.categories,
                                onOpen = { navigate(Events(it)) },
                                modifier = Modifier.padding(top = 21.dp),
                            )
                        }
                    }
                    if (state.cities.size > 1) {
                        item {
                            CityCarousel(
                                state.cities,
                                state.city?.slug,
                                onOpenAll = { navigate(Cities) },
                                onSelect = { navigate(CityPage(it)) },
                                modifier = Modifier.padding(top = 33.dp),
                            )
                        }
                    }
                    if (state.organizers.isNotEmpty()) {
                        item {
                            OrganizerList(
                                state.organizers,
                                onOpen = { navigate(OrganizerPage(it)) },
                                modifier = Modifier.padding(top = 32.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    navigate: (NavKey) -> Unit,
    onOpenMap: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
    ) {
        Box(
            Modifier.size(
                44.dp,
            ).clip(CircleShape).background(AroundColors.tabSelected).clickable { navigate(Settings) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Person, contentDescription = strings.settingsTitle, tint = AroundColors.secondary)
        }
        Text(
            strings.tabDiscover,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AroundColors.ink,
            modifier = Modifier.weight(1f),
        )
        HeaderActions(
            HeaderAction(Icons.Outlined.Map, strings.mapTitle, onOpenMap),
            HeaderAction(Icons.Outlined.Search, strings.searchPlaceholder) { navigate(Search) },
        )
    }
}

/** "Los Angeles ›" over "Upcoming Events": 20sp semibold, the subtitle in light grey, a small grey chevron. */
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
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AroundColors.ink)
            if (onClick != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AroundColors.chevron,
                )
            }
        }
        subtitle?.let { Text(it, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AroundColors.tertiary) }
    }
}

@Composable
private fun UpcomingShelf(
    state: DiscoverUiState,
    onOpenAll: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val strings = LocalAroundStrings.current
    val columnWidth = (LocalConfiguration.current.screenWidthDp - SHELF_PEEK_DP).dp
    Column {
        SectionTitle(state.city?.name ?: strings.tabFeed, strings.discoverUpcoming, onOpenAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(state.upcoming.chunked(SHELF_ROWS)) { column: List<Event> ->
                Column(Modifier.width(columnWidth)) {
                    column.forEachIndexed { index, event ->
                        EventRow(
                            event,
                            onClick = { onOpen(event.id) },
                            compact = true,
                            showsSeparator = index < column.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

/** White chips with a hairline border, an icon and a 15sp label, in two rows that scroll sideways together. */
@Composable
private fun CategoryGrid(
    categories: List<CategoryCount>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val half = (categories.size + 1) / 2
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(strings.discoverCategories)
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(9.5.dp)) {
                    listOf(categories.take(half), categories.drop(half)).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            row.forEach { item -> CategoryChip(item, onOpen) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    item: CategoryCount,
    onOpen: (String) -> Unit,
) {
    val strings = LocalAroundStrings.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.5.dp),
        modifier = Modifier
            .height(38.5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AroundColors.surface)
            .border(1.dp, AroundColors.chipBorder, RoundedCornerShape(12.dp))
            .clickable { onOpen(item.key) }
            .padding(start = 10.5.dp, end = 13.dp),
    ) {
        Icon(
            categoryIcon(item.key),
            contentDescription = null,
            tint = categoryTint(item.key),
            modifier = Modifier.size(20.dp),
        )
        Text(categoryLabel(item.key, strings), fontSize = 15.sp, color = AroundColors.ink)
    }
}

/** 174dp square cards with a shade, an icon in a translucent circle and the name; gradients stand in for photos. */
@Composable
private fun CityCarousel(
    cities: List<City>,
    current: String?,
    onOpenAll: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(strings.discoverCities, onClick = onOpenAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            items(cities.size) { index ->
                val city = cities[index]
                Box(
                    Modifier
                        .size(174.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(cityGradient(index)))
                        .clickable { onSelect(city.slug) },
                ) {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(SHADE_START to Color.Transparent, 1f to ShadeEnd),
                        ),
                    )
                    Box(
                        Modifier.padding(
                            start = 12.dp,
                            top = 11.dp,
                        ).size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (city.slug == current) Icons.Filled.Check else Icons.Outlined.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                    Text(
                        city.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    )
                }
            }
        }
    }
}

/** "Featured calendars": a 60dp picture, the name in 17sp and two grey lines about them. */
@Composable
private fun OrganizerList(
    organizers: List<Organizer>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(17.dp)) {
        SectionTitle(strings.discoverOrganizers)
        organizers.forEach { organizer ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.5.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    onOpen(
                        organizer.slug,
                    )
                }.padding(horizontal = 20.dp, vertical = 2.dp),
            ) {
                Avatar(organizer.avatarUrl, organizer.name, size = 60.dp, cornerRadius = 10.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(organizer.name, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = AroundColors.ink)
                    Text(
                        organizer.bio ?: strings.eventsCount("${organizer.upcomingCount}"),
                        fontSize = 15.sp,
                        color = AroundColors.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
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
        Text(strings.feedEmptyTitle, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AroundColors.ink)
        Text(message, textAlign = TextAlign.Center, color = AroundColors.secondary)
        TextButton(onClick = onRetry) { Text(strings.retry) }
    }
}
