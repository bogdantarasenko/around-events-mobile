import AroundKit
import SwiftUI

/// The event's facts, as the organizer sent them.
struct EventDetails: View {
    let event: MyEvent
    let nextDates: [String]
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if let description = event.description_, !description.isEmpty {
                row(strings.detailDescription, description)
            }
            let place = [event.venue, event.address].compactMap { $0 }.joined(separator: ", ")
            if !place.isEmpty { row(strings.detailAddress, place) }
            if let price = Dates.price(event, strings: strings) {
                row(strings.formPricing, [price, event.priceNote].compactMap { $0 }.joined(separator: " · "))
            }
            if let tickets = event.ticketsUrl { row(strings.detailTickets, tickets) }
            if let link = event.eventUrl { row(strings.detailLink, link) }
            if let category = event.category { row(strings.detailCategory, Labels.category(category, strings)) }
            if !event.languages.isEmpty { row(strings.detailLanguages, languageNames) }
            if event.withKids?.boolValue == true { row(strings.detailWithKids, "✓") }
            if let recurrence = event.recurrence {
                let every = recurrence.freq == "biweekly" ? strings.everyBiweekly : strings.everyWeekly
                row(strings.detailRepeats, strings.repeatRule(every, Dates.day(recurrence.until, locale: locale) ?? recurrence.until))
            }
            if !nextDates.isEmpty {
                row(strings.nextDates, nextDates.compactMap { Dates.day($0, locale: locale) }.joined(separator: ", "))
            }
        }
    }

    private var languageNames: String {
        let names = Dictionary(uniqueKeysWithValues: VocabularyKt.EVENT_LANGUAGES.map {
            ($0.first as String? ?? "", $0.second as String? ?? "")
        })
        return event.languages.map { names[$0] ?? $0 }.joined(separator: ", ")
    }

    private func row(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            Text(value).textSelection(.enabled)
        }
    }
}
