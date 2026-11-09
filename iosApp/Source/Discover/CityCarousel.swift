import AroundKit
import SwiftUI

/// "Cities ›": 174pt square cards, each opening the city's page with a shade at the bottom, an icon in a translucent circle and the name.
struct CityCarousel: View {
    let cities: [City]
    let current: String?
    let onOpenAll: () -> Void
    let onSelect: (String) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            SectionTitle(title: strings.discoverCities, action: onOpenAll)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 9) {
                    ForEach(Array(cities.enumerated()), id: \.element.slug) { index, city in
                        Button {
                            onSelect(city.slug)
                        } label: {
                            card(city, index: index)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
            }
        }
    }

    // No city photos on the backend: a gradient per city stands in for them, under the same shade.
    private func card(_ city: City, index: Int) -> some View {
        ZStack(alignment: .topLeading) {
            LinearGradient(colors: CityStyle.colors(index), startPoint: .topLeading, endPoint: .bottomTrailing)
            LinearGradient(colors: [.clear, .black.opacity(0.45)], startPoint: .center, endPoint: .bottom)
            Image(systemName: city.slug == current ? "checkmark" : "building.columns")
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 40, height: 40)
                .background(.white.opacity(0.3), in: Circle())
                .padding(.leading, 12)
                .padding(.top, 11)
            Text(city.name)
                .font(.system(size: 19, weight: .medium))
                .tracking(-0.44)
                .foregroundStyle(.white)
                .padding(12)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
        }
        .frame(width: 174, height: 174)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
