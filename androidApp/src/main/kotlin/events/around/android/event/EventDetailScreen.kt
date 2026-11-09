package events.around.android.event

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import events.around.android.map.MapLibreMap
import events.around.android.ui.components.Avatar
import events.around.android.ui.components.EventThumb
import events.around.android.ui.components.categoryTint
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

private val Backdrop = Color(0xFF696969)
private val Muted = Color(0xFFB3B3B3)
private val Glass = Color.White.copy(alpha = 0.18f)
private const val MAX_ACTIONS = 3
private val MAP_PREVIEW_HEIGHT = 140.dp
private val DividerThickness = 0.8.dp
private val SectionLabel = Color(0xFFAFAFAF)
private const val SCREEN_MARGINS_DP = 40

/** The event as in the designs: dark, over its own blurred poster, with the poster card, actions and details. */
@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    navigateBack: () -> Unit,
    onOpenOrganizer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    Box(modifier.fillMaxSize().background(Backdrop)) {
        val event = state.event
        if (event == null) {
            Text(strings.eventGone, color = Muted, modifier = Modifier.align(Alignment.Center))
        } else {
            EventBackdrop(event)
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 32.dp),
            ) {
                EventThumb(
                    event,
                    size = (LocalConfiguration.current.screenWidthDp - SCREEN_MARGINS_DP).dp,
                    cornerRadius = 20.dp,
                    modifier = Modifier.shadow(20.dp, RoundedCornerShape(20.dp)),
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        event.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(event.whenText(strings, LocalAppLocale.current), color = Muted)
                }
                Actions(event)
                Facts(event, state.nextDates, onOpenOrganizer)
            }
        }
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp)) {
            GlassButton(Icons.AutoMirrored.Filled.ArrowBack, strings.back, navigateBack)
            Box(Modifier.weight(1f))
            val context = LocalContext.current
            val shared = event?.let { linkOf(it.postUrl ?: it.eventUrl) }
            if (event != null && shared != null) {
                GlassButton(
                    Icons.Outlined.Share,
                    strings.share,
                ) { context.shareText("${event.title}\n$shared", event.title) }
            }
        }
    }
}

@Composable
private fun EventBackdrop(event: Event) {
    val poster = event.posterUrl
    if (poster != null) {
        AsyncImage(
            poster,
            null,
            contentScale = ContentScale.Crop,
            alpha = 0.55f,
            modifier = Modifier.fillMaxSize().blur(60.dp),
        )
    } else {
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(categoryTint(event.category).copy(alpha = 0.45f), Color.Transparent)),
            ),
        )
    }
}

@Composable
private fun GlassButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp).clip(CircleShape).background(Glass)) {
        Icon(icon, contentDescription = label, tint = Color.White)
    }
}

/**
 * Three 55dp buttons under the title, as Register / Contact / More in the designs: the first white, the others
 * translucent. Ours are tickets or the link, the channel post, the map and sharing — the first three there are.
 */
@Composable
private fun Actions(event: Event) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    val place = listOfNotNull(event.venue, event.address).joinToString(", ")
    val shared = linkOf(event.postUrl ?: event.ticketsUrl ?: event.eventUrl)
    val actions = buildList {
        linkOf(
            event.ticketsUrl,
        )?.let { add(Triple(strings.detailTickets, Icons.Outlined.ConfirmationNumber) { context.openUrl(it) }) }
            ?: linkOf(
                event.eventUrl,
            )?.let { add(Triple(strings.detailLink, Icons.Outlined.Link) { context.openUrl(it) }) }
        linkOf(
            event.postUrl,
        )?.let { add(Triple(strings.openPost, Icons.AutoMirrored.Outlined.Send) { context.openUrl(it) }) }
        if (place.isNotEmpty()) {
            add(
                Triple(strings.mapTitle, Icons.Outlined.Map) { context.openExternal(geoUri(event, place)) },
            )
        }
        shared?.let {
            add(
                Triple(strings.share, Icons.Outlined.Share) { context.shareText("${event.title}\n$it", event.title) },
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.take(
            MAX_ACTIONS,
        ).forEachIndexed { index, (title, icon, run) -> ActionButton(title, icon, index == 0, run) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ActionButton(
    title: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val content = if (primary) Color(0xFF141414) else Color(0xFFE9E9E9)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .height(55.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (primary) Color.White else Glass)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Text(title, color = content, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** Location, organizer, price, languages, repeats and the description — in the designs' sections. */
@Composable
private fun Facts(
    event: Event,
    nextDates: List<String>,
    onOpenOrganizer: (String) -> Unit,
) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Location(event)
        event.organizer?.let { organizer ->
            Section(strings.detailOrganizer) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onOpenOrganizer(organizer.slug) },
                ) {
                    Avatar(organizer.avatarUrl, organizer.name, size = 32.dp)
                    Text(
                        organizer.name,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
                }
            }
        }
        event.priceText(strings)?.let { Section(strings.detailPrice) { Text(it, color = Color.White) } }
        if (event.languages.isNotEmpty()) {
            val names = EVENT_LANGUAGES.toMap()
            Section(
                strings.detailLanguages,
            ) { Text(event.languages.joinToString(", ") { names[it] ?: it }, color = Color.White) }
        }
        if (event.withKids == true) Section(strings.detailWithKids) { Text("✓", color = Color.White) }
        event.recurrence?.let { recurrence ->
            val every = if (recurrence.freq == "biweekly") strings.everyBiweekly else strings.everyWeekly
            val until = formatDay(recurrence.until, locale) ?: recurrence.until
            Section(strings.detailRepeats) { Text(strings.repeatRule(every, until), color = Color.White) }
        }
        if (nextDates.isNotEmpty()) {
            Section(
                strings.nextDates,
            ) { Text(nextDates.mapNotNull { formatDay(it, locale) }.joinToString(", "), color = Color.White) }
        }
        event.description?.takeIf { it.isNotBlank() }?.let {
            Section(
                strings.detailAbout,
            ) { Text(it, color = Color.White) }
        }
        event.category?.let {
            Text(
                "# " + categoryLabel(it, strings),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(CircleShape).background(Glass).padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun Location(event: Event) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    val place = listOfNotNull(event.venue, event.address).joinToString(", ")
    if (place.isEmpty()) return
    Section(strings.detailLocation) {
        event.venue?.let { Text(it, color = Color.White, fontWeight = FontWeight.Medium) }
        event.address?.let { Text(it, color = Muted) }
        if (event.latitude != null && event.longitude != null) {
            MapLibreMap(
                events = listOf(event),
                interactive = false,
                modifier = Modifier.fillMaxWidth().height(MAP_PREVIEW_HEIGHT).clip(RoundedCornerShape(18.dp)),
            )
        }
        TextButton(onClick = { context.openExternal(geoUri(event, place)) }) {
            Text(strings.openMap)
        }
    }
}

// The maps app: at the point when the backend found one, by the text otherwise.
private fun geoUri(event: Event, place: String): String {
    val point = event.latitude?.let { lat -> event.longitude?.let { lon -> "$lat,$lon" } } ?: "0,0"
    return "geo:$point?q=" + Uri.encode("$place, ${event.city.name}")
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = SectionLabel, fontSize = 15.sp)
        HorizontalDivider(thickness = DividerThickness, color = Color.White.copy(alpha = 0.2f))
        content()
    }
}
