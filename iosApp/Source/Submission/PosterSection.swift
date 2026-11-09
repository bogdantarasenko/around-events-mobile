import AroundKit
import PhotosUI
import SwiftUI

/// The poster: picked from Photos, or the one a repeat inherits, or the one an edited event already has.
struct PosterSection: View {
    let poster: PosterState
    let problem: String?
    let onPick: (Data) -> Void
    let onRemove: () -> Void
    @Environment(\.strings) private var strings
    @State private var item: PhotosPickerItem?
    @State private var picked: UIImage?

    var body: some View {
        Section {
            if let picked, poster.pickedName != nil {
                Image(uiImage: picked).resizable().scaledToFit().frame(maxHeight: 180)
            } else if let shown = poster.inheritedUrl ?? poster.currentUrl {
                PosterView(url: shown, size: 160)
                Text("\(poster.inheritedUrl != nil ? strings.formPreviousPoster : strings.formCurrentPoster). \(strings.posterInherited)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            PhotosPicker(selection: $item, matching: .images) {
                Text(hasPoster ? strings.posterReplace : strings.posterAttach)
            }
            // An edit keeps its poster on the server: there is nothing to remove from here.
            if poster.pickedName != nil || poster.inheritedUrl != nil {
                Button(strings.posterRemove, role: .destructive) {
                    picked = nil
                    item = nil
                    onRemove()
                }
            }
            if let problem {
                Text(problem).font(.footnote).foregroundStyle(.red)
            }
        } header: {
            Text(strings.formPoster)
        } footer: {
            Text(strings.formPosterFooter)
        }
        .onChange(of: item) { _, newItem in
            Task { await load(newItem) }
        }
    }

    private var hasPoster: Bool {
        poster.pickedName != nil || poster.inheritedUrl != nil || poster.currentUrl != nil
    }

    /// Re-encoded as JPEG: Photos may hand over HEIC, which the server doesn't accept.
    private func load(_ item: PhotosPickerItem?) async {
        guard let item,
              let data = try? await item.loadTransferable(type: Data.self),
              let image = UIImage(data: data),
              let jpeg = image.jpegData(compressionQuality: 0.85)
        else { return }
        picked = image
        onPick(jpeg)
    }
}
