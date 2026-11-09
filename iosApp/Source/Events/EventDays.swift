import AroundKit
import SwiftUI

/// List sections of events by day, each row opening the event.
struct EventDays: View {
    let days: [FeedDay]

    var body: some View {
        ForEach(days, id: \.self) { day in
            Section {
                ForEach(day.events, id: \.id) { event in
                    NavigationLink(value: AppRoute.eventDetail(event.id)) {
                        EventRow(event: event)
                    }
                    .listRowSeparator(.hidden)
                }
            } header: {
                DayHeader(day: day)
            }
        }
    }
}
