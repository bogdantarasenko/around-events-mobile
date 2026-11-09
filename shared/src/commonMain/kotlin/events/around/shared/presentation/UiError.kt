package events.around.shared.presentation

import events.around.shared.data.network.ApiError
import events.around.shared.data.network.ApiException
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

enum class ErrorKind {
    Offline,
    SignedOut,
    NotFound,
    TooMany,
    /** The server explained itself: show [UiError.message]. */
    Explained,
    Server,
    Unknown,
}

/** An error ready for the screen: a kind the UI has a string for, plus the server's own words when it gave any. */
data class UiError(
    val kind: ErrorKind,
    val message: String? = null,
    val status: Int? = null,
)

fun Throwable.toUiError(): UiError = when (val error = (this as? ApiException)?.error) {
    ApiError.Offline -> UiError(ErrorKind.Offline)
    ApiError.Unauthorized -> UiError(ErrorKind.SignedOut)
    ApiError.NotFound -> UiError(ErrorKind.NotFound)
    ApiError.TooMany -> UiError(ErrorKind.TooMany)
    is ApiError.Conflict -> explained(error.message)
    is ApiError.Rejected -> explained(error.message)
    is ApiError.Server -> UiError(ErrorKind.Server, status = error.status)
    null -> UiError(ErrorKind.Unknown)
}

private fun explained(message: String?) =
    if (message.isNullOrBlank()) UiError(ErrorKind.Unknown) else UiError(ErrorKind.Explained, message)

/** Today on the device, `yyyy-MM-dd` — the cabinet compares dates the way the person sees them. */
fun Clock.today(): String = todayIn(TimeZone.currentSystemDefault()).toString()
