package events.around.android.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import events.around.android.BuildConfig
import events.around.android.ui.components.categoryTint
import events.around.android.ui.theme.LocalDarkTheme
import events.around.core.model.Event
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import java.util.Locale
import org.maplibre.android.maps.MapLibreMap as NativeMap

private const val SOURCE = "events"
private const val CLUSTERS = "events-clusters"
private const val CLUSTER_COUNT = "events-cluster-count"
private const val SELECTION = "events-selected"
private const val POINTS = "events-points"
private const val LABELS = "events-labels"
private const val SINGLE_PLACE_ZOOM = 14.5
private const val LABEL_MIN_ZOOM = 13f
private const val CLUSTER_RADIUS = 44
private const val CLUSTER_MAX_ZOOM = 14
private const val DEFAULT_PADDING_PX = 80
private const val TOUCH_SLOP_DP = 22f
private const val CORE_RADIUS_M = 15_000.0
private const val RGB_MASK = 0xFFFFFF
private const val CLUSTER_SIZE = 17f
private const val CLUSTER_STROKE = 3f
private const val COUNT_TEXT_SIZE = 14f
private const val SELECTION_SIZE = 15f
private const val POINT_SIZE = 9f
private const val POINT_SIZE_STILL = 10f
private const val POINT_STROKE = 2.5f
private const val LABEL_TEXT_SIZE = 12f
private const val LABEL_HALO = 1.5f
private const val LABEL_OFFSET = 1.1f
private const val LABEL_MAX_WIDTH = 9f

// Present in both OpenFreeMap styles; a font the style's glyph server lacks draws no text at all.
private val FONT = arrayOf("Noto Sans Regular")

/** Room around the pins when framing them, in pixels: overlays such as the controls cover the map. */
data class MapPadding(
    val left: Int = DEFAULT_PADDING_PX,
    val top: Int = DEFAULT_PADDING_PX,
    val right: Int = DEFAULT_PADDING_PX,
    val bottom: Int = DEFAULT_PADDING_PX,
)

/**
 * MapLibre's map on the OpenFreeMap styles: light, or dark with the dark theme. Places are drawn by the map itself
 * — a dot in the category's colour, a "Title · 19:00" label when close enough, clusters with a count far out. A tap
 * on a cluster zooms into it, on a place reports its events, on nothing calls [onClear].
 */
@Composable
fun MapLibreMap(
    events: List<Event>,
    modifier: Modifier = Modifier,
    selected: Set<Long> = emptySet(),
    interactive: Boolean = true,
    padding: MapPadding = MapPadding(),
    fitToken: Int = 0,
    /** Changing it shows the person's location and follows it; ask for the location permission first. */
    locateToken: Int = 0,
    onSelect: (List<Long>) -> Unit = {},
    onClear: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val dark = LocalDarkTheme.current
    val slop = with(LocalDensity.current) { TOUCH_SLOP_DP * density }
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnClear by rememberUpdatedState(onClear)
    var map by remember { mutableStateOf<NativeMap?>(null) }
    var loadedDark by remember { mutableStateOf<Boolean?>(null) }
    val shown = remember { Shown() }
    val view = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { native ->
                native.uiSettings.isTiltGesturesEnabled = false
                native.uiSettings.isLogoEnabled = false
                native.uiSettings.setAllGesturesEnabled(interactive)
                native.addOnMapClickListener { point ->
                    handleTap(
                        native,
                        point,
                        slop,
                        currentOnSelect,
                        currentOnClear,
                    )
                }
                map = native
            }
        }
    }
    DisposableEffect(lifecycle, view) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> view.onStart()
                Lifecycle.Event.ON_RESUME -> view.onResume()
                Lifecycle.Event.ON_PAUSE -> view.onPause()
                Lifecycle.Event.ON_STOP -> view.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            view.onDestroy()
        }
    }
    val places = remember(events) { places(events) }
    AndroidView(
        factory = { view },
        modifier = modifier,
        update = {
            val native = map ?: return@AndroidView
            // A new style (first load, or the theme changed) has none of our layers: add them, then draw.
            if (loadedDark != dark) {
                loadedDark = dark
                shown.reset()
                loadStyle(
                    native,
                    dark,
                    interactive,
                ) { draw(native, places, selected, padding, shown.apply { reset() }) }
                return@AndroidView
            }
            draw(native, places, selected, padding, shown)
            if (shown.fitToken != fitToken) {
                shown.fitToken = fitToken
                fit(native, places, padding)
            }
            if (shown.locateToken != locateToken) {
                shown.locateToken = locateToken
                followLocation(native, context)
            }
        },
    )
}

private fun loadStyle(map: NativeMap, dark: Boolean, interactive: Boolean, onLoaded: () -> Unit) {
    val url = if (dark) BuildConfig.MAP_DARK_STYLE_URL else BuildConfig.MAP_STYLE_URL
    map.setStyle(Style.Builder().fromUri(url)) {
        addLayers(it, dark, interactive)
        onLoaded()
    }
}

// A cluster zooms in; a place reports its events; nothing clears the selection.
private fun handleTap(
    map: NativeMap,
    point: LatLng,
    slop: Float,
    onSelect: (List<Long>) -> Unit,
    onClear: () -> Unit,
): Boolean {
    val screen = map.projection.toScreenLocation(point)
    val area = RectF(screen.x - slop, screen.y - slop, screen.x + slop, screen.y + slop)
    val hits = map.queryRenderedFeatures(area, CLUSTERS, POINTS)
    val cluster = hits.firstOrNull { it.hasProperty("point_count") }
    val source = map.style?.getSourceAs<GeoJsonSource>(SOURCE)
    when {
        cluster != null && source != null -> {
            val zoom = source.getClusterExpansionZoom(cluster).toDouble()
            val target = (cluster.geometry() as Point).let { LatLng(it.latitude(), it.longitude()) }
            map.easeCamera(CameraUpdateFactory.newLatLngZoom(target, maxOf(zoom, map.cameraPosition.zoom + 1)))
        }
        hits.isNotEmpty() -> hits.first().getStringProperty(
            "ids",
        )?.split(',')?.mapNotNull { it.toLongOrNull() }?.let(onSelect)
        else -> onClear()
    }
    return true
}

private class Place(val id: String, val point: LatLng, val feature: Feature)

// What the map last drew, so a recomposition doesn't reset the camera the person moved.
private class Shown {
    var places: List<Place>? = null
    var selected: Set<Long>? = null
    var fitToken = 0
    var locateToken = 0

    fun reset() {
        places = null
        selected = null
    }
}

private fun draw(map: NativeMap, places: List<Place>, selected: Set<Long>, padding: MapPadding, shown: Shown) {
    val style = map.style ?: return
    if (shown.places !== places) {
        val first = shown.places == null
        style.getSourceAs<GeoJsonSource>(SOURCE)?.setGeoJson(FeatureCollection.fromFeatures(places.map { it.feature }))
        shown.places = places
        if (first || places.isNotEmpty()) fit(map, places, padding)
    }
    if (shown.selected != selected) {
        shown.selected = selected
        val ids = places.filter { place ->
            place.feature.getStringProperty("ids").split(',').any { it.toLongOrNull() in selected }
        }.map { it.id }
        (style.getLayer(SELECTION) as? CircleLayer)?.setFilter(
            Expression.`in`(Expression.get("place"), Expression.literal(ids.toTypedArray())),
        )
    }
}

private fun addLayers(style: Style, dark: Boolean, interactive: Boolean) {
    val ink = if (dark) "#FFFFFF" else "#141414"
    val halo = if (dark) "#0C0C0C" else "#FFFFFF"
    val isCluster = Expression.has("point_count")
    val isPlace = Expression.not(isCluster)
    style.addSource(
        GeoJsonSource(
            SOURCE,
            GeoJsonOptions().withCluster(true).withClusterRadius(CLUSTER_RADIUS).withClusterMaxZoom(CLUSTER_MAX_ZOOM),
        ),
    )
    style.addLayer(
        CircleLayer(CLUSTERS, SOURCE).withFilter(isCluster).withProperties(
            PropertyFactory.circleColor(ink),
            PropertyFactory.circleRadius(CLUSTER_SIZE),
            PropertyFactory.circleStrokeColor(halo),
            PropertyFactory.circleStrokeWidth(CLUSTER_STROKE),
        ),
    )
    style.addLayer(
        SymbolLayer(CLUSTER_COUNT, SOURCE).withFilter(isCluster).withProperties(
            PropertyFactory.textField(Expression.toString(Expression.get("point_count"))),
            PropertyFactory.textFont(FONT),
            PropertyFactory.textSize(COUNT_TEXT_SIZE),
            PropertyFactory.textColor(halo),
            PropertyFactory.textAllowOverlap(true),
        ),
    )
    style.addLayer(
        CircleLayer(
            SELECTION,
            SOURCE,
        ).withFilter(Expression.`in`(Expression.get("place"), Expression.literal(arrayOf<String>()))).withProperties(
            PropertyFactory.circleColor("rgba(0,0,0,0)"),
            PropertyFactory.circleRadius(SELECTION_SIZE),
            PropertyFactory.circleStrokeColor(ink),
            PropertyFactory.circleStrokeWidth(CLUSTER_STROKE),
        ),
    )
    style.addLayer(
        CircleLayer(POINTS, SOURCE).withFilter(isPlace).withProperties(
            PropertyFactory.circleColor(Expression.toColor(Expression.get("color"))),
            PropertyFactory.circleRadius(if (interactive) POINT_SIZE else POINT_SIZE_STILL),
            PropertyFactory.circleStrokeColor(halo),
            PropertyFactory.circleStrokeWidth(POINT_STROKE),
        ),
    )
    if (!interactive) return
    style.addLayer(
        SymbolLayer(LABELS, SOURCE).withFilter(isPlace).withProperties(
            PropertyFactory.textField(Expression.get("label")),
            PropertyFactory.textFont(FONT),
            PropertyFactory.textSize(LABEL_TEXT_SIZE),
            PropertyFactory.textColor(ink),
            PropertyFactory.textHaloColor(halo),
            PropertyFactory.textHaloWidth(LABEL_HALO),
            PropertyFactory.textAnchor("top"),
            PropertyFactory.textOffset(Expression.literal(arrayOf<Any>(0f, LABEL_OFFSET))),
            PropertyFactory.textMaxWidth(LABEL_MAX_WIDTH),
            PropertyFactory.textOptional(true),
        ).apply { minZoom = LABEL_MIN_ZOOM },
    )
}

// Grouped by the point rounded to about ten metres, so one venue is one place.
private fun places(events: List<Event>): List<Place> = events
    .filter { it.latitude != null && it.longitude != null }
    .groupBy { String.format(Locale.ROOT, "%.4f,%.4f", it.latitude, it.longitude) }
    .map { (key, placed) ->
        val first = placed.first()
        val lat = first.latitude ?: 0.0
        val lon = first.longitude ?: 0.0
        val more = if (placed.size > 1) " +${placed.size - 1}" else ""
        val label = listOfNotNull(first.title.take(LABEL_MAX_TITLE) + more, first.startTime).joinToString(" · ")
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat)).apply {
            addStringProperty("place", key)
            addStringProperty("ids", placed.joinToString(",") { it.id.toString() })
            addStringProperty(
                "color",
                String.format(Locale.ROOT, "#%06X", categoryTint(first.category).toArgb() and RGB_MASK),
            )
            addStringProperty("label", label)
        }
        Place(key, LatLng(lat, lon), feature)
    }

private const val LABEL_MAX_TITLE = 32

private fun fit(map: NativeMap, places: List<Place>, padding: MapPadding) {
    val core = core(places.map { it.point })
    when (core.size) {
        0 -> Unit
        1 -> map.moveCamera(CameraUpdateFactory.newLatLngZoom(core.first(), SINGLE_PLACE_ZOOM))
        else -> {
            val bounds = LatLngBounds.Builder().includes(core).build()
            map.easeCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding.left, padding.top, padding.right, padding.bottom),
            )
        }
    }
}

// The city's mass, without the odd event out of town that would zoom the map out to a region.
private fun core(points: List<LatLng>): List<LatLng> {
    if (points.size <= 2) return points
    val center = LatLng(
        points.map { it.latitude }.sorted()[points.size / 2],
        points.map { it.longitude }.sorted()[points.size / 2],
    )
    val near = points.filter { it.distanceTo(center) <= CORE_RADIUS_M }
    return if (near.size >= 2) near else points
}

// The caller asked for the permission before changing locateToken; without it there is nothing to show.
@SuppressLint("MissingPermission")
private fun followLocation(map: NativeMap, context: Context) {
    val style = map.style ?: return
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    if (!granted) return
    map.locationComponent.apply {
        if (!isLocationComponentActivated) {
            activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build())
        }
        isLocationComponentEnabled = true
        cameraMode = CameraMode.TRACKING
    }
}
