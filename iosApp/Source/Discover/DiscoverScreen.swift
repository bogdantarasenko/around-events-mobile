import AroundKit
import SwiftUI

/// Discover as in the designs: the city's upcoming events, categories, cities and organizers, under a large title
/// with the map and search in the toolbar.
struct DiscoverScreen: View {
    let viewModel: DiscoverViewModel
    @Binding var path: [AppRoute]
    let onOpenMap: () -> Void
    @Environment(\.strings) private var strings
    @State private var state: DiscoverUiState?

    var body: some View {
        let state = state ?? viewModel.state.value
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                content(state)
            }
            .padding(.bottom, 24)
        }
        .scrollIndicators(.hidden)
        .refreshable { viewModel.refresh() }
        .navigationTitle(strings.tabDiscover)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button { path.append(.settings) } label: { Image(systemName: "person.crop.circle") }
                    .accessibilityLabel(strings.settingsTitle)
            }
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button(action: onOpenMap) { Image(systemName: "map") }
                    .accessibilityLabel(strings.mapTitle)
                Button { path.append(.search) } label: { Image(systemName: "magnifyingglass") }
                    .accessibilityLabel(strings.searchPlaceholder)
            }
        }
        .task {
            for await value in viewModel.state {
                self.state = value
            }
        }
    }

    @ViewBuilder
    private func content(_ state: DiscoverUiState) -> some View {
        if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, minHeight: 300)
        } else if state.upcoming.isEmpty, state.isEmpty || state.error != nil {
            ContentUnavailableView {
                Label(strings.feedEmptyTitle, systemImage: "calendar")
            } description: {
                Text(state.error.map { Labels.error($0, strings) } ?? strings.feedEmptyText)
            } actions: {
                if state.error != nil { Button(strings.retry) { viewModel.refresh() } }
            }
            .frame(minHeight: 400)
        } else {
            if let error = state.error {
                ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { viewModel.refresh() }
                    .padding(.horizontal, 20)
            }
            UpcomingShelf(state: state, path: $path)
            if !state.categories.isEmpty {
                CategoryGrid(categories: state.categories) { path.append(.events(category: $0)) }
                    .padding(.top, 21)
            }
            if state.cities.count > 1 {
                CityCarousel(cities: state.cities, current: state.city?.slug, onOpenAll: { path.append(.cities) }) {
                    path.append(.city(slug: $0))
                }
                .padding(.top, 33)
            }
            if !state.organizers.isEmpty {
                OrganizerList(organizers: state.organizers) { path.append(.organizer(slug: $0)) }
                    .padding(.top, 32)
            }
        }
    }
}
