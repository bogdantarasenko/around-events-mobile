package events.around.shared.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.EventStatus
import events.around.core.model.Organizer
import events.around.core.model.OrganizerLinks
import events.around.shared.data.auth.Session
import events.around.shared.data.auth.SessionRepository
import events.around.shared.data.events.MyEventsRepository
import events.around.shared.data.network.ApiError
import events.around.shared.data.network.ApiException
import events.around.shared.data.network.OrganizerInput
import events.around.shared.data.organizer.OrganizerRepository
import events.around.shared.presentation.UiError
import events.around.shared.presentation.today
import events.around.shared.presentation.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

/** The public page `around.events/o/{slug}`: shown in the profile, created and edited from there. */
class OrganizerViewModel(
    private val repository: OrganizerRepository,
    private val events: MyEventsRepository,
    private val session: SessionRepository,
    private val clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(OrganizerUiState())
    val state: StateFlow<OrganizerUiState> = mutableState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            events.events.collect { list ->
                val today = clock.today()
                val listed = list.orEmpty().filter { it.status == EventStatus.Published }
                mutableState.update { state ->
                    state.copy(
                        upcomingCount = listed.count { it.lastDay.let { day -> day == null || day >= today } },
                        pastCount = listed.count { it.lastDay.let { day -> day != null && day < today } },
                    )
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            try {
                val organizer = repository.load()
                mutableState.update { it.copy(isLoading = false, organizer = organizer, isLoaded = true) }
            } catch (exception: Exception) {
                mutableState.update { it.copy(isLoading = false, error = exception.toUiError()) }
            }
        }
    }

    fun startEditing() {
        val organizer = mutableState.value.organizer
        val login = (session.session.value as? Session.SignedIn)?.login
        mutableState.update {
            it.copy(
                editor = OrganizerEditor(
                    name = organizer?.name ?: login?.firstName.orEmpty(),
                    slug = organizer?.slug.orEmpty(),
                    bio = organizer?.bio.orEmpty(),
                    instagram = organizer?.links?.instagram.orEmpty(),
                    telegram = organizer?.links?.telegram.orEmpty(),
                    website = organizer?.links?.website.orEmpty(),
                    isPublic = organizer?.isPublic ?: true,
                ),
                justSaved = false,
            )
        }
    }

    fun cancelEditing() = mutableState.update { it.copy(editor = null) }

    fun setName(value: String) = editor { copy(name = value) }
    fun setSlug(value: String) = editor { copy(slug = value.lowercase()) }
    fun setBio(value: String) = editor { copy(bio = value) }
    fun setInstagram(value: String) = editor { copy(instagram = value) }
    fun setTelegram(value: String) = editor { copy(telegram = value) }
    fun setWebsite(value: String) = editor { copy(website = value) }
    fun setPublic(value: Boolean) = editor { copy(isPublic = value) }

    fun save() {
        val editor = mutableState.value.editor ?: return
        if (editor.isSaving) return
        viewModelScope.launch {
            this@OrganizerViewModel.editor { copy(isSaving = true, fieldError = null, fieldMessage = null) }
            try {
                val saved = repository.save(editor.toInput())
                mutableState.update { it.copy(organizer = saved, editor = null, justSaved = true) }
            } catch (exception: Exception) {
                val rejected = (exception as? ApiException)?.error as? ApiError.Rejected
                val field = when (rejected?.field) {
                    "name" -> OrganizerField.Name
                    "slug" -> OrganizerField.Slug
                    "bio" -> OrganizerField.Bio
                    else -> null
                }
                this@OrganizerViewModel.editor {
                    copy(
                        isSaving = false,
                        fieldError = field,
                        fieldMessage = rejected?.message.takeIf { field != null },
                        error = if (field == null) exception.toUiError() else null,
                    )
                }
            }
        }
    }

    private fun editor(change: OrganizerEditor.() -> OrganizerEditor) {
        mutableState.update { state -> state.copy(editor = state.editor?.change()) }
    }

    private fun OrganizerEditor.toInput() = OrganizerInput(
        name = name.trim(),
        slug = slug.trim().ifEmpty { null },
        bio = bio.trim().ifEmpty { null },
        links = OrganizerLinks(
            instagram = instagram.trim().ifEmpty { null },
            telegram = telegram.trim().ifEmpty { null },
            website = website.trim().ifEmpty { null },
        ),
        isPublic = isPublic,
        // The backend accepts only a Telegram photo: nobody else's picture on our page.
        avatarUrl = (session.session.value as? Session.SignedIn)?.login?.photoUrl
            ?.takeIf { it.startsWith("https://t.me/") },
    )
}

data class OrganizerUiState(
    val isLoading: Boolean = false,
    /** The first load finished; [organizer] null then means no page yet. */
    val isLoaded: Boolean = false,
    val organizer: Organizer? = null,
    val upcomingCount: Int = 0,
    val pastCount: Int = 0,
    val error: UiError? = null,
    val editor: OrganizerEditor? = null,
    val justSaved: Boolean = false,
)

data class OrganizerEditor(
    val name: String,
    val slug: String,
    val bio: String,
    val instagram: String,
    val telegram: String,
    val website: String,
    val isPublic: Boolean,
    val isSaving: Boolean = false,
    val fieldError: OrganizerField? = null,
    val fieldMessage: String? = null,
    val error: UiError? = null,
)

enum class OrganizerField {
    Name,
    Slug,
    Bio,
}
