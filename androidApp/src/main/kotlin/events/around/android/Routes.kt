package events.around.android

import androidx.navigation3.runtime.NavKey
import events.around.shared.domain.miniapp.MiniAppScreen
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object MapRoute : NavKey

@Serializable
data object Search : NavKey

@Serializable
data class EventDetail(val eventId: Long) : NavKey

/** The city's listing; with a category, that category's page. */
@Serializable
data class Events(val category: String? = null) : NavKey

@Serializable
data class OrganizerPage(val slug: String) : NavKey

@Serializable
data object Cities : NavKey

/** A city's page, which may not be the city the app reads. */
@Serializable
data class CityPage(val slug: String) : NavKey

/** The around.events mini-app in a WebView. */
@Serializable
data class MiniApp(val screen: MiniAppScreen) : NavKey
