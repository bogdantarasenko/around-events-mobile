package events.around.shared.data.network

import events.around.core.model.City
import events.around.core.model.EventStatus
import events.around.shared.AppConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AroundApiTest {
    private val requests = mutableListOf<HttpRequestData>()

    private fun api(
        authorization: String? = "tglogin sig",
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData,
    ): AroundApi {
        val engine = MockEngine { request ->
            requests += request
            handler(request)
        }
        val config = AppConfig(baseUrl = "https://around.test", versionName = "1", isDebug = false)
        return AroundApi(createHttpClient(engine, config) { authorization })
    }

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun `my events are read from the data envelope with the signature attached`() = runTest {
        val api = api {
            json(
                """{"data":[{"id":3,"status":"published","title":"T","city":{"slug":"krakow","name":"Kraków"},""" +
                    """"posts":[{"target":"telegram","url":"https://t.me/c/1"}],"future_field":1}]}""",
            )
        }

        val events = api.myEvents()

        assertEquals(EventStatus.Published, events.single().status)
        assertEquals("https://t.me/c/1", events.single().telegramPost?.url)
        assertEquals("https://around.test/api/me/events", requests.single().url.toString())
        assertEquals("tglogin sig", requests.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `submission is multipart with the poster file`() = runTest {
        val api = api { json("""{"event_id":9,"status":"review","message":"ok","post_url":null}""") }

        val receipt = api.submit(
            listOf("title" to "T", "languages[]" to "uk"),
            PosterUpload(byteArrayOf(1, 2, 3), "poster.jpg", "image/jpeg"),
        )

        assertEquals("review", receipt.status)
        val body = requests.single().body as OutgoingContent
        assertTrue(body.contentType.toString().startsWith("multipart/form-data"))
    }

    @Test
    fun `errors keep the server's words and field`() {
        assertEquals(
            ApiError.Rejected("Too far", "repeat_until"),
            errorOf(422, """{"message":"The given data was invalid","errors":{"repeat_until":["Too far"]}}"""),
        )
        assertEquals(
            ApiError.Rejected("Already in the listing", "start_date"),
            errorOf(422, """{"message":"Already in the listing","field":"start_date"}"""),
        )
        assertEquals(ApiError.Conflict("Already published"), errorOf(409, """{"message":"Already published"}"""))
        assertEquals(ApiError.Unauthorized, errorOf(401, "<html>"))
        assertEquals(ApiError.TooMany, errorOf(429, ""))
        assertEquals(ApiError.Server(500), errorOf(500, ""))
    }

    @Test
    fun `failed request throws the mapped error`() = runTest {
        val api = api { json("""{"message":"Can't cancel"}""", HttpStatusCode.Conflict) }

        val error = assertFailsWith<ApiException> { api.cancelEvent(1) }.error

        assertEquals(ApiError.Conflict("Can't cancel"), error)
    }

    @Test
    fun `no organizer page reads as null`() = runTest {
        assertNull(api { json("""{"data":null}""") }.organizer())
    }

    @Test
    fun `cities and bot id come from the page props`() {
        val html = """<html><body><script data-page="app" type="application/json">""" +
            """{"component":"mini-app","props":{"cities":[{"slug":"krakow","name":"Kraków"}],"botId":9161757426}}""" +
            """</script><div id="app"></div></body></html>"""

        assertEquals(SiteProps(listOf(City("krakow", "Kraków")), 9161757426), parseSiteProps(html))
        assertFailsWith<ApiException> { parseSiteProps("<html></html>") }
    }
}
