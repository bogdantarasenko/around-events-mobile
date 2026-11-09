package events.around.shared.presentation.feed

import com.russhwolf.settings.MapSettings
import events.around.core.model.City
import events.around.shared.AppConfig
import events.around.shared.data.feed.FeedRepository
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.SiteProps
import events.around.shared.data.network.apiJson
import events.around.shared.data.network.createHttpClient
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.data.site.SiteRepository
import events.around.shared.presentation.ErrorKind
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class FeedViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val requested = mutableListOf<String>()
    private var online = true

    // 2026-11-10 12:00 UTC; the events below are days away, so the device time zone doesn't move them.
    private val clock = object : Clock {
        override fun now() = Instant.fromEpochSeconds(1_794_312_000)
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        settings.putString(
            "site_props",
            apiJson.encodeToString(
                SiteProps.serializer(),
                SiteProps(listOf(City("krakow", "Kraków"), City("warsaw", "Warszawa"))),
            ),
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun listing(slug: String, title: String) =
        """{"data":[{"id":1,"title":"$title","city":{"slug":"$slug","name":"$slug"},"start_date":"2026-11-20"}]}"""

    private fun viewModel(): FeedViewModel {
        // On the test dispatcher, so advanceUntilIdle() also waits for the responses.
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = this@FeedViewModelTest.dispatcher
                addHandler { request ->
                    if (!online) error("offline")
                    val path = request.url.encodedPath
                    requested += path
                    val body = when {
                        path.endsWith("/krakow/events") -> listing("krakow", "Kraków event")
                        path.endsWith("/warsaw/events") -> listing("warsaw", "Warsaw event")
                        else -> SITE_PAGE
                    }
                    respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            },
        )
        val api = AroundApi(createHttpClient(engine, AppConfig("https://around.test", "1", false)))
        return FeedViewModel(
            FeedRepository(api, settings),
            SiteRepository(api, settings),
            SettingsRepository(settings),
            clock,
        )
    }

    @Test
    fun `opens the first city when none was picked`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertEquals("krakow", city?.slug)
            assertEquals("Kraków event", days.single().events.single().title)
            assertEquals("2026-11-20", days.single().date)
        }
    }

    @Test
    fun `picking a city stores it and loads its listing`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectCity("warsaw")
        advanceUntilIdle()

        assertEquals("Warsaw event", viewModel.state.value.days.single().events.single().title)
        assertEquals("warsaw", settings.getStringOrNull("city"))
        assertEquals(
            listOf("/api/cities/krakow/events", "/api/cities/warsaw/events"),
            requested.filter { "cities" in it },
        )
    }

    @Test
    fun `offline shows the cached listing with the error`() = runTest(dispatcher) {
        viewModel()
        advanceUntilIdle()

        online = false
        val offline = viewModel()
        advanceUntilIdle()

        with(offline.state.value) {
            assertEquals("Kraków event", days.single().events.single().title)
            assertEquals(ErrorKind.Offline, error?.kind)
        }
    }

    @Test
    fun `offline with nothing cached is an error, not a spinner`() = runTest(dispatcher) {
        online = false
        val viewModel = viewModel()
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertEquals(false, isLoading)
            assertEquals(ErrorKind.Offline, error?.kind)
        }
    }

    private companion object {
        const val SITE_PAGE = """<script data-page="app">{"props":{"cities":[{"slug":"krakow","name":"Kraków"},""" +
            """{"slug":"warsaw","name":"Warszawa"}]}}</script>"""
    }
}
