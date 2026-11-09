import AroundKit
import SwiftUI

struct EventRow: View {
    let event: MyEvent
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            PosterView(url: event.posterUrl, size: 64)
            VStack(alignment: .leading, spacing: 3) {
                StatusBadge(status: event.status)
                Text(event.title)
                    .font(.headline)
                    .lineLimit(2)
                Text(Dates.when(event, strings: strings, locale: locale))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                if let venue = event.venue {
                    Text(venue)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
        }
        .padding(.vertical, 4)
    }
}
