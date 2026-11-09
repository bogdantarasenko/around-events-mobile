import SwiftUI

/// A form row with its problem, if any, right under it.
struct FieldRow<Content: View>: View {
    let problem: String?
    var hint: String?
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            content
            if let problem {
                Text(problem).font(.footnote).foregroundStyle(.red)
            } else if let hint {
                Text(hint).font(.footnote).foregroundStyle(.secondary)
            }
        }
    }
}
