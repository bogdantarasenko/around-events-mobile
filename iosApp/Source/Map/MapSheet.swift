import AroundKit
import SwiftUI

/// The card over the map in the designs: a grabber, "16 events" with a close button, and the event rows. Drag or
/// tap the grabber to open it to full height.
struct MapSheet: View {
    let state: MapUiState
    @Binding var expanded: Bool
    let onClose: () -> Void
    let onOpen: (Int64) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(spacing: 0) {
            Capsule().fill(Palette.chipBorder).frame(width: 36, height: 5).padding(.top, 6)
            ZStack {
                Text(strings.eventsCount("\(state.sheet.count)"))
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundStyle(Palette.ink)
                HStack {
                    Spacer()
                    if state.isSelection {
                        Button(action: onClose) {
                            Image(systemName: "xmark")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundStyle(Palette.ink)
                                .frame(width: 44, height: 44)
                                .background(Palette.tabSelected, in: Circle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(strings.close)
                    }
                }
            }
            .frame(height: 50)
            .padding(.horizontal, 16)
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(Array(state.sheet.enumerated()), id: \.element.id) { index, event in
                        Button { onOpen(event.id) } label: {
                            EventRow(event: event, compact: false, showsSeparator: index < state.sheet.count - 1)
                        }
                        .buttonStyle(.plain)
                    }
                    if state.unlocated > 0, !state.isSelection {
                        Text(strings.mapUnlocated("\(state.unlocated)"))
                            .font(.system(size: 13))
                            .foregroundStyle(Palette.secondary)
                            .padding(.vertical, 12)
                    }
                }
                .padding(.horizontal, 19)
            }
        }
        .frame(height: expanded ? 620 : 300)
        .background(Palette.surface, in: RoundedRectangle(cornerRadius: 34))
        .shadow(color: .black.opacity(0.12), radius: 16, y: 4)
        .padding(.horizontal, 9)
        .gesture(
            DragGesture(minimumDistance: 12).onEnded { drag in
                withAnimation(.spring) { expanded = drag.translation.height < 0 }
            }
        )
        .onTapGesture(count: 2) { withAnimation(.spring) { expanded.toggle() } }
    }
}
