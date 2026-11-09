package events.around.android.city

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.BackScaffold
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.shared.presentation.feed.DiscoverViewModel

/** "Cities" from the designs: a short intro, then every city with its number of events, each opening its page. */
@Composable
fun CitiesScreen(
    viewModel: DiscoverViewModel,
    navigateBack: () -> Unit,
    onOpenCity: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    BackScaffold(navigateBack, modifier, title = strings.discoverCities) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item {
                Text(
                    strings.citiesIntro,
                    fontSize = 15.sp,
                    color = AroundColors.secondary,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }
            itemsIndexed(state.cities, key = { _, city -> city.slug }) { index, city ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onOpenCity(city.slug) }.padding(vertical = 8.dp),
                ) {
                    Box(
                        Modifier.size(32.dp).clip(CircleShape).background(cityGradient(index).first()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(city.name, fontSize = 17.sp, color = AroundColors.ink, modifier = Modifier.weight(1f))
                    Text(strings.eventsCount("${city.upcomingCount}"), fontSize = 17.sp, color = AroundColors.tertiary)
                }
            }
        }
    }
}
