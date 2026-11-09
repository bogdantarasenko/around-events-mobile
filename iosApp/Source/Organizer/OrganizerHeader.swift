import AroundKit
import SwiftUI

/// The top of an organizer's page as in the designs: cover, avatar over its edge, name, bio and links.
struct OrganizerHeader: View {
    let organizer: Organizer
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            cover
            HStack(alignment: .bottom) {
                Avatar(url: organizer.avatarUrl, name: organizer.name, size: 84, cornerRadius: 14)
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(.systemBackground), lineWidth: 4))
                    .offset(y: -42)
                    .padding(.bottom, -42)
                Spacer()
                if let follow = organizer.followUrl.flatMap(URL.init(string:)) {
                    Button(strings.follow) { openURL(follow) }
                        .font(.subheadline.weight(.semibold))
                        .padding(.horizontal, 14)
                        .padding(.vertical, 7)
                        .foregroundStyle(Color(.systemBackground))
                        .background(Color.primary, in: Capsule())
                        .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
            .padding(.top, 10)

            VStack(alignment: .leading, spacing: 8) {
                Text(organizer.name).font(.title2.weight(.bold))
                if let bio = organizer.bio, !bio.isEmpty {
                    Text(bio).font(.subheadline).foregroundStyle(.secondary)
                }
                HStack(spacing: 12) {
                    if organizer.pastCount > 0 {
                        Text(strings.pastCount("\(organizer.pastCount)"))
                    }
                    Text(strings.eventsCount("\(organizer.events.count)"))
                }
                .font(.footnote)
                .foregroundStyle(.secondary)
                links
            }
            .padding(.horizontal)
            .padding(.top, 12)
            .padding(.bottom, 8)
        }
    }

    // No cover on the backend: the avatar, blurred, stands in for it.
    private var cover: some View {
        ZStack {
            LinearGradient(colors: [.accentColor.opacity(0.5), .purple.opacity(0.4)], startPoint: .topLeading, endPoint: .bottomTrailing)
            if let avatar = organizer.avatarUrl.flatMap(URL.init(string:)) {
                AsyncImage(url: avatar) { image in
                    image.resizable().scaledToFill().blur(radius: 24).opacity(0.8)
                } placeholder: {
                    Color.clear
                }
            }
        }
        .frame(height: 150)
        .clipped()
    }

    private var links: some View {
        HStack(spacing: 18) {
            link(organizer.links.instagram, systemImage: "camera")
            link(organizer.links.telegram, systemImage: "paperplane")
            link(organizer.links.website, systemImage: "globe")
        }
        .foregroundStyle(.secondary)
    }

    @ViewBuilder
    private func link(_ raw: String?, systemImage: String) -> some View {
        if let url = raw.flatMap(URL.init(string:)) {
            Button { openURL(url) } label: { Image(systemName: systemImage).font(.body) }
                .buttonStyle(.plain)
        }
    }
}
