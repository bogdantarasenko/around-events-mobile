import AroundKit
import SwiftUI

/// The feed, with event pages and settings pushed on top.
struct HomeScreen: View {
    @Environment(\.strings) private var strings
    @StateObject private var feedStore = VMStoreOwner<FeedViewModel>(Deps.shared.getFeedViewModel())
    @State private var path: [AppRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            FeedScreen(viewModel: feedStore.instance)
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button {
                            path.append(.settings)
                        } label: {
                            Image(systemName: "gearshape").accessibilityLabel(strings.settingsTitle)
                        }
                    }
                }
                .navigationDestination(for: AppRoute.self) { route in
                    switch route {
                    case let .eventDetail(id): EventDetailScreen(eventId: id)
                    case .settings: SettingsScreen()
                    }
                }
        }
    }
}
