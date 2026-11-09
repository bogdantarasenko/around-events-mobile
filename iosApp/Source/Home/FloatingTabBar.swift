import SwiftUI

/// The glass pill at the bottom of every screen in the designs: Home, Discover, Map. 274 × 62, the selected tab
/// on a grey 94 × 54 capsule.
struct FloatingTabBar: View {
    @Binding var selection: AppTab
    var dark = false
    @Environment(\.strings) private var strings

    var body: some View {
        HStack(spacing: 0) {
            item(.home, strings.tabHome, "house.fill")
            item(.discover, strings.tabDiscover, "safari.fill")
            item(.map, strings.tabMap, "map.fill")
        }
        .padding(3)
        .frame(width: 274, height: 62)
        .background(.ultraThinMaterial, in: Capsule())
        .background((dark ? Color.white.opacity(0.17) : Palette.surface.opacity(0.78)), in: Capsule())
        .overlay(Capsule().stroke(Color.white.opacity(dark ? 0.25 : 0.9), lineWidth: 1))
        .shadow(color: .black.opacity(0.08), radius: 12, y: 4)
    }

    private func item(_ tab: AppTab, _ title: String, _ systemImage: String) -> some View {
        let isOn = selection == tab
        return Button {
            selection = tab
        } label: {
            VStack(spacing: 4) {
                Image(systemName: systemImage).font(.system(size: 18, weight: .medium))
                Text(title).font(.system(size: 11, weight: isOn ? .semibold : .medium))
            }
            .foregroundStyle(foreground(isOn))
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background {
                if isOn {
                    Capsule().fill(dark ? Color.white.opacity(0.22) : Palette.tabSelected)
                }
            }
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isOn ? .isSelected : [])
    }

    private func foreground(_ isOn: Bool) -> Color {
        if dark { return isOn ? .white : .white.opacity(0.85) }
        return isOn ? Palette.ink : Palette.tabInactive
    }
}
