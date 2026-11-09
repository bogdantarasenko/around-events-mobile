package events.around.android.feed

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import events.around.android.ui.components.InfoRow
import events.around.android.ui.components.openExternal
import events.around.android.ui.components.openUrl
import events.around.android.ui.components.shareText
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.formatDay
import events.around.android.ui.format.linkOf
import events.around.android.ui.format.priceText
import events.around.android.ui.format.whenText
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.EVENT_LANGUAGES
import events.around.core.model.Event
import events.around.shared.presentation.feed.EventDetailViewModel

@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
            )
        },
    ) { padding ->
        val event = state.event
        if (event == null) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(strings.eventGone, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EventHeader(event)
                EventActions(event)
                HorizontalDivider()
                EventFacts(event, state.nextDates)
            }
        }
    }
}

@Composable
private fun EventHeader(
    event: Event,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        event.posterUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            )
        }
        event.category?.let {
            Text(
                categoryLabel(it, strings).uppercase(locale),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(event.title, style = MaterialTheme.typography.headlineSmall)
        Text(event.whenText(strings, locale), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EventActions(event: Event) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    val tickets = linkOf(event.ticketsUrl ?: event.eventUrl)
    val post = linkOf(event.postUrl)
    val shared = post ?: linkOf(event.eventUrl)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tickets?.let {
            Button(onClick = { context.openUrl(it) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (event.ticketsUrl != null) strings.detailTickets else strings.detailLink)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            post?.let {
                OutlinedButton(onClick = { context.openUrl(it) }, modifier = Modifier.weight(1f)) {
                    Text(strings.openPost)
                }
            }
            shared?.let {
                OutlinedButton(
                    onClick = { context.shareText("${event.title}\n$it", event.title) },
                    modifier = Modifier.weight(1f),
                ) { Text(strings.share) }
            }
        }
    }
}

@Composable
private fun EventFacts(
    event: Event,
    nextDates: List<String>,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    val context = LocalContext.current
    val place = listOfNotNull(event.venue, event.address).joinToString(", ")
    Column(modifier = modifier) {
        if (place.isNotEmpty()) {
            InfoRow(strings.detailAddress, place)
            // A search in the maps app: the backend has the place as text, without coordinates.
            TextButton(
                onClick = { context.openExternal("geo:0,0?q=" + Uri.encode("$place, ${event.city.name}")) },
            ) { Text(strings.openMap) }
        }
        event.priceText(strings)?.let { InfoRow(strings.detailPrice, it) }
        event.organizer?.let { organizer ->
            Text(
                strings.detailOrganizer,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            TextButton(onClick = { context.openUrl(organizer.url) }) { Text(organizer.name) }
        }
        if (event.languages.isNotEmpty()) {
            val names = EVENT_LANGUAGES.toMap()
            InfoRow(strings.detailLanguages, event.languages.joinToString(", ") { names[it] ?: it })
        }
        if (event.withKids == true) InfoRow(strings.detailWithKids, "✓")
        event.recurrence?.let { recurrence ->
            val every = if (recurrence.freq == "biweekly") strings.everyBiweekly else strings.everyWeekly
            val until = formatDay(recurrence.until, locale) ?: recurrence.until
            InfoRow(strings.detailRepeats, strings.repeatRule(every, until))
        }
        if (nextDates.isNotEmpty()) {
            InfoRow(strings.nextDates, nextDates.mapNotNull { formatDay(it, locale) }.joinToString(", "))
        }
        event.description?.takeIf { it.isNotBlank() }?.let { InfoRow(strings.detailDescription, it) }
    }
}
