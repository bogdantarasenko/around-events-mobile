import AroundKit
import SwiftUI

/// "Browse by category": white chips with a hairline border, an icon and a 15pt label, in two rows that scroll
/// sideways together.
struct CategoryGrid: View {
    let categories: [CategoryCount]
    let onOpen: (String) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            SectionTitle(title: strings.discoverCategories)
            ScrollView(.horizontal, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 9.5) {
                    row(Array(categories.prefix(half)))
                    row(Array(categories.dropFirst(half)))
                }
                .padding(.horizontal, 20)
            }
        }
    }

    private var half: Int { (categories.count + 1) / 2 }

    private func row(_ items: [CategoryCount]) -> some View {
        HStack(spacing: 9) {
            ForEach(items, id: \.key) { item in
                Button {
                    onOpen(item.key)
                } label: {
                    HStack(spacing: 8.5) {
                        Image(systemName: CategoryStyle.symbol(item.key))
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(CategoryStyle.tint(item.key))
                            .frame(width: 22, height: 22)
                        Text(Labels.category(item.key, strings))
                            .font(.subheadline)
                            .foregroundStyle(Palette.ink)
                    }
                    .padding(.leading, 10.5)
                    .padding(.trailing, 13)
                    .frame(minHeight: 38.5)
                    .background(Palette.surface, in: RoundedRectangle(cornerRadius: 12))
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Palette.chipBorder, lineWidth: 1))
                }
                .buttonStyle(.pressable)
            }
        }
    }
}
