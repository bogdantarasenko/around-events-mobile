import AroundKit
import SwiftUI

struct SettingsScreen: View {
    @Environment(\.strings) private var strings
    @StateObject private var store = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @State private var state: SettingsUiState?

    var body: some View {
        Form {
            if let state {
                Section(strings.settingsAppearance) {
                    Picker(strings.settingsAppearance, selection: themeBinding(state)) {
                        Text(strings.themeSystem).tag(ThemeMode.system)
                        Text(strings.themeLight).tag(ThemeMode.light)
                        Text(strings.themeDark).tag(ThemeMode.dark)
                    }
                    .pickerStyle(.segmented)
                    .labelsHidden()
                }

                Section(strings.settingsLanguage) {
                    Picker(strings.settingsLanguage, selection: languageBinding(state)) {
                        ForEach(languages, id: \.self) { language in
                            Text(LanguageName.of(language, system: strings.languageSystem)).tag(language)
                        }
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()
                }

                Section(strings.settingsAbout) {
                    Text(strings.settingsVersion(state.versionName))
                    Text(strings.settingsOpenSource)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
        }
        .navigationTitle(strings.settingsTitle)
        .task {
            for await value in store.instance.state {
                state = value
            }
        }
    }

    private let languages: [AppLanguage] = [.system, .ukrainian, .russian, .belarusian, .english]

    private func themeBinding(_ state: SettingsUiState) -> Binding<ThemeMode> {
        Binding(
            get: { state.theme },
            set: { store.instance.onThemeSelected(theme: $0) }
        )
    }

    private func languageBinding(_ state: SettingsUiState) -> Binding<AppLanguage> {
        Binding(
            get: { state.language },
            set: { store.instance.onLanguageSelected(language: $0) }
        )
    }
}
