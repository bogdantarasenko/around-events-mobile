package events.around.android.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.EventRow
import events.around.android.ui.strings.LocalAroundStrings
import events.around.shared.presentation.feed.MapUiState
import events.around.shared.presentation.feed.MapViewModel

private const val SHEET_FRACTION = 0.38f
private val SHEET_BOTTOM = 78.dp
private val MAP_SIDE_PADDING = 40.dp

/** The city's events on an OpenStreetMap map, the list in a panel below. */
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    /** Null on the Map tab, which has nowhere to go back to. */
    navigateBack: (() -> Unit)?,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    Box(modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val sheetHeight = (LocalConfiguration.current.screenHeightDp * SHEET_FRACTION).dp
        val bottom = with(density) { (sheetHeight + SHEET_BOTTOM).roundToPx() }
        val side = with(density) { MAP_SIDE_PADDING.roundToPx() }
        var fitToken by remember { mutableIntStateOf(0) }
        MapLibreMap(
            events = state.events,
            padding = MapPadding(left = side, top = side * 2, right = side, bottom = bottom),
            fitToken = fitToken,
            onSelect = viewModel::select,
            modifier = Modifier.fillMaxSize(),
        )
        Surface(
            shape = CircleShape,
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = sheetHeight + SHEET_BOTTOM + 12.dp),
        ) {
            IconButton(onClick = {
                viewModel.clearSelection()
                fitToken++
            }) {
                Icon(Icons.Outlined.NearMe, contentDescription = strings.mapTitle)
            }
        }
        if (navigateBack != null) {
            Surface(
                shape = CircleShape,
                shadowElevation = 3.dp,
                modifier = Modifier.statusBarsPadding().padding(start = 20.dp, top = 8.dp),
            ) {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
            }
        }
        // The floating card of the designs, kept above the tab bar.
        Sheet(
            state = state,
            onClose = viewModel::clearSelection,
            onOpenEvent = onOpenEvent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 9.dp, end = 9.dp, bottom = SHEET_BOTTOM)
                .fillMaxHeight(SHEET_FRACTION),
        )
    }
}

@Composable
private fun Sheet(
    state: MapUiState,
    onClose: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    Surface(
        shape = RoundedCornerShape(34.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .padding(horizontal = 18.dp, vertical = 2.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp),
            ) {
                Text(
                    strings.eventsCount("${state.sheet.size}"),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                )
                if (state.isSelection) {
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = strings.close) }
                }
            }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                items(state.sheet.size, key = { state.sheet[it].id }) { index ->
                    val event = state.sheet[index]
                    EventRow(event, onClick = { onOpenEvent(event.id) }, showsSeparator = index < state.sheet.lastIndex)
                }
                if (state.unlocated > 0 && !state.isSelection) {
                    item {
                        Text(
                            strings.mapUnlocated("${state.unlocated}"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
