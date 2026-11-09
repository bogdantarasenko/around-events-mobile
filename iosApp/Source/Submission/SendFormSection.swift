import AroundKit
import SwiftUI

/// Who runs it, the rules, and the send button.
struct SendFormSection: View {
    let state: SubmissionFormUiState
    let viewModel: SubmissionFormViewModel
    let problem: (FormField) -> String?
    @Environment(\.strings) private var strings
    @Environment(\.openURL) private var openURL

    var body: some View {
        Section {
            FieldRow(problem: nil, hint: strings.formOrganizationHint) {
                TextField(
                    strings.formOrganization,
                    text: Binding(get: { state.form.organization }, set: viewModel.setOrganization)
                )
            }
            FieldRow(problem: problem(.acceptedRules)) {
                Toggle(isOn: Binding(get: { state.form.acceptedRules }, set: viewModel.setAcceptedRules)) {
                    Text("\(strings.formAccept) \(strings.formRulesLink) \(strings.formAcceptRest)")
                        .font(.subheadline)
                }
            }
            .id(FormField.acceptedRules)
            Button(strings.settingsRules) {
                if let url = rulesURL { openURL(url) }
            }
        } header: {
            Text(strings.stepSend)
        }

        Section {
            if let error = state.error {
                ErrorBanner(message: Labels.error(error, strings))
            }
            Button {
                viewModel.submit()
            } label: {
                if state.isSubmitting {
                    ProgressView().frame(maxWidth: .infinity)
                } else {
                    Text(state.kind == .edit ? strings.formSave : strings.formSend)
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                }
            }
            .disabled(state.isSubmitting)
        } footer: {
            if state.kind != .edit {
                Text(strings.formSendHint)
            }
        }
    }

    private var rulesURL: URL? {
        let base = Bundle.main.infoDictionary?["AroundBaseURL"] as? String ?? "https://around.events"
        return URL(string: base.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/rules")
    }
}
