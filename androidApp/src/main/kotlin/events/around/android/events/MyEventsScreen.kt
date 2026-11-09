package events.around.android.events

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.Poster
import events.around.android.ui.components.SectionHeader
import events.around.android.ui.components.StatusBadge
import events.around.android.ui.format.label
import events.around.android.ui.format.text
import events.around.android.ui.format.whenText
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.EventStatus
import events.around.core.model.MyEvent
import events.around.shared.presentation.events.MyEventsUiState
import events.around.shared.presentation.events.MyEventsViewModel

@Composable
fun MyEventsScreen(
    viewModel: MyEventsViewModel,
    onOpenEvent: (Long) -> Unit,
    onAddEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MyEventsContent(
        state = state,
        onRefresh = viewModel::refresh,
        onFilter = viewModel::setFilter,
        onOpenEvent = onOpenEvent,
        onAddEvent = onAddEvent,
        modifier = modifier,
    )
}

@Composable
private fun MyEventsContent(
    state: MyEventsUiState,
    onRefresh: () -> Unit,
    onFilter: (EventStatus?) -> Unit,
    onOpenEvent: (Long) -> Unit,
    onAddEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    PullToRefreshBox(
        isRefreshing = state.isRefreshing && !state.isLoading,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.isEmpty -> EmptyState(onAddEvent)
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
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
                item { StatusFilter(selected = state.filter, onFilter = onFilter) }
                state.groups.forEach { group ->
                    item(key = "header-${group.kind}") { SectionHeader(group.kind.label(strings)) }
                    items(group.events, key = { it.id }) { event ->
                        EventRow(event = event, onClick = { onOpenEvent(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilter(
    selected: EventStatus?,
    onFilter: (EventStatus?) -> Unit,
) {
    val strings = LocalAroundStrings.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            FilterChip(selected = selected == null, onClick = { onFilter(null) }, label = { Text(strings.filterAll) })
        }
        items(EventStatus.entries) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onFilter(status) },
                label = { Text(status.label(strings)) },
            )
        }
    }
}

@Composable
private fun EventRow(
    event: MyEvent,
    onClick: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Poster(url = event.posterUrl, size = 72.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            StatusBadge(event.status, event.status.label(strings))
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                event.whenText(strings, locale),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        }
    }
}

@Composable
private fun EmptyState(onAddEvent: () -> Unit) {
    val strings = LocalAroundStrings.current
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.mineEmptyTitle, style = MaterialTheme.typography.titleLarge)
            Text(
                strings.mineEmptyText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onAddEvent) { Text(strings.mineAddEvent) }
        }
    }
}
