import AroundKit
import SwiftUI

/// Events by day, as in the designs' lists: a day heading, then rows that open the event.
struct EventDays: View {
    let days: [FeedDay]

    var body: some View {
        ForEach(days, id: \.self) { day in
            DayHeader(day: day)
                .padding(.top, 18)
                .listRowInsets(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20))
                .listRowSeparator(.hidden)
            ForEach(Array(day.events.enumerated()), id: \.element.id) { index, event in
                ZStack {
                    NavigationLink(value: AppRoute.eventDetail(event.id)) { EmptyView() }.opacity(0)
                    EventRow(event: event, showsSeparator: index < day.events.count - 1)
                }
                .listRowInsets(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20))
                .listRowSeparator(.hidden)
            }
        }
    }
}
