package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.City
import events.around.core.model.EventOrganizer
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.feed.FeedRepository
import events.around.shared.domain.feed.CategoryCount
import events.around.shared.domain.feed.FeedDay
import events.around.shared.domain.feed.categoryCounts
import events.around.shared.domain.feed.feedDays
import events.around.shared.presentation.Refresher
import events.around.shared.presentation.UiError
import events.around.shared.presentation.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock

/**
 * The city's listing by day. With a [category] it is that category's page: its events and the organizers who
 * run them; without one, every event with a category filter on top.
 */
class EventsViewModel(
    category: String?,
    cities: CitiesRepository,
    private val feed: FeedRepository,
    clock: Clock,
) : ViewModel() {

    private val refresher = Refresher(viewModelScope)
    private val filter = MutableStateFlow<String?>(null)
    private val city = cities.current.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<EventsUiState> = combine(
        city,
        feed.listings,
        filter,
        refresher.refreshing,
        refresher.error,
    ) { current, listings, selected, isRefreshing, lastError ->
        val all = current?.let { listings[it.slug] }
        val events = if (category == null) all else all?.filter { it.category == category }
        EventsUiState(
            city = current,
            category = category,
            count = events?.size ?: 0,
            days = events?.let { feedDays(it, clock.today(), selected) }.orEmpty(),
            filters = if (category == null) events?.let(::categoryCounts).orEmpty() else emptyList(),
            filter = selected,
            organizers = events.orEmpty().mapNotNull { it.organizer }.groupBy { it.slug }.values
                .sortedByDescending { it.size }
                .map { it.first() },
            isLoading = all == null,
            isRefreshing = isRefreshing,
            error = lastError,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EventsUiState(category = category, isLoading = true))

    fun refresh() {
        val current = city.value ?: return
        if (!refresher.isActive) refresher.run { feed.refresh(current.slug) }
    }

    /** Null shows every category. */
    fun selectFilter(key: String?) {
        filter.value = key
    }
}

data class EventsUiState(
    val city: City? = null,
    /** The category page this is, or null for the whole listing. */
    val category: String? = null,
    val count: Int = 0,
    val days: List<FeedDay> = emptyList(),
    /** Category chips over the whole listing; empty on a category page. */
    val filters: List<CategoryCount> = emptyList(),
    val filter: String? = null,
    /** Organizers with a public page behind these events, the busiest first. */
    val organizers: List<EventOrganizer> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiError? = null,
)
