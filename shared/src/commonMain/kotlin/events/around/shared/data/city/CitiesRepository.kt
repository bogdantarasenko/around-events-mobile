package events.around.shared.data.city

import com.russhwolf.settings.Settings
import events.around.core.model.City
import events.around.shared.data.cache.CachedMap
import events.around.shared.data.network.AroundApi
import events.around.shared.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

/** The cities the listing covers, kept for the next start, and the one the person reads. */
class CitiesRepository(
    private val api: AroundApi,
    private val preferences: SettingsRepository,
    settings: Settings,
) {
    private val cache = CachedMap(settings, ListSerializer(City.serializer()), prefix = "cities").apply { restore("") }

    /** Null until the first answer on this device. */
    val cities: Flow<List<City>?> = cache.values.map { it[""] }

    /** The stored city while the site still lists it, otherwise the first one listed. */
    val current: Flow<City?> = combine(preferences.city, cities) { stored, all ->
        all?.firstOrNull { it.slug == stored } ?: all?.firstOrNull()
    }

    val citiesNow: List<City>? get() = cache.values.value[""]

    /** Throws [events.around.shared.data.network.ApiException]; the cached list stays in place. */
    suspend fun refresh(): List<City> = api.cities().also { cache.put("", it) }

    fun select(slug: String) = preferences.setCity(slug)
}
