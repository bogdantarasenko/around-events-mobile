import AroundKit
import SwiftUI

/// Discover: the Home bar — logo, city, settings — over the city's organizers instead of its events. Each row
/// opens the organizer's page.
struct DiscoverScreen: View {
    let viewModel: DiscoverViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @State private var state: DiscoverUiState?

    var body: some View {
        let state = state ?? viewModel.state.value
        List {
            if let error = state.error {
                ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { viewModel.refresh() }
                    .listRowSeparator(.hidden)
            }
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 60).listRowSeparator(.hidden)
            } else if state.organizers.isEmpty {
                Text(strings.discoverOrganizersEmpty)
                    .foregroundStyle(Palette.secondary)
                    .padding(.top, 8)
                    .listRowSeparator(.hidden)
            }
            ForEach(Array(state.organizers.enumerated()), id: \.element.slug) { index, organizer in
                ZStack {
                    NavigationLink(value: AppRoute.organizer(slug: organizer.slug)) { EmptyView() }.opacity(0)
                    OrganizerRow(organizer: organizer, showsSeparator: index < state.organizers.count - 1)
                }
                .listRowInsets(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20))
                .listRowSeparator(.hidden)
            }
        }
        .listStyle(.plain)
        .navigationTitle(strings.discoverOrganizers)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) { Color.clear.frame(width: 1, height: 1) }
            ToolbarItem(placement: .topBarLeading) { CityBrandMenu(state: state) { viewModel.selectCity(slug: $0) } }
                .hidingSharedBackground()
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { path.append(.search) } label: { Image(systemName: "magnifyingglass") }
                    .accessibilityLabel(strings.searchPlaceholder)
                Button { path.append(.settings) } label: { Image(systemName: "gearshape") }
                    .accessibilityLabel(strings.settingsTitle)
            }
        }
        .refreshable { viewModel.refresh() }
        .task {
            for await value in viewModel.state {
                self.state = value
            }
        }
    }
}
