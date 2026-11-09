package events.around.shared.presentation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * One reload at a time for a screen, with its spinner and its error. A new [run] drops the one in flight: its
 * answer would be for a city or page no longer on screen.
 */
class Refresher(private val scope: CoroutineScope) {
    private val mutableRefreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = mutableRefreshing.asStateFlow()

    private val mutableError = MutableStateFlow<UiError?>(null)
    val error: StateFlow<UiError?> = mutableError.asStateFlow()

    private var job: Job? = null

    val isActive: Boolean get() = job?.isActive == true

    fun run(block: suspend () -> Unit) {
        job?.cancel()
        job = scope.launch {
            mutableRefreshing.value = true
            mutableError.value = null
            // Not in `finally`: a cancelled run would switch the spinner off under the one that replaced it.
            try {
                block()
                mutableRefreshing.value = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                mutableError.value = exception.toUiError()
                mutableRefreshing.value = false
            }
        }
    }
}
