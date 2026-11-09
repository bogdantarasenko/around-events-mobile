import AroundKit
import SwiftUI

struct SubmissionFormScreen: View {
    let onClose: () -> Void
    @Environment(\.strings) private var strings
    @StateObject private var store: VMStoreOwner<SubmissionFormViewModel>
    @State private var state: SubmissionFormUiState?
    @State private var confirmDiscard = false

    init(kind: FormKind, eventId: Int64, onClose: @escaping () -> Void) {
        self.onClose = onClose
        _store = StateObject(wrappedValue: VMStoreOwner(Deps.shared.getSubmissionFormViewModel(kind: kind, eventId: eventId)))
    }

    private var viewModel: SubmissionFormViewModel { store.instance }

    var body: some View {
        let state = state ?? viewModel.state.value
        Group {
            if let result = state.result {
                SubmissionResultView(result: result, kind: state.kind, onDone: onClose)
            } else {
                form(state)
            }
        }
        .navigationTitle(state.kind == .edit ? strings.formEditTitle : strings.formNewTitle)
        .navigationBarTitleDisplayMode(.inline)
        // A new submission is kept as a draft; anything else would be lost on leaving.
        .navigationBarBackButtonHidden(state.kind != .theNew && state.result == nil)
        .toolbar {
            if state.kind != .theNew && state.result == nil {
                ToolbarItem(placement: .cancellationAction) {
                    Button(strings.back) {
                        if state.form.hasContent { confirmDiscard = true } else { onClose() }
                    }
                }
            }
        }
        .confirmationDialog(strings.formDiscardTitle, isPresented: $confirmDiscard, titleVisibility: .visible) {
            Button(strings.formDiscard, role: .destructive, action: onClose)
            Button(strings.formKeepEditing, role: .cancel) {}
        }
        .task {
            for await value in viewModel.state {
                self.state = value
            }
        }
    }

    private func form(_ state: SubmissionFormUiState) -> some View {
        ScrollViewReader { proxy in
            Form {
                banner(state)
                EventFormSection(state: state, viewModel: viewModel, problem: problem(state))
                PosterSection(
                    poster: state.poster,
                    problem: problem(state)(.poster),
                    onPick: { data in
                        viewModel.pickPoster(bytes: Deps.shared.bytes(data: data), fileName: "poster.jpg", mimeType: "image/jpeg")
                    },
                    onRemove: viewModel.removePoster
                )
                .id(FormField.poster)
                WhenFormSection(form: state.form, viewModel: viewModel, problem: problem(state))
                WhereFormSection(form: state.form, viewModel: viewModel, problem: problem(state))
                EntryFormSection(form: state.form, viewModel: viewModel, problem: problem(state))
                SendFormSection(state: state, viewModel: viewModel, problem: problem(state))
            }
            .scrollDismissesKeyboard(.interactively)
            .onChange(of: state.problem) { _, field in
                guard let field else { return }
                withAnimation { proxy.scrollTo(field, anchor: .center) }
            }
        }
    }

    @ViewBuilder
    private func banner(_ state: SubmissionFormUiState) -> some View {
        if state.sourceMissing {
            Section { ErrorBanner(message: strings.apiNotFound) }
        } else if state.kind == .fix {
            Section {
                ErrorBanner(message: "\(state.rejectionReason.map { strings.reason($0) } ?? strings.noReason) \(strings.rejectedFix)")
            }
        } else if state.kind == .theNew, state.restoredDraft {
            Section {
                ErrorBanner(message: strings.restored, actionLabel: strings.startOver) { viewModel.startOver() }
            }
        } else if state.kind == .theNew {
            Section { Text(strings.formIntro).font(.footnote).foregroundStyle(.secondary) }
        }
    }

    /// The text to show under a field: the server's words when it named the field, otherwise the local check's.
    private func problem(_ state: SubmissionFormUiState) -> (FormField) -> String? {
        let strings = strings
        return { field in
            guard state.problem == field else { return nil }
            return state.serverMessage ?? Labels.problem(field, strings)
        }
    }
}
