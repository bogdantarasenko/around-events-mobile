import AroundKit
import SwiftUI

/// "Kraków › / Upcoming events": the next events in columns of three, swiped sideways.
struct UpcomingShelf: View {
    let state: DiscoverUiState
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionTitle(title: state.city?.name ?? strings.tabFeed, subtitle: strings.discoverUpcoming) {
                path.append(.events(category: nil))
            }
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(alignment: .top, spacing: 16) {
                    ForEach(Array(columns.enumerated()), id: \.offset) { _, column in
                        VStack(spacing: 0) {
                            ForEach(Array(column.enumerated()), id: \.element.id) { index, event in
                                Button {
                                    path.append(.eventDetail(event.id))
                                } label: {
                                    EventRow(event: event, showsDay: true)
                                }
                                .buttonStyle(.plain)
                                if index < column.count - 1 { Divider().padding(.leading, 78) }
                            }
                        }
                        .containerRelativeFrame(.horizontal) { width, _ in width - 56 }
                    }
                }
                .scrollTargetLayout()
            }
            .scrollTargetBehavior(.viewAligned)
            .contentMargins(.horizontal, 16, for: .scrollContent)
        }
    }

    private var columns: [[Event]] {
        stride(from: 0, to: state.upcoming.count, by: 3).map { Array(state.upcoming[$0..<min($0 + 3, state.upcoming.count)]) }
    }
}
