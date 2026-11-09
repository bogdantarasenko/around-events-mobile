import AroundKit
import SwiftUI

/// "Kraków › / Upcoming events": columns of three rows, the next column peeking in from the right.
struct UpcomingShelf: View {
    let state: DiscoverUiState
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            SectionTitle(title: state.city?.name ?? strings.tabFeed, subtitle: strings.discoverUpcoming) {
                path.append(.events(category: nil))
            }
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack(alignment: .top, spacing: 20) {
                    ForEach(Array(columns.enumerated()), id: \.offset) { _, column in
                        VStack(spacing: 0) {
                            ForEach(Array(column.enumerated()), id: \.element.id) { index, event in
                                Button {
                                    path.append(.eventDetail(event.id))
                                } label: {
                                    EventRow(event: event, compact: true, showsSeparator: index < column.count - 1)
                                }
                                .buttonStyle(.pressable)
                            }
                        }
                        // The design's 333pt column on a 393pt screen: 20 margin, 20 gap, 20 of the next.
                        .containerRelativeFrame(.horizontal) { width, _ in width - 60 }
                    }
                }
                .scrollTargetLayout()
            }
            .scrollTargetBehavior(.viewAligned)
            .contentMargins(.horizontal, 20, for: .scrollContent)
        }
    }

    private var columns: [[Event]] {
        stride(from: 0, to: state.upcoming.count, by: 3).map { Array(state.upcoming[$0..<min($0 + 3, state.upcoming.count)]) }
    }
}
