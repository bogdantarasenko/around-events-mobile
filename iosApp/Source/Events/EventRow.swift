import AroundKit
import SwiftUI

/// A line of every event list in the designs: thumbnail, who runs it and the price, the title, when and where.
struct EventRow: View {
    let event: Event
    /// The section heading already says the day, so the row shows only the time.
    var showsDay = false
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            EventThumb(event: event)
            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    if let organizer = event.organizer {
                        Avatar(url: organizer.avatarUrl, name: organizer.name, size: 14)
                        Text(organizer.name).lineLimit(1)
                    } else if let category = event.category {
                        Text(Labels.category(category, strings)).lineLimit(1)
                    }
                    Spacer(minLength: 4)
                    if let price = Dates.price(event, strings: strings) {
                        PriceBadge(text: price)
                    }
                }
                .font(.footnote)
                .foregroundStyle(.secondary)

                Text(event.title)
                    .font(.body.weight(.medium))
                    .foregroundStyle(.primary)
                    .lineLimit(2)

                HStack(spacing: 10) {
                    if let when = showsDay ? Dates.short(event, strings: strings, locale: locale) : Dates.time(event) {
                        Label(when, systemImage: "clock").lineLimit(1)
                    }
                    if let venue = event.venue {
                        Label(venue, systemImage: "mappin.and.ellipse").lineLimit(1)
                    }
                }
                .font(.footnote)
                .foregroundStyle(.secondary)
                .labelStyle(CompactLabelStyle())
            }
        }
        .padding(.vertical, 6)
        .contentShape(Rectangle())
    }
}
