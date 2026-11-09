import SwiftUI

/// The green price tag: 12pt medium on a pale green, 5pt corners.
struct PriceBadge: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.caption.weight(.medium))
            .foregroundStyle(Palette.price)
            .padding(.horizontal, 6)
            .padding(.vertical, 1.5)
            .background(Palette.priceBackground, in: RoundedRectangle(cornerRadius: 5))
    }
}
