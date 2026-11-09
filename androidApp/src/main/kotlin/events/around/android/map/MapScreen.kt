package events.around.android.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.EventRow
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.theme.AroundColors
import events.around.shared.presentation.feed.MapUiState
import events.around.shared.presentation.feed.MapViewModel

private val SIDE_PADDING = 40.dp
private val BOTTOM_PADDING = 120.dp

/**
 * The map tab with Material's own parts: the map edge to edge, tonal buttons for "my location" and "show all", a
 * "List" button at the bottom, and the events in a bottom sheet — all of them, or the tapped place's.
 */
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
    var fitToken by remember { mutableIntStateOf(0) }
    var locateToken by remember { mutableIntStateOf(0) }
    var showsList by remember { mutableStateOf(false) }
    val askLocation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.any { it }) locateToken++
    }
    val side = with(LocalDensity.current) { SIDE_PADDING.roundToPx() }
    val bottom = with(LocalDensity.current) { BOTTOM_PADDING.roundToPx() }

    Box(modifier.fillMaxSize()) {
        MapLibreMap(
            events = state.events,
            padding = MapPadding(left = side, top = side * 2, right = side, bottom = bottom),
            fitToken = fitToken,
            locateToken = locateToken,
            onSelect = {
                viewModel.select(it)
                showsList = true
            },
            modifier = Modifier.fillMaxSize(),
        )
        Column(Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp)) {
            if (navigateBack != null) {
                FilledTonalIconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
            }
            FilledTonalIconButton(onClick = {
                askLocation.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                )
            }) {
                Icon(Icons.Outlined.MyLocation, contentDescription = strings.mapMyLocation)
            }
            FilledTonalIconButton(onClick = {
                viewModel.clearSelection()
                fitToken++
            }) {
                Icon(Icons.Outlined.FitScreen, contentDescription = strings.mapShowAll)
            }
        }
        if (state.events.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { showsList = true },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                text = { Text("${strings.mapList} · ${state.events.size}") },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            )
        }
    }

    if (showsList) {
        EventsSheet(
            state = state,
            onDismiss = {
                showsList = false
                viewModel.clearSelection()
            },
            onOpenEvent = {
                showsList = false
                onOpenEvent(it)
            },
        )
    }
}

@Composable
private fun EventsSheet(
    state: MapUiState,
    onDismiss: () -> Unit,
    onOpenEvent: (Long) -> Unit,
) {
    val strings = LocalAroundStrings.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
    ) {
        Text(
            strings.eventsCount("${state.sheet.size}"),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = AroundColors.ink,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp),
        )
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            items(state.sheet.size, key = { state.sheet[it].id }) { index ->
                val event = state.sheet[index]
                EventRow(event, onClick = { onOpenEvent(event.id) }, showsSeparator = index < state.sheet.lastIndex)
            }
            if (state.unlocated > 0 && !state.isSelection) {
                item {
                    Text(
                        strings.mapUnlocated("${state.unlocated}"),
                        fontSize = 13.sp,
                        color = AroundColors.secondary,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}
