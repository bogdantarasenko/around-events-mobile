import AroundKit
import SwiftUI

/// The events under the map in a system sheet: the tapped place's, or every event on the map, then how many are
/// not on the map yet.
struct MapEventsSheet: View {
    let state: MapUiState
    let onOpen: (Int64) -> Void
    @Environment(\.strings) private var strings
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                ForEach(Array(state.sheet.enumerated()), id: \.element.id) { index, event in
                    Button { onOpen(event.id) } label: {
                        EventRow(event: event, compact: false, showsSeparator: index < state.sheet.count - 1)
                    }
                    .buttonStyle(.pressable)
                    .listRowSeparator(.hidden)
                }
                if state.unlocated > 0, !state.isSelection {
                    Text(strings.mapUnlocated("\(state.unlocated)"))
                        .font(.footnote)
                        .foregroundStyle(Palette.secondary)
                        .listRowSeparator(.hidden)
                }
            }
            .listStyle(.plain)
            .navigationTitle(strings.eventsCount("\(state.sheet.count)"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: { Image(systemName: "xmark") }
                        .accessibilityLabel(strings.close)
                }
            }
        }
    }
}
