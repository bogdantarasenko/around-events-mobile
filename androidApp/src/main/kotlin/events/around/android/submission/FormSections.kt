package events.around.android.submission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import events.around.android.BuildConfig
import events.around.android.ui.components.SectionHeader
import events.around.android.ui.components.openUrl
import events.around.android.ui.format.categoryLabel
import events.around.android.ui.strings.LocalAroundStrings
import events.around.core.model.CATEGORIES
import events.around.core.model.CATEGORY_KIDS
import events.around.core.model.CATEGORY_OTHER
import events.around.core.model.EVENT_LANGUAGES
import events.around.core.model.Repeat
import events.around.shared.domain.submission.DayMode
import events.around.shared.domain.submission.FormField
import events.around.shared.domain.submission.Pricing
import events.around.shared.presentation.submission.FormKind

@Composable
fun EventSection(
    scope: FormScope,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val form = scope.state.form
    val actions = scope.actions
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        SectionHeader(strings.stepEvent)
        if (scope.state.cities.size > 1 && scope.state.kind != FormKind.Edit) {
            Text(strings.formCity, style = MaterialTheme.typography.labelLarge)
            ChipRow(
                options = scope.state.cities.map { it.slug to it.name },
                isSelected = { it == form.city },
                onClick = actions::setCity,
            )
        }
        FormTextField(
            value = form.title,
            onValueChange = actions::setTitle,
            label = strings.formTitle,
            placeholder = strings.formTitlePlaceholder,
            error = scope.problem(FormField.Title),
            modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.Title)),
        )
        Text(strings.formCategory, style = MaterialTheme.typography.labelLarge)
        ChipRow(
            options = CATEGORIES.map { it to categoryLabel(it, strings) },
            isSelected = { it == form.category },
            onClick = actions::setCategory,
            modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.Category)),
        )
        scope.problem(FormField.Category)?.let { FieldError(it) }
        if (form.category == CATEGORY_OTHER) {
            FormTextField(
                value = form.categorySuggestion,
                onValueChange = actions::setCategorySuggestion,
                label = strings.formCategorySuggestion,
                placeholder = strings.formCategorySuggestionPlaceholder,
                hint = strings.formCategorySuggestionHint,
            )
        }
        Text(strings.formLanguages, style = MaterialTheme.typography.labelLarge)
        ChipRow(options = EVENT_LANGUAGES, isSelected = { it in form.languages }, onClick = actions::toggleLanguage)
        if (form.languages.isEmpty()) FieldHint(strings.formLanguageAny)
        // A kids' event is kids-friendly by definition: the form sends it without asking.
        if (form.category != CATEGORY_KIDS) {
            SwitchRow(strings.formWithKids, form.withKids, actions::setWithKids)
        }
    }
}

@Composable
fun PosterSection(
    scope: FormScope,
    pickedUri: String?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val poster = scope.state.poster
    val shown = pickedUri?.takeIf { poster.pickedName != null } ?: poster.inheritedUrl ?: poster.currentUrl
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.bringIntoViewRequester(scope.requester(FormField.Poster)),
    ) {
        SectionHeader(strings.formPoster)
        if (shown != null) {
            AsyncImage(model = shown, contentDescription = null, modifier = Modifier.size(160.dp))
            when {
                poster.pickedName != null -> Unit
                poster.inheritedUrl != null -> FieldHint("${strings.formPreviousPoster}. ${strings.posterInherited}")
                poster.currentUrl != null -> FieldHint("${strings.formCurrentPoster}. ${strings.posterInherited}")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPick) {
                Text(if (shown == null) strings.posterAttach else strings.posterReplace)
            }
            // An edit keeps its poster on the server: there is nothing to remove from here.
            if (poster.pickedName != null || poster.inheritedUrl != null) {
                TextButton(onClick = onRemove) { Text(strings.posterRemove) }
            }
        }
        scope.problem(FormField.Poster)?.let { FieldError(it) }
        FieldHint(strings.formPosterFooter)
    }
}

@Composable
fun WhenSection(
    scope: FormScope,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val form = scope.state.form
    val actions = scope.actions
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        SectionHeader(strings.stepWhen)
        Segmented(
            options = listOf(
                DayMode.OneDay to strings.formOneDay,
                DayMode.MultiDay to strings.formMultiDay,
                DayMode.Repeats to strings.formRepeats,
            ),
            selected = form.dayMode,
            onSelect = actions::setDayMode,
        )
        DateField(
            value = form.startDate,
            onValueChange = actions::setStartDate,
            label = strings.formDate,
            error = scope.problem(FormField.StartDate),
            modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.StartDate)),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeField(form.startTime, actions::setStartTime, strings.formStartTime, Modifier.weight(1f))
            TimeField(form.endTime, actions::setEndTime, strings.formEndTime, Modifier.weight(1f))
        }
        when (form.dayMode) {
            DayMode.OneDay -> Unit
            DayMode.MultiDay -> {
                DateField(
                    value = form.endDate,
                    onValueChange = actions::setEndDate,
                    label = strings.formEndDate,
                    error = scope.problem(FormField.EndDate),
                    modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.EndDate)),
                )
                FieldHint(strings.formMultiDayFooter)
            }
            DayMode.Repeats -> {
                ChipRow(
                    options = listOf(
                        Repeat.Weekly to strings.formRepeatWeekly,
                        Repeat.Biweekly to strings.formRepeatBiweekly,
                    ),
                    isSelected = { it == form.repeat },
                    onClick = actions::setRepeat,
                )
                DateField(
                    value = form.repeatUntil,
                    onValueChange = actions::setRepeatUntil,
                    label = strings.formRepeatUntil,
                    error = scope.problem(FormField.RepeatUntil),
                    modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.RepeatUntil)),
                )
                FieldHint(strings.formRepeatFooter)
            }
        }
    }
}

@Composable
fun WhereSection(
    scope: FormScope,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val form = scope.state.form
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        SectionHeader(strings.stepWhere)
        FormTextField(
            value = form.venue,
            onValueChange = scope.actions::setVenue,
            label = strings.formVenue,
            placeholder = strings.formVenuePlaceholder,
            error = scope.problem(FormField.Venue),
            modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.Venue)),
        )
        FormTextField(
            value = form.address,
            onValueChange = scope.actions::setAddress,
            label = strings.formAddress,
            hint = strings.formAddressHint,
        )
    }
}

@Composable
fun DescriptionSection(
    scope: FormScope,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val form = scope.state.form
    val actions = scope.actions
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        SectionHeader(strings.stepDescription)
        FormTextField(
            value = form.description,
            onValueChange = actions::setDescription,
            label = strings.formDescription,
            placeholder = strings.formDescriptionPlaceholder,
            error = scope.problem(FormField.DescriptionText),
            singleLine = false,
            minLines = 4,
            modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.DescriptionText)),
        )
        Text(strings.formPricing, style = MaterialTheme.typography.labelLarge)
        Segmented(
            options = listOf(
                Pricing.Free to strings.free,
                Pricing.Paid to strings.formPaid,
                Pricing.Unknown to strings.formUnknown,
            ),
            selected = form.pricing,
            onSelect = actions::setPricing,
            modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.Pricing)),
        )
        scope.problem(FormField.Pricing)?.let { FieldError(it) }
        when (form.pricing) {
            Pricing.Paid -> {
                FormTextField(
                    value = form.priceFrom,
                    onValueChange = actions::setPriceFrom,
                    label = strings.formPriceFrom,
                    hint = strings.formPricingPaidFooter,
                    error = scope.problem(FormField.PriceFrom),
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.PriceFrom)),
                )
                FormTextField(
                    form.priceNote,
                    actions::setPriceNote,
                    strings.formPaidNote,
                    placeholder = strings.formPaidNotePlaceholder,
                )
                UrlField(
                    form.ticketsUrl,
                    actions::setTicketsUrl,
                    strings.formTicketsUrl,
                    strings.formTicketsPlaceholder,
                )
            }
            Pricing.Free -> {
                FormTextField(
                    form.priceNote,
                    actions::setPriceNote,
                    strings.formFreeNote,
                    placeholder = strings.formFreeNotePlaceholder,
                )
                UrlField(
                    form.ticketsUrl,
                    actions::setTicketsUrl,
                    strings.formRegistrationUrl,
                    strings.formRegistrationPlaceholder,
                )
            }
            Pricing.Unknown -> FieldHint(strings.formPricingUnknownFooter)
            null -> Unit
        }
        UrlField(form.eventUrl, actions::setEventUrl, strings.formEventUrl, strings.formEventUrlPlaceholder)
    }
}

@Composable
fun SendSection(
    scope: FormScope,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAroundStrings.current
    val context = LocalContext.current
    val form = scope.state.form
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        SectionHeader(strings.stepSend)
        FormTextField(
            value = form.organization,
            onValueChange = scope.actions::setOrganization,
            label = strings.formOrganization,
            placeholder = strings.formOrganizationPlaceholder,
            hint = strings.formOrganizationHint,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.bringIntoViewRequester(scope.requester(FormField.AcceptedRules)),
        ) {
            Checkbox(checked = form.acceptedRules, onCheckedChange = scope.actions::setAcceptedRules)
            Column(Modifier.weight(1f)) {
                Text(
                    "${strings.formAccept} ${strings.formRulesLink} ${strings.formAcceptRest}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = { context.openUrl(BuildConfig.BASE_URL.trimEnd('/') + "/rules") }) {
                    Text(strings.settingsRules)
                }
            }
        }
        scope.problem(FormField.AcceptedRules)?.let { FieldError(it) }
    }
}

@Composable
private fun UrlField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
) {
    FormTextField(value, onValueChange, label, placeholder = placeholder, keyboardType = KeyboardType.Uri)
}

@Composable
private fun <T> ChipRow(
    options: List<Pair<T, String>>,
    isSelected: (T) -> Boolean,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        options.forEach { (value, label) ->
            FilterChip(selected = isSelected(value), onClick = { onClick(value) }, label = { Text(label) })
        }
    }
}

@Composable
private fun <T> Segmented(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = selected == value,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label, maxLines = 1) }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
