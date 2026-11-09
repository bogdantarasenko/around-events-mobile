package events.around.shared.di

import events.around.shared.AppConfig
import events.around.shared.data.feed.FeedRepository
import events.around.shared.data.network.AroundApi
import events.around.shared.data.network.createHttpClient
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.data.site.SiteRepository
import events.around.shared.presentation.feed.EventDetailViewModel
import events.around.shared.presentation.feed.FeedViewModel
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
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

    single { createHttpClient(engine = get(), config = get()) }
    single { AroundApi(client = get()) }

    single { SiteRepository(api = get(), settings = get()) }
    single { FeedRepository(api = get(), settings = get()) }

    viewModelOf(::SettingsViewModel)
    viewModelOf(::FeedViewModel)
    viewModel { (eventId: Long) -> EventDetailViewModel(eventId, get(), get()) }
}

/** Provides `Settings` and `HttpClientEngine` for the platform. */
internal expect val platformModule: Module
