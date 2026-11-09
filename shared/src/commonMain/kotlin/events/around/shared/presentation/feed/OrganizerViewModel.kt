package events.around.shared.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import events.around.core.model.Organizer
import events.around.shared.data.organizers.OrganizersRepository
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

/** An organizer's public page: who they are, how to follow them, and their events by day. */
class OrganizerViewModel(
    private val slug: String,
    private val organizers: OrganizersRepository,
    clock: Clock,
) : ViewModel() {

    private val refresher = Refresher(viewModelScope)
    private val filter = MutableStateFlow<String?>(null)

    val state: StateFlow<OrganizerUiState> = combine(
        organizers.organizerPages,
        filter,
        refresher.refreshing,
        refresher.error,
    ) { pages, selected, isRefreshing, lastError ->
        val page = pages[slug]
        OrganizerUiState(
            organizer = page,
            days = page?.let { feedDays(it.events, clock.today(), selected) }.orEmpty(),
            filters = page?.let { categoryCounts(it.events) }.orEmpty(),
            filter = selected,
            isLoading = page == null && (isRefreshing || lastError == null),
            isRefreshing = isRefreshing,
            error = lastError,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, OrganizerUiState(isLoading = true))

    init {
        organizers.restorePage(slug)
        refresh()
    }

    fun refresh() {
        if (!refresher.isActive) refresher.run { organizers.refreshPage(slug) }
    }

    /** Null shows every category. */
    fun selectFilter(key: String?) {
        filter.value = key
    }
}

data class OrganizerUiState(
    val organizer: Organizer? = null,
    val days: List<FeedDay> = emptyList(),
    val filters: List<CategoryCount> = emptyList(),
    val filter: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    /** With [organizer] null: the page is gone or never loaded. */
    val error: UiError? = null,
)
