import AroundKit
import SwiftUI

/// A line of the feed. The day is the section heading, so the row starts with the time.
struct EventRow: View {
    let event: Event
    @Environment(\.strings) private var strings

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            if event.posterUrl != nil {
                PosterView(url: event.posterUrl, size: 64)
            }
            VStack(alignment: .leading, spacing: 3) {
                if let time = Dates.time(event) {
                    Text(time)
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(Color.accentColor)
                }
                Text(event.title)
                    .font(.headline)
                    .lineLimit(3)
                if let venue = event.venue {
                    Text(venue)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
                let tags = [event.category.map { Labels.category($0, strings) }, Dates.price(event, strings: strings)]
                    .compactMap { $0 }
                if !tags.isEmpty {
                    Text(tags.joined(separator: " · "))
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
        .padding(.vertical, 4)
    }
}
