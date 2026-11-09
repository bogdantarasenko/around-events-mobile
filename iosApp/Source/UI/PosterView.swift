import SwiftUI

struct PosterView: View {
    let url: String?
    let size: CGFloat

    var body: some View {
        Group {
            if let url, let parsed = URL(string: url) {
                AsyncImage(url: parsed) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color(.secondarySystemFill)
                }
            } else {
                Color(.secondarySystemFill)
                    .overlay(Image(systemName: "photo").foregroundStyle(.secondary))
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}
