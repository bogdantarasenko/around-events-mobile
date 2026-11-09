import AroundKit
import SwiftUI

/// The page behind an event: its poster blurred and dimmed enough for white text on any poster, or the
/// category's tint without a poster.
struct EventBackdrop: View {
    let event: Event

    var body: some View {
        ZStack {
            Color(white: 0.41)
            if let poster = event.posterUrl.flatMap(URL.init(string:)) {
                AsyncImage(url: poster) { image in
                    image.resizable().scaledToFill().blur(radius: 45).saturation(0.8)
                } placeholder: {
                    Color.clear
                }
            } else {
                LinearGradient(
                    colors: [CategoryStyle.tint(event.category).opacity(0.18), .clear],
                    startPoint: .top,
                    endPoint: .center
                )
            }
            // A light poster would wash the text out: a flat dim, then deeper towards the details below.
            Color.black.opacity(0.42)
            LinearGradient(colors: [.clear, .black.opacity(0.35)], startPoint: .center, endPoint: .bottom)
        }
        .ignoresSafeArea()
    }
}
