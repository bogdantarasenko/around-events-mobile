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
}
