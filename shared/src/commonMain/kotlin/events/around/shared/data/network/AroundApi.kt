package events.around.shared.data.network

import events.around.core.model.City
import events.around.core.model.Event
import events.around.core.model.Organizer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.Serializable

/**
 * The endpoints the app uses — the public ones in `around-events`; see `routes/web.php`.
 * Every call throws [ApiException] on failure.
 */
class AroundApi(
    private val client: HttpClient,
) {
    suspend fun cities(): List<City> = get("api/cities")

    suspend fun cityEvents(citySlug: String): List<Event> = get("api/cities/$citySlug/events")

    suspend fun cityOrganizers(citySlug: String): List<Organizer> = get("api/cities/$citySlug/organizers")

    suspend fun organizer(slug: String): Organizer = get("api/organizers/$slug")

    private suspend inline fun <reified T> get(path: String): T = call { client.get(path) }.body<DataEnvelope<T>>().data

    private suspend fun call(request: suspend () -> HttpResponse): HttpResponse {
        val response = try {
            request()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            throw ApiException(ApiError.Offline)
        }
        if (response.status.isSuccess()) return response
        throw ApiException(errorOf(response.status.value))
    }
}

internal fun errorOf(status: Int): ApiError = when (status) {
    HttpStatusCode.NotFound.value -> ApiError.NotFound
    HttpStatusCode.TooManyRequests.value -> ApiError.TooMany
    else -> ApiError.Server(status)
}

@Serializable
internal data class DataEnvelope<T>(val data: T)
