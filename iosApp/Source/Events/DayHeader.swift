import AroundKit
import SwiftUI

/// "8 November / Wednesday": the day in black, the weekday in grey.
struct DayHeader: View {
    let day: FeedDay
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale

    var body: some View {
        let (main, weekday) = Dates.headingParts(day, strings: strings, locale: locale)
        HStack(spacing: 6) {
            Text(main).foregroundStyle(.primary)
            if let weekday {
                Text("/").foregroundStyle(.tertiary)
                Text(weekday).foregroundStyle(.secondary)
            }
        }
        .font(.body.weight(.medium))
        .textCase(nil)
    }
}
