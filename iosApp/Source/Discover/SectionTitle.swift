import SwiftUI

/// "Los Angeles ›" over "Upcoming Events": 20pt semibold, the subtitle in light grey, a small grey chevron.
struct SectionTitle: View {
    let title: String
    var subtitle: String?
    var action: (() -> Void)?

    var body: some View {
        Group {
            if let action {
                Button(action: action) { label(withChevron: true) }.buttonStyle(.plain)
            } else {
                label(withChevron: false)
            }
        }
        .padding(.horizontal, 20)
    }

    private func label(withChevron: Bool) -> some View {
        VStack(alignment: .leading, spacing: 1) {
            HStack(spacing: 14) {
                Text(title).foregroundStyle(Palette.ink)
                if withChevron {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(Palette.chevron)
                }
            }
            if let subtitle {
                Text(subtitle).foregroundStyle(Palette.tertiary)
            }
        }
        .font(.system(size: 20, weight: .semibold))
        .tracking(-0.45)
    }
}
