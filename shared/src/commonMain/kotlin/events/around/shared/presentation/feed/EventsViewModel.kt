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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock

/**
 * A city's listing by day: the chosen city's, or [citySlug]'s when a city page asks for another one. With a
 * [category] it is that category's page: its events and the organizers who run them; without one, every event
 * with a category filter on top.
 */
class EventsViewModel(
    category: String?,
    private val citySlug: String?,
    private val cities: CitiesRepository,
    private val feed: FeedRepository,
    clock: Clock,
) : ViewModel() {

    private val refresher = Refresher(viewModelScope)
    private val currentCity = cities.current.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val filter = MutableStateFlow<String?>(null)
    private val city = (
        if (citySlug == null) cities.current else cities.cities.map { all -> all?.firstOrNull { it.slug == citySlug } }
        ).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<EventsUiState> = combine(
        combine(city, currentCity) { shown, chosen -> shown to chosen?.slug },
        feed.listings,
        filter,
        refresher.refreshing,
        refresher.error,
    ) { (current, currentSlug), listings, selected, isRefreshing, lastError ->
        val all = current?.let { listings[it.slug] }
        val events = if (category == null) all else all?.filter { it.category == category }
        EventsUiState(
            city = current,
            category = category,
            isCurrentCity = citySlug == null || current?.slug == currentSlug,
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

    // Another city's listing is loaded on demand: Discover only keeps the chosen one fresh.
    init {
        if (citySlug != null) {
            feed.restore(citySlug)
            refresher.run { feed.refresh(citySlug) }
        }
    }

    /** On a city page: make it the city the app reads. */
    fun makeCurrent() {
        citySlug?.let(cities::select)
    }

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
    /** False on the page of a city other than the one the app reads. */
    val isCurrentCity: Boolean = true,
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
