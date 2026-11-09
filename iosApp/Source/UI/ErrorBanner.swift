import SwiftUI

struct ErrorBanner: View {
    let message: String
    var actionLabel: String?
    var action: (() -> Void)?

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(message)
                .font(.subheadline)
                .frame(maxWidth: .infinity, alignment: .leading)
            if let actionLabel, let action {
                Button(actionLabel, action: action)
                    .font(.subheadline.weight(.semibold))
            }
        }
        .padding(12)
        .foregroundStyle(.red)
        .background(Color.red.opacity(0.1), in: RoundedRectangle(cornerRadius: 12))
    }
}
