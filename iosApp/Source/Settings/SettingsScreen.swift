import AroundKit
import SwiftUI

/// Settings as in the designs — grey page, rounded groups, rows with a coloured square icon — drawn by the
/// system's grouped form, so pickers, rows and links behave as everywhere in iOS.
struct SettingsScreen: View {
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL
    @StateObject private var store = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @State private var state: SettingsUiState?
    @State private var miniApp: MiniAppScreen?

    var body: some View {
        let state = state ?? store.instance.state.value
        Form {
            Section(strings.settingsPreferences) {
                if state.cities.count > 1 {
                    Picker(selection: Binding(
                        get: { state.city ?? state.cities.first?.slug ?? "" },
                        set: { store.instance.onCitySelected(slug: $0) }
                    )) {
                        ForEach(state.cities, id: \.slug) { Text($0.name).tag($0.slug) }
                    } label: {
                        Label(strings.feedCity, systemImage: "building.2.fill").labelStyle(.settingsIcon(.blue))
                    }
                }
                Picker(selection: Binding(get: { state.theme }, set: { store.instance.onThemeSelected(theme: $0) })) {
                    Text(strings.themeSystem).tag(ThemeMode.system)
                    Text(strings.themeLight).tag(ThemeMode.light)
                    Text(strings.themeDark).tag(ThemeMode.dark)
                } label: {
                    Label(strings.settingsAppearance, systemImage: "paintpalette.fill").labelStyle(.settingsIcon(.pink))
                }
                Picker(selection: Binding(get: { state.language }, set: { store.instance.onLanguageSelected(language: $0) })) {
                    ForEach(languages, id: \.self) { language in
                        Text(LanguageName.of(language, system: strings.languageSystem)).tag(language)
                    }
                } label: {
                    Label(strings.settingsLanguage, systemImage: "globe").labelStyle(.settingsIcon(.purple))
                }
            }

            Section {
                Button { miniApp = .submit } label: {
                    Label(strings.feedSubmit, systemImage: "plus").labelStyle(.settingsIcon(.orange))
                }
                Button { miniApp = .myEvents } label: {
                    Label(strings.myEvents, systemImage: "list.bullet.rectangle").labelStyle(.settingsIcon(.green))
                }
                Link(destination: Site.rules) {
                    Label(strings.settingsRules, systemImage: "doc.text.fill").labelStyle(.settingsIcon(.gray))
                }
            } header: {
                Text(strings.settingsResources)
            } footer: {
                VStack(alignment: .leading, spacing: 6) {
                    Text(strings.settingsVersion(state.versionName))
                    Text(strings.feedSubmitHint)
                    Text(strings.settingsOpenSource)
                }
                .padding(.top, 8)
            }
        }
        .tint(Palette.ink)
        .fullScreenCover(item: $miniApp) { MiniAppSheet(screen: $0) }
        .navigationTitle(strings.settingsTitle)
        .navigationBarTitleDisplayMode(.inline)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private let languages: [AppLanguage] = [.system, .ukrainian, .russian, .belarusian, .english]
}
