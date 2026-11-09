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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Festival
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material3.Icon
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
import events.around.android.ui.theme.AroundColors
import events.around.core.model.Event
import events.around.shared.domain.feed.CategoryCount
import events.around.shared.domain.feed.FeedDay

private const val THUMB_ICON_SCALE = 0.36f
private const val AVATAR_LETTER_SCALE = 0.45f

/** An icon per category: the chips on Discover and the stand-in when an event has no poster. */
fun categoryIcon(key: String?): ImageVector = when (key) {
    "concert", "music" -> Icons.Outlined.MusicNote
    "standup" -> Icons.Outlined.Mic
    "theatre" -> Icons.Outlined.TheaterComedy
    "party" -> Icons.Outlined.Celebration
    "festival" -> Icons.Outlined.Festival
    "exhibition" -> Icons.Outlined.Image
    "kids" -> Icons.Outlined.ChildCare
    "market" -> Icons.Outlined.ShoppingBag
    "food" -> Icons.Outlined.Restaurant
    "cinema" -> Icons.Outlined.Movie
    "lecture", "learning" -> Icons.Outlined.School
    "sport" -> Icons.AutoMirrored.Outlined.DirectionsRun
    "games" -> Icons.Outlined.Casino
    "creative" -> Icons.Outlined.Palette
    else -> Icons.Outlined.AutoAwesome
}

private val Purple = Color(0xFFAF52DE)
private val Red = Color(0xFFFF3B30)
private val Pink = Color(0xFFFF2D55)
private val Orange = Color(0xFFFF9500)
private val Yellow = Color(0xFFFFCC00)
private val Brown = Color(0xFFA2845E)
private val Blue = Color(0xFF007AFF)
private val Green = Color(0xFF34C759)
private val Teal = Color(0xFF30B0C7)

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

/** The event's poster, or its category's icon on a soft tint when there is none. */
@Composable
fun EventThumb(
    event: Event,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    cornerRadius: Dp = 10.dp,
) {
    val tint = categoryTint(event.category)
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(cornerRadius)).background(tint.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            categoryIcon(event.category),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * THUMB_ICON_SCALE),
        )
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
        modifier = modifier.size(size).clip(shape).background(AroundColors.tabSelected),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.take(1).uppercase(),
            fontSize = (size.value * AVATAR_LETTER_SCALE).sp,
            fontWeight = FontWeight.SemiBold,
            color = AroundColors.secondary,
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

/** The green price tag: 12sp medium on a pale green, 5dp corners. */
@Composable
fun PriceBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = AroundColors.price,
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(AroundColors.priceBackground)
            .padding(horizontal = 6.dp, vertical = 1.5.dp),
    )
}

/**
 * The event line of the designs: an 80dp thumbnail, the organizer in grey with the price tag, a 17sp title, then
 * the time and the place with their icons. [compact] is the Discover shelf: the day in the time line, no place.
 */
@Composable
fun EventRow(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showsSeparator: Boolean = true,
) {
    val strings = LocalAroundStrings.current
    val locale = LocalAppLocale.current
    Column(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(top = 14.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            // No poster, no square: an empty tile says nothing the category line doesn't.
            if (event.posterUrl != null) EventThumb(event)
            Column(Modifier.weight(1f)) {
                HostLine(event)
                Text(
                    event.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = AroundColors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 7.dp),
                )
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val time = if (compact) event.shortWhen(strings, locale) else event.timeText()
                    time?.let { Meta(Icons.Outlined.Schedule, it) }
                    if (!compact) event.venue?.let { Meta(Icons.Outlined.Place, it) }
                }
            }
        }
        if (showsSeparator) {
            Box(
                Modifier
                    .padding(start = if (event.posterUrl != null) 93.dp else 0.dp)
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(AroundColors.separator),
            )
        }
    }
}

@Composable
private fun HostLine(event: Event) {
    val strings = LocalAroundStrings.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        val organizer = event.organizer
        val category = event.category
        if (organizer != null) {
            Avatar(organizer.avatarUrl, organizer.name, size = 15.dp, cornerRadius = 4.5.dp)
        } else if (category != null) {
            Box(
                Modifier.size(
                    15.dp,
                ).clip(RoundedCornerShape(4.5.dp)).background(categoryTint(category).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    categoryIcon(category),
                    contentDescription = null,
                    tint = categoryTint(category),
                    modifier = Modifier.size(11.dp),
                )
            }
        }
        Text(
            organizer?.name ?: category?.let { categoryLabel(it, strings) }.orEmpty(),
            fontSize = 14.sp,
            color = AroundColors.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        event.priceText(strings)?.let { PriceBadge(it) }
    }
}

@Composable
private fun Meta(
    icon: ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AroundColors.secondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 15.sp, color = AroundColors.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** "19 July / Sunday": the date in ink, a light slash, the weekday in grey — 17sp semibold. */
@Composable
fun DayHeader(
    day: FeedDay,
    modifier: Modifier = Modifier,
) {
    val (main, weekday) = day.headingParts(LocalAroundStrings.current, LocalAppLocale.current)
    Row(modifier = modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(main, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = AroundColors.ink)
        weekday?.let {
            Text("/", fontSize = 17.sp, color = AroundColors.slash)
            Text(it, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = AroundColors.weekday)
        }
    }
}

/** List items of events by day, each opening the event. */
fun LazyListScope.eventDays(
    days: List<FeedDay>,
    onOpen: (Long) -> Unit,
) {
    days.forEach { day ->
        item(key = "day-${day.date}") { DayHeader(day, Modifier.padding(horizontal = 20.dp)) }
        items(day.events.size, key = { "${day.date}-${day.events[it].id}" }) { index ->
            val event = day.events[index]
            EventRow(
                event,
                onClick = { onOpen(event.id) },
                showsSeparator = index < day.events.lastIndex,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

/** "Cake Decor 1 · Clay 1 · Club 10": outlined pills with counts, as on the organizer page in the designs. */
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    ) {
        items(categories, key = { it.key }) { item ->
            val isOn = item.key == selected
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(33.5.dp)
                    .clip(CircleShape)
                    .background(if (isOn) AroundColors.ink else AroundColors.surface)
                    .border(1.dp, if (isOn) Color.Transparent else AroundColors.chipBorder, CircleShape)
                    .clickable { onSelect(if (isOn) null else item.key) }
                    .padding(horizontal = 11.dp),
            ) {
                Text(
                    categoryLabel(item.key, strings),
                    fontSize = 14.5.sp,
                    color = if (isOn) AroundColors.surface else AroundColors.chipText,
                )
                Text(
                    "${item.count}",
                    fontSize = 14.5.sp,
                    color = if (isOn) AroundColors.surface.copy(alpha = 0.7f) else AroundColors.chipCount,
                )
            }
        }
    }
}
