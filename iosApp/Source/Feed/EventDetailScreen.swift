import AroundKit
import SwiftUI

struct EventDetailScreen: View {
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale
    @Environment(\.openURL) private var openURL
    @StateObject private var store: VMStoreOwner<EventDetailViewModel>
    @State private var state: EventDetailUiState?

    init(eventId: Int64) {
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getEventDetailViewModel(eventId: eventId)))
    }

    var body: some View {
        let state = state ?? store.instance.state.value
        Group {
            if let event = state.event {
                content(event, nextDates: state.nextDates)
            } else {
                ContentUnavailableView(strings.eventGone, systemImage: "calendar.badge.exclamationmark")
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func content(_ event: Event, nextDates: [String]) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                if let poster = event.posterUrl, let url = URL(string: poster) {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        Color(.secondarySystemFill).aspectRatio(1, contentMode: .fit)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                if let category = event.category {
                    Text(Labels.category(category, strings).uppercased())
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(Color.accentColor)
                }
                Text(event.title).font(.title2.weight(.semibold))
                Text(Dates.when(event, strings: strings, locale: locale)).foregroundStyle(.secondary)
                actions(event)
                Divider()
                EventFacts(event: event, nextDates: nextDates)
            }
            .padding()
        }
    }

    private func actions(_ event: Event) -> some View {
        VStack(spacing: 10) {
            if let tickets = link(event.ticketsUrl ?? event.eventUrl) {
                Button { openURL(tickets) } label: {
                    Text(event.ticketsUrl != nil ? strings.detailTickets : strings.detailLink).frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
            }
            HStack {
                if let post = link(event.postUrl) {
                    Button { openURL(post) } label: { Text(strings.openPost).frame(maxWidth: .infinity) }
                        .buttonStyle(.bordered)
                }
                if let shared = link(event.postUrl ?? event.eventUrl) {
                    ShareLink(item: shared, subject: Text(event.title)) {
                        Text(strings.share).frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                }
            }
        }
    }

    /// Links come as people typed them: `t.me/name` without a scheme is common.
    private func link(_ raw: String?) -> URL? {
        guard let raw = raw?.trimmingCharacters(in: .whitespaces), !raw.isEmpty else { return nil }
        return URL(string: raw.contains("://") ? raw : "https://" + raw)
    }
}
