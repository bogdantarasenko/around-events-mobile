package events.around.shared.data.site

import com.russhwolf.settings.Settings
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.SiteProps
import events.around.shared.data.network.apiJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Cities and the bot id, read from the site and kept for the next start so the app works offline. */
class SiteRepository(
    private val api: AroundApi,
    private val settings: Settings,
) {
    private val mutableProps = MutableStateFlow(cached())
    val props: StateFlow<SiteProps?> = mutableProps.asStateFlow()

    /** Throws [events.around.shared.data.network.ApiException]; the cached value stays in place. */
    suspend fun refresh(): SiteProps {
        val fresh = api.siteProps()
        settings.putString(KEY, apiJson.encodeToString(SiteProps.serializer(), fresh))
        mutableProps.value = fresh
        return fresh
    }

    private fun cached(): SiteProps? = settings.getStringOrNull(KEY)
        ?.let { runCatching { apiJson.decodeFromString(SiteProps.serializer(), it) }.getOrNull() }

    private companion object {
        const val KEY = "site_props"
    }
}
