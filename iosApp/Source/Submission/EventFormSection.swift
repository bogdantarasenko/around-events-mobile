import AroundKit
import SwiftUI

struct EventFormSection: View {
    let state: SubmissionFormUiState
    let viewModel: SubmissionFormViewModel
    let problem: (FormField) -> String?
    @Environment(\.strings) private var strings

    private var form: EventForm { state.form }

    var body: some View {
        Section(strings.stepEvent) {
            if state.cities.count > 1, state.kind != .edit {
                Picker(strings.formCity, selection: Binding(get: { form.city }, set: viewModel.setCity)) {
                    ForEach(state.cities, id: \.slug) { city in
                        Text(city.name).tag(city.slug)
                    }
                }
            }
            FieldRow(problem: problem(.title)) {
                TextField(strings.formTitle, text: Binding(get: { form.title }, set: viewModel.setTitle), axis: .vertical)
            }
            .id(FormField.title)
            FieldRow(problem: problem(.category)) {
                Picker(strings.formCategory, selection: Binding(get: { form.category }, set: viewModel.setCategory)) {
                    Text(strings.formChoose).tag("")
                    ForEach(VocabularyKt.CATEGORIES, id: \.self) { key in
                        Text(Labels.category(key, strings)).tag(key)
                    }
                }
            }
            .id(FormField.category)
            if form.category == VocabularyKt.CATEGORY_OTHER {
                VStack(alignment: .leading) {
                    TextField(
                        strings.formCategorySuggestion,
                        text: Binding(get: { form.categorySuggestion }, set: viewModel.setCategorySuggestion)
                    )
                    Text(strings.formCategorySuggestionHint).font(.footnote).foregroundStyle(.secondary)
                }
            }
            languages
            // A kids' event is kids-friendly by definition: the form sends it without asking.
            if form.category != VocabularyKt.CATEGORY_KIDS {
                Toggle(strings.formWithKids, isOn: Binding(get: { form.withKids }, set: viewModel.setWithKids))
            }
        }
    }

    private var languages: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(strings.formLanguages)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    ForEach(VocabularyKt.EVENT_LANGUAGES, id: \.first) { pair in
                        let code = pair.first as String? ?? ""
                        let isOn = form.languages.contains(code)
                        Button {
                            viewModel.toggleLanguage(code: code)
                        } label: {
                            Text(pair.second as String? ?? code)
                                .font(.subheadline)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(isOn ? Color.accentColor.opacity(0.18) : Color(.secondarySystemFill), in: Capsule())
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            if form.languages.isEmpty {
                Text(strings.formLanguageAny).font(.footnote).foregroundStyle(.secondary)
            }
        }
    }
}
