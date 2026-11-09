import SwiftUI

/// A `yyyy-MM-dd` value; empty until the person picks a date.
struct DateRow: View {
    let label: String
    let value: String
    let onChange: (String) -> Void
    var canClear = false
    @Environment(\.strings) private var strings

    var body: some View {
        if let date = Dates.parse(value) {
            HStack {
                DatePicker(
                    label,
                    selection: Binding(get: { date }, set: { onChange(Dates.iso($0)) }),
                    displayedComponents: .date
                )
                .environment(\.timeZone, TimeZone(identifier: "UTC") ?? .current)
                if canClear {
                    Button(strings.clear) { onChange("") }.buttonStyle(.borderless)
                }
            }
        } else {
            HStack {
                Text(label)
                Spacer()
                Button(strings.formChoose) { onChange(Dates.iso(Date())) }
            }
        }
    }
}
