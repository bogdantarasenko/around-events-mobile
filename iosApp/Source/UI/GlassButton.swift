import SwiftUI

extension View {
    /// The system glass button on iOS 26 and later, the bordered one before it.
    @ViewBuilder
    func glassButtonStyle() -> some View {
        if #available(iOS 26.0, *) {
            buttonStyle(.glass)
        } else {
            buttonStyle(.bordered)
        }
    }

    /// Liquid Glass behind a custom control group on iOS 26 and later, the regular material before it.
    @ViewBuilder
    func glassBackground<S: Shape>(in shape: S) -> some View {
        if #available(iOS 26.0, *) {
            glassEffect(.regular.interactive(), in: shape)
        } else {
            background(.regularMaterial, in: shape)
        }
    }
}

extension ToolbarContent {
    /// No glass capsule behind a toolbar item on iOS 26 and later: for the logo and city, which are content, not a button.
    @ToolbarContentBuilder
    func hidingSharedBackground() -> some ToolbarContent {
        if #available(iOS 26.0, *) {
            sharedBackgroundVisibility(.hidden)
        } else {
            self
        }
    }
}
