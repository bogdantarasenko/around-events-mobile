package events.around.android.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import events.around.android.ui.strings.LocalAroundStrings

/** The three tabs of the designs on the system navigation bar; [tabContent] draws the selected one. */
@Composable
fun HomeScreen(
    tabContent: @Composable (tab: HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    var tab by rememberSaveable { mutableStateOf(HomeTab.Home) }
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple(HomeTab.Home, strings.tabHome, Icons.Filled.Home to Icons.Outlined.Home),
                    Triple(HomeTab.Discover, strings.tabDiscover, Icons.Filled.Explore to Icons.Outlined.Explore),
                    Triple(HomeTab.Map, strings.tabMap, Icons.Filled.Map to Icons.Outlined.Map),
                ).forEach { (item, title, icons) ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(if (tab == item) icons.first else icons.second, contentDescription = null) },
                        label = { Text(title) },
                    )
                }
            }
        },
        // The tabs draw their own top bars; only the bottom bar's space is taken here.
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) { tabContent(tab) }
    }
}
