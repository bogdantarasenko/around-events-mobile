import AroundKit
import SwiftUI

/// The Home tab: the city's events by day, as "Picked for you" in the designs. The large title is the city and
/// switches it from the title menu; "+" opens the mini-app to submit, the gear opens settings.
struct HomeFeedScreen: View {
    let discover: DiscoverViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<EventsViewModel>(Deps.shared.getEventsViewModel(category: nil, citySlug: nil))
    @State private var state: EventsUiState?
    @State private var cities: DiscoverUiState?
    @State private var showsSubmit = false

    var body: some View {
        let state = state ?? store.instance.state.value
        let cities = cities ?? discover.state.value
        List {
            if let error = state.error ?? cities.error {
                ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { discover.refresh() }
                    .listRowSeparator(.hidden)
            }
            if !state.filters.isEmpty {
                CategoryFilter(categories: state.filters, selected: state.filter) { store.instance.selectFilter(key: $0) }
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
            }
            if state.isLoading || cities.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 60).listRowSeparator(.hidden)
            } else if state.days.isEmpty {
                Text(strings.feedEmptyText).foregroundStyle(Palette.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
        }
        .listStyle(.plain)
        // The title stays for VoiceOver and the back menu, out of sight behind the brand.
        .navigationTitle(cities.city?.name ?? strings.tabHome)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) { Color.clear.frame(width: 1, height: 1) }
            ToolbarItem(placement: .topBarLeading) { CityBrandMenu(state: cities) { discover.selectCity(slug: $0) } }
                .hidingSharedBackground()
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { showsSubmit = true } label: { Image(systemName: "plus") }
                    .accessibilityLabel(strings.feedSubmit)
                Button { path.append(.settings) } label: { Image(systemName: "gearshape") }
                    .accessibilityLabel(strings.settingsTitle)
            }
        }
        .refreshable { discover.refresh() }
        .fullScreenCover(isPresented: $showsSubmit) { MiniAppSheet(screen: debugMiniApp ?? .submit) }
        .onAppear {
            #if DEBUG
            // `-AroundMiniApp submit|me` opens the mini-app at launch, to check it in the simulator.
            if debugMiniApp != nil { showsSubmit = true }
            #endif
        }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
        .task {
            for await value in discover.state {
                self.cities = value
            }
        }
    }

    private var debugMiniApp: MiniAppScreen? {
        switch UserDefaults.standard.string(forKey: "AroundMiniApp") {
        case "submit": .submit
        case "me": .myEvents
        default: nil
        }
    }
}
