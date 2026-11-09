import AroundKit
import SwiftUI

struct SettingsScreen: View {
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL
    @StateObject private var store = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @State private var state: SettingsUiState?

    var body: some View {
        let state = state ?? store.instance.state.value
        Form {
            if state.cities.count > 1 {
                Section {
                    Picker(strings.feedCity, selection: Binding(
                        get: { state.city ?? state.cities.first?.slug ?? "" },
                        set: { store.instance.onCitySelected(slug: $0) }
                    )) {
                        ForEach(state.cities, id: \.slug) { Text($0.name).tag($0.slug) }
                    }
                }
            }

            Section(strings.settingsAppearance) {
                Picker(strings.settingsAppearance, selection: Binding(get: { state.theme }, set: { store.instance.onThemeSelected(theme: $0) })) {
                    Text(strings.themeSystem).tag(ThemeMode.system)
                    Text(strings.themeLight).tag(ThemeMode.light)
                    Text(strings.themeDark).tag(ThemeMode.dark)
                }
                .pickerStyle(.segmented)
                .labelsHidden()
            }

            Section(strings.settingsLanguage) {
                Picker(strings.settingsLanguage, selection: Binding(
                    get: { state.language },
                    set: { store.instance.onLanguageSelected(language: $0) }
                )) {
                    ForEach(languages, id: \.self) { language in
                        Text(LanguageName.of(language, system: strings.languageSystem)).tag(language)
                    }
                }
                .pickerStyle(.inline)
                .labelsHidden()
            }

            Section(strings.settingsAbout) {
                Button(strings.feedSubmit) { openURL(Site.submit) }
                Button(strings.settingsRules) { openURL(Site.rules) }
                Text(strings.settingsVersion(state.versionName))
                Text(strings.feedSubmitHint).font(.footnote).foregroundStyle(.secondary)
                Text(strings.settingsOpenSource).font(.footnote).foregroundStyle(.secondary)
            }
        }
        .navigationTitle(strings.settingsTitle)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private let languages: [AppLanguage] = [.system, .ukrainian, .russian, .belarusian, .english]
}
