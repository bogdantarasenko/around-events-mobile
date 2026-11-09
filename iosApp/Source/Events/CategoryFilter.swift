import AroundKit
import SwiftUI

/// "Music 12 · Sport 4 …": outlined chips with counts that filter a list by category.
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
                        HStack(spacing: 4) {
                            Text(Labels.category(item.key, strings)).foregroundStyle(isOn ? Color.white : Color.primary)
                            Text("\(item.count)").foregroundStyle(isOn ? Color.white.opacity(0.7) : Color.secondary)
                        }
                        .font(.subheadline)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(isOn ? Color.primary : Color.clear, in: Capsule())
                        .overlay(Capsule().stroke(Color(.separator), lineWidth: isOn ? 0 : 0.5))
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(isOn ? .isSelected : [])
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 4)
        }
    }
}
