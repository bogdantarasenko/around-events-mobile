import AroundKit
import MapKit
import SwiftUI

/// Location with a map, price, organizer, languages, repeats and the description — in the designs' sections.
struct EventFacts: View {
    let event: Event
    let nextDates: [String]
    let onOpenOrganizer: (String) -> Void
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(alignment: .leading, spacing: 22) {
            location
            if let organizer = event.organizer {
                section(strings.detailOrganizer) {
                    Button { onOpenOrganizer(organizer.slug) } label: {
                        HStack(spacing: 10) {
                            Avatar(url: organizer.avatarUrl, name: organizer.name, size: 32)
                            Text(organizer.name).font(.body.weight(.medium))
                            Spacer()
                            Image(systemName: "chevron.right").foregroundStyle(.white.opacity(0.5))
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
            if let price = Dates.price(event, strings: strings) {
                section(strings.detailPrice) { Text(price) }
            }
            details
            if let description = event.description_, !description.isEmpty {
                section(strings.detailAbout) {
                    Text(description).textSelection(.enabled)
                }
            }
            if let category = event.category {
                Text("# " + Labels.category(category, strings))
                    .font(.subheadline.weight(.medium))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(.white.opacity(0.15), in: Capsule())
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private var location: some View {
        if event.venue != nil || event.address != nil {
            section(strings.detailLocation) {
                VStack(alignment: .leading, spacing: 10) {
                    VStack(alignment: .leading, spacing: 4) {
                        if let venue = event.venue { Text(venue).font(.system(size: 17, weight: .medium)) }
                        if let address = event.address {
                            Text(address).font(.system(size: 16)).foregroundStyle(Color(white: 0.7))
                        }
                    }
                    if let lat = event.latitude?.doubleValue, let lon = event.longitude?.doubleValue {
                        let point = CLLocationCoordinate2D(latitude: lat, longitude: lon)
                        Map(initialPosition: .region(MKCoordinateRegion(center: point, latitudinalMeters: 800, longitudinalMeters: 800))) {
                            Marker(event.venue ?? event.title, coordinate: point)
                        }
                        .frame(height: 140)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                        .allowsHitTesting(false)
                    }
                    Button(strings.openMap) { MapLink.open(event, openURL: openURL) }.font(.subheadline.weight(.medium))
                }
            }
        }
    }

    @ViewBuilder
    private var details: some View {
        let rows: [(String, String)] = [
            event.languages.isEmpty ? nil : (strings.detailLanguages, languageNames),
            event.withKids?.boolValue == true ? (strings.detailWithKids, "✓") : nil,
            event.recurrence.map { recurrence in
                let every = recurrence.freq == "biweekly" ? strings.everyBiweekly : strings.everyWeekly
                return (strings.detailRepeats, strings.repeatRule(every, Dates.day(recurrence.until, locale: locale) ?? recurrence.until))
            },
            nextDates.isEmpty ? nil : (strings.nextDates, nextDates.compactMap { Dates.day($0, locale: locale) }.joined(separator: ", ")),
        ].compactMap { $0 }
        ForEach(rows, id: \.0) { label, value in
            section(label) { Text(value) }
        }
    }

    private var languageNames: String {
        let names = Dictionary(uniqueKeysWithValues: VocabularyKt.EVENT_LANGUAGES.map {
            ($0.first as String? ?? "", $0.second as String? ?? "")
        })
        return event.languages.map { names[$0] ?? $0 }.joined(separator: ", ")
    }

    private func section<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.system(size: 15)).foregroundStyle(Color(white: 0.69))
            Rectangle().fill(Color.white.opacity(0.2)).frame(height: 0.8)
            content()
        }
    }
}
