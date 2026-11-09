package events.around.shared.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import events.around.shared.AppConfig
import events.around.shared.data.auth.Session
import events.around.shared.data.auth.SessionRepository
import events.around.shared.data.auth.TelegramLoginPage
import events.around.shared.data.events.MyEventsRepository
import events.around.shared.data.site.SiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Signed in or not, and the way in: Telegram's login page opened in a web view. */
class SessionViewModel(
    private val session: SessionRepository,
    private val site: SiteRepository,
    private val events: MyEventsRepository,
    private val config: AppConfig,
) : ViewModel() {

    private val siteLoading = MutableStateFlow(false)
    private val siteFailed = MutableStateFlow(false)

    val state: StateFlow<SessionUiState> = combine(
        session.session,
        site.props,
        siteLoading,
        siteFailed,
    ) { current, props, loading, failed ->
        val login = (current as? Session.SignedIn)?.login
        SessionUiState(
            isSignedIn = login != null,
            displayName = login?.displayName,
            photoUrl = login?.photoUrl,
            isExpired = (current as? Session.SignedOut)?.expired == true,
            canSignIn = props?.botId != null,
            isPreparing = loading && props?.botId == null,
            preparationFailed = failed && props?.botId == null,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SessionUiState())

    init {
        refreshSite()
    }

    fun refreshSite() {
        viewModelScope.launch {
            siteLoading.value = true
            siteFailed.value = false
            try {
                site.refresh()
            } catch (exception: Exception) {
                Logger.withTag("session").w(exception) { "Couldn't read the site props" }
                siteFailed.value = true
            } finally {
                siteLoading.value = false
            }
        }
    }

    /** Null until the bot id is known. */
    fun loginUrl(languageTag: String): String? =
        site.props.value?.botId?.let { TelegramLoginPage.url(it, config.baseUrl, languageTag) }

    /** True when the web view reached the return page; it should close then, signed in or not. */
    fun isLoginReturn(url: String): Boolean = TelegramLoginPage.isReturn(url, config.baseUrl)

    /** Returns true when [url] carried a login and the person is now signed in. */
    fun onLoginReturn(url: String): Boolean {
        val login = TelegramLoginPage.parseResult(url) ?: return false
        events.clear()
        session.signIn(login)
        return true
    }

    fun signOut() {
        session.signOut()
        events.clear()
    }
}

data class SessionUiState(
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val photoUrl: String? = null,
    /** Was signed in, and the day-long signature ran out. */
    val isExpired: Boolean = false,
    /** The bot id is known, so the login page can open. */
    val canSignIn: Boolean = false,
    val isPreparing: Boolean = false,
    val preparationFailed: Boolean = false,
)
