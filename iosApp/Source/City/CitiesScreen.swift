import AroundKit
import SwiftUI

/// "Cities" from the designs: a short intro, then every city with its number of events, each opening its page.
struct CitiesScreen: View {
    let discover: DiscoverViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @State private var state: DiscoverUiState?

    var body: some View {
        let state = state ?? discover.state.value
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text(strings.citiesIntro)
                    .font(.system(size: 15))
                    .foregroundStyle(Palette.secondary)
                    .padding(.bottom, 14)
                ForEach(Array(state.cities.enumerated()), id: \.element.slug) { index, city in
                    Button { path.append(.city(slug: city.slug)) } label: {
                        HStack(spacing: 12) {
                            Image(systemName: "building.columns.fill")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(.white)
                                .frame(width: 32, height: 32)
                                .background(CityStyle.color(index), in: Circle())
                            Text(city.name).font(.system(size: 17)).foregroundStyle(Palette.ink)
                            Spacer()
                            Text(strings.eventsCount("\(city.upcomingCount)"))
                                .font(.system(size: 17))
                                .foregroundStyle(Palette.tertiary)
                        }
                        .padding(.vertical, 8)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 100)
        }
        .safeAreaInset(edge: .top) {
            ZStack {
                Text(strings.discoverCities).font(.system(size: 17, weight: .semibold)).foregroundStyle(Palette.ink)
                TopBar(path: $path)
            }
            .padding(.bottom, 8)
            .background(Palette.surface)
        }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            for await value in discover.state {
                self.state = value
            }
        }
    }
}
