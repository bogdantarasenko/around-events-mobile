import AroundKit
import SwiftUI

/// Discover as in the designs: the city's upcoming events, categories, cities and organizers.
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
                header.padding(.bottom, 12)
                content(state)
            }
            .padding(.bottom, 100)
        }
        .scrollIndicators(.hidden)
        .refreshable { viewModel.refresh() }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            for await value in viewModel.state {
                self.state = value
            }
        }
    }

    private var header: some View {
        HStack(spacing: 9) {
            Button { path.append(.settings) } label: {
                Image(systemName: "person.fill")
                    .font(.system(size: 20))
                    .foregroundStyle(Palette.secondary)
                    .frame(width: 44, height: 44)
                    .background(Palette.tabSelected, in: Circle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(strings.settingsTitle)
            Text(strings.tabDiscover).font(.system(size: 22, weight: .bold)).tracking(-0.26).foregroundStyle(Palette.ink)
            Spacer()
            HeaderActions(
                first: ("map", strings.mapTitle, onOpenMap),
                second: ("magnifyingglass", strings.searchPlaceholder, { path.append(.search) })
            )
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
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
                CityCarousel(cities: state.cities, current: state.city?.slug) { viewModel.selectCity(slug: $0) }
                    .padding(.top, 33)
            }
            if !state.organizers.isEmpty {
                OrganizerList(organizers: state.organizers) { path.append(.organizer(slug: $0)) }
                    .padding(.top, 32)
            }
        }
    }
}
