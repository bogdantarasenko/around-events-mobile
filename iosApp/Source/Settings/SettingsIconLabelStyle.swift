import SwiftUI

/// A settings row label from the designs: the icon white in a 28pt rounded square of [tint].
struct SettingsIconLabelStyle: LabelStyle {
    let tint: Color
    @ScaledMetric(relativeTo: .body) private var side: CGFloat = 28

    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 12) {
            configuration.icon
                .font(.footnote.weight(.semibold))
                .foregroundStyle(.white)
                .frame(width: side, height: side)
                .background(tint, in: RoundedRectangle(cornerRadius: side / 4))
            configuration.title.foregroundStyle(Palette.ink)
        }
    }
}

extension LabelStyle where Self == SettingsIconLabelStyle {
    static func settingsIcon(_ tint: Color) -> SettingsIconLabelStyle { SettingsIconLabelStyle(tint: tint) }
}
