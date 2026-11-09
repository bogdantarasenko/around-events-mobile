import SwiftUI

/// A poster thumbnail with a white edge, and "+N" when the place has more events.
struct MapPin: View {
    let place: MapPlace

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            if let first = place.events.first {
                EventThumb(event: first, size: 40, cornerRadius: 8)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(.white, lineWidth: 2))
                    .shadow(color: .black.opacity(0.25), radius: 3, y: 1)
            }
            if place.events.count > 1 {
                Text("+\(place.events.count - 1)")
                    .font(.caption2.weight(.bold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 5)
                    .padding(.vertical, 1)
                    .background(Color.accentColor, in: Capsule())
                    .offset(x: 6, y: 6)
            }
        }
    }
}
