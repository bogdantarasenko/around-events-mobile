import AroundKit
import SwiftUI

/// "Featured calendars" in the designs: a 60pt picture, the name in 17pt and a grey line about them.
struct OrganizerRow: View {
    let organizer: Organizer
    var showsSeparator = true
    @Environment(\.strings) private var strings

    var body: some View {
        HStack(alignment: .center, spacing: 13) {
            Avatar(url: organizer.avatarUrl, name: organizer.name, size: 60, cornerRadius: 10)
            VStack(alignment: .leading, spacing: 3) {
                Text(organizer.name)
                    .font(.body.weight(.medium))
                    .foregroundStyle(Palette.ink)
                    .lineLimit(1)
                Text(organizer.bio ?? strings.eventsCount("\(organizer.upcomingCount)"))
                    .font(.subheadline)
                    .foregroundStyle(Palette.secondary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.vertical, 12)
        .overlay(alignment: .bottom) {
            if showsSeparator {
                Rectangle().fill(Palette.separator).frame(height: 0.5).padding(.leading, 73)
            }
        }
        .contentShape(Rectangle())
    }
}
