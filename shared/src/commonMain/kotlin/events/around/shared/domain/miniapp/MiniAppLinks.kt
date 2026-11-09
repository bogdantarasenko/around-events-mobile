package events.around.shared.domain.miniapp

import events.around.shared.AppConfig
import events.around.shared.data.city.CitiesRepository
import events.around.shared.data.settings.SettingsRepository

/** Mini-app addresses for the app's WebView, filled from what the person has chosen in the app. */
class MiniAppLinks(
    private val config: AppConfig,
    private val settings: SettingsRepository,
    private val cities: CitiesRepository,
) {
    /** The site's host: navigation inside it stays in the WebView, anything else opens in the browser. */
    val host: String = config.baseUrl.removePrefix("https://").removePrefix("http://").substringBefore('/')

    fun url(screen: MiniAppScreen): String = MiniApp.url(
        baseUrl = config.baseUrl,
        screen = screen,
        citySlug = settings.city.value ?: cities.citiesNow?.firstOrNull()?.slug,
        language = settings.language.value,
        theme = settings.theme.value,
    )
}
