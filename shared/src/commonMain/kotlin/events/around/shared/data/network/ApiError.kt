package events.around.shared.data.network

/** Why a request failed, in the terms the UI needs to explain it. */
sealed interface ApiError {
    /** No answer at all: offline, DNS, timeout. */
    data object Offline : ApiError

    /** 404: the city is gone. */
    data object NotFound : ApiError

    /** 429: the rate limit. */
    data object TooMany : ApiError

    data class Server(val status: Int) : ApiError
}

class ApiException(val error: ApiError) : Exception(error.toString())
