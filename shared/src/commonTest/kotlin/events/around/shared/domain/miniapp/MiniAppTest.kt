package events.around.shared.domain.miniapp

import events.around.core.model.AppLanguage
import events.around.core.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class MiniAppTest {
    @Test
    fun `the form opens on the city with the app's language and theme`() {
        assertEquals(
            "https://around.test/krakow?lang=uk&theme=dark",
            MiniApp.url("https://around.test/", MiniAppScreen.Submit, "krakow", AppLanguage.Ukrainian, ThemeMode.Dark),
        )
    }

    @Test
    fun `the system language is left to the mini-app and my events go to me`() {
        assertEquals(
            "https://around.test/me?theme=system",
            MiniApp.url("https://around.test", MiniAppScreen.MyEvents, "krakow", AppLanguage.System, ThemeMode.System),
        )
    }
}
