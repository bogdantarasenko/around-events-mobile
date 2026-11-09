package events.around.android.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The three tabs of the designs under the floating bar. [tabContent] draws the selected tab and gets a way to
 * switch tabs (Discover's map button opens the Map tab).
 */
@Composable
fun HomeScreen(
    tabContent: @Composable (tab: HomeTab, selectTab: (HomeTab) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(HomeTab.Home) }
    Box(modifier.fillMaxSize()) {
        tabContent(tab) { tab = it }
        FloatingTabBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp),
        )
    }
}
