import SwiftUI

/// "All" and the categories the listing has, as chips in one scrolling row.
struct CategoryFilter: View {
    let categories: [String]
    let selected: String?
    let onSelect: (String?) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                chip(strings.feedAll, isOn: selected == nil) { onSelect(nil) }
                ForEach(categories, id: \.self) { key in
                    chip(Labels.category(key, strings), isOn: selected == key) { onSelect(key) }
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
    }

    private func chip(_ title: String, isOn: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(.medium))
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .foregroundStyle(isOn ? Color.white : Color.primary)
                .background(isOn ? Color.accentColor : Color(.secondarySystemFill), in: Capsule())
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isOn ? .isSelected : [])
    }
}
