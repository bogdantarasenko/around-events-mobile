import AroundKit
import SwiftUI

/// Two sections — my events and profile — and the "+" for a new submission.
struct HomeScreen: View {
    let session: SessionUiState
    let sessionViewModel: SessionViewModel
    @Environment(\.strings) private var strings
    @StateObject private var eventsStore = VMStoreOwner<MyEventsViewModel>(Deps.shared.getMyEventsViewModel())
    @StateObject private var organizerStore = VMStoreOwner<OrganizerViewModel>(Deps.shared.getOrganizerViewModel())
    @State private var eventsPath: [AppRoute] = []
    @State private var profilePath: [AppRoute] = []

    var body: some View {
        TabView {
            NavigationStack(path: $eventsPath) {
                MyEventsScreen(viewModel: eventsStore.instance, path: $eventsPath)
                    .navigationTitle(strings.tabEvents)
                    .toolbar { settingsButton(path: $eventsPath) }
                    .navigationDestination(for: AppRoute.self) { destination(for: $0, path: $eventsPath) }
            }
            .tabItem { Label(strings.tabEvents, systemImage: "calendar") }

            NavigationStack(path: $profilePath) {
                ProfileScreen(session: session, viewModel: organizerStore.instance, path: $profilePath)
                    .navigationTitle(strings.tabProfile)
                    .toolbar { settingsButton(path: $profilePath) }
                    .navigationDestination(for: AppRoute.self) { destination(for: $0, path: $profilePath) }
            }
            .tabItem { Label(strings.tabProfile, systemImage: "person") }
        }
    }

    private func settingsButton(path: Binding<[AppRoute]>) -> some ToolbarContent {
        ToolbarItem(placement: .topBarTrailing) {
            Button {
                path.wrappedValue.append(.settings)
            } label: {
                Image(systemName: "gearshape").accessibilityLabel(strings.settingsTitle)
            }
        }
    }

    @ViewBuilder
    private func destination(for route: AppRoute, path: Binding<[AppRoute]>) -> some View {
        switch route {
        case let .eventDetail(id):
            EventDetailScreen(eventId: id, path: path)
        case let .form(kind, id):
            SubmissionFormScreen(kind: kind, eventId: id) {
                if !path.wrappedValue.isEmpty { path.wrappedValue.removeLast() }
            }
        case .settings:
            SettingsScreen(onSignOut: sessionViewModel.signOut)
        case .organizerEdit:
            OrganizerEditScreen(viewModel: organizerStore.instance)
        }
    }
}
