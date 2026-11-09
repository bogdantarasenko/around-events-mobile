package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.City
import events.around.shared.data.feed.FeedRepository
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.data.site.SiteRepository
import events.around.shared.domain.feed.FeedDay
import events.around.shared.domain.feed.feedCategories
import events.around.shared.domain.feed.feedDays
import events.around.shared.presentation.UiError
import events.around.shared.presentation.toUiError
import events.around.shared.presentation.today
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock

class FeedViewModel(
    private val feed: FeedRepository,
    private val site: SiteRepository,
    private val settings: SettingsRepository,
    clock: Clock,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private var loading: Job? = null
    private val error = MutableStateFlow<UiError?>(null)
    private val category = MutableStateFlow<String?>(null)

    // The stored city while the site still lists it, otherwise the first one the site lists.
    private val city: StateFlow<City?> = combine(settings.city, site.props) { stored, props ->
        val cities = props?.cities.orEmpty()
        cities.firstOrNull { it.slug == stored } ?: cities.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val listing = combine(city, feed.listings) { current, listings -> current?.let { listings[it.slug] } }

    val state: StateFlow<FeedUiState> = combine(
        combine(city, site.props, listing, ::Triple),
        refreshing,
        error,
        category,
    ) { (current, props, events), isRefreshing, lastError, selected ->
        FeedUiState(
            city = current,
            cities = props?.cities.orEmpty(),
            days = events?.let { feedDays(it, clock.today(), selected) }.orEmpty(),
            categories = events?.let(::feedCategories).orEmpty(),
            category = selected,
            // Nothing cached to show meanwhile: the first start, or a city never opened on this device.
            isLoading = events == null && (isRefreshing || lastError == null),
            isRefreshing = isRefreshing,
            isEmpty = events?.isEmpty() == true,
            error = lastError,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FeedUiState(isLoading = true))

    init {
        city.filterNotNull()
            .distinctUntilChanged()
            .onEach { selected ->
                category.value = null
                feed.restore(selected.slug)
                load(selected.slug)
            }
            .launchIn(viewModelScope)
        refreshSite()
    }

    fun refresh() {
        val current = city.value
        when {
            current == null -> refreshSite()
            loading?.isActive != true -> load(current.slug)
        }
    }

    fun selectCity(slug: String) = settings.setCity(slug)

    /** Null shows every category. */
    fun selectCategory(key: String?) {
        category.value = key
    }

    // The cities come with the page; with none cached yet, the feed has nothing to ask for until they arrive.
    private fun refreshSite() {
        viewModelScope.launch {
            try {
                site.refresh()
            } catch (exception: Exception) {
                if (site.props.value == null) error.value = exception.toUiError()
            }
        }
    }

    // A new city drops the request for the previous one: its answer would land on the wrong screen.
    private fun load(citySlug: String) {
        loading?.cancel()
        loading = viewModelScope.launch {
            refreshing.value = true
            error.value = null
            // Not in `finally`: a cancelled load would switch the spinner off under the one that replaced it.
            try {
                feed.refresh(citySlug)
                refreshing.value = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                error.value = exception.toUiError()
                refreshing.value = false
            }
        }
    }
}

data class FeedUiState(
    val city: City? = null,
    val cities: List<City> = emptyList(),
    val days: List<FeedDay> = emptyList(),
    val categories: List<String> = emptyList(),
    val category: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    /** Loaded, and the city has no upcoming events at all (not just none in the category). */
    val isEmpty: Boolean = false,
    val error: UiError? = null,
)
