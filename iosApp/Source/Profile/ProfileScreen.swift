import AroundKit
import SwiftUI

struct ProfileScreen: View {
    let session: SessionUiState
    let viewModel: OrganizerViewModel
    @Binding var path: [AppRoute]
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL
    @State private var state: OrganizerUiState?

    var body: some View {
        let state = state ?? viewModel.state.value
        List {
            Section {
                HStack(spacing: 12) {
                    if let photo = session.photoUrl, let url = URL(string: photo) {
                        AsyncImage(url: url) { $0.resizable() } placeholder: { Color(.secondarySystemFill) }
                            .frame(width: 52, height: 52)
                            .clipShape(Circle())
                    }
                    VStack(alignment: .leading) {
                        Text(strings.signedAs).font(.caption).foregroundStyle(.secondary)
                        Text(session.displayName ?? "").font(.title3.weight(.semibold))
                    }
                }
            }
            Section(strings.pageTitle) {
                page(state)
            }
        }
        .task {
            for await value in viewModel.state {
                self.state = value
            }
        }
    }

    @ViewBuilder
    private func page(_ state: OrganizerUiState) -> some View {
        if state.isLoading, !state.isLoaded {
            ProgressView()
        } else if let error = state.error, !state.isLoaded {
            ErrorBanner(message: Labels.error(error, strings), actionLabel: strings.retry) { viewModel.load() }
        } else if let organizer = state.organizer {
            VStack(alignment: .leading, spacing: 6) {
                Text(organizer.name).font(.headline)
                Text(organizer.url + (organizer.isPublic ? "" : " · \(strings.pageHidden)"))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                if let bio = organizer.bio { Text(bio).font(.subheadline) }
                Text("\(state.upcomingCount) \(strings.upcomingHeader.lowercased()) · \(strings.pastCount(String(state.pastCount)))")
                    .font(.footnote)
                if state.justSaved {
                    Text(strings.pageSaved).font(.footnote).foregroundStyle(.tint)
                }
            }
            if organizer.isPublic, let url = URL(string: organizer.url) {
                Button(strings.pageOpen) { openURL(url) }
                ShareLink(strings.share, item: url)
            }
            Button(strings.pageEdit) { edit() }
        } else {
            Text(strings.pageHint).font(.subheadline).foregroundStyle(.secondary)
            Button(strings.pageCreate) { edit() }
        }
    }

    private func edit() {
        viewModel.startEditing()
        path.append(.organizerEdit)
    }
}
