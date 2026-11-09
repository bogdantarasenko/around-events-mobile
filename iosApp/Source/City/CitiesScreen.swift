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
        List {
            Section {
                ForEach(Array(state.cities.enumerated()), id: \.element.slug) { index, city in
                    NavigationLink(value: AppRoute.city(slug: city.slug)) {
                        HStack(spacing: 12) {
                            Image(systemName: "building.columns.fill")
                                .font(.footnote.weight(.semibold))
                                .foregroundStyle(.white)
                                .frame(width: 32, height: 32)
                                .background(CityStyle.color(index), in: Circle())
                            Text(city.name).foregroundStyle(Palette.ink)
                            Spacer()
                            Text(strings.eventsCount("\(city.upcomingCount)")).foregroundStyle(Palette.tertiary)
                        }
                    }
                }
            } header: {
                Text(strings.citiesIntro).textCase(nil)
            }
        }
        .listStyle(.plain)
        .navigationTitle(strings.discoverCities)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            for await value in discover.state {
                self.state = value
            }
        }
    }
}
