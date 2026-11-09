import SwiftUI

/// A settings row from the designs: a 28pt rounded square with a white icon, the title, the value in grey and a
/// chevron — or a diagonal arrow for a link that leaves the app.
struct SettingsRow: View {
    let icon: String
    let tint: Color
    let title: String
    var value: String?
    var external = false

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 28, height: 28)
                .background(tint, in: RoundedRectangle(cornerRadius: 7))
            Text(title).font(.system(size: 17)).foregroundStyle(Palette.ink)
            Spacer()
            if let value {
                Text(value).font(.system(size: 17)).foregroundStyle(Palette.secondary).lineLimit(1)
            }
            Image(systemName: external ? "arrow.up.right" : "chevron.right")
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(Palette.chevron)
        }
        .padding(.horizontal, 16)
        .frame(height: 49)
        .contentShape(Rectangle())
    }
}
