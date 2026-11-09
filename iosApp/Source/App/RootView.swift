import AroundKit
import SwiftUI

/// Applies the theme and interface language, and shows sign-in or the cabinet.
struct RootView: View {
    @StateObject private var settingsStore = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @StateObject private var sessionStore = VMStoreOwner<SessionViewModel>(Deps.shared.getSessionViewModel())
    @State private var settings: SettingsUiState?
    @State private var session: SessionUiState?

    var body: some View {
        let session = session ?? sessionStore.instance.state.value
        Group {
            if session.isSignedIn {
                HomeScreen(session: session, sessionViewModel: sessionStore.instance)
                    // A fresh cabinet per account: signing out must not leave the old one underneath.
                    .id(session.displayName)
            } else {
                LoginScreen(state: session, viewModel: sessionStore.instance, languageTag: languageTag)
            }
        }
        .environment(\.strings, Deps.shared.strings(languageTag: languageTag))
        .environment(\.locale, Locale(identifier: languageTag))
        .preferredColorScheme(colorScheme)
        .task {
            for await state in settingsStore.instance.state {
                settings = state
            }
        }
        .task {
            for await state in sessionStore.instance.state {
                self.session = state
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
