import SwiftUI

/// A 44pt round button over a picture or a map: back, share, search.
struct GlassCircle: View {
    let systemImage: String
    let label: String
    var dark = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(dark ? Color.white : Palette.ink)
                .frame(width: 44, height: 44)
                .background(.ultraThinMaterial, in: Circle())
                .background((dark ? Color.white.opacity(0.16) : Color.white.opacity(0.5)), in: Circle())
                .overlay(Circle().stroke(Color.white.opacity(dark ? 0.3 : 0.35), lineWidth: 1.2))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}
