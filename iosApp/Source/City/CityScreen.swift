import AroundKit
import SwiftUI

/// A city's page as in the designs: a dark hero "What's happening in Kraków", the button to make it the app's
/// city, then its events by day.
struct CityScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store: VMStoreOwner<EventsViewModel>
    @State private var state: EventsUiState?
    private let index: Int

    init(slug: String, path: Binding<[AppRoute]>) {
        _path = path
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getEventsViewModel(category: nil, citySlug: slug)))
        // Stable across launches, unlike hashValue.
        index = slug.unicodeScalars.reduce(0) { $0 + Int($1.value) }
    }

    var body: some View {
        let state = state ?? store.instance.state.value
        List {
            hero(state)
                .listRowInsets(EdgeInsets())
                .listRowSeparator(.hidden)
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 40).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
            Color.clear.frame(height: 80).listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .scrollIndicators(.hidden)
        .ignoresSafeArea(edges: .top)
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.hidden, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .refreshable { store.instance.refresh() }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func hero(_ state: EventsUiState) -> some View {
        ZStack(alignment: .bottomLeading) {
            LinearGradient(colors: CityStyle.colors(index), startPoint: .topLeading, endPoint: .bottomTrailing)
            LinearGradient(colors: [.clear, .black.opacity(0.75)], startPoint: .top, endPoint: .bottom)
            VStack(alignment: .leading, spacing: 4) {
                Text(strings.cityWhatsOn).font(.subheadline).foregroundStyle(.white.opacity(0.75))
                Text(state.city?.name ?? "").font(.largeTitle.weight(.bold)).foregroundStyle(.white)
                Text(strings.eventsCount("\(state.count)"))
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.75))
                    .padding(.top, 6)
                Button {
                    store.instance.makeCurrent()
                } label: {
                    Text(state.isCurrentCity ? strings.cityIsCurrent : strings.cityMakeCurrent)
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 11)
                        .frame(minHeight: 33)
                        .background(state.isCurrentCity ? Color.white.opacity(0.2) : Color.green.opacity(0.85), in: Capsule())
                }
                .buttonStyle(.pressable)
                .disabled(state.isCurrentCity)
                .sensoryFeedback(.success, trigger: state.isCurrentCity)
                .padding(.top, 14)
            }
            .padding(20)
        }
        .frame(height: 400)
    }
}
