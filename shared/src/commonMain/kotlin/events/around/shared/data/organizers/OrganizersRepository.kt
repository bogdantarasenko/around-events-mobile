package events.around.shared.data.organizers

import com.russhwolf.settings.Settings
import events.around.core.model.Event
import events.around.core.model.Organizer
import events.around.shared.data.cache.CachedMap
import events.around.shared.data.network.AroundApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.builtins.ListSerializer

/** Organizers with a public page: each city's list and each organizer's page, all cached. */
class OrganizersRepository(
    private val api: AroundApi,
    settings: Settings,
) {
    private val byCity = CachedMap(settings, ListSerializer(Organizer.serializer()), prefix = "organizers_")
    private val pages = CachedMap(settings, Organizer.serializer(), prefix = "organizer_")

    val cityOrganizers: StateFlow<Map<String, List<Organizer>>> = byCity.values
    val organizerPages: StateFlow<Map<String, Organizer>> = pages.values

    fun restoreCity(citySlug: String) = byCity.restore(citySlug)

    fun restorePage(slug: String) = pages.restore(slug)

    suspend fun refreshCity(citySlug: String): List<Organizer> =
        api.cityOrganizers(citySlug).also { byCity.put(citySlug, it) }

    suspend fun refreshPage(slug: String): Organizer = api.organizer(slug).also { pages.put(slug, it) }

    /** An event opened from an organizer's page may be in no loaded city listing. */
    fun findEvent(id: Long): Event? = pages.values.value.values.firstNotNullOfOrNull { page ->
        page.events.firstOrNull { it.id == id }
    }
}
