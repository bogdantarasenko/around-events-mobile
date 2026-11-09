import AroundKit
import SwiftUI

/// The logo with the city next to it, as the brand next to the avatar in the designs; a menu switches the city.
/// Both tabs with a city's content wear it in the leading toolbar slot.
struct CityBrandMenu: View {
    let state: DiscoverUiState
    let onSelect: (String) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        Menu {
            Picker(strings.feedCity, selection: Binding(
                get: { state.city?.slug ?? "" },
                set: { onSelect($0) }
            )) {
                ForEach(state.cities, id: \.slug) { Text($0.name).tag($0.slug) }
            }
        } label: {
            HStack(spacing: 8) {
                Image("Logo").resizable().scaledToFit().frame(width: 36, height: 36).accessibilityHidden(true)
                Text(state.city?.name ?? strings.tabHome)
                    .font(.title3.weight(.bold))
                    .foregroundStyle(Palette.ink)
                if state.cities.count > 1 {
                    Image(systemName: "chevron.down")
                        .font(.footnote.weight(.semibold))
                        .foregroundStyle(Palette.secondary)
                }
            }
        }
        .disabled(state.cities.count < 2)
        .accessibilityLabel(strings.feedCity)
        .accessibilityValue(state.city?.name ?? "")
    }
}
