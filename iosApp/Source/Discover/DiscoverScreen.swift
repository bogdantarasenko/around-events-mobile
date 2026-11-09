import AroundKit
import SwiftUI

/// The home screen: the city's next events, categories, cities and organizers.
struct DiscoverScreen: View {
    let viewModel: DiscoverViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @State private var state: DiscoverUiState?

    var body: some View {
        let state = state ?? viewModel.state.value
        ScrollView {
            VStack(alignment: .leading, spacing: 28) {
                header
                content(state)
            }
            .padding(.bottom, 24)
        }
        .refreshable { viewModel.refresh() }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            for await value in viewModel.state {
                self.state = value
            }
        }
    }

    private var header: some View {
        HStack(spacing: 10) {
            CircleButton(systemImage: "gearshape", label: strings.settingsTitle) { path.append(.settings) }
            Text(strings.tabFeed).font(.title2.weight(.bold))
            Spacer()
            HStack(spacing: 0) {
                iconButton("map", strings.mapTitle) { path.append(.map) }
                iconButton("magnifyingglass", strings.searchPlaceholder) { path.append(.search) }
            }
            .background(.regularMaterial, in: Capsule())
        }
        .padding(.horizontal)
        .padding(.top, 8)
    }

    @ViewBuilder
    private func content(_ state: DiscoverUiState) -> some View {
        if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, minHeight: 300)
        } else if state.upcoming.isEmpty, state.isEmpty || state.error != nil {
            empty(state)
        } else {
            if let error = state.error {
                ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { viewModel.refresh() }
                    .padding(.horizontal)
            }
            UpcomingShelf(state: state, path: $path)
            if !state.categories.isEmpty {
                CategoryGrid(categories: state.categories) { path.append(.events(category: $0)) }
            }
            if state.cities.count > 1 {
                CityCarousel(cities: state.cities, current: state.city?.slug) { viewModel.selectCity(slug: $0) }
            }
            if !state.organizers.isEmpty {
                OrganizerList(organizers: state.organizers) { path.append(.organizer(slug: $0)) }
            }
            SubmitCard()
        }
    }

    private func empty(_ state: DiscoverUiState) -> some View {
        ContentUnavailableView {
            Label(strings.feedEmptyTitle, systemImage: "calendar")
        } description: {
            Text(state.error.map { Labels.error($0, strings) } ?? strings.feedEmptyText)
        } actions: {
            if state.error != nil {
                Button(strings.retry) { viewModel.refresh() }
            }
        }
        .frame(minHeight: 400)
    }

    private func iconButton(_ systemImage: String, _ label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: 16, weight: .semibold))
                .frame(width: 40, height: 36)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}
