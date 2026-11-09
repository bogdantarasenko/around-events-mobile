import AroundKit
import SwiftUI

/// "Organizers": the city's organizers with a public page, the busiest first.
struct OrganizerList: View {
    let organizers: [Organizer]
    let onOpen: (String) -> Void
    @Environment(\.strings) private var strings

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionTitle(title: strings.discoverOrganizers)
            VStack(spacing: 14) {
                ForEach(organizers, id: \.slug) { organizer in
                    Button {
                        onOpen(organizer.slug)
                    } label: {
                        HStack(alignment: .top, spacing: 14) {
                            Avatar(url: organizer.avatarUrl, name: organizer.name, size: 48, cornerRadius: 10)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(organizer.name).font(.body.weight(.medium)).foregroundStyle(.primary)
                                Text(organizer.bio ?? strings.eventsCount("\(organizer.upcomingCount)"))
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                                    .lineLimit(2)
                                    .multilineTextAlignment(.leading)
                            }
                            Spacer(minLength: 0)
                        }
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
        }
    }
}
