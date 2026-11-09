package events.around.shared.presentation.feed

import com.russhwolf.settings.MapSettings
import events.around.core.model.City
import events.around.shared.AppConfig
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.feed.FeedRepository
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.apiJson
import events.around.shared.data.network.createHttpClient
import events.around.shared.data.organizers.OrganizersRepository
import events.around.shared.data.settings.SettingsRepository
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
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DiscoverViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val requested = mutableListOf<String>()
    private var online = true

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        settings.putString(
            "cities",
            apiJson.encodeToString(
                ListSerializer(City.serializer()),
                listOf(City("krakow", "Kraków"), City("warsaw", "Warszawa")),
            ),
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun listing(slug: String, category: String) =
        """{"data":[{"id":1,"title":"T","city":{"slug":"$slug","name":"$slug"},"start_date":"2026-11-20",""" +
            """"category":"$category"}]}"""

    private fun viewModel(): DiscoverViewModel {
        // On the test dispatcher, so advanceUntilIdle() also waits for the responses.
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = this@DiscoverViewModelTest.dispatcher
                addHandler { request ->
                    if (!online) error("offline")
                    val path = request.url.encodedPath
                    requested += path
                    val body = when {
                        path.endsWith("/krakow/events") -> listing("krakow", "music")
                        path.endsWith("/warsaw/events") -> listing("warsaw", "sport")
                        path.endsWith("/krakow/organizers") -> ORGANIZERS
                        path.endsWith("/organizers") -> """{"data":[]}"""
                        else -> CITIES
                    }
                    respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            },
        )
        val api = AroundApi(createHttpClient(engine, AppConfig("https://around.test", "1", false)))
        return DiscoverViewModel(
            CitiesRepository(api, SettingsRepository(settings), settings),
            FeedRepository(api, settings),
            OrganizersRepository(api, settings),
        )
    }

    @Test
    fun `opens the first city when none was picked`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertEquals("krakow", city?.slug)
            assertEquals("Дом", organizers.single().name)
            assertEquals(false, isLoading)
        }
    }

    @Test
    fun `picking a city stores it and loads its listing`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectCity("warsaw")
        advanceUntilIdle()

        assertEquals(emptyList(), viewModel.state.value.organizers)
        assertEquals("warsaw", settings.getStringOrNull("city"))
        assertEquals(
            listOf("/api/cities/krakow/events", "/api/cities/warsaw/events"),
            requested.filter { it.endsWith("/events") },
        )
        assertEquals(
            listOf("/api/cities/krakow/organizers", "/api/cities/warsaw/organizers"),
            requested.filter { it.endsWith("/organizers") },
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
            assertEquals("Дом", organizers.single().name)
            assertEquals(false, isLoading)
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
        const val CITIES = """{"data":[{"slug":"krakow","name":"Kraków"},{"slug":"warsaw","name":"Warszawa"}]}"""
        const val ORGANIZERS = """{"data":[{"slug":"dom","name":"Дом","url":"https://around.test/o/dom"}]}"""
    }
}
