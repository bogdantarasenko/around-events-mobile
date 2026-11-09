package events.around.android.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import events.around.android.ui.components.EventRow
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.Event
import events.around.shared.presentation.feed.MapUiState
import events.around.shared.presentation.feed.MapViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val SHEET_FRACTION = 0.4f
private const val BOUNDS_PADDING_PX = 120
private const val SINGLE_PLACE_ZOOM = 15.0

/** The city's events on an OpenStreetMap map, the list in a panel below. */
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    navigateBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAroundStrings.current
    Box(modifier.fillMaxSize()) {
        EventsMap(state.events, onSelect = viewModel::select, modifier = Modifier.fillMaxSize())
        Surface(
            shape = CircleShape,
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
            modifier = Modifier.statusBarsPadding().padding(12.dp),
        ) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
            }
        }
        Sheet(
            state = state,
            onClose = viewModel::clearSelection,
            onOpenEvent = onOpenEvent,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxHeight(SHEET_FRACTION),
        )
    }
}

/** One marker per place: events at one venue share it, and tapping it lists them below. */
@Composable
private fun EventsMap(
    events: List<Event>,
    onSelect: (List<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val map = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
        }
    }
    DisposableEffect(map) { onDispose { map.onDetach() } }
    AndroidView(
        factory = { map },
        modifier = modifier,
        update = { view ->
            view.overlays.clear()
            val places = events.groupBy { "%.4f,%.4f".format(java.util.Locale.ROOT, it.latitude, it.longitude) }.values
            val points = places.mapNotNull { placed ->
                val first = placed.first()
                val point = GeoPoint(
                    first.latitude ?: return@mapNotNull null,
                    first.longitude ?: return@mapNotNull null,
                )
                view.overlays += Marker(view).apply {
                    position = point
                    title = if (placed.size > 1) "${first.title} +${placed.size - 1}" else first.title
                    setOnMarkerClickListener { _, _ ->
                        onSelect(placed.map { it.id })
                        true
                    }
                }
                point
            }
            when {
                points.size > 1 -> view.post {
                    view.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), false, BOUNDS_PADDING_PX)
                }
                points.size == 1 -> {
                    view.controller.setZoom(SINGLE_PLACE_ZOOM)
                    view.controller.setCenter(points.first())
                }
            }
            view.invalidate()
        },
    )
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
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
                items(state.sheet, key = { it.id }) { event ->
                    EventRow(event, onClick = { onOpenEvent(event.id) }, showsDay = true)
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
