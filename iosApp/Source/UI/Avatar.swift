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
                AsyncImage(url: parsed) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    letter
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
