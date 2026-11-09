import AroundKit
import SwiftUI

/// "16 events" under the map: the tapped place's events, or all of them.
struct MapSheet: View {
    let state: MapUiState
    let onClose: () -> Void
    let onOpen: (Int64) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Text(strings.eventsCount("\(state.sheet.count)")).font(.headline)
                if state.isSelection {
                    HStack {
                        Spacer()
                        Button(action: onClose) {
                            Image(systemName: "xmark").font(.headline).frame(width: 32, height: 32)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(strings.close)
                    }
                }
            }
            .padding()
            List {
                ForEach(state.sheet, id: \.id) { event in
                    Button { onOpen(event.id) } label: { EventRow(event: event, showsDay: true) }
                        .buttonStyle(.plain)
                }
                if state.unlocated > 0, !state.isSelection {
                    Text(strings.mapUnlocated("\(state.unlocated)"))
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .listRowSeparator(.hidden)
                }
            }
            .listStyle(.plain)
        }
    }
}
