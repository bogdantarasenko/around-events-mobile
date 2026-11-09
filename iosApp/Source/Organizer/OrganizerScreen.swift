import AroundKit
import SwiftUI

/// An organizer's page as in the designs: cover, avatar card, Follow, bio, links, chips, events by day.
struct OrganizerScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store: VMStoreOwner<OrganizerViewModel>
    @State private var state: OrganizerUiState?

    init(slug: String, path: Binding<[AppRoute]>) {
        _path = path
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
        .overlay(alignment: .top) { TopBar(path: $path) }
        .toolbar(.hidden, for: .navigationBar)
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
                    .padding(.top, 6)
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
            }
            if state.days.isEmpty {
                Text(strings.organizerEmpty).foregroundStyle(Palette.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
            Color.clear.frame(height: 80).listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .scrollIndicators(.hidden)
        .ignoresSafeArea(edges: .top)
        .refreshable { store.instance.refresh() }
    }
}
