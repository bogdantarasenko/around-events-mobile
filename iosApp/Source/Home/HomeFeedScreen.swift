import AroundKit
import SwiftUI

/// The Home tab: the city's events by day, as "Picked for you" in the designs. The large title is the city and
/// switches it from the title menu; "+" opens the mini-app to submit, the gear opens settings.
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
        }
        .listStyle(.plain)
        // The city next to the logo, as the brand next to the avatar in the designs; the title stays for VoiceOver
        // and the back menu, out of sight.
        .navigationTitle(cities.city?.name ?? strings.tabHome)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) { Color.clear.frame(width: 1, height: 1) }
            ToolbarItem(placement: .topBarLeading) { brand(cities) }
                .hidingSharedBackground()
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { openURL(Site.submit) } label: { Image(systemName: "plus") }
                    .accessibilityLabel(strings.feedSubmit)
                Button { path.append(.settings) } label: { Image(systemName: "gearshape") }
                    .accessibilityLabel(strings.settingsTitle)
            }
        }
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

    private func brand(_ cities: DiscoverUiState) -> some View {
        Menu {
            Picker(strings.feedCity, selection: Binding(
                get: { cities.city?.slug ?? "" },
                set: { discover.selectCity(slug: $0) }
            )) {
                ForEach(cities.cities, id: \.slug) { Text($0.name).tag($0.slug) }
            }
        } label: {
            HStack(spacing: 8) {
                Image("Logo").resizable().scaledToFit().frame(width: 36, height: 36).accessibilityHidden(true)
                Text(cities.city?.name ?? strings.tabHome)
                    .font(.title3.weight(.bold))
                    .foregroundStyle(Palette.ink)
                if cities.cities.count > 1 {
                    Image(systemName: "chevron.down")
                        .font(.footnote.weight(.semibold))
                        .foregroundStyle(Palette.secondary)
                }
            }
        }
        .disabled(cities.cities.count < 2)
        .accessibilityLabel(strings.feedCity)
        .accessibilityValue(cities.city?.name ?? "")
    }
}
