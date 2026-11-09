import AroundKit

/// Language names are written in the language itself, as in any language picker.
enum LanguageName {
    static func of(_ language: AppLanguage, system: String) -> String {
        switch language {
        case .system: system
        case .ukrainian: "Українська"
        case .russian: "Русский"
        case .belarusian: "Беларуская"
        case .english: "English"
        }
    }
}
