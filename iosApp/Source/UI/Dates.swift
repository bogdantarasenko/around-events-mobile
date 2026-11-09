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

    static func iso(_ date: Date) -> String {
        isoDay.string(from: date)
    }

    /// `1 December, Tue`.
    static func day(_ iso: String?, locale: Locale) -> String? {
        guard let date = parse(iso) else { return nil }
        return formatted(date, template: "dMMMMEEE", locale: locale)
    }

    static func moment(_ iso: String?, locale: Locale) -> String? {
        guard let iso, let date = ISO8601DateFormatter().date(from: iso) else { return nil }
        let formatter = DateFormatter()
        formatter.locale = locale
        formatter.setLocalizedDateFormatFromTemplate("dMMMMHHmm")
        return formatter.string(from: date)
    }

    /// `1 December, Tue · 19:00–21:00`, or a range for several days.
    static func when(_ event: MyEvent, strings: AroundStrings, locale: Locale) -> String {
        guard let start = day(event.startDate, locale: locale) else { return strings.dateNone }
        let time = [event.startTime, event.endTime].compactMap { $0 }.joined(separator: "–")
        var days = start
        if let end = event.endDate, end != event.startDate {
            days += " — " + (day(end, locale: locale) ?? end)
        }
        return time.isEmpty ? days : "\(days) · \(time)"
    }

    static func price(_ event: MyEvent, strings: AroundStrings) -> String? {
        guard let raw = event.priceFrom, let price = Double(raw) else { return nil }
        if price == 0 { return strings.free }
        let text = price.truncatingRemainder(dividingBy: 1) == 0 ? String(Int(price)) : String(price)
        return strings.priceFrom(text)
    }

    private static func formatted(_ date: Date, template: String, locale: Locale) -> String {
        let formatter = DateFormatter()
        formatter.locale = locale
        formatter.timeZone = TimeZone(identifier: "UTC")
        formatter.setLocalizedDateFormatFromTemplate(template)
        return formatter.string(from: date)
    }
}
