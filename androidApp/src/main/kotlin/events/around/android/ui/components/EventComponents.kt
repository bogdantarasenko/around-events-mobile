package events.around.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.format.headingParts
import events.around.android.ui.format.priceText
import events.around.android.ui.format.shortWhen
import events.around.android.ui.format.timeText
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.Event
import events.around.shared.domain.feed.CategoryCount
import events.around.shared.domain.feed.FeedDay

private val PriceGreen = Color(0xFF1E8E3E)

/** An emoji per category: the chips on the home screen and the stand-in when an event has no poster. */
fun categoryEmoji(key: String?): String = when (key) {
    "concert", "music" -> "🎵"
    "standup" -> "🎤"
    "theatre" -> "🎭"
    "party" -> "🪩"
    "festival" -> "🎪"
    "exhibition" -> "🖼️"
    "kids" -> "🧸"
    "market" -> "🛍️"
    "food" -> "🍽️"
    "cinema" -> "🎬"
    "lecture", "learning" -> "🎓"
    "sport" -> "⚽"
    "games" -> "🎲"
    "creative" -> "🎨"
    else -> "✨"
}

private val Purple = Color(0xFF8E44AD)
private val Red = Color(0xFFE53935)
private val Pink = Color(0xFFD81B60)
private val Orange = Color(0xFFF57C00)
private val Yellow = Color(0xFFF9A825)
private val Brown = Color(0xFF795548)
private val Blue = Color(0xFF1E88E5)
private val Green = Color(0xFF43A047)
private val Teal = Color(0xFF00897B)

fun categoryTint(key: String?): Color = when (key) {
    "concert", "music", "standup" -> Purple
    "theatre", "cinema" -> Red
    "party", "festival" -> Pink
    "exhibition", "creative" -> Orange
    "kids", "games" -> Yellow
    "market", "food" -> Brown
    "lecture", "learning" -> Blue
    "sport" -> Green
    else -> Teal
}

/** The event's poster, or its category's emoji on a soft tint when there is none. */
@Composable
fun EventThumb(
    event: Event,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    cornerRadius: Dp = 10.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier.size(size).clip(shape).background(categoryTint(event.category).copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(categoryEmoji(event.category), fontSize = (size.value * 0.4f).sp)
        event.posterUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        }
    }
}

/** A picture from a URL, or the first letter of [name] on grey. */
@Composable
fun Avatar(
    url: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    cornerRadius: Dp? = null,
) {
    val shape = cornerRadius?.let { RoundedCornerShape(it) } ?: CircleShape
    Box(
        modifier = modifier.size(size).clip(shape).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.take(1).uppercase(),
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        url?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        }
    }
}

@Composable
fun PriceBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = PriceGreen,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(PriceGreen.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** A line of every event list in the designs: thumbnail, who runs it and the price, the title, when and where. */
@Composable
fun EventRow(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showsDay: Boolean = false,
) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        EventThumb(event)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val organizer = event.organizer
                if (organizer != null) {
                    Avatar(organizer.avatarUrl, organizer.name, size = 14.dp)
                }
                Text(
                    organizer?.name ?: event.category?.let { categoryLabel(it, strings) }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                event.priceText(strings)?.let { PriceBadge(it) }
            }
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val time = if (showsDay) event.shortWhen(strings, locale) else event.timeText()
                time?.let { IconText(Icons.Outlined.Schedule, it) }
                event.venue?.let { IconText(Icons.Outlined.Place, it, Modifier.weight(1f, fill = false)) }
            }
        }
    }
}

@Composable
private fun IconText(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** "8 November / Wednesday": the day in full colour, the weekday muted. */
@Composable
fun DayHeader(
    day: FeedDay,
    modifier: Modifier = Modifier,
) {
    val (main, weekday) = day.headingParts(LocalAroundStrings.current, LocalAppLocale.current)
    Row(modifier = modifier.padding(top = 18.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(main, style = MaterialTheme.typography.titleMedium)
        weekday?.let {
            Text("/", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
            Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** List items of events by day, each opening the event. */
fun LazyListScope.eventDays(
    days: List<FeedDay>,
    onOpen: (Long) -> Unit,
) {
    days.forEach { day ->
        item(key = "day-${day.date}") { DayHeader(day) }
        items(day.events, key = { "${day.date}-${it.id}" }) { event -> EventRow(event, onClick = { onOpen(event.id) }) }
    }
}

/** "Music 12 · Sport 4 …": outlined chips with counts that filter a list by category. */
@Composable
fun CategoryFilter(
    categories: List<CategoryCount>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(categories, key = { it.key }) { item ->
            val isOn = item.key == selected
            val content = if (isOn) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isOn) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .clickable { onSelect(if (isOn) null else item.key) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(categoryLabel(item.key, strings), style = MaterialTheme.typography.bodyMedium, color = content)
                Text("${item.count}", style = MaterialTheme.typography.bodyMedium, color = content.copy(alpha = 0.6f))
            }
        }
    }
}
