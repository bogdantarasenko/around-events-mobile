import AroundKit
import SwiftUI

struct StatusFilter: View {
    let selected: EventStatus?
    let onSelect: (EventStatus?) -> Void
    @Environment(\.strings) private var strings

    private let statuses: [EventStatus] = [.review, .queued, .published, .rejected, .duplicate, .cancelled]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                chip(strings.filterAll, isOn: selected == nil) { onSelect(nil) }
                ForEach(statuses, id: \.self) { status in
                    chip(Labels.status(status, strings), isOn: selected == status) { onSelect(status) }
                }
            }
        }
    }

    private func chip(_ title: String, isOn: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isOn ? Color.accentColor.opacity(0.18) : Color(.secondarySystemFill), in: Capsule())
        }
        .buttonStyle(.plain)
    }
}
