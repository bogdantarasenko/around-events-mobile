import AroundKit
import SwiftUI

/// The around.events mini-app inside the app: submitting an event, or the organizer's own events. It is the same
/// page as on the open web, opened with the app's city, language and theme.
struct MiniAppSheet: View {
    let screen: MiniAppScreen
    @Environment(\.strings) private var strings
    @Environment(\.dismiss) private var dismiss
    @State private var failed = false
    @State private var attempt = 0

    private let links = Deps.shared.getMiniAppLinks()

    var body: some View {
        NavigationStack {
            Group {
                if failed {
                    ContentUnavailableView {
                        Label(strings.apiOffline, systemImage: "wifi.slash")
                    } description: {
                        Text(strings.miniAppOffline)
                    } actions: {
                        Button(strings.retry) {
                            failed = false
                            attempt += 1
                        }
                    }
                } else {
                    MiniAppWebView(url: links.url(screen: screen), siteHost: links.host, attempt: attempt) { failed = true }
                        .ignoresSafeArea(edges: .bottom)
                }
            }
            .navigationTitle(screen == .submit ? strings.feedSubmit : strings.myEvents)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: { Image(systemName: "xmark") }
                        .accessibilityLabel(strings.close)
                }
            }
        }
        .interactiveDismissDisabled()
    }
}

extension MiniAppScreen: @retroactive Identifiable {
    public var id: Self { self }
}
