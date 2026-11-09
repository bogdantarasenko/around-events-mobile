package events.around.shared.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogWriter
import events.around.i18n.AroundStrings
import events.around.i18n.aroundStringsFor
import events.around.shared.AppConfig
import events.around.shared.presentation.settings.SettingsViewModel
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private lateinit var koin: Koin

/** Called once from the SwiftUI `App` initializer. */
fun startAround(baseUrl: String, versionName: String, isDebug: Boolean) {
    Logger.setLogWriters(NSLogWriter())
    koin = initKoin(AppConfig(baseUrl = baseUrl, versionName = versionName, isDebug = isDebug)).koin
}

/** Entry points for Swift, which cannot call Koin's reified functions. */
object Deps : KoinComponent {
    override fun getKoin(): Koin = koin

    fun getSettingsViewModel(): SettingsViewModel = get()

    fun strings(languageTag: String): AroundStrings = aroundStringsFor(languageTag)
}
