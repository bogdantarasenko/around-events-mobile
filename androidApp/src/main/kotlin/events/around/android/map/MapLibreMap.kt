package events.around.android.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color.WHITE
import android.graphics.PointF
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import events.around.android.BuildConfig
import events.around.android.ui.components.categoryTint
import events.around.core.model.Event
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import java.util.Locale
import org.maplibre.android.maps.MapLibreMap as NativeMap

private const val SOURCE = "events"
private const val LAYER = "events-pins"
private const val SINGLE_PLACE_ZOOM = 14.5
private const val PIN_RADIUS = 9f
private const val PIN_STROKE = 3f
private const val DEFAULT_PADDING_PX = 80
private const val RGB_MASK = 0xFFFFFF

/**
 * MapLibre's map on the OpenFreeMap style. Events at one point share a pin, coloured by the first one's category;
 * a tap reports that place's events. Changing [fitToken] frames every pin again; [paddingPx] leaves room for
 * overlays such as the events card.
 */
@Composable
fun MapLibreMap(
    events: List<Event>,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    padding: MapPadding = MapPadding(),
    fitToken: Int = 0,
    /** Changing it shows the person's location and follows it; ask for the location permission first. */
    locateToken: Int = 0,
    onSelect: (List<Long>) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnSelect by rememberUpdatedState(onSelect)
    var map by remember { mutableStateOf<NativeMap?>(null) }
    val view = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { native ->
                native.uiSettings.isTiltGesturesEnabled = false
                native.uiSettings.isLogoEnabled = false
                native.uiSettings.setAllGesturesEnabled(interactive)
                native.setStyle(Style.Builder().fromUri(BuildConfig.MAP_STYLE_URL)) { style ->
                    style.addSource(GeoJsonSource(SOURCE))
                    style.addLayer(
                        CircleLayer(LAYER, SOURCE).withProperties(
                            circleRadius(PIN_RADIUS),
                            circleColor(get("color")),
                            circleStrokeColor(WHITE),
                            circleStrokeWidth(PIN_STROKE),
                        ),
                    )
                    map = native
                }
                native.addOnMapClickListener { point ->
                    val screen: PointF = native.projection.toScreenLocation(point)
                    val ids = native.queryRenderedFeatures(screen, LAYER).firstOrNull()?.getStringProperty("ids")
                    ids?.split(',')?.mapNotNull { it.toLongOrNull() }?.let { currentOnSelect(it) }
                    ids != null
                }
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
    val shown = remember { Shown() }
    AndroidView(
        factory = { view },
        modifier = modifier,
        update = {
            val native = map ?: return@AndroidView
            if (shown.places !== places) {
                native.style?.getSourceAs<GeoJsonSource>(SOURCE)
                    ?.setGeoJson(FeatureCollection.fromFeatures(places.map { it.feature }))
                fit(native, places, padding)
                shown.places = places
            } else if (shown.fitToken != fitToken) {
                fit(native, places, padding)
            }
            shown.fitToken = fitToken
            if (shown.locateToken != locateToken) {
                shown.locateToken = locateToken
                followLocation(native, context)
            }
        },
    )
}

/** Room around the pins when framing them, in pixels: overlays such as the events card cover the map. */
data class MapPadding(
    val left: Int = DEFAULT_PADDING_PX,
    val top: Int = DEFAULT_PADDING_PX,
    val right: Int = DEFAULT_PADDING_PX,
    val bottom: Int = DEFAULT_PADDING_PX,
)

// What the map last drew, so a recomposition doesn't reset the camera the person moved.
private class Shown {
    var places: List<Place>? = null
    var fitToken = 0
    var locateToken = 0
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

private class Place(val point: LatLng, val feature: Feature)

// Grouped by the point rounded to about ten metres, so one venue gets one pin.
private fun places(events: List<Event>): List<Place> = events
    .filter { it.latitude != null && it.longitude != null }
    .groupBy { String.format(Locale.ROOT, "%.4f,%.4f", it.latitude, it.longitude) }
    .values
    .map { placed ->
        val first = placed.first()
        val lat = first.latitude ?: 0.0
        val lon = first.longitude ?: 0.0
        val feature = Feature.fromGeometry(Point.fromLngLat(lon, lat)).apply {
            addStringProperty("ids", placed.joinToString(",") { it.id.toString() })
            addStringProperty(
                "color",
                String.format(Locale.ROOT, "#%06X", categoryTint(first.category).toArgb() and RGB_MASK),
            )
        }
        Place(LatLng(lat, lon), feature)
    }

private fun fit(map: NativeMap, places: List<Place>, padding: MapPadding) {
    when (places.size) {
        0 -> Unit
        1 -> map.moveCamera(CameraUpdateFactory.newLatLngZoom(places.first().point, SINGLE_PLACE_ZOOM))
        else -> {
            val bounds = LatLngBounds.Builder().includes(places.map { it.point }).build()
            map.easeCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding.left, padding.top, padding.right, padding.bottom),
            )
        }
    }
}
