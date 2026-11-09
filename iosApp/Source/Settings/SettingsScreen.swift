import AroundKit
import SwiftUI

struct SettingsScreen: View {
    let onSignOut: () -> Void
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL
    @StateObject private var store = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @State private var state: SettingsUiState?
    @State private var organization: String?

    var body: some View {
        let state = state ?? store.instance.state.value
        Form {
            Section(strings.settingsNewSubmission) {
                // Local copy so typing isn't fought by the trimmed value the setting stores.
                TextField(
                    strings.formOrganization,
                    text: Binding(
                        get: { organization ?? state.defaultOrganization },
                        set: {
                            organization = $0
                            store.instance.onDefaultOrganizationChanged(organization: $0)
                        }
                    )
                )
                if state.cities.count > 1 {
                    Picker(strings.formCity, selection: Binding(
                        get: { state.defaultCity ?? state.cities.first?.slug ?? "" },
                        set: { store.instance.onDefaultCitySelected(slug: $0) }
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
                Button(strings.settingsRules) {
                    if let url = URL(string: state.siteUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/rules") {
                        openURL(url)
                    }
                }
                Text(strings.settingsVersion(state.versionName))
                Text(strings.settingsOpenSource).font(.footnote).foregroundStyle(.secondary)
            }

            Section {
                Button(strings.signOut, role: .destructive, action: onSignOut)
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
