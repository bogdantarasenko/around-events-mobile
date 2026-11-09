import SwiftUI

/// A picture from the network that counts as loaded only on a 2xx answer with a real image in it. Telegram's
/// expired picture links answer 404 with an empty GIF, which `AsyncImage` would happily show.
struct RemoteImage<Content: View, Placeholder: View>: View {
    let url: URL
    @ViewBuilder let content: (Image) -> Content
    @ViewBuilder let placeholder: () -> Placeholder
    @State private var image: Image?

    var body: some View {
        Group {
            if let image {
                content(image)
            } else {
                placeholder()
            }
        }
        .task(id: url) { image = await RemoteImages.shared.load(url) }
    }
}

/// Loads and keeps pictures for the session; a failed address isn't retried until the next launch.
actor RemoteImages {
    static let shared = RemoteImages()
    private let cache = NSCache<NSURL, UIImage>()
    private var failed: Set<URL> = []

    func load(_ url: URL) async -> Image? {
        if let cached = cache.object(forKey: url as NSURL) { return Image(uiImage: cached) }
        if failed.contains(url) { return nil }
        guard let (data, response) = try? await URLSession.shared.data(from: url),
              (response as? HTTPURLResponse).map({ (200..<300).contains($0.statusCode) }) ?? true,
              let picture = UIImage(data: data), picture.size.width > 2, picture.size.height > 2 else {
            failed.insert(url)
            return nil
        }
        cache.setObject(picture, forKey: url as NSURL)
        return Image(uiImage: picture)
    }
}
