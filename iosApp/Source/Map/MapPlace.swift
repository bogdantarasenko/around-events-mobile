import AroundKit
import CoreLocation

/// Events at one point on the map: several events at one venue share a pin.
struct MapPlace: Identifiable {
    let id: String
    let coordinate: CLLocationCoordinate2D
    let events: [Event]

    var caption: String {
        events.count > 1 ? "\(events[0].title) +\(events.count - 1)" : events[0].title
    }

    /// The map label: the first event's title and time, and how many more are here.
    var label: String {
        let first = events[0]
        let title = first.title.count > 32 ? String(first.title.prefix(30)) + "…" : first.title
        let more = events.count > 1 ? " +\(events.count - 1)" : ""
        return [title + more, first.startTime].compactMap { $0 }.joined(separator: " · ")
    }

    /// Groups by the point rounded to about ten metres, keeping the feed's order inside a place.
    static func group(_ events: [Event]) -> [MapPlace] {
        var order: [String] = []
        var byKey: [String: [Event]] = [:]
        for event in events {
            guard let lat = event.latitude?.doubleValue, let lon = event.longitude?.doubleValue else { continue }
            let key = String(format: "%.4f,%.4f", lat, lon)
            if byKey[key] == nil { order.append(key) }
            byKey[key, default: []].append(event)
        }
        return order.compactMap { key in
            guard let placed = byKey[key], let first = placed.first,
                  let lat = first.latitude?.doubleValue, let lon = first.longitude?.doubleValue else { return nil }
            return MapPlace(id: key, coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lon), events: placed)
        }
    }
}
