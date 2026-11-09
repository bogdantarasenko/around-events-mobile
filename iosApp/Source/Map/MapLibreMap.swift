import MapLibre
import SwiftUI

/// MapLibre's map view for SwiftUI on the OpenFreeMap styles: light, or dark with the dark theme. Places are drawn
/// by the map itself — a dot in the category's colour, a "Title · 19:00" label when close enough, clusters with a
/// count far out. A tap on a cluster zooms into it, on a place reports its events, on nothing clears the selection.
struct MapLibreMap: UIViewRepresentable {
    let places: [MapPlace]
    var selected: Set<Int64> = []
    var interactive = true
    /// Room left for overlays when framing the places, such as the controls and the list button.
    var padding = UIEdgeInsets(top: 80, left: 40, bottom: 80, right: 40)
    var fitToken = 0
    /// Changing it shows the person's location and follows it; the first time, iOS asks for permission.
    var locateToken = 0
    var onSelect: ([Int64]) -> Void = { _ in }
    var onClear: () -> Void = {}

    @Environment(\.colorScheme) private var colorScheme

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> MLNMapView {
        let map = MLNMapView(frame: .zero, styleURL: Self.styleURL(dark: colorScheme == .dark))
        map.delegate = context.coordinator
        map.isUserInteractionEnabled = interactive
        map.isPitchEnabled = false
        map.isRotateEnabled = interactive
        map.compassViewPosition = .topLeft
        map.compassViewMargins = CGPoint(x: 16, y: 16)
        map.logoView.isHidden = true
        // The OpenStreetMap credit must stay visible: bottom left, clear of the list button.
        map.attributionButtonPosition = .bottomLeft
        map.attributionButtonMargins = CGPoint(x: 12, y: interactive ? 12 : 6)
        context.coordinator.isDark = colorScheme == .dark
        if interactive {
            let tap = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.tapped(_:)))
            // Wait for the map's own double tap, so zooming in doesn't also select.
            for recognizer in map.gestureRecognizers ?? [] where (recognizer as? UITapGestureRecognizer)?.numberOfTapsRequired == 2 {
                tap.require(toFail: recognizer)
            }
            map.addGestureRecognizer(tap)
        }
        return map
    }

    func updateUIView(_ map: MLNMapView, context: Context) {
        let coordinator = context.coordinator
        coordinator.parent = self
        let dark = colorScheme == .dark
        if dark != coordinator.isDark {
            coordinator.isDark = dark
            map.styleURL = Self.styleURL(dark: dark)
            return
        }
        coordinator.showPlaces(on: map)
        coordinator.showSelection(on: map)
        if fitToken != coordinator.fitToken {
            coordinator.fitToken = fitToken
            map.userTrackingMode = .none
            coordinator.fit(map, animated: true)
        }
        if locateToken != coordinator.locateToken {
            coordinator.locateToken = locateToken
            map.showsUserLocation = true
            map.setUserTrackingMode(.follow, animated: true, completionHandler: nil)
        }
    }

    private static func styleURL(dark: Bool) -> URL? {
        let key = dark ? "AroundMapDarkStyleURL" : "AroundMapStyleURL"
        return (Bundle.main.infoDictionary?[key] as? String).flatMap(URL.init(string:))
    }

    final class Coordinator: NSObject, MLNMapViewDelegate {
        var parent: MapLibreMap
        var isDark = false
        var fitToken = 0
        var locateToken = 0
        private var shownIds: [String]?
        private var shownSelection: Set<Int64>?
        private var source: MLNShapeSource?
        private var selectionLayer: MLNCircleStyleLayer?

        init(_ parent: MapLibreMap) {
            self.parent = parent
        }

        // A new style (first load, or the theme changed) has none of our layers: add them and draw everything.
        func mapView(_ mapView: MLNMapView, didFinishLoading style: MLNStyle) {
            let source = MLNShapeSource(identifier: MapLayers.source, features: [], options: [
                .clustered: true,
                .clusterRadius: 44,
                .maximumZoomLevelForClustering: 14,
            ])
            style.addSource(source)
            MapLayers.add(to: style, source: source, dark: isDark, interactive: parent.interactive)
            self.source = source
            selectionLayer = style.layer(withIdentifier: MapLayers.selection) as? MLNCircleStyleLayer
            shownIds = nil
            shownSelection = nil
            showPlaces(on: mapView)
            showSelection(on: mapView)
            fit(mapView, animated: false)
        }

        func showPlaces(on map: MLNMapView) {
            guard let source else { return }
            let ids = parent.places.map(\.id)
            guard ids != shownIds else { return }
            let refit = shownIds != nil
            shownIds = ids
            source.shape = MLNShapeCollectionFeature(shapes: parent.places.map(MapLayers.feature))
            if refit { fit(map, animated: true) }
        }

        func showSelection(on map: MLNMapView) {
            guard let selectionLayer, parent.selected != shownSelection else { return }
            shownSelection = parent.selected
            let ids = parent.places.filter { place in place.events.contains { parent.selected.contains($0.id) } }.map(\.id)
            selectionLayer.predicate = NSPredicate(format: "place IN %@", ids)
        }

        func fit(_ map: MLNMapView, animated: Bool) {
            let coordinates = MapFraming.core(parent.places.map(\.coordinate))
            switch coordinates.count {
            case 0:
                return
            case 1:
                map.setCenter(coordinates[0], zoomLevel: 14.5, animated: animated)
            default:
                map.setVisibleCoordinates(coordinates, count: UInt(coordinates.count), edgePadding: parent.padding, animated: animated)
            }
        }

        @objc func tapped(_ gesture: UITapGestureRecognizer) {
            guard let map = gesture.view as? MLNMapView, gesture.state == .ended else { return }
            let point = gesture.location(in: map)
            // A finger-sized area rather than a point: the dots are small.
            let area = CGRect(x: point.x - 22, y: point.y - 22, width: 44, height: 44)
            let features = map.visibleFeatures(in: area, styleLayerIdentifiers: [MapLayers.clusters, MapLayers.points])
            if let cluster = features.compactMap({ $0 as? MLNPointFeatureCluster }).first, let source {
                let zoom = source.zoomLevel(forExpanding: cluster)
                map.setCenter(cluster.coordinate, zoomLevel: max(zoom, map.zoomLevel + 1), animated: true)
            } else if let place = features.first, let ids = place.attribute(forKey: "ids") as? String {
                parent.onSelect(ids.split(separator: ",").compactMap { Int64($0) })
            } else {
                parent.onClear()
            }
        }
    }
}
