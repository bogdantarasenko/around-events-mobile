import AroundKit
import SwiftUI

/// The event line of the designs: an 80pt thumbnail, the organizer in grey with the price tag, a 17pt title,
/// then the time and the place with their icons.
struct EventRow: View {
    let event: Event
    /// The shelf on Discover: the day goes in the time line, the place is left out.
    var compact = false
    var showsSeparator = true
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale

    var body: some View {
        HStack(alignment: .top, spacing: 13) {
            EventThumb(event: event)
            VStack(alignment: .leading, spacing: 0) {
                hostLine
                Text(event.title)
                    .font(.system(size: 17, weight: .medium))
                    .tracking(-0.43)
                    .foregroundStyle(Palette.ink)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                    .padding(.top, 7)
                VStack(alignment: .leading, spacing: 6) {
                    if let when = compact ? Dates.short(event, strings: strings, locale: locale) : Dates.time(event) {
                        meta("clock", when)
                    }
                    if !compact, let venue = event.venue {
                        meta("mappin", venue)
                    }
                }
                .padding(.top, 8)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.top, 14)
        .padding(.bottom, 16)
        .overlay(alignment: .bottom) {
            if showsSeparator {
                Rectangle().fill(Palette.separator).frame(height: 0.5).padding(.leading, 93)
            }
        }
        .contentShape(Rectangle())
    }

    private var hostLine: some View {
        HStack(spacing: 5) {
            if let organizer = event.organizer {
                Avatar(url: organizer.avatarUrl, name: organizer.name, size: 15, cornerRadius: 4.5)
                Text(organizer.name).lineLimit(1)
            } else if let category = event.category {
                Image(systemName: CategoryStyle.symbol(category))
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundStyle(CategoryStyle.tint(category))
                    .frame(width: 15, height: 15)
                    .background(CategoryStyle.tint(category).opacity(0.16), in: RoundedRectangle(cornerRadius: 4.5))
                Text(Labels.category(category, strings)).lineLimit(1)
            }
            Spacer(minLength: 6)
            if let price = Dates.price(event, strings: strings) {
                PriceBadge(text: price)
            }
        }
        .font(.system(size: 14))
        .tracking(-0.15)
        .foregroundStyle(Palette.secondary)
    }

    private func meta(_ systemImage: String, _ text: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: systemImage).font(.system(size: 13, weight: .medium)).frame(width: 16)
            Text(text).font(.system(size: 15)).tracking(-0.23).lineLimit(1)
        }
        .foregroundStyle(Palette.secondary)
    }
}
