package events.around.shared.presentation

import events.around.shared.data.network.ApiError
import events.around.shared.data.network.ApiException
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

enum class ErrorKind {
    Offline,
    NotFound,
    TooMany,
    Server,
    Unknown,
}

/** An error ready for the screen: a kind the UI has a string for. */
data class UiError(
    val kind: ErrorKind,
    val status: Int? = null,
)

fun Throwable.toUiError(): UiError = when (val error = (this as? ApiException)?.error) {
    ApiError.Offline -> UiError(ErrorKind.Offline)
    ApiError.NotFound -> UiError(ErrorKind.NotFound)
    ApiError.TooMany -> UiError(ErrorKind.TooMany)
    is ApiError.Server -> UiError(ErrorKind.Server, status = error.status)
    null -> UiError(ErrorKind.Unknown)
}

/** Today on the device, `yyyy-MM-dd` — the feed compares dates the way the person sees them. */
fun Clock.today(): String = todayIn(TimeZone.currentSystemDefault()).toString()
