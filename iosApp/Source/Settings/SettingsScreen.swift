import AroundKit
import SwiftUI

/// Settings as in the designs: grey page, white rounded groups, rows with a coloured square icon and a chevron.
struct SettingsScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL
    @StateObject private var store = VMStoreOwner<SettingsViewModel>(Deps.shared.getSettingsViewModel())
    @State private var state: SettingsUiState?

    var body: some View {
        let state = state ?? store.instance.state.value
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                SettingsGroup {
                    if state.cities.count > 1 {
                        Menu {
                            Picker(strings.feedCity, selection: Binding(
                                get: { state.city ?? state.cities.first?.slug ?? "" },
                                set: { store.instance.onCitySelected(slug: $0) }
                            )) {
                                ForEach(state.cities, id: \.slug) { Text($0.name).tag($0.slug) }
                            }
                        } label: {
                            SettingsRow(icon: "building.2.fill", tint: .blue, title: strings.feedCity, value: cityName(state))
                        }
                        SettingsDivider()
                    }
                    Menu {
                        Picker(strings.settingsAppearance, selection: Binding(
                            get: { state.theme },
                            set: { store.instance.onThemeSelected(theme: $0) }
                        )) {
                            Text(strings.themeSystem).tag(ThemeMode.system)
                            Text(strings.themeLight).tag(ThemeMode.light)
                            Text(strings.themeDark).tag(ThemeMode.dark)
                        }
                    } label: {
                        SettingsRow(icon: "paintpalette.fill", tint: .pink, title: strings.settingsAppearance, value: themeName(state.theme))
                    }
                    SettingsDivider()
                    Menu {
                        Picker(strings.settingsLanguage, selection: Binding(
                            get: { state.language },
                            set: { store.instance.onLanguageSelected(language: $0) }
                        )) {
                            ForEach(languages, id: \.self) { language in
                                Text(LanguageName.of(language, system: strings.languageSystem)).tag(language)
                            }
                        }
                    } label: {
                        SettingsRow(
                            icon: "globe",
                            tint: .purple,
                            title: strings.settingsLanguage,
                            value: LanguageName.of(state.language, system: strings.languageSystem)
                        )
                    }
                }
                .titled(strings.settingsPreferences)

                SettingsGroup {
                    Button { openURL(Site.submit) } label: {
                        SettingsRow(icon: "plus", tint: .orange, title: strings.feedSubmit, external: true)
                    }
                    SettingsDivider()
                    Button { openURL(Site.rules) } label: {
                        SettingsRow(icon: "doc.text.fill", tint: .gray, title: strings.settingsRules, external: true)
                    }
                }
                .titled(strings.settingsResources)

                VStack(alignment: .leading, spacing: 6) {
                    Text(strings.settingsVersion(state.versionName))
                    Text(strings.feedSubmitHint)
                    Text(strings.settingsOpenSource)
                }
                .font(.system(size: 13))
                .foregroundStyle(Palette.secondary)
                .padding(.horizontal, 16)
                .padding(.top, 20)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 100)
        }
        .background(Color(.systemGroupedBackground))
        .safeAreaInset(edge: .top) {
            ZStack {
                Text(strings.settingsTitle).font(.system(size: 17, weight: .semibold)).foregroundStyle(Palette.ink)
                TopBar(path: $path)
            }
            .padding(.bottom, 8)
            .background(Color(.systemGroupedBackground))
        }
        .toolbar(.hidden, for: .navigationBar)
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func cityName(_ state: SettingsUiState) -> String {
        state.cities.first { $0.slug == state.city }?.name ?? state.cities.first?.name ?? ""
    }

    private func themeName(_ theme: ThemeMode) -> String {
        switch theme {
        case .light: strings.themeLight
        case .dark: strings.themeDark
        default: strings.themeSystem
        }
    }

    private let languages: [AppLanguage] = [.system, .ukrainian, .russian, .belarusian, .english]
}
