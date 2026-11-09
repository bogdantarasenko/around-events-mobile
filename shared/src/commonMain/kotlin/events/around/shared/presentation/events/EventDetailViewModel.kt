package events.around.shared.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.MyEvent
import events.around.shared.data.events.MyEventsRepository
import events.around.shared.domain.events.nextDates
import events.around.shared.presentation.UiError
import events.around.shared.presentation.today
import events.around.shared.presentation.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

/** One event from the cabinet. It reads from the cached list, so it opens instantly and offline. */
class EventDetailViewModel(
    private val eventId: Long,
    private val repository: MyEventsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val cancelling = MutableStateFlow(false)
    private val error = MutableStateFlow<UiError?>(null)

    val state: StateFlow<EventDetailUiState> = combine(
        repository.events,
        cancelling,
        error,
    ) { events, isCancelling, lastError ->
        val event = events?.firstOrNull { it.id == eventId }
        EventDetailUiState(
            event = event,
            nextDates = event?.let { nextDates(it, clock.today()) }.orEmpty(),
            isCancelling = isCancelling,
            error = lastError,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EventDetailUiState(event = repository.find(eventId)))

    /** Irreversible; the UI asks first. */
    fun cancel() {
        if (cancelling.value) return
        viewModelScope.launch {
            cancelling.value = true
            error.value = null
            try {
                repository.cancel(eventId)
            } catch (exception: Exception) {
                error.value = exception.toUiError()
                // A 409 means the event changed elsewhere (published, cancelled): show its current state.
                runCatching { repository.refresh() }
            } finally {
                cancelling.value = false
            }
        }
    }

    fun dismissError() {
        error.value = null
    }
}

data class EventDetailUiState(
    val event: MyEvent? = null,
    val nextDates: List<String> = emptyList(),
    val isCancelling: Boolean = false,
    val error: UiError? = null,
)
