import AroundKit
import SwiftUI

/// "19 July / Sunday": the date in ink, a light slash, the weekday in grey — 17pt semibold.
struct DayHeader: View {
    let day: FeedDay
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale

    var body: some View {
        let (main, weekday) = Dates.headingParts(day, strings: strings, locale: locale)
        HStack(spacing: 6) {
            Text(main).font(.system(size: 17, weight: .semibold)).foregroundStyle(Palette.ink)
            if let weekday {
                Text("/").font(.system(size: 17)).foregroundStyle(Palette.slash)
                Text(weekday).font(.system(size: 17, weight: .semibold)).foregroundStyle(Palette.weekday)
            }
        }
        .tracking(-0.43)
        .textCase(nil)
    }
}
