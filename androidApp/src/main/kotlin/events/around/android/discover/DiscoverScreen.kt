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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.Cities
import events.around.android.CityPage
import events.around.android.Events
import events.around.android.OrganizerPage
import events.around.android.Search
import events.around.android.city.cityGradient
import events.around.android.ui.components.Avatar
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.categoryIcon
import events.around.android.ui.components.categoryTint
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.core.model.City
import events.around.core.model.Organizer
import events.around.shared.domain.feed.CategoryCount
import events.around.shared.presentation.feed.DiscoverUiState
import events.around.shared.presentation.feed.DiscoverViewModel

private const val SHADE_START = 0.5f

private val ShadeEnd = Color.Black.copy(alpha = 0.45f)

/**
 * Discover: the way around the city — its categories, the other cities and the organizers. The listing itself
 * lives on the Home tab, so nothing here repeats it.
 */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    navigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(strings.tabDiscover) },
                actions = {
                    IconButton(onClick = { navigate(Search) }) {
                        Icon(Icons.Outlined.Search, contentDescription = strings.searchPlaceholder)
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
                discoverContent(state, viewModel, navigate, strings.retry)
            }
        }
    }
}

private fun LazyListScope.discoverContent(
    state: DiscoverUiState,
    viewModel: DiscoverViewModel,
    navigate: (NavKey) -> Unit,
    retry: String,
) {
    when {
        state.isLoading -> item {
            Box(
                Modifier.fillMaxWidth().padding(64.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
        }
        state.categories.isEmpty() && (state.isEmpty || state.error != null) -> item {
            Empty(state.error?.text(LocalAroundStrings.current), viewModel::refresh)
        }
        else -> {
            state.error?.let { error ->
                item {
                    ErrorBanner(
                        error.text(LocalAroundStrings.current),
                        actionLabel = retry,
                        onAction = viewModel::refresh,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
            if (state.categories.isNotEmpty()) {
                categoryTiles(state.categories) { navigate(Events(it)) }
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

private const val TILE_COLUMNS = 2

/** The categories the city has, as tiles in two columns: the icon on its tint, the name and how many events. */
private fun LazyListScope.categoryTiles(
    categories: List<CategoryCount>,
    onOpen: (String) -> Unit,
) {
    item { SectionTitle(LocalAroundStrings.current.discoverCategories) }
    items(categories.chunked(TILE_COLUMNS)) { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp),
        ) {
            row.forEach { item -> CategoryTile(item, onOpen, Modifier.weight(1f)) }
            if (row.size < TILE_COLUMNS) Box(Modifier.weight(1f))
        }
    }
}

@Composable
private fun CategoryTile(
    item: CategoryCount,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AroundColors.surface)
            .border(1.dp, AroundColors.chipBorder, RoundedCornerShape(14.dp))
            .clickable { onOpen(item.key) }
            .padding(12.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(categoryTint(item.key).copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                categoryIcon(item.key),
                contentDescription = null,
                tint = categoryTint(item.key),
                modifier = Modifier.size(20.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                categoryLabel(item.key, strings),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AroundColors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(strings.eventsCount("${item.count}"), fontSize = 12.sp, color = AroundColors.secondary)
        }
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
    error: String?,
    onRetry: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    val message = error ?: strings.feedEmptyText
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
