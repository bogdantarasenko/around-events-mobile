package events.around.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import events.around.android.home.HomeScreen
import events.around.android.settings.SettingsScreen
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.strings.stringsFor
import events.around.android.ui.theme.AroundTheme
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            val settings by settingsViewModel.state.collectAsStateWithLifecycle()

            AroundTheme(themeMode = settings.theme) {
                CompositionLocalProvider(LocalAroundStrings provides stringsFor(settings.language)) {
                    AroundNavigation(backStack = rememberNavBackStack(Home))
                }
            }
        }
    }

    @Composable
    private fun AroundNavigation(backStack: NavBackStack<NavKey>) {
        val navigateBack: () -> Unit = {
            if (backStack.size > 1) backStack.removeLastOrNull() else finish()
        }
        NavDisplay(
            backStack = backStack,
            onBack = navigateBack,
            entryProvider = entryProvider {
                entry<Home> {
                    HomeScreen(onOpenSettings = { backStack.add(Settings) })
                }
                entry<Settings> {
                    SettingsScreen(navigateBack = navigateBack)
                }
            },
        )
    }
}
