package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.City
import events.around.core.model.Event
import events.around.core.model.Organizer
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.feed.FeedRepository
import events.around.shared.data.organizers.OrganizersRepository
import events.around.shared.domain.feed.CategoryCount
import events.around.shared.domain.feed.categoryCounts
import events.around.shared.domain.feed.upcomingEvents
import events.around.shared.presentation.Refresher
import events.around.shared.presentation.UiError
import events.around.shared.presentation.today
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * The home screen: the city's next events, its categories, the other cities and the organizers. It also loads
 * the city's listing for every other screen, so it lives as long as the app.
 */
class DiscoverViewModel(
    private val cities: CitiesRepository,
    private val feed: FeedRepository,
    private val organizers: OrganizersRepository,
    clock: Clock,
) : ViewModel() {

    private val refresher = Refresher(viewModelScope)
    private val city = cities.current.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val content = combine(city, feed.listings, organizers.cityOrganizers) { current, listings, byCity ->
        Triple(current, current?.let { listings[it.slug] }, current?.let { byCity[it.slug] })
    }

    val state: StateFlow<DiscoverUiState> = combine(
        content,
        cities.cities,
        refresher.refreshing,
        refresher.error,
    ) { (current, events, cityOrganizers), allCities, isRefreshing, lastError ->
        DiscoverUiState(
            city = current,
            cities = allCities.orEmpty(),
            upcoming = events?.let { upcomingEvents(it, clock.today(), UPCOMING_COUNT) }.orEmpty(),
            categories = events?.let(::categoryCounts).orEmpty(),
            organizers = cityOrganizers.orEmpty(),
            // Nothing cached to show meanwhile: the first start, or a city never opened on this device.
            isLoading = events == null && (isRefreshing || lastError == null),
            isRefreshing = isRefreshing,
            isEmpty = events?.isEmpty() == true,
            error = lastError,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DiscoverUiState(isLoading = true))

    init {
        city.filterNotNull()
            .map { it.slug }
            .distinctUntilChanged()
            .onEach { slug ->
                feed.restore(slug)
                organizers.restoreCity(slug)
                load(slug)
            }
            .launchIn(viewModelScope)
        refreshCities()
    }

    fun refresh() {
        val current = city.value
        if (current == null) {
            refreshCities()
        } else if (!refresher.isActive) {
            load(current.slug)
        }
    }

    fun selectCity(slug: String) = cities.select(slug)

    // With no city cached yet the screen has nothing to ask for until the list arrives.
    private fun refreshCities() {
        if (cities.citiesNow == null) {
            refresher.run { cities.refresh() }
        } else {
            viewModelScope.launch { runCatching { cities.refresh() } }
        }
    }

    // The organizers are extra: the screen stands without them, so their failure isn't the screen's error.
    private fun load(citySlug: String) = refresher.run {
        coroutineScope {
            val extra = async { runCatching { organizers.refreshCity(citySlug) } }
            feed.refresh(citySlug)
            extra.await()
        }
    }

    private companion object {
        const val UPCOMING_COUNT = 9
    }
}

data class DiscoverUiState(
    val city: City? = null,
    val cities: List<City> = emptyList(),
    /** The shelf of next events: columns of three on screen. */
    val upcoming: List<Event> = emptyList(),
    val categories: List<CategoryCount> = emptyList(),
    val organizers: List<Organizer> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    /** Loaded, and the city has no upcoming events at all. */
    val isEmpty: Boolean = false,
    val error: UiError? = null,
)
