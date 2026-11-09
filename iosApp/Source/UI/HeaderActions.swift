import SwiftUI

/// The white pill with two icons at the top right of Home and Discover: 88 × 44, a soft shadow.
struct HeaderActions: View {
    let first: (systemImage: String, label: String, action: () -> Void)
    let second: (systemImage: String, label: String, action: () -> Void)

    var body: some View {
        HStack(spacing: 0) {
            button(first)
            button(second)
        }
        .frame(width: 88, height: 44)
        .background(Palette.surface, in: Capsule())
        .shadow(color: .black.opacity(0.08), radius: 4, y: 2)
    }

    private func button(_ item: (systemImage: String, label: String, action: () -> Void)) -> some View {
        Button(action: item.action) {
            Image(systemName: item.systemImage)
                .font(.system(size: 18, weight: .medium))
                .foregroundStyle(Palette.ink)
                .frame(width: 44, height: 44)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(item.label)
    }
}
