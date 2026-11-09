package events.around.shared.data.cache

import com.russhwolf.settings.Settings
import events.around.shared.data.network.apiJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer

/**
 * Values by key, each also kept in settings under `<prefix><key>` so a screen shows the last answer at once and
 * offline. A key missing from [values] has nothing loaded on this device yet.
 */
class CachedMap<T>(
    private val settings: Settings,
    private val serializer: KSerializer<T>,
    private val prefix: String,
) {
    private val mutableValues = MutableStateFlow<Map<String, T>>(emptyMap())
    val values: StateFlow<Map<String, T>> = mutableValues.asStateFlow()

    /** Puts the stored value of [key] into [values], if there is one and it isn't there yet. */
    fun restore(key: String) {
        if (key in mutableValues.value) return
        val stored = settings.getStringOrNull(prefix + key)
            ?.let { runCatching { apiJson.decodeFromString(serializer, it) }.getOrNull() }
            ?: return
        mutableValues.value += key to stored
    }

    fun put(key: String, value: T) {
        settings.putString(prefix + key, apiJson.encodeToString(serializer, value))
        mutableValues.value += key to value
    }
}
