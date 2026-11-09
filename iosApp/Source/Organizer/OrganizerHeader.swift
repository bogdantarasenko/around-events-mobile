import AroundKit
import SwiftUI

/// The top of an organizer's page: a 223pt cover, the 80pt avatar in a white card over its edge, the black
/// Follow pill, name, bio, place and links, then a hairline.
struct OrganizerHeader: View {
    let organizer: Organizer
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            cover
            HStack(alignment: .top) {
                Avatar(url: organizer.avatarUrl, name: organizer.name, size: 80, cornerRadius: 10)
                    .padding(3.5)
                    .background(Palette.surface, in: RoundedRectangle(cornerRadius: 14))
                    .shadow(color: .black.opacity(0.06), radius: 2.5, y: 2)
                    .offset(y: -44)
                    .padding(.bottom, -44)
                Spacer()
                if let follow = organizer.followUrl.flatMap(URL.init(string:)) {
                    Button(strings.follow) { openURL(follow) }
                        .font(.system(size: 15, weight: .medium))
                        .foregroundStyle(Palette.surface)
                        .padding(.horizontal, 11)
                        .frame(height: 33)
                        .background(Palette.ink, in: Capsule())
                        .buttonStyle(.plain)
                }
            }
            .padding(.leading, 16.5)
            .padding(.trailing, 20)
            .padding(.top, 10.5)

            VStack(alignment: .leading, spacing: 0) {
                Text(organizer.name)
                    .font(.system(size: 22, weight: .bold))
                    .tracking(-0.26)
                    .foregroundStyle(Palette.ink)
                if let bio = organizer.bio, !bio.isEmpty {
                    Text(bio)
                        .font(.system(size: 15))
                        .tracking(-0.23)
                        .foregroundStyle(Palette.secondary)
                        .padding(.top, 7)
                }
                HStack(spacing: 8) {
                    Image(systemName: "calendar").font(.system(size: 14))
                    Text([organizer.pastCount > 0 ? strings.pastCount("\(organizer.pastCount)") : nil,
                          strings.eventsCount("\(organizer.events.count)")].compactMap { $0 }.joined(separator: " · "))
                }
                .font(.system(size: 15))
                .foregroundStyle(Palette.weekday)
                .padding(.top, 10)
                links.padding(.top, 14)
            }
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 16)
            Rectangle().fill(Palette.chipBorder).frame(height: 0.5)
        }
    }

    // No cover on the backend: the avatar, blurred and dimmed, stands in for it.
    private var cover: some View {
        ZStack {
            LinearGradient(colors: [.accentColor.opacity(0.55), .purple.opacity(0.45)], startPoint: .topLeading, endPoint: .bottomTrailing)
            if let avatar = organizer.avatarUrl.flatMap(URL.init(string:)) {
                AsyncImage(url: avatar) { image in
                    image.resizable().scaledToFill().blur(radius: 20)
                } placeholder: {
                    Color.clear
                }
            }
        }
        .frame(height: 223)
        .clipped()
    }

    private var links: some View {
        HStack(spacing: 18) {
            link(organizer.links.instagram, systemImage: "camera")
            link(organizer.links.telegram, systemImage: "paperplane")
            link(organizer.links.website, systemImage: "globe")
        }
        .foregroundStyle(Palette.weekday)
    }

    @ViewBuilder
    private func link(_ raw: String?, systemImage: String) -> some View {
        if let url = raw.flatMap(URL.init(string:)) {
            Button { openURL(url) } label: { Image(systemName: systemImage).font(.system(size: 17)) }
                .buttonStyle(.plain)
        }
    }
}
