import AroundKit
import SwiftUI

/// Where, how much, who and the rest — what a reader checks before going.
struct EventFacts: View {
    let event: Event
    let nextDates: [String]
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            let place = [event.venue, event.address].compactMap { $0 }.joined(separator: ", ")
            if !place.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    row(strings.detailAddress, place)
                    Button(strings.openMap) { openMap(place) }.font(.subheadline)
                }
            }
            if let price = Dates.price(event, strings: strings) { row(strings.detailPrice, price) }
            if let organizer = event.organizer {
                VStack(alignment: .leading, spacing: 2) {
                    Text(strings.detailOrganizer).font(.caption).foregroundStyle(.secondary)
                    if let url = URL(string: organizer.url) {
                        Link(organizer.name, destination: url)
                    } else {
                        Text(organizer.name)
                    }
                }
            }
            if !event.languages.isEmpty { row(strings.detailLanguages, languageNames) }
            if event.withKids?.boolValue == true { row(strings.detailWithKids, "✓") }
            if let recurrence = event.recurrence {
                let every = recurrence.freq == "biweekly" ? strings.everyBiweekly : strings.everyWeekly
                row(strings.detailRepeats, strings.repeatRule(every, Dates.day(recurrence.until, locale: locale) ?? recurrence.until))
            }
            if !nextDates.isEmpty {
                row(strings.nextDates, nextDates.compactMap { Dates.day($0, locale: locale) }.joined(separator: ", "))
            }
            if let description = event.description_, !description.isEmpty {
                row(strings.detailDescription, description)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var languageNames: String {
        let names = Dictionary(uniqueKeysWithValues: VocabularyKt.EVENT_LANGUAGES.map {
            ($0.first as String? ?? "", $0.second as String? ?? "")
        })
        return event.languages.map { names[$0] ?? $0 }.joined(separator: ", ")
    }

    // A search in Maps: the backend has the place as text, without coordinates.
    private func openMap(_ place: String) {
        var components = URLComponents(string: "https://maps.apple.com/")
        components?.queryItems = [URLQueryItem(name: "q", value: "\(place), \(event.city.name)")]
        if let url = components?.url { openURL(url) }
    }

    private func row(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            Text(value).textSelection(.enabled)
        }
    }
}
