import AroundKit
import SwiftUI

/// Search as in the designs: a glass back circle and a white search pill on top, results as event rows below.
struct SearchScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<SearchViewModel>(Deps.shared.getSearchViewModel())
    @State private var state: SearchUiState?
    @State private var query = ""
    @FocusState private var focused: Bool

    var body: some View {
        let state = state ?? store.instance.state.value
        List {
            if state.isBlank {
                Text(strings.searchHint).foregroundStyle(Palette.secondary).listRowSeparator(.hidden)
            } else if state.days.isEmpty {
                Text(strings.searchEmpty).foregroundStyle(Palette.secondary).listRowSeparator(.hidden)
            }
            EventDays(days: state.days)
            Color.clear.frame(height: 80).listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .scrollDismissesKeyboard(.immediately)
        .safeAreaInset(edge: .top) { header.padding(.bottom, 6).background(Palette.surface) }
        .toolbar(.hidden, for: .navigationBar)
        .onChange(of: query) { _, text in store.instance.setQuery(text: text) }
        .onAppear { focused = true }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private var header: some View {
        HStack(spacing: 8) {
            GlassCircle(systemImage: "chevron.left", label: strings.back) {
                if !path.isEmpty { path.removeLast() }
            }
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass").foregroundStyle(Palette.secondary)
                TextField(strings.searchPlaceholder, text: $query)
                    .focused($focused)
                    .submitLabel(.search)
                    .autocorrectionDisabled()
                if !query.isEmpty {
                    Button { query = "" } label: {
                        Image(systemName: "xmark.circle.fill").foregroundStyle(Palette.chevron)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(strings.close)
                }
            }
            .font(.system(size: 17))
            .padding(.horizontal, 14)
            .frame(height: 44)
            .background(Palette.surface, in: Capsule())
            .shadow(color: .black.opacity(0.08), radius: 4, y: 2)
        }
        .padding(.horizontal, 20)
    }
}
