import SwiftUI

/// The green price tag next to the organizer line.
struct PriceBadge: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.caption2.weight(.semibold))
            .foregroundStyle(Color.green)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(Color.green.opacity(0.12), in: RoundedRectangle(cornerRadius: 4))
    }
}
