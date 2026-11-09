import AroundKit
import SwiftUI

/// Discover: the way around the city — its categories, the other cities and the organizers. The listing itself
/// lives on the Home tab, so nothing here repeats it.
struct DiscoverScreen: View {
    let viewModel: DiscoverViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @State private var state: DiscoverUiState?

    var body: some View {
        let state = state ?? viewModel.state.value
        ScrollView {
            VStack(alignment: .leading, spacing: 32) {
                content(state)
            }
            .padding(.top, 4)
            .padding(.bottom, 24)
        }
        .scrollIndicators(.hidden)
        .refreshable { viewModel.refresh() }
        .navigationTitle(strings.tabDiscover)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
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
        } else if state.categories.isEmpty, state.isEmpty || state.error != nil {
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
            CategoryTiles(categories: state.categories) { path.append(.events(category: $0)) }
            if state.cities.count > 1 {
                CityCarousel(cities: state.cities, current: state.city?.slug, onOpenAll: { path.append(.cities) }) {
                    path.append(.city(slug: $0))
                }
            }
            if !state.organizers.isEmpty {
                OrganizerList(organizers: state.organizers) { path.append(.organizer(slug: $0)) }
            }
        }
    }
}
