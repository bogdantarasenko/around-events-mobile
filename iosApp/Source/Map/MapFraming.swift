import CoreLocation

/// Which points to frame: the city's mass, without the odd event out of town that would zoom the map out to a region.
enum MapFraming {
    /// Points within [radius] metres of the median point; all of them when that leaves fewer than two.
    static func core(_ points: [CLLocationCoordinate2D], radius: CLLocationDistance = 15000) -> [CLLocationCoordinate2D] {
        guard points.count > 2 else { return points }
        let latitudes = points.map(\.latitude).sorted()
        let longitudes = points.map(\.longitude).sorted()
        let center = CLLocation(latitude: latitudes[latitudes.count / 2], longitude: longitudes[longitudes.count / 2])
        let near = points.filter { CLLocation(latitude: $0.latitude, longitude: $0.longitude).distance(from: center) <= radius }
        return near.count >= 2 ? near : points
    }
}
