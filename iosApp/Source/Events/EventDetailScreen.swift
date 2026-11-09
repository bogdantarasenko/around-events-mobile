import AroundKit
import SwiftUI

struct EventDetailScreen: View {
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @Environment(\.locale) private var locale
    @Environment(\.openURL) private var openURL
    @StateObject private var store: VMStoreOwner<EventDetailViewModel>
    @State private var state: EventDetailUiState?
    @State private var confirmCancel = false

    init(eventId: Int64, path: Binding<[AppRoute]>) {
        _path = path
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getEventDetailViewModel(eventId: eventId)))
    }

    var body: some View {
        let state = state ?? store.instance.state.value
        Group {
            if let event = state.event {
                content(event, state)
            } else {
                ContentUnavailableView(strings.apiNotFound, systemImage: "calendar.badge.exclamationmark")
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .confirmationDialog(strings.cancelTitle, isPresented: $confirmCancel, titleVisibility: .visible) {
            Button(strings.yesCancel, role: .destructive) { store.instance.cancel() }
            Button(strings.no, role: .cancel) {}
        } message: {
            Text(strings.cancelText)
        }
        .task {
            for await value in store.instance.state {
                self.state = value
            }
        }
    }

    private func content(_ event: MyEvent, _ state: EventDetailUiState) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                if let poster = event.posterUrl, let url = URL(string: poster) {
                    AsyncImage(url: url) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        Color(.secondarySystemFill).aspectRatio(1, contentMode: .fit)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                Text(event.title).font(.title2.weight(.semibold))
                Text(Dates.when(event, strings: strings, locale: locale)).foregroundStyle(.secondary)
                statusBlock(event)
                if let error = state.error {
                    ErrorBanner(message: Labels.error(error, strings))
                }
                actions(event, isCancelling: state.isCancelling)
                Divider()
                EventDetails(event: event, nextDates: state.nextDates)
            }
            .padding()
        }
    }

    private func statusBlock(_ event: MyEvent) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            StatusBadge(status: event.status)
            if let explanation = explanation(event) {
                Text(explanation).font(.subheadline)
            }
            HStack {
                if event.status != .cancelled,
                   let post = event.telegramPost?.url ?? event.duplicateOf?.telegramPost?.url,
                   let url = URL(string: post) {
                    Button(strings.openPost) { openURL(url) }.buttonStyle(.bordered)
                }
                if event.status != .cancelled, let threads = event.threadsPost?.url, let url = URL(string: threads) {
                    Button(strings.openThreads) { openURL(url) }.buttonStyle(.bordered)
                }
            }
        }
    }

    private func explanation(_ event: MyEvent) -> String? {
        switch event.status {
        case .review: strings.reviewTakes
        case .queued: strings.soonOut
        case .published: Dates.moment(event.telegramPost?.publishedAt, locale: locale).map { strings.inAfishaSince($0) }
        case .rejected: event.skipReason.map { strings.reason($0) } ?? strings.noReason
        case .duplicate: strings.inAfishaOther
        case .cancelled: Dates.moment(event.cancelledAt, locale: locale).map { strings.cancelledAt($0) }
        }
    }

    private func actions(_ event: MyEvent, isCancelling: Bool) -> some View {
        VStack(spacing: 10) {
            if event.canEdit {
                wideButton(strings.edit, prominent: true) { path.append(.form(.edit, event.id)) }
            }
            if event.canFix {
                wideButton(strings.fixResend, prominent: true) { path.append(.form(.fix, event.id)) }
            }
            if event.canRepeat {
                wideButton(strings.repeatEvent, prominent: false) { path.append(.form(.repeat, event.id)) }
            }
            if event.status != .cancelled, let post = event.telegramPost?.url, let url = URL(string: post) {
                ShareLink(item: url, subject: Text(event.title)) {
                    Text(strings.share).frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
            }
            if event.canCancel {
                Button(role: .destructive) {
                    confirmCancel = true
                } label: {
                    if isCancelling { ProgressView() } else { Text(strings.cancelEvent) }
                }
                .disabled(isCancelling)
                .padding(.top, 4)
            }
        }
    }

    @ViewBuilder
    private func wideButton(_ title: String, prominent: Bool, action: @escaping () -> Void) -> some View {
        if prominent {
            Button(action: action) { Text(title).frame(maxWidth: .infinity) }.buttonStyle(.borderedProminent)
        } else {
            Button(action: action) { Text(title).frame(maxWidth: .infinity) }.buttonStyle(.bordered)
        }
    }
}
