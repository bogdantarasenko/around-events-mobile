package events.around.shared.data.network

/** Why a request failed, in the terms the UI needs to explain it. */
sealed interface ApiError {
    /** No answer at all: offline, DNS, timeout. */
    data object Offline : ApiError

    /** 401: the Telegram signature is missing, expired or doesn't match. */
    data object Unauthorized : ApiError

    /** 404: not this person's event, or it is gone. */
    data object NotFound : ApiError

    /** 409: the event can no longer be edited or cancelled; [message] comes from the server. */
    data class Conflict(val message: String?) : ApiError

    /** 422: validation or a rejection on the merits, with the server field when it is about one. */
    data class Rejected(val message: String?, val field: String?) : ApiError

    /** 429: the rate limit. */
    data object TooMany : ApiError

    data class Server(val status: Int) : ApiError
}

class ApiException(val error: ApiError) : Exception(error.toString())
