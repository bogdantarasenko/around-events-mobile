import AroundKit
import SwiftUI

/// Three tabs, each with its own navigation stack, under the floating bar from the designs.
struct HomeScreen: View {
    @StateObject private var discoverStore = VMStoreOwner<DiscoverViewModel>(Deps.shared.getDiscoverViewModel())
    @State private var tab: AppTab = .home
    @State private var homePath: [AppRoute] = []
    @State private var discoverPath: [AppRoute] = []
    @State private var mapPath: [AppRoute] = []

    var body: some View {
        ZStack(alignment: .bottom) {
            switch tab {
            case .home:
                stack($homePath) { HomeFeedScreen(discover: discoverStore.instance, path: $homePath) }
            case .discover:
                stack($discoverPath) {
                    DiscoverScreen(viewModel: discoverStore.instance, path: $discoverPath) { tab = .map }
                }
            case .map:
                stack($mapPath) { MapScreen(path: $mapPath) }
            }
            FloatingTabBar(selection: $tab, dark: currentPath.last.map(isDark) ?? false)
                .padding(.bottom, 8)
        }
        .ignoresSafeArea(.keyboard)
        .onAppear(perform: openFromLaunchArguments)
    }

    // Debug builds only: `-AroundTab discover|map` and `-AroundRoute event:<id>|organizer:<slug>|events:<category>`
    // open a screen at launch, so the simulator can be checked without tapping.
    private func openFromLaunchArguments() {
        #if DEBUG
        let defaults = UserDefaults.standard
        switch defaults.string(forKey: "AroundTab") {
        case "discover": tab = .discover
        case "map": tab = .map
        default: break
        }
        guard let route = defaults.string(forKey: "AroundRoute")?.split(separator: ":", maxSplits: 1),
              route.count == 2 else { return }
        let value = String(route[1])
        let target: AppRoute? = switch route[0] {
        case "event": Int64(value).map { .eventDetail($0) }
        case "organizer": .organizer(slug: value)
        case "events": .events(category: value)
        default: nil
        }
        if let target { homePath.append(target) }
        #endif
    }

    private var currentPath: [AppRoute] {
        switch tab {
        case .home: homePath
        case .discover: discoverPath
        case .map: mapPath
        }
    }

    // The event page is dark; the bar goes glass-on-dark over it, as in the designs.
    private func isDark(_ route: AppRoute) -> Bool {
        if case .eventDetail = route { return true }
        return false
    }

    private func stack<Root: View>(_ path: Binding<[AppRoute]>, @ViewBuilder root: () -> Root) -> some View {
        NavigationStack(path: path) {
            root()
                .navigationDestination(for: AppRoute.self) { route in
                    switch route {
                    case let .eventDetail(id): EventDetailScreen(eventId: id, path: path)
                    case let .events(category): EventsScreen(category: category, path: path)
                    case let .organizer(slug): OrganizerScreen(slug: slug, path: path)
                    case .map: MapScreen(path: path)
                    case .search: SearchScreen()
                    case .settings: SettingsScreen()
                    }
                }
        }
    }
}
