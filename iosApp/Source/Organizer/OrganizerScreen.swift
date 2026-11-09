import AroundKit
import SwiftUI

/// An organizer's page: cover, who they are, links, "Follow" in the bot, and their events by day.
struct OrganizerScreen: View {
    @Environment(\.strings) private var strings
    @StateObject private var store: VMStoreOwner<OrganizerViewModel>
    @State private var state: OrganizerUiState?

    init(slug: String) {
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getOrganizerViewModel(slug: slug)))
    }

    var body: some View {
        let state = state ?? store.instance.state.value
        Group {
            if let organizer = state.organizer {
                page(organizer, state)
            } else if state.isLoading {
                ProgressView()
            } else {
                ContentUnavailableView {
                    Label(strings.apiNotFound, systemImage: "person.crop.circle.badge.questionmark")
                } description: {
                    Text(state.error.map { Labels.error($0, strings) } ?? "")
                } actions: {
                    Button(strings.retry) { store.instance.refresh() }
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func page(_ organizer: Organizer, _ state: OrganizerUiState) -> some View {
        List {
            OrganizerHeader(organizer: organizer)
                .listRowInsets(EdgeInsets())
                .listRowSeparator(.hidden)
            if !state.filters.isEmpty {
                CategoryFilter(categories: state.filters, selected: state.filter) { store.instance.selectFilter(key: $0) }
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
            }
            if state.days.isEmpty {
                Text(strings.organizerEmpty).foregroundStyle(.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
        }
        .listStyle(.plain)
        .refreshable { store.instance.refresh() }
    }
}
