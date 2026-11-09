package events.around.shared.data.auth

import com.russhwolf.settings.Settings
import events.around.shared.data.network.AuthorizationProvider
import events.around.shared.data.network.apiJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

/**
 * Who is signed in. There is no server session: every request carries the Telegram signature, which the server
 * accepts for a day. After that the person signs in again.
 */
class SessionRepository(
    private val settings: Settings,
    private val clock: Clock = Clock.System,
) : AuthorizationProvider {

    private val mutableSession = MutableStateFlow(initialSession())
    val session: StateFlow<Session> = mutableSession.asStateFlow()

    fun signIn(login: TelegramLogin) {
        settings.putString(LOGIN_KEY, apiJson.encodeToString(TelegramLogin.serializer(), login))
        mutableSession.value = Session.SignedIn(login)
    }

    fun signOut() {
        settings.remove(LOGIN_KEY)
        mutableSession.value = Session.SignedOut(expired = false)
    }

    /** The server refused the signature (401): it expired or the bot token changed. */
    fun onRejected() {
        settings.remove(LOGIN_KEY)
        mutableSession.value = Session.SignedOut(expired = true)
    }

    override fun authorization(): String? {
        val login = (mutableSession.value as? Session.SignedIn)?.login ?: return null
        if (login.isExpired(clock.now().epochSeconds)) {
            onRejected()
            return null
        }
        return "tglogin ${login.signature}"
    }

    private fun initialSession(): Session {
        val login = settings.getStringOrNull(LOGIN_KEY)
            ?.let { runCatching { apiJson.decodeFromString(TelegramLogin.serializer(), it) }.getOrNull() }
        return when {
            login == null -> Session.SignedOut(expired = false)
            login.isExpired(clock.now().epochSeconds) -> {
                settings.remove(LOGIN_KEY)
                Session.SignedOut(expired = true)
            }
            else -> Session.SignedIn(login)
        }
    }

    private companion object {
        const val LOGIN_KEY = "telegram_login"
    }
}

sealed interface Session {
    data class SignedIn(val login: TelegramLogin) : Session

    /** [expired]: the person was signed in and the signature ran out, as opposed to never having signed in. */
    data class SignedOut(val expired: Boolean) : Session
}
