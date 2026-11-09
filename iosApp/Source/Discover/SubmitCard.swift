import SwiftUI

/// The way to the mini-app for organizers: submitting happens there, not in the app.
struct SubmitCard: View {
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL

    var body: some View {
        Button {
            openURL(Site.submit)
        } label: {
            HStack(spacing: 12) {
                Image(systemName: "plus").font(.headline).frame(width: 40, height: 40)
                    .background(Color.accentColor.opacity(0.15), in: Circle())
                    .foregroundStyle(Color.accentColor)
                VStack(alignment: .leading, spacing: 2) {
                    Text(strings.feedSubmit).font(.body.weight(.medium)).foregroundStyle(.primary)
                    Text(strings.feedSubmitHint).font(.footnote).foregroundStyle(.secondary)
                }
                Spacer(minLength: 0)
            }
            .padding(14)
            .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain)
        .padding(.horizontal)
    }
}
