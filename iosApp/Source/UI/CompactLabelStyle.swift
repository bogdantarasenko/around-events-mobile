import SwiftUI

/// Icon and text close together, as in the designs' "🕒 7:00 PM" lines.
struct CompactLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 4) {
            configuration.icon.imageScale(.small)
            configuration.title
        }
    }
}
