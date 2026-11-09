import AroundKit
import SwiftUI

struct WhereFormSection: View {
    let form: EventForm
    let viewModel: SubmissionFormViewModel
    let problem: (FormField) -> String?
    @Environment(\.strings) private var strings

    var body: some View {
        Section {
            FieldRow(problem: problem(.venue)) {
                TextField(strings.formVenue, text: Binding(get: { form.venue }, set: viewModel.setVenue))
            }
            .id(FormField.venue)
            FieldRow(problem: nil, hint: strings.formAddressHint) {
                TextField(strings.formAddress, text: Binding(get: { form.address }, set: viewModel.setAddress))
            }
        } header: {
            Text(strings.stepWhere)
        }
    }
}
