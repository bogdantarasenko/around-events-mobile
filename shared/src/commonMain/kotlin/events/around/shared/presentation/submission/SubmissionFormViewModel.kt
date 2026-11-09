package events.around.shared.presentation.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.City
import events.around.core.model.Repeat
import events.around.shared.data.events.MyEventsRepository
import events.around.shared.data.network.ApiError
import events.around.shared.data.network.ApiException
import events.around.shared.data.network.PosterUpload
import events.around.shared.data.network.SubmissionReceipt
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.data.site.SiteRepository
import events.around.shared.data.submission.SubmissionRepository
import events.around.shared.domain.submission.DayMode
import events.around.shared.domain.submission.EventForm
import events.around.shared.domain.submission.FormField
import events.around.shared.domain.submission.Pricing
import events.around.shared.domain.submission.firstProblem
import events.around.shared.domain.submission.serverField
import events.around.shared.domain.submission.toForm
import events.around.shared.domain.submission.toParts
import events.around.shared.presentation.UiError
import events.around.shared.presentation.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FormKind {
    /** A new submission; the unfinished one is kept as a draft. */
    New,

    /** The same event on another date: everything but the dates comes from [FormMode.eventId]. */
    Repeat,

    /** A rejected event, sent again as a new submission with its dates kept. */
    Fix,

    /** An event still in review, changed in place. */
    Edit,
}

data class FormMode(
    val kind: FormKind,
    val eventId: Long? = null,
)

class SubmissionFormViewModel(
    private val mode: FormMode,
    private val submissions: SubmissionRepository,
    private val events: MyEventsRepository,
    private val settings: SettingsRepository,
    site: SiteRepository,
) : ViewModel(),
    SubmissionFormActions {

    private val source = mode.eventId?.let(events::find)
    private var poster: PosterUpload? = null

    private val mutableState = MutableStateFlow(initialState(site.props.value?.cities.orEmpty()))
    val state: StateFlow<SubmissionFormUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            site.props.collect { props ->
                val cities = props?.cities.orEmpty()
                mutableState.update { current ->
                    val city = current.form.city.ifEmpty { defaultCity(cities) }
                    current.copy(cities = cities, form = current.form.copy(city = city))
                }
            }
        }
    }

    override fun setCity(slug: String) = edit { copy(city = slug) }
    override fun setTitle(value: String) = edit { copy(title = value) }
    override fun setCategory(value: String) = edit { copy(category = value) }
    override fun setCategorySuggestion(value: String) = edit { copy(categorySuggestion = value) }
    override fun toggleLanguage(code: String) = edit {
        copy(languages = if (code in languages) languages - code else languages + code)
    }
    override fun setWithKids(value: Boolean) = edit { copy(withKids = value) }
    override fun setDayMode(value: DayMode) = edit { copy(dayMode = value) }
    override fun setStartDate(value: String) = edit { copy(startDate = value) }
    override fun setStartTime(value: String) = edit { copy(startTime = value) }
    override fun setEndTime(value: String) = edit { copy(endTime = value) }
    override fun setEndDate(value: String) = edit { copy(endDate = value) }
    override fun setRepeat(value: Repeat) = edit { copy(repeat = value) }
    override fun setRepeatUntil(value: String) = edit { copy(repeatUntil = value) }
    override fun setVenue(value: String) = edit { copy(venue = value) }
    override fun setAddress(value: String) = edit { copy(address = value) }
    override fun setPricing(value: Pricing) = edit { copy(pricing = value) }
    override fun setPriceFrom(value: String) = edit { copy(priceFrom = value) }
    override fun setPriceNote(value: String) = edit { copy(priceNote = value) }
    override fun setEventUrl(value: String) = edit { copy(eventUrl = value) }
    override fun setTicketsUrl(value: String) = edit { copy(ticketsUrl = value) }
    override fun setDescription(value: String) = edit { copy(description = value) }
    override fun setOrganization(value: String) = edit { copy(organization = value) }
    override fun setAcceptedRules(value: Boolean) = edit { copy(acceptedRules = value) }

    override fun pickPoster(bytes: ByteArray, fileName: String, mimeType: String) {
        poster = PosterUpload(bytes, fileName, mimeType)
        mutableState.update {
            it.copy(
                poster = PosterState(pickedName = fileName, pickedBytes = bytes.size.toLong()),
                problem = it.problem.takeUnless { field -> field == FormField.Poster },
            )
        }
    }

    /** Drops the picked file, or the inherited poster of a repeat or fix. An edit keeps its poster on the server. */
    override fun removePoster() {
        poster = null
        mutableState.update { it.copy(poster = PosterState()) }
    }

    /** Throws the restored draft away. */
    override fun startOver() {
        submissions.clearDraft()
        poster = null
        mutableState.update { initialState(it.cities, ignoreDraft = true).copy(cities = it.cities) }
    }

    override fun submit() {
        val current = mutableState.value
        if (current.isSubmitting || current.result != null) return

        val problem = current.form.firstProblem(poster?.bytes?.size?.toLong())
        if (problem != null) {
            mutableState.update { it.copy(problem = problem, serverMessage = null, error = null) }
            return
        }

        viewModelScope.launch {
            mutableState.update { it.copy(isSubmitting = true, problem = null, serverMessage = null, error = null) }
            try {
                val result = send(current)
                mutableState.update { it.copy(isSubmitting = false, result = result) }
            } catch (exception: Exception) {
                val rejected = (exception as? ApiException)?.error as? ApiError.Rejected
                val field = serverField(rejected?.field)
                mutableState.update {
                    it.copy(
                        isSubmitting = false,
                        problem = field,
                        // A field-level message shows under its field; anything else goes in a banner.
                        serverMessage = rejected?.message.takeIf { field != null },
                        error = if (field == null) exception.toUiError() else null,
                    )
                }
            }
        }
    }

    fun dismissError() = mutableState.update { it.copy(error = null) }

    private suspend fun send(current: SubmissionFormUiState): FormResult {
        val parts = current.form.toParts()
        if (mode.kind == FormKind.Edit) {
            val id = requireNotNull(mode.eventId)
            events.update(id, parts, poster)
            return FormResult(saved = true)
        }

        val posterFrom = mode.eventId.takeIf { current.poster.inheritedUrl != null }
        val receipt = submissions.submit(parts, poster, posterFrom)
        if (mode.kind == FormKind.New) submissions.clearDraft()
        current.form.organization.trim().takeIf { it.isNotEmpty() }?.let(settings::setDefaultOrganization)
        settings.setDefaultCity(current.form.city)
        // The cabinet should show the new submission when the person goes back; a failure here is harmless.
        runCatching { events.refresh() }
        return FormResult(receipt = receipt)
    }

    private fun edit(change: EventForm.() -> EventForm) {
        mutableState.update { current ->
            val form = current.form.change()
            current.copy(
                form = form,
                // Typing in the field with a problem clears it; the next submit checks again.
                problem = null,
                serverMessage = null,
            )
        }
        if (mode.kind == FormKind.New) submissions.saveDraft(mutableState.value.form)
    }

    private fun initialState(cities: List<City>, ignoreDraft: Boolean = false): SubmissionFormUiState {
        val draft = if (mode.kind == FormKind.New && !ignoreDraft) submissions.draft() else null
        val form = when {
            source != null -> source.toForm(keepDates = mode.kind != FormKind.Repeat)
            draft != null -> draft
            else -> EventForm(organization = settings.defaultOrganization.value)
        }
        return SubmissionFormUiState(
            kind = mode.kind,
            form = form.copy(city = form.city.ifEmpty { defaultCity(cities) }),
            cities = cities,
            restoredDraft = draft != null,
            rejectionReason = source?.skipReason.takeIf { mode.kind == FormKind.Fix },
            poster = PosterState(
                inheritedUrl = source?.posterUrl.takeIf { mode.kind == FormKind.Repeat || mode.kind == FormKind.Fix },
                currentUrl = source?.posterUrl.takeIf { mode.kind == FormKind.Edit },
            ),
            sourceMissing = mode.eventId != null && source == null,
        )
    }

    private fun defaultCity(cities: List<City>): String =
        settings.defaultCity.value?.takeIf { slug -> cities.isEmpty() || cities.any { it.slug == slug } }
            ?: cities.firstOrNull()?.slug.orEmpty()
}

data class SubmissionFormUiState(
    val kind: FormKind = FormKind.New,
    val form: EventForm = EventForm(),
    val cities: List<City> = emptyList(),
    val poster: PosterState = PosterState(),
    /** The field to show [serverMessage] or the local problem next to, and to scroll to. */
    val problem: FormField? = null,
    val serverMessage: String? = null,
    /** A failure that isn't about one field. */
    val error: UiError? = null,
    val isSubmitting: Boolean = false,
    val result: FormResult? = null,
    /** The form came back from an unfinished draft. */
    val restoredDraft: Boolean = false,
    /** Fix mode: why the moderator rejected it. */
    val rejectionReason: String? = null,
    /** The event to repeat, fix or edit isn't in the cabinet any more. */
    val sourceMissing: Boolean = false,
)

data class PosterState(
    /** Picked on the device and not sent yet. */
    val pickedName: String? = null,
    val pickedBytes: Long? = null,
    /** Repeat and fix: the past event's poster, reused unless another is picked. */
    val inheritedUrl: String? = null,
    /** Edit: the poster already on the event; it stays unless another is picked. */
    val currentUrl: String? = null,
)

data class FormResult(
    val receipt: SubmissionReceipt? = null,
    /** An edit went through. */
    val saved: Boolean = false,
) {
    val isDuplicate: Boolean get() = receipt?.status == "duplicate"
}
