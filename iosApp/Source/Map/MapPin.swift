import SwiftUI

/// A poster thumbnail with a white edge, stacked when the place has more events, as in the designs.
struct MapPin: View {
    let place: MapPlace

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            if place.events.count > 1, let second = place.events.dropFirst().first {
                EventThumb(event: second, size: 36, cornerRadius: 6)
                    .overlay(RoundedRectangle(cornerRadius: 6).stroke(.white, lineWidth: 2))
                    .rotationEffect(.degrees(8))
                    .offset(x: 8, y: -4)
            }
            if let first = place.events.first {
                EventThumb(event: first, size: 36, cornerRadius: 6)
                    .overlay(RoundedRectangle(cornerRadius: 6).stroke(.white, lineWidth: 2))
                    .rotationEffect(.degrees(-4))
            }
        }
        .shadow(color: .black.opacity(0.25), radius: 3, y: 1)
    }
}
