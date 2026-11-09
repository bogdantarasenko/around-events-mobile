import AroundKit
import SwiftUI

/// Apple Maps at the event's place: by the point when the backend found one, by the text otherwise.
enum MapLink {
    static func open(_ event: Event, openURL: OpenURLAction) {
        var components = URLComponents(string: "https://maps.apple.com/")
        let place = [event.venue, event.address].compactMap { $0 }.joined(separator: ", ")
        var items = [URLQueryItem(name: "q", value: place.isEmpty ? event.title : place)]
        if let lat = event.latitude?.doubleValue, let lon = event.longitude?.doubleValue {
            items.append(URLQueryItem(name: "ll", value: "\(lat),\(lon)"))
        } else {
            items[0].value = "\(place), \(event.city.name)"
        }
        components?.queryItems = items
        if let url = components?.url { openURL(url) }
    }
}
