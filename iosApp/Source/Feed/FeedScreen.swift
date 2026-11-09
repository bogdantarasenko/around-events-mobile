import AroundKit
import SwiftUI

/// The city's listing by day, with the city switch in the title and a category filter on top.
struct FeedScreen: View {
    let viewModel: FeedViewModel
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL
    @Environment(\.locale) private var locale
    @State private var state: FeedUiState?

    var body: some View {
        let state = state ?? viewModel.state.value
        content(state)
            .navigationTitle(state.city?.name ?? strings.tabFeed)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) { cityMenu(state) }
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        openURL(Site.submit)
                    } label: {
                        Image(systemName: "plus").accessibilityLabel(strings.feedSubmit)
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
    private func content(_ state: FeedUiState) -> some View {
        if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if state.days.isEmpty, state.isEmpty || state.error != nil, state.category == nil {
            empty(state)
        } else {
            List {
                if let error = state.error {
                    ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { viewModel.refresh() }
                        .listRowSeparator(.hidden)
                }
                if !state.categories.isEmpty {
                    CategoryFilter(categories: state.categories, selected: state.category) {
                        viewModel.selectCategory(key: $0)
                    }
                    .listRowInsets(EdgeInsets())
                    .listRowSeparator(.hidden)
                }
                if state.days.isEmpty {
                    Text(strings.feedCategoryEmpty).foregroundStyle(.secondary).listRowSeparator(.hidden)
                }
                ForEach(state.days, id: \.self) { day in
                    Section(Dates.heading(day, strings: strings, locale: locale)) {
                        ForEach(day.events, id: \.id) { event in
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

    private func empty(_ state: FeedUiState) -> some View {
        ContentUnavailableView {
            Label(strings.feedEmptyTitle, systemImage: "calendar")
        } description: {
            Text(state.error.map { Labels.error($0, strings) } ?? strings.feedEmptyText)
        } actions: {
            if state.error != nil {
                Button(strings.retry) { viewModel.refresh() }
            }
        }
    }

    @ViewBuilder
    private func cityMenu(_ state: FeedUiState) -> some View {
        let title = Text(state.city?.name ?? strings.tabFeed).font(.headline)
        if state.cities.count > 1 {
            Menu {
                Picker(strings.feedCity, selection: Binding(
                    get: { state.city?.slug ?? "" },
                    set: { viewModel.selectCity(slug: $0) }
                )) {
                    ForEach(state.cities, id: \.slug) { Text($0.name).tag($0.slug) }
                }
            } label: {
                HStack(spacing: 4) {
                    title
                    Image(systemName: "chevron.down").font(.caption.weight(.semibold))
                }
                .foregroundStyle(.primary)
            }
            .accessibilityLabel(strings.feedCity)
        } else {
            title
        }
    }
}
