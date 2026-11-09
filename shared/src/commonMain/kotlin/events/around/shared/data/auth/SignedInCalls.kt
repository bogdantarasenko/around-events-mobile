package events.around.shared.data.auth

import events.around.shared.data.network.ApiError
import events.around.shared.data.network.ApiException

/** Runs a signed request; a 401 ends the session so the UI returns to sign-in instead of showing an error. */
class SignedInCalls(
    private val session: SessionRepository,
) {
    suspend fun <T> run(block: suspend () -> T): T = try {
        block()
    } catch (exception: ApiException) {
        if (exception.error == ApiError.Unauthorized) session.onRejected()
        throw exception
    }
}
