package events.around.shared.di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module
import java.util.prefs.Preferences

internal actual val platformModule = module {
    single<Settings> { PreferencesSettings(Preferences.userRoot().node("events.around")) }
    single<HttpClientEngine> { OkHttp.create() }
}
