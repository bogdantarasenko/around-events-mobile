package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.City
import events.around.core.model.Event
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.feed.FeedRepository
import events.around.shared.domain.feed.feedDays
import events.around.shared.presentation.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock

/**
 * The city's events on a map. Only those the backend found a point for; the sheet lists the events at the
 * tapped place, or all of them.
 */
class MapViewModel(
    cities: CitiesRepository,
    feed: FeedRepository,
    clock: Clock,
) : ViewModel() {

    private val selection = MutableStateFlow<List<Long>?>(null)

    val state: StateFlow<MapUiState> = combine(cities.current, feed.listings, selection) { city, listings, selected ->
        val all = city?.let { listings[it.slug] }.orEmpty()
        val events = feedDays(all.filter { it.hasLocation }, clock.today()).flatMap { it.events }.distinctBy { it.id }
        MapUiState(
            city = city,
            events = events,
            sheet = selected?.let { ids -> events.filter { it.id in ids } } ?: events,
            isSelection = selected != null,
            unlocated = all.size - events.size,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MapUiState())

    /** The events behind a tapped pin or cluster. */
    fun select(ids: List<Long>) {
        selection.value = ids
    }

    fun clearSelection() {
        selection.value = null
    }
}

data class MapUiState(
    val city: City? = null,
    /** Events with a point, in feed order. */
    val events: List<Event> = emptyList(),
    /** What the sheet lists: the tapped place's events, or every event on the map. */
    val sheet: List<Event> = emptyList(),
    val isSelection: Boolean = false,
    /** Events left off the map because the backend has no point for them yet. */
    val unlocated: Int = 0,
)
