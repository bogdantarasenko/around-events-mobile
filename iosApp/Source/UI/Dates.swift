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

    /// The heading of a feed day in two parts, "8 November" and "Wednesday", or "Today" and "Wednesday".
    static func headingParts(_ day: FeedDay, strings: AroundStrings, locale: Locale) -> (String, String?) {
        guard let iso = day.date, let date = parse(iso) else { return (strings.feedOngoing, nil) }
        let weekday = formatted(date, template: "EEEE", locale: locale)
        let now = Date()
        if iso == localDay(now) { return (strings.feedToday, weekday) }
        if let tomorrow = Calendar.current.date(byAdding: .day, value: 1, to: now), iso == localDay(tomorrow) {
            return (strings.feedTomorrow, weekday)
        }
        return (formatted(date, template: "dMMMM", locale: locale), weekday)
    }

    /// `Tomorrow, 19:00`, `Sat, 15 Nov, 19:00`: a day and time in one short line, for shelves and the map sheet.
    static func short(_ event: Event, strings: AroundStrings, locale: Locale) -> String {
        guard let iso = event.startDate, let date = parse(iso) else { return strings.dateNone }
        let now = Date()
        let day: String
        if iso == localDay(now) {
            day = strings.feedToday
        } else if let tomorrow = Calendar.current.date(byAdding: .day, value: 1, to: now), iso == localDay(tomorrow) {
            day = strings.feedTomorrow
        } else {
            day = formatted(date, template: "EEEdMMM", locale: locale)
        }
        return [day, event.startTime].compactMap { $0 }.joined(separator: ", ")
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
