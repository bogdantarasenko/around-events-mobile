package events.around.android.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.SectionHeader
import events.around.android.ui.components.openUrl
import events.around.android.ui.components.shareText
import events.around.android.ui.format.text
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.presentation.organizer.OrganizerViewModel
import events.around.shared.presentation.session.SessionUiState

@Composable
fun ProfileScreen(
    session: SessionUiState,
    viewModel: OrganizerViewModel,
    onEditPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val organizer = state.organizer

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            session.photoUrl?.let {
                AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(56.dp).clip(CircleShape))
            }
            Column {
                Text(
                    strings.signedAs,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(session.displayName.orEmpty(), style = MaterialTheme.typography.titleLarge)
            }
        }

        SectionHeader(strings.pageTitle)
        when {
            state.isLoading && !state.isLoaded -> CircularProgressIndicator()
            state.error != null && !state.isLoaded -> ErrorBanner(
                state.error?.text(strings) ?: strings.pageLoadFailed,
                actionLabel = strings.retry,
                onAction = viewModel::load,
            )
            organizer == null -> {
                Text(
                    strings.pageHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = {
                    viewModel.startEditing()
                    onEditPage()
                }) { Text(strings.pageCreate) }
            }
            else -> {
                Text(organizer.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    organizer.url + if (organizer.isPublic) "" else " · ${strings.pageHidden}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                organizer.bio?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                Text(
                    "${state.upcomingCount} ${strings.upcomingHeader.lowercase()} · ${strings.pastCount(
                        state.pastCount.toString(),
                    )}",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (state.justSaved) Text(strings.pageSaved, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (organizer.isPublic) {
                        Button(onClick = { context.openUrl(organizer.url) }) { Text(strings.pageOpen) }
                        OutlinedButton(
                            onClick = { context.shareText(organizer.url, organizer.name) },
                        ) { Text(strings.share) }
                    }
                    OutlinedButton(onClick = {
                        viewModel.startEditing()
                        onEditPage()
                    }) { Text(strings.pageEdit) }
                }
            }
        }
    }
}
