import SwiftUI

/// The hairline between rows of a settings group, starting after the icon.
struct SettingsDivider: View {
    var body: some View {
        Rectangle().fill(Palette.separator).frame(height: 0.5).padding(.leading, 56)
    }
}
