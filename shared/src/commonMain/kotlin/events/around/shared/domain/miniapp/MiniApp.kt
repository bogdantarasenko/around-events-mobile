package events.around.shared.domain.miniapp

import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

/** What the app opens in the mini-app: the submission form, or the organizer's own events. */
enum class MiniAppScreen {
    Submit,
    MyEvents,
}

/**
 * The address of the mini-app for the app's WebView. The mini-app runs there as on the open web, so the app
 * passes what it knows in the query: the language, the theme and the city of the form.
 */
object MiniApp {
    fun url(
        baseUrl: String,
        screen: MiniAppScreen,
        citySlug: String?,
        language: AppLanguage,
        theme: ThemeMode,
    ): String =
        URLBuilder(baseUrl.trimEnd('/')).apply {
            when (screen) {
                MiniAppScreen.Submit -> citySlug?.let { appendPathSegments(it) }
                MiniAppScreen.MyEvents -> appendPathSegments("me")
            }
            language.tag?.let { parameters.append("lang", it) }
            parameters.append(
                "theme",
                when (theme) {
                    ThemeMode.System -> "system"
                    ThemeMode.Light -> "light"
                    ThemeMode.Dark -> "dark"
                },
            )
        }.buildString()
}
