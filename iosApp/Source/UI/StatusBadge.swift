import AroundKit
import SwiftUI

/// What needs attention stands out, what is settled stays quiet.
struct StatusBadge: View {
    let status: EventStatus
    @Environment(\.strings) private var strings

    var body: some View {
        Text(Labels.status(status, strings))
            .font(.caption.weight(.medium))
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .foregroundStyle(color)
            .background(color.opacity(0.15), in: RoundedRectangle(cornerRadius: 6))
    }

    private var color: Color {
        switch status {
        case .review: .orange
        case .queued: .blue
        case .published, .duplicate: .green
        case .rejected, .cancelled: .red
        }
    }
}
