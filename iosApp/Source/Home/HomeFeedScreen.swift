import AroundKit
import SwiftUI

/// The Home tab: the city's events by day, as "Picked for you" in the designs. The city is switched from the
/// title; "+" opens the mini-app to submit, the gear opens settings.
struct HomeFeedScreen: View {
    let discover: DiscoverViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL
    @StateObject private var store = VMStoreOwner<EventsViewModel>(Deps.shared.getEventsViewModel(category: nil, citySlug: nil))
    @State private var state: EventsUiState?
    @State private var cities: DiscoverUiState?

    var body: some View {
        let state = state ?? store.instance.state.value
        let cities = cities ?? discover.state.value
        List {
            header(cities)
                .listRowInsets(EdgeInsets(top: 8, leading: 20, bottom: 4, trailing: 20))
                .listRowSeparator(.hidden)
            if let error = state.error ?? cities.error {
                ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { discover.refresh() }
                    .listRowSeparator(.hidden)
            }
            if !state.filters.isEmpty {
                CategoryFilter(categories: state.filters, selected: state.filter) { store.instance.selectFilter(key: $0) }
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
            }
            if state.isLoading || cities.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 60).listRowSeparator(.hidden)
            } else if state.days.isEmpty {
                Text(strings.feedEmptyText).foregroundStyle(Palette.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
            Color.clear.frame(height: 80).listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .scrollIndicators(.hidden)
        .toolbar(.hidden, for: .navigationBar)
        .refreshable { discover.refresh() }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
        .task {
            for await value in discover.state {
                self.cities = value
            }
        }
    }

    private func header(_ cities: DiscoverUiState) -> some View {
        HStack(spacing: 10) {
            Image("Logo")
                .resizable()
                .scaledToFit()
                .frame(width: 44, height: 44)
                .accessibilityHidden(true)
            Menu {
                Picker(strings.feedCity, selection: Binding(
                    get: { cities.city?.slug ?? "" },
                    set: { discover.selectCity(slug: $0) }
                )) {
                    ForEach(cities.cities, id: \.slug) { Text($0.name).tag($0.slug) }
                }
            } label: {
                HStack(spacing: 4) {
                    Text(cities.city?.name ?? strings.tabHome).font(.system(size: 22, weight: .bold))
                    Image(systemName: "chevron.down").font(.system(size: 13, weight: .semibold)).foregroundStyle(Palette.chevron)
                }
                .foregroundStyle(Palette.ink)
            }
            Spacer()
            HeaderActions(
                first: ("plus", strings.feedSubmit, { openURL(Site.submit) }),
                second: ("gearshape", strings.settingsTitle, { path.append(.settings) })
            )
        }
    }
}
