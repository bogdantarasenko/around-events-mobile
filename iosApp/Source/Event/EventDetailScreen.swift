import AroundKit
import SwiftUI

/// The event as in the designs: grey page over the blurred poster, the poster card, title, three buttons, details.
struct EventDetailScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale
    @Environment(\.openURL) private var openURL
    @StateObject private var store: VMStoreOwner<EventDetailViewModel>
    @State private var state: EventDetailUiState?

    init(eventId: Int64, path: Binding<[AppRoute]>) {
        _path = path
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
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func content(_ event: Event, nextDates: [String]) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if event.posterUrl != nil {
                    EventPosterCard(event: event).padding(.bottom, 16)
                }
                Text(event.title)
                    .font(.title.weight(.bold))
                Text(Dates.when(event, strings: strings, locale: locale))
                    .font(.callout)
                    .foregroundStyle(.white.opacity(0.85))
                    .padding(.top, 8)
                EventActions(event: event) { MapLink.open(event, openURL: openURL) }.padding(.top, 18)
                EventFacts(event: event, nextDates: nextDates) { path.append(.organizer(slug: $0)) }
                    .padding(.top, 22)
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
            .padding(.bottom, 110)
            .foregroundStyle(.white)
        }
        .scrollIndicators(.hidden)
        .background(EventBackdrop(event: event))
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .toolbar {
            if let shared = Links.url(event.postUrl ?? event.eventUrl) {
                ToolbarItem(placement: .topBarTrailing) {
                    ShareLink(item: shared, subject: Text(event.title)) { Image(systemName: "square.and.arrow.up") }
                        .accessibilityLabel(strings.share)
                }
            }
        }
        .environment(\.colorScheme, .dark)
    }
}
