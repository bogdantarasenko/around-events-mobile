import AroundKit
import SwiftUI

struct MyEventsScreen: View {
    let viewModel: MyEventsViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @State private var state: MyEventsUiState?

    var body: some View {
        let state = state ?? viewModel.state.value
        content(state)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        path.append(.form(.theNew, 0))
                    } label: {
                        Image(systemName: "plus").accessibilityLabel(strings.mineAddEvent)
                    }
                }
            }
            .refreshable { viewModel.refresh() }
            .task {
                for await value in viewModel.state {
                    self.state = value
                }
            }
    }

    @ViewBuilder
    private func content(_ state: MyEventsUiState) -> some View {
        if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if state.isEmpty {
            ContentUnavailableView {
                Label(strings.mineEmptyTitle, systemImage: "calendar.badge.plus")
            } description: {
                Text(strings.mineEmptyText)
            }
        } else {
            List {
                if let error = state.error {
                    ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) {
                        viewModel.refresh()
                    }
                    .listRowSeparator(.hidden)
                }
                StatusFilter(selected: state.filter) { viewModel.setFilter(status: $0) }
                    .listRowSeparator(.hidden)
                ForEach(state.groups, id: \.kind) { group in
                    Section(Labels.group(group.kind, strings)) {
                        ForEach(group.events, id: \.id) { event in
                            NavigationLink(value: AppRoute.eventDetail(event.id)) {
                                EventRow(event: event)
                            }
                        }
                    }
                }
            }
            .listStyle(.plain)
        }
    }
}
