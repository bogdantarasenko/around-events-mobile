package events.around.shared.data.feed

import com.russhwolf.settings.Settings
import events.around.core.model.Event
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.apiJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer

/**
 * Each city's listing. The last one loaded is cached per city so the feed shows something at once and offline;
 * a city missing from [listings] has nothing loaded on this device yet.
 */
class FeedRepository(
    private val api: AroundApi,
    private val settings: Settings,
) {
    private val mutableListings = MutableStateFlow<Map<String, List<Event>>>(emptyMap())
    val listings: StateFlow<Map<String, List<Event>>> = mutableListings.asStateFlow()

    /** Puts the cached listing of [citySlug] into [listings], if there is one and it isn't there yet. */
    fun restore(citySlug: String) {
        if (citySlug in mutableListings.value) return
        val cached = settings.getStringOrNull(key(citySlug))
            ?.let { runCatching { apiJson.decodeFromString(LIST, it) }.getOrNull() }
            ?: return
        mutableListings.value += citySlug to cached
    }

    /** Throws [events.around.shared.data.network.ApiException]; the cached listing stays in place. */
    suspend fun refresh(citySlug: String): List<Event> {
        val fresh = api.cityEvents(citySlug)
        settings.putString(key(citySlug), apiJson.encodeToString(LIST, fresh))
        mutableListings.value += citySlug to fresh
        return fresh
    }

    fun find(id: Long): Event? = mutableListings.value.values.firstNotNullOfOrNull { events ->
        events.firstOrNull { it.id == id }
    }

    private fun key(citySlug: String) = "feed_$citySlug"

    private companion object {
        val LIST = ListSerializer(Event.serializer())
    }
}
