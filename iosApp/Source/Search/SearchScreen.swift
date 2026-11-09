import AroundKit
import SwiftUI

/// Search in the city's listing, results by day.
struct SearchScreen: View {
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<SearchViewModel>(Deps.shared.getSearchViewModel())
    @State private var state: SearchUiState?
    @State private var query = ""

    var body: some View {
        let state = state ?? store.instance.state.value
        List {
            if state.isBlank {
                Text(strings.searchHint).foregroundStyle(.secondary).listRowSeparator(.hidden)
            } else if state.days.isEmpty {
                Text(strings.searchEmpty).foregroundStyle(.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
        }
        .listStyle(.plain)
        .navigationTitle(state.city?.name ?? strings.searchPlaceholder)
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: strings.searchPlaceholder)
        .onChange(of: query) { _, text in store.instance.setQuery(text: text) }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }
}
