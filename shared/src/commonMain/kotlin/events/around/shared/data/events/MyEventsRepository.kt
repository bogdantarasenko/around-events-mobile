package events.around.shared.data.events

import com.russhwolf.settings.Settings
import events.around.core.model.MyEvent
import events.around.shared.data.auth.SignedInCalls
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.PosterUpload
import events.around.shared.data.network.apiJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer

/**
 * The organizer's events. The last list is cached so the screen shows something at once and offline;
 * null means nothing has been loaded on this device yet.
 */
class MyEventsRepository(
    private val api: AroundApi,
    private val calls: SignedInCalls,
    private val settings: Settings,
) {
    private val mutableEvents = MutableStateFlow(cached())
    val events: StateFlow<List<MyEvent>?> = mutableEvents.asStateFlow()

    suspend fun refresh(): List<MyEvent> {
        val fresh = calls.run { api.myEvents() }
        store(fresh)
        return fresh
    }

    suspend fun update(id: Long, parts: List<Pair<String, String>>, poster: PosterUpload?): MyEvent {
        val updated = calls.run { api.updateEvent(id, parts, poster) }
        replace(updated)
        return updated
    }

    suspend fun cancel(id: Long): MyEvent {
        val cancelled = calls.run { api.cancelEvent(id) }
        replace(cancelled)
        return cancelled
    }

    fun find(id: Long): MyEvent? = mutableEvents.value?.firstOrNull { it.id == id }

    /** On sign-out: another account must not see these. */
    fun clear() {
        settings.remove(KEY)
        mutableEvents.value = null
    }

    private fun replace(event: MyEvent) {
        val current = mutableEvents.value.orEmpty()
        val next = if (current.any { it.id == event.id }) {
            current.map { if (it.id == event.id) event else it }
        } else {
            listOf(event) + current
        }
        store(next)
    }

    private fun store(events: List<MyEvent>) {
        settings.putString(KEY, apiJson.encodeToString(LIST, events))
        mutableEvents.value = events
    }

    private fun cached(): List<MyEvent>? = settings.getStringOrNull(KEY)
        ?.let { runCatching { apiJson.decodeFromString(LIST, it) }.getOrNull() }

    private companion object {
        const val KEY = "my_events"
        val LIST = ListSerializer(MyEvent.serializer())
    }
}
