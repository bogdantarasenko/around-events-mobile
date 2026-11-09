package events.around.android.events

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import events.around.android.EventDetail
import events.around.android.OrganizerPage
import events.around.android.ui.components.Avatar
import events.around.android.ui.components.BackScaffold
import events.around.android.ui.components.CategoryFilter
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.categoryIcon
import events.around.android.ui.components.categoryTint
import events.around.android.ui.components.eventDays
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.shared.presentation.feed.EventsUiState
import events.around.shared.presentation.feed.EventsViewModel

private const val ORGANIZERS_SHOWN = 5

/** The city's listing by day, or a category's page: its title, count, organizers and events. */
@Composable
fun EventsScreen(
    viewModel: EventsViewModel,
    navigateBack: () -> Unit,
    onOpen: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    BackScaffold(navigateBack, modifier) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                item { Header(state) }
                state.error?.let { error ->
                    item {
                        ErrorBanner(
                            error.text(strings),
                            actionLabel = strings.retry,
                            onAction = viewModel::refresh,
                        )
                    }
                }
                if (state.filters.isNotEmpty()) {
                    item { CategoryFilter(state.filters, state.filter, viewModel::selectFilter) }
                }
                if (state.category != null && state.organizers.isNotEmpty()) {
                    item {
                        Text(
                            strings.discoverOrganizers,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(state.organizers.take(ORGANIZERS_SHOWN), key = { it.slug }) { organizer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                onOpen(
                                    OrganizerPage(organizer.slug),
                                )
                            }.padding(vertical = 6.dp),
                        ) {
                            Avatar(organizer.avatarUrl, organizer.name, size = 40.dp, cornerRadius = 8.dp)
                            Text(organizer.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                when {
                    state.isLoading -> item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.days.isEmpty() -> item {
                        Text(
                            strings.feedCategoryEmpty,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    }
                }
                if (state.category != null) {
                    item {
                        Column(Modifier.padding(top = 8.dp)) {
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(AroundColors.chipBorder))
                            Text(
                                strings.discoverUpcoming,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AroundColors.ink,
                                modifier = Modifier.padding(top = 18.dp),
                            )
                        }
                    }
                }
                eventDays(state.days) { onOpen(EventDetail(it)) }
            }
        }
    }
}

@Composable
private fun Header(state: EventsUiState) {
    val strings = LocalAroundStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        state.category?.let { category ->
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(categoryTint(category).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    categoryIcon(category),
                    contentDescription = null,
                    tint = categoryTint(category),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Text(
            state.category?.let { categoryLabel(it, strings) } ?: state.city?.name ?: strings.allEvents,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        // "3K Events" in the designs: the number in ink, the word in grey.
        val word = strings.eventsCount("${state.count}").replace("${state.count}", "").trim(' ', ':').lowercase()
        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(color = AroundColors.ink, fontWeight = FontWeight.Medium),
                ) { append("${state.count}") }
                withStyle(SpanStyle(color = AroundColors.secondary)) { append(" $word") }
            },
            fontSize = 15.sp,
        )
    }
}
