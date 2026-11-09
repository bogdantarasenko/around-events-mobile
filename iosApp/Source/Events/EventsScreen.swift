import AroundKit
import SwiftUI

/// The city's listing by day, or a category's page: its title, count, organizers and events.
struct EventsScreen: View {
    @Environment(\.strings) private var strings
    @StateObject private var store: VMStoreOwner<EventsViewModel>
    @State private var state: EventsUiState?

    init(category: String?) {
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getEventsViewModel(category: category)))
    }

    var body: some View {
        let state = state ?? store.instance.state.value
        List {
            header(state).listRowSeparator(.hidden)
            if let error = state.error {
                ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { store.instance.refresh() }
                    .listRowSeparator(.hidden)
            }
            if !state.filters.isEmpty {
                CategoryFilter(categories: state.filters, selected: state.filter) { store.instance.selectFilter(key: $0) }
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
            }
            if !state.organizers.isEmpty, state.category != nil {
                Section {
                    ForEach(state.organizers.prefix(5), id: \.slug) { organizer in
                        NavigationLink(value: AppRoute.organizer(slug: organizer.slug)) {
                            HStack(spacing: 12) {
                                Avatar(url: organizer.avatarUrl, name: organizer.name, size: 40, cornerRadius: 8)
                                Text(organizer.name).font(.body.weight(.medium))
                            }
                        }
                        .listRowSeparator(.hidden)
                    }
                } header: {
                    Text(strings.discoverOrganizers).font(.title3.weight(.semibold)).foregroundStyle(.primary).textCase(nil)
                }
            }
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).listRowSeparator(.hidden)
            } else if state.days.isEmpty {
                Text(strings.feedCategoryEmpty).foregroundStyle(.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
        }
        .listStyle(.plain)
        .navigationBarTitleDisplayMode(.inline)
        .refreshable { store.instance.refresh() }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func header(_ state: EventsUiState) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            if let category = state.category {
                Text(CategoryStyle.emoji(category))
                    .font(.system(size: 30))
                    .frame(width: 64, height: 64)
                    .background(CategoryStyle.tint(category).opacity(0.18), in: Circle())
            }
            Text(state.category.map { Labels.category($0, strings) } ?? state.city?.name ?? strings.allEvents)
                .font(.largeTitle.weight(.bold))
            Text(strings.eventsCount("\(state.count)")).font(.subheadline).foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}
