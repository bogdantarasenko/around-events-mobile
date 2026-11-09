import AroundKit
import SwiftUI

/// The white card with the poster: full width, 20pt corners, a deep soft shadow. Without a poster, the category
/// icon on its tint.
struct EventPosterCard: View {
    let event: Event

    var body: some View {
        GeometryReader { proxy in
            EventThumb(event: event, size: proxy.size.width, cornerRadius: 20)
        }
        .aspectRatio(1, contentMode: .fit)
        .background(Color.white, in: RoundedRectangle(cornerRadius: 20))
        .shadow(color: .black.opacity(0.3), radius: 20, y: 14)
    }
}
