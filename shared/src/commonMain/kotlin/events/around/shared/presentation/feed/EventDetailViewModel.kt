package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.Event
import events.around.shared.data.feed.FeedRepository
import events.around.shared.data.organizers.OrganizersRepository
import events.around.shared.domain.feed.nextDates
import events.around.shared.presentation.today
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock

/**
 * One event from what is loaded: a city's listing or an organizer's page. It follows them, so a refresh updates
 * it too.
 */
class EventDetailViewModel(
    private val eventId: Long,
    private val feed: FeedRepository,
    private val organizers: OrganizersRepository,
    private val clock: Clock,
) : ViewModel() {

    val state: StateFlow<EventDetailUiState> = combine(feed.listings, organizers.organizerPages) { _, _ -> current() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, current())

    private fun current(): EventDetailUiState {
        val event = feed.find(eventId) ?: organizers.findEvent(eventId)
        return EventDetailUiState(
            event = event,
            nextDates = event?.let { nextDates(it, clock.today()) }.orEmpty(),
        )
    }
}

data class EventDetailUiState(
    /** Null when the event left the listing. */
    val event: Event? = null,
    val nextDates: List<String> = emptyList(),
)
