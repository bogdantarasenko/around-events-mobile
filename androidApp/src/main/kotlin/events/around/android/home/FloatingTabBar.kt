package events.around.android.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors

enum class HomeTab {
    Home,
    Discover,
    Map,
}

/** The pill at the bottom of the designs: Home, Discover, Map; 274 × 62, the selected tab on a grey capsule. */
@Composable
fun FloatingTabBar(
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Surface(
        shape = CircleShape,
        color = AroundColors.surface.copy(alpha = 0.94f),
        shadowElevation = 8.dp,
        modifier = modifier.size(
            width = 274.dp,
            height = 62.dp,
        ).border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape),
    ) {
        Row(Modifier.padding(3.dp)) {
            Item(HomeTab.Home, strings.tabHome, Icons.Filled.Home, selected, onSelect)
            Item(HomeTab.Discover, strings.tabDiscover, Icons.Filled.Explore, selected, onSelect)
            Item(HomeTab.Map, strings.tabMap, Icons.Filled.Map, selected, onSelect)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Item(
    tab: HomeTab,
    title: String,
    icon: ImageVector,
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
) {
    val isOn = tab == selected
    val color = if (isOn) AroundColors.ink else AroundColors.tabInactive
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(CircleShape)
            .background(if (isOn) AroundColors.tabSelected else Color.Transparent)
            .clickable { onSelect(tab) },
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(21.dp))
        Text(title, fontSize = 11.sp, fontWeight = if (isOn) FontWeight.SemiBold else FontWeight.Medium, color = color)
    }
}
