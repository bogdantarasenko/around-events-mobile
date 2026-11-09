import AroundKit
import SwiftUI

/// Applies the stored theme and interface language to everything below it.
struct RootView: View {
    @StateObject private var store = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @State private var settings: SettingsUiState?

    var body: some View {
        HomeScreen()
            .environment(\.strings, Deps.shared.strings(languageTag: languageTag))
            .preferredColorScheme(colorScheme)
            .task {
                for await state in store.instance.state {
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
