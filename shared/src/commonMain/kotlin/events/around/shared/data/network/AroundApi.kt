package events.around.shared.data.network

import events.around.core.model.City
import events.around.core.model.Event
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * The endpoints the app uses — public ones in `around-events`; see `routes/web.php`.
 * Every call throws [ApiException] on failure.
 */
class AroundApi(
    private val client: HttpClient,
) {
    suspend fun cityEvents(citySlug: String): List<Event> =
        call { client.get("api/cities/$citySlug/events") }.body<DataEnvelope<List<Event>>>().data

    /**
     * The cities. There is no JSON endpoint for them: the mini-app page embeds them as Inertia props in
     * `<script data-page="app" type="application/json">`, so the app reads that page like a browser would.
     */
    suspend fun siteProps(): SiteProps =
        parseSiteProps(call { client.get("") { headers[HttpHeaders.Accept] = "text/html" } }.bodyAsText())

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

internal fun parseSiteProps(html: String): SiteProps {
    val marker = Regex("""<script[^>]*data-page="app"[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
    val page = marker.find(html)?.groupValues?.get(1) ?: throw ApiException(ApiError.Server(0))
    val props = runCatching { apiJson.parseToJsonElement(page).jsonObject["props"]?.jsonObject }.getOrNull()
        ?: throw ApiException(ApiError.Server(0))
    val cities = props["cities"]?.jsonArray?.mapNotNull { element ->
        runCatching { apiJson.decodeFromJsonElement(City.serializer(), element) }.getOrNull()
    }.orEmpty()
    return SiteProps(cities = cities)
}

@Serializable
internal data class DataEnvelope<T>(val data: T)

@Serializable
data class SiteProps(
    val cities: List<City>,
)
