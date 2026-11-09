import AroundKit
import SwiftUI

/// "Featured calendars" in the designs: a 60pt picture, the name in 17pt and two grey lines about them.
struct OrganizerList: View {
    let organizers: [Organizer]
    let onOpen: (String) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 17) {
            SectionTitle(title: strings.discoverOrganizers)
            VStack(spacing: 21) {
                ForEach(organizers, id: \.slug) { organizer in
                    Button {
                        onOpen(organizer.slug)
                    } label: {
                        HStack(alignment: .top, spacing: 12.5) {
                            Avatar(url: organizer.avatarUrl, name: organizer.name, size: 60, cornerRadius: 10)
                            VStack(alignment: .leading, spacing: 3) {
                                Text(organizer.name)
                                    .font(.body.weight(.medium))
                                    .foregroundStyle(Palette.ink)
                                Text(organizer.bio ?? strings.eventsCount("\(organizer.upcomingCount)"))
                                    .font(.subheadline)
                                    .foregroundStyle(Palette.secondary)
                                    .lineLimit(2)
                                    .multilineTextAlignment(.leading)
                            }
                            Spacer(minLength: 0)
                        }
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.pressable)
                }
            }
            .padding(.horizontal, 20)
        }
    }
}
