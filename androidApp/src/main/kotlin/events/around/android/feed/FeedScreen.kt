package events.around.android.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.BuildConfig
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.Poster
import events.around.android.ui.components.SectionHeader
import events.around.android.ui.components.openUrl
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.heading
import events.around.android.ui.format.priceText
import events.around.android.ui.format.text
import events.around.android.ui.format.timeText
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.City
import events.around.core.model.Event
import events.around.shared.presentation.feed.FeedUiState
import events.around.shared.presentation.feed.FeedViewModel

@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onOpenEvent: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { CityTitle(state.city, state.cities, onSelect = viewModel::selectCity) },
                navigationIcon = {
                    // The mini-app opened in a browser takes submissions outside Telegram too.
                    IconButton(onClick = { context.openUrl(BuildConfig.BASE_URL) }) {
                        Icon(Icons.Default.Add, contentDescription = strings.feedSubmit)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = strings.settingsTitle)
                    }
                },
            )
        },
    ) { padding ->
        FeedContent(
            state = state,
            onRefresh = viewModel::refresh,
            onCategory = viewModel::selectCategory,
            onOpenEvent = onOpenEvent,
            modifier = Modifier.padding(padding),
        )
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
    val name = city?.name ?: strings.tabFeed
    if (cities.size < 2) {
        Text(name)
        return
    }
    Box {
        TextButton(onClick = { open = true }) {
            Text(name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Icon(Icons.Default.ArrowDropDown, contentDescription = strings.feedCity)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            cities.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        open = false
                        onSelect(option.slug)
                    },
                )
            }
        }
    }
}

@Composable
private fun FeedContent(
    state: FeedUiState,
    onRefresh: () -> Unit,
    onCategory: (String?) -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    PullToRefreshBox(
        isRefreshing = state.isRefreshing && !state.isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.days.isEmpty() && state.category == null && (state.isEmpty || state.error != null) ->
                EmptyState(message = state.error?.text(strings) ?: strings.feedEmptyText, onRetry = onRefresh)
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                state.error?.let { error ->
                    item {
                        ErrorBanner(
                            error.text(strings),
                            actionLabel = strings.retry,
                            onAction = onRefresh,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                if (state.categories.isNotEmpty()) {
                    item { CategoryFilter(state.categories, state.category, onCategory) }
                }
                if (state.days.isEmpty()) {
                    item {
                        Text(
                            strings.feedCategoryEmpty,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                }
                state.days.forEach { day ->
                    item(key = "day-${day.date}") { SectionHeader(day.heading(strings, locale)) }
                    items(day.events, key = { "${day.date}-${it.id}" }) { event ->
                        EventRow(event = event, onClick = { onOpenEvent(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    val strings = LocalAroundStrings.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text(strings.feedAll) })
        }
        items(categories) { key ->
            FilterChip(
                selected = selected == key,
                onClick = { onSelect(key) },
                label = { Text(categoryLabel(key, strings)) },
            )
        }
    }
}

/** A line of the feed. The day is the section heading, so the row starts with the time. */
@Composable
private fun EventRow(
    event: Event,
    onClick: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (event.posterUrl != null) Poster(url = event.posterUrl, size = 72.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            event.timeText()?.let {
                Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            event.venue?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val tags = listOfNotNull(event.category?.let { categoryLabel(it, strings) }, event.priceText(strings))
            if (tags.isNotEmpty()) {
                Text(
                    tags.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    onRetry: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.feedEmptyTitle, style = MaterialTheme.typography.titleLarge)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) { Text(strings.retry) }
        }
    }
}
