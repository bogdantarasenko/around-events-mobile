import AroundKit
import SwiftUI

/// The dark page behind an event: its poster blurred, or its category's tint when there is no poster.
struct EventBackdrop: View {
    let event: Event

    var body: some View {
        ZStack {
            Color(white: 0.12)
            if let poster = event.posterUrl.flatMap(URL.init(string:)) {
                AsyncImage(url: poster) { image in
                    image.resizable().scaledToFill().blur(radius: 60).opacity(0.6)
                } placeholder: {
                    Color.clear
                }
            } else {
                LinearGradient(
                    colors: [CategoryStyle.tint(event.category).opacity(0.45), .clear],
                    startPoint: .top,
                    endPoint: .bottom
                )
            }
            Color.black.opacity(0.25)
        }
        .ignoresSafeArea()
    }
}
