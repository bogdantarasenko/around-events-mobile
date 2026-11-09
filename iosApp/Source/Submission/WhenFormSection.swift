import AroundKit
import SwiftUI

struct WhenFormSection: View {
    let form: EventForm
    let viewModel: SubmissionFormViewModel
    let problem: (FormField) -> String?
    @Environment(\.strings) private var strings

    var body: some View {
        Section {
            Picker(strings.stepWhen, selection: Binding(get: { form.dayMode }, set: viewModel.setDayMode)) {
                Text(strings.formOneDay).tag(DayMode.oneDay)
                Text(strings.formMultiDay).tag(DayMode.multiDay)
                Text(strings.formRepeats).tag(DayMode.repeats)
            }
            .pickerStyle(.segmented)
            FieldRow(problem: problem(.startDate)) {
                DateRow(label: strings.formDate, value: form.startDate, onChange: viewModel.setStartDate)
            }
            .id(FormField.startDate)
            TimeRow(label: strings.formStartTime, value: form.startTime, onChange: viewModel.setStartTime)
            TimeRow(label: strings.formEndTime, value: form.endTime, onChange: viewModel.setEndTime)
            switch form.dayMode {
            case .oneDay:
                EmptyView()
            case .multiDay:
                FieldRow(problem: problem(.endDate), hint: strings.formMultiDayFooter) {
                    DateRow(label: strings.formEndDate, value: form.endDate, onChange: viewModel.setEndDate, canClear: true)
                }
                .id(FormField.endDate)
            case .repeats:
                Picker(strings.formRepeats, selection: Binding(get: { form.repeat }, set: viewModel.setRepeat)) {
                    Text(strings.formRepeatWeekly).tag(Repeat.weekly)
                    Text(strings.formRepeatBiweekly).tag(Repeat.biweekly)
                }
                FieldRow(problem: problem(.repeatUntil), hint: strings.formRepeatFooter) {
                    DateRow(label: strings.formRepeatUntil, value: form.repeatUntil, onChange: viewModel.setRepeatUntil)
                }
                .id(FormField.repeatUntil)
            }
        } header: {
            Text(strings.stepWhen)
        }
    }
}
