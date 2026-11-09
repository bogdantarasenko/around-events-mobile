import MapLibre
import SwiftUI

/// MapLibre's map view for SwiftUI, on the OpenFreeMap style (`AroundMapStyleURL` in Info.plist). Places are drawn
/// as poster pins; a tap reports the place's events. Changing [fitToken] frames every place again.
struct MapLibreMap: UIViewRepresentable {
    let places: [MapPlace]
    var interactive = true
    /// Room left for overlays when framing the places, such as the events card under the map.
    var padding = UIEdgeInsets(top: 80, left: 40, bottom: 80, right: 40)
    var fitToken = 0
    /// Changing it shows the person's location and follows it; the first time, iOS asks for permission.
    var locateToken = 0
    var onSelect: ([Int64]) -> Void = { _ in }

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> MLNMapView {
        let map = MLNMapView(frame: .zero, styleURL: Self.styleURL)
        map.delegate = context.coordinator
        map.isUserInteractionEnabled = interactive
        map.isPitchEnabled = false
        map.isRotateEnabled = interactive
        // The compass appears when the map is turned, as in Apple's maps; it sits under the map controls.
        map.compassViewPosition = .topLeft
        map.compassViewMargins = CGPoint(x: 16, y: 16)
        map.logoView.isHidden = true
        // The OpenStreetMap credit must stay visible: above the tab bar, away from the list button.
        map.attributionButtonPosition = .bottomLeft
        map.attributionButtonMargins = CGPoint(x: 12, y: interactive ? 12 : 6)
        return map
    }

    func updateUIView(_ map: MLNMapView, context: Context) {
        context.coordinator.parent = self
        let ids = places.map(\.id)
        if ids != context.coordinator.shownIds {
            context.coordinator.shownIds = ids
            map.removeAnnotations(map.annotations ?? [])
            map.addAnnotations(places.map(PlaceAnnotation.init))
            context.coordinator.fit(map, animated: false)
        } else if fitToken != context.coordinator.fitToken {
            map.userTrackingMode = .none
            context.coordinator.fit(map, animated: true)
        }
        context.coordinator.fitToken = fitToken
        if locateToken != context.coordinator.locateToken {
            context.coordinator.locateToken = locateToken
            map.showsUserLocation = true
            map.setUserTrackingMode(.follow, animated: true, completionHandler: nil)
        }
    }

    private static var styleURL: URL? {
        (Bundle.main.infoDictionary?["AroundMapStyleURL"] as? String).flatMap(URL.init(string:))
    }

    final class Coordinator: NSObject, MLNMapViewDelegate {
        var parent: MapLibreMap
        var shownIds: [String] = []
        var fitToken = 0
        var locateToken = 0

        init(_ parent: MapLibreMap) {
            self.parent = parent
        }

        func fit(_ map: MLNMapView, animated: Bool) {
            let coordinates = parent.places.map(\.coordinate)
            switch coordinates.count {
            case 0:
                return
            case 1:
                map.setCenter(coordinates[0], zoomLevel: 14.5, animated: animated)
            default:
                map.setVisibleCoordinates(
                    coordinates,
                    count: UInt(coordinates.count),
                    edgePadding: parent.padding,
                    animated: animated
                )
            }
        }

        // The first framing waits for the style: before it the view has no size and the camera is ignored.
        func mapView(_ mapView: MLNMapView, didFinishLoading style: MLNStyle) {
            fit(mapView, animated: false)
        }

        func mapView(_ mapView: MLNMapView, viewFor annotation: MLNAnnotation) -> MLNAnnotationView? {
            guard let annotation = annotation as? PlaceAnnotation else { return nil }
            let view = MLNAnnotationView(reuseIdentifier: nil)
            let pin = UIHostingController(rootView: MapPin(place: annotation.place)).view ?? UIView()
            pin.backgroundColor = .clear
            pin.frame = CGRect(x: 0, y: 0, width: 56, height: 52)
            view.frame = pin.frame
            view.addSubview(pin)
            return view
        }

        func mapView(_ mapView: MLNMapView, didSelect annotation: MLNAnnotation) {
            if let annotation = annotation as? PlaceAnnotation {
                parent.onSelect(annotation.place.events.map(\.id))
            }
            mapView.deselectAnnotation(annotation, animated: false)
        }
    }
}

/// A place on the map, carrying its events for the pin and the tap.
final class PlaceAnnotation: MLNPointAnnotation {
    let place: MapPlace

    init(_ place: MapPlace) {
        self.place = place
        super.init()
        coordinate = place.coordinate
        title = place.caption
    }

    required init?(coder: NSCoder) { nil }
}
