import AroundKit

/// Interface texts for shared enums, errors and category keys, in the language picked in Settings.
enum Labels {
    static func error(_ error: UiError, _ strings: AroundStrings) -> String {
        switch error.kind {
        case .offline: strings.apiOffline
        case .notFound: strings.apiNotFound
        case .tooMany: strings.apiTooMany
        case .server: strings.apiServerError(error.status.map { "\($0)" } ?? "")
        case .unknown: strings.genericError
        }
    }

    /// Imported events carry the source's own words; an unknown one is shown as it came, capitalised.
    static func category(_ key: String, _ strings: AroundStrings) -> String {
        switch key {
        case "concert": strings.categoryConcert
        case "music": strings.categoryMusic
        case "standup": strings.categoryStandup
        case "theatre": strings.categoryTheatre
        case "party": strings.categoryParty
        case "festival": strings.categoryFestival
        case "exhibition": strings.categoryExhibition
        case "kids": strings.categoryKids
        case "market": strings.categoryMarket
        case "food": strings.categoryFood
        case "cinema": strings.categoryCinema
        case "lecture": strings.categoryLecture
        case "learning": strings.categoryLearning
        case "sport": strings.categorySport
        case "games": strings.categoryGames
        case "creative": strings.categoryCreative
        case "other": strings.categoryOther
        default: key.prefix(1).uppercased() + key.dropFirst()
        }
    }
}
