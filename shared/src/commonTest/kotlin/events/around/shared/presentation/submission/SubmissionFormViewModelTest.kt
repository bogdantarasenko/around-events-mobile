package events.around.shared.presentation.submission

import com.russhwolf.settings.MapSettings
import events.around.core.model.City
import events.around.shared.AppConfig
import events.around.shared.data.auth.SessionRepository
import events.around.shared.data.auth.SignedInCalls
import events.around.shared.data.events.MyEventsRepository
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.SiteProps
import events.around.shared.data.network.apiJson
import events.around.shared.data.network.createHttpClient
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.data.site.SiteRepository
import events.around.shared.data.submission.SubmissionRepository
import events.around.shared.domain.submission.FormField
import events.around.shared.domain.submission.Pricing
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubmissionFormViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val requests = mutableListOf<HttpRequestData>()
    private var answer: Pair<HttpStatusCode, String> = HttpStatusCode.OK to """{"status":"review","event_id":1}"""

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        settings.putString(
            "site_props",
            apiJson.encodeToString(
                SiteProps.serializer(),
                SiteProps(listOf(City("krakow", "Kraków"), City("warsaw", "Warszawa")), 1),
            ),
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(mode: FormMode = FormMode(FormKind.New)): SubmissionFormViewModel {
        // On the test dispatcher, so advanceUntilIdle() also waits for the responses.
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = this@SubmissionFormViewModelTest.dispatcher
                addHandler { request ->
                    requests += request
                    val (status, body) = if (request.url.encodedPath.endsWith("/api/me/events")) {
                        HttpStatusCode.OK to """{"data":[]}"""
                    } else {
                        answer
                    }
                    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
                }
            },
        )
        val api = AroundApi(createHttpClient(engine, AppConfig("https://around.test", "1", false)) { "tglogin s" })
        val calls = SignedInCalls(SessionRepository(settings))
        return SubmissionFormViewModel(
            mode = mode,
            submissions = SubmissionRepository(api, calls, settings),
            events = MyEventsRepository(api, calls, settings),
            settings = SettingsRepository(settings),
            site = SiteRepository(api, settings),
        )
    }

    private fun SubmissionFormViewModel.fillIn() {
        setTitle("Jazz night")
        setCategory("concert")
        setStartDate("2026-12-01")
        setVenue("Klub")
        setDescription("Ten words about the evening.")
        setPricing(Pricing.Free)
        setOrganization("Fund")
        setAcceptedRules(true)
    }

    @Test
    fun `an incomplete form stops at its first problem without a request`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.setTitle("Jazz night")

        vm.submit()
        advanceUntilIdle()

        assertEquals(FormField.Category, vm.state.value.problem)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun `a sent submission clears the draft and remembers the organization`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillIn()
        assertNotNull(settings.getStringOrNull("submission_draft"))

        vm.submit()
        advanceUntilIdle()

        assertEquals("review", vm.state.value.result?.receipt?.status)
        assertNull(settings.getStringOrNull("submission_draft"))
        assertEquals("Fund", settings.getStringOrNull("default_organization"))
        assertEquals("krakow", settings.getStringOrNull("default_city"))
    }

    @Test
    fun `the draft comes back next time`() = runTest(dispatcher) {
        viewModel().setTitle("Unfinished")

        val restored = viewModel()

        assertEquals("Unfinished", restored.state.value.form.title)
        assertTrue(restored.state.value.restoredDraft)
    }

    @Test
    fun `a server rejection lands on its field`() = runTest(dispatcher) {
        answer = HttpStatusCode.UnprocessableEntity to """{"message":"Too far","errors":{"repeat_until":["Too far"]}}"""
        val vm = viewModel()
        vm.fillIn()

        vm.submit()
        advanceUntilIdle()

        assertEquals(FormField.RepeatUntil, vm.state.value.problem)
        assertEquals("Too far", vm.state.value.serverMessage)
        assertNull(vm.state.value.result)
    }

    @Test
    fun `the city defaults to the last one used`() = runTest(dispatcher) {
        settings.putString("default_city", "warsaw")

        assertEquals("warsaw", viewModel().state.value.form.city)
    }
}
