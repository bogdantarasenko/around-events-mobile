import SwiftUI

/// The round buttons over content in the designs: back, share, map, search.
struct CircleButton: View {
    let systemImage: String
    let label: String
    var dark = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: 16, weight: .semibold))
                .frame(width: 36, height: 36)
                .foregroundStyle(dark ? Color.white : Color.primary)
                .background(dark ? AnyShapeStyle(.white.opacity(0.18)) : AnyShapeStyle(.regularMaterial), in: Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}
