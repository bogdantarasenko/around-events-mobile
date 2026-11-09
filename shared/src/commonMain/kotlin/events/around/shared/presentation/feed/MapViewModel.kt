package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.City
import events.around.core.model.Event
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.feed.FeedRepository
import events.around.shared.domain.feed.Period
import events.around.shared.domain.feed.feedDays
import events.around.shared.domain.feed.isDuring
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
    private val period = MutableStateFlow(Period.All)

    val state: StateFlow<MapUiState> = combine(
        cities.current,
        feed.listings,
        selection,
        period,
    ) { city, listings, selected, chosen ->
        val today = clock.today()
        val all = city?.let { listings[it.slug] }.orEmpty().filter { isDuring(it, chosen, today) }
        val events = feedDays(all.filter { it.hasLocation }, today).flatMap { it.events }.distinctBy { it.id }
        MapUiState(
            city = city,
            events = events,
            total = all.size,
            period = chosen,
            sheet = selected?.let { ids -> events.filter { it.id in ids } } ?: events,
            selected = selected.orEmpty(),
            isSelection = selected != null,
            unlocated = all.size - events.size,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MapUiState())

    /** Today, tomorrow, the weekend or everything; a new period drops the tapped place. */
    fun selectPeriod(value: Period) {
        period.value = value
        selection.value = null
    }

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
    /** Events with a point during [period], in feed order. */
    val events: List<Event> = emptyList(),
    /** Every event during [period], with a point or not: "33 of 86 on the map". */
    val total: Int = 0,
    val period: Period = Period.All,
    /** What the sheet lists: the tapped place's events, or every event on the map. */
    val sheet: List<Event> = emptyList(),
    /** The tapped place's events, highlighted on the map. */
    val selected: List<Long> = emptyList(),
    val isSelection: Boolean = false,
    /** Events left off the map because the backend has no point for them yet. */
    val unlocated: Int = 0,
)
