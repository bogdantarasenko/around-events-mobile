import AroundKit
import SwiftUI

/// Description, entry price and links.
struct EntryFormSection: View {
    let form: EventForm
    let viewModel: SubmissionFormViewModel
    let problem: (FormField) -> String?
    @Environment(\.strings) private var strings

    var body: some View {
        Section {
            FieldRow(problem: problem(.descriptionText)) {
                TextField(
                    strings.formDescription,
                    text: Binding(get: { form.description_ }, set: viewModel.setDescription),
                    axis: .vertical
                )
                .lineLimit(4 ... 12)
            }
            .id(FormField.descriptionText)
            FieldRow(problem: problem(.pricing)) {
                Picker(strings.formPricing, selection: pricing) {
                    Text(strings.free).tag(Pricing?.some(.free))
                    Text(strings.formPaid).tag(Pricing?.some(.paid))
                    Text(strings.formUnknown).tag(Pricing?.some(.unknown))
                }
                .pickerStyle(.segmented)
            }
            .id(FormField.pricing)
            priceFields
            urlField(strings.formEventUrl, form.eventUrl, viewModel.setEventUrl)
        } header: {
            Text(strings.stepDescription)
        }
    }

    private var pricing: Binding<Pricing?> {
        Binding(get: { form.pricing }, set: { if let value = $0 { viewModel.setPricing(value: value) } })
    }

    @ViewBuilder
    private var priceFields: some View {
        switch form.pricing {
        case .paid:
            FieldRow(problem: problem(.priceFrom), hint: strings.formPricingPaidFooter) {
                TextField(strings.formPriceFrom, text: Binding(get: { form.priceFrom }, set: viewModel.setPriceFrom))
                    .keyboardType(.decimalPad)
            }
            .id(FormField.priceFrom)
            TextField(strings.formPaidNote, text: Binding(get: { form.priceNote }, set: viewModel.setPriceNote))
            urlField(strings.formTicketsUrl, form.ticketsUrl, viewModel.setTicketsUrl)
        case .free:
            TextField(strings.formFreeNote, text: Binding(get: { form.priceNote }, set: viewModel.setPriceNote))
            urlField(strings.formRegistrationUrl, form.ticketsUrl, viewModel.setTicketsUrl)
        case .unknown:
            Text(strings.formPricingUnknownFooter).font(.footnote).foregroundStyle(.secondary)
        default:
            EmptyView()
        }
    }

    private func urlField(_ label: String, _ value: String, _ set: @escaping (String) -> Void) -> some View {
        TextField(label, text: Binding(get: { value }, set: set))
            .keyboardType(.URL)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
    }
}
