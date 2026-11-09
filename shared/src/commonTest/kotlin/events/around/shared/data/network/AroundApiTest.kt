package events.around.shared.data.network

import events.around.core.model.City
import events.around.shared.AppConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AroundApiTest {
    private val requests = mutableListOf<HttpRequestData>()

    private fun api(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): AroundApi {
        val engine = MockEngine { request ->
            requests += request
            handler(request)
        }
        val config = AppConfig(baseUrl = "https://around.test", versionName = "1", isDebug = false)
        return AroundApi(createHttpClient(engine, config))
    }

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun `city events are read from the data envelope`() = runTest {
        val api = api {
            json(
                """{"data":[{"id":3,"title":"T","city":{"slug":"warsaw","name":"Warszawa"},""" +
                    """"start_date":"2026-12-01","price_from":"34.00","currency":"PLN","category":"games",""" +
                    """"organizer":{"slug":"rij","name":"Рій","url":"https://around.test/o/rij"},"future_field":1}]}""",
            )
        }

        val event = api.cityEvents("warsaw").single()

        assertEquals("games", event.category)
        assertEquals("PLN", event.currency)
        assertEquals("Рій", event.organizer?.name)
        assertNull(event.postUrl)
        assertEquals("https://around.test/api/cities/warsaw/events", requests.single().url.toString())
    }

    @Test
    fun `failed request throws the mapped error`() = runTest {
        val api = api { json("""{"message":"Not Found"}""", HttpStatusCode.NotFound) }

        assertEquals(ApiError.NotFound, assertFailsWith<ApiException> { api.cityEvents("atlantis") }.error)
        assertEquals(ApiError.TooMany, errorOf(429))
        assertEquals(ApiError.Server(500), errorOf(500))
    }

    @Test
    fun `no answer reads as offline`() = runTest {
        val api = api { throw IllegalStateException("no route") }

        assertEquals(ApiError.Offline, assertFailsWith<ApiException> { api.cityEvents("warsaw") }.error)
    }

    @Test
    fun `cities come from the page props`() {
        val html = """<html><body><script data-page="app" type="application/json">""" +
            """{"component":"mini-app","props":{"cities":[{"slug":"krakow","name":"Kraków"}],"botId":9161757426}}""" +
            """</script><div id="app"></div></body></html>"""

        assertEquals(SiteProps(listOf(City("krakow", "Kraków"))), parseSiteProps(html))
        assertFailsWith<ApiException> { parseSiteProps("<html></html>") }
    }
}
