package events.around.shared.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

internal actual val platformModule = module {
    single<Settings> {
        SharedPreferencesSettings(get<Context>().getSharedPreferences("around", Context.MODE_PRIVATE))
    }
    single<HttpClientEngine> { OkHttp.create() }
}
