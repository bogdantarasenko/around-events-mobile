import AroundKit
import SwiftUI

/// The page behind an event: #696969 under its poster blurred, or under its category's tint without a poster.
struct EventBackdrop: View {
    let event: Event

    var body: some View {
        ZStack {
            Color(white: 0.41)
            if let poster = event.posterUrl.flatMap(URL.init(string:)) {
                AsyncImage(url: poster) { image in
                    image.resizable().scaledToFill().blur(radius: 45)
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
            Color.black.opacity(0.12)
        }
        .ignoresSafeArea()
    }
}
