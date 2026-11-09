package events.around.shared.data.network

import events.around.core.model.City
import events.around.core.model.MyEvent
import events.around.core.model.Organizer
import events.around.core.model.OrganizerLinks
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * The endpoints the app uses — all of them exist in `around-events` already; see `routes/web.php`.
 * Every call throws [ApiException] on failure.
 */
class AroundApi(
    private val client: HttpClient,
) {
    suspend fun myEvents(): List<MyEvent> =
        call { client.get("api/me/events") }.body<DataEnvelope<List<MyEvent>>>().data

    suspend fun submit(parts: List<Pair<String, String>>, poster: PosterUpload?): SubmissionReceipt =
        call { client.submitFormWithBinaryData("api/submissions", multipart(parts, poster)) }.body()

    suspend fun updateEvent(id: Long, parts: List<Pair<String, String>>, poster: PosterUpload?): MyEvent =
        call { client.submitFormWithBinaryData("api/me/events/$id", multipart(parts, poster)) }
            .body<DataEnvelope<MyEvent>>().data

    suspend fun cancelEvent(id: Long): MyEvent =
        call { client.post("api/me/events/$id/cancel") }.body<DataEnvelope<MyEvent>>().data

    suspend fun organizer(): Organizer? =
        call { client.get("api/me/organizer") }.body<DataEnvelope<Organizer?>>().data

    suspend fun saveOrganizer(input: OrganizerInput): Organizer =
        call {
            client.post("api/me/organizer") {
                contentType(ContentType.Application.Json)
                setBody(input)
            }
        }.body<DataEnvelope<Organizer>>().data

    /**
     * Cities and the bot id. There is no JSON endpoint for them: the form page embeds them as Inertia props in
     * `<script data-page="app" type="application/json">`, so the app reads that page like a browser would.
     */
    suspend fun siteProps(): SiteProps =
        parseSiteProps(call { client.get("") { headers[HttpHeaders.Accept] = "text/html" } }.bodyAsText())

    private fun multipart(parts: List<Pair<String, String>>, poster: PosterUpload?) = formData {
        parts.forEach { (key, value) -> append(key, value) }
        poster?.let {
            append(
                "poster",
                it.bytes,
                Headers.build {
                    append(HttpHeaders.ContentType, it.mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"${it.fileName}\"")
                },
            )
        }
    }

    private suspend fun call(request: suspend () -> HttpResponse): HttpResponse {
        val response = try {
            request()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            throw ApiException(ApiError.Offline)
        }
        if (response.status.isSuccess()) return response
        throw ApiException(errorOf(response.status.value, runCatching { response.bodyAsText() }.getOrDefault("")))
    }
}

internal fun errorOf(status: Int, body: String): ApiError {
    val payload = runCatching { apiJson.parseToJsonElement(body).jsonObject }.getOrNull()
    val message = (payload?.get("message") as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
    return when (status) {
        401 -> ApiError.Unauthorized
        404 -> ApiError.NotFound
        409 -> ApiError.Conflict(message)
        422 -> {
            // Field validation lists messages under `errors`; a rejection on the merits puts one `field` next to
            // `message`.
            val errors = payload?.get("errors") as? JsonObject
            val firstField = errors?.keys?.firstOrNull()
            val firstError = firstField?.let { (errors[it] as? JsonArray)?.firstOrNull()?.jsonPrimitive?.contentOrNull }
            val field = firstField ?: (payload?.get("field") as? JsonPrimitive)?.contentOrNull
            ApiError.Rejected(firstError ?: message, field)
        }
        429 -> ApiError.TooMany
        else -> ApiError.Server(status)
    }
}

internal fun parseSiteProps(html: String): SiteProps {
    val marker = Regex("""<script[^>]*data-page="app"[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
    val page = marker.find(html)?.groupValues?.get(1) ?: throw ApiException(ApiError.Server(0))
    val props = runCatching { apiJson.parseToJsonElement(page).jsonObject["props"]?.jsonObject }.getOrNull()
        ?: throw ApiException(ApiError.Server(0))
    val cities = props["cities"]?.jsonArray?.mapNotNull { element ->
        runCatching { apiJson.decodeFromJsonElement(City.serializer(), element) }.getOrNull()
    }.orEmpty()
    val botId = (props["botId"] as? JsonPrimitive)?.longOrNull
    return SiteProps(cities = cities, botId = botId)
}

@Serializable
internal data class DataEnvelope<T>(val data: T)

/** What `POST /api/submissions` answers. */
@Serializable
data class SubmissionReceipt(
    @SerialName("event_id") val eventId: Long? = null,
    /** `review`, `published`, `duplicate`: what the backend did with it. */
    val status: String,
    val message: String? = null,
    /** For a duplicate: the post of the event it matched. */
    @SerialName("post_url") val postUrl: String? = null,
)

@Serializable
data class SiteProps(
    val cities: List<City>,
    val botId: Long?,
)

@Serializable
data class OrganizerInput(
    val name: String,
    val slug: String?,
    val bio: String?,
    val links: OrganizerLinks,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

/** A poster picked on the device. */
class PosterUpload(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
)
