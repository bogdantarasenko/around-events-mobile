import AroundKit
import SwiftUI

private struct StringsKey: EnvironmentKey {
    static let defaultValue: AroundStrings = Deps.shared.strings(languageTag: "en")
}

extension EnvironmentValues {
    /// Interface strings in the language picked in Settings.
    var strings: AroundStrings {
        get { self[StringsKey.self] }
        set { self[StringsKey.self] = newValue }
    }
}
