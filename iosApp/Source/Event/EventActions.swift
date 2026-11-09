import AroundKit
import SwiftUI

/// The three buttons under the title: tickets (white, the main one), the channel post, and the link.
struct EventActions: View {
    let event: Event
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL

    var body: some View {
        HStack(spacing: 8) {
            if let tickets = Links.url(event.ticketsUrl) {
                action(strings.detailTickets, "ticket", primary: true) { openURL(tickets) }
            }
            if let post = Links.url(event.postUrl) {
                action(strings.openPost, "paperplane", primary: event.ticketsUrl == nil) { openURL(post) }
            }
            if let link = Links.url(event.eventUrl) {
                action(strings.detailLink, "link", primary: event.ticketsUrl == nil && event.postUrl == nil) { openURL(link) }
            }
        }
    }

    private func action(_ title: String, _ systemImage: String, primary: Bool, run: @escaping () -> Void) -> some View {
        Button(action: run) {
            VStack(spacing: 4) {
                Image(systemName: systemImage).font(.system(size: 15, weight: .semibold))
                Text(title).font(.caption.weight(.medium)).lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 10)
            .foregroundStyle(primary ? Color.black : Color.white)
            .background(primary ? AnyShapeStyle(Color.white) : AnyShapeStyle(Color.white.opacity(0.15)), in: RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}
