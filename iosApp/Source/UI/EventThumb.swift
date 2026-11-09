import AroundKit
import SwiftUI

/// The event's poster, or its category's icon on a soft tint when there is none.
struct EventThumb: View {
    let event: Event
    var size: CGFloat = 80
    var cornerRadius: CGFloat = 10

    var body: some View {
        Group {
            if let poster = event.posterUrl, let url = URL(string: poster) {
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    placeholder
                }
            } else {
                placeholder
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
    }

    private var placeholder: some View {
        CategoryStyle.tint(event.category).opacity(0.16)
            .overlay(
                Image(systemName: CategoryStyle.symbol(event.category))
                    .font(.system(size: min(size * 0.36, 64), weight: .medium))
                    .foregroundStyle(CategoryStyle.tint(event.category))
            )
    }
}
