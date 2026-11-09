import AroundKit
import SwiftUI

/// The categories the city has, as tiles in two columns: the icon on its tint, the name and how many events.
struct CategoryTiles: View {
    let categories: [CategoryCount]
    let onOpen: (String) -> Void
    @Environment(\.strings) private var strings

    private let columns = [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            SectionTitle(title: strings.discoverCategories)
            LazyVGrid(columns: columns, spacing: 10) {
                ForEach(categories, id: \.key) { item in
                    Button { onOpen(item.key) } label: { tile(item) }
                        .buttonStyle(.pressable)
                }
            }
            .padding(.horizontal, 20)
        }
    }

    private func tile(_ item: CategoryCount) -> some View {
        HStack(spacing: 12) {
            Image(systemName: CategoryStyle.symbol(item.key))
                .font(.body.weight(.medium))
                .foregroundStyle(CategoryStyle.tint(item.key))
                .frame(width: 40, height: 40)
                .background(CategoryStyle.tint(item.key).opacity(0.16), in: Circle())
            VStack(alignment: .leading, spacing: 2) {
                Text(Labels.category(item.key, strings))
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(Palette.ink)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                Text(strings.eventsCount("\(item.count)"))
                    .font(.caption)
                    .foregroundStyle(Palette.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding(12)
        .frame(maxWidth: .infinity, minHeight: 72, alignment: .leading)
        .background(Palette.surface, in: RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Palette.chipBorder, lineWidth: 1))
    }
}
