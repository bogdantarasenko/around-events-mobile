import AroundKit
import SwiftUI

/// Discover at the root, everything else pushed on top.
struct HomeScreen: View {
    @StateObject private var discoverStore = VMStoreOwner<DiscoverViewModel>(Deps.shared.getDiscoverViewModel())
    @State private var path: [AppRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            DiscoverScreen(viewModel: discoverStore.instance, path: $path)
                .navigationDestination(for: AppRoute.self) { route in
                    switch route {
                    case let .eventDetail(id): EventDetailScreen(eventId: id, path: $path)
                    case let .events(category): EventsScreen(category: category)
                    case let .organizer(slug): OrganizerScreen(slug: slug)
                    case .map: MapScreen(path: $path)
                    case .search: SearchScreen()
                    case .settings: SettingsScreen()
                    }
                }
        }
    }
}
