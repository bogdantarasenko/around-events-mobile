import AroundKit
import SwiftUI

/// Three 55pt buttons under the title, as Register / Contact / More in the designs: the first white, the others
/// translucent. Ours are tickets or the link, the channel post, the map and sharing — the first three there are.
struct EventActions: View {
    let event: Event
    let onOpenMap: () -> Void
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL

    var body: some View {
        let items = actions
        HStack(spacing: 8) {
            ForEach(Array(items.prefix(3).enumerated()), id: \.offset) { index, item in
                button(item, primary: index == 0)
            }
        }
    }

    private enum Item {
        case open(String, String, URL)
        case run(String, String, () -> Void)
        case share(String, URL)
    }

    private var actions: [Item] {
        var items: [Item] = []
        if let tickets = Links.url(event.ticketsUrl) {
            items.append(.open(strings.detailTickets, "ticket.fill", tickets))
        } else if let link = Links.url(event.eventUrl) {
            items.append(.open(strings.detailLink, "link", link))
        }
        if let post = Links.url(event.postUrl) { items.append(.open(strings.openPost, "paperplane.fill", post)) }
        if event.venue != nil || event.address != nil { items.append(.run(strings.mapTitle, "map.fill", onOpenMap)) }
        if let shared = Links.url(event.postUrl ?? event.ticketsUrl ?? event.eventUrl) {
            items.append(.share(strings.share, shared))
        }
        return items
    }

    @ViewBuilder
    private func button(_ item: Item, primary: Bool) -> some View {
        switch item {
        case let .open(title, icon, url):
            Button { openURL(url) } label: { label(title, icon, primary) }.buttonStyle(.pressable)
        case let .run(title, icon, action):
            Button(action: action) { label(title, icon, primary) }.buttonStyle(.pressable)
        case let .share(title, url):
            ShareLink(item: url, subject: Text(event.title)) { label(title, "square.and.arrow.up", primary) }
                .buttonStyle(.pressable)
        }
    }

    private func label(_ title: String, _ systemImage: String, _ primary: Bool) -> some View {
        VStack(spacing: 5) {
            Image(systemName: systemImage).font(.body.weight(.semibold))
            Text(title).font(.footnote.weight(.semibold)).lineLimit(1)
        }
        .frame(maxWidth: .infinity)
        .frame(minHeight: 55)
        .foregroundStyle(primary ? Color(white: 0.08) : Color(white: 0.91))
        .background(primary ? AnyShapeStyle(Color.white) : AnyShapeStyle(Color.white.opacity(0.18)), in: RoundedRectangle(cornerRadius: 16))
    }
}
