import SwiftUI

/// A picture from a URL, or the first letter of [name] on grey.
struct Avatar: View {
    let url: String?
    let name: String
    var size: CGFloat = 16
    var cornerRadius: CGFloat?

    var body: some View {
        Group {
            if let url, let parsed = URL(string: url) {
                // Telegram's picture links expire and then answer 404 with an empty image: only a real success
                // replaces the letter.
                AsyncImage(url: parsed) { phase in
                    if case let .success(image) = phase {
                        image.resizable().scaledToFill()
                    } else {
                        letter
                    }
                }
            } else {
                letter
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius ?? size / 2))
    }

    private var letter: some View {
        Color(.tertiarySystemFill)
            .overlay(
                Text(name.prefix(1).uppercased())
                    .font(.system(size: size * 0.45, weight: .semibold))
                    .foregroundStyle(.secondary)
            )
    }
}
