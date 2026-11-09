import SwiftUI

struct HomeScreen: View {
    @Environment(\.strings) private var strings

    var body: some View {
        TabView {
            NavigationStack {
                ContentUnavailableView(strings.placeholderEmpty, systemImage: "calendar")
                    .navigationTitle(strings.tabEvents)
            }
            .tabItem { Label(strings.tabEvents, systemImage: "calendar") }

            NavigationStack {
                ContentUnavailableView(strings.placeholderEmpty, systemImage: "person")
                    .navigationTitle(strings.tabProfile)
                    .toolbar {
                        NavigationLink {
                            SettingsScreen()
                        } label: {
                            Image(systemName: "gearshape")
                                .accessibilityLabel(strings.settingsTitle)
                        }
                    }
            }
            .tabItem { Label(strings.tabProfile, systemImage: "person") }
        }
    }
}
