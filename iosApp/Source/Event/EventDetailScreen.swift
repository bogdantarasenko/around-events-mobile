import AroundKit
import SwiftUI

/// The event as in the designs: dark, over its own blurred poster, with the poster card, actions and details.
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
        .toolbar(.hidden, for: .navigationBar)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func content(_ event: Event, nextDates: [String]) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                EventThumb(event: event, size: posterSize, cornerRadius: 18)
                    .frame(maxWidth: .infinity)
                    .shadow(color: .black.opacity(0.35), radius: 16, y: 8)
                VStack(alignment: .leading, spacing: 6) {
                    Text(event.title).font(.title2.weight(.bold))
                    Text(Dates.when(event, strings: strings, locale: locale)).foregroundStyle(.white.opacity(0.7))
                }
                EventActions(event: event)
                EventFacts(event: event, nextDates: nextDates) { path.append(.organizer(slug: $0)) }
            }
            .padding(.horizontal, 20)
            .padding(.top, 64)
            .padding(.bottom, 32)
            .foregroundStyle(.white)
        }
        .background(EventBackdrop(event: event))
        .overlay(alignment: .top) {
            HStack {
                CircleButton(systemImage: "chevron.left", label: strings.back, dark: true) {
                    if !path.isEmpty { path.removeLast() }
                }
                Spacer()
                if let shared = Links.url(event.postUrl ?? event.eventUrl) {
                    ShareLink(item: shared, subject: Text(event.title)) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(width: 36, height: 36)
                            .foregroundStyle(.white)
                            .background(.white.opacity(0.18), in: Circle())
                    }
                    .accessibilityLabel(strings.share)
                }
            }
            .padding(.horizontal)
        }
        .preferredColorScheme(.dark)
    }

    private var posterSize: CGFloat { 300 }
}
