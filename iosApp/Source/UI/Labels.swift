import AroundKit

/// Interface texts for shared enums and errors, in the language picked in Settings.
enum Labels {
    static func status(_ status: EventStatus, _ strings: AroundStrings) -> String {
        switch status {
        case .review: strings.statusReview
        case .queued: strings.statusQueued
        case .published: strings.statusPublished
        case .rejected: strings.statusRejected
        case .duplicate: strings.statusDuplicate
        case .cancelled: strings.statusCancelled
        }
    }

    static func group(_ kind: EventGroupKind, _ strings: AroundStrings) -> String {
        switch kind {
        case .review: strings.groupReview
        case .soon: strings.groupSoon
        case .off: strings.groupOff
        case .past: strings.groupPast
        }
    }

    static func error(_ error: UiError, _ strings: AroundStrings) -> String {
        switch error.kind {
        case .offline: strings.apiOffline
        case .signedOut: strings.loginExpired
        case .notFound: strings.apiNotFound
        case .tooMany: strings.apiTooMany
        case .explained: error.message ?? strings.genericError
        case .server: strings.apiServerError(error.status.map { "\($0)" } ?? "")
        case .unknown: strings.genericError
        }
    }

    static func problem(_ field: FormField, _ strings: AroundStrings) -> String {
        switch field {
        case .title: strings.problemTitle
        case .category: strings.problemCategory
        case .poster: strings.problemPoster
        case .startDate: strings.problemStartDate
        case .endDate: strings.problemEndDate
        case .repeatUntil: strings.problemRepeatUntil
        case .venue: strings.problemVenue
        case .descriptionText: strings.problemDescription
        case .pricing: strings.problemPricing
        case .priceFrom: strings.problemPriceFrom
        case .acceptedRules: strings.problemAcceptedRules
        }
    }

    static func category(_ key: String, _ strings: AroundStrings) -> String {
        switch key {
        case "concert": strings.categoryConcert
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
        case "sport": strings.categorySport
        case "other": strings.categoryOther
        default: key
        }
    }
}
