import AroundKit
import SwiftUI

/// The city's listing by day, or a category's page as in the designs: the title, "N events", its organizers and
/// the events by day.
struct EventsScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store: VMStoreOwner<EventsViewModel>
    @State private var state: EventsUiState?

    init(category: String?, path: Binding<[AppRoute]>) {
        _path = path
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getEventsViewModel(category: category)))
    }

    var body: some View {
        let state = state ?? store.instance.state.value
        List {
            header(state)
                .listRowInsets(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20))
                .listRowSeparator(.hidden)
            if let error = state.error {
                ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { store.instance.refresh() }
                    .listRowSeparator(.hidden)
            }
            if !state.filters.isEmpty {
                CategoryFilter(categories: state.filters, selected: state.filter) { store.instance.selectFilter(key: $0) }
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
            }
            if state.category != nil, !state.organizers.isEmpty {
                organizers(state)
            }
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).listRowSeparator(.hidden)
            } else if state.days.isEmpty {
                Text(strings.feedCategoryEmpty).foregroundStyle(Palette.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
            Color.clear.frame(height: 80).listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .scrollIndicators(.hidden)
        .safeAreaInset(edge: .top) { TopBar(path: $path) { path.append(.search) }.padding(.bottom, 4) }
        .toolbar(.hidden, for: .navigationBar)
        .refreshable { store.instance.refresh() }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func header(_ state: EventsUiState) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            if let category = state.category {
                Image(systemName: CategoryStyle.symbol(category))
                    .font(.system(size: 26, weight: .medium))
                    .foregroundStyle(CategoryStyle.tint(category))
                    .frame(width: 64, height: 64)
                    .background(CategoryStyle.tint(category).opacity(0.16), in: Circle())
            }
            Text(state.category.map { Labels.category($0, strings) } ?? state.city?.name ?? strings.allEvents)
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(Palette.ink)
            Text(strings.eventsCount("\(state.count)"))
                .font(.system(size: 15))
                .foregroundStyle(Palette.secondary)
        }
        .padding(.vertical, 8)
    }

    @ViewBuilder
    private func organizers(_ state: EventsUiState) -> some View {
        Text(strings.discoverOrganizers)
            .font(.system(size: 20, weight: .semibold))
            .foregroundStyle(Palette.ink)
            .padding(.top, 12)
            .listRowInsets(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20))
            .listRowSeparator(.hidden)
        ForEach(state.organizers.prefix(5), id: \.slug) { organizer in
            Button { path.append(.organizer(slug: organizer.slug)) } label: {
                HStack(spacing: 13) {
                    Avatar(url: organizer.avatarUrl, name: organizer.name, size: 60, cornerRadius: 10)
                    Text(organizer.name).font(.system(size: 17, weight: .medium)).foregroundStyle(Palette.ink)
                    Spacer()
                }
            }
            .buttonStyle(.plain)
            .listRowInsets(EdgeInsets(top: 8, leading: 20, bottom: 8, trailing: 20))
            .listRowSeparator(.hidden)
        }
    }
}
