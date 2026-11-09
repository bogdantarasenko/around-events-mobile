package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.City
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.feed.FeedRepository
import events.around.shared.domain.feed.FeedDay
import events.around.shared.domain.feed.feedDays
import events.around.shared.domain.feed.searchEvents
import events.around.shared.presentation.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock

/** Search in the city's loaded listing: no request per keystroke, and it works offline. */
class SearchViewModel(
    cities: CitiesRepository,
    feed: FeedRepository,
    clock: Clock,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val state: StateFlow<SearchUiState> = combine(cities.current, feed.listings, query) { city, listings, text ->
        val found = searchEvents(city?.let { listings[it.slug] }.orEmpty(), text)
        SearchUiState(
            city = city,
            query = text,
            days = feedDays(found, clock.today()),
            isBlank = text.isBlank(),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState())

    fun setQuery(text: String) {
        query.value = text
    }
}

data class SearchUiState(
    val city: City? = null,
    val query: String = "",
    val days: List<FeedDay> = emptyList(),
    /** Nothing typed yet: the screen shows a hint rather than "nothing found". */
    val isBlank: Boolean = true,
)
