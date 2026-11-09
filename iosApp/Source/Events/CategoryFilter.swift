import AroundKit
import SwiftUI

/// "Cake Decor 1 · Clay 1 · Club 10": outlined pills with counts, as on the organizer page in the designs.
struct CategoryFilter: View {
    let categories: [CategoryCount]
    let selected: String?
    let onSelect: (String?) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(categories, id: \.key) { item in
                    let isOn = selected == item.key
                    Button {
                        onSelect(isOn ? nil : item.key)
                    } label: {
                        HStack(spacing: 5) {
                            Text(Labels.category(item.key, strings)).foregroundStyle(isOn ? Palette.surface : Palette.chipText)
                            Text("\(item.count)").foregroundStyle(isOn ? Palette.surface.opacity(0.7) : Palette.chipCount)
                        }
                        .font(.system(size: 14.5))
                        .padding(.horizontal, 11)
                        .frame(height: 33.5)
                        .background(isOn ? Palette.ink : Palette.surface, in: Capsule())
                        .overlay(Capsule().stroke(Palette.chipBorder, lineWidth: isOn ? 0 : 1))
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(isOn ? .isSelected : [])
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 8)
        }
    }
}
