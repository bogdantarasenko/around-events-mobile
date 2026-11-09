package events.around.android.city

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.eventDays
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.presentation.feed.EventsUiState
import events.around.shared.presentation.feed.EventsViewModel

private val HeroHeight = 400.dp
private val Shade = Color.Black.copy(alpha = 0.75f)
private val Soft = Color.White.copy(alpha = 0.75f)
private val Glass = Color.White.copy(alpha = 0.2f)
private val Subscribe = Color(0xFF34C759)

/**
 * A city's page as in the designs: a dark hero "What's happening in Kraków", the button to make it the app's city,
 * its events by day.
 */
@Composable
fun CityScreen(
    viewModel: EventsViewModel,
    navigateBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), modifier = Modifier.fillMaxSize()) {
            item { Hero(state, viewModel::makeCurrent) }
            eventDays(state.days, onOpenEvent)
        }
        IconButton(
            onClick = navigateBack,
            modifier = Modifier.statusBarsPadding().padding(
                start = 20.dp,
                top = 8.dp,
            ).clip(CircleShape).background(Glass),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = LocalAroundStrings.current.back,
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun Hero(
    state: EventsUiState,
    onMakeCurrent: () -> Unit,
) {
    val strings = LocalAroundStrings.current
    val colors = cityGradient(state.city?.slug.orEmpty())
    Box(
        Modifier
            .fillMaxWidth()
            .height(HeroHeight)
            .background(Brush.linearGradient(colors))
            .background(Brush.verticalGradient(listOf(Color.Transparent, Shade))),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.cityWhatsOn, fontSize = 15.sp, color = Soft)
            Text(state.city?.name.orEmpty(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(strings.eventsCount("${state.count}"), fontSize = 15.sp, color = Soft)
            Text(
                if (state.isCurrentCity) strings.cityIsCurrent else strings.cityMakeCurrent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .clip(CircleShape)
                    .background(if (state.isCurrentCity) Glass else Subscribe)
                    .clickable(enabled = !state.isCurrentCity, onClick = onMakeCurrent)
                    .padding(horizontal = 11.dp, vertical = 7.dp),
            )
        }
    }
}
