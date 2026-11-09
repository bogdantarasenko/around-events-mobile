package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.Event
import events.around.shared.data.feed.FeedRepository
import events.around.shared.domain.feed.nextDates
import events.around.shared.presentation.today
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock

/** One event from the loaded listing; it follows the listing, so a refresh updates it too. */
class EventDetailViewModel(
    private val eventId: Long,
    feed: FeedRepository,
    private val clock: Clock,
) : ViewModel() {

    val state: StateFlow<EventDetailUiState> = feed.listings
        .map { stateOf(feed.find(eventId)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, stateOf(feed.find(eventId)))

    private fun stateOf(event: Event?) = EventDetailUiState(
        event = event,
        nextDates = event?.let { nextDates(it, clock.today()) }.orEmpty(),
    )
}

data class EventDetailUiState(
    /** Null when the event left the listing. */
    val event: Event? = null,
    val nextDates: List<String> = emptyList(),
)
