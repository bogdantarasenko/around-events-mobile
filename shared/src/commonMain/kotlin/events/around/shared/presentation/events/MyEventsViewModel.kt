package events.around.shared.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.EventStatus
import events.around.shared.data.events.MyEventsRepository
import events.around.shared.domain.events.EventGroup
import events.around.shared.domain.events.groupEvents
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

class MyEventsViewModel(
    private val repository: MyEventsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private val error = MutableStateFlow<UiError?>(null)
    private val filter = MutableStateFlow<EventStatus?>(null)

    val state: StateFlow<MyEventsUiState> = combine(
        repository.events,
        refreshing,
        error,
        filter,
    ) { events, isRefreshing, lastError, statusFilter ->
        MyEventsUiState(
            groups = events?.let { groupEvents(it, clock.today(), statusFilter) }.orEmpty(),
            // The first load on this device: nothing cached to show meanwhile.
            isLoading = events == null && isRefreshing,
            isRefreshing = isRefreshing,
            isEmpty = events?.isEmpty() == true,
            error = lastError,
            filter = statusFilter,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MyEventsUiState())

    init {
        refresh()
    }

    fun refresh() {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            error.value = null
            try {
                repository.refresh()
            } catch (exception: Exception) {
                error.value = exception.toUiError()
            } finally {
                refreshing.value = false
            }
        }
    }

    /** Null shows every status. */
    fun setFilter(status: EventStatus?) {
        filter.value = status
    }

    fun dismissError() {
        error.value = null
    }
}

data class MyEventsUiState(
    val groups: List<EventGroup> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    /** Loaded, and there are no events at all (not just none under the filter). */
    val isEmpty: Boolean = false,
    val error: UiError? = null,
    val filter: EventStatus? = null,
)
