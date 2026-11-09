import SwiftUI

/// The pushed screens' top row in the designs: a glass back circle on the left, a two-icon pill on the right.
struct TopBar: View {
    @Binding var path: [AppRoute]
    var onSearch: (() -> Void)?
    @Environment(\.strings) private var strings

    var body: some View {
        HStack {
            GlassCircle(systemImage: "chevron.left", label: strings.back) {
                if !path.isEmpty { path.removeLast() }
            }
            Spacer()
            if let onSearch {
                GlassCircle(systemImage: "magnifyingglass", label: strings.searchPlaceholder, action: onSearch)
            }
        }
        .padding(.horizontal, 20)
    }
}
