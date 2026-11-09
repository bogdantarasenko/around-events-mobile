import SwiftUI

/// A white rounded card of settings rows.
struct SettingsGroup<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(spacing: 0) { content }
            .buttonStyle(.plain)
            .background(Palette.surface, in: RoundedRectangle(cornerRadius: 22))
    }

    /// The group with its grey caption above, as "Preferences" in the designs.
    func titled(_ title: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.system(size: 17)).foregroundStyle(Palette.tertiary).padding(.leading, 16)
            self
        }
        .padding(.top, 22)
    }
}
