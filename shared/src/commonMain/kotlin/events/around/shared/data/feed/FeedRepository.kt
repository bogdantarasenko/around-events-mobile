package events.around.shared.data.feed

import com.russhwolf.settings.Settings
import events.around.core.model.Event
import events.around.shared.data.cache.CachedMap
import events.around.shared.data.network.AroundApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.builtins.ListSerializer

/** Each city's listing, cached per city. */
class FeedRepository(
    private val api: AroundApi,
    settings: Settings,
) {
    private val cache = CachedMap(settings, ListSerializer(Event.serializer()), prefix = "feed_")
    val listings: StateFlow<Map<String, List<Event>>> = cache.values

    fun restore(citySlug: String) = cache.restore(citySlug)

    /** Throws [events.around.shared.data.network.ApiException]; the cached listing stays in place. */
    suspend fun refresh(citySlug: String): List<Event> = api.cityEvents(citySlug).also { cache.put(citySlug, it) }

    fun find(id: Long): Event? = listings.value.values.firstNotNullOfOrNull { events ->
        events.firstOrNull { it.id == id }
    }
}
