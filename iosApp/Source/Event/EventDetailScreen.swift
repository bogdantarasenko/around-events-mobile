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
        .toolbar(.hidden, for: .navigationBar)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func content(_ event: Event, nextDates: [String]) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                EventPosterCard(event: event)
                Text(event.title)
                    .font(.system(size: 26, weight: .bold))
                    .tracking(0.17)
                    .padding(.top, 16)
                Text(Dates.when(event, strings: strings, locale: locale))
                    .font(.system(size: 16))
                    .tracking(-0.31)
                    .foregroundStyle(Color(white: 0.7))
                    .padding(.top, 8)
                EventActions(event: event) { MapLink.open(event, openURL: openURL) }.padding(.top, 18)
                EventFacts(event: event, nextDates: nextDates) { path.append(.organizer(slug: $0)) }
                    .padding(.top, 22)
            }
            .padding(.horizontal, 20)
            .padding(.top, 56)
            .padding(.bottom, 110)
            .foregroundStyle(.white)
        }
        .scrollIndicators(.hidden)
        .background(EventBackdrop(event: event))
        .overlay(alignment: .top) {
            HStack {
                GlassCircle(systemImage: "chevron.left", label: strings.back, dark: true) {
                    if !path.isEmpty { path.removeLast() }
                }
                Spacer()
                if let shared = Links.url(event.postUrl ?? event.eventUrl) {
                    ShareLink(item: shared, subject: Text(event.title)) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(.white)
                            .frame(width: 44, height: 44)
                            .background(.ultraThinMaterial, in: Circle())
                            .background(Color.white.opacity(0.16), in: Circle())
                            .overlay(Circle().stroke(Color.white.opacity(0.3), lineWidth: 1.2))
                    }
                    .accessibilityLabel(strings.share)
                }
            }
            .padding(.horizontal, 20)
        }
        .environment(\.colorScheme, .dark)
    }
}
