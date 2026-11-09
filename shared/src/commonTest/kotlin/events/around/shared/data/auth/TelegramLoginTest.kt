package events.around.shared.data.auth

import com.russhwolf.settings.MapSettings
import io.ktor.http.Url
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalEncodingApi::class)
class TelegramLoginTest {
    private val site = "https://around.events"

    private fun redirect(json: String): String =
        "$site/#tgAuthResult=" + Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(json.encodeToByteArray())

    @Test
    fun `login page carries the bot, the site origin and the return address`() {
        val url = Url(TelegramLoginPage.url(botId = 42, siteBaseUrl = "$site/", languageTag = "uk-UA"))

        assertEquals("oauth.telegram.org", url.host)
        assertEquals("42", url.parameters["bot_id"])
        assertEquals(site, url.parameters["origin"])
        assertEquals("write", url.parameters["request_access"])
        assertEquals("uk", url.parameters["lang"])
        assertEquals("$site/", url.parameters["return_to"])
    }

    @Test
    fun `result keeps every field Telegram sent, in its order, as the signature`() {
        val login = TelegramLoginPage.parseResult(
            redirect(
                """{"id":123456789,"first_name":"Ann Lee","username":"ann","photo_url":"https://t.me/i/userpic/320/a.jpg",""" +
                    """"auth_date":1794200000,"hash":"abc123"}""",
            ),
        )!!

        assertEquals(123456789, login.userId)
        assertEquals("@ann", login.displayName)
        assertEquals(1794200000, login.authDate)
        assertEquals(
            "id=123456789&first_name=Ann+Lee&username=ann&photo_url=https%3A%2F%2Ft.me%2Fi%2Fuserpic%2F320%2Fa.jpg" +
                "&auth_date=1794200000&hash=abc123",
            login.signature,
        )
    }

    @Test
    fun `return without a result, or a broken one, is not a login`() {
        assertNull(TelegramLoginPage.parseResult("$site/"))
        assertNull(TelegramLoginPage.parseResult("$site/#tgAuthResult=%%%"))
        assertNull(TelegramLoginPage.parseResult(redirect("""{"id":1,"auth_date":1}""")), "no hash")
        assertTrue(TelegramLoginPage.isReturn("$site/#tgAuthResult=x", site))
        assertFalse(TelegramLoginPage.isReturn("https://oauth.telegram.org/auth?x", site))
    }

    @Test
    fun `session signs requests until the day runs out`() {
        val clock = FixedClock(Instant.fromEpochSeconds(1_000_000))
        val sessions = SessionRepository(MapSettings(), clock)
        sessions.signIn(TelegramLogin(userId = 1, authDate = 1_000_000 - 60, signature = "id=1&hash=h"))

        assertEquals("tglogin id=1&hash=h", sessions.authorization())

        clock.now = Instant.fromEpochSeconds(1_000_000 + TelegramLogin.MAX_AGE_SECONDS)
        assertNull(sessions.authorization())
        assertEquals(Session.SignedOut(expired = true), sessions.session.value)
    }

    @Test
    fun `stored login survives a restart, an expired one does not`() {
        val settings = MapSettings()
        val clock = FixedClock(Instant.fromEpochSeconds(2_000_000))
        SessionRepository(settings, clock).signIn(TelegramLogin(userId = 7, authDate = 2_000_000, signature = "s"))

        assertIs<Session.SignedIn>(SessionRepository(settings, clock).session.value)

        clock.now = Instant.fromEpochSeconds(2_000_000 + TelegramLogin.MAX_AGE_SECONDS + 1)
        assertEquals(Session.SignedOut(expired = true), SessionRepository(settings, clock).session.value)
        assertEquals(Session.SignedOut(expired = false), SessionRepository(settings, clock).session.value)
    }
}

class FixedClock(var now: Instant) : Clock {
    override fun now(): Instant = now
}
