import AroundKit
import SwiftUI

/// Applies the theme and interface language around the feed.
struct RootView: View {
    @StateObject private var settingsStore = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @State private var settings: SettingsUiState?

    var body: some View {
        HomeScreen()
            .environment(\.strings, Deps.shared.strings(languageTag: languageTag))
            .environment(\.locale, Locale(identifier: languageTag))
            .preferredColorScheme(colorScheme)
            .task {
                for await state in settingsStore.instance.state {
                    settings = state
                }
            }
    }

    private var languageTag: String {
        settings?.language.tag ?? Locale.preferredLanguages.first ?? "en"
    }

    private var colorScheme: ColorScheme? {
        switch settings?.theme {
        case .light: .light
        case .dark: .dark
        default: nil
        }
    }
}
