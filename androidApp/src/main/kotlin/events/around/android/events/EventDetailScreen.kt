package events.around.android.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import events.around.android.ui.components.ErrorBanner
import events.around.android.ui.components.InfoRow
import events.around.android.ui.components.StatusBadge
import events.around.android.ui.components.openUrl
import events.around.android.ui.components.shareText
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.formatDay
import events.around.android.ui.format.formatMoment
import events.around.android.ui.format.label
import events.around.android.ui.format.priceText
import events.around.android.ui.format.text
import events.around.android.ui.format.whenText
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.EVENT_LANGUAGES
import events.around.core.model.EventStatus
import events.around.core.model.MyEvent
import events.around.core.model.Repeat
import events.around.i18n.AroundStrings
import events.around.shared.presentation.events.EventDetailUiState
import events.around.shared.presentation.events.EventDetailViewModel
import java.util.Locale

@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    navigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onRepeat: (Long) -> Unit,
    onFix: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventDetailContent(
        state = state,
        navigateBack = navigateBack,
        onEdit = onEdit,
        onRepeat = onRepeat,
        onFix = onFix,
        onCancel = viewModel::cancel,
        modifier = modifier,
    )
}

@Composable
private fun EventDetailContent(
    state: EventDetailUiState,
    navigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onRepeat: (Long) -> Unit,
    onFix: (Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    var confirmCancel by rememberSaveable { mutableStateOf(false) }

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
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.apiNotFound)
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            event.posterUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)),
                )
            }
            Text(event.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                event.whenText(strings, LocalAppLocale.current),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusBlock(event)
            state.error?.let { ErrorBanner(it.text(strings)) }
            Actions(
                event = event,
                isCancelling = state.isCancelling,
                onEdit = { onEdit(event.id) },
                onRepeat = { onRepeat(event.id) },
                onFix = { onFix(event.id) },
                onCancel = { confirmCancel = true },
            )
            HorizontalDivider()
            Details(event, state.nextDates)
            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(strings.cancelTitle) },
            text = { Text(strings.cancelText) },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = false
                    onCancel()
                }) { Text(strings.yesCancel, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text(strings.no) } },
        )
    }
}

/** Where the submission is and what that means for the organizer. */
@Composable
private fun StatusBlock(event: MyEvent) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    val context = LocalContext.current
    val telegram = event.telegramPost

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StatusBadge(event.status, event.status.label(strings))
        val explanation = when (event.status) {
            EventStatus.Review -> strings.reviewTakes
            EventStatus.Queued -> strings.soonOut
            EventStatus.Published -> formatMoment(telegram?.publishedAt, locale)?.let(strings.inAfishaSince)
            EventStatus.Rejected -> event.skipReason?.let(strings.reason) ?: strings.noReason
            EventStatus.Duplicate -> strings.inAfishaOther
            EventStatus.Cancelled -> formatMoment(event.cancelledAt, locale)?.let(strings.cancelledAt)
        }
        explanation?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val postUrl = telegram?.url ?: event.duplicateOf?.telegramPost?.url
            if (postUrl != null && event.status != EventStatus.Cancelled) {
                OutlinedButton(onClick = { context.openUrl(postUrl) }) { Text(strings.openPost) }
            }
            event.threadsPost?.url?.takeIf { event.status != EventStatus.Cancelled }?.let { url ->
                OutlinedButton(onClick = { context.openUrl(url) }) { Text(strings.openThreads) }
            }
        }
    }
}

@Composable
private fun Actions(
    event: MyEvent,
    isCancelling: Boolean,
    onEdit: () -> Unit,
    onRepeat: () -> Unit,
    onFix: () -> Unit,
    onCancel: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (event.canEdit) {
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text(strings.edit) }
        }
        if (event.canFix) {
            Button(onClick = onFix, modifier = Modifier.fillMaxWidth()) { Text(strings.fixResend) }
        }
        if (event.canRepeat) {
            OutlinedButton(onClick = onRepeat, modifier = Modifier.fillMaxWidth()) { Text(strings.repeatEvent) }
        }
        event.telegramPost?.url?.takeIf { event.status != EventStatus.Cancelled }?.let { url ->
            OutlinedButton(onClick = { context.shareText(url, event.title) }, modifier = Modifier.fillMaxWidth()) {
                Text(strings.share)
            }
        }
        if (event.canCancel) {
            TextButton(
                onClick = onCancel,
                enabled = !isCancelling,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isCancelling) CircularProgressIndicator(Modifier.height(18.dp)) else Text(strings.cancelEvent)
            }
        }
    }
}

@Composable
private fun Details(event: MyEvent, nextDates: List<String>) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    Column {
        event.description?.takeIf { it.isNotBlank() }?.let { InfoRow(strings.detailDescription, it) }
        val place = listOfNotNull(event.venue, event.address).joinToString(", ")
        if (place.isNotEmpty()) InfoRow(strings.detailAddress, place)
        event.priceText(strings)?.let { price ->
            InfoRow(strings.formPricing, listOfNotNull(price, event.priceNote).joinToString(" · "))
        }
        event.ticketsUrl?.let { InfoRow(strings.detailTickets, it) }
        event.eventUrl?.let { InfoRow(strings.detailLink, it) }
        event.category?.let { InfoRow(strings.detailCategory, categoryLabel(it, strings)) }
        if (event.languages.isNotEmpty()) {
            val names = event.languages.map { code -> EVENT_LANGUAGES.firstOrNull { it.first == code }?.second ?: code }
            InfoRow(strings.detailLanguages, names.joinToString(", "))
        }
        if (event.withKids == true) InfoRow(strings.detailWithKids, "✓")
        event.recurrence?.let { recurrence ->
            InfoRow(strings.detailRepeats, repeatRule(recurrence.freq, recurrence.until, strings, locale))
        }
        if (nextDates.isNotEmpty()) {
            InfoRow(strings.nextDates, nextDates.mapNotNull { formatDay(it, locale) }.joinToString(", "))
        }
    }
}

private fun repeatRule(freq: String, until: String, strings: AroundStrings, locale: Locale): String {
    val every = if (Repeat.fromApi(freq) == Repeat.Biweekly) strings.everyBiweekly else strings.everyWeekly
    return strings.repeatRule(every, formatDay(until, locale) ?: until)
}
