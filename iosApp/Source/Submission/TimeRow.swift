import SwiftUI

/// An optional `HH:mm` value.
struct TimeRow: View {
    let label: String
    let value: String
    let onChange: (String) -> Void
    @Environment(\.strings) private var strings

    private static let format: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone(identifier: "UTC")
        formatter.dateFormat = "HH:mm"
        return formatter
    }()

    var body: some View {
        if let time = Self.format.date(from: value) {
            HStack {
                DatePicker(
                    label,
                    selection: Binding(get: { time }, set: { onChange(Self.format.string(from: $0)) }),
                    displayedComponents: .hourAndMinute
                )
                .environment(\.timeZone, TimeZone(identifier: "UTC") ?? .current)
                Button(strings.clear) { onChange("") }.buttonStyle(.borderless)
            }
        } else {
            HStack {
                Text(label)
                Spacer()
                Button(strings.formChoose) { onChange("19:00") }
            }
        }
    }
}
