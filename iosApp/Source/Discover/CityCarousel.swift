import AroundKit
import SwiftUI

/// "Cities": square cards to switch the city the app reads.
struct CityCarousel: View {
    let cities: [City]
    let current: String?
    let onSelect: (String) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionTitle(title: strings.discoverCities)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    ForEach(Array(cities.enumerated()), id: \.element.slug) { index, city in
                        Button {
                            onSelect(city.slug)
                        } label: {
                            card(city, index: index)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal)
            }
        }
    }

    // No city photos on the backend: a gradient per city stands in for them.
    private func card(_ city: City, index: Int) -> some View {
        let colors: [[Color]] = [[.indigo, .purple], [.teal, .blue], [.orange, .pink], [.green, .teal]]
        return ZStack(alignment: .bottomLeading) {
            LinearGradient(colors: colors[index % colors.count], startPoint: .topLeading, endPoint: .bottomTrailing)
            VStack(alignment: .leading, spacing: 2) {
                Text(city.name).font(.headline)
                if city.upcomingCount > 0 {
                    Text(strings.eventsCount("\(city.upcomingCount)")).font(.caption)
                }
            }
            .foregroundStyle(.white)
            .padding(12)
            Image(systemName: city.slug == current ? "checkmark.circle.fill" : "building.2")
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 30, height: 30)
                .background(.white.opacity(0.25), in: Circle())
                .padding(10)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        }
        .frame(width: 150, height: 150)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
