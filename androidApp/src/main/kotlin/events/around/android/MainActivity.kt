package events.around.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import events.around.android.events.EventDetailScreen
import events.around.android.events.MyEventsScreen
import events.around.android.home.HomeScreen
import events.around.android.login.LoginScreen
import events.around.android.login.TelegramLoginWebView
import events.around.android.profile.OrganizerEditScreen
import events.around.android.profile.ProfileScreen
import events.around.android.settings.SettingsScreen
import events.around.android.submission.SubmissionFormScreen
import events.around.android.ui.strings.LocalAppLocale
import events.around.android.ui.strings.LocalAroundStrings
import events.around.android.ui.strings.languageTagFor
import events.around.android.ui.strings.stringsFor
import events.around.android.ui.theme.AroundTheme
import events.around.shared.presentation.events.EventDetailViewModel
import events.around.shared.presentation.events.MyEventsViewModel
import events.around.shared.presentation.organizer.OrganizerViewModel
import events.around.shared.presentation.session.SessionViewModel
import events.around.shared.presentation.settings.SettingsViewModel
import events.around.shared.presentation.submission.FormKind
import events.around.shared.presentation.submission.FormMode
import events.around.shared.presentation.submission.SubmissionFormViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            val settings by settingsViewModel.state.collectAsStateWithLifecycle()
            val sessionViewModel = koinViewModel<SessionViewModel>()
            val session by sessionViewModel.state.collectAsStateWithLifecycle()
            val languageTag = languageTagFor(settings.language)

            AroundTheme(themeMode = settings.theme) {
                CompositionLocalProvider(
                    LocalAroundStrings provides stringsFor(languageTag),
                    LocalAppLocale provides Locale.forLanguageTag(languageTag),
                ) {
                    // A new back stack per account state: signing out must not leave the cabinet underneath.
                    key(session.isSignedIn) {
                        val backStack = rememberNavBackStack(Home)
                        AroundNavigation(backStack, sessionViewModel, languageTag)
                    }
                }
            }
        }
    }

    @Composable
    private fun AroundNavigation(
        backStack: NavBackStack<NavKey>,
        sessionViewModel: SessionViewModel,
        languageTag: String,
    ) {
        val session by sessionViewModel.state.collectAsStateWithLifecycle()
        val navigateBack: () -> Unit = {
            if (backStack.size > 1) backStack.removeLastOrNull() else finish()
        }
        NavDisplay(
            backStack = backStack,
            onBack = navigateBack,
            entryProvider = entryProvider {
                entry<Home> {
                    if (session.isSignedIn) {
                        HomeScreen(
                            onAddEvent = { backStack.add(Submission(FormKind.New)) },
                            onOpenSettings = { backStack.add(Settings) },
                            // Activity-scoped view models: the list and the page survive opening an event.
                            eventsContent = { contentModifier ->
                                MyEventsScreen(
                                    viewModel = koinViewModel<MyEventsViewModel>(),
                                    onOpenEvent = { backStack.add(EventDetail(it)) },
                                    onAddEvent = { backStack.add(Submission(FormKind.New)) },
                                    modifier = contentModifier,
                                )
                            },
                            profileContent = { contentModifier ->
                                ProfileScreen(
                                    session = session,
                                    viewModel = koinViewModel<OrganizerViewModel>(),
                                    onEditPage = { backStack.add(OrganizerEdit) },
                                    modifier = contentModifier,
                                )
                            },
                        )
                    } else {
                        LoginScreen(
                            state = session,
                            onSignIn = { backStack.add(TelegramLogin) },
                            onRetry = sessionViewModel::refreshSite,
                        )
                    }
                }
                entry<TelegramLogin> {
                    // The sign-in button only shows once the bot id is known, so the URL is there.
                    sessionViewModel.loginUrl(languageTag)?.let { url ->
                        TelegramLoginWebView(
                            loginUrl = url,
                            isReturn = sessionViewModel::isLoginReturn,
                            onReturn = { returned ->
                                sessionViewModel.onLoginReturn(returned)
                                navigateBack()
                            },
                            onClose = navigateBack,
                        )
                    }
                }
                entry<Settings> {
                    SettingsScreen(
                        navigateBack = navigateBack,
                        onSignOut = sessionViewModel::signOut,
                    )
                }
                entry<EventDetail> { route ->
                    // Keyed by event: each event gets its own view model.
                    val viewModel = koinViewModel<EventDetailViewModel>(key = "event-${route.eventId}") {
                        parametersOf(route.eventId)
                    }
                    EventDetailScreen(
                        viewModel = viewModel,
                        navigateBack = navigateBack,
                        onEdit = { backStack.add(Submission(FormKind.Edit, it)) },
                        onRepeat = { backStack.add(Submission(FormKind.Repeat, it)) },
                        onFix = { backStack.add(Submission(FormKind.Fix, it)) },
                    )
                }
                entry<Submission> { route ->
                    val viewModel = koinViewModel<SubmissionFormViewModel>(
                        key = "form-${route.kind}-${route.eventId}-${backStack.size}",
                    ) {
                        parametersOf(FormMode(route.kind, route.eventId))
                    }
                    SubmissionFormScreen(viewModel = viewModel, navigateBack = navigateBack)
                }
                entry<OrganizerEdit> {
                    OrganizerEditScreen(viewModel = koinViewModel<OrganizerViewModel>(), navigateBack = navigateBack)
                }
            },
        )
    }
}
