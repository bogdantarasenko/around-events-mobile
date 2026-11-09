package events.around.shared.di

import events.around.shared.AppConfig
import events.around.shared.data.auth.SessionRepository
import events.around.shared.data.auth.SignedInCalls
import events.around.shared.data.events.MyEventsRepository
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.AuthorizationProvider
import events.around.shared.data.network.createHttpClient
import events.around.shared.data.organizer.OrganizerRepository
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.data.site.SiteRepository
import events.around.shared.data.submission.SubmissionRepository
import events.around.shared.presentation.events.EventDetailViewModel
import events.around.shared.presentation.events.MyEventsViewModel
import events.around.shared.presentation.organizer.OrganizerViewModel
import events.around.shared.presentation.session.SessionViewModel
import events.around.shared.presentation.settings.SettingsViewModel
import events.around.shared.presentation.submission.FormMode
import events.around.shared.presentation.submission.SubmissionFormViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

fun initKoin(
    config: AppConfig,
    appDeclaration: KoinApplication.() -> Unit = {},
): KoinApplication = startKoin {
    appDeclaration()
    modules(
        module { single { config } },
        platformModule,
        commonModule,
    )
}

internal val commonModule = module {
    single<Clock> { Clock.System }
    single { SettingsRepository(settings = get()) }
    single { SessionRepository(settings = get(), clock = get()) } bind AuthorizationProvider::class
    single { SignedInCalls(session = get()) }

    single { createHttpClient(engine = get(), config = get(), authorization = get()) }
    single { AroundApi(client = get()) }

    single { SiteRepository(api = get(), settings = get()) }
    single { MyEventsRepository(api = get(), calls = get(), settings = get()) }
    single { SubmissionRepository(api = get(), calls = get(), settings = get()) }
    single { OrganizerRepository(api = get(), calls = get()) }

    viewModelOf(::SettingsViewModel)
    viewModelOf(::SessionViewModel)
    viewModelOf(::MyEventsViewModel)
    viewModelOf(::OrganizerViewModel)
    viewModel { (eventId: Long) -> EventDetailViewModel(eventId, get(), get()) }
    viewModel { (mode: FormMode) -> SubmissionFormViewModel(mode, get(), get(), get(), get()) }
}

/** Provides `Settings` and `HttpClientEngine` for the platform. */
internal expect val platformModule: Module
