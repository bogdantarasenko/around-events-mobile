package events.around.android.organizer

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import events.around.android.ui.components.Avatar
import events.around.android.ui.components.BackScaffold
import events.around.android.ui.components.CategoryFilter
import events.around.android.ui.components.eventDays
import events.around.android.ui.components.openUrl
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.core.model.Organizer
import events.around.shared.presentation.feed.OrganizerViewModel

private val CoverPurple = Color(0x668E24AA)

/** An organizer's page: cover, who they are, links, "Follow" in the bot, and their events by day. */
@Composable
fun OrganizerScreen(
    viewModel: OrganizerViewModel,
    navigateBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    BackScaffold(navigateBack, modifier) { padding ->
        val organizer = state.organizer
        when {
            organizer != null -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    item { OrganizerHeader(organizer) }
                    if (state.filters.isNotEmpty()) {
                        item {
                            CategoryFilter(
                                state.filters,
                                state.filter,
                                viewModel::selectFilter,
                                Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                    if (state.days.isEmpty()) {
                        item {
                            Text(
                                strings.organizerEmpty,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                    eventDays(state.days, onOpenEvent)
                }
            }
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                Text(
                    state.error?.text(strings) ?: strings.apiNotFound,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = viewModel::refresh) { Text(strings.retry) }
            }
        }
    }
}

@Composable
private fun OrganizerHeader(organizer: Organizer) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    Column {
        // No cover on the backend: the avatar, blurred, stands in for it.
        Box(
            Modifier
                .fillMaxWidth()
                .height(223.dp)
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CoverPurple),
                    ),
                ),
        ) {
            organizer.avatarUrl?.let {
                AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().blur(24.dp))
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Box(
                Modifier
                    .offset(y = (-44).dp)
                    .shadow(2.dp, RoundedCornerShape(14.dp))
                    .background(AroundColors.surface, RoundedCornerShape(14.dp))
                    .padding(3.5.dp),
            ) {
                Avatar(organizer.avatarUrl, organizer.name, size = 80.dp, cornerRadius = 10.dp)
            }
            Box(Modifier.weight(1f))
            organizer.followUrl?.let { url ->
                Text(
                    strings.follow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AroundColors.surface,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(CircleShape)
                        .background(AroundColors.ink)
                        .clickable { context.openUrl(url) }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.offset(y = (-24).dp).padding(horizontal = 16.dp),
        ) {
            Text(organizer.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AroundColors.ink)
            organizer.bio?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 15.sp, color = AroundColors.secondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (organizer.pastCount > 0) Muted(strings.pastCount("${organizer.pastCount}"))
                Muted(strings.eventsCount("${organizer.events.size}"))
            }
            Row {
                LinkIcon(organizer.links.instagram, Icons.Outlined.CameraAlt)
                LinkIcon(organizer.links.telegram, Icons.AutoMirrored.Outlined.Send)
                LinkIcon(organizer.links.website, Icons.Outlined.Language)
            }
        }
    }
}

@Composable
private fun Muted(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun LinkIcon(
    url: String?,
    icon: ImageVector,
) {
    val context = LocalContext.current
    if (url != null) {
        IconButton(onClick = { context.openUrl(url) }) {
            Icon(icon, contentDescription = url, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
