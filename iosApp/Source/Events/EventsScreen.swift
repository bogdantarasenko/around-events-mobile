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
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getEventsViewModel(category: category, citySlug: nil)))
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
            if state.category != nil {
                VStack(alignment: .leading, spacing: 0) {
                    Rectangle().fill(Palette.chipBorder).frame(height: 0.5).padding(.horizontal, -20)
                    Text(strings.discoverUpcoming)
                        .font(.title3.weight(.semibold))
                        .foregroundStyle(Palette.ink)
                        .padding(.top, 18)
                }
                .listRowInsets(EdgeInsets(top: 8, leading: 20, bottom: 0, trailing: 20))
                .listRowSeparator(.hidden)
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
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { path.append(.search) } label: { Image(systemName: "magnifyingglass") }
                    .accessibilityLabel(strings.searchPlaceholder)
            }
        }
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
                    .font(.system(size: 22, weight: .medium))
                    .foregroundStyle(CategoryStyle.tint(category))
                    .frame(width: 52, height: 52)
                    .background(CategoryStyle.tint(category).opacity(0.16), in: Circle())
            }
            Text(state.category.map { Labels.category($0, strings) } ?? state.city?.name ?? strings.allEvents)
                .font(.largeTitle.weight(.bold))
                .foregroundStyle(Palette.ink)
                .padding(.top, 2)
            countLine(state)
        }
        .padding(.top, 4)
        .padding(.bottom, 8)
    }

    // "3K Events" in the designs: the number in ink, the word in grey.
    private func countLine(_ state: EventsUiState) -> some View {
        let label = strings.eventsCount("\(state.count)")
        let number = "\(state.count)"
        let word = label.replacingOccurrences(of: number, with: "").trimmingCharacters(in: CharacterSet(charactersIn: ": ")).lowercased()
        return (Text(number).foregroundStyle(Palette.ink).fontWeight(.medium) + Text(" " + word).foregroundStyle(Palette.secondary))
            .font(.subheadline)
    }

    @ViewBuilder
    private func organizers(_ state: EventsUiState) -> some View {
        Rectangle().fill(Palette.chipBorder).frame(height: 0.5)
            .listRowInsets(EdgeInsets(top: 8, leading: 0, bottom: 0, trailing: 0))
            .listRowSeparator(.hidden)
        Text(strings.discoverOrganizers)
            .font(.title3.weight(.semibold))
            .foregroundStyle(Palette.ink)
            .padding(.top, 12)
            .listRowInsets(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20))
            .listRowSeparator(.hidden)
        ForEach(state.organizers.prefix(5), id: \.slug) { organizer in
            Button { path.append(.organizer(slug: organizer.slug)) } label: {
                HStack(spacing: 13) {
                    Avatar(url: organizer.avatarUrl, name: organizer.name, size: 60, cornerRadius: 10)
                    Text(organizer.name).font(.body.weight(.medium)).foregroundStyle(Palette.ink)
                    Spacer()
                }
            }
            .buttonStyle(.pressable)
            .listRowInsets(EdgeInsets(top: 8, leading: 20, bottom: 8, trailing: 20))
            .listRowSeparator(.hidden)
        }
    }
}
