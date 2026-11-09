package events.around.shared.di

import events.around.shared.AppConfig
import events.around.shared.data.network.AuthorizationProvider
import events.around.shared.data.network.createHttpClient
import events.around.shared.data.settings.SettingsRepository
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

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
    single { SettingsRepository(settings = get()) }

    // Replaced by the session store once sign-in exists.
    single<AuthorizationProvider> { AuthorizationProvider { null } }

    single { createHttpClient(engine = get(), config = get(), authorization = get()) }

    viewModelOf(::SettingsViewModel)
}

/** Provides `Settings` and `HttpClientEngine` for the platform. */
internal expect val platformModule: Module
