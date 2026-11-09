import SwiftUI

/// "Cities ›": a section heading that opens the full list when it has an action.
struct SectionTitle: View {
    let title: String
    var subtitle: String?
    var action: (() -> Void)?

    var body: some View {
        Button {
            action?()
        } label: {
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(title).font(.title3.weight(.semibold)).foregroundStyle(.primary)
                    if action != nil {
                        Image(systemName: "chevron.right").font(.subheadline.weight(.semibold)).foregroundStyle(.tertiary)
                    }
                }
                if let subtitle {
                    Text(subtitle).font(.title3).foregroundStyle(.tertiary)
                }
            }
        }
        .buttonStyle(.plain)
        .disabled(action == nil)
        .padding(.horizontal)
    }
}
