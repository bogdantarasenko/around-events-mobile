import AroundKit
import SwiftUI

/// The system tab bar — on iOS 26 the floating glass bar of the designs — with a navigation stack per tab.
struct HomeScreen: View {
    @StateObject private var discoverStore = VMStoreOwner<DiscoverViewModel>(Deps.shared.getDiscoverViewModel())
    @Environment(\.strings) private var strings
    @State private var tab: AppTab = .home
    @State private var homePath: [AppRoute] = []
    @State private var discoverPath: [AppRoute] = []
    @State private var mapPath: [AppRoute] = []

    var body: some View {
        TabView(selection: $tab) {
            stack($homePath) { HomeFeedScreen(discover: discoverStore.instance, path: $homePath) }
                .tabItem { Label(strings.tabHome, systemImage: "house.fill") }
                .tag(AppTab.home)
            stack($discoverPath) { DiscoverScreen(viewModel: discoverStore.instance, path: $discoverPath) }
            .tabItem { Label(strings.tabDiscover, systemImage: "safari.fill") }
            .tag(AppTab.discover)
            stack($mapPath) { MapScreen(path: $mapPath) }
                .tabItem { Label(strings.tabMap, systemImage: "map.fill") }
                .tag(AppTab.map)
        }
        // Ink, not the brand orange: the designs mark the selected tab and toolbar icons in black.
        .tint(Palette.ink)
        .onAppear(perform: openFromLaunchArguments)
    }

    private func stack<Root: View>(_ path: Binding<[AppRoute]>, @ViewBuilder root: () -> Root) -> some View {
        NavigationStack(path: path) {
            root()
                .navigationDestination(for: AppRoute.self) { route in
                    switch route {
                    case let .eventDetail(id): EventDetailScreen(eventId: id, path: path)
                    case let .events(category): EventsScreen(category: category, path: path)
                    case .cities: CitiesScreen(discover: discoverStore.instance, path: path)
                    case let .city(slug): CityScreen(slug: slug, path: path)
                    case let .organizer(slug): OrganizerScreen(slug: slug, path: path)
                    case .map: MapScreen(path: path)
                    case .search: SearchScreen(path: path)
                    case .settings: SettingsScreen()
                    }
                }
        }
    }

    // Debug builds only: `-AroundTab discover|map` and `-AroundRoute event:<id>|organizer:<slug>|events:<category>|city:<slug>|cities|search|settings`
    // open a screen at launch, so the simulator can be checked without tapping.
    private func openFromLaunchArguments() {
        #if DEBUG
        let defaults = UserDefaults.standard
        switch defaults.string(forKey: "AroundTab") {
        case "discover": tab = .discover
        case "map": tab = .map
        default: break
        }
        guard let route = defaults.string(forKey: "AroundRoute")?.split(separator: ":", maxSplits: 1) else { return }
        let value = route.count == 2 ? String(route[1]) : ""
        let target: AppRoute? = switch route[0] {
        case "event": Int64(value).map { .eventDetail($0) }
        case "organizer": .organizer(slug: value)
        case "events": .events(category: value)
        case "city": .city(slug: value)
        case "cities": .cities
        case "search": .search
        case "settings": .settings
        default: nil
        }
        if let target { homePath.append(target) }
        #endif
    }
}
