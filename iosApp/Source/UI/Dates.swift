import AroundKit
import Foundation

/// Dates from the server (`yyyy-MM-dd`, ISO timestamps) shown in the interface language.
enum Dates {
    private static let isoDay: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone(identifier: "UTC")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    static func parse(_ iso: String?) -> Date? {
        guard let iso, !iso.isEmpty else { return nil }
        return isoDay.date(from: iso)
    }

    /// `1 December, Tue`.
    static func day(_ iso: String?, locale: Locale) -> String? {
        guard let date = parse(iso) else { return nil }
        return formatted(date, template: "dMMMMEEE", locale: locale)
    }

    /// The heading of a feed day: today, tomorrow, or the date.
    static func heading(_ day: FeedDay, strings: AroundStrings, locale: Locale) -> String {
        guard let date = day.date else { return strings.feedOngoing }
        let now = Date()
        if date == localDay(now) { return strings.feedToday }
        if let tomorrow = Calendar.current.date(byAdding: .day, value: 1, to: now), date == localDay(tomorrow) {
            return strings.feedTomorrow
        }
        return Self.day(date, locale: locale) ?? date
    }

    /// `19:00–21:00`, or nil when the event gave no time.
    static func time(_ event: Event) -> String? {
        let time = [event.startTime, event.endTime].compactMap { $0 }.joined(separator: "–")
        return time.isEmpty ? nil : time
    }

    /// `1 December, Tue · 19:00–21:00`, or a range for several days.
    static func when(_ event: Event, strings: AroundStrings, locale: Locale) -> String {
        guard let start = day(event.startDate, locale: locale) else { return strings.dateNone }
        var days = start
        if let end = event.endDate, end != event.startDate {
            days += " — " + (day(end, locale: locale) ?? end)
        }
        return time(event).map { "\(days) · \($0)" } ?? days
    }

    static func price(_ event: Event, strings: AroundStrings) -> String? {
        guard let raw = event.priceFrom, let price = Double(raw) else { return nil }
        if price == 0 { return strings.free }
        let amount = price.truncatingRemainder(dividingBy: 1) == 0 ? String(Int(price)) : String(price)
        return strings.priceFrom([amount, currency(event.currency)].joined(separator: " "))
    }

    // The device's calendar day, as the shared feed counts "today".
    private static func localDay(_ date: Date) -> String {
        let parts = Calendar.current.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d", parts.year ?? 0, parts.month ?? 0, parts.day ?? 0)
    }

    // Polish złoty is how the listing's own posts write it.
    private static func currency(_ code: String?) -> String {
        switch code {
        case nil, "PLN": "zł"
        case "EUR": "€"
        case "USD": "$"
        case let other?: other
        }
    }

    private static func formatted(_ date: Date, template: String, locale: Locale) -> String {
        let formatter = DateFormatter()
        formatter.locale = locale
        formatter.timeZone = TimeZone(identifier: "UTC")
        formatter.setLocalizedDateFormatFromTemplate(template)
        return formatter.string(from: date)
    }
}
