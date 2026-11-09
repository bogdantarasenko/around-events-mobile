import AroundKit
import SwiftUI

struct OrganizerEditScreen: View {
    let viewModel: OrganizerViewModel
    @Environment(\.strings) private var strings
    @Environment(\.dismiss) private var dismiss
    @State private var state: OrganizerUiState?

    var body: some View {
        let editor = (state ?? viewModel.state.value).editor
        Form {
            if let editor {
                fields(editor)
            }
        }
        .navigationTitle(strings.pageTitle)
        .navigationBarTitleDisplayMode(.inline)
        .onDisappear { viewModel.cancelEditing() }
        .task {
            for await value in viewModel.state {
                self.state = value
                // Saving closes the editor in the view model; the screen leaves with it.
                if value.editor == nil { dismiss() }
            }
        }
    }

    @ViewBuilder
    private func fields(_ editor: OrganizerEditor) -> some View {
        Section {
            Text(strings.pageHint).font(.footnote).foregroundStyle(.secondary)
            FieldRow(problem: message(editor, .name)) {
                TextField(strings.pageName, text: Binding(get: { editor.name }, set: viewModel.setName))
            }
            FieldRow(problem: message(editor, .slug), hint: "\(host)/o/\(editor.slug)") {
                TextField(strings.pageSlug, text: Binding(get: { editor.slug }, set: viewModel.setSlug))
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            }
            FieldRow(problem: message(editor, .bio)) {
                TextField(strings.pageBio, text: Binding(get: { editor.bio }, set: viewModel.setBio), axis: .vertical)
                    .lineLimit(3 ... 8)
            }
        }
        Section {
            linkField(strings.pageInstagram, editor.instagram, viewModel.setInstagram)
            linkField(strings.pageTelegram, editor.telegram, viewModel.setTelegram)
            linkField(strings.pageWebsite, editor.website, viewModel.setWebsite)
        }
        Section {
            Toggle(strings.pagePublic, isOn: Binding(get: { editor.isPublic }, set: viewModel.setPublic))
        } footer: {
            Text(strings.pagePublicHint)
        }
        Section {
            if let error = editor.error {
                ErrorBanner(message: Labels.error(error, strings))
            }
            Button {
                viewModel.save()
            } label: {
                if editor.isSaving { ProgressView().frame(maxWidth: .infinity) } else {
                    Text(strings.pageSave).font(.headline).frame(maxWidth: .infinity)
                }
            }
            .disabled(editor.isSaving)
        }
    }

    private func message(_ editor: OrganizerEditor, _ field: OrganizerField) -> String? {
        editor.fieldError == field ? editor.fieldMessage : nil
    }

    private func linkField(_ label: String, _ value: String, _ set: @escaping (String) -> Void) -> some View {
        TextField(label, text: Binding(get: { value }, set: set))
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .keyboardType(.URL)
    }

    private var host: String {
        let base = Bundle.main.infoDictionary?["AroundBaseURL"] as? String ?? "https://around.events"
        return URL(string: base)?.host() ?? "around.events"
    }
}
