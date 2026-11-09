package events.around.shared.presentation.settings

import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import events.around.shared.AppConfig
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.createHttpClient
import events.around.shared.data.settings.SettingsRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val config = AppConfig(baseUrl = "https://example.test", versionName = "1.2.3", isDebug = false)

    // Settings never call the network; the engine only has to exist.
    private fun site(settings: MapSettings) = CitiesRepository(
        AroundApi(createHttpClient(MockEngine { respondError(HttpStatusCode.NotFound) }, config)),
        SettingsRepository(settings),
        settings,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts from stored preferences`() = runTest(dispatcher) {
        val settings = MapSettings("theme" to "Dark", "language" to "Belarusian")
        val viewModel = SettingsViewModel(SettingsRepository(settings), site(settings), config)

        with(viewModel.state.value) {
            assertEquals(ThemeMode.Dark, theme)
            assertEquals(AppLanguage.Belarusian, language)
            assertEquals("1.2.3", versionName)
        }
    }

    @Test
    fun `unknown stored value falls back to system`() = runTest(dispatcher) {
        val settings = MapSettings("theme" to "Sepia")
        val viewModel = SettingsViewModel(SettingsRepository(settings), site(settings), config)

        assertEquals(ThemeMode.System, viewModel.state.value.theme)
    }

    @Test
    fun `selection is saved and emitted`() = runTest(dispatcher) {
        val settings = MapSettings()
        val viewModel = SettingsViewModel(SettingsRepository(settings), site(settings), config)

        viewModel.state.test {
            assertEquals(ThemeMode.System, awaitItem().theme)

            viewModel.onThemeSelected(ThemeMode.Light)
            assertEquals(ThemeMode.Light, awaitItem().theme)

            viewModel.onLanguageSelected(AppLanguage.Ukrainian)
            assertEquals(AppLanguage.Ukrainian, awaitItem().language)
        }
        assertEquals("Light", settings.getStringOrNull("theme"))
        assertEquals("Ukrainian", settings.getStringOrNull("language"))
    }
}
