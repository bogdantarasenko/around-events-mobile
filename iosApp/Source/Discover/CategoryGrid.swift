import AroundKit
import SwiftUI

/// "Browse by category": emoji chips in two rows that scroll sideways together.
struct CategoryGrid: View {
    let categories: [CategoryCount]
    let onOpen: (String) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionTitle(title: strings.discoverCategories)
            ScrollView(.horizontal, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 8) {
                    row(Array(categories.enumerated().filter { $0.offset.isMultiple(of: 2) }.map(\.element)))
                    row(Array(categories.enumerated().filter { !$0.offset.isMultiple(of: 2) }.map(\.element)))
                }
                .padding(.horizontal)
            }
        }
    }

    private func row(_ items: [CategoryCount]) -> some View {
        HStack(spacing: 8) {
            ForEach(items, id: \.key) { item in
                Button {
                    onOpen(item.key)
                } label: {
                    HStack(spacing: 6) {
                        Text(CategoryStyle.emoji(item.key))
                        Text(Labels.category(item.key, strings)).foregroundStyle(.primary)
                    }
                    .font(.subheadline)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(.separator), lineWidth: 0.5))
                }
                .buttonStyle(.plain)
            }
        }
    }
}
