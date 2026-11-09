package events.around.android.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import events.around.android.ui.strings.LocalAroundStrings

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    var tab by rememberSaveable { mutableStateOf(HomeTab.Events) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (tab) {
                            HomeTab.Events -> strings.tabEvents
                            HomeTab.Profile -> strings.tabProfile
                        },
                    )
                },
                actions = {
                    if (tab == HomeTab.Profile) {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = strings.settingsTitle)
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == HomeTab.Events,
                    onClick = { tab = HomeTab.Events },
                    icon = { Icon(Icons.Outlined.Event, contentDescription = null) },
                    label = { Text(strings.tabEvents) },
                )
                NavigationBarItem(
                    selected = tab == HomeTab.Profile,
                    onClick = { tab = HomeTab.Profile },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text(strings.tabProfile) },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(strings.placeholderEmpty)
        }
    }
}

private enum class HomeTab {
    Events,
    Profile,
}
